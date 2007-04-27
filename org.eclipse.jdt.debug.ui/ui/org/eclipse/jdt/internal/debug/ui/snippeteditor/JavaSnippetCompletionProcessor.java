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
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

 
import java.util.Arrays;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.widgets.Shell;

/**
 * Java snippet completion processor.
 */
public class JavaSnippetCompletionProcessor implements IContentAssistProcessor {
	
	private CompletionProposalCollector fCollector;
	private JavaSnippetEditor fEditor;
	private IContextInformationValidator fValidator;
	private TemplateEngine fTemplateEngine;
	private CompletionProposalComparator fComparator;
	private String fErrorMessage;
	
	private char[] fProposalAutoActivationSet;
			
	public JavaSnippetCompletionProcessor(JavaSnippetEditor editor) {
		fEditor= editor;
		TemplateContextType contextType= JavaPlugin.getDefault().getTemplateContextRegistry().getContextType("java"); //$NON-NLS-1$
		if (contextType != null) {
			fTemplateEngine= new TemplateEngine(contextType);
		}
		
		fComparator= new CompletionProposalComparator();
	}
	
	/**
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}
	
	protected void setErrorMessage(String message) {
		if (message != null && message.length() == 0) {
			message = null;
		}
		fErrorMessage = message;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null) {
			fValidator= new JavaParameterListValidator();
		}
		return fValidator;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}
	
	/**
	 * @see IContentAssistProcessor#computeProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
		try {
			setErrorMessage(null);
			try {
				fCollector = new CompletionProposalCollector(fEditor.getJavaProject());
				fEditor.codeComplete(fCollector);
			} catch (JavaModelException x) {
				Shell shell= viewer.getTextWidget().getShell();
				ErrorDialog.openError(shell, SnippetMessages.getString("CompletionProcessor.errorTitle"), SnippetMessages.getString("CompletionProcessor.errorMessage"), x.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
				JDIDebugUIPlugin.log(x);
			}
			
			IJavaCompletionProposal[] results= fCollector.getJavaCompletionProposals();
			
			if (fTemplateEngine != null) {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, position, null);			
			
				TemplateProposal[] templateResults= fTemplateEngine.getResults();
	
				// concatenate arrays
				IJavaCompletionProposal[] total= new IJavaCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results= total;
			}
			return order(results);
		} finally {
			setErrorMessage(fCollector.getErrorMessage());
			fCollector = null;
		}
	}
	
	/**
	 * Order the given proposals.
	 */
	private ICompletionProposal[] order(IJavaCompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;	
	}	
	
	/**
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fProposalAutoActivationSet;
	}
	
	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		fProposalAutoActivationSet= activationSet;
	}
	
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
	}
}
