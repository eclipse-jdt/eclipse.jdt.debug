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
package org.eclipse.jdi.internal.jdwp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the corresponding Java Debug Wire Protocol (JDWP)
 * packet declared by the JDWP specification.
 * 
 */
public class JdwpCommandPacket extends JdwpPacket {
	/** Command Sets. */
	public static final int CSET_VIRTUAL_MACHINE = 1;
	public static final int CSET_REFERENCE_TYPE = 2;
	public static final int CSET_CLASS_TYPE = 3;
	public static final int CSET_ARRAY_TYPE = 4;
	public static final int CSET_INTERFACE_TYPE = 5;
	public static final int CSET_METHOD = 6;
	public static final int CSET_FIELD = 8;
	public static final int CSET_OBJECT_REFERENCE = 9;
	public static final int CSET_STRING_REFERENCE = 10;
	public static final int CSET_THREAD_REFERENCE = 11;
	public static final int CSET_THREAD_GROUP_REFERENCE = 12;
	public static final int CSET_ARRAY_REFERENCE = 13;
	public static final int CSET_CLASS_LOADER_REFERENCE = 14;
	public static final int CSET_EVENT_REQUEST = 15;
	public static final int CSET_STACK_FRAME = 16;
	public static final int CSET_CLASS_OBJECT_REFERENCE = 17;
	public static final int CSET_EVENT = 64;
	public static final int CSET_HOT_CODE_REPLACEMENT = 128;

	/** Commands VirtualMachine. */
	public static final int VM_VERSION = 1 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_CLASSES_BY_SIGNATURE = 2 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_ALL_CLASSES = 3 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_ALL_THREADS = 4 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_TOP_LEVEL_THREAD_GROUPS = 5 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_DISPOSE = 6 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_ID_SIZES = 7 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_SUSPEND = 8 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_RESUME = 9 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_EXIT = 10 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_CREATE_STRING = 11 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_CAPABILITIES = 12 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_CLASS_PATHS = 13 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_DISPOSE_OBJECTS = 14 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_HOLD_EVENTS = 15 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_RELEASE_EVENTS = 16 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_CAPABILITIES_NEW = 17 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_REDEFINE_CLASSES = 18 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_SET_DEFAULT_STRATUM = 19 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_ALL_CLASSES_WITH_GENERIC = 20 + (CSET_VIRTUAL_MACHINE << 8);
	public static final int VM_INSTANCE_COUNTS = 21 + (CSET_VIRTUAL_MACHINE << 8);

	/** Commands ReferenceType. */
	public static final int RT_SIGNATURE = 1 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_CLASS_LOADER = 2 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_MODIFIERS = 3 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_FIELDS = 4 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_METHODS = 5 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_GET_VALUES = 6 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_SOURCE_FILE = 7 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_NESTED_TYPES = 8 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_STATUS = 9 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_INTERFACES = 10 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_CLASS_OBJECT = 11 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_SOURCE_DEBUG_EXTENSION = 12 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_SIGNATURE_WITH_GENERIC = 13 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_FIELDS_WITH_GENERIC = 14 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_METHODS_WITH_GENERIC = 15 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_INSTANCES = 16 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_CLASS_VERSION = 17 + (CSET_REFERENCE_TYPE << 8);
	public static final int RT_CONSTANT_POOL = 18 + (CSET_REFERENCE_TYPE << 8);

	/** Commands ClassType. */
	public static final int CT_SUPERCLASS = 1 + (CSET_CLASS_TYPE << 8);
	public static final int CT_SET_VALUES = 2 + (CSET_CLASS_TYPE << 8);
	public static final int CT_INVOKE_METHOD = 3 + (CSET_CLASS_TYPE << 8);
	public static final int CT_NEW_INSTANCE = 4 + (CSET_CLASS_TYPE << 8);

	/** Commands ArrayType. */
	public static final int AT_NEW_INSTANCE = 1 + (CSET_ARRAY_TYPE << 8);

	/** Commands Method. */
	public static final int M_LINE_TABLE = 1 + (CSET_METHOD << 8);
	public static final int M_VARIABLE_TABLE = 2 + (CSET_METHOD << 8);
	public static final int M_BYTECODES = 3 + (CSET_METHOD << 8);
	public static final int M_OBSOLETE = 4 + (CSET_METHOD << 8);
	public static final int M_VARIABLE_TABLE_WITH_GENERIC = 5 + (CSET_METHOD << 8);

	/** Commands ObjectReference. */
	public static final int OR_REFERENCE_TYPE = 1 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_GET_VALUES = 2 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_SET_VALUES = 3 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_MONITOR_INFO = 5 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_INVOKE_METHOD = 6 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_DISABLE_COLLECTION = 7 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_ENABLE_COLLECTION = 8 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_IS_COLLECTED = 9 + (CSET_OBJECT_REFERENCE << 8);
	public static final int OR_REFERRING_OBJECTS = 10 + (CSET_OBJECT_REFERENCE << 8);

	/** Commands StringReference. */
	public static final int SR_VALUE = 1 + (CSET_STRING_REFERENCE << 8);

	/** Commands ThreadReference. */
	public static final int TR_NAME = 1 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_SUSPEND = 2 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_RESUME = 3 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_STATUS = 4 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_THREAD_GROUP = 5 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_FRAMES = 6 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_FRAME_COUNT = 7 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_OWNED_MONITORS = 8 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_CURRENT_CONTENDED_MONITOR = 9 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_STOP = 10 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_INTERRUPT = 11 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_SUSPEND_COUNT = 12 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_OWNED_MONITOR_STACK_DEPTH = 13 + (CSET_THREAD_REFERENCE << 8);
	public static final int TR_FORCE_EARLY_RETURN = 14 + (CSET_THREAD_REFERENCE << 8);

	/** Commands ThreadGroupReference. */
	public static final int TGR_NAME = 1 + (CSET_THREAD_GROUP_REFERENCE << 8);
	public static final int TGR_PARENT = 2 + (CSET_THREAD_GROUP_REFERENCE << 8);
	public static final int TGR_CHILDREN = 3 + (CSET_THREAD_GROUP_REFERENCE << 8);

	/** Commands ArrayReference. */
	public static final int AR_LENGTH = 1 + (CSET_ARRAY_REFERENCE << 8);
	public static final int AR_GET_VALUES = 2 + (CSET_ARRAY_REFERENCE << 8);
	public static final int AR_SET_VALUES = 3 + (CSET_ARRAY_REFERENCE << 8);

	/** Commands ClassLoaderReference. */
	public static final int CLR_VISIBLE_CLASSES = 1 + (CSET_CLASS_LOADER_REFERENCE << 8);

	/** Commands EventRequest. */
	public static final int ER_SET = 1 + (CSET_EVENT_REQUEST << 8);
	public static final int ER_CLEAR = 2 + (CSET_EVENT_REQUEST << 8);
	public static final int ER_CLEAR_ALL_BREAKPOINTS = 3 + (CSET_EVENT_REQUEST << 8);

	/** Commands StackFrame. */
	public static final int SF_GET_VALUES = 1 + (CSET_STACK_FRAME << 8);
	public static final int SF_SET_VALUES = 2 + (CSET_STACK_FRAME << 8);
	public static final int SF_THIS_OBJECT = 3 + (CSET_STACK_FRAME << 8);
	public static final int SF_POP_FRAME = 4 + (CSET_STACK_FRAME << 8);

	/** Commands ClassObjectReference. */
	public static final int COR_REFLECTED_TYPE = 1 + (CSET_CLASS_OBJECT_REFERENCE << 8);

	/** Commands Event. */
	public static final int E_COMPOSITE = 100 + (CSET_EVENT << 8);

	/** Commands Hot Code Replacement (OTI specific). */
	public static final int HCR_CLASSES_HAVE_CHANGED = 1 + (CSET_HOT_CODE_REPLACEMENT << 8);
	public static final int HCR_GET_CLASS_VERSION = 2 + (CSET_HOT_CODE_REPLACEMENT << 8);
	public static final int HCR_DO_RETURN = 3 + (CSET_HOT_CODE_REPLACEMENT << 8);
	public static final int HCR_REENTER_ON_EXIT = 4 + (CSET_HOT_CODE_REPLACEMENT << 8);
	public static final int HCR_CAPABILITIES = 5 + (CSET_HOT_CODE_REPLACEMENT << 8);

	/** Mapping of command codes to strings. */
	private static Map<Integer, String> fgCommandMap = null;

	/** Next id to be assigned. */
	private static int fgNextId = 1;
	/**
	 * Command, note that this field is 256 * JDWP CommandSet (unsigned) + JDWP
	 * Command.
	 */
	private int fCommand;

	/**
	 * Creates new JdwpCommandPacket.
	 */
	protected JdwpCommandPacket() {
	}

	/**
	 * Creates new JdwpCommandPacket.
	 */
	public JdwpCommandPacket(int command) {
		setCommand(command);
		setId(getNewId());
	}

	/**
	 * @return Returns unique id for command packet.
	 */
	public static synchronized int getNewId() {
		return fgNextId++;
	}

	/**
	 * @return Returns JDWP command set of packet.
	 */
	public byte getCommandSet() {
		return (byte) (fCommand >>> 8);
	}

	/**
	 * @return Returns 256 * JDWP CommandSet (unsigned) + JDWP Command.
	 */
	public int getCommand() {
		return fCommand;
	}

	/**
	 * Assigns command (256 * JDWP CommandSet (unsigned) + JDWP Command)
	 */
	public void setCommand(int command) {
		fCommand = command;
	}

	/**
	 * Reads header fields that are specific for this type of packet.
	 */
	@Override
	protected int readSpecificHeaderFields(byte[] bytes, int index) {
		byte commandSet = bytes[index];
		fCommand = bytes[index + 1] + (commandSet << 8);
		return 2;
	}

	/**
	 * Writes header fields that are specific for this type of packet.
	 */
	@Override
	protected int writeSpecificHeaderFields(byte[] bytes, int index)
			throws IOException {
		bytes[index] = getCommandSet();
		bytes[index + 1] = (byte) fCommand;
		return 2;
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fgCommandMap != null) {
			return;
		}

		Field[] fields = JdwpCommandPacket.class.getDeclaredFields();

		// First get the set names.
		Map<Integer, String> setNames = new HashMap<Integer, String>(fields.length);
		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0)
				continue;

			try {
				String name = field.getName();
				// If it is not a set, continue.
				if (!name.startsWith("CSET_")) {//$NON-NLS-1$
					continue;
				}
				int value = field.getInt(null);
				setNames.put(new Integer(value), removePrefix(name));
			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			} catch (IllegalArgumentException e) {
				// Should not occur.
				// We should take care that all public static final constants
				// in this class are numbers that are convertible to int.
			}
		}

		// Get the commands.
		fgCommandMap = new HashMap<Integer, String>();
		for (Field field : fields) {
			if ((field.getModifiers() & Modifier.PUBLIC) == 0
					|| (field.getModifiers() & Modifier.STATIC) == 0
					|| (field.getModifiers() & Modifier.FINAL) == 0) {
				continue;
			}

			try {
				String name = field.getName();

				// If it is a set, continue.
				if (name.startsWith("CSET_")) { //$NON-NLS-1$
					continue;
				}
				Integer val = (Integer) field.get(null);
				int value = val.intValue();
				int set = value >>> 8;
				String setName = setNames.get(new Integer(set));
				String entryName = setName + " - " + removePrefix(name); //$NON-NLS-1$

				fgCommandMap.put(val, entryName);

			} catch (IllegalAccessException e) {
				// Will not occur for own class.
			}
		}
	}

	/**
	 * @return Returns a map with string representations of error codes.
	 */
	public static Map<Integer, String> commandMap() {
		getConstantMaps();
		return fgCommandMap;
	}

	/**
	 * @return Returns string without XXX_ prefix.
	 */
	public static String removePrefix(String str) {
		int i = str.indexOf('_');
		if (i < 0) {
			return str;
		}
		return str.substring(i + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		buffer.append(getId());
		buffer.append("] "); //$NON-NLS-1$
		switch (getCommand()) {

		/** Commands VirtualMachine. */
		case VM_VERSION:
			buffer.append("VM_VERSION"); //$NON-NLS-1$
			break;
		case VM_CLASSES_BY_SIGNATURE:
			buffer.append("VM_CLASSES_BY_SIGNATURE"); //$NON-NLS-1$
			break;
		case VM_ALL_CLASSES:
			buffer.append("VM_ALL_CLASSES"); //$NON-NLS-1$
			break;
		case VM_ALL_THREADS:
			buffer.append("VM_ALL_THREADS"); //$NON-NLS-1$
			break;
		case VM_TOP_LEVEL_THREAD_GROUPS:
			buffer.append("VM_TOP_LEVEL_THREAD_GROUPS"); //$NON-NLS-1$
			break;
		case VM_DISPOSE:
			buffer.append("VM_DISPOSE"); //$NON-NLS-1$
			break;
		case VM_ID_SIZES:
			buffer.append("VM_ID_SIZES"); //$NON-NLS-1$
			break;
		case VM_SUSPEND:
			buffer.append("VM_SUSPEND"); //$NON-NLS-1$
			break;
		case VM_RESUME:
			buffer.append("VM_RESUME"); //$NON-NLS-1$
			break;
		case VM_EXIT:
			buffer.append("VM_EXIT"); //$NON-NLS-1$
			break;
		case VM_CREATE_STRING:
			buffer.append("VM_CREATE_STRING"); //$NON-NLS-1$
			break;
		case VM_CAPABILITIES:
			buffer.append("VM_CAPABILITIES"); //$NON-NLS-1$
			break;
		case VM_CLASS_PATHS:
			buffer.append("VM_CLASS_PATHS"); //$NON-NLS-1$
			break;
		case VM_DISPOSE_OBJECTS:
			buffer.append("VM_DISPOSE_OBJECTS"); //$NON-NLS-1$
			break;
		case VM_HOLD_EVENTS:
			buffer.append("VM_HOLD_EVENTS"); //$NON-NLS-1$
			break;
		case VM_RELEASE_EVENTS:
			buffer.append("VM_RELEASE_EVENTS"); //$NON-NLS-1$
			break;
		case VM_CAPABILITIES_NEW:
			buffer.append("VM_CAPABILITIES_NEW"); //$NON-NLS-1$
			break;
		case VM_REDEFINE_CLASSES:
			buffer.append("VM_REDEFINE_CLASSES"); //$NON-NLS-1$
			break;
		case VM_SET_DEFAULT_STRATUM:
			buffer.append("VM_SET_DEFAULT_STRATUM"); //$NON-NLS-1$
			break;
		case VM_ALL_CLASSES_WITH_GENERIC:
			buffer.append("VM_ALL_CLASSES_WITH_GENERIC"); //$NON-NLS-1$
			break;

		/** Commands ReferenceType. */
		case RT_SIGNATURE:
			buffer.append("RT_SIGNATURE"); //$NON-NLS-1$
			break;
		case RT_CLASS_LOADER:
			buffer.append("RT_CLASS_LOADER"); //$NON-NLS-1$
			break;
		case RT_MODIFIERS:
			buffer.append("RT_MODIFIERS"); //$NON-NLS-1$
			break;
		case RT_FIELDS:
			buffer.append("RT_FIELDS"); //$NON-NLS-1$
			break;
		case RT_METHODS:
			buffer.append("RT_METHODS"); //$NON-NLS-1$
			break;
		case RT_GET_VALUES:
			buffer.append("RT_GET_VALUES"); //$NON-NLS-1$
			break;
		case RT_SOURCE_FILE:
			buffer.append("RT_SOURCE_FILE"); //$NON-NLS-1$
			break;
		case RT_NESTED_TYPES:
			buffer.append("RT_NESTED_TYPES"); //$NON-NLS-1$
			break;
		case RT_STATUS:
			buffer.append("RT_STATUS"); //$NON-NLS-1$
			break;
		case RT_INTERFACES:
			buffer.append("RT_INTERFACES"); //$NON-NLS-1$
			break;
		case RT_CLASS_OBJECT:
			buffer.append("RT_CLASS_OBJECT"); //$NON-NLS-1$
			break;
		case RT_SOURCE_DEBUG_EXTENSION:
			buffer.append("RT_SOURCE_DEBUG_EXTENSION"); //$NON-NLS-1$
			break;
		case RT_SIGNATURE_WITH_GENERIC:
			buffer.append("RT_SIGNATURE_WITH_GENERIC"); //$NON-NLS-1$
			break;
		case RT_FIELDS_WITH_GENERIC:
			buffer.append("RT_FIELDS_WITH_GENERIC"); //$NON-NLS-1$
			break;
		case RT_METHODS_WITH_GENERIC:
			buffer.append("RT_METHODS_WITH_GENERIC"); //$NON-NLS-1$
			break;

		/** Commands ClassType. */
		case CT_SUPERCLASS:
			buffer.append("CT_SUPERCLASS"); //$NON-NLS-1$
			break;
		case CT_SET_VALUES:
			buffer.append("CT_SET_VALUES"); //$NON-NLS-1$
			break;
		case CT_INVOKE_METHOD:
			buffer.append("CT_INVOKE_METHOD"); //$NON-NLS-1$
			break;
		case CT_NEW_INSTANCE:
			buffer.append("CT_NEW_INSTANCE"); //$NON-NLS-1$
			break;

		/** Commands ArrayType. */
		case AT_NEW_INSTANCE:
			buffer.append("AT_NEW_INSTANCE"); //$NON-NLS-1$
			break;

		/** Commands Method. */
		case M_LINE_TABLE:
			buffer.append("M_LINE_TABLE"); //$NON-NLS-1$
			break;
		case M_VARIABLE_TABLE:
			buffer.append("M_VARIABLE_TABLE"); //$NON-NLS-1$
			break;
		case M_BYTECODES:
			buffer.append("M_BYTECODES"); //$NON-NLS-1$
			break;
		case M_OBSOLETE:
			buffer.append("M_OBSOLETE"); //$NON-NLS-1$
			break;
		case M_VARIABLE_TABLE_WITH_GENERIC:
			buffer.append("M_VARIABLE_TABLE_WITH_GENERIC"); //$NON-NLS-1$
			break;

		/** Commands ObjectReference. */
		case OR_REFERENCE_TYPE:
			buffer.append("OR_REFERENCE_TYPE"); //$NON-NLS-1$
			break;
		case OR_GET_VALUES:
			buffer.append("OR_GET_VALUES"); //$NON-NLS-1$
			break;
		case OR_SET_VALUES:
			buffer.append("OR_SET_VALUES"); //$NON-NLS-1$
			break;
		case OR_MONITOR_INFO:
			buffer.append("OR_MONITOR_INFO"); //$NON-NLS-1$
			break;
		case OR_INVOKE_METHOD:
			buffer.append("OR_INVOKE_METHOD"); //$NON-NLS-1$
			break;
		case OR_DISABLE_COLLECTION:
			buffer.append("OR_DISABLE_COLLECTION"); //$NON-NLS-1$
			break;
		case OR_ENABLE_COLLECTION:
			buffer.append("OR_ENABLE_COLLECTION"); //$NON-NLS-1$
			break;
		case OR_IS_COLLECTED:
			buffer.append("OR_IS_COLLECTED"); //$NON-NLS-1$
			break;

		/** Commands StringReference. */
		case SR_VALUE:
			buffer.append("SR_VALUE"); //$NON-NLS-1$
			break;

		/** Commands ThreadReference. */
		case TR_NAME:
			buffer.append("TR_NAME"); //$NON-NLS-1$
			break;
		case TR_SUSPEND:
			buffer.append("TR_SUSPEND"); //$NON-NLS-1$
			break;
		case TR_RESUME:
			buffer.append("TR_RESUME"); //$NON-NLS-1$
			break;
		case TR_STATUS:
			buffer.append("TR_STATUS"); //$NON-NLS-1$
			break;
		case TR_THREAD_GROUP:
			buffer.append("TR_THREAD_GROUP"); //$NON-NLS-1$
			break;
		case TR_FRAMES:
			buffer.append("TR_FRAMES"); //$NON-NLS-1$
			break;
		case TR_FRAME_COUNT:
			buffer.append("TR_FRAME_COUNT"); //$NON-NLS-1$
			break;
		case TR_OWNED_MONITORS:
			buffer.append("TR_OWNED_MONITORS"); //$NON-NLS-1$
			break;
		case TR_CURRENT_CONTENDED_MONITOR:
			buffer.append("TR_CURRENT_CONTENDED_MONITOR"); //$NON-NLS-1$
			break;
		case TR_STOP:
			buffer.append("TR_STOP"); //$NON-NLS-1$
			break;
		case TR_INTERRUPT:
			buffer.append("TR_INTERRUPT"); //$NON-NLS-1$
			break;
		case TR_SUSPEND_COUNT:
			buffer.append("TR_SUSPEND_COUNT"); //$NON-NLS-1$
			break;

		/** Commands ThreadGroupReference. */
		case TGR_NAME:
			buffer.append("TGR_NAME"); //$NON-NLS-1$
			break;
		case TGR_PARENT:
			buffer.append("TGR_PARENT"); //$NON-NLS-1$
			break;
		case TGR_CHILDREN:
			buffer.append("TGR_CHILDREN"); //$NON-NLS-1$
			break;

		/** Commands ArrayReference. */
		case AR_LENGTH:
			buffer.append("AR_LENGTH"); //$NON-NLS-1$
			break;
		case AR_GET_VALUES:
			buffer.append("AR_GET_VALUES"); //$NON-NLS-1$
			break;
		case AR_SET_VALUES:
			buffer.append("AR_SET_VALUES"); //$NON-NLS-1$
			break;

		/** Commands ClassLoaderReference. */
		case CLR_VISIBLE_CLASSES:
			buffer.append("CLR_VISIBLE_CLASSES"); //$NON-NLS-1$
			break;

		/** Commands EventRequest. */
		case ER_SET:
			buffer.append("ER_SET"); //$NON-NLS-1$
			break;
		case ER_CLEAR:
			buffer.append("ER_CLEAR"); //$NON-NLS-1$
			break;
		case ER_CLEAR_ALL_BREAKPOINTS:
			buffer.append("ER_CLEAR_ALL_BREAKPOINTS"); //$NON-NLS-1$
			break;

		/** Commands StackFrame. */
		case SF_GET_VALUES:
			buffer.append("SF_GET_VALUES"); //$NON-NLS-1$
			break;
		case SF_SET_VALUES:
			buffer.append("SF_SET_VALUES"); //$NON-NLS-1$
			break;
		case SF_THIS_OBJECT:
			buffer.append("SF_THIS_OBJECT"); //$NON-NLS-1$
			break;
		case SF_POP_FRAME:
			buffer.append("SF_POP_FRAME"); //$NON-NLS-1$
			break;

		/** Commands ClassObjectReference. */
		case COR_REFLECTED_TYPE:
			buffer.append("COR_REFLECTED_TYPE"); //$NON-NLS-1$
			break;

		/** Commands Event. */
		case E_COMPOSITE:
			buffer.append("E_COMPOSITE"); //$NON-NLS-1$
			break;

		default:
			buffer.append("UNKNOWN COMMAND: "); //$NON-NLS-1$
			buffer.append(getCommand());
			break;
		}
		return buffer.toString();
	}
}
