/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

public abstract class Tests extends AbstractDebugTest {

	static final String xByte = "((byte)-3)";
	static final String xChar = "((char)-3)";
	static final String xShort = "((short)-3)";
	static final String xInt = "(-3)";
	static final String xLong = "(-3l)";
	static final String xFloat = "(-3.2f)";
	static final String xDouble = "(-3.2)";
	static final String xString = "\"minus three\"";
	static final String xBoolean = "true";
	static final String xNull = "null";

	static byte xByteValue = (byte)-3;
	static char xCharValue = (char)-3;
	static short xShortValue = (short)-3;
	static int xIntValue = -3;
	static long xLongValue = -3l;
	static final float xFloatValue = -3.2f;
	static final double xDoubleValue = -3.2;
	static final String xStringValue = "minus three";
	static final boolean xBooleanValue = true;
	static final Object xNullValue = null;

	static final String xVarByte = "xVarByte";
	static final String xVarChar = "xVarChar";
	static final String xVarShort = "xVarShort";
	static final String xVarInt = "xVarInt";
	static final String xVarLong = "xVarLong";
	static final String xVarFloat = "xVarFloat";
	static final String xVarDouble = "xVarDouble";
	static final String xVarString = "xVarString";
	static final String xVarBoolean = "xVarBoolean";
//	static final String xVarNull = "xVarNull";

	static final byte xVarByteValue = (byte)-5;
	static final char xVarCharValue = (char)-5;
	static final short xVarShortValue = (short)-5;
	static final int xVarIntValue = -5;
	static final long xVarLongValue = -5;
	static final float xVarFloatValue = (float)-5.3;
	static final double xVarDoubleValue = -5.3;
	static final String xVarStringValue = "minus five";
	static final boolean xVarBooleanValue = true;
//	static final Object xVarNullValue = null;

	static final String yByte = "((byte)8)";
	static final String yChar = "((char)8)";
	static final String yShort = "((short)8)";
	static final String yInt = "8";
	static final String yLong = "8l";
	static final String yFloat = "7.8f";
	static final String yDouble = "7.8";
	static final String yString = "\"eight\"";
	static final String yBoolean = "false";
	static final String yNull = "null";

	static final byte yByteValue = (byte)8;
	static final char yCharValue = (char)8;
	static final short yShortValue = (short)8;
	static final int yIntValue = 8;
	static final long yLongValue = 8;
	static final float yFloatValue = (float)7.8;
	static final double yDoubleValue = 7.8;
	static final String yStringValue = "eight";
	static final boolean yBooleanValue = false;
	static final Object yNullValue = null;

	static final String yVarByte = "yVarByte";
	static final String yVarChar = "yVarChar";
	static final String yVarShort = "yVarShort";
	static final String yVarInt = "yVarInt";
	static final String yVarLong = "yVarLong";
	static final String yVarFloat = "yVarFloat";
	static final String yVarDouble = "yVarDouble";
	static final String yVarString = "yVarString";
	static final String yVarBoolean = "yVarBoolean";
//	static final String yVarNull = "yVarNull";

	static final byte yVarByteValue = (byte)7;
	static final char yVarCharValue = (char)7;
	static final short yVarShortValue = (short)7;
	static final int yVarIntValue = 7;
	static final long yVarLongValue = 7;
	static final float yVarFloatValue = (float)6.9;
	static final double yVarDoubleValue = 6.9;
	static final String yVarStringValue = "seven";
	static final boolean yVarBooleanValue = false;
//	static final Object yVarNullValue = null;

	static final String xFieldByte = "xFieldByte";
	static final String xFieldChar = "xFieldChar";
	static final String xFieldShort = "xFieldShort";
	static final String xFieldInt = "xFieldInt";
	static final String xFieldLong = "xFieldLong";
	static final String xFieldFloat = "xFieldFloat";
	static final String xFieldDouble = "xFieldDouble";
	static final String xFieldString = "xFieldString";
	static final String xFieldBoolean = "xFieldBoolean";

	static final String yFieldByte = "yFieldByte";
	static final String yFieldChar = "yFieldChar";
	static final String yFieldShort = "yFieldShort";
	static final String yFieldInt = "yFieldInt";
	static final String yFieldLong = "yFieldLong";
	static final String yFieldFloat = "yFieldFloat";
	static final String yFieldDouble = "yFieldDouble";
	static final String yFieldString = "yFieldString";
	static final String yFieldBoolean = "yFieldBoolean";

	static final String xStaticFieldByte = "xStaticFieldByte";
	static final String xStaticFieldChar = "xStaticFieldChar";
	static final String xStaticFieldShort = "xStaticFieldShort";
	static final String xStaticFieldInt = "xStaticFieldInt";
	static final String xStaticFieldLong = "xStaticFieldLong";
	static final String xStaticFieldFloat = "xStaticFieldFloat";
	static final String xStaticFieldDouble = "xStaticFieldDouble";
	static final String xStaticFieldString = "xStaticFieldString";
	static final String xStaticFieldBoolean = "xStaticFieldBoolean";

	static final String yStaticFieldByte = "yStaticFieldByte";
	static final String yStaticFieldChar = "yStaticFieldChar";
	static final String yStaticFieldShort = "yStaticFieldShort";
	static final String yStaticFieldInt = "yStaticFieldInt";
	static final String yStaticFieldLong = "yStaticFieldLong";
	static final String yStaticFieldFloat = "yStaticFieldFloat";
	static final String yStaticFieldDouble = "yStaticFieldDouble";
	static final String yStaticFieldString = "yStaticFieldString";
	static final String yStaticFieldBoolean = "yStaticFieldBoolean";

	static final byte xFieldByteValue = -2;
	static final char xFieldCharValue = (char)-2;
	static final short xFieldShortValue = -2;
	static final int xFieldIntValue = -2;
	static final long xFieldLongValue = -2;
	static final float xFieldFloatValue = (float)-2.1;
	static final double xFieldDoubleValue = -2.1;
	static final String xFieldStringValue = "minus two";
	static final boolean xFieldBooleanValue = true;

	static final byte yFieldByteValue = 9;
	static final char yFieldCharValue = 9;
	static final short yFieldShortValue = 9;
	static final int yFieldIntValue = 9;
	static final long yFieldLongValue = 9;
	static final float yFieldFloatValue = (float)8.6;
	static final double yFieldDoubleValue = 8.6;
	static final String yFieldStringValue = "nine";
	static final boolean yFieldBooleanValue = false;

	static final byte xStaticFieldByteValue = -1;
	static final char xStaticFieldCharValue = (char)-1;
	static final short xStaticFieldShortValue = -1;
	static final int xStaticFieldIntValue = -1;
	static final long xStaticFieldLongValue = -1;
	static final float xStaticFieldFloatValue = (float)-1.5;
	static final double xStaticFieldDoubleValue = -1.5;
	static final String xStaticFieldStringValue = "minus one";
	static final boolean xStaticFieldBooleanValue = true;

	static final byte yStaticFieldByteValue = 6;
	static final char yStaticFieldCharValue = 6;
	static final short yStaticFieldShortValue = 6;
	static final int yStaticFieldIntValue = 6;
	static final long yStaticFieldLongValue = 6;
	static final float yStaticFieldFloatValue = (float)6.5;
	static final double yStaticFieldDoubleValue = 6.5;
	static final String yStaticFieldStringValue = "six";
	static final boolean yStaticFieldBooleanValue = false;

	static final String xArrayByte = "xArrayByte";
	static final String xArrayChar = "xArrayChar";
	static final String xArrayShort = "xArrayShort";
	static final String xArrayInt = "xArrayInt";
	static final String xArrayLong = "xArrayLong";
	static final String xArrayFloat = "xArrayFloat";
	static final String xArrayDouble = "xArrayDouble";
	static final String xArrayString = "xArrayString";
	static final String xArrayBoolean = "xArrayBoolean";

	static final String yArrayByte = "yArrayByte";
	static final String yArrayChar = "yArrayChar";
	static final String yArrayShort = "yArrayShort";
	static final String yArrayInt = "yArrayInt";
	static final String yArrayLong = "yArrayLong";
	static final String yArrayFloat = "yArrayFloat";
	static final String yArrayDouble = "yArrayDouble";
	static final String yArrayString = "yArrayString";
	static final String yArrayBoolean = "yArrayBoolean";

	static final byte[] xArrayByteValue = new byte[]{1, 2, 3};
	static final char[] xArrayCharValue = new char[]{1, 2, 3};
	static final short[] xArrayShortValue = new short[]{1, 2, 3};
	static final int[] xArrayIntValue = new int[]{1, 2, 3};
	static final long[] xArrayLongValue = new long[]{1, 2, 3};
	static final float[] xArrayFloatValue = new float[]{(float)1.2, (float)2.3, (float)3.4};
	static final double[] xArrayDoubleValue = new double[]{1.2, 2.3, 3.4};
	static final String[] xArrayStringValue = new String[]{"one", "two", "three"};
	static final boolean[] xArrayBooleanValue = new boolean[]{true, false, true};

	static final byte[] yArrayByteValue = new byte[]{7, 8, 9};
	static final char[] yArrayCharValue = new char[]{7, 8, 9};
	static final short[] yArrayShortValue = new short[]{7, 8, 9};
	static final int[] yArrayIntValue = new int[]{7, 8, 9};
	static final long[] yArrayLongValue = new long[]{7, 8, 9};
	static final float[] yArrayFloatValue = new float[]{(float)7.6, (float)8.7, (float)9.8};
	static final double[] yArrayDoubleValue = new double[]{7.6, 8.7, 9.8};
	static final String[] yArrayStringValue = new String[]{"seven", "eight", "nine"};
	static final boolean[] yArrayBooleanValue = new boolean[]{false, true, false};


	static final String plusOp = "+";
	static final String minusOp = "-";
	static final String multiplyOp = "*";
	static final String divideOp = "/";
	static final String remainderOp = "%";
	static final String greaterOp = ">";
	static final String greaterEqualOp = ">=";
	static final String lessOp = "<";
	static final String lessEqualOp = "<=";
	static final String equalEqualOp = "==";
	static final String notEqualOp = "!=";
	static final String leftShiftOp = "<<";
	static final String rightShiftOp = ">>";
	static final String unsignedRightShiftOp = ">>>";
	static final String orOp = "|";
	static final String andOp = "&";
	static final String xorOp = "^";
	static final String notOp = "!";
	static final String twiddleOp = "~";
	static final String equalOp = "=";
	static final String plusAssignmentOp = "+=";
	static final String minusAssignmentOp = "-=";
	static final String multiplyAssignmentOp = "*=";
	static final String divideAssignmentOp = "/=";
	static final String remainderAssignmentOp = "%=";
	static final String leftShiftAssignmentOp = "<<=";
	static final String rightShiftAssignmentOp = ">>=";
	static final String unsignedRightShiftAssignmentOp = ">>>=";
	static final String orAssignmentOp = "|=";
	static final String andAssignmentOp = "&=";
	static final String xorAssignmentOp = "^=";
	static final String prefixPlusPlusOp = "++";
	static final String postfixPlusPlusOp = "++";
	static final String prefixMinusMinusOp = "--";
	static final String postfixMinusMinusOp = "--";


	static final String aInt = "a";
	static final String bInt = "b";
	static final String cInt = "c";
	static final String dInt = "d";
	static final String eInt = "e";
	static final String fInt = "f";
	static final String gInt = "g";
	static final String hInt = "h";
	static final String iInt = "i";
	static final String jInt = "j";

	static final String aString = "aa";
	static final String bString = "bb";
	static final String cString = "cc";
	static final String dString = "dd";
	static final String eString = "ee";
	static final String fString = "ff";
	static final String gString = "gg";
	static final String hString = "hh";
	static final String iString = "ii";
	static final String jString = "jj";

	static final int aIntValue_0 = 1;
	static final int bIntValue_0 = 2;
	static final int cIntValue_0 = 3;
	static final int dIntValue_0 = 4;
	static final int eIntValue_0 = 5;
	static final int fIntValue_0 = 6;
	static final int aIntValue_1 = 1;
	static final int bIntValue_1 = 2;
	static final int cIntValue_1 = 37;
	static final int dIntValue_1 = 48;
	static final int eIntValue_1 = 5;
	static final int fIntValue_1 = 6;
	static final int gIntValue_1 = 7;
	static final int hIntValue_1 = 8;
	static final int aIntValue_2 = 1;
	static final int bIntValue_2 = 2;
	static final int cIntValue_2 = 379;
	static final int dIntValue_2 = 480;
	static final int eIntValue_2 = 59;
	static final int fIntValue_2 = 60;
	static final int gIntValue_2 = 7;
	static final int hIntValue_2 = 8;
	static final int iIntValue_2 = 9;
	static final int jIntValue_2 = 0;


	static final String aStringValue_0 = "one";
	static final String bStringValue_0 = "two";
	static final String cStringValue_0 = "three";
	static final String dStringValue_0 = "four";
	static final String eStringValue_0 = "five";
	static final String fStringValue_0 = "six";
	static final String aStringValue_1 = "one";
	static final String bStringValue_1 = "two";
	static final String cStringValue_1 = "three seven";
	static final String dStringValue_1 = "four eight";
	static final String eStringValue_1 = "five";
	static final String fStringValue_1 = "six";
	static final String gStringValue_1 = "seven";
	static final String hStringValue_1 = "eight";
	static final String aStringValue_2 = "one";
	static final String bStringValue_2 = "two";
	static final String cStringValue_2 = "three seven nine";
	static final String dStringValue_2 = "four eight zero";
	static final String eStringValue_2 = "five nine";
	static final String fStringValue_2 = "six zero";
	static final String gStringValue_2 = "seven";
	static final String hStringValue_2 = "eight";
	static final String iStringValue_2 = "nine";
	static final String jStringValue_2 = "zero";

	static final String EMPTY= "";
	static final String THIS= "this.";
	static final String T_T= "EvalNestedTypeTests.";
	static final String T_T_A= T_T + "A.";
	static final String T_A= "A.";
	static final String T_T_A_AA= T_T_A + "AA.";
	static final String T_A_AA= T_A + "AA.";
	static final String T_AA= "AA.";
	static final String T_T_A_AB= T_T_A + "AB.";
	static final String T_A_AB= T_A + "AB.";
	static final String T_AB= "AB.";
	static final String T_T_B= T_T + "B.";
	static final String T_B= "B.";
	static final String T_T_B_BB= T_T_B + "BB.";
	static final String T_B_BB= T_B + "BB.";
	static final String T_BB= "BB.";
	static final String T_C= "C.";
	static final String T_E= "E.";

	static final String T_T_this= T_T + "this.";
	static final String T_T_A_this= T_T_A + "this.";
	static final String T_A_this= T_A + "this.";
	static final String T_B_this= T_B + "this.";
	static final String T_C_this= T_C + "this.";
	static final String T_E_this= T_E + "this.";

	static final String I_A= "i_a.";
	static final String I_AA= "i_aa.";
	static final String I_AB= "i_ab.";
	static final String I_AC= "i_ac.";
	static final String I_AD= "i_ad.";
	static final String I_AE= "i_ae.";
	static final String I_AF= "i_af.";
	static final String I_B= "i_b.";
	static final String I_BB= "i_bb.";
	static final String I_BC= "i_bc.";
	static final String I_BD= "i_bd.";
	static final String I_C= "i_c.";
	static final String I_CB= "i_cb.";
	static final String I_CC= "i_cc.";
	static final String I_CD= "i_cd.";
	static final String I_D= "i_d.";
	static final String I_DB= "i_db.";
	static final String I_DC= "i_dc.";
	static final String I_DD= "i_dd.";
	static final String I_E= "i_e.";
	static final String I_EB= "i_eb.";
	static final String I_EC= "i_ec.";
	static final String I_ED= "i_ed.";
	static final String I_F= "i_f.";
	static final String I_FB= "i_fb.";
	static final String I_FC= "i_fc.";
	static final String I_FD= "i_fd.";


	/**
	 * Constructor for Tests.
	 * @param name
	 */
	public Tests(String name) {
		super(name);
	}

	static protected IJavaThread fSuspendeeThread;

	static protected IJavaStackFrame fFrame;

	static protected ICompilationUnit fCu;

	static protected IEvaluationEngine fEngine;


	static protected IValue eval(String command) {

		class Listener implements IEvaluationListener {
			IEvaluationResult fResult;

			@Override
			public void evaluationComplete(IEvaluationResult result) {
				fResult= result;
			}

			public IEvaluationResult getResult() {
				return fResult;
			}
		}
		Listener listener= new Listener();
		try {
			fEngine.evaluate(command, fFrame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
		} catch (DebugException e) {
			e.printStackTrace();
		}
		while (listener.fResult == null) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
			}
		}
		IEvaluationResult result= listener.getResult();
		if (result.hasErrors()) {
			String message;
			DebugException exception= result.getException();
			if (exception == null) {
				message= IInternalDebugCoreConstants.EMPTY_STRING;
				String[] messages= result.getErrorMessages();
				for (int i= 0, limit= messages.length; i < limit; i++) {
					message += messages[i] + ", ";
				}
			} else {
				message= exception.getStatus().getMessage();
			}
			assertTrue(message, false);
		}
		return result.getValue();
	}

	protected void initializeFrame(String testClass, int breakPointLine, int numberFrames) throws Exception {
		fFrame = getStackFrame(breakPointLine, numberFrames, 0, 0, testClass);

		fEngine = getEvaluationEngine((IJavaDebugTarget)fFrame.getDebugTarget(), get14Project());
	}

	protected void initializeFrame(String testClass, int breakPointLine, int numberFrames, int hitCount) throws Exception {
		fFrame = getStackFrame(breakPointLine, numberFrames, 0, hitCount, testClass);

		fEngine = getEvaluationEngine((IJavaDebugTarget)fFrame.getDebugTarget(), get14Project());
	}

	protected void destroyFrame() throws Exception {
		try {
			terminateAndRemove(fSuspendeeThread);
		} finally {
			removeAllBreakpoints();
			if (fEngine != null) {
				fEngine.dispose();
			}
		}
		fFrame = null;
	}


	protected IEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm, IJavaProject project) {
		IEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(project, vm);
		return engine;
	}

	protected IJavaStackFrame getStackFrame(int breakpointLine, int numberFrames, int frameNumber, int hitCount, String testClass) throws Exception {
		IJavaLineBreakpoint breakpoint= createLineBreakpoint(breakpointLine, testClass);
		breakpoint.setHitCount(hitCount);
		fSuspendeeThread= launchToLineBreakpoint(testClass, breakpoint);
		IStackFrame[] stackFrames= fSuspendeeThread.getStackFrames();
		assertEquals("Should be " + numberFrames + " stack frame children, was: " + stackFrames.length, numberFrames, stackFrames.length);
		IStackFrame stackFrame= stackFrames[frameNumber];
		return (IJavaStackFrame)stackFrame;
	}

}
