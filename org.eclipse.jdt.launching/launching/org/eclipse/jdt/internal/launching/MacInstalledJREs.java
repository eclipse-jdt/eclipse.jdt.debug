/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import java.io.InputStream;
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
	 * The plist attribute describing the bundle id of the VM
	 * @since 3.8
	 */
	private static final String PLIST_JVM_BUNDLE_ID = "JVMBundleID"; //$NON-NLS-1$
	
	static final JREDescriptor[] NO_DESCRIPTORS = new JREDescriptor[0];
	
	/**
	 * Describes an installed JRE on MacOS
	 */
	public class JREDescriptor {
		String fName;
		File fHome;
		String fVersion;
		String fId;
		
		/**
		 * Constructs a new JRE descriptor 
		 * 
		 * @param home Home directory of the JRE
		 * @param name JRE name
		 * @param version JRE version
		 * @param id the computed id of the JRE from the plist output
		 */
		public JREDescriptor(File home, String name, String version, String id) {
			fHome = home;
			fName = name;
			fVersion = version;
			fId = id;
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
		
		/**
		 * returns the computed id of the descriptor
		 * 
		 * @return the descriptor id
		 * @since 3.8
		 */
		public String getId() {
			return fId;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof JREDescriptor) {
				JREDescriptor jre = (JREDescriptor) obj;
				return jre.fHome.equals(fHome) && jre.fName.equals(fName) && jre.fVersion.equals(fVersion) && fId.equals(jre.fId);
			}
			return false;
		}
		
		@Override
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
			return parseJREInfo(stream);
		}
		return NO_DESCRIPTORS;
	}
	
	/**
	 * Parse {@link JREDescriptor}s from the given input stream. The stream is expected to be in the 
	 * XML properties format.
	 * 
	 * @param stream
	 * @return the array of {@link JREDescriptor}s or an empty array never <code>null</code>
	 * @since 3.8
	 */
	public JREDescriptor[] parseJREInfo(InputStream stream) {
		try {
			Object result = new PListParser().parse(stream);
			if (result instanceof Object[]) {
				Object[] maps = (Object[]) result;
				List<JREDescriptor> jres= new ArrayList<JREDescriptor>();
				for (int i = 0; i < maps.length; i++) {
					Object object = maps[i];
					if (object instanceof Map) {
						Map<?, ?> map = (Map<?, ?>) object;
						Object home = map.get(PLIST_JVM_HOME_PATH);
						Object name = map.get(PLIST_JVM_NAME);
						Object version = map.get(PLIST_JVM_VERSION);
						if (home instanceof String && name instanceof String && version instanceof String) {
							String ver = (String) version;
							JREDescriptor descriptor = new JREDescriptor(new File((String)home), (String)name, (String)version, computeId(map, ver));
							if (!jres.contains(descriptor)) { // remove duplicates
								jres.add(descriptor);	
							}
						} 
					} 
				}
				return jres.toArray(new JREDescriptor[jres.size()]);
			}
		} catch (CoreException ce) {
			LaunchingPlugin.log(ce);
		}
		return NO_DESCRIPTORS;
	}
	
	/**
	 * Tries to compute the descriptor id using the {@link #PLIST_JVM_BUNDLE_ID}. If that is not defined
	 * we fall back to using the version.
	 * @param map the map to look up the VM bundle version in
	 * @param version the current version - fall-back for no VM bundle id defined
	 * @return the id to use for the {@link JREDescriptor}
	 * @since 3.8
	 */
	String computeId(Map<?, ?> map, String version) {
		Object o = map.get(PLIST_JVM_BUNDLE_ID);
		if(o instanceof String) {
			return (String) o;
		}
		return version;
 	}
}
