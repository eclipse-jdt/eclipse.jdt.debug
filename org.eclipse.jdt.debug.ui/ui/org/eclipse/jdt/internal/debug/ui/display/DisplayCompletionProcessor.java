package org.eclipse.jdt.internal.debug.ui.display;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.java.ResultCollector;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.widgets.Shell;

/**
 * Display snippet completion processor.
 */
public class DisplayCompletionProcessor implements IContentAssistProcessor {
		
	private ResultCollector fCollector;
	private IContextInformationValidator fValidator;
	
	private char[] fProposalAutoActivationSet;
	private Comparator fComparator;
	
	private static class CompletionProposalComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ICompletionProposal c1= (ICompletionProposal) o1;
			ICompletionProposal c2= (ICompletionProposal) o2;
			return c1.getDisplayString().compareTo(c2.getDisplayString());
		}
	};
	
	public DisplayCompletionProcessor() {
		fCollector= new ResultCollector();
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
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context == null) {
			return new ICompletionProposal[0];
		}
		
		IJavaStackFrame stackFrame= (IJavaStackFrame)context.getAdapter(IJavaStackFrame.class);
		if (stackFrame == null) {
			return new ICompletionProposal[0];
		}
			
		return computeCompletionProposals(stackFrame, viewer, documentOffset);
	}

	protected ICompletionProposal[] computeCompletionProposals(IJavaStackFrame stackFrame, ITextViewer viewer, int documentOffset) {
		try {
			IJavaProject project= getJavaProject(stackFrame);
			if (project != null) {
				IType receivingType= getReceivingType(project, stackFrame);
				
				if (receivingType == null) {
					return new ICompletionProposal[0];
				}
				IVariable[] variables= stackFrame.getLocalVariables();
				char[][] localVariableNames= new char[variables.length][];
				char[][] localVariableTypeNames= new char[variables.length][];
				resolveLocalVariables(variables, localVariableNames, localVariableTypeNames);
				
				ITextSelection selection= (ITextSelection)viewer.getSelectionProvider().getSelection();
				configureResultCollector(project, selection);	
				
				int[] localModifiers= new int[localVariableNames.length];
				Arrays.fill(localModifiers, 0);
				receivingType.codeComplete(viewer.getDocument().get().toCharArray(), -1, documentOffset,
					 localVariableTypeNames, localVariableNames,
					 localModifiers, stackFrame.isStatic(), fCollector);
					 
				 //Order here and not in result collector to make sure that the order
				 //applies to all proposals and not just those of the compilation unit. 
				return order(getCollector().getResults());	
			}
		} catch (JavaModelException x) {
			handle(viewer, x);
		} catch (DebugException de) {
			handle(viewer, de);
		}
		
		return null;
	}
	protected void handle(ITextViewer viewer, CoreException x) {
		Shell shell= viewer.getTextWidget().getShell();
		ErrorDialog.openError(shell,
			DisplayMessages.getString("DisplayCompletionProcessor.Problems_during_completion_1"), //$NON-NLS-1$
			DisplayMessages.getString("DisplayCompletionProcessor.An_exception_occurred_during_code_completion_2"), //$NON-NLS-1$ 
			x.getStatus());  
		JDIDebugUIPlugin.log(x);
	}
	
	protected void resolveLocalVariables(IVariable[] variables, char[][] localVariableNames, char[][] localVariableTypeNames) throws DebugException {
		for (int i = 0; i < variables.length; i++) {
			IVariable variable = variables[i];
			localVariableNames[i]= variable.getName().toCharArray();
			localVariableTypeNames[i]= getTranslatedTypeName(variable.getReferenceTypeName()).toCharArray();
		}
	}
	
	/**
	 * Returns the Java project associated with the given stack
	 * frame, or <code>null</code> if none.
	 */
	protected IJavaProject getJavaProject(IStackFrame stackFrame) {
		
		// Get the corresponding element.
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null)
			return null;
		
		Object sourceElement = locator.getSourceElement(stackFrame);
		if (sourceElement instanceof IJavaElement) {
			return ((IJavaElement) sourceElement).getJavaProject();
		}			
		return null;
	}	
	
	/**
	 * Order the given proposals.
	 */
	protected ICompletionProposal[] order(ICompletionProposal[] proposals) {
		if (fComparator != null)
			Arrays.sort(proposals, fComparator);
		return proposals;	
	}	
	
	/**
	 * Configures the display result collection for the current code assist session
	 */
	protected void configureResultCollector(IJavaProject project, ITextSelection selection) {
		fCollector.reset(selection.getOffset(), project, null);
		if (selection.getLength() != 0) {
			fCollector.setReplacementLength(selection.getLength());
		} 
	}
	
	/**
	 * Returns an array of simple type names that are
	 * part of the given type's qualified name. For
	 * example, if the given name is <code>x.y.A$B</code>,
	 * an array with <code>["A", "B"]</code> is returned.
	 * 
	 * @param typeName fully qualified type name
	 * @return array of nested type names
	 */
	protected String[] getNestedTypeNames(String typeName) {
		int index = typeName.lastIndexOf('.');
		if (index >= 0) {
			typeName= typeName.substring(index + 1);
		}
		index = typeName.indexOf('$');
		List list = new ArrayList(1);
		while (index >= 0) {
			list.add(typeName.substring(0, index));
			typeName = typeName.substring(index + 1);
			index = typeName.indexOf('$');
		}
		list.add(typeName);
		return (String[])list.toArray(new String[list.size()]);	
	}
	
	/**
	 * Returns a copy of the type name with '$' replaced by
	 * '.', or returns <code>null</code> if the given type
	 * name refers to an anonymous inner class. 
	 * 
	 * @param typeName a fully qualified type name
	 * @return a copy of the type name with '$' replaced by
	 * '.', or returns <code>null</code> if the given type
	 * name refers to an anonymous inner class.
	 */
	protected String getTranslatedTypeName(String typeName) {
		int index = typeName.lastIndexOf('$');
		if (index == -1) {
			return typeName;
		}
		if (index + 1 > typeName.length()) {
			// invalid name
			return typeName;
		}
		String last = typeName.substring(index + 1);
		try {
			Integer.parseInt(last);
			return null;
		} catch (NumberFormatException e) {
			return typeName.replace('$', '.');
		}
	}


	/**
	 * Returns the receiving type of the the given stack frame.
	 * 
	 * @return receiving type
	 * @exception DebugException if:<ul>
	 * <li>A failure occurs while accessing attributes of 
	 *  the stack frame</li>
	 * <li>the resolved type is an inner type</li>
	 * <li>unable to resolve a type</li>
	 * </ul>
	 */
	private IType getReceivingType(IJavaProject project, IJavaStackFrame frame) throws DebugException {
		String typeName= frame.getReceivingTypeName();
		String sourceName= frame.getSourceName();
		if (sourceName == null || !typeName.equals(frame.getDeclaringTypeName())) {
			// if there is no debug attribute or the declaring type is not the
			// same as the receiving type, we must guess at the receiver's source
			// file
			int dollarIndex= typeName.indexOf('$');
			if (dollarIndex >= 0) {
				typeName= typeName.substring(0, dollarIndex);
			}
			typeName = typeName.replace('.', IPath.SEPARATOR);
			typeName+= ".java";			 //$NON-NLS-1$
		} else {
			int index = typeName.lastIndexOf('.');
			if (index >= 0) {
				typeName = typeName.substring(0, index + 1);
				typeName = typeName.replace('.', IPath.SEPARATOR);
			} else {
				typeName = ""; //$NON-NLS-1$
			}
			typeName+=sourceName;
		}
		return getType(project, frame.getReceivingTypeName(), typeName);
	}
	
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator= order ? new CompletionProposalComparator() : null;
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
	
	protected ResultCollector getCollector() {
		return fCollector;
	}

	protected void setCollector(ResultCollector collector) {
		fCollector = collector;
	}

	protected IType getType(IJavaProject project, String originalTypeName, String typeName) throws DebugException {
		
		int dollarIndex= typeName.indexOf('$');
		if (dollarIndex > 0) {
			typeName= typeName.substring(0, dollarIndex);
		}
		IPath sourcePath =  new Path(typeName);
		IType type = null;
		try {
			IJavaElement result= project.findElement(sourcePath);
			String[] typeNames = getNestedTypeNames(originalTypeName);
			if (result != null) {
				if (result instanceof IClassFile) {
					type = ((IClassFile)result).getType();
				} else if (result instanceof ICompilationUnit) {
					type = ((ICompilationUnit)result).getType(typeNames[0]);
				} else if (result instanceof IType) {
					type = (IType)result;
				}
			}
			for (int i = 1; i < typeNames.length; i++) {
				String innerTypeName= typeNames[i];
				try {
					Integer.parseInt(innerTypeName);
					continue; //loop over anonymous types
				} catch (NumberFormatException e) {
				}
				type = type.getType(innerTypeName);
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		
		return type;	
	}
}