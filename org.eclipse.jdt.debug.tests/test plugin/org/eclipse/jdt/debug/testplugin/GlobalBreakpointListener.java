/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;

/**
 * Listens to all breakpoint notifications.
 */
public class GlobalBreakpointListener implements IJavaBreakpointListener {

	public static Set<IJavaBreakpoint> ADDED = new LinkedHashSet<>();
	public static Set<IJavaBreakpoint> HIT = new LinkedHashSet<>();
	public static Set<IJavaBreakpoint> INSTALLED = new LinkedHashSet<>();
	public static Set<IJavaBreakpoint> REMOVED = new LinkedHashSet<>();
	public static Set<IJavaBreakpoint> INSTALLING = new LinkedHashSet<>();

	public static void clear() {
		ADDED.clear();
		HIT.clear();
		INSTALLED.clear();
		REMOVED.clear();
		INSTALLING.clear();
	}

	public GlobalBreakpointListener() {
	}

	@Override
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		ADDED.add(breakpoint);
	}

	@Override
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
	}

	@Override
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
	}

	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		HIT.add(breakpoint);
		return DONT_CARE;
	}

	@Override
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		INSTALLED.add(breakpoint);
	}

	@Override
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
		REMOVED.add(breakpoint);
	}

	@Override
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		INSTALLING.add(breakpoint);
		return DONT_CARE;
	}

}
