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
				ITextSelection textSelection= (ITextSelection)viewer.getSelectionProvider().getSelection();			
				IType receivingType= getReceivingType(stackFrame.getLaunch(), element);
				if (receivingType == null) {
                    setErrorMessage(DisplayMessages.DetailsCompletionProcessor_2); //$NON-NLS-1$
					return new ICompletionProposal[0];
				}
				IJavaProject project = receivingType.getJavaProject(); 
		
				configureResultCollector(project, textSelection);	
				int insertionPosition= computeInsertionPosition(receivingType, stackFrame);
				receivingType.codeComplete(viewer.getDocument().get().toCharArray(), insertionPosition, documentOffset,
					 new char[0][], new char[0][],
					 new int[0], false, getCollector());
					 
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
	
	private IType getReceivingType(ILaunch launch, Object element) throws DebugException {
		String originalTypeName= getReceivingTypeName(element);
		if (originalTypeName == null) {
			return null;
		}
		
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

	private String getReceivingTypeName(Object element) {
		
		IValue value= null;
		try {
			if (element instanceof IVariable) {
				value= ((IVariable)element).getValue();
				if (value instanceof IJavaArray) {
					return null;
				}
			} else if (element instanceof IExpression) {
				value= ((IExpression)element).getValue();	
			}
			if (value != null) {
				return value.getReferenceTypeName();
			}
		} catch (DebugException de) {
			JDIDebugUIPlugin.log(de);
		}
				
		return null;
	}
}
