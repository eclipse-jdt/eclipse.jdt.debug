/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdi.internal.event;

import java.io.IOException;

import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.connect.PacketReceiveManager;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public class EventQueueImpl extends MirrorImpl implements EventQueue {
	/** Flag used to see if a VMDisconnectEvent has already been generated. */
	private boolean genereatedVMDisconnectEvent = false;

	/**
	 * Creates new EventQueueImpl.
	 */
	public EventQueueImpl(VirtualMachineImpl vmImpl) {
		super("EventQueue", vmImpl); //$NON-NLS-1$
	}

	/*
	 * @return Returns next EventSet from Virtual Machine.
	 */
	@Override
	public EventSet remove() throws InterruptedException {
		return remove(PacketReceiveManager.TIMEOUT_INFINITE);
	}

	/*
	 * @return Returns next EventSet from Virtual Machine, returns null if times
	 * out.
	 */
	@Override
	public EventSet remove(long timeout) throws InterruptedException {
		// Return a received EventSet or null if no EventSet is received in
		// time.
		// Note that handledJdwpEventSet() is not don in a 'finally' clause
		// because
		// it must also be done when an 'empty' set is read (i.e. a set composed
		// of internal
		// events only).
		try {
			// We remove elements from event sets that are generated from
			// inside, therefore the set may become empty.
			EventSetImpl set;
			do {
				JdwpCommandPacket packet = getCommandVM(
						JdwpCommandPacket.E_COMPOSITE, timeout);
				initJdwpEventSet(packet);
				set = EventSetImpl.read(this, packet.dataInStream());
				handledJdwpEventSet(null);
			} while (set.isEmpty());
			return set;
		} catch (TimeoutException e) {
			// Timeout in getCommand, JDI spec says return null.
			handledJdwpEventSet(e);
			return null;
		} catch (IOException e) {
			// This means the already received data is invalid.
			handledJdwpEventSet(e);
			defaultIOExceptionHandler(e);
			return null;
		} catch (VMDisconnectedException e) {
			// JDI spec says that a VMDisconnectedException must always be
			// preceeded by a VMDisconnectEvent.
			handledJdwpEventSet(e);
			if (!genereatedVMDisconnectEvent) {
				genereatedVMDisconnectEvent = true;
				return new EventSetImpl(virtualMachineImpl(),
						new VMDisconnectEventImpl(virtualMachineImpl(),
								RequestID.nullID));
			}
			throw e;
		}
	}
}
