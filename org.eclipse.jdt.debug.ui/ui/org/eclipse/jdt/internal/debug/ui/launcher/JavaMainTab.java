package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationHelper;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This tab appears in the LaunchConfigurationDialog for launch configurations that
 * require Java-specific launching information such as a main type and JRE.
 */
public class JavaMainTab extends JavaLaunchConfigurationTab implements IAddVMDialogRequestor {
		
	// Project UI widgets
	private Label fProjLabel;
	private Text fProjText;
	private Button fProjButton;

	// Main class UI widgets
	private Label fMainLabel;
	private Text fMainText;
	private Button fSearchButton;
	private Button fSearchExternalJarsCheckButton;
	
	// JRE UI widgets
	private Label fJRELabel;
	private Combo fJRECombo;
	private Button fJREAddButton;
	
	// Collections used to populating the JRE Combo box
	private IVMInstallType[] fVMTypes;
	private List fVMStandins;
	
	private static final String EMPTY_STRING = "";
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp);
		
		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);
		
		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText("&Project:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);
		
		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fProjButton = new Button(projComp, SWT.PUSH);
		fProjButton.setText("&Browse...");
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
			}
		});
		
		Composite mainComp = new Composite(comp, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);
		
		fMainLabel = new Label(mainComp, SWT.NONE);
		fMainLabel.setText("Main cla&ss:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		fMainLabel.setLayoutData(gd);

		fMainText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fMainText.setLayoutData(gd);
		fMainText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fSearchButton = new Button(mainComp, SWT.PUSH);
		fSearchButton.setText("Searc&h...");
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
			}
		});
		
		fSearchExternalJarsCheckButton = new Button(mainComp, SWT.CHECK);
		fSearchExternalJarsCheckButton.setText("E&xt. jars");
		fSearchExternalJarsCheckButton.setToolTipText("Include external jars when searching for a main class");
				
		createVerticalSpacer(comp);
		
		Composite jreComp = new Composite(comp, SWT.NONE);
		GridLayout jreLayout = new GridLayout();
		jreLayout.numColumns = 2;
		jreLayout.marginHeight = 0;
		jreLayout.marginWidth = 0;
		jreComp.setLayout(jreLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		jreComp.setLayoutData(gd);
		
		fJRELabel = new Label(jreComp, SWT.NONE);
		fJRELabel.setText("&JRE:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fJRELabel.setLayoutData(gd);
		
		fJRECombo = new Combo(jreComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJRECombo.setLayoutData(gd);
		initializeJREComboBox();
		fJRECombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fJREAddButton = new Button(jreComp, SWT.PUSH);
		fJREAddButton.setText("A&dd...");
		fJREAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleJREAddButtonSelected();
			}
		});
				
	}
		
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		updateMainTypeFromConfig(config);
		updateJREFromConfig(config);
	}
	
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = "";
		try {
			projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		} catch (CoreException ce) {
		}
		fProjText.setText(projectName);
	}
	
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
		String mainTypeName = "";
		try {
			mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
		} catch (CoreException ce) {			
		}	
		fMainText.setText(mainTypeName);	
	}

	protected void updateJREFromConfig(ILaunchConfiguration config) {
		String vmID = null;
		try {
			vmID = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, EMPTY_STRING);
		} catch (CoreException ce) {			
		}
		if (vmID == null) {
			clearJREComboBoxEntry();
		} else {
			selectJREComboBoxEntry(vmID);
		}
	}
		
	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)fProjText.getText());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)fMainText.getText());
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			String vmID = vmStandin.getId();
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vmID);
			String vmTypeID = vmStandin.getVMInstallType().getId();
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
		}		
	}
			
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}
	
	/**
	 * Load the JRE related collections, and use these to set the values on the combo box
	 */
	protected void initializeJREComboBox() {
		fVMTypes= JavaRuntime.getVMInstallTypes();
		fVMStandins= createFakeVMInstalls(fVMTypes);
		populateJREComboBox();		
	}
	
	private List createFakeVMInstalls(IVMInstallType[] vmTypes) {
		ArrayList vms= new ArrayList();
		for (int i= 0; i < vmTypes.length; i++) {
			IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
			for (int j= 0; j < vmInstalls.length; j++) 
				vms.add(new VMStandin(vmInstalls[j]));
		}
		return vms;
	}
	
	/**
	 * Set the available items on the JRE combo box
	 */
	protected void populateJREComboBox() {
		String[] vmNames = new String[fVMStandins.size()];
		Iterator iterator = fVMStandins.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			VMStandin standin = (VMStandin)iterator.next();
			String vmName = standin.getName();
			vmNames[index] = vmName;
			index++;
		}
		fJRECombo.setItems(vmNames);
	}
	
	/**
	 * Cause the VM with the specified ID to be selected in the JRE combo box.
	 * This relies on the fact that the items set on the combo box are done so in 
	 * the same order as they in the <code>fVMStandins</code> list.
	 */
	protected void selectJREComboBoxEntry(String vmID) {
		//VMStandin selectedVMStandin = null;
		int index = -1;
		for (int i = 0; i < fVMStandins.size(); i++) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(i);
			if (vmStandin.getId().equals(vmID)) {
				index = i;
				//selectedVMStandin = vmStandin;
				break;
			}
		}
		if (index > -1) {
			fJRECombo.select(index);
			//fJRECombo.setData(JavaDebugUI.VM_INSTALL_TYPE_ATTR, selectedVMStandin.getVMInstallType().getId());
		} else {
			clearJREComboBoxEntry();
		}
	}
	
	/**
	 * Convenience method to remove any selection in the JRE combo box
	 */
	protected void clearJREComboBoxEntry() {
		//fJRECombo.clearSelection();
		fJRECombo.deselectAll();
	}
	
	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {
		
		IJavaProject javaProject = getJavaProject();
		IJavaSearchScope searchScope = null;
		if ((javaProject == null) || !javaProject.exists()) {
			searchScope = SearchEngine.createWorkspaceScope();
		} else {
			searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject}, false);
		}		
		
		int constraints = IJavaElementSearchConstants.CONSIDER_BINARIES;
		if (fSearchExternalJarsCheckButton.getSelection()) {
			constraints |= IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS;
		}
		
		Shell shell = getShell();
		SelectionDialog dialog = JavaUI.createMainTypeDialog(shell, 
															 getLaunchConfigurationDialog(), 
															 searchScope, 
															 constraints, 
															 false, 
															 "");
		dialog.setTitle("Choose main type");
		dialog.setMessage("Choose a main type to launch");
		if (dialog.open() == dialog.CANCEL) {
			return;
		}
		
		Object[] results = dialog.getResult();
		if ((results == null) || (results.length < 1)) {
			return;
		}		
		IType type = (IType)results[0];
		if (type != null) {
			fMainText.setText(type.getFullyQualifiedName());
			javaProject = type.getJavaProject();
			fProjText.setText(javaProject.getElementName());
		}
	}
	
	/**
	 * Show a dialog that lets the user add a new JRE definition
	 */
	protected void handleJREAddButtonSelected() {
		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, null);
		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.editJRE.title")); //$NON-NLS-1$
		if (dialog.open() != dialog.OK) {
			return;
		}
	}
	
	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		IJavaProject project = chooseJavaProject();
		if (project == null) {
			return;
		}
		
		String projectName = project.getElementName();
		fProjText.setText(projectName);		
	}
	
	/**
	 * Realize a Java Project selection dialog and return the first selected project,
	 * or null if there was none.
	 */
	protected IJavaProject chooseJavaProject() {
		IJavaProject[] projects;
		try {
			projects= JavaCore.create(getWorkspaceRoot()).getJavaProjects();
		} catch (JavaModelException e) {
			JavaPlugin.log(e.getStatus());
			projects= new IJavaProject[0];
		}
		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project selection");
		dialog.setMessage("Choose a project to constrain the search for main types");
		dialog.setElements(projects);
		
		IJavaProject javaProject = getJavaProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == dialog.OK) {			
			return (IJavaProject) dialog.getFirstResult();
		}			
		return null;		
	}
	
	/**
	 * Return the IJavaProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected IJavaProject getJavaProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getJavaModel().getJavaProject(projectName);		
	}
	
	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Convenience method to get access to the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * @see IAddVMDialogRequestor#isDuplicateName(IVMInstallType, String)
	 */
	public boolean isDuplicateName(IVMInstallType type, String name) {
		for (int i= 0; i < fVMStandins.size(); i++) {
			IVMInstall vm= (IVMInstall)fVMStandins.get(i);
			if (vm.getVMInstallType() == type) {
				if (vm.getName().equals(name))
					return true;
			}
		}
		return false;
	}

	/**
	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
		((VMStandin)vm).convertToRealVM();		
		try {
			JavaRuntime.saveVMConfiguration();
		} catch(CoreException e) {
		}
		fVMStandins.add(vm);
		populateJREComboBox();
		selectJREComboBoxEntry(vm.getId());
	}

	/**
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {
		
		setErrorMessage(null);
		setMessage(null);
		
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage("Project does not exist.");
				return false;
			}
		}

		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage("Main type not specified.");
			return false;
		}
		IJavaProject jp = getJavaProject();
		if (jp != null) {
			// only verify type exists if Java project is specified
			try {
				JavaLaunchConfigurationHelper.getMainType(name, jp);
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				return false;
			}
		}
		
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			IVMInstall vm = vmStandin.convertToRealVM();
			File location = vm.getInstallLocation();
			if (location == null) {
				setErrorMessage("JRE home directory not specified.");
				return false;
			}
			if (!location.exists()) {
				setErrorMessage("JRE home directory does not exist.");
				return false;
			}			
		} else {
			setErrorMessage("JRE not specified.");
			return false;
		}	
		
		return true;
	}
	
	/**
	 * Initialize default attribute values based on the
	 * given Java element.
	 */
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		initializeJavaProject(javaElement, config);
		initializeMainTypeAndName(javaElement, config);
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement je = getContext();
		if (je == null) {
			initializeHardCodedDefaults(config);
		} else {
			initializeDefaults(je, config);
		}
	}

	/**
	 * Set the main type & name attributes on the working copy based on the IJavaElement
	 */
	protected void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		String name = "";
		if (javaElement instanceof IMember) {
			IMember member = (IMember)javaElement;
			if (member.isBinary()) {
				javaElement = member.getClassFile();
			} else {
				javaElement = member.getCompilationUnit();
			}
		}
		if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
			try {
				IType[] types = MainMethodFinder.findTargets(new BusyIndicatorRunnableContext(), new Object[] {javaElement});
				if (types != null && (types.length > 0)) {
					// Simply grab the first main type found in the searched element
					name = types[0].getFullyQualifiedName();
				}
			} catch (InterruptedException ie) {
			} catch (InvocationTargetException ite) {
			}
		}
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, name);
		if (name.length() > 0) {
			int index = name.lastIndexOf('.');
			if (index > 0) {
				name = name.substring(index + 1);
			}		
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
		}
	}

	/**
	 * Set the VM attributes on the working copy based on the workbench default VM.
	 */
	protected void initializeDefaultVM(ILaunchConfigurationWorkingCopy config) {
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		if (vmInstall == null) {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, (String)null);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		} else {
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vmInstall.getId());
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmInstall.getVMInstallType().getId());
		}
	}

	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {
		initializeDefaultVM(config);
	}

}

