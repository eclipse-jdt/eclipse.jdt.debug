/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public class JavaExceptionBreakpointAdvancedPage extends JavaBreakpointAdvancedPage {

	private ExceptionFilterEditor fFilterEditor;

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointAdvancedPage#doStore()
	 */
	protected void doStore() throws CoreException {
		super.doStore();
		fFilterEditor.doStore();
	}

	protected void createTypeSpecificEditors(Composite parent) {
		fFilterEditor= new ExceptionFilterEditor(parent, this);
	}
	
	protected Button createButton(Composite parent, String text) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(text);
		button.setFont(parent.getFont());
		button.setLayoutData(new GridData());
		return button;
	}

}
