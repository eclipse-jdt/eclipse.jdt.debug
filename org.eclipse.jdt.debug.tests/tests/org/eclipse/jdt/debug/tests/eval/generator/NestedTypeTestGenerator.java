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

public class NestedTypeTestGenerator extends TestGenerator {

	static final int T_T = 0;
	static final int T_T_A= 1;
	static final int T_A= 2;
	static final int T_T_A_AA= 3;
	static final int T_A_AA= 4;
	static final int T_AA= 5;
	static final int T_T_A_AB= 44;
	static final int T_A_AB= 45;
	static final int T_AB= 46;
	static final int T_T_B= 39;
	static final int T_B= 40;
	static final int T_T_B_BB= 41;
	static final int T_B_BB= 42;
	static final int T_BB= 43;
	static final int T_C= -1;
	static final int T_E= -1;

	static final int T_T_this= 6;
	static final int T_T_A_this= 7;
	static final int T_A_this= 8;
	static final int T_B_this= 9;
	static final int T_C_this= 10;
	static final int T_E_this= 11;

	static final int I_A= 12;
	static final int I_AA= 13;
	static final int I_AB= 14;
	static final int I_AC= 15;
	static final int I_AD= 16;
	static final int I_AE= 17;
	static final int I_AF= 18;
	static final int I_B= 19;
	static final int I_BB= 20;
	static final int I_BC= 21;
	static final int I_BD= 22;
	static final int I_C= 23;
	static final int I_CB= 24;
	static final int I_CC= 25;
	static final int I_CD= 26;
	static final int I_D= 27;
	static final int I_DB= 28;
	static final int I_DC= 29;
	static final int I_DD= 30;
	static final int I_E= 31;
	static final int I_EB= 32;
	static final int I_EC= 33;
	static final int I_ED= 34;
	static final int I_F= 35;
	static final int I_FB= 36;
	static final int I_FC= 37;
	static final int I_FD= 38;

	static final String[] qualifiers= 
		{"T_T",     "T_T_A",    "T_A",    "T_T_A_AA", "T_A_AA",   "T_AA",   "T_T_this", "T_T_Athis", "T_A_this", "T_B_this",
		"T_C_this", "T_E_this", "I_A",    "I_AA",     "I_AB",     "I_AC",   "I_AD",     "I_AE",      "I_AF",     "I_B",
		"I_BB",     "I_BC",     "I_BD",   "I_C",      "I_CB",     "I_CC",   "I_CD",     "I_D",       "I_DB",     "I_DC",
		"I_DD",     "I_E",      "I_EB",   "I_EC",     "I_ED",     "I_F",    "I_FB",     "I_FC",      "I_FD",     "T_T_B",
		"T_B",      "T_T_B_BB", "T_B_BB", "T_BB",     "T_T_A_AB", "T_A_AB", "T_AB"};
	
	static final int [] qualifiersLevel=
		{0, 1, 1, 2, 2, 2, 0, 1, 1, 1,
		 1, 1, 1, 2, 2, 2, 2, 2, 2, 1,
		 2, 2, 2, 1, 2, 2, 2, 1, 2, 2,
		 2, 1, 2, 2, 2, 1, 2, 2, 2, 1,
		 1, 2, 2, 2, 2, 2, 2};

	public static void main(String[] args) throws Exception {
		gen_AA_aa();
		gen_AA_aaStatic();
		gen_AB_ab();
		gen_AC_ac();
		gen_AD_ad();
		gen_A_a();
		gen_AE_ae();
		gen_AF_af();
		gen_A_aStatic();
		gen_BB_bb();
		gen_BC_bc();
		gen_BD_bd();
		gen_B_b();
		gen_CB_cb();
		gen_CC_cc();
		gen_CD_cd();
		gen_C_c();
		gen_DB_db();
		gen_DC_dc();
		gen_DD_dd();
		gen_D_d();
		gen_EB_eb();
		gen_EC_ec();
		gen_ED_ed();
		gen_E_e();
		gen_FB_fb();
		gen_FC_fc();
		gen_FD_fd();
		gen_F_f();
		gen_evalNestedTypeTest();
		gen_evalNestedTypeTestStatic();
		System.out.println("done");
	}
	
	public static void gen_AA_aa() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('c', 2, code);
		createTest('d', 2, code);
		createTest('e', 2, code);
		createTest('f', 2, code);
		createTest('h', 2, code);
		createTest('i', 2, code);
		createTest('j', 2, code);
		createTestThis('c', 2, code);
		createTestThis('d', 2, code);
		createTestThis('e', 2, code);
		createTestThis('f', 2, code);
		createTestThis('i', 2, code);
		createTestThis('j', 2, code);
		createTestsStaticFields_A(code);
		
		createJavaFile(code, 54, 4);
	}
	
	public static void gen_AA_aaStatic() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('d', 2, code);
		createTest('f', 2, code);
		createTest('h', 2, code);
		createTest('j', 2, code);
		createTestsStaticFields_A(code);

		createJavaFile(code, 58, 4);
	}
	
	public static void gen_AB_ab() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('c', 2, code);
		createTest('d', 2, code);
		createTest('e', 2, code);
		createTest('f', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTest('i', 2, code);
		createTest('j', 2, code);
		createTestThis('c', 2, code);
		createTestThis('d', 2, code);
		createTestThis('e', 2, code);
		createTestThis('f', 2, code);
		createTestThis('i', 2, code);
		createTestThis('j', 2, code);
		createTestsStaticFields_A(code);
		createTestQualifier(T_A_this, 'c', code);
		createTestQualifier(T_A_this, 'd', code);
		createTestQualifier(T_A_this, 'g', code);
		createTestQualifier(T_A_this, 'h', code);
		
		createJavaFile(code, 83, 4);
	}

	public static void gen_AC_ac() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_A(code);
		createTestQualifier(T_A_this, 'c', code);
		createTestQualifier(T_A_this, 'd', code);
		createTestQualifier(T_A_this, 'g', code);
		createTestQualifier(T_A_this, 'h', code);
		
		createJavaFile(code, 109, 4);
	}
	
	public static void gen_AD_ad() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_A(code);
		createTestQualifier(T_A_this, 'c', code);
		createTestQualifier(T_A_this, 'd', code);
		createTestQualifier(T_A_this, 'g', code);
		createTestQualifier(T_A_this, 'h', code);
		
		createJavaFile(code, 134, 4);
	}
	
	public static void gen_A_a() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 1, code);
		createTest('c', 1, code);
		createTest('d', 1, code);
		createTest('f', 1, code);
		createTest('g', 1, code);
		createTest('h', 1, code);
		createTestThis('c', 1, code);
		createTestThis('d', 1, code);
		createTestThis('g', 1, code);
		createTestThis('h', 1, code);
		createTestsStaticFields_A(code);
		createTestQualifier(I_AB, 'c', code);
		createTestQualifier(I_AB, 'd', code);
		createTestQualifier(I_AB, 'e', code);
		createTestQualifier(I_AB, 'f', code);
		createTestQualifier(I_AB, 'i', code);
		createTestQualifier(I_AB, 'j', code);
		
		createJavaFile(code, 144, 3);
	}
	
	public static void gen_AE_ae() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_A(code);
		
		createJavaFile(code, 168, 4);
	}
	
	public static void gen_AF_af() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_A(code);
		
		createJavaFile(code, 192, 4);
	}
	
	public static void gen_A_aStatic() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 1, code);
		createTest('d', 1, code);
		createTest('f', 1, code);
		createTest('h', 1, code);
		createTestsStaticFields_A(code);
		createTestQualifier(I_AA, 'c', code);
		createTestQualifier(I_AA, 'd', code);
		createTestQualifier(I_AA, 'e', code);
		createTestQualifier(I_AA, 'f', code);
		createTestQualifier(I_AA, 'i', code);
		createTestQualifier(I_AA, 'j', code);
		
		createJavaFile(code, 203, 3);
	}
	
	public static void gen_BB_bb() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTest('c', 2, code);
		createTest('d', 2, code);
		createTest('e', 2, code);
		createTest('f', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTest('i', 2, code);
		createTest('j', 2, code);
		createTestThis('c', 2, code);
		createTestThis('d', 2, code);
		createTestThis('e', 2, code);
		createTestThis('f', 2, code);
		createTestThis('i', 2, code);
		createTestThis('j', 2, code);
		createTestsStaticFields_B(code);
		createTestQualifier(T_B_this, 'c', code);
		createTestQualifier(T_B_this, 'd', code);
		createTestQualifier(T_B_this, 'g', code);
		createTestQualifier(T_B_this, 'h', code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 241, 4);
	}

	public static void gen_BC_bc() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_B(code);
		createTestQualifier(T_B_this, 'c', code);
		createTestQualifier(T_B_this, 'd', code);
		createTestQualifier(T_B_this, 'g', code);
		createTestQualifier(T_B_this, 'h', code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 268, 4);
	}

	public static void gen_BD_bd() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTest('g', 2, code);
		createTest('h', 2, code);
		createTestsStaticFields_B(code);
		createTestQualifier(T_B_this, 'c', code);
		createTestQualifier(T_B_this, 'd', code);
		createTestQualifier(T_B_this, 'g', code);
		createTestQualifier(T_B_this, 'h', code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 293, 4);
	}

	public static void gen_B_b() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 1, code);
		createTest('b', 1, code);
		createTest('c', 1, code);
		createTest('d', 1, code);
		createTest('e', 1, code);
		createTest('f', 1, code);
		createTest('g', 1, code);
		createTest('h', 1, code);
		createTestThis('c', 1, code);
		createTestThis('d', 1, code);
		createTestThis('g', 1, code);
		createTestThis('h', 1, code);
		createTestsStaticFields_B(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		createTestQualifier(I_BB, 'c', code);
		createTestQualifier(I_BB, 'd', code);
		createTestQualifier(I_BB, 'e', code);
		createTestQualifier(I_BB, 'f', code);
		createTestQualifier(I_BB, 'i', code);
		createTestQualifier(I_BB, 'j', code);
		
		createJavaFile(code, 304, 3);
	}

	public static void gen_CB_cb() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 343, 4);
	}

	public static void gen_CC_cc() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 370, 4);
	}

	public static void gen_CD_cd() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 395, 4);
	}

	public static void gen_C_c() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 1, code);
		createTest('b', 1, code);
		createTest('e', 1, code);
		createTest('f', 1, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 406, 3);
	}

	public static void gen_DB_db() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 444, 4);
	}

	public static void gen_DC_dc() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 470, 4);
	}

	public static void gen_DD_dd() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 2, code);
		createTest('b', 2, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 495, 4);
	}

	public static void gen_D_d() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('a', 1, code);
		createTest('b', 1, code);
		createTest('e', 1, code);
		createTest('f', 1, code);
		createTestsStaticFields(code);
		createTestQualifier(T_T_this, 'a', code);
		createTestQualifier(T_T_this, 'b', code);
		createTestQualifier(T_T_this, 'c', code);
		createTestQualifier(T_T_this, 'd', code);
		createTestQualifier(T_T_this, 'e', code);
		createTestQualifier(T_T_this, 'f', code);
		
		createJavaFile(code, 506, 3);
	}
	
	public static void gen_evalNestedTypeTest() throws Exception {
		StringBuffer code= new StringBuffer();
		
		
		createTest('a', 0, code);
		createTest('b', 0, code);
		createTest('c', 0, code);
		createTest('d', 0, code);
		createTest('e', 0, code);
		createTest('f', 0, code);
		createTestThis('a', 0, code);
		createTestThis('b', 0, code);
		createTestThis('c', 0, code);
		createTestThis('d', 0, code);
		createTestThis('e', 0, code);
		createTestThis('f', 0, code);
		createTestsStaticFields(code);
		createTestQualifier(I_B, 'c', code);
		createTestQualifier(I_B, 'd', code);
		createTestQualifier(I_B, 'g', code);
		createTestQualifier(I_B, 'h', code);
		createTestQualifier(I_BB, 'c', code);
		createTestQualifier(I_BB, 'd', code);
		createTestQualifier(I_BB, 'e', code);
		createTestQualifier(I_BB, 'f', code);
		createTestQualifier(I_BB, 'i', code);
		createTestQualifier(I_BB, 'j', code);
		
		createJavaFile(code, 518, 2);
	}

	public static void gen_EB_eb() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 555, 4);
	}

	public static void gen_EC_ec() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 581, 4);
	}

	public static void gen_ED_ed() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 605, 4);
	}

	public static void gen_E_e() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 1, code);
		createTest('f', 1, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 615, 3);
	}
	
	public static void gen_FB_fb() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 653, 4);
	}

	public static void gen_FC_fc() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 679, 4);
	}

	public static void gen_FD_fd() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 2, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 703, 4);
	}

	public static void gen_F_f() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 1, code);
		createTest('f', 1, code);
		createTestsStaticFields(code);
		
		createJavaFile(code, 713, 3);
	}
	
	public static void gen_evalNestedTypeTestStatic() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTest('b', 0, code);
		createTest('d', 0, code);
		createTest('f', 0, code);
		createTestsStaticFields(code);
		createTestQualifier(I_A, 'c', code);
		createTestQualifier(I_A, 'd', code);
		createTestQualifier(I_A, 'g', code);
		createTestQualifier(I_A, 'h', code);
		createTestQualifier(I_AA, 'c', code);
		createTestQualifier(I_AA, 'd', code);
		createTestQualifier(I_AA, 'e', code);
		createTestQualifier(I_AA, 'f', code);
		createTestQualifier(I_AA, 'i', code);
		createTestQualifier(I_AA, 'j', code);
		createTestQualifier(I_AB, 'c', code);
		createTestQualifier(I_AB, 'd', code);
		createTestQualifier(I_AB, 'e', code);
		createTestQualifier(I_AB, 'f', code);
		createTestQualifier(I_AB, 'i', code);
		createTestQualifier(I_AB, 'j', code);
		
		createJavaFile(code, 728, 2);
	}
	
	public static void gen_main() throws Exception {
		StringBuffer code= new StringBuffer();
		
		createTestsStaticFields(code);

		createJavaFile(code, 734, 1);
	}

	// ------------------------------
	
	public static void createTestsStaticFields(StringBuffer code) {
		createTestQualifier(T_T, 'b', code);
		createTestQualifier(T_T, 'd', code);
		createTestQualifier(T_T, 'f', code);
		createTestQualifier(T_T_A, 'd', code);
		createTestQualifier(T_T_A, 'h', code);
		createTestQualifier(T_A, 'd', code);
		createTestQualifier(T_A, 'h', code);
		createTestQualifier(T_T_A_AA, 'd', code);
		createTestQualifier(T_T_A_AA, 'f', code);
		createTestQualifier(T_T_A_AA, 'j', code);
		createTestQualifier(T_T_A_AB, 'd', code);
		createTestQualifier(T_T_A_AB, 'f', code);
		createTestQualifier(T_T_A_AB, 'j', code);
		createTestQualifier(T_A_AA, 'd', code);
		createTestQualifier(T_A_AA, 'f', code);
		createTestQualifier(T_A_AA, 'j', code);
		createTestQualifier(T_A_AB, 'd', code);
		createTestQualifier(T_A_AB, 'f', code);
		createTestQualifier(T_A_AB, 'j', code);
		createTestQualifier(T_T_B, 'd', code);
		createTestQualifier(T_T_B, 'h', code);
		createTestQualifier(T_B, 'd', code);
		createTestQualifier(T_B, 'h', code);
		createTestQualifier(T_T_B_BB, 'd', code);
		createTestQualifier(T_T_B_BB, 'f', code);
		createTestQualifier(T_T_B_BB, 'j', code);
		createTestQualifier(T_B_BB, 'd', code);
		createTestQualifier(T_B_BB, 'f', code);
		createTestQualifier(T_B_BB, 'j', code);
	}
	
	public static void createTestsStaticFields_A(StringBuffer code) {
		createTestsStaticFields(code);
		createTestQualifier(T_AA, 'd', code);
		createTestQualifier(T_AA, 'f', code);
		createTestQualifier(T_AA, 'j', code);
		createTestQualifier(T_AB, 'd', code);
		createTestQualifier(T_AB, 'f', code);
		createTestQualifier(T_AB, 'j', code);
	}
	
	public static void createTestsStaticFields_B(StringBuffer code) {
		createTestsStaticFields(code);
		createTestQualifier(T_BB, 'd', code);
		createTestQualifier(T_BB, 'f', code);
		createTestQualifier(T_BB, 'j', code);
	}
	
	// ------------------------------
	
	public static void createTest(char variable, int level, StringBuffer code) {
		code.append("\tpublic void testEvalNestedTypeTest_" + variable + "() throws Throwable {\n");
		tryBlockBegin(code);
		
		genCodeEval("" + variable + "Int", true, code);
		genCodeReturnTypeCheck("" + variable, "int", true, code);
		genCodeReturnValueCheckPrimitiveType("" + variable, "int", "Int", variable + "IntValue_" + level, true, code);
		code.append("\n");
		
		genCodeEval("" + variable + "String", false, code);
		genCodeReturnTypeCheck("" + variable, "java.lang.String", false, code);
		genCodeReturnValueCheckStringType("" + variable, variable + "StringValue_" + level, true, code);
		
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}
	
	public static void createTestThis(char variable, int level, StringBuffer code) {
		code.append("\tpublic void testEvalNestedTypeTest_this_" + variable + "() throws Throwable {\n");
		tryBlockBegin(code);
		
		genCodeEval("THIS + " + variable + "Int", true, code);
		genCodeReturnTypeCheck("" + variable, "int", true, code);
		genCodeReturnValueCheckPrimitiveType("" + variable, "int", "Int", variable + "IntValue_" + level, true, code);
		code.append("\n");
		
		genCodeEval("THIS + " + variable + "String", false, code);
		genCodeReturnTypeCheck("" + variable, "java.lang.String", false, code);
		genCodeReturnValueCheckStringType("" + variable, variable + "StringValue_" + level, true, code);
		
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}
	
	
	
	public static void createTestQualifier(int qualifier, char variable, StringBuffer code) {
		String strQualifier= qualifiers[qualifier];
		code.append("\tpublic void testEvalNestedTypeTest_" + strQualifier + "_" + variable + "() throws Throwable {\n");
		tryBlockBegin(code);
		
		genCodeEval(strQualifier + " + " + variable + "Int", true, code);
		genCodeReturnTypeCheck("" + strQualifier + "_" + variable, "int", true, code);
		genCodeReturnValueCheckPrimitiveType("" + strQualifier + "_" + variable, "int", "Int", variable + "IntValue_" + qualifiersLevel[qualifier], true, code);
		code.append("\n");
		
		genCodeEval(strQualifier + " + " + variable + "String", false, code);
		genCodeReturnTypeCheck("" + strQualifier + "_" + variable, "java.lang.String", false, code);
		genCodeReturnValueCheckStringType("" + strQualifier + "_" + variable, variable + "StringValue_" + qualifiersLevel[qualifier], true, code);
		
		tryBlockEnd(code);
		code.append("\t}\n\n");
	}
	
	//---------------------
	
	public static void createJavaFile(StringBuffer tests, int lineNumber, int numberFrames) throws Exception {
		
		StringBuffer code= new StringBuffer();
		
		code.append("package org.eclipse.jdt.debug.tests.eval;\n\n");
		code.append("import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;\n\n");
		code.append("import org.eclipse.debug.core.model.IValue;\n");
		code.append("import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;\n\n");
		code.append("public class NestedTypeFieldValue_" + lineNumber + " extends Tests {\n");
		code.append("\t/**\n");
		code.append("\t * Constructor for NestedTypeFieldValue.\n");
		code.append("\t * @param name\n");
		code.append("\t */\n");
		code.append("\tpublic NestedTypeFieldValue_" + lineNumber + "(String name) {\n");
		code.append("\t\tsuper(name);\n");
		code.append("\t}\n\n");
		code.append("\tpublic void init() throws Exception {\n");
		code.append("\t\tinitializeFrame(\"EvalNestedTypeTests\", " + lineNumber + ", " + numberFrames + ");\n");
		code.append("\t}\n\n");
		code.append("\tprotected void end() throws Exception {\n");
		code.append("\t\tdestroyFrame();\n");
		code.append("\t}\n\n");
		
		code.append(tests.toString());
		
		code.append("}\n");
		
		Writer file = new FileWriter(new File("NestedTypeFieldValue_" + lineNumber + ".java").getAbsoluteFile());
		
		file.write(code.toString());
		
		file.close();
		
	}
}
