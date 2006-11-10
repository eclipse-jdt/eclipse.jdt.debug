/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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


public class AsyncJavaWaitingThreadAdapter extends AsyncMonitorAdapter {
	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		return ((JavaWaitingThread)parent).getOwnedMonitors();
	}

	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		JavaWaitingThread thread = (JavaWaitingThread) element;
		return thread.getOwnedMonitors().length > 0;
	}
}
