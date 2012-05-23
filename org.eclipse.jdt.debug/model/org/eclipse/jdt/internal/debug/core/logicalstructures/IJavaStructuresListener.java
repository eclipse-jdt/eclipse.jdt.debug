/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

/**
 * Listener which can be notified when the Java logical structures are changed
 * in some way.
 */
public interface IJavaStructuresListener {

	/**
	 * Notifies this listener that the Java logical structures have changed.
	 */
	public void logicalStructuresChanged();

}
