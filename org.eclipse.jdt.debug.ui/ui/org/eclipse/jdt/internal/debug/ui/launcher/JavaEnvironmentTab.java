package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;

public class JavaEnvironmentTab implements ILaunchConfigurationTab {

	// The launch configuration dialog that owns this tab
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	// Flag that when true, prevents the owning dialog's status area from getting updated.
	// Used when multiple config attributes are getting updated at once.
	private boolean fBatchUpdate = false;
	
	// Paths UI widgets
	private TabFolder fPathTabFolder;
	private TabItem fBootPathTabItem;
	private TabItem fClassPathTabItem;
	private TabItem fExtensionPathTabItem;
	private List fBootPathList;
	private List fClassPathList;
	private List fExtensionPathList;
	private Button fPathAddButton;
	private Button fPathRemoveButton;
	private Button fPathMoveUpButton;
	private Button fPathMoveDownButton;
	
	// Environment variables UI widgets
	private Label fEnvLabel;
	private Table fEnvTable;
	private Button fEnvAddButton;
	private Button fEnvRemoveButton;
	
	// The launch config working copy providing the values shown on this tab
	private ILaunchConfigurationWorkingCopy fWorkingCopy;

	private static final String EMPTY_STRING = "";
	
	protected void setLaunchDialog(ILaunchConfigurationDialog dialog) {
		fLaunchConfigurationDialog = dialog;
	}
	
	protected ILaunchConfigurationDialog getLaunchDialog() {
		return fLaunchConfigurationDialog;
	}
	
	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}
	
	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(ILaunchConfigurationDialog, TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp, 2);
		
		fPathTabFolder = new TabFolder(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		fPathTabFolder.setLayoutData(gd);
		
		fBootPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI);
		gd = new GridData(GridData.FILL_BOTH);
		fBootPathList.setLayoutData(gd);
		fBootPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 0);
		fBootPathTabItem.setText("Bootpath");
		fBootPathTabItem.setControl(fBootPathList);
		
		fClassPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI);
		gd = new GridData(GridData.FILL_BOTH);
		fClassPathList.setLayoutData(gd);
		fClassPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 1);
		fClassPathTabItem.setText("Classpath");
		fClassPathTabItem.setControl(fClassPathList);
		
		fExtensionPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI);
		gd = new GridData(GridData.FILL_BOTH);
		fExtensionPathList.setLayoutData(gd);
		fExtensionPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 2);
		fExtensionPathTabItem.setText("Extension path");
		fExtensionPathTabItem.setControl(fExtensionPathList);
		
				
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		pathButtonComp.setLayoutData(gd);
		
		createVerticalSpacer(pathButtonComp, 1);
		
		fPathAddButton = new Button(pathButtonComp, SWT.PUSH);
		fPathAddButton.setText("Add entry");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathAddButton.setLayoutData(gd);
		
		fPathRemoveButton = new Button(pathButtonComp, SWT.PUSH);
		fPathRemoveButton.setText("Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathRemoveButton.setLayoutData(gd);
		
		fPathMoveUpButton = new Button(pathButtonComp, SWT.PUSH);
		fPathMoveUpButton.setText("Move up");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathMoveUpButton.setLayoutData(gd);
		
		fPathMoveDownButton = new Button(pathButtonComp, SWT.PUSH);
		fPathMoveDownButton.setText("Move down");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathMoveDownButton.setLayoutData(gd);
		
		createVerticalSpacer(comp, 2);
		
		fEnvLabel = new Label(comp, SWT.NONE);
		fEnvLabel.setText("Environment variables");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fEnvLabel.setLayoutData(gd);
		
		fEnvTable = new Table(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		fEnvTable.setLayoutData(gd);
		
		Composite envButtonComp = new Composite(comp, SWT.NONE);
		GridLayout envButtonLayout = new GridLayout();
		envButtonLayout.marginHeight = 0;
		envButtonLayout.marginWidth = 0;
		envButtonComp.setLayout(envButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		envButtonComp.setLayoutData(gd);
		
		fEnvAddButton = new Button(envButtonComp, SWT.PUSH);
		fEnvAddButton.setText("Add");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fEnvAddButton.setLayoutData(gd);
		
		fEnvRemoveButton = new Button(envButtonComp, SWT.PUSH);
		fEnvRemoveButton.setText("Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fEnvRemoveButton.setLayoutData(gd);
		
		
		
		return comp;
	}

	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
		if (launchConfiguration.equals(getWorkingCopy())) {
			return;
		}
		
		setBatchUpdate(true);
		updateWidgetsFromConfig(launchConfiguration);
		setBatchUpdate(false);

		setWorkingCopy(launchConfiguration);
	}

	/**
	 * Set values for all UI widgets in this tab using values kept in the specified
	 * launch configuration.
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
	}
	
	protected void setBatchUpdate(boolean update) {
		fBatchUpdate = update;
	}
	
	protected boolean isBatchUpdate() {
		return fBatchUpdate;
	}

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}
	
}
