/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;


import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Displays details of a VM install (read only, for contributed VMs).
 * 
 * @since 3.2
 */
public class VMDetailsDialog extends Dialog {
	
	private IVMInstall fVM;
		
	public VMDetailsDialog(Shell shell, IVMInstall vm) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);		
		fVM= vm;
	}
	
	/**
	 * @see Windows#configureShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JREMessages.VMDetailsDialog_0); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaDebugHelpContextIds.JRE_DETAILS_DIALOG);
	}		
			
	@Override
	protected Control createDialogArea(Composite ancestor) {
		Composite parent = (Composite)super.createDialogArea(ancestor);
		GridLayout layout = new GridLayout(2, false);
		layout.makeColumnsEqualWidth = false;
		parent.setLayout(layout);
		
		// type
		createLabel(parent, JREMessages.addVMDialog_jreType);
		createLabel(parent, fVM.getVMInstallType().getName());

		// name
		createLabel(parent, JREMessages.addVMDialog_jreName);
		createLabel(parent, fVM.getName());
		
		// home
		createLabel(parent, JREMessages.addVMDialog_jreHome);
		createLabel(parent, fVM.getInstallLocation().getAbsolutePath());
		
		// vm args
		SWTFactory.createLabel(parent, JREMessages.AddVMDialog_23, 2);
		String text = null;
		if (fVM instanceof IVMInstall2) {
			text = ((IVMInstall2)fVM).getVMArgs();
		} else {
			String[] args = fVM.getVMArguments();
			if (args != null) {
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < args.length; i++) {
					buf.append(args[i]);
					if (i < (args.length - 1)) {
						buf.append(" "); //$NON-NLS-1$
					}
				}
				text = buf.toString();
			}
		}
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		Text argText = SWTFactory.createText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL, 2, text);
		GridData gd = (GridData) argText.getLayoutData();
		gd.heightHint = 75;
		gd.widthHint = 300;
		
		// libraries
		SWTFactory.createLabel(parent, JREMessages.AddVMDialog_JRE_system_libraries__1, 2);
		TreeViewer libraryViewer= new TreeViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 100;
		libraryViewer.getControl().setLayoutData(gd);
		LibraryContentProvider provider = new LibraryContentProvider();
		libraryViewer.setContentProvider(provider);
		libraryViewer.setLabelProvider(new LibraryLabelProvider());
		libraryViewer.setInput(this);
		provider.setLibraries(JavaRuntime.getLibraryLocations(fVM));
		
		applyDialogFont(parent);
		return parent;
	}
	
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return "VM_DETAILS_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
	protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         } 
         return section;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}    
}
