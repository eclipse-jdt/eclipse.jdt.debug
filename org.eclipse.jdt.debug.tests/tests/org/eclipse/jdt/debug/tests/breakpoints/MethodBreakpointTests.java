/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Tests method breakpoints.
 */
public class MethodBreakpointTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public MethodBreakpointTests(String name) {
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testEntryAndExitBreakpoints() throws Exception {
		String typeName = "DropTests";
		List<IJavaMethodBreakpoint> bps = new ArrayList<>();
		// method 4 - entry
		bps.add(createMethodBreakpoint(typeName, "method4", "()V", true, false));
		// method 1 - exit
		bps.add(createMethodBreakpoint(typeName, "method1", "()V", false, true));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			// onto the next breakpoint
			thread = resume(thread);

			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit exit breakpoint second", bps.get(1), hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the 'stop in main' launching preference
	 * {@link IJavaLaunchConfigurationConstants#ATTR_STOP_IN_MAIN}
	 * @throws Exception
	 */
	public void testStopInMain() throws Exception {
		String typeName = "DropTests";
		ILaunchConfiguration config = getLaunchConfiguration(typeName);
		assertNotNull("Could not find launch config", config);
		ILaunchConfigurationWorkingCopy wc = config.copy("DropTests - Stop in main");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
		config = wc.doSave();

		IJavaThread thread= null;
		try {
			thread= launchAndSuspend(config);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertTrue("Should be in 'main'", frame.getMethodName().equals("main") && frame.isStatic());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests disabled method entry and exit breakpoints
	 * @throws Exception
	 */
	public void testDisabledEntryAndExitBreakpoints() throws Exception {
		String typeName = "DropTests";
		// method 4 - entry
		IBreakpoint bp1 = createMethodBreakpoint(typeName, "method4", "()V", true, false);
		bp1.setEnabled(false);
		// method 1 - exit
		IBreakpoint bp2 = createMethodBreakpoint(typeName, "method1", "()V", false, true);
		bp2.setEnabled(false);

		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a method is NOT hit in an inner class
	 * @throws Exception
	 */
	public void testInnerClassNotHit() throws Exception {
		String typeNamePattern = "A";
		List<IJavaMethodBreakpoint> bps = new ArrayList<>();
		// method b - entry
		bps.add(createMethodBreakpoint(typeNamePattern, "b", "()V", true, false));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeNamePattern);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);


			resumeAndExit(thread);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that a given method IS hit in an inner class
	 * @throws Exception
	 */
	public void testInnerClassesHit() throws Exception {
		String typeNamePattern = "A*";
		List<IJavaMethodBreakpoint> bps = new ArrayList<>();
		// method b - entry
		bps.add(createMethodBreakpoint(typeNamePattern, "b", "()V", true, false));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint("A");
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			thread= resume(thread);
			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			thread= resume(thread);
			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			thread= resume(thread);
			hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the hit count for a method breakpoint suspends when reached
	 * @throws Exception
	 */
	public void testHitCountEntryBreakpoint() throws Exception {
		String typeName = "MethodLoop";
		IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, "calculateSum", "()V", true, false);
		bp.setHitCount(3);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Method entry breakpoint not hit within timeout period", thread);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "sum");
			assertNotNull("Could not find variable 'sum'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'sum' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'sum' should be '3', but was " + iValue, iValue == 3);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that the a method exit breakpoint suspends when its hit count is reached
	 * @throws Exception
	 */
	public void testHitCountExitBreakpoint() throws Exception {
		String typeName = "MethodLoop";
		IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, "calculateSum", "()V", false, true);
		bp.setHitCount(3);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Method exit breakpoint not hit within timeout period", thread);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
			IVariable var = findVariable(frame, "sum");
			assertNotNull("Could not find variable 'sum'", var);

			IJavaPrimitiveValue value = (IJavaPrimitiveValue)var.getValue();
			assertNotNull("variable 'sum' has no value", value);
			int iValue = value.getIntValue();
			assertTrue("value of 'sum' should be '6', but was " + iValue, iValue == 6);

			bp.delete();
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests an inclusive thread filter on a method breakpoint
	 * @throws Exception
	 */
	public void testThreadFilterInclusive() throws Exception {
		String typeName = "MethodLoop";
		IJavaMethodBreakpoint methodBp = createMethodBreakpoint(typeName, "calculateSum", "()V", true, false);
		createLineBreakpoint(18, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName, false);
			assertNotNull("breakpoint not hit within timeout period", thread);

			IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();

			// set a thread filter (to the main thread)
			methodBp.setThreadFilter(thread);

			thread = resume(thread);
			assertNotNull("breakpoint not hit", thread);

			frame = (IJavaStackFrame)thread.getTopStackFrame();
			assertEquals("should be in 'calucateSum'", "calculateSum", frame.getMethodName());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests an exclusive thread filter on a method breakpoint
	 * @throws Exception
	 */
	public void testThreadFilterExclusive() throws Exception {
		String typeName = "MethodLoop";
		IJavaMethodBreakpoint methodBp = createMethodBreakpoint(typeName, "calculateSum", "()V", true, false);
		createLineBreakpoint(18, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName, false);
			assertNotNull("breakpoint not hit within timeout period", thread);

			thread.getTopStackFrame();

			// set a thread filter (*not* the main thread)
			IThread[] threads = thread.getDebugTarget().getThreads();
			for (int i = 0; i < threads.length; i++) {
				IThread thread2 = threads[i];
				if (!thread2.equals(thread)) {
					methodBp.setThreadFilter((IJavaThread)thread2);
					break;
				}
			}
			assertNotNull("Did not set thread filter",methodBp.getThreadFilter((IJavaDebugTarget)thread.getDebugTarget()));

			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test for bug 33551
	 * Tests that a method breakpoint is hit properly in the default package
	 * @throws Exception
	 */
	public void testEntryDefaultPackageReturnType() throws Exception {
		String typeName = "DefPkgReturnType";
		IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, "self", "()LDefPkgReturnType;", true, false);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint", bp,hit);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Test for bug 43611
	 * Tests that the debug model presentation is returning the correct signature for a specific method breakpoint
	 * @throws Exception
	 */
	public void testLabelWithoutSignature() throws Exception {
		IDebugModelPresentation modelPresentation = DebugUITools.newDebugModelPresentation();
		try {
			String typeName = "DefPkgReturnType";
			IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, "self", null, true, false);
			String label = modelPresentation.getText(bp);
			assertTrue(label.indexOf("self") >= 0);
		} finally {
			removeAllBreakpoints();
			modelPresentation.dispose();
		}
	}

	/**
	 * Test for bug 43611
	 * Tests that the debug model presentation handles a label with no name for a specific method breakpoint
	 * @throws Exception
	 */
	public void testLabelWithoutMethodName() throws Exception {
		IDebugModelPresentation modelPresentation = DebugUITools.newDebugModelPresentation();
		try {
			String typeName = "DefPkgReturnType";
			IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, null, "()LDefPkgReturnType;", true, false);
			String label = modelPresentation.getText(bp);
			assertTrue(label.indexOf(typeName) >= 0);
		} finally {
			removeAllBreakpoints();
			modelPresentation.dispose();
		}
	}

	/**
	 * Test for bug 43611
	 * Tests that the debug model presentation handles no signature or method name for a specific
	 * method breakpoint
	 * @throws Exception
	 */
	public void testLabelWithoutSigOrMethodName() throws Exception {
		IDebugModelPresentation modelPresentation = DebugUITools.newDebugModelPresentation();
		try {
			String typeName = "DefPkgReturnType";
			IJavaMethodBreakpoint bp = createMethodBreakpoint(typeName, null, null, true, false);
			String label = modelPresentation.getText(bp);
			assertTrue(label.indexOf(typeName) >= 0);
		} finally {
			removeAllBreakpoints();
			modelPresentation.dispose();
		}
	}

	/**
	 * Tests that a specific method breakpoint is skipped when set to do so
	 * @throws Exception
	 */
	public void testSkipMethodBreakpoint() throws Exception {
		String typeName = "DropTests";
		List<IJavaMethodBreakpoint> bps = new ArrayList<>();
		// method 4 - entry
		bps.add(createMethodBreakpoint(typeName, "method4", "()V", true, false));
		// method 1 - exit
		bps.add(createMethodBreakpoint(typeName, "method1", "()V", false, true));


		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IBreakpoint hit = getBreakpoint(thread);
			assertNotNull("suspended, but not by breakpoint", hit);
			assertEquals("should hit entry breakpoint first", bps.get(0),hit);

			getBreakpointManager().setEnabled(false);
			resumeAndExit(thread);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			getBreakpointManager().setEnabled(true);
		}
	}
}
