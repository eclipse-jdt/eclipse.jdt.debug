package org.eclipse.jdt.internal.debug.core.breakpoints;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.eval.model.ICompiledExpression;
import org.eclipse.jdt.debug.eval.model.IPrimitiveValue;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;

public class JavaLineBreakpoint extends JavaBreakpoint implements IJavaLineBreakpoint {

	/**
	 * Breakpoint attribute storing a breakpoint's conditional expression
	 * (value <code>"org.eclipse.jdt.debug.core.condition"</code>). This attribute is stored as a
	 * <code>String</code>.
	 */
	protected static final String CONDITION= "org.eclipse.jdt.debug.core.condition";
	/**
	 * Breakpoint attribute storing a breakpoint's condition enablement
	 * (value <code>"org.eclipse.jdt.debug.core.conditionEnabled"</code>). This attribute is stored as an
	 * <code>boolean</code>.
	 */
	protected static final String CONDITION_ENABLED= "org.eclipse.jdt.debug.core.conditionEnabled";
	
	/**
	 * Breakpoint attribute storing a breakpoint's source file name (debug attribute)
	 * (value <code>"org.eclipse.jdt.debug.core.sourceName"</code>). This attribute is stored as
	 * a <code>String</code>.
	 */
	protected static final String SOURCE_NAME= "org.eclipse.jdt.debug.core.sourceName";	

	private static final String JAVA_LINE_BREAKPOINT = "org.eclipse.jdt.debug.javaLineBreakpointMarker"; //$NON-NLS-1$
	// Marker label String keys
	protected static final String LINE= "line"; //$NON-NLS-1$
	
	/**
	 * Maps suspended threads to the suspend event that suspended them
	 */
	private Map fSuspendEvents= new HashMap();
	/**
	 * The cached compiled expression for this breakpoint. This value must be cleared
	 * everytime the breakpoint is added to a target.
	 */
	private ICompiledExpression fCompiledExpression;
		
	public JavaLineBreakpoint() {
	}

	/**
	 * @see JDIDebugModel#createLineBreakpoint(IResource, String, int, int, int, int, boolean, Map)
	 */
	public JavaLineBreakpoint(IResource resource, String typeName, int lineNumber, int charStart, int charEnd, int hitCount, boolean add, Map attributes) throws DebugException {
		this(resource, typeName,lineNumber, charStart, charEnd, hitCount, add, attributes, JAVA_LINE_BREAKPOINT);
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
		run(wr);
	}
	
	/**
	 * @see JavaBreakpoint#addToTarget(JDIDebugTarget)
	 */
	public void addToTarget(JDIDebugTarget target) throws CoreException {
		fCompiledExpression= null;
		super.addToTarget(target);
	}
	
	public void removeFromTarget(JDIDebugTarget target) throws CoreException {
		super.removeFromTarget(target);
	}
	
	/**
	 * @see ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/**
	 * @see ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/**
	 * @see ILineBreakpoint#getCharEnd()
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
		
	/**
	 * @see JavaBreakpoint#newRequest(JDIDebugTarget, ReferenceType)
	 */
	protected EventRequest newRequest(JDIDebugTarget target, ReferenceType type) throws CoreException {
		Location location= null;
		int lineNumber= getLineNumber();			
		location= determineLocation(lineNumber, type);
		if (location == null) {
			// could be an inner type not yet loaded, or line information not available
			return null;
		}
		
		EventRequest request = createLineBreakpointRequest(location, target);	
		return request;		
	}	

	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for this breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, JDIDebugTarget target) throws CoreException {
		BreakpointRequest request = null;
		try {
			request= target.getEventRequestManager().createBreakpointRequest(location);
			configureRequest(request, target);
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {			
				return null;
			} 
			JDIDebugPlugin.log(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.log(e);
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
	 * Returns a location for the line number in the given type.
	 * Returns <code>null</code> if a location cannot be determined.
	 */
	protected Location determineLocation(int lineNumber, ReferenceType type) {
		List locations= null;
		try {
			locations= type.locationsOfLine(lineNumber);
		} catch (AbsentInformationException e) {
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
			JDIDebugPlugin.log(e);
			return null;
		}
		
		if (locations != null && locations.size() > 0) {
			return (Location) locations.get(0);
		} 
		
		return null;
	}
	
	/**
	 * Update the hit count of an <code>EventRequest</code>. Return a new request with
	 * the appropriate settings.
	 */
	protected EventRequest updateHitCount(EventRequest request, JDIDebugTarget target) throws CoreException {		
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hasHitCountChanged(request) || (isExpired(request) && isEnabled())) {
			request= recreateRequest(request, target);
		}
		return request;
	}
	
	/**
	 * @see JavaBreakpoint#recreateRequest(EventRequest, JDIDebugTarget)
	 */
	protected EventRequest recreateRequest(EventRequest request, JDIDebugTarget target) throws CoreException {
		try {
			Location location = ((BreakpointRequest) request).location();			
			request = createLineBreakpointRequest(location, target);
		} catch (VMDisconnectedException e) {
			if (!target.isAvailable()) {
				return request;
			}
			JDIDebugPlugin.log(e);
		} catch (RuntimeException e) {
			JDIDebugPlugin.log(e);
		}
		return request;
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
			attributes.put(EXPIRED, new Boolean(false));
		}
	}
	
	/**
	 * @see JavaBreakpoint#handleBreakpointEvent(Event, JDIDebugTarget)
	 * 
	 * (From referenced JavaDoc:
	 * 	Returns whethers the thread should be resumed
	 */
	public boolean handleBreakpointEvent(Event event, JDIDebugTarget target) {
		ThreadReference threadRef= ((LocatableEvent)event).thread();
		JDIThread thread= target.findThread(threadRef);		
		if (thread == null) {
			return true;
		} else {
			if (hasCondition()) {
				try {
					return handleConditionalBreakpointEvent(event, thread, target);
				} catch (CoreException exception) {
					// log error
					return !suspendForEvent(event, thread);
				}
			} else {
				return !suspendForEvent(event, thread); // Resume if suspend fails
			}
		}						
	}
	
	/**
	 * Returns whether this breakpoint has an enabled condition
	 */
	protected boolean hasCondition() {
		try {
			return isConditionEnabled() && !"".equals(getCondition());
		} catch (CoreException exception) {
			// log error
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
		if (thread.isPerformingEvaluation()) {
			// If an evaluation is already being computed for this thread,
			// we can't perform another
			return !suspendForEvent(event, thread);
		}
		final String condition= getCondition();
		if (!isConditionEnabled() || "".equals(condition)) {
			return !suspendForEvent(event, thread);
		}
		IMarker marker= getMarker();
		if (marker == null) {
			return true;
		}
		IJavaProject project= getJavaProject(marker.getResource().getProject());
		final IEvaluationEngine engine = getEvaluationEngine(target, project);
		if (engine == null) {
			// If no engine is available, suspend
			return !suspendForEvent(event, thread);
		}
		thread.handleSuspendForBreakpointQuiet(this);
		final JDIStackFrame frame= (JDIStackFrame)thread.computeNewStackFrames().get(0);
		
		final EvaluationListener listener= new EvaluationListener();

		if (fCompiledExpression == null) {
			fCompiledExpression= engine.getCompiledExpression(condition, frame);
		}
		if (conditionHasErrors()) {
			fireConditionHasErrors();
			return !suspendForEvent(event, thread);
		}
		fSuspendEvents.put(thread, event);
		engine.evaluateExpression(fCompiledExpression, frame, listener);

		// Do not resume. When the evaluation returns, the evaluation listener
		// will resume the thread if necessary or update for suspension.
		return false;
	}
	
	/**
	 * Listens for evaluation completion for condition evaluation.
	 * If an evaluation evaluates true or has an error, this breakpoint
	 * will suspend the thread in which the breakpoint was hit.
	 * If the evaluation returns false, the thread is resumed.
	 */
	class EvaluationListener implements IEvaluationListener {
		IEvaluationResult fResult;
		
		public void evaluationComplete(IEvaluationResult result) {
			fResult= result;
			JDIThread thread= (JDIThread)result.getThread();
			Event event= (Event)fSuspendEvents.get(thread);
			if (result.hasErrors()) {
				DebugException exception= result.getException();
				Throwable wrappedException= exception.getStatus().getException();
				if (wrappedException instanceof VMDisconnectedException) {
					JDIDebugPlugin.log((VMDisconnectedException)wrappedException);
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
				if (value instanceof IJavaPrimitiveValue) {
					// Suspend when the condition evaluates true
					IJavaPrimitiveValue javaValue= (IJavaPrimitiveValue)value;
					if (javaValue.getJavaType().getName().equals("boolean") && javaValue.getBooleanValue()) {
						suspendForCondition(event, thread);
						return;
					}
				}
				thread.resumeQuiet();
				return;
			} catch (DebugException e) {
				JDIDebugPlugin.log(e);
			}
			// Suspend when the an error occurs
			suspendForEvent(event, thread);
		}
		
		public boolean evaluationTimedOut(IJavaThread thread) {
			return true; // Keep waiting
		}
		
		public IEvaluationResult getResult() {
			return fResult;
		}
	};
	
	private void fireConditionHasRuntimeErrors(DebugException exception) {
		JDIDebugPlugin.getDefault().fireBreakpointHasRuntimeException(this, exception);
	}
	
	/**
	 * Notifies listeners that a conditional breakpoint expression has been
	 * compiled that contains errors
	 */
	private void fireConditionHasErrors() {
		JDIDebugPlugin.getDefault().fireBreakpointHasCompilationErrors(this, fCompiledExpression.getErrors());
	}
	
	/**
	 * Returns whether the cached conditional expression has errors or
	 * <code>false</code> if there is no cached expression
	 */
	public boolean conditionHasErrors() {
		if (fCompiledExpression == null) {
			return false;
		}
		return fCompiledExpression.hasErrors();
	}
	
	private IJavaProject getJavaProject(IProject project) {
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				JavaModel model = JavaModelManager.getJavaModel(project.getWorkspace());
				if (model != null) {
					return model.getJavaProject(project);
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Returns an evaluation engine for evaluating this breakpoint's condition
	 */
	public IEvaluationEngine getEvaluationEngine(IJavaDebugTarget vm, IJavaProject project)   {
		IEvaluationEngine engine = EvaluationManager.getEvaluationEngine(vm);
		if (engine == null) {
			engine= EvaluationManager.newASTAPIEvaluationEngine(project, vm);
		}
		return engine;
	}
	
	/**
	 * @see IJavaLineBreakpoint#supportsCondition
	 */
	public boolean supportsCondition() {
		return EvaluationManager.isUsingASTEvaluationEngine();
	}
	
	/**
	 * @see IJavaLineBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, "");
	}
	
	/**
	 * @see IJavaLineBreakpoint#setCondition(String)
	 */
	public void setCondition(String condition) throws CoreException {
		// Clear the cached compiled expression
		fCompiledExpression= null;	
		ensureMarker().setAttributes(new String []{CONDITION},
			new Object[]{condition});
	}			

	/**
	 * @see IJavaLineBreakpoint#isConditionEnabled()
	 */
	public boolean isConditionEnabled() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_ENABLED, false);
	}
	
	/**
	 * @see IJavaLineBreakpoint#setConditionEnabled(boolean)
	 */
	public void setConditionEnabled(boolean conditionEnabled) throws CoreException {	
		ensureMarker().setAttributes(new String []{CONDITION_ENABLED},
			new Object[]{new Boolean(conditionEnabled)});
	}
	
}



