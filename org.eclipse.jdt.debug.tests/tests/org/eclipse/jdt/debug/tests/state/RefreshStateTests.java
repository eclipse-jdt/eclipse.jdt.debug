/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.state;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests that state refresh works after modifying the target state
 * with a custom JDWP command.
 *
 * @since 3.6
 */
public class RefreshStateTests extends AbstractDebugTest {

	public RefreshStateTests(String name) {
		super(name);
	}

	/**
	 * Resume a thread behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testThreadHasResumed() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		ILineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// Resume thread (set 11, command 3)
			IJavaObject reference = thread.getThreadObject();
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			outData.writeLong(reference.getUniqueId());
			byte[] reply = jdiTarget.sendCommand((byte)11, (byte)3, outBytes.toByteArray());
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);

			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());

			// model should still be suspended
			assertTrue("Model should be in suspended state", thread.isSuspended());

			// refresh the model, expect a resume event
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, thread);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Thread never sent resume event", source);
			assertEquals("Wrong thread resumed", thread, source);
			// model should now be in running state
			assertFalse("Model should now be running", thread.isSuspended());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Resume all threads behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testAllThreadsResumed() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// Resume each thread (set 11, command 3)
			IThread[] threads = jdiTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				IJavaThread jThread = (IJavaThread) threads[i];
				IJavaObject reference = jThread.getThreadObject();
				ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
				DataOutputStream outData = new DataOutputStream(outBytes);
				outData.writeLong(reference.getUniqueId());
				byte[] reply = jdiTarget.sendCommand((byte)11, (byte)3, outBytes.toByteArray());
				JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);
				assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());
			}

			// model should still be suspended
			assertTrue("Model should be in suspended state", jdiTarget.isSuspended());

			// refresh the model, expect a resume event
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, jdiTarget);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Thread never sent resume event", source);
			// model should now be in running state
			assertFalse("Model should now be running", jdiTarget.isSuspended());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Suspend a thread behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testThreadHasSuspended() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		ILineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// resume the thread to get into running state
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, thread);
			thread.resume();
			waiter.waitForEvent();
			// model should now be in running state
			assertFalse("Model should now be running", thread.isSuspended());

			// suspend the thread with a JDWP command (set 11, command 2)
			IJavaObject reference = thread.getThreadObject();
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			outData.writeLong(reference.getUniqueId());
			byte[] reply = jdiTarget.sendCommand((byte)11, (byte)2, outBytes.toByteArray());
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);

			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());

			// model should still be running
			assertFalse("Model should be in running state", thread.isSuspended());

			// refresh the model, expect a suspend event
			waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Thread never sent suspend event", source);
			assertEquals("Wrong thread suspended", thread, source);

			// model should be suspended now
			assertTrue("Model should be in suspended state", thread.isSuspended());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Suspend all threads behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testAllThreadsSuspended() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		ILineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// resume the thread to get into running state
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, thread);
			thread.resume();
			waiter.waitForEvent();
			// model should now be in running state
			assertFalse("Model should now be running", thread.isSuspended());

			IThread[] threads = jdiTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				IJavaThread jThread = (IJavaThread) threads[i];
				// suspend each thread with a JDWP command (set 11, command 2)
				IJavaObject reference = jThread.getThreadObject();
				ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
				DataOutputStream outData = new DataOutputStream(outBytes);
				outData.writeLong(reference.getUniqueId());
				byte[] reply = jdiTarget.sendCommand((byte)11, (byte)2, outBytes.toByteArray());
				JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);
				assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());
			}


			// model should still be running
			assertFalse("Model should be in running state", jdiTarget.isSuspended());

			// refresh the model, expect a suspend event
			waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, jdiTarget);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Target never sent suspend event", source);

			// model should be suspended now
			assertTrue("Model should be in suspended state", jdiTarget.isSuspended());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Suspend the entire target behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testTargetHasSuspended() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		ILineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// resume the thread to get into running state
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, thread);
			thread.resume();
			waiter.waitForEvent();
			// model should now be in running state
			assertFalse("Model should now be running", thread.isSuspended());

			// suspend the target with a JDWP command (set 1, command 8)
			IJavaObject reference = thread.getThreadObject();
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			outData.writeLong(reference.getUniqueId());
			byte[] reply = jdiTarget.sendCommand((byte)1, (byte)8, outBytes.toByteArray());
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);

			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());

			// model should still be running
			assertFalse("Model should be in running state", jdiTarget.isSuspended());

			// refresh the model, expect a suspend event
			waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, jdiTarget);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Target never sent suspend event", source);

			// model should be suspended now
			assertTrue("Model should be in suspended state", jdiTarget.isSuspended());

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Resume a target behind the scenes and ensure model state updates appropriately.
	 *
	 * @throws CoreException
	 */
	public void testTargetHasResumed() throws Exception {
		String typeName = "org.eclipse.debug.tests.targets.CallLoop";
		IJavaLineBreakpoint bp = createLineBreakpoint(19, "org.eclipse.debug.tests.targets.Looper");
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) thread.getDebugTarget();
			// Resume target (set 1, command 9)
			IJavaObject reference = thread.getThreadObject();
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			outData.writeLong(reference.getUniqueId());
			byte[] reply = jdiTarget.sendCommand((byte)1, (byte)9, outBytes.toByteArray());
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);

			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());

			// model should still be suspended
			assertTrue("Model should be in suspended state", jdiTarget.isSuspended());

			// refresh the model, expect a resume event
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.RESUME, jdiTarget);
			jdiTarget.refreshState();
			Object source = waiter.waitForEvent();
			assertNotNull("Target never sent resume event", source);
			// model should now be in running state
			assertFalse("Model should now be running", jdiTarget.isSuspended());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
}
