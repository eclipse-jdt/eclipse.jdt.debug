/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;

public class VariableDeclarationTests extends Tests {

	public VariableDeclarationTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 18, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// int
	public void testInt() throws Throwable {
		try {
			init();
			IValue value = eval("int i= 3; return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 3, intValue);
		} finally {
			end();
		}
	}

	// java.lang.String
	public void testString() throws Throwable {
		try {
			init();
			IValue value = eval("String i= \"test\"; return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("java.lang.String : wrong type : ", "java.lang.String", typeName);
			String stringValue = ((JDIObjectValue)value).getValueString();
			assertEquals("java.lang.String : wrong result : ", "test", stringValue);
		} finally {
			end();
		}
	}

	// java.util.ArrayList
	public void testArrayList() throws Throwable {
		try {
			init();
			IValue value = eval("java.util.ArrayList i= new java.util.ArrayList(); return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("java.util.ArrayList : wrong type : ", "java.util.ArrayList", Signature.getTypeErasure(typeName));
		} finally {
			end();
		}
	}

	// int[]
	public void testIntTab() throws Throwable {
		try {
			init();
			IValue value = eval("int[] i= new int[] {3,2}; return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int[] : wrong type : ", "int[]", typeName);
			IValue cellValue = ((JDIArrayValue)value).getValue(0);
			typeName = cellValue.getReferenceTypeName();
			assertEquals("int[] : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)cellValue).getIntValue();
			assertEquals("int[] : wrong result : ", 3, intValue);
		} finally {
			end();
		}
	}

	// java.lang.String[]
	public void testStringTab() throws Throwable {
		try {
			init();
			IValue value = eval("String[] i= new String[] {\"test1\",\"test2\"}; return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("java.lang.String[] : wrong type : ", "java.lang.String[]", typeName);
			IValue cellValue = ((JDIArrayValue)value).getValue(0);
			typeName = cellValue.getReferenceTypeName();
			assertEquals("java.lang.String[] : wrong type : ", "java.lang.String", typeName);
			String stringValue = ((JDIObjectValue)cellValue).getValueString();
			assertEquals("java.lang.String[] : wrong result : ", "test1", stringValue);
		} finally {
			end();
		}
	}

	// java.util.ArrayList[]
	public void testArrayListTab() throws Throwable {
		try {
			init();
			IValue value = eval("java.util.ArrayList[] i= new java.util.ArrayList[] {new java.util.ArrayList(), new java.util.ArrayList()}; return i;");
			String typeName = value.getReferenceTypeName();
			assertEquals("java.util.ArrayList[] : wrong type : ", "java.util.ArrayList[]", Signature.getTypeErasure(typeName));
			IValue cellValue = ((JDIArrayValue)value).getValue(0);
			typeName = cellValue.getReferenceTypeName();
			assertEquals("java.util.ArrayList[] : wrong type : ", "java.util.ArrayList", Signature.getTypeErasure(typeName));
		} finally {
			end();
		}
	}

}
