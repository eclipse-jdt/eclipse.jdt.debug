package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public abstract class StackFrameAction extends OpenTypeAction {
	
	protected boolean isEnabledFor(Object element) {
		return element instanceof IAdaptable && ((IAdaptable) element).getAdapter(IJavaStackFrame.class) != null;
	}
		
	protected IDebugElement getDebugElement(IAdaptable element) {
		return (IDebugElement)element.getAdapter(IJavaStackFrame.class);
	}
}
