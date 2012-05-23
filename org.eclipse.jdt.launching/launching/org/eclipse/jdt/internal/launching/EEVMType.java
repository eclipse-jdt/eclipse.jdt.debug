/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.ExecutionEnvironmentDescription;
import org.eclipse.osgi.util.NLS;

/**
 * Utility class for Standard VM type. Used to generate/retrieve information for
 * VMs defined by EE property file.
 * 
 * @since 3.4
 */
public class EEVMType extends AbstractVMInstallType {
	
	/**
	 * VM Type id
	 */
	public static final String ID_EE_VM_TYPE = "org.eclipse.jdt.launching.EEVMType"; //$NON-NLS-1$

	/**
	 * Substitution in EE file - replaced with directory of EE file,
	 * to support absolute path names where needed.
	 */
	public static final String VAR_EE_HOME = "${ee.home}"; //$NON-NLS-1$
	
	private static final String[] REQUIRED_PROPERTIES = new String[]{
		ExecutionEnvironmentDescription.EXECUTABLE,
		ExecutionEnvironmentDescription.BOOT_CLASS_PATH,
		ExecutionEnvironmentDescription.LANGUAGE_LEVEL,
		ExecutionEnvironmentDescription.JAVA_HOME};
		
	/**
	 * Returns the default javadoc location specified in the properties or <code>null</code>
	 * if none.
	 * 
	 * @param properties properties map
	 * @return javadoc location specified in the properties or <code>null</code> if none
	 */
	public static URL getJavadocLocation(Map<String, String> properties) {
		String javadoc = getProperty(ExecutionEnvironmentDescription.JAVADOC_LOC, properties); 
		if (javadoc != null && javadoc.length() > 0){
			try{
				URL url = new URL(javadoc);
				if ("file".equalsIgnoreCase(url.getProtocol())){ //$NON-NLS-1$
					File file = new File(url.getFile());
					url = file.getCanonicalFile().toURL();
				}
				return url;
			} catch (MalformedURLException e){
				LaunchingPlugin.log(e);
				return null;
			} catch (IOException e){
				LaunchingPlugin.log(e);
				return null;
			}
		}
		String version = getProperty(ExecutionEnvironmentDescription.LANGUAGE_LEVEL, properties);
		if (version != null) {
			return StandardVMType.getDefaultJavadocLocation(version);
		}
		return null;
	}	

	/**
	 * Returns a status indicating if the given definition file is valid.
	 * 
	 * @param description definition file
	 * @return status indicating if the given definition file is valid
	 */
	public static IStatus validateDefinitionFile(ExecutionEnvironmentDescription description) {
		// validate required properties
		for (int i = 0; i < REQUIRED_PROPERTIES.length; i++) {
			String key = REQUIRED_PROPERTIES[i];
			String property = description.getProperty(key);
			if (property == null) {
				return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), NLS.bind(LaunchingMessages.EEVMType_1, new String[]{key} ));
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns the specified property value from the given map, as a {@link String},
	 * or <code>null</code> if none.
	 * 
	 * @param property the name of the property
	 * @param properties property map
	 * @return value or <code>null</code>
	 */
	private static String getProperty(String property, Map<String, String> properties) {
		return properties.get(property);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#doCreateVMInstall(java.lang.String)
	 */
	@Override
	protected IVMInstall doCreateVMInstall(String id) {
		return new EEVMInstall(this, id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	public LibraryLocation[] getDefaultLibraryLocations(File installLocationOrDefinitionFile) {
		return new LibraryLocation[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	public String getName() {
		return LaunchingMessages.EEVMType_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public IStatus validateInstallLocation(File installLocation) {
		if (installLocation.exists()) {
			return new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.EEVMType_4);
		}
		return new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, NLS.bind(LaunchingMessages.EEVMType_3, new String[]{installLocation.getPath()}));
	}
	
}
