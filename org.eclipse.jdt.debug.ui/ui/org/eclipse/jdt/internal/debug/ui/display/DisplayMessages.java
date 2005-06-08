/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.debug.ui.display;

import org.eclipse.osgi.util.NLS;

public class DisplayMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.display.DisplayMessages";//$NON-NLS-1$

    public static String ClearDisplay_description;
	public static String ClearDisplay_label;
	public static String ClearDisplay_tooltip;

	public static String DisplayCompletionProcessor_An_exception_occurred_during_code_completion_2;
	public static String DisplayCompletionProcessor_Problems_during_completion_1;

	public static String DisplayView_Co_ntent_Assist_Ctrl_Space_1;
	public static String DisplayView_Content_Assist_2;
	public static String DisplayView_Copy_description;
	public static String DisplayCompletionProcessor_0;
	public static String DisplayCompletionProcessor_1;
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

	public static String find_replace_action_label;
	public static String find_replace_action_tooltip;
	public static String find_replace_action_image;
	public static String find_replace_action_description;
	public static String JavaInspectExpression_0;
	public static String DetailsCompletionProcessor_0;
	public static String DetailsCompletionProcessor_1;
	public static String DetailsCompletionProcessor_2;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DisplayMessages.class);
	}
}