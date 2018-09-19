/*******************************************************************************
 *  Copyright (c) 2017 salesforce.com.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     salesforce.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.EventDispatcher.AbstractDispatchJob;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

public class EventDispatcherTest extends AbstractDebugTest {

	private JobChangeAdapter jobListener;
	private Map<AbstractDispatchJob, Object> jobs;

	public EventDispatcherTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		jobs = Collections.synchronizedMap(new IdentityHashMap<AbstractDispatchJob, Object>());
		jobListener = new JobChangeAdapter() {
			@Override
			public void aboutToRun(IJobChangeEvent event) {
				Job job = event.getJob();
				if (job instanceof AbstractDispatchJob) {
					jobs.put((AbstractDispatchJob) job, Boolean.TRUE);
				}
			}
		};
		Job.getJobManager().addJobChangeListener(jobListener);
	}

	@Override
	protected void tearDown() throws Exception {
		if (jobListener != null) {
			Job.getJobManager().removeJobChangeListener(jobListener);
		}
		super.tearDown();
	}

	public void test285130_bulkThreadCreation() throws Exception {
		// the real goal of this test is to validate that rapidly delivered JDI events do not start large number of threads
		// unfortunately there is no direct way to observe startup of the JDI event processing threads
		// as approximation, assert single JDI processing Job was ever run during the test
		// as sanity check, assert expected number of DebugEvent was delivered during the test

		// sanity check: count number of JDIThread thread-create events
		AtomicInteger threadCount = new AtomicInteger();
		IDebugEventSetListener debugListener = events -> {
			for (DebugEvent event : events) {
				if (event.getKind() == DebugEvent.CREATE && event.getSource() instanceof JDIThread) {
					JDIThread thread = (JDIThread) event.getSource();
					try {
						if (thread.getName().startsWith("bulk-")) {
							threadCount.incrementAndGet();
						}
					}
					catch (DebugException e) {
					}
				}
			}
		};
		IJavaThread suspendedThread = null;
		try {
			DebugPlugin.getDefault().addDebugEventListener(debugListener);
			createLineBreakpoint(27, "BulkThreadCreationTest");
			suspendedThread = launchToBreakpoint("BulkThreadCreationTest");
		}
		finally {
			terminateAndRemove(suspendedThread);
			removeAllBreakpoints();
			DebugPlugin.getDefault().removeDebugEventListener(debugListener);
		}

		assertEquals("Unexpected number of JDIThread thread-create events", 1000, threadCount.get());
		assertEquals("Unexpected number of event dispatching jobs: " + jobs.size() + " | " + jobs.keySet(), 0, jobs.size());
	}

	/**
	 * Tests that a conditional breakpoint with an expression that will hit a breakpoint will complete the conditional expression evaluation (bug
	 * 269231) and that we dispatch events for conditional breakpoints in dedicated jobs.
	 *
	 * @throws Exception
	 */
	public void testConditionalExpressionEventDispatching() throws Exception {
		String typeName = "BreakpointListenerTest";
		createConditionalLineBreakpoint(18, typeName, "foo(); return false;", true);
		IJavaLineBreakpoint breakpoint = createLineBreakpoint(20, typeName);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, breakpoint);
			IStackFrame top = thread.getTopStackFrame();
			assertNotNull("Missing top frame", top);
			assertTrue("Thread should be suspended", thread.isSuspended());
			assertEquals("Wrong location", breakpoint.getLineNumber(), top.getLineNumber());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
		// Expect to see two jobs for conditional breakpoint with 1) class prepare and 2) breakpoint hit events
		assertEquals("Unexpected number of event dispatching jobs: " + jobs.size() + " | " + jobs.keySet(), 2, jobs.size());
	}

}