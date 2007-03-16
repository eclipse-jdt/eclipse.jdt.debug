/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.WorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;

/**
 * Controls access to the java working copy.  Isolated in this class as implementation requires
 * use of internal JDT UI code.  See bug 151260 for more information.
 *  
 * @since 3.3
 * @see org.eclipse.jdt.internal.ui.javaeditor.WorkingCopyManager
 */
public class DebugWorkingCopyManager {
	
	/**
	 * Returns the working copy remembered for the compilation unit encoded in the
	 * given editor input.	 
	 *
	 * @param input the editor input
	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
	 * @return the working copy of the compilation unit, or <code>null</code> if the
	 *   input does not encode an editor input, or if there is no remembered working
	 *   copy for this compilation unit
	 */
	public static ICompilationUnit getWorkingCopy(IEditorInput input, boolean primaryOnly) {
    	//TODO Using JDT UI internal code here, see bug 151260 for more information
		return ((WorkingCopyManager)JavaUI.getWorkingCopyManager()).getWorkingCopy(input, primaryOnly);
	}
	
}
