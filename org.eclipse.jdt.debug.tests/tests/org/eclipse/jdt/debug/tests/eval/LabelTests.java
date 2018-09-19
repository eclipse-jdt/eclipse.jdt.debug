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

public class LabelTests extends Tests {

	public LabelTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests",37,1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// if break
	public void testIfBreak() throws Throwable {
		try {
			init();
			IValue value = eval("a: if (1 == 1) { if (1 == 1) { break a; } return 1; } return 2;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 2, intValue);
		} finally {
			end();
		}
	}

	// do while break
	public void testDoWhileBreak() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; a:do {xVarInt++; for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {break a;} yVarInt++;}} while (xVarInt < 5); return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 4, intValue);
		} finally {
			end();
		}
	}

	// do while continue
	public void testDoWhileContinue() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; a:do {xVarInt++; for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {continue a;} yVarInt++;}} while (xVarInt < 5); return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 8, intValue);
		} finally {
			end();
		}
	}

	// while break
	public void testWhileBreak() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; a:while (xVarInt < 5) {xVarInt++; for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {break a;} yVarInt++;}} return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 4, intValue);
		} finally {
			end();
		}
	}

	// while continue
	public void testWhileContinue() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; a:while (xVarInt < 5) {xVarInt++; for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {continue a;} yVarInt++;}} return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 8, intValue);
		} finally {
			end();
		}
	}

	// for break
	public void testForBreak() throws Throwable {
		try {
			init();
			IValue value = eval("a:for (xVarInt= 0, yVarInt=0; xVarInt < 5;xVarInt++) {for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {break a;} yVarInt++;}} return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 6, intValue);
		} finally {
			end();
		}
	}

	// for continue
	public void testForContinue() throws Throwable {
		try {
			init();
			IValue value = eval("a:for (xVarInt= 0, yVarInt=0; xVarInt < 5;xVarInt++) {for (xVarLong= 0; xVarLong < 2; xVarLong++){if (xVarInt == 3) {continue a;} yVarInt++;}} return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 8, intValue);
		} finally {
			end();
		}
	}

}
