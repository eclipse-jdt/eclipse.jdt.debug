package org.eclipse.jdt.internal.debug.ui.console;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A hyperlink from a stack trace line of the form "*(*.java:*)"
 */
public class JavaStackTraceHyperlink implements IConsoleHyperlink {
	
	private int fOffset;
	private int fLength;
	private IConsole fConsole;

	/**
	 * Constructor for JavaStackTraceHyperlink.
	 */
	public JavaStackTraceHyperlink(IConsole console, int offset, int length) {
		fOffset = offset;
		fLength = length;
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
			String typeName = getTypeName();
			int lineNumber = getLineNumber();
			// documents start at 0
			if (lineNumber > 0) {
				lineNumber--;
			}
			IJavaSourceLocation[] sourceLocations = getSourceLocations();
		
			for (int i = 0; i < sourceLocations.length; i++) {
				IJavaSourceLocation location = sourceLocations[i];
				Object sourceElement = location.findSourceElement(typeName);
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
			}
			// did not find source
			MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Information", MessageFormat.format("Source not found for {0}", new String[] {typeName}));
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog("An exception occurred while following link.", e);
			return;
		}
	}
	
	/**
	 * Returns the locations in which to look for source associatd with the
	 * stack trace, or <code>null</code> if none.
	 *  
	 * @return IJavaSourceLocation[]
	 */
	protected IJavaSourceLocation[] getSourceLocations() {
		ISourceLocator sourceLocator = null;
		ILaunch launch = getConsole().getProcess().getLaunch();
		if (launch != null) {
			sourceLocator = launch.getSourceLocator();
		}
		IJavaSourceLocation[] sourceLocations = null;
		if (sourceLocator instanceof JavaSourceLocator) {
			sourceLocations = ((JavaSourceLocator)sourceLocator).getSourceLocations();
		} else if (sourceLocator instanceof JavaUISourceLocator) {
			sourceLocations = ((JavaUISourceLocator)sourceLocator).getSourceLocations();
		}
		if (sourceLocations == null) {
			// create a source locator using all projects in the workspace
			IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			if (javaModel == null) {
				return null;
			}
			try {
				sourceLocator = new JavaUISourceLocator(javaModel.getJavaProjects(), false);
			} catch (JavaModelException e) {
				JDIDebugUIPlugin.errorDialog("Unable to retrieve workspace source.", e);
				return null;
			}
			sourceLocations = ((JavaUISourceLocator)sourceLocator).getSourceLocations();
		}
		return sourceLocations;
	}
	
	/**
	 * Returns the fully qualified name of the type to open
	 *  
	 * @return fully qualified type name
	 * @exception CoreException if unable to parse the type name
	 */
	protected String getTypeName() throws CoreException {
		String linkText = getLinkText();		
		int index = linkText.lastIndexOf('(');
		if (index >= 0) {
			String typeName = linkText.substring(0, index);
			// remove the method name
			index = typeName.lastIndexOf('.');
			if (index >= 0) {
				typeName = typeName.substring(0, index);
			}
			return typeName;
		} else {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, "Unable to parse type name from hyperlink.", null);
			throw new CoreException(status);
		}
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
				IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, "Unable to parse line number from hyperlink.", e);
				throw new CoreException(status);
			}		
		} else {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, "Unable to parse line number from hyperlink.", null);
			throw new CoreException(status);			
		}
	}

	/**
	 * @see org.eclipse.jface.text.IRegion#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/**
	 * @see org.eclipse.jface.text.IRegion#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}
	
	/**
	 * Returns the console this link is contained in.
	 *  
	 * @return console
	 */
	protected IConsole getConsole() {
		return fConsole;
	}
	
	/**
	 * Returns this link's text
	 * 
	 * @exception CoreException if unable to retrieve the text
	 */
	protected String getLinkText() throws CoreException {
		try {
			return getConsole().getDocument().get(getOffset(), getLength());
		} catch (BadLocationException e) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, "Unable to retrieve hyperlink text.", e);
			throw new CoreException(status);
		}		
	}

}
