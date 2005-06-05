/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JavaLogicalStructure;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 */
public class EditLogicalStructureDialog extends StatusDialog implements Listener, ISelectionChangedListener, IDocumentListener, CodeSnippetCompletionProcessor.ITypeProvider {

	public class AttributesContentProvider implements IStructuredContentProvider {
		

		private final List fVariables;

		public AttributesContentProvider(String[][] variables) {
			fVariables= new ArrayList();
			for (int i= 0; i < variables.length; i++) {
				String[] variable= new String[2];
				variable[0]= variables[i][0];
				variable[1]= variables[i][1];
				fVariables.add(variable);
			}
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

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getElements();
		}
		
		/**
		 * Returns the attributes.
		 */
		public String[][] getElements() {
			return (String[][])fVariables.toArray(new String[fVariables.size()][]);
		}

		/**
		 * Adds the given attributes.
		 */
		public void add(String[] newAttribute) {
			fVariables.add(newAttribute);
		}

		/**
		 * Remove the given attributes
		 */
		public void remove(List list) {
			fVariables.removeAll(list);
		}

		/**
		 * Moves the given attributes up in the list.
		 */
		public void up(List list) {
			for (Iterator iter= list.iterator(); iter.hasNext();) {
				String[] variable= (String[]) iter.next();
				int index= fVariables.indexOf(variable);
				fVariables.remove(variable);
				fVariables.add(index - 1, variable);
			}
		}

		/**
		 * Moves the given attributes down int the list.
		 */
		public void down(List list) {
			for (Iterator iter= list.iterator(); iter.hasNext();) {
				String[] variable= (String[]) iter.next();
				int index= fVariables.indexOf(variable);
				fVariables.remove(variable);
				fVariables.add(index + 1, variable);
			}
		}

	}
	
	public class AttributesLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((String[])element)[0];
		}
	}
	
	private final JavaLogicalStructure fLogicalStructure;
	private Text fQualifiedTypeNameText;
	private Text fDescriptionText;
	private TableViewer fAttributeListViewer;
	private Button fSubTypeButton;
	private Button fValueButton;
	private Button fVariablesButton;
	private Button fAttributeUpButton;
	private Button fAttributeDownButton;
	private JDISourceViewer fSnippetViewer;
	private Document fSnippetDocument;
	private Button fBrowseTypeButton;
	private Button fAttributeAddButton;
	private Button fAttributeRemoveButton;
	private Text fAttributeNameText;
	private Composite fAttributesContainer;
	private Group fCodeGroup;
	private Composite fParentComposite;
	private AttributesContentProvider fAttributesContentProvider;
	private String fValueTmp;
	private IStructuredSelection fCurrentAttributeSelection;
	private IType fType;
	private boolean fTypeSearched= false;
	private DisplayViewerConfiguration fViewerConfiguration;
	private HandlerSubmission fSubmission;

	public EditLogicalStructureDialog(Shell parentShell, JavaLogicalStructure logicalStructure) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE);
		if (logicalStructure.getQualifiedTypeName().length() == 0) {
			setTitle(DebugUIMessages.EditLogicalStructureDialog_32);
		} else {
			setTitle(DebugUIMessages.EditLogicalStructureDialog_31); //$NON-NLS-1$
		}
		fLogicalStructure= logicalStructure;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		fParentComposite= parent;
		
		IHandler handler = new AbstractHandler() {
			public Object execute(Map parameterValuesByName) throws ExecutionException {
				findCorrespondingType();
				fSnippetViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);				
				return null;
			}
		};
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();		
		fSubmission = new HandlerSubmission(null, parent.getShell(), null, ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM); //$NON-NLS-1$
		commandSupport.addHandlerSubmission(fSubmission);	
		
		// big container
		Composite container= new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setFont(parent.getFont());

		// name and description container
		Composite typeNameDescriptionContainer= new Composite(container, SWT.NONE);
		GridLayout gridLayout= new GridLayout(2, false);
		typeNameDescriptionContainer.setLayout(gridLayout);
		typeNameDescriptionContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		
		Label typeLabel= new Label(typeNameDescriptionContainer, SWT.NONE);
		typeLabel.setText(DebugUIMessages.EditLogicalStructureDialog_0); //$NON-NLS-1$
		typeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		// name text area
		fQualifiedTypeNameText= new Text(typeNameDescriptionContainer, SWT.SINGLE | SWT.BORDER);
		fQualifiedTypeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fQualifiedTypeNameText.addListener(SWT.Modify, this);
		
		// browse button
		fBrowseTypeButton= new Button(typeNameDescriptionContainer, SWT.PUSH);
		fBrowseTypeButton.setText(DebugUIMessages.EditLogicalStructureDialog_1); //$NON-NLS-1$
		fBrowseTypeButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_25); //$NON-NLS-1$
		setButtonLayoutData(fBrowseTypeButton);
		fBrowseTypeButton.addListener(SWT.Selection, this);

		Label descriptionLabel= new Label(typeNameDescriptionContainer, SWT.NONE);
		descriptionLabel.setText(DebugUIMessages.EditLogicalStructureDialog_2); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		
		// description text area
		fDescriptionText= new Text(typeNameDescriptionContainer, SWT.SINGLE | SWT.BORDER);
		fDescriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fDescriptionText.addListener(SWT.Modify, this);
		
		// isSubtype button
		fSubTypeButton= new Button(typeNameDescriptionContainer, SWT.CHECK);
		fSubTypeButton.setText(DebugUIMessages.EditLogicalStructureDialog_3); //$NON-NLS-1$
		fSubTypeButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_26); //$NON-NLS-1$
		fSubTypeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// value/variable container
		Group radioContainer= new Group(container, SWT.NONE);
		radioContainer.setText(DebugUIMessages.EditLogicalStructureDialog_33);
		gridLayout= new GridLayout(1, true);
		radioContainer.setLayout(gridLayout);
		radioContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		// value button
		fValueButton= new Button(radioContainer, SWT.RADIO);
		fValueButton.setText(DebugUIMessages.EditLogicalStructureDialog_4); //$NON-NLS-1$
		fValueButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		fValueButton.addListener(SWT.Selection, this);
		
		// variable button
		fVariablesButton= new Button(radioContainer, SWT.RADIO);
		fVariablesButton.setText(DebugUIMessages.EditLogicalStructureDialog_5); //$NON-NLS-1$
		fVariablesButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// attribute list container
		fAttributesContainer= new Composite(container, SWT.NONE);
		gridLayout= new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		fAttributesContainer.setLayout(gridLayout);
		fAttributesContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		
		boolean isValue = fLogicalStructure.getValue() != null;
		if (!isValue) {
			// creates the attribute list if needed
			createAttributeListWidgets();
		}
		
		// code snippet editor group
		fCodeGroup= new Group(container, SWT.NONE);
		fCodeGroup.setLayout(new GridLayout(1, false));
		fCodeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		createCodeGroupWidgets(isValue);


		applyDialogFont(container);
		
		// initialize the data in the widgets
		initializeData();
		
		return container;
	}

	/**
	 * Create the widgets it the code snippet editor group
	 */
	private void createCodeGroupWidgets(boolean isValue) {
        Font font= fCodeGroup.getFont();
		if (isValue) {
			fCodeGroup.setText(DebugUIMessages.EditLogicalStructureDialog_9); //$NON-NLS-1$
		} else {
			fCodeGroup.setText(DebugUIMessages.EditLogicalStructureDialog_7); //$NON-NLS-1$
		
			// if it's a variable, create the attribute name text area
			Composite attributeNameContainer= new Composite(fCodeGroup, SWT.NONE);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 0;
			attributeNameContainer.setLayout(gridLayout);
			attributeNameContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
			
			Label attributeNameLabel= new Label(attributeNameContainer, SWT.NONE);
			attributeNameLabel.setText(DebugUIMessages.EditLogicalStructureDialog_8); //$NON-NLS-1$
			attributeNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            attributeNameLabel.setFont(font);
			
			fAttributeNameText= new Text(attributeNameContainer, SWT.SINGLE | SWT.BORDER);
			fAttributeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fAttributeNameText.addListener(SWT.Modify, this);
            fAttributeNameText.setFont(font);
		}

		if (!isValue) {
			Label attributeValueLabel= new Label(fCodeGroup, SWT.NONE);
			attributeValueLabel.setText(DebugUIMessages.EditLogicalStructureDialog_9); //$NON-NLS-1$
			attributeValueLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
	        attributeValueLabel.setFont(font);
		}
		
		// snippet viewer
		fSnippetViewer= new JDISourceViewer(fCodeGroup,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
		fSnippetViewer.setInput(this);
	
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		if (fSnippetDocument == null) {
			fSnippetDocument= new Document();
			fSnippetDocument.addDocumentListener(this);
		}
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		fSnippetDocument.setDocumentPartitioner(partitioner);
		partitioner.connect(fSnippetDocument);
		fSnippetViewer.configure(new JavaSourceViewerConfiguration(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), JavaPlugin.getDefault().getPreferenceStore(), null, null));
		if (fViewerConfiguration == null) {
			fViewerConfiguration= new DisplayViewerConfiguration() {
				public IContentAssistProcessor getContentAssistantProcessor() {
					return new CodeSnippetCompletionProcessor(EditLogicalStructureDialog.this);
				}
			};
		}
		fSnippetViewer.configure(fViewerConfiguration);
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(fSnippetDocument);
		
		Control control= fSnippetViewer.getControl();
		GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint= convertHeightInCharsToPixels(isValue ? 20 : 10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
	}

	/**
	 * Create the widgets for the attribute list
	 */
	private void createAttributeListWidgets() {
        Font font= fAttributesContainer.getFont();
		// attribute list
		fAttributeListViewer= new TableViewer(fAttributesContainer, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fAttributeListViewer.getControl();
		GridData gd= new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint= convertHeightInCharsToPixels(5);
		gd.widthHint= convertWidthInCharsToPixels(10);
		table.setLayoutData(gd);
        table.setFont(font);
		if (fAttributesContentProvider == null) {
			fAttributesContentProvider= new AttributesContentProvider(fLogicalStructure.getVariables());
		}
		fAttributeListViewer.setContentProvider(fAttributesContentProvider);
		fAttributeListViewer.setLabelProvider(new AttributesLabelProvider());
		fAttributeListViewer.setInput(this);
		fAttributeListViewer.addSelectionChangedListener(this);
		
		// button container
		Composite attributesListButtonsContainer= new Composite(fAttributesContainer, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		attributesListButtonsContainer.setLayout(gridLayout);
		attributesListButtonsContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		
		// add attribute button
		fAttributeAddButton= new Button(attributesListButtonsContainer, SWT.PUSH);
		fAttributeAddButton.setText(DebugUIMessages.EditLogicalStructureDialog_10); //$NON-NLS-1$
		fAttributeAddButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_27); //$NON-NLS-1$
        fAttributeAddButton.setFont(font);
		setButtonLayoutData(fAttributeAddButton);
		fAttributeAddButton.addListener(SWT.Selection, this);
		
		// remove attribute button
		fAttributeRemoveButton= new Button(attributesListButtonsContainer, SWT.PUSH);
		fAttributeRemoveButton.setText(DebugUIMessages.EditLogicalStructureDialog_11); //$NON-NLS-1$
		fAttributeRemoveButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_28); //$NON-NLS-1$
        fAttributeRemoveButton.setFont(font);
		setButtonLayoutData(fAttributeRemoveButton);
		fAttributeRemoveButton.addListener(SWT.Selection, this);
		
		// attribute up button
		fAttributeUpButton= new Button(attributesListButtonsContainer, SWT.PUSH);
		fAttributeUpButton.setText(DebugUIMessages.EditLogicalStructureDialog_12); //$NON-NLS-1$
		fAttributeUpButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_29); //$NON-NLS-1$
        fAttributeUpButton.setFont(font);
		setButtonLayoutData(fAttributeUpButton);
		fAttributeUpButton.addListener(SWT.Selection, this);
		
		// attribute down button
		fAttributeDownButton= new Button(attributesListButtonsContainer, SWT.PUSH);
		fAttributeDownButton.setText(DebugUIMessages.EditLogicalStructureDialog_13); //$NON-NLS-1$
		fAttributeDownButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_30); //$NON-NLS-1$
        fAttributeDownButton.setFont(font);
		setButtonLayoutData(fAttributeDownButton);
		fAttributeDownButton.addListener(SWT.Selection, this);
		
	}

	private void initializeData() {
		fQualifiedTypeNameText.setText(fLogicalStructure.getQualifiedTypeName());
		fDescriptionText.setText(fLogicalStructure.getDescription());
		fSubTypeButton.setSelection(fLogicalStructure.isSubtypes());
		fValueTmp= fLogicalStructure.getValue();
		if (fValueTmp == null) {
			fValueTmp= ""; //$NON-NLS-1$
			fVariablesButton.setSelection(true);
			setAttributesData(false);
		} else {
			fValueButton.setSelection(true);
			setAttributesData(true);
		}
		checkValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Widget source= event.widget;
		switch (event.type) {
			case SWT.Selection:
				if (source == fValueButton) {
					toggleAttributesWidgets(fValueButton.getSelection());
					checkValues();
				} else if (source == fBrowseTypeButton) {
					selectType();
				} else if (source == fAttributeAddButton) {
					addAttribute();
				} else if (source == fAttributeRemoveButton) {
					removeAttribute();
				} else if (source == fAttributeUpButton) {
					attributeUp();
				} else if (source == fAttributeDownButton) {
					attributeDown();
				}
				break;
			case SWT.Modify:
				if (source == fAttributeNameText) {
					saveNewAttributeName();
					checkValues();
				} else if (source == fQualifiedTypeNameText) {
					checkValues();
					fTypeSearched= false;
				} else if (source == fDescriptionText) {
					checkValues();
				}
				break;
		}
	}

	// code for add attribute button
	private void addAttribute() {
		String[] newAttribute= new String[] {DebugUIMessages.EditLogicalStructureDialog_14, DebugUIMessages.EditLogicalStructureDialog_15}; //$NON-NLS-1$ //$NON-NLS-2$
		fAttributesContentProvider.add(newAttribute);
		fAttributeListViewer.refresh();
		fAttributeListViewer.setSelection(new StructuredSelection((Object)newAttribute));
	}

	// code for remove attribute button
	private void removeAttribute() {
		IStructuredSelection selection= (IStructuredSelection)fAttributeListViewer.getSelection();
		if (selection.size() > 0) {
			List selectedElements= selection.toList();
			Object[] elements= fAttributesContentProvider.getElements();
			Object newSelectedElement= null;
			for (int i= 0; i < elements.length; i++) {
				if (!selectedElements.contains(elements[i])) {
					newSelectedElement= elements[i];
				} else {
					break;
				}
			}
			fAttributesContentProvider.remove(selectedElements);
			fAttributeListViewer.refresh();
			if (newSelectedElement == null) {
				Object[] newElements= fAttributesContentProvider.getElements();
				if (newElements.length > 0) {
					fAttributeListViewer.setSelection(new StructuredSelection(newElements[0]));
				}
			} else {
				fAttributeListViewer.setSelection(new StructuredSelection(newSelectedElement));
			}
		}
	}

	// code for attribute up button
	private void attributeUp() {
		IStructuredSelection selection= (IStructuredSelection)fAttributeListViewer.getSelection();
		if (selection.size() > 0) {
			fAttributesContentProvider.up(selection.toList());
			fAttributeListViewer.refresh();
			fAttributeListViewer.setSelection(selection);
		}
	}

	// code for attribute down button
	private void attributeDown() {
		IStructuredSelection selection= (IStructuredSelection)fAttributeListViewer.getSelection();
		if (selection.size() > 0) {
			fAttributesContentProvider.down(selection.toList());
			fAttributeListViewer.refresh();
			fAttributeListViewer.setSelection(selection);
		}
	}

	// save the new attribute name typed by the user
	private void saveNewAttributeName() {
		if (fCurrentAttributeSelection.size() == 1) {
			String[] variable= (String[])fCurrentAttributeSelection.getFirstElement();
			variable[0]= fAttributeNameText.getText();
			fAttributeListViewer.refresh(variable);
		}
	}

	/*
	 * Display or hide the widgets specific to a logicial structure with
	 * variables.
	 */
	private void toggleAttributesWidgets(boolean isValue) {
		if (!isValue) {
			// recreate the attribute list
			fValueTmp= fSnippetDocument.get();
			createAttributeListWidgets();
		} else if (isValue) {
			// dispose the attribute list
			saveAttributeValue();
			Control[] children= fAttributesContainer.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].dispose();
			}
		}
		
		// dispose and recreate the code snippet editor group
		Control[] children= fCodeGroup.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		fSnippetViewer.dispose();
		createCodeGroupWidgets(isValue);
		setAttributesData(isValue);
		fParentComposite.layout(true, true);
	}

	/**
	 * Set the data in the attributes and code widgets
	 */
	private void setAttributesData(boolean isValue) {
		if (isValue) {
			fSnippetDocument.set(fValueTmp);
		} else {
			Object[] elements= fAttributesContentProvider.getElements(null);
			fCurrentAttributeSelection= new StructuredSelection();
			if (elements.length > 0) {
				IStructuredSelection newSelection= new StructuredSelection(elements[0]);
				fAttributeListViewer.setSelection(newSelection);
			} else {
				fAttributeListViewer.setSelection(fCurrentAttributeSelection);
			}
			
		}
	}

	/**
	 * Set the data and enable/disablet the attribute widgets for the current selection.
	 */
	private void setNameValueToSelection() {
		if (fCurrentAttributeSelection.size() == 1) {
			String[] variable= (String[]) fCurrentAttributeSelection.getFirstElement();
			fAttributeNameText.setText(variable[0]);
			fSnippetDocument.set(variable[1]);
			fAttributeNameText.setEnabled(true);
			fSnippetViewer.setEditable(true);
			fAttributeNameText.setSelection(0, variable[0].length());
			fAttributeNameText.setFocus();
		} else {
			fAttributeNameText.setEnabled(false);
			fSnippetViewer.setEditable(false);
			fAttributeNameText.setText(""); //$NON-NLS-1$
			fSnippetDocument.set(""); //$NON-NLS-1$
		}
	}

	// save the code to the current attribute.
	private void saveAttributeValue() {
		if (fCurrentAttributeSelection.size() == 1) {
			((String[])fCurrentAttributeSelection.getFirstElement())[1]= fSnippetDocument.get();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		saveAttributeValue();
		fCurrentAttributeSelection= (IStructuredSelection) event.getSelection();
		setNameValueToSelection();
		updateAttributeListButtons();
	}
	
	/**
	 * Enable/disable the attribute buttons for the current selection.
	 */
	private void updateAttributeListButtons() {
		int selectionSize= fCurrentAttributeSelection.size();
		if (selectionSize > 0) {
			fAttributeRemoveButton.setEnabled(true);
			Object[] elements= fAttributesContentProvider.getElements();
			fAttributeUpButton.setEnabled(fCurrentAttributeSelection.getFirstElement() != elements[0]);
			fAttributeDownButton.setEnabled(fCurrentAttributeSelection.toArray()[selectionSize - 1] != elements[elements.length - 1]);
		} else {
			fAttributeRemoveButton.setEnabled(false);
			fAttributeUpButton.setEnabled(false);
			fAttributeDownButton.setEnabled(false);
		}
	}

	/**
	 * Check the values in the widgets.
	 */
	public void checkValues() {
		StatusInfo status= new StatusInfo();
		if (fQualifiedTypeNameText.getText().trim().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_16); //$NON-NLS-1$
		} else if (fDescriptionText.getText().trim().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_17); //$NON-NLS-1$
		} else if (fValueButton.getSelection() && fSnippetDocument.get().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_18); //$NON-NLS-1$
		} else if (fVariablesButton.getSelection()) {
			Object[] elements= fAttributesContentProvider.getElements(null);
			boolean oneElementSelected= fCurrentAttributeSelection.size() == 1;
			if (elements.length == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_19); //$NON-NLS-1$
			} else if (oneElementSelected && fAttributeNameText.getText().trim().length() == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_20); //$NON-NLS-1$
			} else if (oneElementSelected && fSnippetDocument.get().trim().length() == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_21); //$NON-NLS-1$
			} else {
				for (int i= 0; i < elements.length; i++) {
					String[] variable= (String[]) elements[i];
					if (variable[0].trim().length() == 0) {
						status.setError(DebugUIMessages.EditLogicalStructureDialog_22); //$NON-NLS-1$
						break;
					}
					if (variable[1].trim().length() == 0) {
						if (!oneElementSelected || fCurrentAttributeSelection.getFirstElement() != variable) {
							status.setError(MessageFormat.format(DebugUIMessages.EditLogicalStructureDialog_23, new String[] {variable[0]})); //$NON-NLS-1$
							break;
						}
					}
				}
			}
		}
		if (!status.isError()) {
			if (fType == null && fTypeSearched) {
				status.setWarning(DebugUIMessages.EditLogicalStructureDialog_24); //$NON-NLS-1$
			}
		}
		updateStatus(status);
	}

	/**
	 * Open the 'select type' dialog, and set the user choice into the formatter.
	 */
	private void selectType() {
		Shell shell= getShell();
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES,
                false, fQualifiedTypeNameText.getText());
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.DetailFormatterDialog_Select_type_6; //$NON-NLS-1$
			String message= DebugUIMessages.DetailFormatterDialog_Could_not_open_type_selection_dialog_for_detail_formatters_7; //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}
	
		dialog.setTitle(DebugUIMessages.DetailFormatterDialog_Select_type_8); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.DetailFormatterDialog_Select_a_type_to_format_when_displaying_its_detail_9); //$NON-NLS-1$
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			fType = (IType)types[0];
			fQualifiedTypeNameText.setText(fType.getFullyQualifiedName());
			fTypeSearched = true;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		checkValues();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// save the new data in the logical structure
		fLogicalStructure.setType(fQualifiedTypeNameText.getText().trim());
		fLogicalStructure.setDescription(fDescriptionText.getText().trim());
		fLogicalStructure.setSubtypes(fSubTypeButton.getSelection());
		if (fValueButton.getSelection()) {
			fLogicalStructure.setValue(fSnippetDocument.get());
		} else {
			saveAttributeValue();
			fLogicalStructure.setValue(null);
		}
		if (fAttributesContentProvider != null) {
			fLogicalStructure.setVariables(fAttributesContentProvider.getElements());
		}
		super.okPressed();
	}

	/**
	 * Use the Java search engine to find the type which corresponds
	 * to the given name.
	 */
	private void findCorrespondingType() {
		if (fTypeSearched) {
			return;
		}
		fType= null;
		fTypeSearched= true;
		final String pattern= fQualifiedTypeNameText.getText().trim().replace('$', '.');
		if (pattern == null || "".equals(pattern)) { //$NON-NLS-1$
			return;
		}
		final IProgressMonitor monitor = new NullProgressMonitor();
		final SearchRequestor collector = new SearchRequestor() {
			private boolean fFirst= true;
			
			public void endReporting() {
				checkValues();
			}

			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				Object enclosingElement = match.getElement();
				if (!fFirst) {
					return;
				}
				fFirst= false;
				if (enclosingElement instanceof IType) {
					fType= (IType) enclosingElement;
				}
				// stop after we find one
				monitor.setCanceled(true);
			}
		};
		
		SearchEngine engine= new SearchEngine(JavaCore.getWorkingCopies(null));
		SearchPattern searchPattern = SearchPattern.createPattern(pattern, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		IJavaSearchScope scope= SearchEngine.createWorkspaceScope();
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		try {
			engine.search(searchPattern, participants, scope, collector, monitor);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		} catch (OperationCanceledException e){
		}
	}
	
	/**
	 * Return the type object which corresponds to the given name.
	 */
	public IType getType() {
		if (!fTypeSearched) {
			findCorrespondingType();
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		PlatformUI.getWorkbench().getCommandSupport().removeHandlerSubmission(fSubmission);
		
		fSnippetViewer.dispose();
		return super.close();
	}

}
