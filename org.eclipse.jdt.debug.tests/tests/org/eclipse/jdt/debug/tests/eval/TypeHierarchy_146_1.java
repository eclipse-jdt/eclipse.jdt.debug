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

public class TypeHierarchy_146_1 extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TypeHierarchy_146_1(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalTypeHierarchyTests", 146, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testEvalNestedTypeTest_iaa_m1() throws Throwable {
		try {
		init();
		IValue value = eval("iaa.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("iaa.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("iaa.m1 : wrong result : ", 1, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_iab_m1() throws Throwable {
		try {
		init();
		IValue value = eval("iab.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("iab.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("iab.m1 : wrong result : ", 11, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_iac_m1() throws Throwable {
		try {
		init();
		IValue value = eval("iac.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("iac.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("iac.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_aa_m1() throws Throwable {
		try {
		init();
		IValue value = eval("aa.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("aa.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("aa.m1 : wrong result : ", 1, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_aa_m2() throws Throwable {
		try {
		init();
		IValue value = eval("aa.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("aa.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("aa.m2 : wrong result : ", 2, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_aa_s2() throws Throwable {
		try {
		init();
		IValue value = eval("aa.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("aa.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("aa.s2 : wrong result : ", 9, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ab_m1() throws Throwable {
		try {
		init();
		IValue value = eval("ab.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ab.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ab.m1 : wrong result : ", 11, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ab_m2() throws Throwable {
		try {
		init();
		IValue value = eval("ab.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ab.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ab.m2 : wrong result : ", 22, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ab_s2() throws Throwable {
		try {
		init();
		IValue value = eval("ab.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ab.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ab.s2 : wrong result : ", 9, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ac_m1() throws Throwable {
		try {
		init();
		IValue value = eval("ac.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ac.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ac.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ac_m2() throws Throwable {
		try {
		init();
		IValue value = eval("ac.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ac.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ac.m2 : wrong result : ", 222, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ac_s2() throws Throwable {
		try {
		init();
		IValue value = eval("ac.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ac.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ac.s2 : wrong result : ", 9, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ibb_m1() throws Throwable {
		try {
		init();
		IValue value = eval("ibb.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ibb.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ibb.m1 : wrong result : ", 11, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ibb_m3() throws Throwable {
		try {
		init();
		IValue value = eval("ibb.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ibb.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ibb.m3 : wrong result : ", 33, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ibc_m1() throws Throwable {
		try {
		init();
		IValue value = eval("ibc.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ibc.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ibc.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_ibc_m3() throws Throwable {
		try {
		init();
		IValue value = eval("ibc.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("ibc.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("ibc.m3 : wrong result : ", 333, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_m1() throws Throwable {
		try {
		init();
		IValue value = eval("bb.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.m1 : wrong result : ", 11, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_m2() throws Throwable {
		try {
		init();
		IValue value = eval("bb.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.m2 : wrong result : ", 22, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_s2() throws Throwable {
		try {
		init();
		IValue value = eval("bb.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.s2 : wrong result : ", 99, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_m3() throws Throwable {
		try {
		init();
		IValue value = eval("bb.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.m3 : wrong result : ", 33, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_m4() throws Throwable {
		try {
		init();
		IValue value = eval("bb.m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.m4 : wrong result : ", 44, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bb_s4() throws Throwable {
		try {
		init();
		IValue value = eval("bb.s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bb.s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bb.s4 : wrong result : ", 88, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_m1() throws Throwable {
		try {
		init();
		IValue value = eval("bc.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_m2() throws Throwable {
		try {
		init();
		IValue value = eval("bc.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.m2 : wrong result : ", 222, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_s2() throws Throwable {
		try {
		init();
		IValue value = eval("bc.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.s2 : wrong result : ", 99, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_m3() throws Throwable {
		try {
		init();
		IValue value = eval("bc.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.m3 : wrong result : ", 333, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_m4() throws Throwable {
		try {
		init();
		IValue value = eval("bc.m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.m4 : wrong result : ", 444, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_bc_s4() throws Throwable {
		try {
		init();
		IValue value = eval("bc.s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("bc.s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("bc.s4 : wrong result : ", 88, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_icc_m1() throws Throwable {
		try {
		init();
		IValue value = eval("icc.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("icc.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("icc.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_icc_m3() throws Throwable {
		try {
		init();
		IValue value = eval("icc.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("icc.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("icc.m3 : wrong result : ", 333, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_icc_m5() throws Throwable {
		try {
		init();
		IValue value = eval("icc.m5()");
		String typeName = value.getReferenceTypeName();
		assertEquals("icc.m5 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("icc.m5 : wrong result : ", 555, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m1() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m2() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m2 : wrong result : ", 222, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_s2() throws Throwable {
		try {
		init();
		IValue value = eval("cc.s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.s2 : wrong result : ", 999, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m3() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m3 : wrong result : ", 333, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m4() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m4 : wrong result : ", 444, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_s4() throws Throwable {
		try {
		init();
		IValue value = eval("cc.s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.s4 : wrong result : ", 888, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m5() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m5()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m5 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m5 : wrong result : ", 555, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_m6() throws Throwable {
		try {
		init();
		IValue value = eval("cc.m6()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.m6 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.m6 : wrong result : ", 666, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_cc_s6() throws Throwable {
		try {
		init();
		IValue value = eval("cc.s6()");
		String typeName = value.getReferenceTypeName();
		assertEquals("cc.s6 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("cc.s6 : wrong result : ", 777, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_A___m1() throws Throwable {
		try {
		init();
		IValue value = eval("new A().m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new A().m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new A().m1 : wrong result : ", 1, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_A___m2() throws Throwable {
		try {
		init();
		IValue value = eval("new A().m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new A().m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new A().m2 : wrong result : ", 2, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_A___s2() throws Throwable {
		try {
		init();
		IValue value = eval("new A().s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new A().s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new A().s2 : wrong result : ", 9, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___m1() throws Throwable {
		try {
		init();
		IValue value = eval("new B().m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().m1 : wrong result : ", 11, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___m2() throws Throwable {
		try {
		init();
		IValue value = eval("new B().m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().m2 : wrong result : ", 22, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___s2() throws Throwable {
		try {
		init();
		IValue value = eval("new B().s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().s2 : wrong result : ", 99, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___m3() throws Throwable {
		try {
		init();
		IValue value = eval("new B().m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().m3 : wrong result : ", 33, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___m4() throws Throwable {
		try {
		init();
		IValue value = eval("new B().m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().m4 : wrong result : ", 44, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_B___s4() throws Throwable {
		try {
		init();
		IValue value = eval("new B().s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new B().s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new B().s4 : wrong result : ", 88, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m1() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m1 : wrong result : ", 111, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m2() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m2 : wrong result : ", 222, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___s2() throws Throwable {
		try {
		init();
		IValue value = eval("new C().s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().s2 : wrong result : ", 999, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m3() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m3 : wrong result : ", 333, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m4() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m4 : wrong result : ", 444, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___s4() throws Throwable {
		try {
		init();
		IValue value = eval("new C().s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().s4 : wrong result : ", 888, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m5() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m5()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m5 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m5 : wrong result : ", 555, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___m6() throws Throwable {
		try {
		init();
		IValue value = eval("new C().m6()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().m6 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().m6 : wrong result : ", 666, intValue);
		} finally {
		end();
		}
	}

	public void testEvalNestedTypeTest_new_C___s6() throws Throwable {
		try {
		init();
		IValue value = eval("new C().s6()");
		String typeName = value.getReferenceTypeName();
		assertEquals("new C().s6 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("new C().s6 : wrong result : ", 777, intValue);
		} finally {
		end();
		}
	}

}
