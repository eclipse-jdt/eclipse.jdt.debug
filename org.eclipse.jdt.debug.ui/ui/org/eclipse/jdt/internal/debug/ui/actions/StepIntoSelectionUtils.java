/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility class for aiding step into selection actions and hyper-linking
 * 
 * @see StepIntoSelectionActionDelegate
 * @see StepIntoSelectionHyperlinkDetector
 * 
 * @since 3.3
 */
public class StepIntoSelectionUtils {

	
	/**
     * gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     */
    public static IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	return JavaUI.getWorkingCopyManager().getWorkingCopy(input);
    }
    
    /**
     * Returns the <code>IMethod</code> from the given selection within the given <code>IJavaElement</code>, 
     * or <code>null</code> if the selection does not container or is not an <code>IMethod</code>
     * @param selection
     * @param element
     * @return the corresponding <code>IMethod</code> from the selection within the provided <code>IJavaElement</code>
     * @throws JavaModelException
     */
    public static IMethod getMethod(ITextSelection selection, IJavaElement element) throws JavaModelException {
    	if(element != null && element instanceof ICodeAssist) {
    		return resolveMethod(selection.getOffset(), selection.getLength(), (ICodeAssist)element);
    	}
    	return null;
    }

	/**
	 * @param offset selection offset
	 * @param length selection length
	 * @param codeAssist context
	 * @return the method at the given position, or <code>null</code> if no method could be resolved 
	 * @throws JavaModelException
	 */
	private static IMethod resolveMethod(int offset, int length, ICodeAssist codeAssist) throws JavaModelException {
		IJavaElement[] elements = codeAssist.codeSelect(offset, length);
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof IMethod) {
				return (IMethod)elements[i];
			}
		}
		return null;
	}
    
	/**
	 * @param offset
	 * @param activeEditor
	 * @param element
	 * @return the first method found at or after <code>offset</code> on the same line
	 * @throws JavaModelException
	 */
	public static IMethod getFirstMethodOnLine(int offset, IEditorPart activeEditor, IJavaElement element) throws JavaModelException {
		if (! (activeEditor instanceof ITextEditor) || ! (element instanceof ICodeAssist)) {
			return null;
		}
		ITextEditor editor = (ITextEditor)activeEditor;
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null) {
			return null;
		}
		IDocument document = documentProvider.getDocument(editor.getEditorInput());
		if (document == null) {
			return null;
		}
		try {
			IRegion lineInfo = document.getLineInformationOfOffset(offset);
			String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
			IScanner scanner = ToolFactory.createScanner(false, false, false, null, JavaCore.VERSION_1_5);
			scanner.setSource(line.toCharArray());
			scanner.resetTo(offset - lineInfo.getOffset(), lineInfo.getLength());
			int token = scanner.getNextToken();
			while (token != ITerminalSymbols.TokenNameEOF) {
				if (token == ITerminalSymbols.TokenNameIdentifier) {
					int methodStart = scanner.getCurrentTokenStartPosition();
					token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameLPAREN) {
						return resolveMethod(lineInfo.getOffset() + methodStart, 0, (ICodeAssist)element);
					}
				} 
				else {
					token = scanner.getNextToken();
				}
			}
		} 
		catch (BadLocationException e) {
			return null;
		} 
		catch (InvalidInputException e) {
			return null;
		}
		return null;
	}

}
