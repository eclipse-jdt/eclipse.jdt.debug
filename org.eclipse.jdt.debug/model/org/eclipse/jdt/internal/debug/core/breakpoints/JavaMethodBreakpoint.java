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
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

public class JavaMethodBreakpoint extends JavaLineBreakpoint implements
		IJavaMethodBreakpoint {

	public static final String JAVA_METHOD_BREAKPOINT = "org.eclipse.jdt.debug.javaMethodBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the name of the method in which a breakpoint
	 * is contained. (value <code>"org.eclipse.jdt.debug.core.methodName"</code>
	 * ). This attribute is a <code>String</code>.
	 */
	protected static final String METHOD_NAME = "org.eclipse.jdt.debug.core.methodName"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the signature of the method in which a
	 * breakpoint is contained. (value
	 * <code>"org.eclipse.jdt.debug.core.methodSignature"</code>). This
	 * attribute is a <code>String</code>.
	 */
	protected static final String METHOD_SIGNATURE = "org.eclipse.jdt.debug.core.methodSignature"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing whether this breakpoint is an entry
	 * breakpoint. (value <code>"org.eclipse.jdt.debug.core.entry"</code>). This
	 * attribute is a <code>boolean</code>.
	 */
	protected static final String ENTRY = "org.eclipse.jdt.debug.core.entry"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing whether this breakpoint is an exit
	 * breakpoint. (value <code>"org.eclipse.jdt.debug.core.exit"</code>). This
	 * attribute is a <code>boolean</code>.
	 */
	protected static final String EXIT = "org.eclipse.jdt.debug.core.exit"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing whether this breakpoint only applies to
	 * native methods. (value <code>"org.eclipse.jdt.debug.core.native"</code>).
	 * This attribute is a <code>boolean</code>.
	 */
	protected static final String NATIVE = "org.eclipse.jdt.debug.core.native"; //$NON-NLS-1$

	/**
	 * Cache of method name attribute
	 */
	private String fMethodName = null;

	/**
	 * Cache of method signature attribute
	 */
	private String fMethodSignature = null;

	/**
	 * Flag indicating that this breakpoint last suspended execution due to a
	 * method entry
	 */
	protected static final Integer ENTRY_EVENT = new Integer(0);

	/**
	 * Flag indicating that this breakpoint last suspended execution due to a
	 * method exit
	 */
	protected static final Integer EXIT_EVENT = new Integer(1);

	/**
	 * Maps each debug target that is suspended for this breakpoint to reason
	 * that this breakpoint suspended it. Reasons include:
	 * <ol>
	 * <li>Method entry (value <code>ENTRY_EVENT</code>)</li>
	 * <li>Method exit (value <code>EXIT_EVENT</code>)</li>
	 * </ol>
	 */
	private Map<IDebugTarget, Integer> fLastEventTypes = new HashMap<IDebugTarget, Integer>(10);

	/**
	 * Used to match type names
	 */
	private Pattern fPattern;

	/**
	 * Cache of whether this breakpoint uses a type name pattern
	 */
	private Boolean fUsesTypePattern = null;

	/**
	 * Constructs a new method breakpoint
	 */
	public JavaMethodBreakpoint() {
	}

	public JavaMethodBreakpoint(final IResource resource,
			final String typePattern, final String methodName,
			final String methodSignature, final boolean entry,
			final boolean exit, final boolean nativeOnly, final int lineNumber,
			final int charStart, final int charEnd, final int hitCount,
			final boolean register, final Map<String, Object> attributes) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(JAVA_METHOD_BREAKPOINT));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addMethodNameAndSignature(attributes, methodName,
						methodSignature);
				addTypeNameAndHitCount(attributes, typePattern, hitCount);
				attributes.put(ENTRY, Boolean.valueOf(entry));
				attributes.put(EXIT, Boolean.valueOf(exit));
				attributes.put(NATIVE, Boolean.valueOf(nativeOnly));
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				// set attributes
				ensureMarker().setAttributes(attributes);
				register(register);
			}

		};
		run(getMarkerRule(resource), wr);
		String type = convertToRegularExpression(typePattern);
		fPattern = Pattern.compile(type);
	}

	/**
	 * Creates and installs an entry and exit requests in the given type name,
	 * configuring the requests as appropriate for this breakpoint. The requests
	 * are then enabled based on whether this breakpoint is an entry breakpoint,
	 * exit breakpoint, or both. Finally, the requests are registered with the
	 * given target.
	 */
	protected void createRequest(JDIDebugTarget target, String typePattern)
			throws CoreException {
		MethodEntryRequest entryRequest = createMethodEntryRequest(target,
				typePattern);
		MethodExitRequest exitRequest = createMethodExitRequest(target,
				typePattern);

		registerRequest(entryRequest, target);
		registerRequest(exitRequest, target);
	}

	/**
	 * Returns a new method entry request for this breakpoint's criteria
	 * 
	 * @param the
	 *            target in which to create the request
	 * @param type
	 *            the type on which to create the request
	 * @return method entry request
	 * @exception CoreException
	 *                if an exception occurs accessing this breakpoint's
	 *                underlying marker
	 */
	protected MethodEntryRequest createMethodEntryRequest(
			JDIDebugTarget target, String typePattern) throws CoreException {
		return (MethodEntryRequest) createMethodRequest(target, typePattern,
				true);
	}

	/**
	 * Returns a new method exit request for this breakpoint's criteria
	 * 
	 * @param target
	 *            the target in which to create the request
	 * @param type
	 *            the type on which to create the request
	 * @return method exit request
	 * @exception CoreException
	 *                if an exception occurs accessing this breakpoint's
	 *                underlying marker
	 */
	protected MethodExitRequest createMethodExitRequest(JDIDebugTarget target,
			String typePattern) throws CoreException {
		return (MethodExitRequest) createMethodRequest(target, typePattern,
				false);
	}

	/**
	 * Returns a new method entry request for this breakpoint's criteria
	 * 
	 * @param the
	 *            target in which to create the request
	 * @param type
	 *            the type on which to create the request
	 * @return method entry request
	 * @exception CoreException
	 *                if an exception occurs accessing this breakpoint's
	 *                underlying marker
	 */
	protected EventRequest createMethodEntryRequest(JDIDebugTarget target,
			ReferenceType type) throws CoreException {
		return createMethodRequest(target, type, true);
	}

	/**
	 * Returns a new method exit request for the given reference type
	 * 
	 * @param target
	 *            the target in which to create the request
	 * @param type
	 *            the type on which to create the request
	 * @return method exit request
	 * @exception CoreException
	 *                if an exception occurs accessing this breakpoint's
	 *                underlying marker
	 */
	protected EventRequest createMethodExitRequest(JDIDebugTarget target,
			ReferenceType type) throws CoreException {
		return createMethodRequest(target, type, false);
	}

	/**
	 * @see JavaMethodBreakpoint#createMethodEntryRequest(JDIDebugTarget,
	 *      ReferenceType) or
	 *      JavaMethodBreakpoint#createMethodExitRequest(JDIDebugTarget,
	 *      ReferenceType)
	 * 
	 *      Returns a <code>MethodEntryRequest</code> or
	 *      <code>BreakpointRequest</code> if entry is <code>true</code>, a
	 *      <code>MethodExitRequest</code> if entry is <code>false</code>.
	 * 
	 * @param target
	 *            the debug target in which to create the request
	 * @param classFilter
	 *            a filter which specifies the scope of the method request. This
	 *            parameter must be either a <code>String</code> or a
	 *            <code>ReferenceType</code>
	 * @param entry
	 *            whether or not the request will be a method entry request. If
	 *            <code>false</code>, the request will be a method exit request.
	 */
	private EventRequest createMethodRequest(JDIDebugTarget target,
			Object classFilter, boolean entry) throws CoreException {
		EventRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if (manager != null) {
			try {
				if (entry) {
					if (classFilter instanceof ClassType && getMethodName() != null
							&& getMethodSignature() != null) {
						// use a line breakpoint if possible for better performance
						ClassType clazz = (ClassType) classFilter;
						if (clazz.name().equals(getTypeName())) {
							// only use line breakpoint when there is an exact match
							Method method = clazz.concreteMethodByName(
									getMethodName(), getMethodSignature());
							if (method != null && !method.isNative()) {
								Location location = method.location();
								if (location != null && location.codeIndex() != -1) {
									request = manager
											.createBreakpointRequest(location);
								}
							}
						}
					}
					if (request == null) {
						request = manager.createMethodEntryRequest();
						if (classFilter instanceof String) {
							((MethodEntryRequest) request)
									.addClassFilter((String) classFilter);
						} else if (classFilter instanceof ReferenceType) {
							((MethodEntryRequest) request)
									.addClassFilter((ReferenceType) classFilter);
						}
					}
				} else {
					request = manager.createMethodExitRequest();
					if (classFilter instanceof String) {
						((MethodExitRequest) request)
								.addClassFilter((String) classFilter);
					} else if (classFilter instanceof ReferenceType) {
						((MethodExitRequest) request)
								.addClassFilter((ReferenceType) classFilter);
					}
				}
				configureRequest(request, target);
			} catch (VMDisconnectedException e) {
				if (!target.isAvailable()) {
					return null;
				}
				JDIDebugPlugin.log(e);
			} catch (RuntimeException e) {
				target.internalError(e);
			}
			return request;
		}
		target.requestFailed(
				JDIDebugBreakpointMessages.JavaMethodBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1,
				null);
		return null;
	}

	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	@Override
	protected void setRequestThreadFilter(EventRequest request,
			ThreadReference thread) {
		if (request instanceof MethodEntryRequest) {
			((MethodEntryRequest) request).addThreadFilter(thread);
		} else if (request instanceof MethodExitRequest) {
			((MethodExitRequest) request).addThreadFilter(thread);
		} else if (request instanceof BreakpointRequest) {
			((BreakpointRequest) request).addThreadFilter(thread);
		}
	}

	/**
	 * Configure the given request's hit count. Since method entry/exit requests
	 * do not support hit counts, we simulate a hit count by manually updating a
	 * counter stored on the request.
	 */
	@Override
	protected void configureRequestHitCount(EventRequest request)
			throws CoreException {
		if (request instanceof BreakpointRequest) {
			super.configureRequestHitCount(request);
		} else {
			int hitCount = getHitCount();
			if (hitCount > 0) {
				request.putProperty(HIT_COUNT, new Integer(hitCount));
			}
		}
	}

	/**
	 * @see JavaBreakpoint#updateEnabledState(EventRequest, JDIDebugTarget)
	 */
	@Override
	protected void updateEnabledState(EventRequest request,
			JDIDebugTarget target) throws CoreException {
		boolean enabled = isEnabled();
		if (request instanceof MethodEntryRequest
				|| request instanceof BreakpointRequest) {
			enabled = enabled && isEntry();
		} else if (request instanceof MethodExitRequest) {
			enabled = enabled && isExit();
		}

		if (enabled != request.isEnabled()) {
			internalUpdateEnabledState(request, enabled, target);
		}
	}

	/**
	 * Adds the method name and signature attributes to the given attribute map,
	 * and initializes the local cache of method name and signature.
	 */
	private void addMethodNameAndSignature(Map<String, Object> attributes, String methodName,
			String methodSignature) {
		if (methodName != null) {
			attributes.put(METHOD_NAME, methodName);
		}
		if (methodSignature != null) {
			attributes.put(METHOD_SIGNATURE, methodSignature);
		}
		fMethodName = methodName;
		fMethodSignature = methodSignature;
	}

	/**
	 * @see IJavaMethodBreakpoint#isEntrySuspend(IDebugTarget)
	 */
	public boolean isEntrySuspend(IDebugTarget target) {
		Integer lastEventType = fLastEventTypes.get(target);
		if (lastEventType == null) {
			return false;
		}
		return lastEventType.equals(ENTRY_EVENT);
	}

	/**
	 * @see JavaBreakpoint#handleBreakpointEvent(Event, JDIDebugTarget,
	 *      JDIThread)
	 */
	@Override
	public boolean handleBreakpointEvent(Event event, JDIThread thread,
			boolean suspendVote) {
		if (event instanceof MethodEntryEvent) {
			MethodEntryEvent entryEvent = (MethodEntryEvent) event;
			fLastEventTypes.put(thread.getDebugTarget(), ENTRY_EVENT);
			return handleMethodEvent(entryEvent, entryEvent.method(), thread,
					suspendVote);
		} else if (event instanceof MethodExitEvent) {
			MethodExitEvent exitEvent = (MethodExitEvent) event;
			fLastEventTypes.put(thread.getDebugTarget(), EXIT_EVENT);
			return handleMethodEvent(exitEvent, exitEvent.method(), thread,
					suspendVote);
		} else if (event instanceof BreakpointEvent) {
			fLastEventTypes.put(thread.getDebugTarget(), ENTRY_EVENT);
			return super.handleBreakpointEvent(event, thread, suspendVote);
		}
		return true;
	}

	/**
	 * Method entry/exit events are fired each time any method is invoked in a
	 * class in which a method entry/exit breakpoint has been installed. When a
	 * method entry/exit event is received by this breakpoint, ensure that the
	 * event has been fired by a method invocation that this breakpoint is
	 * interested in. If it is not, do nothing.
	 */
	protected boolean handleMethodEvent(LocatableEvent event, Method method,
			JDIThread thread, boolean suspendVote) {
		try {
			if (isNativeOnly()) {
				if (!method.isNative()) {
					return true;
				}
			}

			if (getMethodName() != null) {
				if (!method.name().equals(getMethodName())) {
					return true;
				}
			}

			if (getMethodSignature() != null) {
				String sig = method.signature();
				if (sig.indexOf('$') > -1) {
					sig = sig.replace('$', '.');
				}
				if (!sig.equals(getMethodSignature())) {
					return true;
				}
			}

			if (fPattern != null) {
				if (!fPattern.matcher(method.declaringType().name()).find()) {
					return true;
				}
			}

			// simulate hit count
			Integer count = (Integer) event.request().getProperty(HIT_COUNT);
			if (count != null && handleHitCount(event, count)) {
				return true;
			}
			// no hit count
			return !suspendForEvent(event, thread, suspendVote); // Resume if
																	// suspend
																	// fails
		} catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return true;
	}

	/**
	 * Method breakpoints simulate hit count. When a method event is received,
	 * decrement the hit count property on the request and suspend if the hit
	 * count reaches 0.
	 */
	private boolean handleHitCount(LocatableEvent event, Integer count) {
		// decrement count and suspend if 0
		int hitCount = count.intValue();
		if (hitCount > 0) {
			hitCount--;
			count = new Integer(hitCount);
			event.request().putProperty(HIT_COUNT, count);
			if (hitCount == 0) {
				// the count has reached 0, breakpoint hit
				try {
					// make a note that we auto-disabled the breakpoint
					// order is important here...see methodEntryChanged
					setExpired(true);
					setEnabled(false);
				} catch (CoreException e) {
					JDIDebugPlugin.log(e);
				}
				return false;
			}
			// count still > 0, keep running
			return true;
		}
		// hit count expired, keep running
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint#getMethodName()
	 */
	public String getMethodName() {
		return fMethodName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaMethodEntryBreakpoint#getMethodSignature
	 * ()
	 */
	public String getMethodSignature() {
		return fMethodSignature;
	}

	/**
	 * @see IJavaMethodBreakpoint#isEntry()
	 */
	public boolean isEntry() throws CoreException {
		return ensureMarker().getAttribute(ENTRY, false);
	}

	/**
	 * @see IJavaMethodBreakpoint#isExit()
	 */
	public boolean isExit() throws CoreException {
		return ensureMarker().getAttribute(EXIT, false);
	}

	/**
	 * @see IJavaMethodBreakpoint#isNative()
	 */
	public boolean isNativeOnly() throws CoreException {
		return ensureMarker().getAttribute(NATIVE, false);
	}

	/**
	 * @see IJavaMethodBreakpoint#setEntry(boolean)
	 */
	public void setEntry(boolean entry) throws CoreException {
		if (isEntry() != entry) {
			setAttribute(ENTRY, entry);
			if (entry && !isEnabled()) {
				setEnabled(true);
			} else if (!(entry || isExit())) {
				setEnabled(false);
			}
			recreate();
		}
	}

	/**
	 * @see IJavaMethodBreakpoint#setExit(boolean)
	 */
	public void setExit(boolean exit) throws CoreException {
		if (isExit() != exit) {
			setAttribute(EXIT, exit);
			if (exit && !isEnabled()) {
				setEnabled(true);
			} else if (!(exit || isEntry())) {
				setEnabled(false);
			}
			recreate();
		}
	}

	/**
	 * @see IJavaMethodBreakpoint#setNativeOnly(boolean)
	 */
	public void setNativeOnly(boolean nativeOnly) throws CoreException {
		if (isNativeOnly() != nativeOnly) {
			setAttribute(NATIVE, nativeOnly);
			recreate();
		}
	}

	/**
	 * Initialize cache of attributes
	 * 
	 * @see org.eclipse.debug.core.model.IBreakpoint#setMarker(IMarker)
	 */
	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		fMethodName = marker.getAttribute(METHOD_NAME, null);
		fMethodSignature = marker.getAttribute(METHOD_SIGNATURE, null);
		String typePattern = marker.getAttribute(TYPE_NAME, ""); //$NON-NLS-1$
		if (typePattern != null) {
			try {
				fPattern = Pattern
						.compile(convertToRegularExpression(typePattern));
			} catch (PatternSyntaxException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						JDIDebugModel.getPluginIdentifier(),
						JDIDebugBreakpointMessages.JavaMethodBreakpoint_0, e));
			}
		}
	}

	/**
	 * converts the specified string to one which has been formated to our needs
	 * 
	 * @param stringMatcherPattern
	 *            the initial pattern
	 * @return the modified pattern
	 */
	private String convertToRegularExpression(String stringMatcherPattern) {
		String regex = stringMatcherPattern.replaceAll("\\.", "\\\\."); //$NON-NLS-1$//$NON-NLS-2$
		regex = regex.replaceAll("\\*", "\\.\\*"); //$NON-NLS-1$//$NON-NLS-2$
		regex = regex.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$
		return regex;
	}

	/**
	 * If this breakpoint is not entry or exit enabled, set the default (entry)
	 * 
	 * @see org.eclipse.debug.core.model.IBreakpoint#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		if (enabled) {
			if (!(isEntry() || isExit())) {
				setDefaultEntryAndExit();
			}
		}
		super.setEnabled(enabled);
	}

	/**
	 * Sets the default entry and exit attributes of the method breakpoint The
	 * default values are:
	 * <ul>
	 * <li>entry = <code>true</code>
	 * <li>exit = <code>false</code>
	 * <ul>
	 */
	protected void setDefaultEntryAndExit() throws CoreException {
		Object[] values = new Object[] { Boolean.TRUE, Boolean.FALSE };
		String[] attributes = new String[] { ENTRY, EXIT };
		setAttributes(attributes, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#supportsCondition()
	 */
	@Override
	public boolean supportsCondition() {
		return true;
	}

	/**
	 * @see JavaBreakpoint#addToTarget(JDIDebugTarget)
	 */
	@Override
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		if (usesTypePattern()) {
			// pre-notification
			fireAdding(target);

			String referenceTypeNamePattern = getTypeName();
			if (referenceTypeNamePattern == null) {
				return;
			}

			createRequest(target, referenceTypeNamePattern);
		} else {
			super.addToTarget(target);
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#removeFromTarget(JDIDebugTarget)
	 */
	@Override
	public void removeFromTarget(JDIDebugTarget target) throws CoreException {
		fLastEventTypes.remove(target);
		super.removeFromTarget(target);
	}

	/**
	 * Returns whether this breakpoint uses type name pattern matching.
	 * 
	 * @return whether this breakpoint uses type name pattern matching
	 */
	protected boolean usesTypePattern() throws CoreException {
		if (fUsesTypePattern == null) {
			String name = getTypeName();
			fUsesTypePattern = Boolean.valueOf(name != null
					&& (name.startsWith("*") || name.endsWith("*"))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return fUsesTypePattern.booleanValue();
	}

	/**
	 * Used when this breakpoint is for a specific type (i.e. not using type
	 * name pattern matching).
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#createRequest(JDIDebugTarget,
	 *      ReferenceType)
	 */
	@Override
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type)
			throws CoreException {
		if (!type.name().equals(getTypeName()) || shouldSkipBreakpoint()) {
			// do not create requests for inner/outer types if this is for a
			// specific type
			return false;
		}
		EventRequest entryRequest = createMethodEntryRequest(target, type);
		EventRequest exitRequest = createMethodExitRequest(target, type);

		registerRequest(entryRequest, target);
		registerRequest(exitRequest, target);
		return true;
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#setTypeName(String)
	 */
	@Override
	protected void setTypeName(String typeName) throws CoreException {
		fUsesTypePattern = null;
		super.setTypeName(typeName);
	}

	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addInstanceFilter(EventRequest,
	 *      ObjectReference)
	 */
	@Override
	protected void addInstanceFilter(EventRequest request,
			ObjectReference object) {
		if (request instanceof MethodEntryRequest) {
			((MethodEntryRequest) request).addInstanceFilter(object);
		} else if (request instanceof MethodExitRequest) {
			((MethodExitRequest) request).addInstanceFilter(object);
		} else {
			super.addInstanceFilter(request, object);
		}
	}
}
