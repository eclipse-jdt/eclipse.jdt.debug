package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;

/**
 * Help context ids for the Java Debug UI.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IJavaDebugHelpContextIds {
	
	public static final String PREFIX= IJavaDebugUIConstants.PLUGIN_ID + '.';

	// view parts
	public static final String DISPLAY_VIEW= PREFIX + "display_view_context"; //$NON-NLS-1$

	//dialogs
	public static final String EDIT_JRE_DIALOG= PREFIX + "edit_jre_dialog_context"; //$NON-NLS-1$

	// Preference/Property pages
	public static final String JRE_PREFERENCE_PAGE= PREFIX + "jre_preference_page_context"; //$NON-NLS-1$
	public static final String LAUNCH_JRE_PROPERTY_PAGE= PREFIX + "launch_jre_property_page_context"; //$NON-NLS-1$
	public static final String JAVA_DEBUG_PREFERENCE_PAGE= PREFIX + "java_debug_preference_page_context"; //$NON-NLS-1$
	public static final String JAVA_DEBUG_APPEARANCE_PREFERENCE_PAGE= PREFIX + "java_debug_appearance_preference_page_context"; //$NON-NLS-1$	
	public static final String JAVA_STEP_FILTER_PREFERENCE_PAGE= PREFIX + "java_step_filter_preference_page_context"; //$NON-NLS-1$
}