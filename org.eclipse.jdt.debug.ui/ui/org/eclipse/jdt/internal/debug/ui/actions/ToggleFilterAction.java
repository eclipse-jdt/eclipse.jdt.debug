package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.custom.BusyIndicator;
/**
 * A generic Toggle filter action, meant to be subclassed to provide
 * a specific filter.
 */
public abstract class ToggleFilterAction extends ToggleDelegateAction {
	/**
	 * Adds or removes the viewer filter depending
	 * on the value of the parameter.
	 */
	protected void valueChanged(final boolean on) {
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
			public void run() {
				if (on) {
					getViewer().removeFilter(getViewerFilter());
				} else {
					addFilterToViewer();
				}
			}
		});
	}

	/**
	 * Returns the <code>ViewerFilter</code> that this action
	 * will add/remove from the viewer, or <code>null</code>
	 * if no filter is involved.
	 */
	protected abstract ViewerFilter getViewerFilter();
	
	protected void setViewer(StructuredViewer viewer) {
		super.setViewer(viewer);
		addFilterToViewer();
	}
	
	protected void addFilterToViewer() {
		ViewerFilter filter = getViewerFilter();
		ViewerFilter[] filters = getViewer().getFilters();
		boolean alreadyAdded = false;
		for (int i = 0; i < filters.length; i++) {
			ViewerFilter addedFilter = filters[i];
			if (addedFilter.equals(filter)) {
				alreadyAdded = true;
				break;
			}
		}
		if (!alreadyAdded) {
			getViewer().addFilter(filter);
		}					
	}
}