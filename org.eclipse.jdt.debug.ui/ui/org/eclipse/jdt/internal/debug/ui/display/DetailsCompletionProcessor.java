package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
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
 
public class DetailsCompletionProcessor extends DisplayCompletionProcessor {

	/**
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context == null) {
			return new ICompletionProposal[0];
		}
		IJavaStackFrame stackFrame= (IJavaStackFrame)context.getAdapter(IJavaStackFrame.class);
		if (stackFrame == null) {
			return new ICompletionProposal[0];
		}
		
		IDebugView view= (IDebugView)JDIDebugUIPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
		ISelection selection= view.getViewer().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return super.computeCompletionProposals(stackFrame, viewer, documentOffset);
		}
		IStructuredSelection viewerSelection= (IStructuredSelection)selection;
		if (viewerSelection.size() > 1) {
			return new ICompletionProposal[0];
		}
		Object element= viewerSelection.getFirstElement();	
		IJavaProject project= getJavaProject(stackFrame);
		if (project != null) {
			try {
				ITextSelection textSelection= (ITextSelection)viewer.getSelectionProvider().getSelection();			
				IType receivingType= getReceivingType(project, element);
					
				if (receivingType == null) {
					return new ICompletionProposal[0];
				}
		
				configureResultCollector(project, textSelection);	
				int insertionPosition= computeInsertionPosition(receivingType, stackFrame);
				receivingType.codeComplete(viewer.getDocument().get().toCharArray(), insertionPosition, documentOffset,
					 new char[0][], new char[0][],
					 new int[0], stackFrame.isStatic(), getCollector());
					 
				 //Order here and not in result collector to make sure that the order
				 //applies to all proposals and not just those of the compilation unit. 
				return order(getCollector().getResults());	
			} catch (JavaModelException x) {
				handle(viewer, x);
			} catch (DebugException de) {
				handle(viewer, de);
			}
		}
		return null;
	}
	
	private IType getReceivingType(IJavaProject project, Object element) throws DebugException {
		String originalTypeName= getReceivingTypeName(element);
		if (originalTypeName == null) {
			return null;
		}
		String typeName= originalTypeName;
		int dollarIndex= typeName.indexOf('$');
		if (dollarIndex >= 0) {
			typeName= typeName.substring(0, dollarIndex);
		}
		int index = typeName.lastIndexOf('.');
		if (index >= 0) {
			typeName = typeName.replace('.', IPath.SEPARATOR);
		} 
		typeName+=".java"; //$NON-NLS-1$
		
		return getType(project, originalTypeName, typeName);
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