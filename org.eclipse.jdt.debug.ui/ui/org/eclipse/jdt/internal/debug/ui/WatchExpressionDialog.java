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


import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog for edit watch expression.
 */
public class WatchExpressionDialog extends StatusDialog {

	/**
	 * The detail formatter to edit.
	 */
	private JavaWatchExpression fWatchExpression;

	// widgets
	private JDISourceViewer fSnippetViewer;
	private Button fCheckBox;

	public WatchExpressionDialog(Shell parent, JavaWatchExpression watchExpression, boolean editDialog) {
		super(parent);
		fWatchExpression= watchExpression;
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
		String helpContextId = null;
		if (editDialog) {
			setTitle(DebugUIMessages.getString("WatchExpressionDialog.Edit_Watch_Expression_1")); //$NON-NLS-1$
			helpContextId = IJavaDebugHelpContextIds.EDIT_WATCH_EXPRESSION_DIALOG;
		} else {
			setTitle(DebugUIMessages.getString("WatchExpressionDialog.Add_Watch_Expression_2")); //$NON-NLS-1$
			helpContextId = IJavaDebugHelpContextIds.ADD_WATCH_EXPRESSION_DIALOG;
		}
		WorkbenchHelp.setHelp(parent, helpContextId);
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

		// snippet label
		Label label= new Label(container, SWT.NONE);
		label.setText(DebugUIMessages.getString("WatchExpressionDialog.E&xpression_3")); //$NON-NLS-1$
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
		fSnippetViewer.configure(new JavaSourceViewerConfiguration(JavaPlugin.getDefault().getJavaTextTools(), null));
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);
		document.addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});

		fSnippetViewer.getTextWidget().setFont(JFaceResources.getTextFont());

		Control control= fSnippetViewer.getControl();
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(80);
		control.setLayoutData(gd);
		document.set(fWatchExpression.getExpressionText());

		// enable checkbox
		fCheckBox= new Button(container, SWT.CHECK | SWT.LEFT);
		fCheckBox.setText(DebugUIMessages.getString("WatchExpressionDialog.&Enable_4")); //$NON-NLS-1$
		fCheckBox.setSelection(fWatchExpression.isEnabled());
		fCheckBox.setFont(font);

		applyDialogFont(container);
		fSnippetViewer.getControl().setFocus();
		return container;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fWatchExpression.setEnabled(fCheckBox.getSelection());
		fWatchExpression.setExpressionText(fSnippetViewer.getDocument().get());
		super.okPressed();
	}
	
	/**
	 * Check the field values and display a message in the status if needed.
	 */
	private void checkValues() {
		StatusInfo status= new StatusInfo();
		if (fSnippetViewer.getDocument().get().trim().length() == 0) {
			status.setError(DebugUIMessages.getString("WatchExpressionDialog.Expression_must_not_be_empty_5")); //$NON-NLS-1$
		}
		updateStatus(status);
	}

}
