/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal.event;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.RequestID;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;

/**
 * This class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public abstract class EventImpl extends MirrorImpl implements Event {
	/** Constants for EventKind. */
	public static final byte EVENT_SINGLE_STEP = 1;
	public static final byte EVENT_BREAKPOINT = 2;
	public static final byte EVENT_FRAME_POP = 3;
	public static final byte EVENT_EXCEPTION = 4;
	public static final byte EVENT_USER_DEFINED = 5;
	public static final byte EVENT_THREAD_START = 6;
	public static final byte EVENT_THREAD_END = 7;
	public static final byte EVENT_CLASS_PREPARE = 8;
	public static final byte EVENT_CLASS_UNLOAD = 9;
	public static final byte EVENT_CLASS_LOAD = 10;
	public static final byte EVENT_FIELD_ACCESS = 20;
	public static final byte EVENT_FIELD_MODIFICATION = 21;
	public static final byte EVENT_EXCEPTION_CATCH = 30;
	public static final byte EVENT_METHOD_ENTRY = 40;
	public static final byte EVENT_METHOD_EXIT = 41;
	public static final byte EVENT_METHOD_EXIT_WITH_RETURN_VALUE = 42;
	public static final byte EVENT_MONITOR_CONTENDED_ENTER = 43;
	public static final byte EVENT_MONITOR_CONTENDED_ENTERED = 44;
	public static final byte EVENT_MONITOR_WAIT = 45;
	public static final byte EVENT_MONITOR_WAITED = 46;
	public static final byte EVENT_VM_INIT = 90;
	public static final byte EVENT_VM_DEATH = 99;
	public static final byte EVENT_VM_DISCONNECTED = 100; // Never sent by
															// across JDWP.
	public static final byte EVENT_VM_START = EVENT_VM_INIT;
	public static final byte EVENT_THREAD_DEATH = EVENT_THREAD_END;

	/** ThreadReference of thread that generated this event. */
	protected ThreadReferenceImpl fThreadRef;

	/** Mapping of command codes to strings. */
	private static HashMap<Integer, String> fEventKindMap = null;

	/** Request ID of event. */
	private RequestID fRequestID;

	/** The EventRequest that requested this event. */
	private EventRequest fRequest;

	/**
	 * Creates new EventImpl, only used by subclasses.
	 */
	protected EventImpl(String description, VirtualMachineImpl vmImpl,
			RequestID requestID) {
		super(description, vmImpl);
		fRequestID = requestID;
	}

	/**
	 * @return Returns ThreadReference of thread that generated this event.
	 */
	public ThreadReference thread() {
		return fThreadRef;
	}

	/**
	 * @return Returns requestID.
	 */
	public RequestID requestID() {
		return fRequestID;
	}

	/**
	 * @return Returns string representation.
	 */
	@Override
	public String toString() {
		return super.toString() + ": " + fRequestID; //$NON-NLS-1$
	}

	/**
	 * @return Creates, reads and returns new EventImpl.
	 */
	public static EventImpl read(MirrorImpl target, DataInputStream dataInStream)
			throws IOException {
		byte eventKind = target.readByte(
				"event kind", eventKindMap(), dataInStream); //$NON-NLS-1$
		RequestID requestID = RequestID.read(target, dataInStream);

		// Create, read and return Event of eventKind.
		EventImpl result;
		switch (eventKind) {
		case 0:
			return null;
		case AccessWatchpointEventImpl.EVENT_KIND:
			result = AccessWatchpointEventImpl.read(target, requestID,
					dataInStream);
			break;
		case BreakpointEventImpl.EVENT_KIND:
			result = BreakpointEventImpl.read(target, requestID, dataInStream);
			break;
		case ClassPrepareEventImpl.EVENT_KIND:
			result = ClassPrepareEventImpl
					.read(target, requestID, dataInStream);
			break;
		case ClassUnloadEventImpl.EVENT_KIND:
			result = ClassUnloadEventImpl.read(target, requestID, dataInStream);
			break;
		case ExceptionEventImpl.EVENT_KIND:
			result = ExceptionEventImpl.read(target, requestID, dataInStream);
			break;
		case MethodEntryEventImpl.EVENT_KIND:
			result = MethodEntryEventImpl.read(target, requestID, dataInStream);
			break;
		case MethodExitEventImpl.EVENT_KIND:
			result = MethodExitEventImpl.read(target, requestID, dataInStream);
			break;
		case EVENT_METHOD_EXIT_WITH_RETURN_VALUE:
			result = MethodExitEventImpl.readWithReturnValue(target, requestID,
					dataInStream);
			break;
		case MonitorContendedEnteredEventImpl.EVENT_KIND:
			result = MonitorContendedEnteredEventImpl.read(target, requestID,
					dataInStream);
			break;
		case MonitorContendedEnterEventImpl.EVENT_KIND:
			result = MonitorContendedEnterEventImpl.read(target, requestID,
					dataInStream);
			break;
		case MonitorWaitedEventImpl.EVENT_KIND:
			result = MonitorWaitedEventImpl.read(target, requestID,
					dataInStream);
			break;
		case MonitorWaitEventImpl.EVENT_KIND:
			result = MonitorWaitEventImpl.read(target, requestID, dataInStream);
			break;
		case ModificationWatchpointEventImpl.EVENT_KIND:
			result = ModificationWatchpointEventImpl.read(target, requestID,
					dataInStream);
			break;
		case StepEventImpl.EVENT_KIND:
			result = StepEventImpl.read(target, requestID, dataInStream);
			break;
		case ThreadDeathEventImpl.EVENT_KIND:
			result = ThreadDeathEventImpl.read(target, requestID, dataInStream);
			break;
		case ThreadStartEventImpl.EVENT_KIND:
			result = ThreadStartEventImpl.read(target, requestID, dataInStream);
			break;
		case VMDeathEventImpl.EVENT_KIND:
			result = VMDeathEventImpl.read(target, requestID, dataInStream);
			break;
		case VMDisconnectEventImpl.EVENT_KIND:
			result = VMDisconnectEventImpl
					.read(target, requestID, dataInStream);
			break;
		case VMStartEventImpl.EVENT_KIND:
			result = VMStartEventImpl.read(target, requestID, dataInStream);
			break;
		default:
			throw new IOException(
					EventMessages.EventImpl_Read_invalid_EventKind___1
							+ eventKind);
		}

		// Find and store original request.
		if (!requestID.isNull())
			result.fRequest = target.virtualMachineImpl().eventRequestManagerImpl().findRequest(result);

		return result;
	}

	/**
	 * @return Returns EventRequest that caused this event to be generated by
	 *         the Virtual Machine.
	 */
	public EventRequest request() {
		return fRequest;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fEventKindMap != null)
			return;

		java.lang.reflect.Field[] fields = EventImpl.class.getDeclaredFields();
		fEventKindMap = new HashMap<Integer, String>();
		for (Field field : fields) {
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;

			try {
				String name = field.getName();
				Integer intValue = new Integer(field.getInt(null));

				if (name.startsWith("EVENT_")) { //$NON-NLS-1$
					name = name.substring(6);
					fEventKindMap.put(intValue, name);
				}
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are numbers that are convertible to int.
			}
		}
	}

	/**
	 * @return Returns a map with string representations of tags.
	 */
	public static Map<Integer, String> eventKindMap() {
		getConstantMaps();
		return fEventKindMap;
	}
}
