package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
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
	// Primitive display preference widgets
	private Button fHexButton;
	private Button fCharButton;
	private Button fUnsignedButton;
	
	private PropertyChangeListener fPropertyChangeListener;
	
	protected class PropertyChangeListener implements IPropertyChangeListener {
		private boolean fHasStateChanged= false;
		
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IJDIPreferencesConstants.SHOW_HEX_VALUES)) {
				fHasStateChanged= true;
			} else if (event.getProperty().equals(IJDIPreferencesConstants.SHOW_CHAR_VALUES)) {
				fHasStateChanged= true;
			} else if (event.getProperty().equals(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES)) {
				fHasStateChanged= true;
			} else if (!event.getProperty().equals(IJDIPreferencesConstants.VARIABLE_RENDERING)) {
				return;
			}
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
		
		public boolean hasStateChanged() {
 			return fHasStateChanged;
 		}
	}
	

		
	public JavaDebugPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(getPropertyChangeListener());
	}
	
	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IJDIPreferencesConstants.SHOW_HEX_VALUES, false);
		store.setDefault(IJDIPreferencesConstants.SHOW_CHAR_VALUES, false);
		store.setDefault(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES, false);		
		store.setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);
		store.setDefault(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
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
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);		
		
		Composite comp= createLabelledComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePageSuspend_Execution_1")); //$NON-NLS-1$
		fSuspendButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_&execution_on_uncaught_exceptions_1")); //$NON-NLS-1$
		fSuspendOnCompilationErrors= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Suspend_execution_on_co&mpilation_errors_1")); //$NON-NLS-1$
		
		comp= createLabelledComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePageHot_Code_Replace_Error_Reporting_2")); //$NON-NLS-1$
		fAlertHCRButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_hot_code_replace_fails_1")); //$NON-NLS-1$
		fAlertObsoleteButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Alert_me_when_obsolete_methods_remain_1")); //$NON-NLS-1$
		
		createPrimitiveDisplayPreferences(composite);
		
		setValues();
		
		return composite;		
	}
	/**
	 * Create the primitive display preferences composite widget
	 */
	private void createPrimitiveDisplayPreferences(Composite parent) {
		Composite comp= createLabelledComposite(parent, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Primitive_type_display_options_2"));	 //$NON-NLS-1$
		
		fHexButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Display_&hexadecimal_values_(byte,_short,_char,_int,_long)_3")); //$NON-NLS-1$
		fCharButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Display_ASCII_&character_values_(byte,_short,_int,_long)_4")); //$NON-NLS-1$
		fUnsignedButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Display_&unsigned_values_(byte)_5")); //$NON-NLS-1$
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
			//only fire the notification if the user has toggled a button.
			getPreferenceStore().firePropertyChangeEvent(IJDIPreferencesConstants.VARIABLE_RENDERING, new Boolean(true), new Boolean(false));
		}
		return true;
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
		fHexButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.SHOW_HEX_VALUES));
		fCharButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.SHOW_CHAR_VALUES));
		fUnsignedButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES));
		
		fSuspendButton.setSelection(store.getBoolean(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
		fSuspendOnCompilationErrors.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
		fAlertHCRButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.ALERT_HCR_FAILED));
		fAlertObsoleteButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.ALERT_OBSOLETE_METHODS));
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
	 * Creates composite control and sets the default layout data.
	 *
	 * @param parent  the parent of the new composite
	 * @param numColumns  the number of columns for the new composite
	 * @param labelText  the text label of the new composite
	 * @return the newly-created composite
	 */
	private Composite createLabelledComposite(Composite parent, int numColumns, String labelText) {
		Composite comp = new Composite(parent, SWT.NONE);
		
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		comp.setLayout(layout);
		//GridData
		GridData gd= new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		comp.setLayoutData(gd);
		
		//Label
		Label label = new Label(comp, SWT.NONE);
		label.setText(labelText);
		gd = new GridData();
		gd.horizontalSpan = numColumns;
		label.setLayoutData(gd);
		return comp;
	}
		
	/**
	 * Set the values of the component widgets based on the
	 * values in the preference store
	 */
	private void setValues() {
		IPreferenceStore store = getPreferenceStore();
		
		fHexButton.setSelection(store.getBoolean(IJDIPreferencesConstants.SHOW_HEX_VALUES));
		fCharButton.setSelection(store.getBoolean(IJDIPreferencesConstants.SHOW_CHAR_VALUES));
		fUnsignedButton.setSelection(store.getBoolean(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES));		
		fSuspendButton.setSelection(store.getBoolean(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS));
		fSuspendOnCompilationErrors.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS));
		fAlertHCRButton.setSelection(store.getBoolean(IJDIPreferencesConstants.ALERT_HCR_FAILED));
		fAlertObsoleteButton.setSelection(store.getBoolean(IJDIPreferencesConstants.ALERT_OBSOLETE_METHODS));
	}
	
	/**
	 * Store the preference values based on the state of the
	 * component widgets
	 */
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IJDIPreferencesConstants.SHOW_HEX_VALUES, fHexButton.getSelection());
		store.setValue(IJDIPreferencesConstants.SHOW_CHAR_VALUES, fCharButton.getSelection());
		store.setValue(IJDIPreferencesConstants.SHOW_UNSIGNED_VALUES, fUnsignedButton.getSelection());
		store.setValue(IJDIPreferencesConstants.SUSPEND_ON_UNCAUGHT_EXCEPTIONS, fSuspendButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, fSuspendOnCompilationErrors.getSelection());
		store.setValue(IJDIPreferencesConstants.ALERT_HCR_FAILED, fAlertHCRButton.getSelection());
		store.setValue(IJDIPreferencesConstants.ALERT_OBSOLETE_METHODS, fAlertObsoleteButton.getSelection());
	}
	protected PropertyChangeListener getPropertyChangeListener() {
		if (fPropertyChangeListener == null) {
			fPropertyChangeListener= new PropertyChangeListener();
		}
		return fPropertyChangeListener;
	}
	
	/**
	 * @see Dialog#dispose()
	 */
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(getPropertyChangeListener());
	}
}

