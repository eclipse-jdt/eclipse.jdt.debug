/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.internal.ui.SWTFactory;
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
import org.eclipse.jdt.internal.debug.ui.contentassist.DynamicTypeContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.DynamicTypeContext.ITypeProvider;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A dialog that allows users to create/edit logical structures.
 */
public class EditLogicalStructureDialog extends StatusDialog implements Listener, ISelectionChangedListener, IDocumentListener, ITypeProvider {

	public class AttributesContentProvider implements IStructuredContentProvider {

		private final List<String[]> fVariables;

		public AttributesContentProvider(String[][] variables) {
			fVariables= new ArrayList<>();
			for (String[] v : variables) {
				String[] variable= new String[2];
				variable[0] = v[0];
				variable[1] = v[1];
				fVariables.add(variable);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return getElements();
		}

		/**
		 * Returns the attributes.
		 * @return the elements
		 */
		public String[][] getElements() {
			return fVariables.toArray(new String[fVariables.size()][]);
		}

		/**
		 * Adds the given attributes.
		 * @param newAttribute the new attribute
		 */
		public void add(String[] newAttribute) {
			fVariables.add(newAttribute);
		}

		/**
		 * Remove the given attributes
		 * @param list the list
		 */
		public void remove(List<String[]> list) {
			fVariables.removeAll(list);
		}

		/**
		 * Moves the given attributes up in the list.
		 * @param list the list
		 */
		public void up(List<String[]> list) {
			for (String[] variable : list) {
				int index= fVariables.indexOf(variable);
				fVariables.remove(variable);
				fVariables.add(index - 1, variable);
			}
		}

		/**
		 * Moves the given attributes down int the list.
		 * @param list the list
		 */
		public void down(List<String[]> list) {
			for (String[] variable : list) {
				int index= fVariables.indexOf(variable);
				fVariables.remove(variable);
				fVariables.add(index + 1, variable);
			}
		}

	}

	public static class AttributesLabelProvider extends LabelProvider {
		@Override
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
    private IHandlerActivation fHandlerActivation;

	public EditLogicalStructureDialog(Shell parentShell, JavaLogicalStructure logicalStructure) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		if (logicalStructure.getQualifiedTypeName().length() == 0) {
			setTitle(DebugUIMessages.EditLogicalStructureDialog_32);
		} else {
			setTitle(DebugUIMessages.EditLogicalStructureDialog_31);
		}
		fLogicalStructure= logicalStructure;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getHelpSystem().setHelp(
				parent,
				IJavaDebugHelpContextIds.EDIT_LOGICAL_STRUCTURE_DIALOG);
		fParentComposite= parent;

		IHandler handler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				if(fSnippetViewer.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS) && fSnippetViewer.getControl().isFocusControl()){
					findCorrespondingType();
					fSnippetViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
				return null;
			}
		};

		IHandlerService handlerService = workbench.getAdapter(IHandlerService.class);
        fHandlerActivation = handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);

		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);

		Composite typeNameDescriptionContainer = SWTFactory.createComposite(container, container.getFont(), 2, 1, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(typeNameDescriptionContainer, DebugUIMessages.EditLogicalStructureDialog_0, 2);

		fQualifiedTypeNameText = SWTFactory.createSingleText(typeNameDescriptionContainer, 1);
		fQualifiedTypeNameText.addListener(SWT.Modify, this);

		fBrowseTypeButton = SWTFactory.createPushButton(typeNameDescriptionContainer, DebugUIMessages.EditLogicalStructureDialog_1, DebugUIMessages.EditLogicalStructureDialog_25, null);

		fBrowseTypeButton.addListener(SWT.Selection, this);

		SWTFactory.createLabel(typeNameDescriptionContainer, DebugUIMessages.EditLogicalStructureDialog_2, 2);

		fDescriptionText = SWTFactory.createSingleText(typeNameDescriptionContainer, 2);
		fDescriptionText.addListener(SWT.Modify, this);

		fSubTypeButton = SWTFactory.createCheckButton(typeNameDescriptionContainer, DebugUIMessages.EditLogicalStructureDialog_3, null, false, 1);
		fSubTypeButton.setToolTipText(DebugUIMessages.EditLogicalStructureDialog_26);

		Group radioContainer= SWTFactory.createGroup(container, DebugUIMessages.EditLogicalStructureDialog_33, 1, 1, GridData.FILL_HORIZONTAL);

		fValueButton = SWTFactory.createRadioButton(radioContainer, DebugUIMessages.EditLogicalStructureDialog_4);
		fValueButton.addListener(SWT.Selection, this);

		fVariablesButton = SWTFactory.createRadioButton(radioContainer, DebugUIMessages.EditLogicalStructureDialog_5);

		fAttributesContainer = SWTFactory.createComposite(container, container.getFont(), 2, 1, GridData.FILL_HORIZONTAL);

		boolean isValue = fLogicalStructure.getValue() != null;
		if (!isValue) {
			// creates the attribute list if needed
			createAttributeListWidgets();
		}

		fCodeGroup = SWTFactory.createGroup(container, "", 1, 1, GridData.FILL_BOTH); //$NON-NLS-1$
		createCodeGroupWidgets(isValue);

		applyDialogFont(container);

		initializeData();

		return container;
	}

	/**
	 * Create the widgets it the code snippet editor group
	 * @param isValue if it is a snippet value
	 */
	private void createCodeGroupWidgets(boolean isValue) {
		if (isValue) {
			fCodeGroup.setText(DebugUIMessages.EditLogicalStructureDialog_9);
		} else {
			fCodeGroup.setText(DebugUIMessages.EditLogicalStructureDialog_7);

			Composite attributeNameContainer = SWTFactory.createComposite(fCodeGroup, fCodeGroup.getFont(), 2,  1, GridData.FILL_HORIZONTAL);
			((GridLayout)attributeNameContainer.getLayout()).marginWidth = 0;

			SWTFactory.createLabel(attributeNameContainer, DebugUIMessages.EditLogicalStructureDialog_8, 1);

			fAttributeNameText = SWTFactory.createSingleText(attributeNameContainer, 1);
			fAttributeNameText.addListener(SWT.Modify, this);

			SWTFactory.createLabel(fCodeGroup, DebugUIMessages.EditLogicalStructureDialog_9, 1);
		}

		// snippet viewer
		fSnippetViewer= new JDISourceViewer(fCodeGroup,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		fSnippetViewer.setInput(this);

		JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
		if (fSnippetDocument == null) {
			fSnippetDocument= new Document();
			fSnippetDocument.addDocumentListener(this);
		}
		tools.setupJavaDocumentPartitioner(fSnippetDocument, IJavaPartitions.JAVA_PARTITIONING);
		if (fViewerConfiguration == null) {
			fViewerConfiguration= new DisplayViewerConfiguration() {
				@Override
				public IContentAssistProcessor getContentAssistantProcessor() {
					return new JavaDebugContentAssistProcessor(new DynamicTypeContext(EditLogicalStructureDialog.this));
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
		fAttributeListViewer = new TableViewer(fAttributesContainer, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = (Table)fAttributeListViewer.getControl();
		GridData gd= new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint= convertHeightInCharsToPixels(5);
		gd.widthHint= convertWidthInCharsToPixels(10);
		table.setLayoutData(gd);
        table.setFont(fAttributesContainer.getFont());
		if (fAttributesContentProvider == null) {
			fAttributesContentProvider= new AttributesContentProvider(fLogicalStructure.getVariables());
		}
		fAttributeListViewer.setContentProvider(fAttributesContentProvider);
		fAttributeListViewer.setLabelProvider(new AttributesLabelProvider());
		fAttributeListViewer.setInput(this);
		fAttributeListViewer.addSelectionChangedListener(this);

		Composite attributeListButtonsCotnainer = SWTFactory.createComposite(fAttributesContainer, fAttributesContainer.getFont(), 1, 1, SWT.NONE);

		fAttributeAddButton = SWTFactory.createPushButton(attributeListButtonsCotnainer, DebugUIMessages.EditLogicalStructureDialog_10, DebugUIMessages.EditLogicalStructureDialog_27, null);
		fAttributeAddButton.addListener(SWT.Selection, this);

		fAttributeRemoveButton = SWTFactory.createPushButton(attributeListButtonsCotnainer, DebugUIMessages.EditLogicalStructureDialog_11, DebugUIMessages.EditLogicalStructureDialog_28, null);
		fAttributeRemoveButton.addListener(SWT.Selection, this);

		fAttributeUpButton = SWTFactory.createPushButton(attributeListButtonsCotnainer, DebugUIMessages.EditLogicalStructureDialog_12, DebugUIMessages.EditLogicalStructureDialog_29, null);
		fAttributeUpButton.addListener(SWT.Selection, this);

		fAttributeDownButton = SWTFactory.createPushButton(attributeListButtonsCotnainer, DebugUIMessages.EditLogicalStructureDialog_13, DebugUIMessages.EditLogicalStructureDialog_30, null);
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
	@Override
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
		String[] newAttribute= new String[] {DebugUIMessages.EditLogicalStructureDialog_14, DebugUIMessages.EditLogicalStructureDialog_15}; //
		fAttributesContentProvider.add(newAttribute);
		fAttributeListViewer.refresh();
		fAttributeListViewer.setSelection(new StructuredSelection((Object)newAttribute));
	}

	// code for remove attribute button
	private void removeAttribute() {
		IStructuredSelection selection= (IStructuredSelection)fAttributeListViewer.getSelection();
		if (selection.size() > 0) {
			List<String[]> selectedElements= selection.toList();
			Object newSelectedElement= null;
			for (Object element : fAttributesContentProvider.getElements()) {
				if (!selectedElements.contains(element)) {
					newSelectedElement = element;
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
	 * Display or hide the widgets specific to a logical structure with
	 * variables.
	 */
	private void toggleAttributesWidgets(boolean isValue) {
		if (!isValue) {
			// recreate the attribute list
			fValueTmp= fSnippetDocument.get();
			createAttributeListWidgets();
		} else {
			// dispose the attribute list
			saveAttributeValue();
			for (Control child : fAttributesContainer.getChildren()) {
				child.dispose();
			}
		}

		// dispose and recreate the code snippet editor group
		for (Control child : fCodeGroup.getChildren()) {
			child.dispose();
		}
		fSnippetViewer.dispose();
		createCodeGroupWidgets(isValue);
		setAttributesData(isValue);
		fParentComposite.layout(true, true);
	}

	/**
	 * Set the data in the attributes and code widgets
	 * @param isValue if it is a snippet value
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

	// save the code to the current attribute.
	private void saveAttributeValue() {
		if (fCurrentAttributeSelection.size() == 1) {
			((String[])fCurrentAttributeSelection.getFirstElement())[1]= fSnippetDocument.get();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		saveAttributeValue();
		fCurrentAttributeSelection= (IStructuredSelection) event.getSelection();
		boolean sizeone = fCurrentAttributeSelection.size() == 1;
	//update viewers
		fAttributeNameText.setEnabled(sizeone);
		fSnippetViewer.setEditable(sizeone);
		if (sizeone) {
			String[] variable = (String[]) fCurrentAttributeSelection.getFirstElement();
			fAttributeNameText.setText(variable[0]);
			fSnippetDocument.set(variable[1]);
			fAttributeNameText.setSelection(0, variable[0].length());
			fAttributeNameText.setFocus();
		} else {
			fAttributeNameText.setText(""); //$NON-NLS-1$
			fSnippetDocument.set(""); //$NON-NLS-1$
		}
	//update buttons
		fAttributeRemoveButton.setEnabled(fCurrentAttributeSelection.size() > 0);
		int index = fAttributeListViewer.getTable().getSelectionIndex();
		fAttributeUpButton.setEnabled(sizeone && (index != 0));
		fAttributeDownButton.setEnabled(sizeone && (index != fAttributeListViewer.getTable().getItemCount()-1));
	}

	/**
	 * Check the values in the widgets.
	 */
	public void checkValues() {
		StatusInfo status= new StatusInfo();
		if (fQualifiedTypeNameText.getText().trim().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_16);
		} else if (fDescriptionText.getText().trim().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_17);
		} else if (fValueButton.getSelection() && fSnippetDocument.get().length() == 0) {
			status.setError(DebugUIMessages.EditLogicalStructureDialog_18);
		} else if (fVariablesButton.getSelection()) {
			Object[] elements= fAttributesContentProvider.getElements(null);
			boolean oneElementSelected= fCurrentAttributeSelection.size() == 1;
			if (elements.length == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_19);
			} else if (oneElementSelected && fAttributeNameText.getText().trim().length() == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_20);
			} else if (oneElementSelected && fSnippetDocument.get().trim().length() == 0) {
				status.setError(DebugUIMessages.EditLogicalStructureDialog_21);
			} else {
				for (Object element : elements) {
					String[] variable = (String[]) element;
					if (variable[0].trim().length() == 0) {
						status.setError(DebugUIMessages.EditLogicalStructureDialog_22);
						break;
					}
					if (variable[1].trim().length() == 0) {
						if (!oneElementSelected || fCurrentAttributeSelection.getFirstElement() != variable) {
							status.setError(NLS.bind(DebugUIMessages.EditLogicalStructureDialog_23, variable[0]));
							break;
						}
					}
				}
			}
		}
		if (!status.isError()) {
			if (fType == null && fTypeSearched) {
				status.setWarning(DebugUIMessages.EditLogicalStructureDialog_24);
			}
		}
		updateStatus(status);
	}

	/**
	 * Open the 'select type' dialog, and set the user choice into the formatter.
	 */
	private void selectType() {
		Shell shell = getShell();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (projects.length == 0) {
			MessageDialog.openInformation(shell, DebugUIMessages.DetailFormatterDialog_Select_type_9, DebugUIMessages.DetailFormatterDialog_Select_type_10);
			return;
		}
		boolean allClosed = true;
		for (IProject iProject : projects) {
			if (iProject.isOpen()) {
				allClosed = false;
				break;
			}
		}

		if (allClosed) {
			MessageDialog.openInformation(shell, DebugUIMessages.DetailFormatterDialog_Select_type_9, DebugUIMessages.DetailFormatterDialog_Select_type_10);
			return;
		}
		SelectionDialog dialog= null;
		try {
			dialog= JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES,
                false, fQualifiedTypeNameText.getText());
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.DetailFormatterDialog_Select_type_6;
			String message= DebugUIMessages.DetailFormatterDialog_Could_not_open_type_selection_dialog_for_detail_formatters_7;
			ExceptionHandler.handle(jme, title, message);
			return;
		}

		dialog.setTitle(DebugUIMessages.DetailFormatterDialog_Select_type_8);
		dialog.setMessage(DebugUIMessages.DetailFormatterDialog_Select_a_type_to_format_when_displaying_its_detail_9);
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
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void documentChanged(DocumentEvent event) {
		checkValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
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

			@Override
			public void endReporting() {
				checkValues();
			}

			@Override
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
		if (searchPattern == null) {
			return;
		}
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
	 * @return the {@link IType}
	 */
	@Override
	public IType getType() {
		if (!fTypeSearched) {
			findCorrespondingType();
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IHandlerService handlerService = workbench.getAdapter(IHandlerService.class);
        handlerService.deactivateHandler(fHandlerActivation);

		fSnippetViewer.dispose();
		return super.close();
	}

}
