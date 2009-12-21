/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.ui.breakpoints.JavaBreakpointConditionEditor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;

/**
 * Property page for editing breakpoints of type
 * <code>org.eclipse.jdt.debug.core.IJavaLineBreakpoint</code>.
 */
public class JavaLineBreakpointPage extends JavaBreakpointPage {
	
	private JavaBreakpointConditionEditor fConditionEditor;
	private String fPrevMessage;
	// Watchpoint editors
	private Button fFieldAccess;
	private Button fFieldModification;
	// Method breakpoint editors
	private Button fMethodEntry;
	private Button fMethodExit;
	
	private static final String fgWatchpointError = PropertyPageMessages.JavaLineBreakpointPage_0; 
	private static final String fgMethodBreakpointError = PropertyPageMessages.JavaLineBreakpointPage_1;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#doStore()
	 */
	protected void doStore() throws CoreException {
		IJavaLineBreakpoint breakpoint= (IJavaLineBreakpoint) getBreakpoint();
		super.doStore();
		if (fConditionEditor != null) {
			fConditionEditor.doSave();
		}
		if (breakpoint instanceof IJavaWatchpoint) {
			IJavaWatchpoint watchpoint= (IJavaWatchpoint) getBreakpoint();
			boolean access = fFieldAccess.getSelection();
			boolean modification = fFieldModification.getSelection();
			if (access != watchpoint.isAccess()) {
				watchpoint.setAccess(access);
			}
			if (modification != watchpoint.isModification()) {
				watchpoint.setModification(modification);
			}
		}
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			IJavaMethodBreakpoint methodBreakpoint= (IJavaMethodBreakpoint) getBreakpoint();
			boolean entry = fMethodEntry.getSelection();
			boolean exit = fMethodExit.getSelection();
			if (entry != methodBreakpoint.isEntry()) {
				methodBreakpoint.setEntry(entry);
			}
			if (exit != methodBreakpoint.isExit()) {
				methodBreakpoint.setExit(exit);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#createTypeSpecificLabels(org.eclipse.swt.widgets.Composite)
	 */
	protected void createTypeSpecificLabels(Composite parent) {
		// Line number
		IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) getBreakpoint();
		StringBuffer lineNumber = new StringBuffer(4);
		try {
			int lNumber = breakpoint.getLineNumber();
			if (lNumber > 0) {
				lineNumber.append(lNumber);
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
		if (lineNumber.length() > 0) {
			createLabel(parent, PropertyPageMessages.JavaLineBreakpointPage_2); 
			Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, lineNumber.toString());
			text.setBackground(parent.getBackground());
		}
		// Member
		try {
			IMember member = BreakpointUtils.getMember(breakpoint);
			if (member == null) {
				return;
			}
			String label = PropertyPageMessages.JavaLineBreakpointPage_3; 
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				label = PropertyPageMessages.JavaLineBreakpointPage_4; 
			} else if (breakpoint instanceof IJavaWatchpoint) {
				label = PropertyPageMessages.JavaLineBreakpointPage_5; 
			}
			createLabel(parent, label);
			Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, fJavaLabelProvider.getText(member));
			text.setBackground(parent.getBackground());
		} 
		catch (CoreException exception) {JDIDebugUIPlugin.log(exception);}
	}
	
	/**
	 * Create the condition editor and associated editors.
	 * @see org.eclipse.jdt.internal.debug.ui.propertypages.JavaBreakpointPage#createTypeSpecificEditors(org.eclipse.swt.widgets.Composite)
	 */
	protected void createTypeSpecificEditors(Composite parent) throws CoreException {
		setTitle(PropertyPageMessages.JavaLineBreakpointPage_18);
		IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) getBreakpoint();
		if (breakpoint.supportsCondition()) {
			createConditionEditor(parent);
		}
		if (breakpoint instanceof IJavaWatchpoint) {
			setTitle(PropertyPageMessages.JavaLineBreakpointPage_19);
			IJavaWatchpoint watchpoint= (IJavaWatchpoint) getBreakpoint();
			SelectionAdapter watchpointValidator= new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					validateWatchpoint();
				}
			};
			createLabel(parent, PropertyPageMessages.JavaLineBreakpointPage_6); 
			fEnabledButton.addSelectionListener(watchpointValidator);
			fFieldAccess = createCheckButton(parent, PropertyPageMessages.JavaLineBreakpointPage_7); 
			fFieldAccess.setSelection(watchpoint.isAccess());
			fFieldAccess.addSelectionListener(watchpointValidator);
			fFieldModification = createCheckButton(parent, PropertyPageMessages.JavaLineBreakpointPage_8); 
			fFieldModification.setSelection(watchpoint.isModification());
			fFieldModification.addSelectionListener(watchpointValidator);
		}
		if (breakpoint instanceof IJavaMethodBreakpoint) {
			setTitle(PropertyPageMessages.JavaLineBreakpointPage_20);
			IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) getBreakpoint();
			SelectionAdapter methodBreakpointValidator= new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					validateMethodBreakpoint();
				}
			}; 
			createLabel(parent, PropertyPageMessages.JavaLineBreakpointPage_9); 
			fEnabledButton.addSelectionListener(methodBreakpointValidator);
			fMethodEntry = createCheckButton(parent, PropertyPageMessages.JavaLineBreakpointPage_10); 
			fMethodEntry.setSelection(methodBreakpoint.isEntry());
			fMethodEntry.addSelectionListener(methodBreakpointValidator);
			fMethodExit = createCheckButton(parent, PropertyPageMessages.JavaLineBreakpointPage_11); 
			fMethodExit.setSelection(methodBreakpoint.isExit());
			fMethodExit.addSelectionListener(methodBreakpointValidator);
		}
	}
	
	/**
	 * Validates the watchpoint...if we are one
	 */
	private void validateWatchpoint() {
		if (fEnabledButton.getSelection() && !(fFieldAccess.getSelection() || fFieldModification.getSelection())) {
			addErrorMessage(fgWatchpointError);
		} 
		else {
			removeErrorMessage(fgWatchpointError);
		}
	}
	
	/**
	 * Validates the method breakpoint, if we are one
	 */
	private void validateMethodBreakpoint() {
		boolean valid = true;
		if (fEnabledButton.getSelection() && !(fMethodEntry.getSelection() || fMethodExit.getSelection())) {
			setErrorMessage(fgMethodBreakpointError);
			valid = false;
		} 
		else {
			setErrorMessage(null);
		}
		setValid(valid);
	}
	
	/**
	 * Creates the controls that allow the user to specify the breakpoint's
	 * condition
	 * @param parent the composite in which the condition editor should be created
	 * @throws CoreException if an exception occurs accessing the breakpoint
	 */
	private void createConditionEditor(Composite parent) throws CoreException {
		IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) getBreakpoint();		
		Composite conditionComposite = SWTFactory.createGroup(parent, EMPTY_STRING, 1, 1, GridData.FILL_BOTH);
		fConditionEditor = new JavaBreakpointConditionEditor(); 
		fConditionEditor.createControl(conditionComposite);
		fConditionEditor.setInput(breakpoint);
		fConditionEditor.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				IStatus status = fConditionEditor.getStatus();
				if (status.isOK()) {
					if (fPrevMessage != null) {
						removeErrorMessage(fPrevMessage);
						fPrevMessage = null;
					}
				} else {
					fPrevMessage = status.getMessage();
					addErrorMessage(fPrevMessage);
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#convertHeightInCharsToPixels(int)
	 */
	public int convertHeightInCharsToPixels(int chars) {
		return super.convertHeightInCharsToPixels(chars);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#convertWidthInCharsToPixels(int)
	 */
	public int convertWidthInCharsToPixels(int chars) {
		return super.convertWidthInCharsToPixels(chars);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_LINE_BREAKPOINT_PROPERTY_PAGE);
	}
}
