package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.ui.IActionFilter;

public class JavaThreadAdapterFactory implements IAdapterFactory {
	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
		if (adapterType == IActionFilter.class) {
			if (adaptableObject instanceof IJavaThread) {
				return new TerminateEvaluationActionFilter();
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
