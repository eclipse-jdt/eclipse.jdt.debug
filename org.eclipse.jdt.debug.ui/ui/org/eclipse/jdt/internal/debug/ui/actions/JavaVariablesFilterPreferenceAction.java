package org.eclipse.jdt.internal.debug.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaVariablesFilterPreferenceDialog;
import org.eclipse.jdt.internal.debug.ui.JavaVariablesViewerFilter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Open the variable filtering dialog for the view part that init'd this action.
 */
public class JavaVariablesFilterPreferenceAction implements IViewActionDelegate {

	/**
	 * This part listener cleans up when the specified viewer is closed.
	 */
	private static class JavaVariablesFilterPartListener implements IPartListener {
		
		private StructuredViewer fViewer;
		private IWorkbenchPage fWorkbenchPage;
		
		public JavaVariablesFilterPartListener(StructuredViewer viewer, IWorkbenchPage workbenchPage) {
			fViewer = viewer;
			fWorkbenchPage = workbenchPage;
		}
		
		/**
		 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof IDebugView) {
				IDebugView debugView = (IDebugView) part;
				Viewer viewer = debugView.getViewer();
				if (viewer.equals(fViewer)) {
					fgViewerSet.remove(fViewer);
					fWorkbenchPage.removePartListener(this);
				}
			}
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {
		}

	}

	/**
	 * The viewer that 'owns' this action.
	 */
	private StructuredViewer fViewer;
	
	/**
	 * Keeps track of all viewers that use a Java variables filter.
	 */
	private static Set fgViewerSet = new HashSet(3);
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Shell shell= JDIDebugUIPlugin.getActiveWorkbenchShell();
		JavaVariablesFilterPreferenceDialog dialog = new JavaVariablesFilterPreferenceDialog(shell, getViewer());
		dialog.open();
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		if (view instanceof IDebugView) {
			Viewer viewer = ((IDebugView)view).getViewer();
			if (viewer instanceof StructuredViewer) {
				setViewer((StructuredViewer) viewer);
				initializeCleanupInfrastructure(view);
				applyFilterToViewer(getViewer());
			}
		}
	}
	
	private void initializeCleanupInfrastructure(IViewPart viewPart) {
		StructuredViewer viewer = getViewer();
		fgViewerSet.add(viewer);
		IWorkbenchPage workbenchPage = viewPart.getSite().getPage();
		workbenchPage.addPartListener(new JavaVariablesFilterPartListener(viewer, workbenchPage));
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}

	private StructuredViewer getViewer() {
		return fViewer;
	}

	/**
	 * Create filters or refresh the existing filter for all viewers that
	 * require them.
	 */
	public static void applyFilterToViewers() {
		Iterator iterator = fgViewerSet.iterator();
		while (iterator.hasNext()) {
			StructuredViewer viewer = (StructuredViewer) iterator.next();
			applyFilterToViewer(viewer);
		}
	}

	/**
	 * Apply a new filter to the specified viewer.  If one is already present,
	 * refresh it.
	 */
	public static void applyFilterToViewer(StructuredViewer viewer) {
		JavaVariablesViewerFilter filter = retrieveViewerFilter(viewer);
		if (filter == null) {
			filter = new JavaVariablesViewerFilter();
			viewer.addFilter(filter);
		} else {
			filter.resetState();
			viewer.refresh();
		}
	}

	/**
	 * Find & return the first instance of
	 * <code>JavaVariablesViewerFilter</code> that is registered as a filter on
	 * the specified viewer.
	 */
	private static JavaVariablesViewerFilter retrieveViewerFilter(StructuredViewer viewer) {
		ViewerFilter[] filters = viewer.getFilters();
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] instanceof JavaVariablesViewerFilter) {
				return (JavaVariablesViewerFilter) filters[i];
			}
		}
		return null;
	}

}
