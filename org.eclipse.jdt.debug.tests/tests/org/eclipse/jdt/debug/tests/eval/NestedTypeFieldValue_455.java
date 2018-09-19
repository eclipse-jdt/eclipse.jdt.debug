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

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class NestedTypeFieldValue_455 extends Tests {
	/**
	 * Constructor for NestedTypeFieldValue.
	 * @param name
	 */
	public NestedTypeFieldValue_455(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalNestedTypeTests", 458, 4);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testEvalNestedTypeTest_a() throws Throwable {
		try {
		init();
		IValue value = eval(aInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("a : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("a : wrong result : ", aIntValue_2, intValue);

		value = eval(aString);
		typeName = value.getReferenceTypeName();
		assertEquals("a : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("a : wrong result : ", aStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_b() throws Throwable {
		try {
		init();
		IValue value = eval(bInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("b : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("b : wrong result : ", bIntValue_2, intValue);

		value = eval(bString);
		typeName = value.getReferenceTypeName();
		assertEquals("b : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("b : wrong result : ", bStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_T_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_d : wrong result : ", dIntValue_0, intValue);

		value = eval(T_T + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_d : wrong result : ", dStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_T + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_f : wrong result : ", fIntValue_0, intValue);

		value = eval(T_T + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_f : wrong result : ", fStringValue_0, stringValue);
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

	public void testEvalNestedTypeTest_T_T_A_h() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A + hInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_h : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_h : wrong result : ", hIntValue_1, intValue);

		value = eval(T_T_A + hString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_h : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_h : wrong result : ", hStringValue_1, stringValue);
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

	public void testEvalNestedTypeTest_T_A_h() throws Throwable {
		try {
		init();
		IValue value = eval(T_A + hInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_h : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_h : wrong result : ", hIntValue_1, intValue);

		value = eval(T_A + hString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_h : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_h : wrong result : ", hStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_A_AA_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AA + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AA_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_T_A_AA + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AA_d : wrong result : ", dStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_T_A_AA_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AA + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AA_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_T_A_AA + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AA_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AA_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_A_AB_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AB + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AB_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_T_A_AB + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AB_d : wrong result : ", dStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_T_A_AB_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_A_AB + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_A_AB_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_T_A_AB + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_A_AB_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_A_AB_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_AA_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AA + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AA_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_A_AA + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AA_d : wrong result : ", dStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_AA_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AA + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AA_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_A_AA + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AA_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AA_f : wrong result : ", fStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_A_AB_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AB + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AB_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_A_AB + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AB_d : wrong result : ", dStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_A_AB_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_A_AB + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_A_AB_f : wrong result : ", fIntValue_2, intValue);

		value = eval(T_A_AB + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_A_AB_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_A_AB_f : wrong result : ", fStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_T_B_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_B + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_B_d : wrong result : ", dIntValue_1, intValue);

		value = eval(T_T_B + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_B_d : wrong result : ", dStringValue_1, stringValue);
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

	public void testEvalNestedTypeTest_T_B_h() throws Throwable {
		try {
		init();
		IValue value = eval(T_B + hInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_B_h : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_B_h : wrong result : ", hIntValue_1, intValue);

		value = eval(T_B + hString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_B_h : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_B_h : wrong result : ", hStringValue_1, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_B_BB_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_B_BB + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_B_BB_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_T_B_BB + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_B_BB_d : wrong result : ", dStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_T_B_BB_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_B_BB + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_B_BB_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_T_B_BB + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_B_BB_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_B_BB_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_B_BB_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_B_BB + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_B_BB_d : wrong result : ", dIntValue_2, intValue);

		value = eval(T_B_BB + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_B_BB_d : wrong result : ", dStringValue_2, stringValue);
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

	public void testEvalNestedTypeTest_T_B_BB_j() throws Throwable {
		try {
		init();
		IValue value = eval(T_B_BB + jInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_j : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_B_BB_j : wrong result : ", jIntValue_2, intValue);

		value = eval(T_B_BB + jString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_B_BB_j : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_B_BB_j : wrong result : ", jStringValue_2, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_a() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + aInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_a : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_a : wrong result : ", aIntValue_0, intValue);

		value = eval(T_T_this + aString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_a : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_a : wrong result : ", aStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_b() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + bInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_b : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_b : wrong result : ", bIntValue_0, intValue);

		value = eval(T_T_this + bString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_b : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_b : wrong result : ", bStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_c() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + cInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_c : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_c : wrong result : ", cIntValue_0, intValue);

		value = eval(T_T_this + cString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_c : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_c : wrong result : ", cStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_d() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + dInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_d : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_d : wrong result : ", dIntValue_0, intValue);

		value = eval(T_T_this + dString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_d : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_d : wrong result : ", dStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_e() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + eInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_e : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_e : wrong result : ", eIntValue_0, intValue);

		value = eval(T_T_this + eString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_e : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_e : wrong result : ", eStringValue_0, stringValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_T_T_this_f() throws Throwable {
		try {
		init();
		IValue value = eval(T_T_this + fInt);
		String typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_f : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("T_T_this_f : wrong result : ", fIntValue_0, intValue);

		value = eval(T_T_this + fString);
		typeName = value.getReferenceTypeName();
		assertEquals("T_T_this_f : wrong type : ", "java.lang.String", typeName);
		String stringValue = ((JDIObjectValue)value).getValueString();
		assertEquals("T_T_this_f : wrong result : ", fStringValue_0, stringValue);
		} finally {
		end();
		}
	}

}
