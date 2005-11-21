package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class AsyncJavaOwningThreadAdapter extends AsyncMonitorAdapter {

	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		JavaContendedMonitor contendedMonitor= ((JavaOwningThread)parent).getContendedMonitor();
		if (contendedMonitor == null) {
		    return EMPTY;
		}
		return new Object[]{contendedMonitor};
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		JavaOwningThread monitor = (JavaOwningThread) element;
		return monitor.getContendedMonitor() != null;
	}

}
