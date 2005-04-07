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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointConditionCompletionProcessor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class BreakpointConditionEditor {
	
	private JDISourceViewer fViewer;
	private BreakpointConditionCompletionProcessor fCompletionProcessor;
		
	private boolean fIsValid;
		
	private String fOldValue;
	private String fErrorMessage;
	
	private JavaLineBreakpointPage fPage;
	private IJavaLineBreakpoint fBreakpoint;
	
	private HandlerSubmission submission;
    private IDocumentListener fDocumentListener;
		
	public BreakpointConditionEditor(Composite parent, JavaLineBreakpointPage page) {
		fPage= page;
		fBreakpoint= (IJavaLineBreakpoint) fPage.getBreakpoint();
		String condition;
		try {
			condition= fBreakpoint.getCondition();
		} catch (CoreException exception) {
			JDIDebugUIPlugin.log(exception);
			return;
		}
		fErrorMessage= PropertyPageMessages.BreakpointConditionEditor_1; //$NON-NLS-1$
		fOldValue= ""; //$NON-NLS-1$
			
		// the source viewer
		fViewer= new JDISourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fViewer.setInput(parent);
		
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
		
		// listener for check the value
		fDocumentListener= new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            public void documentChanged(DocumentEvent event) {
                valueChanged();
            }
        };
		fViewer.getDocument().addDocumentListener(fDocumentListener);
		
		// we can only do code assist if there is an associated type
		IType type= BreakpointUtils.getType(fBreakpoint);
		if (type != null) {
			try {
				getCompletionProcessor().setType(type);			
				String source= null;
				ICompilationUnit compilationUnit= type.getCompilationUnit();
				if (compilationUnit != null) {
					source= compilationUnit.getSource();
				} else {
					IClassFile classFile= type.getClassFile();
					if (classFile != null) {
						source= classFile.getSource();
					}
				}
				int lineNumber= fBreakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
				int position= -1;
				if (source != null && lineNumber != -1) {
					try {
						position= new Document(source).getLineOffset(lineNumber - 1);
					} catch (BadLocationException e) {
					}
				}
				getCompletionProcessor().setPosition(position);
			} catch (CoreException e) {
			}
		}
			
		gd= (GridData)fViewer.getControl().getLayoutData();
		gd.heightHint= fPage.convertHeightInCharsToPixels(10);
		gd.widthHint= fPage.convertWidthInCharsToPixels(40);	
		document.set(condition);
		valueChanged();
		
		IHandler handler = new AbstractHandler() {
		    public Object execute(Map parameter) throws ExecutionException {
		        fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		        return null;
			}
		};
		submission = new HandlerSubmission(null, parent.getShell(), null, ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM); //$NON-NLS-1$	
	}

	/**
	 * Returns the condition defined in the source viewer.
	 * @return the contents of this condition editor
	 */
	public String getCondition() {
		return fViewer.getDocument().get();
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
	 */
	protected void refreshValidState() {
		// the value is valid if the field is not editable, or if the value is not empty
		if (!fViewer.isEditable()) {
			fPage.removeErrorMessage(fErrorMessage);
			fIsValid= true;
		} else {
			String text= fViewer.getDocument().get();
			fIsValid= text != null && text.trim().length() > 0;
			if (!fIsValid) {
				fPage.addErrorMessage(fErrorMessage);
			} else {
				fPage.removeErrorMessage(fErrorMessage);
			}
		}
	}
		
	/**
	 * Return the completion processor associated with this viewer.
	 * @return BreakPointConditionCompletionProcessor
	 */
	protected BreakpointConditionCompletionProcessor getCompletionProcessor() {
		if (fCompletionProcessor == null) {
			fCompletionProcessor= new BreakpointConditionCompletionProcessor(null);
		}
		return fCompletionProcessor;
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
	 */
	public void setEnabled(boolean enabled) {
	    fViewer.setEditable(enabled);
	    fViewer.getTextWidget().setEnabled(enabled);
		if (enabled) {
			fViewer.updateViewerColors();
			fViewer.getTextWidget().setFocus();
			
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
			commandSupport.addHandlerSubmission(submission);
		} else {
			Color color= fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			fViewer.getTextWidget().setBackground(color);
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
			commandSupport.removeHandlerSubmission(submission);
		}
		valueChanged();
	}
	
	protected void valueChanged() {
		refreshValidState();
				
		String newValue = fViewer.getDocument().get();
		if (!newValue.equals(fOldValue)) {
			fOldValue = newValue;
		}
	}
	
	public void dispose() {
	    if (fViewer.isEditable()) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
			commandSupport.removeHandlerSubmission(submission);
	    }
	    fViewer.getDocument().removeDocumentListener(fDocumentListener);
		fViewer.dispose();
	}
}
