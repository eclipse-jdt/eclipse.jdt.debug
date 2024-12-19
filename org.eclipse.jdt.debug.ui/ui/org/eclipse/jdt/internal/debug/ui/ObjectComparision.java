/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;

public class ObjectComparision {

	public static final String IMMEDIATE_RESULT_KEY = "ImmediateResult"; //$NON-NLS-1$
	public static final String RESULT_FAILED = "ResultFailed"; //$NON-NLS-1$

	public static final String SELECTED_OBJECT_1 = "Selection1"; //$NON-NLS-1$
	public static final String SELECTED_OBJECT_2 = "Selection2"; //$NON-NLS-1$

	public static final String KEYSET_1 = "keySet1"; //$NON-NLS-1$
	public static final String KEYSET_2 = "keySet2"; //$NON-NLS-1$

	public static final String VALUESET_1 = "valueSet1"; //$NON-NLS-1$
	public static final String VALUESET_2 = "valueSet2"; //$NON-NLS-1$

	/**
	 * Extracts the actual value of the given object
	 *
	 * @param value
	 *            IJavaValue of the selected object
	 * @throws DebugException
	 * @return returns actual value of the object or return combination object's name and id if extraction is not possible
	 */
	@SuppressWarnings("nls")
	private String objectExtraction(IJavaValue value) throws DebugException {
		if (value.getReferenceTypeName().equals("java.lang.String")) {
			return value.toString();
		}
		if (value.getReferenceTypeName().startsWith("java.lang.StringBu")) {
			return stringObjectExtraction(value);
		}
		for (IVariable v : value.getVariables()) {
			if (v.getName().equalsIgnoreCase("value")) {
				return v.getValue().getValueString();
			}
		}
		return value.toString();
	}

	/**
	 * Extracts string value of the given string object
	 *
	 * @param value
	 *            IJavaValue of the selected object
	 * @throws DebugException
	 * @return returns actual value of the string or return combination object's name and id if extraction is not possible
	 */
	@SuppressWarnings("nls")
	private String stringObjectExtraction(IJavaValue value) throws DebugException {
		char c;
		int i;
		StringBuilder contentBuilder = new StringBuilder();
		for (IVariable v : value.getVariables()) {
			if (v.getName().equalsIgnoreCase("value")) {
				for (IVariable inner : v.getValue().getVariables()) {
					i = Integer.parseInt(inner.getValue().toString());
					if (i == 0) {
						break;
					}
					c = (char) i;
					contentBuilder.append(c);
				}
			}
		}
		return contentBuilder.toString();
	}

	/**
	 * Compare selected Set objects
	 *
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> compareSets(IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		if (selectedObject1.getValue() instanceof IJavaObject javaObject1 && selectedObject2.getValue() instanceof IJavaObject javaObject2) {
			IJavaFieldVariable f1 = javaObject1.getField("map", false);
			IJavaFieldVariable f2 = javaObject2.getField("map", false);
			if (f1 == null & f2 == null && javaObject1.getReferenceTypeName().startsWith("java.util.TreeSet")) {
				IJavaFieldVariable map1 = javaObject1.getField("m", false);
				IJavaFieldVariable map2 = javaObject2.getField("m", false);
				if (map1 != null) {
					result = compareTreeMapElements((IJavaObject) map1.getValue(), (IJavaObject) map2.getValue(), selectedObject1, selectedObject2);
					@SuppressWarnings("unchecked")
					List<String> l1 = (List<String>) result.get(KEYSET_1);
					@SuppressWarnings("unchecked")
					List<String> l2 = (List<String>) result.get(KEYSET_2);
					result.remove(KEYSET_1);
					result.remove(KEYSET_2);
					result.put(VALUESET_1, l1);
					result.put(VALUESET_2, l2);
					return result;
				}
			}
			if (f1 == null & f2 == null && javaObject1.getReferenceTypeName().startsWith("java.util.concurrent.CopyOnWriteArraySet")) {
				IJavaFieldVariable map1 = javaObject1.getField("al", false);
				IJavaFieldVariable map2 = javaObject2.getField("al", false);
				if (map1 != null) {
					return compareConcurrentSet((IJavaObject) map1.getValue(), (IJavaObject) map2.getValue(), selectedObject1, selectedObject2);
				}
			}
			if (f1 == null & f2 == null) {
				return compareObjects(selectedObject1, selectedObject2);
			}
			IJavaObject map1 = (IJavaObject) f1.getValue();
			IJavaObject map2 = (IJavaObject) f2.getValue();
			IJavaFieldVariable elements1 = map1.getField("table", false);
			IJavaFieldVariable elements2 = map2.getField("table", false);
			List<String> l1 = new ArrayList<>();
			List<String> l2 = new ArrayList<>();
			Object value;
			if (elements1.getValue() != null && elements2.getValue() != null) {
				if (elements1.getValue() instanceof IJavaArray arr && elements2.getValue() instanceof IJavaArray arr2) {
					if (arr != null) {
						for (Object ob : arr.getValues()) {
							if (!(ob instanceof JDINullValue) && ob instanceof IJavaObject jdiObject) {
								value = jdiObject.getField("key", false).getValue();
								l1.add(objectExtraction((IJavaValue) value));
							}
						}
					}
					if (arr2 != null) {
						for (Object ob : arr2.getValues()) {
							if (!(ob instanceof JDINullValue) && ob instanceof IJavaObject jdiObject) {
								value = jdiObject.getField("key", false).getValue();
								l2.add(objectExtraction((IJavaValue) value));
							}
						}
					}
					result.put(VALUESET_1, l1);
					result.put(VALUESET_2, l2);
					return result;
				}
			}
			return compareObjects(selectedObject1, selectedObject2);
		}
		return compareObjects(selectedObject1, selectedObject2);

	}

	/**
	 * Compare selected TreeMap objects
	 *
	 * @param elements1
	 *            Processed IJavaObject type of selection 1
	 * @param elements2
	 *            Processed IJavaObject type of selection 2
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	private Map<String, Object> compareTreeMapElements(IJavaObject elements1, IJavaObject elements2, IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {

		Map<String, Object> result = new HashMap<>();
		IJavaFieldVariable root1 = elements1.getField("root", false);
		IJavaFieldVariable root2 = elements2.getField("root", false);
		List<String> keys1 = new ArrayList<>();
		List<String> keys2 = new ArrayList<>();
		List<String> value1 = new ArrayList<>();
		List<String> value2 = new ArrayList<>();
		Object key;
		Object value;
		Stack<Object> tempStack = new Stack<>();
		while (!(root1.getValue() instanceof JDINullValue) || !tempStack.isEmpty()) {
			while (!(root1.getValue() instanceof JDINullValue)) {
				tempStack.push(root1);
				root1 = (IJavaFieldVariable) root1.getValue().getVariables()[2];
			}
			root1 = (IJavaFieldVariable) tempStack.pop();
			key = root1.getValue().getVariables()[1].getValue();
			value = root1.getValue().getVariables()[5].getValue();
			keys1.add(objectExtraction((IJavaValue) key));
			value1.add(objectExtraction((IJavaValue) value));
			root1 = (IJavaFieldVariable) root1.getValue().getVariables()[4];
		}

		tempStack.clear();

		while (!(root2.getValue() instanceof JDINullValue) || !tempStack.isEmpty()) {
			while (!(root2.getValue() instanceof JDINullValue)) {
				tempStack.push(root2);
				root2 = (IJavaFieldVariable) root2.getValue().getVariables()[2];
			}
			root2 = (IJavaFieldVariable) tempStack.pop();
			key = root2.getValue().getVariables()[1].getValue();
			value = root2.getValue().getVariables()[5].getValue();
			keys2.add(objectExtraction((IJavaValue) key));
			value2.add(objectExtraction((IJavaValue) value));
			root2 = (IJavaFieldVariable) root2.getValue().getVariables()[4];
		}
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		result.put(KEYSET_1, keys1);
		result.put(KEYSET_2, keys2);
		result.put(VALUESET_1, value1);
		result.put(VALUESET_2, value2);
		return result;
	}

	/**
	 * Compare Concurrent Set objects
	 *
	 * @param val1
	 *            Processed IJavaObject type of selection 1
	 * @param val2
	 *            Processed IJavaObject type of selection 2
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	private Map<String, Object> compareConcurrentSet(IJavaObject val1, IJavaObject val2, IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		IJavaObject root1 = (IJavaObject) val1.getVariables()[1].getValue();
		IJavaObject root2 = (IJavaObject) val2.getVariables()[1].getValue();
		List<Object> values1 = new ArrayList<>();
		List<Object> values2 = new ArrayList<>();
		Map<String, Object> result = new HashMap<>();
		if (root1 instanceof JDIArrayValue jdiArray1 && root2 instanceof JDIArrayValue jdiArray2) {
			for (IJavaValue jdiValue : jdiArray1.getValues()) {
				if (!(jdiValue instanceof JDINullValue)) {
					values1.add(objectExtraction(jdiValue));
				}
			}
			for (IJavaValue jdiValue : jdiArray2.getValues()) {
				if (!(jdiValue instanceof JDINullValue)) {
					values2.add(objectExtraction(jdiValue));
				}
			}
			result.put(SELECTED_OBJECT_1, selectedObject1.getName());
			result.put(SELECTED_OBJECT_2, selectedObject2.getName());
			result.put(VALUESET_1, values1);
			result.put(VALUESET_2, values2);
			return result;
		}
		result.put(IMMEDIATE_RESULT_KEY, false);
		String failed = "Selected type is not IJavaValue type"; //$NON-NLS-1$
		result.put(RESULT_FAILED, failed);
		return result;

	}

	/**
	 * Compare Normal objects
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> compareObjects(IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		boolean res = false;
		if (selectedObject1.getValue() instanceof IJavaValue javaVal1 && selectedObject2.getValue() instanceof IJavaValue javaVal2) {
			String v1 = objectExtraction(javaVal1);
			String v2 = objectExtraction(javaVal2);
			if (v1 == null || v2 == null) {
				res = javaVal1.toString().equals(javaVal2.toString());
				result.put(IMMEDIATE_RESULT_KEY, res);
				return result;
			}
			res = v1.equals(v2);
			if (!res) {
				result.put(IMMEDIATE_RESULT_KEY, res);
				String failureReason = "Selected objects are different ";
				result.put(RESULT_FAILED, failureReason);
				return result;
			}
			result.put(IMMEDIATE_RESULT_KEY, res);
			return result;
		}
		result.put(IMMEDIATE_RESULT_KEY, res);
		String failed = "Selected type is not IJavaValue type";
		result.put(RESULT_FAILED, failed);
		return result;
	}
	/**
	 * Compare Array objects
	 *
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> compareArrays(IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		List<String> arrayElements1 = new ArrayList<>();
		List<String> arrayElements2 = new ArrayList<>();
		boolean res = false;
		String val;
		if (selectedObject1.getValue() instanceof IJavaValue javaVal1 && selectedObject2.getValue() instanceof IJavaValue javaVal2) {
			for(IVariable jv : javaVal1.getVariables()) {
				val = objectExtraction((IJavaValue) jv.getValue());
				arrayElements1.add(val);
			}
			for (IVariable jv : javaVal2.getVariables()) {
				val = objectExtraction((IJavaValue) jv.getValue());
				arrayElements2.add(val);
			}
			result.put(VALUESET_1, arrayElements1);
			result.put(VALUESET_2, arrayElements2);
			return result;
		}
		result.put(IMMEDIATE_RESULT_KEY, res);
		String failed = "Selected type is not IJavaValue type";
		result.put(RESULT_FAILED, failed);
		return result;
	}

	/**
	 * Compare two String objects
	 *
	 * @param v1
	 *            First selected object from variable view
	 * @param v2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> stringCompare(IJavaVariable v1, IJavaVariable v2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, v1.getName());
		result.put(SELECTED_OBJECT_2, v2.getName());
		if (v1.getValue().getValueString().equals(v2.getValue().getValueString())) {
			result.put(IMMEDIATE_RESULT_KEY, true);
			return result;
		}
		result.put(IMMEDIATE_RESULT_KEY, false);
		result.put(RESULT_FAILED, "Strings contains different values");
		return result;
	}

	/**
	 * Utility method to check whether contents in list1 contains in list2
	 *
	 * @param l1
	 *            List of type T
	 * @param l2
	 *            List of type T
	 * @return returns <code>true</code> if elements of list1 are present in list2 .<code>false</code> if one or more elements of list1 are missing in
	 *         list2
	 */
	public <T> boolean listContentsCheck(List<T> l1, List<T> l2) {
		for (T t : l1) {
			if (!l2.contains(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compare two List objects
	 *
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> compareLists(IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		if (selectedObject1.getValue().getValueString().equals(selectedObject2.getValue().getValueString())) {
			result.put(IMMEDIATE_RESULT_KEY, true);
			return result;
		}
		if (selectedObject1.getValue() instanceof IJavaObject javaObject1 && selectedObject2.getValue() instanceof IJavaObject javaObject2) {
			IJavaFieldVariable elements1 = javaObject1.getField("elementData", false);
			IJavaFieldVariable elements2 = javaObject2.getField("elementData", false);

			List<String> contents1 = new ArrayList<>();
			List<String> contents2 = new ArrayList<>();

			if (elements1 == null && elements2 == null && javaObject1.getJavaType().toString().equals("java.util.LinkedList")) {
				result = linkedListCompare(javaObject1, javaObject2, selectedObject1, selectedObject2);
				return result;
			}

			if (elements1 != null && elements2 != null) {
				if (elements1.getValue() instanceof IJavaValue elementData1 && elementData1 instanceof IJavaArray arr1
						&& elements2.getValue() instanceof IJavaValue elementData2 && elementData2 instanceof IJavaArray arr2) {

					for (IJavaValue val : arr1.getValues()) {
						contents1.add(objectExtraction(val));
					}
					for (IJavaValue val : arr2.getValues()) {
						contents2.add(objectExtraction(val));
					}
					result.put(VALUESET_1, contents1);
					result.put(VALUESET_2, contents2);
					return result;
				}
			} else {
				return compareObjects(selectedObject1, selectedObject2);
			}
		}
		return compareObjects(selectedObject1, selectedObject2);
	}

	/**
	 * Compare two LinkedList objects
	 *
	 * @param javaObject1
	 *            Processed IJavaObject type of selection 1
	 * @param javaObject2
	 *            Processed IJavaObject type of selection 2
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	private Map<String, Object> linkedListCompare(IJavaObject javaObject1, IJavaObject javaObject2, IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		IJavaFieldVariable root1 = javaObject1.getField("first", false);
		IJavaFieldVariable root2 = javaObject2.getField("first", false);
		List<String> l1 = new ArrayList<>();
		List<String> l2 = new ArrayList<>();
		Object value;
		while (!(root1.getValue() instanceof JDINullValue)) {
			value = root1.getValue().getVariables()[0].getValue();
			l1.add(objectExtraction((IJavaValue) value));
			root1 = (IJavaFieldVariable) root1.getValue().getVariables()[1];
		}
		while (!(root2.getValue() instanceof JDINullValue)) {
			value = root2.getValue().getVariables()[0].getValue();
			l2.add(objectExtraction((IJavaValue) value));
			root2 = (IJavaFieldVariable) root2.getValue().getVariables()[1];
		}
		result.put(SELECTED_OBJECT_1, selectedObject1.getName());
		result.put(SELECTED_OBJECT_2, selectedObject2.getName());
		result.put(VALUESET_1, l1);
		result.put(VALUESET_2, l2);
		return result;
	}

	/**
	 * Parent method for Map objects comparison
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> compareForMaps(IJavaVariable selectedObject1, IJavaVariable selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1);
		result.put(SELECTED_OBJECT_2, selectedObject2);
		boolean res;
		if (selectedObject1.getValue().getValueString().equals(selectedObject2.getValue().getValueString())) {
			res = true;
			result.put(IMMEDIATE_RESULT_KEY, res);
			return result;
		}
		if (selectedObject1.getValue() instanceof IJavaObject javaObject1 && selectedObject2.getValue() instanceof IJavaObject javaObject2) {
			if (!javaObject1.getJavaType().getName().equals(javaObject2.getJavaType().getName())) {
				res = false;
				String message = "Selected objects are of different types\n" + javaObject1.getJavaType().getName() + " and "
						+ javaObject2.getJavaType().getName();
				result.put(IMMEDIATE_RESULT_KEY, res);
				result.put(RESULT_FAILED, message);
				return result;
			}
			IJavaFieldVariable elements1 = javaObject1.getField("table", false);
			IJavaFieldVariable elements2 = javaObject2.getField("table", false);
			if (elements1 == null && elements2 == null && javaObject1.getJavaType().toString().equals("java.util.TreeMap")) {
				return compareTreeMapElements(javaObject1, javaObject2, selectedObject1, selectedObject2);
			}
			if (elements1 != null && elements2 != null) {
				return compareForNormalMaps(elements1, elements2, selectedObject1.getName(), selectedObject2.getName());
			}
		}
		return compareObjects(selectedObject1, selectedObject2);

	}

	/**
	 * Compare two Normal Map objects
	 *
	 * @param elements1
	 *            Processed IJavaObject type of selection 1
	 * @param elements2
	 *            Processed IJavaObject type of selection 2
	 * @param selectedObject1
	 *            First selected object from variable view
	 * @param selectedObject2
	 *            Second selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	private Map<String, Object> compareForNormalMaps(IJavaFieldVariable elements1, IJavaFieldVariable elements2, String selectedObject1, String selectedObject2) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		result.put(SELECTED_OBJECT_1, selectedObject1);
		result.put(SELECTED_OBJECT_2, selectedObject2);
		if (elements1.getValue() instanceof IJavaValue j1 && elements2.getValue() instanceof IJavaValue j2) {
			if (j1 instanceof IJavaArray ar1 && j2 instanceof IJavaArray ar2) {
				int size = ar1.getSize();
				List<String> keySetforFirst = new ArrayList<>();
				List<String> keySetforSecond = new ArrayList<>();
				List<String> valueSetforFirst = new ArrayList<>();
				List<String> valueSetforSecond = new ArrayList<>();
				IJavaFieldVariable key;
				IJavaFieldVariable value;
				String keyExtracted;
				String valExtracted;
				int localCounter1 = 0;
				int localCounter2 = 0;
				for (int i = 0; i < size; i++) {
					IJavaValue val1 = ar1.getValue(i);
					IJavaValue val2 = ar2.getValue(i);
					if (!(val1 instanceof JDINullValue) && val1 instanceof IJavaObject java1) {
						key = java1.getField("key", false);
						value = java1.getField("value", false);
						if (value == null) {
							value = java1.getField("val", false);
						}
						if (key == null) {
							key = java1.getField("referent", false); // For Weak hashMap
						}
						if (key == null) {
							if (localCounter1 % 2 == 0) {
								if (!java1.getReferenceTypeName().equals("java.lang.String")) {
									keyExtracted = objectExtraction((IJavaValue) value.getValue());
									keySetforFirst.add(keyExtracted);
								} else {
									keySetforFirst.add(java1.toString());
								}

							} else {
								if (!java1.getReferenceTypeName().equals("java.lang.String")) {
									valExtracted = objectExtraction((IJavaValue) value.getValue());
									valueSetforFirst.add(valExtracted);
								} else {
									valueSetforFirst.add(java1.toString());
								}
							}
							localCounter1++;
						} else {
							keyExtracted = objectExtraction((IJavaValue) key.getValue());
							keySetforFirst.add(keyExtracted);
							valExtracted = objectExtraction((IJavaValue) value.getValue());
							valueSetforFirst.add(valExtracted);
						}

					}
					if (!(val2 instanceof JDINullValue) && val2 instanceof IJavaObject java2) {

						key = java2.getField("key", false);
						value = java2.getField("value", false);
						if (value == null) {
							value = java2.getField("val", false);
						}
						if (key == null) {
							key = java2.getField("referent", false); // For WeakhashMap
						}
						if (key == null) {
							if (localCounter2 % 2 == 0) {
								if (!java2.getReferenceTypeName().equals("java.lang.String")) {
									keyExtracted = objectExtraction((IJavaValue) value.getValue());
									keySetforSecond.add(keyExtracted);
								} else {
									keySetforSecond.add(java2.toString());
								}
							} else {
								if (!java2.getReferenceTypeName().equals("java.lang.String")) {
									valExtracted = objectExtraction((IJavaValue) value.getValue());
									valueSetforSecond.add(valExtracted);
								} else {
									valueSetforSecond.add(java2.toString());
								}
							}
							localCounter2++;
						} else {
							keyExtracted = objectExtraction((IJavaValue) key.getValue());
							keySetforSecond.add(keyExtracted);
							valExtracted = objectExtraction((IJavaValue) value.getValue());
							valueSetforSecond.add(valExtracted);
						}
					}
				}
				result.put(KEYSET_1, keySetforFirst);
				result.put(KEYSET_2, keySetforSecond);
				result.put(VALUESET_1, valueSetforFirst);
				result.put(VALUESET_2, valueSetforSecond);
				return result;
			}
		}
		result.put(IMMEDIATE_RESULT_KEY, false);
		String failed = "Selected type is not IJavaValue type";
		result.put(RESULT_FAILED, failed);
		return result;
	}
}
