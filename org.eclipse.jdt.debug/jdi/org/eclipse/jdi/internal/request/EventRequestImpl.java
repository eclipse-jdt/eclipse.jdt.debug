package org.eclipse.jdi.internal.request;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.jdwp.*;
import org.eclipse.jdi.internal.connect.*;
import org.eclipse.jdi.internal.event.*;
import java.io.*;
import java.util.*;


/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class EventRequestImpl extends MirrorImpl implements EventRequest {
	/** Jdwp constants for StepRequests. */
	public static final byte STEP_SIZE_MIN_JDWP = 0;
	public static final byte STEP_SIZE_LINE_JDWP = 1;
	public static final byte STEP_DEPTH_INTO_JDWP = 0;
	public static final byte STEP_DEPTH_OVER_JDWP = 1;
	public static final byte STEP_DEPTH_OUT_JDWP = 2;
	public static final byte STEP_DEPTH_REENTER_JDWP_HCR = 3;	// OTI specific for Hot Code Replacement.

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
	
	/** Mapping of command codes to strings. */
	private static HashMap fStepSizeMap = null;
	private static HashMap fStepDepthMap = null;
	private static HashMap fSuspendPolicyMap = null;
	private static HashMap fModifierKindMap = null;

	/** Value for counfilter if not set. */	
	protected static final int COUNTFILTER_NONE = -1;
	
	/** Flag that indicates the request was generated from inside of this JDI implementation. */
	private boolean fGeneratedInside = false;
	
	/** User property map. */
	private HashMap fPropertyMap;
	
	/** RequestId of EventRequest, assigned by the reply data of the JDWP Event Reuqest Set command, null if request had not yet been enabled. */
	protected RequestID fRequestID = null;
	/** Determines the threads to suspend when the requested event occurs in the target VM. */
	private byte fSuspendPolicy = SUSPEND_ALL;	// Default is as specified bu JDI spec.
	
	/**
	 * Modifiers.
	 */	
	/** Limits the requested event to be reported at most once after a given number of occurrences. */
	protected int fCountFilter = COUNTFILTER_NONE;
	
	/** Thread filter. */
	protected ThreadReferenceImpl fThreadFilter = null;
	
	/** Class filter. */
	protected String fClassFilter = null;
	
	/** Class filter. */
	protected ReferenceTypeImpl fClassFilterRef = null;
	
	/** Class Exclusion filter. */
	protected String fClassExclusionFilter = null;
	
	/** Location filter. */
	protected LocationImpl fLocationFilter = null;
	
	/** If non-null, specifies that exceptions which are instances of fExceptionFilterRef will be reported. */
	protected boolean fHasExceptionFilter = false;
	/** If non-null, specifies that exceptions which are instances of fExceptionFilterRef will be reported. */
	protected ReferenceTypeImpl fExceptionFilter = null;
	/** If true, caught exceptions will be reported. */
	protected boolean fNotifyCaught = false;
	/** If true, uncaught exceptions will be reported. */
	protected boolean fNotifyUncaught = false;
	
	/** Restricts reported events to those that occur for a given field. */
	protected FieldImpl fFieldFilter = null;
	
	/** ThreadReference of thread in which to step. */
	protected ThreadReferenceImpl fThreadStepFilter = null;
	/** Size of each step. */
	protected int fThreadStepSize;
	/** Relative call stack limit. */
	protected int fThreadStepDepth;

	/**
	 * Creates new EventRequest.
	 */
	protected EventRequestImpl(String description, VirtualMachineImpl vmImpl) {
		super(description, vmImpl);
	}
	
	/**
	 * @return Returns string representation.
	 */
	public String toString() {
		return super.toString() + (fRequestID == null ? " (not enabled)" : ": " + fRequestID);
	}
	
	/**
	 * @return Returns the value of the property with the specified key.
	 */
	public Object getProperty(Object key) {
		if (fPropertyMap == null)
			return null;
		else
			return fPropertyMap.get(key);
	}
	
	/**
	 * Add an arbitrary key/value "property" to this request.
	 */
	public void putProperty(Object key, Object value) {
		if (fPropertyMap == null)
			fPropertyMap = new HashMap();
			
		if (key == null)
			fPropertyMap.remove(key);
		else
			fPropertyMap.put(key, value);
	}
           
	/**
	 * Sets the generated inside flag. Used for requests that are not generated by JDI requests from outside.
	 */
	public void setGeneratedInside() {
		fGeneratedInside = true;
	}
	
	/**
	 * @return Returns whether the event request was generated from inside of this JDI implementation.
	 */
	public final boolean isGeneratedInside() {
		return fGeneratedInside;
	}

	/**
	 * Disables event request.
	 */
	public void disable() {
		if (!isEnabled())
			return;
		
		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(), "event kind", EventImpl.eventKindMap(), outData);
			fRequestID.write(this, outData);
	
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.ER_CLEAR, outBytes);
			defaultReplyErrorHandler(replyPacket.errorCode());
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
	public void enable() {
		if (isEnabled())
			return;

		initJdwpRequest();
		try {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			DataOutputStream outData = new DataOutputStream(outBytes);
			writeByte(eventKind(), "event kind", EventImpl.eventKindMap(), outData);
			writeByte(suspendPolicyJDWP(), "suspend policy", EventRequestImpl.suspendPolicyMap(), outData);
			writeInt(modifierCount(), "modifiers", outData);
			writeModifiers(outData);
			
			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.ER_SET, outBytes);
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
			JdwpReplyPacket replyPacket = mirror.requestVM(JdwpCommandPacket.ER_CLEAR_ALL_BREAKPOINTS);
			mirror.defaultReplyErrorHandler(replyPacket.errorCode());
		} finally {
			mirror.handledJdwpRequest();
		}
	}

	/**
	 * @return Returns whether event request is enabled.
	 */
	public final boolean isEnabled() {
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
	 * @exception InvalidRequestStateException is thrown if this request is enabled.
	 */
	public void checkDisabled() throws InvalidRequestStateException {
		if (isEnabled())
			throw new InvalidRequestStateException();
	}
	
	/**
	 * Sets suspend policy.
	 */
	public void setSuspendPolicy(int suspendPolicy) {
		fSuspendPolicy = (byte)suspendPolicy;
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
		fCountFilter = count;
	}
	
	/**
	 * Restricts reported events to those in the given thread.
	 */
	public void addThreadFilter(ThreadReference threadFilter) throws ObjectCollectedException, VMMismatchException, InvalidRequestStateException {
		checkVM(threadFilter);
		checkDisabled();
		if (threadFilter.isCollected())
			throw new ObjectCollectedException();
		fThreadFilter = (ThreadReferenceImpl)threadFilter;
	}

	/**
	 * Restricts the events generated by this request to the preparation of reference types whose name matches this restricted regular expression.
	 */
	public void addClassFilter(ReferenceType filter) throws VMMismatchException, InvalidRequestStateException {
		checkVM(filter);
		checkDisabled();
		fClassFilterRef = (ReferenceTypeImpl)filter;
	}
	
	/**
	 * Restricts the events generated by this request to be the preparation of the given reference type and any subtypes.
	 */
	public void addClassFilter(String filter) throws InvalidRequestStateException {
		checkDisabled();
		fClassFilter = filter;
	}
	
	/**
	 * Restricts the events generated by this request to the preparation of reference types whose name does not match this restricted regular expression.
	 */
	public void addClassExclusionFilter(String filter) throws InvalidRequestStateException {
		checkDisabled();
		fClassExclusionFilter = filter;
	}

   	/**
	 * Restricts the events generated by this request to those that occur at the given location.
	 */
	public void addLocationFilter(LocationImpl location) throws VMMismatchException {
		// Used in createBreakpointRequest.
		checkVM(location);
		fLocationFilter = location;
	}
	
   	/**
	 * Restricts reported exceptions by their class and whether they are caught or uncaught.
	 */
	 public void addExceptionFilter(ReferenceTypeImpl refType, boolean notifyCaught, boolean notifyUncaught) throws VMMismatchException {
		// Used in createExceptionRequest.
		fHasExceptionFilter = true;
		// refType Null means report exceptions of all types.
		if (refType != null)
			checkVM(refType);
		fExceptionFilter = refType;
		fNotifyCaught = notifyCaught;
		fNotifyUncaught = notifyUncaught;
	}

   	/**
	 * Restricts reported events to those that occur for a given field. 
	 */
	 public void addFieldFilter(FieldImpl field) throws VMMismatchException {
		// Used in createXWatchpointRequest methods.
		checkVM(field);
		fFieldFilter = field;
	}

   	/**
	 * Restricts reported step events to those which satisfy depth and size constraints.
	 */
	 public void addStepFilter(ThreadReferenceImpl thread, int size, int depth) throws VMMismatchException, ObjectCollectedException {
		// Used in createStepRequest.
	 	checkVM(thread);
		if (thread.isCollected())
			throw new ObjectCollectedException();
	 	fThreadStepFilter = thread;
	 	fThreadStepSize = size;
	 	fThreadStepDepth = depth;
	 }

	/**
	 * From here on JDWP functionality of EventRequest is implemented.
	 */

	/**
	 * @return Returns JDWP constant for suspend policy.
	 */
	public byte suspendPolicyJDWP() {
		switch(fSuspendPolicy) {
			case SUSPEND_NONE:
				return SUSPENDPOL_NONE_JDWP;
			case SUSPEND_EVENT_THREAD:
				return SUSPENDPOL_EVENT_THREAD_JDWP;
			case SUSPEND_ALL:
				return SUSPENDPOL_ALL_JDWP;
			default:
				throw new InternalException("Invalid suspend policy encountered: " + fSuspendPolicy);
		}
	}

	/**
	 * @return Returns JDWP constant for step size.
	 */
	public int threadStepSizeJDWP() {
		switch (fThreadStepSize) {
			case StepRequest.STEP_MIN:
				return STEP_SIZE_MIN_JDWP;
			case StepRequest.STEP_LINE:
				return STEP_SIZE_LINE_JDWP;
			default:
				throw new InternalException("Invalid step size encountered: " + fThreadStepSize);
		}
	}
	
	/**
	 * @return Returns JDWP constant for step depth.
	 */
	public int threadStepDepthJDWP() {
		switch (fThreadStepDepth) {
			case StepRequest.STEP_INTO:
				return STEP_DEPTH_INTO_JDWP;
			case StepRequest.STEP_OVER:
				return STEP_DEPTH_OVER_JDWP;
			case StepRequest.STEP_OUT:
				return STEP_DEPTH_OUT_JDWP;
			default:
				throw new InternalException("Invalid step depth encountered: " + fThreadStepDepth);
		}
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	protected abstract byte eventKind();
	
	/**
	 * @return Returns true if modifier count is set.
	 */
	private boolean modifierCountIsSet() {
		return (fCountFilter >= 0 );
	}

	/**
	 * @return Returns number of modifiers.
	 */
	protected int modifierCount() {
		int count =  0;
		
		if (modifierCountIsSet())
			count++;
		if (fThreadFilter != null)
			count++;
		if (fClassFilterRef != null)
			count++;
		if (fClassFilter != null)
			count++;
		if (fClassExclusionFilter != null)
			count++;
		if (fLocationFilter != null)
			count++;
		if (fHasExceptionFilter)
			count++;
		if (fFieldFilter != null)
			count++;
		if (fThreadStepFilter != null)
			count++;
			
		return count;
	}

	/**
	 * Writes JDWP bytestream representation of modifiers.
	 */
	protected void writeModifiers(DataOutputStream outData) throws IOException {
		// Note: for some reason the order of these modifiers matters when communicating with SUN's VM.
		// It seems to expect them 'the wrong way around'.
		if (fThreadStepFilter != null) {
			writeByte(MODIF_KIND_STEP, "modifier", modifierKindMap(), outData);
			fThreadStepFilter.write(this, outData);
			writeInt(threadStepSizeJDWP(), "step size", outData);
			writeInt(threadStepDepthJDWP(), "step depth", outData);
		}
		if (fFieldFilter != null) {
			writeByte(MODIF_KIND_FIELDONLY, "modifier", modifierKindMap(), outData);
			fFieldFilter.writeWithReferenceType(this, outData);
		}
		if (fHasExceptionFilter) {
			writeByte(MODIF_KIND_EXCEPTIONONLY, "modifier", modifierKindMap(), outData);
			if (fExceptionFilter != null)
				fExceptionFilter.write(this, outData);
			else
				ReferenceTypeImpl.writeNull(this, outData);

			writeBoolean(fNotifyCaught, "notify caught", outData);
			writeBoolean(fNotifyUncaught, "notify uncaught", outData);
		}
		if (fLocationFilter != null) {
			writeByte(MODIF_KIND_LOCATIONONLY, "modifier", modifierKindMap(), outData);
			fLocationFilter.write(this, outData);
		}
		if (fClassExclusionFilter != null) {
			writeByte(MODIF_KIND_CLASSEXCLUDE, "modifier", modifierKindMap(), outData);
			writeString(fClassExclusionFilter, "class excl. filter", outData);
		}
		if (fClassFilter != null) {
			writeByte(MODIF_KIND_CLASSMATCH, "modifier", modifierKindMap(), outData);
			writeString(fClassFilter, "class filter", outData);
		}
		if (fClassFilterRef != null) {
			writeByte(MODIF_KIND_CLASSONLY, "modifier", modifierKindMap(), outData);
			fClassFilterRef.write(this, outData);
		}
		if (fThreadFilter != null) {
			writeByte(MODIF_KIND_THREADONLY, "modifier", modifierKindMap(), outData);
			fThreadFilter.write(this, outData);
		}
		if (modifierCountIsSet()) {
			writeByte(MODIF_KIND_COUNT, "modifier", modifierKindMap(), outData);
			writeInt(fCountFilter, "count filter", outData);
		}
	}

	/**
	 * Retrieves constant mappings.
	 */
	public static void getConstantMaps() {
		if (fStepSizeMap != null)
			return;
		
		java.lang.reflect.Field[] fields = EventRequestImpl.class.getDeclaredFields();
		fStepSizeMap = new HashMap();
		fStepDepthMap = new HashMap();
		fSuspendPolicyMap = new HashMap();
		fModifierKindMap = new HashMap();
		for (int i = 0; i < fields.length; i++) {
			java.lang.reflect.Field field = fields[i];
			if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0 || (field.getModifiers() & java.lang.reflect.Modifier.FINAL) == 0)
				continue;
				
			try {
				String name = field.getName();
				Integer intValue = new Integer(field.getInt(null));
				if (name.startsWith("STEP_SIZE_")) {
					name = name.substring(10);
					fStepSizeMap.put(intValue, name);
				} else if (name.startsWith("STEP_DEPTH_")) {
					name = name.substring(11);
					fStepDepthMap.put(intValue, name);
				} else if (name.startsWith("SUSPENDPOL_")) {
					name = name.substring(11);
					fSuspendPolicyMap.put(intValue, name);
				} else if (name.startsWith("MODIF_KIND_")) {
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
	 public static Map stepSizeMap() {
	 	getConstantMaps();
	 	return fStepSizeMap;
	 }

	/**
	 * @return Returns a map with string representations of tags.
	 */
	 public static Map stepDepthMap() {
	 	getConstantMaps();
	 	return fStepDepthMap;
	 }

	/**
	 * @return Returns a map with string representations of type tags.
	 */
	 public static Map suspendPolicyMap() {
	 	getConstantMaps();
	 	return fSuspendPolicyMap;
	 }
	 
	/**
	 * @return Returns a map with string representations of type tags.
	 */
	 public static Map modifierKindMap() {
	 	getConstantMaps();
	 	return fModifierKindMap;
	 }
}
