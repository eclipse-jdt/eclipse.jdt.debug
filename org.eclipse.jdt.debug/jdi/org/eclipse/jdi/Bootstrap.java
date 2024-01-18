/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
package org.eclipse.jdi;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

public class Bootstrap {
	private static com.sun.jdi.VirtualMachineManager fVirtualMachineManager;

	public Bootstrap() {
	}

	public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
		if (fVirtualMachineManager != null) {
			return fVirtualMachineManager;
		}

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		String className = null;
		if (extensionRegistry != null) { // is null if the platform was not started
			className = extensionRegistry.getExtensionPoint(JDIDebugPlugin.getUniqueIdentifier(), "jdiclient").getLabel(); //$NON-NLS-1$
		}
		Class<?> clazz = null;
		try {
			if (className != null) {
				clazz = Class.forName(className);
			}
			if (clazz != null) {
				fVirtualMachineManager = (com.sun.jdi.VirtualMachineManager) clazz.getDeclaredConstructor().newInstance();
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not instantiate " + className, e); //$NON-NLS-1$
		}

		if (fVirtualMachineManager == null) {
			// If any exceptions occurred, we'll end up here
			fVirtualMachineManager = new org.eclipse.jdi.internal.VirtualMachineManagerImpl();
		}

		return fVirtualMachineManager;
	}
}
