/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Sets default VM per execution environment.
 * 
 * @since 3.2
 */
public class ExecutionEnvironmentsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * The ID of the page 
	 * 
	 * @since 3.5
	 */
	public static final String ID = "org.eclipse.jdt.debug.ui.jreProfiles"; //$NON-NLS-1$
	
	private TableViewer fProfilesViewer;
	private CheckboxTableViewer fJREsViewer;
	private Text fDescription;
	
	/**
	 * Working copy "EE Profile -> Default JRE" 
	 */
	private Map<Object, Object> fDefaults = new HashMap<Object, Object>();
	
	class JREsContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return ((IExecutionEnvironment)inputElement).getCompatibleVMs();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
															
	public ExecutionEnvironmentsPreferencePage() {
		super();
		// only used when page is shown programatically
		setTitle(JREMessages.JREProfilesPreferencePage_0);	 
		setDescription(JREMessages.JREProfilesPreferencePage_1); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		noDefaultAndApplyButton();
		// TODO: fix help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(ancestor, IJavaDebugHelpContextIds.JRE_PROFILES_PAGE);
		
		// init default mappings
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
		for (int i = 0; i < environments.length; i++) {
			IExecutionEnvironment environment = environments[i];
			IVMInstall install = environment.getDefaultVM();
			if (install != null) {
				fDefaults.put(environment, install);
			}
		}
		
		Composite container = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.makeColumnsEqualWidth = true;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		container.setFont(ancestor.getFont());
		
		Composite eeContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		eeContainer.setLayout(layout);
		eeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(eeContainer, SWT.NONE);
		label.setFont(ancestor.getFont());
		label.setText(JREMessages.JREProfilesPreferencePage_2);
		label.setLayoutData(new GridData(SWT.FILL, 0, true, false));
		
		Table table= new Table(eeContainer, SWT.BORDER | SWT.SINGLE);
		table.setLayout(layout);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fProfilesViewer = new TableViewer(table);
		fProfilesViewer.setContentProvider(new ArrayContentProvider());
		fProfilesViewer.setLabelProvider(new ExecutionEnvironmentsLabelProvider());
		fProfilesViewer.setComparator(new ViewerComparator());
		fProfilesViewer.setInput(JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments());
		
		Composite jreContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		jreContainer.setLayout(layout);
		jreContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		label = new Label(jreContainer, SWT.NONE);
		label.setFont(ancestor.getFont());
		label.setText(JREMessages.JREProfilesPreferencePage_3);
		label.setLayoutData(new GridData(SWT.FILL, 0, true, false));
	
		table= new Table(jreContainer, SWT.CHECK | SWT.BORDER | SWT.SINGLE);
		table.setLayout(layout);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fJREsViewer = new CheckboxTableViewer(table);
		fJREsViewer.setContentProvider(new JREsContentProvider());
		fJREsViewer.setLabelProvider(new JREsEnvironmentLabelProvider(new JREsEnvironmentLabelProvider.IExecutionEnvironmentProvider() {		
			public IExecutionEnvironment getEnvironment() {
				return (IExecutionEnvironment) fJREsViewer.getInput();
			}
		}));
		fJREsViewer.setComparator(new JREsEnvironmentComparator());
		
		label = new Label(container, SWT.NONE);
		label.setFont(ancestor.getFont());
		label.setText(JREMessages.JREProfilesPreferencePage_4);
		label.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
		
		Text text = new Text(container, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		text.setFont(ancestor.getFont());
		text.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
		fDescription = text;
					
		fProfilesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IExecutionEnvironment env = (IExecutionEnvironment) ((IStructuredSelection)event.getSelection()).getFirstElement();
				fJREsViewer.setInput(env);
				String description = env.getDescription();
				if (description == null) {
					description = ""; //$NON-NLS-1$
				}
				fDescription.setText(description);
				IVMInstall jre = (IVMInstall) fDefaults.get(env);
				if (jre != null) {
					fJREsViewer.setCheckedElements(new Object[]{jre});
				} else {
					fJREsViewer.setCheckedElements(new Object[0]);
				}
			}
		});
		
		fJREsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					Object element = event.getElement();
					fDefaults.put(fJREsViewer.getInput(), element);
					fJREsViewer.setCheckedElements(new Object[]{element});
				} else {
					fDefaults.remove(fJREsViewer.getInput());
				}
		
			}
		});
		
		Dialog.applyDialogFont(ancestor);
		return ancestor;
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
		for (int i = 0; i < environments.length; i++) {
			IExecutionEnvironment environment = environments[i];
			IVMInstall vm = (IVMInstall) fDefaults.get(environment);
			// if the VM no longer exists - set to default to avoid illegal argument exception (bug 267914)
			if (vm != null) {
				if (vm.getVMInstallType().findVMInstall(vm.getId()) == null) {
					vm = null;
				}
			}
			environment.setDefaultVM(vm);
		}
		return super.performOk();
	}	
	
}
