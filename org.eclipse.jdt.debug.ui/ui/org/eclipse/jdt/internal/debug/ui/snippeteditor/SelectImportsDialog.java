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
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
			fImportNames= new ArrayList<Filter>(1);
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
		public Object[] getElements(Object inputElement) {
			return fImportNames.toArray();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
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
		
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
		
		// Add type button
		fAddTypeButton = SWTFactory.createPushButton(buttonContainer, 
				SnippetMessages.getString("SelectImportsDialog.Add_&Type_1"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Choose_a_Type_to_Add_as_an_Import_2"),  //$NON-NLS-1$
				null);
		fAddTypeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				addType();
			}
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		// Add package button
		fAddPackageButton = SWTFactory.createPushButton(buttonContainer, 
				SnippetMessages.getString("SelectImportsDialog.Add_&Package_3"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Choose_a_Package_to_Add_as_an_Import_4"),  //$NON-NLS-1$
				null);
		fAddPackageButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				addPackage();
			}
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		// Remove button
		fRemoveImportsButton = SWTFactory.createPushButton(buttonContainer, 
				SnippetMessages.getString("SelectImportsDialog.&Remove_5"),  //$NON-NLS-1$
				SnippetMessages.getString("SelectImportsDialog.Remove_All_Selected_Imports_6"),  //$NON-NLS-1$
				null);
		fRemoveImportsButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				removeImports();
			}
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
			List<IJavaElement> projects= new ArrayList<IJavaElement>();
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
		Font font = parent.getFont();
		
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		// top level container
		Composite outer = new Composite(dialogComp, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		outer.setLayout(topLayout);
		outer.setFont(font);
		
		setTitle(NLS.bind(SnippetMessages.getString("SelectImportsDialog.Manage_the_Java_Snippet_Editor_Imports_for___{0}__1"), new String[]{fEditor.getEditorInput().getName()})); //$NON-NLS-1$
		
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		outer.setLayoutData(gd);
		
		// filter table
		fImportsTable= new Table(outer, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		TableLayout tableLayout= new TableLayout();
		ColumnLayoutData[] columnLayoutData= new ColumnLayoutData[1];
		columnLayoutData[0]= new ColumnWeightData(100);		
		tableLayout.addColumnData(columnLayoutData[0]);
		fImportsTable.setLayout(tableLayout);
		fImportsTable.setFont(font);
		new TableColumn(fImportsTable, SWT.NONE);

		fImportsViewer = new TableViewer(fImportsTable);
		fImportsViewer.setLabelProvider(new FilterLabelProvider());
		fImportsViewer.setComparator(new FilterViewerComparator());
		fImportContentProvider = new ImportsContentProvider(fImportsViewer);
		fImportsViewer.setContentProvider(fImportContentProvider);
		// input just needs to be non-null
		fImportsViewer.setInput(this);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.widthHint = 100;
		gd.heightHint= 300;
		fImportsViewer.getTable().setLayoutData(gd);
		fImportsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
		return outer;
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
