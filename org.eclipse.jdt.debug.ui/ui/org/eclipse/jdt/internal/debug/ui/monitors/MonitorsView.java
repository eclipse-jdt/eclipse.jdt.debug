package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.Page;

/**
 * Handles the different viewers: Thread, Monitor and Deadlock
 */
public class MonitorsView extends AbstractDebugEventHandlerView{

	public static final int VIEW_ID_THREAD = 1;
	public static final int VIEW_ID_MONITOR = 2;
	public static final int VIEW_ID_DEADLOCK = 3;
	
	private int viewId;
	
	private Viewer fDeadLocksViewer;
	private Viewer fMonitorsViewer;
	
	/**
	 * A page in this view's page book that contains this
	 * view's viewer.
	 */
	class MonitorsViewerPage extends Page {
		
		/**
		 * @see IPage#createControl(Composite)
		 */
		public void createControl(Composite parent) {
			Viewer viewer = createMonitorsViewer(parent);
			setMonitorsViewer(viewer);			
		}

		/**
		 * @see IPage#getControl()
		 */
		public Control getControl() {
			return getMonitorsViewer().getControl();
		}

		/**
		 * @see IPage#setFocus()
		 */
		public void setFocus() {
			Viewer viewer= getMonitorsViewer();
			if (viewer != null) {
				Control c = viewer.getControl();
				if (!c.isFocusControl()) {
					c.setFocus();
				}
			}
		}
	}
	
	/**
	 * A page in this view's page book that contains this
	 * view's viewer.
	 */
	class DeadLocksViewerPage extends Page {
		
		/**
		 * @see IPage#createControl(Composite)
		 */
		public void createControl(Composite parent) {
			Viewer viewer = createDeadLocksViewer(parent);
			setDeadLocksViewer(viewer);			
		}

		/**
		 * @see IPage#getControl()
		 */
		public Control getControl() {
			return getDeadLocksViewer().getControl();
		}

		/**
		 * @see IPage#setFocus()
		 */
		public void setFocus() {
			Viewer viewer= getDeadLocksViewer();
			if (viewer != null) {
				Control c = viewer.getControl();
				if (!c.isFocusControl()) {
					c.setFocus();
				}
			}
		}

	}
	
	public MonitorsView(){		
		viewId = VIEW_ID_THREAD;
		setEventHandler(new MonitorsDebugEventHandler(this));
	}

	/**
	 * Sets the current view (see view id)
	 * called from ToggleViewAction. Must be called after creation of the viewpart.
	 */	
	public void setView(int viewerIndex) {
		viewId = viewerIndex;
		switch (viewId) {
			case VIEW_ID_THREAD:
				showViewer();
				break;
			case VIEW_ID_DEADLOCK:
				showDeadLocksViewer();
				break;
			case VIEW_ID_MONITOR:
				showMonitorsViewer();
				break;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		StructuredViewer threadViewer = new TreeViewer(parent, SWT.MULTI);
		threadViewer.setContentProvider(new ThreadsViewContentProvider());
		threadViewer.setLabelProvider(new MonitorModelPresentation());
		threadViewer.setInput(JDIDebugModel.getMonitorManager());	
		return threadViewer;
	}

	protected Viewer createMonitorsViewer(Composite parent) {
		StructuredViewer monitorsViewer = new TreeViewer(parent, SWT.MULTI);
		monitorsViewer.setContentProvider(new MonitorsViewContentProvider());
		monitorsViewer.setLabelProvider(new MonitorModelPresentation());
		monitorsViewer.setInput(JDIDebugModel.getMonitorManager());	
		return monitorsViewer;
	}

	protected Viewer createDeadLocksViewer(Composite parent) {
		StructuredViewer deadLocksViewer = new TreeViewer(parent, SWT.MULTI) {
			//when refreshing, sets the color of the threads caught in a deadlock to red
			public void refresh() {
				getControl().setRedraw(false);
				super.refresh();
				
				Item[] children = getChildren(getControl());
				if (children != null) {
					//to be changed
					Color c= DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CHANGED_VARIABLE_RGB);
					for (int i = 0; i < children.length; i++) {
						updateColor((TreeItem)children[i], c, 0);
					}
				}
				getControl().setRedraw(true);
			}
			
			//goes down the tree, but only changes the color of the first and last element
			public void updateColor(TreeItem item, Color c, int count) {
				if((count ==0)||(item.getItems().length==0))
					item.setForeground(c);
				
				TreeItem[] children = item.getItems();
				for (int i = 0; i < children.length; i++) {
					updateColor(children[i], c, (count+1));
				}
			}

		};
		deadLocksViewer.setContentProvider(new DeadLocksViewContentProvider());
		deadLocksViewer.setLabelProvider(new MonitorModelPresentation());
		deadLocksViewer.setInput(JDIDebugModel.getMonitorManager());	
		return deadLocksViewer;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		// create the message page
		
		DeadLocksViewerPage deadLocksPage = new DeadLocksViewerPage();
		deadLocksPage.createControl(getPageBook());
		initPage(deadLocksPage);
		
		MonitorsViewerPage monitorsViewerPage = new MonitorsViewerPage();
		monitorsViewerPage.createControl(getPageBook());
		initPage(monitorsViewerPage);

		createContextMenu(getDeadLocksViewer().getControl());
		createContextMenu(getMonitorsViewer().getControl());
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return null;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator("vmGroup"));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator("vmGroup"));
	}

	/**
	 * Returns the deadLocksViewer.
	 * @return DeadLocksViewer
	 */
	public Viewer getDeadLocksViewer() {
		return fDeadLocksViewer;
	}

	/**
	 * Returns the monitorsViewer.
	 * @return MonitorsViewer
	 */
	public Viewer getMonitorsViewer() {
		return fMonitorsViewer;
	}

	/**
	 * Sets the deadLocksViewer.
	 * @param deadLocksViewer The deadLocksViewer to set
	 */
	public void setDeadLocksViewer(Viewer deadLocksViewer) {
		fDeadLocksViewer = deadLocksViewer;
	}

	/**
	 * Sets the monitorsViewer.
	 * @param monitorsViewer The monitorsViewer to set
	 */
	public void setMonitorsViewer(Viewer monitorsViewer) {
		fMonitorsViewer = monitorsViewer;
	}

	public void showDeadLocksViewer() {
		if (getPageBook().isDisposed()) {
			return;
		}
		if(JDIDebugModel.getMonitorManager().getDeadLockLists().isEmpty()){
			showMessage("No deadlock detected");
		} else{
			getPageBook().showPage(getDeadLocksViewer().getControl());
		}
	}
	
	public void showMonitorsViewer() {
		if (getPageBook().isDisposed()) {
			return;
		}
		getPageBook().showPage(getMonitorsViewer().getControl());
	}

	
	public void refreshViewers() {
		getViewer().refresh();
		((TreeViewer)getViewer()).expandAll();
		getMonitorsViewer().refresh();
		((TreeViewer)getMonitorsViewer()).expandAll();
		getDeadLocksViewer().refresh();
		((TreeViewer)getDeadLocksViewer()).expandAll();
		setView(viewId);
	}
}
