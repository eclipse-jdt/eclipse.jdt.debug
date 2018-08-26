/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdi.internal.jdwp.JdwpPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.jdwp.JdwpString;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * Tests raw JDWP commands sent to a target.
 *
 * @since 3.3
 */
public class JDWPTests extends AbstractDebugTest {

	/**
	 * Constructor
	 * @param name
	 */
	public JDWPTests(String name) {
		super(name);
	}

	/**
	 * Sends a request to the VirtualMachine Command Set (byte 1) to ask for
	 * the VM Capabilities (byte 12)
	 * @throws Exception
	 */
	public void testCapabilities() throws Exception {
		String typeName = "Breakpoints";
		ILineBreakpoint bp = createLineBreakpoint(55, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IDebugTarget target = thread.getDebugTarget();
			assertTrue("Wrong target", target instanceof JDIDebugTarget);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) target;
			// VM capabilities
			byte[] reply = jdiTarget.sendCommand((byte)1, (byte)12, null);
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);

			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());
			DataInputStream replyData = packet.dataInStream();
			// should be 7 booleans in reply
			for (int i = 0; i < 7; i++) {
				replyData.readBoolean();
			}
			assertEquals("Should be no available bytes", 0, replyData.available());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	/**
	 * Sends a request to the VirtualMachine Command Set (byte 1) to ask
	 * for ClassesBySignature (byte 2)
	 * @throws Exception
	 */
	public void testClassesBySingature() throws Exception {
		String typeName = "Breakpoints";
		ILineBreakpoint bp = createLineBreakpoint(55, typeName);

		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IDebugTarget target = thread.getDebugTarget();
			assertTrue("Wrong target", target instanceof JDIDebugTarget);
			IJavaDebugTarget jdiTarget = (IJavaDebugTarget) target;
			// VM capabilities
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			JdwpString.write("LBreakpoints;", outData);
			byte[] reply = jdiTarget.sendCommand((byte)1, (byte)2, outBytes.toByteArray());
			JdwpReplyPacket packet = (JdwpReplyPacket) JdwpPacket.build(reply);
			assertEquals("Unexpected error code in reply packet", 0, packet.errorCode());
			DataInputStream replyData = packet.dataInStream();
			// should be 1 type in reply
			assertEquals("Wrong number of types", 1, replyData.readInt());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
