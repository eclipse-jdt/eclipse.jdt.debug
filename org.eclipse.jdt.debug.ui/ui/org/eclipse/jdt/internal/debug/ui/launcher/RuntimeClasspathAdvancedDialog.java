package org.eclipse.jdt.internal.debug.ui.launcher;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.PixelConverter;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathContainerDescriptor;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathContainerWizard;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog of radio buttons/actions for advanced classpath options.
 */
public class RuntimeClasspathAdvancedDialog extends Dialog {
	
	private IAction[] fActions;
	private Button[] fButtons;	
	
	private Button fAddContainerButton;
	private Combo fContainerCombo;
	private ClasspathContainerDescriptor[] fDescriptors;
	private RuntimeClasspathViewer fViewer;

	/**
	 * Constructs a new dialog on the given shell, with the specified
	 * set of actions.
	 * 
	 * @param parentShell
	 * @param actions advanced actions
	 */
	public RuntimeClasspathAdvancedDialog(Shell parentShell, IAction[] actions, RuntimeClasspathViewer viewer) {
		super(parentShell);
		fActions = actions;
		fViewer = viewer;
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		
		initializeDialogUnits(parent);
		
		Composite composite= (Composite) super.createDialogArea(parent);
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.makeColumnsEqualWidth = false;
		layout.numColumns = 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		inner.setLayout(layout);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		inner.setLayoutData(gd);
		
		Label l = new Label(inner, SWT.NONE);
		l.setText(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Select_an_advanced_option__1")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);
		l.setFont(font);
		
		fButtons = new Button[fActions.length];
		for (int i = 0; i < fActions.length; i++) {
			IAction action= fActions[i];
			fButtons[i] = new Button(inner, SWT.RADIO);
			fButtons[i].setText(action.getText());
			fButtons[i].setData(action);
			fButtons[i].setEnabled(action.isEnabled());
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			fButtons[i].setLayoutData(gd);
			fButtons[i].setFont(font);
		}
		
		fAddContainerButton = new Button(inner, SWT.RADIO);
		fAddContainerButton.setText(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Add_&Container__1")); //$NON-NLS-1$
		fAddContainerButton.setFont(font);
		
		fContainerCombo = new Combo(inner, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		fContainerCombo.setLayoutData(gd);
		fContainerCombo.setFont(font);
		fDescriptors= ClasspathContainerDescriptor.getDescriptors();
		String[] names= new String[fDescriptors.length];
		for (int i = 0; i < names.length; i++) {
			names[i]= fDescriptors[i].getName();
		}	
		fContainerCombo.setItems(names);
		fContainerCombo.select(0);
		
		new Label(inner, SWT.NONE);
		
		getShell().setText(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Advanced_Options_1")); //$NON-NLS-1$
		
		applyDialogFont(composite);
		return composite;

	}

	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		if (fAddContainerButton.getSelection()) {
			int index = fContainerCombo.getSelectionIndex();
			IRuntimeClasspathEntry entry = chooseContainerEntry(fDescriptors[index]);
			if (entry != null) {
				// check if duplicate
				int pos = fViewer.indexOf(entry);
				if (pos == -1) {
					fViewer.addEntries(new IRuntimeClasspathEntry[]{entry});
				}
			}
		} else {
			for (int i = 0; i < fButtons.length; i++) {
				if (fButtons[i].getSelection()) {
					IAction action = (IAction)fButtons[i].getData();
					if (action instanceof RuntimeClasspathAction) {
						((RuntimeClasspathAction)action).setShell(getShell());
					}
					action.run();
					break;
				}
			}
		}
		super.okPressed();
	}
	
	private IRuntimeClasspathEntry chooseContainerEntry(ClasspathContainerDescriptor desc) {
		IRuntimeClasspathEntry[] currentEntries = fViewer.getEntries();
		IClasspathEntry[] entries = new IClasspathEntry[currentEntries.length];
		for (int i = 0; i < entries.length; i++) {
			entries[i]= currentEntries[i].getClasspathEntry();
		}
		ClasspathContainerWizard wizard= new ClasspathContainerWizard(desc, null, entries);
		
		WizardDialog dialog= new WizardDialog(getShell(), wizard);
		PixelConverter converter= new PixelConverter(getShell());
		
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(40), converter.convertHeightInCharsToPixels(20));
		dialog.create();
		dialog.getShell().setText(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Select_Container_2")); //$NON-NLS-1$
		if (dialog.open() == WizardDialog.OK) {
			IClasspathEntry created= wizard.getNewEntry();
			if (created != null) {
				// XXX: kind needs to be resolved
				try {
					return JavaRuntime.newRuntimeContainerClasspathEntry(created.getPath(), IRuntimeClasspathEntry.STANDARD_CLASSES);
				} catch (CoreException e) {
					JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("RuntimeClasspathAdvancedDialog.Unable_to_create_new_entry._3"), e); //$NON-NLS-1$
				}
			}
		}			
		return null;
	}	
}
