package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
	// Alert preference widgets
	private Button fAlertHCRButton;
	private Button fAlertObsoleteButton;
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
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES, false);		
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, true);
		store.setDefault(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, true);
		
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES, true);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_FINAL_FIELDS, false);
		store.setDefault(IJDIPreferencesConstants.PREF_SHOW_STATIC_FIELDS, false);
	}
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.JAVA_DEBUG_PREFERENCE_PAGE);
		
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
		
		Composite comp= createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_Execution_1")); //$NON-NLS-1$
		fSuspendButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_&execution_on_uncaught_exceptions_1")); //$NON-NLS-1$
		fSuspendOnCompilationErrors= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_execution_on_co&mpilation_errors_1")); //$NON-NLS-1$
		
		createSpacer(composite, 1);
		
		comp= createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Hot_Code_Replace_Error_Reporting_2")); //$NON-NLS-1$
		fAlertHCRButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_hot_code_replace_fails_1")); //$NON-NLS-1$
		fAlertObsoleteButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_obsolete_methods_remain_1")); //$NON-NLS-1$
		
		createSpacer(composite, 1);
		
		comp = createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Communication_1")); //$NON-NLS-1$
		fTimeoutText = new IntegerFieldEditor(JDIDebugModel.PREF_REQUEST_TIMEOUT, DebugUIMessages.getString("JavaDebugPreferencePage.Debugger_&timeout__2"), comp); //$NON-NLS-1$
		fTimeoutText.setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		fTimeoutText.setPreferencePage(this);
		fTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		fTimeoutText.setValidRange(50, Integer.MAX_VALUE);
		fTimeoutText.setErrorMessage("Value must be a valid integer greater than 50 ms");
		fTimeoutText.load();
		fTimeoutText.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(fTimeoutText.isValid());
			}
		});
		fConnectionTimeoutText = new IntegerFieldEditor(JavaRuntime.PREF_CONNECT_TIMEOUT, DebugUIMessages.getString("JavaDebugPreferencePage.&Launch_timeout_(ms)__1"), comp); //$NON-NLS-1$
		fConnectionTimeoutText.setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		fConnectionTimeoutText.setPreferencePage(this);
		fConnectionTimeoutText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
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
					refreshViews(page, IDebugUIConstants.ID_EXPRESSION_VIEW);
					refreshViews(page, IDebugUIConstants.ID_VARIABLE_VIEW);
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
		fAlertObsoleteButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS));
		fTimeoutText.setStringValue(new Integer(JDIDebugModel.DEF_REQUEST_TIMEOUT).toString());
		fConnectionTimeoutText.setStringValue(new Integer(JavaRuntime.DEF_CONNECT_TIMEOUT).toString());
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
		fAlertObsoleteButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS));
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
		store.setValue(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, fAlertObsoleteButton.getSelection());
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