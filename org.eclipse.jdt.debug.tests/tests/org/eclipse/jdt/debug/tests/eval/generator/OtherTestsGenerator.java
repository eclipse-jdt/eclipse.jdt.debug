package org.eclipse.jdt.debug.tests.eval.generator;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

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
		
		genInstanceOfTests();

		System.out.println("done");
	}

	public static void genTestsOperators1() throws Exception {
		StringBuffer code= new StringBuffer();
		
		genTestTypeBinaryOpTypeBinaryPromotion(T_int, Op_plus, T_int, code);
		genTestTypeBinaryOpTypeBinaryPromotion(T_String, Op_plus, T_String, code);
		genTestLocalVarValue(T_int, code);
		genTestLocalVarValue(T_String, code);
		
		createJavaFile(code, "TestsOperators1", "EvalSimpleTests", 27, 1, 1);
	}

	public static void genTestsOperators2() throws Exception {
		StringBuffer code= new StringBuffer();
		
		genTestLocalVarAssignment(T_int, code);
		genTestLocalVarAssignment(T_String, code);
		genTestTypeAssignmentOpType(T_int, Op_plusAss, T_int, code);
		genTestTypeAssignmentOpType(T_String, Op_plusAss, T_String, code);
		
		createJavaFile(code, "TestsOperators2", "EvalSimpleTests", 27, 1, 1);
	}
	
	public static void genTestsArray() throws Exception {
		StringBuffer code= new StringBuffer();
		
		genTestArrayValue(T_int, code);
		genTestArrayLength(T_int, code);
		genTestArrayAssignment(T_int, code);
		genTestArrayInitialization(T_int, code);

		genTestArrayValue(T_String, code);
		genTestArrayLength(T_String, code);
		genTestArrayAssignment(T_String, code);
		genTestArrayInitialization(T_String, code);
		
		createJavaFile(code, "TestsArrays", "EvalArrayTests", 27, 1, 1);
	}

	public static void genTestsNestedTypes1() throws Exception {
		StringBuffer code= new StringBuffer();
		
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

		createJavaFile(code, "TestsNestedTypes1", "EvalNestedTypeTests",  241, 4, 1);
	}

	public static void genTestsNestedTypes2() throws Exception {
		StringBuffer code= new StringBuffer();
		
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

		createJavaFile(code, "TestsNestedTypes2", "EvalNestedTypeTests",  728, 2, 1);
	}


	public static void genTestsTypeHierarchy1() throws Exception {
		StringBuffer code= new StringBuffer();
		
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

		createJavaFile(code, "TestsTypeHierarchy1", "EvalTypeHierarchyTests",   135, 1, 1);
	}

	public static void genTestsTypeHierarchy2() throws Exception {
		StringBuffer code= new StringBuffer();

		TypeHierarchyTestsGenerator.createTest_TestC(code, TypeHierarchyTestsGenerator.CC);
		
		createJavaFile(code, "TestsTypeHierarchy2", "EvalTypeHierarchyTests",   108, 2, 1);
	}

	public static void genInstanceOfTests() throws Exception {
	}

	public static void createJavaFile(StringBuffer tests, String className, String testClass, int lineNumber, int numberFrames, int hitCount) throws Exception {
		
		StringBuffer code= new StringBuffer();
		
		code.append("package org.eclipse.jdt.debug.tests.eval;\n\n");
		code.append("import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;\n\n");
		code.append("import org.eclipse.debug.core.model.IValue;\n");
		code.append("import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;\n\n");
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
		
		Writer file = new FileWriter(new File(className + ".java").getAbsoluteFile());
		
		file.write(code.toString());
		
		file.close();
		
	}
}
