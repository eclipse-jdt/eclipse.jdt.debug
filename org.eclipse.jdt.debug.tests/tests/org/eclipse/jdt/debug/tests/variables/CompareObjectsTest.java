/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.ObjectComparison;

public class CompareObjectsTest extends AbstractDebugTest {

	private ObjectComparison objectComparision;

	public CompareObjectsTest(String name) {
		super(name);
		objectComparision = new ObjectComparison();
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

	@SuppressWarnings("unchecked")
	public void testForNormalStrings() throws Exception { // Test for String, StringBuffer & StringBuilder

		String typeName = "compare.CompareObjectsStringTest";
		IJavaLineBreakpoint bp = createLineBreakpoint(31, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 31, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];

			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];

			IJavaVariable s5 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable s6 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];

			IJavaVariable s7 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable s8 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];

			IJavaVariable s9 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[10];
			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(s1, objectComparision.objectValueExtraction((IJavaValue) s1.getValue()));
			result.put(s2, objectComparision.objectValueExtraction((IJavaValue) s2.getValue()));
			result = objectComparision.stringCompare(result);
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(s1);
			assertThat(compareResult.get("ImmediateResult"), containsString("Same"));
			result.clear();

			result.put(s3, objectComparision.objectValueExtraction((IJavaValue) s3.getValue()));
			result.put(s4, objectComparision.objectValueExtraction((IJavaValue) s4.getValue()));
			result = objectComparision.stringCompare(result);
			compareResult = (Map<String, String>) result.get(s3);
			assertThat(compareResult.get("ImmediateResult"), containsString("Different"));
			result.clear();

			result.put(s5, objectComparision.objectValueExtraction((IJavaValue) s5.getValue()));
			result.put(s6, objectComparision.objectValueExtraction((IJavaValue) s6.getValue()));
			result = objectComparision.stringCompare(result);
			compareResult = (Map<String, String>) result.get(s5);
			assertThat(compareResult.get("ImmediateResult"), containsString("Same"));
			result.clear();

			result.put(s7, objectComparision.objectValueExtraction((IJavaValue) s7.getValue()));
			result.put(s8, objectComparision.objectValueExtraction((IJavaValue) s8.getValue()));
			result = objectComparision.stringCompare(result);
			compareResult = (Map<String, String>) result.get(s8);
			assertThat(compareResult.get("ImmediateResult"), containsString("Same"));
			result.clear();

			result.put(s1, objectComparision.objectValueExtraction((IJavaValue) s1.getValue()));
			result.put(s9, objectComparision.objectValueExtraction((IJavaValue) s9.getValue()));
			result = objectComparision.stringCompare(result);
			compareResult = (Map<String, String>) result.get(s9);
			assertThat(compareResult.get("ImmediateResult"), containsString("Different"));
			result.clear();

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForLis() throws Exception { // Test for Lists, Stack, Vector, ArrayLists

		String typeName = "compare.CompareListObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(48, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 48, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];

			IJavaVariable s5 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable s6 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];

			IJavaVariable stack = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable arrayList = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			IJavaVariable vector = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[10];
			IJavaVariable linkedList = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[11];

			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(s1, objectComparision.listElementsExtraction((IJavaObject) s1.getValue()));
			result.put(s2, objectComparision.listElementsExtraction((IJavaObject) s2.getValue()));
			result = objectComparision.compareSelectedLists(result, "Lists");
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(s1);
			String resultValue = compareResult.get("ImmediateResult");
			assertEquals("Lists contain same elements as in s2, but in different order", resultValue);
			result.clear();

			result.put(s3, objectComparision.listElementsExtraction((IJavaObject) s3.getValue()));
			result.put(s4, objectComparision.listElementsExtraction((IJavaObject) s4.getValue()));
			result = objectComparision.compareSelectedLists(result, "Lists");
			compareResult = (Map<String, String>) result.get(s3);
			Object o = compareResult.get("Values");
			resultValue = o.toString();
			assertEquals("Element is actually missing - [apple1]", "[apple1]",resultValue);
			result.clear();

			result.put(s5, objectComparision.listElementsExtraction((IJavaObject) s5.getValue()));
			result.put(s6, objectComparision.listElementsExtraction((IJavaObject) s6.getValue()));
			result = objectComparision.compareSelectedLists(result, "Lists");
			compareResult = (Map<String, String>) result.get(s6);
			o = compareResult.get("Values");
			resultValue = o.toString();
			assertEquals("Element is actually missing - [22]","[22]" ,resultValue );
			result.clear();

			result.put(stack, objectComparision.listElementsExtraction((IJavaObject) stack.getValue()));
			result.put(arrayList, objectComparision.listElementsExtraction((IJavaObject) arrayList.getValue()));
			result.put(vector, objectComparision.listElementsExtraction((IJavaObject) vector.getValue()));
			result.put(linkedList, objectComparision.listElementsExtraction((IJavaObject) linkedList.getValue()));
			result = objectComparision.compareSelectedLists(result, "Lists");
			compareResult = (Map<String, String>) result.get(stack);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertThat(resultValue, containsString("same"));
			compareResult = (Map<String, String>) result.get(vector);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertThat(resultValue, containsString("different"));
			compareResult = (Map<String, String>) result.get(arrayList);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertThat(resultValue, containsString("same"));
			compareResult = (Map<String, String>) result.get(linkedList);
			o = compareResult.get("MultiValues");
			resultValue = o.toString();
			assertThat(resultValue, containsString("{ArrayList=[Banana]"));
			result.clear();

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForMaps() throws Exception { // Test for Maps

		String typeName = "compare.CompareMapObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(37, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 37, hitLine);
			IJavaVariable map1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable map2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(map1, objectComparision.mapElementsExtraction(map1));
			result.put(map2, objectComparision.mapElementsExtraction(map2));
			result = objectComparision.compareSelectedMaps(result);
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(map1);
			Object o = compareResult.get("valSameInfo");
			String resultValue = o.toString();
			assertEquals("Values same as [map9]", resultValue);
			o = compareResult.get("MapKeys");
			String resultKey = o.toString();
			assertEquals("[12]", resultKey);
			result.clear();

			IJavaVariable map3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable map4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result.put(map3, objectComparision.mapElementsExtraction(map3));
			result.put(map4, objectComparision.mapElementsExtraction(map4));
			result = objectComparision.compareSelectedMaps(result);
			compareResult = (Map<String, String>) result.get(map3);
			o = compareResult.get("valSameInfo");
			resultValue = o.toString();
			assertEquals("Values same as [map1]", resultValue);
			o = compareResult.get("keySameInfo");
			resultKey = o.toString();
			assertEquals("Keys same as [map1]", resultKey);
			result.clear();

			IJavaVariable map5 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable map6 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			result.put(map5, objectComparision.mapElementsExtraction(map5));
			result.put(map6, objectComparision.mapElementsExtraction(map6));
			result = objectComparision.compareSelectedMaps(result);
			compareResult = (Map<String, String>) result.get(map6);
			o = compareResult.get("MapValues");
			resultValue = o.toString();
			assertEquals("[17.0]", resultValue);
			o = compareResult.get("keySameInfo");
			resultKey = o.toString();
			assertEquals("Keys same as [map4]", resultKey);
			result.clear();

			IJavaVariable map7 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable map8 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			result.put(map7, objectComparision.mapElementsExtraction(map7));
			result.put(map8, objectComparision.mapElementsExtraction(map8));
			result = objectComparision.compareSelectedMaps(result);
			compareResult = (Map<String, String>) result.get(map7);
			o = compareResult.get("MapValues");
			resultValue = o.toString();
			assertEquals("[8.0]", resultValue);
			o = compareResult.get("MapKeys");
			resultKey = o.toString();
			assertEquals("[key2]", resultKey);
			result.clear();

			result.put(map1, objectComparision.mapElementsExtraction(map1));
			result.put(map2, objectComparision.mapElementsExtraction(map2));
			result.put(map3, objectComparision.mapElementsExtraction(map3));
			result.put(map4, objectComparision.mapElementsExtraction(map4));
			result.put(map5, objectComparision.mapElementsExtraction(map5));
			result.put(map6, objectComparision.mapElementsExtraction(map6));
			result.put(map7, objectComparision.mapElementsExtraction(map7));
			result.put(map8, objectComparision.mapElementsExtraction(map8));
			result = objectComparision.compareSelectedMaps(result);
			compareResult = (Map<String, String>) result.get(map7);
			o = compareResult.get("MultiMapValues");
			resultValue = o.toString();
			assertThat(resultValue, containsString("map2"));
			assertThat(resultValue, containsString("map8"));
			assertThat(resultValue, containsString("map1"));
			assertThat(resultValue, containsString("map7"));
			o = compareResult.get("MultiMapKeys");
			resultKey = o.toString();
			assertThat(resultKey, containsString("map2"));
			assertThat(resultKey, containsString("map8"));
			assertThat(resultKey, containsString("map1"));
			assertThat(resultKey, containsString("map7"));
			result.clear();

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForSets() throws Exception { // Test for Sets

		String typeName = "compare.CompareSetObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(46, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 46, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];

			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(s1, objectComparision.setElementsExtraction((IJavaObject) s1.getValue()));
			result.put(s2, objectComparision.setElementsExtraction((IJavaObject) s2.getValue()));
			result = objectComparision.compareSelectedLists(result, "Sets");
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(s1);
			Object o = compareResult.get("Values");
			String resultValue = o.toString();
			assertEquals("[21]", resultValue);
			result.clear();

			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result.put(s3, objectComparision.setElementsExtraction((IJavaObject) s3.getValue()));
			result.put(s4, objectComparision.setElementsExtraction((IJavaObject) s4.getValue()));
			result = objectComparision.compareSelectedLists(result, "Sets");
			compareResult = (Map<String, String>) result.get(s3);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertEquals("Sets same as of hashS2", resultValue);
			result.clear();

			IJavaVariable linkedHashS1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];
			IJavaVariable linkedHashS2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			result.put(linkedHashS1, objectComparision.setElementsExtraction((IJavaObject) linkedHashS1.getValue()));
			result.put(linkedHashS2, objectComparision.setElementsExtraction((IJavaObject) linkedHashS2.getValue()));
			result = objectComparision.compareSelectedLists(result, "Sets");
			compareResult = (Map<String, String>) result.get(linkedHashS2);
			o = compareResult.get("Values");
			resultValue = o.toString();
			assertEquals("[21]", resultValue);
			result.clear();

			IJavaVariable copyWr1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			IJavaVariable copyWr2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[9];
			result.put(copyWr1, objectComparision.setElementsExtraction((IJavaObject) copyWr1.getValue()));
			result.put(copyWr2, objectComparision.setElementsExtraction((IJavaObject) copyWr2.getValue()));
			result = objectComparision.compareSelectedLists(result, "Sets");
			compareResult = (Map<String, String>) result.get(copyWr1);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertEquals("Sets same as of xx2", resultValue);
			result.clear();

			result.put(s1, objectComparision.setElementsExtraction((IJavaObject) s1.getValue()));
			result.put(s2, objectComparision.setElementsExtraction((IJavaObject) s2.getValue()));
			result.put(linkedHashS1, objectComparision.setElementsExtraction((IJavaObject) linkedHashS1.getValue()));
			result.put(linkedHashS2, objectComparision.setElementsExtraction((IJavaObject) linkedHashS2.getValue()));
			result.put(copyWr1, objectComparision.setElementsExtraction((IJavaObject) copyWr1.getValue()));
			result.put(copyWr2, objectComparision.setElementsExtraction((IJavaObject) copyWr2.getValue()));
			result.put(s3, objectComparision.setElementsExtraction((IJavaObject) s3.getValue()));
			result.put(s4, objectComparision.setElementsExtraction((IJavaObject) s4.getValue()));
			result = objectComparision.compareSelectedLists(result, "Sets");
			compareResult = (Map<String, String>) result.get(copyWr1);
			o = compareResult.get("MultiValues");
			resultValue = o.toString();
			assertThat(resultValue, containsString("linkedHashS2"));
			assertThat(resultValue, containsString("linkedHashS1"));
			assertThat(resultValue, containsString("hashS1"));
			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForArrayObjects() throws Exception { // Test for Arrays

		String typeName = "compare.CompareArrayObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(23, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 23, hitLine);
			IJavaVariable s1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable s2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];

			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(s1, objectComparision.arrayElementsExtraction(s1));
			result.put(s2, objectComparision.arrayElementsExtraction(s2));
			result = objectComparision.compareSelectedLists(result, "Arrays");
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(s1);
			Object o = compareResult.get("ImmediateResult");
			String resultValue = o.toString();
			assertEquals("Arrays same as of args1", resultValue);
			result.clear();

			IJavaVariable s3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];
			IJavaVariable s4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			result.put(s3, objectComparision.arrayElementsExtraction(s3));
			result.put(s4, objectComparision.arrayElementsExtraction(s4));
			result = objectComparision.compareSelectedLists(result, "Arrays");
			compareResult = (Map<String, String>) result.get(s4);
			o = compareResult.get("Values");
			resultValue = o.toString();
			assertEquals("[1]", resultValue);
			result.clear();

			result.put(s1, objectComparision.arrayElementsExtraction(s1));
			result.put(s2, objectComparision.arrayElementsExtraction(s2));
			result.put(s3, objectComparision.arrayElementsExtraction(s3));
			result.put(s4, objectComparision.arrayElementsExtraction(s4));
			result = objectComparision.compareSelectedLists(result, "Arrays");
			compareResult = (Map<String, String>) result.get(s3);
			o = compareResult.get("MultiValues");
			resultValue = o.toString();
			assertThat(resultValue, containsString("args12"));
			assertThat(resultValue, containsString("args1"));

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForNormalObjects() throws Exception { // Test for normal objects

		String typeName = "compare.CompareNormalObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(32, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 32, hitLine);
			IJavaVariable wrap1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[5];
			IJavaVariable wrap2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[6];

			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(wrap1, objectComparision.objectValueExtraction((IJavaValue) wrap1.getValue()));
			result.put(wrap2, objectComparision.objectValueExtraction((IJavaValue) wrap2.getValue()));
			result = objectComparision.compareObjects(result);
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(wrap1);
			Object o = compareResult.get("ImmediateResult");
			String resultValue = o.toString();
			assertEquals("Same value as of [i2]", resultValue);
			result.clear();

			IJavaVariable wrap3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[7];
			IJavaVariable wrap4 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[8];
			result.put(wrap3, objectComparision.objectValueExtraction((IJavaValue) wrap3.getValue()));
			result.put(wrap4, objectComparision.objectValueExtraction((IJavaValue) wrap4.getValue()));
			result = objectComparision.compareObjects(result);
			compareResult = (Map<String, String>) result.get(wrap4);
			o = compareResult.get("ImmediateResult");
			resultValue = o.toString();
			assertEquals("Different value in [f1]", resultValue);

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

	@SuppressWarnings("unchecked")
	public void testForCustomObjects() throws Exception { // Test for custom objects

		String typeName = "compare.CompareNormalObjects";
		IJavaLineBreakpoint bp = createLineBreakpoint(32, typeName);
		IJavaThread mainThread = null;
		try {
			mainThread = launchToBreakpoint(typeName);
			int hitLine = 0;
			assertTrue("Thread should be suspended", mainThread.isSuspended());
			hitLine = mainThread.getStackFrames()[0].getLineNumber();
			assertEquals("Didn't suspend at the expected line", 32, hitLine);
			IJavaVariable wrap1 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[2];
			IJavaVariable wrap2 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[3];
			IJavaVariable wrap3 = (IJavaVariable) mainThread.getTopStackFrame().getVariables()[4];

			Map<IJavaVariable, Object> result = new HashMap<>();
			result.put(wrap1, objectComparision.customObjectValueExtraction((IJavaObject) wrap1.getValue()));
			result.put(wrap2, objectComparision.customObjectValueExtraction((IJavaObject) wrap2.getValue()));
			result = objectComparision.compareCustomObjects(result);
			Map<String, String> compareResult = new HashMap<>();
			compareResult = (Map<String, String>) result.get(wrap1);
			Object o = compareResult.get("fields");
			String resultValue = o.toString();
			assertEquals("{custom=Different in [a2]}", resultValue);
			compareResult = (Map<String, String>) result.get(wrap2);
			o = compareResult.get("fields");
			resultValue = o.toString();
			assertEquals("{custom=Different in [a1]}", resultValue);
			result.clear();

			result.put(wrap1, objectComparision.customObjectValueExtraction((IJavaObject) wrap1.getValue()));
			result.put(wrap3, objectComparision.customObjectValueExtraction((IJavaObject) wrap3.getValue()));
			result = objectComparision.compareCustomObjects(result);
			compareResult = (Map<String, String>) result.get(wrap1);
			o = compareResult.get("fields");
			resultValue = o.toString();
			assertEquals("{custom=Same in [a3]}", resultValue);

			bp.delete();
		} finally {
			terminateAndRemove(mainThread);
			removeAllBreakpoints();
		}
	}

}
