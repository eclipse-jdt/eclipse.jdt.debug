/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.display.DisplayCompletionProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A dialog which prompts the user to enter an expression.
 */
public class ExpressionInputDialog extends Dialog {

    private JDISourceViewer fViewer;
    private DisplayCompletionProcessor fCompletionProcessor;
    private IDocumentListener fDocumentListener;
    private HandlerSubmission fSubmission;
    private Text fErrorText;
    private IJavaVariable fVariable;
    
    private String fResult= null;
    
    /**
     * @param parentShell
     */
    protected ExpressionInputDialog(Shell parentShell, IJavaVariable variable) {
        super(parentShell);
        fVariable= variable;
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite= (Composite) super.createDialogArea(parent);
        Label label= new Label(composite, SWT.WRAP);
        label.setText(ActionMessages.getString("ExpressionInputDialog.0")); //$NON-NLS-1$
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        
        fViewer= new JDISourceViewer(composite, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fViewer.setInput(parent);
        
        fErrorText= new Text(composite, SWT.READ_ONLY);
        fErrorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fErrorText.setBackground(fErrorText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        fErrorText.setFont(parent.getFont());

        // Configure the source viewer after creating the error text so that any
        // necessary error messages can be set.
        configureSourceViewer();
        return composite;
    }
    
    private void configureSourceViewer() {
        JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);
		fViewer.configure(new DisplayViewerConfiguration() {
			public IContentAssistProcessor getContentAssistantProcessor() {
				return getCompletionProcessor();
			}
		});
		fViewer.setEditable(true);
		fViewer.setDocument(document);
		final IUndoManager undoManager= new DefaultUndoManager(10);
		fViewer.setUndoManager(undoManager);
		undoManager.connect(fViewer);
		
		fViewer.getTextWidget().setFont(JFaceResources.getTextFont());
			
		Control control= fViewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
			
		gd= (GridData)fViewer.getControl().getLayoutData();
		gd.heightHint= convertHeightInCharsToPixels(10);
		gd.widthHint= convertWidthInCharsToPixels(40);	
		document.set(getInitialText(fVariable));	
		
		fDocumentListener= new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            public void documentChanged(DocumentEvent event) {
                refreshValidState();
            }
        };
		fViewer.getDocument().addDocumentListener(fDocumentListener);
		
		IHandler handler = new AbstractHandler() {
		    public Object execute(Map parameter) throws ExecutionException {
		        fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		        return null;
			}
		};
		fSubmission = new HandlerSubmission(null, fViewer.getControl().getShell(), null, ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM); //$NON-NLS-1$
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		commandSupport.addHandlerSubmission(fSubmission);
    }
    
    /**
     * @param variable
     * @return
     */
    private String getInitialText(IJavaVariable variable) {
        try {
            String signature = variable.getSignature();
            if (signature.equals("Ljava/lang/String;")) { //$NON-NLS-1$
                IValue value = variable.getValue();
                if (!(value instanceof JDINullValue)) {
                    String currentValue= value.getValueString();
                    StringBuffer buffer= new StringBuffer(currentValue.length());
                    buffer.append('"'); // Surround value in quotes
                    char[] chars = currentValue.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        char c = chars[i];
                        if (c == '"') {
                            buffer.append("\\\""); //$NON-NLS-1$
                        } else {
                            buffer.append(c);
                        }
                    }
                    buffer.append('"'); // Surround value in quotes
                    return buffer.toString();
                }
            }
        } catch (DebugException e) {
        }
        return null;
    }

    /**
	 * Return the completion processor associated with this viewer.
	 * @return BreakPointConditionCompletionProcessor
	 */
	protected DisplayCompletionProcessor getCompletionProcessor() {
		if (fCompletionProcessor == null) {
			fCompletionProcessor= new DisplayCompletionProcessor();
		}
		return fCompletionProcessor;
	}
    
	/**
	 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
	 */
	private void refreshValidState() {
	    String errorMessage= null;
		String text= fViewer.getDocument().get();
		boolean valid= text != null && text.trim().length() > 0;
		if (!valid) {
			errorMessage= ActionMessages.getString("ExpressionInputDialog.1"); //$NON-NLS-1$
		}
		setErrorMessage(errorMessage);
	}
	
	private void setErrorMessage(String message) {
	    if (message == null) {
	        message= ""; //$NON-NLS-1$
	    }
	    fErrorText.setText(message);
	    getButton(IDialogConstants.OK_ID).setEnabled(message.length() == 0);
	}
	
    protected void okPressed() {
        fResult= fViewer.getDocument().get();
        
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		commandSupport.removeHandlerSubmission(fSubmission);
		
	    fViewer.getDocument().removeDocumentListener(fDocumentListener);
	    
        super.okPressed();
    }
    
    /**
     * Returns the text entered by the user or <code>null</code> if the user cancelled.
     * @return
     */
    public String getResult() {
        return fResult;
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ActionMessages.getString("ExpressionInputDialog.2")); //$NON-NLS-1$
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        //do this here because setting the text will set enablement on the ok
        // button
        refreshValidState();
    }
}
