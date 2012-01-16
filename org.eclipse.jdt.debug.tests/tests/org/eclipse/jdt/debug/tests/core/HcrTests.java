/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;

/**
 * Tests hot code replace
 */
public class HcrTests extends AbstractDebugTest {
	
	class HCRListener implements IJavaHotCodeReplaceListener {
		
		boolean notified = false;

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#hotCodeReplaceFailed(org.eclipse.jdt.debug.core.IJavaDebugTarget, org.eclipse.debug.core.DebugException)
		 */
		public synchronized void hotCodeReplaceFailed(IJavaDebugTarget target, DebugException exception) {
			notified = true;
			notifyAll();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#hotCodeReplaceSucceeded(org.eclipse.jdt.debug.core.IJavaDebugTarget)
		 */
		public synchronized void hotCodeReplaceSucceeded(IJavaDebugTarget target) {
			notified = true;
			notifyAll();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener#obsoleteMethods(org.eclipse.jdt.debug.core.IJavaDebugTarget)
		 */
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
			cu.commitWorkingCopy(false, null);
			waitForBuild();
		}
	}	

	public void testSimpleHcr() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.HcrClass";
		createLineBreakpoint(39, typeName);		
		
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
				cu.commitWorkingCopy(false, null);
				waitForBuild();
				waiter.waitForEvent();
	
				// should have dropped to frame 'one'
				frame = (IJavaStackFrame)thread.getTopStackFrame();
				assertNotNull("No top stack frame", frame);
				if (!"one".equals(frame.getMethodName())) {
					// terminate & restore, and try again - @see bug 287084
					thread.terminate();
					buffer.setContents(originalContent);
					cu.commitWorkingCopy(false, null);
					throw new TestAgainException("Retest - the correct method name was not present after HCR");
				}
				assertEquals("Should have dropped to method 'one'", "one", frame.getMethodName());
				
				// resume to breakpoint
				createLineBreakpoint(39, typeName);
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
		createLineBreakpoint(39, typeName);		
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
				cu.commitWorkingCopy(false, null);
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
		createLineBreakpoint(39, typeName);		
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
				cu.commitWorkingCopy(false, null);
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
}
