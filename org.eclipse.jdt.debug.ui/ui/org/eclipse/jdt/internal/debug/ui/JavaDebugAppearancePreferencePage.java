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
public class JavaDebugAppearancePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// Primitive display preference widgets
	private Button fHexButton;
	private Button fCharButton;
	private Button fUnsignedButton;
	//view settings
	private Button fPackagesButton;
	
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
	

		
	public JavaDebugAppearancePreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(getPropertyChangeListener());
		setDescription(DebugUIMessages.getString("JavaDebugAppearancePreferencePage.Appearance_settings_for_Java_Debugging_1")); //$NON-NLS-1$
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_DEBUG_APPEARANCE_PREFERENCE_PAGE);
		
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
		
		
		createPrimitiveDisplayPreferences(composite);
		
		createSpacer(composite, 1);
		
		Composite comp= createGroupComposite(composite, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Opened_View_Default_Settings_1")); //$NON-NLS-1$
		fPackagesButton= createCheckButton(comp, DebugUIMessages.getString("JavaDebugPreferencePage.Show_&qualified_names_2")); //$NON-NLS-1$
		
		setValues();
		
		return composite;		
	}
	
	/**
	 * Create the primitive display preferences composite widget
	 */
	private void createPrimitiveDisplayPreferences(Composite parent) {
		Composite comp= createGroupComposite(parent, 1, DebugUIMessages.getString("JavaDebugPreferencePage.Primitive_type_display_options_2"));	 //$NON-NLS-1$
		
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
			refreshViews();
		}
		JDIDebugUIPlugin.getDefault().savePluginPreferences();
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
		fHexButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES));
		fCharButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES));
		fUnsignedButton.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES));
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
		
		fHexButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES));
		fCharButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES));
		fUnsignedButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES));		
		
		fPackagesButton.setSelection(store.getBoolean(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES));
	}
	
	/**
	 * Store the preference values based on the state of the
	 * component widgets
	 */
	private void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IJDIPreferencesConstants.PREF_SHOW_HEX_VALUES, fHexButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SHOW_CHAR_VALUES, fCharButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED_VALUES, fUnsignedButton.getSelection());
		store.setValue(IJDIPreferencesConstants.PREF_SHOW_QUALIFIED_NAMES, fPackagesButton.getSelection());
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