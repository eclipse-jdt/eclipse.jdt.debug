/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.ui.macbundler;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;


public class MacOSXUILaunchingPlugin extends Plugin {
	
	private static MacOSXUILaunchingPlugin fgPlugin;

	public MacOSXUILaunchingPlugin() {
		super();
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
	}
	
	public static MacOSXUILaunchingPlugin getDefault() {
		return fgPlugin;
	}

	/*
	 * Convenience method which returns the unique identifier of this plug-in.
	 */
	static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.jdt.launching.ui.macosx"; //$NON-NLS-1$
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
}
