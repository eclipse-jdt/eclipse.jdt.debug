package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public abstract class OpenStackFrameAction extends OpenTypeAction {
	
	protected boolean isEnabledFor(Object element) {
		return element instanceof IAdaptable && ((IAdaptable) element).getAdapter(IJavaStackFrame.class) != null;
	}
		
	protected IDebugElement getDebugElement(IAdaptable element) {
		return (IDebugElement)element.getAdapter(IJavaStackFrame.class);
	}
}
