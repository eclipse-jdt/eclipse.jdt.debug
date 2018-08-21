/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jdi.internal.spy;

import com.ibm.icu.text.MessageFormat;

public class JdwpConversation {
	private int fId;
	private JdwpCommandPacket fCommand;
	private JdwpReplyPacket fReply;

	JdwpConversation(int id) {
		fId = id;
	}

	void setCommand(JdwpCommandPacket command) {
		if (fCommand != null) {
			throw new IllegalArgumentException(
					MessageFormat
							.format("Attempt to overwrite command with {0}", new Object[] { command.toString() })); //$NON-NLS-1$
		}
		fCommand = command;
	}

	void setReply(JdwpReplyPacket reply) {
		if (fReply != null) {
			throw new IllegalArgumentException(
					MessageFormat
							.format("Attempt to overwrite reply with {0}", new Object[] { reply.toString() })); //$NON-NLS-1$
		}
		fReply = reply;
	}

	public JdwpCommandPacket getCommand() {
		return fCommand;
	}

	public JdwpReplyPacket getReply() {
		return fReply;
	}

	public int getId() {
		return fId;
	}
}
