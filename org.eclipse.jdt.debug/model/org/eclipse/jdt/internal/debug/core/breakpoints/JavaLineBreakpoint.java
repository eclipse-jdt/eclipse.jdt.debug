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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
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
	private Map<IJavaThread, ICompiledExpression> fSuspendEvents = new HashMap<IJavaThread, ICompiledExpression>();
	/**
	 * The map of cached compiled expressions (ICompiledExpression) for this
	 * breakpoint, keyed by thread. This value must be cleared every time the
	 * breakpoint is added to a target.
	 */
	private Map<IJavaThread, ICompiledExpression> fCompiledExpressions = new HashMap<IJavaThread, ICompiledExpression>();

	/**
	 * Cache of projects for stack frames to avoid repetitive project resolution
	 * on conditional breakpoints.
	 */
	private Map<IJavaStackFrame, IJavaProject> fProjectsByFrame = new HashMap<IJavaStackFrame, IJavaProject>();

	/**
	 * The map of the result value of the condition (IValue) for this
	 * breakpoint, keyed by debug target.
	 */
	private Map<IDebugTarget, IValue> fConditionValues = new HashMap<IDebugTarget, IValue>();

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
		List<IJavaThread> threadsToRemove = new ArrayList<IJavaThread>();
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
			List<IJavaStackFrame> framesToRemove = new ArrayList<IJavaStackFrame>();
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
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
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
		EventRequest[] requests = new EventRequest[locations.size()];
		int i = 0;
		for(Location location : locations) {
			requests[i] = createLineBreakpointRequest(location, target);
			i++;
		}
		return requests;
	}

	/**
	 * Creates, installs, and returns a line breakpoint request at the given
	 * location for this breakpoint.
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
				null);
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
			locations = type.locationsOfLine(lineNumber);
		} catch (AbsentInformationException aie) {
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
		return suspend(thread, suspendVote);
	}

	protected IJavaProject getJavaProject(IJavaStackFrame stackFrame) {
		synchronized (fProjectsByFrame) {
			IJavaProject project = fProjectsByFrame
					.get(stackFrame);
			if (project == null) {
				project = computeJavaProject(stackFrame);
				if (project != null) {
					fProjectsByFrame.put(stackFrame, project);
				}
			}
			return project;
		}
	}

	private IJavaProject computeJavaProject(IJavaStackFrame stackFrame) {
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator = launch.getSourceLocator();
		if (locator == null)
			return null;

		Object sourceElement = null;
		try {
			if (locator instanceof ISourceLookupDirector
					&& !stackFrame.isStatic()) {
				IJavaType thisType = stackFrame.getThis().getJavaType();
				if (thisType instanceof IJavaReferenceType) {
					String[] sourcePaths = ((IJavaReferenceType) thisType)
							.getSourcePaths(null);
					if (sourcePaths != null && sourcePaths.length > 0) {
						sourceElement = ((ISourceLookupDirector) locator)
								.getSourceElement(sourcePaths[0]);
					}
				}
			}
		} catch (DebugException e) {
			DebugPlugin.log(e);
		}
		if (sourceElement == null) {
			sourceElement = locator.getSourceElement(stackFrame);
		}
		if (!(sourceElement instanceof IJavaElement)
				&& sourceElement instanceof IAdaptable) {
			Object element = ((IAdaptable) sourceElement)
					.getAdapter(IJavaElement.class);
			if (element != null) {
				sourceElement = element;
			}
		}
		if (sourceElement instanceof IJavaElement) {
			return ((IJavaElement) sourceElement).getJavaProject();
		} else if (sourceElement instanceof IResource) {
			IJavaProject project = JavaCore.create(((IResource) sourceElement)
					.getProject());
			if (project.exists()) {
				return project;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#supportsCondition()
	 */
	public boolean supportsCondition() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#getCondition()
	 */
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
		StringBuffer message = new StringBuffer(super.getMarkerMessage(
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
