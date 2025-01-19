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
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
		try {
			Set<String> valueSet = selectionTypeChecker(selections);
			if (valueSet.size() != 1) {
				displayComparisonFailure(DebugUIMessages.DifferentDataStructures);
				return;
			}
			String temp = (String) valueSet.toArray()[0];
			if (temp.equals("Lists")) {
				result = obcp.listExtraction(selections);
				result = obcp.compareSelectedLists(result, temp);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Sets")) {
				result = obcp.setExtraction(selections);
				result = obcp.compareSelectedLists(result, temp);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Queues") || temp.equals("Deques")) {
				result = obcp.listExtraction(selections);
				result = obcp.compareSelectedLists(result, temp);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Maps")) {
				result = obcp.mapExtraction(selections);
				result = obcp.compareSelectedMaps(result);
				if (selections.size() == 2) {
					displayMapResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Iterables")) {
				result = obcp.iterableExtraction(selections);
				result = obcp.compareSelectedLists(result, temp);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Arrays")) {
				result = obcp.arrayExtraction(selections);
				result = obcp.compareSelectedLists(result, temp);
				if (selections.size() == 2) {
					displayListResultsInDialogBox(result);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("CharSequences")) {
				result = obcp.stringExtraction(selections);
				result = obcp.stringCompare(result);
				if (selections.size() == 2) {
					String type = temp.substring(temp.lastIndexOf('.') + 1);
					displayComparisonResults(result, type);
					return;
				}
				displayInDiffView(result, temp);
			} else if (temp.equals("Wrappers")) {
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
			displayComparisonFailed(DebugUIMessages.CompareObjectsFailedException);
			DebugUIPlugin.log(e);
			return;
		}
	}

	/**
	 * Returns the interface or java type of selected objects
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
		if (declarationReferenceType.endsWith("[]")) {
			return "Arrays";
		}
		String interfaceType = ObjectComparison.checkInterfaces(valueReferenceType);
		return interfaceType;
	}


	/**
	 * Parent method for checking interface or java type of selected objects
	 *
	 * @param selections
	 *            List of selections
	 * @return returns Set containing types of selected objects
	 * @throws DebugException
	 */
	private Set<String> selectionTypeChecker(List<IStructuredSelection> selections) throws DebugException {
		Map<String, String> selectionTypeCheck = new HashMap<>();
		for (Object selection : selections) {
			if (selection instanceof IJavaVariable selectedObject) {
				String temp = comparisonTypeCheck(selectedObject);
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
		String message;
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
			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, message);
			return;
		}
		message = NLS.bind(DebugUIMessages.CompareObjectsFailed, new Object[] { type });
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_failed, message);
		return;
	}

	private void displayComparisonResults() {
		String message = DebugUIMessages.ObjectsSame;
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, message);
		return;
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
		int displayLimit = 30;
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
			String msg = obj1.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
			if (msg.indexOf("as") != -1 && msg.indexOf(",") != -1) {
				String msgRemove = msg.substring(msg.indexOf("as") - 1, msg.indexOf(","));
				msg = msg.replace(msgRemove, "");
			}
			if (msg.contains("same as of") && msg.indexOf(",") == -1) {
				msg = NLS.bind(DebugUIMessages.ListSameElementsFor2, new Object[] { msg.split(" ")[0] });
			}
			outputMessage.append(msg);
			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, outputMessage.toString());
			return;
		}
		String selection1 = selections.get(0).getName().toString();
		String selection2 = selections.get(1).getName().toString();
		String message;
		if (!size1.equals(size2)) {
			message = NLS.bind(DebugUIMessages.DialogBoxListItemCount, new Object[] { selection1, size1 });
			outputMessage.append(message + "\n");
			message = NLS.bind(DebugUIMessages.DialogBoxListItemCount, new Object[] { selection2, size2 });
			outputMessage.append(message);
		}
		if (obj1.containsKey(ObjectComparison.OBJECT_VALUES)) {
			List<String> missingValues = (List<String>) obj1.get(ObjectComparison.OBJECT_VALUES);
			if (missingValues.size() > displayLimit) {
				outputMessage.append(printListContents(selection1, missingValues, displayLimit));
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxListMissing, new Object[] { selection1, missingValues.toString() });
				outputMessage.append("\n" + message);
			}
		}
		if (obj2.containsKey(ObjectComparison.OBJECT_VALUES)) {
			List<String> missingValues = (List<String>) obj2.get(ObjectComparison.OBJECT_VALUES);
			if (missingValues.size() > displayLimit) {
				outputMessage.append(printListContents(selection2, missingValues, displayLimit));
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxListMissing, new Object[] { selection2, missingValues.toString() });
				outputMessage.append("\n" + message);
			}
		}
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, outputMessage.toString());
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
		int displayLimit = 30;
		StringBuilder outputMessage = new StringBuilder();
		List<IJavaVariable> selections = new ArrayList<>();
		for (Map.Entry<IJavaVariable, Object> current : result.entrySet()) {
			IJavaVariable c = current.getKey();
			selections.add(c);
		}
		Map<String, Object> obj1 = (Map<String, Object>) result.get(selections.get(0));
		Map<String, Object> obj2 = (Map<String, Object>) result.get(selections.get(1));

		String selection1 = selections.get(0).getName().toString();
		String selection2 = selections.get(1).getName().toString();
		List<String> tempList;
		if (obj1.containsKey(ObjectComparison.MAP_KEY_SAME) && obj1.containsKey(ObjectComparison.MAP_VAL_SAME)
				|| obj2.containsKey(ObjectComparison.MAP_KEY_SAME) && obj2.containsKey(ObjectComparison.MAP_VAL_SAME)) {

			MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, DebugUIMessages.DialogBoxMapSame);
			return;
		}
		if (obj1.containsKey(ObjectComparison.MAP_KEY_SAME) || obj2.containsKey(ObjectComparison.MAP_KEY_SAME)) {
			outputMessage.append(DebugUIMessages.DialogBoxMapKeySame + "\n");
		}
		if (obj1.containsKey(ObjectComparison.MAP_VAL_SAME) || obj2.containsKey(ObjectComparison.MAP_VAL_SAME)) {
			outputMessage.append(DebugUIMessages.DialogBoxMapValSame + "\n");
		}
		String message;
		if (obj1.containsKey("MapValues")) {
			tempList = (List<String>) obj1.get("MapValues");
			if (tempList.size() > displayLimit) {
				outputMessage.append(printListContents(selection1, tempList, displayLimit) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingVal, new Object[] { selection1, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj2.containsKey("MapValues")) {
			tempList = (List<String>) obj2.get("MapValues");
			if (tempList.size() > displayLimit) {
				outputMessage.append(printListContents(selection2, tempList, displayLimit) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingVal, new Object[] { selection2, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj1.containsKey("MapKeys")) {
			tempList = (List<String>) obj1.get("MapKeys");
			if (tempList.size() > displayLimit) {
				outputMessage.append(printListContents(selection1, tempList, displayLimit) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingKey, new Object[] { selection1, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}
		if (obj2.containsKey("MapKeys")) {
			tempList = (List<String>) obj2.get("MapKeys");
			if (tempList.size() > displayLimit) {
				outputMessage.append(printListContents(selection2, tempList, displayLimit) + "\n");
			} else {
				message = NLS.bind(DebugUIMessages.DialogBoxMapMissingKey, new Object[] { selection2, tempList.toString() });
				outputMessage.append(message + "\n");
			}
		}

		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_long, outputMessage.toString());
		return;
	}

	/**
	 * Concatenate list contents to display appropriately in dialog box
	 *
	 * @param Selection
	 *            Selected object's name
	 * @param list
	 *            List of elements object have
	 * @param displayLimit
	 *            Number of elements shown in dialog box
	 * @return returns a modified list of elements in the object
	 */
	@SuppressWarnings("nls")
	private String printListContents(String Selection, List<String> list, int displayLimit) {
		StringBuilder content = new StringBuilder();
		content.append("[");
		content.append(list.stream().limit(displayLimit).collect(Collectors.joining(",")));
		content.append("......]");
		String message = NLS.bind(DebugUIMessages.DialogBoxMapMissingKey, new Object[] { Selection, content.toString() });
		return message;
	}

	/**
	 * Displays exception and failure information
	 *
	 * @param message
	 *            Exception details
	 */
	private void displayComparisonFailed(String message) {
		MessageDialog.openError(getShell(), DebugUIMessages.ObjectComparisonTitle_failed, message);
	}

	/**
	 * Displays failure cases
	 *
	 * @param message
	 *            Exception details
	 */
	private void displayComparisonFailure(String message) {
		MessageDialog.openInformation(getShell(), DebugUIMessages.ObjectComparisonTitle_failed, message);
	}

	protected Shell getShell() {
		if (fWindow != null) {
			return fWindow.getShell();
		}
		IWorkbenchWindow win = getWorkbenchWindow();
		if (win != null) {
			return win.getShell();
		}
		return null;
	}
}
