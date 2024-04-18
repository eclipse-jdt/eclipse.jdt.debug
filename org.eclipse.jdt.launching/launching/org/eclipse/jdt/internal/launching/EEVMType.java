/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
	 * @param eeDescription the execution-environment to query
	 * @return javadoc location specified in the properties or <code>null</code> if none
	 */
	public static URL getJavadocLocation(ExecutionEnvironmentDescription eeDescription) {
		String javadoc = eeDescription.getProperty(ExecutionEnvironmentDescription.JAVADOC_LOC);
		if (javadoc != null && javadoc.length() > 0){
			try{
				URL url = new URL(javadoc);
				if ("file".equalsIgnoreCase(url.getProtocol())){ //$NON-NLS-1$
					File file = new File(url.getFile());
					url = file.getCanonicalFile().toURI().toURL();
				}
				return url;
			} catch (IOException e) {
				LaunchingPlugin.log(e);
				return null;
			}
		}
		String version = eeDescription.getProperty(ExecutionEnvironmentDescription.LANGUAGE_LEVEL);
		if (version != null) {
			return StandardVMType.getDefaultJavadocLocation(version);
		}
		return null;
	}

	/**
	 * Returns the default index location specified in the properties or <code>null</code>
	 * if none.
	 *
	 * @param eeDescription the execution-environment to query
	 * @return index location specified in the properties or <code>null</code> if none
	 * @since 3.7.0
	 */
	public static URL getIndexLocation(ExecutionEnvironmentDescription eeDescription) {
		String index = eeDescription.getProperty(ExecutionEnvironmentDescription.INDEX_LOC);
		if (index != null && index.length() > 0){
			try{
				URL url = new URL(index);
				if ("file".equalsIgnoreCase(url.getProtocol())){ //$NON-NLS-1$
					File file = new File(url.getFile());
					url = file.getCanonicalFile().toURI().toURL();
				}
				return url;
			} catch (IOException e) {
				LaunchingPlugin.log(e);
				return null;
			}
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
		for (String key : REQUIRED_PROPERTIES) {
			String property = description.getProperty(key);
			if (property == null) {
				return Status.error(NLS.bind(LaunchingMessages.EEVMType_1, key));
			}
		}
		return Status.OK_STATUS;
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
	@Override
	public File detectInstallLocation() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getDefaultLibraryLocations(java.io.File)
	 */
	@Override
	public LibraryLocation[] getDefaultLibraryLocations(File installLocationOrDefinitionFile) {
		return new LibraryLocation[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#getName()
	 */
	@Override
	public String getName() {
		return LaunchingMessages.EEVMType_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	@Override
	public IStatus validateInstallLocation(File installLocation) {
		if (installLocation.exists()) {
			return new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN, LaunchingMessages.EEVMType_4);
		}
		return Status.error(NLS.bind(LaunchingMessages.EEVMType_3, installLocation.getPath()));
	}

}
