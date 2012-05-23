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
package org.eclipse.jdt.internal.ui.macbundler;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;


public class BundleWizardPage1 extends BundleWizardBasePage {
	
	static String[] JVMS= {
		"1.3+",	//$NON-NLS-1$
		"1.3*",	//$NON-NLS-1$
		"1.4.2",	//$NON-NLS-1$
		"1.4+",	//$NON-NLS-1$
		"1.4*",	//$NON-NLS-1$
		"1.5+",	//$NON-NLS-1$
		"1.5*",	//$NON-NLS-1$
		"1.6+",	//$NON-NLS-1$
		"1.6*"	//$NON-NLS-1$
	};

	ILaunchConfiguration[] fConfigurations= new ILaunchConfiguration[0];
	Combo fLocation;	
	Combo fLaunchConfigs;
	Combo fJVMVersion;
	Text fAppName;
	Text fMainClass;
	Text fArguments;
	Text fIconFileName;
	Button fUseSWT;
	
	
	public BundleWizardPage1(BundleDescription bd) {
		super("page1", bd); //$NON-NLS-1$
	}

	@Override
	public void createContents(Composite c) {
		
		final Shell shell= c.getShell();
				
		Composite c1= createComposite(c, 2);
			createLabel(c1, Util.getString("page1.launchConfig.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
		
			fLaunchConfigs= new Combo(c1, SWT.READ_ONLY);
			fillCombo(fLaunchConfigs);
			fLaunchConfigs.addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int ix= fLaunchConfigs.getSelectionIndex();
						if (ix > 0 && ix < fConfigurations.length) {
							fBundleDescription.clear();
							fBundleDescription.inititialize(fConfigurations[ix]);
						}
					}
				}
			);
			
					
		Group c2= createGroup(c, "Main", 2); //$NON-NLS-1$
			createLabel(c2, Util.getString("page1.mainClass.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			Composite c7a= createHBox(c2);
			
				fMainClass= createText(c7a, MAINCLASS, 1);
				Button b1= createButton(c7a, SWT.NONE, Util.getString("page1.mainClass.chooseButton.label")); //$NON-NLS-1$
				b1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MessageBox mb= new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
						mb.setMessage(Util.getString("page1.mainClass.dialog.message")); //$NON-NLS-1$
						mb.setText(Util.getString("page1.mainClass.dialog.title")); //$NON-NLS-1$
						mb.open();
					}
				});
				
				createLabel(c2, Util.getString("page1.arguments.label"), GridData.VERTICAL_ALIGN_BEGINNING); //$NON-NLS-1$
				fArguments= createText(c2, ARGUMENTS, 2);
					
		Group c5= createGroup(c, "Destination", 2); //$NON-NLS-1$	
			createLabel(c5, Util.getString("page1.appName.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			fAppName= createText(c5, APPNAME, 1);
		
			createLabel(c5, Util.getString("page1.appFolder.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			Composite c3a= createHBox(c5);
			
				fLocation= createCombo(c3a, DESTINATIONDIRECTORY);
				
				final Button browse= createButton(c3a, SWT.NONE, Util.getString("page1.appFolder.browseButton.label")); //$NON-NLS-1$
				browse.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog dd= new DirectoryDialog(browse.getShell(), SWT.SAVE);
						dd.setMessage(Util.getString("page1.appFolder.browseDialog.message")); //$NON-NLS-1$
						dd.setText(Util.getString("page1.appFolder.browseDialog.title")); //$NON-NLS-1$
						String name= dd.open();
						if (name != null)
							fLocation.setText(name);
					}
				});
		
		Group g6= createGroup(c, "Options", 2); //$NON-NLS-1$
		
			createLabel(g6, Util.getString("page1.jvm.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			
			Composite c8= createComposite(g6, 4);
			
				fJVMVersion= new Combo(c8, SWT.READ_ONLY);
				for (int i= 0; i < JVMS.length; i++)
					fJVMVersion.add(JVMS[i]);
				fJVMVersion.setText(JVMS[4]);
				hookField(fJVMVersion, JVMVERSION);
				createLabel(c8, "      ", GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
				createLabel(c8, Util.getString("page1.useSWT.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
				fUseSWT= createButton(c8, SWT.CHECK, null);
				hookButton(fUseSWT, USES_SWT);
			
			createLabel(g6, Util.getString("page1.appIcon.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			Composite c7= createComposite(g6, 2);
				fIconFileName= createText(c7, ICONFILE, 1);
				final Button b= createButton(c7, SWT.NONE, Util.getString("page1.appIcon.chooseButton.label")); //$NON-NLS-1$
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FileDialog fd= new FileDialog(b.getShell(), SWT.OPEN);
						fd.setText(Util.getString("page1.appIcon.chooseDialog.title")); //$NON-NLS-1$
						fd.setFilterExtensions(new String[] { "icns" }); //$NON-NLS-1$
						String name= fd.open();
						if (name != null)
							fIconFileName.setText(name);
					}
				});
			
	}
	
	@Override
	void enterPage() {
		super.enterPage();
		initCombo(fLaunchConfigs);
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (fAppName != null)
			fAppName.setText(fBundleDescription.get(APPNAME, "")); //$NON-NLS-1$
		if (fMainClass != null)
			fMainClass.setText(fBundleDescription.get(MAINCLASS, "")); //$NON-NLS-1$
		if (fJVMVersion != null)
			fJVMVersion.setText(fBundleDescription.get(JVMVERSION, "")); //$NON-NLS-1$
		if (fUseSWT != null)
			fUseSWT.setSelection(fBundleDescription.get(USES_SWT, false));
	}
	
	public boolean isPageComplete() {
		return fAppName != null && fAppName.getText().length() > 0 && fLocation.getText().length() > 0;
	}

	// private stuff
	
	private void collectLaunchConfigs() {
		ArrayList<ILaunchConfiguration> configs= new ArrayList<ILaunchConfiguration>();
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type= manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		try {
			ILaunchConfiguration[] configurations= manager.getLaunchConfigurations(type);
			for (int i= 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration= configurations[i];
				if (BundleDescription.verify(configuration))
					configs.add(configuration);
			}
		} catch (CoreException e) {
			//
		}
		fConfigurations= configs.toArray(new ILaunchConfiguration[configs.size()]);
		Arrays.sort(fConfigurations, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				ILaunchConfiguration lc1= (ILaunchConfiguration) o1;
				ILaunchConfiguration lc2= (ILaunchConfiguration) o2;
				return lc1.getName().compareTo(lc2.getName());
			}

			@Override
			public boolean equals(Object obj) {
				return false;
			}
		});
	}
	
	private void fillCombo(Combo c) {
		collectLaunchConfigs();
		for (int i= 0; i < fConfigurations.length; i++) {
			ILaunchConfiguration configuration= fConfigurations[i];
			c.add(configuration.getName());
		}
	}
	
	private void initCombo(Combo c) {
		IStructuredSelection sel= ((MacBundleWizard)getWizard()).getSelection();
		Object o= sel.getFirstElement();
		if (o instanceof IJavaElement) {
			IJavaProject project= ((IJavaElement) o).getJavaProject();
			if (project != null) {
				for (int i= 0; i < fConfigurations.length; i++) {
					ILaunchConfiguration configuration= fConfigurations[i];
					if (BundleDescription.matches(configuration, project)) {
						c.setText(configuration.getName());
						fBundleDescription.inititialize(configuration);
						return;
					}
				}
			}
		}
	}	
}
