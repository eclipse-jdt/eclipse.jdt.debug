package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for debug preferences that apply specifically to
 * Java Debugging.
 */
public class JavaDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// Suspend preference widgets
	private Button fSuspendButton;
	private Button fSuspendOnCompilationErrors;
	// Hot code replace preference widgets
	private Button fAlertHCRButton;
	private Button fAlertHCRNotSupportedButton;
	private Button fAlertObsoleteButton;
	private Button fPerformHCRWithCompilationErrors;
	// Timeout preference widgets
	private IntegerFieldEditor fTimeoutText;
	private IntegerFieldEditor fConnectionTimeoutText;
	
	private PropertyChangeListener fPropertyChangeListener;
	
	protected class PropertyChangeListener implements IPropertyChangeListener {
		private boolean fHasStateChanged= false;
		
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES)) {
				fHasStateChanged= true;
			} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES)) {
				fHasStateChanged= true;
			} else if (event.getProperty().equals(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES)) {
				fHasStateChanged= true;
			}
		}
		
		protected boolean hasStateChanged() {
 			return fHasStateChanged;
 		}
	}
	

		
	public JavaDebugPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(getPropertyChangeListener());
		setDescription(DebugUIMessages.getString("JavaDebugPreferencePage.description")); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_DEBUG_PREFERENCE_PAGE);
		
		Font font = parent.getFont();
		
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);		
		composite.setFont(font);
		
		Composite comp= createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_Execution_1")); //$NON-NLS-1$
		fSuspendButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_&execution_on_uncaught_exceptions_1")); //$NON-NLS-1$
		fSuspendOnCompilationErrors= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_execution_on_co&mpilation_errors_1")); //$NON-NLS-1$
		
		createSpacer(composite, 1);
		
		comp= createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Hot_Code_Replace_2")); //$NON-NLS-1$
		fAlertHCRButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_hot_code_replace_fails_1")); //$NON-NLS-1$
		fAlertHCRNotSupportedButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_hot_code_replace_is_not_supported_1")); //$NON-NLS-1$
		fAlertObsoleteButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_obsolete_methods_remain_1")); //$NON-NLS-1$
		fPerformHCRWithCompilationErrors= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Replace_classfiles_containing_compilation_errors_1")); //$NON-NLS-1$
		
		createSpacer(composite, 1);
		
		comp = createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Communication_1")); //$NON-NLS-1$
		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite(comp, SWT.NONE);
		layout = new GridLayout();
		spacingComposite.setLayout(layout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData(data);
		spacingComposite.setFont(font);
		
		IPreferenceStore store= JDIDebugUIPlugin.getDefault().getPreferenceStore();
		int minValue;
		
		fTimeoutText = new IntegerFieldEditor(JDIDebugModel.PREF_REQUEST_TIMEOUT, DebugUIMessages.getString("JavaDebugPreferencePage.Debugger_&timeout__2"), spacingComposite); //$NON-NLS-1$
		fTimeoutText.setPreferenceStore(store);
		fTimeoutText.setPreferencePage(this);
		fTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		minValue= store.getDefaultInt(JDIDebugModel.PREF_REQUEST_TIMEOUT);
		fTimeoutText.setValidRange(minValue, Integer.MAX_VALUE);
		fTimeoutText.setErrorMessage(MessageFormat.format(DebugUIMessages.getString("JavaDebugPreferencePage.Value_must_be_a_valid_integer_greater_than_{0}_ms_1"), new Object[] {new Integer(minValue)})); //$NON-NLS-1$
		fTimeoutText.load();
		fTimeoutText.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(fTimeoutText.isValid());
			}
		});
		fConnectionTimeoutText = new IntegerFieldEditor(JavaRuntime.PREF_CONNECT_TIMEOUT, DebugUIMessages.getString("JavaDebugPreferencePage.&Launch_timeout_(ms)__1"), spacingComposite); //$NON-NLS-1$
		fConnectionTimeoutText.setPreferenceStore(store);
		fConnectionTimeoutText.setPreferencePage(this);
		fConnectionTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		minValue= store.getDefaultInt(JavaRuntime.PREF_CONNECT_TIMEOUT);
		fConnectionTimeoutText.setValidRange(minValue, Integer.MAX_VALUE);
		fConnectionTimeoutText.setErrorMessage(MessageFormat.format(DebugUIMessages.getString("JavaDebugPreferencePage.Value_must_be_a_valid_integer_greater_than_{0}_ms_1"), new Object[] {new Integer(minValue)})); //$NON-NLS-1$
		fConnectionTimeoutText.load();
		fConnectionTimeoutText.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(fConnectionTimeoutText.isValid());
			}
		});
		// cannot set preference store, as it is a core preference
				
		setValues();
		
		return composite;		
	}
		
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 * Also, notifies interested listeners
	 */
	public boolean performOk() {
		storeValues();
		if (getPropertyChangeListener().hasStateChanged()) {
			refreshViews();
		}
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
		JDIDebugModel.savePreferences();
		JavaRuntime.savePreferences();
		return true;
	}
	
	/**
	 * Refresh the variables and expression views as changes
	 * have occurred that affects these views.
	 */
	private void refreshViews() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Refresh interested views
				IWorkbenchWindow[] windows= JDIDebugUIPlugin.getDefault().getWorkbench().getWorkbenchWindows();
				IWorkbenchPage page= null;
				for (int i= 0; i < windows.length; i++) {
					page= windows[i].getActivePage();
					if (page != null) {
						refreshViews(page, IDebugUIConstants.ID_EXPRESSION_VIEW);
						refreshViews(page, IDebugUIConstants.ID_VARIABLE_VIEW);
					}
				}
			}
		});
	}
	
	/**
	 * Refresh all views in the given workbench page with the given view id
	 */
	private void refreshViews(IWorkbenchPage page, String viewID) {
		IViewPart part= page.findView(viewID);
		if (part != null) {
			IDebugView adapter= (IDebugView)part.getAdapter(IDebugView.class);
			if (adapter != null) {
				Viewer viewer= adapter.getViewer();
				if (viewer instanceof StructuredViewer) {
					((StructuredViewer)viewer).refresh();
				}
			}
		}
	}
	
	/**
	 * Sets the default preferences.
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();	
	}
	
	private void setDefaultValues() {
		IPreferenceStore store = getPreferenceStore();		
		fSuspendButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
		fSuspendOnCompilationErrors.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
		fAlertHCRButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED));
		fAlertHCRNotSupportedButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED));
		fAlertObsoleteButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS));
		fPerformHCRWithCompilationErrors.setSelection(store.getDefaultBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS));
		fTimeoutText.setStringValue(new Integer(store.getDefaultInt(JDIDebugModel.PREF_REQUEST_TIMEOUT)).toString());
		fConnectionTimeoutText.setStringValue(new Integer(store.getDefaultInt(JavaRuntime.PREF_CONNECT_TIMEOUT)).toString());
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);		
		// FieldEditor GridData
		GridData data = new GridData();	
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		
		return button;
	}
	
	/**
	 * Creates composite group and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @param labelText  the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createGroupComposite(Composite parent, int numColumns, String labelText) {
		Group comp = new Group(parent, SWT.NONE);
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		comp.setLayout(layout);
		//GridData
		GridData gd= new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		comp.setLayoutData(gd);
		comp.setText(labelText);
		comp.setFont(parent.getFont());
		return comp;
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
		fPerformHCRWithCompilationErrors.setSelection(JDIDebugModel.getPreferences().getBoolean(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS));
		fTimeoutText.setStringValue(new Integer(JDIDebugModel.getPreferences().getInt(JDIDebugModel.PREF_REQUEST_TIMEOUT)).toString());
		fConnectionTimeoutText.setStringValue(new Integer(JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT)).toString());
	}
	
	/**
	 * Store the preference values based on the state of the
	 * component widgets
	 */
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, fSuspendButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, fSuspendOnCompilationErrors.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, fAlertHCRButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, fAlertHCRNotSupportedButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, fAlertObsoleteButton.getSelection());
		JDIDebugModel.getPreferences().setValue(JDIDebugModel.PREF_HCR_WITH_COMPILATION_ERRORS, fPerformHCRWithCompilationErrors.getSelection());
		JDIDebugModel.getPreferences().setValue(JDIDebugModel.PREF_REQUEST_TIMEOUT, fTimeoutText.getIntValue());
		JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_CONNECT_TIMEOUT, fConnectionTimeoutText.getIntValue());
	}
	
	protected PropertyChangeListener getPropertyChangeListener() {
		if (fPropertyChangeListener == null) {
			fPropertyChangeListener= new PropertyChangeListener();
		}
		return fPropertyChangeListener;
	}
	
	/**
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(getPropertyChangeListener());
	}
	
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
}