package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
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

class DetailFormatterDialog extends StatusDialog {
	
	private DetailFormatter fDetailFormat;
	
	private Text fTypeNameText;

	private JDISourceViewer fSnippetViewer;
	
	public DetailFormatterDialog(Shell parent, DetailFormatter detailFormat, boolean editDialog) {
		super(parent);
		fDetailFormat= detailFormat;
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		if (editDialog) {
			setTitle(DebugUIMessages.getString("DetailFormatterDialog.Edit_Detail_Formatter_1")); //$NON-NLS-1$
		} else {
			setTitle(DebugUIMessages.getString("DetailFormatterDialog.Add_Detail_Formatter_2")); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
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
		fTypeNameText.setText(fDetailFormat.getTypeName());
		gd= new GridData(GridData.FILL_HORIZONTAL);
		fTypeNameText.setLayoutData(gd);
		fTypeNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkValues();
			}
		});
		
		// type search button
		Button typeSearchButton = SWTUtil.createPushButton(innerContainer, DebugUIMessages.getString("DetailFormatterDialog.Select_&type_4"), null);  //$NON-NLS-1$
		gd= (GridData)typeSearchButton.getLayoutData();
		gd.horizontalAlignment = GridData.END;
		typeSearchButton.setLayoutData(gd);
		typeSearchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selectType();
			}
		});
		
		// snippet label
		label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("DetailFormatterDialog.Associated_&code__5")); //$NON-NLS-1$
		gd= new GridData(GridData.BEGINNING);
		label.setLayoutData(gd);

		// snippet viewer
		fSnippetViewer= new JDISourceViewer(container,  null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
	
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);		
		fSnippetViewer.configure(new JavaSourceViewerConfiguration(tools, null));
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		fSnippetViewer.getTextWidget().setFont(font);
		
		Control control= fSnippetViewer.getControl();
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
		document.set(fDetailFormat.getSnippet());
		
		fSnippetViewer.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});
		
		checkValues();

		return container;
	}
	
	
	private void checkValues() {
		StatusInfo status= new StatusInfo();
		if (fTypeNameText.getText().trim().length() == 0) {
			status.setError(DebugUIMessages.getString("DetailFormatterDialog.Qualified_type_name_must_not_be_empty._3")); //$NON-NLS-1$
		} else if (fSnippetViewer.getDocument().get().trim().length() == 0) {
			status.setError(DebugUIMessages.getString("DetailFormatterDialog.Associated_code_must_not_be_empty_3")); //$NON-NLS-1$
		}
		updateStatus(status);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fDetailFormat.setEnabled(true);
		fDetailFormat.setTypeName(fTypeNameText.getText());
		fDetailFormat.setSnippet(fSnippetViewer.getDocument().get());
		
		super.okPressed();
	}
	
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
			IType type = (IType)types[0];
			fTypeNameText.setText(type.getFullyQualifiedName());
		}		
	}
	
}