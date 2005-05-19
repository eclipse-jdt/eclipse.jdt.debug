/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Dialog for edit detail formatter.
 */
public class DetailFormatterDialog extends StatusDialog implements CodeSnippetCompletionProcessor.ITypeProvider {
	
	/**
	 * The detail formatter to edit.
	 */
	private DetailFormatter fDetailFormatter;

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
	private boolean fEditTypeName;

	/**
	 * The type object which corresponds to the given name.
	 * If this field is <code>null</code> and <code>fTypeSearched</code> is
	 * <code>true</code>, that means there is no type with the given name in 
	 * the workspace.
	 */
	private IType fType;
	
	private List fDefinedTypes;

	private HandlerSubmission submission;
	
	/**
	 * DetailFormatterDialog constructor.
	 * 
	 * @param detailFormatter the detail formatter to edit/add.
	 * @param editDialog flag which indicates if the dialog is used for
	 * edit an existing formatter, or for enter the info of a new one.
	 */
	public DetailFormatterDialog(Shell parent, DetailFormatter detailFormatter, List definedTypes, boolean editDialog) {
		this(parent, detailFormatter, definedTypes, true, editDialog);
	}
	
	public DetailFormatterDialog(Shell parent, DetailFormatter detailFormatter, List definedTypes, boolean editTypeName, boolean editDialog) {
		super(parent);
		fDetailFormatter= detailFormatter;
		fTypeSearched= false;
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		if (editDialog) {
			setTitle(DebugUIMessages.DetailFormatterDialog_Edit_Detail_Formatter_1); //$NON-NLS-1$
		} else {
			setTitle(DebugUIMessages.DetailFormatterDialog_Add_Detail_Formatter_2); //$NON-NLS-1$
		}
		fEditTypeName= editTypeName;
		fDefinedTypes= definedTypes;
	}
	
	/**
	 * Create the dialog area.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
			parent,
			IJavaDebugHelpContextIds.EDIT_DETAIL_FORMATTER_DIALOG);			
		
		Font font = parent.getFont();

		IHandler handler = new AbstractHandler() {
			public Object execute(Map parameterValuesByName) throws ExecutionException {
				findCorrespondingType();
				fSnippetViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);				
				return null;
			}
		};
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();		
		submission = new HandlerSubmission(null, parent.getShell(), null, ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM); //$NON-NLS-1$
		commandSupport.addHandlerSubmission(submission);	
		
		Composite container = (Composite)super.createDialogArea(parent);
		
		// type name label
		Label label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.DetailFormatterDialog_Qualified_type__name__2); //$NON-NLS-1$
		GridData gd= new GridData(GridData.BEGINNING);
		label.setLayoutData(gd);
		label.setFont(font);

		Composite innerContainer = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		innerContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		innerContainer.setLayoutData(gd);
		// type name text
		fTypeNameText= new Text(innerContainer, SWT.SINGLE | SWT.BORDER);
		fTypeNameText.setEditable(fEditTypeName);
		fTypeNameText.setText(fDetailFormatter.getTypeName());
		gd= new GridData(GridData.FILL_HORIZONTAL);
		fTypeNameText.setLayoutData(gd);
		fTypeNameText.setFont(font);		
		fTypeNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fTypeSearched= false;
				checkValues();
			}
		});
		
		// type search button
		Button typeSearchButton = new Button(innerContainer, SWT.PUSH); 
		typeSearchButton.setText(DebugUIMessages.DetailFormatterDialog_Select__type_4);  //$NON-NLS-1$
		setButtonLayoutData(typeSearchButton);
		gd= (GridData)typeSearchButton.getLayoutData();
		gd.horizontalAlignment = GridData.END;
		typeSearchButton.setEnabled(fEditTypeName);
		typeSearchButton.setLayoutData(gd);
		typeSearchButton.setFont(font);		
		typeSearchButton.setEnabled(fEditTypeName);
		typeSearchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selectType();
			}
		});
		
		// snippet label
		String labelText = null;
		ICommandManager commandManager = PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
		ICommand command = commandManager.getCommand("org.eclipse.ui.edit.text.contentAssist.proposals"); //$NON-NLS-1$
		if (command != null) {
			List keyBindings = command.getKeySequenceBindings();
			if (keyBindings != null && keyBindings.size() > 0) {
				IKeySequenceBinding binding = (IKeySequenceBinding)keyBindings.get(0);
				labelText = MessageFormat.format(DebugUIMessages.DetailFormatterDialog_17, new String[] {binding.getKeySequence().format()});  //$NON-NLS-1$
			} 
		}
		if (labelText == null) {
			labelText = DebugUIMessages.DetailFormatterDialog_Detail_formatter__code_snippet__1; //$NON-NLS-1$
		}
		
		label= new Label(container, SWT.NONE);
		label.setText(labelText); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		label.setLayoutData(gd);
		label.setFont(font);

		// snippet viewer
		fSnippetViewer= new JDISourceViewer(container,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
		fSnippetViewer.setInput(this);
	
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);		
		fSnippetViewer.configure(new DisplayViewerConfiguration() {
			public IContentAssistProcessor getContentAssistantProcessor() {
				return new CodeSnippetCompletionProcessor(DetailFormatterDialog.this);
			}
		});
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);
		
		Control control= fSnippetViewer.getControl();
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
		document.set(fDetailFormatter.getSnippet());	
		
		fSnippetViewer.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});
        
        if (fDetailFormatter.getTypeName().length() > 0) {
            fSnippetViewer.getControl().setFocus();
        }
		
		// enable checkbox
		fCheckBox= new Button(container, SWT.CHECK | SWT.LEFT);
		fCheckBox.setText(DebugUIMessages.DetailFormatterDialog__Enable_1); //$NON-NLS-1$
		fCheckBox.setSelection(fDetailFormatter.isEnabled());
		fCheckBox.setFont(font);
		
		checkValues();
		return container;
	}
	
	/**
	 * Check the field values and display a message in the status if needed.
	 */
	private void checkValues() {
		StatusInfo status= new StatusInfo();
		String typeName= fTypeNameText.getText().trim();
		if (typeName.length() == 0) {
			status.setError(DebugUIMessages.DetailFormatterDialog_Qualified_type_name_must_not_be_empty__3); //$NON-NLS-1$
		} else if (fDefinedTypes != null && fDefinedTypes.contains(typeName)) {
			status.setError(DebugUIMessages.DetailFormatterDialog_A_detail_formatter_is_already_defined_for_this_type_2); //$NON-NLS-1$
		} else if (fSnippetViewer.getDocument().get().trim().length() == 0) {
			status.setError(DebugUIMessages.DetailFormatterDialog_Associated_code_must_not_be_empty_3); //$NON-NLS-1$
		} else if (fType == null && fTypeSearched) {
			status.setWarning(DebugUIMessages.No_type_with_the_given_name_found_in_the_workspace__1); //$NON-NLS-1$
		}
		updateStatus(status);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
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
			dialog= JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false, fTypeNameText.getText());
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
			fTypeNameText.setText(fType.getFullyQualifiedName());
			fTypeSearched = true;
		}		
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
				// cancel once we have one match
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
		} catch (OperationCanceledException e) {
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
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		IWorkbench workbench = PlatformUI.getWorkbench();

		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		commandSupport.removeHandlerSubmission(submission);
		
		fSnippetViewer.dispose();
		return super.close();
	}

}
