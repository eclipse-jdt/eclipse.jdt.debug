/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
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
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Dialog for edit detail formatter.
 */
public class DetailFormatterDialog extends StatusDialog {
	
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
			setTitle(DebugUIMessages.getString("DetailFormatterDialog.Edit_Detail_Formatter_1")); //$NON-NLS-1$
		} else {
			setTitle(DebugUIMessages.getString("DetailFormatterDialog.Add_Detail_Formatter_2")); //$NON-NLS-1$
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
		Font font = parent.getFont();
		
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		// type name label
		Label label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("DetailFormatterDialog.Qualified_type_&name__2")); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		label.setLayoutData(gd);
		label.setFont(font);

		Composite innerContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
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
		typeSearchButton.setText(DebugUIMessages.getString("DetailFormatterDialog.Select_&type_4"));  //$NON-NLS-1$
		setButtonLayoutData(typeSearchButton);
		gd= (GridData)typeSearchButton.getLayoutData();
		gd.horizontalAlignment = GridData.END;
		typeSearchButton.setLayoutData(gd);
		typeSearchButton.setFont(font);		
		typeSearchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selectType();
			}
		});
		
		// snippet label
		label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("DetailFormatterDialog.Detail_formatter_&code_snippet__1")); //$NON-NLS-1$
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
				return new DetailFormatterCompletionProcessor(DetailFormatterDialog.this);
			}
		});
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);
	
		fSnippetViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		
		Control control= fSnippetViewer.getControl();
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
		document.set(fDetailFormatter.getSnippet());
		
		fSnippetViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				//do code assist for CTRL-SPACE
				if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
					if (event.character == 0x20) {
						findCorrespondingType();
						fSnippetViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						event.doit= false;
					}
				}
			}
		});
		
		fSnippetViewer.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});
		
		// enable checkbox
		fCheckBox= new Button(container, SWT.CHECK | SWT.LEFT);
		fCheckBox.setText(DebugUIMessages.getString("DetailFormatterDialog.&Enable_1")); //$NON-NLS-1$
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
			status.setError(DebugUIMessages.getString("DetailFormatterDialog.Qualified_type_name_must_not_be_empty._3")); //$NON-NLS-1$
		} else if (fDefinedTypes != null && fDefinedTypes.contains(typeName)) {
			status.setError(DebugUIMessages.getString("DetailFormatterDialog.A_detail_formatter_is_already_defined_for_this_type_2")); //$NON-NLS-1$
		} else if (fSnippetViewer.getDocument().get().trim().length() == 0) {
			status.setError(DebugUIMessages.getString("DetailFormatterDialog.Associated_code_must_not_be_empty_3")); //$NON-NLS-1$
		} else if (fType == null && fTypeSearched) {
			status.setWarning(DebugUIMessages.getString("No_type_with_the_given_name_found_in_the_workspace._1")); //$NON-NLS-1$
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
			dialog= JavaUI.createTypeDialog(shell, new ProgressMonitorDialog(shell),
				SearchEngine.createWorkspaceScope(), IJavaElementSearchConstants.CONSIDER_CLASSES, false);
		} catch (JavaModelException jme) {
			String title= DebugUIMessages.getString("DetailFormatterDialog.Select_type_6"); //$NON-NLS-1$
			String message= DebugUIMessages.getString("DetailFormatterDialog.Could_not_open_type_selection_dialog_for_detail_formatters_7"); //$NON-NLS-1$
			ExceptionHandler.handle(jme, title, message);
			return;
		}
	
		dialog.setTitle(DebugUIMessages.getString("DetailFormatterDialog.Select_type_8")); //$NON-NLS-1$
		dialog.setMessage(DebugUIMessages.getString("DetailFormatterDialog.Select_a_type_to_format_when_displaying_its_detail_9")); //$NON-NLS-1$
		if (dialog.open() == IDialogConstants.CANCEL_ID) {
			return;
		}
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			fType = (IType)types[0];
			fTypeNameText.setText(fType.getFullyQualifiedName());
		}		
	}
	
	/**
	 * Use the java search engine for find the (a) type which corresponds
	 * to the given name.
	 */
	private void findCorrespondingType() {
		if (fTypeSearched) {
			return;
		}
		fType= null;
		fTypeSearched= true;
		final String pattern= fTypeNameText.getText().trim();
		if (pattern == null || "".equals(pattern)) { //$NON-NLS-1$
			return;
		}
		final IJavaSearchResultCollector collector= new IJavaSearchResultCollector() {
			private boolean fFirst= true;
			
			public void aboutToStart() {
			}

			public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy) throws CoreException {
				if (!fFirst) {
					return;
				}
				fFirst= false;
				if (enclosingElement instanceof IType) {
					fType= (IType) enclosingElement;
				}
			}

			public void done() {
				checkValues();
			}

			public IProgressMonitor getProgressMonitor() {
				return null;
			}
		};
	
		SearchEngine engine= new SearchEngine(JavaUI.getSharedWorkingCopies());
		IJavaSearchScope scope= SearchEngine.createWorkspaceScope();
		try {
			engine.search(JavaPlugin.getWorkspace(), SearchEngine.createSearchPattern(pattern, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, true), scope, collector);
		} catch (JavaModelException e) {
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

}
