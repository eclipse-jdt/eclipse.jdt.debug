/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class SuspendTimeoutStatusHandler implements IStatusHandler {

	/**
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		IJavaThread thread= (IJavaThread) source;
		final ErrorDialog dialog= new ErrorDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), DebugUIMessages.SuspendTimeoutHandler_suspend, NLS.bind(DebugUIMessages.SuspendTimeoutHandler_timeout_occurred, new String[] {thread.getName()}), status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO); // 
		Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return null;
	}
}
