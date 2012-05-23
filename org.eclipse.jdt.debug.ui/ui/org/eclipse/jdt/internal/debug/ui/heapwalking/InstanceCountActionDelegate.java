/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.heapwalking;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

/**
 * Action to display the instance count for a selected type.
 * 
 * @since 3.6
 */
public class InstanceCountActionDelegate extends AllInstancesActionDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.heapwalking.AllInstancesActionDelegate#displayInstaces(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.jdt.internal.debug.core.model.JDIReferenceType)
	 */
	@Override
	protected void displayInstaces(IJavaDebugTarget target, JDIReferenceType rtype) {
		try {
			displayNumInstances(rtype.getName(), rtype.getInstanceCount());
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
			report(Messages.AllInstancesActionDelegate_0,getPart());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.heapwalking.AllInstancesActionDelegate#displayNoInstances(org.eclipse.jdt.debug.core.IJavaDebugTarget, java.lang.String)
	 */
	@Override
	protected void displayNoInstances(IJavaDebugTarget target, String typeName) {
		displayNumInstances(typeName, 0);
	}
	
	/**
	 * Displays a message dialog with the number of instances.
	 * 
	 * @param typeName type name
	 * @param instanceCount number of instances
	 */
	protected void displayNumInstances(String typeName, long instanceCount) {
		String message = null;
		if (instanceCount == 0L) {
			message = NLS.bind(Messages.InstanceCountActionDelegate_0, new String[]{typeName});
		} else if (instanceCount == 1L) {
			message = NLS.bind(Messages.InstanceCountActionDelegate_1, new String[]{typeName});
		} else {
			message = NLS.bind(Messages.InstanceCountActionDelegate_2, new String[]{Long.toString(instanceCount), typeName});
		}
		MessageDialog.openInformation(getShell(), Messages.InstanceCountActionDelegate_3, message);
	}
}
