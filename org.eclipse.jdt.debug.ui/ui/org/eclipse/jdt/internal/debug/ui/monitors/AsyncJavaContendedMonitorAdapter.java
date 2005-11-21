package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class AsyncJavaContendedMonitorAdapter extends AsyncMonitorAdapter {

	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
        JavaOwningThread owningThread= ((JavaContendedMonitor)parent).getOwningThread();
        if (owningThread == null) {
            return EMPTY;
        }
        return new Object[]{owningThread};
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		JavaContendedMonitor monitor = (JavaContendedMonitor) element;
		return monitor.getOwningThread() != null;
	}

}
