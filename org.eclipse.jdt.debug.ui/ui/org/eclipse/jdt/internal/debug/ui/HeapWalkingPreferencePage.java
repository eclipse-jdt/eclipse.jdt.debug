/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Provides a page for changing the default settings for heap walking options
 * @since 3.3
 */
public class HeapWalkingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Constructor
	 */
	public HeapWalkingPreferencePage() {
		super(GRID);
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		setDescription(DebugUIMessages.HeapWalkingPreferencePage_0); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_HEAPWALKING_PREFERENCE_PAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		Group igroup = SWTUtil.createGroup(getFieldEditorParent(), DebugUIMessages.HeapWalkingPreferencePage_3, 1, 1, GridData.FILL_HORIZONTAL);
		Composite comp = SWTUtil.createComposite(igroup, igroup.getFont(), 1, 2, GridData.FILL_HORIZONTAL);
		SWTUtil.createLabel(comp, DebugUIMessages.HeapWalkingPreferencePage_4, 2);
		addField(new IntegerFieldEditor(IJavaDebugUIConstants.PREF_ALLINSTANCES_MAX_COUNT, DebugUIMessages.HeapWalkingPreferencePage_1, comp));
		addField(new IntegerFieldEditor(IJavaDebugUIConstants.PREF_ALLREFERENCES_MAX_COUNT, DebugUIMessages.HeapWalkingPreferencePage_2, comp));
	}
}
