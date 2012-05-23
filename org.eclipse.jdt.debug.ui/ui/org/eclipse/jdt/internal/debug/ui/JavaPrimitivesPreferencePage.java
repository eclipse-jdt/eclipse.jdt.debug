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

 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page for debug preferences that apply specifically to
 * Java Debugging.
 */
public class JavaPrimitivesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private List<BooleanFieldEditor> fEdtiors = new ArrayList<BooleanFieldEditor>();

	public JavaPrimitivesPreferencePage() {
		super(DebugUIMessages.JavaPrimitivesPreferencePage_0);
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.JavaPrimitivesPreferencePage_1); 
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_PRIMITIVES_PREFERENCE_PAGE);
		
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
		
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		
		BooleanFieldEditor editor = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_HEX, 
				DebugUIMessages.JavaDebugPreferencePage_Display__hexadecimal_values__byte__short__char__int__long__3,
				composite);
		editor.setPreferenceStore(preferenceStore);
		fEdtiors.add(editor);
		editor = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_CHAR,
				DebugUIMessages.JavaDebugPreferencePage_Display_ASCII__character_values__byte__short__int__long__4,
				composite);
		editor.setPreferenceStore(preferenceStore);
		fEdtiors.add(editor);
		editor = new BooleanFieldEditor(IJDIPreferencesConstants.PREF_SHOW_UNSIGNED,
				DebugUIMessages.JavaDebugPreferencePage_Display__unsigned_values__byte__5,
				composite);
		editor.setPreferenceStore(preferenceStore);
		fEdtiors.add(editor);
		
		loadValues();
		applyDialogFont(composite);
		return composite;		
	}
		
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 * Also, notifies interested listeners
	 */
	@Override
	public boolean performOk() {
		storeValues();
		return true;
	}
	
	/**
	 * Sets the default preferences.
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		Iterator<BooleanFieldEditor> iterator = fEdtiors.iterator();
		while (iterator.hasNext()) {
			BooleanFieldEditor editor = iterator.next();
			editor.loadDefault();
		}
		super.performDefaults();	
	}
			
	/**
	 * Set the values of the component widgets based on the
	 * values in the preference store
	 */
	private void loadValues() {
		Iterator<BooleanFieldEditor> iterator = fEdtiors.iterator();
		while (iterator.hasNext()) {
			BooleanFieldEditor editor = iterator.next();
			editor.load();
		}
	}
	
	/**
	 * Store the preference values based on the state of the
	 * component widgets
	 */
	private void storeValues() {
		Iterator<BooleanFieldEditor> iterator = fEdtiors.iterator();
		while (iterator.hasNext()) {
			BooleanFieldEditor editor = iterator.next();
			editor.store();
		}
	}

}
