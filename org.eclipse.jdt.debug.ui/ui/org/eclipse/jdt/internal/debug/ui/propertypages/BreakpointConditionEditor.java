/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.contentassist.TypeContext;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * The widget for the conditional editor on the breakpoints properties page
 */
public class BreakpointConditionEditor {
	
	private JDISourceViewer fViewer;
	private IContentAssistProcessor fCompletionProcessor;	
	private String fOldValue;
	private String fErrorMessage;
	private JavaLineBreakpointPage fPage;
	private IJavaLineBreakpoint fBreakpoint;
	private IHandlerService fHandlerService;
	private IHandler fHandler;
	private IHandlerActivation fActivation;
    private IDocumentListener fDocumentListener;
		
	/**
	 * Constructor
	 * @param parent the parent to add this widget to
	 * @param page the page that is associated with this widget
	 */
	public BreakpointConditionEditor(Composite parent, JavaLineBreakpointPage page) {
		fPage = page;
		fBreakpoint = (IJavaLineBreakpoint) fPage.getBreakpoint();
		String condition = new String();
		try {
			condition = fBreakpoint.getCondition();
			fErrorMessage  = PropertyPageMessages.BreakpointConditionEditor_1; 
			fOldValue = ""; //$NON-NLS-1$
			
			fViewer = new JDISourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
			fViewer.setInput(parent);
			IDocument document = new Document();
			JDIDebugUIPlugin.getDefault().getJavaTextTools().setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		// we can only do code assist if there is an associated type
			IJavaDebugContentAssistContext context = null;
			IType type = BreakpointUtils.getType(fBreakpoint);
			if (type == null) {
				context = new TypeContext(null, -1);
			} 
			else {
				try {	
					String source = null;
					ICompilationUnit compilationUnit = type.getCompilationUnit();
					if (compilationUnit != null && compilationUnit.getJavaProject().getProject().exists()) {
						source = compilationUnit.getSource();
					} 
					else {
						IClassFile classFile = type.getClassFile();
						if (classFile != null) {
							source = classFile.getSource();
						}
					}
					int lineNumber = fBreakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
					int position= -1;
					if (source != null && lineNumber != -1) {
						try {
							position = new Document(source).getLineOffset(lineNumber - 1);
						} 
						catch (BadLocationException e) {JDIDebugUIPlugin.log(e);}
					}
					context = new TypeContext(type, position);
				} 
				catch (CoreException e) {JDIDebugUIPlugin.log(e);}
			}
			fCompletionProcessor = new JavaDebugContentAssistProcessor(context);
			fViewer.configure(new DisplayViewerConfiguration() {
				public IContentAssistProcessor getContentAssistantProcessor() {
						return fCompletionProcessor;
				}
			});
			fViewer.setEditable(true);
		//if we don't check upstream tracing can throw assertion exceptions see bug 181914
			document.set((condition == null ? "" : condition)); //$NON-NLS-1$
			fViewer.setDocument(document);
			fViewer.setUndoManager(new TextViewerUndoManager(10));
			fViewer.getUndoManager().connect(fViewer);
			fDocumentListener = new IDocumentListener() {
	            public void documentAboutToBeChanged(DocumentEvent event) {
	            }
	            public void documentChanged(DocumentEvent event) {
	                valueChanged();
	            }
	        };
			fViewer.getDocument().addDocumentListener(fDocumentListener);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = fPage.convertHeightInCharsToPixels(10);
			gd.widthHint = fPage.convertWidthInCharsToPixels(40);
			fViewer.getControl().setLayoutData(gd);
			fHandler = new AbstractHandler() {
				public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
					fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
					return null;
				}
			};
			fHandlerService = (IHandlerService) PlatformUI.getWorkbench().getAdapter(IHandlerService.class);
		} 
		catch (CoreException exception) {JDIDebugUIPlugin.log(exception);}
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
		if (!fViewer.isEditable()) {
			fPage.removeErrorMessage(fErrorMessage);
		} else {
			String text = fViewer.getDocument().get();
			if (!(text != null && text.trim().length() > 0)) {
				fPage.addErrorMessage(fErrorMessage);
			} else {
				fPage.removeErrorMessage(fErrorMessage);
			}
		}
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
			fActivation = fHandlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, fHandler);
		} else {
			Color color = fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			fViewer.getTextWidget().setBackground(color);
			if(fActivation != null) {
				fHandlerService.deactivateHandler(fActivation);
			}
		}
		valueChanged();
	}
	
	/**
	 * Handle that the value changed
	 */
	protected void valueChanged() {
		String newValue = fViewer.getDocument().get();
		if (!newValue.equals(fOldValue)) {
			fOldValue = newValue;
		}
		refreshValidState();
	}
	
	/**
	 * Dispose of the handlers, etc
	 */
	public void dispose() {
	    if (fViewer.isEditable()) {
	    	fHandlerService.deactivateHandler(fActivation);
	    }
	    fViewer.getDocument().removeDocumentListener(fDocumentListener);
		fViewer.dispose();
	}
}
