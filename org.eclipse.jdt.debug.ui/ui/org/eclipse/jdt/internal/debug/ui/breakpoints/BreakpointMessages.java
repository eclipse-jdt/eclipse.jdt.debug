/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.osgi.util.NLS;

public class BreakpointMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.breakpoints.BreakpointMessages";//$NON-NLS-1$

	public static String AddExceptionAction_0;
	public static String AddExceptionAction_1;

	public static String AddExceptionAction_EnableExceptionBreakpoint;
	public static String AddExceptionDialog_13;
	public static String AddExceptionDialog_15;
	public static String AddExceptionDialog_16;

	public static String AddClassPrepareBreakpointAction_0;
	public static String AddClassPrepareBreakpointAction_1;
	public static String AddClassPrepareBreakpointAction_2;
	
	public static String BreakpointConditionDetailPane_0;

	public static String BreakpointDetailPaneFactory_0;

	public static String BreakpointDetailPaneFactory_1;
	
	public static String ExceptionBreakpointDetailPane_0;
	
	public static String JavaBreakpointTypeAdapterFactory_0;	
	public static String StandardBreakpointDetailPane_0;

	public static String WatchpointDetailPane_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, BreakpointMessages.class);
	}
	
}
