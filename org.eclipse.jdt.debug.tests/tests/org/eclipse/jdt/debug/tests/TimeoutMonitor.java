/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A progress monitor which reports that the task is cancelled if a timeout occurs. The starting time for the timeout is the creation of the monitor.
 */
public class TimeoutMonitor implements IProgressMonitor {

	private final long timeoutMs;
	private final long startMs;

	TimeoutMonitor(long timeoutMs) {
		this.timeoutMs = timeoutMs;
		this.startMs = System.currentTimeMillis();
	}

	@Override
	public void beginTask(String name, int totalWork) {
	}

	@Override
	public void done() {
	}

	@Override
	public void internalWorked(double work) {
	}

	@Override
	public boolean isCanceled() {
		return System.currentTimeMillis() - startMs > timeoutMs;
	}

	@Override
	public void setCanceled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTaskName(String name) {
	}

	@Override
	public void subTask(String name) {
	}

	@Override
	public void worked(int work) {
	}

}
