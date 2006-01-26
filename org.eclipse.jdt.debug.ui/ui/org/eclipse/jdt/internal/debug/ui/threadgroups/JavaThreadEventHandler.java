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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.ThreadEventHandler;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;

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
		if (AsynchDebugTargetTreeContentAdapter.isShowThreadGroups()) {
			delta = delta.addNode(thread.getLaunch(), IModelDelta.NOCHANGE);
			delta = delta.addNode(thread.getDebugTarget(), IModelDelta.NOCHANGE);
			List groups = new ArrayList();
			IJavaThread javaThread = (IJavaThread) thread;
			try {
				IJavaThreadGroup threadGroup = javaThread.getThreadGroup();
				while (threadGroup != null) {
					groups.add(0, threadGroup);
					threadGroup = threadGroup.getThreadGroup();
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Iterator iterator = groups.iterator();
			while (iterator.hasNext()) {
				delta = delta.addNode(iterator.next(), IModelDelta.NOCHANGE);
			}
			return delta;
		} else {
			return super.addPathToThread(delta, thread);
		}
	}	

}
