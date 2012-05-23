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
package org.eclipse.jdi.internal.request;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdi.internal.LocationImpl;
import org.eclipse.jdi.internal.MirrorImpl;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdi.internal.ReferenceTypeImpl;
import org.eclipse.jdi.internal.ThreadReferenceImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.event.EventImpl;
import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;

import com.sun.jdi.InternalException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 */
public abstract class EventRequestImpl extends MirrorImpl implements
		EventRequest {
	/** Jdwp constants for StepRequests. */
	public static final byte STEP_SIZE_MIN_JDWP = 0;
	public static final byte STEP_SIZE_LINE_JDWP = 1;
	public static final byte STEP_DEPTH_INTO_JDWP = 0;
	public static final byte STEP_DEPTH_OVER_JDWP = 1;
	public static final byte STEP_DEPTH_OUT_JDWP = 2;
	public static final byte STEP_DEPTH_REENTER_JDWP_HCR = 3; // OTI specific
																// for Hot Code
																// Replacement.

	/** Jdwp constants for SuspendPolicy. */
	public static final byte SUSPENDPOL_NONE_JDWP = 0;
	public static final byte SUSPENDPOL_EVENT_THREAD_JDWP = 1;
	public static final byte SUSPENDPOL_ALL_JDWP = 2;

	/** Constants for ModifierKind. */
	public static final byte MODIF_KIND_COUNT = 1;
	public static final byte MODIF_KIND_CONDITIONAL = 2;
	public static final byte MODIF_KIND_THREADONLY = 3;
	public static final byte MODIF_KIND_CLASSONLY = 4;
	public static final byte MODIF_KIND_CLASSMATCH = 5;
	public static final byte MODIF_KIND_CLASSEXCLUDE = 6;
	public static final byte MODIF_KIND_LOCATIONONLY = 7;
	public static final byte MODIF_KIND_EXCEPTIONONLY = 8;
	public static final byte MODIF_KIND_FIELDONLY = 9;
	public static final byte MODIF_KIND_STEP = 10;
	public static final byte MODIF_KIND_INSTANCE = 11;
	public static final byte MODIF_KIND_SOURCE_NAME_FILTER = 12;

	/** Mapping of command codes to strings. */
	private static HashMap<Integer, String> fStepSizeMap = null;
	private static HashMap<Integer, String> fStepDepthMap = null;
	private static HashMap<Integer, String> fSuspendPolicyMap = null;
	private static HashMap<Integer, String> fModifierKindMap = null;

	/**
	 * Flag that indicates the request was generated from inside of this JDI
	 * implementation.
	 */
	private boolean fGeneratedInside = false;

	/** User property map. */
	private HashMap<Object, Object> fPropertyMap;

	/**
	 * RequestId of EventRequest, assigned by the reply data of the JDWP Event
	 * Reuqest Set command, null if request had not yet been enabled.
	 */
	protected RequestID fRequestID = null;
	/**
	 * Determines the threads to suspend when the requested event occurs in the
	 * target VM.
	 */
	private byte fSuspendPolicy = SUSPEND_ALL; // Default is as specified by JDI
												// spec.

	/**
	 * Modifiers.
	 */
	/** Count filters. */
	protected ArrayList<Integer> fCountFilters;

	/** Thread filters. */
	protected ArrayList<ThreadReference> fThreadFilters = null;

	/** Class filters. */
	protected ArrayList<String> fClassFilters = null;

	/** Class filters. */
	protected ArrayList<ReferenceType> fClassFilterRefs = null;

	/** Class Exclusion filters. */
	protected ArrayList<String> fClassExclusionFilters = null;

	/** Location filters. */
	protected ArrayList<LocationImpl> fLocationFilters = null;

	/** Exception filters. */
	protected ArrayList<ExceptionFilter> fExceptionFilters = null;

	/** Field filters. */
	protected ArrayList<FieldImpl> fFieldFilters = null;

	/** Thread step filters. */
	protected ArrayList<ThreadStepFilter> fThreadStepFilters = null;

	/** Instance filters. */
	protected ArrayList<ObjectReference> fInstanceFilters = null;
	/**
	 * source name filters
	 * 
	 * @since 3.3
	 */
	protected ArrayList<String> fSourceNameFilters = null;

	/**
	 * Creates new EventRequest.
	 */
	protected EventRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}

	/**
	 * @return Returns string representation.
	 */
	@Override
	public String toString() {
		return super.toString()
				+ (fRequestID == null ? RequestMessages.EventRequestImpl___not_enabled__1
						: RequestMessages.EventRequestImpl____2 + fRequestID); //
	}

	/**
	 * @return Returns the value of the property with the specified key.
	 */
	public Object getProperty(Object key) {
		if (fPropertyMap == null) {
			return null;
		}

		return fPropertyMap.get(key);
	}

	/**
	 * Add an arbitrary key/value "property" to this request.
	 */
	public void putProperty(Object key, Object value) {
		if (fPropertyMap == null)
			fPropertyMap = new HashMap<Object, Object>();

		if (value == null)
			fPropertyMap.remove(key);
		else
			fPropertyMap.put(key, value);
	}

	/**
	 * Sets the generated inside flag. Used for requests that are not generated
	 * by JDI requests from outside.
	 */
	public void setGeneratedInside() {
		fGeneratedInside = true;
	}

	/**
	 * @return Returns whether the event request was generated from inside of
	 *         this JDI implementation.
	 */
	public final boolean isGeneratedInside() {
		return fGeneratedInside;
	}

	/**
	 * Disables event request.
	 */
	public synchronized void disable() {
		if (!isEnabled())
			return;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(),
					"event kind", EventImpl.eventKindMap(), outData); //$NON-NLS-1$
			fRequestID.write(this, outData);

			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.ER_CLEAR,
					outBytes);
			switch (replyPacket.errorCode()) {
			case JdwpReplyPacket.NOT_FOUND:
				throw new InvalidRequestStateException();
			}
			defaultReplyErrorHandler(replyPacket.errorCode());

			virtualMachineImpl().eventRequestManagerImpl()
					.removeRequestIDMapping(this);
			fRequestID = null;
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Enables event request.
	 */
	public synchronized void enable() {
		if (isEnabled())
			return;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(),
					"event kind", EventImpl.eventKindMap(), outData); //$NON-NLS-1$
			writeByte(
					suspendPolicyJDWP(),
					"suspend policy", EventRequestImpl.suspendPolicyMap(), outData); //$NON-NLS-1$
			writeInt(modifierCount(), "modifiers", outData); //$NON-NLS-1$
			writeModifiers(outData);

			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.ER_SET,
					outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
			DataInputStream replyData = replyPacket.dataInStream();
			fRequestID = RequestID.read(this, replyData);
			virtualMachineImpl().eventRequestManagerImpl().addRequestIDMapping(this);
		} catch (IOException e) {
			defaultIOExceptionHandler(e);
		} finally {
			handledJdwpRequest();
		}
	}

	/**
	 * Clear all breakpoints (used by EventRequestManager).
	 */
	public static void clearAllBreakpoints(MirrorImpl mirror) {
		mirror.initJdwpRequest();
		try {
			JdwpReplyPacket replyPacket = mirror
					.requestVM(JdwpCommandPacket.ER_CLEAR_ALL_BREAKPOINTS);
			mirror.defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			mirror.handledJdwpRequest();
		}
	}

	/**
	 * @return Returns whether event request is enabled.
	 */
	public synchronized final boolean isEnabled() {
		return fRequestID != null;
	}

	/**
	 * Disables or enables event request.
	 */
	public void setEnabled(boolean enable) {
		if (enable)
			enable();
		else
			disable();
	}

	/**
	 * @exception InvalidRequestStateException
	 *                is thrown if this request is enabled.
	 */
	public void checkDisabled() throws InvalidRequestStateException {
		if (isEnabled())
			throw new InvalidRequestStateException();
	}

	/**
	 * Sets suspend policy.
	 */
	public void setSuspendPolicy(int suspendPolicy) {
		fSuspendPolicy = (byte) suspendPolicy;
		if (isEnabled()) {
			disable();
			enable();
		}
	}

	/**
	 * @return Returns suspend policy.
	 */
	public int suspendPolicy() {
		return fSuspendPolicy;
	}

	/**
	 * @return Returns requestID, or null if request ID is not (yet) assigned.
	 */
	public final RequestID requestID() {
		return fRequestID;
	}

	/**
	 * Sets countfilter.
	 */
	public void addCountFilter(int count) throws InvalidRequestStateException {
		checkDisabled();
		if (fCountFilters == null)
			fCountFilters = new ArrayList<Integer>();

		fCountFilters.add(new Integer(count));
	}

	/**
	 * Restricts reported events to those in the given thread.
	 */
	public void addThreadFilter(ThreadReference threadFilter)
			throws ObjectCollectedException, VMMismatchException,
			InvalidRequestStateException {
		checkVM(threadFilter);
		checkDisabled();
		if (threadFilter.isCollected())
			throw new ObjectCollectedException();
		if (fThreadFilters == null)
			fThreadFilters = new ArrayList<ThreadReference>();

		fThreadFilters.add(threadFilter);
	}

	/**
	 * Restricts the events generated by this request to the preparation of
	 * reference types whose name matches this restricted regular expression.
	 */
	public void addClassFilter(ReferenceType filter)
			throws VMMismatchException, InvalidRequestStateException {
		checkVM(filter);
		checkDisabled();
		if (fClassFilterRefs == null)
			fClassFilterRefs = new ArrayList<ReferenceType>();

		fClassFilterRefs.add(filter);
	}

	/**
	 * Restricts the events generated by this request to be the preparation of
	 * the given reference type and any subtypes.
	 */
	public void addClassFilter(String filter)
			throws InvalidRequestStateException {
		checkDisabled();
		if (fClassFilters == null)
			fClassFilters = new ArrayList<String>();

		fClassFilters.add(filter);
	}

	/**
	 * Restricts the events generated by this request to the preparation of
	 * reference types whose name does not match this restricted regular
	 * expression.
	 */
	public void addClassExclusionFilter(String filter)
			throws InvalidRequestStateException {
		checkDisabled();
		if (fClassExclusionFilters == null)
			fClassExclusionFilters = new ArrayList<String>();

		fClassExclusionFilters.add(filter);
	}

	/**
	 * Restricts the events generated by this request to those that occur at the
	 * given location.
	 */
	public void addLocationFilter(LocationImpl location)
			throws VMMismatchException {
		checkDisabled();
		// Used in createBreakpointRequest.
		checkVM(location);
		if (fLocationFilters == null)
			fLocationFilters = new ArrayList<LocationImpl>();

		fLocationFilters.add(location);
	}

	/**
	 * Restricts reported exceptions by their class and whether they are caught
	 * or uncaught.
	 */
	public void addExceptionFilter(ReferenceTypeImpl refType,
			boolean notifyCaught, boolean notifyUncaught)
			throws VMMismatchException {
		checkDisabled();
		// refType Null means report exceptions of all types.
		if (refType != null)
			checkVM(refType);

		if (fExceptionFilters == null)
			fExceptionFilters = new ArrayList<ExceptionFilter>();

		ExceptionFilter filter = new ExceptionFilter();
		filter.fException = refType;
		filter.fNotifyCaught = notifyCaught;
		filter.fNotifyUncaught = notifyUncaught;
		fExceptionFilters.add(filter);
	}

	/**
	 * Restricts reported events to those that occur for a given field.
	 */
	public void addFieldFilter(FieldImpl field) throws VMMismatchException {
		checkDisabled();
		// Used in createXWatchpointRequest methods.
		checkVM(field);
		if (fFieldFilters == null)
			fFieldFilters = new ArrayList<FieldImpl>();

		fFieldFilters.add(field);
	}

	/**
	 * Restricts reported step events to those which satisfy depth and size
	 * constraints.
	 */
	public void addStepFilter(ThreadReferenceImpl thread, int size, int depth)
			throws VMMismatchException {
		checkDisabled();
		// Used in createStepRequest.
		checkVM(thread);

		if (fThreadStepFilters == null)
			fThreadStepFilters = new ArrayList<ThreadStepFilter>();

		ThreadStepFilter filter = new ThreadStepFilter();
		filter.fThread = thread;
		filter.fThreadStepSize = size;
		filter.fThreadStepDepth = depth;
		fThreadStepFilters.add(filter);
	}

	/**
	 * Helper method which allows instance filters to be added
	 * 
	 * @param instance
	 *            the object ref instance to add to the listing
	 */
	public void addInstanceFilter(ObjectReference instance) {
		checkDisabled();
		checkVM(instance);
		if (fInstanceFilters == null) {
			fInstanceFilters = new ArrayList<ObjectReference>();
		}
		fInstanceFilters.add(instance);
	}

	/**
	 * Adds a source name filter to the request. An exact match or pattern
	 * beginning OR ending in '*'.
	 * 
	 * @param pattern
	 *            source name pattern
	 * @since 3.3
	 */
	public void addSourceNameFilter(String pattern) {
		checkDisabled();
		if (fSourceNameFilters == null) {
			fSourceNameFilters = new ArrayList<String>();
		}
		fSourceNameFilters.add(pattern);
	}

	/**
	 * From here on JDWP functionality of EventRequest is implemented.
	 */

	/**
	 * @return Returns JDWP constant for suspend policy.
	 */
	public byte suspendPolicyJDWP() {
		switch (fSuspendPolicy) {
		case SUSPEND_NONE:
			return SUSPENDPOL_NONE_JDWP;
		case SUSPEND_EVENT_THREAD:
			return SUSPENDPOL_EVENT_THREAD_JDWP;
		case SUSPEND_ALL:
			return SUSPENDPOL_ALL_JDWP;
		default:
			throw new InternalException(
					RequestMessages.EventRequestImpl_Invalid_suspend_policy_encountered___3
							+ fSuspendPolicy);
		}
	}

	/**
	 * @return Returns JDWP constant for step size.
	 */
	public int threadStepSizeJDWP(int threadStepSize) {
		switch (threadStepSize) {
		case StepRequest.STEP_MIN:
			return STEP_SIZE_MIN_JDWP;
		case StepRequest.STEP_LINE:
			return STEP_SIZE_LINE_JDWP;
		default:
			throw new InternalException(
					RequestMessages.EventRequestImpl_Invalid_step_size_encountered___4
							+ threadStepSize);
		}
	}

	/**
	 * @return Returns JDWP constant for step depth.
	 */
	public int threadStepDepthJDWP(int threadStepDepth) {
		switch (threadStepDepth) {
		case StepRequest.STEP_INTO:
			return STEP_DEPTH_INTO_JDWP;
		case StepRequest.STEP_OVER:
			return STEP_DEPTH_OVER_JDWP;
		case StepRequest.STEP_OUT:
			return STEP_DEPTH_OUT_JDWP;
		default:
			throw new InternalException(
					RequestMessages.EventRequestImpl_Invalid_step_depth_encountered___5
							+ threadStepDepth);
		}
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected abstract byte eventKind();

	/**
	 * @return Returns number of modifiers.
	 */
	protected int modifierCount() {
		int count = 0;

		if (fCountFilters != null)
			count += fCountFilters.size();
		if (fThreadFilters != null)
			count += fThreadFilters.size();
		if (fClassFilterRefs != null)
			count += fClassFilterRefs.size();
		if (fClassFilters != null)
			count += fClassFilters.size();
		if (fClassExclusionFilters != null)
			count += fClassExclusionFilters.size();
		if (fLocationFilters != null)
			count += fLocationFilters.size();
		if (fExceptionFilters != null)
			count += fExceptionFilters.size();
		if (fFieldFilters != null)
			count += fFieldFilters.size();
		if (fThreadStepFilters != null)
			count += fThreadStepFilters.size();
		if (fInstanceFilters != null)
			count += fInstanceFilters.size();
		if (fSourceNameFilters != null) {
			if (supportsSourceNameFilters()) {
				count += fSourceNameFilters.size();
			}
		}
		return count;
	}

	/**
	 * Writes JDWP bytestream representation of modifiers.
	 */
	protected void writeModifiers(DataOutputStream outData) throws IOException {
		// Note: for some reason the order of these modifiers matters when
		// communicating with SUN's VM.
		// It seems to expect them 'the wrong way around'.
		if (fThreadStepFilters != null) {
			for (int i = 0; i < fThreadStepFilters.size(); i++) {
				ThreadStepFilter filter = fThreadStepFilters
						.get(i);
				writeByte(MODIF_KIND_STEP,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				filter.fThread.write(this, outData);
				writeInt(threadStepSizeJDWP(filter.fThreadStepSize),
						"step size", outData); //$NON-NLS-1$
				writeInt(threadStepDepthJDWP(filter.fThreadStepDepth),
						"step depth", outData); //$NON-NLS-1$
			}
		}
		if (fFieldFilters != null) {
			for (int i = 0; i < fFieldFilters.size(); i++) {
				writeByte(MODIF_KIND_FIELDONLY,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				fFieldFilters.get(i).writeWithReferenceType(this,
						outData);
			}
		}
		if (fExceptionFilters != null) {
			for (int i = 0; i < fExceptionFilters.size(); i++) {
				ExceptionFilter filter = fExceptionFilters
						.get(i);
				writeByte(MODIF_KIND_EXCEPTIONONLY,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				if (filter.fException != null)
					filter.fException.write(this, outData);
				else
					ReferenceTypeImpl.writeNull(this, outData);

				writeBoolean(filter.fNotifyCaught, "notify caught", outData); //$NON-NLS-1$
				writeBoolean(filter.fNotifyUncaught, "notify uncaught", outData); //$NON-NLS-1$
			}
		}
		if (fLocationFilters != null) {
			for (int i = 0; i < fLocationFilters.size(); i++) {
				writeByte(MODIF_KIND_LOCATIONONLY,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				fLocationFilters.get(i).write(this, outData);
			}
		}
		if (fClassExclusionFilters != null) {
			for (int i = 0; i < fClassExclusionFilters.size(); i++) {
				writeByte(MODIF_KIND_CLASSEXCLUDE,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				writeString(fClassExclusionFilters.get(i),
						"class excl. filter", outData); //$NON-NLS-1$
			}
		}
		if (fClassFilters != null) {
			for (int i = 0; i < fClassFilters.size(); i++) {
				writeByte(MODIF_KIND_CLASSMATCH,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				writeString(fClassFilters.get(i),
						"class filter", outData); //$NON-NLS-1$
			}
		}
		if (fClassFilterRefs != null) {
			for (int i = 0; i < fClassFilterRefs.size(); i++) {
				writeByte(MODIF_KIND_CLASSONLY,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				((ReferenceTypeImpl) fClassFilterRefs.get(i)).write(this,
						outData);
			}
		}
		if (fThreadFilters != null) {
			for (int i = 0; i < fThreadFilters.size(); i++) {
				writeByte(MODIF_KIND_THREADONLY,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				((ThreadReferenceImpl) fThreadFilters.get(i)).write(this,
						outData);
			}
		}
		if (fCountFilters != null) {
			for (int i = 0; i < fCountFilters.size(); i++) {
				writeByte(MODIF_KIND_COUNT,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				writeInt(fCountFilters.get(i).intValue(),
						"count filter", outData); //$NON-NLS-1$
			}
		}
		if (fInstanceFilters != null) {
			for (int i = 0; i < fInstanceFilters.size(); i++) {
				writeByte(MODIF_KIND_INSTANCE,
						"modifier", modifierKindMap(), outData); //$NON-NLS-1$
				((ObjectReferenceImpl) fInstanceFilters.get(i)).write(this,
						outData);
			}
		}
		if (fSourceNameFilters != null) {
			if (supportsSourceNameFilters()) {
				for (int i = 0; i < fSourceNameFilters.size(); i++) {
					writeByte(MODIF_KIND_SOURCE_NAME_FILTER,
							"modifier", modifierKindMap(), outData); //$NON-NLS-1$
					writeString(fSourceNameFilters.get(i),
							"modifier", outData); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Returns whether JDWP supports source name filters (a 1.6 feature).
	 * 
	 * @return whether JDWP supports source name filters
	 */
	private boolean supportsSourceNameFilters() {
		return ((VirtualMachineImpl) virtualMachine())
				.isJdwpVersionGreaterOrEqual(1, 6);
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fStepSizeMap != null)
			return;

		java.lang.reflect.Field[] fields = EventRequestImpl.class
				.getDeclaredFields();
		fStepSizeMap = new HashMap<Integer, String>();
		fStepDepthMap = new HashMap<Integer, String>();
		fSuspendPolicyMap = new HashMap<Integer, String>();
		fModifierKindMap = new HashMap<Integer, String>();
		for (Field field : fields) {
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0
					|| (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;

			try {
				String name = field.getName();
				Integer intValue = new Integer(field.getInt(null));
				if (name.startsWith("STEP_SIZE_")) { //$NON-NLS-1$
					name = name.substring(10);
					fStepSizeMap.put(intValue, name);
				} else if (name.startsWith("STEP_DEPTH_")) { //$NON-NLS-1$
					name = name.substring(11);
					fStepDepthMap.put(intValue, name);
				} else if (name.startsWith("SUSPENDPOL_")) { //$NON-NLS-1$
					name = name.substring(11);
					fSuspendPolicyMap.put(intValue, name);
				} else if (name.startsWith("MODIF_KIND_")) { //$NON-NLS-1$
					name = name.substring(11);
					fModifierKindMap.put(intValue, name);
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
	public static Map<Integer, String> stepSizeMap() {
		getConstantMaps();
		return fStepSizeMap;
	}

	/**
	 * @return Returns a map with string representations of tags.
	 */
	public static Map<Integer, String> stepDepthMap() {
		getConstantMaps();
		return fStepDepthMap;
	}

	/**
	 * @return Returns a map with string representations of type tags.
	 */
	public static Map<Integer, String> suspendPolicyMap() {
		getConstantMaps();
		return fSuspendPolicyMap;
	}

	/**
	 * @return Returns a map with string representations of type tags.
	 */
	public static Map<Integer, String> modifierKindMap() {
		getConstantMaps();
		return fModifierKindMap;
	}

	class ExceptionFilter {
		/**
		 * If non-null, specifies that exceptions which are instances of
		 * fExceptionFilterRef will be reported.
		 */
		ReferenceTypeImpl fException = null;
		/** If true, caught exceptions will be reported. */
		boolean fNotifyCaught = false;
		/** If true, uncaught exceptions will be reported. */
		boolean fNotifyUncaught = false;
	}

	class ThreadStepFilter {
		/** ThreadReference of thread in which to step. */
		protected ThreadReferenceImpl fThread = null;
		/** Size of each step. */
		protected int fThreadStepSize;
		/** Relative call stack limit. */
		protected int fThreadStepDepth;
	}
}
