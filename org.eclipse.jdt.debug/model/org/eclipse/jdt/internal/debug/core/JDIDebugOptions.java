/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core;

import java.util.Hashtable;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * Debug flags in options file.
 * 
 * @since 3.5
 */
public class JDIDebugOptions implements DebugOptionsListener {
	
	public static final String DEBUG_AST_EVALUATIONS_CALLING_THREADS_FLAG = "org.eclipse.jdt.debug/debug/astEvaluations/callingThreads"; //$NON-NLS-1$
	public static final String DEBUG_AST_EVALUATIONS_FLAG = "org.eclipse.jdt.debug/debug/astEvaluations"; //$NON-NLS-1$
	public static final String DEBUG_JDI_REQUEST_TIMES_FLAG = "org.eclipse.jdt.debug/debug/jdiRequestTimes"; //$NON-NLS-1$
	public static final String DEBUG_JDI_EVENTS_FLAG = "org.eclipse.jdt.debug/debug/jdiEvents"; //$NON-NLS-1$
	public static final String DEBUG_FLAG = "org.eclipse.jdt.debug/debug"; //$NON-NLS-1$
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_JDI_EVENTS = false;
	public static boolean DEBUG_JDI_REQUEST_TIMES = false;
	public static boolean DEBUG_AST_EVAL = false;
	public static boolean DEBUG_AST_EVAL_THREAD_TRACE = false;

	/**
	 * The {@link DebugTrace} object to print to OSGi tracing
	 * @since 3.8
	 */
	private static DebugTrace fgDebugTrace;
	
	/**
	 * Constructor
	 */
	public JDIDebugOptions(BundleContext context) {
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, JDIDebugPlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}
	
	// used to format debug messages
	public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.debug.DebugOptionsListener#optionsChanged(org.eclipse.osgi.service.debug.DebugOptions)
	 */
	public void optionsChanged(DebugOptions options) {
		fgDebugTrace = options.newDebugTrace(JDIDebugPlugin.getUniqueIdentifier());
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_JDI_EVENTS = DEBUG && options.getBooleanOption(DEBUG_JDI_EVENTS_FLAG, false);
		DEBUG_JDI_REQUEST_TIMES = DEBUG && options.getBooleanOption(DEBUG_JDI_REQUEST_TIMES_FLAG, false);
		DEBUG_AST_EVAL = DEBUG && options.getBooleanOption(DEBUG_AST_EVALUATIONS_FLAG, false);
		DEBUG_AST_EVAL_THREAD_TRACE = DEBUG && options.getBooleanOption(DEBUG_AST_EVALUATIONS_CALLING_THREADS_FLAG, false);
	}
	
	/**
	 * Prints the given message to System.out and to the OSGi tracing (if started)
	 * @param option the option or <code>null</code>
	 * @param message the message to print or <code>null</code>
	 * @param throwable the {@link Throwable} or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String option, String message, Throwable throwable) {
		System.out.println(message);
		if(fgDebugTrace != null) {
			fgDebugTrace.trace(option, message, throwable);
		}
	}
	
	/**
	 * Prints the given message to System.out and to the OSGi tracing (if enabled)
	 * 
	 * @param message the message or <code>null</code>
	 * @since 3.8
	 */
	public static void trace(String message) {
		trace(null, message, null);
	}
}
