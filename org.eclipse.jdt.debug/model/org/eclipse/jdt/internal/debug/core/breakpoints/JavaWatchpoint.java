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
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

public class JavaWatchpoint extends JavaLineBreakpoint implements
		IJavaWatchpoint {

	public static final String JAVA_WATCHPOINT = "org.eclipse.jdt.debug.javaWatchpointMarker"; //$NON-NLS-1$
	/**
	 * Watchpoint attribute storing the access value (value
	 * <code>"org.eclipse.jdt.debug.core.access"</code>). This attribute is
	 * stored as a <code>boolean</code>, indicating whether a watchpoint is an
	 * access watchpoint.
	 */
	protected static final String ACCESS = "org.eclipse.jdt.debug.core.access"; //$NON-NLS-1$
	/**
	 * Watchpoint attribute storing the modification value (value
	 * <code>"org.eclipse.jdt.debug.core.modification"</code>). This attribute
	 * is stored as a <code>boolean</code>, indicating whether a watchpoint is a
	 * modification watchpoint.
	 */
	protected static final String MODIFICATION = "org.eclipse.jdt.debug.core.modification"; //$NON-NLS-1$	
	/**
	 * Watchpoint attribute storing the auto_disabled value (value
	 * <code>"org.eclipse.jdt.debug.core.auto_disabled"</code>). This attribute
	 * is stored as a <code>boolean</code>, indicating whether a watchpoint has
	 * been auto-disabled (as opposed to being disabled explicitly by the user)
	 */
	protected static final String AUTO_DISABLED = "org.eclipse.jdt.debug.core.auto_disabled"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the name of the field on which a breakpoint
	 * is set. (value <code>"org.eclipse.jdt.debug.core.fieldName"</code>). This
	 * attribute is a <code>String</code>.
	 */
	protected static final String FIELD_NAME = "org.eclipse.jdt.debug.core.fieldName"; //$NON-NLS-1$		
	/**
	 * Flag indicating that this breakpoint last suspended execution due to a
	 * field access
	 */
	protected static final Integer ACCESS_EVENT = new Integer(0);
	/**
	 * Flag indicating that this breakpoint last suspended execution due to a
	 * field modification
	 */
	protected static final Integer MODIFICATION_EVENT = new Integer(1);
	/**
	 * Maps each debug target that is suspended for this breakpoint to reason
	 * that this breakpoint suspended it. Reasons include:
	 * <ol>
	 * <li>Field access (value <code>ACCESS_EVENT</code>)</li>
	 * <li>Field modification (value <code>MODIFICATION_EVENT</code>)</li>
	 * </ol>
	 */
	private HashMap<JDIDebugTarget, Integer> fLastEventTypes = new HashMap<JDIDebugTarget, Integer>(10);

	public JavaWatchpoint() {
	}

	/**
	 * @see JDIDebugModel#createWatchpoint(IResource, String, String, int, int,
	 *      int, int, boolean, Map)
	 */
	public JavaWatchpoint(final IResource resource, final String typeName,
			final String fieldName, final int lineNumber, final int charStart,
			final int charEnd, final int hitCount, final boolean add,
			final Map<String, Object> attributes) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				setMarker(resource.createMarker(JAVA_WATCHPOINT));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				// configure the field handle
				addFieldName(attributes, fieldName);
				// configure the access and modification flags to defaults
				addDefaultAccessAndModification(attributes);

				// set attributes
				ensureMarker().setAttributes(attributes);

				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	/**
	 * @see JavaBreakpoint#createRequest(JDIDebugTarget, ReferenceType)
	 * 
	 *      Creates and installs an access and modification watchpoint request
	 *      in the given reference type, configuring the requests as appropriate
	 *      for this watchpoint. The requests are then enabled based on whether
	 *      this watchpoint is an access watchpoint, modification watchpoint, or
	 *      both. Finally, the requests are registered with the given target.
	 */
	@Override
	protected boolean createRequest(JDIDebugTarget target, ReferenceType type)
			throws CoreException {
		if (shouldSkipBreakpoint()) {
			return false;
		}
		Field field = null;

		field = type.fieldByName(getFieldName());
		if (field == null) {
			// error
			return false;
		}
		AccessWatchpointRequest accessRequest = null;
		ModificationWatchpointRequest modificationRequest = null;
		if (target.supportsAccessWatchpoints()) {
			accessRequest = createAccessWatchpoint(target, field);
			registerRequest(accessRequest, target);
		} else {
			notSupported(JDIDebugBreakpointMessages.JavaWatchpoint_no_access_watchpoints);
		}
		if (target.supportsModificationWatchpoints()) {
			modificationRequest = createModificationWatchpoint(target, field);
			if (modificationRequest == null) {
				return false;
			}
			registerRequest(modificationRequest, target);
			return true;
		}
		notSupported(JDIDebugBreakpointMessages.JavaWatchpoint_no_modification_watchpoints);
		return false;
	}

	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	@Override
	protected void setRequestThreadFilter(EventRequest request,
			ThreadReference thread) {
		((WatchpointRequest) request).addThreadFilter(thread);
	}

	/**
	 * Either access or modification watchpoints are not supported. Throw an
	 * appropriate exception.
	 * 
	 * @param message
	 *            the message that states that access or modification
	 *            watchpoints are not supported
	 */
	protected void notSupported(String message) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR,
				DebugPlugin.getUniqueIdentifier(),
				DebugException.NOT_SUPPORTED, message, null)); //
	}

	/**
	 * Create an access watchpoint for the given breakpoint and associated field
	 */
	protected AccessWatchpointRequest createAccessWatchpoint(
			JDIDebugTarget target, Field field) throws CoreException {
		return (AccessWatchpointRequest) createWatchpoint(target, field, true);
	}

	/**
	 * Create a modification watchpoint for the given breakpoint and associated
	 * field
	 */
	protected ModificationWatchpointRequest createModificationWatchpoint(
			JDIDebugTarget target, Field field) throws CoreException {
		return (ModificationWatchpointRequest) createWatchpoint(target, field,
				false);
	}

	/**
	 * Create a watchpoint for the given breakpoint and associated field.
	 * 
	 * @param target
	 *            the target in which the request will be installed
	 * @param field
	 *            the field on which the request will be set
	 * @param access
	 *            <code>true</code> if an access watchpoint will be created.
	 *            <code>false</code> if a modification watchpoint will be
	 *            created.
	 * 
	 * @return an WatchpointRequest (AccessWatchpointRequest if access is
	 *         <code>true</code>; ModificationWatchpointRequest if access is
	 *         <code>false</code>).
	 */
	protected WatchpointRequest createWatchpoint(JDIDebugTarget target,
			Field field, boolean access) throws CoreException {
		WatchpointRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if (manager == null) {
			target.requestFailed(
					JDIDebugBreakpointMessages.JavaWatchpoint_Unable_to_create_breakpoint_request___VM_disconnected__1,
					null);
		}
		try {
			if (access) {
				request = manager.createAccessWatchpointRequest(field);
			} else {
				request = manager.createModificationWatchpointRequest(field);
			}
			configureRequest(request, target);
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {
				return null;
			}
			target.internalError(e);
			return null;
		} catch (RuntimeException e) {
			target.internalError(e);
			return null;
		}
		return request;
	}

	/**
	 * @see JavaBreakpoint#recreateRequest(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest recreateRequest(EventRequest request,
			JDIDebugTarget target) throws CoreException {
		try {
			Field field = ((WatchpointRequest) request).field();
			if (request instanceof AccessWatchpointRequest) {
				request = createAccessWatchpoint(target, field);
			} else if (request instanceof ModificationWatchpointRequest) {
				request = createModificationWatchpoint(target, field);
			}
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {
				return request;
			}
			target.internalError(e);
			return request;
		} catch (RuntimeException e) {
			target.internalError(e);
		}
		return request;
	}

	/**
	 * @see IBreakpoint#setEnabled(boolean)
	 * 
	 *      If the watchpoint is not watching access or modification, set the
	 *      default values. If this isn't done, the resulting state (enabled
	 *      with access and modification both disabled) is ambiguous.
	 */
	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		if (enabled) {
			if (!(isAccess() || isModification())) {
				setDefaultAccessAndModification();
			}
		}
		super.setEnabled(enabled);
	}

	/**
	 * @see org.eclipse.debug.core.model.IWatchpoint#isAccess()
	 */
	public boolean isAccess() throws CoreException {
		return ensureMarker().getAttribute(ACCESS, false);
	}

	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is accessed. If true and this watchpoint is disabled, this
	 * watchpoint is automatically enabled. If both access and modification are
	 * false, this watchpoint is automatically disabled.
	 * 
	 * @param access
	 *            whether to suspend on field access
	 * @exception CoreException
	 *                if unable to set the property on this breakpoint's
	 *                underlying marker
	 * @see org.eclipse.debug.core.model.IWatchpoint#setAccess(boolean)
	 */
	public void setAccess(boolean access) throws CoreException {
		if (access == isAccess()) {
			return;
		}
		setAttribute(ACCESS, access);
		if (access && !isEnabled()) {
			setEnabled(true);
		} else if (!(access || isModification())) {
			setEnabled(false);
		}
		recreate();
	}

	/**
	 * @see org.eclipse.debug.core.model.IWatchpoint#isModification()
	 */
	public boolean isModification() throws CoreException {
		return ensureMarker().getAttribute(MODIFICATION, false);
	}

	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * field is modified. If true and this watchpoint is disabled, this
	 * watchpoint is automatically enabled. If both access and modification are
	 * false, this watchpoint is automatically disabled.
	 * 
	 * @param modification
	 *            whether to suspend on field modification
	 * @exception CoreException
	 *                if unable to set the property on this breakpoint's
	 *                underlying marker
	 * @see org.eclipse.debug.core.model.IWatchpoint#setModification(boolean)
	 */
	public void setModification(boolean modification) throws CoreException {
		if (modification == isModification()) {
			return;
		}
		setAttribute(MODIFICATION, modification);
		if (modification && !isEnabled()) {
			setEnabled(true);
		} else if (!(modification || isAccess())) {
			setEnabled(false);
		}
		recreate();
	}

	/**
	 * Sets the default access and modification attributes of the watchpoint.
	 * The default values are:
	 * <ul>
	 * <li>access = <code>false</code>
	 * <li>modification = <code>true</code>
	 * <ul>
	 */
	protected void setDefaultAccessAndModification() throws CoreException {
		boolean[] def = getDefaultAccessAndModificationValues();
		Object[] values = new Object[def.length];
		for (int i = 0; i < def.length; i++) {
			values[i] = new Boolean(def[i]);
		}
		String[] attributes = new String[] { ACCESS, MODIFICATION };
		setAttributes(attributes, values);
	}

	/**
	 * Returns the default access and modification suspend option for a new
	 * watchpoint based on the user preference settings The return array will
	 * only ever contain two values, where the possibilities are:
	 * <ul>
	 * <li> <code>{true, true}</code> - both access and modification are enabled</li>
	 * <li> <code>{true, false}</code> - access is enabled and modification is
	 * disabled</li>
	 * <li> <code>{false, true}</code> -access is disabled and modification is
	 * enabled</li>
	 * </ul>
	 * The default returned array is <code>{true, true}</code>
	 * 
	 * @return an array of two boolean values representing the default access
	 *         and modification settings
	 * 
	 * @since 3.3.1
	 */
	protected boolean[] getDefaultAccessAndModificationValues() {
		int value = Platform.getPreferencesService().getInt(
				JDIDebugPlugin.getUniqueIdentifier(), 
				JDIDebugPlugin.PREF_DEFAULT_WATCHPOINT_SUSPEND_POLICY, 
				0, 
				null);
		switch (value) {
		case 0: {
			return new boolean[] { true, true };
		}
		case 1: {
			return new boolean[] { true, false };
		}
		case 2: {
			return new boolean[] { false, true };
		}
		default: {
			return new boolean[] { true, true };
		}
		}
	}

	/**
	 * Adds the default access and modification attributes of the watchpoint to
	 * the given map
	 * <ul>
	 * <li>access = true
	 * <li>modification = true
	 * <li>auto disabled = false
	 * <ul>
	 */
	protected void addDefaultAccessAndModification(Map<String, Object> attributes) {
		boolean[] values = getDefaultAccessAndModificationValues();
		attributes.put(ACCESS, (values[0] ? Boolean.TRUE : Boolean.FALSE));
		attributes
				.put(MODIFICATION, (values[1] ? Boolean.TRUE : Boolean.FALSE));
		attributes.put(AUTO_DISABLED, Boolean.FALSE);
	}

	/**
	 * Adds the field name to the given attribute map
	 */
	protected void addFieldName(Map<String, Object> attributes, String fieldName) {
		attributes.put(FIELD_NAME, fieldName);
	}

	/**
	 * @see IJavaWatchpoint#getFieldName()
	 */
	public String getFieldName() throws CoreException {
		return ensureMarker().getAttribute(FIELD_NAME, null);
	}

	/**
	 * Store the type of the event, then handle it as specified in the
	 * superclass. This is useful for correctly generating the thread text when
	 * asked (assumes thread text is requested after the event is passed to this
	 * breakpoint.
	 * 
	 * Also, @see JavaBreakpoint#handleEvent(Event, JDIDebugTarget)
	 */
	@Override
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet) {
		if (event instanceof AccessWatchpointEvent) {
			fLastEventTypes.put(target, ACCESS_EVENT);
		} else if (event instanceof ModificationWatchpointEvent) {
			fLastEventTypes.put(target, MODIFICATION_EVENT);
		}
		return super.handleEvent(event, target, suspendVote, eventSet);
	}

	/**
	 * @see JavaBreakpoint#updateEnabledState(EventRequest, JDIDebugTarget)
	 */
	@Override
	protected void updateEnabledState(EventRequest request,
			JDIDebugTarget target) throws CoreException {
		boolean enabled = isEnabled();
		if (request instanceof AccessWatchpointRequest) {
			if (isAccess()) {
				if (enabled != request.isEnabled()) {
					internalUpdateEnabledState(request, enabled, target);
				}
			} else {
				if (request.isEnabled()) {
					internalUpdateEnabledState(request, false, target);
				}
			}
		}
		if (request instanceof ModificationWatchpointRequest) {
			if (isModification()) {
				if (enabled != request.isEnabled()) {
					internalUpdateEnabledState(request, enabled, target);
				}
			} else {
				if (request.isEnabled()) {
					internalUpdateEnabledState(request, false, target);
				}
			}
		}
	}

	/**
	 * @see IJavaWatchpoint#isAccessSuspend(IDebugTarget)
	 */
	public boolean isAccessSuspend(IDebugTarget target) {
		Integer lastEventType = fLastEventTypes.get(target);
		if (lastEventType == null) {
			return false;
		}
		return lastEventType.equals(ACCESS_EVENT);
	}

	/**
	 * @see IJavaLineBreakpoint#supportsCondition()
	 */
	@Override
	public boolean supportsCondition() {
		return false;
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
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addInstanceFilter(EventRequest,
	 *      ObjectReference)
	 */
	@Override
	protected void addInstanceFilter(EventRequest request,
			ObjectReference object) {
		if (request instanceof WatchpointRequest) {
			((WatchpointRequest) request).addInstanceFilter(object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IWatchpoint#supportsAccess()
	 */
	public boolean supportsAccess() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IWatchpoint#supportsModification()
	 */
	public boolean supportsModification() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * installableReferenceType(com.sun.jdi.ReferenceType,
	 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	@Override
	protected boolean installableReferenceType(ReferenceType type,
			JDIDebugTarget target) throws CoreException {
		String installableType = getTypeName();
		String queriedType = type.name();
		if (installableType == null || queriedType == null) {
			return false;
		}
		if (installableType.equals(queriedType)) {
			return queryInstallListeners(target, type);
		}

		return false;
	}
}
