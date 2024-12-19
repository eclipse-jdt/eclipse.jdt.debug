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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.ObjectComparision;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class CompareObjectsAction extends ObjectActionDelegate implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow fWindow;
	private static final String TITLE_NAME_SUCCESS = "Object Comparision"; //$NON-NLS-1$
	private static final String TITLE_NAME_FAILED = "Object Comparision Failed"; //$NON-NLS-1$

	private static final int DISPLAY_LIMIT = 20;
	@Override
	public void init(IWorkbenchWindow window) {
		this.fWindow = window;
	}

	@Override
	protected IWorkbenchPart getPart() {
		IWorkbenchPart part = super.getPart();
		if (part != null) {
			return part;
		} else if (fWindow != null) {
			return fWindow.getActivePage().getActivePart();
		}
		return null;
	}

	@Override
	public void run(IAction action) {
		if (getPart() != null) {
			ISelectionProvider provider = getPart().getSite().getSelectionProvider();
			if (provider != null) {
				ISelection selection = provider.getSelection();
				if (selection instanceof IStructuredSelection selections) {
					compareSelectedObjects(selections.toList());
					return;
				}
			}
		}
	}

	/**
	 * Parent method for comparing selected two objects from variable view
	 *
	 * @param selections
	 *            Selected two objects from variable view
	 */
	@SuppressWarnings("nls")
	protected void compareSelectedObjects(List<IStructuredSelection> selections) {

		Map<String, Object> resultMap = new HashMap<>();
		if (selections != null && !selections.isEmpty()) {
			IJavaVariable selectedObject1 = null;
			IJavaVariable selectedObject2 = null;
			if (selections.get(0) instanceof IJavaVariable && selections.get(1) instanceof IJavaVariable) {
				selectedObject1 = ((IJavaVariable) selections.get(0));
				selectedObject2 = ((IJavaVariable) selections.get(1));
			}
			if (selectedObject2 != null && selectedObject1 != null) {
				ObjectComparision obcp = new ObjectComparision();
				try {
					String referenceTypeObject1 = selectedObject1.getValue().getReferenceTypeName();
					String referenceTypeObject2 = selectedObject2.getValue().getReferenceTypeName();

					if (referenceTypeObject1.equals(referenceTypeObject2)) {

						String selectionReference = selectedObject1.getReferenceTypeName();
						if (selectionReference.equals("java.lang.String")) {
							resultMap = obcp.stringCompare(selectedObject1, selectedObject2);
							displayComparisonResults(resultMap);
						} else if (listLookup(selectionReference) || listLookup(referenceTypeObject1)) {
							resultMap = obcp.compareLists(selectedObject1, selectedObject2);
							displayListResults(resultMap);
						} else if (setLookup(selectionReference)) {
							resultMap = obcp.compareSets(selectedObject1, selectedObject2);
							displayListResults(resultMap);
						} else if (mapLookup(selectionReference)) {
							resultMap = obcp.compareForMaps(selectedObject1, selectedObject2);
							displayMapResults(resultMap);
						} else if (selectionReference.endsWith("[]")) {
							resultMap = obcp.compareArrays(selectedObject1, selectedObject2);
							displayListResults(resultMap);
						} else {
							resultMap = obcp.compareObjects(selectedObject1, selectedObject2);
							displayComparisonResults(resultMap);
						}
					} else {
						resultMap.put(ObjectComparision.SELECTED_OBJECT_1, selectedObject1.getName());
						resultMap.put(ObjectComparision.SELECTED_OBJECT_2, selectedObject2.getName());
						resultMap.put(ObjectComparision.IMMEDIATE_RESULT_KEY, false);
						resultMap.put(ObjectComparision.RESULT_FAILED, "\nSelected objects are of different types :\n"
								+ referenceTypeObject1 + "\n" + referenceTypeObject2);
						displayComparisonResults(resultMap);
					}

				} catch (Exception e) {
					displayComparisonFailed(e.getMessage());
					return;
				}
			}
		}
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
	private <T> boolean listContentsCheck(List<T> l1, List<T> l2) {
		for (T t : l1) {
			if (!l2.contains(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether the given type is of any List's references
	 *
	 * @param reference
	 *            Reference of the selected object
	 * @return returns <code>true</code> if object of list reference .<code>false</code> if object is not of List any references
	 */
	@SuppressWarnings("nls")
	private boolean listLookup(String reference) {
		if (reference.startsWith("java.util.List")) {
			return true;
		} else if (reference.startsWith("java.util.ArrayList")) {
			return true;
		} else if (reference.startsWith("java.util.LinkedList")) {
			return true;
		} else if (reference.startsWith("java.util.Vector")) {
			return true;
		} else if (reference.startsWith("java.util.Stack")) {
			return true;
		} else if (reference.startsWith("java.util.PriorityQueue")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the given type is of any Set's references
	 *
	 * @param reference
	 *            Reference of the selected object
	 * @return returns <code>true</code> if object of set reference .<code>false</code> if object is not of set any references
	 */
	@SuppressWarnings("nls")
	private boolean setLookup(String reference) {
		if (reference.startsWith("java.util.Set")) {
			return true;
		} else if (reference.startsWith("java.util.HashSet")) {
			return true;
		} else if (reference.startsWith("java.util.LinkedHashSet")) {
			return true;
		} else if (reference.startsWith("java.util.TreeSet")) {
			return true;
		} else if (reference.startsWith("java.util.concurrent.CopyOnWriteArraySet")) {
			return true;
		} else if (reference.startsWith("java.util.concurrent.ConcurrentSkipListSet")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the given type is of any Map's references
	 *
	 * @param reference
	 *            Reference of the selected object
	 * @return returns <code>true</code> if object of map reference .<code>false</code> if object is not of map any references
	 */
	@SuppressWarnings("nls")
	private boolean mapLookup(String reference) {
		if (reference.startsWith("java.util.Hashtable")) {
			return true;
		} else if (reference.startsWith("java.util.HashMap")) {
			return true;
		} else if (reference.startsWith("java.util.Map")) {
			return true;
		} else if (reference.startsWith("java.util.LinkedHashMap")) {
			return true;
		} else if (reference.startsWith("java.util.ConcurrentHashMap")) {
			return true;
		} else if (reference.startsWith("java.util.IdentityHashMap")) {
			return true;
		} else if (reference.startsWith("java.util.WeakHashMap")) {
			return true;
		} else if (reference.startsWith("java.util.Hashtable")) {
			return true;
		}
		return false;
	}

	/**
	 * Displays comparison result for Normal objects
	 *
	 * @param compareResults
	 *            Map containing details of comparison result
	 */
	@SuppressWarnings("nls")
	private void displayComparisonResults(Map<String, Object> compareResults) {
		StringBuilder message = new StringBuilder();
		if (compareResults.get(ObjectComparision.IMMEDIATE_RESULT_KEY) != null) {
			if (compareResults.get(ObjectComparision.RESULT_FAILED) != null) {
				message.append("\n" + compareResults.get(ObjectComparision.RESULT_FAILED));
			} else {
				String s1 = compareResults.get(ObjectComparision.SELECTED_OBJECT_1).toString();
				String s2 = compareResults.get(ObjectComparision.SELECTED_OBJECT_2).toString();
				message.append(s1 + " and " + s2 + " are same");
			}
			MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
			return;
		}
		MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, "Couldn't process");
		return;
	}

	/**
	 * Displays comparison result for List objects
	 *
	 * @param compareResults
	 *            Map containing details of comparison result
	 */
	@SuppressWarnings("nls")
	private void displayListResults(Map<String, Object> compareResults) {
		StringBuilder message = new StringBuilder();
		if (compareResults.get(ObjectComparision.IMMEDIATE_RESULT_KEY) != null) {
			if (compareResults.get(ObjectComparision.RESULT_FAILED) != null) {
				message.append("\n" + compareResults.get(ObjectComparision.RESULT_FAILED));
			} else {
				message.append("\nSelected objects are same");
			}
			MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
			return;
		}
		@SuppressWarnings("unchecked")
		List<String> list1 = (List<String>) compareResults.get(ObjectComparision.VALUESET_1);
		@SuppressWarnings("unchecked")
		List<String> list2 = (List<String>) compareResults.get(ObjectComparision.VALUESET_2);
		String Selection1 = compareResults.get(ObjectComparision.SELECTED_OBJECT_1).toString();
		String Selection2 = compareResults.get(ObjectComparision.SELECTED_OBJECT_2).toString();
		boolean contentDifference = listContentsCheck(list1, list2) && listContentsCheck(list2, list1);
		boolean sizeCheck = list1.size() == list2.size();
		if (contentDifference && sizeCheck) {
			message.append("Selected objects contains same data");
			MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
			return;
		}
		if (!contentDifference && sizeCheck) {
			message.append("Selected objects contains different data");
			List<String> onlyInList1 = new ArrayList<>(list1);
			List<String> onlyInList2 = new ArrayList<>(list2);
			onlyInList1.removeAll(list2);
			onlyInList2.removeAll(list1);
			if (Math.max(onlyInList1.size(), onlyInList2.size()) > DISPLAY_LIMIT) {
				message.append(printListContents(Selection1, onlyInList2));
				message.append(printListContents(Selection2, onlyInList1));
			} else {
				message.append("\n" + Selection1 + " dont includes : " + onlyInList2.toString());
				message.append("\n" + Selection2 + " dont includes : " + onlyInList1.toString());
			}
		}
		if (!sizeCheck) {
			message.append("Selected objects are of different lengths ");
			message.append("\n" + Selection1 + " : " + list1.size() + " in length");
			message.append("\n" + Selection2 + " : " + list2.size() + " in length");
		}
		MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
		return;
	}

	/**
	 * Displays comparison result for Map objects
	 *
	 * @param compareResults
	 *            Map containing details of comparison result
	 */
	@SuppressWarnings("nls")
	private void displayMapResults(Map<String, Object> compareResults) {
		StringBuilder message = new StringBuilder();
		if (compareResults.get(ObjectComparision.IMMEDIATE_RESULT_KEY) != null) {
			if (compareResults.get(ObjectComparision.RESULT_FAILED) != null) {
				message.append("\n" + compareResults.get(ObjectComparision.RESULT_FAILED));
			} else {
				message.append("Selected objects are same");
			}
			MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
			return;
		}
		@SuppressWarnings("unchecked")
		List<String> keysList1 = (List<String>) compareResults.get(ObjectComparision.KEYSET_1);
		@SuppressWarnings("unchecked")
		List<String> keysList2 = (List<String>) compareResults.get(ObjectComparision.KEYSET_2);
		@SuppressWarnings("unchecked")
		List<String> valuesList1 = (List<String>) compareResults.get(ObjectComparision.VALUESET_1);
		@SuppressWarnings("unchecked")
		List<String> valuesList2 = (List<String>) compareResults.get(ObjectComparision.VALUESET_2);
		String Selection1 = compareResults.get(ObjectComparision.SELECTED_OBJECT_1).toString();
		String Selection2 = compareResults.get(ObjectComparision.SELECTED_OBJECT_2).toString();
		boolean keyDifference = listContentsCheck(keysList1, keysList2) && listContentsCheck(keysList2, keysList1);
		boolean valueDifference = listContentsCheck(valuesList1, valuesList2) && listContentsCheck(valuesList2, valuesList1);
		boolean sizeCheck = keysList1.size() == keysList2.size();
		if (keyDifference && valueDifference && sizeCheck) {
			message.append("Selected Maps contains same <Key,Value> pairs");
			MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
			return;
		}
		if (!keyDifference && sizeCheck && valueDifference) {
			message.append("Selected Maps contains different keys but same values");
			List<String> onlyInKeys1 = new ArrayList<>(keysList1);
			List<String> onlyInKeys2 = new ArrayList<>(keysList2);
			onlyInKeys1.removeAll(keysList2);
			onlyInKeys2.removeAll(keysList1);
			if (Math.max(onlyInKeys1.size(), onlyInKeys2.size()) > DISPLAY_LIMIT) {
				message.append(printListContents(Selection1, onlyInKeys2));
				message.append(printListContents(Selection2, onlyInKeys1));
			} else {
				message.append("\n" + Selection1 + " dont includes  [KEYS] : " + onlyInKeys2.toString());
				message.append("\n" + Selection2 + " dont includes  [KEYS] : " + onlyInKeys1.toString());
			}
		}
		if (!valueDifference && sizeCheck && keyDifference) {
			message.append("Selected Maps contains different values but same keys");
			List<String> onlyInValues1 = new ArrayList<>(valuesList1);
			List<String> onlyInValues2 = new ArrayList<>(valuesList2);
			onlyInValues1.removeAll(valuesList2);
			onlyInValues2.removeAll(valuesList1);
			if (keysList1.size() > DISPLAY_LIMIT) {
				message.append(printListContents(Selection1, onlyInValues2));
				message.append(printListContents(Selection2, onlyInValues1));
			} else {
				message.append("\n" + Selection1 + " dont includes  [VALUES] : " + onlyInValues2.toString());
				message.append("\n" + Selection2 + " dont includes  [VALUES] : " + onlyInValues1.toString());
			}
		}
		if (!valueDifference && !keyDifference && sizeCheck) {
			message.append("Selected Maps contains different <Key,Value> pairs");
			List<String> onlyInKeys1 = new ArrayList<>(keysList1);
			List<String> onlyInKeys2 = new ArrayList<>(keysList2);
			List<String> onlyInValues1 = new ArrayList<>(valuesList1);
			List<String> onlyInValues2 = new ArrayList<>(valuesList2);
			if (keysList1.size() > DISPLAY_LIMIT) {
				message.append(printListContents(Selection1, onlyInKeys2));
				message.append(printListContents(Selection2, onlyInValues2));
				message.append(printListContents(Selection1, onlyInKeys1));
				message.append(printListContents(Selection2, onlyInValues1));
			} else {
				message.append("\n" + Selection1 + " dont includes [KEYS] : " + onlyInKeys2.toString());
				message.append("\n" + Selection1 + " dont includes [VALUES] : " + onlyInValues2.toString());
				message.append("\n" + Selection2 + " dont includes [KEYS] : " + onlyInKeys1.toString());
				message.append("\n" + Selection2 + " dont includes [VALUES] : " + onlyInValues1.toString());
			}
		}
		if (!sizeCheck) {
			message.append("Selected Maps contains size difference in  <Key,Value> pairs");
		}
		MessageDialog.openInformation(getShell(), TITLE_NAME_SUCCESS, message.toString());
		return;
	}

	/**
	 * Concatenate list contains do display appropriately
	 *
	 * @param Selection
	 *            Selected object's name
	 * @param list
	 *            List of elements object have
	 * @return returns a modified list of elements in the object
	 */
	@SuppressWarnings("nls")
	private String printListContents(String Selection, List<String> list) {
		StringBuffer content = new StringBuffer();
		int i = 0;
		content.append("\n" + Selection + " dont includes [");
		for (String s : list) {
			content.append(s + ",");
			if (i == DISPLAY_LIMIT) {
				content.deleteCharAt(content.length() - 1);
				content.append("....]");
				break;
			}
			i++;
		}
		if (i != DISPLAY_LIMIT) {
			content.deleteCharAt(content.length() - 1);
			content.append("]");
		}
		return content.toString();
	}

	/**
	 * Displays exception information
	 *
	 * @param message
	 *            Exception details
	 */
	private void displayComparisonFailed(String message) {
		MessageDialog.openError(getShell(), TITLE_NAME_FAILED, message);
	}

	protected Shell getShell() {
		if (fWindow != null) {
			return fWindow.getShell();
		}
		if (getWorkbenchWindow() != null) {
			return getWorkbenchWindow().getShell();
		}
		return null;
	}
}
