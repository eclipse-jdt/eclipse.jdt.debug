/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.util.NLS;

/**
 * Lookup for VMs installed in standard or usual locations; and add the existing ones that
 * are not yet known by JDT to the VM registry (usually visible in the "Installed JREs"
 * preference page)
 */
public class DetectVMInstallationsJob extends Job {

	/**
	 * CI is a common variable defined in CI/CDI servers like Jenkins, Gitlab, Github, ... to indicate it is a CI environment
	 */
	private static final String ENV_CI = "CI"; //$NON-NLS-1$
	/**
	 * Property that can be defined to control general behavior
	 * <ul>
	 * <li><code>DetectVMInstallationsJob.disabled = true</code> - automatic discovery is always disabled</li>
	 * <li><code>DetectVMInstallationsJob.disabled = false</code> - check runs everywhere depending on preferences</li>
	 * <li><code>DetectVMInstallationsJob.disabled</code> not specified - automatic discovery is disabled if environment variable <code>CI</code> has
	 * value <code>true</code> otherwise runs depending on preferences</li>
	 * </pre>
	 */
	private static final String PROPERTY_DETECT_VM_INSTALLATIONS_JOB_DISABLED = "DetectVMInstallationsJob.disabled"; //$NON-NLS-1$
	private static final Object FAMILY = DetectVMInstallationsJob.class;

	public DetectVMInstallationsJob() {
		super(LaunchingMessages.lookupInstalledJVMs);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		StandardVMType standardType = (StandardVMType) JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
		Collection<File> candidates = computeCandidateVMs(standardType);
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		Set<File> knownVMs = knownVMs();
		candidates.removeIf(knownVMs::contains);
		Collection<VMStandin> systemVMs = Collections.EMPTY_LIST;
		// for MacOS, system installed VMs need a special command to locate
		if (Platform.OS.isMac()) {
			try {
				systemVMs = new ArrayList<>(Arrays.asList(MacInstalledJREs.getInstalledJREs(monitor)));
				systemVMs.removeIf(t -> knownVMs.contains(t.getInstallLocation()));
				for (VMStandin systemVM : systemVMs) {
					candidates.removeIf(t -> t.equals(systemVM.getInstallLocation()));
				}
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
		monitor.beginTask(LaunchingMessages.lookupInstalledJVMs, candidates.size() + systemVMs.size());
		for (File f : candidates) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			SubMonitor subMon = SubMonitor.convert(monitor, f.getAbsolutePath(), 1);
			VMStandin workingCopy = new VMStandin(standardType, f.getAbsolutePath());
			workingCopy.setInstallLocation(f);
			String name = f.getName();
			int i = 2;
			while (isDuplicateName(name)) {
				name = f.getName() + '(' + i++ + ')';
			}
			workingCopy.setName(name);
			IVMInstall install = workingCopy.convertToRealVM();
			if (!(install instanceof IVMInstall2 vm && vm.getJavaVersion() != null)) {
				// worksaround: such VMs may cause issue later
				// https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/248
				standardType.disposeVMInstall(install.getId());
			}
			subMon.done();
		}

		// for MacOS, we may have additional system installed VMs so add them here
		for (VMStandin systemVM : systemVMs) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			SubMonitor subMon = SubMonitor.convert(monitor, systemVM.getInstallLocation().getAbsolutePath(), 1);
			String name = systemVM.getName();
			int i = 2;
			while (isDuplicateName(name)) {
				name = systemVM.getName() + '(' + i++ + ')';
			}
			systemVM.setName(name);
			IVMInstall install = systemVM.convertToRealVM();
			if (!(install instanceof IVMInstall2 vm && vm.getJavaVersion() != null)) {
				// worksaround: such VMs may cause issue later
				// https://github.com/eclipse-jdt/eclipse.jdt.debug/issues/248
				standardType.disposeVMInstall(install.getId());
			}
			subMon.done();
		}
		return Status.OK_STATUS;
	}

	private boolean isDuplicateName(String name) {
		return Stream.of(JavaRuntime.getVMInstallTypes()) //
			.flatMap(vmType -> Arrays.stream(vmType.getVMInstalls())) //
			.map(IVMInstall::getName) //
			.anyMatch(name::equals);
	}

	private Collection<File> computeCandidateVMs(StandardVMType standardType) {
		// parent directories containing a collection of VM installations
		Collection<File> rootDirectories = new HashSet<>();
		if (Platform.OS.isWindows()) {
			computeWindowsCandidates(rootDirectories);
		} else {
			rootDirectories.add(new File("/usr/lib/jvm")); //$NON-NLS-1$
		}
		rootDirectories.add(new File(System.getProperty("user.home"), ".sdkman/candidates/java")); //$NON-NLS-1$ //$NON-NLS-2$
		rootDirectories.add(new File(miseDataDir(), "installs/java")); //$NON-NLS-1$

		Set<File> directories = rootDirectories.stream().filter(File::isDirectory)
			.map(dir -> dir.listFiles(File::isDirectory))
			.flatMap(Arrays::stream)
			.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new));

		// particular VM installations
		String javaHome = System.getenv("JAVA_HOME"); //$NON-NLS-1$
		if (javaHome != null) {
			directories.add(new File(javaHome));
		}
		String jdkHome = System.getenv("JDK_HOME"); //$NON-NLS-1$
		if (jdkHome != null) {
			directories.add(new File(jdkHome));
		}
		System.getenv().entrySet().forEach(entry -> {
			if (entry.getKey().startsWith("JAVA_HOME_")) { //$NON-NLS-1$
				directories.add(new File(entry.getValue()));
			}
		});
		// other common/standard lookup strategies can be added here
		return directories.stream()
			.filter(Objects::nonNull)
			.filter(File::isDirectory)
			.map(t -> {
				try {
					return t.getCanonicalFile();
				} catch (IOException e) {
					return null;
				}
			}).filter(Objects::nonNull)
			.filter(location -> standardType.validateInstallLocation(location).isOK())
			.collect(Collectors.toCollection(HashSet::new));
	}

	@SuppressWarnings("nls")
	private void computeWindowsCandidates(Collection<File> rootDirectories) {
		List<String> progFiles = List.of("ProgramFiles", "ProgramFiles(x86)");
		/// Could not find directory name for vendors:
		/// - Alibaba - instructs to unzip in arbitrary directory
		/// - IBM - each product carries its own installation of JDK
		/// - SAP - each product carries its own installation of JDK
		List<String> subDirs = List.of("Eclipse Adoptium", "RedHat", "Java", "Axiom", "Zulu", "BellSoft", "Microsoft", "Amazon Corretto");
		rootDirectories.addAll(
		progFiles.stream()
			.map(name -> System.getenv(name))
			.filter(Objects::nonNull)
			.distinct()
			.flatMap(progFilesDir -> subDirs.stream().map(subDir -> new File(progFilesDir, subDir)))
			.collect(Collectors.toList()));
	}

	private static File miseDataDir() {
		String miseDataDir = System.getenv("MISE_DATA_DIR"); //$NON-NLS-1$
		return miseDataDir != null ? new File(miseDataDir) : new File(xdgDataHome(), "mise"); //$NON-NLS-1$
	}

	private static File xdgDataHome() {
		String xdgDataHome = System.getenv("XDG_DATA_HOME"); //$NON-NLS-1$
		if (Platform.OS.isWindows()) {
			if (xdgDataHome == null) {
				xdgDataHome = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
			}
			if (xdgDataHome == null) {
				return new File(System.getProperty("user.home"), "AppData/Local"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if (xdgDataHome == null) {
			return new File(System.getProperty("user.home"), ".local/share"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new File(xdgDataHome);
	}

	private static Set<File> knownVMs() {
		return Stream.of(JavaRuntime.getVMInstallTypes())
			.map(IVMInstallType::getVMInstalls)
			.flatMap(Arrays::stream)
			.map(IVMInstall::getInstallLocation)
			.filter(Objects::nonNull)
			.map(t -> {
				try {
					return t.getCanonicalFile();
				} catch (IOException e) {
					return null;
				}
			}).filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Override
	public boolean belongsTo(Object family) {
		return family.equals(FAMILY);
	}

	/**
	 * Searches the specified directory recursively for installed VMs, adding each
	 * detected VM to the <code>found</code> list. Any directories specified in
	 * the <code>ignore</code> are not traversed.
	 */
	public static void search(File directory, List<File> found, List<IVMInstallType> types, Set<File> ignore, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		String[] fileNames = directory.list();
		if (fileNames == null) {
			return; // not a directory
		}
		List<String> names = new ArrayList<>();
		names.add(null); // self
		names.addAll(List.of(fileNames));
		List<File> subDirs = new ArrayList<>();
		for (String name : names) {
			if (monitor.isCanceled()) {
				return;
			}
			File file = name == null ? directory : new File(directory, name);
			monitor.subTask(NLS.bind(LaunchingMessages.SearchingJVMs, Integer.toString(found.size()),
					file.toPath().normalize().toAbsolutePath().toString().replace("&", "&&") )); // @see bug 29855 //$NON-NLS-1$ //$NON-NLS-2$
			IVMInstallType[] vmTypes = JavaRuntime.getVMInstallTypes();
			if (file.isDirectory()) {
				if (ignore.add(file)) {
					boolean validLocation = false;

					// Take the first VM install type that claims the location as a
					// valid VM install.  VM install types should be smart enough to not
					// claim another type's VM, but just in case...
					for (int j = 0; j < vmTypes.length; j++) {
						if (monitor.isCanceled()) {
							return;
						}
						IVMInstallType type = vmTypes[j];
						IStatus status = type.validateInstallLocation(file);
						if (status.isOK()) {
							String filePath = file.getPath();
							int index = filePath.lastIndexOf(File.separatorChar);
							File newFile = file;
							// remove bin folder from install location as java executables are found only under bin for Java 9 and above
							if (index > 0 && filePath.substring(index + 1).equals("bin")) { //$NON-NLS-1$
								newFile = new File(filePath.substring(0, index));
							}
							found.add(newFile);
							types.add(type);
							validLocation = true;
							break;
						}
					}
					if (!validLocation) {
						subDirs.add(file);
					}
				}
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = subDirs.remove(0);
			search(subDir, found, types, ignore, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}

	}

	public static void initialize() {
		if (Boolean.getBoolean(PROPERTY_DETECT_VM_INSTALLATIONS_JOB_DISABLED)) {
			// early exit no need to read preferences or check env variable!
			return;
		}
		if (System.getProperty(PROPERTY_DETECT_VM_INSTALLATIONS_JOB_DISABLED) == null && Boolean.parseBoolean(System.getenv(ENV_CI))) {
			// exit because no explicit value for the property was given and we are running in a CI environment
			return;
		}
		// finally look what is defined in the preferences
		IEclipsePreferences instanceNode = InstanceScope.INSTANCE.getNode(LaunchingPlugin.getDefault().getBundle().getSymbolicName());
		IEclipsePreferences defaultNode = DefaultScope.INSTANCE.getNode(LaunchingPlugin.getDefault().getBundle().getSymbolicName());
		boolean defaultValue = defaultNode.getBoolean(LaunchingPlugin.PREF_DETECT_VMS_AT_STARTUP, true);
		if (instanceNode.getBoolean(LaunchingPlugin.PREF_DETECT_VMS_AT_STARTUP, defaultValue)) {
			new DetectVMInstallationsJob().schedule();
		}
	}

}
