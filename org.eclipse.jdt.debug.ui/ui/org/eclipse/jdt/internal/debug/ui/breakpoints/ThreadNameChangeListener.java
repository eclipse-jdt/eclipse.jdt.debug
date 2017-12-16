/*******************************************************************************
 * Copyright (c) 2017 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;

/**
 * Breakpoint listener extension for the "thread name change" breakpoint.
 *
 * @since 3.9
 */
public class ThreadNameChangeListener implements IJavaBreakpointListener {

	public static final String ID_THREAD_CHANGE_NAME_LISTENER = JDIDebugUIPlugin.getUniqueIdentifier() + ".threadNameChangeListener"; //$NON-NLS-1$

	@Override
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
	}

	@Override
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
	}

	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		JavaDebugOptionsManager manager = JavaDebugOptionsManager.getDefault();
		if (!breakpoint.equals(manager.getThreadNameChangeBreakpoint())) {
			return DONT_CARE;
		}
		if (manager.isListeningOnThreadNameChanges()) {
			// notify debug view to refresh the thread name
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { new DebugEvent(thread, DebugEvent.CHANGE, DebugEvent.STATE) });
		}
		return DONT_SUSPEND;
	}

	@Override
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return DONT_CARE;
	}

}
