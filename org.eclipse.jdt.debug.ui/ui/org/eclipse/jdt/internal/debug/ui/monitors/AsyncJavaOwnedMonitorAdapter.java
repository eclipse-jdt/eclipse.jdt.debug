package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class AsyncJavaOwnedMonitorAdapter extends AsyncMonitorAdapter {

	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
        return ((JavaOwnedMonitor)parent).getWaitingThreads();
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		JavaOwnedMonitor monitor = (JavaOwnedMonitor) element;
		return monitor.getWaitingThreads().length > 0;
	}

}
