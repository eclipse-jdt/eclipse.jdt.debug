package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
	protected List fFilter;
	
	/**
	 * Creates a new filter that filters the given 
	 * objects.
	 */
	public ObjectFilter(List objects) {
		fFilter = objects;
	}
	
	/**
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return !fFilter.contains(element);
	}

}
