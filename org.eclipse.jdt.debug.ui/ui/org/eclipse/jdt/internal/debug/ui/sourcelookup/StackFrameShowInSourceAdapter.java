/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

/**
 * @since 3.2
 *
 */
public class StackFrameShowInSourceAdapter implements IShowInSource {
	
	class LazyShowInContext extends ShowInContext {

		boolean resolved = false;
		
		/**
		 * Constructs a 'show in context' that resolves its selection lazily
		 * since it requires a source lookup.
		 */
		public LazyShowInContext() {
			super(null, null);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.part.ShowInContext#getSelection()
		 */
		@Override
		public ISelection getSelection() {
			if (!resolved) {
				try {
					resolved = true;
					IType type = JavaDebugUtils.resolveDeclaringType(fFrame);
					if (type != null) {
						setSelection(new StructuredSelection(type));
					}
				} catch (CoreException e) {
				}
			}
			return super.getSelection();
		}
		
	}
	
	private IJavaStackFrame fFrame;
	
	private ShowInContext fLazyContext = null;

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
		if (fLazyContext == null) {
			fLazyContext = new LazyShowInContext();
		}
		return fLazyContext;
	}

}
