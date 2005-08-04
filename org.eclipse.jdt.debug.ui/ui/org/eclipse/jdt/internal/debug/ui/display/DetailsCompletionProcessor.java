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
package org.eclipse.jdt.internal.debug.ui.display;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ArrayRuntimeContext;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
 
public class DetailsCompletionProcessor extends DisplayCompletionProcessor {

	/**
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		try {
	        setErrorMessage(DisplayMessages.DetailsCompletionProcessor_0); //$NON-NLS-1$
			IAdaptable context = DebugUITools.getDebugContext();
			if (context == null) {
				return new ICompletionProposal[0];
			}
			IJavaStackFrame stackFrame= (IJavaStackFrame)context.getAdapter(IJavaStackFrame.class);
			if (stackFrame == null) {
				return new ICompletionProposal[0];
			}
			
	        setErrorMessage(DisplayMessages.DetailsCompletionProcessor_1); //$NON-NLS-1$
			IWorkbenchWindow window= JDIDebugUIPlugin.getActiveWorkbenchWindow();
			if (window == null) {
				return new ICompletionProposal[0];
			}
			IWorkbenchPage page= window.getActivePage();
			if (page == null) {
				return new ICompletionProposal[0];
			}
			IDebugView view= (IDebugView)page.getActivePart();
			if (view == null) {
				return new ICompletionProposal[0];
			}
			ISelection selection= view.getViewer().getSelection();
			if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
				return super.computeCompletionProposals(stackFrame, viewer, documentOffset);
			}
			
			IStructuredSelection viewerSelection= (IStructuredSelection)selection;
			if (viewerSelection.size() > 1) {
				return new ICompletionProposal[0];
			}
			Object element= viewerSelection.getFirstElement();	
			try {
                setErrorMessage(null);
        		IValue value= null;
    			if (element instanceof IVariable) {
    				value= ((IVariable)element).getValue();
    			} else if (element instanceof IExpression) {
    				value= ((IExpression)element).getValue();	
    			}
    			String recTypeName = null;
    			if (value instanceof IJavaArray) {
    				recTypeName = "java.lang.Object"; //$NON-NLS-1$
    			} else if (value != null) {
    				recTypeName = value.getReferenceTypeName();
    			}
							
				IType receivingType= getReceivingType(stackFrame.getLaunch(), recTypeName);
				if (receivingType == null) {
                    setErrorMessage(DisplayMessages.DetailsCompletionProcessor_2); //$NON-NLS-1$
					return new ICompletionProposal[0];
				}
				IJavaProject project = receivingType.getJavaProject(); 
				ITextSelection textSelection= (ITextSelection)viewer.getSelectionProvider().getSelection();
				configureResultCollector(project, textSelection);	
				int insertionPosition= computeInsertionPosition(receivingType, stackFrame);
				char[][] localTypeNames;
				char[][] localNames;
				int[] modifiers;
				char[] snippet;
				if (value instanceof IJavaArray) {
					// do a song and dance to fake 'this' as an array receiver
					IJavaArray array = (IJavaArray) value;
					localTypeNames = new char[][]{array.getJavaType().getName().toCharArray()};
					localNames = new char[][]{ArrayRuntimeContext.ARRAY_THIS_VARIABLE.toCharArray()};
					modifiers = new int[]{0};
					snippet = ASTEvaluationEngine.replaceThisReferences(viewer.getDocument().get()).toCharArray();
				} else {
					localTypeNames = new char[0][];
					localNames = new char[0][];
					modifiers = new int[0];
					snippet = viewer.getDocument().get().toCharArray();
				}
				receivingType.codeComplete(snippet, insertionPosition, documentOffset,
					 localTypeNames, localNames, modifiers, false, getCollector());
					 
				 //Order here and not in result collector to make sure that the order
				 //applies to all proposals and not just those of the compilation unit. 
				return order(getCollector().getJavaCompletionProposals());	
			} catch (JavaModelException x) {
				handle(viewer, x);
			} catch (DebugException de) {
				handle(viewer, de);
			}
			return new ICompletionProposal[0];
		} finally {
			releaseCollector();
		}
	}
	
	private IType getReceivingType(ILaunch launch, String originalTypeName) throws DebugException {
		String sourceName= originalTypeName;
		// strip off generic info
		int genIndex = sourceName.indexOf('<');
		if (genIndex >= 0) {
			sourceName = sourceName.substring(0, genIndex);
		}
		int dollarIndex= sourceName.indexOf('$');
		if (dollarIndex >= 0) {
			sourceName= sourceName.substring(0, dollarIndex);
		}
		int index = sourceName.lastIndexOf('.');
		if (index >= 0) {
			sourceName = sourceName.replace('.', IPath.SEPARATOR);
		} 
		sourceName+=".java"; //$NON-NLS-1$
		
		return resolveType(launch, originalTypeName, sourceName);
	}

}
