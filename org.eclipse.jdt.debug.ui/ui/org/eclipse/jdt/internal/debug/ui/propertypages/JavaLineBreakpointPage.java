/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import com.ibm.icu.text.MessageFormat;

/**
 * Property page for editing breakpoints of type
 * <code>org.eclipse.jdt.debug.core.IJavaLineBreakpoint</code>.
 */
public class JavaLineBreakpointPage extends JavaBreakpointPage {
	
	private Button fEnableConditionButton;
	private BreakpointConditionEditor fConditionEditor;
	private Button fConditionIsTrue;
	private Button fConditionHasChanged;
	private Label fSuspendWhenLabel;
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
			boolean enableCondition = fEnableConditionButton.getSelection();
			String condition = fConditionEditor.getCondition();
			boolean suspendOnTrue= fConditionIsTrue.getSelection();
			if (breakpoint.isConditionEnabled() != enableCondition) {
				breakpoint.setConditionEnabled(enableCondition);
			}
			if (!condition.equals(breakpoint.getCondition())) {
				breakpoint.setCondition(condition);
			}
			if (breakpoint.isConditionSuspendOnTrue() != suspendOnTrue) {
				breakpoint.setConditionSuspendOnTrue(suspendOnTrue);
			}
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
		String label = null;
		if (BreakpointUtils.getType(breakpoint) != null) {
			IBindingService bindingService = (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
			if(bindingService != null) {
				TriggerSequence keyBinding = bindingService.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
				if (keyBinding != null) {
					label = MessageFormat.format(PropertyPageMessages.JavaLineBreakpointPage_12, new String[] {keyBinding.format()}); 
				} 
			}
		}
		if (label == null) {
			label = PropertyPageMessages.JavaLineBreakpointPage_13; 
		}
		
		Composite conditionComposite = SWTFactory.createGroup(parent, EMPTY_STRING, 1, 1, GridData.FILL_BOTH);
		fEnableConditionButton = createCheckButton(conditionComposite, label);
		fEnableConditionButton.setSelection(breakpoint.isConditionEnabled());
		fEnableConditionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setConditionEnabled(fEnableConditionButton.getSelection());
			}
		});
		fConditionEditor = new BreakpointConditionEditor(conditionComposite, this); 
		fSuspendWhenLabel = createLabel(conditionComposite, PropertyPageMessages.JavaLineBreakpointPage_15); 
		fConditionIsTrue = createRadioButton(conditionComposite, PropertyPageMessages.JavaLineBreakpointPage_16); 
		fConditionHasChanged = createRadioButton(conditionComposite, PropertyPageMessages.JavaLineBreakpointPage_17); 
		if (breakpoint.isConditionSuspendOnTrue()) {
			fConditionIsTrue.setSelection(true);
		} 
		else {
			fConditionHasChanged.setSelection(true);
		}
		setConditionEnabled(fEnableConditionButton.getSelection());
	}

	/**
	 * Sets the enabled state of the condition editing controls.
	 * @param enabled
	 */
	private void setConditionEnabled(boolean enabled) {
		fConditionEditor.setEnabled(enabled);
		fSuspendWhenLabel.setEnabled(enabled);
		fConditionIsTrue.setEnabled(enabled);
		fConditionHasChanged.setEnabled(enabled);
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
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		if (fConditionEditor != null) {
			fConditionEditor.dispose();
		}
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_LINE_BREAKPOINT_PROPERTY_PAGE);
	}
}
