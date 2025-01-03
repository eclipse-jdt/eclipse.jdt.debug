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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;

public class ObjectComparison {

	public static final String IMMEDIATE_RESULT_KEY = "ImmediateResult"; //$NON-NLS-1$
	public static final String SELECTED_OBJECT_1 = "Selection1"; //$NON-NLS-1$
	public static final String KEYSET_1 = "keySet"; //$NON-NLS-1$
	public static final String VALUESET_1 = "valueSet"; //$NON-NLS-1$

	public static final String MAP_KEY_SAME = "keySameInfo"; //$NON-NLS-1$
	public static final String MAP_VAL_SAME = "valSameInfo"; //$NON-NLS-1$

	public static final String ELEMENT_SIZE = "Element Size"; //$NON-NLS-1$

	public static final String OBJECT_TYPE = "Type"; //$NON-NLS-1$
	public static final String OBJECT_VALUES = "Values"; //$NON-NLS-1$

	/**
	 * Extracts the actual value as string representation of the given object
	 *
	 * @param value
	 *            IJavaValue of the selected object
	 * @throws DebugException
	 * @return Returns actual value of the object in string format for valid objects else returns combination object's name and reference identity as
	 *         presented in variable view
	 */
	@SuppressWarnings("nls")
	public String objectValueExtraction(IJavaValue value) throws DebugException {
		String refType1 = value.getReferenceTypeName();
		if (refType1.equals("java.lang.String")) {
			return value.getValueString();
		}
		if (refType1.equals("java.lang.StringBuffer") || refType1.equals("java.lang.StringBuilder")) {
			return stringValueExtraction(value);
		}
		for (IVariable v : value.getVariables()) {
			if (v.getName().equalsIgnoreCase("value")) {
				return v.getValue().getValueString();
			}
		}

		return value.toString();
	}

	/**
	 * Extracts byte array of ASCII values received from StringBuffer,StringBuilder and converts it into its actual character value
	 *
	 * @param value
	 *            IJavaValue of the selected object
	 * @throws DebugException
	 * @return Returns string content
	 */
	@SuppressWarnings("nls")
	private String stringValueExtraction(IJavaValue value) throws DebugException {
		int i;
		StringBuilder contentBuilder = new StringBuilder();
		String val;
		for (IVariable v : value.getVariables()) {
			if (v.getName().equalsIgnoreCase("value")) {
				for (IVariable inner : v.getValue().getVariables()) {
					val = inner.getValue().getValueString();
					i = Integer.parseInt(val);
					char[] chars = Character.toChars(i);
					for (char e : chars) {
						contentBuilder.append(e);
					}
				}
			}
		}
		return contentBuilder.toString();
	}

	/**
	 * Parent method for extracting set contents for given collection of selected objects
	 *
	 * @param selections
	 *            List of IStructuredSelection
	 * @throws Exception
	 * @return Returns a Map containing selected IJavaVariable and its extracted Set contents
	 */
	public Map<IJavaVariable, Object> setExtraction(List<IStructuredSelection> selections) throws Exception {
		List<String> contents;
		String message;
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject) {
					contents = setElementsExtraction(javaObject);
					if (contents != null) {
						result.put(selectedObject, contents);
					} else {
						message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
						throw new Exception(message);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Extract Set contents for the given IJavaObject tyoe
	 *
	 * @param javaObject1
	 *            Selected Set object
	 * @throws DebugException
	 * @return Returns a List of Set contents
	 */
	@SuppressWarnings("nls")
	public List<String> setElementsExtraction(IJavaObject javaObject1) throws DebugException {

		IJavaFieldVariable root1 = javaObject1.getField("map", false);
		if (root1 == null && javaObject1.getReferenceTypeName().startsWith("java.util.TreeSet")) {
			IJavaFieldVariable map1 = javaObject1.getField("m", false);
			if (map1 != null) {
				Map<String, Object> result;
				result = extractFromTreeMaps((IJavaObject) map1.getValue());
				@SuppressWarnings("unchecked")
				List<String> l1 = (List<String>) result.get(KEYSET_1);
				return l1;
			}
		}
		if (root1 == null && javaObject1.getReferenceTypeName().startsWith("java.util.concurrent.CopyOnWriteArraySet")) {
			IJavaFieldVariable map1 = javaObject1.getField("al", false);
			if (map1 != null) {
				return extractFromConcurrentSets((IJavaObject) map1.getValue());
			}
		}
		if (root1 == null) {
			return null;
		}
		IJavaObject map1 = (IJavaObject) root1.getValue();
		IJavaFieldVariable elements1 = map1.getField("table", false);
		List<String> l1 = new ArrayList<>();
		Object value;
		if (elements1.getValue() != null) {
			if (elements1.getValue() instanceof IJavaArray arr) {
				if (arr != null) {
					for (Object ob : arr.getValues()) {
						if (!(ob instanceof JDINullValue) && ob instanceof IJavaObject jdiObject) {
							value = jdiObject.getField("key", false).getValue();
							l1.add(objectValueExtraction((IJavaValue) value));
						}
					}
				}
				return l1;
			}
		}
		return null;
	}


	/**
	 * Extract Keys and Values from TreeMap
	 *
	 * @param elements1
	 *            IJavaObject of to be processed selection
	 * @throws DebugException
	 * @return Returns a Map of KeySet and ValueSet
	 */
	@SuppressWarnings("nls")
	private Map<String, Object> extractFromTreeMaps(IJavaObject elements1) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		List<String> keySet;
		List<String> valSet;
		IJavaFieldVariable root1 = elements1.getField("root", false);
		keySet = new ArrayList<>();
		valSet = new ArrayList<>();
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
			keySet.add(objectValueExtraction((IJavaValue) key));
			valSet.add(objectValueExtraction((IJavaValue) value));
			root1 = (IJavaFieldVariable) root1.getValue().getVariables()[4];
		}
		result.put(KEYSET_1, keySet);
		result.put(VALUESET_1, valSet);
		return result;
	}

	/**
	 * Extract Set contents from Concurrent Set
	 *
	 * @param val1
	 *            Processed IJavaObject type of selection 1
	 * @throws DebugException
	 * @return returns a List of contents in Concurrent Set
	 */
	private List<String> extractFromConcurrentSets(IJavaObject val1) throws DebugException {
		IJavaObject root1 = (IJavaObject) val1.getVariables()[1].getValue();
		List<String> values1 = new ArrayList<>();
		Map<String, Object> result = new HashMap<>();
		if (root1 instanceof JDIArrayValue jdiArray1) {
			for (IJavaValue jdiValue : jdiArray1.getValues()) {
				if (!(jdiValue instanceof JDINullValue)) {
					values1.add(objectValueExtraction(jdiValue));
				}
			}
			result.put(VALUESET_1, values1);
			return values1;
		}
		return null;

	}

	/**
	 * Compares objects having accessible fields that are not part of built-in Java
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and its extracted fields with values
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable keys
	 */
	@SuppressWarnings({ "unchecked", "nls" })
	public Map<IJavaVariable, Object> compareCustomObjects(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		Map<String, String> objectFields1;
		Map<String, String> objectFields2;
		Map<String, Object> properties;
		String val1;
		String val2;
		IJavaVariable key1;
		IJavaVariable key2;
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {

			if (entry1.getValue() instanceof String obj1Value) {
				key1 = entry1.getKey();
				properties = new HashMap<>();
				Set<String> same = new HashSet<>();
				Set<String> diff = new HashSet<>();
				for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
					key2 = entrySub.getKey();
					if (key1.equals(key2)) {
						continue;
					}
					String obj1Value2 = entrySub.getValue().toString();
					if (obj1Value.equals(obj1Value2)) {
						same.add(key2.getName());
					} else {
						diff.add(key2.getName());
					}
				}
				properties.put("REF_SAME", same);
				properties.put("REF_DIFF", diff);
				result.put(key1, properties);

			} else {
				objectFields1 = (Map<String, String>) entry1.getValue();
				key1 = entry1.getKey();
				properties = new HashMap<>();
				Map<String, Set<String>> fieldSame = new HashMap<>();
				Map<String, Set<String>> filedDiff = new HashMap<>();
				Map<String, String> fieldsStatus = new HashMap<>();
				for (String keys : objectFields1.keySet()) {
					fieldsStatus.put(keys, null);
					fieldSame.put(keys, new HashSet<>());
					filedDiff.put(keys, new HashSet<>());
				}
				for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
					key2 = entrySub.getKey();
					if (key1.equals(key2)) {
						continue;
					}
					objectFields2 = (Map<String, String>) entrySub.getValue();
					for (String keys : objectFields1.keySet()) {
						val1 = objectFields1.get(keys);
						val2 = objectFields2.get(keys);
						if (val1.equals(val2)) {
							fieldSame.get(keys).add(key2.getName());
						} else {
							filedDiff.get(keys).add(key2.getName());
						}
					}
				}
				for (String key : fieldsStatus.keySet()) {
					Set<String> same = fieldSame.get(key);
					Set<String> diff = filedDiff.get(key);
					if (same.isEmpty() && !diff.isEmpty()) {
						fieldsStatus.put(key, NLS.bind(DebugUIMessages.ObjectsReferenceDifferent, diff.toString()));
					} else if (diff.isEmpty() && !same.isEmpty()) {
						fieldsStatus.put(key, NLS.bind(DebugUIMessages.ObjectsExtractedSame, same.toString()));
					} else {
						fieldsStatus.put(key, NLS.bind(DebugUIMessages.ObjectsReferenceSameAndDifferent, new Object[] { same.toString(),
								diff.toString() }));
					}
				}
				properties.put("fields", fieldsStatus);
				result.put(key1, properties);
			}
		}
		return result;

	}

	/**
	 * Compare Normal java objects
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and its extracted value
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	public Map<IJavaVariable, Object> compareObjects(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		Map<String, Object> properties;
		String val1;
		String val2;
		String message;
		IJavaVariable key1;
		IJavaVariable key2;
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			val1 = (String) entry1.getValue();
			key1 = entry1.getKey();
			String refType1 = key1.getValue().getReferenceTypeName();
			properties = new HashMap<>();
			List<String> sameList = new ArrayList<>();
			List<String> diffList = new ArrayList<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				key2 = entrySub.getKey();
				if (key1.equals(key2)) {
					continue;
				}
				val2 = (String) entrySub.getValue();
				if (val1.equals(val2)) {
					sameList.add(key2.getName());
				} else {
					diffList.add(key2.getName());
				}
			}
			if (diffList.isEmpty()) {
				message = NLS.bind(DebugUIMessages.ObjectsSameValue, new Object[] { sameList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			} else if (!diffList.isEmpty() && !sameList.isEmpty()) {
				message = NLS.bind(DebugUIMessages.ObjectsSameValueAndDifferentValue, new Object[] { sameList.toString(),
						diffList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			} else {
				message = NLS.bind(DebugUIMessages.DifferentValue, new Object[] { diffList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			}
			properties.put(OBJECT_TYPE, refType1);

			result.put(key1, properties);
		}
		return result;

	}

	/**
	 * Extracts all fields and its values for custom objects if fields are available else throw exception
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws Exception
	 * @return Returns a Map of IJavaVariable and field value pair
	 */
	public Map<IJavaVariable, Object> extractCustomObjects(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		Map<String, String> fields = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					if (javaObject1.getVariables().length == 0) {
						result.put(selectedObject, javaObject1.getValueString());
					} else {
						fields = customObjectValueExtraction(javaObject1);
						result.put(selectedObject, fields);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Extracts all selected objects references status with variable type + id combination
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws Exception
	 * @return Returns a Map of IJavaVariable and references
	 */
	public Map<IJavaVariable, Object> customObjectsReferencesExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		String message;
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject) {
					result.put(selectedObject, javaObject);
				} else {
					message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject.getJavaType() });
					throw new Exception(message);
				}
			} else {
				message = DebugUIMessages.ObjectComparisonFailed;
				throw new Exception(message);
			}
		}
		return result;
	}

	/**
	 * Checks whether all selected objects are from same memory space or not
	 *
	 * @param compareResults
	 *            List of selected objects
	 * @return Return <code>True</code> if all objects are of same reference. <code>False</code> if any one of the objects are from different
	 *         reference
	 */
	public boolean objectsRefCheck(Map<IJavaVariable, Object> compareResults) {
		String val1;
		String val2;
		IJavaVariable key1;
		IJavaVariable key2;
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			val1 = entry1.getValue().toString();
			key1 = entry1.getKey();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				key2 = entrySub.getKey();
				if (key1.equals(key2)) {
					continue;
				}
				val2 = entrySub.getValue().toString();
				if (!val1.equals(val2)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Extracts all fields and its values for custom objects if fields are available else throw exception
	 *
	 * @param javaObject1
	 *            IJavaObject of selected object
	 * @throws DebugException
	 * @return Returns a Map of IJavaVariable and field value pair
	 */
	public Map<String, String> customObjectValueExtraction(IJavaObject javaObject1) throws DebugException {
		String content;
		Map<String, String> contents = new HashMap<>();
		for (IVariable ob : javaObject1.getVariables()) {
			if (ob.getValue() instanceof IJavaObject javaObj) {
				content = objectValueExtraction(javaObj);
			} else {
				content = ob.getValue().getValueString();
			}
			contents.put(ob.getName(), content);
		}
		return contents;
	}

	/**
	 * Extracts values of Java build-in objects like Integer, Boolean etc
	 *
	 * @param selections
	 *            List of selected objects
	 * @return Returns a Map of IJavaVariable and field value pair
	 * @throws Exception
	 */
	public Map<IJavaVariable, Object> extractOtherObjects(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		String contents;
		String message;
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					contents = objectValueExtraction(javaObject1);
					if (contents != null) {
						result.put(selectedObject, contents);
					} else {
						message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
						throw new Exception(message);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Parent method for Array Extractions
	 *
	 * @param selections
	 *            List of selected objects
	 * @return Returns a Map of IJavaVariable and array values
	 * @throws Exception
	 */
	public Map<IJavaVariable, Object> arrayExtraction(List<IStructuredSelection> selections) throws Exception {
		List<String> contents;
		String message;
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				contents = arrayElementsExtraction(selectedObject);
				if (contents != null) {
					result.put(selectedObject, contents);
				} else {
					message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
					throw new Exception(message);
				}
			}
		}
		return result;
	}

	/**
	 * Extracts individual array elements
	 *
	 * @param selectedObject1
	 *            IJavaVariable of selected object
	 * @throws DebugException
	 * @return Returns a List of array elements
	 */
	public List<String> arrayElementsExtraction(IJavaVariable selectedObject1) throws DebugException {
		String val1;
		List<String> arrayElements1 = new ArrayList<>();
		if (selectedObject1.getValue() instanceof IJavaValue javaVal1) {
			for (IVariable jv : javaVal1.getVariables()) {
				val1 = objectValueExtraction((IJavaValue) jv.getValue());
				arrayElements1.add(val1);
			}
			return arrayElements1;
		}
		return null;
	}

	/**
	 * Utility method to check whether contents in list1 contains in list2
	 *
	 * @param l1
	 *            List of type T
	 * @param l2
	 *            List of type T
	 * @return returns <code>true</code> if elements of all list1 elements are present in list2 .<code>false</code> if any element of list1 is missing
	 *         from list2
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
	 * Compares multiple strings
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and its string content
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable
	 */
	public Map<IJavaVariable, Object> stringCompare(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		String val1;
		String val2;
		IJavaVariable key1;
		IJavaVariable key2;
		String message;
		Map<String, Object> properties;
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			val1 = (String) entry1.getValue();
			key1 = entry1.getKey();
			String refType1 = key1.getValue().getReferenceTypeName();
			properties = new HashMap<>();
			List<String> sameList = new ArrayList<>();
			List<String> diffList = new ArrayList<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				key2 = entrySub.getKey();
				if (key1.equals(key2)) {
					continue;
				}
				val2 = (String) entrySub.getValue();
				if (val1.equals(val2)) {
					sameList.add(key2.getName());
				} else {
					diffList.add(key2.getName());
				}
			}
			if (diffList.isEmpty()) {
				message = NLS.bind(DebugUIMessages.StringSame, new Object[] { sameList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			} else if (!diffList.isEmpty() && !sameList.isEmpty()) {
				message = NLS.bind(DebugUIMessages.StringSameAndDifferent, new Object[] { sameList.toString(), diffList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			} else {
				message = NLS.bind(DebugUIMessages.StringDifferent, new Object[] { diffList.toString() });
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
			}
			properties.put(ELEMENT_SIZE, val1.length());
			properties.put(OBJECT_TYPE, refType1);
			result.put(key1, properties);
		}
		return result;
	}

	/**
	 * Compares multiple List
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and its List contents
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable
	 */
	@SuppressWarnings({ "unchecked", "nls" })
	public Map<IJavaVariable, Object> compareSelectedLists(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> difference = new HashMap<>();
		Map<IJavaVariable, Set<String>> setMap = new HashMap<>();
		Map<IJavaVariable, Integer> sizeMap;
		sizeMap = new HashMap<>();
		IJavaVariable key2;
		List<String> listV1;
		List<String> listV2;
		String message;
		Map<String, Object> properties;
		for (Entry<IJavaVariable, Object> entry : compareResults.entrySet()) {
			List<String> list = (List<String>) entry.getValue();
			setMap.put(entry.getKey(), new HashSet<>(list));
			sizeMap.put(entry.getKey(), list.size());
		}

		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			Set<String> differencesTop = new HashSet<>();
			listV1 = (List<String>) entry1.getValue();
			IJavaVariable key1 = entry1.getKey();
			Set<String> set1 = setMap.get(key1);
			Map<String, String> missingData = new HashMap<>();
			String refType1 = key1.getValue().getReferenceTypeName();
			properties = new HashMap<>();

			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				Set<String> differences = new HashSet<>();
				key2 = entrySub.getKey();
				if (key1.equals(key2)) {
					continue;
				}
				Set<String> set2 = setMap.get(key2);
				listV2 = (List<String>) entrySub.getValue();
				if (listV1.equals(listV2)) {
					message = NLS.bind(DebugUIMessages.ListSameElements, new Object[] { key2.getName() });
					properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
					continue;
				} else if (listContentsCheck(listV1, listV2) && listContentsCheck(listV2, listV1)) {
					if (key1.getSignature().contains("Set") || key2.getSignature().contains("Set")) {
						message = NLS.bind(DebugUIMessages.ListSameElements, new Object[] { key2.getName() });
						properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
					} else {
						message = NLS.bind(DebugUIMessages.ListSameELementsInDiffOrder, new Object[] { key2.getName() });
						properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
					}
					continue;
				} else {
					for (String item : set2) {
						if (!set1.contains(item)) {
							differences.add(item);
						}
					}
					missingData.put(key2.getName(), differences.toString());
					differencesTop = new HashSet<>(differences);
				}
			}
			if (!differencesTop.isEmpty()) {
				String listString = missingData.toString();
				properties.put("MultiValues", listString);
				properties.put(OBJECT_VALUES, new ArrayList<>(differencesTop));
			}
			properties.put(ELEMENT_SIZE, sizeMap.get(key1));
			properties.put(OBJECT_TYPE, refType1);
			difference.put(key1, properties);
		}

		return difference;
	}

	/**
	 * Compares multiple Map
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and Map contents
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable
	 */
	@SuppressWarnings({ "nls", "unchecked", "unlikely-arg-type" })
	public Map<IJavaVariable, Object> compareSelectedMaps(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> current;
		Map<IJavaVariable, Object> difference = new HashMap<>();
		Map<IJavaVariable, Set<String>> setMap = new HashMap<>();
		Map<IJavaVariable, Integer> sizeMap = new HashMap<>();
		Map<String, Object> properties;
		IJavaVariable key2;
		String refType1;
		String message;
		List<String> listV1;
		List<String> listV2;
		Set<String> keyDifferences;
		Set<String> valueDifferences;
		Set<String> keySimilarities;
		Set<String> valueSimilarities;
		Map<String, String> missingKeyData;
		Map<String, String> missingValData;
		Map<IJavaVariable, Object> currentSub;
		for (Entry<IJavaVariable, Object> entry : compareResults.entrySet()) {
			current = (Map<IJavaVariable, Object>) entry.getValue();
			List<String> values = (List<String>) current.get(VALUESET_1);
			setMap.put(entry.getKey(), new HashSet<>(values));
			sizeMap.put(entry.getKey(), values.size());
		}

		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			current = (Map<IJavaVariable, Object>) entry1.getValue();
			listV1 = (List<String>) current.get(VALUESET_1);
			IJavaVariable key1 = entry1.getKey();
			Set<String> set1 = setMap.get(key1);
			keyDifferences = new HashSet<>();
			valueDifferences = new HashSet<>();
			List<String> keyList1 = (List<String>) current.get(KEYSET_1);
			refType1 = key1.getValue().getReferenceTypeName();
			properties = new HashMap<>();
			missingKeyData = new HashMap<>();
			missingValData = new HashMap<>();
			keySimilarities = new HashSet<>();
			valueSimilarities = new HashSet<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				currentSub = (Map<IJavaVariable, Object>) entrySub.getValue();
				key2 = entrySub.getKey();
				if (key1.equals(key2)) {
					continue;
				}
				Set<String> set2 = setMap.get(key2);
				listV2 = (List<String>) currentSub.get(VALUESET_1);
				List<String> keyList2 = (List<String>) currentSub.get(KEYSET_1);

				if (keyList1.equals(keyList2)
						|| (listContentsCheck(keyList1, keyList2) && listContentsCheck(keyList2, keyList1) && keyList2.size() == keyList1.size())) {

					keySimilarities.add(key2.getName());
				} else {
					for (String item : keyList2) {
						if (!keyList1.contains(item)) {
							keyDifferences.add(item);
						}
					}
					missingKeyData.put(key2.getName(), keyDifferences.toString());
				}
				if (listV1.equals(listV2)
						|| listContentsCheck(listV2, listV1) && listContentsCheck(listV1, listV2) && listV1.size() == listV2.size()) {
					valueSimilarities.add(key2.getName());

				} else {
					for (String item : set2) {
						if (!set1.contains(item)) {
							valueDifferences.add(item);
						}
					}
					missingValData.put(key2.getName(), valueDifferences.toString());
				}
			}
			if (keyDifferences.isEmpty() || valueDifferences.isEmpty()) {
				if (!keySimilarities.isEmpty()) {
					message = NLS.bind(DebugUIMessages.MapKeysSame, new Object[] { keySimilarities.toString() });
					properties.put(MAP_KEY_SAME, message);
				}
				if (!valueSimilarities.isEmpty()) {
					message = NLS.bind(DebugUIMessages.MapValuesSame, new Object[] { valueSimilarities.toString() });
					properties.put(MAP_VAL_SAME, message);
				}
			}
			if (!keyDifferences.isEmpty()) {
				properties.put("MapKeys", new ArrayList<>(keyDifferences));
				String listKeyString = missingKeyData.toString();
				properties.put("MultiMapKeys", listKeyString);
			}
			if (!valueDifferences.isEmpty()) {
				properties.put("MapValues", new ArrayList<>(valueDifferences));
				String listValString = missingValData.toString();
				properties.put("MultiMapValues", listValString);
			}
			properties.put(ELEMENT_SIZE, keyList1.size());
			properties.put(OBJECT_TYPE, refType1);
			difference.put(key1, properties);
		}
		return difference;
	}

	/**
	 * Parent method for list extraction
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws Exception
	 * @return Returns a Map of IJavaVariable and its List contents
	 */
	public Map<IJavaVariable, Object> listExtraction(List<IStructuredSelection> selections) throws Exception {
		List<String> contents;
		String message;
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					contents = listElementsExtraction(javaObject1);
					if (contents != null) {
						result.put(selectedObject, contents);
					} else {
						message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
						throw new Exception(message);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Extracts List contents for the given IJavaObject type
	 *
	 * @param javaObject1
	 *            IJavaObject of selected object
	 * @throws DebugException
	 * @return Returns actual List of selected List object
	 */
	@SuppressWarnings("nls")
	public List<String> listElementsExtraction(IJavaObject javaObject1) throws DebugException {
		IJavaFieldVariable elements1 = javaObject1.getField("elementData", false);
		List<String> contents = new ArrayList<>();
		if (elements1 == null && javaObject1.getJavaType().toString().equals("java.util.LinkedList")) {
			contents = linkedListExtraction(javaObject1);
			return contents;
		}
		if (elements1 == null && javaObject1.getJavaType().toString().startsWith("java.util.ImmutableCollections")) {
			contents = immutableCollectionListExtraction(javaObject1);
			return contents;
		}
		if (elements1 != null) {
			if (elements1.getValue() instanceof IJavaValue elementData1 && elementData1 instanceof IJavaArray arr1) {
				for (IJavaValue val : arr1.getValues()) {
					if (!(val instanceof JDINullValue)) {
						contents.add(objectValueExtraction(val));
					}
				}
				return contents.stream().filter(e -> !"null".equals(e)).toList();
			}
		} else {
			return null;
		}
		return null;
	}

	/**
	 * Extracts contents for LinkedList
	 *
	 * @param javaObject1
	 *            Processed IJavaObject type of selected object
	 * @throws DebugException
	 * @return Returns actual LinkedList with ignored Null values
	 */
	@SuppressWarnings("nls")
	private List<String> linkedListExtraction(IJavaObject javaObject1) throws DebugException {
		IJavaFieldVariable root1 = javaObject1.getField("first", false);
		List<String> l1 = new ArrayList<>();
		Object value;
		while (!(root1.getValue() instanceof JDINullValue)) {
			value = root1.getValue().getVariables()[0].getValue();
			l1.add(objectValueExtraction((IJavaValue) value));
			root1 = (IJavaFieldVariable) root1.getValue().getVariables()[1];
		}
		return l1.stream().filter(e -> !"null".equals(e)).toList();
	}

	/**
	 * Extracts contents for ImmutableCollection Lists
	 *
	 * @param javaObject1
	 *            Processed IJavaObject type of selected object
	 * @throws DebugException
	 * @return Returns actual List with ignored Null values
	 */
	@SuppressWarnings("nls")
	private List<String> immutableCollectionListExtraction(IJavaObject javaObject1) throws DebugException {
		List<String> l1 = new ArrayList<>();

		for (IVariable fields : javaObject1.getVariables()) {
			l1.add(objectValueExtraction((IJavaValue) fields.getValue()));
		}
		return l1.stream().filter(e -> !"null".equals(e)).toList();
	}

	/**
	 * Parent method for Map Key Values extraction
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws DebugException
	 * @return Returns actual List with ignored Null values
	 */
	public Map<IJavaVariable, Object> mapExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		String message;
		Map<String, Object> mapData = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				mapData = mapElementsExtraction(selectedObject);
				if (mapData != null && !mapData.isEmpty()) {
					result.put(selectedObject, mapData);
				} else {
					message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
					throw new Exception(message);
				}
			}

		}
		return result;
	}

	/**
	 * Parent method for String extraction
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws DebugException
	 * @return Returns Map of IJavaVariable and string contents
	 */
	public Map<IJavaVariable, Object> stringExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		String value;
		String message;
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					value = objectValueExtraction(javaObject1);
					if (value != null) {
						result.put(selectedObject, value);
					} else {
						message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { selectedObject });
						throw new Exception(message);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Parent method for Normal Map and Tree Map objects comparison
	 *
	 * @param selectedObject1
	 *            selected object from variable view
	 * @throws DebugException
	 * @return returns a Map of comparison result details
	 */
	@SuppressWarnings("nls")
	public Map<String, Object> mapElementsExtraction(IJavaVariable selectedObject1) throws DebugException {
		if (selectedObject1.getValue() instanceof IJavaObject javaObject1) {
			IJavaFieldVariable elements1 = javaObject1.getField("table", false);
			if (elements1 == null && javaObject1.getJavaType().toString().equals("java.util.TreeMap")) {
				return extractFromTreeMaps(javaObject1);
			}
			if (elements1 != null) {
				return extractFromNormalMaps(elements1);
			}
		}
		return null;
	}

	/**
	 * Extracts Keys and Values for Normal Map
	 *
	 * @param elements1
	 *            Processed IJavaFieldVariable type of selected object
	 * @throws DebugException
	 * @return returns a Map of Keys and Values for the given IJavaFieldVariable
	 */
	@SuppressWarnings("nls")
	private Map<String, Object> extractFromNormalMaps(IJavaFieldVariable elements1) throws DebugException {
		Map<String, Object> result = new HashMap<>();
		List<String> keySet;
		List<String> valSet;
		if (elements1.getValue() instanceof IJavaValue j1) {
			if (j1 instanceof IJavaArray ar1) {
				int size = ar1.getSize();
				keySet = new ArrayList<>();
				valSet = new ArrayList<>();
				IJavaFieldVariable key;
				IJavaFieldVariable value;
				String keyExtracted;
				String valExtracted;
				int localCounter1 = 0;
				for (int i = 0; i < size; i++) {
					IJavaValue val1 = ar1.getValue(i);
					if (!(val1 instanceof JDINullValue) && val1 instanceof IJavaObject java1) {
						key = java1.getField("key", false);
						value = java1.getField("value", false);
						if (value == null) {
							value = java1.getField("val", false);
						}
						if (key == null) {
							key = java1.getField("referent", false); // For Weak hashMap
						}
						if (key == null) { // identity HashMap
							if (localCounter1 % 2 == 0) {
								keyExtracted = objectValueExtraction(java1);
								keySet.add(keyExtracted);
							} else {
								valExtracted = objectValueExtraction(java1);
								valSet.add(valExtracted);
							}
							localCounter1++;
						} else {
							keyExtracted = objectValueExtraction((IJavaValue) key.getValue());
							keySet.add(keyExtracted);
							valExtracted = objectValueExtraction((IJavaValue) value.getValue());
							valSet.add(valExtracted);
						}
					}
				}
				result.put(KEYSET_1, keySet);
				result.put(VALUESET_1, valSet);
				return result;
			}
		}
		return null;
	}
}
