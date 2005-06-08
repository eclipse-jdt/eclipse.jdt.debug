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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
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
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.widgets.Shell;

import com.sun.jdi.ClassNotLoadedException;

/**
 * Display snippet completion processor.
 */
public class DisplayCompletionProcessor implements IContentAssistProcessor {
		
	private CompletionProposalCollector fCollector;
	private IContextInformationValidator fValidator;
	private TemplateEngine fTemplateEngine;
    private String fErrorMessage = null;
	
	private char[] fProposalAutoActivationSet;
	private CompletionProposalComparator fComparator;
		
	public DisplayCompletionProcessor() {
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
    protected void setErrorMessage(String string) {
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
		try {
	        setErrorMessage(DisplayMessages.DisplayCompletionProcessor_0); //$NON-NLS-1$
			IAdaptable context = DebugUITools.getDebugContext();
			if (context == null) {
				return new ICompletionProposal[0];
			}
			
			IJavaStackFrame stackFrame= (IJavaStackFrame)context.getAdapter(IJavaStackFrame.class);
			if (stackFrame == null) {
				return new ICompletionProposal[0];
			}
	        setErrorMessage(null);	
			return computeCompletionProposals(stackFrame, viewer, documentOffset);
		} finally {
			releaseCollector();
		}
	}

	protected ICompletionProposal[] computeCompletionProposals(IJavaStackFrame stackFrame, ITextViewer viewer, int documentOffset) {
        setErrorMessage(null);
		try {
			IType receivingType = resolveType(stackFrame.getLaunch(), stackFrame.getReceivingTypeName(), getReceivingSourcePath(stackFrame));
			if (receivingType == null) {
                setErrorMessage(DisplayMessages.DisplayCompletionProcessor_1); //$NON-NLS-1$
				return new ICompletionProposal[0];
			}
			IJavaProject project = receivingType.getJavaProject();
				
			IVariable[] variables= stackFrame.getLocalVariables();
			char[][][] res= resolveLocalVariables(variables);
			char[][] localVariableNames= res[0];
			char[][] localVariableTypeNames= res[1];
			
			ITextSelection selection= (ITextSelection)viewer.getSelectionProvider().getSelection();
			configureResultCollector(project, selection);	
			
			int[] localModifiers= new int[localVariableNames.length];
			Arrays.fill(localModifiers, 0);
			
			int insertionPosition = computeInsertionPosition(receivingType, stackFrame);
			
			receivingType.codeComplete(viewer.getDocument().get().toCharArray(), insertionPosition, documentOffset,
				 localVariableTypeNames, localVariableNames,
				 localModifiers, stackFrame.isStatic(), fCollector);
			
			IJavaCompletionProposal[] results= fCollector.getJavaCompletionProposals();
			
			if (fTemplateEngine != null) {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, documentOffset, null);
				TemplateProposal[] templateResults= fTemplateEngine.getResults();

				// concatenate arrays
				IJavaCompletionProposal[] total= new IJavaCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results= total;					
			}	 
			 //Order here and not in result collector to make sure that the order
			 //applies to all proposals and not just those of the compilation unit. 
			return order(results);	
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
			return ((IType)sourceElement).getCompilationUnit();
		}
		if (sourceElement instanceof ICompilationUnit) {
			return (ICompilationUnit)sourceElement;
		}
		return null;
	}
	
	protected void handle(ITextViewer viewer, CoreException x) {
		Shell shell= viewer.getTextWidget().getShell();
		ErrorDialog.openError(shell,
			DisplayMessages.DisplayCompletionProcessor_Problems_during_completion_1, //$NON-NLS-1$
			DisplayMessages.DisplayCompletionProcessor_An_exception_occurred_during_code_completion_2, //$NON-NLS-1$ 
			x.getStatus());  
		JDIDebugUIPlugin.log(x);
	}
	
	protected char[][][] resolveLocalVariables(IVariable[] variables) throws DebugException {
		List localVariableNames= new ArrayList();
		List localVariableTypeNames= new ArrayList();
		for (int i = 0; i < variables.length; i++) {
			IVariable variable = variables[i];
			try {
				localVariableTypeNames.add(getTranslatedTypeName(variable.getReferenceTypeName()).toCharArray());
				localVariableNames.add(variable.getName().toCharArray());
			} catch (DebugException e) {
				// do not throw ClassNotLoadedException
				// nothing we can do, just ignore this local variable
				if (!(e.getStatus().getException() instanceof ClassNotLoadedException)) {
					throw e;
				}
			}
		}
		char[][] names= new char[localVariableNames.size()][];
		int i= 0;
		for (Iterator iter= localVariableNames.iterator(); iter.hasNext();) {
			names[i++]= (char[]) iter.next();
		}
		char[][] typeNames= new char[localVariableNames.size()][];
		i= 0;
		for (Iterator iter= localVariableTypeNames.iterator(); iter.hasNext();) {
			typeNames[i++]= (char[]) iter.next();
		}
		return new char[][][] {names, typeNames};
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
		if (sourceElement instanceof IResource) {
			IJavaProject project = JavaCore.create(((IResource)sourceElement).getProject());
			if (project.exists()) {
				return project;
			}
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
		fCollector = new CompletionProposalCollector(project);
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
	 * Returns a file name for the receiving type associated with the given
	 * stack frame.
	 * 
	 * @return file name for the receiving type associated with the given
	 * stack frame
	 * @exception DebugException if:<ul>
	 * <li>A failure occurs while accessing attributes of 
	 *  the stack frame</li>
	 * </ul>
	 */
	protected String getReceivingSourcePath(IJavaStackFrame frame) throws DebugException {
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
		return typeName;
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
	
	protected CompletionProposalCollector getCollector() {
		return fCollector;
	}
	
	/**
	 * Clears reference to result proposal collector.
	 */
	protected void releaseCollector() {
		if (fCollector != null && fCollector.getErrorMessage().length() > 0 && fErrorMessage != null) {
			setErrorMessage(fCollector.getErrorMessage());
		}		
		fCollector = null;
	}

	protected void setCollector(CompletionProposalCollector collector) {
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

	
	/**
	 * Returns the type associated with the given type name and source name
	 * from the given launch, or <code>null</code> if none.
	 * 
	 * @param launch the launch in which to resolve a type
	 * @param typeName fully qualified receiving type name (may include inner types)
	 * @param sourceName fully qualified name source file name containing the type
	 * @return associated Java model type or <code>null</code>
	 * @throws DebugException
	 */
	protected IType resolveType(ILaunch launch, String typeName, String sourceName) throws DebugException {
		ISourceLocator sourceLocator = launch.getSourceLocator();
		if (sourceLocator != null) {
			if (sourceLocator instanceof ISourceLookupDirector) {
				ISourceLookupDirector director = (ISourceLookupDirector) sourceLocator;
				try {
					Object[] objects = director.findSourceElements(sourceName);
					if (objects.length > 0) {
						Object element = objects[0];
						if (element instanceof IAdaptable) {
							IAdaptable adaptable = (IAdaptable) element;
							IJavaElement javaElement = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
							if (javaElement != null) {
								IType type = null;
								String[] typeNames = getNestedTypeNames(typeName);
								if (javaElement instanceof IClassFile) {
									type = ((IClassFile)javaElement).getType();
								} else if (javaElement instanceof ICompilationUnit) {
									type = ((ICompilationUnit)javaElement).getType(typeNames[0]);
								} else if (javaElement instanceof IType) {
									type = (IType)javaElement;
								}
								if (type != null) {
									for (int i = 1; i < typeNames.length; i++) {
										String innerTypeName= typeNames[i];
										try {
											Integer.parseInt(innerTypeName);
											return type;
										} catch (NumberFormatException e) {
										}
										type = type.getType(innerTypeName);
									}
								}
								return type;
							}
						}
					}
				} catch (CoreException e) {
					throw new DebugException(e.getStatus());
				}
			}
		}	
		return null;
	}	
}
