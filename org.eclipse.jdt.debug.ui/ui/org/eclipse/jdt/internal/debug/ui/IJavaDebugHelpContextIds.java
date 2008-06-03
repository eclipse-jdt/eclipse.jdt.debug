/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


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
	public static final String STACK_TRACE_CONSOLE = PREFIX + "stack_trace_console_context"; //$NON-NLS-1$

	//dialogs
	public static final String JRE_DETAILS_DIALOG= PREFIX + "jre_details_dialog_context"; //$NON-NLS-1$
	public static final String MAIN_TYPE_SELECTION_DIALOG= PREFIX + "main_type_selection_dialog_context"; //$NON-NLS-1$
	public static final String EDIT_DETAIL_FORMATTER_DIALOG= PREFIX + "edit_detail_formatter_dialog_context"; //$NON-NLS-1$
	public static final String EDIT_LOGICAL_STRUCTURE_DIALOG= PREFIX + "edit_logical_structure_dialog_context"; //$NON-NLS-1$
	public static final String INSTANCE_BREAKPOINT_SELECTION_DIALOG= PREFIX + "instance_breakpoint_selection_dialog_context"; //$NON-NLS-1$
	public static final String SNIPPET_IMPORTS_DIALOG= PREFIX + "snippet_imports_dialog_context"; //$NON-NLS-1$
	public static final String ADD_EXCEPTION_DIALOG= PREFIX + "add_exception_dialog_context"; //$NON-NLS-1$
	public static final String DETAIL_DISPLAY_OPTIONS_DIALOG= PREFIX + "detail_options_dialog_context"; //$NON-NLS-1$
	public static final String SELECT_MAIN_METHOD_DIALOG = PREFIX + "select_main_method_dialog"; //$NON-NLS-1$
	public static final String EXPRESSION_INPUT_DIALOG = PREFIX + "expression_input_dialog"; //$NON-NLS-1$
	public static final String STRING_VALUE_INPUT_DIALOG = PREFIX + "string_value_input_dialog"; //$NON-NLS-1$
	public static final String DEFAULT_INPUT_DIALOG = PREFIX + "default_input_dialog"; //$NON-NLS-1$
	public static final String SELECT_PROJECT_DIALOG = PREFIX + "select_project_dialog"; //$NON-NLS-1$

	// Preference/Property pages
	public static final String JRE_PREFERENCE_PAGE= PREFIX + "jre_preference_page_context"; //$NON-NLS-1$
	public static final String JRE_PROFILES_PAGE= PREFIX + "jre_profiles_page_context"; //$NON-NLS-1$
	public static final String LAUNCH_JRE_PROPERTY_PAGE= PREFIX + "launch_jre_property_page_context"; //$NON-NLS-1$
	public static final String JAVA_DEBUG_PREFERENCE_PAGE= PREFIX + "java_debug_preference_page_context"; //$NON-NLS-1$	
	public static final String JAVA_STEP_FILTER_PREFERENCE_PAGE= PREFIX + "java_step_filter_preference_page_context"; //$NON-NLS-1$
	public static final String JAVA_BREAKPOINT_PREFERENCE_PAGE= PREFIX + "java_breakpoint_preference_page_context"; //$NON-NLS-1$
	public static final String JAVA_DETAIL_FORMATTER_PREFERENCE_PAGE= PREFIX + "java_detail_formatter_preference_page_context"; //$NON-NLS-1$
	public static final String JAVA_PRIMITIVES_PREFERENCE_PAGE= PREFIX + "java_primitives_preference_page_context"; //$NON-NLS-1$
	public static final String JAVA_LOGICAL_STRUCTURES_PAGE = PREFIX + "java_logical_structures_page"; //$NON-NLS-1$
	public static final String VMCAPABILITIES_PROPERTY_PAGE = PREFIX + "vm_capabilities_property_page"; //$NON-NLS-1$
	public static final String JAVA_HEAPWALKING_PREFERENCE_PAGE = PREFIX + "java_heapwalking_preference_page"; //$NON-NLS-1$
	public static final String JAVA_BREAKPOINT_PROPERTY_PAGE = PREFIX + "java_breakpoint_property_page"; //$NON-NLS-1$
	public static final String JAVA_BREAKPOINT_ADVANCED_PROPERTY_PAGE = PREFIX + "java_breakpoint_advanced_property_page"; //$NON-NLS-1$
	public static final String JAVA_EXCEPTION_BREAKPOINT_PROPERTY_PAGE = PREFIX + "java_exception_breakpoint_property_page"; //$NON-NLS-1$
	public static final String JAVA_EXCEPTION_BREAKPOINT_FILTERING_PROPERTY_PAGE = PREFIX + "java_exception_breakpoint_filtering_property_page"; //$NON-NLS-1$
	public static final String JAVA_LINE_BREAKPOINT_PROPERTY_PAGE = PREFIX + "java_line_breakpoint_property_page"; //$NON-NLS-1$
	
	// reused ui-blocks
	public static final String SOURCE_ATTACHMENT_BLOCK= PREFIX + "source_attachment_context"; //$NON-NLS-1$
	public static final String WORKING_DIRECTORY_BLOCK= PREFIX + "working_directory_context"; //$NON-NLS-1$

	// application launch configuration dialog tabs
	public static final String LAUNCH_CONFIGURATION_DIALOG_ARGUMENTS_TAB= PREFIX + "launch_configuration_dialog_arguments_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB= PREFIX + "launch_configuration_dialog_classpath_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_CONNECT_TAB= PREFIX + "launch_configuration_dialog_connect_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_JRE_TAB= PREFIX + "launch_configuration_dialog_jre_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB= PREFIX + "launch_configuration_dialog_main_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_SOURCE_TAB= PREFIX + "launch_configuration_dialog_source_tab"; //$NON-NLS-1$
	
	// applet launch configuration dialog tabs
	public static final String LAUNCH_CONFIGURATION_DIALOG_APPLET_MAIN_TAB = PREFIX + "launch_configuration_dialog_applet_main_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_APPLET_ARGUMENTS_TAB = PREFIX + "launch_configuration_dialog_applet_arguments_tab"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIGURATION_DIALOG_APPLET_PARAMETERS_TAB = PREFIX +"launch_configuration_dialog_applet_parameters_tab"; //$NON-NLS-1$

	//actions
	public static final String STEP_INTO_SELECTION_ACTION = PREFIX + "step_into_selection_action"; //$NON-NLS-1$
	public static final String SHOW_STATICS_ACTION = PREFIX + "show_static_action_context"; //$NON-NLS-1$
	public static final String SHOW_CONSTANTS_ACTION = PREFIX + "show_constants_action_context"; //$NON-NLS-1$
	public static final String CLEAR_DISPLAY_VIEW_ACTION = PREFIX + "clear_display_view_action_context"; //$NON-NLS-1$
	public static final String TERMINATE_SCRAPBOOK_VM_ACTION = PREFIX + "terminate_scrapbook_vm_action_context"; //$NON-NLS-1$
	public static final String SCRAPBOOK_IMPORTS_ACTION = PREFIX + "scrapbook_imports_action_context"; //$NON-NLS-1$
	public static final String CONSOLE_AUTOFORMAT_STACKTRACES_ACTION = PREFIX + "console_autoformat_stacktraces_action"; //$NON-NLS-1$
	
	//wizards
	public static final String ADD_NEW_JRE_WIZARD_PAGE= PREFIX + "add_new_jre_wizard_page_context"; //$NON-NLS-1$
	public static final String EDIT_JRE_STD_VM_WIZARD_PAGE= PREFIX + "edit_std_vm_jre_wizard_page_context"; //$NON-NLS-1$
	public static final String EDIT_JRE_EE_FILE_WIZARD_PAGE= PREFIX + "edit_ee_file_jre_wizard_page_context"; //$NON-NLS-1$
	public static final String NEW_SNIPPET_WIZARD_PAGE= PREFIX + "new_snippet_wizard_page_context"; //$NON-NLS-1$
}
