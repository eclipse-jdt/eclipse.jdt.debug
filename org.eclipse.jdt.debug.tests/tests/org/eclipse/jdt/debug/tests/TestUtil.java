/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.swt.widgets.Display;
import org.junit.Assert;

public class TestUtil {

	/**
	 * Call this in the tearDown method of every test to clean up state that can
	 * otherwise leak through SWT between tests.
	 */
	public static void cleanUp(String owner) {
		// Ensure that the Thread.interrupted() flag didn't leak.
		Assert.assertFalse("The main thread should not be interrupted at the end of a test", Thread.interrupted());

		// Wait for any outstanding jobs to finish. Protect against deadlock by
		// terminating the wait after a timeout.
		boolean timedOut = waitForJobs(owner, 5, 5000);
		if (timedOut) {
			// We don't expect any extra jobs run during the test: try to cancel them
			log(IStatus.INFO, owner, "Trying to cancel running jobs: " + getRunningOrWaitingJobs(null));
			getRunningOrWaitingJobs(null).forEach(job -> job.cancel());
			waitForJobs(owner, 5, 1000);
		}

		// Ensure that the Thread.interrupted() flag didn't leak.
		Assert.assertFalse("The main thread should not be interrupted at the end of a test", Thread.interrupted());

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		List<ILaunch> launches = Arrays.asList(launchManager.getLaunches());
		// in case some test left a launch, remove it before reporting a failure, so that further tests can work on a clean Debug View state
		for (ILaunch launch : launches) {
			launchManager.removeLaunch(launch);
		}
		Assert.assertEquals("expected no launches after test", Collections.EMPTY_LIST, launches);
	}

	public static void log(int severity, String owner, String message, Throwable... optionalError) {
		message = "[" + owner + "] " + message;
		Throwable error = null;
		if (optionalError != null && optionalError.length > 0) {
			error = optionalError[0];
		}
		Status status = new Status(severity, JavaTestPlugin.getDefault().getBundle().getSymbolicName(), message, error);
		JavaTestPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Process all queued UI events. If called from background thread, does
	 * nothing.
	 */
	public static void runEventLoop() {
		Display display = Display.getCurrent();
		if (display != null) {
			if (!display.isDisposed()) {
				while (display.readAndDispatch()) {
					// Keep pumping events until the queue is empty
				}
			}
		} else {
			long start = System.currentTimeMillis();
			AtomicBoolean stop = new AtomicBoolean();
			Display.getDefault().asyncExec(() -> stop.set(true));
			while (!stop.get() && System.currentTimeMillis() - start < AbstractDebugTest.DEFAULT_TIMEOUT) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	/**
	 * Utility for waiting until the execution of jobs of any family has finished or timeout is reached. If no jobs are running, the method waits
	 * given minimum wait time. While this method is waiting for jobs, UI events are processed.
	 *
	 * @param owner
	 *            name of the caller which will be logged as prefix if the wait times out
	 * @param minTimeMs
	 *            minimum wait time in milliseconds
	 * @param maxTimeMs
	 *            maximum wait time in milliseconds
	 * @return true if the method timed out, false if all the jobs terminated before the timeout
	 */
	public static boolean waitForJobs(String owner, long minTimeMs, long maxTimeMs) {
		return waitForJobs(owner, minTimeMs, maxTimeMs, (Object[]) null);
	}

	/**
	 * Utility for waiting until the execution of jobs of any family has finished or timeout is reached. If no jobs are running, the method waits
	 * given minimum wait time. While this method is waiting for jobs, UI events are processed.
	 *
	 * @param owner
	 *            name of the caller which will be logged as prefix if the wait times out
	 * @param minTimeMs
	 *            minimum wait time in milliseconds
	 * @param maxTimeMs
	 *            maximum wait time in milliseconds
	 * @param excludedFamilies
	 *            optional list of job families to NOT wait for
	 *
	 * @return true if the method timed out, false if all the jobs terminated before the timeout
	 */
	public static boolean waitForJobs(String owner, long minTimeMs, long maxTimeMs, Object... excludedFamilies) {
		if (maxTimeMs < minTimeMs) {
			throw new IllegalArgumentException("Max time is smaller as min time!");
		}
		final long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < minTimeMs) {
			runEventLoop();
			try {
				Thread.sleep(Math.min(10, minTimeMs));
			} catch (InterruptedException e) {
				// Uninterruptable
			}
		}
		while (!Job.getJobManager().isIdle()) {
			runEventLoop();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// Uninterruptable
			}
			List<Job> jobs = getRunningOrWaitingJobs(null, excludedFamilies);
			if (jobs.isEmpty()) {
				// only uninteresting jobs running
				break;
			}

			if (!Collections.disjoint(runningJobs, jobs)) {
				// There is a job which runs already quite some time, don't wait for it to avoid test timeouts
				dumpRunningOrWaitingJobs(owner, jobs);
				return true;
			}

			if (System.currentTimeMillis() - start >= maxTimeMs) {
				dumpRunningOrWaitingJobs(owner, jobs);
				return true;
			}
		}
		runningJobs.clear();
		return false;
	}

	static Set<Job> runningJobs = new LinkedHashSet<>();

	private static void dumpRunningOrWaitingJobs(String owner, List<Job> jobs) {
		String message = "Some job is still running or waiting to run: " + dumpRunningOrWaitingJobs(jobs);
		log(IStatus.ERROR, owner, message);
	}

	private static String dumpRunningOrWaitingJobs(List<Job> jobs) {
		if (jobs.isEmpty()) {
			return "";
		}
		// clear "old" running jobs, we only remember most recent
		runningJobs.clear();
		StringBuilder sb = new StringBuilder();
		for (Job job : jobs) {
			runningJobs.add(job);
			sb.append("\n'").append(job.toString()).append("'/");
			sb.append(job.getClass().getName());
			Thread thread = job.getThread();
			if (thread != null) {
				ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean().getThreadInfo(new long[] { thread.getId() }, true, true);
				if (threadInfos[0] != null) {
					sb.append("\nthread info: ").append(threadInfos[0]);
				}
			}
			sb.append(", ");
		}

		Thread thread = Display.getDefault().getThread();
		ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean().getThreadInfo(new long[] { thread.getId() }, true, true);
		if (threadInfos[0] != null) {
			sb.append("\n").append("UI thread info: ").append(threadInfos[0]);
		}
		return sb.toString();
	}

	public static List<Job> getRunningOrWaitingJobs(Object jobFamily, Object... excludedFamilies) {
		List<Job> running = new ArrayList<>();
		Job[] jobs = Job.getJobManager().find(jobFamily);
		for (Job job : jobs) {
			if (isRunningOrWaitingJob(job) && !belongsToFamilies(job, excludedFamilies)) {
				running.add(job);
			}
		}
		return running;
	}

	private static boolean isRunningOrWaitingJob(Job job) {
		int state = job.getState();
		return (state == Job.RUNNING || state == Job.WAITING);
	}

	private static boolean belongsToFamilies(Job job, Object... excludedFamilies) {
		if (excludedFamilies == null || excludedFamilies.length == 0) {
			return false;
		}
		for (Object family : excludedFamilies) {
			if (job.belongsTo(family)) {
				return true;
			}
		}
		return false;
	}

}
