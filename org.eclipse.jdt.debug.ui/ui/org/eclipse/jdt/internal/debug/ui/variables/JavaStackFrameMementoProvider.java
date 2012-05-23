/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Creates mementos for Java stack frames. Uses qualified names and signature rather than just
 * simple method name used by the default model.
 * 
 * @since 3.4
 */
public class JavaStackFrameMementoProvider extends DebugElementMementoProvider implements IElementMementoProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	protected String getElementName(Object element, IPresentationContext context) throws CoreException {
		if (element instanceof IJavaStackFrame) {
			StringBuffer buf = new StringBuffer();
			IJavaStackFrame frame = (IJavaStackFrame) element;
			buf.append(frame.getDeclaringTypeName());
			buf.append("#"); //$NON-NLS-1$
			buf.append(frame.getSignature());
			return buf.toString();
		}
		return null;
	}

}
