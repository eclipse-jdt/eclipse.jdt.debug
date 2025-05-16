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
package org.eclipse.jdi.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdi.internal.connect.PacketReceiveManager;
import org.eclipse.jdi.internal.connect.PacketSendManager;
import org.eclipse.jdi.internal.event.EventQueueImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
import org.eclipse.jdi.internal.jdwp.JdwpReferenceTypeID;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdi.internal.request.EventRequestManagerImpl;
import org.eclipse.osgi.util.NLS;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;
import com.sun.jdi.connect.spi.Connection;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.request.EventRequestManager;

/**
 * This class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 */
public class VirtualMachineImpl extends MirrorImpl implements VirtualMachine,
		org.eclipse.jdi.hcr.VirtualMachine, org.eclipse.jdi.VirtualMachine {
	/** Result flags for Classes Have Changed command. */
	public static final byte HCR_RELOAD_SUCCESS = 0;
	public static final byte HCR_RELOAD_FAILURE = 1;
	public static final byte HCR_RELOAD_IGNORED = 2;

	/* Indexes in HCR capabilities list. */
	private static final int HCR_CAN_RELOAD_CLASSES = 0;
	private static final int HCR_CAN_GET_CLASS_VERSION = 1;
	private static final int HCR_CAN_DO_RETURN = 2;
	private static final int HCR_CAN_REENTER_ON_EXIT = 3;

	protected static final String JAVA_STRATUM_NAME = "Java"; //$NON-NLS-1$

	/** Timeout value for requests to VM if not overridden for a particular VM. */
	private int fRequestTimeout;
	/** Mapping of command codes to strings. */

	private static Map<Integer, String> fgHCRResultMap = null;

	/** EventRequestManager that creates event objects on request. */
	private final EventRequestManagerImpl fEventReqMgr;
	/** EventQueue that returns EventSets from the Virtual Manager. */
	private final EventQueueImpl fEventQueue;

	/** If a launching connector is used, we store the process. */
	private Process fLaunchedProcess;

	/**
	 * The following field contains cached Mirrors. Note that these are
	 * optional: their only purpose is to speed up the debugger by being able to
	 * use the stored results of JDWP calls.
	 */
	private final ValueCache fCachedReftypes = new ValueCache();
	private final ValueCache fCachedObjects = new ValueCache();

	/** The following are the stored results of JDWP calls. */
	private String fVersionDescription = null; // Text information on the VM
												// version.
	private int fJdwpMajorVersion;
	private int fJdwpMinorVersion;
	private String fVMVersion; // Target VM JRE version, as in the java.version
								// property.
	private String fVMName; // Target VM name, as in the java.vm.name property.
	private boolean fGotIDSizes = false;
	private int fFieldIDSize;
	private int fMethodIDSize;
	private int fObjectIDSize;
	private int fReferenceTypeIDSize;
	private int fFrameIDSize;

	private boolean fGotCapabilities = false;
	private boolean fCanWatchFieldModification;
	private boolean fCanWatchFieldAccess;
	private boolean fCanGetBytecodes;
	private boolean fCanGetSyntheticAttribute;
	private boolean fCanGetOwnedMonitorInfo;
	private boolean fCanGetCurrentContendedMonitor;
	private boolean fCanGetMonitorInfo;
	private boolean fCanRedefineClasses;
	private boolean fCanAddMethod;
	private boolean fCanUnrestrictedlyRedefineClasses;
	private boolean fCanPopFrames;
	private boolean fCanUseInstanceFilters;
	private boolean fCanGetSourceDebugExtension;
	private boolean fCanRequestVMDeathEvent;
	private boolean fCanSetDefaultStratum;
	private boolean fCanGetInstanceInfo;
	private boolean fCanGetConstantPool;
	private boolean fCanUseSourceNameFilters;
	private boolean fCanForceEarlyReturn;
	private boolean fCanRequestMonitorEvents;
	private boolean fCanGetMonitorFrameInfo;
	private boolean[] fHcrCapabilities = null;

	/*
	 * singletons for primitive types
	 */
	private BooleanTypeImpl fBooleanType;
	private ByteTypeImpl fByteType;
	private CharTypeImpl fCharType;
	private DoubleTypeImpl fDoubleType;
	private FloatTypeImpl fFloatType;
	private IntegerTypeImpl fIntegerType;
	private LongTypeImpl fLongType;
	private ShortTypeImpl fShortType;

	/**
	 * Disconnected flag
	 */
	private boolean fIsDisconnected = false;

	/**
	 * The name of the current default stratum.
	 */
	private String fDefaultStratum;
	private final PacketReceiveManager fPacketReceiveManager;
	private final PacketSendManager fPacketSendManager;

	/**
	 * Creates a new Virtual Machine.
	 */
	public VirtualMachineImpl(Connection connection) {
		super("VirtualMachine"); //$NON-NLS-1$
		fEventReqMgr = new EventRequestManagerImpl(this);
		fEventQueue = new EventQueueImpl(this);
		fRequestTimeout = ((VirtualMachineManagerImpl) Bootstrap
				.virtualMachineManager()).getGlobalRequestTimeout();

		fPacketReceiveManager = new PacketReceiveManager(connection, this);
		Thread receiveThread = new Thread(fPacketReceiveManager,
				JDIMessages.VirtualMachineImpl_0);
		receiveThread.setDaemon(true);
		fPacketReceiveManager.setPartnerThread(receiveThread);
		receiveThread.start();

		fPacketSendManager = new PacketSendManager(connection);
		Thread sendThread = new Thread(fPacketSendManager,
				JDIMessages.VirtualMachineImpl_1);
		sendThread.setDaemon(true);
		fPacketReceiveManager.setPartnerThread(sendThread);
		sendThread.start();
	}

	/**
	 * @return Returns size of JDWP ID.
	 */
	public final int fieldIDSize() {
		return fFieldIDSize;
	}

	/**
	 * @return Returns size of JDWP ID.
	 */
	public final int methodIDSize() {
		return fMethodIDSize;
	}

	/**
	 * @return Returns size of JDWP ID.
	 */
	public final int objectIDSize() {
		return fObjectIDSize;
	}

	/**
	 * @return Returns size of JDWP ID.
	 */
	public final int referenceTypeIDSize() {
		return fReferenceTypeIDSize;
	}

	/**
	 * @return Returns size of JDWP ID.
	 */
	public final int frameIDSize() {
		return fFrameIDSize;
	}

	/**
	 * @return Returns cached mirror object, or null if method is not in cache.
	 */
	public ReferenceTypeImpl getCachedMirror(JdwpReferenceTypeID ID) {
		return (ReferenceTypeImpl) fCachedReftypes.get(ID);
	}

	/**
	 * @return Returns cached mirror object, or null if method is not in cache.
	 */
	public ObjectReferenceImpl getCachedMirror(JdwpObjectID ID) {
		return (ObjectReferenceImpl) fCachedObjects.get(ID);
	}

	/**
	 * Adds mirror object to cache.
	 */
	public void addCachedMirror(ReferenceTypeImpl mirror) {
		fCachedReftypes.put(mirror.getRefTypeID(), mirror);
		// TBD: It is now yet possible to only ask for unload events for
		// classes that we know of due to a limitation in the J9 VM.
		// eventRequestManagerImpl().enableInternalClasUnloadEvent(mirror);
	}

	/**
	 * Adds mirror object to cache.
	 */
	public void addCachedMirror(ObjectReferenceImpl mirror) {
		fCachedObjects.put(mirror.getObjectID(), mirror);
	}

	/**
	 * Flushes all stored Jdwp results.
	 */
	public void flushStoredJdwpResults() {
		// All known classes also become invalid.
		Iterator<Object> iter = fCachedReftypes.values().iterator();
		while (iter.hasNext()) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl) iter.next();
			refType.flushStoredJdwpResults();
		}

		fVersionDescription = null;
		fGotIDSizes = false;
		fHcrCapabilities = null;
	}

	/*
	 * Removes a known class. A class/interface is known if we have ever
	 * received its ReferenceTypeID and we have not received an unload event for
	 * it.
	 */
	public final void removeKnownRefType(String signature) {
		List<ReferenceType> refTypeList = classesBySignature(signature);
		if (refTypeList.isEmpty()) {
			return;
		}

		// If we have only one known class for this signature, we known that
		// this is the class
		// to be removed.
		if (refTypeList.size() == 1) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl) refTypeList.get(0);
			refType.flushStoredJdwpResults();
			fCachedReftypes.remove(refType.getRefTypeID());
			return;
		}

		// We have more than one known class for the signature, let's find the
		// unloaded one(s).
		Iterator<ReferenceType> iter = refTypeList.iterator();
		while (iter.hasNext()) {
			ReferenceTypeImpl refType = (ReferenceTypeImpl) iter.next();
			boolean prepared = false;
			try {
				prepared = refType.isPrepared();
			} catch (ObjectCollectedException exception) {
				// The type is unloaded. Fall through
			}
			if (!prepared) {
				refType.flushStoredJdwpResults();
				iter.remove();
				fCachedReftypes.remove(refType.getRefTypeID());
			}
		}
	}

	/*
	 * @exception Throws UnsupportedOperationException if VM does not support J9
	 * HCR.
	 */
	public void checkHCRSupported() throws UnsupportedOperationException {
		if (!isHCRSupported()) {
			throw new UnsupportedOperationException(
					NLS.bind(JDIMessages.VirtualMachineImpl_Target_VM__0__does_not_support_Hot_Code_Replacement_1, name()));
		}
	}

	/*
	 * Returns whether J9 HCR is supported
	 */
	public boolean isHCRSupported() throws UnsupportedOperationException {
		return name().equals("j9"); //$NON-NLS-1$
	}

	/*
	 * @return Returns Manager for receiving packets from the Virtual Machine.
	 */
	public final PacketReceiveManager packetReceiveManager() {
		return fPacketReceiveManager;
	}

	/*
	 * @return Returns Manager for sending packets to the Virtual Machine.
	 */
	public final PacketSendManager packetSendManager() {
		/*
		 * Before we send out first bytes to the VM by JDI calls, we need some
		 * initial requests: - Get the sizes of the IDs (fieldID, method ID
		 * etc.) that the VM uses; - Request class prepare and unload events. We
		 * used these to cache classes/interfaces and map their signatures.
		 */
		if (!fGotIDSizes) {
			getIDSizes();
			if (!fGotIDSizes) { // We can't do much without them.
				disconnectVM();
				throw new VMDisconnectedException(
						JDIMessages.VirtualMachineImpl_Failed_to_get_ID_sizes_2);
			}

			// TBD: This call should be moved to addKnownRefType() when it can
			// be made specific
			// for a reference type.
			eventRequestManagerImpl().enableInternalClasUnloadEvent();
		}

		return fPacketSendManager;
	}

	/**
	 * Returns all loaded types (classes, interfaces, and array types). For each
	 * loaded type in the target VM a ReferenceType will be placed in the
	 * returned list.
	 */
	@Override
	public List<ReferenceType> allClasses() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			boolean withGenericSignature = virtualMachineImpl()
					.isJdwpVersionGreaterOrEqual(1, 5);
			int jdwpCommand = withGenericSignature ? JdwpCommandPacket.VM_ALL_CLASSES_WITH_GENERIC
					: JdwpCommandPacket.VM_ALL_CLASSES;
			JdwpReplyPacket replyPacket = requestVM(jdwpCommand);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<ReferenceType> elements = new ArrayList<>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				ReferenceTypeImpl elt = ReferenceTypeImpl
						.readWithTypeTagAndSignature(this,
								withGenericSignature, replyData);
				if (elt == null) {
					continue;
				}
				readInt("status", ReferenceTypeImpl.classStatusStrings(), replyData); //$NON-NLS-1$
				elements.add(elt);
			}
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}

	}

	/**
	 * @return Returns an iterator over all loaded classes.
	 */
	protected final Iterator<ReferenceType> allRefTypes() {
		return allClasses().iterator();
	}

	/**
	 * @return Returns an iterator over all cached classes.
	 */
	protected final Iterator<Object> allCachedRefTypes() {
		return fCachedReftypes.values().iterator();
	}

	/**
	 * Returns a list of the currently running threads. For each running thread
	 * in the target VM, a ThreadReference that mirrors it is placed in the
	 * list.
	 */
	@Override
	public List<ThreadReference> allThreads() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.VM_ALL_THREADS);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<ThreadReference> elements = new ArrayList<>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				ThreadReferenceImpl elt = ThreadReferenceImpl.read(this,
						replyData);
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Retrieve this VM's capabilities.
	 */
	public void getCapabilities() {
		if (fGotCapabilities) {
			return;
		}

		int command = JdwpCommandPacket.VM_CAPABILITIES;
		if (isJdwpVersionGreaterOrEqual(1, 4)) {
			command = JdwpCommandPacket.VM_CAPABILITIES_NEW;
		}

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(command);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			fCanWatchFieldModification = readBoolean(
					"watch field modification", replyData); //$NON-NLS-1$
			fCanWatchFieldAccess = readBoolean("watch field access", replyData); //$NON-NLS-1$
			fCanGetBytecodes = readBoolean("get bytecodes", replyData); //$NON-NLS-1$
			fCanGetSyntheticAttribute = readBoolean("synth. attr", replyData); //$NON-NLS-1$
			fCanGetOwnedMonitorInfo = readBoolean(
					"owned monitor info", replyData); //$NON-NLS-1$
			fCanGetCurrentContendedMonitor = readBoolean(
					"curr. contended monitor", replyData); //$NON-NLS-1$
			fCanGetMonitorInfo = readBoolean("monitor info", replyData); //$NON-NLS-1$
			if (command == JdwpCommandPacket.VM_CAPABILITIES_NEW) {
				// extended capabilities
				fCanRedefineClasses = readBoolean("redefine classes", replyData); //$NON-NLS-1$
				fCanAddMethod = readBoolean("add method", replyData); //$NON-NLS-1$
				fCanUnrestrictedlyRedefineClasses = readBoolean(
						"unrestrictedly redefine classes", replyData); //$NON-NLS-1$
				fCanPopFrames = readBoolean("pop frames", replyData); //$NON-NLS-1$
				fCanUseInstanceFilters = readBoolean(
						"use instance filters", replyData); //$NON-NLS-1$
				fCanGetSourceDebugExtension = readBoolean(
						"get source debug extension", replyData); //$NON-NLS-1$
				fCanRequestVMDeathEvent = readBoolean(
						"request vm death", replyData); //$NON-NLS-1$
				fCanSetDefaultStratum = readBoolean(
						"set default stratum", replyData); //$NON-NLS-1$
				fCanGetInstanceInfo = readBoolean("instance info", replyData); //$NON-NLS-1$
				fCanRequestMonitorEvents = readBoolean(
						"request monitor events", replyData); //$NON-NLS-1$
				fCanGetMonitorFrameInfo = readBoolean(
						"monitor frame info", replyData); //$NON-NLS-1$
				fCanUseSourceNameFilters = readBoolean(
						"source name filters", replyData); //$NON-NLS-1$
				fCanGetConstantPool = readBoolean("constant pool", replyData); //$NON-NLS-1$
				fCanForceEarlyReturn = readBoolean(
						"force early return", replyData); //$NON-NLS-1$
			} else {
				fCanRedefineClasses = false;
				fCanAddMethod = false;
				fCanUnrestrictedlyRedefineClasses = false;
				fCanPopFrames = false;
				fCanUseInstanceFilters = false;
				fCanGetSourceDebugExtension = false;
				fCanRequestVMDeathEvent = false;
				fCanSetDefaultStratum = false;
				fCanGetInstanceInfo = false;
				fCanGetConstantPool = false;
				fCanUseSourceNameFilters = false;
				fCanForceEarlyReturn = false;
				fCanRequestMonitorEvents = false;
				fCanGetMonitorFrameInfo = false;
			}
			fGotCapabilities = true;
		} catch (IOException e) {
			fGotIDSizes = false;
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @see com.sun.jdi.VirtualMachine#canForceEarlyReturn()
	 * @since 3.3
	 */
	@Override
	public boolean canForceEarlyReturn() {
		getCapabilities();
		return fCanForceEarlyReturn;
	}

	/**
	 * @return Returns true if this implementation supports the retrieval of a
	 *         method's bytecodes.
	 */
	@Override
	public boolean canGetBytecodes() {
		getCapabilities();
		return fCanGetBytecodes;
	}

	/**
	 * @return Returns true if this implementation supports the retrieval of the
	 *         monitor for which a thread is currently waiting.
	 */
	@Override
	public boolean canGetCurrentContendedMonitor() {
		getCapabilities();
		return fCanGetCurrentContendedMonitor;
	}

	/**
	 * @see com.sun.jdi.VirtualMachine#canGetInstanceInfo()
	 * @since 3.3
	 */
	@Override
	public boolean canGetInstanceInfo() {
		getCapabilities();
		return fCanGetInstanceInfo;
	}

	/**
	 * @see com.sun.jdi.VirtualMachine#canGetMethodReturnValues()
	 * @since 3.3
	 */
	@Override
	public boolean canGetMethodReturnValues() {
		return isJdwpVersionGreaterOrEqual(1, 6);
	}

	/**
	 * @return Returns true if this implementation supports the retrieval of the
	 *         monitor information for an object.
	 */
	@Override
	public boolean canGetMonitorInfo() {
		getCapabilities();
		return fCanGetMonitorInfo;
	}

	/**
	 * @see com.sun.jdi.VirtualMachine#canGetMonitorFrameInfo()
	 * @since 3.3
	 */
	@Override
	public boolean canGetMonitorFrameInfo() {
		getCapabilities();
		return fCanGetMonitorFrameInfo;
	}

	/**
	 * @return Returns true if this implementation supports the retrieval of the
	 *         monitors owned by a thread.
	 */
	@Override
	public boolean canGetOwnedMonitorInfo() {
		getCapabilities();
		return fCanGetOwnedMonitorInfo;
	}

	/**
	 * @return Returns true if this implementation supports the query of the
	 *         synthetic attribute of a method or field.
	 */
	@Override
	public boolean canGetSyntheticAttribute() {
		getCapabilities();
		return fCanGetSyntheticAttribute;
	}

	/**
	 * @see com.sun.jdi.VirtualMachine#canRequestMonitorEvents()
	 * @since 3.3
	 */
	@Override
	public boolean canRequestMonitorEvents() {
		getCapabilities();
		return fCanRequestMonitorEvents;
	}

	/**
	 * @return Returns true if this implementation supports watchpoints for
	 *         field access.
	 */
	@Override
	public boolean canWatchFieldAccess() {
		getCapabilities();
		return fCanWatchFieldAccess;
	}

	/**
	 * @return Returns true if this implementation supports watchpoints for
	 *         field modification.
	 */
	@Override
	public boolean canWatchFieldModification() {
		getCapabilities();
		return fCanWatchFieldModification;
	}

	/**
	 * @return Returns the loaded reference types that match a given signature.
	 */
	public List<ReferenceType> classesBySignature(String signature) {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeString(signature, "signature", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.VM_CLASSES_BY_SIGNATURE, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
			List<ReferenceType> elements = new ArrayList<>(nrOfElements);
			for (int i = 0; i < nrOfElements; i++) {
				ReferenceTypeImpl elt = ReferenceTypeImpl.readWithTypeTag(this,
						replyData);
				readInt("status", ReferenceTypeImpl.classStatusStrings(), replyData); //$NON-NLS-1$
				if (elt == null) {
					continue;
				}
				elements.add(elt);
			}
			return elements;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#classesByName(java.lang.String)
	 */
	@Override
	public List<ReferenceType> classesByName(String name) {
		String signature = TypeImpl.classNameToSignature(name);
		return classesBySignature(signature);
	}

	/**
	 * Invalidates this virtual machine mirror.
	 */
	@Override
	public void dispose() {
		initJdwpRequest();
		try {
			requestVM(JdwpCommandPacket.VM_DISPOSE);
			disconnectVM();
		} catch (VMDisconnectedException e) {
			// The VM can exit before we receive the reply.
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#eventQueue()
	 */
	@Override
	public EventQueue eventQueue() {
		return fEventQueue;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#eventRequestManager()
	 */
	@Override
	public EventRequestManager eventRequestManager() {
		return fEventReqMgr;
	}

	/**
	 * @return Returns EventRequestManagerImpl that creates all event objects on
	 *         request.
	 */
	public EventRequestManagerImpl eventRequestManagerImpl() {
		return fEventReqMgr;
	}

	/**
	 * Causes the mirrored VM to terminate with the given error code.
	 */
	@Override
	public void exit(int exitCode) {
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeInt(exitCode, "exit code", outData); //$NON-NLS-1$
			requestVM(JdwpCommandPacket.VM_EXIT, outBytes);
			disconnectVM();
		} catch (VMDisconnectedException e) {
			// The VM can exit before we receive the reply.
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(byte)
	 */
	@Override
	public ByteValue mirrorOf(byte value) {
		return new ByteValueImpl(virtualMachineImpl(), Byte.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(char)
	 */
	@Override
	public CharValue mirrorOf(char value) {
		return new CharValueImpl(virtualMachineImpl(), Character.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(double)
	 */
	@Override
	public DoubleValue mirrorOf(double value) {
		return new DoubleValueImpl(virtualMachineImpl(), Double.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(float)
	 */
	@Override
	public FloatValue mirrorOf(float value) {
		return new FloatValueImpl(virtualMachineImpl(), Float.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(int)
	 */
	@Override
	public IntegerValue mirrorOf(int value) {
		return new IntegerValueImpl(virtualMachineImpl(), Integer.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(long)
	 */
	@Override
	public LongValue mirrorOf(long value) {
		return new LongValueImpl(virtualMachineImpl(), Long.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(short)
	 */
	@Override
	public ShortValue mirrorOf(short value) {
		return new ShortValueImpl(virtualMachineImpl(), Short.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(boolean)
	 */
	@Override
	public BooleanValue mirrorOf(boolean value) {
		return new BooleanValueImpl(virtualMachineImpl(),
				Boolean.valueOf(value));
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOf(java.lang.String)
	 */
	@Override
	public StringReference mirrorOf(String value) {
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeString(value, "string value", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.VM_CREATE_STRING, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			StringReference result = StringReferenceImpl.read(this, replyData);
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#mirrorOfVoid()
	 */
	@Override
	public VoidValue mirrorOfVoid() {
		return new VoidValueImpl(this);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#process()
	 */
	@Override
	public Process process() {
		return fLaunchedProcess;
	}

	/**
	 * Sets Process object for this virtual machine if launched by a
	 * LaunchingConnector.
	 */
	public void setLaunchedProcess(Process proc) {
		fLaunchedProcess = proc;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#resume()
	 */
	@Override
	public void resume() {
		initJdwpRequest();
		try {
			resetThreadEventFlags();
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.VM_RESUME);
			defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#setDebugTraceMode(int)
	 */
	@Override
	public void setDebugTraceMode(int traceFlags) {
		// We don't have trace info.
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#suspend()
	 */
	@Override
	public void suspend() {
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.VM_SUSPEND);
			defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#topLevelThreadGroups()
	 */
	@Override
	public List<ThreadGroupReference> topLevelThreadGroups() {
		// Note that this information should not be cached.
		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.VM_TOP_LEVEL_THREAD_GROUPS);
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int nrGroups = readInt("nr of groups", replyData); //$NON-NLS-1$
			ArrayList<ThreadGroupReference> result = new ArrayList<>(nrGroups);
			for (int i = 0; i < nrGroups; i++) {
				ThreadGroupReferenceImpl threadGroup = ThreadGroupReferenceImpl.read(this, replyData);
				result.add(threadGroup);
			}
			return result;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#name()
	 */
	@Override
	public String name() {
		getVersionInfo();
		return fVMName;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#version()
	 */
	@Override
	public String version() {
		getVersionInfo();
		return fVMVersion;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#description()
	 */
	@Override
	public String description() {
		getVersionInfo();
		return fVersionDescription;
	}

	/**
	 * Reset event flags of all threads.
	 */
	private void resetThreadEventFlags() {
		Iterator<ThreadReference> iter = allThreads().iterator();
		ThreadReferenceImpl thread;
		while (iter.hasNext()) {
			thread = (ThreadReferenceImpl) iter.next();
			thread.resetEventFlags();
		}
	}

	/**
	 * Request and fetch ID sizes of Virtual Machine.
	 */
	private void getIDSizes() {
		if (fGotIDSizes) {
			return;
		}

		/*
		 * fGotIDSizes must first be assigned true to prevent an infinite loop
		 * because getIDSizes() calls requestVM which calls packetSendManager.
		 */
		fGotIDSizes = true;

		// We use a different mirror to avoid having verbose output mixed with
		// the initiating command.
		MirrorImpl mirror = new VoidValueImpl(this);

		mirror.initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = mirror
					.requestVM(JdwpCommandPacket.VM_ID_SIZES);
			mirror.defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			fFieldIDSize = mirror.readInt("field ID size", replyData); //$NON-NLS-1$
			fMethodIDSize = mirror.readInt("method ID size", replyData); //$NON-NLS-1$
			fObjectIDSize = mirror.readInt("object ID size", replyData); //$NON-NLS-1$
			fReferenceTypeIDSize = mirror.readInt("refType ID size", replyData); //$NON-NLS-1$
			fFrameIDSize = mirror.readInt("frame ID size", replyData); //$NON-NLS-1$
		} catch (IOException e) {
			fGotIDSizes = false;
			mirror.defaultIOExceptionHandler(e);
		} finally {
			mirror.handledJdwpRequest();
		}
	}

	/**
	 * Retrieves version info of the VM.
	 */
	public void getVersionInfo() {
		if (fVersionDescription != null) {
			return;
		}

		initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.VM_VERSION);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			fVersionDescription = readString("version descr.", replyData); //$NON-NLS-1$
			fJdwpMajorVersion = readInt("major version", replyData); //$NON-NLS-1$
			fJdwpMinorVersion = readInt("minor version", replyData); //$NON-NLS-1$
			fVMVersion = readString("version", replyData); //$NON-NLS-1$
			fVMName = readString("name", replyData); //$NON-NLS-1$

			if ((fVMName != null) && fVMName.equals("KVM")) { //$NON-NLS-1$
				// KVM requires class preparation events in order
				// to resolve things correctly
				eventRequestManagerImpl().enableInternalClassPrepareEvent();
			}

		} catch (IOException e) {
			fVersionDescription = null;
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Retrieves the HCR capabilities of the VM.
	 */
	public void getHCRCapabilities() {
		if (fHcrCapabilities != null) {
			return;
		}
		fHcrCapabilities = new boolean[HCR_CAN_REENTER_ON_EXIT + 1];

		if (isHCRSupported()) {
			initJdwpRequest();
			try {
				JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.HCR_CAPABILITIES);
				defaultReplyErrorHandler(replyPacket.errorCode());
				DataInputStream replyData = replyPacket.dataInStream();

				fHcrCapabilities[HCR_CAN_RELOAD_CLASSES] = readBoolean(
						"reload classes", replyData); //$NON-NLS-1$
				fHcrCapabilities[HCR_CAN_GET_CLASS_VERSION] = readBoolean(
						"get class version", replyData); //$NON-NLS-1$
				fHcrCapabilities[HCR_CAN_DO_RETURN] = readBoolean(
						"do return", replyData); //$NON-NLS-1$
				fHcrCapabilities[HCR_CAN_REENTER_ON_EXIT] = readBoolean(
						"reenter on exit", replyData); //$NON-NLS-1$
			} catch (IOException e) {
				fHcrCapabilities = null;
				defaultIOExceptionHandler(e);
			} finally {
				handledJdwpRequest();
			}
		} else {
			for (int i = 0; i < fHcrCapabilities.length; i++) {
				fHcrCapabilities[i] = false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.VirtualMachine#canReloadClasses()
	 */
	@Override
	public boolean canReloadClasses() {
		getHCRCapabilities();
		return fHcrCapabilities[HCR_CAN_RELOAD_CLASSES];
	}

	/**
	 * @return Returns Whether VM can get the version of a given class file.
	 */
	public boolean canGetClassFileVersion1() {
		getHCRCapabilities();
		return fHcrCapabilities[HCR_CAN_GET_CLASS_VERSION];
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#canGetClassFileVersion()
	 */
	@Override
	public boolean canGetClassFileVersion() {
		return isJdwpVersionGreaterOrEqual(1, 6);
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#canGetConstantPool()
	 */
	@Override
	public boolean canGetConstantPool() {
		getCapabilities();
		return fCanGetConstantPool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.VirtualMachine#canDoReturn()
	 */
	@Override
	public boolean canDoReturn() {
		getHCRCapabilities();
		return fHcrCapabilities[HCR_CAN_DO_RETURN];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.VirtualMachine#canReenterOnExit()
	 */
	@Override
	public boolean canReenterOnExit() {
		getHCRCapabilities();
		return fHcrCapabilities[HCR_CAN_REENTER_ON_EXIT];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.hcr.VirtualMachine#classesHaveChanged(java.lang.String[])
	 */
	@Override
	public int classesHaveChanged(String[] names) {
		checkHCRSupported();
		// We convert the class/interface names to signatures.
		String[] signatures = new String[names.length];

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeInt(names.length, "length", outData); //$NON-NLS-1$
			for (int i = 0; i < names.length; i++) {
				signatures[i] = TypeImpl.classNameToSignature(names[i]);
				writeString(signatures[i], "signature", outData); //$NON-NLS-1$
			}

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.HCR_CLASSES_HAVE_CHANGED, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();

			byte resultFlag = readByte("result", resultHCRMap(), replyData); //$NON-NLS-1$
			switch (resultFlag) {
			case HCR_RELOAD_SUCCESS:
				return RELOAD_SUCCESS;
			case HCR_RELOAD_FAILURE:
				return RELOAD_FAILURE;
			case HCR_RELOAD_IGNORED:
				return RELOAD_IGNORED;
			}
			throw new InternalError(
					JDIMessages.VirtualMachineImpl_Invalid_result_flag_in_Classes_Have_Changed_response___3
							+ resultFlag + JDIMessages.VirtualMachineImpl__4); //
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return 0;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * @return Returns description of Mirror object.
	 */
	@Override
	public String toString() {
		try {
			return name();
		} catch (Exception e) {
			return fDescription;
		}
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgHCRResultMap != null) {
			return;
		}

		Field[] fields = VirtualMachineImpl.class.getDeclaredFields();
		fgHCRResultMap = new HashMap<>();
		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}

			try {
				String name = field.getName();
				if (name.startsWith("HCR_RELOAD_")) { //$NON-NLS-1$
					Integer intValue = Integer.valueOf(field.getInt(null));
					name = name.substring(4);
					fgHCRResultMap.put(intValue, name);
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
	public static Map<Integer, String> resultHCRMap() {
		getConstantMaps();
		return fgHCRResultMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.VirtualMachine#setRequestTimeout(int)
	 */
	@Override
	public void setRequestTimeout(int timeout) {
		fRequestTimeout = timeout;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdi.VirtualMachine#getRequestTimeout()
	 */
	@Override
	public int getRequestTimeout() {
		return fRequestTimeout;
	}

	/**
	 * Returns whether the JDWP version is greater than or equal to the
	 * specified major/minor version numbers.
	 *
	 * @return whether the JDWP version is greater than or equal to the
	 *         specified major/minor version numbers
	 */
	public boolean isJdwpVersionGreaterOrEqual(int major, int minor) {
		getVersionInfo();
		return (fJdwpMajorVersion > major)
				|| (fJdwpMajorVersion == major && fJdwpMinorVersion >= minor);
	}

	@Override
	public void redefineClasses(Map<? extends ReferenceType, byte[]> typesToBytes) {
		if (!canRedefineClasses()) {
			throw new UnsupportedOperationException();
		}

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeInt(typesToBytes.size(), "classes", outData); //$NON-NLS-1$

			Set<? extends ReferenceType> types = typesToBytes.keySet();
			Iterator<? extends ReferenceType> iter = types.iterator();
			while (iter.hasNext()) {
				ReferenceTypeImpl type = (ReferenceTypeImpl) iter.next();
				type.write(this, outData);
				byte[] bytes = typesToBytes.get(type);
				writeInt(bytes.length, "classfile", outData); //$NON-NLS-1$
				for (byte b : bytes) {
					writeByte(b, "classByte", outData); //$NON-NLS-1$
				}
				fCachedReftypes.remove(type.getRefTypeID()); // flush local
																// cache of
																// redefined
																// types
			}

			JdwpReplyPacket reply = requestVM(
					JdwpCommandPacket.VM_REDEFINE_CLASSES, outBytes);
			switch (reply.errorCode()) {
			case JdwpReplyPacket.UNSUPPORTED_VERSION:
				throw new UnsupportedClassVersionError();
			case JdwpReplyPacket.INVALID_CLASS_FORMAT:
				throw new ClassFormatError();
			case JdwpReplyPacket.CIRCULAR_CLASS_DEFINITION:
				throw new ClassCircularityError();
			case JdwpReplyPacket.FAILS_VERIFICATION:
				throw new VerifyError();
			case JdwpReplyPacket.NAMES_DONT_MATCH:
				throw new NoClassDefFoundError();
			case JdwpReplyPacket.ADD_METHOD_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Add_method_not_implemented_1);
			case JdwpReplyPacket.SCHEMA_CHANGE_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Scheme_change_not_implemented_2);
			case JdwpReplyPacket.HIERARCHY_CHANGE_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Hierarchy_change_not_implemented_3);
			case JdwpReplyPacket.DELETE_METHOD_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Delete_method_not_implemented_4);
			case JdwpReplyPacket.CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Class_modifiers_change_not_implemented_5);
			case JdwpReplyPacket.METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.VirtualMachineImpl_Method_modifiers_change_not_implemented_6);
			default:
				defaultReplyErrorHandler(reply.errorCode());
			}
		} catch (IOException ioe) {
			defaultIOExceptionHandler(ioe);
			return;
		} finally {
			handledJdwpRequest();
		}
	}

	/*
	 * @see VirtualMachine#canRedefineClasses()
	 */
	@Override
	public boolean canRedefineClasses() {
		getCapabilities();
		return fCanRedefineClasses;
	}

	/*
	 * @see VirtualMachine#canUseInstanceFilters()
	 */
	@Override
	public boolean canUseInstanceFilters() {
		getCapabilities();
		return fCanUseInstanceFilters;
	}

	/*
	 * @see VirtualMachine#canAddMethod()
	 */
	@Override
	public boolean canAddMethod() {
		getCapabilities();
		return fCanAddMethod;
	}

	/*
	 * @see VirtualMachine#canUnrestrictedlyRedefineClasses()
	 */
	@Override
	public boolean canUnrestrictedlyRedefineClasses() {
		getCapabilities();
		return fCanUnrestrictedlyRedefineClasses;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#canUseSourceNameFilters()
	 */
	@Override
	public boolean canUseSourceNameFilters() {
		getCapabilities();
		return fCanUseSourceNameFilters;
	}

	/*
	 * @see VirtualMachine#canPopFrames()
	 */
	@Override
	public boolean canPopFrames() {
		getCapabilities();
		return fCanPopFrames;
	}

	/*
	 * @see VirtualMachine#canGetSourceDebugExtension()
	 */
	@Override
	public boolean canGetSourceDebugExtension() {
		getCapabilities();
		return fCanGetSourceDebugExtension;
	}

	/*
	 * @see VirtualMachine#canRequestVMDeathEvent()
	 */
	@Override
	public boolean canRequestVMDeathEvent() {
		getCapabilities();
		return fCanRequestVMDeathEvent;
	}

	public boolean canSetDefaultStratum() {
		getCapabilities();
		return fCanSetDefaultStratum;
	}

	/*
	 * @see VirtualMachine#setDefaultStratum(String)
	 */
	@Override
	public void setDefaultStratum(String stratum) {
		fDefaultStratum = stratum;

		if (!canSetDefaultStratum()) {
			// TODO: how to inform the user that the VM doesn't manage
			// setDefaultStartum ?
			return;
		}
		if (stratum == null) {
			stratum = ""; //$NON-NLS-1$
		}
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeString(stratum, "stratum ID", outData); //$NON-NLS-1$

			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.VM_SET_DEFAULT_STRATUM, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());

		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#getDefaultStratum()
	 */
	@Override
	public String getDefaultStratum() {
		return fDefaultStratum;
	}

	/* (non-Javadoc)
	 * @see com.sun.jdi.VirtualMachine#instanceCounts(java.util.List)
	 */
	@Override
	public long[] instanceCounts(List<? extends ReferenceType> refTypes) {
		if (refTypes == null) {
			throw new NullPointerException(JDIMessages.VirtualMachineImpl_2);
		}
		int size = refTypes.size();
		if (size == 0) {
			if (isJdwpVersionGreaterOrEqual(1, 6)) {
				return new long[0];
			}
			throw new UnsupportedOperationException(JDIMessages.ReferenceTypeImpl_27);
		}
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeInt(size, "size", outData); //$NON-NLS-1$
			for (int i = 0; i < size; i++) {
				((ReferenceTypeImpl) refTypes.get(i)).getRefTypeID().write(
						outData);
			}
			JdwpReplyPacket replyPacket = requestVM(
					JdwpCommandPacket.VM_INSTANCE_COUNTS, outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.INVALID_CLASS:
			case JdwpReplyPacket.INVALID_OBJECT:
				throw new ObjectCollectedException(
						JDIMessages.class_or_object_not_known);
			case JdwpReplyPacket.ILLEGAL_ARGUMENT:
				throw new IllegalArgumentException(
						JDIMessages.VirtualMachineImpl_count_less_than_zero);
			case JdwpReplyPacket.NOT_IMPLEMENTED:
				throw new UnsupportedOperationException(
						JDIMessages.ReferenceTypeImpl_27);
			case JdwpReplyPacket.VM_DEAD:
				throw new VMDisconnectedException(JDIMessages.vm_dead);
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			DataInputStream replyData = replyPacket.dataInStream();
			int counts = readInt("counts", replyData); //$NON-NLS-1$
			if (counts != size) {
				throw new InternalError(JDIMessages.VirtualMachineImpl_3);
			}
			long[] ret = new long[counts];
			for (int i = 0; i < counts; i++) {
				ret[i] = readLong("ref count", replyData); //$NON-NLS-1$
			}
			return ret;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
			return null;
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Returns whether this VM is disconnected.
	 *
	 * @return whether this VM is disconnected
	 */
	public boolean isDisconnected() {
		return fIsDisconnected;
	}

	/**
	 * Sets whether this VM is disconnected.
	 *
	 * @param disconnected
	 *            whether this VM is disconnected
	 */
	public synchronized void setDisconnected(boolean disconnected) {
		fIsDisconnected = disconnected;
	}

	/**
	 * Return the boolean type for this VM.
	 */
	protected BooleanTypeImpl getBooleanType() {
		if (fBooleanType == null) {
			fBooleanType = new BooleanTypeImpl(this);
		}
		return fBooleanType;
	}

	/**
	 * Return the byte type for this VM.
	 */
	protected ByteTypeImpl getByteType() {
		if (fByteType == null) {
			fByteType = new ByteTypeImpl(this);
		}
		return fByteType;
	}

	/**
	 * Return the char type for this VM.
	 */
	protected CharTypeImpl getCharType() {
		if (fCharType == null) {
			fCharType = new CharTypeImpl(this);
		}
		return fCharType;
	}

	/**
	 * Return the double type for this VM.
	 */
	protected DoubleTypeImpl getDoubleType() {
		if (fDoubleType == null) {
			fDoubleType = new DoubleTypeImpl(this);
		}
		return fDoubleType;
	}

	/**
	 * Return the float type for this VM.
	 */
	protected FloatTypeImpl getFloatType() {
		if (fFloatType == null) {
			fFloatType = new FloatTypeImpl(this);
		}
		return fFloatType;
	}

	/**
	 * Return the integer type for this VM.
	 */
	protected IntegerTypeImpl getIntegerType() {
		if (fIntegerType == null) {
			fIntegerType = new IntegerTypeImpl(this);
		}
		return fIntegerType;
	}

	/**
	 * Return the long type for this VM.
	 */
	protected LongTypeImpl getLongType() {
		if (fLongType == null) {
			fLongType = new LongTypeImpl(this);
		}
		return fLongType;
	}

	/**
	 * Return the short type for this VM.
	 */
	protected ShortTypeImpl getShortType() {
		if (fShortType == null) {
			fShortType = new ShortTypeImpl(this);
		}
		return fShortType;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.sun.jdi.VirtualMachine#canBeModified()
	 */
	@Override
	public boolean canBeModified() {
		return true;
	}

	public boolean mayCreateVirtualThreads() {
		return isJdwpVersionGreaterOrEqual(19, 0);
	}
}
