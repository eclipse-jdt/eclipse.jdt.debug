/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

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
	 * Describes an installed JRE on MacOS
	 */
	public class JREDescriptor {
		
		String fName;
		File fHome;
		String fVersion;
		
		/**
		 * Constructs a new JRE descriptor 
		 * 
		 * @param home Home directory of the JRE
		 * @param name JRE name
		 * @param version JRE version
		 */
		public JREDescriptor(File home, String name, String version) {
			fHome = home;
			fName = name;
			fVersion = version;
		}
		
		/**
		 * Returns the home installation directory for this JRE.
		 * 
		 * @return home directory
		 */
		public File getHome() {
			return fHome;
		}
		
		/**
		 * Returns the name of the JRE.
		 * 
		 * @return JRE name
		 */
		public String getName() {
			return fName;
		}
		
		/**
		 * Returns the version of the JRE.
		 * 
		 * @return JRE version
		 */
		public String getVersion() {
			return fVersion;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof JREDescriptor) {
				JREDescriptor jre = (JREDescriptor) obj;
				return jre.fHome.equals(fHome) && jre.fName.equals(fName) && jre.fVersion.equals(fVersion);
			}
			return false;
		}
		
		public int hashCode() {
			return fHome.hashCode() + fName.hashCode() + fVersion.hashCode();
		}
	}
	
	/**
	 * Parses the XML output produced from "java_home -X" (see bug 325777), and return a collection
	 * of descriptions of JRE installations.
	 * 
	 * @return array of JRE descriptions installed in the OS
	 * @exception CoreException if unable to parse the output or the executable does not exist
	 */
	public JREDescriptor[] getInstalledJREs() throws CoreException {
		// locate the "java_home" executable
		File java_home = new File(JAVA_HOME_PLIST);
		if (!java_home.exists()) {
			throw new CoreException(new Status(IStatus.WARNING, LaunchingPlugin.getUniqueIdentifier(), "The java_home executable does not exist")); //$NON-NLS-1$
		}
		String[] cmdLine = new String[] {JAVA_HOME_PLIST, "-X"}; //$NON-NLS-1$
		Process p = null;
		try {
			p = DebugPlugin.exec(cmdLine, null);
			IProcess process = DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), p, "JRE Install Detection"); //$NON-NLS-1$
			for (int i= 0; i < 600; i++) {
				// Wait no more than 30 seconds (600 * 50 ms)
				if (process.isTerminated()) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			return parseJREInfo(process);
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
	}
	
	/**
	 * Parses the output from 'java_home -X'.
	 * 
	 * @param process process with output from 'java_home -X'
	 * @return array JRE descriptions installed in the OS
	 * @exception CoreException if unable to parse the output
	 */
	private JREDescriptor[] parseJREInfo(IProcess process) throws CoreException {
		IStreamsProxy streamsProxy = process.getStreamsProxy();
		String text = null;
		if (streamsProxy != null) {
			text = streamsProxy.getOutputStreamMonitor().getContents();
		}
		if (text != null && text.length() > 0) {
			ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
			Object result = new PListParser().parse(stream);
			if (result instanceof Object[]) {
				Object[] maps = (Object[]) result;
				List jres= new ArrayList();
				for (int i = 0; i < maps.length; i++) {
					Object object = maps[i];
					if (object instanceof Map) {
						Map map = (Map) object;
						Object home = map.get(PLIST_JVM_HOME_PATH);
						Object name = map.get(PLIST_JVM_NAME);
						Object version = map.get(PLIST_JVM_VERSION);
						if (home instanceof String && name instanceof String && version instanceof String) {
							JREDescriptor descriptor = new JREDescriptor(new File((String)home), (String)name, (String)version);
							if (!jres.contains(descriptor)) { // remove duplicates
								jres.add(descriptor);	
							}
						} else {
							unexpectedFormat();
						}
					} else {
						unexpectedFormat();
					}
				}
				return (JREDescriptor[]) jres.toArray(new JREDescriptor[jres.size()]);
			}
			unexpectedFormat();
		}
		unexpectedFormat();
		return null; // previous line will throw an exception
	}
	
	private void unexpectedFormat() throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), "Output from java_home not in expected format")); //$NON-NLS-1$
	}
}
