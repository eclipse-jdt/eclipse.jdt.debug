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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointConditionCompletionProcessor;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */
public class BreakpointConditionEditor {
	
	private SourceViewer fViewer;
	private BreakpointConditionCompletionProcessor fCompletionProcessor;
		
	private boolean fIsValid;
		
	private String fOldValue;
	private String fErrorMessage;
	
	private JavaLineBreakpointPage fPage;
	private IJavaLineBreakpoint fBreakpoint;
		
	public BreakpointConditionEditor(Composite parent, String labelText, JavaLineBreakpointPage page) {
		
		fPage= page;
		fBreakpoint= (IJavaLineBreakpoint) fPage.getBreakpoint();
		String condition;
		try {
			condition= fBreakpoint.getCondition();
		} catch (CoreException exception) {
			JDIDebugUIPlugin.log(exception);
			return;
		}
		fErrorMessage= PropertyPageMessages.getString("BreakpointConditionEditor.1"); //$NON-NLS-1$
		fOldValue= ""; //$NON-NLS-1$
			
		// the source viewer
		fViewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
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
		
		// listener for activate the code assist
		fViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
					if (event.character == 0x20) { //do code assist for CTRL-SPACE
						fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
						event.doit= false;
					} else if (event.character == 0x1A) { // undo for CTRL-z
						undoManager.undo();
						event.doit= false;
					}
				}
			}
		});

		// listener for check the value
		fViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				valueChanged();
			}
		});
		// we can only do code assist if there is an associated type
		IType type = null;
		try {
			type= BreakpointUtils.getType(fBreakpoint);
		} catch (CoreException e) {
		}
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
	}

	/**
	 * Returns the condition defined in the source viewer.
	 * @return the contents of this condition editor
	 */
	public String getCondition() {
		return fViewer.getDocument().get();
	}

	/**
	 * @see FieldEditor#refreshValidState()
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
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData)fViewer.getControl().getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}
		
	/**
	 * Return the completion processor associated with this viewer.
	 * @return BreakPointConditionCompletionProcessor
	 */
	private BreakpointConditionCompletionProcessor getCompletionProcessor() {
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
		Color color= null;
		if (enabled) {
			color= fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
			fViewer.getTextWidget().setFocus();
		} else {
			color= fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		}
		fViewer.getTextWidget().setBackground(color);
		valueChanged();
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditor#isValid()
	 */
	public boolean isValid() {
		return fIsValid;
	}
	
	public void valueChanged() {
		refreshValidState();
				
		String newValue = fViewer.getDocument().get();
		if (!newValue.equals(fOldValue)) {
			fOldValue = newValue;
		}
	}
}
