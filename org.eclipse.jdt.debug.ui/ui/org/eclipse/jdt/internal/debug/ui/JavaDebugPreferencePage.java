/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

 
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Preference page for debug preferences that apply specifically to
 * Java Debugging.
 */
public class JavaDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {
	
	/**
	 * This class exists to provide visibility to the
	 * <code>refreshValidState</code> method and to perform more intelligent
	 * clearing of the error message.
	 */
	protected class JavaDebugIntegerFieldEditor extends IntegerFieldEditor {						
		
		public JavaDebugIntegerFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		@Override
		protected void refreshValidState() {
			super.refreshValidState();
		}

		@Override
		protected void clearErrorMessage() {
			if (canClearErrorMessage()) {
				super.clearErrorMessage();
			}
		}
	}
	
	// Suspend preference widgets
	private Button fSuspendButton;
	private Button fSuspendOnCompilationErrors;
	private Button fSuspendDuringEvaluations;
	private Button fOpenInspector;
	private Button fPromptUnableToInstallBreakpoint;
	private Button fPromptDeleteConditionalBreakpoint;
	private Combo fSuspendVMorThread;
	private Combo fWatchpoint;
	
	// Hot code replace preference widgets
	private Button fAlertHCRButton;
	private Button fAlertHCRNotSupportedButton;
	private Button fAlertObsoleteButton;
	private Button fPerformHCRWithCompilationErrors;
	// Timeout preference widgets
	private JavaDebugIntegerFieldEditor fTimeoutText;
	private JavaDebugIntegerFieldEditor fConnectionTimeoutText;

	public JavaDebugPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.JavaDebugPreferencePage_description); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_DEBUG_PREFERENCE_PAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		//The main composite
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 0, 0, GridData.FILL);
		PreferenceLinkArea link = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.debug.ui.DebugPreferencePage", DebugUIMessages.JavaDebugPreferencePage_0, //$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);
		link.getControl().setFont(composite.getFont());
		Group group = SWTFactory.createGroup(composite, DebugUIMessages.JavaDebugPreferencePage_Suspend_Execution_1, 2, 1, GridData.FILL_HORIZONTAL); 
		fSuspendButton = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Suspend__execution_on_uncaught_exceptions_1, null, false, 2); 
		fSuspendOnCompilationErrors = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Suspend_execution_on_co_mpilation_errors_1, null, false, 2); 
		fSuspendDuringEvaluations = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_14, null, false, 2);
		fOpenInspector = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_20, null, false, 2);
		
		SWTFactory.createLabel(group, DebugUIMessages.JavaDebugPreferencePage_21, 1);
		fSuspendVMorThread = new Combo(group, SWT.BORDER|SWT.READ_ONLY);
		fSuspendVMorThread.setItems(new String[]{DebugUIMessages.JavaDebugPreferencePage_22, DebugUIMessages.JavaDebugPreferencePage_23});
		fSuspendVMorThread.setFont(group.getFont());
		
		SWTFactory.createLabel(group, DebugUIMessages.JavaDebugPreferencePage_24, 1);
		fWatchpoint = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		fWatchpoint.setItems(new String[] {DebugUIMessages.JavaDebugPreferencePage_25, DebugUIMessages.JavaDebugPreferencePage_26, DebugUIMessages.JavaDebugPreferencePage_27});
		fWatchpoint.setFont(group.getFont());
			
		group = SWTFactory.createGroup(composite, DebugUIMessages.JavaDebugPreferencePage_Hot_Code_Replace_2, 1, 1, GridData.FILL_HORIZONTAL);
		fAlertHCRButton = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Alert_me_when_hot_code_replace_fails_1, null, false, 1); 
		fAlertHCRNotSupportedButton = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Alert_me_when_hot_code_replace_is_not_supported_1, null, false, 1); 
		fAlertObsoleteButton = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Alert_me_when_obsolete_methods_remain_1, null, false, 1); 
		fPerformHCRWithCompilationErrors = SWTFactory.createCheckButton(group, DebugUIMessages.JavaDebugPreferencePage_Replace_classfiles_containing_compilation_errors_1, null, false, 1);  

		group = SWTFactory.createGroup(composite, DebugUIMessages.JavaDebugPreferencePage_Communication_1, 1, 1, GridData.FILL_HORIZONTAL);
		Composite space = SWTFactory.createComposite(group, group.getFont(), 1, 1, GridData.FILL_HORIZONTAL);
		
		fTimeoutText = new JavaDebugIntegerFieldEditor(JDIDebugModel.PREF_REQUEST_TIMEOUT, DebugUIMessages.JavaDebugPreferencePage_Debugger__timeout__2, space);
		fTimeoutText.setPage(this);
		fTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		fTimeoutText.setValidRange(JDIDebugModel.DEF_REQUEST_TIMEOUT, Integer.MAX_VALUE);
		fTimeoutText.setErrorMessage(NLS.bind(DebugUIMessages.JavaDebugPreferencePage_Value_must_be_a_valid_integer_greater_than__0__ms_1, new Object[] {new Integer(JDIDebugModel.DEF_REQUEST_TIMEOUT)})); 
		fTimeoutText.load();
		fConnectionTimeoutText = new JavaDebugIntegerFieldEditor(JavaRuntime.PREF_CONNECT_TIMEOUT, DebugUIMessages.JavaDebugPreferencePage__Launch_timeout__ms___1, space); 
		fConnectionTimeoutText.setPage(this);
		fConnectionTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		fConnectionTimeoutText.setValidRange(JavaRuntime.DEF_CONNECT_TIMEOUT, Integer.MAX_VALUE);
		fConnectionTimeoutText.setErrorMessage(NLS.bind(DebugUIMessages.JavaDebugPreferencePage_Value_must_be_a_valid_integer_greater_than__0__ms_1, new Object[] {new Integer(JavaRuntime.DEF_CONNECT_TIMEOUT)})); 
		fConnectionTimeoutText.load();
		
		SWTFactory.createVerticalSpacer(composite, 1);
		fPromptUnableToInstallBreakpoint = SWTFactory.createCheckButton(composite, DebugUIMessages.JavaDebugPreferencePage_19, null, false, 1);
		fPromptDeleteConditionalBreakpoint= SWTFactory.createCheckButton(composite, DebugUIMessages.JavaDebugPreferencePage_promptWhenDeletingCondidtionalBreakpoint, null, false, 1);
		
		setValues();
		fTimeoutText.setPropertyChangeListener(this);
		fConnectionTimeoutText.setPropertyChangeListener(this);
		return composite;		
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, fAlertHCRButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, fAlertHCRNotSupportedButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, fAlertObsoleteButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, fSuspendButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, fSuspendOnCompilationErrors.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT, fPromptUnableToInstallBreakpoint.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT, fPromptDeleteConditionalBreakpoint.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION, fOpenInspector.getSelection());
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.putBoolean(JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, fSuspendDuringEvaluations.getSelection());
			int selectionIndex = fSuspendVMorThread.getSelectionIndex();
			int policy = IJavaBreakpoint.SUSPEND_THREAD;
			if (selectionIndex > 0) {
				policy = IJavaBreakpoint.SUSPEND_VM;
			}
			prefs.putInt(JDIDebugPlugin.PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY, policy);
			prefs.putInt(JDIDebugPlugin.PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY, fWatchpoint.getSelectionIndex());
			prefs.putBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, fPerformHCRWithCompilationErrors.getSelection());
			prefs.putInt(JDIDebugModel.PREF_REQUEST_TIMEOUT, fTimeoutText.getIntValue());
			try {
				prefs.flush();
			}
			catch (BackingStoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		if(prefs != null) {
			prefs.putInt(JavaRuntime.PREF_CONNECT_TIMEOUT, fConnectionTimeoutText.getIntValue());
			try {
				prefs.flush();
			}
			catch (BackingStoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		fSuspendButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
		fSuspendOnCompilationErrors.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
		fAlertHCRButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED));
		fAlertHCRNotSupportedButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED));
		fAlertObsoleteButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS));
		fPromptUnableToInstallBreakpoint.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT));
		fPromptDeleteConditionalBreakpoint.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT));
		fOpenInspector.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION));
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			fSuspendDuringEvaluations.setSelection(prefs.getBoolean(JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, true));
			int value = prefs.getInt(JDIDebugPlugin.PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY, IJavaBreakpoint.SUSPEND_THREAD);
			fSuspendVMorThread.select((value == IJavaBreakpoint.SUSPEND_THREAD) ? 0 : 1);
			fWatchpoint.select(prefs.getInt(JDIDebugPlugin.PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY, 0));
			fPerformHCRWithCompilationErrors.setSelection(prefs.getBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, true));
			fTimeoutText.setStringValue(new Integer(prefs.getInt(JDIDebugModel.PREF_REQUEST_TIMEOUT, JDIDebugModel.DEF_REQUEST_TIMEOUT)).toString());
		}
		prefs = DefaultScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		if(prefs != null) {
			fConnectionTimeoutText.setStringValue(new Integer(prefs.getInt(JavaRuntime.PREF_CONNECT_TIMEOUT,JavaRuntime.DEF_CONNECT_TIMEOUT)).toString());
		}
		super.performDefaults();	
	}
	
	/**
	 * Set the values of the component widgets based on the
	 * values in the preference store
	 */
	private void setValues() {
		IPreferenceStore store = getPreferenceStore();
		fSuspendButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
		fSuspendOnCompilationErrors.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
		fAlertHCRButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED));
		fAlertHCRNotSupportedButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED));
		fAlertObsoleteButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS));
		fPromptUnableToInstallBreakpoint.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_ALERT_UNABLE_TO_INSTALL_BREAKPOINT));
		fPromptDeleteConditionalBreakpoint.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_PROMPT_DELETE_CONDITIONAL_BREAKPOINT));
		fOpenInspector.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION));
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugPlugin.getUniqueIdentifier());
		if(prefs != null) {
			fSuspendDuringEvaluations.setSelection(prefs.getBoolean(JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, true));
			int value = prefs.getInt(JDIDebugPlugin.PREF_DEFAULT_BREAKPOINT_SUSPEND_POLICY, IJavaBreakpoint.SUSPEND_THREAD);
			fSuspendVMorThread.select((value == IJavaBreakpoint.SUSPEND_THREAD ? 0 : 1));
			fWatchpoint.select(prefs.getInt(JDIDebugPlugin.PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY, 0));
			fPerformHCRWithCompilationErrors.setSelection(prefs.getBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, true));
			fTimeoutText.setStringValue(new Integer(prefs.getInt(JDIDebugModel.PREF_REQUEST_TIMEOUT, JDIDebugModel.DEF_REQUEST_TIMEOUT)).toString());
		}
		prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		if(prefs != null) {
			fConnectionTimeoutText.setStringValue(new Integer(prefs.getInt(JavaRuntime.PREF_CONNECT_TIMEOUT,JavaRuntime.DEF_CONNECT_TIMEOUT)).toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
			// If the new value is true then we must check all field editors.
			// If it is false, then the page is invalid in any case.
			if (newValue) {
				if (fTimeoutText != null && event.getSource() != fTimeoutText) {
					fTimeoutText.refreshValidState();
				} 
				if (fConnectionTimeoutText != null && event.getSource() != fConnectionTimeoutText) {
					fConnectionTimeoutText.refreshValidState();
				}
			} 
			setValid(fTimeoutText.isValid() && fConnectionTimeoutText.isValid());
			getContainer().updateButtons();
			updateApplyButton();
		}
	}

	/**
	 * if the error message can be cleared or not
	 * @return true if the error message can be cleared, false otherwise
	 */
	protected boolean canClearErrorMessage() {
		if (fTimeoutText.isValid() && fConnectionTimeoutText.isValid()) {
			return true;
		}
		return false;
	}	
}
