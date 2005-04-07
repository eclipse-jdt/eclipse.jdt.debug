/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
	
	private class AppletTabListener extends SelectionAdapter implements ModifyListener {

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == fParametersTable) {
				setParametersButtonsEnableState();
			} else if (source == fParametersAddButton) {
				handleParametersAddButtonSelected();
			} else if (source == fParametersEditButton) {
				handleParametersEditButtonSelected();
			} else if (source == fParametersRemoveButton) {
				handleParametersRemoveButtonSelected();
			}
		}

	}
	
	private AppletTabListener fListener= new AppletTabListener();

	private static final String EMPTY_STRING = "";	 //$NON-NLS-1$
	
	/**
	 * The default value for the 'width' attribute.
	 */
	public static final int DEFAULT_APPLET_WIDTH = 200;
	
	/**
	 * The default value for the 'height' attribute.
	 */
	public static final int DEFAULT_APPLET_HEIGHT = 200;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		Composite widthHeightNameComp = new Composite(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		widthHeightNameComp.setLayoutData(gd);
		GridLayout widthHeightNameLayout = new GridLayout();
		widthHeightNameLayout.marginHeight = 0;
		widthHeightNameLayout.marginWidth = 0;
		widthHeightNameLayout.numColumns = 4;
		widthHeightNameComp.setLayout(widthHeightNameLayout);
		
		fWidthLabel= new Label(widthHeightNameComp, SWT.NONE);
		fWidthLabel.setText(LauncherMessages.appletlauncher_argumenttab_widthlabel_text); //$NON-NLS-1$
		fWidthLabel.setFont(font);
		
		fWidthText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWidthText.setLayoutData(gd);
		fWidthText.setFont(font);
		fWidthText.addModifyListener(fListener);

		fNameLabel = new Label(widthHeightNameComp, SWT.NONE);
		fNameLabel.setText(LauncherMessages.appletlauncher_argumenttab_namelabel_text); //$NON-NLS-1$
		fNameLabel.setFont(font);
		
		fNameText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fNameText.setLayoutData(gd);
		fNameText.setFont(font);
		fNameText.addModifyListener(fListener);	

		fHeightLabel= new Label(widthHeightNameComp, SWT.NONE);
		fHeightLabel.setText(LauncherMessages.appletlauncher_argumenttab_heightlabel_text); //$NON-NLS-1$
		fHeightLabel.setFont(font);
		
		fHeightText = new Text(widthHeightNameComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fHeightText.setLayoutData(gd);
		fHeightText.setFont(font);
		fHeightText.addModifyListener(fListener);
		
		Label blank = new Label(widthHeightNameComp, SWT.NONE);
		blank.setText(EMPTY_STRING);
		Label hint = new Label(widthHeightNameComp, SWT.NONE);
		hint.setText(LauncherMessages.AppletParametersTab__optional_applet_instance_name__1); //$NON-NLS-1$
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
		parameterLabel.setText(LauncherMessages.appletlauncher_argumenttab_parameterslabel_text); //$NON-NLS-1$
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
		column1.setText(LauncherMessages.appletlauncher_argumenttab_parameterscolumn_name_text); //$NON-NLS-1$
		TableColumn column2 = new TableColumn(this.fParametersTable, SWT.NONE);
		column2.setText(LauncherMessages.appletlauncher_argumenttab_parameterscolumn_value_text);		 //$NON-NLS-1$
		tableLayout.addColumnData(new ColumnWeightData(100));
		tableLayout.addColumnData(new ColumnWeightData(100));
		fParametersTable.setHeaderVisible(true);
		fParametersTable.setLinesVisible(true);
		fParametersTable.addSelectionListener(fListener);
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
		
		fParametersAddButton = createPushButton(envButtonComp ,LauncherMessages.appletlauncher_argumenttab_parameters_button_add_text, null); //$NON-NLS-1$
		fParametersAddButton.addSelectionListener(fListener);
		
		fParametersEditButton = createPushButton(envButtonComp, LauncherMessages.appletlauncher_argumenttab_parameters_button_edit_text, null); //$NON-NLS-1$
		fParametersEditButton.addSelectionListener(fListener);
		
		fParametersRemoveButton = createPushButton(envButtonComp, LauncherMessages.appletlauncher_argumenttab_parameters_button_remove_text, null); //$NON-NLS-1$
		fParametersRemoveButton.addSelectionListener(fListener);
	}

		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			Integer.parseInt(getWidthText());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.appletlauncher_argumenttab_width_error_notaninteger); //$NON-NLS-1$
			return false;
		}
		try {
			Integer.parseInt(getHeightText());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.appletlauncher_argumenttab_height_error_notaninteger); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void handleParametersAddButtonSelected() {
		NameValuePairDialog dialog = 
			new NameValuePairDialog(getShell(), 
				LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_title,  //$NON-NLS-1$
				new String[] {LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_name_text, LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_value_text},  //$NON-NLS-1$ //$NON-NLS-2$
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
				LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_title,  //$NON-NLS-1$
				new String[] {LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_name_text, LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_value_text},  //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {name, value});
		openNewParameterDialog(dialog, selectedItem);		
	}

	private void handleParametersRemoveButtonSelected() {
		int[] selectedIndices = this.fParametersTable.getSelectionIndices();
		this.fParametersTable.remove(selectedIndices);
		setParametersButtonsEnableState();
		updateLaunchConfigurationDialog();
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		try {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, Integer.parseInt(getWidthText()));
		} catch (NumberFormatException e) {
		}
		try {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, Integer.parseInt(getHeightText()));
		} catch (NumberFormatException e) {
		}
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, fNameText.getText());
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, getMapFromParametersTable());
	}
	
	/**
	 * Returns the current width specified by the user
	 * @return the width specified by the user
	 */
	private String getWidthText() {
		return fWidthText.getText().trim();
	}
	
	/**
	 * Returns the current height specified by the user
	 * @return the height specified by the user
	 */
	private String getHeightText() {
		return fHeightText.getText().trim();
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
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
			fNameText.setText(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, LauncherMessages.appletlauncher_argumenttab_name_defaultvalue)); //$NON-NLS-1$
		} catch(CoreException ce) {
			fNameText.setText(LauncherMessages.appletlauncher_argumenttab_name_defaultvalue); //$NON-NLS-1$
		}
		updateParametersFromConfig(config);
	}
	
	/**
	 * Create some empty space 
	 */
	private void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.appletlauncher_argumenttab_name; //$NON-NLS-1$
	}	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when activated
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}	
}

