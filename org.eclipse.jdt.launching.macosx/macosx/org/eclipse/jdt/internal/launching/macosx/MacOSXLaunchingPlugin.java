/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.macosx;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;


public class MacOSXLaunchingPlugin extends Plugin {

	private static MacOSXLaunchingPlugin fgPlugin;
	private static final String RESOURCE_BUNDLE= "org.eclipse.jdt.internal.launching.macosx.MacOSXLauncherMessages";//$NON-NLS-1$
	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	public MacOSXLaunchingPlugin() {
		super();
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
	}
	
	public static MacOSXLaunchingPlugin getDefault() {
		return fgPlugin;
	}
	
	static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/*
	 * Convenience method which returns the unique identifier of this plug-in.
	 */
	static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plug-in id defined in plugin.xml
			return "org.eclipse.jdt.launching.macosx"; //$NON-NLS-1$
		}
		return getDefault().getBundle().getSymbolicName();
	}
}
