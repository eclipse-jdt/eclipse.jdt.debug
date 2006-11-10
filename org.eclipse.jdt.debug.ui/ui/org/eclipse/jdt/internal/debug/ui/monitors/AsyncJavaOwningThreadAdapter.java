/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

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
