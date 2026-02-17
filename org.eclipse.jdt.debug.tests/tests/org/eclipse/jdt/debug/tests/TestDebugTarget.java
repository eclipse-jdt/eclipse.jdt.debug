/*******************************************************************************
 * Copyright (c) 2025 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Advantest Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * This is a test debug target which can be used to mimic an IDebugTarget instance.
 */
public class TestDebugTarget implements IDebugTarget {

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	@Override
	public boolean isDisconnected() {
		return false;
	}

	@Override
	public void disconnect() throws DebugException {

	}

	@Override
	public boolean canDisconnect() {
		return false;
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {

	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {

	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {

	}

	@Override
	public void suspend() throws DebugException {

	}

	@Override
	public void resume() throws DebugException {

	}

	@Override
	public boolean isSuspended() {
		return false;
	}

	@Override
	public boolean canSuspend() {
		return false;
	}

	@Override
	public boolean canResume() {
		return false;
	}

	@Override
	public void terminate() throws DebugException {

	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean canTerminate() {
		return false;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public String getModelIdentifier() {
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return null;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return false;
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return false;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return null;
	}

	@Override
	public IProcess getProcess() {
		return null;
	}

	@Override
	public String getName() throws DebugException {
		return null;
	}
}