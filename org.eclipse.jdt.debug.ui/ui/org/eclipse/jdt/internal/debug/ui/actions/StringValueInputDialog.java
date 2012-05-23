/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog which prompts the user to enter a new value for a
 * String variable. The user is given the option of entering the
 * literal value that they want assigned to the String or entering
 * an expression for evaluation.
 */
public class StringValueInputDialog extends ExpressionInputDialog {

	private Group fTextGroup;
	private TextViewer fTextViewer;
    private Button fTextButton;
    private Button fEvaluationButton;
    private Button fWrapText;
    
    private boolean fUseLiteralValue= true;
    private static final String USE_EVALUATION = "USE_EVALUATION"; //$NON-NLS-1$
    private static final String WRAP_TEXT = "WRAP_TEXT"; //$NON-NLS-1$

    /**
     * @param parentShell
     * @param variable
     */
    protected StringValueInputDialog(Shell parentShell, IJavaVariable variable) {
        super(parentShell, variable);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.actions.ExpressionInputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
	protected Control createDialogArea(Composite parent) {
    	Control control = super.createDialogArea(parent);
    	IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getHelpSystem().setHelp(parent, IJavaDebugHelpContextIds.STRING_VALUE_INPUT_DIALOG);
		return control;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.ui.actions.ExpressionInputDialog#createInputArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
	protected Composite createInputArea(Composite parent) {
    	Composite composite = super.createInputArea(parent);
    	createRadioButtons(parent);
    	return composite;
    }
    
    /**
     * Override superclass method to create the appropriate viewer
     * (source viewer or simple text viewer) in the input area.
     */
    @Override
	protected void populateInputArea(Composite parent) {
    	super.populateInputArea(parent);
    	createTextViewer(parent);
    	
    	// Use the stored dialog settings to determine what radio button is selected and what viewer to show.
        boolean useEvaluation= false;
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            useEvaluation= settings.getBoolean(USE_EVALUATION);
        }
        
        setTextViewerVisible(!useEvaluation);
        setSourceViewerVisible(useEvaluation);
        fUseLiteralValue = !useEvaluation;
        fEvaluationButton.setSelection(useEvaluation);
        fTextButton.setSelection(!useEvaluation);
    }

    /**
     * Creates the text viewer that allows the user to enter a new String
     * value.
     * @param parent parent composite
     */
    private void createTextViewer(Composite parent) {
    	fTextGroup = SWTFactory.createGroup(parent, ActionMessages.StringValueInputDialog_0, 1, 1, GridData.FILL_BOTH);
    	                
        fTextViewer= new TextViewer(fTextGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fTextViewer.setDocument(new Document());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint= 300;
        gridData.heightHint= 150;
        fTextViewer.getTextWidget().setLayoutData(gridData);
        try {
            String valueString = fVariable.getValue().getValueString();
            fTextViewer.getDocument().set(valueString);
            fTextViewer.setSelectedRange(0, valueString.length());
        } catch (DebugException e) {
            JDIDebugUIPlugin.log(e);
        }
        fTextViewer.getControl().setFocus();
        
        boolean wrap= true;
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            wrap= settings.getBoolean(WRAP_TEXT);
        }
        fWrapText = SWTFactory.createCheckButton(fTextGroup, ActionMessages.StringValueInputDialog_4, null, wrap, 1);
        updateWordWrap();
        fWrapText.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                updateWordWrap();
            }
        });
        
		IDocumentListener listener = new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            public void documentChanged(DocumentEvent event) {
                refreshValidState(fTextViewer);
            }
        };
		fTextViewer.getDocument().addDocumentListener(listener);        
    }
    
    private void updateWordWrap() {
        fTextViewer.getTextWidget().setWordWrap(fWrapText.getSelection());
    }

    /**
     * Creates the radio buttons that allow the user to choose between
     * simple text mode and evaluation mode.
     */
    protected void createRadioButtons(Composite parent) {
    	
    	fTextButton = SWTFactory.createRadioButton(parent, ActionMessages.StringValueInputDialog_1);
        fTextButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                handleRadioSelectionChanged();
            }
        });
        fEvaluationButton = SWTFactory.createRadioButton(parent, ActionMessages.StringValueInputDialog_2);
    }
    
    /**
     * The radio button selection has changed update the input widgetry
     * to reflect the user's selection.
     */
    private void handleRadioSelectionChanged() {
        boolean literal = fTextButton.getSelection();
        if (literal != fUseLiteralValue) {
			fUseLiteralValue= literal;
	        if (fUseLiteralValue) {
	        	setSourceViewerVisible(false);
	        	setTextViewerVisible(true);
	        } else {
	        	setTextViewerVisible(false);
	        	setSourceViewerVisible(true);
	        }
	        fInputArea.layout(true, true);
	        refreshValidState();
        }
    }
    
    /**
     * Sets the visibility of the source viewer and the exclude attribute of its layout.
     * @param value If <code>true</code>, the viewer will be visible, if <code>false</code>, the viewer will be hidden.
     */
    protected void setTextViewerVisible(boolean value){
    	if (fTextGroup != null){
    		fTextGroup.setVisible(value);
	    	GridData data = (GridData)fTextGroup.getLayoutData();
			data.exclude = !value;
    	}
    }
      
    /**
     * Updates the error message based on the user's input.
     */
    @Override
	protected void refreshValidState() {
        if (isUseLiteralValue()) {
        	refreshValidState(fTextViewer);
        } else {
        	super.refreshValidState();
        }
	}
    
    /**
     * Override superclass method to persist user's evaluation/literal mode
     * selection.
     */
    @Override
	protected void okPressed() {
        IDialogSettings settings= getDialogSettings();
        if (settings == null) {
        	settings= JDIDebugUIPlugin.getDefault().getDialogSettings().addNewSection(getDialogSettingsSectionName());
        }
        settings.put(USE_EVALUATION, fEvaluationButton.getSelection());
        if (fWrapText != null) {
        	settings.put(WRAP_TEXT, fWrapText.getSelection());
        }
        super.okPressed();
    }
    
    /**
     * Returns <code>true</code> if this dialog's result should be interpreted
     * as a literal value and <code>false</code> if the result should be interpreted
     * as an expression for evaluation.
     * 
     * @return whether or not this dialog's result is a literal value.
     */
    public boolean isUseLiteralValue() {
        return fUseLiteralValue;
    }
    
    /**
     * Override superclass method to return text from the simple text
     * viewer if appropriate.
     * @see ExpressionInputDialog#getText()
     */
    @Override
	protected String getText() {
        if (fTextButton.getSelection()) {
            return fTextViewer.getDocument().get();
        }
        return super.getText();
    }
    
    /**
     * Returns the dialog settings used for this dialog
     * @return the dialog settings used for this dialog
     */
    protected IDialogSettings getDialogSettings() {
        return JDIDebugUIPlugin.getDefault().getDialogSettings().getSection(getDialogSettingsSectionName());
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.actions.ExpressionInputDialog#getDialogSettingsSectionName()
	 */
	@Override
	protected String getDialogSettingsSectionName() {
		return "STRING_VALUE_INPUT_DIALOG"; //$NON-NLS-1$
	}
}
