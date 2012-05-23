/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * Provides a type dialog extension for the JDT type selection dialog
 * 
 * @since 3.4
 */
public class AddExceptionTypeDialogExtension extends TypeSelectionExtension {
	
	/**
	  * widgets
	  */
	 private Button fCaughtButton;
	 private Button fUncaughtButton;
	 private boolean fCaught = false;
	 private boolean fUncaught = false;
	
	 /**
	 * Constructor
	 * @param caught
	 * @param uncaught
	 */
	public AddExceptionTypeDialogExtension(boolean caught, boolean uncaught) {
		 fCaught = caught;
		 fUncaught = uncaught;
	 }
	 
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.dialogs.TypeSelectionExtension#createContentArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createContentArea(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL);
		fCaughtButton = SWTFactory.createCheckButton(comp, BreakpointMessages.AddExceptionDialog_15, null, fCaught, 1);
		fCaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fCaught = fCaughtButton.getSelection();
			}
		});
		fUncaughtButton = SWTFactory.createCheckButton(comp, BreakpointMessages.AddExceptionDialog_16, null, fUncaught, 1);
		fUncaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fUncaught = fUncaughtButton.getSelection();
			}
		});
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.dialogs.TypeSelectionExtension#getSelectionValidator()
	 */
	@Override
	public ISelectionStatusValidator getSelectionValidator() {
		return new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if(selection.length == 1) {
					try {
						IType type = (IType) selection[0];
			            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
			            IType curr = type;
			            while (curr != null) {
			                if ("java.lang.Throwable".equals(curr.getFullyQualifiedName('.'))) { //$NON-NLS-1$
			                    return Status.OK_STATUS;
			                }
			                curr = hierarchy.getSuperclass(curr);
			            }
			        } 
			        catch (JavaModelException e) {
			        	JDIDebugUIPlugin.log(e);
			        	return Status.CANCEL_STATUS;
			        }
				}
				return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), BreakpointMessages.AddExceptionDialog_13);
			}
			
		};
	}
	
	/**
	 * Returns if the breakpoint should be set to suspend when the associated exception is thrown, but caught
	 * @return if the breakpoint should be set to suspend when the associated exception is thrown, but caught
	 */
	public boolean shouldHandleCaughtExceptions() {
		return fCaught;
	}
	
	/**Returns if the breakpoint should be set to suspend when the associated exception is thrown, but not caught
	 * @return if the breakpoint should be set to suspend when the associated exception is thrown, but not caught
	 */
	public boolean shouldHandleUncaughtExceptions() {
		return fUncaught;
	}	
}
