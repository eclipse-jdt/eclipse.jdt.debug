package org.eclipse.jdt.internal.debug.ui;
/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.display.DisplayCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class DetailFormatterCompletionProcessor extends DisplayCompletionProcessor {

	/**
	 * The dialog with which this processor is associated.
	 */
	private DetailFormatterDialog fDetailFormatDialog;
		
	public DetailFormatterCompletionProcessor(DetailFormatterDialog detailFormatDialog) {
		fDetailFormatDialog= detailFormatDialog;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {

		IType receivingType= fDetailFormatDialog.getType();
		if (receivingType == null) {
			return new ICompletionProposal[0];
		}
		
		IJavaProject project= receivingType.getJavaProject();
		try {
			// Generate selections from the compilation unit
			ITextSelection textSelection= (ITextSelection)viewer.getSelectionProvider().getSelection();			
			configureResultCollector(project, textSelection);	
			receivingType.codeComplete(viewer.getDocument().get().toCharArray(), -1, documentOffset,
				 new char[0][], new char[0][],
				 new int[0], false, getCollector());
				 
			IJavaCompletionProposal[] results= getCollector().getResults();
			
			// Generate selections from the template engine
			TemplateEngine templateEngine= getTemplateEngine();
			if (templateEngine != null) {
				try {
					templateEngine.reset();
					templateEngine.complete(viewer, documentOffset, null);
					IJavaCompletionProposal[] templateResults= templateEngine.getResults();

					// concatenate arrays
					IJavaCompletionProposal[] total= new IJavaCompletionProposal[results.length + templateResults.length];
					System.arraycopy(templateResults, 0, total, 0, templateResults.length);
					System.arraycopy(results, 0, total, templateResults.length, results.length);
					results= total;
				} catch (JavaModelException x) {
					JDIDebugUIPlugin.log(x);
				}					
			}	 
			 //Order here and not in result collector to make sure that the order
			 //applies to all proposals and not just those of the compilation unit. 
			return order(results);	
		} catch (JavaModelException x) {
			handle(viewer, x);
		}
		return null;
	}
}
