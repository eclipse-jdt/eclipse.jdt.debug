/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.NameValuePairDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
 
/**
 * This tab appears for java applet launch configurations and allows the user to edit
 * applet-specific attributes such as width, height, name & applet parameters.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.1
 */
public class AppletParametersTab extends JavaLaunchConfigurationTab {
	
	private Label fWidthLabel;
	private Text fWidthText;
	private Label fHeightLabel;
	private Text fHeightText;
	private Label fNameLabel;
	private Text fNameText;
	private Table fParametersTable;
	private Button fParametersAddButton;
	private Button fParametersRemoveButton;
	private Button fParametersEditButton;

	private static final String EMPTY_STRING = "";	 //$NON-NLS-1$
	
	/**
	 * The default value for the 'width' attribute.	 */
	public static final int DEFAULT_APPLET_WIDTH = 200;
	
	/**
	 * The default value for the 'height' attribute.
	 */
	public static final int DEFAULT_APPLET_HEIGHT = 200;

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp);
		
		Composite widthHeightNameComp = new Composite(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		widthHeightNameComp.setLayoutData(gd);
		GridLayout widthHeightNameLayout = new GridLayout();
		widthHeightNameLayout.marginHeight = 0;
		widthHeightNameLayout.marginWidth = 0;
		widthHeightNameLayout.numColumns = 4;
		widthHeightNameComp.setLayout(widthHeightNameLayout);
		
		fWidthLabel= new Label(widthHeightNameComp, SWT.NONE);
		fWidthLabel.setText(LauncherMessages.getString("appletlauncher.argumenttab.widthlabel.text")); //$NON-NLS-1$
		fWidthLabel.setFont(font);
		
		fWidthText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWidthText.setLayoutData(gd);
		fWidthText.setFont(font);
		fWidthText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fNameLabel = new Label(widthHeightNameComp, SWT.NONE);
		fNameLabel.setText(LauncherMessages.getString("appletlauncher.argumenttab.namelabel.text")); //$NON-NLS-1$
		fNameLabel.setFont(font);
		
		fNameText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fNameText.setLayoutData(gd);
		fNameText.setFont(font);
		fNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});	

		fHeightLabel= new Label(widthHeightNameComp, SWT.NONE);
		fHeightLabel.setText(LauncherMessages.getString("appletlauncher.argumenttab.heightlabel.text")); //$NON-NLS-1$
		fHeightLabel.setFont(font);
		
		fHeightText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHeightText.setLayoutData(gd);
		fHeightText.setFont(font);
		fHeightText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		Label blank = new Label(widthHeightNameComp, SWT.NONE);
		blank.setText(EMPTY_STRING);
		Label hint = new Label(widthHeightNameComp, SWT.NONE);
		hint.setText(LauncherMessages.getString("AppletParametersTab.(optional_applet_instance_name)_1")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		hint.setLayoutData(gd);
		hint.setFont(font);
				
		createVerticalSpacer(comp);
		
		Composite parametersComp = new Composite(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		parametersComp.setLayoutData(gd);
		GridLayout parametersLayout = new GridLayout();
		parametersLayout.numColumns = 2;
		parametersLayout.marginHeight = 0;
		parametersLayout.marginWidth = 0;
		parametersComp.setLayout(parametersLayout);
		parametersComp.setFont(font);
		
		Label parameterLabel = new Label(parametersComp, SWT.NONE);
		parameterLabel.setText(LauncherMessages.getString("appletlauncher.argumenttab.parameterslabel.text")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		parameterLabel.setLayoutData(gd);
		parameterLabel.setFont(font);
		
		fParametersTable = new Table(parametersComp, SWT.BORDER | SWT.MULTI);
		fParametersTable.setData(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS);
		TableLayout tableLayout = new TableLayout();
		fParametersTable.setLayout(tableLayout);
		fParametersTable.setFont(font);
		gd = new GridData(GridData.FILL_BOTH);
		fParametersTable.setLayoutData(gd);
		TableColumn column1 = new TableColumn(this.fParametersTable, SWT.NONE);
		column1.setText(LauncherMessages.getString("appletlauncher.argumenttab.parameterscolumn.name.text")); //$NON-NLS-1$
		TableColumn column2 = new TableColumn(this.fParametersTable, SWT.NONE);
		column2.setText(LauncherMessages.getString("appletlauncher.argumenttab.parameterscolumn.value.text"));		 //$NON-NLS-1$
		tableLayout.addColumnData(new ColumnWeightData(100));
		tableLayout.addColumnData(new ColumnWeightData(100));
		fParametersTable.setHeaderVisible(true);
		fParametersTable.setLinesVisible(true);
		fParametersTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setParametersButtonsEnableState();
			}
		});
		fParametersTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				setParametersButtonsEnableState();
				if (fParametersEditButton.isEnabled()) {
					handleParametersEditButtonSelected();
				}
			}
		});
	
		Composite envButtonComp = new Composite(parametersComp, SWT.NONE);
		GridLayout envButtonLayout = new GridLayout();
		envButtonLayout.marginHeight = 0;
		envButtonLayout.marginWidth = 0;
		envButtonComp.setLayout(envButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		envButtonComp.setLayoutData(gd);
		envButtonComp.setFont(font);
		
		fParametersAddButton = createPushButton(envButtonComp ,LauncherMessages.getString("appletlauncher.argumenttab.parameters.button.add.text"), null); //$NON-NLS-1$
		fParametersAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleParametersAddButtonSelected();
			}
		});
		
		fParametersEditButton = createPushButton(envButtonComp, LauncherMessages.getString("appletlauncher.argumenttab.parameters.button.edit.text"), null); //$NON-NLS-1$
		fParametersEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleParametersEditButtonSelected();
			}
		});
		
		fParametersRemoveButton = createPushButton(envButtonComp, LauncherMessages.getString("appletlauncher.argumenttab.parameters.button.remove.text"), null); //$NON-NLS-1$
		fParametersRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleParametersRemoveButtonSelected();
			}
		});
	}

		
	/**
	 * @see ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			Integer.parseInt(fWidthText.getText().trim());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.getString("appletlauncher.argumenttab.width.error.notaninteger")); //$NON-NLS-1$
			return false;
		}
		try {
			Integer.parseInt(fHeightText.getText().trim());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.getString("appletlauncher.argumenttab.height.error.notaninteger")); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void handleParametersAddButtonSelected() {
		NameValuePairDialog dialog = 
			new NameValuePairDialog(getShell(), 
				LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.add.title"),  //$NON-NLS-1$
				new String[] {LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.add.name.text"), LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.add.value.text")},  //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {EMPTY_STRING, EMPTY_STRING}); 
		openNewParameterDialog(dialog, null);
		setParametersButtonsEnableState();
	}

	private void handleParametersEditButtonSelected() {
		TableItem selectedItem = this.fParametersTable.getSelection()[0];
		String name = selectedItem.getText(0);
		String value = selectedItem.getText(1);
		NameValuePairDialog dialog =
			new NameValuePairDialog(getShell(), 
				LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.edit.title"),  //$NON-NLS-1$
				new String[] {LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.edit.name.text"), LauncherMessages.getString("appletlauncher.argumenttab.parameters.dialog.edit.value.text")},  //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {name, value});
		openNewParameterDialog(dialog, selectedItem);		
	}

	private void handleParametersRemoveButtonSelected() {
		int[] selectedIndices = this.fParametersTable.getSelectionIndices();
		this.fParametersTable.remove(selectedIndices);
		setParametersButtonsEnableState();
	}

	/**
	 * Set the enabled state of the three environment variable-related buttons based on the
	 * selection in the Table widget.
	 */
	private void setParametersButtonsEnableState() {
		int selectCount = this.fParametersTable.getSelectionIndices().length;
		if (selectCount < 1) {
			fParametersEditButton.setEnabled(false);
			fParametersRemoveButton.setEnabled(false);
		} else {
			fParametersRemoveButton.setEnabled(true);
			if (selectCount == 1) {
				fParametersEditButton.setEnabled(true);
			} else {
				fParametersEditButton.setEnabled(false);
			}
		}		
		fParametersAddButton.setEnabled(true);
	}

	/**
	 * Show the specified dialog and update the parameter table based on its results.
	 * 
	 * @param updateItem the item to update, or <code>null</code> if
	 *  adding a new item
	 */
	private void openNewParameterDialog(NameValuePairDialog dialog, TableItem updateItem) {
		if (dialog.open() != Window.OK) {
			return;
		}
		String[] nameValuePair = dialog.getNameValuePair();
		TableItem tableItem = updateItem;
		if (tableItem == null) {
			tableItem = getTableItemForName(nameValuePair[0]);
			if (tableItem == null) {
				tableItem = new TableItem(this.fParametersTable, SWT.NONE);
			}
		}
		tableItem.setText(nameValuePair);
		this.fParametersTable.setSelection(new TableItem[] {tableItem});
		updateLaunchConfigurationDialog();	
	}
	
	/**
	 * Helper method that indicates whether the specified parameter name is already present 
	 * in the parameters table.
	 */
	private TableItem getTableItemForName(String candidateName) {
		TableItem[] items = this.fParametersTable.getItems();
		for (int i = 0; i < items.length; i++) {
			String name = items[i].getText(0);
			if (name.equals(candidateName)) {
				return items[i];
			}
		}
		return null;
	}
	
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, Integer.parseInt(fWidthText.getText()));
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, Integer.parseInt(fHeightText.getText()));
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, (String)fNameText.getText());
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, getMapFromParametersTable());
	}
	
	private Map getMapFromParametersTable() {
		TableItem[] items = fParametersTable.getItems();
		if (items.length == 0) {
			return null;
		}
		Map map = new HashMap(items.length);
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String key = item.getText(0);
			String value = item.getText(1);
			map.put(key, value);
		}		
		return map;
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}
	
	private void updateParametersFromConfig(ILaunchConfiguration config) {
		Map envVars = null;
		try {
			if (config != null) {
				envVars = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, (Map)null);
			}
			updateTable(envVars, this.fParametersTable);
			setParametersButtonsEnableState();
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
	}

	private void updateTable(Map map, Table tableWidget) {
		tableWidget.removeAll();
		if (map == null) {
			return;
		}
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			TableItem tableItem = new TableItem(tableWidget, SWT.NONE);
			tableItem.setText(new String[] {key, value});			
		}
	}
		
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			fWidthText.setText(Integer.toString(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, DEFAULT_APPLET_WIDTH))); //$NON-NLS-1$
		} catch(CoreException ce) {
			fWidthText.setText(Integer.toString(DEFAULT_APPLET_WIDTH)); //$NON-NLS-1$
		}
		try {
			fHeightText.setText(Integer.toString(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, DEFAULT_APPLET_HEIGHT))); //$NON-NLS-1$
		} catch(CoreException ce) {
			fHeightText.setText(Integer.toString(DEFAULT_APPLET_HEIGHT)); //$NON-NLS-1$
		}
		try {
			fNameText.setText(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, LauncherMessages.getString("appletlauncher.argumenttab.name.defaultvalue"))); //$NON-NLS-1$
		} catch(CoreException ce) {
			fNameText.setText(LauncherMessages.getString("appletlauncher.argumenttab.name.defaultvalue")); //$NON-NLS-1$
		}
		updateParametersFromConfig(config);
	}
	
	/**
	 * Create some empty space 
	 */
	private void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}	
	
	private Table createParameterTable(Composite comp) {
		
		String[][] data;
		int count = 3;
		data = new String[count][2];
		for (int i = 0; i < count; i++) {
			data[i] = new String[] {};
		};
		
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;
		final Table table = new Table(comp, style | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFont(comp.getFont());
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(LauncherMessages.getString("appletlauncher.argumenttab.parameterscolumn.name.text")); //$NON-NLS-1$
		column1.setWidth(150);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LauncherMessages.getString("appletlauncher.argumenttab.parameterscolumn.value.text")); //$NON-NLS-1$
		column2.setWidth(150);
		for (int i = 0; i < data.length; i++) {
			TableItem item = new TableItem(table, 0);
			item.setText(data[i]);
		}
		// create a TableCursor to navigate around the table
		final TableCursor cursor = new TableCursor(table, SWT.SINGLE);
		// create an editor to edit the cell when the user hits "ENTER" 
		// while over a cell in the table
		final ControlEditor editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		cursor.addSelectionListener(new SelectionAdapter() {
			// when the TableEditor is over a cell, select the corresponding row in 
			// the table
			public void widgetSelected(SelectionEvent e) {
				table.setSelection(new TableItem[] { cursor.getRow()});
			}
			// when the user hits "ENTER" in the TableCursor, pop up a text editor so that 
			// they can change the text of the cell
			public void widgetDefaultSelected(SelectionEvent e) {
				final Text text = new Text(cursor, SWT.NONE);
				TableItem row = cursor.getRow();
				int column = cursor.getColumn();
				text.setText(row.getText(column));
				text.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						// close the text editor and copy the data over 
						// when the user hits "ENTER"
						if (e.character == SWT.CR) {
							TableItem row = cursor.getRow();
							int column = cursor.getColumn();
							row.setText(column, text.getText());
							text.dispose();
						}
						// close the text editor when the user hits "ESC"
						if (e.character == SWT.ESC) {
							text.dispose();
						}
					}
				});
				editor.setEditor(text);
				text.setFocus();
			}
		});
		// Hide the TableCursor when the user hits the "CTRL" or "SHIFT" key.
		// This alows the user to select multiple items in the table.
		cursor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CTRL
					|| e.keyCode == SWT.SHIFT
					|| (e.stateMask & SWT.CONTROL) != 0
					|| (e.stateMask & SWT.SHIFT) != 0) {
					cursor.setVisible(false);
				}
			}
		});
		// Show the TableCursor when the user releases the "SHIFT" or "CTRL" key.
		// This signals the end of the multiple selection task.
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CONTROL && (e.stateMask & SWT.SHIFT) != 0)
					return;
				if (e.keyCode == SWT.SHIFT && (e.stateMask & SWT.CONTROL) != 0)
					return;
				if (e.keyCode != SWT.CONTROL && (e.stateMask & SWT.CONTROL) != 0)
					return;
				if (e.keyCode != SWT.SHIFT && (e.stateMask & SWT.SHIFT) != 0)
					return;

				TableItem[] selection = table.getSelection();
				TableItem row =
					(selection.length == 0) ? table.getItem(table.getTopIndex()) : selection[0];
				table.showItem(row);
				cursor.setSelection(row, 0);
				cursor.setVisible(true);
				cursor.setFocus();
			}
		});		
		table.pack();
		return table;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("appletlauncher.argumenttab.name"); //$NON-NLS-1$
	}	

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
	}	

}

