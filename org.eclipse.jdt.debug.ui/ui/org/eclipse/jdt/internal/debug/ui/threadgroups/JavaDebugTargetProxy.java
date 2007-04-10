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

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy;
import org.eclipse.debug.internal.ui.viewers.update.StackFrameEventHandler;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public class JavaDebugTargetProxy extends DebugTargetProxy {

	private JavaThreadEventHandler fThreadEventHandler;
	/**
	 * @param target
	 */
	public JavaDebugTargetProxy(IDebugTarget target) {
		super(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		fThreadEventHandler = new JavaThreadEventHandler(this);
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), fThreadEventHandler,
				new StackFrameEventHandler(this, fThreadEventHandler)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugTargetProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		super.installed(viewer);
		fThreadEventHandler.init(viewer);
	}

}
