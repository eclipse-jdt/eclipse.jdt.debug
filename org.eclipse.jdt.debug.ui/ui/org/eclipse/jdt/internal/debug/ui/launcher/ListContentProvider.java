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


import java.util.List;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/** 
 * A specialized content provider to show a list of vm installs
 */ 
public class ListContentProvider implements IStructuredContentProvider {
	StructuredViewer fViewer;
	List fInput;	

	public ListContentProvider(StructuredViewer viewer, List input) {
		fViewer= viewer;
		fInput= input;
	}
	
	public Object[] getElements(Object input) {
		IVMInstall[] installs= new IVMInstall[fInput.size()];
		return fInput.toArray(installs);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fInput= (List)newInput;
	}

	public void dispose() {
		fViewer= null;
		fInput= null;
	}
	
	public boolean isDeleted(Object o) {
		return fInput.contains(o);
	}
}
