/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;

/**
 * Combines editors.
 * 
 * @since 3.6
 */
public class CompositeBreakpointEditor extends AbstractJavaBreakpointEditor {
	
	private AbstractJavaBreakpointEditor[] fEditors;
	
	public CompositeBreakpointEditor(AbstractJavaBreakpointEditor[] editors) {
		fEditors = editors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	@Override
	public void addPropertyListener(IPropertyListener listener) {
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].addPropertyListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#removePropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	@Override
	public void removePropertyListener(IPropertyListener listener) {
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].removePropertyListener(listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#dispose()
	 */
	@Override
	protected void dispose() {
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].dispose();
		}
		fEditors = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].createControl(comp);
		}
		return comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setFocus()
	 */
	@Override
	public void setFocus() {
		fEditors[0].setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#doSave()
	 */
	@Override
	public void doSave() throws CoreException {
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].doSave();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#isDirty()
	 */
	@Override
	public boolean isDirty() {
		for (int i = 0; i < fEditors.length; i++) {
			if (fEditors[i].isDirty()) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		for (int i = 0; i < fEditors.length; i++) {
			IStatus status = fEditors[i].getStatus();
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getInput()
	 */
	@Override
	public Object getInput() {
		return fEditors[0].getInput();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object breakpoint) throws CoreException {
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].setInput(breakpoint);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setMnemonics(boolean)
	 */
	@Override
	public void setMnemonics(boolean mnemonics) {
		super.setMnemonics(mnemonics);
		for (int i = 0; i < fEditors.length; i++) {
			fEditors[i].setMnemonics(mnemonics);
		}
	}

}
