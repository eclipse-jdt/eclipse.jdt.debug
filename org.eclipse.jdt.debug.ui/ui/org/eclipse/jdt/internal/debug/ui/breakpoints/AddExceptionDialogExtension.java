/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.StatusInfo;
import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * 
 * This class adds the extensions for the AddExceptionDialog, to use the new Camal cased selection dialog
 * @since 3.2
 *
 */
public class AddExceptionDialogExtension extends TypeSelectionExtension {

	 /**
	  * widgets
	  */
	 private Button fCaughtButton;
	 private Button fUncaughtButton;
	 private boolean fCaught = false;
	 private boolean fUncaught = false;
	
	/**
	 * Constructor
	 */
	public AddExceptionDialogExtension(boolean caught, boolean uncaught) {
		super();
		fCaught = caught;
		fUncaught = uncaught;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.dialogs.TypeSelectionExtension#createContentArea(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContentArea(Composite parent) {
		super.createContentArea(parent);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fCaughtButton = new Button(comp, SWT.CHECK);
        fCaughtButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fCaughtButton.setFont(comp.getFont());
        fCaughtButton.setText(BreakpointMessages.AddExceptionDialog_15); 
        fCaughtButton.setSelection(fCaught);
        fCaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fCaught = fCaughtButton.getSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
        });
        fUncaughtButton = new Button(comp, SWT.CHECK);
        fUncaughtButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fUncaughtButton.setFont(comp.getFont());
        fUncaughtButton.setText(BreakpointMessages.AddExceptionDialog_16); 
        fUncaughtButton.setSelection(fUncaught);
        fUncaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fUncaught = fUncaughtButton.getSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}       	
        });
		return comp;
	}

	/**
	 * Returns if the caught button has been checked or not 
	 * @return if the caught button has been checked or not
	 */
	public boolean catchExceptions() {
		return fCaught;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.dialogs.TypeSelectionExtension#getSelectionValidator()
     */
    public ISelectionStatusValidator getSelectionValidator() {
    	ISelectionStatusValidator validator = new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				IType type = null;
				for(int i = 0; i < selection.length; i ++) {
					type = (IType)selection[i];
					if(!isException(type)) {
						return new StatusInfo(IStatus.ERROR, BreakpointMessages.AddExceptionDialogExtension_0);
					}
				}
				return new StatusInfo(IStatus.OK, ""); //$NON-NLS-1$
			}
    		
    	};
		return validator;
	}

    /**
     * Returns if the exception is checked or not
     * @param type the type of the exception breakpoint
     * @return true if it is a checked exception, false other wise
     * @since 3.2
     */
    protected boolean isException(IType type) {
        if(type != null) {
	    	try {
	            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
	            IType curr = type;
	            while (curr != null) {
	                if ("java.lang.Throwable".equals(curr.getFullyQualifiedName('.'))) { //$NON-NLS-1$
	                    return true;
	                }
	                curr = hierarchy.getSuperclass(curr);
	            }
	        } 
	        catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
        }
        return false;
    }
    
	/**
     * Returns if the uncaught button has been checked or not
     * @return if the uncaught button has been checked or not
     */
    public boolean uncaughtExceptions() {
    	return fUncaught;
    }
}
