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
package org.eclipse.jdt.debug.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * 
 * 
 * @since 3.0
 */
public interface IJavaObjectBrowser {

	public IJavaVariable[] getChildren(IJavaValue value) throws DebugException;
	
	public boolean hasChildren(IJavaValue value) throws DebugException;
	
}
