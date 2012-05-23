/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.6
 */
public class StandardJavaBreakpointEditor extends AbstractJavaBreakpointEditor {
	
	private IJavaBreakpoint fBreakpoint;
	private Button fHitCountButton;
	private Text fHitCountText;
	private Button fSuspendThread;
	private Button fSuspendVM;
	
	/**
     * Property id for hit count enabled state.
     */
    public static final int PROP_HIT_COUNT_ENABLED = 0x1005;
    
	/**
     * Property id for breakpoint hit count.
     */
    public static final int PROP_HIT_COUNT = 0x1006;  
    
	/**
     * Property id for suspend policy.
     */
    public static final int PROP_SUSPEND_POLICY = 0x1007;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		return createStandardControls(parent);
	}
	
	protected Control createStandardControls(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 4, 1, 0, 0, 0);
		fHitCountButton = SWTFactory.createCheckButton(composite, processMnemonics(PropertyPageMessages.JavaBreakpointPage_4), null, false, 1);
		fHitCountButton.setLayoutData(new GridData());
		fHitCountButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean enabled = fHitCountButton.getSelection();
				fHitCountText.setEnabled(enabled);
				if(enabled) {
					fHitCountText.setFocus();
				}
				setDirty(PROP_HIT_COUNT_ENABLED);
			}
		});
		fHitCountText = SWTFactory.createSingleText(composite, 1);
		GridData gd = (GridData) fHitCountText.getLayoutData();
		gd.minimumWidth = 50;
		fHitCountText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(PROP_HIT_COUNT);
			}
		});
		SWTFactory.createLabel(composite, "", 1); // spacer //$NON-NLS-1$
		Composite radios = SWTFactory.createComposite(composite, composite.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
		fSuspendThread = SWTFactory.createRadioButton(radios, processMnemonics(PropertyPageMessages.JavaBreakpointPage_7), 1);
		fSuspendThread.setLayoutData(new GridData());
		fSuspendVM = SWTFactory.createRadioButton(radios, processMnemonics(PropertyPageMessages.JavaBreakpointPage_8), 1);
		fSuspendVM.setLayoutData(new GridData());
		fSuspendThread.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(PROP_SUSPEND_POLICY);
			}
		});	
		fSuspendVM.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(PROP_SUSPEND_POLICY);
			}
		});
		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object breakpoint) throws CoreException {
		try {
			suppressPropertyChanges(true);
			if (breakpoint instanceof IJavaBreakpoint) {
				setBreakpoint((IJavaBreakpoint) breakpoint);
			} else {
				setBreakpoint(null);
			}
		} finally {
			suppressPropertyChanges(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getInput()
	 */
	@Override
	public Object getInput() {
		return fBreakpoint;
	}
	
	/**
	 * Sets the breakpoint to edit. The same editor can be used iteratively for different breakpoints.
	 * 
	 * @param breakpoint the breakpoint to edit or <code>null</code> if none
	 * @exception CoreException if unable to access breakpoint attributes
	 */
	protected void setBreakpoint(IJavaBreakpoint breakpoint) throws CoreException {
		fBreakpoint = breakpoint;
		boolean enabled = false;
		boolean hasHitCount = false;
		String text = ""; //$NON-NLS-1$
		boolean suspendThread = true;
		if (breakpoint != null) {
			enabled = true;
			int hitCount = breakpoint.getHitCount();
			if (hitCount > 0) {
				text = new Integer(hitCount).toString();
				hasHitCount = true;
			}
			suspendThread= breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD;
		}
		fHitCountButton.setEnabled(enabled);
		fHitCountButton.setSelection(enabled & hasHitCount);
		fHitCountText.setEnabled(hasHitCount);
		fHitCountText.setText(text);
		fSuspendThread.setEnabled(enabled);
		fSuspendVM.setEnabled(enabled);
		fSuspendThread.setSelection(suspendThread);
		fSuspendVM.setSelection(!suspendThread);
		setDirty(false);
	}
	
	/**
	 * Returns the current breakpoint being edited or <code>null</code> if none.
	 * 
	 * @return breakpoint or <code>null</code>
	 */
	protected IJavaBreakpoint getBreakpoint() { 
		return fBreakpoint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setFocus()
	 */
	@Override
	public void setFocus() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#doSave()
	 */
	@Override
	public void doSave() throws CoreException {
		if (fBreakpoint != null) {
			int suspendPolicy = IJavaBreakpoint.SUSPEND_THREAD;
			if(fSuspendVM.getSelection()) {
				suspendPolicy = IJavaBreakpoint.SUSPEND_VM;
			}
			fBreakpoint.setSuspendPolicy(suspendPolicy);
			int hitCount = -1;
			if (fHitCountButton.getSelection()) {
				try {
					hitCount = Integer.parseInt(fHitCountText.getText());
				} 
				catch (NumberFormatException e) {
					throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, e));
				}
			}
			fBreakpoint.setHitCount(hitCount);
		}
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		if (fHitCountButton.getSelection()) {
			String hitCountText= fHitCountText.getText();
			int hitCount= -1;
			try {
				hitCount = Integer.parseInt(hitCountText);
			} catch (NumberFormatException e1) {
				return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, null);
			}
			if (hitCount < 1) {
				return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, null);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates and returns a check box button with the given text.
	 * 
	 * @param parent parent composite
	 * @param text label
	 * @param propId property id to fire on modification
	 * @return check box
	 */
	protected Button createSusupendPropertyEditor(Composite parent, String text, final int propId) {
		Button button = new Button(parent, SWT.CHECK);
		button.setFont(parent.getFont());
		button.setText(text);
		GridData gd = new GridData(SWT.BEGINNING);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(propId);
			}
		});
		return button;
	}
}
