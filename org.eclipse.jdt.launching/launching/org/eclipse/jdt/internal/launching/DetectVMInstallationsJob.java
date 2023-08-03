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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Lookup for VMs installed in standard or usual locations; and add the existing ones that
 * are not yet known by JDT to the VM registry (usually visible in the "Installed JREs"
 * preference page)
 */
class DetectVMInstallationsJob extends Job {

	private static final Object FAMILY = DetectVMInstallationsJob.class;

	private final StandardVMType standardType;

	DetectVMInstallationsJob() {
		super(LaunchingMessages.lookupInstalledJVMs);
		this.standardType = (StandardVMType)JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Collection<File> candidates = computeCandidateVMs();
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		Set<File> knownVMs = knownVMs();
		candidates.removeIf(knownVMs::contains);
		Collection<VMStandin> systemVMs = Collections.EMPTY_LIST;
		// for MacOS, system installed VMs need a special command to locate
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
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
			int i = 1;
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
			int i = 1;
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

	private Collection<File> computeCandidateVMs() {
		// parent directories containing a collection of VM installations
		Collection<File> rootDirectories = new HashSet<>();
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			rootDirectories.add(new File("/usr/lib/jvm")); //$NON-NLS-1$
		}
		rootDirectories.add(new File(System.getProperty("user.home"), ".sdkman/candidates/java")); //$NON-NLS-1$ //$NON-NLS-2$

		Set<File> directories = rootDirectories.stream().filter(File::isDirectory)
			.map(dir -> dir.listFiles(File::isDirectory))
			.flatMap(Arrays::stream)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		// particular VM installations
		String javaHome = System.getenv("JAVA_HOME"); //$NON-NLS-1$
		if (javaHome != null) {
			directories.add(new File(javaHome));
		}
		String jdkHome = System.getenv("JDK_HOME"); //$NON-NLS-1$
		if (jdkHome != null) {
			directories.add(new File(jdkHome));
		}
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

}
