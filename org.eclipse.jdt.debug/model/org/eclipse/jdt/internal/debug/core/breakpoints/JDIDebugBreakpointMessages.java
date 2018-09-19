/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import org.eclipse.osgi.util.NLS;

public class JDIDebugBreakpointMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.core.breakpoints.JDIDebugBreakpointMessages";//$NON-NLS-1$

	public static String ConditionalBreakpointHandler_0;

	public static String ConditionalBreakpointHandler_1;

	public static String JavaBreakpoint___Hit_Count___0___1;
	public static String JavaBreakpoint_Exception;
	public static String JavaPatternBreakpoint_0;
	public static String JavaBreakpoint__suspend_policy__thread__1;
	public static String JavaBreakpoint__suspend_policy__VM__2;

	public static String JavaLineBreakpoint___Condition___0___2;
	public static String JavaLineBreakpoint_Unable_to_compile_conditional_breakpoint___missing_Java_project_context__1;
	public static String JavaLineBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1;
	public static String JavaLineBreakpoint___line___0___1;
	public static String JavaLineBreakpoint_Absent_Line_Number_Information_1;

	public static String JavaPatternBreakpoint_exception_source_name;
	public static String JavaPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1;

	public static String JavaWatchpoint_no_access_watchpoints;
	public static String JavaWatchpoint_no_modification_watchpoints;
	public static String JavaWatchpoint_Unable_to_create_breakpoint_request___VM_disconnected__1;

	public static String JavaExceptionBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1;

	public static String JavaMethodBreakpoint_0;

	public static String JavaMethodBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1;

	public static String JavaTargetPatternBreakpoint_Unable_to_add_breakpoint___VM_disconnected__1;

	public static String JavaClassPrepareBreakpoint_2;
	public static String JavaClassPrepareBreakpoint_3;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, JDIDebugBreakpointMessages.class);
	}
}