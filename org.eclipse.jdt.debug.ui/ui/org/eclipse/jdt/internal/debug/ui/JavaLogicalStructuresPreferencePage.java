/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructure;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructures;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class JavaLogicalStructuresPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ISelectionChangedListener, Listener {

    public class LogicalStructuresListViewerLabelProvider extends LabelProvider implements IColorProvider {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
        	JavaLogicalStructure logicalStructure= (JavaLogicalStructure) element;
        	return logicalStructure.getQualifiedTypeName() + " - " + logicalStructure.getDescription(); //$NON-NLS-1$
        }

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			if (element instanceof JavaLogicalStructure) {
				if (((JavaLogicalStructure) element).isContributed()) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);		
				}
			}
			return null;
		}
    }
    
    public class LogicalStructuresListViewerContentProvider implements IStructuredContentProvider {
        
        private List fLogicalStructures;
        
        LogicalStructuresListViewerContentProvider() {
			fLogicalStructures= new ArrayList();
			JavaLogicalStructure[] logicalStructures= JavaLogicalStructures.getJavaLogicalStructures();
			for (int i= 0; i < logicalStructures.length; i++) {
				add(logicalStructures[i]);
			}
		}

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            return fLogicalStructures.toArray();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

		/**
		 * Add the given logical structure to the content provider.
		 */
		public void add(JavaLogicalStructure logicalStructure) {
			for (int i= 0, length= fLogicalStructures.size(); i < length; i++) {
				if (!greaterThan(logicalStructure, (JavaLogicalStructure)fLogicalStructures.get(i))) {
					fLogicalStructures.add(i, logicalStructure);
					return;
				}
			}
			fLogicalStructures.add(logicalStructure);
		}

		/**
		 * Compare two logical structures, return <code>true</code> if the first one is 'greater' than
		 * the second one.
		 */
		private boolean greaterThan(JavaLogicalStructure logicalStructure1, JavaLogicalStructure logicalStructure2) {
			int res= logicalStructure1.getQualifiedTypeName().compareToIgnoreCase(logicalStructure2.getQualifiedTypeName());
			if (res != 0) {
				return res > 0;
			}
			res= logicalStructure1.getDescription().compareToIgnoreCase(logicalStructure2.getDescription());
			if (res != 0) {
				return res > 0;
			}
			return logicalStructure1.hashCode() > logicalStructure2.hashCode();
		}

		/**
		 * Remove the given logical structures from the content provider.
		 */
		public void remove(List list) {
			fLogicalStructures.removeAll(list);
		}

		/**
		 * Refresh (reorder) the given logical structure.
		 */
		public void refresh(JavaLogicalStructure logicalStructure) {
			fLogicalStructures.remove(logicalStructure);
			add(logicalStructure);
		}

		public void saveUserDefinedJavaLogicalStructures() {
			List logicalStructures= new ArrayList();
			for (Iterator iter = fLogicalStructures.iterator(); iter.hasNext();) {
				JavaLogicalStructure logicalStructure= (JavaLogicalStructure) iter.next();
				if (!logicalStructure.isContributed()) {
					logicalStructures.add(logicalStructure);
				}
			}
			JavaLogicalStructures.setUserDefinedJavaLogicalStructures((JavaLogicalStructure[]) logicalStructures.toArray(new JavaLogicalStructure[logicalStructures.size()]));
		}

    }
    
	private TableViewer fLogicalStructuresListViewer;
	private Button fAddLogicalStructureButton;
	private Button fEditLogicalStructureButton;
	private Button fRemoveLogicalStructureButton;
    private LogicalStructuresListViewerContentProvider fLogicalStructuresListViewerContentProvider;

	public JavaLogicalStructuresPreferencePage() {
		super(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.0")); //$NON-NLS-1$
		setPreferenceStore(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		Font font = parent.getFont();
		initializeDialogUnits(parent);
		
		// top level container
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		container.setFont(font);
		
		Label label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.1")); //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		label.setFont(font);
		
		// logical structures list
		fLogicalStructuresListViewer= new TableViewer(container, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fLogicalStructuresListViewer.getControl();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(10);
		table.setLayoutData(gd);
		table.setFont(font);
		fLogicalStructuresListViewerContentProvider= new LogicalStructuresListViewerContentProvider();
        fLogicalStructuresListViewer.setContentProvider(fLogicalStructuresListViewerContentProvider);
		fLogicalStructuresListViewer.setLabelProvider(new LogicalStructuresListViewerLabelProvider());
		fLogicalStructuresListViewer.addSelectionChangedListener(this);
		fLogicalStructuresListViewer.setInput(this);
		
		// button container
		Composite buttonContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonContainer.setLayout(layout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttonContainer.setLayoutData(gd);
		buttonContainer.setFont(font);
		
		// add button
		fAddLogicalStructureButton = new Button(buttonContainer, SWT.PUSH);
		fAddLogicalStructureButton.setText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.2")); //$NON-NLS-1$
		fAddLogicalStructureButton.setToolTipText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.3")); //$NON-NLS-1$
		fAddLogicalStructureButton.setFont(font);
		setButtonLayoutData(fAddLogicalStructureButton);
		fAddLogicalStructureButton.addListener(SWT.Selection, this);

		// edit button
		fEditLogicalStructureButton = new Button(buttonContainer, SWT.PUSH);
		fEditLogicalStructureButton.setText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.4")); //$NON-NLS-1$
		fEditLogicalStructureButton.setToolTipText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.5")); //$NON-NLS-1$
		fEditLogicalStructureButton.setFont(font);
		setButtonLayoutData(fEditLogicalStructureButton);
		fEditLogicalStructureButton.addListener(SWT.Selection, this);

		// remove button
		fRemoveLogicalStructureButton = new Button(buttonContainer, SWT.PUSH);
		fRemoveLogicalStructureButton.setText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.6")); //$NON-NLS-1$
		fRemoveLogicalStructureButton.setToolTipText(DebugUIMessages.getString("JavaLogicalStructuresPreferencePage.7")); //$NON-NLS-1$
		fRemoveLogicalStructureButton.setFont(font);
		setButtonLayoutData(fRemoveLogicalStructureButton);
		fRemoveLogicalStructureButton.addListener(SWT.Selection, this);

		// initialize the buttons state
		selectionChanged((IStructuredSelection)fLogicalStructuresListViewer.getSelection());
		
		return container;
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection= event.getSelection();
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection)selection);
		}
	}
	
	/**
	 * Modify the state of the button from the selection.
	 */
	private void selectionChanged(IStructuredSelection structuredSelection) {
		int size= structuredSelection.size();
		if (size == 0) {
			fEditLogicalStructureButton.setEnabled(false);
			fRemoveLogicalStructureButton.setEnabled(false);
		} else {
			fEditLogicalStructureButton.setEnabled(size == 1 && !((JavaLogicalStructure)structuredSelection.getFirstElement()).isContributed());
			boolean removeEnabled= true;
			for (Iterator iter= structuredSelection.iterator(); iter.hasNext();) {
				if (((JavaLogicalStructure) iter.next()).isContributed()) {
					removeEnabled= false;
				}
			}
			fRemoveLogicalStructureButton.setEnabled(removeEnabled);
		} 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Widget source= event.widget;
		if (source == fAddLogicalStructureButton) {
			addLogicalStructure();
		} else if (source == fEditLogicalStructureButton) {
			editLogicalStructure();
		} else if (source == fRemoveLogicalStructureButton) {
			removeLogicalStrutures();
		}
	}

	// code for the add button
	protected void addLogicalStructure() {
		JavaLogicalStructure logicalStructure= new JavaLogicalStructure("", true, "", "", new String[0][], false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (new EditLogicalStructureDialog(fLogicalStructuresListViewer.getControl().getShell(), logicalStructure).open() == Window.OK) {
			fLogicalStructuresListViewerContentProvider.add(logicalStructure);
			fLogicalStructuresListViewer.refresh();
			fLogicalStructuresListViewer.setSelection(new StructuredSelection(logicalStructure));
		}
	}

	// code for the edit button
	protected void editLogicalStructure() {
		IStructuredSelection structuredSelection= (IStructuredSelection) fLogicalStructuresListViewer.getSelection();
		if (structuredSelection.size() == 1) {
			JavaLogicalStructure logicalStructure= (JavaLogicalStructure)structuredSelection.getFirstElement();
			new EditLogicalStructureDialog(fLogicalStructuresListViewer.getControl().getShell(), logicalStructure).open();
			fLogicalStructuresListViewerContentProvider.refresh(logicalStructure);
			fLogicalStructuresListViewer.refresh();
		}
	}

	// code for the remove button
	protected void removeLogicalStrutures() {
		IStructuredSelection selection= (IStructuredSelection)fLogicalStructuresListViewer.getSelection();
		if (selection.size() > 0) {
			List selectedElements= selection.toList();
			Object[] elements= fLogicalStructuresListViewerContentProvider.getElements(null);
			Object newSelectedElement= null;
			for (int i= 0; i < elements.length; i++) {
				if (!selectedElements.contains(elements[i])) {
					newSelectedElement= elements[i];
				} else {
					break;
				}
			}
			fLogicalStructuresListViewerContentProvider.remove(((IStructuredSelection) fLogicalStructuresListViewer.getSelection()).toList());
			fLogicalStructuresListViewer.refresh();
			if (newSelectedElement == null) {
				Object[] newElements= fLogicalStructuresListViewerContentProvider.getElements(null);
				if (newElements.length > 0) {
					fLogicalStructuresListViewer.setSelection(new StructuredSelection(newElements[0]));
				}
			} else {
				fLogicalStructuresListViewer.setSelection(new StructuredSelection(newSelectedElement));
			}
		}
		
	}
	
    /* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		fLogicalStructuresListViewerContentProvider.saveUserDefinedJavaLogicalStructures();
		return super.performOk();
	}
	
}
