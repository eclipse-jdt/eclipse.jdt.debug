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

public class OtherTestsGenerator extends TestGenerator {

	public static void main(String[] args) throws Exception {

		genTestsOperators1();
		genTestsOperators2();
		genTestsArray();
		genTestsNestedTypes1();
		genTestsNestedTypes2();
		genTestsTypeHierarchy1();
		genTestsTypeHierarchy2();
		genTestNumberLiteral();

		genInstanceOfTests();

		System.out.println("done");
	}

	public static void genTestsOperators1() throws Exception {
		StringBuilder code= new StringBuilder();

		genTestTypeBinaryOpTypeBinaryPromotion(T_int, Op_plus, T_int, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_String, code);
		genTestLocalVarValue(T_int, code);
		genTestLocalVarValue(T_String, code);

		createJavaFile(code, "TestsOperators1", "EvalSimpleTests", 37, 1, 1);
	}

	public static void genTestsOperators2() throws Exception {
		StringBuilder code= new StringBuilder();

		genTestLocalVarAssignment(T_int, code);
		genTestLocalVarAssignment(T_String, code);
		genTestTypeAssignmentOpType(T_int, Op_plusAss, T_int, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_String, code);

		createJavaFile(code, "TestsOperators2", "EvalSimpleTests", 37, 1, 1);
	}

	public static void genTestsArray() throws Exception {
		StringBuilder code= new StringBuilder();

		genTestArrayValue(T_int, code);
		genTestArrayLength(T_int, code);
		genTestArrayAssignment(T_int, code);
		genTestArrayInitialization(T_int, code);

		genTestArrayValue(T_String, code);
		genTestArrayLength(T_String, code);
		genTestArrayAssignment(T_String, code);
		genTestArrayInitialization(T_String, code);

		createJavaFile(code, "TestsArrays", "EvalArrayTests", 37, 1, 1);
	}

	public static void genTestsNestedTypes1() throws Exception {
		StringBuilder code= new StringBuilder();

		NestedTypeTestGenerator.createTest('a', 2, code);
		NestedTypeTestGenerator.createTest('d', 2, code);
		NestedTypeTestGenerator.createTest('e', 2, code);
		NestedTypeTestGenerator.createTest('h', 2, code);
		NestedTypeTestGenerator.createTest('i', 2, code);
		NestedTypeTestGenerator.createTestThis('c', 2, code);
		NestedTypeTestGenerator.createTestThis('f', 2, code);
		NestedTypeTestGenerator.createTestThis('j', 2, code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T, 'b', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A_AA, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A_AB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A_AA, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A_AB, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_B, 'h', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_B_BB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B_BB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_BB, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B_this, 'c', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B_this, 'h', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_this, 'a', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_this, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_this, 'e', code);

		createJavaFile(code, "TestsNestedTypes1", "EvalNestedTypeTests",  252, 4, 1);
	}

	public static void genTestsNestedTypes2() throws Exception {
		StringBuilder code= new StringBuilder();

		NestedTypeTestGenerator.createTest('f', 0, code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T, 'b', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A_AA, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_A_AB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A_AA, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_A_AB, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_B, 'h', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B, 'd', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_T_B_BB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.T_B_BB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_A, 'h', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AA, 'c', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AA, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AA, 'j', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AB, 'c', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AB, 'f', code);
		NestedTypeTestGenerator.createTestQualifier(NestedTypeTestGenerator.I_AB, 'i', code);

		createJavaFile(code, "TestsNestedTypes2", "EvalNestedTypeTests",  739, 2, 1);
	}


	public static void genTestsTypeHierarchy1() throws Exception {
		StringBuilder code= new StringBuilder();

		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.IAA, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.AA, TypeHierarchyTestsGenerator.M2, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.AB, TypeHierarchyTestsGenerator.S2, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.AC, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.IBB, TypeHierarchyTestsGenerator.M3, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.IBC, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.BB, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.BB, TypeHierarchyTestsGenerator.M3, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.BC, TypeHierarchyTestsGenerator.S2, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.BC, TypeHierarchyTestsGenerator.S4, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.ICC, TypeHierarchyTestsGenerator.M3, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.CC, TypeHierarchyTestsGenerator.M2, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.CC, TypeHierarchyTestsGenerator.M4, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.CC, TypeHierarchyTestsGenerator.M6, code);

		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_A, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_B, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_B, TypeHierarchyTestsGenerator.M2, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_B, TypeHierarchyTestsGenerator.S4, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_C, TypeHierarchyTestsGenerator.M1, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_C, TypeHierarchyTestsGenerator.M4, code);
		TypeHierarchyTestsGenerator.createTestQualifier(TypeHierarchyTestsGenerator.N_C, TypeHierarchyTestsGenerator.S6, code);

		createJavaFile(code, "TestsTypeHierarchy1", "EvalTypeHierarchyTests",   146, 1, 1, false);
	}

	public static void genTestsTypeHierarchy2() throws Exception {
		StringBuilder code= new StringBuilder();

		TypeHierarchyTestsGenerator.createTest_TestC(code, TypeHierarchyTestsGenerator.CC);

		createJavaFile(code, "TestsTypeHierarchy2", "EvalTypeHierarchyTests",   119, 2, 1, false);
	}

	/**
	 * Method genTestNumberLiteral.
	 */
	private static void genTestNumberLiteral() throws Exception {
		StringBuilder code= new StringBuilder();

		createTestNumberLiteral1("0", T_int, code);
		createTestNumberLiteral1("00", T_int, code);
		createTestNumberLiteral1("0x0", T_int, code);
		createTestNumberLiteral1("-1", T_int, code);
		createTestNumberLiteral1("1", T_int, code);
		createTestNumberLiteral1("2147483647", T_int, code);
		createTestNumberLiteral1("-2147483648", T_int, code);
		createTestNumberLiteral1("0x7fffffff", T_int, code);
		createTestNumberLiteral1("0x80000000", T_int, code);
		createTestNumberLiteral1("0xffffffff", T_int, code);
		createTestNumberLiteral1("017777777777", T_int, code);
		createTestNumberLiteral1("020000000000", T_int, code);
		createTestNumberLiteral1("037777777777", T_int, code);
		createTestNumberLiteral1("2", T_int, code);
		createTestNumberLiteral1("0372", T_int, code);
		createTestNumberLiteral1("0xDadaCafe", T_int, code);
		createTestNumberLiteral1("1996", T_int, code);
		createTestNumberLiteral1("0x00FF00FF", T_int, code);

		createTestNumberLiteral1("0L", T_long, code);
		createTestNumberLiteral1("00L", T_long, code);
		createTestNumberLiteral1("0x0L", T_long, code);
		createTestNumberLiteral1("-1L", T_long, code);
		createTestNumberLiteral1("1L", T_long, code);
		createTestNumberLiteral1("9223372036854775807L", T_long, code);
		createTestNumberLiteral1("-9223372036854775808L", T_long, code);
		createTestNumberLiteral1("0x7fffffffffffffffL", T_long, code);
		createTestNumberLiteral1("0x8000000000000000L", T_long, code);
		createTestNumberLiteral1("0xffffffffffffffffL", T_long, code);
		createTestNumberLiteral1("0777777777777777777777L", T_long, code);
		createTestNumberLiteral1("01000000000000000000000L", T_long, code);
		createTestNumberLiteral1("01777777777777777777777L", T_long, code);
		createTestNumberLiteral1("0777l", T_long, code);
		createTestNumberLiteral1("0x100000000L", T_long, code);
		createTestNumberLiteral1("2147483648L", T_long, code);
		createTestNumberLiteral1("0xC0B0L", T_long, code);

		createTestNumberLiteral2("3.40282347e+38f", T_float, code);
		createTestNumberLiteral2("1.40239846e-45f", T_float, code);
		createTestNumberLiteral2("1e1f", T_float, code);
		createTestNumberLiteral2("2.f", T_float, code);
		createTestNumberLiteral2(".3f", T_float, code);
		createTestNumberLiteral2("0f", T_float, code);
		createTestNumberLiteral2("3.14f", T_float, code);
		createTestNumberLiteral2("6.022137e+23f", T_float, code);

		createTestNumberLiteral2("1.79769313486231570e+308", T_double, code);
		createTestNumberLiteral2("4.94065645841246544e-324", T_double, code);
		createTestNumberLiteral2("1e1", T_double, code);
		createTestNumberLiteral2("2.", T_double, code);
		createTestNumberLiteral2(".3", T_double, code);
		createTestNumberLiteral2("0.0", T_double, code);
		createTestNumberLiteral2("3.14", T_double, code);
		createTestNumberLiteral2("1e-9d", T_double, code);
		createTestNumberLiteral2("1e137", T_double, code);

		createJavaFile(code, "TestsNumberLiteral", "EvalSimpleTests", 37, 1, 1, false);
	}

	/**
	 * Method createTestNumberLiteral.
	 */
	private static void createTestNumberLiteral1(String literal, int type, StringBuilder code) {
		String tName= fTypeName[type];

		code.append("\tpublic void test" + literal.replace('-', 'N').replace('.', '_').replace('+', 'P') + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeEval("\"" + literal + '"', true, code);
		genCodeReturnTypeCheck(literal, tName, true, code);
		genCodeReturnValueCheckPrimitiveType(literal, tName, typeUpperName[type], literal, true, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	/**
	 * Method createTestNumberLiteral.
	 */
	private static void createTestNumberLiteral2(String literal, int type, StringBuilder code) {
		String tName= fTypeName[type];

		code.append("\tpublic void test" + literal.replace('-', 'N').replace('.', '_').replace('+', 'P') + "() throws Throwable {\n");
		tryBlockBegin(code);
		genCodeEval("\"" + literal + '"', true, code);
		genCodeReturnTypeCheck(literal, tName, true, code);
		genCodeReturnValueCheckFloatDoubleType(literal, tName, typeUpperName[type], literal, true, code);
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	public static void genInstanceOfTests() throws Exception {
	}


	public static void createJavaFile(StringBuilder tests, String className, String testClass, int lineNumber, int numberFrames, int hitCount) throws Exception {
		createJavaFile(tests, className, testClass, lineNumber, numberFrames, hitCount, true);
	}

	public static void createJavaFile(StringBuilder tests, String className, String testClass, int lineNumber, int numberFrames, int hitCount, boolean importJDIObjectValue) throws Exception {

		StringBuilder code= new StringBuilder();

		code.append("/*******************************************************************************\n");
		code.append(" * Copyright (c) 2002, 2003 IBM Corporation and others.\n");
		code.append(" * \n");
		code.append(" This program and the accompanying materials \n");
		code.append(" * are made available under the terms of the Eclipse Public License 2.0\n");
		code.append(" * which accompanies this distribution, and is available at\n");
		code.append(" * https://www.eclipse.org/legal/epl-2.0/");
		code.append(" * \n");
		code.append(" * SPDX-License-Identifier: EPL-2.0\n");
		code.append(" * \n");
		code.append(" * Contributors:\n");
		code.append(" *     IBM Corporation - initial API and implementation\n");
		code.append(" *******************************************************************************/\n");
		code.append("package org.eclipse.jdt.debug.tests.eval;\n\n");
		code.append("import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;\n\n");
		code.append("import org.eclipse.debug.core.model.IValue;\n");
		if (importJDIObjectValue) {
			code.append("import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;\n\n");
		}
		code.append("public class " + className + " extends Tests {\n");
		code.append("\t/**\n");
		code.append("\t * Constructor for TypeHierarchy.\n");
		code.append("\t * @param name\n");
		code.append("\t */\n");
		code.append("\tpublic " + className + "(String name) {\n");
		code.append("\t\tsuper(name);\n");
		code.append("\t}\n\n");
		code.append("\tpublic void init() throws Exception {\n");
		code.append("\t\tinitializeFrame(\"" + testClass + "\", " + lineNumber + ", " + numberFrames + ", " + hitCount + ");\n");
		code.append("\t}\n\n");
		code.append("\tprotected void end() throws Exception {\n");
		code.append("\t\tdestroyFrame();\n");
		code.append("\t}\n\n");

		code.append(tests.toString());

		code.append("}\n");

		try (Writer file = new FileWriter(new File(className + ".java").getAbsoluteFile())) {
			file.write(code.toString());
		}

	}
}
