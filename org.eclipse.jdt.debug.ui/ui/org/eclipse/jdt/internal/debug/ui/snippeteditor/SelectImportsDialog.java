package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */ 
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.Filter;
import org.eclipse.jdt.internal.debug.ui.FilterLabelProvider;
import org.eclipse.jdt.internal.debug.ui.FilterViewerSorter;
import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.SelectionDialog;

public class SelectImportsDialog extends TitleAreaDialog {

	/**
	 * Ignore package fragments that contain only non-Java resources, and make sure
	 * that each fragment is added to the list only once.
	 */
	private SelectionDialog createAllPackagesDialog(Shell shell) throws JavaModelException{
		IWorkspaceRoot wsroot= ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model= JavaCore.create(wsroot);
		IJavaProject[] projects= model.getJavaProjects();
		Set packageNameSet= new HashSet(); 
		List packageList = new ArrayList();
		for (int i = 0; i < projects.length; i++) {						
			IPackageFragment[] pkgs= projects[i].getPackageFragments();	
			for (int j = 0; j < pkgs.length; j++) {
				IPackageFragment pkg = pkgs[j];
				if (!pkg.hasChildren() && (pkg.getNonJavaResources().length > 0)) {
					continue;
				}
				if (packageNameSet.add(pkg.getElementName())) {
					packageList.add(pkg);
				}
			}
		}
		int flags= JavaElementLabelProvider.SHOW_DEFAULT;
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new JavaElementLabelProvider(flags));
		dialog.setIgnoreCase(false);
		dialog.setElements(packageList.toArray()); // XXX inefficient
		return dialog;
	}

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
		private List fImportNames;
		
		public ImportsContentProvider(TableViewer viewer) {
			fViewer = viewer;
			populateImports();
		}
		
		protected void populateImports() {
			fImportNames= new ArrayList(1);
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
		
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fImportNames.toArray();
		}
		
		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		/**
		 * @see IContentProvider#dispose()
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
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);
		
		// Add type button
		fAddTypeButton = new Button(buttonContainer, SWT.PUSH);
		fAddTypeButton.setText("Add &Type...");
		fAddTypeButton.setToolTipText("Choose a Java Type to Add as an Import");
		gd = getButtonGridData(fAddTypeButton);
		fAddTypeButton.setLayoutData(gd);
		fAddTypeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				addType();
			}
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		// Add package button
		fAddPackageButton = new Button(buttonContainer, SWT.PUSH);
		fAddPackageButton.setText("Add &Package...");
		fAddPackageButton.setToolTipText("Choose a Package to Add an an Import");
		gd = getButtonGridData(fAddPackageButton);
		fAddPackageButton.setLayoutData(gd);
		fAddPackageButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				addPackage();
			}
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		
		// Remove button
		fRemoveImportsButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveImportsButton.setText("&Remove");
		fRemoveImportsButton.setToolTipText("Remove All Selected Imports");
		gd = getButtonGridData(fRemoveImportsButton);
		fRemoveImportsButton.setLayoutData(gd);
		fRemoveImportsButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent se) {
				removeImports();
			}
			public void widgetDefaultSelected(SelectionEvent se) {
			}
		});
		fRemoveImportsButton.setEnabled(false);
		
	}
	
	private GridData getButtonGridData(Button button) {
		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		GC gc = new GC(button);
		gc.setFont(button.getFont());
		FontMetrics fontMetrics= gc.getFontMetrics();
		gc.dispose();
		int widthHint= Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		gd.widthHint= Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		
		gd.heightHint= Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_HEIGHT);
		return gd;
	}
		
	private void removeImports() {
		IStructuredSelection selection = (IStructuredSelection)fImportsViewer.getSelection();		
		fImportContentProvider.removeImports(selection.toArray());
	}
	
	private void addPackage() {
		Shell shell= fAddPackageButton.getDisplay().getActiveShell();
		SelectionDialog dialog = null;
		try {
			dialog = createAllPackagesDialog(shell);
		} catch (JavaModelException jme) {
			String title= "Add package as import";
			String message= "Could not open package selection dialog"; 
			ExceptionHandler.handle(jme, title, message);
			return;			
		}
	
		dialog.setTitle("Add Package as Import"); 
		dialog.setMessage("&Select a package to add as an Import");
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		Object[] packages= dialog.getResult();
		if (packages != null && packages.length > 0) {
			IJavaElement pkg = (IJavaElement)packages[0];
			String filter = pkg.getElementName();
			if (filter.length() < 1) {
				filter = "(default package)"; 
			} else {
				filter += ".*"; //$NON-NLS-1$
			}
			fImportContentProvider.addImport(filter);
		}		
	}
				
	private void addType() {
		Shell shell= fAddTypeButton.getDisplay().getActiveShell();
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, new ProgressMonitorDialog(shell),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_TYPES, false);
		} catch (JavaModelException jme) {
			String title= "Add Type as Import";
			String message= "Could not open class selection dialog";
			ExceptionHandler.handle(jme, title, message);
			return;
		}
	
		dialog.setTitle("Add Type as Import");
		dialog.setMessage("&Select a type to add to add as an import");
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type = (IType)types[0];
			fImportContentProvider.addImport(type.getFullyQualifiedName());
		}		
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
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		// top level container
		Composite outer = new Composite(dialogComp, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 0;
		outer.setLayout(topLayout);
		
		setTitle("Manage the Java Snippet Editor Imports for \"" + fEditor.getPage().getName() + "\"");
		
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
		new TableColumn(fImportsTable, SWT.NONE);

		fImportsViewer = new TableViewer(fImportsTable);
		fImportsViewer.setLabelProvider(new FilterLabelProvider());
		fImportsViewer.setSorter(new FilterViewerSorter());
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
		return outer;
	}

	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] results= fImportContentProvider.getElements(null);
		if (results != null && results.length > 0) {
			String[] imports= new String[results.length];
			for (int i = 0; i < results.length; i++) {
				Filter imprt = (Filter)results[i];
				imports[i]= imprt.getName();
			}
			fEditor.setImports(imports);
		}

		super.okPressed();
	}
	
	/**
	 * Sets the title for the dialog and establishes the help context.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Java Snippet Imports");
	}
}
