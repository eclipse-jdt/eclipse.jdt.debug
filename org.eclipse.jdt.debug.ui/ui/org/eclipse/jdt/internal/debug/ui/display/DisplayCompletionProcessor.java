package org.eclipse.jdt.internal.debug.ui.display;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.java.ResultCollector;
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
	private DisplayView fView;
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
	
	public DisplayCompletionProcessor(DisplayView view) {
		fCollector= new ResultCollector();
		fView= view;
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
			IJavaStackFrame stackFrame= fView.getContext();
			if (stackFrame == null) {
				return new ICompletionProposal[0];
			}
			
			IJavaProject project= fView.getJavaProject(stackFrame);
			if (project != null) {
				ITextSelection selection= (ITextSelection)viewer.getSelectionProvider().getSelection();			
				ICompilationUnit cu= getCompilationUnit(stackFrame);
				if (cu == null) {
					return new ICompletionProposal[0];
				}
				IDocument doc = new Document(cu.getSource());
				int offset = doc.getLineOffset(stackFrame.getLineNumber());	
				configureResultCollector(project, selection, offset);	
				IWorkingCopy workingCopy= (IWorkingCopy) cu.getWorkingCopy();
				IBuffer buffer= ((ICompilationUnit)workingCopy).getBuffer();
				buffer.replace(offset, 0, fView.getContents());
				((ICompilationUnit)workingCopy).codeComplete(offset + selection.getOffset(), fCollector);
				workingCopy.destroy();
			
				// modify the replacement offsets to work on the display document
				JavaCompletionProposal[] proposals= fCollector.getResults();
				for (int i= 0; i < proposals.length; i++) {
					JavaCompletionProposal curr= (JavaCompletionProposal) proposals[i];
					int newOffset= curr.getReplacementOffset() - offset;
					if (newOffset >= 0) {
						curr.setReplacementOffset(newOffset);
					} else {
						curr.setReplacementOffset(0);
						curr.setReplacementLength(0);
					}
				}
				/*
				 * Order here and not in result collector to make sure that the order
				 * applies to all proposals and not just those of the compilation unit. 
				 */
				return order(proposals);	
			}
		} catch (JavaModelException x) {
			Shell shell= viewer.getTextWidget().getShell();
			ErrorDialog.openError(shell,
				DisplayMessages.getString("DisplayCompletionProcessor.Problems_during_completion_1"), //$NON-NLS-1$
				DisplayMessages.getString("DisplayCompletionProcessor.An_exception_occurred_during_code_completion_2"), //$NON-NLS-1$ 
				x.getStatus());  
		} catch (DebugException de) {
			Shell shell= viewer.getTextWidget().getShell();
			ErrorDialog.openError(shell,
				DisplayMessages.getString("DisplayCompletionProcessor.Problems_during_completion_1"), //$NON-NLS-1$
				DisplayMessages.getString("DisplayCompletionProcessor.An_exception_occurred_during_code_completion_2"), //$NON-NLS-1$
				de.getStatus());  
		} catch (BadLocationException ble) {
			Shell shell= viewer.getTextWidget().getShell();
			IStatus status= new Status(IStatus.ERROR, JDIDebugUIPlugin.getPluginId(), IStatus.ERROR, ble.getMessage(), ble);
			ErrorDialog.openError(shell, 
				DisplayMessages.getString("DisplayCompletionProcessor.Problems_during_completion_1"), //$NON-NLS-1$
				DisplayMessages.getString("DisplayCompletionProcessor.An_exception_occurred_during_code_completion_2"), //$NON-NLS-1$
				status); 
		}
		return null;

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
	 * Configures the display result collection for the current code assist session
	 */
	protected void configureResultCollector(IJavaProject project, ITextSelection selection, int editorOffset) {
		fCollector.reset(editorOffset + selection.getOffset(), project, null);
		if (selection.getLength() != 0) {
			fCollector.setReplacementLength(selection.getLength());
		} 
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
}