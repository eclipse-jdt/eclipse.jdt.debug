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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class CompareElementsDiffView extends EditorPart implements IEditorInput {

	private List<Map<String, Object>> resultsList;
	private List<String> objectsName;

	private List<String> fieldName;

	private String elementsType;

	private Font fnt1;
	private Font fnt2;
	private FontData fntData;

	private String size;

	private String refType;
	/**
	 * Text widgets used for this editor
	 */
	public Text fText;

	/**
	 * Launch listener to handle launch events, or <code>null</code> if none
	 */
	private ILaunchesListener2 fLaunchesListener;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	public CompareElementsDiffView() {
		resultsList = new ArrayList<>();
		objectsName = new ArrayList<>();
	}
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@SuppressWarnings({ "nls", "unchecked" })
	@Override
	public void createPartControl(Composite parent) {
		GridLayout topLayout = new GridLayout();
		GridData data = new GridData();
		topLayout.numColumns = 1;
		topLayout.verticalSpacing = 20;
		parent.setLayout(topLayout);
		parent.setLayoutData(data);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		Display display = Display.getDefault();
		Label l = new Label(parent, SWT.None);
		fntData = new FontData("Arial", 17, SWT.BOLD);
		fnt1 = new Font(parent.getDisplay(), fntData);
		l.setText("\n" + DebugUIMessages.ComparisionMultiHeader);
		l.setFont(fnt1);
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		Color rowColor = null;
		Color cellColor = null;
		Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		fntData = new FontData("Arial", 17, SWT.NORMAL);
		fnt2 = new Font(parent.getDisplay(), fntData);
		table.setFont(fnt2);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(tableData);
		List<String> headers = headerSelection(elementsType);
		for (String header : headers) {
			TableColumn column = new TableColumn(table, SWT.BOLD);
			column.setText(header);
			column.setWidth(200);
			column.setResizable(true);
			column.setAlignment(SWT.CENTER);
		}
		for (int i = 0; i < resultsList.size(); i++) {
			Map<String, Object> temp = resultsList.get(i);
			TableItem row = new TableItem(table, SWT.NONE);
			Map<String, Object> contentsToShow = (Map<String, Object>) temp.get(objectsName.get(i));
			String elements = "";
			if (contentsToShow.containsKey(ObjectComparison.ELEMENT_SIZE)) {
				size = contentsToShow.get(ObjectComparison.ELEMENT_SIZE).toString();
			}
			if (contentsToShow.containsKey(ObjectComparison.OBJECT_TYPE)) {
				refType = contentsToShow.get(ObjectComparison.OBJECT_TYPE).toString();
			}
			if (elementsType.equals("Set") || elementsType.equals("List") || elementsType.equals("Arrays")) {
				if (!contentsToShow.containsKey("Values") && contentsToShow.containsKey(ObjectComparison.IMMEDIATE_RESULT_KEY)) {
					elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else if (contentsToShow.containsKey("Values")) {
					String missingValExtracted = contentsToShow.get("MultiValues").toString();
					elements = missingValExtracted.substring(1, missingValExtracted.length() - 1);
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);

				}
				row.setText(new String[] { i + 1 + "", objectsName.get(i), elements, size, refType });
				row.setBackground(rowColor);
			} else if (elementsType.equals("String")) {
				elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
				if (elements.contains("Same")) {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				row.setText(new String[] { i + 1 + "", objectsName.get(i), elements, size, refType });
				row.setBackground(rowColor);
			} else if (elementsType.equals("Map")) {
				String forKey = null;
				String forVal = null;
				if (contentsToShow.containsKey(ObjectComparison.MAP_KEY_SAME)) {
					forKey = contentsToShow.get(ObjectComparison.MAP_KEY_SAME).toString();
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				if (contentsToShow.containsKey(ObjectComparison.MAP_VAL_SAME)) {
					forVal = contentsToShow.get(ObjectComparison.MAP_VAL_SAME).toString();
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				if (contentsToShow.containsKey("MapValues")) {
					String missingValExtracted = contentsToShow.get("MultiMapValues").toString();
					forVal = missingValExtracted.substring(1, missingValExtracted.length() - 1);
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				if (contentsToShow.containsKey("MapKeys")) {
					String missingKeyExtracted = contentsToShow.get("MultiMapKeys").toString();
					forKey = missingKeyExtracted.substring(1, missingKeyExtracted.length() - 1);
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				if (forVal == null) {
					forVal = "None";
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				if (forKey == null) {
					forKey = "None";
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				row.setText(new String[] { i + 1 + "", objectsName.get(i), forKey, forVal, size, refType });
				row.setBackground(rowColor);
				if (forVal != null && forVal.contains("same")) {
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
					row.setBackground(3, cellColor);
				}
				if (forKey != null && forKey.contains("same")) {
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
					row.setBackground(2, cellColor);
				}
			} else if (elementsType.equals("custom")) {
				String[] con = new String[fieldName.size()];
				if (contentsToShow.containsKey("fields")) {
					Map<String, String> field = (Map<String, String>) contentsToShow.get("fields");
					con[0] = i + 1 + "";
					con[1] = objectsName.get(i);
					for (int j = 2; j < con.length; j++) {
						con[j] = field.get(fieldName.get(j));
						if (field.get(fieldName.get(j)).contains("Same") && !field.get(fieldName.get(j)).contains("diffe")) {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
						} else {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_RED);
						}
						row.setBackground(j, cellColor);
					}
					row.setText(con);
				} else {
					Set<String> same = (Set<String>) contentsToShow.get("REF_SAME");
					Set<String> diff = (Set<String>) contentsToShow.get("REF_DIFF");
					con[0] = i + 1 + "";
					con[1] = objectsName.get(i);
					for (int j = 2; j < con.length; j++) {
						if (!same.isEmpty()) {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
							con[j] = NLS.bind(DebugUIMessages.ObjectsReferenceSameAndDifferent, new Object[] { same.toString(), diff.toString() });
						} else {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_RED);
							con[j] = NLS.bind(DebugUIMessages.ObjectsReferenceDifferent, diff.toString());
						}
					}
					row.setText(con);
					row.setBackground(cellColor);
				}

			} else {
				elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
				if (elements.contains("Same")) {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				row.setText(new String[] { i + 1 + "", objectsName.get(i), elements, refType });
				row.setBackground(rowColor);
			}

		}
		for (TableColumn column : table.getColumns()) {
			column.pack();
		}
		Dialog.applyDialogFont(parent);
	}

	/**
	 * Choose the appropriate column headers according to the specified type
	 *
	 * @param type
	 *            Type of the objects
	 * @return Returns a List of column values
	 */
	@SuppressWarnings("nls")
	private List<String> headerSelection(String type) {

		if (type.equals("Set") || type.equals("List") || type.equals("Arrays")) {
			return List.of("SL No.", "Variable Name", "Missing Elements", "Size", "Type");

		} else if (type.equals("Map")) {
			return List.of("SL No.", "Variable Name", "Missing Keys", "Missing Values", "<K,V> Size", "Type");

		} else if (type.equals("String")) {
			return List.of("SL No.", "Variable Name", "String Comparision result", "Characters", "Type");

		} else if (type.equals("custom")) {
			fieldName.add(0, "Variable Name");
			fieldName.add(0, "SL No.");
			return fieldName;
		} else {
			return List.of("SL No.", "Variable Name", "Comparision result", "Type");
		}
	}

	@Override
	public void setFocus() {
		if (fText != null) {
			fText.setFocus();
		}
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		if (fText != null) {
			fText.setText(getText());
		}

		firePropertyChange(PROP_INPUT);
		if (input instanceof CompareElementsEditor inp2) {
			resultsList = inp2.resultsList;
			objectsName = inp2.objectsName;
			elementsType = inp2.elementsType;
			fieldName = inp2.fieldNames;
		}
	}

	protected String getText() {
		return getEditorInput().getName() + "\n"; //$NON-NLS-1$
	}

	/**
	 * Closes this editor.
	 */
	protected void closeEditor() {
		dispose();
		final IEditorPart editor = this;
		DebugUIPlugin.getStandardDisplay().syncExec(() -> {
			IWorkbenchWindow activeWorkbenchWindow = DebugUIPlugin.getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					activePage.closeEditor(editor, false);
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		if (fnt1 != null && !fnt1.isDisposed()) {
			fnt1.dispose();
		}
		if (fnt2 != null && !fnt2.isDisposed()) {
			fnt2.dispose();
		}
		getSite().getPage().closeEditor(this, false);
		if (fLaunchesListener != null) {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchesListener);
		}
	}

	/**
	 * Initialize this editor.
	 */
	protected void initialize() {
		fLaunchesListener = new ILaunchesListener2() {
			@Override
			public void launchesTerminated(ILaunch[] launches) {
				Object artifact = getArtifact();
				if (artifact instanceof IDebugElement element) {
					for (ILaunch launch : launches) {
						if (launch.equals(element.getLaunch())) {
							closeEditor();
							return;
						}
					}
				}
			}

			@Override
			public void launchesRemoved(ILaunch[] launches) {
				launchesTerminated(launches);
			}

			@Override
			public void launchesAdded(ILaunch[] launches) {
			}

			@Override
			public void launchesChanged(ILaunch[] launches) {
			}
		};

		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchesListener);
	}

	protected Object getArtifact() {
		IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof CompareElementsDiffView input) {
			return input.getArtifact();
		}
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}
}
