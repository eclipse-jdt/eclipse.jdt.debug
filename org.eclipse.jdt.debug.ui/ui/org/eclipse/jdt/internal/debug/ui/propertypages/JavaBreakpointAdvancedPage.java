/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * 
 */
public class JavaBreakpointAdvancedPage extends PropertyPage {

	ThreadFilterEditor fThreadFilterEditor;
	InstanceFilterEditor fInstanceFilterEditor;

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		doStore();
		return super.performOk();
	}

	/**
	 * Stores the values configured in this page.
	 */
	protected void doStore() {
		fThreadFilterEditor.doStore();
		if (fInstanceFilterEditor != null) {
			fInstanceFilterEditor.doStore();
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		mainComposite.setLayout(new GridLayout());
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createThreadFilterEditor(mainComposite);
		createTypeSpecificEditors(mainComposite);
		createInstanceFilterEditor(mainComposite);
		setValid(true);
		return mainComposite;
	}
	
	public void createInstanceFilterEditor(Composite parent) {
		IJavaBreakpoint breakpoint= getBreakpoint();
		try {
			IJavaObject[] instances = breakpoint.getInstanceFilters();
			if (instances.length > 0) {
				fInstanceFilterEditor= new InstanceFilterEditor(parent, breakpoint);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Allow subclasses to create type-specific editors.
	 * @param parent
	 */
	protected void createTypeSpecificEditors(Composite parent) {
		// Do nothing.
	}

	protected void createThreadFilterEditor(Composite parent) {
		fThreadFilterEditor = new ThreadFilterEditor(parent, this);
	}

	public IJavaBreakpoint getBreakpoint() {
		return (IJavaBreakpoint) getElement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_BREAKPOINT_ADVANCED_PROPERTY_PAGE);
	}
}
