package org.eclipse.jdt.launching;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import org.eclipse.core.runtime.IPath;

/**
 * Determines if container entries are duplicates/redundant on a runtime
 * classpath. If an <code>IClasspathContianer</code> implements this interface,
 * the <code>isDuplicate</code> method is used to determine if containers are
 * duplicates/redundant. Otherwise, containers with the same identifier are
 * considered duplicates. 
 * 
 * @since 2.0.1
 */
public interface IRuntimeContainerComparator {
	
	/**
	 * Returns whether this container is a duplicate of the conatiner
	 * identified by the given path.
	 * 
	 * @return whether this container is a duplicate of the conatiner
	 * identified by the given path
	 */
	public boolean isDuplicate(IPath containerPath);

}
