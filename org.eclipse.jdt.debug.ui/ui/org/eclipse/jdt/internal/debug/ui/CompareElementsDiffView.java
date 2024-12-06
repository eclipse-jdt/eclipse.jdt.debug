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

/**
 * Class provides custom difference viewer for n selected objects
 *
 */
public class CompareElementsDiffView extends EditorPart implements IEditorInput {

	private List<Map<String, Object>> results;
	private List<String> objectNames;

	private List<String> fieldNames;

	private String elementTypeName;

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
		results = new ArrayList<>();
		objectNames = new ArrayList<>();
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
		List<String> headers = headerSelection(elementTypeName);
		for (String header : headers) {
			TableColumn column = new TableColumn(table, SWT.BOLD);
			column.setText(header);
			column.setWidth(200);
			column.setResizable(true);
			column.setAlignment(SWT.CENTER);
		}
		for (int i = 0; i < results.size(); i++) {
			Map<String, Object> temp = results.get(i);
			TableItem row = new TableItem(table, SWT.NONE);
			Map<String, Object> contentsToShow = (Map<String, Object>) temp.get(objectNames.get(i));
			String elements = "";
			if (contentsToShow.containsKey(ObjectComparison.ELEMENT_SIZE)) {
				size = contentsToShow.get(ObjectComparison.ELEMENT_SIZE).toString();
			}
			if (contentsToShow.containsKey(ObjectComparison.OBJECT_TYPE)) {
				refType = contentsToShow.get(ObjectComparison.OBJECT_TYPE).toString();
			}
			if (elementTypeName.equals("Sets") || elementTypeName.equals("Lists") || elementTypeName.equals("Arrays")
					|| elementTypeName.equals("Queues") || elementTypeName.equals("Deques") || elementTypeName.equals("Iterables")) {
				if (!contentsToShow.containsKey("Values") && contentsToShow.containsKey(ObjectComparison.IMMEDIATE_RESULT_KEY)) {
					elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else if (contentsToShow.containsKey("Values")) {
					String missingValExtracted = contentsToShow.get("MultiValues").toString();
					elements = "Missing contents from ";
					elements = elements + missingValExtracted.substring(1, missingValExtracted.length() - 1);
					elements = elements.replace('[', '{');
					elements = elements.replace(']', '}');
					elements = elements.replace("=", "-");
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				} else {
					elements = "Contains every element of others";
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				int count = i + 1;
				String SlNo = "\t " + count;
				elements = replaceToSingular(elements);
				row.setText(new String[] { SlNo, objectNames.get(i), elements, size, refType });
				row.setBackground(rowColor);
				row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			} else if (elementTypeName.equals("CharSequences")) {
				elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
				if (elements.contains("Same")) {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				int count = i + 1;
				String SlNo = "\t " + count;
				row.setText(new String[] { SlNo, objectNames.get(i), elements, size, refType });
				row.setBackground(rowColor);
				row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			} else if (elementTypeName.equals("Maps")) {
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
					forVal = "Missing values : " + missingValExtracted.substring(1, missingValExtracted.length() - 1);
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				if (contentsToShow.containsKey("MapKeys")) {
					String missingKeyExtracted = contentsToShow.get("MultiMapKeys").toString();
					forKey = "Missing keys : " + missingKeyExtracted.substring(1, missingKeyExtracted.length() - 1);
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				if (forVal == null) {
					forVal = "Contains every value of others";
				}
				if (forKey == null) {
					forKey = "Contains every key of others";
				}
				int count = i + 1;
				String SlNo = "\t " + count;
				row.setText(new String[] { SlNo, objectNames.get(i), forKey, forVal, size, refType });
				row.setBackground(rowColor);
				if (forVal.contains("Contains")) {
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
					row.setBackground(3, cellColor);
				}
				if (forKey.contains("Contains")) {
					cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
					row.setBackground(2, cellColor);
				}
				if (forKey.contains("Contains") && forVal.contains("Contains")) {
					row.setBackground(cellColor);
				}
				row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
			} else if (elementTypeName.equals("custom")) {
				String[] con = new String[fieldNames.size()];
				if (contentsToShow.containsKey("fields")) {
					Map<String, String> field = (Map<String, String>) contentsToShow.get("fields");
					int count = i + 1;
					String SlNo = "\t " + count;
					con[0] = SlNo;
					con[1] = objectNames.get(i);
					for (int j = 2; j < con.length; j++) {
						con[j] = field.get(fieldNames.get(j));
						if (field.get(fieldNames.get(j)).contains("Same") && !field.get(fieldNames.get(j)).contains("diffe")) {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
						} else {
							cellColor = display.getSystemColor(SWT.COLOR_DARK_RED);
						}
						row.setBackground(j, cellColor);
					}
					row.setText(con);
					row.setBackground(cellColor);
					row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
				} else {
					int count = i + 1;
					String SlNo = "\t " + count;
					Set<String> same = (Set<String>) contentsToShow.get("REF_SAME");
					Set<String> diff = (Set<String>) contentsToShow.get("REF_DIFF");
					con[0] = SlNo;
					con[1] = objectNames.get(i);
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
					row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
				}
			} else {
				elements = contentsToShow.get(ObjectComparison.IMMEDIATE_RESULT_KEY).toString();
				if (elements.contains("Same")) {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
				} else {
					rowColor = display.getSystemColor(SWT.COLOR_DARK_RED);
				}
				int count = i + 1;
				String SlNo = "\t " + count;
				row.setText(new String[] { SlNo, objectNames.get(i), elements, refType });
				row.setBackground(rowColor);
				row.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
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

		if (type.equals("Sets") || type.equals("Lists") || type.equals("Arrays") || type.equals("Queues") || type.equals("Deques")
				|| type.equals("Iterables")) {
			return List.of("\tSL No.", "Variable Name", "Results", "Size", "Type");

		} else if (type.equals("Maps")) {
			return List.of("\tSL No.", "Variable Name", "Map Keys", "Map Values", "<K,V> Size", "Type");

		} else if (type.equals("CharSequences")) {
			return List.of("\tSL No.", "Variable Name", "String Comparision Result", "Characters", "Type");

		} else if (type.equals("custom")) {
			fieldNames.add(0, "Variable Name");
			fieldNames.add(0, "\tSL No.");
			return fieldNames;
		} else {
			return List.of("\tSL No.", "Variable Name", "Comparision Result", "Type");
		}
	}

	/**
	 * Utility method that replaces plural type name with singular
	 *
	 * @param content
	 *            message content
	 * @return String that contains singular type name
	 */
	@SuppressWarnings("nls")
	private String replaceToSingular(String content) {
		if (content.contains("Sets")) {
			content = content.replace("Sets", "Set");
		}
		if (content.contains("Lists")) {
			content = content.replace("Lists", "List");
		}
		if (content.contains("Arrays")) {
			content = content.replace("Arrays", "Array");
		}
		if (content.contains("Queues")) {
			content = content.replace("Queues", "Queue");
		}
		if (content.contains("Deques")) {
			content = content.replace("Deques", "Deque");
		}
		if (content.contains("Iterables")) {
			content = content.replace("Iterables", "Iterable");
		}
		if (content.contains("Maps")) {
			content = content.replace("Maps", "Map");
		}
		if (content.contains("CharSequences")) {
			content = content.replace("CharSequences", "CharSequence");
		}
		return content;
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
			results = inp2.results;
			objectNames = inp2.objectNames;
			elementTypeName = inp2.elementTypeName;
			fieldNames = inp2.fieldNames;
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
