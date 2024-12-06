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
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.CompareElementsEditor;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.ObjectComparison;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class CompareObjectsAction extends ObjectActionDelegate implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow fWindow;
	private static final String EDITOR_ID = "org.eclipse.jdt.debug.compareElementsEditor"; //$NON-NLS-1$

	private String message;

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
	 * Parent method for comparing selected objects from variables view
	 *
	 * @param selections
	 *            Selected objects from variable view
	 */
	@SuppressWarnings("nls")
	protected void compareSelectedObjects(List<IStructuredSelection> selections) {
		ObjectComparison obcp = new ObjectComparison();
		Map<IJavaVariable, Object> result = new HashMap<>();
		String temp;
		try {
			Set<String> valueSet = selectionTypeChecker(selections);
			if (valueSet.size() != 1) {
				displayComparisonFailed(DebugUIMessages.DifferentDataStructures);
				return;
			}
			temp = (String) valueSet.toArray()[0];
			if (temp.equals("List")) {
				result = obcp.listExtraction(selections);
				result = obcp.compareSelectedLists(result);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Set")) {
				result = obcp.setExtraction(selections);
				result = obcp.compareSelectedLists(result);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Map")) {
				result = obcp.mapExtraction(selections);
				result = obcp.compareSelectedMaps(result);
				if (selections.size() == 2) {
					displayMapResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Arrays")) {
				result = obcp.arrayExtraction(selections);
				result = obcp.compareSelectedLists(result);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("String")) {
				result = obcp.stringExtraction(selections);
				result = obcp.stringCompare(result);
				if (selections.size() == 2) {
					String type = temp.substring(temp.lastIndexOf('.') + 1);
					displayComparisonResults(result, type);
					return;
				}
				displayInDiffView(result, temp);
			} else if (wrapperLookup(temp)) {
				result = obcp.extractOtherObjects(selections);
				result = obcp.compareObjects(result);
				if (selections.size() == 2) {
					String type = temp.substring(temp.lastIndexOf('.') + 1);
					displayComparisonResults(result, type);
					return;
				}
				displayInDiffView(result, temp);
			} else {
				result = obcp.customObjectsReferencesExtraction(selections);
				if (obcp.objectsRefCheck(result)) {
					displayComparisonResults();
					return;
				}
				result = obcp.extractCustomObjects(selections);
				result = obcp.compareCustomObjects(result);
				displayInDiffViewForComplex(result);
			}

		} catch (Exception e) {
			displayComparisonFailed(e.getMessage());
			return;
		}
	}

	/**
	 * returns the interface or java type of selected objects
	 *
	 * @param selectedObject
	 *            IJavaVariable object of selected item
	 * @return returns name of the interface or java type
	 * @throws DebugException
	 */

	@SuppressWarnings("nls")
	private String comparisonTypeCheck(IJavaVariable selectedObject) throws DebugException {
		String valueReferenceType = selectedObject.getValue().getReferenceTypeName();
		String declarationReferenceType = selectedObject.getReferenceTypeName();
		if (mapLookup(valueReferenceType) || mapLookup(declarationReferenceType)) {
			return "Map";
		} else if (stringLookup(declarationReferenceType)) {
			return "String";
		} else if (listLookup(declarationReferenceType) || listLookup(valueReferenceType)) {
			return "List";
		} else if (setLookup(valueReferenceType) || setLookup(declarationReferenceType)) {
			return "Set";
		} else if (declarationReferenceType.endsWith("[]")) {
			return "Arrays";
		}
		return valueReferenceType;
	}


	/**
	 * parent method for checking interface or java type of selected objects
	 *
	 * @param selections
	 *            List of selections
	 * @return returns Set containing types of selected objects
	 * @throws DebugException
	 */
	private Set<String> selectionTypeChecker(List<IStructuredSelection> selections) throws DebugException {
		String temp;
		Map<String, String> selectionTypeCheck = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				temp = comparisonTypeCheck(selectedObject);
				selectionTypeCheck.put(selectedObject.getName(), temp);
			}
		}
		Set<String> valueSet = Set.copyOf(selectionTypeCheck.values());
		return valueSet;
	}

	/**
	 * Display comparison result in custom diff view
	 *
	 * @param result
	 *            Map containing variables and their comparison results
	 * @param type
	 *            Type of the objects
	 * @throws DebugException
	 * @throws PartInitException
	 */
	private void displayInDiffView(Map<IJavaVariable, Object> result, String type) throws DebugException, PartInitException {
		List<Map<String, Object>> resultsList = new ArrayList<>();
		List<String> objectsName = new ArrayList<>();
		for (Entry<IJavaVariable, Object> mp : result.entrySet()) {
			Map<String, Object> temp = new HashMap<>();
			temp.put(mp.getKey().getName(), mp.getValue());
			resultsList.add(temp);
			objectsName.add(mp.getKey().getName());
		}
		CompareElementsEditor cd = new CompareElementsEditor(resultsList, objectsName, type);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(cd, EDITOR_ID);
	}

	/**
	 * Display comparison result in custom diff view for complex objects
	 *
	 * @param result
	 *            Map containing variables and their comparison results
	 * @param type
	 *            Type of the objects
	 * @throws DebugException
	 * @throws PartInitException
	 */
	private void displayInDiffViewForComplex(Map<IJavaVariable, Object> result) throws DebugException, PartInitException {
		List<Map<String, Object>> resultsList = new ArrayList<>();
		List<String> objectsName = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		IJavaVariable javaVar = null;
		for (Entry<IJavaVariable, Object> mp : result.entrySet()) {
			Map<String, Object> temp = new HashMap<>();
			temp.put(mp.getKey().getName(), mp.getValue());
			resultsList.add(temp);
			javaVar = mp.getKey();
			objectsName.add(mp.getKey().getName());
		}
		for (IVariable x : javaVar.getValue().getVariables()) {
			fieldNames.add(x.getName());
		}
		if (fieldNames.isEmpty()) {
			fieldNames.add(DebugUIMessages.CompareObjectsReference);
		}
		CompareElementsEditor cd = new CompareElementsEditor(resultsList, objectsName, "custom", fieldNames); //$NON-NLS-1$
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(cd, EDITOR_ID);
	}

	/**
	 * Displays comparison result for Normal objects String objects in dialog box
	 *
	 * @param result
	 *            Map containing details of comparison result
	 * @param type
	 *            Type of the objects
	 * @throws DebugException
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	private void displayComparisonResults(Map<IJavaVariable, Object> result, String type) throws DebugException {
		List<IJavaVariable> selections = new ArrayList<>();
		for (Map.Entry<IJavaVariable, Object> current : result.entrySet()) {
			IJavaVariable c = current.getKey();
			selections.add(c);
		}
		Map<String, Object> obj1 = (Map<String, Object>) result.get(selections.get(0));
		if (obj1.containsKey(ObjectComparison.IMMEDIATE_RESULT_KEY)) {
			String msg = obj1.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
			if (msg.contains("Same")) {
				message = NLS.bind(DebugUIMessages.DialogBoxObjectsSame, new Object[] { type });
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxObjectsDifferent, new Object[] { type });
			}
			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, message);
			return;
		}
		message = NLS.bind(DebugUIMessages.CompareObjectsFailedException, new Object[] { type });
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_1, message);
		return;
	}

	private void displayComparisonResults() {
		message = DebugUIMessages.ObjectsSame;
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, message);
		return;
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
		} else if (reference.startsWith("java.util.SortedSet")) {
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

	@SuppressWarnings("nls")
	private boolean stringLookup(String reference) {
		if (reference.equals("java.lang.String")) {
			return true;
		} else if (reference.equals("java.lang.StringBuffer")) {
			return true;
		} else if (reference.equals("java.lang.StringBuilder")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the given type is of any Java Wrapper types
	 *
	 * @param reference
	 *            Reference of the selected object
	 * @return returns <code>true</code> if object of Wrapper reference .<code>false</code> if object is not of Wrapper references
	 */

	@SuppressWarnings("nls")
	private boolean wrapperLookup(String reference) {
		if (reference.equals("java.lang.Boolean")) {
			return true;
		} else if (reference.equals("java.lang.Byte")) {
			return true;
		} else if (reference.equals("java.lang.Character")) {
			return true;
		} else if (reference.equals("java.lang.Double")) {
			return true;
		} else if (reference.equals("java.lang.Float")) {
			return true;
		} else if (reference.equals("java.lang.Integer")) {
			return true;
		} else if (reference.equals("java.lang.Long")) {
			return true;
		} else if (reference.equals("java.lang.Short")) {
			return true;
		}
		return false;
	}

	/**
	 * Displays comparison result for List objects in dialog box
	 *
	 * @param result
	 *            Map containing details of comparison result
	 * @throws DebugException
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	private void displayListResultsInDialogBox(Map<IJavaVariable, Object> result) throws DebugException {
		int dispLimit = 20;
		StringBuilder outputMessage = new StringBuilder();
		List<IJavaVariable> selections = new ArrayList<>();
		for (Map.Entry<IJavaVariable, Object> current : result.entrySet()) {
			IJavaVariable c = current.getKey();
			selections.add(c);
		}
		Map<String, Object> obj1 = (Map<String, Object>) result.get(selections.get(0));
		Map<String, Object> obj2 = (Map<String, Object>) result.get(selections.get(1));

		String size1 = obj1.get(ObjectComparison.ELEMENT_SIZE).toString();
		String size2 = obj2.get(ObjectComparison.ELEMENT_SIZE).toString();
		if (obj1.get(ObjectComparison.IMMEDIATE_RESULT_KEY) != null || obj2.get(ObjectComparison.IMMEDIATE_RESULT_KEY) != null) {
			outputMessage.append(obj1.get(ObjectComparison.IMMEDIATE_RESULT_KEY));
			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, outputMessage.toString());
			return;
		}
		String Selection1 = selections.get(0).getName().toString();
		String Selection2 = selections.get(1).getName().toString();
		if (!size1.equals(size2)) {
			message = NLS.bind(DebugUIMessages.DialogBoxListItemCount, new Object[] { Selection1, size1 });
			outputMessage.append(message + "\n");
			message = NLS.bind(DebugUIMessages.DialogBoxListItemCount, new Object[] { Selection2, size2 });
			outputMessage.append(message);
		}
		if (obj1.containsKey(ObjectComparison.OBJECT_VALUES)) {
			List<String> missingValues = (List<String>) obj1.get(ObjectComparison.OBJECT_VALUES);
			if (missingValues.size() > dispLimit) {
				outputMessage.append(printListContents(Selection1, missingValues));
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxListMissing, new Object[] { Selection1, missingValues.toString() });
				outputMessage.append("\n" + message);
			}
		}
		if (obj2.containsKey(ObjectComparison.OBJECT_VALUES)) {
			List<String> missingValues = (List<String>) obj2.get(ObjectComparison.OBJECT_VALUES);
			if (missingValues.size() > dispLimit) {
				outputMessage.append(printListContents(Selection2, missingValues));
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxListMissing, new Object[] { Selection2, missingValues.toString() });
				outputMessage.append("\n" + message);
			}
		}
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, outputMessage.toString());
		return;
	}

	/**
	 * Displays comparison result for Map objects in dialog box
	 *
	 * @param result
	 *            Map containing details of comparison result
	 * @throws DebugException
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	private void displayMapResultsInDialogBox(Map<IJavaVariable, Object> result) throws DebugException {
		int dispLimit = 20;
		StringBuilder outputMessage = new StringBuilder();
		List<IJavaVariable> selections = new ArrayList<>();
		for (Map.Entry<IJavaVariable, Object> current : result.entrySet()) {
			IJavaVariable c = current.getKey();
			selections.add(c);
		}
		Map<String, Object> obj1 = (Map<String, Object>) result.get(selections.get(0));
		Map<String, Object> obj2 = (Map<String, Object>) result.get(selections.get(1));

		String Selection1 = selections.get(0).getName().toString();
		String Selection2 = selections.get(1).getName().toString();
		List<String> tempList;
		if (obj1.containsKey(ObjectComparison.MAP_KEY_SAME) && obj1.containsKey(ObjectComparison.MAP_VAL_SAME)
				|| obj2.containsKey(ObjectComparison.MAP_KEY_SAME) && obj2.containsKey(ObjectComparison.MAP_VAL_SAME)) {

			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, DebugUIMessages.DialogBoxMapSame);
			return;
		}
		if (obj1.containsKey(ObjectComparison.MAP_KEY_SAME) || obj2.containsKey(ObjectComparison.MAP_KEY_SAME)) {
			outputMessage.append(DebugUIMessages.DialogBoxMapKeySame + "\n");
		}
		if (obj1.containsKey(ObjectComparison.MAP_VAL_SAME) || obj2.containsKey(ObjectComparison.MAP_VAL_SAME)) {
			outputMessage.append(DebugUIMessages.DialogBoxMapValSame + "\n");
		}
		if (obj1.containsKey("MapValues")) {
			tempList = (List<String>) obj1.get("MapValues");
			if (tempList.size() > dispLimit) {
				outputMessage.append(printListContents(Selection1, tempList) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingVal, new Object[] { Selection1, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj2.containsKey("MapValues")) {
			tempList = (List<String>) obj2.get("MapValues");
			if (tempList.size() > dispLimit) {
				outputMessage.append(printListContents(Selection2, tempList) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingVal, new Object[] { Selection2, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj1.containsKey("MapKeys")) {
			tempList = (List<String>) obj1.get("MapKeys");
			if (tempList.size() > dispLimit) {
				outputMessage.append(printListContents(Selection1, tempList) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingKey, new Object[] { Selection1, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj2.containsKey("MapKeys")) {
			tempList = (List<String>) obj2.get("MapKeys");
			if (tempList.size() > dispLimit) {
				outputMessage.append(printListContents(Selection2, tempList) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingKey, new Object[] { Selection2, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}

		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_0, outputMessage.toString());
		return;
	}


	/**
	 * Concatenate list contents to display appropriately in dialoge box
	 *
	 * @param Selection
	 *            Selected object's name
	 * @param list
	 *            List of elements object have
	 * @return returns a modified list of elements in the object
	 */
	@SuppressWarnings("nls")
	private String printListContents(String Selection, List<String> list) {
		int dispLimit = 20;
		StringBuffer content = new StringBuffer();
		int i = 0;
		content.append("\n" + Selection + " does not include [");
		for (String s : list) {
			content.append(s + ",");
			if (i == dispLimit) {
				content.deleteCharAt(content.length() - 1);
				content.append("....]");
				break;
			}
			i++;
		}
		if (i != dispLimit) {
			content.deleteCharAt(content.length() - 1);
			content.append("]");
		}
		return content.toString();
	}

	/**
	 * Displays exception and failure information
	 *
	 * @param message
	 *            Exception details
	 */
	private void displayComparisonFailed(String message) {
		MessageDialog.openError(getShell(), DebugUIMessages.ObjectComparisonTitle_1, message);
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
