package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import com.sun.jdi.*;import com.sun.jdi.request.*;import java.util.*;import org.eclipse.core.runtime.*;import org.eclipse.debug.core.*;import org.eclipse.debug.core.model.*;import org.eclipse.jdi.TimeoutException;import org.eclipse.jdi.hcr.OperationRefusedException;import org.eclipse.jdt.debug.core.JDIDebugModel;

public abstract class JDIDebugElement extends PlatformObject implements IDebugElement {
			
	/**
	 * Collection of possible JDI exceptions
	 */
	protected static List fgJDIExceptions;
	
	/**
	 * Debug target
	 */
	protected JDIDebugTarget fDebugTarget;
	
	static {
		fgJDIExceptions = new ArrayList(15);
		
		// Runtime/unchecked exceptions
		fgJDIExceptions.add(ClassNotPreparedException.class);
		fgJDIExceptions.add(InconsistentDebugInfoException.class);
		fgJDIExceptions.add(InternalException.class);
		fgJDIExceptions.add(InvalidCodeIndexException.class);
		fgJDIExceptions.add(InvalidLineNumberException.class);
		fgJDIExceptions.add(InvalidStackFrameException.class);
		fgJDIExceptions.add(NativeMethodException.class);
		fgJDIExceptions.add(ObjectCollectedException.class);
		fgJDIExceptions.add(TimeoutException.class);
		fgJDIExceptions.add(VMDisconnectedException.class);
		fgJDIExceptions.add(VMMismatchException.class);
		fgJDIExceptions.add(VMOutOfMemoryException.class);
		fgJDIExceptions.add(DuplicateRequestException.class);
		fgJDIExceptions.add(InvalidRequestStateException.class);
		fgJDIExceptions.add(OperationRefusedException.class);
	}
	
	public JDIDebugElement(JDIDebugTarget target) {
		fDebugTarget = target;
	}

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		DebugJavaUtils.logError(e);
	}
	
	/**
	 * This provides some simple properties and forwards requests for other properties
	 * to the extender manager.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IDebugElement.class) {
			return this;
		}			
		return super.getAdapter(adapter);
	}
	
	/**
	 * @see IDebugElement
	 */
	public String getModelIdentifier() {
		return JDIDebugPlugin.getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	/**
	 * Fire a debug event marking the creation of this element.
	 */
	public void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fire a debug event
	 */
	public void fireEvent(DebugEvent event) {
		getDebugPlugin().fireDebugEvent(event);
	}

	/**
	 * Fire a debug event marking the RESUME of this element.
	 */
	public void fireResumeEvent() {
		fireResumeEvent(-1);
	}

	/**
	 * Fire a debug event marking the SUSPEND of this element.
	 */
	public void fireSuspendEvent() {
		fireSuspendEvent(-1);
	}

	/**
	 * Fire a debug event marking the RESUME of this element with
	 * the associated detail.
	 */
	public void fireResumeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
	}

	/**
	 * Fire a debug event marking the SUSPEND of this element with
	 * the associated detail.
	 */
	public void fireSuspendEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
	}
	
	/**
	 * Fire a debug event marking the termination of this element.
	 */
	public void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fire a debug event marking the CHANGE of this element.
	 */
	public void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}

	/**
	 * Convenience method to get the breakpoint manager
	 */
	public IBreakpointManager getBreakpointManager() {
		return getDebugPlugin().getBreakpointManager();
	}

	/**
	 * @see IDebugElement
	 */
	public DebugPlugin getDebugPlugin() {
		return DebugPlugin.getDefault();
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>REQUEST_FAILED</code>.
	 * A lower level exception is optional.
	 */
	public void requestFailed(String message,  Exception e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.REQUEST_FAILED, message, e));	
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 */
	public void targetRequestFailed(String message, RuntimeException e) throws DebugException {
		if (e == null || fgJDIExceptions.contains(e.getClass())) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
				IDebugStatusConstants.TARGET_REQUEST_FAILED, message, e));
		} else {
			throw e;
		}
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 */
	public void targetRequestFailed(String message, Throwable e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.TARGET_REQUEST_FAILED, message, e));
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>NOT_SUPPORTED</code>.
	 */
	public void notSupported(String message) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.NOT_SUPPORTED, message, null));
	}
	
	
	/**
	 * Logs the given exception if it is a jdi exception, otherwise throws the exception
	 */
	public void internalError(RuntimeException e) {
		if (fgJDIExceptions.contains(e.getClass())) {
			logError(e);
		} else {
			throw e;
		}
	}
	
	/**
	 * Logs the given exception.
	 */
	public void internalError(Exception e) {
		logError(e);
	}
	
	/**
	 * Logs a debug exception with the given message,
	 * with a status code of <code>INTERNAL_ERROR</code>.
	 */
	public void internalError(String message) {
		logError(new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.INTERNAL_ERROR, message, null)));
	}

	
	/**
	 * Returns the common "<unknown>" message
	 */
	public String getUnknownMessage() {
		return "<unknown>";
	}
	
	protected boolean hasPendingEvents() {
		return ((JDIDebugTarget)getDebugTarget()).fEventDispatcher.hasPendingEvents();
	}
	
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	protected VirtualMachine getVM() {
		return fDebugTarget.getVM();
	}
	
	protected EventRequestManager getEventRequestManager() {
		return getVM().eventRequestManager();
	}
	
	public ILaunch getLaunch() {
		ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
		return mgr.findLaunch(getDebugTarget());
	}
}
