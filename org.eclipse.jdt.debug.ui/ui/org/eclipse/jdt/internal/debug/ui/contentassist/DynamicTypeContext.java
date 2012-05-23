/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
import org.eclipse.jdt.core.IType;

/**
 * Code completion for a dynamic type. Code completion is performed relative to the
 * type with no position information, in a non-static context.
 * 
 * @since 3.2
 */
public class DynamicTypeContext extends TypeContext {
	
	/**
	 * Provides a type in which to perform completions.
	 * @since 3.2
	 */
	public interface ITypeProvider {
		/**
		 * Returns the type in which to perform completions or <code>null</code>
		 * if no type is available.
		 * 
		 * @return type in which to perform completions or <code>null</code>
		 * @exception CoreException if a type cannot be resolved
		 */
		public IType getType() throws CoreException;
	}
	
	private ITypeProvider fTypeProvider;
	
	/**
	 * Constructs a completion context on the given type.
	 * 
	 * @param type type in which to perform completions
	 */
	public DynamicTypeContext(ITypeProvider type) {
		super(null, -1);
		fTypeProvider = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.text.IJavaDebugCompletionProcessorContext#getType()
	 */
	@Override
	public IType getType() throws CoreException {
		IType type = fTypeProvider.getType();
		if (type == null) {
			return super.getType();
		}
		return type;
	}

}
