/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.Filter;
import org.eclipse.jdt.internal.debug.ui.FilterLabelProvider;
import org.eclipse.jdt.internal.debug.ui.FilterViewerComparator;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public class SelectImportsDialog extends TitleAreaDialog {

	private String[] fImports;
	private Button fAddPackageButton;
	private Button fAddTypeButton;
	private Button fRemoveImportsButton;
	private TableViewer fImportsViewer;
	private Table fImportsTable;
	private JavaSnippetEditor fEditor;

	private ImportsContentProvider fImportContentProvider;

	/**
	 * Content provider for the table.  Content consists of instances of Filter.
	 */
	protected class ImportsContentProvider implements IStructuredContentProvider {

		private TableViewer fViewer;
		private List<Filter> fImportNames;

		public ImportsContentProvider(TableViewer viewer) {
			fViewer = viewer;
			populateImports();
		}

		protected void populateImports() {
			fImportNames= new ArrayList<>(1);
			if (fImports != null) {
				for (int i = 0; i < fImports.length; i++) {
					String name = fImports[i];
					addImport(name);
				}
			}
		}

		protected void addImport(String name) {
			Filter imprt = new Filter(name, false);
			if (!fImportNames.contains(imprt)) {
				fImportNames.add(imprt);
				fViewer.add(imprt);
			}
		}


		protected void removeImports(Object[] imports) {
			for (int i = 0; i < imports.length; i++) {
				Filter imprt = (Filter)imports[i];
				fImportNames.remove(imprt);
			}
			fViewer.remove(imports);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return fImportNames.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}
	}

	public SelectImportsDialog(JavaSnippetEditor editor, String[] imports) {
		super(editor.getShell());
		fEditor= editor;
		fImports= imports;
	}

	private void createImportButtons(Composite container) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, IJavaDebugHelpContextIds.SNIPPET_IMPORTS_DIALOG);
		Composite bcomp = SWTFactory.createComposite(container, container.getFont(), 1, 1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL, 0, 0);
		GridLayout gl = (GridLayout) bcomp.getLayout();
		gl.verticalSpacing = 0;
		// Add type button
		fAddTypeButton = SWTFactory.createPushButton(bcomp,
				SnippetMessages.getString("SelectImportsDialog.Add_&Type_1"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Choose_a_Type_to_Add_as_an_Import_2"),  //$NON-NLS-1$
				null);
		fAddTypeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				addType();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});

		// Add package button
		fAddPackageButton = SWTFactory.createPushButton(bcomp,
				SnippetMessages.getString("SelectImportsDialog.Add_&Package_3"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Choose_a_Package_to_Add_as_an_Import_4"),  //$NON-NLS-1$
				null);
		fAddPackageButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				addPackage();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});

		// Remove button
		fRemoveImportsButton = SWTFactory.createPushButton(bcomp,
				SnippetMessages.getString("SelectImportsDialog.&Remove_5"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Remove_All_Selected_Imports_6"),  //$NON-NLS-1$
				null);
		fRemoveImportsButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				removeImports();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		fRemoveImportsButton.setEnabled(false);
	}

	private void removeImports() {
		IStructuredSelection selection = (IStructuredSelection)fImportsViewer.getSelection();
		fImportContentProvider.removeImports(selection.toArray());
	}

	private void addPackage() {
		Shell shell= fAddPackageButton.getDisplay().getActiveShell();
		ElementListSelectionDialog dialog = null;
		try {
			IJavaProject project= fEditor.getJavaProject();
			List<IJavaElement> projects= new ArrayList<>();
			projects.add(project);
			IPackageFragmentRoot[] roots= project.getAllPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				projects.add(root.getParent());
			}
			dialog = JDIDebugUIPlugin.createAllPackagesDialog(shell, projects.toArray(new IJavaProject[projects.size()]), false);
		} catch (JavaModelException jme) {
			String title= SnippetMessages.getString("SelectImportsDialog.Add_package_as_import_7"); //$NON-NLS-1$
			String message= SnippetMessages.getString("SelectImportsDialog.Could_not_open_package_selection_dialog_8");  //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}
		if (dialog == null) {
			return;
		}
		dialog.setTitle(SnippetMessages.getString("SelectImportsDialog.Add_package_as_import_7"));  //$NON-NLS-1$
		dialog.setMessage(SnippetMessages.getString("SelectImportsDialog.&Select_a_package_to_add_as_an_Import_10")); //$NON-NLS-1$
		dialog.setMultipleSelection(true);
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		Object[] packages= dialog.getResult();
		if (packages != null) {
			for (int i = 0; i < packages.length; i++) {
				IJavaElement pkg = (IJavaElement)packages[i];
				String filter = pkg.getElementName();
				filter += ".*"; //$NON-NLS-1$
				fImportContentProvider.addImport(filter);
			}
		}
	}

	private void addType() {
		Shell shell= fAddTypeButton.getDisplay().getActiveShell();
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createJavaSearchScope(new IJavaElement[]{fEditor.getJavaProject()}, true), IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false);
		} catch (JavaModelException jme) {
			String title= SnippetMessages.getString("SelectImportsDialog.Add_Type_as_Import_12"); //$NON-NLS-1$
			String message= SnippetMessages.getString("SelectImportsDialog.Could_not_open_class_selection_dialog_13"); //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}

		dialog.setTitle(SnippetMessages.getString("SelectImportsDialog.Add_Type_as_Import_12")); //$NON-NLS-1$
		dialog.setMessage(SnippetMessages.getString("SelectImportsDialog.&Select_a_type_to_add_to_add_as_an_import_15")); //$NON-NLS-1$
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}

		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type = (IType)types[0];
			fImportContentProvider.addImport(type.getFullyQualifiedName());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(NLS.bind(SnippetMessages.getString("SelectImportsDialog.Manage_the_Java_Snippet_Editor_Imports_for___{0}__1"), new String[]{fEditor.getEditorInput().getName()})); //$NON-NLS-1$
		setMessage(NLS.bind(SnippetMessages.getString("SelectImportsDialog.add_remove_imports"), new String[]{fEditor.getEditorInput().getName()})); //$NON-NLS-1$
		Composite outer = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
		GridLayout gl = (GridLayout) outer.getLayout();
		gl.marginLeft = 7;
		gl.marginTop = 0;
		gl.marginBottom = 0;
		SWTFactory.createLabel(outer, SnippetMessages.getString("SelectImportsDialog.imports_heading"), 2); //$NON-NLS-1$
		fImportsTable= new Table(outer, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 150;
		fImportsTable.setLayoutData(gd);
		fImportsViewer = new TableViewer(fImportsTable);
		fImportsViewer.setLabelProvider(new FilterLabelProvider());
		fImportsViewer.setComparator(new FilterViewerComparator());
		fImportContentProvider = new ImportsContentProvider(fImportsViewer);
		fImportsViewer.setContentProvider(fImportContentProvider);
		// input just needs to be non-null
		fImportsViewer.setInput(this);
		fImportsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					fRemoveImportsButton.setEnabled(false);
				} else {
					fRemoveImportsButton.setEnabled(true);
				}
			}
		});
		createImportButtons(outer);
		applyDialogFont(outer);
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		String[] imports= null;
		Object[] results= fImportContentProvider.getElements(null);
		if (results != null && results.length > 0) {
			imports= new String[results.length];
			for (int i = 0; i < results.length; i++) {
				Filter imprt = (Filter)results[i];
				imports[i]= imprt.getName();
			}
		}
		fEditor.setImports(imports);
		super.okPressed();
	}

	/**
	 * Sets the title for the dialog and establishes the help context.
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SnippetMessages.getString("SelectImportsDialog.Java_Snippet_Imports_18")); //$NON-NLS-1$
	}
}
