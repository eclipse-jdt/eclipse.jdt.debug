/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

 
import java.text.MessageFormat;
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
import org.eclipse.debug.core.DebugEvent;
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
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidLineNumberException;
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

public class JavaLineBreakpoint extends JavaBreakpoint implements IJavaLineBreakpoint {

	/**
	 * Breakpoint attribute storing a breakpoint's conditional expression
	 * (value <code>"org.eclipse.jdt.debug.core.condition"</code>). This attribute is stored as a
	 * <code>String</code>.
	 */
	protected static final String CONDITION= "org.eclipse.jdt.debug.core.condition"; //$NON-NLS-1$
	/**
	 * Breakpoint attribute storing a breakpoint's condition enablement
	 * (value <code>"org.eclipse.jdt.debug.core.conditionEnabled"</code>). This attribute is stored as an
	 * <code>boolean</code>.
	 */
	protected static final String CONDITION_ENABLED= "org.eclipse.jdt.debug.core.conditionEnabled"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing a breakpoint's condition suspend policy
	 * (value <code>" org.eclipse.jdt.debug.core.conditionSuspendOnTrue"
	 * </code>). This attribute is stored as an <code>boolean</code>.
	 */
	protected static final String CONDITION_SUSPEND_ON_TRUE= "org.eclipse.jdt.debug.core.conditionSuspendOnTrue"; //$NON-NLS-1$
	
	/**
	 * Breakpoint attribute storing a breakpoint's source file name (debug attribute)
	 * (value <code>"org.eclipse.jdt.debug.core.sourceName"</code>). This attribute is stored as
	 * a <code>String</code>.
	 */
	protected static final String SOURCE_NAME= "org.eclipse.jdt.debug.core.sourceName";	 //$NON-NLS-1$

	private static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$
	
	/**
	 * Maps suspended threads to the suspend event that suspended them
	 */
	private Map fSuspendEvents= new HashMap();
	/**
	 * The map of cached compiled expressions (ICompiledExpression) for this breakpoint, keyed by thread.
	 * This value must be cleared everytime the breakpoint is added to a target.
	 */
	private Map fCompiledExpressions= new HashMap();
	
	/**
	 * Cache of projects for stack frames to avoid repetitive porject resolution on conditional 
	 * breakpoints.
	 */
	private Map fProjectsByFrame= new HashMap();
	
	/**
	 * The map of the result value of the condition (IValue) for this
	 * breakpoint, keyed by debug target.
	 */
	private Map fConditionValues= new HashMap();
	
	/**
	 * Status code indicating that a request to create a breakpoint in a type
	 * with no line number attributes has occurred.
	 */
	public static final int NO_LINE_NUMBERS= 162;
		
	public JavaLineBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createLineBreakpoint(IResource, String, int, int, int, int, boolean, Map)
	 */
	public JavaLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean add, Map attributes) throws DebugException {
		this(resource, typeName, lineNumber, charStart, charEnd, hitCount, add, attributes, JAVA_LINE_BREAKPOINT);
	}
	
	protected JavaLineBreakpoint(final IResource resource, final String typeName, final int lineNumber, final int charStart, final int charEnd, final int hitCount, final boolean add, final Map attributes, final String markerType) throws DebugException {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(markerType));
				
				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, charStart, charEnd);
				addTypeNameAndHitCount(attributes, typeName, hitCount);
				// set attributes
				ensureMarker().setAttributes(attributes);
				
				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(getMarkerRule(resource), wr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addToTarget(org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		clearCachedExpressionFor(target);
		super.addToTarget(target);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#removeFromTarget(org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget)
	 */
	public void removeFromTarget(JDIDebugTarget target) throws CoreException {
		clearCachedExpressionFor(target);
		clearCachedSuspendEvents(target);
		fConditionValues.remove(target);
		super.removeFromTarget(target);
	}
	
	/**
	 * Removes all suspend events which are currently
	 * being cached for threads in the given target.
	 */
	protected void clearCachedSuspendEvents(JDIDebugTarget target) {
		removeCachedThreads(fSuspendEvents, target);
	}
	
	private void removeCachedThreads(Map map, JDIDebugTarget target) {
		Set threads= map.keySet();
		List threadsToRemove= new ArrayList();
		Iterator iter= threads.iterator();
		JDIThread thread;
		while (iter.hasNext()) {
			thread= (JDIThread)iter.next();
			if (thread.getDebugTarget() == target) {
				threadsToRemove.add(thread);
			}
		}
		iter= threadsToRemove.iterator();
		while (iter.hasNext()) {
			map.remove(iter.next());
		}
	}
	
	/**
	 * Removes all compiled expressions which are currently
	 * being cached for threads in the given target.
	 */
	protected void clearCachedExpressionFor(JDIDebugTarget target) {
		removeCachedThreads(fCompiledExpressions, target);

		// clean up cached projects for stack frames
		Set frames= fProjectsByFrame.keySet();
		List framesToRemove= new ArrayList();
		Iterator iter= frames.iterator();
		JDIStackFrame frame;
		while (iter.hasNext()) {
			frame= (JDIStackFrame)iter.next();
			if (frame.getDebugTarget() == target) {
				framesToRemove.add(frame);
			}
		}
		iter= framesToRemove.iterator();
		while (iter.hasNext()) {
			fProjectsByFrame.remove(iter.next());
		}		
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/* (non-Javadoc)
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
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#newRequest(org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, com.sun.jdi.ReferenceType)
	 */
	protected EventRequest[] newRequests(JDIDebugTarget target, ReferenceType type) throws CoreException {
		int lineNumber = getLineNumber();			
		List locations = determineLocations(lineNumber, type, target);
		if (locations == null || locations.isEmpty()) {
			// could be an inner type not yet loaded, or line information not available
			return null;
		}
		EventRequest[] requests = new EventRequest[locations.size()];
		int i = 0;
	    Iterator iterator = locations.iterator();
	    while (iterator.hasNext()) {
	        Location location = (Location) iterator.next();
	        requests[i] = createLineBreakpointRequest(location, target);
	        i++;
	    }
	    return requests;
	}	

	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for this breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, JDIDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		EventRequestManager manager = target.getEventRequestManager();
		if (manager == null) {
			target.requestFailed(JDIDebugBreakpointMessages.JavaLineBreakpoint_Unable_to_create_breakpoint_request___VM_disconnected__1, null);  //$NON-NLS-1$
		}
		try {
			request= manager.createBreakpointRequest(location);
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
	
	/**
	 * @see JavaBreakpoint#setRequestThreadFilter(EventRequest)
	 */
	protected void setRequestThreadFilter(EventRequest request, ThreadReference thread) {
		((BreakpointRequest)request).addThreadFilter(thread);
	}
		
	/**
	 * Returns a list of locations of the given line number in the given type.
	 * Returns <code>null</code> if locations cannot be determined.
	 */
	protected List determineLocations(int lineNumber, ReferenceType type, JDIDebugTarget target) {
		List locations= null;
		try {
			locations= type.locationsOfLine(lineNumber);
		} catch (AbsentInformationException aie) {
			IStatus status= new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), NO_LINE_NUMBERS, JDIDebugBreakpointMessages.JavaLineBreakpoint_Absent_Line_Number_Information_1, null);  //$NON-NLS-1$
			IStatusHandler handler= DebugPlugin.getDefault().getStatusHandler(status);
			if (handler != null) {
				try {
					handler.handleStatus(status, type);
				} catch (CoreException e) {
				}
			}
			return null;
		} catch (NativeMethodException e) {
			return null;
		} catch (InvalidLineNumberException e) {
			//possibly in a nested type, will be handled when that class is loaded
			return null;
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			target.internalError(e);
			return null;
		}
		return locations;
	}
	
	/**
	 * Adds the standard attributes of a line breakpoint to
	 * the given attribute map.
	 * The standard attributes are:
	 * <ol>
	 * <li>IBreakpoint.ID</li>
	 * <li>IBreakpoint.ENABLED</li>
	 * <li>IMarker.LINE_NUMBER</li>
	 * <li>IMarker.CHAR_START</li>
	 * <li>IMarker.CHAR_END</li>
	 * </ol>	
	 * 
	 */	
	public void addLineBreakpointAttributes(Map attributes, String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd)); 
	}		
	
	/**
	 * Adds type name and hit count attributes to the given
	 * map.
	 *
	 * If <code>hitCount > 0</code>, adds the <code>HIT_COUNT</code> attribute
	 * to the given breakpoint, and resets the <code>EXPIRED</code> attribute
	 * to false (since, if the hit count is changed, the breakpoint should no
	 * longer be expired).
	 */
	public void addTypeNameAndHitCount(Map attributes, String typeName, int hitCount) {
		attributes.put(TYPE_NAME, typeName);
		if (hitCount > 0) {
			attributes.put(HIT_COUNT, new Integer(hitCount));
			attributes.put(EXPIRED, Boolean.FALSE);
		}
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#handleBreakpointEvent(com.sun.jdi.event.Event, org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, org.eclipse.jdt.internal.debug.core.model.JDIThread)
	 * 
	 * (From referenced JavaDoc:
	 * 	Returns whethers the thread should be resumed
	 */
	public boolean handleBreakpointEvent(Event event, JDIDebugTarget target, JDIThread thread) {
		if (hasCondition()) {
			try {
				return handleConditionalBreakpointEvent(event, thread, target);
			} catch (CoreException exception) {
				JDIDebugPlugin.log(exception);
				return !suspendForEvent(event, thread);
			}
		}
		return !suspendForEvent(event, thread); // Resume if suspend fails					
	}
	
	/**
	 * Returns whether this breakpoint has an enabled condition
	 */
	protected boolean hasCondition() {
		try {
			String condition = getCondition();
			return isConditionEnabled() && condition != null && (condition.length() > 0);
		} catch (CoreException exception) {
			JDIDebugPlugin.log(exception);
			return false;
		}
	}
	
	/**
	 * Suspends the given thread for the given breakpoint event. Returns
	 * whether the thread suspends.
	 */
	protected boolean suspendForEvent(Event event, JDIThread thread) {
		expireHitCount(event);
		return suspend(thread);
	}
	
	/**
	 * Suspends the given thread for the given breakpoint event after
	 * a conditional expression evaluation. This method tells the thread
	 * to fire a suspend event immediately instead of queueing the event.
	 * This is required because of the asynchronous nature of expression
	 * evaluation. The EventDispatcher has already fired queued events
	 * by the time the evaluation completes.
	 */
	protected boolean suspendForCondition(Event event, JDIThread thread) {
		expireHitCount(event);
		return thread.handleSuspendForBreakpoint(this, false);
	}
	
	/**
	 * Returns whether this breakpoint should resume based on the
	 * value of its condition.
	 * 
	 * If there is not an enabled condition which evaluates to <code>true</code>,
	 * the thread should resume.
	 */
	protected boolean handleConditionalBreakpointEvent(Event event, JDIThread thread, JDIDebugTarget target) throws CoreException {
		synchronized (thread) {
			if (thread.isPerformingEvaluation()) {
				// If an evaluation is already being computed for this thread,
				// we can't perform another
				return !suspendForEvent(event, thread);
			}
			final String condition= getCondition();
			if (!hasCondition()) {
				return !suspendForEvent(event, thread);
			}
			EvaluationListener listener= new EvaluationListener();
	
			int suspendPolicy= SUSPEND_THREAD;
			try {
				suspendPolicy= getSuspendPolicy();
			} catch (CoreException e) {
			}
			if (suspendPolicy == SUSPEND_VM) {
				((JDIDebugTarget)thread.getDebugTarget()).prepareToSuspendByBreakpoint(this);
			} else {
				thread.handleSuspendForBreakpointQuiet(this);
			}
			List frames = thread.computeNewStackFrames();
			if (frames.size() == 0) {
				return !suspendForEvent(event, thread);
			}
			JDIStackFrame frame= (JDIStackFrame)frames.get(0);
			IJavaProject project= getJavaProject(frame);
			if (project == null) {
				throw new CoreException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(), DebugException.REQUEST_FAILED,
					JDIDebugBreakpointMessages.JavaLineBreakpoint_Unable_to_compile_conditional_breakpoint___missing_Java_project_context__1, null)); //$NON-NLS-1$
			}
			IAstEvaluationEngine engine = getEvaluationEngine(target, project);
			if (engine == null) {
				// If no engine is available, suspend
				return !suspendForEvent(event, thread);
			}
			ICompiledExpression expression= (ICompiledExpression)fCompiledExpressions.get(thread);
			if (expression == null) {
				expression= engine.getCompiledExpression(condition, frame);
				fCompiledExpressions.put(thread, expression);
			}
			if (conditionHasErrors(expression)) {
				fireConditionHasErrors(expression);
				return !suspendForEvent(event, thread);
			}
			fSuspendEvents.put(thread, event);
            thread.setEvaluatingConditionalBreakpoint(true);
			engine.evaluateExpression(expression, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
	
			// Do not resume. When the evaluation returns, the evaluation listener
			// will resume the thread if necessary or update for suspension.
			return false;
		}
	}
	
	private IJavaProject getJavaProject(JDIStackFrame stackFrame) {
	    IJavaProject project= (IJavaProject) fProjectsByFrame.get(stackFrame);
	    if (project == null) {
	        project = computeJavaProject(stackFrame);
	        if (project != null) {
	            fProjectsByFrame.put(stackFrame, project);
	        }
	    }
	    return project;
	}
	
	private IJavaProject computeJavaProject(JDIStackFrame stackFrame) {
		ILaunch launch = stackFrame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null)
			return null;
		
		Object sourceElement= null;
		try {
			if (locator instanceof ISourceLookupDirector && !stackFrame.isStatic()) {
				IJavaType thisType = stackFrame.getThis().getJavaType();
				if (thisType instanceof JDIReferenceType) {
					String[] sourcePaths= ((JDIReferenceType) thisType).getSourcePaths(null);
					if (sourcePaths.length > 0) {
						sourceElement= ((ISourceLookupDirector) locator).getSourceElement(sourcePaths[0]);
					}
				}
			}
		} catch (DebugException e) {
			DebugPlugin.log(e);
		}
		if (sourceElement == null) {
			sourceElement = locator.getSourceElement(stackFrame);
		}
		if (!(sourceElement instanceof IJavaElement) && sourceElement instanceof IAdaptable) {
			Object element= ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
			if (element != null) {
				sourceElement= element;
			}
		}
		if (sourceElement instanceof IJavaElement) {
			return ((IJavaElement) sourceElement).getJavaProject();
		} else if (sourceElement instanceof IResource) {
			IJavaProject project = JavaCore.create(((IResource)sourceElement).getProject());
			if (project.exists()) {
				return project;
			}
		}
		return null;
	}
	
	/**
	 * Listens for evaluation completion for condition evaluation.
	 * If an evaluation evaluates <code>true</code> or has an error, this breakpoint
	 * will suspend the thread in which the breakpoint was hit.
	 * If the evaluation returns <code>false</code>, the thread is resumed.
	 */
	class EvaluationListener implements IEvaluationListener {
		public void evaluationComplete(IEvaluationResult result) {
			JDIThread thread= (JDIThread)result.getThread();
            thread.setEvaluatingConditionalBreakpoint(false);
			Event event= (Event)fSuspendEvents.get(thread);
			if (result.hasErrors()) {
				DebugException exception= result.getException();
				Throwable wrappedException= exception.getStatus().getException();
				if (wrappedException instanceof VMDisconnectedException) {
					JDIDebugPlugin.log(wrappedException);
					try {
						thread.resumeQuiet();
					} catch(DebugException e) {
						JDIDebugPlugin.log(e);
					}
				} else {
					fireConditionHasRuntimeErrors(exception);
					suspendForCondition(event, thread);
					return;
				}
			}
			try {
				IValue value= result.getValue();
				if (isConditionSuspendOnTrue()) {
					if (value instanceof IJavaPrimitiveValue) {
						// Suspend when the condition evaluates true
						IJavaPrimitiveValue javaValue= (IJavaPrimitiveValue)value;
						if (isConditionSuspendOnTrue()) {
							if (javaValue.getJavaType().getName().equals("boolean") && javaValue.getBooleanValue()) { //$NON-NLS-1$
								suspendForCondition(event, thread);
								return;
							}
						}
					}
				} else {
					IDebugTarget debugTarget= thread.getDebugTarget();
					IValue lastValue= (IValue)fConditionValues.get(debugTarget);
					fConditionValues.put(debugTarget, value);
					if (!value.equals(lastValue)) {
						suspendForCondition(event, thread);
						return;
					}
				}
				int suspendPolicy= SUSPEND_THREAD;
				try {
					suspendPolicy= getSuspendPolicy();
				} catch (CoreException e) {
				}
				if (suspendPolicy == SUSPEND_VM) {
					((JDIDebugTarget)thread.getDebugTarget()).resumeQuiet();
				} else {
					thread.resumeQuiet();
				}
				return;
			} catch (DebugException e) {
				JDIDebugPlugin.log(e);
			}
			// Suspend when an error occurs
			suspendForEvent(event, thread);
		}
	}
	
	private void fireConditionHasRuntimeErrors(DebugException exception) {
		JDIDebugPlugin.getDefault().fireBreakpointHasRuntimeException(this, exception);
	}
	
	/**
	 * Notifies listeners that a conditional breakpoint expression has been
	 * compiled that contains errors
	 */
	private void fireConditionHasErrors(ICompiledExpression expression) {
		JDIDebugPlugin.getDefault().fireBreakpointHasCompilationErrors(this, getMessages(expression));
	}
	
	/**
	 * Convert an array of <code>String</code> to an array of
	 * <code>Message</code>.
	 */
	private Message[] getMessages(ICompiledExpression expression) {
		String[] errorMessages= expression.getErrorMessages();
		Message[] messages= new Message[errorMessages.length];
		for (int i= 0; i < messages.length; i++) {
			messages[i]= new Message(errorMessages[i], -1);
		}
		return messages;
	}

	/**
	 * Returns whether the cached conditional expression has errors or
	 * <code>false</code> if there is no cached expression
	 */
	public boolean conditionHasErrors(ICompiledExpression expression) {
		return expression.hasErrors();
	}
	
	/**
	 * Returns an evaluation engine for evaluating this breakpoint's condition
	 * in the given target and project context.
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm, IJavaProject project)   {
		return ((JDIDebugTarget)vm).getEvaluationEngine(project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#supportsCondition()
	 */
	public boolean supportsCondition() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setCondition(java.lang.String)
	 */
	public void setCondition(String condition) throws CoreException {
		// Clear the cached compiled expressions
		fCompiledExpressions.clear();
		fConditionValues.clear();
		fSuspendEvents.clear();
		if (condition != null && condition.trim().length() == 0) {
			condition = null;
		}
		setAttributes(new String []{CONDITION}, new Object[]{condition});
		recreate();
	}

	protected String getMarkerMessage(boolean conditionEnabled, String condition, int hitCount, int suspendPolicy, int lineNumber) {
		StringBuffer message= new StringBuffer(super.getMarkerMessage(hitCount, suspendPolicy));
		if (lineNumber != -1) {
			message.append(MessageFormat.format(JDIDebugBreakpointMessages.JavaLineBreakpoint___line___0___1, new Object[]{Integer.toString(lineNumber)})); //$NON-NLS-1$
		}
		if (conditionEnabled && condition != null) {
			message.append(MessageFormat.format(JDIDebugBreakpointMessages.JavaLineBreakpoint___Condition___0___2, new Object[]{condition})); //$NON-NLS-1$
		}
			
		return message.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#isConditionEnabled()
	 */
	public boolean isConditionEnabled() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_ENABLED, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setConditionEnabled(boolean)
	 */
	public void setConditionEnabled(boolean conditionEnabled) throws CoreException {	
		setAttributes(new String[]{CONDITION_ENABLED}, new Object[]{new Boolean(conditionEnabled)});
		recreate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#cleanupForThreadTermination(org.eclipse.jdt.internal.debug.core.model.JDIThread)
	 */
	protected void cleanupForThreadTermination(JDIThread thread) {
		fSuspendEvents.remove(thread);
		fCompiledExpressions.remove(thread);
		super.cleanupForThreadTermination(thread);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint#addInstanceFilter(com.sun.jdi.request.EventRequest, com.sun.jdi.ObjectReference)
	 */
	protected void addInstanceFilter(EventRequest request,ObjectReference object) {
		if (request instanceof BreakpointRequest) {
			((BreakpointRequest)request).addInstanceFilter(object);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#isConditionSuspendOnTrue()
	 */
	public boolean isConditionSuspendOnTrue() throws DebugException {
		return ensureMarker().getAttribute(CONDITION_SUSPEND_ON_TRUE, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaLineBreakpoint#setConditionSuspendOnTrue(boolean)
	 */
	public void setConditionSuspendOnTrue(boolean suspendOnTrue) throws CoreException {
		if (isConditionSuspendOnTrue() != suspendOnTrue) {
			setAttributes(new String[]{CONDITION_SUSPEND_ON_TRUE}, new Object[]{new Boolean(suspendOnTrue)});
			fConditionValues.clear();
			recreate();
		}
	}

}
