/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.DialogSettingsHelper;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
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
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
 * A dialog which prompts the user to enter an expression for
 * evaluation.
 */
public class ExpressionInputDialog extends Dialog {

    protected IJavaVariable fVariable;
    protected String fResult= null;
    
    // Input area composite which acts as a placeholder for
    // input widgetry that is created/disposed dynamically.
    protected Composite fInputArea;
    // Source viewer widgets
    protected Label fEvaluateLabel;
    protected JDISourceViewer fSourceViewer;
    protected DisplayCompletionProcessor fCompletionProcessor;
    protected IDocumentListener fDocumentListener;
    protected HandlerSubmission fSubmission;
    // Text for error reporting
    protected Text fErrorText;
    
    private boolean fShellResized= false;
    
    /**
     * @param parentShell
     */
    protected ExpressionInputDialog(Shell parentShell, IJavaVariable variable) {
        super(parentShell);
        setShellStyle(SWT.CLOSE|SWT.MIN|SWT.MAX|SWT.RESIZE);
        fVariable= variable;
    }

    /**
     * Creates and populates the dialog area
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite= (Composite) super.createDialogArea(parent);
        
        // Create the composite which will hold the input widgetry
        createInputArea(composite);
        // Create the error reporting text area
        createErrorText(composite);

        // Create the source viewer after creating the error text so that any
        // necessary error messages can be set.
        populateInputArea();
        return composite;
    }
    
    /**
     * Creates the text widget for reporting errors
     */
    protected void createErrorText(Composite parent) {
        fErrorText= new Text(parent, SWT.READ_ONLY);
        fErrorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        fErrorText.setBackground(fErrorText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        fErrorText.setFont(parent.getFont());
    }

    /**
     * Creates the composite that will be used to contain the
     * input widgetry.
     * @param composite the parent composite
     */
    protected void createInputArea(Composite parent) {
        fInputArea= new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        fInputArea.setLayoutData(gridData);
        GridLayout layout = new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        fInputArea.setLayout(layout);
        Dialog.applyDialogFont(fInputArea);
    }
    
    /**
     * Creates the appropriate widgetry in the input area. This
     * method is intended to be overridden by subclasses who wish
     * to use alternate input widgets.
     */
    protected void populateInputArea() {
        createSourceViewer();
    }

    /**
     * Creates the source viewer that allows the user to enter
     * an evaluation expression.
     */
    protected void createSourceViewer() {
        Composite parent= fInputArea;
        String name= ActionMessages.ExpressionInputDialog_3; //$NON-NLS-1$
        try {
            name= fVariable.getName();
        } catch (DebugException e) {
            JDIDebugUIPlugin.log(e);
        }
        
        fEvaluateLabel= new Label(parent, SWT.WRAP);
        fEvaluateLabel.setText(MessageFormat.format(ActionMessages.ExpressionInputDialog_0, new String[] {name})); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        fEvaluateLabel.setLayoutData(data);
        fEvaluateLabel.setFont(parent.getFont());
        
        fSourceViewer= new JDISourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fSourceViewer.setInput(parent);
        configureSourceViewer();
        fSourceViewer.doOperation(ITextOperationTarget.SELECT_ALL);
    }
    
    /**
     * Initializes the source viewer. This method is based on code in BreakpointConditionEditor.
     */
    private void configureSourceViewer() {
        JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocument document= new Document();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);
		fSourceViewer.configure(new DisplayViewerConfiguration() {
			public IContentAssistProcessor getContentAssistantProcessor() {
				return getCompletionProcessor();
			}
		});
		fSourceViewer.setEditable(true);
		fSourceViewer.setDocument(document);
		final IUndoManager undoManager= new DefaultUndoManager(10);
		fSourceViewer.setUndoManager(undoManager);
		undoManager.connect(fSourceViewer);
		
		fSourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());
			
		Control control= fSourceViewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
			
		gd= (GridData)fSourceViewer.getControl().getLayoutData();
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
		fSourceViewer.getDocument().addDocumentListener(fDocumentListener);
		
		IHandler handler = new AbstractHandler() {
		    public Object execute(Map parameter) throws ExecutionException {
		        fSourceViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		        return null;
			}
		};
		fSubmission = new HandlerSubmission(null, fSourceViewer.getControl().getShell(), null, ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM); //$NON-NLS-1$
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		commandSupport.addHandlerSubmission(fSubmission);
    }
    
    /**
     * Returns the text that should be shown in the source viewer upon
     * initialization. The text should be presented in such a way that
     * it can be used as an evaluation expression which will return the
     * current value.
     * @param variable the variable
     * @return the initial text to display in the source viewer or <code>null</code>
     *  if none.
     */
    protected String getInitialText(IJavaVariable variable) {
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
                        if (c == '\b') {
                            buffer.append("\\b"); //$NON-NLS-1$
                        } else if (c == '\t') {
                            buffer.append("\\t"); //$NON-NLS-1$
                        } else if (c == '\n') {
                            buffer.append("\\n"); //$NON-NLS-1$
                        } else if (c == '\f') {
                            buffer.append("\\f"); //$NON-NLS-1$
                        } else if (c == '\r') {
                            buffer.append("\\r"); //$NON-NLS-1$
                        } else if (c == '"') {
                            buffer.append("\\\""); //$NON-NLS-1$
                        } else if (c == '\'') {
                            buffer.append("\\\'"); //$NON-NLS-1$
                        } else if (c == '\\') {
                            buffer.append("\\\\"); //$NON-NLS-1$
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
	 * @return DisplayConditionCompletionProcessor
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
	protected void refreshValidState() {
	    String errorMessage= null;
		String text= fSourceViewer.getDocument().get();
		boolean valid= text != null && text.trim().length() > 0;
		if (!valid) {
			errorMessage= ActionMessages.ExpressionInputDialog_1; //$NON-NLS-1$
		}
		setErrorMessage(errorMessage);
	}
	
	/**
	 * Sets the error message to display to the user. <code>null</code>
	 * is the same as the empty string.
	 * @param message the error message to display to the user or
	 *  <code>null</code> if the error message should be cleared
	 */
	protected void setErrorMessage(String message) {
	    if (message == null) {
	        message= ""; //$NON-NLS-1$
	    }
	    fErrorText.setText(message);
	    getButton(IDialogConstants.OK_ID).setEnabled(message.length() == 0);
	}
	
	/**
	 * Persist the dialog size and store the user's input on OK is pressed.
	 */
    protected void okPressed() {
        fResult= getText();
		dispose();
        super.okPressed();
    }
    
    /**
     * Returns the text that is currently displayed in the source viewer.
     * @return the text that is currently displayed in the source viewer
     */
    protected String getText() {
        return fSourceViewer.getDocument().get();
    }
    
    /**
     * Disposes the source viewer. This method is intended to be overridden
     * by subclasses.
     */
    protected void dispose() {
        disposeSourceViewer();
    }
    
    /**
     * Persists the current dialog dimensions in the dialog settings
     */
    protected void persistDialogSize() {
        if (fShellResized) {
        	DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
        }
    }

    /**
     * Disposes the source viewer and all associated widgetry.
     */
    protected void disposeSourceViewer() {
        if (fSubmission != null) {
            IWorkbenchCommandSupport commandSupport = PlatformUI.getWorkbench().getCommandSupport();
			commandSupport.removeHandlerSubmission(fSubmission);
		    fSubmission= null;
        }
		if (fSourceViewer != null) {
	    	fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
	    	fSourceViewer.getTextWidget().dispose();
		    fSourceViewer.dispose();
		    fSourceViewer= null;
		}
	    if (fEvaluateLabel != null) {
		    fEvaluateLabel.dispose();
		    fEvaluateLabel= null;
	    }		
	    fDocumentListener= null;
	    fCompletionProcessor= null;
    }
    
    /**
     * Returns the text entered by the user or <code>null</code> if the user cancelled.
     * @return the text entered by the user or <code>null</code> if the user cancelled
     */
    public String getResult() {
        return fResult;
    }
    
    /**
     * Initializes the dialog shell with a title.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ActionMessages.ExpressionInputDialog_2); //$NON-NLS-1$
        newShell.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
            }
            public void controlResized(ControlEvent e) {
                fShellResized= true;
            }
        });
    }
    
    /**
     * Override method to initialize the enablement of the OK button after
     * it is created.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        //do this here because setting the text will set enablement on the ok
        // button
        refreshValidState();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
    	persistDialogSize();
        dispose();
        return super.close();
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point initialLocation= DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
		if (initialLocation != null) {
			return initialLocation;
		}
		return super.getInitialLocation(initialSize);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
	}
	
	protected String getDialogSettingsSectionName() {
		return "EXPRESSION_INPUT_DIALOG"; //$NON-NLS-1$
	}
}
