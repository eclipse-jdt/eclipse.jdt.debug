package org.eclipse.jdt.internal.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import com.sun.jdi.*;import com.sun.jdi.request.*;import java.util.*;import org.eclipse.core.runtime.*;import org.eclipse.debug.core.*;import org.eclipse.debug.core.model.*;import org.eclipse.jdi.TimeoutException;import org.eclipse.jdi.hcr.OperationRefusedException;import org.eclipse.jdt.debug.core.JDIDebugModel;

public abstract class JDIDebugElement extends PlatformObject implements IDebugElement {
	
	// Resource String keys
	protected static final String UNKNOWN= "jdi.common.unknown";
	protected static final String ERROR_GET_CHILDREN= "jdi.common.error.get_children";
	
	/**
	 * This element's parent, or <code>null</code> if this
	 * element does not have a parent.
	 */
	protected JDIDebugElement fParent= null;
	/**
	 * A collection of the children of this element.
	 */
	protected List fChildren= null;
	
	/**
	 * Collection of possible JDI exceptions
	 */
	protected static List fgJDIExceptions;
	
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
	
	/**
	 * Constructs a new <code>JDIDebugElement</code>.
	 */
	public JDIDebugElement(JDIDebugElement parent) {
		fParent= parent;
	}

	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		DebugJavaUtils.logError(e);
	}
	
	/**
	 * Returns the EventRequestManager for this element's VirtualMachine
	 */
	protected EventRequestManager getEventRequestManager() {
		return getVM().eventRequestManager();
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
	 * Retursn the VM this element is contained in
	 */
	public VirtualMachine getVM() {
		return ((JDIDebugTarget)getDebugTarget()).getVM();
	}
	
	
	/**
	 * @see IDebugElement
	 */
	public IDebugTarget getDebugTarget() {
		return getParent().getDebugTarget();

	}
		
	/**
	 * @see IDebugElement
	 */
	public IStackFrame getStackFrame() {
		return null;
	}
	
	/**
	 * @see IDebugElement
	 */
	public IThread getThread() {
		return null;
	}
	
	/**
	 * @see IProcess
	 */
	public ILaunch getLaunch() {
		return getDebugPlugin().getLaunchManager().findLaunch(getDebugTarget());
	}

	/**
	 * Return children as a list
	 */
	protected List getChildren0() throws DebugException {
		if (fChildren == null) {
			return Collections.EMPTY_LIST;
		} else {
			return fChildren;
		}
	}
	
	/**
	 * @see IDebugElement
	 */
	public IDebugElement[] getChildren() throws DebugException {
		List list = getChildren0();
		return (IDebugElement[])list.toArray(new IDebugElement[list.size()]);
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
	 * Returns the source locator for this debug element
	 */
	public ISourceLocator getSourceLocator() {
		ILaunch launch= getLaunch();
		if (launch == null) {
			return null;
		}
		
		return launch.getSourceLocator();
	}

	/**
	 * @see IDebugElement
	 */
	public DebugPlugin getDebugPlugin() {
		return DebugPlugin.getDefault();
	}

	/**
	 * @see IDebugElement
	 */
	public IDebugElement getParent() {
		return fParent;
	}

	/**
	 * @see IDebugElement
	 */
	public IProcess getProcess() {
		return getDebugTarget().getProcess();
	}

	/**
	 * @see IDebugElement
	 */
	public boolean hasChildren() throws DebugException {
		return !getChildren0().isEmpty();
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>REQUEST_FAILED</code>.
	 * The message in the status is derived from the given key for this
	 * plug-in's resource bundle. A lower level exception is optional.
	 */
	public void requestFailed(String key,  Exception e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.REQUEST_FAILED, DebugJavaUtils.getResourceString(key), e));	
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 * The message in the status is derived from the given key for this
	 * plug-in's resource bundle.
	 */
	public void targetRequestFailed(String key, RuntimeException e) throws DebugException {
		if (e == null || fgJDIExceptions.contains(e.getClass())) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
				IDebugStatusConstants.TARGET_REQUEST_FAILED, DebugJavaUtils.getResourceString(key), e));
		} else {
			throw e;
		}
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 * The message in the status is derived from the given key for this
	 * plug-in's resource bundle.
	 */
	public void targetRequestFailed(String key, Throwable e) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.TARGET_REQUEST_FAILED, DebugJavaUtils.getResourceString(key), e));
	}
	
	/**
	 * Throws a new debug exception with a status code of <code>NOT_SUPPORTED</code>.
	 * The message in the status is derived from the given key for this
	 * plug-in's resource bundle.
	 */
	public void notSupported(String key) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.NOT_SUPPORTED, DebugJavaUtils.getResourceString(key), null));
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
	 * Logs a debug exception with a message based on the given key,
	 * with a status code of <code>INTERNAL_ERROR</code>.
	 */
	public void internalError(String key) {
		logError(new DebugException(new Status(IStatus.ERROR, JDIDebugModel.getPluginIdentifier(),
			IDebugStatusConstants.INTERNAL_ERROR, DebugJavaUtils.getResourceString(key), null)));
	}

	
	/**
	 * Returns the common "<unknown>" message
	 */
	public String getUnknownMessage() {
		return DebugJavaUtils.getResourceString(UNKNOWN);
	}
}
