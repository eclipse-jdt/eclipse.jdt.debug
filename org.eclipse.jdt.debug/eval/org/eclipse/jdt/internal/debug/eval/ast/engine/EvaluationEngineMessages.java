/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.osgi.util.NLS;

public class EvaluationEngineMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.eval.ast.engine.EvaluationEngineMessages";//$NON-NLS-1$

	public static String ASTInstructionCompiler_Anonymous_type_declaration_cannot_be_used_in_an_evaluation_expression_2;
	public static String ASTInstructionCompiler_Assert_statement_cannot_be_used_in_an_evaluation_expression_3;
	public static String ASTInstructionCompiler_Unrecognized_assignment_operator____4;

	public static String ASTInstructionCompiler_Catch_clause_cannot_be_used_in_an_evaluation_expression_6;
	public static String ASTInstructionCompiler_Anonymous_type_declaration_cannot_be_used_in_an_evaluation_expression_7;
	public static String ASTInstructionCompiler_Constructor_of_a_local_type_cannot_be_used_in_an_evaluation_expression_8;
	public static String ASTInstructionCompiler_this_constructor_invocation_cannot_be_used_in_an_evaluation_expression_9;
	public static String ASTInstructionCompiler_Error_in_type_declaration_statement;
	public static String ASTInstructionCompiler_Unrecognized_infix_operator____13;

	public static String ASTInstructionCompiler_unrecognized_postfix_operator____15;
	public static String ASTInstructionCompiler_unrecognized_prefix_operator____16;

	public static String ASTInstructionCompiler_super_constructor_invocation_cannot_be_used_in_an_evaluation_expression_19;
	public static String ASTInstructionCompiler_Try_statement_cannot_be_used_in_an_evaluation_expression_23;
	public static String ASTInstructionCompiler_Type_declaration_cannot_be_used_in_an_evaluation_expression_24;
	public static String ASTInstructionCompiler_Type_declaration_statement_cannot_be_used_in_an_evaluation_expression_25;
	public static String ASTInstructionCompiler_Local_type_array_instance_creation_cannot_be_used_in_an_evaluation_expression_29;
	public static String ASTInstructionCompiler_Constructor_which_contains_a_local_type_as_parameter_cannot_be_used_in_an_evaluation_expression_30;
	public static String ASTInstructionCompiler_Qualified_local_type_field_access_cannot_be_used_in_an_evaluation_expression_31;
	public static String ASTInstructionCompiler_Method_which_contains_a_local_type_as_parameter_cannot_be_used_in_an_evaluation_expression_32;
	public static String ASTInstructionCompiler_Must_explicitly_qualify_the_allocation_with_an_instance_of_the_enclosing_type_33;
	public static String ASTEvaluationEngine_Evaluations_must_contain_either_an_expression_or_a_block_of_well_formed_statements_1;

	public static String InterpreterVariable_setValue_String__not_supported_for_interpreter_variable_1;
	public static String InterpreterVariable_verifyValue_IValue__not_supported_for_interpreter_variable_2;
	public static String InterpreterVariable_verifyValue_String__not_supported_for_interpreter_variable_3;

	public static String ASTEvaluationEngine_AST_evaluation_engine_cannot_evaluate_expression;
	public static String ASTEvaluationEngine_An_unknown_error_occurred_during_evaluation;
	public static String ASTEvaluationEngine_Cannot_perform_nested_evaluations;
	public static String ASTInstructionCompiler_3;
	public static String ASTInstructionCompiler_36;
	public static String ASTInstructionCompiler_0;
	public static String ASTInstructionCompiler_1;
	public static String ASTInstructionCompiler_2;
	public static String ASTInstructionCompiler_5;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, EvaluationEngineMessages.class);
	}

	public static String ASTEvaluationEngine_0;
	public static String ASTEvaluationEngine_1;
	public static String ArrayRuntimeContext_0;
}
