/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.launching;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Searches for Java source code.
 * 
 * @since 3.0
 */
public class JavaSourceLookupParticipant extends AbstractSourceLookupParticipant {
	
	/**
	 * Returns the source name associated with the given object, or <code>null</code>
	 * if none.
	 * 
	 * @param object a Java stack frame
	 * @return the source name associated with the given object, or <code>null</code>
	 * if none
	 * @exception CoreException if unable to retrieve the source name
	 */
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof IAdaptable) {
			IJavaStackFrame frame = (IJavaStackFrame) ((IAdaptable)object).getAdapter(IJavaStackFrame.class);
			if (frame != null) {
				if (frame.isObsolete()) {
					return null;
				}
				String sourceName = frame.getSourcePath();
				// TODO: this may break fix to bug 21518
				if (sourceName == null) {
					// no debug attributes, guess at source name
					sourceName = frame.getDeclaringTypeName();
					int index = sourceName.lastIndexOf('.');
					if (index < 0) {
						index = 0;
					}
					sourceName = sourceName.replace('.', File.separatorChar);
					index = sourceName.indexOf('$');
					if (index >= 0) {
						sourceName = sourceName.substring(0, index);
					}
					sourceName = sourceName + ".java"; //$NON-NLS-1$
				}
				return sourceName;	
			}
		}
		return null;
	}
}
