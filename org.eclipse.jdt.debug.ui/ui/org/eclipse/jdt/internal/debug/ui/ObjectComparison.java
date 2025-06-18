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

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;

/**
 * Class to provides methods to extract and compare Java Collections, Java Types and Custom Types
 *
 */
public class ObjectComparison {
	public static final String IMMEDIATE_RESULT_KEY = "ImmediateResult"; //$NON-NLS-1$
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
		List<String> interfaceCheck = getInterfaces(refType1);
		if (interfaceCheck.contains("java.lang.CharSequence")) {
			return stringValueExtraction((IJavaObject) value);
		}
		for (IVariable v : value.getVariables()) {
			if (v.getName().equalsIgnoreCase("value")) {
				return v.getValue().getValueString();
			}
		}
		if (interfaceCheck.contains("Number")) {
			IJavaThread thread = getSuspendedThread(value);
			IJavaValue stringVal = ((IJavaObject) value).sendMessage("doubleValue", "()D", null, thread, false);
			return stringVal.getValueString();
		}
		return value.toString();
	}

	/**
	 * Extracts actual String contents from any CharSequence implementations
	 *
	 * @param value
	 *            IJavaObject of the selected object
	 * @throws DebugException
	 * @return Returns string content
	 */
	@SuppressWarnings("nls")
	private String stringValueExtraction(IJavaObject value) throws DebugException {
		IJavaThread thread = getSuspendedThread(value);
		IJavaValue stringVal = value.sendMessage("toString", "()Ljava/lang/String;", null, thread, false);
		return stringVal.getValueString();
	}

	/**
	 * Parent method for extracting set contents for given collection of selected objects
	 *
	 * @param selections
	 *            List of IStructuredSelection
	 * @return Returns a Map containing selected IJavaVariable and its extracted Set contents
	 * @throws DebugException
	 */
	public Map<IJavaVariable, Object> setExtraction(List<IStructuredSelection> selections) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject) {
					List<String> contents = setElementsExtraction(javaObject);
					result.put(selectedObject, contents);
				}
			}
		}
		return result;
	}

	/**
	 * Extract Set contents for the given IJavaObject type
	 *
	 * @param javaObject1
	 *            Selected Set object
	 * @throws DebugException
	 * @return Returns a List of Set contents
	 */
	@SuppressWarnings("nls")
	public List<String> setElementsExtraction(IJavaObject javaObject1) throws DebugException {
		List<String> contents = new ArrayList<>();
		IJavaThread thread = getSuspendedThread(javaObject1);
		IJavaValue toArray = javaObject1.sendMessage("toArray", "()[Ljava/lang/Object;", null, thread, false);
		for (IVariable ob : toArray.getVariables()) {
			contents.add(objectValueExtraction((IJavaValue) ob.getValue()));
		}
		return contents;
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
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			Map<String, Object> properties = new HashMap<>();
			if (entry1.getValue() instanceof String obj1Value) { // Types that doesn't have any fields
				IJavaVariable key1 = entry1.getKey();
				Set<String> same = new HashSet<>();
				Set<String> diff = new HashSet<>();
				for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
					IJavaVariable key2 = entrySub.getKey();
					if (!key1.equals(key2)) {
						String obj1Value2 = entrySub.getValue().toString();
						if (obj1Value.equals(obj1Value2)) {
							same.add(key2.getName());
						} else {
							diff.add(key2.getName());
						}
					}
				}
				properties.put("REF_SAME", same);
				properties.put("REF_DIFF", diff);
				result.put(key1, properties);

			} else { // Types that have one or more fields

				Map<String, String> objectFields1 = (Map<String, String>) entry1.getValue();
				IJavaVariable key1 = entry1.getKey();
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
					IJavaVariable key2 = entrySub.getKey();
					if (!key1.equals(key2)) {
						Map<String, String> objectFields2 = (Map<String, String>) entrySub.getValue();
						for (String keys : objectFields1.keySet()) {
							String val1 = objectFields1.get(keys);
							String val2 = objectFields2.get(keys);
							if (val1.equals(val2)) {
								fieldSame.get(keys).add(key2.getName());
							} else {
								filedDiff.get(keys).add(key2.getName());
							}
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
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			String val1 = entry1.getValue().toString();
			IJavaVariable key1 = entry1.getKey();
			String refType1 = key1.getValue().getReferenceTypeName();
			Map<String, Object> properties = new HashMap<>();
			List<String> sameList = new ArrayList<>();
			List<String> diffList = new ArrayList<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				IJavaVariable key2 = entrySub.getKey();
				if (!key1.equals(key2)) {
					String val2 = entrySub.getValue().toString();
					if (val1.equals(val2)) {
						sameList.add(key2.getName());
					} else {
						diffList.add(key2.getName());
					}
				}
			}
			if (diffList.isEmpty()) {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.ObjectsSameValue, new Object[] {
						sameList.toString() }));
			} else if (!diffList.isEmpty() && !sameList.isEmpty()) {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.ObjectsSameValueAndDifferentValue, new Object[] {
						sameList.toString(), diffList.toString() }));
			} else {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.DifferentValue, new Object[] { diffList.toString() }));
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
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					if (javaObject1.getVariables().length == 0) { // No fields for comparison
						result.put(selectedObject, javaObject1.getValueString());
					} else {
						Map<String, String> fields = customObjectValueExtraction(javaObject1);
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
	 * @return Returns a Map of IJavaVariable and references
	 * @throws DebugException
	 */
	public Map<IJavaVariable, Object> customObjectsReferencesExtraction(List<IStructuredSelection> selections) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject) {
					result.put(selectedObject, javaObject);
				}
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
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			String val1 = entry1.getValue().toString();
			IJavaVariable key1 = entry1.getKey();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				IJavaVariable key2 = entrySub.getKey();
				if (!key1.equals(key2)) {
					String val2 = entrySub.getValue().toString();
					if (!val1.equals(val2)) {
						return false;
					}
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
		Map<String, String> contents = new HashMap<>();
		for (IVariable ob : javaObject1.getVariables()) {
			String content;
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
	 * @throws DebugException
	 */
	public Map<IJavaVariable, Object> extractOtherObjects(List<IStructuredSelection> selections) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					String contents = objectValueExtraction(javaObject1);
					result.put(selectedObject, contents);
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
	 * @throws DebugException
	 */
	public Map<IJavaVariable, Object> arrayExtraction(List<IStructuredSelection> selections) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				List<String> contents = arrayElementsExtraction(selectedObject);
				result.put(selectedObject, contents);
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
		List<String> arrayElements1 = new ArrayList<>();
		if (selectedObject1.getValue() instanceof IJavaValue javaVal1) {
			for (IVariable jv : javaVal1.getVariables()) {
				String val1 = objectValueExtraction((IJavaValue) jv.getValue());
				arrayElements1.add(val1);
			}
		}
		return arrayElements1;
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
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			String val1 = (String) entry1.getValue();
			IJavaVariable key1 = entry1.getKey();
			String refType1 = key1.getValue().getReferenceTypeName();
			Map<String, Object> properties = new HashMap<>();
			List<String> sameList = new ArrayList<>();
			List<String> diffList = new ArrayList<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				IJavaVariable key2 = entrySub.getKey();
				if (!key1.equals(key2)) {
					String val2 = (String) entrySub.getValue();
					if (val1.equals(val2)) {
						sameList.add(key2.getName());
					} else {
						diffList.add(key2.getName());
					}
				}
			}
			if (diffList.isEmpty()) {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.StringSame, new Object[] { sameList.toString() }));
			} else if (!diffList.isEmpty() && !sameList.isEmpty()) {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.StringSameAndDifferent, new Object[] {
						sameList.toString(), diffList.toString() }));
			} else {
				properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, NLS.bind(DebugUIMessages.StringDifferent, new Object[] {
						diffList.toString() }));
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
	 * @param interfaceType
	 *            Type of the interface used for comparison
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable
	 */
	@SuppressWarnings({ "unchecked", "nls" })
	public Map<IJavaVariable, Object> compareSelectedLists(Map<IJavaVariable, Object> compareResults, String interfaceType) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			List<String> listV1 = (List<String>) entry1.getValue();
			int size = listV1.size();
			IJavaVariable key1 = entry1.getKey();
			Map<String, String> missingData = new HashMap<>();
			String refType1 = key1.getValue().getReferenceTypeName();
			Map<String, Object> properties = new HashMap<>();
			List<String> differencesMain = new ArrayList<>();
			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				IJavaVariable key2 = entrySub.getKey();
				if (!key1.equals(key2)) {
					List<String> differencesCurrent = new ArrayList<>();
					List<String> listV2 = (List<String>) entrySub.getValue();
					String message;
					if (listV1.equals(listV2)) {
						message = NLS.bind(DebugUIMessages.ListSameElements, new Object[] { interfaceType, key2.getName() });
						properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
						continue;
					} else if (listContentsCheck(listV1, listV2) && listContentsCheck(listV2, listV1)) {
						if (key1.getSignature().contains("Set") || key2.getSignature().contains("Set")) {
							message = NLS.bind(DebugUIMessages.ListSameElements, new Object[] { interfaceType, key2.getName() });
							properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
						} else {
							message = NLS.bind(DebugUIMessages.ListSameELementsInDiffOrder, new Object[] { interfaceType, key2.getName() });
							properties.put(ObjectComparison.IMMEDIATE_RESULT_KEY, message);
						}
						continue;
					} else {
						for (String item : listV2) {
							if (!listV1.contains(item)) {
								if (!differencesCurrent.contains(item)) {
									differencesCurrent.add(item);
								}
							}
						}
						if (!differencesCurrent.isEmpty()) {
							missingData.put(key2.getName(), differencesCurrent.toString());
							differencesMain = new ArrayList<>(differencesCurrent);
						}
					}
				}
			}
			if (!differencesMain.isEmpty()) {
				String listString = missingData.toString();
				properties.put("MultiValues", listString);
				properties.put(OBJECT_VALUES, differencesMain);
			}
			properties.put(ELEMENT_SIZE, size);
			properties.put(OBJECT_TYPE, refType1);
			result.put(key1, properties);
		}

		return result;
	}

	/**
	 * Compares multiple Map
	 *
	 * @param compareResults
	 *            Map containing IJavaVariable and Map contents
	 * @throws DebugException
	 * @return Returns a Map of comparison result for given IJavaVariable
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	public Map<IJavaVariable, Object> compareSelectedMaps(Map<IJavaVariable, Object> compareResults) throws DebugException {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Map.Entry<IJavaVariable, Object> entry1 : compareResults.entrySet()) {
			Map<String, Object> current = (Map<String, Object>) entry1.getValue();
			List<String> listV1 = (List<String>) current.get(VALUESET_1);
			IJavaVariable key1 = entry1.getKey();
			List<String> keyList1 = (List<String>) current.get(KEYSET_1);
			Map<String, Object> properties = new HashMap<>();
			Map<String, String> missingKeyData = new HashMap<>();
			Map<String, String> missingValData = new HashMap<>();
			Set<String> keySimilarities = new HashSet<>();
			Set<String> valueSimilarities = new HashSet<>();
			Set<String> keyDifferences = new HashSet<>();
			Set<String> valueDifferences = new HashSet<>();

			for (Map.Entry<IJavaVariable, Object> entrySub : compareResults.entrySet()) {
				Map<String, Object> currentSub = (Map<String, Object>) entrySub.getValue();
				IJavaVariable key2 = entrySub.getKey();
				if (!key1.equals(key2)) {

					keySimilarities.clear();
					valueSimilarities.clear();
					keyDifferences.clear();
					valueDifferences.clear();
					List<String> listV2 = (List<String>) currentSub.get(VALUESET_1);
					List<String> keyList2 = (List<String>) currentSub.get(KEYSET_1);

					if (keyList1.equals(keyList2) || (listContentsCheck(keyList1, keyList2) && listContentsCheck(keyList2, keyList1)
							&& keyList2.size() == keyList1.size())) {

						keySimilarities.add(key2.getName());
					} else {
						for (String item : keyList2) {
							if (!keyList1.contains(item)) {
								keyDifferences.add(item);
							}
						}
						if (!keyDifferences.isEmpty()) {
							missingKeyData.put(key2.getName(), keyDifferences.toString());
						}
					}
					if (listV1.equals(listV2)
							|| listContentsCheck(listV2, listV1) && listContentsCheck(listV1, listV2) && listV1.size() == listV2.size()) {
						valueSimilarities.add(key2.getName());

					} else {
						for (String item : listV2) {
							if (!listV1.contains(item)) {
								valueDifferences.add(item);
							}
						}
						if (!valueDifferences.isEmpty()) {
							missingValData.put(key2.getName(), valueDifferences.toString());
						}
					}
				}
			}
			if (keyDifferences.isEmpty() || valueDifferences.isEmpty()) {
				if (!keySimilarities.isEmpty()) {
					properties.put(MAP_KEY_SAME, NLS.bind(DebugUIMessages.MapKeysSame, new Object[] { keySimilarities.toString() }));
				}
				if (!valueSimilarities.isEmpty()) {
					properties.put(MAP_VAL_SAME, NLS.bind(DebugUIMessages.MapValuesSame, new Object[] { valueSimilarities.toString() }));
				}
			}
			if (!missingKeyData.isEmpty()) {
				properties.put("MapKeys", new ArrayList<>(keyDifferences));
				String listKeyString = missingKeyData.toString();
				properties.put("MultiMapKeys", listKeyString);
			}
			if (!missingValData.isEmpty()) {
				properties.put("MapValues", new ArrayList<>(valueDifferences));
				String listValString = missingValData.toString();
				properties.put("MultiMapValues", listValString);
			}
			properties.put(ELEMENT_SIZE, keyList1.size());
			properties.put(OBJECT_TYPE, key1.getValue().getReferenceTypeName());
			result.put(key1, properties);
		}
		return result;
	}


	/**
	 * Parent method for extracting elements from Iterable collection
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws Exception
	 * @return Returns a Map of IJavaVariable and its Iterable contents
	 */
	public Map<IJavaVariable, Object> iterableExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					List<String> contents = iterableElementsExtraction(javaObject1);
					result.put(selectedObject, contents);
				}
			}
		}
		return result;
	}

	/**
	 * Extracts contents for the given IJavaObject type
	 *
	 * @param javaObject1
	 *            IJavaObject of selected object
	 * @throws DebugException
	 * @return Returns actual List of selected Iterable object
	 */
	@SuppressWarnings("nls")
	public List<String> iterableElementsExtraction(IJavaObject javaObject1) throws DebugException {
		List<String> contents = new ArrayList<>();
		IJavaThread thread = getSuspendedThread(javaObject1);
		IJavaObject iterator = (IJavaObject) javaObject1.sendMessage("iterator", "()Ljava/util/Iterator;", null, thread, false);
		while (true) {
			IJavaValue hasNext = iterator.sendMessage("hasNext", "()Z", null, thread, false);
			if (hasNext.getValueString().equals("false")) {
				break;
			}
			IJavaValue next = iterator.sendMessage("next", "()Ljava/lang/Object;", null, thread, false);
			String value = objectValueExtraction(next);
			contents.add(value);
		}
		return contents;
	}

	/**
	 * Parent method for extracting elements from List collection
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws Exception
	 * @return Returns a Map of IJavaVariable and its List contents
	 */
	public Map<IJavaVariable, Object> listExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					List<String> contents = listElementsExtraction(javaObject1);
					result.put(selectedObject, contents);
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
		List<String> contents = new ArrayList<>();
		IJavaThread thread = getSuspendedThread(javaObject1);
		IJavaValue toArray = javaObject1.sendMessage("toArray", "()[Ljava/lang/Object;", null, thread, false);
		for (IVariable ob : toArray.getVariables()) {
			contents.add(objectValueExtraction((IJavaValue) ob.getValue()));
		}
		return contents;
	}

	/**
	 * Parent method for Map Key Values extraction
	 *
	 * @param selections
	 *            List of selected objects
	 * @throws DebugException
	 * @return Returns actual List
	 */
	public Map<IJavaVariable, Object> mapExtraction(List<IStructuredSelection> selections) throws Exception {
		Map<IJavaVariable, Object> result = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				Map<String, Object> mapData = mapElementsExtraction(selectedObject);
				result.put(selectedObject, mapData);
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
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				if (selectedObject.getValue() instanceof IJavaObject javaObject1) {
					String value = stringValueExtraction(javaObject1);
					result.put(selectedObject, value);
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
			IJavaThread thread = getSuspendedThread(javaObject1);
			Map<String, Object> result = new HashMap<>();
			List<String> keySet = new ArrayList<>();
			IJavaObject keySetObject = (IJavaObject) javaObject1.sendMessage("keySet", "()Ljava/util/Set;", null, thread, false);
			IJavaValue keyToArray = keySetObject.sendMessage("toArray", "()[Ljava/lang/Object;", null, thread, false);
			for (IVariable ob : keyToArray.getVariables()) {
				keySet.add(objectValueExtraction((IJavaValue) ob.getValue()));
			}

			List<String> valueSet = new ArrayList<>();
			IJavaObject valueSetObject = (IJavaObject) javaObject1.sendMessage("values", "()Ljava/util/Collection;", null, thread, false);
			IJavaValue valToArray = valueSetObject.sendMessage("toArray", "()[Ljava/lang/Object;", null, thread, false);
			for (IVariable ob : valToArray.getVariables()) {
				valueSet.add(objectValueExtraction((IJavaValue) ob.getValue()));
			}

			result.put(KEYSET_1, keySet);
			result.put(VALUESET_1, valueSet);
			return result;
		}
		return null;
	}

	/**
	 * Extracts the interfaces and superclass to find the right implementation for the selected object
	 *
	 * @param className
	 *            Class name of the object
	 * @return returns a List of interfaces
	 */
	public static List<String> getInterfaces(String className) {
		List<String> names;
		if (className.contains("<")) { //$NON-NLS-1$
			className = className.substring(0, className.indexOf('<'));
		}
		try {
			List<Class<?>> interfaces = List.of(Class.forName(className).getInterfaces());
			if (interfaces.isEmpty()) {
				interfaces = List.of(Class.forName(className).getSuperclass().getInterfaces());
			}
			names = new ArrayList<>(interfaces.stream().map(Class::getCanonicalName).toList());
			names.add(Class.forName(className).getSuperclass().getSimpleName());
		} catch (Exception e) {
			names = new ArrayList<>();
			names.add(className);
		}
		return names;

	}

	/**
	 * Checks the type of interface which object implements
	 *
	 * @param className
	 *            Class name of the object
	 * @return returns a Generic type of the object
	 */
	@SuppressWarnings("nls")
	public static String checkInterfaces(String className) {
		if (className.contains("<")) { //$NON-NLS-1$
			className = className.substring(0, className.indexOf('<'));
		}
		try {
			Class<?> cls = Class.forName(className);
			if (List.class.isAssignableFrom(cls)) {
				return "Lists";
			} else if (Map.class.isAssignableFrom(cls)) {
				return "Maps";
			} else if (Queue.class.isAssignableFrom(cls)) {
				return "Queues";
			} else if (Set.class.isAssignableFrom(cls)) {
				return "Sets";
			} else if (Deque.class.isAssignableFrom(cls)) {
				return "Deques";
			} else if (CharSequence.class.isAssignableFrom(cls)) {
				return "CharSequences";
			} else if (Number.class.isAssignableFrom(cls) || Constable.class.isAssignableFrom(cls)) {
				return "Wrappers";
			} else if (Iterable.class.isAssignableFrom(cls)) {
				return "Iterables";
			} else {
				return className;
			}
		} catch (Exception e) {
			return className;
		}
	}

	/**
	 * Returns a suspended thread for vm message operation
	 *
	 * @param value
	 *            IJavaValue object
	 * @return returns a suspended IJavaThread object
	 */
	private IJavaThread getSuspendedThread(IJavaValue value) throws DebugException {
		IJavaThread thread = (IJavaThread) value.getDebugTarget().getThreads()[0];
		if (!thread.isSuspended()) {
			JDIStackFrame frame = (JDIStackFrame) DebugUITools.getDebugContext();
			thread = (IJavaThread) frame.getThread();
		}
		return thread;
	}

}
