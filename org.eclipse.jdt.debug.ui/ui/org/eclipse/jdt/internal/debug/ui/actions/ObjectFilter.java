/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.actions;


import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter on a set of objects
 */
public class ObjectFilter extends ViewerFilter {

	/**
	 * Objects to filter
	 */
	protected List<?> fFilter;

	/**
	 * Creates a new filter that filters the given
	 * objects.
	 * @param objects the objects to filter against
	 */
	public ObjectFilter(List<?> objects) {
		fFilter = objects;
	}

	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return !fFilter.contains(element);
	}

}
