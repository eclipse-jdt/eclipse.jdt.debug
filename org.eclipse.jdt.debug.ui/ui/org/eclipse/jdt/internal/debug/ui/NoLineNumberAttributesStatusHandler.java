package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.sun.jdi.ReferenceType;

public class NoLineNumberAttributesStatusHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		ReferenceType type= (ReferenceType) source;
		final ErrorDialog dialog= new ErrorDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), "Java Breakpoint", MessageFormat.format("Attempting to install a breakpoint in the type {0} that has no line number attributes.  The breakpoint cannot be installed.  Class files must be generated with the line number attributes.", new String[] {type.name()}), status, IStatus.WARNING | IStatus.ERROR | IStatus.INFO);
		Display display= JDIDebugUIPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		return null;
	}

}
