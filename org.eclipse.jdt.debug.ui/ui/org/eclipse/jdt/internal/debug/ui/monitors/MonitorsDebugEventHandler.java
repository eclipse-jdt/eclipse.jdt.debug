package org.eclipse.jdt.internal.debug.ui.monitors;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Listen to certain events in the debug view
 */
public class MonitorsDebugEventHandler extends AbstractDebugEventHandler {

	public MonitorsDebugEventHandler(MonitorsView view) {
		super(view);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events) {
		DebugEvent event;
		Object source;
		for (int i = 0; i < events.length; i++) {
			event= events[i];
			source= event.getSource();
			
			//if a thread is suspended in the debug view
			if(event.getKind() == DebugEvent.SUSPEND) {
				if (source instanceof IJavaDebugTarget) {
					MonitorManager.getDefault().updatePartial((IJavaDebugTarget)source);
					refreshView();
				} else if (source instanceof IJavaThread) {
					MonitorManager.getDefault().updatePartial((IJavaDebugTarget)(((IJavaThread)source).getDebugTarget()));
					refreshView();
				}
			} else if(event.getKind() == DebugEvent.RESUME) { 			
				if (source instanceof IJavaDebugTarget) {
					MonitorManager.getDefault().updatePartial((IJavaDebugTarget)source);
					refreshView();
				} else if (source instanceof IJavaThread) {
					MonitorManager.getDefault().updatePartial((IJavaDebugTarget)(((IJavaThread)source).getDebugTarget()));
					refreshView();
				}
			} else if(event.getKind() == DebugEvent.TERMINATE && source instanceof IJavaDebugTarget) {
				MonitorManager.getDefault().removeMonitorInformation((IJavaDebugTarget)source);
				refreshView();
			}
		}
	}
	
	private void refreshView() {
		((MonitorsView)getView()).refreshViewers();
	}
}
