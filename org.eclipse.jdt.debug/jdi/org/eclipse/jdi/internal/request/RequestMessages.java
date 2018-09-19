/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.request;

import org.eclipse.osgi.util.NLS;

public class RequestMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.request.RequestMessages";//$NON-NLS-1$

	public static String EventRequestImpl___not_enabled__1;
	public static String EventRequestImpl____2;
	public static String EventRequestImpl_Invalid_suspend_policy_encountered___3;
	public static String EventRequestImpl_Invalid_step_size_encountered___4;
	public static String EventRequestImpl_Invalid_step_depth_encountered___5;
	public static String EventRequestManagerImpl_EventRequest_type_of__0__is_unknown_1;
	public static String EventRequestManagerImpl_Got_event_of_unknown_type_2;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, RequestMessages.class);
	}
}
