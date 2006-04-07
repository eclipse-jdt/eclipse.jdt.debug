/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;

/**
 * @since 3.2
 *
 */
public class JavaThreadEventHandler extends ThreadEventHandler {

	/**
	 * Constructs and event handler for a Java thread.
	 * 
	 * @param proxy
	 */
	public JavaThreadEventHandler(AbstractModelProxy proxy) {
		super(proxy);
	}
	
	protected ModelDelta addPathToThread(ModelDelta delta, IThread thread) {
		if (JavaDebugTargetContentAdapter.isShowThreadGroups()) {
			delta = delta.addNode(thread.getLaunch(), IModelDelta.NO_CHANGE);
			delta = delta.addNode(thread.getDebugTarget(), IModelDelta.NO_CHANGE);
			List groups = new ArrayList();
			IJavaThread javaThread = (IJavaThread) thread;
			try {
				IJavaThreadGroup threadGroup = javaThread.getThreadGroup();
				while (threadGroup != null) {
					groups.add(0, threadGroup);
					threadGroup = threadGroup.getThreadGroup();
				}
			} catch (DebugException e) {
				JDIDebugUIPlugin.log(e);
			}
			Iterator iterator = groups.iterator();
			while (iterator.hasNext()) {
				delta = delta.addNode(iterator.next(), IModelDelta.NO_CHANGE);
			}
			return delta;
		} else {
			return super.addPathToThread(delta, thread);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler#handlesEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected boolean handlesEvent(DebugEvent event) {
		if (super.handlesEvent(event)) {
			Object source = event.getSource();
			if (source instanceof IJavaThread) {
				IJavaThread thread = (IJavaThread) source;
				ILaunch launch = thread.getLaunch();
				if (launch != null) {
					if (launch.getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) != null) {
						if (event.getKind() == DebugEvent.SUSPEND) {
							try {
								IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
								if (frame.getDeclaringTypeName().startsWith("org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain")) {
									return false;
								}
							} catch (DebugException e) {
							}
						}
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}	

}
