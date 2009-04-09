/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.runtime.Platform;

/**
 * Debug flags in options file.
 * 
 * @since 3.5
 */
public class JDIDebugOptions {
	// debug option flags
	public static boolean DEBUG = false;
	public static boolean DEBUG_JDI_EVENTS = false;

	public static void initDebugOptions() {
		DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.jdt.debug/debug"));  //$NON-NLS-1$//$NON-NLS-2$
		DEBUG_JDI_EVENTS = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.jdt.debug/debug/jdiEvents")); //$NON-NLS-1$
	}
}
