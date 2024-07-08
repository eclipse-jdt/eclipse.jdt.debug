/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.util.NLS;

/**
 * Searches for installed JREs on the Mac.
 */
public class MacInstalledJREs {

	/** The executable for 'java_home' */
	private static final String JAVA_HOME_PLIST = "/usr/libexec/java_home"; //$NON-NLS-1$
	/** The plist attribute describing the JRE home directory */
	private static final String PLIST_JVM_HOME_PATH = "JVMHomePath"; //$NON-NLS-1$
	/** The plist attribute describing the JRE name */
	private static final String PLIST_JVM_NAME = "JVMName"; //$NON-NLS-1$
	/** The plist attribute describing the JRE version */
	private static final String PLIST_JVM_VERSION = "JVMVersion"; //$NON-NLS-1$
	/**
	 * The plist attribute describing the bundle id of the VM
	 * @since 3.8
	 */
	private static final String PLIST_JVM_BUNDLE_ID = "JVMBundleID"; //$NON-NLS-1$

	public static final VMStandin[] NO_VMS = new VMStandin[0];

	/**
	 * Custom stand-in that allows us to provide a version
	 * @since 3.7.0
	 */
	public static class MacVMStandin extends VMStandin {

		String version = null;

		public MacVMStandin(IVMInstallType type, File location, String name, String version, String id) {
			super(type, id);
			setInstallLocation(location);
			setName(name);
			this.version = version;
		}

		@Override
		public String getJavaVersion() {
			return version;
		}
	}

	/**
	 * Parses the XML output produced from "java_home -X" (see bug 325777), and return a collection
	 * of descriptions of JRE installations.
	 *
	 * @param monitor the {@link IProgressMonitor} or <code>null</code>
	 * @return array of {@link VMStandin}s installed in the OS
	 * @exception CoreException if unable to parse the output or the executable does not exist
	 */
	public static VMStandin[] getInstalledJREs(IProgressMonitor monitor) throws CoreException {
		SubMonitor smonitor = SubMonitor.convert(monitor);
		try {
			// locate the "java_home" executable
			File javaHome = new File(JAVA_HOME_PLIST);
			if (!javaHome.exists()) {
				throw new CoreException(Status.warning("The java_home executable does not exist")); //$NON-NLS-1$
			}
			String[] cmdLine = new String[] {JAVA_HOME_PLIST, "-X"}; //$NON-NLS-1$
			Process p = null;
			try {
				p = DebugPlugin.exec(cmdLine, null);
				IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "JRE Install Detection"); //$NON-NLS-1$
				for (int i= 0; i < 600; i++) {
					// Wait no more than 30 seconds (600 * 50 milliseconds)
					if (process.isTerminated()) {
						break;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
				return parseJREInfo(process, monitor);
			} finally {
				if (p != null) {
					p.destroy();
				}
			}
		}
		finally {
			if(!smonitor.isCanceled()) {
				smonitor.done();
			}
		}
	}

	/**
	 * Parses the output from 'java_home -X'.
	 *
	 * @param process process with output from 'java_home -X'
	 * @param the {@link IProgressMonitor} or <code>null</code>
	 * @return array JRE descriptions installed in the OS
	 * @exception CoreException if unable to parse the output
	 */
	private static VMStandin[] parseJREInfo(IProcess process, IProgressMonitor monitor) {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String text = null;
		if (streamsProxy != null) {
			text = streamsProxy.getOutputStreamMonitor().getContents();
		}
		if (text != null && text.length() > 0) {
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
			return parseJREInfo(stream, monitor);
		}
		return NO_VMS;
	}

	/**
	 * Parse JREDescriptor from the given input stream. The stream is expected to be in the XML properties format (plist.xml).
	 *
	 * @param monitor
	 *            the {@link IProgressMonitor} or <code>null</code>
	 * @return the array of {@link VMStandin}s or an empty array never <code>null</code>
	 * @since 3.8
	 */
	public static VMStandin[] parseJREInfo(InputStream stream, IProgressMonitor monitor) {
		SubMonitor smonitor = SubMonitor.convert(monitor, LaunchingMessages.MacInstalledJREs_0, 10);
		try {
			Object result = new PListParser().parse(stream);
			if (result instanceof Object[] maps) {
				smonitor.setWorkRemaining(maps.length);
				Set<VMStandin> jres = new LinkedHashSet<>(); // prevent duplicates
				AbstractVMInstallType mactype = (AbstractVMInstallType) JavaRuntime.getVMInstallType("org.eclipse.jdt.internal.launching.macosx.MacOSXType"); //$NON-NLS-1$
				if(mactype != null) {
					for (Object entry : maps) {
						if(smonitor.isCanceled()) {
							///stop processing and return what we found
							return jres.toArray(new VMStandin[jres.size()]);
						}
						if (entry instanceof Map<?, ?> map //
								&& map.get(PLIST_JVM_HOME_PATH) instanceof String home //
								&& map.get(PLIST_JVM_NAME) instanceof String name //
								&& map.get(PLIST_JVM_VERSION) instanceof String version) {
							smonitor.setTaskName(NLS.bind(LaunchingMessages.MacInstalledJREs_1, name, version));
							File loc = new File(home);
							// 10.8.2+ can have more than one of the same VM, which will have the same name
							// augment it with the version to make it easier to distinguish
							String vmName = name + " [" + version + "]"; //$NON-NLS-1$//$NON-NLS-2$
							MacVMStandin vm = new MacVMStandin(mactype, loc, vmName, version, computeId(map, version));
							vm.setJavadocLocation(mactype.getDefaultJavadocLocation(loc));
							vm.setLibraryLocations(mactype.getDefaultLibraryLocations(loc));
							vm.setVMArgs(mactype.getDefaultVMArguments(loc));
							jres.add(vm);
						}
						smonitor.worked(1);
					}
				}
				return jres.toArray(new VMStandin[jres.size()]);
			}
		} catch (CoreException ce) {
			LaunchingPlugin.log(ce);
		}
		finally {
			smonitor.done();
		}
		return NO_VMS;
	}

	/**
	 * Tries to compute the descriptor id using the {@link #PLIST_JVM_BUNDLE_ID}. If that is not defined
	 * we fall back to using the version.
	 * @param map the map to look up the VM bundle version in
	 * @param version the current version - fall-back for no VM bundle id defined
	 * @return the id to use for the {@link JREDescriptor}
	 * @since 3.8
	 */
	static String computeId(Map<?, ?> map, String version) {
		Object o = map.get(PLIST_JVM_BUNDLE_ID);
		return o instanceof String id ? id : version;
 	}
}
