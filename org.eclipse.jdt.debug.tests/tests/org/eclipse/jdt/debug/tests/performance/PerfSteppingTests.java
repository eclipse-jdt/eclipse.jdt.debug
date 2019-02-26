/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.performance;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugPerformanceTest;

/**
 * Tests performance of stepping.
 */
public class PerfSteppingTests extends AbstractDebugPerformanceTest {

	/**
	 * Constructor
	 * @param name
	 */
	public PerfSteppingTests(String name) {
		super(name);
	}

	class MyFilter implements IDebugEventFilter {

		private IJavaThread fThread = null;
		private Object fLock;
		private DebugEvent[] EMPTY = new DebugEvent[0];

		/**
		 * Constructor
		 * @param thread
		 * @param lock
		 */
		public MyFilter(IJavaThread thread, Object lock) {
			fThread = thread;
			fLock = lock;
		}

		/**
		 * @see org.eclipse.debug.core.IDebugEventFilter#filterDebugEvents(org.eclipse.debug.core.DebugEvent[])
		 */
		@Override
		public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if (event.getSource() == fThread) {
					if (event.getKind() == DebugEvent.SUSPEND && event.getDetail() == DebugEvent.STEP_END) {
						synchronized (fLock) {
							fLock.notifyAll();
						}
					}
					return EMPTY;
				}
			}
			return events;
		}

		/**
		 * performs a step operation
		 */
		public void step() {
			synchronized (fLock) {
				try {
					fThread.stepOver();
				} catch (DebugException e) {
					assertTrue(e.getMessage(), false);
				}
				try {
					fLock.wait();
				} catch (InterruptedException e) {
					assertTrue(e.getMessage(), false);
				}
			}

		}

	}

	/**
	 * Tests stepping over without taking into account event processing in the UI.
	 *
	 * @throws Exception
	 */
	public void testBareStepOver() throws Exception {
		String typeName = "PerfLoop";
		createLineBreakpoint(23, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName, false);

			// warm up
			Object lock = new Object();
			MyFilter filter = new MyFilter(thread, lock);
			DebugPlugin.getDefault().addDebugEventFilter(filter);
			thread.getTopStackFrame();
			for (int n= 0; n < 10; n++) {
				for (int i = 0; i < 100; i++) {
					filter.step();
				}
			}
			DebugPlugin.getDefault().removeDebugEventFilter(filter);

			// real test
			lock = new Object();
			filter = new MyFilter(thread, lock);
			DebugPlugin.getDefault().addDebugEventFilter(filter);

			thread.getTopStackFrame();
			for (int n= 0; n < 150; n++) {
				startMeasuring();
				for (int i = 0; i < 500; i++) {
					filter.step();
				}
				stopMeasuring();
				System.gc();
			}
			commitMeasurements();
			assertPerformance();

			DebugPlugin.getDefault().removeDebugEventFilter(filter);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
