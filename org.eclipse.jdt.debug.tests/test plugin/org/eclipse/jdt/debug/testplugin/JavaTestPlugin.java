/*******************************************************************************
 *  Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;


/**
 * Implementation of the Test plugin
 */
public class JavaTestPlugin extends AbstractUIPlugin {

	private static JavaTestPlugin fgDefault;

	/**
	 * Constructor
	 */
	public JavaTestPlugin() {
		super();
		fgDefault= this;
	}

	/**
	 * Returns the singleton instance of the plugin
	 * @return the singleton instance of the plugin
	 */
	public static JavaTestPlugin getDefault() {
		return fgDefault;
	}

	/**
	 * Returns a handle to the current workspace
	 * @return a handle to the current workspace
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Sets autobuild to the specified boolean value
	 * @param enable
	 * @throws CoreException
	 */
	public static void enableAutobuild(boolean enable) throws CoreException {
		// disable auto build
		IWorkspace workspace= JavaTestPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		desc.setAutoBuilding(enable);
		workspace.setDescription(desc);
	}

	/**
	 * Returns the file corresponding to the specified path from within this bundle
	 * @param path
	 * @return the file corresponding to the specified path from within this bundle, or
	 * <code>null</code> if not found
	 */
	public File getFileInPlugin(IPath path) {
		try {
			Bundle bundle = getDefault().getBundle();
			URL installURL= new URL(bundle.getEntry("/"), path.toString());
			URL localURL= FileLocator.toFileURL(installURL);//Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
}
