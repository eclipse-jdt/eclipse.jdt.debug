
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class JavaDebugWorkInProgressPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Button fUseASTSupport;
	private Text fEvaluationTimeout;

	public JavaDebugWorkInProgressPreferencePage() {
		super();
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION, true);
		store.setDefault(IJDIPreferencesConstants.PREF_EVALUATION_TIMEOUT, 5);
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
		
		int timeout= getPreferenceStore().getInt(IJDIPreferencesConstants.PREF_EVALUATION_TIMEOUT);
		fEvaluationTimeout= createTextField(composite, "" + timeout, "Evaluation timeout (in seconds)");
		fEvaluationTimeout.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isValid()) {
					setValid(true);
					setErrorMessage(null);
				} else {
					setValid(false);
					setErrorMessage("Evaluation timeout must be a valid integer");
				}
			}
		});

		
		return composite;	
	}
	
	public boolean isValid() {
		if (isPositiveInteger(fEvaluationTimeout.getText())) {
			return true;
		}
		return false;
	}
	
	private boolean isPositiveInteger(String value) {
		try {
			return Integer.parseInt(value) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public boolean performOk() {
		boolean checked= fUseASTSupport.getSelection();
		EvaluationManager.useASTEvaluationEngine(checked);
		getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION, checked);
		getPreferenceStore().setValue(IJDIPreferencesConstants.PREF_EVALUATION_TIMEOUT, Integer.parseInt(fEvaluationTimeout.getText()));
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
		fEvaluationTimeout.setText("" + store.getDefaultInt(IJDIPreferencesConstants.PREF_EVALUATION_TIMEOUT));
		fUseASTSupport.setSelection(store.getDefaultBoolean(IJDIPreferencesConstants.PREF_USE_AST_EVALUATION));
	}
	
	private Text createTextField(Composite parent, String startingText, String labelText) {
		Composite editArea = new Composite(parent, SWT.NONE);
		GridData data = new GridData(SWT.NONE);
		data.horizontalSpan = GridData.FILL_HORIZONTAL;
		data.horizontalAlignment = GridData.BEGINNING;
		editArea.setLayoutData(data);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		editArea.setLayout(layout);
				
		Label label= new Label(editArea, SWT.NONE);
		label.setText(labelText);
		GridData labelData= new GridData(SWT.NONE);
		labelData.horizontalAlignment= GridData.BEGINNING;
		labelData.horizontalSpan= 1;
		label.setLayoutData(labelData);
		
		Text text= new Text(editArea, SWT.SINGLE | SWT.BORDER);
		text.setText(startingText);		
		GridData textData= new GridData(SWT.NONE);
		textData.widthHint= 100;
		textData.horizontalSpan= 1;
		textData.horizontalAlignment= GridData.BEGINNING;
		text.setLayoutData(textData);
		
		return text;
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
