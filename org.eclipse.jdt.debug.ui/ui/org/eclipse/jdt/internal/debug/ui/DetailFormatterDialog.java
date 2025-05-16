/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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


import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
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
import org.eclipse.jdt.internal.debug.ui.contentassist.DynamicTypeContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.DynamicTypeContext.ITypeProvider;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Dialog for edit detail formatter.
 */
public class DetailFormatterDialog extends StatusDialog implements ITypeProvider {

	/**
	 * The detail formatter to edit.
	 */
	private final DetailFormatter fDetailFormatter;

	// widgets
	private Text fTypeNameText;
	private JDISourceViewer fSnippetViewer;
	private Button fCheckBox;

	/**
	 * Indicate if a search for a type with the given name
	 * have been already performed.
	 */
	private boolean fTypeSearched;

	/**
	 * Indicate if the type can be modified.
	 */
	private final boolean fEditTypeName;

	/**
	 * The type object which corresponds to the given name.
	 * If this field is <code>null</code> and <code>fTypeSearched</code> is
	 * <code>true</code>, that means there is no type with the given name in
	 * the workspace.
	 */
	private IType fType;

	/**
	 * List of types that have detail formatters already defined.
	 */
	private final List<?> fDefinedTypes;

    /**
     * Activation handler for content assist, must be deactivated on disposal.
     */
    private IHandlerActivation fHandlerActivation;

	/**
	 * DetailFormatterDialog constructor.  Creates a new dialog to create/edit a detail formatter.
	 *
	 * @param parent parent shell
	 * @param detailFormatter detail formatter to edit, not <code>null</code>
	 * @param definedTypes list of types with detail formatters already defined, or <code>null</code>
	 * @param editDialog whether the dialog is being used to edit a detail formatter
	 */
	public DetailFormatterDialog(Shell parent, DetailFormatter detailFormatter, List<?> definedTypes, boolean editDialog) {
		this(parent, detailFormatter, definedTypes, true, editDialog);
	}

	/**
	 * DetailFormatterDialog constructor.  Creates a new dialog to create/edit a detail formatter.
	 *
	 * @param parent parent shell
	 * @param detailFormatter detail formatter to edit, not <code>null</code>
	 * @param definedTypes list of types with detail formatters already defined, or <code>null</code>
	 * @param editTypeName whether the user should be able to modify the type
	 * @param editDialog whether the dialog is being used to edit a detail formatter
	 */
	public DetailFormatterDialog(Shell parent, DetailFormatter detailFormatter, List<?> definedTypes, boolean editTypeName, boolean editDialog) {
		super(parent);
		fDetailFormatter= detailFormatter;
		fTypeSearched= false;
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		if (editDialog) {
			setTitle(DebugUIMessages.DetailFormatterDialog_Edit_Detail_Formatter_1);
		} else {
			setTitle(DebugUIMessages.DetailFormatterDialog_Add_Detail_Formatter_2);
		}
		fEditTypeName= editTypeName;
		fDefinedTypes= definedTypes;
	}

	/**
	 * Create the dialog area.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		IWorkbench workbench = PlatformUI.getWorkbench();

		workbench.getHelpSystem().setHelp(
			parent,
			IJavaDebugHelpContextIds.EDIT_DETAIL_FORMATTER_DIALOG);

		Font font = parent.getFont();
		Composite container = (Composite)super.createDialogArea(parent);

		SWTFactory.createLabel(container, DebugUIMessages.DetailFormatterDialog_Qualified_type__name__2, 2);

		Composite innerContainer = SWTFactory.createComposite(container, font, 2, 1, GridData.FILL_HORIZONTAL);
		fTypeNameText = SWTFactory.createSingleText(innerContainer, 2);
		fTypeNameText.setEditable(fEditTypeName);
		fTypeNameText.setText(fDetailFormatter.getTypeName());
		fTypeNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fTypeSearched= false;
				checkValues();
			}
		});
		Button primitiveSearchButton = SWTFactory.createPushButton(innerContainer, DebugUIMessages.DetailFormatterPrimitiveSelect, null);
		primitiveSearchButton.setEnabled(fEditTypeName);

		GridData gridLayout = new GridData(GridData.FILL_HORIZONTAL);
		gridLayout.horizontalSpan = 1;
		gridLayout.grabExcessHorizontalSpace = true;
		primitiveSearchButton.setLayoutData(gridLayout);
		primitiveSearchButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				selectPrimitiveType();
			}
		});

		Button typeSearchButton = SWTFactory.createPushButton(innerContainer, DebugUIMessages.DetailFormatterDialog_Select__type_4, null);
		typeSearchButton.setEnabled(fEditTypeName);
		typeSearchButton.setLayoutData(gridLayout);
		typeSearchButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				selectType();
			}
		});


		String labelText = null;
		IBindingService bindingService = workbench.getAdapter(IBindingService.class);
		String binding = bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST);
        if (binding != null) {
			labelText = NLS.bind(DebugUIMessages.DetailFormatterDialog_17, binding);
        }
        if (labelText == null) {
            labelText = DebugUIMessages.DetailFormatterDialog_Detail_formatter__code_snippet__1;
        }

        SWTFactory.createLabel(container, labelText, 1);

        createSnippetViewer(container);

		fCheckBox = SWTFactory.createCheckButton(container, DebugUIMessages.DetailFormatterDialog__Enable_1, null, fDetailFormatter.isEnabled(), 1);

		// Set up content assist in the viewer
        IHandler handler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				if (fSnippetViewer.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS) && fSnippetViewer.getControl().isFocusControl()){
					findCorrespondingType();
					fSnippetViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
				return null;
			}
		};
        IHandlerService handlerService = workbench.getAdapter(IHandlerService.class);
        fHandlerActivation = handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);

		checkValues();
		return container;
	}

	/**
	 * Creates the JDISourceViewer that displays the code snippet to the user.
	 *
	 * @param parent parent composite
	 */
	private void createSnippetViewer(Composite parent) {
		fSnippetViewer= new JDISourceViewer(parent,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		fSnippetViewer.setInput(this);

		JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		fSnippetViewer.configure(new DisplayViewerConfiguration() {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return new JavaDebugContentAssistProcessor(new DynamicTypeContext(DetailFormatterDialog.this));
			}
		});
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);

		Control control= fSnippetViewer.getControl();
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
		document.set(fDetailFormatter.getSnippet());

		fSnippetViewer.getDocument().addDocumentListener(new IDocumentListener() {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			@Override
			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});

        if (fDetailFormatter.getTypeName().length() > 0) {
            fSnippetViewer.getControl().setFocus();
        }
	}

	/**
	 * Check the field values and display a message in the status if needed.
	 */
	private void checkValues() {
		StatusInfo status= new StatusInfo();
		String typeName= fTypeNameText.getText().trim();
		if (typeName.length() == 0) {
			status.setError(DebugUIMessages.DetailFormatterDialog_Qualified_type_name_must_not_be_empty__3);
		} else if (fDefinedTypes != null && fDefinedTypes.contains(typeName)) {
			status.setError(DebugUIMessages.DetailFormatterDialog_A_detail_formatter_is_already_defined_for_this_type_2);
		} else if (fSnippetViewer.getDocument().get().trim().length() == 0) {
			status.setError(DebugUIMessages.DetailFormatterDialog_Associated_code_must_not_be_empty_3);
		} else if (fType == null && fTypeSearched && !getPrimitiveTypes().contains(typeName)) {
			status.setWarning(DebugUIMessages.No_type_with_the_given_name_found_in_the_workspace__1);
		}
		updateStatus(status);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fDetailFormatter.setEnabled(fCheckBox.getSelection());
		fDetailFormatter.setTypeName(fTypeNameText.getText().trim());
		fDetailFormatter.setSnippet(fSnippetViewer.getDocument().get());

		super.okPressed();
	}

	/**
	 * Open the 'select type' dialog, and set the user choice into the formatter.
	 */
	private void selectType() {
		Shell shell= getShell();
		SelectionDialog dialog= null;
		try {
			dialog = JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(), SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false, fTypeNameText.getText());
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
			fTypeNameText.setText(fType.getFullyQualifiedName());
			fTypeSearched = true;
		}
	}

	/**
	 * Open the 'select type' dialog, and set the primitive into the formatter.
	 */
	private void selectPrimitiveType() {
		Shell shell = getShell();
		ListDialog listDialog = new ListDialog(shell);
		listDialog.setTitle(DebugUIMessages.DetailFormatterPrimitiveSelectionTitle);
		listDialog.setMessage(DebugUIMessages.DetailFormatterPrimitiveSelectionLabel);
		listDialog.setInput(getPrimitiveTypes().toArray());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
		if (listDialog.open() == Window.CANCEL) {
			return;
		}
		Object[] types = listDialog.getResult();
		if (types != null && types.length > 0) {
			String s = (String) types[0];
			fTypeNameText.setText(s);
			fTypeSearched = true;
		}
	}

	/**
	 * Returns a List of available primitives and arrays
	 *
	 * @return List primitives and arrays
	 */
	@SuppressWarnings("nls")
	private List<String> getPrimitiveTypes() {
		return List.of("int", "long", "short", "float", "double", "boolean", "char", "byte", "int[]", "long[]", "short[]", "float[]", "double[]", "boolean[]", "char[]", "byte[]");
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
		final String pattern= fTypeNameText.getText().trim().replace('$', '.');
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
				// cancel once we have one match
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
		} catch (OperationCanceledException e) {
		}
	}

	/**
	 * Return the type object which corresponds to the given name.
	 */
	@Override
	public IType getType() {
		if (!fTypeSearched) {
			findCorrespondingType();
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
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
