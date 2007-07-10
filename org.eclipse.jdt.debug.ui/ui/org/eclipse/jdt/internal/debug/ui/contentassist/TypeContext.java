/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.contentassist;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * Code completion for a type with position information and no locals.
 * 
 * @since 3.2
 */
public class TypeContext implements IJavaDebugContentAssistContext {
	
	private IType fType;
	private int fPosition;
	
	/**
	 * Constructs a completion context on the given type.
	 * 
	 * @param type type in which to perform completions
	 * @param insertionPoistion position in source to perform completions or -1
	 */
	public TypeContext(IType type, int insertionPoistion) {
		fType = type;
		fPosition = insertionPoistion;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#getType()
	 */
	public IType getType() throws CoreException {
		if (fType == null) {
			unableToResolveType();
		}
		return fType;
	}

    /**
     * Throws an exception when unable to resolve a type
     * 
     * @throws CoreException
     */
    protected void unableToResolveType() throws CoreException {
        IStatus status = new Status(IStatus.INFO, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "Unable to resolve enclosing type", null); //$NON-NLS-1$
        throw new CoreException(status);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#getInsertionPosition()
	 */
	public int getInsertionPosition() throws CoreException {
		return fPosition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#getLocalVariables()
	 */
	public String[][] getLocalVariables() throws CoreException {
		return new String[0][];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#isStatic()
	 */
	public boolean isStatic() throws CoreException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext#getSnippet(java.lang.String)
	 */
	public String getSnippet(String snippet) throws CoreException {
		return snippet;
	}

}
