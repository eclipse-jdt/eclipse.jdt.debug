/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.ui.launchConfigurations.AbstractVMInstallPage;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard page used to select a VM type.
 * 
 * @since 3.3
 */
public class VMTypePage extends WizardPage {
	
	private ListViewer fTypesViewer;
	
	private AbstractVMInstallPage fNextPage;
	
	/**
	 * Keep track of pages created, so we can dispose of them.
	 */
	private Set<AbstractVMInstallPage> fPages = new HashSet<AbstractVMInstallPage>();
	
	/**
	 * Label provider for VM types
	 */
	private class TypeLabelProvider extends LabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof IVMInstallType) {
				IVMInstallType type = (IVMInstallType) element;
				return type.getName();
			}
			return super.getText(element);
		}
		
	}
	
	/**
	 * Constructs a VM type selection page
	 */
	public VMTypePage() {
		super(JREMessages.VMTypePage_0);
		setDescription(JREMessages.VMTypePage_1);
		setTitle(JREMessages.VMTypePage_2);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		Iterator<AbstractVMInstallPage> iterator = fPages.iterator();
		while (iterator.hasNext()) {
			AbstractVMInstallPage page = iterator.next();
			page.dispose();
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		
		SWTFactory.createLabel(composite, JREMessages.VMTypePage_3, 1);
		
		fTypesViewer = new ListViewer(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 250;
        data.widthHint = 300;
        fTypesViewer.getControl().setFont(composite.getFont());
        fTypesViewer.getControl().setLayoutData(data);
        fTypesViewer.setContentProvider(new ArrayContentProvider());
        fTypesViewer.setLabelProvider(new TypeLabelProvider());
		fTypesViewer.setComparator(new ViewerComparator());
		fTypesViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				setPageComplete(true);
				updateNextPage();
				getWizard().getContainer().showPage(getNextPage());
			}
		});
		fTypesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()){
					setPageComplete(false);
				} else{
					setPageComplete(true);
					updateNextPage();
				}
			}
		});
		fTypesViewer.setInput(JavaRuntime.getVMInstallTypes());
		setControl(composite);
		fTypesViewer.setSelection(new StructuredSelection(JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE)));
		updateNextPage();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.ADD_NEW_JRE_WIZARD_PAGE);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_WIZBAN_LIBRARY);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		return fNextPage;
	}
	
	private void updateNextPage() {
		if (isPageComplete()) {
			IStructuredSelection selection = (IStructuredSelection)fTypesViewer.getSelection();
			if (!selection.isEmpty()){
				IVMInstallType installType = (IVMInstallType)selection.getFirstElement();
				AbstractVMInstallPage page = ((VMInstallWizard)getWizard()).getPage(installType);
				page.setWizard(getWizard());
				VMStandin standin = new VMStandin(installType, StandardVMPage.createUniqueId(installType));
				standin.setName(""); //$NON-NLS-1$
				page.setSelection(standin);
				fNextPage = page;
				fPages.add(page);
			}
		}		
	}

}
