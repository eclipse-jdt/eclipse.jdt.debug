package org.eclipse.jdt.debug.tests.eval;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;

public class TypeHierarchy_57_3 extends Tests {
	/**
	 * Constructor for TypeHierarchy.
	 * @param name
	 */
	public TypeHierarchy_57_3(String name) {
		super(name);
	}

	public void init() throws Exception {
		initializeFrame("EvalTypeHierarchyTests", 68, 2, 3);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	public void testEvalNestedTypeTest_m1() throws Throwable {
		try {
		init();
		IValue value = eval("m1()");
		String typeName = value.getReferenceTypeName();
		assertEquals("m1 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("m1 : wrong result : ", 111, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testEvalNestedTypeTest_m2() throws Throwable {
		try {
		init();
		IValue value = eval("m2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("m2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("m2 : wrong result : ", 222, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testEvalNestedTypeTest_s2() throws Throwable {
		try {
		init();
		IValue value = eval("s2()");
		String typeName = value.getReferenceTypeName();
		assertEquals("s2 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("s2 : wrong result : ", 99, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testEvalNestedTypeTest_m3() throws Throwable {
		try {
		init();
		IValue value = eval("m3()");
		String typeName = value.getReferenceTypeName();
		assertEquals("m3 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("m3 : wrong result : ", 333, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testEvalNestedTypeTest_m4() throws Throwable {
		try {
		init();
		IValue value = eval("m4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("m4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("m4 : wrong result : ", 444, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

	public void testEvalNestedTypeTest_s4() throws Throwable {
		try {
		init();
		IValue value = eval("s4()");
		String typeName = value.getReferenceTypeName();
		assertEquals("s4 : wrong type : ", "int", typeName);
		int intValue = ((IJavaPrimitiveValue)value).getIntValue();
		assertEquals("s4 : wrong result : ", 88, intValue);
		} catch (Throwable e) {
		e.printStackTrace(); throw e;
		} finally {;
		end();
		}
	}

}
