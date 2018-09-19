/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.sourcelookup;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;

/**
 * Tests resolution of Java debug model type into Java model types.
 */
public class TypeResolutionTests extends AbstractDebugTest {

	public TypeResolutionTests(String name) {
		super(name);
	}

	public void testTypeAA() throws Exception {
		resolve(65, "EvalNestedTypeTests$A$AA");
	}

	public void testTypeAB() throws Exception {
		resolve(97, "EvalNestedTypeTests$A$AB");
	}

	public void testTypeAC() throws Exception {
		resolve(123, "EvalNestedTypeTests$A$AC");
	}

	public void testAnonTypeA1() throws Exception {
		resolve(148, "EvalNestedTypeTests$A$1");
	}

	public void testTypeAE() throws Exception {
		resolve(182, "EvalNestedTypeTests$A$AE");
	}

	public void testAnonTypeA2() throws Exception {
		resolve(206, "EvalNestedTypeTests$A$2");
	}

	public void testTypeBB() throws Exception {
		resolve(255, "EvalNestedTypeTests$B$BB");
	}

	public void testTypeBC() throws Exception {
		resolve(282, "EvalNestedTypeTests$B$BC");
	}

	public void testAnonTypeB() throws Exception {
		resolve(307, "EvalNestedTypeTests$B$1");
	}

	public void testTypeB() throws Exception {
		resolve(312, "EvalNestedTypeTests$B");
	}

	public void testTypeCB() throws Exception {
		resolve(357, "EvalNestedTypeTests$C$CB");
	}

	public void testTypeCC() throws Exception {
		resolve(378, "EvalNestedTypeTests$C$CC");
	}

	public void testAnonTypeC1() throws Exception {
		resolve(409, "EvalNestedTypeTests$C$1");
	}

	public void testAnonTypeDB() throws Exception {
		resolve(458, "EvalNestedTypeTests$1$DB");
	}

	public void testAnonTypeDC() throws Exception {
		resolve(484, "EvalNestedTypeTests$1$DC");
	}

	public void testAnonType11() throws Exception {
		resolve(509, "EvalNestedTypeTests$1$1");
	}

	public void testTopLevelType() throws Exception {
		resolve(526, "EvalNestedTypeTests");
	}

	public void testTypeEB() throws Exception {
		resolve(569, "EvalNestedTypeTests$E$EB");
	}

	public void testTypeEC() throws Exception {
		resolve(595, "EvalNestedTypeTests$E$EC");
	}

	public void testAnonTypeE1() throws Exception {
		resolve(619, "EvalNestedTypeTests$E$1");
	}

	public void testAnonTypeFB() throws Exception {
		resolve(667, "EvalNestedTypeTests$2$FB");
	}

	public void testAnonTypeFC() throws Exception {
		resolve(693, "EvalNestedTypeTests$2$FC");
	}

	public void testAnonType21() throws Exception {
		resolve(717, "EvalNestedTypeTests$2$1");
	}

	/**
	 * Performs a resolution test. Debugs to a breakpoint and resolves the
	 * declaring type of the stack frame.
	 *
	 * @param line breakpoint line number
	 * @param expectedName expected fully qualified name of resolved type
	 * @throws Exception on failure
	 */
	protected void resolve(int line, String expectedName) throws Exception {
		String typeName = "EvalNestedTypeTests";
		IJavaLineBreakpoint bp = createLineBreakpoint(line, typeName);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName, false);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IBreakpoint hit = getBreakpoint(thread);
			assertEquals("Wrong breakpoint", bp, hit);
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			IJavaReferenceType referenceType = frame.getReferenceType();
			IType type = JavaDebugUtils.resolveType(referenceType);
			assertNotNull("failed to resolve type", type);
			assertEquals("Wrong type", expectedName, type.getFullyQualifiedName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
