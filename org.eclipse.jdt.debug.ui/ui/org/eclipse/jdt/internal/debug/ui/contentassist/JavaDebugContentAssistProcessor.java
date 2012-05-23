/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Completion processor for the Java debugger. This completion processor
 * operates on a client provided context.
 * 
 * @since 3.2
 */
public class JavaDebugContentAssistProcessor implements IContentAssistProcessor {
		
	private JavaDebugCompletionProposalCollector fCollector;
	private IContextInformationValidator fValidator;
	private TemplateEngine fJavaEngine;
	private TemplateEngine fStatementEngine;
    private String fErrorMessage = null;
	
	private char[] fProposalAutoActivationSet;
	private CompletionProposalComparator fComparator;
	private IJavaDebugContentAssistContext fContext;
		
	public JavaDebugContentAssistProcessor(IJavaDebugContentAssistContext context) {
		fContext = context;
		TemplateContextType contextType= JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.ID_ALL);
		if (contextType != null) {
			fJavaEngine= new TemplateEngine(contextType);
		}
		contextType = JavaPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.ID_STATEMENTS);
		if (contextType != null) {
			fStatementEngine= new TemplateEngine(contextType);
		}
		
		fComparator= new CompletionProposalComparator();
	}
	
	/**
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
        if (fErrorMessage != null) {
            return fErrorMessage;
        }
        if (fCollector != null) {
            return fCollector.getErrorMessage();
        }
        return null;
	}
    
    /**
     * Sets the error message for why completions could not be resolved.
     * Clients should clear this before computing completions.
     * 
     * @param string message
     */
    private void setErrorMessage(String string) {
    	if (string != null && string.length() == 0) {
			string = null;
		}
        fErrorMessage = string;
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
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        setErrorMessage(null);
		try {
			IType type = fContext.getType();
			IJavaProject project = type.getJavaProject();
				
			String[][] locals = fContext.getLocalVariables();
			int numLocals = 0;
			if (locals.length > 0) {
				numLocals = locals[0].length;
			}
			char[][] localVariableNames = new char[numLocals][];
			char[][] localVariableTypeNames = new char[numLocals][];
			for (int i = 0; i < numLocals; i++) {			
				localVariableNames[i] = locals[0][i].toCharArray();
				localVariableTypeNames[i] = locals[1][i].toCharArray();
			}
			
			ITextSelection selection= (ITextSelection)viewer.getSelectionProvider().getSelection();
			configureResultCollector(project, selection);	
			
			int[] localModifiers= new int[localVariableNames.length];
			Arrays.fill(localModifiers, 0);
			
			String snippet = viewer.getDocument().get();
			char[] charSnippet = fContext.getSnippet(snippet).toCharArray();
			type.codeComplete(charSnippet, fContext.getInsertionPosition(), documentOffset,
				 localVariableTypeNames, localVariableNames,
				 localModifiers, fContext.isStatic(), fCollector);
			
			List<IJavaCompletionProposal> total = new ArrayList<IJavaCompletionProposal>();
			total.addAll(Arrays.asList(fCollector.getJavaCompletionProposals()));
			
			if (fJavaEngine != null) {
				fJavaEngine.reset();
				fJavaEngine.complete(viewer, documentOffset, null);
				total.addAll(Arrays.asList(fJavaEngine.getResults()));
			}
			
			if (fStatementEngine != null) {
				fStatementEngine.reset();
				fStatementEngine.complete(viewer, documentOffset, null);
				total.addAll(Arrays.asList(fStatementEngine.getResults()));
			}
		
			 //Order here and not in result collector to make sure that the order
			 //applies to all proposals and not just those of the compilation unit. 
			return order(total.toArray(new IJavaCompletionProposal[total.size()]));	
		} catch (CoreException x) {
			setErrorMessage(x.getStatus().getMessage());
		} finally {
			releaseCollector();
		}
		
		return null;
	}
	
	/**
	 * Order the given proposals.
	 */
	private IJavaCompletionProposal[] order(IJavaCompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;	
	}	
	
	/**
	 * Configures the display result collection for the current code assist session
	 */
	private void configureResultCollector(IJavaProject project, ITextSelection selection) {
		fCollector = new JavaDebugCompletionProposalCollector(project);
		if (selection.getLength() != 0) {
			fCollector.setReplacementLength(selection.getLength());
		} 
	}
	
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
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
	 * Clears reference to result proposal collector.
	 */
	private void releaseCollector() {
		if (fCollector != null && fCollector.getErrorMessage().length() > 0 && fErrorMessage != null) {
			setErrorMessage(fCollector.getErrorMessage());
		}		
		fCollector = null;
	}
	
}
