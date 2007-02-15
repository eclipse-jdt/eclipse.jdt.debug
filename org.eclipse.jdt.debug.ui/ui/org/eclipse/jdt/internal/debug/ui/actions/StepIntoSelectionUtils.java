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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;

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
    		IJavaElement[] elements = ((ICodeAssist)element).codeSelect(selection.getOffset(), selection.getLength());
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IMethod) {
					return (IMethod)elements[i];
				}
			}
    	}
    	return null;
    }
}
