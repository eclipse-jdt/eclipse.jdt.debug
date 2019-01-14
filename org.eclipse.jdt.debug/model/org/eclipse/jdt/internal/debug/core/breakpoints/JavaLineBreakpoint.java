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
 *     Igor Fedorenko - Bug 368212 - JavaLineBreakpoint.computeJavaProject does not let ISourceLocator evaluate the stackFrame
 *     Jesper Møller  - Bug 422016 - [1.8] Having reference expressions or lambdas in file triggers warning for missing line numbers
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdi.internal.AccessibleImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

public class JavaLineBreakpoint extends JavaBreakpoint implements
		IJavaLineBreakpoint {

	/**
	 * Breakpoint attribute storing a breakpoint's conditional expression (value
	 * <code>"org.eclipse.jdt.debug.core.condition"</code>). This attribute is
	 * stored as a <code>String</code>.
	 */
	protected static final String CONDITION = "org.eclipse.jdt.debug.core.condition"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing a breakpoint's condition enabled state
	 * (value <code>"org.eclipse.jdt.debug.core.conditionEnabled"</code>). This
	 * attribute is stored as an <code>boolean</code>.
	 */
	protected static final String CONDITION_ENABLED = "org.eclipse.jdt.debug.core.conditionEnabled"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing a breakpoint's condition suspend policy
	 * (value <code>" org.eclipse.jdt.debug.core.conditionSuspendOnTrue"
	 * </code>). This attribute is stored as an <code>boolean</code>.
	 */
	protected static final String CONDITION_SUSPEND_ON_TRUE = "org.eclipse.jdt.debug.core.conditionSuspendOnTrue"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing a breakpoint's source file name (debug
	 * attribute) (value <code>"org.eclipse.jdt.debug.core.sourceName"</code>).
	 * This attribute is stored as a <code>String</code>.
	 */
	protected static final String SOURCE_NAME = "org.eclipse.jdt.debug.core.sourceName"; //$NON-NLS-1$

	public static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Maps suspended threads to the suspend event that suspended them
	 */
	private Map<IJavaThread, ICompiledExpression> fSuspendEvents = new HashMap<>();
	/**
	 * The map of cached compiled expressions (ICompiledExpression) for this
	 * breakpoint, keyed by thread. This value must be cleared every time the
	 * breakpoint is added to a target.
	 */
	private Map<IJavaThread, ICompiledExpression> fCompiledExpressions = new HashMap<>();

	/**
	 * Cache of projects for stack frames to avoid repetitive project resolution
	 * on conditional breakpoints.
	 */
	private Map<IJavaStackFrame, IJavaProject> fProjectsByFrame = new HashMap<>();

	/**
	 * The map of the result value of the condition (IValue) for this
	 * breakpoint, keyed by debug target.
	 */
	private Map<IDebugTarget, IValue> fConditionValues = new HashMap<>();

	/**
	 * Status code indicating that a request to create a breakpoint in a type
	 * with no line number attributes has occurred.
	 */
	public static final int NO_LINE_NUMBERS = 162;

	public JavaLineBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createLineBreakpoint(IResource, String, int, int, int,
	 *      int, boolean, Map)
	 */
	public JavaLineBreakpoint(IResource resource, String typeName,
			int lineNumber, int charStart, int charEnd, int hitCount,
			boolean add, Map<String, Object> attributes) throws DebugException {
		this(resource, typeName, lineNumber, charStart, charEnd, hitCount, add,
				attributes, JAVA_LINE_BREAKPOINT);
	}

	protected JavaLineBreakpoint(final IResource resource,
			final String typeName, final int lineNumber, final int charStart,
			final int charEnd, final int hitCount, final boolean add,
			final Map<String, Object> attributes, final String markerType)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {

				// create the marker
				setMarker(resource.createMarker(markerType));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				// set attributes
				attributes.put(SUSPEND_POLICY, new Integer(
						getDefaultSuspendPolicy()));
				ensureMarker().setAttributes(attributes);

				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addToTarget
	 * (org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	@Override
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		clearCachedExpressionFor(target);
		super.addToTarget(target);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * removeFromTarget
	 * (org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	@Override
	public void removeFromTarget(JDIDebugTarget target) throws CoreException {
		clearCachedExpressionFor(target);
		clearCachedSuspendEvents(target);
		fConditionValues.remove(target);
		super.removeFromTarget(target);
	}

	/**
	 * Removes all suspend events which are currently being cached for threads
	 * in the given target.
	 */
	protected void clearCachedSuspendEvents(JDIDebugTarget target) {
		removeCachedThreads(fSuspendEvents, target);
	}

	private void removeCachedThreads(Map<IJavaThread, ICompiledExpression> map, JDIDebugTarget target) {
		Set<IJavaThread> threads = map.keySet();
		List<IJavaThread> threadsToRemove = new ArrayList<>();
		Iterator<IJavaThread> iter = threads.iterator();
		JDIThread thread;
		while (iter.hasNext()) {
			thread = (JDIThread) iter.next();
			if (thread.getDebugTarget() == target) {
				threadsToRemove.add(thread);
			}
		}
		iter = threadsToRemove.iterator();
		while (iter.hasNext()) {
			map.remove(iter.next());
		}
	}

	/**
	 * Removes all compiled expressions which are currently being cached for
	 * threads in the given target.
	 */
	protected void clearCachedExpressionFor(JDIDebugTarget target) {
		removeCachedThreads(fCompiledExpressions, target);

		// clean up cached projects for stack frames
		synchronized (fProjectsByFrame) {
			Set<IJavaStackFrame> frames = fProjectsByFrame.keySet();
			List<IJavaStackFrame> framesToRemove = new ArrayList<>();
			Iterator<IJavaStackFrame> iter = frames.iterator();
			JDIStackFrame frame;
			while (iter.hasNext()) {
				frame = (JDIStackFrame) iter.next();
				if (frame.getDebugTarget() == target) {
					framesToRemove.add(frame);
				}
			}
			iter = framesToRemove.iterator();
			while (iter.hasNext()) {
				fProjectsByFrame.remove(iter.next());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}

	/**
	 * Returns the type of marker associated with Java line breakpoints
	 */
	public static String getMarkerType() {
		return JAVA_LINE_BREAKPOINT;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#newRequest
	 * (org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget,
	 * com.sun.jdi.ReferenceType)
	 */
	@Override
	protected EventRequest[] newRequests(JDIDebugTarget target,
			ReferenceType type) throws CoreException {
		int lineNumber = getLineNumber();
		List<Location> locations = determineLocations(lineNumber, type, target);
		if (locations == null || locations.isEmpty()) {
			// could be an inner type not yet loaded, or line information not
			// available
			return null;
		}
		locations = filterLocations(locations);
		if (locations.isEmpty()) {
			return null;
		}
		EventRequest[] requests = new EventRequest[locations.size()];
		int i = 0;
		for(Location location : locations) {
			requests[i] = createLineBreakpointRequest(location, target);
			i++;
		}
		return requests;
	}

	/**
	 * Filter out locations which shouldn't be used for breakpoint creation (like lambda methods)
	 *
	 * @return non null list with filtered locations
	 */
	protected List<Location> filterLocations(List<Location> locations) {
		if (locations.size() <= 1) {
			return locations;
		}

		// JVM has multiple breakpoint locations for this line.
		// Bug 543385: in case we have no condition, we can allow to stop at every location.
		// Bug 541110: In case we have condition, it will most likely fail due the wrong context
		// if evaluated in a lambda where the condition arguments couldn't be resolved.
		if (!hasCondition()) {
			return locations;
		}
		List<Location> result = new ArrayList<>();
		for (Location location : locations) {
			Method method = location.method();
			if (!method.isSynthetic()) {
				result.add(location);
			}
		}
		// this is just a best guess: all locations are synthetic, so we peek the first one
		if (result.isEmpty()) {
			result.add(locations.get(0));
		}
		return result;
	}

	/**
	 * Creates, installs, and returns a line breakpoint request at the given location for this breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location,
			JDIDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if (manager != null) {
			try {
				request = manager.createBreakpointRequest(location);
				configureRequest(request, target);
			} catch (VMDisconnectedException e) {
				if (!target.isAvailable()) {
					return null;
				}
				JDIDebugPlugin.log(e);
			} catch (RuntimeException e) {
				target.internalError(e);
				return null;
			}
			return request;
		}
		target.requestFailed(
				JDIDebugBreakpointMessages.JavaLineBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1,
				new VMDisconnectedException());
		return null;
	}

	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	@Override
	protected void setRequestThreadFilter(EventRequest request,
			ThreadReference thread) {
		((BreakpointRequest) request).addThreadFilter(thread);
	}

	/**
	 * Returns a list of locations of the given line number in the given type.
	 * Returns <code>null</code> if locations cannot be determined.
	 */
	protected List<Location> determineLocations(int lineNumber, ReferenceType type,
			JDIDebugTarget target) {
		List<Location> locations = null;
		try {
			locations = type.locationsOfLine(JavaDebugUtils.JAVA_STRATUM, null, lineNumber);
		} catch (AbsentInformationException aie) {
			if (((type.modifiers() & (AccessibleImpl.MODIFIER_ACC_SYNTHETIC | AccessibleImpl.MODIFIER_SYNTHETIC)) != 0)||(type instanceof InterfaceType)) {
				return null;
			}

			IStatus status = new Status(
					IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(),
					NO_LINE_NUMBERS,
					JDIDebugBreakpointMessages.JavaLineBreakpoint_Absent_Line_Number_Information_1,
					null);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(
					status);
			if (handler != null) {
				try {
					handler.handleStatus(status, type);
				} catch (CoreException e) {
				}
			}
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line information
			target.internalError(e);
			return null;
		}
		return locations;
	}

	/**
	 * Adds the standard attributes of a line breakpoint to the given attribute
	 * map. The standard attributes are:
	 * <ol>
	 * <li>IBreakpoint.ID</li>
	 * <li>IBreakpoint.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * </ol>
	 *
	 */
	public void addLineBreakpointAttributes(Map<String, Object> attributes,
			String modelIdentifier, boolean enabled, int lineNumber,
			int charStart, int charEnd) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
	}

	/**
	 * Adds type name and hit count attributes to the given map.
	 *
	 * If <code>hitCount > 0</code>, adds the <code>HIT_COUNT</code> attribute
	 * to the given breakpoint, and resets the <code>EXPIRED</code> attribute to
	 * false (since, if the hit count is changed, the breakpoint should no
	 * longer be expired).
	 */
	public void addTypeNameAndHitCount(Map<String, Object> attributes, String typeName,
			int hitCount) {
		attributes.put(TYPE_NAME, typeName);
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}

	/**
	 * Returns whether this breakpoint has an enabled condition
	 */
	public boolean hasCondition() {
		try {
			String condition = getCondition();
			return isConditionEnabled() && condition != null
					&& (condition.length() > 0);
		} catch (CoreException exception) {
			JDIDebugPlugin.log(exception);
			return false;
		}
	}

	/**
	 * Suspends the given thread for the given breakpoint event. Returns whether
	 * the thread suspends.
	 */
	protected boolean suspendForEvent(Event event, JDIThread thread,
			boolean suspendVote) {
		expireHitCount(event);
		disableTriggerPoint(event);
		return suspend(thread, suspendVote);
	}

	protected IJavaProject getJavaProject(IJavaStackFrame stackFrame) {
		synchronized (fProjectsByFrame) {
			IJavaProject project = fProjectsByFrame.get(stackFrame);
			if (project == null) {
				project = JavaDebugUtils.resolveJavaProject(stackFrame);
				if (project != null) {
					fProjectsByFrame.put(stackFrame, project);
				}
			}
			return project;
		}
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#getCondition()
	 */
	@Override
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setCondition(java.lang
	 * .String)
	 */
	@Override
	public void setCondition(String condition) throws CoreException {
		// Clear the cached compiled expressions
		fCompiledExpressions.clear();
		fConditionValues.clear();
		fSuspendEvents.clear();
		if (condition != null && condition.trim().length() == 0) {
			condition = null;
		}
		setAttributes(new String[] { CONDITION }, new Object[] { condition });
		recreate();
	}

	protected String getMarkerMessage(boolean conditionEnabled,
			String condition, int hitCount, int suspendPolicy, int lineNumber) {
		StringBuilder message = new StringBuilder(super.getMarkerMessage(
				hitCount, suspendPolicy));
		if (lineNumber != -1) {
			message.append(MessageFormat
					.format(JDIDebugBreakpointMessages.JavaLineBreakpoint___line___0___1,
							new Object[] { Integer.toString(lineNumber) }));
		}
		if (conditionEnabled && condition != null) {
			message.append(MessageFormat
					.format(JDIDebugBreakpointMessages.JavaLineBreakpoint___Condition___0___2,
							new Object[] { condition }));
		}

		return message.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#isConditionEnabled()
	 */
	@Override
	public boolean isConditionEnabled() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_ENABLED, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setConditionEnabled(boolean
	 * )
	 */
	@Override
	public void setConditionEnabled(boolean conditionEnabled)
			throws CoreException {
		setAttributes(new String[] { CONDITION_ENABLED },
				new Object[] { Boolean.valueOf(conditionEnabled) });
		recreate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * cleanupForThreadTermination
	 * (org.eclipse.jdt.internal.debug.core.model.JDIThread)
	 */
	@Override
	protected void cleanupForThreadTermination(JDIThread thread) {
		fSuspendEvents.remove(thread);
		fCompiledExpressions.remove(thread);
		super.cleanupForThreadTermination(thread);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#
	 * addInstanceFilter(com.sun.jdi.request.EventRequest,
	 * com.sun.jdi.ObjectReference)
	 */
	@Override
	protected void addInstanceFilter(EventRequest request,
			ObjectReference object) {
		if (request instanceof BreakpointRequest) {
			((BreakpointRequest) request).addInstanceFilter(object);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaLineBreakpoint#isConditionSuspendOnTrue()
	 */
	@Override
	public boolean isConditionSuspendOnTrue() throws DebugException {
		return ensureMarker().getAttribute(CONDITION_SUSPEND_ON_TRUE, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setConditionSuspendOnTrue
	 * (boolean)
	 */
	@Override
	public void setConditionSuspendOnTrue(boolean suspendOnTrue)
			throws CoreException {
		if (isConditionSuspendOnTrue() != suspendOnTrue) {
			setAttributes(new String[] { CONDITION_SUSPEND_ON_TRUE },
					new Object[] { Boolean.valueOf(suspendOnTrue) });
			fConditionValues.clear();
			recreate();
		}
	}

	/**
	 * Returns existing compiled expression for the given thread or
	 * <code>null</code>.
	 *
	 * @param thread
	 *            thread the breakpoint was hit in
	 * @return compiled expression or <code>null</code>
	 */
	protected ICompiledExpression getExpression(IJavaThread thread) {
		return fCompiledExpressions.get(thread);
	}

	/**
	 * Sets the compiled expression for a thread.
	 *
	 * @param thread
	 *            thread the breakpoint was hit in
	 * @param expression
	 *            associated compiled expression
	 */
	protected void setExpression(IJavaThread thread,
			ICompiledExpression expression) {
		fCompiledExpressions.put(thread, expression);
	}

	/**
	 * Sets the current result value of the conditional expression evaluation
	 * for this breakpoint in the given target, and returns the previous value
	 * or <code>null</code> if none
	 *
	 * @param target
	 *            debug target
	 * @param value
	 *            current expression value
	 * @return previous value or <code>null</code>
	 */
	protected IValue setCurrentConditionValue(IDebugTarget target, IValue value) {
		IValue prev = fConditionValues.get(target);
		fConditionValues.put(target, value);
		return prev;
	}

}
