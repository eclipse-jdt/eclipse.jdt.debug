/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Sorts execution environments.
 * 
 * @since 3.3
 */
public class JREsEnvironmentComparator extends ViewerComparator {
	
	IExecutionEnvironment fEnvironment;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object element) {
		if (fEnvironment == null) {
			return super.category(element);
		} else {
			if (fEnvironment.isStrictlyCompatible((IVMInstall) element)) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		fEnvironment = (IExecutionEnvironment) viewer.getInput();
		int result = super.compare(viewer, e1, e2);
		fEnvironment = null;
		return result;
	}

}
