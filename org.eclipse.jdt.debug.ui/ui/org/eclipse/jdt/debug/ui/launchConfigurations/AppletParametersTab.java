/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.NameValuePairDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
 
/**
 * This tab appears for java applet launch configurations and allows the user to edit
 * applet-specific attributes such as width, height, name & applet parameters.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.1
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class AppletParametersTab extends JavaLaunchTab {
	
	private Text fWidthText;
	private Text fHeightText;
	private Text fNameText;
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
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == fViewer.getTable() || source == fViewer) {
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
	 * The parameters table viewer
	 */
	private TableViewer fViewer;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {	
		Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),	IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_APPLET_PARAMETERS_TAB);
		
		Composite namecomp = SWTFactory.createComposite(comp, comp.getFont(), 4, 1, GridData.FILL_HORIZONTAL, 0, 0);
		
		SWTFactory.createLabel(namecomp, LauncherMessages.appletlauncher_argumenttab_widthlabel_text, 1);
		
		fWidthText = SWTFactory.createSingleText(namecomp, 1);
		fWidthText.addModifyListener(fListener);

		SWTFactory.createLabel(namecomp, LauncherMessages.appletlauncher_argumenttab_namelabel_text, 1);
		
		fNameText = SWTFactory.createSingleText(namecomp, 1);
		fNameText.addModifyListener(fListener);	

		SWTFactory.createLabel(namecomp, LauncherMessages.appletlauncher_argumenttab_heightlabel_text, 1);
		
		fHeightText = SWTFactory.createSingleText(namecomp, 1);
		fHeightText.addModifyListener(fListener);
		
		Label blank = new Label(namecomp, SWT.NONE);
		blank.setText(EMPTY_STRING);
		Label hint = SWTFactory.createLabel(namecomp, LauncherMessages.AppletParametersTab__optional_applet_instance_name__1, 1);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		hint.setLayoutData(gd);
				
		SWTFactory.createVerticalSpacer(comp, 1);
		
		Composite paramcomp = SWTFactory.createComposite(comp, comp.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
		
		SWTFactory.createLabel(paramcomp, LauncherMessages.appletlauncher_argumenttab_parameterslabel_text, 2);		
		
		Table ptable = new Table(paramcomp, SWT.FULL_SELECTION | SWT.BORDER);
		fViewer = new TableViewer(ptable);
		gd = new GridData(GridData.FILL_BOTH);
		ptable.setLayoutData(gd);		
		TableColumn column1 = new TableColumn(ptable, SWT.NONE);
		column1.setText(LauncherMessages.appletlauncher_argumenttab_parameterscolumn_name_text);
		TableColumn column2 = new TableColumn(ptable, SWT.NONE);
		column2.setText(LauncherMessages.appletlauncher_argumenttab_parameterscolumn_value_text);
		TableLayout tableLayout = new TableLayout();
		ptable.setLayout(tableLayout);
		tableLayout.addColumnData(new ColumnWeightData(100));
		tableLayout.addColumnData(new ColumnWeightData(100));
		ptable.setHeaderVisible(true);
		ptable.setLinesVisible(true);
		ptable.addSelectionListener(fListener);
		ptable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				setParametersButtonsEnableState();
				if (fParametersEditButton.isEnabled()) {
					handleParametersEditButtonSelected();
				}
			}
		});
		
		fViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				Map<?, ?> params = (Map<?, ?>) inputElement;
				return params.keySet().toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
	
		fViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0) {
					return element.toString();
				}

				String key = (String) element;
				Map<String, String> params = (Map<String, String>) fViewer.getInput();
				Object object = params.get(key);
				if (object != null)
					return object.toString();
				return null;
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		fViewer.setComparator(new ViewerComparator());
		
		Composite envcomp = SWTFactory.createComposite(paramcomp, paramcomp.getFont(), 1, 1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL, 0, 0);
		
		fParametersAddButton = createPushButton(envcomp ,LauncherMessages.appletlauncher_argumenttab_parameters_button_add_text, null); 
		fParametersAddButton.addSelectionListener(fListener);
		
		fParametersEditButton = createPushButton(envcomp, LauncherMessages.appletlauncher_argumenttab_parameters_button_edit_text, null); 
		fParametersEditButton.addSelectionListener(fListener);
		
		fParametersRemoveButton = createPushButton(envcomp, LauncherMessages.appletlauncher_argumenttab_parameters_button_remove_text, null); 
		fParametersRemoveButton.addSelectionListener(fListener);
		
		setParametersButtonsEnableState();
		Dialog.applyDialogFont(parent);
	}

		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			Integer.parseInt(getWidthText());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.appletlauncher_argumenttab_width_error_notaninteger); 
			return false;
		}
		try {
			Integer.parseInt(getHeightText());
		} catch(NumberFormatException nfe) {
			setErrorMessage(LauncherMessages.appletlauncher_argumenttab_height_error_notaninteger); 
			return false;
		}
		return true;
	}

	private void handleParametersAddButtonSelected() {
		NameValuePairDialog dialog = 
			new NameValuePairDialog(getShell(), 
				LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_title,  
				new String[] {LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_name_text, LauncherMessages.appletlauncher_argumenttab_parameters_dialog_add_value_text},  // 
				new String[] {EMPTY_STRING, EMPTY_STRING}); 
		openNewParameterDialog(dialog, null);
		setParametersButtonsEnableState();
	}

	private void handleParametersEditButtonSelected() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		String key = (String) selection.getFirstElement();
		Map<String, String> params = (Map<String, String>) fViewer.getInput();
		String value = params.get(key);
		
		NameValuePairDialog dialog =
			new NameValuePairDialog(getShell(), 
				LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_title,  
				new String[] {LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_name_text, LauncherMessages.appletlauncher_argumenttab_parameters_dialog_edit_value_text},  // 
				new String[] {key, value});
		
		openNewParameterDialog(dialog, key);		
	}

	private void handleParametersRemoveButtonSelected() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		Object[] keys = selection.toArray();
		for (int i = 0; i < keys.length; i++) {
			String key = (String) keys[i];
			Map<String, String> params = (Map<String, String>) fViewer.getInput();
			params.remove(key);			
		}
		fViewer.refresh();
		setParametersButtonsEnableState();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Set the enabled state of the three environment variable-related buttons based on the
	 * selection in the Table widget.
	 */
	private void setParametersButtonsEnableState() {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		int selectCount = selection.size();
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
	 * @param dialog the dialog
	 * @param key the key to edit
	 */
	private void openNewParameterDialog(NameValuePairDialog dialog, String key) {
		if (dialog.open() != Window.OK) {
			return;
		}
		String[] nameValuePair = dialog.getNameValuePair();
		Map<String, String> params = (Map<String, String>) fViewer.getInput();
		params.remove(key);
		params.put(nameValuePair[0], nameValuePair[1]);
		fViewer.refresh();
		updateLaunchConfigurationDialog();	
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
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, (Map<String, String>) fViewer.getInput());
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

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}
	

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			fWidthText.setText(Integer.toString(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, DEFAULT_APPLET_WIDTH))); 
		} catch(CoreException ce) {
			fWidthText.setText(Integer.toString(DEFAULT_APPLET_WIDTH)); 
		}
		try {
			fHeightText.setText(Integer.toString(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, DEFAULT_APPLET_HEIGHT))); 
		} catch(CoreException ce) {
			fHeightText.setText(Integer.toString(DEFAULT_APPLET_HEIGHT)); 
		}
		try {
			fNameText.setText(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, LauncherMessages.appletlauncher_argumenttab_name_defaultvalue)); 
		} catch(CoreException ce) {
			fNameText.setText(LauncherMessages.appletlauncher_argumenttab_name_defaultvalue); 
		}
		
		Map<String, String> input = new HashMap<String, String>();
		try {
			 Map<String, String> params = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_PARAMETERS, (Map<String, String>) null);
             if (params != null)
                 input.putAll(params);
		} catch (CoreException e) {
		}
		
		fViewer.setInput(input);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.appletlauncher_argumenttab_name; 
	}	
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	@Override
	public String getId() {
		return "org.eclipse.jdt.debug.ui.appletParametersTab"; //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when activated
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when de-activated
	}	
}

