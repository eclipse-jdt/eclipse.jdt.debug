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
package org.eclipse.jdt.internal.debug.ui.console;


import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A hyperlink from a stack trace line of the form "*(*.java:*)"
 */
public class JavaStackTraceHyperlink implements IHyperlink {
	
	private TextConsole fConsole;

	/**
	 * Constructor for JavaStackTraceHyperlink.
	 */
	public JavaStackTraceHyperlink(TextConsole console) {
		fConsole = console;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkEntered()
	 */
	public void linkEntered() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkExited()
	 */
	public void linkExited() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
	 */
	public void linkActivated() {
		try {
			String typeName;
            int lineNumber;
            try {
                typeName = getTypeName();
                lineNumber = getLineNumber();
            } catch (CoreException e1) {
                ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Error, ConsoleMessages.JavaStackTraceHyperlink_Error, e1.getStatus()); 
                return;
            }
			
			// documents start at 0
			if (lineNumber > 0) {
				lineNumber--;
			}
			Object sourceElement = getSourceElement(typeName);
			if (sourceElement != null) {
				IDebugModelPresentation presentation = JDIDebugUIPlugin.getDefault().getModelPresentation();
				IEditorInput editorInput = presentation.getEditorInput(sourceElement);
				if (editorInput != null) {
					String editorId = presentation.getEditorId(editorInput, sourceElement);
					if (editorId != null) {
						IEditorPart editorPart = JDIDebugUIPlugin.getActivePage().openEditor(editorInput, editorId);
						if (editorPart instanceof ITextEditor && lineNumber >= 0) {
							ITextEditor textEditor = (ITextEditor)editorPart;
							IDocumentProvider provider = textEditor.getDocumentProvider();
							provider.connect(editorInput);
							IDocument document = provider.getDocument(editorInput);
							try {
								IRegion line = document.getLineInformation(lineNumber);
								textEditor.selectAndReveal(line.getOffset(), line.getLength());
							} catch (BadLocationException e) {
							}
							provider.disconnect(editorInput);
						}
						return;
					}
				}
			}
			// did not find source
			MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Information_1, MessageFormat.format(ConsoleMessages.JavaStackTraceHyperlink_Source_not_found_for__0__2, new String[] {typeName})); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(ConsoleMessages.JavaStackTraceHyperlink_An_exception_occurred_while_following_link__3, e); //$NON-NLS-1$
			return;
		}
	}
	
	/**
	 * Returns the source element associated with the given type name,
	 * or <code>null</code> if none.
	 * 
	 * @param typeName type name to search for source element
	 * @return the source element associated with the given type name,
	 * or <code>null</code> if none
	 */
	protected Object getSourceElement(String typeName) {
		ISourceLocator locator = getSourceLocator();
		if (locator == null) {
			try {
				// search for the type in the workspace
				return OpenTypeAction.findTypeInWorkspace(typeName);
			} catch (JavaModelException e) {
				return null;
			}
		}
		return OpenTypeAction.findSourceElement(typeName, locator);
	}
	
	/**
	 * Returns the source locator associated with this hyperlink, or
	 *  <code>null</code> if none
	 * 
	 * @return the source locator associated with this hyperlink, or
	 *  <code>null</code> if none
	 */
	private ISourceLocator getSourceLocator() {
		ISourceLocator sourceLocator = null;
		IProcess process = (IProcess) getConsole().getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
		if (process != null) {
		    ILaunch launch = process.getLaunch();
			if (launch != null) {
				sourceLocator = launch.getSourceLocator();
			}  
		}
		return sourceLocator;
	}

	/**
	 * Returns the fully qualified name of the type to open
	 *  
	 * @return fully qualified type name
	 * @exception CoreException if unable to parse the type name
	 */
	protected String getTypeName() throws CoreException {
		String linkText = getLinkText();		
		int index = linkText.indexOf('(');
		if (index >= 0) {
			String typeName = linkText.substring(0, index);
			// remove the method name
			index = typeName.lastIndexOf('.');
			int innerClassIndex = typeName.indexOf('$');
			if (innerClassIndex != -1)
				index = innerClassIndex;
			if (index >= 0) {
				typeName = typeName.substring(0, index);
			}
			return typeName;
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null); //$NON-NLS-1$
		throw new CoreException(status);
	}	
	
	/**
	 * Returns the line number associated with the stack trace or -1 if none.
	 * 
	 * @exception CoreException if unable to parse the number
	 */
	protected int getLineNumber() throws CoreException {
		String linkText = getLinkText();
		int index = linkText.lastIndexOf(':');
		if (index >= 0) {
			String numText = linkText.substring(index + 1, linkText.length() - 1);
			try {
				return Integer.parseInt(numText);
			} catch (NumberFormatException e) {
				IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__6, e); //$NON-NLS-1$
				throw new CoreException(status);
			}		
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__7, null); //$NON-NLS-1$
		throw new CoreException(status);			
	}
	
	/**
	 * Returns the console this link is contained in.
	 *  
	 * @return console
	 */
	protected TextConsole getConsole() {
		return fConsole;
	}
	
	/**
	 * Returns this link's text
	 * 
	 * @exception CoreException if unable to retrieve the text
	 */
	protected String getLinkText() throws CoreException {
	    try {
	        IRegion region = getConsole().getRegion(this);
	        IDocument document = getConsole().getDocument();
            int regionOffset = region.getOffset();
	        int lineNumber = document.getLineOfOffset(regionOffset);
	        IRegion lineInformation = document.getLineInformation(lineNumber);
            int lineOffset = lineInformation.getOffset();
	        String line = document.get(lineOffset, lineInformation.getLength());
            int paren = line.lastIndexOf("("); //$NON-NLS-1$
            String upToParen = line.substring(0, paren);
            int index = upToParen.lastIndexOf(' ');
            return line.substring(index+1);            
		} catch (BadLocationException e) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_retrieve_hyperlink_text__8, e); //$NON-NLS-1$
			throw new CoreException(status);
		}		
	}

}
