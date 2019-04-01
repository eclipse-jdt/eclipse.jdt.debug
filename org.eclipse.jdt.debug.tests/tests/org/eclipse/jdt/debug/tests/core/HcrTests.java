/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;

/**
 * Tests hot code replace
 */
public class HcrTests extends AbstractDebugTest {

	class HCRListener implements IJavaHotCodeReplaceListener {

		boolean notified = false;
		IJavaDebugTarget target = null;

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#hotCodeReplaceFailed(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.debug.core.DebugException)
		 */
		@Override
		public synchronized void hotCodeReplaceFailed(IJavaDebugTarget target, DebugException exception) {
			notified = true;
			notifyAll();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#hotCodeReplaceSucceeded(org.eclipse.jdt.debug.core.IJavaDebugTarget)
		 */
		@Override
		public synchronized void hotCodeReplaceSucceeded(IJavaDebugTarget target) {
			notified = true;
			this.target = target;
			notifyAll();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#obsoleteMethods(org.eclipse.jdt.debug.core.IJavaDebugTarget)
		 */
		@Override
		public synchronized void obsoleteMethods(IJavaDebugTarget target) {
			notified = true;
			notifyAll();
		}

		/**
		 * Returns whether notified (yet).
		 *
		 * @return whether notified
		 */
		public synchronized boolean wasNotified() {
			return notified;
		}

		/**
		 * Waits for notification and returns whether notified.
		 *
		 * @return
		 */
		public synchronized boolean waitNotification() {
			if (!notified) {
				try {
					wait(AbstractDebugTest.DEFAULT_TIMEOUT);
				} catch (InterruptedException e) {
				}
			}
			return notified;
		}

	}

	public HcrTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 *
	 * Revert the source file after the test.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
		cu = cu.getPrimary();
		if (!cu.isWorkingCopy()) {
			cu = cu.getWorkingCopy(null);
		}
		assertTrue("HcrClass.java does not exist", cu.exists());
		IBuffer buffer = cu.getBuffer();
		String contents = buffer.getContents();
		int index = contents.indexOf("\"Two\"");
		if (index >= 0) {
			String newCode = contents.substring(0, index) + "\"One\"" + contents.substring(index + 5);
			buffer.setContents(newCode);
			cu.commitWorkingCopy(true, null);
			waitForBuild();
		}
		super.tearDown();
	}

	public void testSimpleHcr() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		createLineBreakpoint(42, typeName);

		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {

				// look at the value of 'x' - it should be "One"
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaVariable variable = findVariable(frame, "x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'One'", "One", variable.getValue().getValueString());
				removeAllBreakpoints();
				// now do the HCR
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				String originalContent = buffer.getContents();
				int index = contents.indexOf("\"One\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Two\"" + contents.substring(index + 5);
				buffer.setContents(newCode);

				// save contents
				DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				waiter.waitForEvent();

				// should have dropped to frame 'one'
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull("No top stack frame", frame);
				if (!"one".equals(frame.getMethodName())) {
					// terminate & restore, and try again - @see bug 287084
					thread.terminate();
					buffer.setContents(originalContent);
					cu.commitWorkingCopy(true, null);
					throw new TestAgainException("Retest - the correct method name was not present after HCR");
				}
				assertEquals("Should have dropped to method 'one'", "one", frame.getMethodName());

				// resume to breakpoint
				createLineBreakpoint(42, typeName);
				thread = resume(thread);

				// value of 'x' should now be "Two"
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				variable = findVariable(frame, "x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'Two'", "Two", variable.getValue().getValueString());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests a general (plug-in) listener.
	 *
	 * @throws Exception
	 */
	public void testGeneralHcrListener() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		createLineBreakpoint(42, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				// look at the value of 'x' - it should be "One"
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaVariable variable = findVariable(frame, "x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'One'", "One", variable.getValue().getValueString());
				removeAllBreakpoints();
				// now do the HCR
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"One\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Two\"" + contents.substring(index + 5);
				buffer.setContents(newCode);

				// save contents
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				assertTrue("Listener should have been notified", listener.waitNotification());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests that a target specific listener overrides a generic listener.
	 *
	 * @throws Exception
	 */
	public void testSpecificHcrListener() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		createLineBreakpoint(42, typeName);
		HCRListener listener = new HCRListener();
		HCRListener listener2 = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				target.addHotCodeReplaceListener(listener2);
				// look at the value of 'x' - it should be "One"
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				IJavaVariable variable = findVariable(frame, "x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'One'", "One", variable.getValue().getValueString());
				removeAllBreakpoints();
				// now do the HCR
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"One\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Two\"" + contents.substring(index + 5);
				buffer.setContents(newCode);

				// save contents
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				assertTrue("Specific listener should have been notified", listener2.waitNotification());
				assertFalse("General listener should not have been notified", listener.wasNotified());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in a local type with the same name as the enclosing
	 * method
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRLocalType() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass2";
		createLineBreakpoint(36, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$2Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 36", 36, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass2.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass2.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Local#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Local#run\"" + contents.substring(index + 13);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$2Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 36", 36, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in a local type
	 * @throws Exception
	 *
	 * @since 3.8.100
	 */
	public void testHCRLocalType2() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass2";
		createLineBreakpoint(40, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$2Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 40", 40, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass2.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass2.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Local#run2()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Local#run2\"" + contents.substring(index + 14);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$2Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 40", 40, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in a local type defined in a constructor
	 * @throws Exception
	 *
	 * @since 3.8.100
	 */
	public void testHCRLocalType3() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass2";
		createLineBreakpoint(22, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass2.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass2.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"CLocal#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"CLocal#run\"" + contents.substring(index + 14);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the local type", "org.eclipse.debug.tests.targets.HcrClass2$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in an anonymous type with the same name as the method
	 * where the anonymous type was defined
	 *
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRAnonymousType() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass3";
		createLineBreakpoint(37, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$2",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 37", 37, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass3.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass3.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"TEST_RUN1\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"NEW_CODE\"" + contents.substring(index + 11);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertNotNull("Thread did not suspend after HCR", thread);
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$2",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 37", 37, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in an anonymous type defined in a method
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRAnonymousType2() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass3";
		createLineBreakpoint(47, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$3",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 47", 47, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass3.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass3.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"TEST_RUN2\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"NEW_CODE\"" + contents.substring(index + 11);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$3",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 47", 47, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR in an anonymous type defined in a constructor
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRAnonymousType3() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass3";
		createLineBreakpoint(27, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$1",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 27", 27, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass3.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass3.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"TEST_RUN3\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"NEW_CODE\"" + contents.substring(index + 11);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the anonymous type", "org.eclipse.debug.tests.targets.HcrClass3$1",  frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 27", 27, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on a method called from a constructor
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRConstructor() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass4";
		createLineBreakpoint(24, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We shoud be stopped on line 24", 24, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass4.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass4.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"HcrClass4#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"HcrClass4#run\"" + contents.substring(index + 17);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We shoud be stopped on line 24", 24, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR within a constructor
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRConstructor2() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass4";
		createLineBreakpoint(19, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);
			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the constuctor method", "<init>",  frame.getMethodName());
				assertEquals("We shoud be stopped on line 19", 19, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass4.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass4.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Constructor\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Construct\"" + contents.substring(index + 13);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the constructor method", "<init>",  frame.getMethodName());
				//after HCR constructors re-enter
				assertEquals("We shoud be stopped on line 18", 18, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an inner type method with the same name as the enclosing type
	 * method it was called from
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerType() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass5";
		createLineBreakpoint(26, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 26", 26, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass5.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass5.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"InnerClass#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"InnerClass#run\"" + contents.substring(index + 18);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 26", 26, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an inner type method
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerType2() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass5";
		createLineBreakpoint(30, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run2() method", "run2",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 30", 30, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass5.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass5.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"InnerClass#run2()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"InnerClass#run2\"" + contents.substring(index + 19);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run2() method", "run2",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 30", 30, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on a constructor in an inner type
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerType3() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass5";
		createLineBreakpoint(21, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the <init> method", "<init>",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 21", 21, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass5.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass5.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Constructor\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Construct\"" + contents.substring(index + 13);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the <init> method", "<init>",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5$InnerClass type", "org.eclipse.debug.tests.targets.HcrClass5$InnerClass", frame.getDeclaringTypeName());
				//after HCR constructors re-enter
				assertEquals("We shoud be stopped on line 20", 20, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an enclosing method called from an inner type
	 * method with the same name
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerType4() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass5";
		createLineBreakpoint(36, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5 type", "org.eclipse.debug.tests.targets.HcrClass5", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 36", 36, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass5.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass5.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"LocalHCR#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"LocalHCR#run\"" + contents.substring(index + 16);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5 type", "org.eclipse.debug.tests.targets.HcrClass5", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 36", 36, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an enclosing method called from an inner type
	 * method
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerType5() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass5";
		createLineBreakpoint(40, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the outerrun() method", "outerrun",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5 type", "org.eclipse.debug.tests.targets.HcrClass5", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 40", 40, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass5.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass5.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"LocalHCR#outerrun()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"LocalHCR#outerrun\"" + contents.substring(index +21);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the outerrun method", "outerrun",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass5 type", "org.eclipse.debug.tests.targets.HcrClass5", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 40", 40, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on a local type defined in an inner type
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRLocalInner() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass9";
		createLineBreakpoint(22, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass9$1Local type", "org.eclipse.debug.tests.targets.HcrClass9$Inner$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass9.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass9.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Inner$Local#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Inner$Local#run\"" + contents.substring(index +19);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass9$1$Local type", "org.eclipse.debug.tests.targets.HcrClass9$Inner$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on a local type defined in an anonymous type
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRLocalAnonymous() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass8";
		createLineBreakpoint(27, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass8$1$Local type", "org.eclipse.debug.tests.targets.HcrClass8$1$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 27", 27, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass8.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass8.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Inner$Local#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Inner$Local#run\"" + contents.substring(index +19);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass8$1$Local type", "org.eclipse.debug.tests.targets.HcrClass8$1$1Local", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 27", 27, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an inner type defined in a local type
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRInnerLocal() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass7";
		createLineBreakpoint(22, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass7$1Local$Inner type", "org.eclipse.debug.tests.targets.HcrClass7$1Local$Inner", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass7.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass7.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Local$Inner#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Local$Inner#run\"" + contents.substring(index +19);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass7$1$Local$Inner type", "org.eclipse.debug.tests.targets.HcrClass7$1Local$Inner", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 22", 22, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests HCR on an anonymous type defined in a local type
	 * @throws Exception
	 * @since 3.8.100
	 */
	public void testHCRAnnonymousLocal() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass6";
		createLineBreakpoint(26, typeName);
		HCRListener listener = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread= null;
		try {
			thread= launchToBreakpoint(typeName);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget)thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertEquals("We should be stopped in the run() method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass6$1 type", "org.eclipse.debug.tests.targets.HcrClass6$1Local$1", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 26", 26, frame.getLineNumber());
				ICompilationUnit cu = getCompilationUnit(get14Project(), "src", "org.eclipse.debug.tests.targets", "HcrClass6.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass6.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"Local$Inner#run()\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Local$Inner#run\"" + contents.substring(index +19);
				buffer.setContents(newCode);

				DebugEventWaiter waiter = new DebugEventWaiter(DebugEvent.SUSPEND);
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				thread = (IJavaThread) waiter.waitForEvent();
				assertTrue("Listener should have been notified", listener.waitNotification());
				assertNotNull("HCR should have not failed", listener.target);
				assertTrue("the thread should be suspended again after the HCR", thread.isSuspended());
				frame = (IJavaStackFrame) thread.getTopStackFrame();
				assertEquals("We should be stopped in the run method", "run",  frame.getMethodName());
				assertEquals("We should be stopped in the HcrClass6$1 type", "org.eclipse.debug.tests.targets.HcrClass6$1Local$1", frame.getDeclaringTypeName());
				assertEquals("We shoud be stopped on line 26", 26, frame.getLineNumber());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}

	/**
	 * Tests that HCR is NOT triggered if the code change is happened on an unrelated type, see bug 508524 and 5188
	 */
	public void testNoHcrOnUnrelatedType() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		IJavaProject unrelatedProject = get14Project();
		IJavaProject debuggedProject = get15Project();
		IType type1 = unrelatedProject.findType(typeName);
		IType type2 = debuggedProject.findType(typeName);
		assertNotEquals(type1, type2);

		// Types FQNs are same
		assertEquals(type1.getFullyQualifiedName(), type2.getFullyQualifiedName());

		// Paths are same, except the project part
		assertEquals(type1.getResource().getFullPath().removeFirstSegments(1), type2.getResource().getFullPath().removeFirstSegments(1));

		final int lineNumber = 42;
		IJavaLineBreakpoint bp1 = createLineBreakpoint(type1, lineNumber);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(type2, lineNumber);
		assertNotEquals(bp1, bp2);

		HCRListener listener = new HCRListener();
		HCRListener listener2 = new HCRListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread = null;
		try {
			// We start debugging on the one project but do modifications on the unrelated one!
			thread = launchToBreakpoint(debuggedProject, typeName, typeName + CLONE_SUFFIX, true);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
			if (target.supportsHotCodeReplace()) {
				target.addHotCodeReplaceListener(listener2);
				// look at the value of 'x' - it should be "One"
				IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
				IJavaVariable variable = findVariable(frame, "x");
				assertNotNull("Could not find 'x'", variable);
				assertEquals("value of 'x' should be 'One'", "One", variable.getValue().getValueString());
				removeAllBreakpoints();
				// now modify the source in *unrelated* project => NO HCR should happen, even if type names are same!
				ICompilationUnit cu = getCompilationUnit(unrelatedProject, "src", "org.eclipse.debug.tests.targets", "HcrClass.java");
				cu = cu.getPrimary();
				if (!cu.isWorkingCopy()) {
					cu = cu.getWorkingCopy(null);
				}
				assertTrue("HcrClass.java does not exist", cu.exists());
				IBuffer buffer = cu.getBuffer();
				String contents = buffer.getContents();
				int index = contents.indexOf("\"One\"");
				assertTrue("Could not find code to replace", index > 0);
				String newCode = contents.substring(0, index) + "\"Two\"" + contents.substring(index + 5);
				buffer.setContents(newCode);

				// save contents
				cu.commitWorkingCopy(true, null);
				waitForBuild();
				assertFalse("Specific listener should not have been notified", listener2.waitNotification());
				assertFalse("General listener should not have been notified", listener.wasNotified());
			} else {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
			}
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			JDIDebugModel.removeHotCodeReplaceListener(listener);
		}
	}
}
