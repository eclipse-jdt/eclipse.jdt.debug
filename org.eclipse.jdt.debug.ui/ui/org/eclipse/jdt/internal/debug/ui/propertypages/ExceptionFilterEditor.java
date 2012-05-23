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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
import org.eclipse.jdt.internal.debug.ui.Filter;
import org.eclipse.jdt.internal.debug.ui.FilterLabelProvider;
import org.eclipse.jdt.internal.debug.ui.FilterViewerComparator;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;


public class ExceptionFilterEditor {

	protected static final String DEFAULT_PACKAGE = "(default package)"; //$NON-NLS-1$
	private IJavaExceptionBreakpoint fBreakpoint;
	private Button fAddFilterButton;
	private Button fAddPackageButton;
	private Button fAddTypeButton;
	private Button fRemoveFilterButton;
	private Text fEditorText;
	private String fInvalidEditorText = null;
	private TableEditor fTableEditor;
	private TableItem fNewTableItem;
	private Filter fNewFilter;
	private CheckboxTableViewer fFilterViewer;
	private Table fFilterTable;
	private FilterContentProvider fFilterContentProvider;
	
	private SelectionListener fSelectionListener= new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fAddTypeButton) {
				addType();
			} else if (source == fAddPackageButton) {
				addPackage();
			} else if (source == fAddFilterButton) {
				editFilter();
			} else if (source == fRemoveFilterButton) {
				removeFilters();
			}
		}
	};

	public ExceptionFilterEditor(Composite parent, JavaExceptionBreakpointAdvancedPage page) {
		fBreakpoint = (IJavaExceptionBreakpoint) page.getBreakpoint();
		// top level container
		Composite outer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		outer.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		outer.setLayoutData(gd);
		outer.setFont(parent.getFont());
		// filter table
		Label label= new Label(outer, SWT.NONE);
		label.setText(PropertyPageMessages.ExceptionFilterEditor_5); 
		label.setFont(parent.getFont());
		gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		
		fFilterTable = new Table(outer, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		TableLayout tableLayout = new TableLayout();
		ColumnLayoutData[] columnLayoutData = new ColumnLayoutData[1];
		columnLayoutData[0] = new ColumnWeightData(100);
		tableLayout.addColumnData(columnLayoutData[0]);
		fFilterTable.setLayout(tableLayout);
		fFilterTable.setFont(parent.getFont());
		new TableColumn(fFilterTable, SWT.NONE);

		fFilterViewer = new CheckboxTableViewer(fFilterTable);
		fTableEditor = new TableEditor(fFilterTable);
		fFilterViewer.setLabelProvider(new FilterLabelProvider());
		fFilterViewer.setComparator(new FilterViewerComparator());
		fFilterContentProvider = new FilterContentProvider(fFilterViewer);
		fFilterViewer.setContentProvider(fFilterContentProvider);
		// input just needs to be non-null
		fFilterViewer.setInput(this);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.widthHint = 100;
		gd.heightHint = 100;
		fFilterViewer.getTable().setLayoutData(gd);
		fFilterViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Filter filter = (Filter) event.getElement();
				fFilterContentProvider.toggleFilter(filter);
			}
		});
		fFilterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					fRemoveFilterButton.setEnabled(false);
				} else {
					fRemoveFilterButton.setEnabled(true);
				}
			}
		});
		fFilterViewer.getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					removeFilters();
				}
			}
		});

		createFilterButtons(outer);
	}

	protected void doStore() {
		Object[] filters = fFilterContentProvider.getElements(null);
		List<String> inclusionFilters = new ArrayList<String>(filters.length);
		List<String> exclusionFilters = new ArrayList<String>(filters.length);
		for (int i = 0; i < filters.length; i++) {
			Filter filter = (Filter) filters[i];
			String name = filter.getName();
			if (name.equals(DEFAULT_PACKAGE)) {
				name = ""; //$NON-NLS-1$
			}
			if (filter.isChecked()) {
				inclusionFilters.add(name);
			} else {
				exclusionFilters.add(name);
			}
		}
		try {
			fBreakpoint.setInclusionFilters(inclusionFilters.toArray(new String[inclusionFilters.size()]));
			fBreakpoint.setExclusionFilters(exclusionFilters.toArray(new String[exclusionFilters.size()]));
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}

	}

	private void createFilterButtons(Composite container) {
		// button container
		Font font= container.getFont();
		Composite buttonContainer = new Composite(container, SWT.NONE);
		buttonContainer.setFont(font);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonContainer.setLayout(buttonLayout);

		fAddFilterButton = createPushButton(buttonContainer,
				PropertyPageMessages.ExceptionFilterEditor_6, 
				PropertyPageMessages.ExceptionFilterEditor_7); 
		fAddTypeButton = createPushButton(buttonContainer,
				PropertyPageMessages.ExceptionFilterEditor_8, 
				PropertyPageMessages.ExceptionFilterEditor_9); 
		fAddPackageButton = createPushButton(buttonContainer,
				PropertyPageMessages.ExceptionFilterEditor_10, 
				PropertyPageMessages.ExceptionFilterEditor_11); 
		fRemoveFilterButton = createPushButton(buttonContainer,
				PropertyPageMessages.ExceptionFilterEditor_12, 
				PropertyPageMessages.ExceptionFilterEditor_13); 
		fRemoveFilterButton.setEnabled(false);
	}
	
	/**
	 * Creates a fully configured push button with the given label and tooltip.
	 */
	private Button createPushButton(Composite parent, String text, String tooltipText) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(text);
		button.setToolTipText(tooltipText);
		GridData gd = getButtonGridData(button);
		button.setLayoutData(gd);
		button.addSelectionListener(fSelectionListener);
		return button;
	}

	private static GridData getButtonGridData(Button button) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		GC gc = new GC(button);
		gc.setFont(button.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		return gd;
	}

	/**
	 * Create a new filter in the table (with the default 'new filter' value),
	 * then open up an in-place editor on it.
	 */
	private void editFilter() {
		// if a previous edit is still in progress, finish it
		if (fEditorText != null) {
			validateChangeAndCleanup();
		}

		fNewFilter = fFilterContentProvider.addFilter(""); //$NON-NLS-1$
		fNewTableItem = fFilterTable.getItem(0);

		// create & configure Text widget for editor
		// Fix for bug 1766.  Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't.  Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		int textStyles = SWT.SINGLE | SWT.LEFT;
		if (!SWT.getPlatform().equals("motif")) { //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		fEditorText = new Text(fFilterTable, textStyles);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fEditorText.setLayoutData(gd);

		// set the editor
		fTableEditor.horizontalAlignment = SWT.LEFT;
		fTableEditor.grabHorizontal = true;
		fTableEditor.setEditor(fEditorText, fNewTableItem, 0);

		// get the editor ready to use
		fEditorText.setText(fNewFilter.getName());
		fEditorText.selectAll();
		setEditorListeners(fEditorText);
		fEditorText.setFocus();
	}

	private void setEditorListeners(Text text) {
		// CR means commit the changes, ESC means abort and don't commit
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.CR) {
					if (fInvalidEditorText != null) {
						fEditorText.setText(fInvalidEditorText);
						fInvalidEditorText = null;
					} else {
						validateChangeAndCleanup();
					}
				} else if (event.character == SWT.ESC) {
					removeNewFilter();
					cleanupEditor();
				}
			}
		});
		// Consider loss of focus on the editor to mean the same as CR
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				if (fInvalidEditorText != null) {
					fEditorText.setText(fInvalidEditorText);
					fInvalidEditorText = null;
				} else {
					validateChangeAndCleanup();
				}
			}
		});
		// Consume traversal events from the text widget so that CR doesn't 
		// traverse away to dialog's default button.  Without this, hitting
		// CR in the text field closes the entire dialog.
		text.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	private void validateChangeAndCleanup() {
		String trimmedValue = fEditorText.getText().trim();
		// if the new value is blank, remove the filter
		if (trimmedValue.length() < 1) {
			removeNewFilter();
		}
		// if it's invalid, beep and leave sitting in the editor
		else if (!validateEditorInput(trimmedValue)) {
			fInvalidEditorText = trimmedValue;
			fEditorText.setText(PropertyPageMessages.ExceptionFilterEditor_14); 
			fEditorText.getDisplay().beep();
			return;
			// otherwise, commit the new value if not a duplicate
		} else {
			Object[] filters = fFilterContentProvider.getElements(null);
			for (int i = 0; i < filters.length; i++) {
				Filter filter = (Filter) filters[i];
				if (filter.getName().equals(trimmedValue)) {
					removeNewFilter();
					cleanupEditor();
					return;
				}
			}
			fNewTableItem.setText(trimmedValue);
			fNewFilter.setName(trimmedValue);
			fFilterViewer.refresh();
		}
		cleanupEditor();
	}

	/**
	 * A valid filter is simply one that is a valid Java identifier.
	 * and, as defined in the JDI spec, the regular expressions used for
	 * scoping must be limited to exact matches or patterns that
	 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
	 * as corresponding to an existing type or package (and this is probably not
	 * even desirable).  
	 */
	private boolean validateEditorInput(String trimmedValue) {
		char firstChar = trimmedValue.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			if (!(firstChar == '*')) {
				return false;
			}
		}
		int length = trimmedValue.length();
		for (int i = 1; i < length; i++) {
			char c = trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1)) {
					continue;
				}
				if (c == '*' && i == (length - 1)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Cleanup all widgetry & resources used by the in-place editing
	 */
	private void cleanupEditor() {
		if (fEditorText != null) {
			fNewFilter = null;
			fNewTableItem = null;
			fTableEditor.setEditor(null, null, 0);
			fEditorText.getDisplay().asyncExec(new Runnable() {
				public void run() {
					fEditorText.dispose();
					fEditorText = null;
				}
			});
		}
	}

	private void removeFilters() {
		IStructuredSelection selection = (IStructuredSelection) fFilterViewer.getSelection();
		fFilterContentProvider.removeFilters(selection.toArray());
	}

	private void removeNewFilter() {
		fFilterContentProvider.removeFilters(new Object[] { fNewFilter });
	}

	private void addPackage() {
		Shell shell = fAddPackageButton.getDisplay().getActiveShell();
		ElementListSelectionDialog dialog = null;
		try {
			dialog = JDIDebugUIPlugin.createAllPackagesDialog(shell, null, false);
		} catch (JavaModelException jme) {
			String title = PropertyPageMessages.ExceptionFilterEditor_15; 
			String message = PropertyPageMessages.ExceptionFilterEditor_16; 
			ExceptionHandler.handle(jme, title, message);
			return;
		}
		if (dialog == null) {
			return;
		}
		dialog.setTitle(PropertyPageMessages.ExceptionFilterEditor_15); 
		dialog.setMessage(PropertyPageMessages.ExceptionFilterEditor_18); 
		dialog.setMultipleSelection(true);
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		Object[] packages = dialog.getResult();
		if (packages != null) {
			for (int i = 0; i < packages.length; i++) {
				IJavaElement pkg = (IJavaElement) packages[i];
				String filter = pkg.getElementName();
				if (filter.length() < 1) {
					filter = DEFAULT_PACKAGE;
				} else {
					filter += ".*"; //$NON-NLS-1$
				}
				Filter f = fFilterContentProvider.addFilter(filter);
				fFilterContentProvider.checkFilter(f, true);
			}
		}
	}

	private void addType() {
		Shell shell = fAddTypeButton.getDisplay().getActiveShell();
		SelectionDialog dialog = null;
		try {
			dialog = JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(), SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
		} catch (JavaModelException jme) {
			String title = PropertyPageMessages.ExceptionFilterEditor_19; 
			String message = PropertyPageMessages.ExceptionFilterEditor_20; 
			ExceptionHandler.handle(jme, title, message);
			return;
		}

		dialog.setTitle(PropertyPageMessages.ExceptionFilterEditor_19); 
		dialog.setMessage(PropertyPageMessages.ExceptionFilterEditor_22); 
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}

		Object[] types = dialog.getResult();
		IType type;
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				type = (IType) types[i];
				Filter f = fFilterContentProvider.addFilter(type.getFullyQualifiedName());
				fFilterContentProvider.checkFilter(f, true);
			}
		}
	}

	/**
	 * Content provider for the table.  Content consists of instances of Filter.
	 */
	protected class FilterContentProvider implements IStructuredContentProvider {

		private CheckboxTableViewer fViewer;
		private List<Filter> fFilters;

		public FilterContentProvider(CheckboxTableViewer viewer) {
			fViewer = viewer;
			populateFilters();
		}

		protected void populateFilters() {
			String[] iFilters = null;
			String[] eFilters = null;
			try {
				iFilters = fBreakpoint.getInclusionFilters();
				eFilters = fBreakpoint.getExclusionFilters();
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce);
				iFilters = new String[] {
				};
				eFilters = new String[] {
				};
			}
			fFilters = new ArrayList<Filter>();
			populateFilters(iFilters, true);
			populateFilters(eFilters, false);

		}

		protected void populateFilters(String[] filters, boolean checked) {
			for (int i = 0; i < filters.length; i++) {
				String name = filters[i];
				if (name.length() == 0) {
					name = DEFAULT_PACKAGE;
				}
				Filter filter = addFilter(name);
				checkFilter(filter, checked);
			}
		}

		public Filter addFilter(String name) {
			Filter filter = new Filter(name, false);
			if (!fFilters.contains(filter)) {
				fFilters.add(filter);
				fViewer.add(filter);
			}
			return filter;
		}

		public void removeFilters(Object[] filters) {
			for (int i = 0; i < filters.length; i++) {
				Filter filter = (Filter) filters[i];
				fFilters.remove(filter);
			}
			fViewer.remove(filters);
		}

		public void toggleFilter(Filter filter) {
			boolean newState = !filter.isChecked();
			filter.setChecked(newState);
			fViewer.setChecked(filter, newState);
		}

		public void checkFilter(Filter filter, boolean checked) {
			filter.setChecked(checked);
			fViewer.setChecked(filter, checked);
		}

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fFilters.toArray();
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

}
