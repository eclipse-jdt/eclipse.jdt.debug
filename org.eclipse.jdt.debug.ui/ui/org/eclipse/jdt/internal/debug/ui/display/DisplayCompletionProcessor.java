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
package org.eclipse.jdt.internal.debug.ui.display;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jdt.internal.corext.template.ContextType;
import org.eclipse.jdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComparator;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.java.ResultCollector;
import org.eclipse.jdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
	private TemplateEngine fTemplateEngine;
	
	private char[] fProposalAutoActivationSet;
	private JavaCompletionProposalComparator fComparator;
		
	public DisplayCompletionProcessor() {
		fCollector= new ResultCollector();
		ContextType contextType= ContextTypeRegistry.getInstance().getContextType("java"); //$NON-NLS-1$
		if (contextType != null) {
			fTemplateEngine= new TemplateEngine(contextType);
		}
		fComparator= new JavaCompletionProposalComparator();
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
				
				int insertionPosition = computeInsertionPosition(receivingType, stackFrame);
				
				receivingType.codeComplete(viewer.getDocument().get().toCharArray(), insertionPosition, documentOffset,
					 localVariableTypeNames, localVariableNames,
					 localModifiers, stackFrame.isStatic(), fCollector);
				
				IJavaCompletionProposal[] results= fCollector.getResults();
				
				if (fTemplateEngine != null) {
					try {
						fTemplateEngine.reset();
						fTemplateEngine.complete(viewer, documentOffset, null);
						IJavaCompletionProposal[] templateResults= fTemplateEngine.getResults();

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
			}
		} catch (JavaModelException x) {
			handle(viewer, x);
		} catch (DebugException de) {
			handle(viewer, de);
		}
		
		return null;
	}

	protected int computeInsertionPosition(IType receivingType, IJavaStackFrame stackFrame) throws JavaModelException, DebugException {
		int insertion = -1;
		if (!receivingType.isBinary() && receivingType.getDeclaringType() == null) {
			ICompilationUnit stackCU= getCompilationUnit(stackFrame);
			ICompilationUnit typeCU= receivingType.getCompilationUnit();
			if (typeCU != null && typeCU.equals(stackCU)) {
				if (stackCU != null) {
					IDocument doc = new Document(stackCU.getSource());
					try {
						insertion = doc.getLineOffset(stackFrame.getLineNumber() - 1);
					} catch(BadLocationException e) {
						JDIDebugUIPlugin.log(e);
					}	
				}
			}
		}
		return insertion;
	}
	
	/**
	 * Returns the compliation unit associated with this
	 * Java stack frame.  Returns <code>null</code> for a binary stack
	 * frame.
	 */
	protected ICompilationUnit getCompilationUnit(IJavaStackFrame stackFrame) {
		// Get the corresponding element.
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return null;
		}
		Object sourceElement= locator.getSourceElement(stackFrame);
		if (sourceElement instanceof IType) {
			return (ICompilationUnit)((IType)sourceElement).getCompilationUnit();
		}
		if (sourceElement instanceof ICompilationUnit) {
			return (ICompilationUnit)sourceElement;
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
	protected IJavaCompletionProposal[] order(IJavaCompletionProposal[] proposals) {
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
					return type;
				} catch (NumberFormatException e) {
				}
				type = type.getType(innerTypeName);
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		
		return type;	
	}
	/**
	 * Returns the templateEngine.
	 * @return TemplateEngine
	 */
	public TemplateEngine getTemplateEngine() {
		return fTemplateEngine;
	}

}
