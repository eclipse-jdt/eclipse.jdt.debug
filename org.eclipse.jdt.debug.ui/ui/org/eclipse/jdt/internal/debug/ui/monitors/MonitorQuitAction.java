/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;


/**
 * Resumes all the threads
 */
public class MonitorQuitAction extends MonitorAction {
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {	
		IJavaDebugTarget target= getDebugTarget();
		if (target == null) {
			return;
		}
		try {
			IThread[] threads= target.getThreads();
			
			for (int i = 0; i < threads.length; i++) {
				IJavaThread thread = (IJavaThread)threads[i];
				if (thread.isSuspended()) {
					thread.resume();
					while (thread.isSuspended()) {
						Thread.sleep(100);
					}
				}
			}
		}
		catch (DebugException e) {
			JDIDebugUIPlugin.log(e);
		}
		catch (InterruptedException e){
			JDIDebugUIPlugin.log(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		boolean enable= false;
		if (fAction != null) {
			IJavaDebugTarget target= getDebugTarget();
			if (target != null) {
				if (target.supportsMonitorInformation()) {
					try {
						IThread[] threads= target.getThreads();
						for (int i = 0; i < threads.length; i++) {
							IJavaThread thread = (IJavaThread)threads[i];
							if (thread.isSuspended()) {
								enable= true;
								break;
							}
						} 
					}catch (DebugException e) {
					}
				}
			}
			fAction.setEnabled(enable);
		}
	}
}
