/*******************************************************************************
 *  Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A dialog which prompts the user to enter an expression for
 * evaluation.
 */
public class ExpressionInputDialog extends TrayDialog {

    protected IJavaVariable fVariable;
    protected String fResult= null;

    // Input area composite which acts as a placeholder for
    // input widgetry that is created/disposed dynamically.
    protected Composite fInputArea;
    // Source viewer widgets
    protected Composite fSourceViewerComposite;
    protected JDISourceViewer fSourceViewer;
    protected IContentAssistProcessor fCompletionProcessor;
    protected IDocumentListener fDocumentListener;
    protected IHandlerService fService;
    protected IHandlerActivation fActivation;
//    protected HandlerSubmission fSubmission;
    // Text for error reporting
    protected Text fErrorText;

    /**
     * @param parentShell the shell to create the dialog in
     * @param variable the variable being edited
     */
    protected ExpressionInputDialog(Shell parentShell, IJavaVariable variable) {
        super(parentShell);
        setShellStyle(SWT.CLOSE|SWT.MIN|SWT.MAX|SWT.RESIZE);
        fVariable= variable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
	protected Control createDialogArea(Composite parent) {
    	IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getHelpSystem().setHelp(
				parent,
				IJavaDebugHelpContextIds.EXPRESSION_INPUT_DIALOG);

        Composite composite= (Composite) super.createDialogArea(parent);

        // Create the composite which will hold the input widgetry
        fInputArea = createInputArea(composite);
        // Create the error reporting text area
        fErrorText = createErrorText(composite);
        // Create the source viewer after creating the error text so that any
        // necessary error messages can be set.
        populateInputArea(fInputArea);
        return composite;
    }

    /**
     * Returns the text widget for reporting errors
     * @param parent parent composite
     * @return the error text widget
     */
    protected Text createErrorText(Composite parent) {
        Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, ""); //$NON-NLS-1$
        text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        return text;
    }

    /**
	 * Returns the composite that will be used to contain the input widgetry.
	 *
	 * @param parent
	 *            the parent composite
	 * @return the composite that will contain the input widgets
	 */
    protected Composite createInputArea(Composite parent) {
    	Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
    	Dialog.applyDialogFont(composite);
    	return composite;
    }

    /**
     * Creates the appropriate widgetry in the input area. This
     * method is intended to be overridden by subclasses who wish
     * to use alternate input widgets.
     * @param parent parent composite
     */
    protected void populateInputArea(Composite parent) {
    	fSourceViewerComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);

    	String name= ActionMessages.ExpressionInputDialog_3;
        try {
            name= fVariable.getName();
        } catch (DebugException e) {
            JDIDebugUIPlugin.log(e);
        }

		SWTFactory.createWrapLabel(fSourceViewerComposite, NLS.bind(ActionMessages.ExpressionInputDialog_0, name), 1, convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH));

        fSourceViewer= new JDISourceViewer(fSourceViewerComposite, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fSourceViewer.setInput(fSourceViewerComposite);
        configureSourceViewer();
        fSourceViewer.doOperation(ITextOperationTarget.SELECT_ALL);
    }

    /**
     * Sets the visibility of the source viewer and the exclude attribute of its layout.
     * @param value If <code>true</code>, the viewer will be visible, if <code>false</code>, the viewer will be hidden.
     */
    protected void setSourceViewerVisible(boolean value) {
    	if (fSourceViewerComposite != null){
    		fSourceViewerComposite.setVisible(value);
    		GridData data = (GridData)fSourceViewerComposite.getLayoutData();
    		data.exclude = !value;
    		if (value){
    			fSourceViewer.getDocument().addDocumentListener(fDocumentListener);
    			activateHandler();
    		} else if (fActivation != null) {
    			fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
	    		fService.deactivateHandler(fActivation);
    		}
    	}
    }

    /**
     * Initializes the source viewer. This method is based on code in BreakpointConditionEditor.
     */
    private void configureSourceViewer() {
        JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
        IDocument document= new Document();
        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		fSourceViewer.configure(new DisplayViewerConfiguration() {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return getCompletionProcessor();
			}
		});
		fSourceViewer.setEditable(true);
		fSourceViewer.setDocument(document);
		final IUndoManager undoManager= new TextViewerUndoManager(10);
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
            @Override
			public void documentAboutToBeChanged(DocumentEvent event) {
            }
            @Override
			public void documentChanged(DocumentEvent event) {
                refreshValidState(fSourceViewer);
            }
        };
		fSourceViewer.getDocument().addDocumentListener(fDocumentListener);

		activateHandler();
    }

    /**
     * Activates the content assist handler.
     */
    private void activateHandler(){
    	IHandler handler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
				fSourceViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				return null;
			}
		};
		IWorkbench workbench = PlatformUI.getWorkbench();
		fService = workbench.getAdapter(IHandlerService.class);
		fActivation = fService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);
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
                    StringBuilder buffer= new StringBuilder(currentValue.length());
                    buffer.append('"'); // Surround value in quotes
                    char[] chars = currentValue.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        char c = chars[i];
                        switch (c) {
                        	case '\b':
                        		buffer.append("\\b"); //$NON-NLS-1$
                        		break;
                        	case '\t':
                        		buffer.append("\\t"); //$NON-NLS-1$
                        		break;
                        	case '\n':
                        		buffer.append("\\n"); //$NON-NLS-1$
                        		break;
                        	case '\f':
                        		buffer.append("\\f"); //$NON-NLS-1$
                        		break;
                        	case '\r':
                        		buffer.append("\\r"); //$NON-NLS-1$
                        		break;
                        	case '"':
                        		buffer.append("\\\""); //$NON-NLS-1$
                        		break;
                        	case '\'':
                        		buffer.append("\\\'"); //$NON-NLS-1$
                        		break;
                        	case '\\':
                        		buffer.append("\\\\"); //$NON-NLS-1$
                        		break;
                        	default:
                        		buffer.append(c);
                        		break;
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
	protected IContentAssistProcessor getCompletionProcessor() {
		if (fCompletionProcessor == null) {
			fCompletionProcessor= new JavaDebugContentAssistProcessor(new CurrentFrameContext());
		}
		return fCompletionProcessor;
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
	 */
	protected void refreshValidState(TextViewer viewer) {
	    String errorMessage= null;
	    if (viewer != null) {
			String text= viewer.getDocument().get();
			boolean valid= text != null && text.trim().length() > 0;
			if (!valid) {
				errorMessage= ActionMessages.ExpressionInputDialog_1;
			}
	    }
		setErrorMessage(errorMessage);
	}

	protected void refreshValidState() {
		refreshValidState(fSourceViewer);
	}

	/**
	 * Sets the error message to display to the user. <code>null</code>
	 * is the same as the empty string.
	 * @param message the error message to display to the user or
	 *  <code>null</code> if the error message should be cleared
	 */
	protected void setErrorMessage(String message) {
	    if (message == null) {
			message = Util.ZERO_LENGTH_STRING;
	    }
	    fErrorText.setText(message);
	    getButton(IDialogConstants.OK_ID).setEnabled(message.length() == 0);
	}

	/**
	 * Persist the dialog size and store the user's input on OK is pressed.
	 */
    @Override
	protected void okPressed() {
        fResult= getText();
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
	 * Returns the text entered by the user or <code>null</code> if the user cancelled.
	 *
	 * @return the text entered by the user or <code>null</code> if the user cancelled
	 */
    public String getResult() {
        return fResult;
    }

    /**
     * Initializes the dialog shell with a title.
     */
    @Override
	protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(ActionMessages.ExpressionInputDialog_2);
    }

    /**
     * Override method to initialize the enablement of the OK button after
     * it is created.
     */
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        //do this here because setting the text will set enablement on the ok
        // button
        refreshValidState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    @Override
	public boolean close() {
		if (fActivation != null) {
			fService.deactivateHandler(fActivation);
		}
		if (fSourceViewer != null) {
			fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
			fSourceViewer.dispose();
			fSourceViewer = null;
		}
		if (fSourceViewerComposite != null) {
			fSourceViewerComposite.dispose();
			fSourceViewerComposite = null;
		}
		fDocumentListener = null;
		fCompletionProcessor = null;
        return super.close();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
	protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         }
         return section;
    }

	/**
	 * @return the name to use to save the dialog settings
	 */
	protected String getDialogSettingsSectionName() {
		return "EXPRESSION_INPUT_DIALOG"; //$NON-NLS-1$
	}
}
