/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.java.ResultCollector;
import org.eclipse.jdt.internal.ui.text.template.TemplateContext;
import org.eclipse.jdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.widgets.Shell;

/**
 * Java snippet completion processor.
 */
public class JavaSnippetCompletionProcessor implements IContentAssistProcessor {
	
	private ResultCollector fCollector;
	private JavaSnippetEditor fEditor;
	private IContextInformationValidator fValidator;
	private TemplateEngine fTemplateEngine;
	private Comparator fComparator;
	
	private char[] fProposalAutoActivationSet;
	
	private static class CompletionProposalComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ICompletionProposal c1= (ICompletionProposal) o1;
			ICompletionProposal c2= (ICompletionProposal) o2;
			return c1.getDisplayString().compareTo(c2.getDisplayString());
		}
	};
		
	public JavaSnippetCompletionProcessor(JavaSnippetEditor editor) {
		fCollector= new JavaSnippetResultCollector();
		fEditor= editor;
		fTemplateEngine= new TemplateEngine(TemplateContext.JAVA);
	}
	
	/**
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fCollector.getErrorMessage();
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
			fCollector.reset(position, fEditor.findJavaProject(), null);
			fEditor.codeComplete(fCollector);
		} catch (JavaModelException x) {
			Shell shell= viewer.getTextWidget().getShell();
			ErrorDialog.openError(shell, SnippetMessages.getString("CompletionProcessor.errorTitle"), SnippetMessages.getString("CompletionProcessor.errorMessage"), x.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
		}
		
		ICompletionProposal[] results= fCollector.getResults();
		
		try {
			fTemplateEngine.reset();
			fTemplateEngine.complete(viewer, position, null);
		} catch (JavaModelException x) {
			Shell shell= viewer.getTextWidget().getShell();
			ErrorDialog.openError(shell, SnippetMessages.getString("CompletionProcessor.errorTitle"), SnippetMessages.getString("CompletionProcessor.errorMessage"), x.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
		}				
		
		ICompletionProposal[] templateResults= fTemplateEngine.getResults();

		// concatenate arrays
		ICompletionProposal[] total= new ICompletionProposal[results.length + templateResults.length];
		System.arraycopy(templateResults, 0, total, 0, templateResults.length);
		System.arraycopy(results, 0, total, templateResults.length, results.length);
		
		return order(total);
	}
	
	/**
	 * Order the given proposals.
	 */
	private ICompletionProposal[] order(ICompletionProposal[] proposals) {
		if (fComparator != null)
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
		fComparator= order ? new CompletionProposalComparator() : null;
	}
}
