/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.eval;

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class TestsNestedTypes2 extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TestsNestedTypes2(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalNestedTypeTests", 739, 2, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testEvalNestedTypeTest_f() throws Throwable {
		try {
		init();
		IValue value = eval(fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("f : wrong result : ", fIntValue_0, intValue);

		value = eval(fString);
		typeName = value.getReferenceTypeName();
		assertEquals("f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("f : wrong result : ", fStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_b() throws Throwable {
		try {
		init();
		IValue value = eval(T_T + bInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_b : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_b : wrong result : ", bIntValue_0, intValue);

		value = eval(T_T + bString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_b : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_b : wrong result : ", bStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_A_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_d : wrong result : ", dIntValue_1, intValue);

		value = eval(T_T_A + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_d : wrong result : ", dStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_A + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_d : wrong result : ", dIntValue_1, intValue);

		value = eval(T_A + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_d : wrong result : ", dStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_A_AA_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AA + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AA_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_T_A_AA + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AA_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_A_AB_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AB + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AB_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_T_A_AB + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AB_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_AA_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AA + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AA_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_A_AA + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AA_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_AB_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AB + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AB_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_A_AB + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AB_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_B_h() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_B + hInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_h : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_B_h : wrong result : ", hIntValue_1, intValue);

		value = eval(T_T_B + hString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_h : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_B_h : wrong result : ", hStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_B_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_B + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_B_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_B_d : wrong result : ", dIntValue_1, intValue);

		value = eval(T_B + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_B_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_B_d : wrong result : ", dStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_B_BB_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_B_BB + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_B_BB_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_T_B_BB + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_B_BB_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_B_BB_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_B_BB + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_B_BB_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_B_BB + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_B_BB_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_A_h() throws Throwable {
		try {
		init();
		IValue value = eval(I_A + hInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_A_h : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_A_h : wrong result : ", hIntValue_1, intValue);

		value = eval(I_A + hString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_A_h : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_A_h : wrong result : ", hStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AA_c() throws Throwable {
		try {
		init();
		IValue value = eval(I_AA + cInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AA_c : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AA_c : wrong result : ", cIntValue_2, intValue);

		value = eval(I_AA + cString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AA_c : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AA_c : wrong result : ", cStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AA_f() throws Throwable {
		try {
		init();
		IValue value = eval(I_AA + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AA_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AA_f : wrong result : ", fIntValue_2, intValue);

		value = eval(I_AA + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AA_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AA_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AA_j() throws Throwable {
		try {
		init();
		IValue value = eval(I_AA + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AA_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AA_j : wrong result : ", jIntValue_2, intValue);

		value = eval(I_AA + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AA_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AA_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AB_c() throws Throwable {
		try {
		init();
		IValue value = eval(I_AB + cInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AB_c : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AB_c : wrong result : ", cIntValue_2, intValue);

		value = eval(I_AB + cString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AB_c : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AB_c : wrong result : ", cStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AB_f() throws Throwable {
		try {
		init();
		IValue value = eval(I_AB + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AB_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AB_f : wrong result : ", fIntValue_2, intValue);

		value = eval(I_AB + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AB_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AB_f : wrong result : ", fStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_I_AB_i() throws Throwable {
		try {
		init();
		IValue value = eval(I_AB + iInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("I_AB_i : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("I_AB_i : wrong result : ", iIntValue_2, intValue);

		value = eval(I_AB + iString);
		typeName = value.getReferenceTypeName();
		assertEquals("I_AB_i : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("I_AB_i : wrong result : ", iStringValue_2, stringValue);
		} finally {
		end();
		}
	}

}
