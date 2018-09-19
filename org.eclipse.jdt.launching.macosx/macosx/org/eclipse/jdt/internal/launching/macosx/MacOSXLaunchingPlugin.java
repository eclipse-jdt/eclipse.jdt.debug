/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching.macosx;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;


public class MacOSXLaunchingPlugin extends Plugin {

	private static MacOSXLaunchingPlugin fgPlugin;

	public MacOSXLaunchingPlugin() {
		super();
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
	}
	
	public static MacOSXLaunchingPlugin getDefault() {
		return fgPlugin;
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
