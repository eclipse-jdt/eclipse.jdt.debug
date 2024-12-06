/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation -- initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.variables;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.ObjectComparision;

public class CompareObjectsTest extends AbstractDebugTest {

	private ObjectComparision objectComparision;

	public CompareObjectsTest(String name) {
		super(name);
		objectComparision = new ObjectComparision();
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

	public void testForNormalStrings() throws Exception { // Test for String, StringBuffer & StringBuilder

		String typeName = "CompareObjectsStringTest";
		IJavaLineBreakpoint bp = createLineBreakpoint(30, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 30, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			IJavaVariable s5 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable s6 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			IJavaVariable s7 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable s8 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			IJavaVariable s9 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[10];
			Map<String, Object> result = objectComparision.stringCompare(s1, s2);
			boolean compareResult = (boolean) result.get("ImmediateResult");
			assertTrue("Contents are same regardless of memory location", compareResult);
			result = objectComparision.stringCompare(s3, s4);
			compareResult = (boolean) result.get("ImmediateResult");
			assertFalse("Contents are different", compareResult);
			result = objectComparision.compareObjects(s5, s6);
			compareResult = (boolean) result.get("ImmediateResult");
			assertTrue("Contents are same in StringBuffer", compareResult);
			result = objectComparision.compareObjects(s7, s8);
			compareResult = (boolean) result.get("ImmediateResult");
			assertTrue("Contents are same in StringBuilder", compareResult);
			result = objectComparision.compareObjects(s9, s8);
			compareResult = (boolean) result.get("ImmediateResult");
			assertFalse("Contents are different in StringBuilder", compareResult);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForListArrayList() throws Exception { // Test for Lists ArrayList

		String typeName = "CompareListObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(28, typeName);
		IJavaThread mainThread = null;
		List<String> l1;
		List<String> l2;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 28, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			Map<String, Object> result = objectComparision.compareLists(s1, s2);
			l1 = (List<String>) result.get("valueSet1");
			l2 = (List<String>) result.get("valueSet2");
			boolean contentsCheck = objectComparision.listContentsCheck(l1, l2) && objectComparision.listContentsCheck(l2, l1);
			assertTrue("Contents are actually same", contentsCheck);
			result = objectComparision.compareLists(s3, s4);
			l1 = (List<String>) result.get("valueSet1");
			l2 = (List<String>) result.get("valueSet2");
			contentsCheck = objectComparision.listContentsCheck(l1, l2) && objectComparision.listContentsCheck(l2, l1);
			assertFalse("Contents are actually different", contentsCheck);
			IJavaVariable linked1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable linked2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];

			result = objectComparision.compareLists(linked1, linked2);
			l1 = (List<String>) result.get("valueSet1");
			l2 = (List<String>) result.get("valueSet2");
			contentsCheck = objectComparision.listContentsCheck(l1, l2) && objectComparision.listContentsCheck(l2, l1);
			assertFalse("Contents are different", contentsCheck);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForMaps() throws Exception { // Test for Maps

		String typeName = "CompareMapObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(35, typeName);
		IJavaThread mainThread = null;
		List<String> v1;
		List<String> v2;
		List<String> k1;
		List<String> k2;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 35, hitLine);
			IJavaVariable map1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable map2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			Map<String, Object> result = objectComparision.compareForMaps(map1, map2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");

			k1 = (List<String>) result.get("keySet1");
			k2 = (List<String>) result.get("keySet2");
			boolean valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			boolean keyCheck = objectComparision.listContentsCheck(k1, k2) && objectComparision.listContentsCheck(k2, k1);
			assertFalse("Keys are different", keyCheck);
			assertTrue("Values are same", valueCheck);

			IJavaVariable map3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable map4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result = objectComparision.compareForMaps(map3, map4);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			k1 = (List<String>) result.get("keySet1");
			k2 = (List<String>) result.get("keySet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			keyCheck = objectComparision.listContentsCheck(k1, k2) && objectComparision.listContentsCheck(k2, k1);
			assertTrue("Keys are same", keyCheck);
			assertTrue("Values are same", valueCheck);

			IJavaVariable map5 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable map6 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			result = objectComparision.compareForMaps(map5, map6);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			k1 = (List<String>) result.get("keySet1");
			k2 = (List<String>) result.get("keySet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			keyCheck = objectComparision.listContentsCheck(k1, k2) && objectComparision.listContentsCheck(k2, k1);
			assertTrue("Keys are same", keyCheck);
			assertFalse("Values are different", valueCheck);

			IJavaVariable map7 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable map8 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			result = objectComparision.compareForMaps(map7, map8);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			k1 = (List<String>) result.get("keySet1");
			k2 = (List<String>) result.get("keySet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			keyCheck = objectComparision.listContentsCheck(k1, k2) && objectComparision.listContentsCheck(k2, k1);
			assertTrue("Keys are same", keyCheck);
			assertTrue("Values are same", valueCheck);

			result = objectComparision.compareForMaps(map5, map8);
			boolean res = (boolean) result.get("ImmediateResult");
			assertFalse("Selected types are different", res);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForSets() throws Exception { // Test for Sets

		String typeName = "CompareSetObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(44, typeName);
		IJavaThread mainThread = null;
		List<String> v1;
		List<String> v2;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 44, hitLine);
			IJavaVariable set1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable set2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			Map<String, Object> result = objectComparision.compareSets(set1, set2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");

			boolean valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertFalse("Values are different", valueCheck);

			IJavaVariable hashSet1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable hashSet2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result = objectComparision.compareSets(hashSet1, hashSet2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertTrue("Values are same", valueCheck);

			IJavaVariable LinkedHashSet1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable LinkedHashSet2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			result = objectComparision.compareSets(LinkedHashSet1, LinkedHashSet2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertFalse("Values are different", valueCheck);

			IJavaVariable CopyConcurrentSet1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable CopyConcurrentSet2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			result = objectComparision.compareSets(CopyConcurrentSet1, CopyConcurrentSet2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertTrue("Values are same", valueCheck);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	public void testForNormalObjects() throws Exception { // Test for normal objects

		String typeName = "CompareNormalObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(31, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 31, hitLine);
			IJavaVariable obj1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable obj2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			Map<String, Object> result = objectComparision.compareObjects(obj1, obj2);
			boolean res = (boolean) result.get("ImmediateResult");
			assertFalse("Selected objects are different", res);

			IJavaVariable obj3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			result = objectComparision.compareObjects(obj1, obj3);
			res = (boolean) result.get("ImmediateResult");
			assertTrue("Selected objects are same", res);

			IJavaVariable wrap1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			IJavaVariable wrap2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			result = objectComparision.compareObjects(wrap1, wrap2);
			res = (boolean) result.get("ImmediateResult");
			assertTrue("Selected objects are same", res);

			IJavaVariable wrap3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			IJavaVariable wrap4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			result = objectComparision.compareObjects(wrap3, wrap4);
			res = (boolean) result.get("ImmediateResult");
			assertFalse("Selected objects are different", res);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForArrayObjects() throws Exception { // Test for Arrays

		String typeName = "CompareArrayObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(22, typeName);
		IJavaThread mainThread = null;
		List<String> v1;
		List<String> v2;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 22, hitLine);
			IJavaVariable obj1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable obj2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			Map<String, Object> result = objectComparision.compareArrays(obj1, obj2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");

			boolean valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertTrue("Values are same", valueCheck);

			obj1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			obj2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result = objectComparision.compareArrays(obj1, obj2);
			v1 = (List<String>) result.get("valueSet1");
			v2 = (List<String>) result.get("valueSet2");
			valueCheck = objectComparision.listContentsCheck(v1, v2) && objectComparision.listContentsCheck(v2, v1);
			assertFalse("Values are different", valueCheck);
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}
}
