package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.ui.IActionFilter;

public class JavaStackFrameAdapterFactory implements IAdapterFactory {

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
		if (adapterType == IActionFilter.class) {
			if (adaptableObject instanceof IJavaStackFrame) {
				return new DropToFrameActionFilter();
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IActionFilter.class };
	}
}

