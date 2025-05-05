/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.ui.PlatformUI;

public class SuspendTimeoutStatusHandler implements IStatusHandler {

	/**
	 * @see IStatusHandler#handleStatus(IStatus, Object)
	 */
	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		IJavaThread thread= (IJavaThread) source;
		String threadName = thread.getName();
		Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				final ErrorDialog dialog = new ErrorDialog(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), DebugUIMessages.SuspendTimeoutHandler_suspend, NLS.bind(DebugUIMessages.SuspendTimeoutHandler_timeout_occurred, threadName), status, IStatus.WARNING
						| IStatus.ERROR | IStatus.INFO); //
				dialog.open();
			}
		});
		return null;
	}
}
