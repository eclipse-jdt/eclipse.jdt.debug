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
public class TestsNumberLiteral extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TestsNumberLiteral(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalSimpleTests", 40, 1, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void test0() throws Throwable {
		try {
		init();
		IValue value = eval("0");
		String typeName = value.getReferenceTypeName();
		assertEquals("0 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0 : wrong result : ", 0, intValue);
		} finally {
		end();
		}
	}

	public void test00() throws Throwable {
		try {
		init();
		IValue value = eval("00");
		String typeName = value.getReferenceTypeName();
		assertEquals("00 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("00 : wrong result : ", 00, intValue);
		} finally {
		end();
		}
	}

	public void test0x0() throws Throwable {
		try {
		init();
		IValue value = eval("0x0");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x0 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0x0 : wrong result : ", 0x0, intValue);
		} finally {
		end();
		}
	}

	public void testN1() throws Throwable {
		try {
		init();
		IValue value = eval("-1");
		String typeName = value.getReferenceTypeName();
		assertEquals("-1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("-1 : wrong result : ", -1, intValue);
		} finally {
		end();
		}
	}

	public void test1() throws Throwable {
		try {
		init();
		IValue value = eval("1");
		String typeName = value.getReferenceTypeName();
		assertEquals("1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("1 : wrong result : ", 1, intValue);
		} finally {
		end();
		}
	}

	public void test2147483647() throws Throwable {
		try {
		init();
		IValue value = eval("2147483647");
		String typeName = value.getReferenceTypeName();
		assertEquals("2147483647 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("2147483647 : wrong result : ", 2147483647, intValue);
		} finally {
		end();
		}
	}

	public void testN2147483648() throws Throwable {
		try {
		init();
		IValue value = eval("-2147483648");
		String typeName = value.getReferenceTypeName();
		assertEquals("-2147483648 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("-2147483648 : wrong result : ", -2147483648, intValue);
		} finally {
		end();
		}
	}

	public void test0x7fffffff() throws Throwable {
		try {
		init();
		IValue value = eval("0x7fffffff");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x7fffffff : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0x7fffffff : wrong result : ", 0x7fffffff, intValue);
		} finally {
		end();
		}
	}

	public void test0x80000000() throws Throwable {
		try {
		init();
		IValue value = eval("0x80000000");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x80000000 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0x80000000 : wrong result : ", 0x80000000, intValue);
		} finally {
		end();
		}
	}

	public void test0xffffffff() throws Throwable {
		try {
		init();
		IValue value = eval("0xffffffff");
		String typeName = value.getReferenceTypeName();
		assertEquals("0xffffffff : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0xffffffff : wrong result : ", 0xffffffff, intValue);
		} finally {
		end();
		}
	}

	public void test017777777777() throws Throwable {
		try {
		init();
		IValue value = eval("017777777777");
		String typeName = value.getReferenceTypeName();
		assertEquals("017777777777 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("017777777777 : wrong result : ", 017777777777, intValue);
		} finally {
		end();
		}
	}

	public void test020000000000() throws Throwable {
		try {
		init();
		IValue value = eval("020000000000");
		String typeName = value.getReferenceTypeName();
		assertEquals("020000000000 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("020000000000 : wrong result : ", 020000000000, intValue);
		} finally {
		end();
		}
	}

	public void test037777777777() throws Throwable {
		try {
		init();
		IValue value = eval("037777777777");
		String typeName = value.getReferenceTypeName();
		assertEquals("037777777777 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("037777777777 : wrong result : ", 037777777777, intValue);
		} finally {
		end();
		}
	}

	public void test2() throws Throwable {
		try {
		init();
		IValue value = eval("2");
		String typeName = value.getReferenceTypeName();
		assertEquals("2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("2 : wrong result : ", 2, intValue);
		} finally {
		end();
		}
	}

	public void test0372() throws Throwable {
		try {
		init();
		IValue value = eval("0372");
		String typeName = value.getReferenceTypeName();
		assertEquals("0372 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0372 : wrong result : ", 0372, intValue);
		} finally {
		end();
		}
	}

	public void test0xDadaCafe() throws Throwable {
		try {
		init();
		IValue value = eval("0xDadaCafe");
		String typeName = value.getReferenceTypeName();
		assertEquals("0xDadaCafe : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0xDadaCafe : wrong result : ", 0xDadaCafe, intValue);
		} finally {
		end();
		}
	}

	public void test1996() throws Throwable {
		try {
		init();
		IValue value = eval("1996");
		String typeName = value.getReferenceTypeName();
		assertEquals("1996 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("1996 : wrong result : ", 1996, intValue);
		} finally {
		end();
		}
	}

	public void test0x00FF00FF() throws Throwable {
		try {
		init();
		IValue value = eval("0x00FF00FF");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x00FF00FF : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("0x00FF00FF : wrong result : ", 0x00FF00FF, intValue);
		} finally {
		end();
		}
	}

	public void test0L() throws Throwable {
		try {
		init();
		IValue value = eval("0L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0L : wrong result : ", 0L, longValue);
		} finally {
		end();
		}
	}

	public void test00L() throws Throwable {
		try {
		init();
		IValue value = eval("00L");
		String typeName = value.getReferenceTypeName();
		assertEquals("00L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("00L : wrong result : ", 00L, longValue);
		} finally {
		end();
		}
	}

	public void test0x0L() throws Throwable {
		try {
		init();
		IValue value = eval("0x0L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x0L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0x0L : wrong result : ", 0x0L, longValue);
		} finally {
		end();
		}
	}

	public void testN1L() throws Throwable {
		try {
		init();
		IValue value = eval("-1L");
		String typeName = value.getReferenceTypeName();
		assertEquals("-1L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("-1L : wrong result : ", -1L, longValue);
		} finally {
		end();
		}
	}

	public void test1L() throws Throwable {
		try {
		init();
		IValue value = eval("1L");
		String typeName = value.getReferenceTypeName();
		assertEquals("1L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("1L : wrong result : ", 1L, longValue);
		} finally {
		end();
		}
	}

	public void test9223372036854775807L() throws Throwable {
		try {
		init();
		IValue value = eval("9223372036854775807L");
		String typeName = value.getReferenceTypeName();
		assertEquals("9223372036854775807L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("9223372036854775807L : wrong result : ", 9223372036854775807L, longValue);
		} finally {
		end();
		}
	}

	public void testN9223372036854775808L() throws Throwable {
		try {
		init();
		IValue value = eval("-9223372036854775808L");
		String typeName = value.getReferenceTypeName();
		assertEquals("-9223372036854775808L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("-9223372036854775808L : wrong result : ", -9223372036854775808L, longValue);
		} finally {
		end();
		}
	}

	public void test0x7fffffffffffffffL() throws Throwable {
		try {
		init();
		IValue value = eval("0x7fffffffffffffffL");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x7fffffffffffffffL : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0x7fffffffffffffffL : wrong result : ", 0x7fffffffffffffffL, longValue);
		} finally {
		end();
		}
	}

	public void test0x8000000000000000L() throws Throwable {
		try {
		init();
		IValue value = eval("0x8000000000000000L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x8000000000000000L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0x8000000000000000L : wrong result : ", 0x8000000000000000L, longValue);
		} finally {
		end();
		}
	}

	public void test0xffffffffffffffffL() throws Throwable {
		try {
		init();
		IValue value = eval("0xffffffffffffffffL");
		String typeName = value.getReferenceTypeName();
		assertEquals("0xffffffffffffffffL : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0xffffffffffffffffL : wrong result : ", 0xffffffffffffffffL, longValue);
		} finally {
		end();
		}
	}

	public void test0777777777777777777777L() throws Throwable {
		try {
		init();
		IValue value = eval("0777777777777777777777L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0777777777777777777777L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0777777777777777777777L : wrong result : ", 0777777777777777777777L, longValue);
		} finally {
		end();
		}
	}

	public void test01000000000000000000000L() throws Throwable {
		try {
		init();
		IValue value = eval("01000000000000000000000L");
		String typeName = value.getReferenceTypeName();
		assertEquals("01000000000000000000000L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("01000000000000000000000L : wrong result : ", 01000000000000000000000L, longValue);
		} finally {
		end();
		}
	}

	public void test01777777777777777777777L() throws Throwable {
		try {
		init();
		IValue value = eval("01777777777777777777777L");
		String typeName = value.getReferenceTypeName();
		assertEquals("01777777777777777777777L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("01777777777777777777777L : wrong result : ", 01777777777777777777777L, longValue);
		} finally {
		end();
		}
	}

	public void test0777l() throws Throwable {
		try {
		init();
		IValue value = eval("0777l");
		String typeName = value.getReferenceTypeName();
		assertEquals("0777l : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0777l : wrong result : ", 0777l, longValue);
		} finally {
		end();
		}
	}

	public void test0x100000000L() throws Throwable {
		try {
		init();
		IValue value = eval("0x100000000L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0x100000000L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0x100000000L : wrong result : ", 0x100000000L, longValue);
		} finally {
		end();
		}
	}

	public void test2147483648L() throws Throwable {
		try {
		init();
		IValue value = eval("2147483648L");
		String typeName = value.getReferenceTypeName();
		assertEquals("2147483648L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("2147483648L : wrong result : ", 2147483648L, longValue);
		} finally {
		end();
		}
	}

	public void test0xC0B0L() throws Throwable {
		try {
		init();
		IValue value = eval("0xC0B0L");
		String typeName = value.getReferenceTypeName();
		assertEquals("0xC0B0L : wrong type : ", "long", typeName);
		long longValue = ((IJavaPrimitiveValue)value).getLongValue();
		assertEquals("0xC0B0L : wrong result : ", 0xC0B0L, longValue);
		} finally {
		end();
		}
	}

	public void test3_40282347eP38f() throws Throwable {
		try {
		init();
		IValue value = eval("3.40282347e+38f");
		String typeName = value.getReferenceTypeName();
		assertEquals("3.40282347e+38f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("3.40282347e+38f : wrong result : ", 3.40282347e+38f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test1_40239846eN45f() throws Throwable {
		try {
		init();
		IValue value = eval("1.40239846e-45f");
		String typeName = value.getReferenceTypeName();
		assertEquals("1.40239846e-45f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("1.40239846e-45f : wrong result : ", 1.40239846e-45f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test1e1f() throws Throwable {
		try {
		init();
		IValue value = eval("1e1f");
		String typeName = value.getReferenceTypeName();
		assertEquals("1e1f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("1e1f : wrong result : ", 1e1f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test2_f() throws Throwable {
		try {
		init();
		IValue value = eval("2.f");
		String typeName = value.getReferenceTypeName();
		assertEquals("2.f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("2.f : wrong result : ", 2.f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test_3f() throws Throwable {
		try {
		init();
		IValue value = eval(".3f");
		String typeName = value.getReferenceTypeName();
		assertEquals(".3f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals(".3f : wrong result : ", .3f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test0f() throws Throwable {
		try {
		init();
		IValue value = eval("0f");
		String typeName = value.getReferenceTypeName();
		assertEquals("0f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("0f : wrong result : ", 0f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test3_14f() throws Throwable {
		try {
		init();
		IValue value = eval("3.14f");
		String typeName = value.getReferenceTypeName();
		assertEquals("3.14f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("3.14f : wrong result : ", 3.14f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test6_022137eP23f() throws Throwable {
		try {
		init();
		IValue value = eval("6.022137e+23f");
		String typeName = value.getReferenceTypeName();
		assertEquals("6.022137e+23f : wrong type : ", "float", typeName);
		float floatValue = ((IJavaPrimitiveValue)value).getFloatValue();
		assertEquals("6.022137e+23f : wrong result : ", 6.022137e+23f, floatValue, 0);
		} finally {
		end();
		}
	}

	public void test1_79769313486231570eP308() throws Throwable {
		try {
		init();
		IValue value = eval("1.79769313486231570e+308");
		String typeName = value.getReferenceTypeName();
		assertEquals("1.79769313486231570e+308 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("1.79769313486231570e+308 : wrong result : ", 1.79769313486231570e+308, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test4_94065645841246544eN324() throws Throwable {
		try {
		init();
		IValue value = eval("4.94065645841246544e-324");
		String typeName = value.getReferenceTypeName();
		assertEquals("4.94065645841246544e-324 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("4.94065645841246544e-324 : wrong result : ", 4.94065645841246544e-324, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test1e1() throws Throwable {
		try {
		init();
		IValue value = eval("1e1");
		String typeName = value.getReferenceTypeName();
		assertEquals("1e1 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("1e1 : wrong result : ", 1e1, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test2_() throws Throwable {
		try {
		init();
		IValue value = eval("2.");
		String typeName = value.getReferenceTypeName();
		assertEquals("2. : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("2. : wrong result : ", 2., doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test_3() throws Throwable {
		try {
		init();
		IValue value = eval(".3");
		String typeName = value.getReferenceTypeName();
		assertEquals(".3 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals(".3 : wrong result : ", .3, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test0_0() throws Throwable {
		try {
		init();
		IValue value = eval("0.0");
		String typeName = value.getReferenceTypeName();
		assertEquals("0.0 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("0.0 : wrong result : ", 0.0, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test3_14() throws Throwable {
		try {
		init();
		IValue value = eval("3.14");
		String typeName = value.getReferenceTypeName();
		assertEquals("3.14 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("3.14 : wrong result : ", 3.14, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test1eN9d() throws Throwable {
		try {
		init();
		IValue value = eval("1e-9d");
		String typeName = value.getReferenceTypeName();
		assertEquals("1e-9d : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("1e-9d : wrong result : ", 1e-9d, doubleValue, 0);
		} finally {
		end();
		}
	}

	public void test1e137() throws Throwable {
		try {
		init();
		IValue value = eval("1e137");
		String typeName = value.getReferenceTypeName();
		assertEquals("1e137 : wrong type : ", "double", typeName);
		double doubleValue = ((IJavaPrimitiveValue)value).getDoubleValue();
		assertEquals("1e137 : wrong result : ", 1e137, doubleValue, 0);
		} finally {
		end();
		}
	}

}
