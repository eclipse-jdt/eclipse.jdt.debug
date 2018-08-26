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

public class LoopTests extends Tests {

	public LoopTests(String arg) {
		super(arg);
	}

	protected void init() throws Exception {
		initializeFrame("EvalSimpleTests", 40, 1);
	}

	protected void end() throws Exception {
		destroyFrame();
	}

	// while
	public void testWhile() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; while (xVarInt < 5) {xVarInt++; yVarInt++;} return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 5, intValue);
		} finally {
			end();
		}
	}

	// do while
	public void testDoWhile() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; do {xVarInt++; yVarInt++;} while (xVarInt < 5); return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 5, intValue);
		} finally {
			end();
		}
	}

	// for
	public void testFor() throws Throwable {
		try {
			init();
			IValue value = eval("for (xVarInt= 0, yVarInt=0; xVarInt < 5; xVarInt++) {yVarInt++;}  return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 5, intValue);
		} finally {
			end();
		}
	}

	// while break
	public void testWhileBreak() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; while (xVarInt < 5) {xVarInt++; if (xVarInt == 3) {break;} yVarInt++;} return yVarInt;");
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
			IValue value = eval("xVarInt= 0; yVarInt=0; do {xVarInt++; if (xVarInt == 3) {break;} yVarInt++;} while (xVarInt < 5); return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 2, intValue);
		} finally {
			end();
		}
	}

	// for break
	public void testForBreak() throws Throwable {
		try {
			init();
			IValue value = eval("for (xVarInt= 0, yVarInt=0; xVarInt < 5; xVarInt++) {if (xVarInt == 3) {break;} yVarInt++;}  return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 3, intValue);
		} finally {
			end();
		}
	}

	// while continue
	public void testWhileContinue() throws Throwable {
		try {
			init();
			IValue value = eval("xVarInt= 0; yVarInt=0; while (xVarInt < 5) {xVarInt++; if (xVarInt == 3) {continue;} yVarInt++;} return yVarInt;");
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
			IValue value = eval("xVarInt= 0; yVarInt=0; do {xVarInt++; if (xVarInt == 3) {continue;} yVarInt++;} while (xVarInt < 5); return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 4, intValue);
		} finally {
			end();
		}
	}

	// for continue
	public void testForContinue() throws Throwable {
		try {
			init();
			IValue value = eval("for (xVarInt= 0, yVarInt=0; xVarInt < 5; xVarInt++) {if (xVarInt == 3) {continue;} yVarInt++;}  return yVarInt;");
			String typeName = value.getReferenceTypeName();
			assertEquals("int : wrong type : ", "int", typeName);
			int intValue = ((IJavaPrimitiveValue)value).getIntValue();
			assertEquals("int : wrong result : ", 4, intValue);
		} finally {
			end();
		}
	}
	}
