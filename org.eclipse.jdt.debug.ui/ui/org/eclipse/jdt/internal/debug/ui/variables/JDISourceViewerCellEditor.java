/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A {@link CellEditor} for the Expressions view that wraps a {@link JDISourceViewer} configured with Java content assist.
 */
public class JDISourceViewerCellEditor extends CellEditor {

	private JDISourceViewer fViewer;
	private String fValue = ""; //$NON-NLS-1$
	/** Tracks whether the content-assist proposal popup is currently open. */
	private boolean fProposalPopupOpen = false;

	public JDISourceViewerCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	protected Control createControl(Composite parent) {
		fViewer = new JDISourceViewer(parent, null, SWT.BORDER | SWT.SINGLE | SWT.LEFT_TO_RIGHT);

		Document document = new Document();
		JDIDebugUIPlugin.getDefault().getJavaTextTools().setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);

		fViewer.setInput(document);
		fViewer.configure(new DisplayViewerConfiguration() {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return new JavaDebugContentAssistProcessor(new CurrentFrameContext());
			}
		});
		fViewer.setEditable(true);

		ContentAssistantFacade facade = fViewer.getContentAssistantFacade();
		if (facade != null) {
			facade.addCompletionListener(new ICompletionListener() {
				@Override
				public void assistSessionStarted(org.eclipse.jface.text.contentassist.ContentAssistEvent event) {
					fProposalPopupOpen = true;
				}
				@Override
				public void assistSessionEnded(org.eclipse.jface.text.contentassist.ContentAssistEvent event) {
					fProposalPopupOpen = false;
				}
				@Override
				public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
					// not needed
				}
			});
		}

		fViewer.getTextWidget().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!fProposalPopupOpen) {
					JDISourceViewerCellEditor.this.focusLost();
				}
			}
		});

		fViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == ' ' && (e.stateMask & SWT.CTRL) != 0) {
					fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
					e.doit = false;
				} else if (e.keyCode == SWT.CR && (e.stateMask & SWT.SHIFT) == 0) {
					if (fProposalPopupOpen) {
						return;
					}
					fireApplyEditorValue();
					deactivate();
					e.doit = false;
				} else if (e.keyCode == SWT.ESC) {
					fireCancelEditor();
					e.doit = false;
				}
			}
		});

		return fViewer.getControl();
	}

	@Override
	protected Object doGetValue() {
		if (fViewer != null && fViewer.getDocument() != null) {
			return fViewer.getDocument().get();
		}
		return fValue;
	}

	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}

	@Override
	protected void doSetFocus() {
		if (fViewer != null) {
			fViewer.getTextWidget().setFocus();
		}
	}

	@Override
	protected void doSetValue(Object value) {
		fValue = (value == null) ? "" : value.toString(); //$NON-NLS-1$
		if (fViewer != null && fViewer.getDocument() != null) {
			fViewer.getDocument().set(fValue);
			// Move cursor to end
			fViewer.getTextWidget().setCaretOffset(fValue.length());
		}
	}

	@Override
	public void dispose() {
		if (fViewer != null) {
			fViewer.dispose();
		}
		super.dispose();
	}

}
