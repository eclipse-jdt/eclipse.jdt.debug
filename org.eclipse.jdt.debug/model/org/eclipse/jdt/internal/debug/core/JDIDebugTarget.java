package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

/**
 * Debug target for JDI debug model.
 */

public class JDIDebugTarget extends JDIDebugElement implements IJavaDebugTarget {
		
	/**
	 * Threads contained in this debug target. When a thread
	 * starts it is added to the list. When a thread ends it
	 * is removed from the list.
	 */
	private List fThreads;
	/**
	 * Associated system process, or <code>null</code> if not available.
	 */
	private IProcess fProcess;
	/**
	 * Underlying virtual machine.
	 */
	private VirtualMachine fVirtualMachine;
	/**
	 * Whether terminate is supported. Not all targets
	 * support terminate. For example, a VM that was attached
	 * to remotely may not allow the user to terminate it.
	 */
	private boolean fSupportsTerminate;
	/**
	 * Whether terminated or disconnected
	 */
	private boolean fTerminated;
	/**
	 * Whether disconnect is supported.
	 */
	private boolean fSupportsDisconnect;
	/**
	 * Collection of breakpoints added to this target. Values are of type <code>IJavaBreakpoint</code>.
	 */
	private List fBreakpoints;
	 
	/**
	 * The instance of <code>java.lang.ThreadDeath</code> used to
	 * interrupt threads on this target.
	 */
	private ObjectReference fThreadDeath;

	/**
	 * The name of this target - set by the client on creation, or retrieved from the
	 * underlying VM.
	 */
	private String fName;
	
	/**
	 * A cache of evaluation contexts keyed by java projects. When 
	 * an evaluation is performed, a reuseable evaulation context
	 * is cached for the associated java project. Contexts are discarded
	 * when this VM terminates.
	 */
	private HashMap fEvaluationContexts;
	
	/**
	 * Collection of temporary files deployed to the target for evaluation.
	 * These files are deleted when this target terminates.
	 */
	private HashMap fTempFiles;

	/**
	 * The event dispatcher for this debug target, which runs in its
	 * own thread.
	 */
	private EventDispatcher fEventDispatcher= null;
	 
	/**
	 * Creates a new JDI debug target for the given virtual machine.
	 * 
	 * @param jvm the underlying VM
	 * @param name the name to use for this VM, or <code>null</code>
	 * 	if the name should be retrieved from the underlying VM
	 * @param supportsTermiante whether the terminate action
	 *  is supported by this debug target
	 * @param supportsDisconnect whether the disconnect action is
	 * 	supported by this debug target
	 * @param process the system process assocated with the
	 * 	underlying VM, or <code>null</code> if no system process
	 *  is available (for example, a remote VM)
	 */
	public JDIDebugTarget(VirtualMachine jvm, String name, boolean supportTerminate, boolean supportDisconnect, IProcess process) {
		super(null);
		setDebugTarget(this);
		setSupportsTerminate(supportTerminate);
		setSupportsDisconnect(supportDisconnect);
		setVM(jvm);
		getVM().setDebugTraceMode(VirtualMachine.TRACE_NONE);
		setProcess(process);
		setTerminated(false);
		setName(name);
		setBreakpoints(new ArrayList(5));
		setThreadList(new ArrayList(5));
		initialize();
	}

	/**
	 * Returns the event dispatcher for this debug target.
	 * There is one event dispatcher per debug target.
	 * 
	 * @return event dispatcher
	 */
	protected EventDispatcher getEventDispatcher() {
		return fEventDispatcher;
	}
	
	/**
	 * Sets the event dispatcher for this debug target.
	 * Set once at initialization.
	 * 
	 * @param dispatcher event dispatcher
	 * @see #initialize()
	 */
	private void setEventDispatcher(EventDispatcher dispatcher) {
		fEventDispatcher = dispatcher;
	}
	
	/**
	 * Returns the list of threads contained in this debug target.
	 * 
	 * @return list of threads
	 */
	protected List getThreadList() {
		return fThreads;
	}
	
	/**
	 * Sets the list of threads contained in this debug target.
	 * Set to an empty collection on creation. Threads are
	 * added and removed as they start and end. On termination
	 * this collection is set to the immutable singleton empty list.
	 * 
	 * @param threads empty list
	 */
	private void setThreadList(List threads) {
		fThreads = threads;
	}
	
	/**
	 * Returns the collection of breakpoints installed in this
	 * debug target.
	 * 
	 * @return list of installed breakpoints - instances of 
	 * 	<code>IJavaBreakpoint</code>
	 */
	protected List getBreakpoints() {
		return fBreakpoints;
	}
	
	/**
	 * Sets the list of breakpoints installed in this debug
	 * target. Set to an empty list on creation.
	 * 
	 * @param breakpoints empty list
	 */
	private void setBreakpoints(List breakpoints) {
		fBreakpoints = breakpoints;
	}
		
	/**
	 * Notifies this target that the underlying VM has started.
	 * This is the first event received from the VM.
	 * The VM is resumed. This event is not generated when
	 * an attach is made to a VM that is already running
	 * (has already started up).
	 * 
	 * @param event VM start event
	 */
	protected void handleVMStart(VMStartEvent event) {
		try {
			List threads = getThreadList();
			for (int i= 0; i < threads.size(); i++) {
				((JDIThread) threads.get(i)).setRunning(true);
			}
			getVM().resume();
		} catch (RuntimeException e) {
			internalError(e);
		}
	}
	 
	/**
	 * Initialize event requests and state from the underying VM.
	 * This method is synchronized to ensure that we do not start
	 * to process an events from the target until our state is
	 * initialized.
	 */
	protected synchronized void initialize() {
		setEventDispatcher(new EventDispatcher(this));
		initializeRequests();
		initializeState();
		fireCreationEvent();
		initializeBreakpoints();
		new Thread(getEventDispatcher(), JDIDebugModel.getPluginIdentifier() + JDIDebugModelMessages.getString("JDIDebugTarget.JDI_Event_Dispatcher")).start(); //$NON-NLS-1$
	}
	
	/**
	 * Adds all of the pre-existing threads to this debug target.  
	 */
	protected void initializeState() {

		List threads= null;
		try {
			threads= getVM().allThreads();
		} catch (RuntimeException e) {
			internalError(e);
		}
		if (threads != null) {
			Iterator initialThreads= threads.iterator();
			while (initialThreads.hasNext()) {
				createThread((ThreadReference) initialThreads.next());
			}
		}
	}
	 
	/**
	 * Registers event handlers for thread creation,
	 * thread termination. As well, an event handler
	 * is created that attempts to create an instance
	 * of <code>java.lang.ThreadDeath</code>.
	 * 
	 * @see ThreadTerminator 
	 */
	protected void initializeRequests() {
		new ThreadStartHandler();
		new ThreadDeathtHandler();		
		new ThreadTerminator();
	}

	/**
	 * Installs all Java breakpoints that currently exist in
	 * the breakpoint manager
	 */
	protected void initializeBreakpoints() {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener(this);
		IBreakpoint[] bps = (IBreakpoint[]) manager.getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] instanceof IJavaBreakpoint) {
				breakpointAdded((IJavaBreakpoint)bps[i]);
			}
		}
	}
	
	/**
	 * Creates, adds and returns a thread for the given
	 * underlying thread reference. A creation event
	 * is fired for the thread.
	 * 
	 * @param thread underlying thread
	 * @return model thread
	 */
	protected JDIThread createThread(ThreadReference thread) {
		JDIThread jdiThread= new JDIThread(this, thread);
		getThreadList().add(jdiThread);
		jdiThread.fireCreationEvent();
		return jdiThread;
	}
	
	/**
	 * @see IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() {
		List threads = getThreadList();
		return (IThread[])threads.toArray(new IThread[threads.size()]);
	}
	
	/**
	 * @see IDebugTarget#getElementType()
	 */
	public int getElementType() {
		return DEBUG_TARGET;
	}
	
	/**
	 * @see ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return false;
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return false;
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return supportsTerimate() && !isTerminated();
	}

	/**
	 * @see IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		return supportsDisconnect() && !isDisconnected();
	}
	
	/**
	 * Returns whether this debug target supports disconnecting.
	 * 
	 * @return whether this debug target supports disconnecting
	 */
	protected boolean supportsDisconnect() {
		return fSupportsDisconnect;
	}
	
	/**
	 * Sets whether this debug target supports diconnection.
	 * Set on creation.
	 * 
	 * @param supported <code>true</code> if this target supports
	 * 	disconnection, otherwise <code>false</code>
	 */
	private void setSupportsDisconnect(boolean supported) {
		fSupportsDisconnect = supported;
	}
	
	/**
	 * Returns whether this debug target supports termination.
	 * 
	 * @return whether this debug target supports termination
	 */
	protected boolean supportsTerimate() {
		return fSupportsTerminate;
	}
	
	/**
	 * Sets whether this debug target supports termination.
	 * Set on creation.
	 * 
	 * @param supported <code>true</code> if this target supports
	 * 	termination, otherwise <code>false</code>
	 */
	private void setSupportsTerminate(boolean supported) {
		fSupportsTerminate = supported;
	}
	
	/**
	 * Returns whether this debug target supports hot code replace.
	 * 
	 * @return whether this debug target supports hot code replace
	 */
	protected boolean supportsHotCodeReplace() {
		if (!isTerminated() && getVM() instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			try {
				return ((org.eclipse.jdi.hcr.VirtualMachine) getVM()).canReloadClasses();
			} catch (UnsupportedOperationException e) {
				// This is not an error condition - UnsupportedOperationException is thrown when a VM does
				// not support HCR
			}
		}
		return false;
	}

	/**
	 * @see IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {

		if (isTerminated() || isDisconnected()) {
			// already done
			return;
		}

		if (!canDisconnect()) {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_disconnect")); //$NON-NLS-1$
		}

		try {
			getVM().dispose();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_disconnecting"), new String[] {e.toString()}), e); //$NON-NLS-1$
			terminate0();
		}

	}
	
	/**
	 * Returns the underlying virtual machine associated with this
	 * debug target.
	 * 
	 * @return the underlying VM
	 */
	protected VirtualMachine getVM() {
		return fVirtualMachine;
	}
	
	/**
	 * Sets the underlying VM associated with this debug
	 * target. Set on creation.
	 * 
	 * @param vm underlying VM
	 */
	private void setVM(VirtualMachine vm) {
		fVirtualMachine = vm;
	}
	
	/**
	 * Returns the underlying VM's event request manager.
	 * 
	 * @return event request manager
	 */
	protected EventRequestManager getEventRequestManager() {
		return getVM().eventRequestManager();
	}

	/**
	 * Notifies this target that the specified types have been changed and
	 * should be replaced. A fully qualified name of each type must
	 * be supplied.
	 *
	 * Breakpoints are reinstalled automatically when the new
	 * types are loaded.
	 *
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>The target VM was unable to reload a type due to a shape
	 * change</li>
	 * </ul>
	 */
	public void typesHaveChanged(String[] typeNames) throws DebugException {
			
		if (supportsHotCodeReplace()) {
			org.eclipse.jdi.hcr.VirtualMachine vm= (org.eclipse.jdi.hcr.VirtualMachine) getVM();
			int result= org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE;
			try {
				result= vm.classesHaveChanged(typeNames);
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_replacing_types"), new String[] {e.toString()}), e); //$NON-NLS-1$
			}
			switch (result) {
				case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_SUCCESS:
					break;
				case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_IGNORED:
					targetRequestFailed(JDIDebugModelMessages.getString("JDIDebugTarget.hcr_ignored"), null); //$NON-NLS-1$
					break;
				case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE:
					targetRequestFailed(JDIDebugModelMessages.getString("JDIDebugTarget.hcr_failed"), null); //$NON-NLS-1$
					break;
			}
		} else {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_hcr")); //$NON-NLS-1$
		}
		
	}

	/**
	 * Finds and returns the JDI thread for the associated thread reference, 
	 * or <code>null</code> if not found.
	 * 
	 * @param the underlying thread reference
	 * @return the associated model thread
	 */
	protected JDIThread findThread(ThreadReference tr) {
		List threads = getThreadList();
		for (int i= 0; i < threads.size(); i++) {
			JDIThread t= (JDIThread) threads.get(i);
			if (t.getUnderlyingThread().equals(tr))
				return t;
		}
		return null;
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			try {
				setName(getVM().name());
			} catch (RuntimeException e) {
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_retrieving_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
				return getUnknownMessage();
			}
		}
		return fName;
	}
	
	/**
	 * Sets the name of this debug target. Set on creation,
	 * and if set to <code>null</code> the name will be
	 * retrieved lazily from the underlying VM.
	 * 
	 * @param name the name of this VM or <code>null</code>
	 * 	if the name should be retreived from the underlying VM
	 */
	protected void setName(String name) {
		fName = name;
	}
	
	/**
	 * Sets the process associated with this debug target,
	 * possibly <code>null</code>. Set on creation.
	 * 
	 * @param process the system process associated with the
	 * 	underlying VM, or <code>null</code> if no process is
	 * 	associated with this debug target (for example, a remote
	 * 	VM).
	 */
	protected void setProcess(IProcess process) {
		fProcess = process;
	}
	
	/**
	 * @see IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}

	/**
	 * Resumes the given thread
	 * 
	 * @param underlying thread reference
	 * @deprecated this method to be removed
	 */
	protected void resume(ThreadReference thread) {
		try {
			thread.resume();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
	}
	
	/**
	 * Notification the underlying VM has died. Updates
	 * the state of this target to be terminated.
	 * 
	 * @param event VM death event
	 */
	protected void handleVMDeath(VMDeathEvent event) {
		terminate0();
	}

	/**
	 * Notification the underlying VM has disconnected.
	 * Updates the state of this target to be terminated.
	 * 
	 * @param event disconnect event
	 */
	protected void handleVMDisconnect(VMDisconnectEvent event) {
		terminate0();
	}
	
	/**
	 * @see ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return false;
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * Sets whether this debug target is terminated
	 * 
	 * @param terminated <code>true</code> if this debug
	 * 	target is terminated, otherwise <code>false</code>
	 */
	protected void setTerminated(boolean terminated) {
		fTerminated = terminated;
	}
	
	/**
	 * @see IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return isTerminated();
	}
	
	/**
	 * Creates, enables and returns a class prepare request for the
	 * specified class name in this target, or <code>null</code> if
	 * unable to create the request. This is a utility method used
	 * by event requesters that need to create class prepare requests.
	 * 
	 * @param className regular expression specifying the pattern of
	 * 	class names that will cause the event request to fire. Regular
	 * 	expressions may begin with a '*', end with a '*', or be an exact
	 * 	match.
	 */
	protected ClassPrepareRequest createClassPrepareRequest(String className) {
		EventRequestManager manager= getEventRequestManager();
		ClassPrepareRequest req= null;
		try {
			req= manager.createClassPrepareRequest();
			req.addClassFilter(className);
			req.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			req.enable();
		} catch (RuntimeException e) {
			internalError(e);
			return null;
		}
		return req;
	}

	/**
	 * @see ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_resume")); //$NON-NLS-1$
	}

	/**
	 * Notifiation a breakpoint has been added to the
	 * breakpoint manager. If the breakpoint is a Java
	 * breakpoint and this target is not terminated,
	 * the breakpoint is installed.
	 * 
	 * @param breakpoint the breakpoint added to
	 * 	the breakpoint manager
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		if (breakpoint instanceof JavaBreakpoint) {
			try {
				((JavaBreakpoint)breakpoint).addToTarget(this);
				getBreakpoints().add(breakpoint);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * Notification that one or more attributes of the
	 * given breakpoint has changed. If the breakpoint
	 * is a Java breakpoint, the associated event request
	 * in the underlying VM is updated to reflect the
	 * new state of the breakpoint.
	 * 
	 * @param breakpoint the breakpoint that has changed
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (isTerminated() || isDisconnected()) {
			return;
		}		
		if (breakpoint instanceof JavaBreakpoint) {	
			try {
				((JavaBreakpoint)breakpoint).changeForTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}
	
	/**
	 * Notification that the given breakpoint has been removed
	 * from the breakpoint manager. If this target is not terminated,
	 * the breakpoint is removed from the underlying VM.
	 * 
	 * @param breakpoint the breakpoint has been removed from
	 *  the breakpoint manager.
	 */
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {
		if (isTerminated() || isDisconnected()) {
			return;
		}		
		if (breakpoint instanceof JavaBreakpoint) {
			try {
				((JavaBreakpoint)breakpoint).removeFromTarget(this);
				getBreakpoints().remove(breakpoint);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * @see ISuspendResume
	 *
	 * Not supported
	 */
	public void suspend() throws DebugException {
		notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_suspend")); //$NON-NLS-1$
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		if (!canTerminate()) {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_termination")); //$NON-NLS-1$
		}
		try {
			getVM().exit(1);
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_terminating"), new String[] {e.toString()}), e); //$NON-NLS-1$
			terminate0();
		}
	}

	/**
	 * Updates the state of this target to be terminated,
	 * if not already termianted. 
	 * <p>
	 * All threads are removed from this target.
	 * This target is removed as a breakpoint listener,
	 * and all breakpoints are removed from this target.
	 * Temporary .class files created for evaluation
	 * are deleted, and eveluation contexts are cleared.
	 * Finally a terminate event is fired.
	 * </p>
	 */
	protected void terminate0() {
		if (!isTerminated()) {
			setTerminated(true);
			removeAllThreads();
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
			removeAllBreakpoints();
			cleanupTempFiles();
			if (fEvaluationContexts != null) {
				fEvaluationContexts.clear();
			}
			fireTerminateEvent();
		}
	}

	/**
	 * Remvoes all threads from this target's collection
	 * of threads, firing a terminate event for each.
	 */
	protected void removeAllThreads() {
		Iterator itr= getThreadList().iterator();
		setThreadList(Collections.EMPTY_LIST);
		while (itr.hasNext()) {
			JDIThread child= (JDIThread) itr.next();
			child.terminated();
		}
	}

	/**
	 * Removes all breakpoints from this target, such
	 * that each breakpoint can update its install
	 * count.
	 */
	protected void removeAllBreakpoints() {
		Iterator breakpoints= getBreakpoints().iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint= (JavaBreakpoint) breakpoints.next();
			try {
				breakpoint.removeFromTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
		getBreakpoints().clear();
	}

	/**
	 * Returns VirtualMachine.classesByName(String),
	 * logging any JDI exceptions.
	 *
	 * @see com.sun.jdi.VirtualMachine
	 */
	protected List jdiClassesByName(String className) {
		try {
			return getVM().classesByName(className);
		} catch (VMDisconnectedException e) {
			if (isDisconnected() || isTerminated()) {
				return Collections.EMPTY_LIST;
			}
			logError(e);
		} catch (RuntimeException e) {
			internalError(e);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see IJavaDebugTarget#findVariable(String)
	 */
	public IVariable findVariable(String varName) throws DebugException {
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			JDIThread thread = (JDIThread)threads[i];
			IVariable var = thread.findVariable(varName);
			if (var != null) {
				return var;
			}
		}
		return null;
	}
	
	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaDebugTarget.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Deploys the given class files for the given evaluation context.
	 *
	 * <p>Currently, this involves writing them to the output folder of the
	 * associated Java Project.</p>
	 *
	 * @exception DebugException if this fails due to a lower level exception.
	 */
	protected void deploy(final byte[][] classFiles, final String[][] classFileNames, IEvaluationContext context) throws DebugException {
		if (fTempFiles == null) {
			fTempFiles = new HashMap(10);
		}
		IJavaProject javaProject = context.getProject();
		final IProject project= javaProject.getProject();
		IPath projectPath= project.getFullPath();
		
		// determine the folder in which output packages are located
		IPath oPath = null;
		try {
			oPath = javaProject.getOutputLocation();
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}

		// create the files in a workspace runnable
		final IWorkspace workspace= javaProject.getProject().getWorkspace();
		final IPath outputPath= oPath;
		final boolean outputToProject= outputPath.equals(projectPath);
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {					
					// allow for output path to be project itself
					IContainer outputContainer= null;
					if (outputToProject) {
						outputContainer= project;
					} else {
						outputContainer= workspace.getRoot().getFolder(outputPath);
					}
					for (int i = 0; i < classFiles.length; i++) {
						String[] compoundName = classFileNames[i];
						//create required folders
						IContainer parent = outputContainer;
						for (int j = 0; j < (compoundName.length - 1); j++) {
							IFolder folder = parent.getFolder(new Path(compoundName[j]));
							if (!folder.exists()) {
								folder.create(false, true, null);
							}
							parent = folder;
						}
						String name = compoundName[compoundName.length - 1] + ".class"; //$NON-NLS-1$
						IPath path = new Path(name);
						if (fTempFiles.get(path) == null) {
							IFile file = parent.getFile(path);
							if (file.exists()) {
								file.delete(true, null);
							}
							file.create(new ByteArrayInputStream(classFiles[i]), false, null);
							fTempFiles.put(path, file);
						}						
					}	
				}
			};
		try {	
			workspace.run(runnable, null);				
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}
	
	/**
	 * Deletes deployed temporary evaluation class files
	 */
	protected void cleanupTempFiles() {
		if (fTempFiles == null) {
			return;
		}
		if (fTempFiles.size() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IResource[] files = new IResource[fTempFiles.size()];
			files = (IResource[])fTempFiles.values().toArray(files);
			try {
				workspace.delete(files, false, null);
			} catch (CoreException e) {
				logError(e);
			}
			fTempFiles.clear();
		}
	}

	/**
	 * Returns an evaluation context for the given Java project, creating
	 * one if not yet created.
	 * 
	 * @param project the java project for which a context is required
	 * @return an evaluation context
	 */
	protected IEvaluationContext getEvaluationContext(IJavaProject project) {
		if (fEvaluationContexts == null) {
			fEvaluationContexts = new HashMap(2);
		}
		IEvaluationContext context = (IEvaluationContext)fEvaluationContexts.get(project);
		if (context == null) {
			context = project.newEvaluationContext();
			fEvaluationContexts.put(project, context);
		}
		return context;
	}
	
	/**
	 * The JDIDebugPlugin is shutting down.
	 * Shutdown the event dispatcher.
	 */
	protected void shutdown() {
		getEventDispatcher().shutdown();
		cleanupTempFiles();
	}
	
	/**
	 * Returns the CRC-32 of the entire class file contents associated with
	 * given type, on the target VM, or <code>null</code> if the type is
	 * not loaded, or a CRC for the type is not known.
	 * 
	 * @param typeName fully qualified name of the type for which a
	 *    CRC is required. For example, "com.example.Example".
	 * @return 32 bit CRC, or <code>null</code>
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul>
	 * <li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */
	protected Integer getCRC(String typeName) throws DebugException {
		if (getVM() instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			List classes = jdiClassesByName(typeName);
			if (!classes.isEmpty()) {
				ReferenceType type = (ReferenceType)classes.get(0);
				if (type instanceof org.eclipse.jdi.hcr.ReferenceType) {
					try {
						org.eclipse.jdi.hcr.ReferenceType rt = (org.eclipse.jdi.hcr.ReferenceType)type;
						if (rt.isVersionKnown()) {
							return new Integer(rt.getClassFileVersion());
						}
					} catch (RuntimeException e) {
						targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_retrieving_version_information"), new String[] {e.toString(), type.name()}), e); //$NON-NLS-1$
						return null;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds the given listener to this target's event dispatcher's
	 * table of listeners for the specified event request. The listener
	 * will be notified each time the event occurrs.
	 * 
	 * @param listener the listener to register
	 * @param request the event request
	 */
	protected void addJDIEventListener(IJDIEventListener listener, EventRequest request) {
		getEventDispatcher().addJDIEventListener(listener, request);
	}
	
	/**
	 * Removes the given listener from this target's event dispatcher's
	 * table of listeners for the specifed event request. The listener
	 * will no longer be notified when the event occurrs. Listeners
	 * are responsible for deleting the event request if desired.
	 * 
	 * @param listener the listener to remove
	 * @param request the event request
	 */
	protected void removeJDIEventListener(IJDIEventListener listener, EventRequest request) {
		getEventDispatcher().removeJDIEventListener(listener, request);
	}
	
	/**
	 * Sets the instance of <code>java.lang.ThreadDeath</code> used
	 * by this target to terminate threads. The instance is created
	 * by this target's thread terminator.
	 * 
	 * @see ThreadTerminator
	 */
	protected void setThreadDeathInstance(ObjectReference threadDeath) {
		fThreadDeath = threadDeath;
	}
	
	/**
	 * Returns the instance of <code>java.lang.ThreadDeath</code> in the
	 * target VM used by this target to terminate threads, or <code>null</code>
	 * if there is no instance to use.
	 * 
	 * @see ThreadTerminator
	 */
	protected ObjectReference getThreadDeathInstance() {
		return fThreadDeath;
	}
	
	/**
	 * A jdi debug target creates a thread terminator on initialization.
	 * A thread terminator listens to all class loads, and when a class
	 * is loaded, it attempts to create an instance of <code>java.lang.ThreadDeath</code>,
	 * in the thread in which the class load occurred. The instance of
	 * <code>ThreadDeath</code> is used on user requests to terminate
	 * threads.
	 * <p>
	 * The number of times a thread terminator attempts to create an
	 * instance of <code>ThreadDeath</code> is limited, as instantiation
	 * of <code>ThreadDeath</code> does not work on some VMs during startup. 
	 * <code>
	 * 
	 * @see JDIThread#terminate()
	 */
	class ThreadTerminator implements IJDIEventListener {
		
		/**
		 * The maximum number of times a thread terminator attempts to
		 * create an instance of <code>java.lang.ThreadDeath</code>.
		 */
		protected static final int MAX_ATTEMPTS = 1;

		/**
		 * Number of attempts this terminator has made to create
		 * instance of <code>java.lang.ThreadDeath</code>.
		 */
		protected int fAttempts = 0;
		
		/**
		 * The class prepare request used to listen for ALL class loads
		 * so that when the very first class is loaded, an attempt is made
		 * to create an instance of <code>java.lang.ThreadDeath</code>. 
		 */
		protected ClassPrepareRequest fClassPrepareReq;
		
		/**
		 * Constructs a new thread terminator which attemps to create
		 * an instance of <code>java.lang.ThreadDeath</code> for its
		 * debug target.
		 */
		protected ThreadTerminator() {
			createRequest();
		}
		
		/**
		 * Attempts to create an instance of <code>java.lang.ThreadDeath</code>
		 * in the target VM. This instance will be used to terminate threads in
		 * the target VM. Note that if a thread death instance is not created
		 * threads will return <code>false</code> to <code>ITerminate#canTerminate()</code>.
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			if (getAttempts() == MAX_ATTEMPTS) {
				deleteRequest();
				return true;
			}
			incrementAttempts();
			ThreadReference threadRef = ((ClassPrepareEvent)event).thread();
			// Try to create an instance of java.lang.ThreadDeath
			// NB: This has to be done when the VM is interrupted by an event
			if (fThreadDeath == null) {
				JDIThread jt = findThread(threadRef);
				if (jt != null && jt.isPerformingEvaluation()) {
					// cannot perform nested evaluations
					return true;
				}
				//non NLS
				List classes= jdiClassesByName("java.lang.ThreadDeath"); //$NON-NLS-1$
				if (!classes.isEmpty()) {
					ClassType threadDeathClass= (ClassType) classes.get(0);
					Method constructor= null;
					try {
						constructor= threadDeathClass.concreteMethodByName("<init>", "()V"); //$NON-NLS-2$ //$NON-NLS-1$
					} catch (RuntimeException e) {
						internalError(e);
						return true;
					}
					ObjectReference threadDeath = null;
					try {
						threadDeath= threadDeathClass.newInstance(threadRef, constructor, new LinkedList(), ClassType.INVOKE_SINGLE_THREADED);
					} catch (ClassNotLoadedException e) {
						logError(e);
					} catch (InvalidTypeException e) {
						logError(e);
					} catch (InvocationException e) {
						logError(e);
					} catch (IncompatibleThreadStateException e) {
						logError(e);
					} catch (RuntimeException e) {
						logError(e);
					}
					if (threadDeath != null) {
						try {
							threadDeath.disableCollection(); // This object is going to be used for the lifetime of the VM. 							
						} catch (RuntimeException e) {
							logError(e);
							return true;
						}
						setThreadDeathInstance(threadDeath);
						deleteRequest();
					}
				}
			}
			return true;
		}
		
		/**
		 * Returns the number of attempts this thread terminator
		 * has made to create an instance of <code>java.lang.ThreadDeath</code>.
		 * 
		 * @return number of attempts
		 */
		protected int getAttempts() {
			return fAttempts;
		}
		
		/**
		 * Increments the attempt counter
		 */
		protected void incrementAttempts() {
			fAttempts++;
		}
		
		/** 
		 * Returns the event request used to listen to all class loads
		 * or <code>null<code> if there is not currently a request.
		 * 
		 * @return universal class prepare request, or <code>null</code>
		 */
		protected ClassPrepareRequest getRequest() {
			return fClassPrepareReq;
		}
		
		/** 
		 * Sets the event request used to listen to all class loads.
		 * Can be <code>null</code> if the attempt to create the
		 * request fails.
		 * 
		 * @param event request
		 */
		protected void setRequest(ClassPrepareRequest request) {
			fClassPrepareReq = request;
		}
		
		/**
		 * Creates and registers a request to listen to all class
		 * loads.
		 */
		protected void createRequest() {
			ClassPrepareRequest request = createClassPrepareRequest("*"); //$NON-NLS-1$
			if (request != null) {
				setRequest(request);
				addJDIEventListener(this, getRequest());
			}
		}
		
		/**
		 * Deregisters this event listener and deletes any outstanding
		 * class prepare request.
		 */
		protected void deleteRequest() {
			if (getRequest() != null) {
				removeJDIEventListener(this, getRequest());
				try {
					getEventRequestManager().deleteEventRequest(getRequest());
				} catch (RuntimeException e) {
					logError(e);
				}
				setRequest(null);
			}
		}
	}
	
	/**
	 * An event handler for thread start events. When a thread
	 * starts in the target VM, a model thread is created.
	 */
	class ThreadStartHandler implements IJDIEventListener {
		
		protected ThreadStartHandler() {
			createRequest();
		} 
		
		/**
		 * Creates and registers a request to handle all thread start
		 * events
		 */
		protected void createRequest() {
			try {
				EventRequest req= getEventRequestManager().createThreadStartRequest();
				req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
				req.enable();
				addJDIEventListener(this, req);
			} catch (RuntimeException e) {
				logError(e);
			}
		}

		/**
		 * Creates a model thread for the underlying jdi thread
		 * and adds it to the collection of threads for this 
		 * debug target. As a side effect of creating the thread,
		 * a create event is fired for the model thread.
		 * 
		 * @param event a thread start event
		 * @param target the target in which the thread started
		 * @return <code>true</code> - the thread should be resumed
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			ThreadReference thread= ((ThreadStartEvent)event).thread();
			JDIThread jdiThread= findThread(thread);
			if (jdiThread == null) {
				jdiThread = createThread(thread);
			}	
			jdiThread.setRunning(true);
			return true;
		}
	}
	
	/**
	 * An event handler for thread death events. When a thread
	 * dies in the target VM, its associated model thread is
	 * removed from the debug target.
	 */
	class ThreadDeathtHandler implements IJDIEventListener {
		
		protected ThreadDeathtHandler() {
			createRequest();
		}
		
		/**
		 * Creates and registers a request to listen to thread
		 * death events.
		 */
		protected void createRequest() {
			try {
				EventRequest req= getEventRequestManager().createThreadDeathRequest();
				req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
				req.enable();
				addJDIEventListener(this, req);	
			} catch (RuntimeException e) {
				logError(e);
			}	
		}
				
		/**
		 * Locates the model thread associated with the underlying jdi thread
		 * that has terminated, and removes it from the collection of
		 * threads belonging to this debug target. A terminate event is
		 * fired for the model thread.
		 *
		 * @param event a thread death event
		 * @param target the target in which the thread died
		 * @return <code>true</code> - the thread should be resumed
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			ThreadReference ref= ((ThreadDeathEvent)event).thread();
			JDIThread thread= findThread(ref);
			if (thread != null) {
				getThreadList().remove(thread);
				thread.terminated();
			}
			return true;
		}
		
	}
}

