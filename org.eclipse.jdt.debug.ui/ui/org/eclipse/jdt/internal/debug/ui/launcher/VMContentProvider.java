/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.util.ArrayList;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class VMContentProvider implements IStructuredContentProvider {

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IVMInstallType[]) {
			IVMInstallType[] vmTypes= (IVMInstallType[])inputElement;
			ArrayList vms= new ArrayList();
			for (int i= 0; i < vmTypes.length; i++) {
				IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
				for (int j= 0; j < vmInstalls.length; j++) 
					vms.add(vmInstalls[j]);
			}
			IVMInstall[] result= new IVMInstall[vms.size()];
			return vms.toArray(result);
		}
		
		return new Object[0];
	}

}
