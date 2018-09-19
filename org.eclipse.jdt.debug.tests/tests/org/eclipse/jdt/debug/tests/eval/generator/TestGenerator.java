/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.eval.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;

public class TestGenerator {

	static protected int getPromotionType(int left, int right) {
		return fTypeTable[left][right];
	}

	static protected int getUnaryPromotionType(int type) {
		return fTypeTable[type][T_int];
	}

	static protected boolean needCast(int type1, int type2) {
		if (type1 == type2) {
			return false;
		}
		switch (type1) {
			case T_short:
			case T_byte:
				return type1 < type2 || type2 == T_char;
			case T_char:
				return true;
			case T_int:
				return type2 >= T_long;
			case T_long:
				return type2 == T_float || type2 == T_double;
			case T_float:
				return type2 == T_double;
			case T_double:
				return false;
			default:
				return true;
		}
	}

	static final int T_undefined = 0;
	static final int T_object = 1;
	static final int T_char = 2;
	static final int T_byte = 3;
	static final int T_short = 4;
	static final int T_boolean = 5;
	static final int T_void = 6;
	static final int T_long = 7;
	static final int T_double = 8;
	static final int T_float = 9;
	static final int T_int = 10;
	static final int T_String = 11;
	static final int T_null = 12;

	private static final int[][] fTypeTable= {
/* undefined */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined},
/* object */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
/* char */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* byte */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* short */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* boolean */	{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_boolean, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
/* void */		{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined},
/* long */		{T_undefined, T_undefined, T_long, T_long, T_long, T_undefined, T_undefined, T_long, T_double, T_float, T_long, T_String, T_undefined},
/* double */	{T_undefined, T_undefined, T_double, T_double, T_double, T_undefined, T_undefined, T_double, T_double, T_double, T_double, T_String, T_undefined},
/* float */		{T_undefined, T_undefined, T_float, T_float, T_float, T_undefined, T_undefined, T_float, T_double, T_float, T_float, T_String, T_undefined},
/* int */		{T_undefined, T_undefined, T_int, T_int, T_int, T_undefined, T_undefined, T_long, T_double, T_float, T_int, T_String, T_undefined},
/* String */	{T_undefined, T_String, T_String, T_String, T_String, T_String, T_undefined, T_String, T_String, T_String, T_String, T_String, T_String},
/* null */		{T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_undefined, T_String, T_undefined},
	};

	static final String[] fTypeName = new String[] {"undefined", "java.lang.Object", "char", "byte", "short", "boolean", "void", "long", "double", "float", "int", "java.lang.String", "null"};

	static final String[] typeUpperName = new String[] {"Undefined", "Object", "Char", "Byte", "Short", "Boolean", "Void", "Long", "Double", "Float", "Int", "String", "Null"};

	static final int Op_plus = 0;
	static final int Op_minus = 1;
	static final int Op_multiply = 2;
	static final int Op_divide = 3;
	static final int Op_remainder = 4;
	static final int Op_greater = 5;
	static final int Op_greaterEqual = 6;
	static final int Op_less = 7;
	static final int Op_lessEqual = 8;
	static final int Op_equalEqual = 9;
	static final int Op_notEqual = 10;
	static final int Op_leftShift = 11;
	static final int Op_rightShift = 12;
	static final int Op_unsignedRightShift = 13;
	static final int Op_or = 14;
	static final int Op_and = 15;
	static final int Op_xor = 16;
	static final int Op_twiddle = 17;
	static final int Op_not = 18;
	static final int Op_equal = 19;
	static final int Op_plusAss = 20;
	static final int Op_minusAss = 21;
	static final int Op_multiplyAss = 22;
	static final int Op_divideAss = 23;
	static final int Op_remainderAss = 24;
	static final int Op_leftShiftAss = 25;
	static final int Op_rightShiftAss = 26;
	static final int Op_unsignedRightShiftAss = 27;
	static final int Op_orAss = 28;
	static final int Op_andAss = 29;
	static final int Op_xorAss = 30;
	static final int Op_prefixPlusPlus = 31;
	static final int Op_postfixPlusPlus = 32;
	static final int Op_prefixMinusMinus = 33;
	static final int Op_postfixMinusMinus = 34;

	static final String[] opSymbol = new String[] {"+", "-", "*", "/", "%", ">", ">=", "<", "<=", "==", "!=", "<<", ">>", ">>>", "|", "&", "^", "~", "!", "=", "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", ">>>=", "|=", "&=", "^=", "++", "++", "--", "--"};

	static final String[] opName = new String[] {"plus", "minus", "multiply", "divide", "remainder", "greater", "greaterEqual", "less", "lessEqual", "equalEqual", "notEqual", "leftShift", "rightShift", "unsignedRightShift", "or", "and", "xor", "twiddle", "not", "equal", "plusAssignment", "minusAssignment", "multiplyAssignment", "divideAssignment", "remainderAssignment", "leftShiftAssignment", "rightShiftAssignment", "unsignedRightShiftAssignment", "orAssignment", "andAssignment", "xorAssignment", "prefixPlusPlus", "postfixPlusPlus", "prefixMinusMinus", "postfixMinusMinus"};

	static final String[] opUpperName = new String[] {"Plus", "Minus", "Multiply", "Divide", "Remainder", "Greater", "GreaterEqual", "Less", "LessEqual", "EqualEqual", "NotEqual", "LeftShift", "RightShift", "UnsignedRightShift", "Or", "And", "Xor", "Twiddle", "Not", "Equal", "PlusAssignment", "MinusAssignment", "MultiplyAssignment", "DivideAssignment", "RemainderAssignment", "LeftShiftAssignment", "RightShiftAssignment", "UnsignedRightShiftAssignment", "OrAssignment", "AndAssignment", "XorAssignment", "PrefixPlusPlus", "PostfixPlusPlus", "PrefixMinusMinus", "PostfixMinusMinus"};

	static final String[] immediate = new String[] {"x", "y"};

	static final String[] fVariable = new String[] {"xVar", "yVar", "xArray", "yArray"};

	static final String[] field = new String[] {"xField", "yField", "xStaticField", "yStaticField"};

	static final String[] prefixList = new String[] {"foo.", "EvalTypeTests.", IInternalDebugCoreConstants.EMPTY_STRING};

	/**
	 * Set the working directory of the launch configuration to generate the files
	 * in the correct package location.
	 */
	static public void main(String[] args) throws Exception {

		genTestsNumericTypeAllOpsAllTypes(T_byte);
		genTestsNumericTypeAllOpsAllTypes(T_char);
		genTestsNumericTypeAllOpsAllTypes(T_short);
		genTestsNumericTypeAllOpsAllTypes(T_int);
		genTestsNumericTypeAllOpsAllTypes(T_long);
		genTestsNumericTypeAllOpsAllTypes(T_float);
		genTestsNumericTypeAllOpsAllTypes(T_double);
		genTestsBooleanAllOpsBoolean();
		genTestsStringPlusOpAllTypes();

		genTestsLocalVarValue();

		genTestsLocalVarAssignment();

		genTestsNumericTypeAllAssignmentOpsAllTypes(T_byte);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_char);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_short);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_int);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_long);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_float);
		genTestsNumericTypeAllAssignmentOpsAllTypes(T_double);
		genTestsBooleanAllAssignmentOpsBoolean();
		genTestsStringPlusAssignmentOpAllTypes();

		genTestsNumericTypeCast();

		genTestsAllIntegerTypesAllXfixOps();

		genTestsQualifiedFieldValue();
		genTestsQualifiedStaticFieldValue();
		genTestsFieldValue();
		genTestsStaticFieldValue();

		genTestsArrayValue();
		genTestsArrayAssignment();
		genTestsArrayAllocation();
		genTestsArrayInitialization();

		System.out.println("done");

	}

	//-------------------------------

	public static void genTestsNumericTypeAllOpsAllTypes(int type) throws Exception {

		StringBuilder code = new StringBuilder();

		String className = typeUpperName[type] + "OperatorsTests";

		genTestsNumericTypeArithmeticOpNumericTypes(type, Op_plus, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, Op_plus, T_String, code);
		genTestsNumericTypeArithmeticOpNumericTypes(type, Op_minus, code);
		genTestsNumericTypeArithmeticOpNumericTypes(type, Op_multiply, code);
		genTestsNumericTypeArithmeticOpNumericTypes(type, Op_divide, code);
		genTestsNumericTypeArithmeticOpNumericTypes(type, Op_remainder, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_greater, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_greaterEqual, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_less, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_lessEqual, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_equalEqual, code);
		genTestsNumericTypeComparisonOpNumericTypes(type, Op_notEqual, code);
		if (type != T_float && type != T_double) {
			genTestsIntegerTypeShiftOpIntegerTypes(type, Op_leftShift, code);
			genTestsIntegerTypeShiftOpIntegerTypes(type, Op_rightShift, code);
			genTestsIntegerTypeShiftOpIntegerTypes(type, Op_unsignedRightShift, code);
			genTestsIntegerTypeBooleanOpIntegerTypes(type, Op_or, code);
			genTestsIntegerTypeBooleanOpIntegerTypes(type, Op_and, code);
			genTestsIntegerTypeBooleanOpIntegerTypes(type, Op_xor, code);
		}

		genTestUnaryOpNumericType(type, Op_plus, code);
		genTestUnaryOpNumericType(type, Op_minus, code);
		if (type != T_float && type != T_double) {
			genTestUnaryOpNumericType(type, Op_twiddle, code);
		}

		createJavaFile(className, "EvalSimpleTests", 15, 1, true, code);

	}

	public static void genTestsBooleanAllOpsBoolean() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "BooleanOperatorsTests";

		genTestBooleanBinaryOpBoolean(Op_or, code);
		genTestBooleanBinaryOpBoolean(Op_and, code);
		genTestBooleanBinaryOpBoolean(Op_xor, code);
		genTestUnaryOpBoolean(Op_not, code);

		createJavaFile(className, "EvalSimpleTests", 15, 1, false, code);
	}

	public static void genTestsStringPlusOpAllTypes() throws Exception {

		StringBuilder code = new StringBuilder();

		String className = "StringPlusOpTests";

		code.append("\t// " + fTypeName[T_String] + " " + opSymbol[Op_plus] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_String] + ", " + fTypeName[T_null] + "}\n" +
					"\n");

		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_byte, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_char, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_short, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_int, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_long, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_double, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_boolean, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_String, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_null, code);

		createJavaFile(className, "EvalSimpleTests", 15, 1, true, false, code);
	}

	public static void genTestsLocalVarValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "LocalVarValueTests";

		code.append("\t// \n" +
					"\n");

		genTestLocalVarValue(T_byte, code);
		genTestLocalVarValue(T_char, code);
		genTestLocalVarValue(T_short, code);
		genTestLocalVarValue(T_int, code);
		genTestLocalVarValue(T_long, code);
		genTestLocalVarValue(T_float, code);
		genTestLocalVarValue(T_double, code);
		genTestLocalVarValue(T_String, code);
		genTestLocalVarValue(T_boolean, code);
//		genTestLocalVarValue(T_null, code);

		createJavaFile(className, "EvalSimpleTests", 37, 1, true, code);
	}

	public static void genTestsLocalVarAssignment() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "LocalVarAssignmentTests";

		code.append("\t// \n" +
					"\n");

		genTestLocalVarAssignment(T_byte, code);
		genTestLocalVarAssignment(T_char, code);
		genTestLocalVarAssignment(T_short, code);
		genTestLocalVarAssignment(T_int, code);
		genTestLocalVarAssignment(T_long, code);
		genTestLocalVarAssignment(T_float, code);
		genTestLocalVarAssignment(T_double, code);
		genTestLocalVarAssignment(T_String, code);
		genTestLocalVarAssignment(T_boolean, code);

		createJavaFile(className, "EvalSimpleTests", 37, 1, true, code);
	}

	public static void genTestsNumericTypeAllAssignmentOpsAllTypes(int type) throws Exception {

		StringBuilder code = new StringBuilder();

		String className = typeUpperName[type] + "AssignmentOperatorsTests";

		genTestsNumericTypeArithmeticAssignmentOpNumericTypes(type, Op_plusAss, code);
		genTestsNumericTypeArithmeticAssignmentOpNumericTypes(type, Op_minusAss, code);
		genTestsNumericTypeArithmeticAssignmentOpNumericTypes(type, Op_multiplyAss, code);
		genTestsNumericTypeArithmeticAssignmentOpNumericTypes(type, Op_divideAss, code);
		genTestsNumericTypeArithmeticAssignmentOpNumericTypes(type, Op_remainderAss, code);
		if (type != T_float && type != T_double) {
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_leftShiftAss, code);
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_rightShiftAss, code);
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_unsignedRightShiftAss, code);
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_orAss, code);
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_andAss, code);
			genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(type, Op_xorAss, code);
		}


		createJavaFile(className, "EvalSimpleTests", 37, 1, false, code);

	}

	public static void genTestsBooleanAllAssignmentOpsBoolean() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "BooleanAssignmentOperatorsTests";

		code.append("\t// " + fTypeName[T_boolean] + " " + opSymbol[Op_orAss] + " " + fTypeName[T_boolean] + "\n" +
					"\n");
		genTestTypeAssignmentOpType(T_boolean, Op_orAss, T_boolean, code);

		code.append("\t// " + fTypeName[T_boolean] + " " + opSymbol[Op_andAss] + " " + fTypeName[T_boolean] + "\n" +
					"\n");
		genTestTypeAssignmentOpType(T_boolean, Op_andAss, T_boolean, code);

		code.append("\t// " + fTypeName[T_boolean] + " " + opSymbol[Op_xorAss] + " " + fTypeName[T_boolean] + "\n" +
					"\n");
		genTestTypeAssignmentOpType(T_boolean, Op_xorAss, T_boolean, code);

		createJavaFile(className, "EvalSimpleTests", 37, 1, false, code);
	}

	public static void genTestsStringPlusAssignmentOpAllTypes() throws Exception {

		StringBuilder code = new StringBuilder();

		String className = "StringPlusAssignmentOpTests";

		code.append("\t// " + fTypeName[T_String] + " " + opSymbol[Op_plusAss] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_String] + ", " + fTypeName[T_null] + "}\n" +
					"\n");

		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_byte, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_char, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_short, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_int, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_long, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_float, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_double, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_String, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_null, code);

		createJavaFile(className, "EvalSimpleTests", 37, 1, true, false, code);
	}

	public static void genTestsNumericTypeCast() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "NumericTypesCastTests";

		genTestsNumericTypeCast(T_byte, code);
		genTestsNumericTypeCast(T_char, code);
		genTestsNumericTypeCast(T_short, code);
		genTestsNumericTypeCast(T_int, code);
		genTestsNumericTypeCast(T_long, code);
		genTestsNumericTypeCast(T_float, code);
		genTestsNumericTypeCast(T_double, code);

		createJavaFile(className, "EvalSimpleTests", 15, 1, false, code);
	}

	public static void genTestsAllIntegerTypesAllXfixOps() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "XfixOperatorsTests";

		genTestsNumericTypeAllXfixOps(T_byte, code);
		genTestsNumericTypeAllXfixOps(T_char, code);
		genTestsNumericTypeAllXfixOps(T_short, code);
		genTestsNumericTypeAllXfixOps(T_int, code);
		genTestsNumericTypeAllXfixOps(T_long, code);
		genTestsNumericTypeAllXfixOps(T_float, code);
		genTestsNumericTypeAllXfixOps(T_double, code);

		createJavaFile(className, "EvalSimpleTests", 37, 1, false, code);
	}

	public static void genTestsQualifiedFieldValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "QualifiedFieldValueTests";

		genTestFieldValue(T_byte, 0, code);
		genTestFieldValue(T_char, 0, code);
		genTestFieldValue(T_short, 0, code);
		genTestFieldValue(T_int, 0, code);
		genTestFieldValue(T_long, 0, code);
		genTestFieldValue(T_float, 0, code);
		genTestFieldValue(T_double, 0, code);
		genTestFieldValue(T_String, 0, code);
		genTestFieldValue(T_boolean, 0, code);

		createJavaFile(className, "EvalTypeTests", 73, 1, true, code);
	}

	public static void genTestsQualifiedStaticFieldValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "QualifiedStaticFieldValueTests";

		genTestStaticFieldValue(T_byte, 0, code);
		genTestStaticFieldValue(T_char, 0, code);
		genTestStaticFieldValue(T_short, 0, code);
		genTestStaticFieldValue(T_int, 0, code);
		genTestStaticFieldValue(T_long, 0, code);
		genTestStaticFieldValue(T_float, 0, code);
		genTestStaticFieldValue(T_double, 0, code);
		genTestStaticFieldValue(T_String, 0, code);
		genTestStaticFieldValue(T_boolean, 0, code);

		createJavaFile(className, "EvalTypeTests", 73, 1, true, code);

		code = new StringBuilder();

		className = "QualifiedStaticFieldValueTests2";

		genTestStaticFieldValue(T_byte, 1, code);
		genTestStaticFieldValue(T_char, 1, code);
		genTestStaticFieldValue(T_short, 1, code);
		genTestStaticFieldValue(T_int, 1, code);
		genTestStaticFieldValue(T_long, 1, code);
		genTestStaticFieldValue(T_float, 1, code);
		genTestStaticFieldValue(T_double, 1, code);
		genTestStaticFieldValue(T_String, 1, code);
		genTestStaticFieldValue(T_boolean, 1, code);

		createJavaFile(className, "EvalTypeTests", 73, 1, true, code);
	}

	public static void genTestsFieldValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "FieldValueTests";

		genTestFieldValue(T_byte, 2, code);
		genTestFieldValue(T_char, 2, code);
		genTestFieldValue(T_short, 2, code);
		genTestFieldValue(T_int, 2, code);
		genTestFieldValue(T_long, 2, code);
		genTestFieldValue(T_float, 2, code);
		genTestFieldValue(T_double, 2, code);
		genTestFieldValue(T_String, 2, code);
		genTestFieldValue(T_boolean, 2, code);

		createJavaFile(className, "EvalTypeTests", 63, 2, true, code);
	}

	public static void genTestsStaticFieldValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "StaticFieldValueTests";

		genTestStaticFieldValue(T_byte, 2, code);
		genTestStaticFieldValue(T_char, 2, code);
		genTestStaticFieldValue(T_short, 2, code);
		genTestStaticFieldValue(T_int, 2, code);
		genTestStaticFieldValue(T_long, 2, code);
		genTestStaticFieldValue(T_float, 2, code);
		genTestStaticFieldValue(T_double, 2, code);
		genTestStaticFieldValue(T_String, 2, code);
		genTestStaticFieldValue(T_boolean, 2, code);

		createJavaFile(className, "EvalTypeTests", 63, 2, true, code);

		className = "StaticFieldValueTests2";

		createJavaFile(className, "EvalTypeTests", 67, 2, true, code);
	}

	public static void genTestsArrayValue() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "ArrayValueTests";

		genTestArrayLength(T_byte, code);
		genTestArrayLength(T_char, code);
		genTestArrayLength(T_short, code);
		genTestArrayLength(T_int, code);
		genTestArrayLength(T_long, code);
		genTestArrayLength(T_float, code);
		genTestArrayLength(T_double, code);
		genTestArrayLength(T_String, code);
		genTestArrayLength(T_boolean, code);

		genTestArrayValue(T_byte, code);
		genTestArrayValue(T_char, code);
		genTestArrayValue(T_short, code);
		genTestArrayValue(T_int, code);
		genTestArrayValue(T_long, code);
		genTestArrayValue(T_float, code);
		genTestArrayValue(T_double, code);
		genTestArrayValue(T_String, code);
		genTestArrayValue(T_boolean, code);

		createJavaFile(className, "EvalArrayTests", 37, 1, true, code);
	}

	public static void genTestsArrayAssignment() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "ArrayAssignmentTests";

		genTestArrayAssignment(T_byte, code);
		genTestArrayAssignment(T_char, code);
		genTestArrayAssignment(T_short, code);
		genTestArrayAssignment(T_int, code);
		genTestArrayAssignment(T_long, code);
		genTestArrayAssignment(T_float, code);
		genTestArrayAssignment(T_double, code);
		genTestArrayAssignment(T_String, code);
		genTestArrayAssignment(T_boolean, code);

		createJavaFile(className, "EvalArrayTests", 37, 1, true, code);
	}

	public static void genTestsArrayAllocation() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "ArrayAllocationTests";

		genTestArrayAllocation(T_byte, code);
		genTestArrayAllocation(T_char, code);
		genTestArrayAllocation(T_short, code);
		genTestArrayAllocation(T_int, code);
		genTestArrayAllocation(T_long, code);
		genTestArrayAllocation(T_float, code);
		genTestArrayAllocation(T_double, code);
		genTestArrayAllocation(T_String, code);
		genTestArrayAllocation(T_boolean, code);

		createJavaFile(className, "EvalArrayTests", 37, 1, false, code);
	}

	public static void genTestsArrayInitialization() throws Exception {
		StringBuilder code = new StringBuilder();

		String className = "ArrayInitializationTests";

		genTestArrayInitialization(T_byte, code);
		genTestArrayInitialization(T_char, code);
		genTestArrayInitialization(T_short, code);
		genTestArrayInitialization(T_int, code);
		genTestArrayInitialization(T_long, code);
		genTestArrayInitialization(T_float, code);
		genTestArrayInitialization(T_double, code);
		genTestArrayInitialization(T_String, code);
		genTestArrayInitialization(T_boolean, code);

		createJavaFile(className, "EvalArrayTests", 37, 1, true, code);
	}
	//----------------------------

	public static void genTestsNumericTypeArithmeticOpNumericTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_float] + ", " + fTypeName[T_double] + "}\n" +
					"\n");

		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_byte, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_char, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_short, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_int, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_long, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_float, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_double, code);

	}

	public static void genTestsNumericTypeComparisonOpNumericTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_float] + ", " + fTypeName[T_double] + "}\n" +
					"\n");

		genTestTypeBinaryOpTypeBooleanResult(type, op, T_byte, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_char, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_short, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_int, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_long, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_float, code);
		genTestTypeBinaryOpTypeBooleanResult(type, op, T_double, code);

	}

	public static void genTestsIntegerTypeShiftOpIntegerTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + "}\n" +
					"\n");

		genTestTypeBinaryOpTypeUnaryPromotion(type, op, T_byte, code);
		genTestTypeBinaryOpTypeUnaryPromotion(type, op, T_char, code);
		genTestTypeBinaryOpTypeUnaryPromotion(type, op, T_short, code);
		genTestTypeBinaryOpTypeUnaryPromotion(type, op, T_int, code);
		genTestTypeBinaryOpTypeUnaryPromotion(type, op, T_long, code);

	}

	public static void genTestsIntegerTypeBooleanOpIntegerTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + "}\n" +
					"\n");

		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_byte, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_char, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_short, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_int, code);
		genTestTypeBinaryOpTypeBinaryPromotion(type, op, T_long, code);

	}

	public static void genTestsNumericTypeArithmeticAssignmentOpNumericTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_float] + ", " + fTypeName[T_double] + "}\n" +
					"\n");

		genTestTypeAssignmentOpType(type, op, T_byte, code);
		genTestTypeAssignmentOpType(type, op, T_char, code);
		genTestTypeAssignmentOpType(type, op, T_short, code);
		genTestTypeAssignmentOpType(type, op, T_int, code);
		genTestTypeAssignmentOpType(type, op, T_long, code);
		genTestTypeAssignmentOpType(type, op, T_float, code);
		genTestTypeAssignmentOpType(type, op, T_double, code);

	}

	public static void genTestsIntegerTypeArithmeticAssignmentOpIntegerTypes(int type, int op, StringBuilder code) {

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_float] + ", " + fTypeName[T_double] + "}\n" +
					"\n");

		genTestTypeAssignmentOpType(type, op, T_byte, code);
		genTestTypeAssignmentOpType(type, op, T_char, code);
		genTestTypeAssignmentOpType(type, op, T_short, code);
		genTestTypeAssignmentOpType(type, op, T_int, code);
		genTestTypeAssignmentOpType(type, op, T_long, code);

	}

	public static void genTestsNumericTypeCast(int type, StringBuilder code) {

		code.append("\t// (" + fTypeName[type] + ") {" + fTypeName[T_byte] + ", " + fTypeName[T_char] + ", " + fTypeName[T_short] + ", " + fTypeName[T_int] + ", " + fTypeName[T_long] + ", " + fTypeName[T_float] + ", " + fTypeName[T_double] + "}\n" +
					"\n");

		genTestNumericTypeCast(type, T_byte, code);
		genTestNumericTypeCast(type, T_char, code);
		genTestNumericTypeCast(type, T_short, code);
		genTestNumericTypeCast(type, T_int, code);
		genTestNumericTypeCast(type, T_long, code);
		genTestNumericTypeCast(type, T_float, code);
		genTestNumericTypeCast(type, T_double, code);

	}

	public static void genTestsNumericTypeAllXfixOps(int type, StringBuilder code) {

		code.append("\t// {" + opSymbol[Op_prefixPlusPlus] + ", " + opSymbol[Op_prefixMinusMinus] + "} " + fTypeName[type] + "\n" +
					"\t// " + fTypeName[type] + " {" + opSymbol[Op_postfixPlusPlus] + ", " + opSymbol[Op_postfixMinusMinus] + "}\n" +
					"\n");

		genTestNumericTypePrefixOp(type, Op_prefixPlusPlus, code);
		genTestNumericTypePrefixOp(type, Op_prefixMinusMinus, code);
		genTestNumericTypePostfixOp(type, Op_postfixPlusPlus, code);
		genTestNumericTypePostfixOp(type, Op_postfixMinusMinus, code);
	}

	//----------------------------

	public static void genTestTypeBinaryOpTypeBinaryPromotion(int type1, int op, int type2, StringBuilder code) {

		int promotedType = getPromotionType(type1, type2);

		String t1UName = typeUpperName[type1];
		String t2UName = typeUpperName[type2];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + t1UName + oUName + t2UName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeBinaryOp(type1, op, type2, promotedType, 0, 1, true, code);
		code.append("\n");
		genCodeBinaryOp(type1, op, type2, promotedType, 1, 0, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestTypeBinaryOpTypeBooleanResult(int type1, int op, int type2, StringBuilder code) {

		int promotedType = T_boolean;

		String t1UName = typeUpperName[type1];
		String t2UName = typeUpperName[type2];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + t1UName + oUName + t2UName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeBinaryOp(type1, op, type2, promotedType, 0, 1, true, code);
		code.append("\n");
		genCodeBinaryOp(type1, op, type2, promotedType, 1, 0, false, code);
		code.append("\n");
		genCodeBinaryOp(type1, op, type2, promotedType, 0, 0, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");

	}

	public static void genTestTypeBinaryOpTypeUnaryPromotion(int type1, int op, int type2, StringBuilder code) {

		int promotedType = getUnaryPromotionType(type1);

		String t1UName = typeUpperName[type1];
		String t2UName = typeUpperName[type2];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + t1UName + oUName + t2UName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeBinaryOp(type1, op, type2, promotedType, 0, 1, true, code);
		code.append("\n");
		genCodeBinaryOp(type1, op, type2, promotedType, 1, 0, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestBooleanBinaryOpBoolean(int op, StringBuilder code) {

		int type = T_boolean;

		String uName = typeUpperName[type];
		String oUName = opUpperName[op];

		code.append("\t// " + fTypeName[type] + " " + opSymbol[op] + " " + fTypeName[type] + "\n" +
					"\n");

		code.append("\tpublic void test" + uName + oUName + uName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeBinaryOp(type, op, type, type, 0, 0, true, code);
		code.append("\n");
		genCodeBinaryOp(type, op, type, type, 0, 1, false, code);
		code.append("\n");
		genCodeBinaryOp(type, op, type, type, 1, 0, false, code);
		code.append("\n");
		genCodeBinaryOp(type, op, type, type, 1, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");

	}

	public static void genTestUnaryOpNumericType(int type, int op, StringBuilder code) {

		int promotedType = getUnaryPromotionType(type);

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String oSymbol = opSymbol[op];
		String oUName = opUpperName[op];

		code.append("\t// " + oSymbol + " " + tName + "\n" +
					"\n");

		code.append("\tpublic void test" + oUName + tUName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeUnaryOp(type, op, promotedType, 0, true, code);
		code.append("\n");
		genCodeUnaryOp(type, op, promotedType, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestUnaryOpBoolean(int op, StringBuilder code) {

		int type = T_boolean;

		String uName = typeUpperName[type];
		String oUName = opUpperName[op];

		code.append("\t// " + opSymbol[op] + " " + fTypeName[type] + "\n" +
					"\n");

		code.append("\tpublic void test" + oUName + uName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeUnaryOp(type, op, type, 0, true, code);
		code.append("\n");
		genCodeUnaryOp(type, op, type, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");

	}

	public static void genTestLocalVarValue(int type, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeLocalVarValue(type, 0, true, code);
		code.append("\n");
		genCodeLocalVarValue(type, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestLocalVarAssignment(int type, StringBuilder code) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeLocalVarAssignment(type, 0, 0, true, code);
		code.append("\n");
		genCodeLocalVarAssignment(type, 0, 1, false, code);
		code.append("\n");
		genCodeLocalVarAssignment(type, 1, 0, false, code);
		code.append("\n");
		genCodeLocalVarAssignment(type, 1, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestTypeAssignmentOpType(int type1, int op, int type2, StringBuilder code) {

		String t1UName = typeUpperName[type1];
		String t2UName = typeUpperName[type2];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + t1UName + oUName + t2UName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeSetTmp("tmp" + fVariable[0], type1, Op_equal, fVariable[0] + t1UName + "Value", true, code);
		genCodeSetTmp("tmp" + fVariable[0], type1, op, immediate[0] + t2UName + "Value", false, code);
		genCodeAssignmentOp(type1, op, type2, 0, 0, true, code);
		code.append("\n");
		genCodeSetTmp("tmp" + fVariable[0], type1, op, immediate[1] + t2UName + "Value", false, code);
		genCodeAssignmentOp(type1, op, type2, 0, 1, false, code);
		code.append("\n");
		genCodeSetTmp("tmp" + fVariable[1], type1, Op_equal, fVariable[1] + t1UName + "Value", true, code);
		genCodeSetTmp("tmp" + fVariable[1], type1, op, immediate[0] + t2UName + "Value", false, code);
		genCodeAssignmentOp(type1, op, type2, 1, 0, false, code);
		code.append("\n");
		genCodeSetTmp("tmp" + fVariable[1], type1, op, immediate[1] + t2UName + "Value", false, code);
		genCodeAssignmentOp(type1, op, type2, 1, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestNumericTypeCast(int type1, int type2, StringBuilder code) {

		String t1UName = typeUpperName[type1];
		String t2UName = typeUpperName[type2];

		code.append("\tpublic void test" + t1UName + "Cast" + t2UName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeCast(type1, type2, 0, true, code);
		code.append("\n");
		genCodeCast(type1, type2, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");

	}

	public static void genTestNumericTypePrefixOp(int type, int op, StringBuilder code) {

		String tUName = typeUpperName[type];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + oUName + tUName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodePrefixOp(type, op, 0, true, code);
		code.append("\n");
		genCodePrefixOp(type, op, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestNumericTypePostfixOp(int type, int op, StringBuilder code) {

		String tUName = typeUpperName[type];
		String oUName = opUpperName[op];

		code.append("\tpublic void test" + oUName + tUName + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodePostfixOp(type, op, 0, true, code);
		code.append("\n");
		genCodePostfixOp(type, op, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestFieldValue(int type, int prefix, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "FieldValue() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeFieldValue(type, 0, prefix, true, code);
		code.append("\n");
		genCodeFieldValue(type, 1, prefix, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestStaticFieldValue(int type, int prefix, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "StaticFieldValue() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeFieldValue(type, 2, prefix, true, code);
		code.append("\n");
		genCodeFieldValue(type, 3, prefix, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestArrayValue(int type, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "ArrayValue() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeArrayValue(type, 2, 0, true, code);
		code.append("\n");
		genCodeArrayValue(type, 2, 1, false, code);
		code.append("\n");
		genCodeArrayValue(type, 2, 2, false, code);
		code.append("\n");
		genCodeArrayValue(type, 3, 0, false, code);
		code.append("\n");
		genCodeArrayValue(type, 3, 1, false, code);
		code.append("\n");
		genCodeArrayValue(type, 3, 2, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestArrayLength(int type, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "ArrayLength() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeArrayLength(type, 2, true, code);
		code.append("\n");
		genCodeArrayLength(type, 3, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestArrayAssignment(int type, StringBuilder code ) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "ArrayAssignment() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeArrayAssignment(type, 2, 0, 0, true, code);
		code.append("\n");
		genCodeArrayAssignment(type, 2, 1, 1, false, code);
		code.append("\n");
		genCodeArrayAssignment(type, 2, 2, 0, false, code);
		code.append("\n");
		genCodeArrayAssignment(type, 3, 0, 1, false, code);
		code.append("\n");
		genCodeArrayAssignment(type, 3, 1, 0, false, code);
		code.append("\n");
		genCodeArrayAssignment(type, 3, 2, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestArrayAllocation(int type, StringBuilder code) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "ArrayAllocation() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeArrayAllocation(type, 2, 0, true, code);
		code.append("\n");
		genCodeArrayAllocation(type, 3, 1, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genTestArrayInitialization(int type, StringBuilder code) {
		String tUName = typeUpperName[type];

		code.append("\tpublic void test" + tUName + "ArrayAllocation() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeArrayInitialization(type, 2, true, code);
		code.append("\n");
		genCodeArrayInitialization(type, 3, false, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	//-------------------------------

	public static void genCodeBinaryOp(int type1, int op, int type2, int resultType, int var1, int var2, boolean first, StringBuilder code) {

		String t1Name = fTypeName[type1];
		String t1UName = typeUpperName[type1];
		String t2Name = fTypeName[type2];
		String t2UName = typeUpperName[type2];
		String resName = fTypeName[resultType];
		String resUName = typeUpperName[resultType];
		String oName = opName[op];
		String oSymbol = opSymbol[op];
		String v1Name = immediate[var1];
		String v2Name = immediate[var2];

		genCodeEval(v1Name + t1UName + " + " + oName + "Op + " + v2Name + t2UName, first, code);
		genCodeReturnTypeCheck(t1Name + " " + oName + " " + t2Name, resName, first, code);
		switch (resultType) {
			case T_String:
				genCodeReturnValueCheckStringType(t1Name + " " + oName + " " + t2Name, v1Name + t1UName + "Value " + oSymbol + " " + v2Name + t2UName + "Value", first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(t1Name + " " + oName + " " + t2Name, resName, resUName, v1Name + t1UName + "Value " + oSymbol + " " + v2Name + t2UName + "Value", first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(t1Name + " " + oName + " " + t2Name, resName, resUName, v1Name + t1UName + "Value " + oSymbol + " " + v2Name + t2UName + "Value", first, code);
				break;
		}
	}

	public static void genCodeUnaryOp(int type, int op, int resultType, int var, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String resName = fTypeName[resultType];
		String resUName = typeUpperName[resultType];
		String oName = opName[op];
		String oSymbol = opSymbol[op];
		String vName = immediate[var];

		genCodeEval(oName + "Op + " + vName + tUName, first, code);
		genCodeReturnTypeCheck(oName + " " + tName, resName, first, code);
		if (type == T_float || type == T_double) {
			genCodeReturnValueCheckFloatDoubleType(oName + " " + tName, resName, resUName, oSymbol + " " + vName + tUName + "Value", first, code);
		} else {
			genCodeReturnValueCheckPrimitiveType(oName + " " + tName, resName, resUName, oSymbol + " " + vName + tUName + "Value", first, code);
		}
	}

	public static void genCodeLocalVarValue(int type, int var, boolean first, StringBuilder code) {
		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeLocalVarValue(type,var, vName + tUName + "Value", first, code);
	}

	public static void genCodeLocalVarValue(int type, int var, String referenceExpression, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeEval(vName + tUName, first, code);
		genCodeReturnTypeCheck(tName + " local variable value", tName, first,code);
		switch (type) {
			case T_String:
				genCodeReturnValueCheckStringType(tName + " local variable value", referenceExpression, first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(tName + " local variable value", tName, tUName, referenceExpression, first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(tName + " local variable value", tName, tUName, referenceExpression, first, code);
				break;
		}
	}

	public static void genCodeLocalVarAssignment(int type, int var, int imm, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];
		String iName = immediate[imm];
		String oName = opName[Op_equal];

		genCodeEval(vName + tUName + " + " + oName + "Op +" + iName + tUName, first, code);
		genCodeReturnTypeCheck(tName + " local variable assignment", tName, first,code);
		switch (type) {
			case T_String:
				genCodeReturnValueCheckStringType(tName + " local variable assignment", iName + tUName + "Value", first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(tName + " local variable assignment", tName, tUName, iName + tUName + "Value", first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(tName + " local variable assignment", tName, tUName, iName + tUName + "Value", first, code);
				break;
		}
		genCodeLocalVarValue(type, var, iName + tUName + "Value", false, code);
	}

	public static void genCodeAssignmentOp(int type1, int op, int type2, int var, int imm, boolean first, StringBuilder code) {
		String t1Name = fTypeName[type1];
		String t1UName = typeUpperName[type1];
		String t2Name = fTypeName[type2];
		String t2UName = typeUpperName[type2];
		String oName = opName[op];
		String vName = fVariable[var];
		String iName = immediate[imm];

		genCodeEval(vName + t1UName + " + " + oName + "Op + " + iName + t2UName, first, code);
		genCodeReturnTypeCheck(t1Name + " " + oName + " " + t2Name, t1Name, first, code);
		switch (type1) {
			case T_String:
				genCodeReturnValueCheckStringType(t1Name + " " + oName + " " + t2Name, "tmp" + vName, first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(t1Name + " " + oName + " " + t2Name, t1Name, t1UName, "tmp" + vName, first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(t1Name + " " + oName + " " + t2Name, t1Name, t1UName, "tmp" + vName, first, code);
				break;
		}
		genCodeLocalVarValue(type1, var, "tmp" + vName, false, code);
	}

	public static void genCodeCast(int type1, int type2, int imm, boolean first, StringBuilder code) {
		String t1Name = fTypeName[type1];
		String t1UName = typeUpperName[type1];
		String t2Name = fTypeName[type2];
		String t2UName = typeUpperName[type2];
		String iName = immediate[imm];

		genCodeEval("\"(" + t1Name + ")\" + " + iName + t2UName, first, code);
		genCodeReturnTypeCheck("(" + t1Name + ") " + t2Name, t1Name, first, code);
		if (type1 == T_float || type1 == T_double) {
			genCodeReturnValueCheckFloatDoubleType("(" + t1Name + ") " + t2Name, t1Name, t1UName, (needCast(type1, type2) ? "( " + t1Name + ") " : IInternalDebugCoreConstants.EMPTY_STRING) + iName + t2UName + "Value", first, code);
		} else {
			genCodeReturnValueCheckPrimitiveType("(" + t1Name + ") " + t2Name, t1Name, t1UName, (needCast(type1, type2) ? "( " + t1Name + ") " : IInternalDebugCoreConstants.EMPTY_STRING) + iName + t2UName + "Value", first, code);
		}
	}

	public static void genCodePrefixOp(int type, int op, int var, boolean first, StringBuilder code) {
		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];
		String oName = opName[op];
		String oSymbol = opSymbol[op];

		genCodeSetTmp("tmp" + vName, type, Op_equal, vName + tUName + "Value", true, code);
		genCodeEval(oName + "Op + " + vName + tUName, first, code);
		genCodeReturnTypeCheck(oName + " " + tName, tName, first, code);
		if (type == T_float || type == T_double) {
			genCodeReturnValueCheckFloatDoubleType(oName + " " + tName, tName, tUName, oSymbol + "tmp" + vName, first,code);
		} else {
			genCodeReturnValueCheckPrimitiveType(oName + " " + tName, tName, tUName, oSymbol + "tmp" + vName, first,code);
		}
		genCodeLocalVarValue(type, var, "tmp" + vName, false, code);
	}

	public static void genCodePostfixOp(int type, int op, int var, boolean first, StringBuilder code) {
		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];
		String oName = opName[op];
		String oSymbol = opSymbol[op];

		genCodeSetTmp("tmp" + vName, type, Op_equal, vName + tUName + "Value", true, code);
		genCodeEval(vName + tUName + " + " + oName + "Op", first, code);
		genCodeReturnTypeCheck(tName + " " + oName, tName, first, code);
		if (type == T_float || type == T_double) {
			genCodeReturnValueCheckFloatDoubleType(tName + " " + oName, tName, tUName, "tmp" + vName + oSymbol, first,code);
		} else {
			genCodeReturnValueCheckPrimitiveType(tName + " " + oName, tName, tUName, "tmp" + vName + oSymbol, first,code);
		}
		genCodeLocalVarValue(type, var, "tmp" + vName, false, code);
	}

	public static void genCodeFieldValue(int type, int var, int prefix, boolean first, StringBuilder code) {
		String tUName = typeUpperName[type];
		String fName = field[var];

		genCodeFieldValue(type, var, prefix, fName + tUName + "Value", first, code);
	}

	public static void genCodeFieldValue(int type, int var, int prefix, String referenceExpression, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String fName = field[var];
		String prefixVal = prefixList[prefix];

		genCodeEval("\"" + prefixVal + "\" + " + fName + tUName, first, code);
		genCodeReturnTypeCheck(tName + " field value", tName, first,code);
		switch (type) {
			case T_String:
				genCodeReturnValueCheckStringType(tName + " field value", referenceExpression, first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(tName + " field value", tName, tUName, referenceExpression, first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(tName + " field value", tName, tUName, referenceExpression, first, code);
				break;
		}
	}

	public static void genCodeArrayLength(int type, int var, boolean first, StringBuilder code) {

		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeArrayLength(type, var, vName + tUName + "Value.length", first, code);

	}

	public static void genCodeArrayLength(int type, int var, String referenceExpression, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeEval(vName + tUName + " + \".length\"", first, code);
		genCodeReturnTypeCheck(tName + " array length", fTypeName[T_int], first,code);
		genCodeReturnValueCheckPrimitiveType(tName + " array length", fTypeName[T_int], typeUpperName[T_int], referenceExpression, first, code);
	}

	public static void genCodeArrayValue(int type, int var, int index, boolean first, StringBuilder code) {

		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeArrayValue(type, var, index, vName + tUName + "Value[" + index + "]", first, code);
	}

	public static void genCodeArrayValue(int type, int var, int index, String referenceExpression, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];

		genCodeEval(vName + tUName + " + \"[" + index + "]\"", first, code);
		genCodeReturnTypeCheck(tName + " array value", tName, first,code);
		switch (type) {
			case T_String:
				genCodeReturnValueCheckStringType(tName + " array value", referenceExpression, first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(tName + " array value", tName, tUName, referenceExpression, first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(tName + " array value", tName, tUName, referenceExpression, first, code);
				break;
		}
	}

	public static void genCodeArrayAssignment(int type, int var, int index, int imm, boolean first, StringBuilder code) {

		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];
		String iName = immediate[imm];
		String oName = opName[Op_equal];

		genCodeEval(vName + tUName + " + \"[" + index + "]\" + " + oName + "Op +" + iName + tUName, first, code);
		genCodeReturnTypeCheck(tName + " array assignment", tName, first,code);
		switch (type) {
			case T_String:
				genCodeReturnValueCheckStringType(tName + " array assignment", iName + tUName + "Value", first, code);
				break;
			case T_float:
			case T_double:
				genCodeReturnValueCheckFloatDoubleType(tName + " array assignment", tName, tUName, iName + tUName + "Value", first, code);
				break;
			default:
				genCodeReturnValueCheckPrimitiveType(tName + " array assignment", tName, tUName, iName + tUName + "Value", first, code);
				break;
		}
		genCodeArrayValue(type, var, index, iName + tUName + "Value", false, code);
	}

	public static void genCodeArrayAllocation(int type, int var, int dim, boolean first, StringBuilder code) {
		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String charUName = typeUpperName[T_char];
		String vName = fVariable[var];
		String iName = immediate[dim];
		String oName = opName[Op_equal];

		genCodeEval(vName + tUName + " + " + oName + "Op + \"new " + tName + "[\" + " + iName + charUName + " + \"]\"", first, code);
		genCodeReturnTypeCheck(tName + " array assignment", tName + "[]", first, code);
		if (first) {
			genCodeSetTmp("intValue", T_int, Op_equal, "0", true, code);
		}
		genCodeArrayLength(type, var, iName + charUName + "Value", false, code);
	}

	public static void genCodeArrayInitialization(int type, int var, boolean first, StringBuilder code) {
		String tName = fTypeName[type];
		String tUName = typeUpperName[type];
		String vName = fVariable[var];
		String oName = opName[Op_equal];
		String i1Name = immediate[(var + 1) % 2];

		String value = IInternalDebugCoreConstants.EMPTY_STRING;
		boolean f = true;
		for (int i = 0; i < var; i ++) {
			if (f) {
				f = false;
			} else {
				value += " + \", \" + ";
			}
			value += immediate[i % 2] + tUName;
		}

		genCodeEval(vName + tUName + " + " + oName + "Op + \"new " + tName + "[]{\" + " + value + " + \"}\"", first, code);
		genCodeReturnTypeCheck(tName + " array assignment", tName + "[]", first, code);
		if (first) {
			genCodeSetTmp("intValue", T_int, Op_equal, "0", true, code);
			if (type != T_int) {
				genCodeSetTmp(((type == T_String)? "string" : tName) + "Value", type, Op_equal, i1Name + tUName + "Value", true, code);
			}
		}
		genCodeArrayLength(type, var, IInternalDebugCoreConstants.EMPTY_STRING + var, false, code);
		for (int i = 0; i < var; i++) {
			genCodeArrayValue(type, var, i, immediate[i % 2] + tUName + "Value", false, code);
		}
	}

	//----------------------------

	public static void genCodeEval(String expression, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? "IValue " : IInternalDebugCoreConstants.EMPTY_STRING) + "value = eval(" + expression + ");\n");
	}

	public static void genCodeReturnTypeCheck(String test, String typeName, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? "String " : IInternalDebugCoreConstants.EMPTY_STRING) + "typeName = value.getReferenceTypeName();\n" +
					"\t\tassertEquals(\"" + test + " : wrong type : \", \"" + typeName + "\", typeName);\n");
	}

	public static void genCodeReturnValueCheckPrimitiveType(String test, String resType, String uResType, String referenceExpression, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? resType + " " : IInternalDebugCoreConstants.EMPTY_STRING) + resType + "Value = ((IJavaPrimitiveValue)value).get" + uResType + "Value();\n" +
					"\t\tassertEquals(\"" + test + " : wrong result : \", " + referenceExpression + ", " + resType+ "Value);\n");
	}

	public static void genCodeReturnValueCheckFloatDoubleType(String test, String resType, String uResType, String referenceExpression, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? resType + " " : IInternalDebugCoreConstants.EMPTY_STRING) + resType + "Value = ((IJavaPrimitiveValue)value).get" + uResType + "Value();\n" +
					"\t\tassertEquals(\"" + test + " : wrong result : \", " + referenceExpression + ", " + resType+ "Value, 0);\n");
	}

	public static void genCodeReturnValueCheckStringType(String test, String referenceExpression, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? "String " : IInternalDebugCoreConstants.EMPTY_STRING) + "stringValue = ((JDIObjectValue)value).getValueString();\n" +
					"\t\tassertEquals(\"" + test + " : wrong result : \", " + referenceExpression + ", " + "stringValue);\n");
	}

/*	public static void genCodeReturnValueCheckObjectType(String test, String referenceExpression, boolean first, StringBuilder code) {
		code.append("\t\t" + ((first)? "Object " : IInternalDebugUIConstants.EMPTY_STRING) + "objectValue = ((JDIObjectValue)value).getValueString();\n" +
					"\t\tassertEquals(\"" + test + " : wrong result : \" + " + "objectValue, " + "objectValue, " + referenceExpression + ");\n");
	}*/

	public static void genCodeSetTmp(String varName, int type, int op, String value, boolean init , StringBuilder code) {
		code.append("\t\t" + ((init)? fTypeName[type] + " " : IInternalDebugCoreConstants.EMPTY_STRING) + varName + " " + opSymbol[op] + " " + value + ";\n");
	}


	public static void tryBlockBegin(StringBuilder code) {
		code.append("\t\ttry {\n\t\tinit();\n");
	}

	public static void tryBlockEnd(StringBuilder code) {
		code.append("\t\t} finally {\n\t\tend();\n\t\t}\n");
	}

	//------------------------------

	public static void createJavaFile(String className, String testClass, int breakPointLine, int frameNumber, boolean importJDIObjectValue, StringBuilder tests) throws Exception {
		createJavaFile(className, testClass, breakPointLine, frameNumber, importJDIObjectValue, true, tests);
	}

	public static void createJavaFile(String className, String testClass, int breakPointLine, int frameNumber, boolean importJDIObjectValue, boolean importIJavaPrimitiveValue, StringBuilder tests) throws Exception {

		StringBuilder code = new StringBuilder();

		code.append("/*******************************************************************************\n");
		code.append(" * Copyright (c) 2002, 2003 IBM Corporation and others.\n");
		code.append("This program and the accompanying materials \n");
		code.append(" * are made available under the terms of the Eclipse Public License 2.0\n");
		code.append(" * which accompanies this distribution, and is available at\n");
		code.append(" * https://www.eclipse.org/legal/epl-2.0/");
		code.append(" * \n");
		code.append(" * SPDX-License-Identifier: EPL-2.0\n");
		code.append(" * \n");
		code.append(" * Contributors:\n");
		code.append(" *     IBM Corporation - initial API and implementation\n");
		code.append(" *******************************************************************************/\n");
		code.append("package org.eclipse.jdt.debug.tests.eval;\n\n" +
					"import org.eclipse.debug.core.model.IValue;\n");

		if (importIJavaPrimitiveValue) {
			code.append("import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;\n");
		}
		if (importJDIObjectValue) {
			code.append("import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;\n");
		}
		code.append("\n" +
					"public class " + className + " extends Tests {\n" +
					"\n" +
					"\tpublic " + className + "(String arg) {\n" +
					"\t\tsuper(arg);\n" +
					"\t}\n" +
					"\n" +
					"\tprotected void init() throws Exception {\n" +
					"\t\tinitializeFrame(\"" + testClass + "\"," + breakPointLine + "," + frameNumber + ");\n" +
					"\t}\n" +
					"\n" +
					"\tprotected void end() throws Exception {\n" +
					"\t\tdestroyFrame();\n" +
					"\t}\n\n");

		code.append(tests);

		code.append("\n}\n");

		try (Writer file = new FileWriter(new File(className + ".java").getAbsoluteFile())) {
			file.write(code.toString());
		}

	}
}
