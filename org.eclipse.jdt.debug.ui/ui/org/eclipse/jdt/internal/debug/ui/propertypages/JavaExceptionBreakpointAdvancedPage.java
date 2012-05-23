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

import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class JavaExceptionBreakpointAdvancedPage extends JavaBreakpointAdvancedPage {

	private ExceptionFilterEditor fFilterEditor;

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointAdvancedPage#doStore()
	 */
	@Override
	protected void doStore() {
		super.doStore();
		fFilterEditor.doStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointAdvancedPage#createTypeSpecificEditors(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createTypeSpecificEditors(Composite parent) {
		fFilterEditor= new ExceptionFilterEditor(parent, this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_EXCEPTION_BREAKPOINT_FILTERING_PROPERTY_PAGE);
	}

	protected Button createButton(Composite parent, String text) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(text);
		button.setFont(parent.getFont());
		button.setLayoutData(new GridData());
		return button;
	}

}
