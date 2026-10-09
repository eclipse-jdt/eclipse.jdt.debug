/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Carsten Hammer - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.jdi.tests;

import java.util.concurrent.TimeUnit;

/** Bounded termination of processes owned by a JDI test. */
final class TestProcessCleanup {
	private static final long EXIT_TIMEOUT_MILLIS = 5000;

	private TestProcessCleanup() {
	}

	static void terminate(Process... processes) {
		terminate(EXIT_TIMEOUT_MILLIS, processes);
	}

	static void terminate(long timeoutMillis, Process... processes) {
		if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("Process exit timeout must be positive");
		}
		Throwable failure = null;
		for (Process process : processes) {
			try {
				terminate(process, timeoutMillis);
			} catch (RuntimeException | Error e) {
				if (failure == null) {
					failure = e;
				} else if (failure != e) {
					failure.addSuppressed(e);
				}
			}
		}
		if (failure instanceof RuntimeException e) {
			throw e;
		}
		if (failure instanceof Error e) {
			throw e;
		}
	}

	private static void terminate(Process process, long timeoutMillis) {
		if (process == null || !process.isAlive()) {
			return;
		}
		// Cleanup must still run when startup was interrupted. Restore the flag below.
		boolean interrupted = Thread.interrupted();
		try {
			process.destroy();
			if (!interrupted) {
				try {
					if (process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
						return;
					}
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			process.destroyForcibly();
			long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
			while (process.isAlive()) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					throw terminationFailure(process);
				}
				try {
					if (!process.waitFor(remaining, TimeUnit.NANOSECONDS) && process.isAlive()) {
						throw terminationFailure(process);
					}
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static IllegalStateException terminationFailure(Process process) {
		String pid;
		try {
			pid = Long.toString(process.pid());
		} catch (UnsupportedOperationException e) {
			pid = "unavailable";
		}
		return new IllegalStateException("Test process " + pid + " did not terminate after destroyForcibly()");
	}
}
