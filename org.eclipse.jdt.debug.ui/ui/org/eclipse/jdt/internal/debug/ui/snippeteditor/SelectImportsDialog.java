/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;


import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.ui.filtertable.Filter;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.ButtonLabel;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.DialogLabels;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterStorage;
import org.eclipse.jdt.internal.ui.filtertable.JavaFilterTable.FilterTableConfig;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class SelectImportsDialog extends TitleAreaDialog {

	private JavaFilterTable fJavaFilterTable;
	private JavaSnippetEditor fEditor;

	public SelectImportsDialog(JavaSnippetEditor editor, String[] imports) {
		super(editor.getShell());
		fEditor= editor;
		fJavaFilterTable = new JavaFilterTable(new FilterStorage() {

			@Override
			public Filter[] getStoredFilters(boolean defaults) {
				if (imports == null) {
					return new Filter[0];
				}
				Filter[] result = new Filter[imports.length];
				for (int i = 0; i < imports.length; i++) {
					result[i] = new Filter(imports[i], true);
				}
				return result;
			}

			@Override
			public void setStoredFilters(IPreferenceStore store, Filter[] filters) {
				String[] newImports = new String[filters.length];
				for (int i = 0; i < filters.length; i++) {
					newImports[i] = filters[i].getName();
				}
				fEditor.setImports(newImports);
			}

		},
				new FilterTableConfig()
					.setAddType(new ButtonLabel(
							SnippetMessages.getString("SelectImportsDialog.Add_&Type_1"), //$NON-NLS-1$
							SnippetMessages.getString("SelectImportsDialog.Choose_a_Type_to_Add_as_an_Import_2"))) //$NON-NLS-1$
					.setAddPackage(new ButtonLabel(
							SnippetMessages.getString("SelectImportsDialog.Add_&Package_3"), //$NON-NLS-1$
							SnippetMessages.getString("SelectImportsDialog.Choose_a_Package_to_Add_as_an_Import_4"))) //$NON-NLS-1$
					.setRemove(new ButtonLabel(
							SnippetMessages.getString("SelectImportsDialog.&Remove_5"), //$NON-NLS-1$
							SnippetMessages.getString("SelectImportsDialog.Remove_All_Selected_Imports_6"))) //$NON-NLS-1$
					.setAddPackageDialog(new DialogLabels(
							SnippetMessages.getString("SelectImportsDialog.Add_package_as_import_7"), //$NON-NLS-1$
							SnippetMessages.getString("SelectImportsDialog.&Select_a_package_to_add_as_an_Import_10"))) //$NON-NLS-1$
					.setAddTypeDialog(new DialogLabels(
							SnippetMessages.getString("SelectImportsDialog.Add_Type_as_Import_12"), //$NON-NLS-1$
							SnippetMessages.getString("SelectImportsDialog.&Select_a_type_to_add_to_add_as_an_import_15"))) //$NON-NLS-1$
					.setConsiderAllTypes(true)
					.setCheckable(false)
					.setLabelText(SnippetMessages.getString("SelectImportsDialog.imports_heading")) //$NON-NLS-1$
					.setHelpContextId(IJavaDebugHelpContextIds.SNIPPET_IMPORTS_DIALOG)) {
			@Override
			protected IJavaSearchScope getTypeSearchScope() {
				return SearchEngine.createJavaSearchScope(new IJavaElement[]{fEditor.getJavaProject()}, true);
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(NLS.bind(SnippetMessages.getString("SelectImportsDialog.Manage_the_Java_Snippet_Editor_Imports_for___{0}__1"), fEditor.getEditorInput().getName())); //$NON-NLS-1$
		setMessage(NLS.bind(SnippetMessages.getString("SelectImportsDialog.add_remove_imports"), fEditor.getEditorInput().getName())); //$NON-NLS-1$
		Composite outer = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
		GridLayout gl = (GridLayout) outer.getLayout();
		gl.marginLeft = 7;
		gl.marginTop = 0;
		gl.marginBottom = 0;

		PlatformUI.getWorkbench().getHelpSystem().setHelp(outer, IJavaDebugHelpContextIds.SNIPPET_IMPORTS_DIALOG);

		fJavaFilterTable.createTable(outer);

		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fJavaFilterTable.performOk(null);
		super.okPressed();
	}

	/**
	 * Sets the title for the dialog and establishes the help context.
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SnippetMessages.getString("SelectImportsDialog.Java_Snippet_Imports_18")); //$NON-NLS-1$
	}
}
