/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui.performance;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

public class JavaApplicationStatusHandler implements IStatusHandler {

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
     */
    public Object handleStatus(IStatus status, Object source) throws CoreException {
        ILaunchConfigurationDialog dialog = (ILaunchConfigurationDialog)source;
		dialog.setActiveTab(3);
		return null;
    }
}
