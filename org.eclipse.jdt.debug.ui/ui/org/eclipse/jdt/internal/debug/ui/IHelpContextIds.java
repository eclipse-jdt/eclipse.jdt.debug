package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.JavaDebugUI;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Help context ids for the Java Debug UI.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
public interface IHelpContextIds {
	public static final String PREFIX= JavaDebugUI.PLUGIN_ID + '.';

	// Actions
	public static final String ADD_METHODBREAKPOINT_ACTION= PREFIX + "add_methodbreakpoint_action_context"; //$NON-NLS-1$
	public static final String ADD_WATCHPOINT_ACTION= PREFIX + "add_watchpoint_action_context"; //$NON-NLS-1$
	public static final String RUN_TO_LINE_ACTION= PREFIX + "run_to_line_action_context"; //$NON-NLS-1$
	public static final String TOGGLE_PRESENTATION_ACTION= PREFIX + "toggle_presentation_action_context"; //$NON-NLS-1$
	public static final String DISPLAY_ACTION= PREFIX + "display_action_context"; //$NON-NLS-1$

	// view parts
	public static final String DISPLAY_VIEW= PREFIX + "display_view_context"; //$NON-NLS-1$


	// Preference/Property pages
	public static final String SOURCE_ATTACHMENT_PROPERTY_PAGE= PREFIX + "source_attachment_property_page_context"; //$NON-NLS-1$
	public static final String JRE_PREFERENCE_PAGE= PREFIX + "jre_preference_page_context"; //$NON-NLS-1$
	public static final String SOURCE_LOOKUP_PROPERTY_PAGE= PREFIX + "source_lookup_property_page_context"; //$NON-NLS-1$
	public static final String LAUNCH_JRE_PROPERYY_PAGE= PREFIX + "launch_jre_property_page_context"; //$NON-NLS-1$

	// Wizard pages
	public static final String JAVA_APPLICATION_WIZARD_PAGE= PREFIX + "java_application_page_context"; //$NON-NLS-1$
	public static final String JDI_ATTACH_LAUNCHER_WIZARD_PAGE= PREFIX + "jdi_attach_launcher_page_context"; //$NON-NLS-1$

	// reused ui-blocks
	public static final String SOURCE_ATTACHMENT_BLOCK= PREFIX + "source_attachment_context"; //$NON-NLS-1$
	
	
}