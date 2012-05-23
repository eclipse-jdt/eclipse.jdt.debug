/**********************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.osgi.util.NLS;

public class InstructionsEvaluationMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionsEvaluationMessages";//$NON-NLS-1$

	public static String AndAssignmentOperator_operator_1;
	public static String AndOperator_____operator_1;
	public static String ArrayAccess_0;

	public static String ArrayAccess_array_access_1;
	public static String ArrayAccess_illegal_index;
	public static String ArrayAllocation_array_allocation_1;
	public static String ArrayInitializerInstruction_array_initializer_1;
	public static String AssignmentOperator_operator_1;
	public static String Cast_ClassCastException__Cannot_cast__0__as__1___1;
	public static String Cast_cast_3;
	public static String ConditionalJump_conditional_jump_1;
	public static String Constructor_constructor__1;
	public static String LocalVariableCreation_create_local_variable__0___1___1;
	public static String DivideAssignmentOperator_operator_1;
	public static String DivideOperator_Divide_by_zero_1;
	public static String DivideOperator_Divide_by_zero_2;
	public static String DivideOperator______operator_3;
	public static String EqualEqualOperator_operator_1;
	public static String GreaterEqualOperator_operator_1;
	public static String GreaterOperator______operator_1;
	public static String InstanceOfOperator__instanceof___operator_3;

	public static String Jump_jump_1;
	public static String LeftShiftAssignmentOperator_operator_1;
	public static String LeftShiftOperator_______operator_1;
	public static String LessEqualOperator_operator_1;
	public static String LessOperator______operator_1;
	public static String MinusAssignmentOperator_operator_1;
	public static String MinusOperator______operator_1;
	public static String MultiplyAssignmentOperator_operator_1;
	public static String MultiplyOperator______operator_1;
	public static String NoOp_no_op_1;
	public static String NotOperator______operator_1;
	public static String OrAssignmentOperator_operator_1;
	public static String OrOperator______operator_1;
	public static String PlusAssignmentOperator_operator_1;
	public static String PlusOperator______operator_2;
	public static String Pop_pop_1;
	public static String PostfixMinusMinusOperator_postfix________operator_1;
	public static String PostfixPlusPlusOperator_postfix________operator_1;
	public static String PrefixMinusMinusOperator_prefix________operator_1;
	public static String PrefixPlusPlusOperator_prefix________operator_1;
	public static String PushBoolean_push__1;
	public static String PushFieldVariable_0;
	public static String PushChar_push__1;
	public static String PushClassLiteralValue_push_class_literal_value_1;
	public static String PushDouble_push__1;
	public static String PushFieldVariable_Cannot_find_the_field__0__for_the_object__1__1;
	public static String PushFieldVariable_push_field__0__2;
	public static String PushArrayLength_push_array_length__1;
	public static String PushFloat_push__1;
	public static String PushInt_push__1;
	public static String PushLong_push__1;
	public static String PushNull_push___null__1;
	public static String PushString_push__1;
	public static String PushThis_push___this__1;
	public static String PushThis_Unable_to_retrieve_the_correct_enclosing_instance_of__this__2;
	public static String PushType_push_type__1;
	public static String RemainderAssignmentOperator_operator_1;
	public static String RemainderOperator_Divide_by_zero_1;
	public static String RemainderOperator_Divide_by_zero_2;
	public static String RemainderOperator______operator_3;
	public static String ReturnInstruction_return;
	public static String RightShiftAssignmentOperator_operator_1;
	public static String RightShiftOperator_______operator_1;
	public static String TwiddleOperator______operator_1;
	public static String UnaryMinusOperator_unary_minus_operator_1;
	public static String UnaryPlusOperator_unary_plus_operator_1;
	public static String UnsignedRightShiftAssignmentOperator_operator_1;
	public static String UnsignedRightShiftOperator________operator_1;
	public static String XorAssignmentOperator_operator_1;
	public static String XorOperator______operator_1;
	public static String PushLocalVariable_Cannot_find_the_variable____1;
	public static String PushLocalVariable_push____0___2;
	public static String PushStaticFieldVariable_Cannot_find_the_field__0__in__1__1;
	public static String PushStaticFieldVariable_push_static_field__0__2;
	public static String SendMessage_Attempt_to_send_a_message_to_a_non_object_value_1;
	public static String SendMessage_send_message__0___1__2;
	public static String SendStaticMessage_Cannot_send_a_static_message_to_a_non_class_type_object_1;
	public static String SendStaticMessage_send_static_message__0___1__2;
	public static String Instruction_No_type;
	public static String InstanceOfOperator_No_class_object;
	public static String Cast_No_class_object;
	public static String LocalVariableCreation_Execution_failed___VM_disconnected__1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME,
				InstructionsEvaluationMessages.class);
	}
}