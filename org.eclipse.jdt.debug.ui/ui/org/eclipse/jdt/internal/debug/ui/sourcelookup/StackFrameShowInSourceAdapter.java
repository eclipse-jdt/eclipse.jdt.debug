/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

/**
 * @since 3.2
 *
 */
public class StackFrameShowInSourceAdapter implements IShowInSource {
	
	private IJavaStackFrame fFrame;

	/**
	 * Constructs a new adapter on the given frame.
	 * 
	 * @param frame
	 */
	public StackFrameShowInSourceAdapter(IJavaStackFrame frame) {
		fFrame = frame;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		try {
			IType type = JavaDebugUtils.resolveDeclaringType(fFrame);
			if (type != null) {
				return new ShowInContext(null, new StructuredSelection(type));
			}
		} catch (CoreException e) {
		}
		return null;
	}

}
