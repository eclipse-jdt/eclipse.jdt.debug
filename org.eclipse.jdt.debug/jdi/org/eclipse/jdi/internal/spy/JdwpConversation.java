package org.eclipse.jdi.internal.spy;

import java.text.MessageFormat;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

public class JdwpConversation {
	private int fId;
	private JdwpCommandPacket fCommand;
	private JdwpReplyPacket fReply;

	JdwpConversation(int id) {
		fId = id;
	}

	void setCommand(JdwpCommandPacket command) {
		if (fCommand != null) {
			throw new IllegalArgumentException(MessageFormat.format(TcpIpSpyMessages.getString("JdwpConversation.Attempt_to_overwrite_command_with_{0}_1"), new String[] {command.toString()})); //$NON-NLS-1$
		}
		fCommand = command;
	}

	void setReply(JdwpReplyPacket reply) {
		if (fReply != null) {
			throw new IllegalArgumentException(MessageFormat.format(TcpIpSpyMessages.getString("JdwpConversation.Attempt_to_overwrite_reply_with_{0}_2"), new String[] {reply.toString()})); //$NON-NLS-1$
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
