/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import org.eclipse.osgi.util.NLS;

public class JDIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdi.internal.JDIMessages";//$NON-NLS-1$

	public static String ArrayReferenceImpl_Invalid_index_of_array_reference_given_1;
	public static String ArrayReferenceImpl_Invalid_ArrayReference_Value_tag_encountered___2;
	public static String ArrayReferenceImpl_Attempted_to_set_more_values_in_array_than_length_of_array_3;
	public static String ArrayReferenceImpl_Attempted_to_set_more_values_in_array_than_given_4;
	public static String ArrayReferenceImpl__Garbage_Collected__ArrayReference_5;
	public static String ArrayTypeImpl_getValues_not_allowed_on_array_1;
	public static String ArrayTypeImpl_No_source_name_for_Arrays_1;

	public static String class_or_object_not_known;
	public static String ClassTypeImpl_Class_does_not_contain_given_method_1;
	public static String ClassTypeImpl_Number_of_arguments_doesn__t_match_2;
	public static String ClassTypeImpl_Method_is_constructor_or_intitializer_3;
	public static String ClassTypeImpl_Class_does_not_contain_given_method_4;
	public static String ClassTypeImpl_Number_of_arguments_doesn__t_match_5;
	public static String ClassTypeImpl_Method_is_not_a_constructor_6;
	public static String FieldImpl_Can__t_compare_field_to_given_object_1;
	public static String FieldImpl_Got_FieldID_of_ReferenceType_that_is_not_a_member_of_the_ReferenceType_2;
	public static String LocalVariableImpl_Can__t_compare_local_variable_to_given_object_1;
	public static String LocalVariableImpl_Code_indexes_are_assumed_to_be_always_positive_2;
	public static String LocalVariableImpl_The_stack_frame__s_method_does_not_match_this_variable__s_method_3;
	public static String LocalVariableImpl_Code_indexes_are_assumed_to_be_always_positive_4;
	public static String LocationImpl_Can__t_compare_location_to_given_object_1;
	public static String LocationImpl_Code_indexes_are_assumed_to_be_always_positive_2;
	public static String LocationImpl_sourcename___0___line___1__3;
	public static String MethodImpl_Got_empty_line_number_table_for_this_method_1;
	public static String MethodImpl_No_line_number_information_available_2;
	public static String MethodImpl_Got_empty_line_number_table_for_this_method_3;
	public static String MethodImpl_Invalid_code_index_of_a_location_given_4;
	public static String MethodImpl_Can__t_compare_method_to_given_object_6;
	public static String MethodImpl_No_local_variable_information_available_9;
	public static String MethodImpl_Got_MethodID_of_ReferenceType_that_is_not_a_member_of_the_ReferenceType_10;
	public static String MethodImpl_No_valid_location_at_the_specified_code_index__0__2;

	public static String MirrorImpl_Got_error_code_in_reply___1;
	public static String MirrorImpl_Got_invalid_data___2;

	public static String ObjectReferenceImpl_object_not_known;

	public static String ObjectReferenceImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_1;
	public static String ObjectReferenceImpl_Class_does_not_contain_given_method_2;
	public static String ObjectReferenceImpl_Number_of_arguments_doesn__t_match_3;
	public static String ObjectReferenceImpl_Method_is_constructor_or_intitializer_4;
	public static String ObjectReferenceImpl_Method_is_abstract_and_can_therefore_not_be_invoked_nonvirtual_5;
	public static String ObjectReferenceImpl_One_of_the_arguments_of_ObjectReference_invokeMethod___6;
	public static String ObjectReferenceImpl__Garbage_Collected__ObjectReference__8;
	public static String ObjectReferenceImpl_Invalid_ObjectID_tag_encountered___9;

	public static String PrimitiveTypeImpl_Invalid_primitive_signature____1;
	public static String PrimitiveTypeImpl___2;
	public static String PrimitiveTypeImpl_A_PrimitiveType_does_not_have_modifiers_3;
	public static String PrimitiveValueImpl_Invalid_Primitive_Value_encountered_1;
	public static String PrimitiveValueImpl_Invalid_Primitive_Value_tag_encountered___2;

	public static String ReferenceTypeImpl_26;
	public static String ReferenceTypeImpl_27;
	public static String ReferenceTypeImpl_no_class_version_support24;
	public static String ReferenceTypeImpl_no_constant_pool_support;
	public static String ReferenceTypeImpl_Obsolete_method_1;
	public static String ReferenceTypeImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_3;
	public static String ReferenceTypeImpl_Can__t_compare_reference_type_to_given_object_4;
	public static String ReferenceTypeImpl_Source_name_is_not_known_7;
	public static String ReferenceTypeImpl_Invalid_ReferenceTypeID_tag_encountered___8;
	public static String ReferenceTypeImpl_Type_has_not_been_loaded_10;

	public static String StackFrameImpl_no_argument_values_available;
	public static String StackFrameImpl_Retrieved_a_different_number_of_values_from_the_VM_than_requested_1;
	public static String StringReferenceImpl__Garbage_Collected__StringReference__3;

	public static String ThreadReferenceImpl_incapatible_return_type;
	public static String ThreadReferenceImpl_no_force_early_return_on_threads;
	public static String ThreadReferenceImpl_thread_cannot_force_native_method;
	public static String ThreadReferenceImpl_thread_no_stackframes;
	public static String ThreadReferenceImpl_thread_not_suspended;
	public static String ThreadReferenceImpl_thread_object_invalid;
	public static String ThreadReferenceImpl_thread_or_value_unknown;
	public static String ThreadReferenceImpl_Thread_was_not_suspended_1;
	public static String ThreadReferenceImpl_Invalid_index_of_stack_frames_given_4;
	public static String ThreadReferenceImpl_Thread_was_not_suspended_5;
	public static String ThreadReferenceImpl_Unknown_thread_status_received___6;
	public static String ThreadReferenceImpl_Stop_argument_not_an_instance_of_java_lang_Throwable_in_the_target_VM_7;
	public static String ThreadReferenceImpl_8;
	public static String ThreadReferenceImpl__Garbage_Collected__ThreadReference__9;
	public static String ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame_from_the_call_stack__Reasons_include__The_frame_id_was_invalid__The_thread_was_resumed__10;
	public static String ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame__The_requested_stack_frame_is_not_suspended_11;
	public static String ThreadReferenceImpl_Unable_to_pop_the_requested_stack_frame_from_the_call_stack__Reasons_include__The_requested_frame_was_the_last_frame_on_the_call_stack__The_requested_frame_was_the_last_frame_above_a_native_frame__12;
	public static String ThreadReferenceImpl_vm_read_only;
	public static String TypeImpl__Unloaded_Type__1;
	public static String TypeImpl_Can__t_covert_method_signature_to_tag___9;
	public static String TypeImpl_Invalid_signature____10;
	public static String TypeImpl___11;
	public static String TypeImpl_Can__t_convert_method_signature_to_name_2;
	public static String ValueImpl_Invalid_Value_tag_encountered___1;

	public static String VirtualMachineImpl_2;
	public static String VirtualMachineImpl_3;
	public static String VirtualMachineImpl_count_less_than_zero;
	public static String VirtualMachineImpl_Target_VM__0__does_not_support_Hot_Code_Replacement_1;
	public static String VirtualMachineImpl_Failed_to_get_ID_sizes_2;
	public static String VirtualMachineImpl_Invalid_result_flag_in_Classes_Have_Changed_response___3;
	public static String VirtualMachineImpl__4;
	public static String VirtualMachineImpl_0;
	public static String VirtualMachineImpl_1;
	public static String VirtualMachineManagerImpl_Could_not_open_verbose_file___1;
	public static String VirtualMachineManagerImpl_____2;

	public static String vm_dead;
	public static String VoidTypeImpl_A_VoidType_does_not_have_modifiers_1;
	public static String VirtualMachineImpl_Add_method_not_implemented_1;
	public static String VirtualMachineImpl_Scheme_change_not_implemented_2;
	public static String VirtualMachineImpl_Hierarchy_change_not_implemented_3;
	public static String VirtualMachineImpl_Delete_method_not_implemented_4;
	public static String VirtualMachineImpl_Class_modifiers_change_not_implemented_5;
	public static String VirtualMachineImpl_Method_modifiers_change_not_implemented_6;
	public static String VerboseWriter___unknown_value__1;
	public static String VerboseWriter__unknown_bit__2;
	public static String VerboseWriter__none__4;
	public static String ArrayReferenceImpl_Invalid_index_1;
	public static String ArrayReferenceImpl_Invalid_srcIndex_2;
	public static String ArrayReferenceImpl_Invalid_number_of_value_to_set_in_array_3;
	public static String ValueImpl_Type_of_the_value_not_compatible_with_the_expected_type__1;
	public static String ArrayReferenceImpl_Invalid_number_of_value_to_get_from_array_1;
	public static String ArrayReferenceImpl_Attempted_to_get_more_values_from_array_than_length_of_array_2;
	public static String ReferenceTypeImpl_28;
	public static String ReferenceTypeImpl_29;
	public static String ReferenceTypeImpl_30;
	public static String ReferenceTypeImpl_31;
	public static String ReferenceTypeImpl_32;
	public static String ReferenceTypeImpl_34;
	public static String SourceDebugExtensionParser_0;
	public static String SourceDebugExtensionParser_2;
	public static String SourceDebugExtensionParser_3;
	public static String SourceDebugExtensionParser_4;
	public static String SourceDebugExtensionParser_5;
	public static String SourceDebugExtensionParser_6;
	public static String SourceDebugExtensionParser_7;
	public static String SourceDebugExtensionParser_8;
	public static String SourceDebugExtensionParser_9;
	public static String SourceDebugExtensionParser_10;
	public static String SourceDebugExtensionParser_11;
	public static String SourceDebugExtensionParser_12;
	public static String SourceDebugExtensionParser_13;
	public static String SourceDebugExtensionParser_14;
	public static String SourceDebugExtensionParser_16;
	public static String SourceDebugExtensionParser_17;
	public static String SourceDebugExtensionParser_19;
	public static String SourceDebugExtensionParser_22;
	public static String SourceDebugExtensionParser_23;
	public static String SourceDebugExtensionParser_24;
	public static String SourceDebugExtensionParser_25;
	public static String SourceDebugExtensionParser_26;
	public static String SourceDebugExtensionParser_27;
	public static String SourceDebugExtensionParser_28;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, JDIMessages.class);
	}
}
