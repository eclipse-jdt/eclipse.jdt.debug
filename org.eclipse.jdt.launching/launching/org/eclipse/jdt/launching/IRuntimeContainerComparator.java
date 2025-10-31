/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching;


import org.eclipse.core.runtime.IPath;

/**
 * Determines if container entries are duplicates/redundant on a runtime
 * classpath. If an <code>IClasspathContianer</code> implements this interface,
 * the <code>isDuplicate</code> method is used to determine if containers are
 * duplicates/redundant. Otherwise, containers with the same identifier are
 * considered duplicates.
 *
 * @since 2.0.1
 * @deprecated support has been added to <code>ClasspathContainerInitializer</code>
 *  to handle comparison of classpath containers. Use
 *  <code>ClasspathContainerInitializer.getComparisonID(IPath,IJavaProject)</code>.
 *  When a classpath container implements this interface, this interface is
 *  used to determine equality before using the support defined in
 *  <code>ClasspathContainerInitializer</code>.
 */
@Deprecated
public interface IRuntimeContainerComparator {

	/**
	 * Returns whether this container is a duplicate of the container
	 * identified by the given path.
	 *
	 * @param containerPath the container to compare against
	 * @return whether this container is a duplicate of the container
	 * identified by the given path
	 */
	@Deprecated
	public boolean isDuplicate(IPath containerPath);

}
