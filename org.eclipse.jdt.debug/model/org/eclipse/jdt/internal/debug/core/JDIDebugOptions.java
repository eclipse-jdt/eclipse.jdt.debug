/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * Debug flags in options file.
 * 
 * @since 3.5
 */
public class JDIDebugOptions {
	// debug option flags
	public static boolean DEBUG = false;
	public static boolean DEBUG_JDI_EVENTS = false;
	public static boolean DEBUG_JDI_REQUEST_TIMES = false;
	public static boolean DEBUG_AST_EVAL = false;
	public static boolean DEBUG_AST_EVAL_THREAD_TRACE = false;
	
	// used to format debug messages
	public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$

	public static void initDebugOptions() {
		DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.jdt.debug/debug"));  //$NON-NLS-1$//$NON-NLS-2$
		DEBUG_JDI_EVENTS = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.jdt.debug/debug/jdiEvents")); //$NON-NLS-1$
		DEBUG_JDI_REQUEST_TIMES = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.jdt.debug/debug/jdiRequestTimes")); //$NON-NLS-1$
		DEBUG_AST_EVAL = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.jdt.debug/debug/astEvaluations")); //$NON-NLS-1$
		DEBUG_AST_EVAL_THREAD_TRACE = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.jdt.debug/debug/astEvaluations/callingThreads")); //$NON-NLS-1$
	}
}
