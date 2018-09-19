/*******************************************************************************
 *  Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui.performance;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

/**
 * Handles the status passed to it. In this instance the third tab
 * is selected (set as the acive tab)
 */
public class JavaApplicationStatusHandler implements IStatusHandler {

    /**
     * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
     */
    @Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {
        ILaunchConfigurationDialog dialog = (ILaunchConfigurationDialog)source;
		dialog.setActiveTab(3);
		return null;
    }
}
