
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class JavaDebugWorkInProgressPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Button fUseASTSupport;

	public JavaDebugWorkInProgressPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION, false);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);		
		
		new Label(composite, SWT.LEFT).setText("Experimental debugger features");
		fUseASTSupport= createCheckButton(composite, "Use AST evaluation. Enables remote, inner type, and conditional breakpoint evaluation.");
		fUseASTSupport.setSelection(getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION));
		
		return composite;	
	}
	
	public boolean performOk() {
		boolean checked= fUseASTSupport.getSelection();
		EvaluationManager.useASTEvaluationEngine(checked);
		getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION, checked);
		return true;
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT | SWT.WRAP);
		button.setText(label);
		// FieldEditor GridData
		GridData data = new GridData();	
		button.setLayoutData(data);
		
		return button;
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
