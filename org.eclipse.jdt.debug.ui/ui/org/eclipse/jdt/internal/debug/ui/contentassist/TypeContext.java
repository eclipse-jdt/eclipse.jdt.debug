/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
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
	@Override
	public int getInsertionPosition() throws CoreException {
		return fPosition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#getLocalVariables()
	 */
	@Override
	public String[][] getLocalVariables() throws CoreException {
		return new String[0][];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#isStatic()
	 */
	@Override
	public boolean isStatic() throws CoreException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext#getSnippet(java.lang.String)
	 */
	@Override
	public String getSnippet(String snippet) throws CoreException {
		return snippet;
	}

}
