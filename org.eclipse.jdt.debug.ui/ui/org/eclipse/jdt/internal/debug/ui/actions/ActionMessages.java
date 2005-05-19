/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.ui.actions.ActionMessages";//$NON-NLS-1$

	public static String BreakpointAction_Breakpoint_configuration_1;
	public static String BreakpointAction_Exceptions_occurred_attempting_to_modify_breakpoint__2;

	public static String BreakpointHitCountAction__Enter_the_new_hit_count_for_the_breakpoint__3;
	public static String BreakpointHitCountAction_Enable_Hit_Count_1;
	public static String BreakpointHitCountAction_Exception_occurred_attempting_to_set_hit_count_1;
	public static String BreakpointHitCountAction_Set_Breakpoint_Hit_Count_2;
	public static String BreakpointHitCountAction_Value_must_be_positive_integer;

	public static String BreakpointSuspendPolicy_Suspend__Thread_2;
	public static String BreakpointSuspendPolicy_Suspend__VM_1;

	public static String DisplayAction_no_result_value;
	public static String DisplayAction_result_pattern;
	public static String DisplayAction_type_name_pattern;

	public static String EnableDisableBreakpointRulerAction__Disable_Breakpoint_4;
	public static String EnableDisableBreakpointRulerAction__Enable_Breakpoint_1;
	public static String EnableDisableBreakpointRulerAction__Enable_Breakpoint_5;
	public static String EnableDisableBreakpointRulerAction_0;
	public static String EnableDisableBreakpointRulerAction_Enabling_disabling_breakpoints_2;
	public static String EnableDisableBreakpointRulerAction_Exceptions_occurred_enabling_disabling_the_breakpoint_3;

	public static String Evaluate_error_message_direct_exception;
	public static String Evaluate_error_message_exception_pattern;
	public static String Evaluate_error_message_src_context;
	public static String Evaluate_error_message_stack_frame_context;
	public static String Evaluate_error_message_wrapped_exception;
	public static String Evaluate_error_problem_append_pattern;
	public static String Evaluate_error_title_eval_problems;
	public static String EvaluateAction_Cannot_open_Display_view;
	public static String EvaluateAction__evaluation_failed__1;
	public static String EvaluateAction__evaluation_failed__Reason;
	public static String EvaluateAction_Thread_not_suspended___unable_to_perform_evaluation__1;
	public static String EvaluateAction_Cannot_perform_nested_evaluations__1;

	public static String InspectAction_Exception_occurred_inspecting_variable;

	public static String JavaBreakpointPropertiesRulerAction_Breakpoint__Properties_1;

	public static String ManageBreakpointRulerAction_label;
	public static String ManageBreakpointRulerAction_error_adding_message1;

	public static String ManageMethodBreakpointActionDelegate_CantAdd;

	public static String ManageWatchpointActionDelegate_CantAdd;
	public static String OpenTypeAction_2;

	public static String MoveDownAction_M_ove_Down_1;
	public static String MoveUpAction_Move_U_p_1;
	public static String RemoveAction__Remove_1;

	public static String AddProjectAction_Add_Project_1;
	public static String AddProjectAction_Project_Selection_2;
	public static String AddProjectAction_Choose__project_s__to_add__3;
	public static String AddProjectAction_One_or_more_exceptions_occurred_while_adding_projects__1;

	public static String ProjectSelectionDialog_Add_exported_entries_of_selected_projects__1;
	public static String ProjectSelectionDialog_Add_required_projects_of_selected_projects__2;

	public static String AddJarAction_Add__JARs_1;
	public static String AddJarAction_JAR_Selection_7;
	public static String AddJarAction_Choose_jars_to_add__8;

	public static String AddExternalJar_Add_E_xternal_JARs_1;
	public static String AddExternalJar_Jar_Selection_3;

	public static String AddFolderAction_Add__Folders_1;
	public static String AddFolderAction_Selection_must_be_a_folder_2;
	public static String AddFolderAction_Folder_Selection_4;
	public static String AddFolderAction_Choose_folders_to_add__5;
	public static String AddFolderAction_Classpath_already_includes_selected_folder_s___1;

	public static String AddExternalFolderAction_Add_External_Folder_1;
	public static String AddExternalFolderAction_Folder_Selection_3;

	public static String AddVariableAction_Add_Variables_1;

	public static String AttachSourceAction_2;
	public static String AttachSourceAction_3;

	public static String AddAdvancedAction_Ad_vanced____1;

	public static String TerminateEvaluationActionTerminate_Evaluation_1;
	public static String TerminateEvaluationActionAttempts_to_terminate_an_evaluation_can_only_stop_a_series_of_statements__The_currently_executing_statement__such_as_a_method_invocation__cannot_be_interrupted__2;
	public static String TerminateEvaluationActionAn_exception_occurred_while_terminating_the_evaluation_3;

	public static String InstanceFiltersAction_1;
	public static String InstanceFiltersAction_2;
	public static String InstanceFiltersAction_3;
	public static String InstanceFiltersAction_Yes_2;
	public static String InstanceFiltersAction_Cancel_3;

	public static String RestoreDefaultEntriesAction_0;

	public static String StepIntoSelectionHandler_Execution_did_not_enter____0____before_the_current_method_returned__1;

	public static String StepIntoSelectionActionDelegate_No_Method;

	public static String PrimitiveOptionsDialog_Primitive_Type_Display_Options_1;

	public static String StepIntoSelectionActionDelegate_Step_into_selection_only_available_for_types_in_Java_projects__1;
	public static String StepIntoSelectionActionDelegate_Step_into_selection_only_available_in_top_stack_frame__3;
	public static String StepIntoSelectionActionDelegate_Step_into_selection_only_available_in_Java_editor__4;
	public static String StepIntoSelectionActionDelegate_4;

	public static String ObjectActionDelegate_Unable_to_display_type_hierarchy__The_selected_source_element_is_not_contained_in_the_workspace__1;
	public static String ManageBreakpointRulerAction_Breakpoints_can_only_be_created_within_the_type_associated_with_the_editor___0___1;
	public static String BreakpointLocationVerifierJob_breakpoint_location;
	public static String BreakpointLocationVerifierJob_not_valid_location;
	public static String BreakpointLocationVerifierJob_breakpoint_set;
	public static String ManageMethodBreakpointActionDelegate_methodNonAvailable;
	public static String BreakpointLocationVerifierJob_breakpointRemoved;
	public static String BreakpointLocationVerifierJob_breakpointMovedToValidPosition;
	public static String BreakpointLocationVerifierJob_breakpointSetToRightType;
	public static String PopupDisplayAction_2;

	public static String RunToLineAdapter_0;
	public static String RunToLineAdapter_1;
	public static String RunToLineAdapter_2;
	public static String RunToLineAdapter_3;
	public static String RunToLineAdapter_4;
	public static String ToggleBreakpointAdapter_9;
	public static String ToggleBreakpointAdapter_0;
	public static String ToggleBreakpointAdapter_2;
	public static String ToggleBreakpointAdapter_3;
	public static String ToggleBreakpointAdapter_10;
	public static String ToggleBreakpointAdapter_1;
	public static String StepIntoSelectionHandler_1;
	public static String StepIntoSelectionHandler_2;
	public static String AddLibraryAction_0;
	public static String ShowStratumAction_0;
	public static String JavaVariableValueEditor_0;
	public static String JavaVariableValueEditor_1;
	public static String JavaObjectValueEditor_0;
	public static String JavaObjectValueEditor_1;
	public static String JavaObjectValueEditor_2;
	public static String JavaObjectValueEditor_3;
	public static String JavaPrimitiveValueEditor_0;
	public static String JavaPrimitiveValueEditor_1;
	public static String JavaPrimitiveValueEditor_2;
	public static String JavaPrimitiveValueEditor_3;
	public static String JavaPrimitiveValueEditor_4;
	public static String ExpressionInputDialog_0;
	public static String ExpressionInputDialog_1;
	public static String ExpressionInputDialog_2;
	public static String ExpressionInputDialog_3;
	public static String StringValueInputDialog_0;
	public static String StringValueInputDialog_1;
	public static String StringValueInputDialog_2;
	public static String StringValueInputDialog_3;
	public static String StringValueInputDialog_4;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
	}
}