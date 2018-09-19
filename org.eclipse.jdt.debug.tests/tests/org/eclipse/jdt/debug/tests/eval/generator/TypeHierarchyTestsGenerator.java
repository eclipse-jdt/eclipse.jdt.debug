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

public class TypeHierarchyTestsGenerator extends TestGenerator {

	static int IAA= 0;
	static int IAB= 1;
	static int IAC= 2;
	static int AA= 3;
	static int AB= 4;
	static int AC= 5;
	static int IBB= 6;
	static int IBC= 7;
	static int BB= 8;
	static int BC= 9;
	static int ICC= 10;
	static int CC= 11;
	static int N_A= 12;
	static int N_B= 13;
	static int N_C= 14;

	static int SUPER_A= 15;
	static int SUPER_B= 16;

	static int M1= 0;
	static int M2= 1;
	static int S2= 2;
	static int M3= 3;
	static int M4= 4;
	static int S4= 5;
	static int M5= 6;
	static int M6= 7;
	static int S6= 8;

	static String[] qualifiers= new String[] {
		"iaa", "iab", "iac",     "aa",      "ab",      "ac", "ibb", "ibc", "bb", "bc",
		"icc", "cc",  "new A()", "new B()", "new C()" ,"super" , "super"};

	static String[] methods= new String[] {"m1", "m2", "s2", "m3", "m4", "s4", "m5", "m6", "s6"};

	static int[] staticLevel= new int[] {
		0, 0, 0, 0, 0, 0, 1, 1, 1, 1,
		2, 2, 0, 1, 2, 0, 1};

	static int[] instanceLevel= new int[] {
		0, 1, 2, 0, 1, 2, 1, 2, 1, 2,
		2, 2, 0, 1, 2, 0, 1};

	static int[][] values= new int[][] {
		{ 1, 2, 9},
		{ 11, 22, 99, 33, 44, 88},
		{ 111, 222, 999, 333, 444, 888, 555, 666, 777}
	};

	public static void main(String[] args) throws Exception {
		gen_main();
		gen_aa_testA();
		gen_ab_testA();
		gen_ac_testA();
		gen_bb_testA();
		gen_bb_testB();
		gen_bc_testA();
		gen_bc_testB();
		gen_cc_testA();
		gen_cc_testB();
		gen_cc_testC();

		System.out.println("done");
	}

	public static void  gen_main() throws Exception {
		StringBuilder code= new StringBuilder();

		createTestQualifier(IAA, M1, code);
		createTestQualifier(IAB, M1, code);
		createTestQualifier(IAC, M1, code);
		createTestQualifier(AA, M1, code);
		createTestQualifier(AA, M2, code);
		createTestQualifier(AA, S2, code);
		createTestQualifier(AB, M1, code);
		createTestQualifier(AB, M2, code);
		createTestQualifier(AB, S2, code);
		createTestQualifier(AC, M1, code);
		createTestQualifier(AC, M2, code);
		createTestQualifier(AC, S2, code);
		createTestQualifier(IBB, M1, code);
		createTestQualifier(IBB, M3, code);
		createTestQualifier(IBC, M1, code);
		createTestQualifier(IBC, M3, code);
		createTestQualifier(BB, M1, code);
		createTestQualifier(BB, M2, code);
		createTestQualifier(BB, S2, code);
		createTestQualifier(BB, M3, code);
		createTestQualifier(BB, M4, code);
		createTestQualifier(BB, S4, code);
		createTestQualifier(BC, M1, code);
		createTestQualifier(BC, M2, code);
		createTestQualifier(BC, S2, code);
		createTestQualifier(BC, M3, code);
		createTestQualifier(BC, M4, code);
		createTestQualifier(BC, S4, code);
		createTestQualifier(ICC, M1, code);
		createTestQualifier(ICC, M3, code);
		createTestQualifier(ICC, M5, code);
		createTestQualifier(CC, M1, code);
		createTestQualifier(CC, M2, code);
		createTestQualifier(CC, S2, code);
		createTestQualifier(CC, M3, code);
		createTestQualifier(CC, M4, code);
		createTestQualifier(CC, S4, code);
		createTestQualifier(CC, M5, code);
		createTestQualifier(CC, M6, code);
		createTestQualifier(CC, S6, code);

		createTestQualifier(N_A, M1, code);
		createTestQualifier(N_A, M2, code);
		createTestQualifier(N_A, S2, code);
		createTestQualifier(N_B, M1, code);
		createTestQualifier(N_B, M2, code);
		createTestQualifier(N_B, S2, code);
		createTestQualifier(N_B, M3, code);
		createTestQualifier(N_B, M4, code);
		createTestQualifier(N_B, S4, code);
		createTestQualifier(N_C, M1, code);
		createTestQualifier(N_C, M2, code);
		createTestQualifier(N_C, S2, code);
		createTestQualifier(N_C, M3, code);
		createTestQualifier(N_C, M4, code);
		createTestQualifier(N_C, S4, code);
		createTestQualifier(N_C, M5, code);
		createTestQualifier(N_C, M6, code);
		createTestQualifier(N_C, S6, code);

		createJavaFile(code, 146, 1, 1);
	}

	public static void gen_aa_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, AA);

		createJavaFile(code, 32, 2, 1);
	}

	public static void gen_ab_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, AB);

		createJavaFile(code, 32, 2, 2);
	}

	public static void gen_ac_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, AC);

		createJavaFile(code, 32, 2, 3);
	}

	public static void gen_bb_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, BB);

		createJavaFile(code, 32, 2, 4);
	}

	public static void gen_bc_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, BC);

		createJavaFile(code, 32, 2, 5);
	}

	public static void gen_cc_testA() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestA(code, CC);

		createJavaFile(code, 32, 2, 6);
	}

	public static void gen_bb_testB() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestB(code, BB);

		createJavaFile(code, 68, 2, 1);
	}

	public static void gen_bc_testB() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestB(code, BC);

		createJavaFile(code, 68, 2, 2);
	}

	public static void gen_cc_testB() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestB(code, CC);

		createJavaFile(code, 68, 2, 3);
	}

	public static void gen_cc_testC() throws Exception {
		StringBuilder code= new StringBuilder();

		createTest_TestC(code, CC);

		createJavaFile(code, 119, 2, 1);
	}

	//-------------

	public static void createTest_TestA(StringBuilder code, int qualifier) {
		createTest(instanceLevel[qualifier], M1, code);
		createTest(instanceLevel[qualifier], M2, code);
		createTest(0, S2, code);
	}

	public static void createTest_TestB(StringBuilder code, int qualifier) {
		createTest(instanceLevel[qualifier], M1, code);
		createTest(instanceLevel[qualifier], M2, code);
		createTest(1, S2, code);
		createTest(instanceLevel[qualifier], M3, code);
		createTest(instanceLevel[qualifier], M4, code);
		createTest(1, S4, code);
		createTestQualifier(SUPER_A, M1, code);
		createTestQualifier(SUPER_A, M2, code);
	}

	public static void createTest_TestC(StringBuilder code, int qualifier) {
		createTest(instanceLevel[qualifier], M1, code);
		createTest(instanceLevel[qualifier], M2, code);
		createTest(2, S2, code);
		createTest(instanceLevel[qualifier], M3, code);
		createTest(instanceLevel[qualifier], M4, code);
		createTest(2, S4, code);
		createTest(instanceLevel[qualifier], M5, code);
		createTest(instanceLevel[qualifier], M6, code);
		createTest(2, S6, code);
		createTestQualifier(SUPER_B, M1, code);
		createTestQualifier(SUPER_B, M2, code);
		createTestQualifier(SUPER_B, M3, code);
		createTestQualifier(SUPER_B, M4, code);
	}

	//-------------

	public static void createTest(int level, int method, StringBuilder code) {
		String strMethod= methods[method];
		code.append("\tpublic void testEvalNestedTypeTest_" + strMethod + "() throws Throwable {\n");
		tryBlockBegin(code);

		genCodeEval("\"" + strMethod + "()\"", true, code);
		genCodeReturnTypeCheck(strMethod, "int", true, code);
		genCodeReturnValueCheckPrimitiveType(strMethod, "int", "Int", IInternalDebugCoreConstants.EMPTY_STRING + values[level][method], true, code);

		tryBlockEnd(code);
		code.append("\t}\n\n");
	}


	public static void createTestQualifier(int qualifier, int method, StringBuilder code) {
		String strQualifier= qualifiers[qualifier];
		String nameQualifier= strQualifier.replace('(', '_').replace(')', '_').replace(' ', '_');
		String strMethod= methods[method];
		int[] level= ((method + 1) % 3 == 0) ? staticLevel : instanceLevel;
		code.append("\tpublic void testEvalNestedTypeTest_" + nameQualifier + "_" + strMethod + "() throws Throwable {\n");
		tryBlockBegin(code);

		genCodeEval("\"" + strQualifier + "." + strMethod + "()\"", true, code);
		genCodeReturnTypeCheck(strQualifier + "." + strMethod, "int", true, code);
		genCodeReturnValueCheckPrimitiveType(strQualifier + "." + strMethod, "int", "Int", IInternalDebugCoreConstants.EMPTY_STRING + values[level[qualifier]][method], true, code);

		tryBlockEnd(code);
		code.append("\t}\n\n");
	}

	//------------


	public static void createJavaFile(StringBuilder tests, int lineNumber, int numberFrames, int hitCount) throws Exception {

		StringBuilder code= new StringBuilder();

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
		code.append("package org.eclipse.jdt.debug.tests.eval;\n\n");
		code.append("import org.eclipse.debug.core.model.IValue;\n");
		code.append("import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;\n\n");
		code.append("public class TypeHierarchy_" + lineNumber + "_" + hitCount + " extends Tests {\n");
		code.append("\t/**\n");
		code.append("\t * Constructor for TypeHierarchy.\n");
		code.append("\t * @param name\n");
		code.append("\t */\n");
		code.append("\tpublic TypeHierarchy_" + lineNumber + "_" + hitCount + "(String name) {\n");
		code.append("\t\tsuper(name);\n");
		code.append("\t}\n\n");
		code.append("\tpublic void init() throws Exception {\n");
		code.append("\t\tinitializeFrame(\"EvalTypeHierarchyTests\", " + lineNumber + ", " + numberFrames + ", " + hitCount + ");\n");
		code.append("\t}\n\n");
		code.append("\tprotected void end() throws Exception {\n");
		code.append("\t\tdestroyFrame();\n");
		code.append("\t}\n\n");

		code.append(tests.toString());

		code.append("}\n");

		try (Writer file = new FileWriter(new File("TypeHierarchy_" + lineNumber + "_" + hitCount + ".java").getAbsoluteFile())) {
			file.write(code.toString());
		}

	}
}
