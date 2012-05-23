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
package org.eclipse.jdi;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class Bootstrap {
	private static com.sun.jdi.VirtualMachineManager fVirtualMachineManager;

	public Bootstrap() {
	}

	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		if (fVirtualMachineManager != null)
			return fVirtualMachineManager;

		try {
			IExtensionRegistry extensionRegistry = Platform
					.getExtensionRegistry();
			String className = null;
			if (extensionRegistry != null) { // is null if the platform was not
												// started
				className = extensionRegistry
						.getExtensionPoint(
								JDIDebugPlugin.getUniqueIdentifier(),
								"jdiclient").getLabel(); //$NON-NLS-1$
			}
			Class<?> clazz = null;
			if (className != null) {
				clazz = Class.forName(className);
			}
			if (clazz != null) {
				fVirtualMachineManager = (com.sun.jdi.VirtualMachineManager) clazz
						.newInstance();
			}
		} catch (ClassNotFoundException e) {
		} catch (NoClassDefFoundError e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		if (fVirtualMachineManager == null) {
			// If any exceptions occurred, we'll end up here
			fVirtualMachineManager = new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		}

		return fVirtualMachineManager;
	}
}
