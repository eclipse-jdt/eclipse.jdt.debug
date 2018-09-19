/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.display;

import org.eclipse.osgi.util.NLS;

public class DisplayMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.display.DisplayMessages";//$NON-NLS-1$

	public static String DisplayView_Co_ntent_Assist_Ctrl_Space_1;
	public static String DisplayView_Content_Assist_2;
	public static String DisplayView_Copy_description;
	public static String DisplayView_Copy_label;
	public static String DisplayView_Copy_tooltip;
	public static String DisplayView_Cut_description;
	public static String DisplayView_Cut_label;
	public static String DisplayView_Cut_tooltip;
	public static String DisplayView_Paste_Description;
	public static String DisplayView_Paste_label;
	public static String DisplayView_Paste_tooltip;
	public static String DisplayView_SelectAll_description;
	public static String DisplayView_SelectAll_label;
	public static String DisplayView_SelectAll_tooltip;
	public static String DisplayView_Content_Description;

	public static String JavaInspectExpression_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DisplayMessages.class);
	}
}
