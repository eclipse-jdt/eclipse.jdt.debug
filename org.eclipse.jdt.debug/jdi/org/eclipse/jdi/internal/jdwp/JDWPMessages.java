/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdi.internal.jdwp;

import org.eclipse.osgi.util.NLS;

public class JDWPMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.jdwp.JDWPMessages";//$NON-NLS-1$

	public static String JdwpString_Second_byte_input_does_not_match_UTF_Specification_1;
	public static String JdwpString_Second_or_third_byte_input_does_not_mach_UTF_Specification_2;
	public static String JdwpString_Input_does_not_match_UTF_Specification_3;
	public static String JdwpString_str_is_null_4;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, JDWPMessages.class);
	}
}