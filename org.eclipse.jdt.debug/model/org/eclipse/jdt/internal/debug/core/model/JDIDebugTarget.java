package org.eclipse.jdt.internal.debug.core.model;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.internal.debug.core.EventDispatcher;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
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

public class JDIDebugTarget extends JDIDebugElement implements IJavaDebugTarget, ILaunchListener {
		
	/**
	 * Threads contained in this debug target. When a thread
	 * starts it is added to the list. When a thread ends it
	 * is removed from the list.
	 */
	private ArrayList fThreads;
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
	 * Whether terminated
	 */
	private boolean fTerminated;
	
	/**
	 * Whether in the process of terminating
	 */
	private boolean fTerminating;
	/**
	 * Whether disconnected
	 */
	private boolean fDisconnected;
	/**
	 * Whether disconnect is supported.
	 */
	private boolean fSupportsDisconnect;
	/**
	 * Collection of breakpoints added to this target. Values are of type <code>IJavaBreakpoint</code>.
	 */
	private List fBreakpoints;
	
	/**
	 * Collection of types that have attempted HCR, but failed.
	 * The types are stored by their fully qualified names.
	 */
	private Set fOutOfSynchTypes;
	/**
	 * Whether or not this target has performed a hot code replace.
	 */
	private boolean fHasHCROccurred;
	
	/**
	 * The name of this target - set by the client on creation, or retrieved from the
	 * underlying VM.
	 */
	private String fName;

	/**
	 * The event dispatcher for this debug target, which runs in its
	 * own thread.
	 */
	private EventDispatcher fEventDispatcher= null;
	
	/**
	 * The thread start event handler
	 */
	private ThreadStartHandler fThreadStartHandler= null;
	
	/**
	 * Whether this VM is suspended.
	 */
	private boolean fSuspended = true;
	
	/**
	 * Whether the VM should be resumed on startup
	 */
	private boolean fResumeOnStartup = false; 
	
	/**
	 * The launch this target is contained in
	 */
	private ILaunch fLaunch;	
	
	/**
	 * Count of the number of suspend events in this target
	 */
	private int fSuspendCount = 0;
	
	/**
	 * Evaluation engine cache by Java project. Engines
	 * are disposed when this target terminates.
	 */
	private HashMap fEngines;
	
	/**
	 * List of step filters - each string is a pattern/fully qualified
	 * name of a type to filter.
	 */
	private String[] fStepFilters = null;
	
	/**
	 * Step filter state mask.
	 */
	private int fStepFilterMask = 0;
	
	/**
	 * Step filter bit mask - indicates if step filters are enabled.
	 */
	private static final int STEP_FILTERS_ENABLED = 0x001;
	
	/**
	 * Step filter bit mask - indicates if sythetic methods are filtered.
	 */	
	private static final int FILTER_SYNTHETICS = 0x002;
	
	/**
	 * Step filter bit mask - indicates if static initializers are filtered.
	 */		
	private static final int FILTER_STATIC_INITIALIZERS = 0x004;
	
	/**
	 * Step filter bit mask - indicates if constructors are filtered.
	 */		
	private static final int FILTER_CONSTRUCTORS = 0x008;
	
	/**
	 * Mask used to flip individual bit masks via XOR
	 */
	private static final int XOR_MASK = 0xFFF;
	/**
	 * Whether this debug target is currently performing a hot code replace
	 */
	private boolean fIsPerformingHotCodeReplace= false;
	
	 
	/**
	 * Creates a new JDI debug target for the given virtual machine.
	 * 
	 * @param jvm the underlying VM
	 * @param name the name to use for this VM, or <code>null</code>
	 * 	if the name should be retrieved from the underlying VM
	 * @param supportsTerminate whether the terminate action
	 *  is supported by this debug target
	 * @param supportsDisconnect whether the disconnect action is
	 * 	supported by this debug target
	 * @param process the system process associated with the
	 * 	underlying VM, or <code>null</code> if no system process
	 *  is available (for example, a remote VM)
	 * @param resume whether the VM should be resumed on startup.
	 *  Has no effect if the VM is already resumed/running when
	 *  the connection is made.  
	 */
	public JDIDebugTarget(ILaunch launch, VirtualMachine jvm, String name, boolean supportTerminate, boolean supportDisconnect, IProcess process, boolean resume) {
		super(null);
		setLaunch(launch);
		setResumeOnStartup(resume);
		setDebugTarget(this);
		setSupportsTerminate(supportTerminate);
		setSupportsDisconnect(supportDisconnect);
		setVM(jvm);
		getVM().setDebugTraceMode(VirtualMachine.TRACE_NONE);
		setProcess(process);
		setTerminated(false);
		setTerminating(false);
		setDisconnected(false);
		setName(name);
		setBreakpoints(new ArrayList(5));
		setThreadList(new ArrayList(5));
		setOutOfSynchTypes(new ArrayList(0));
		setHCROccurred(false);
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/**
	 * Returns the event dispatcher for this debug target.
	 * There is one event dispatcher per debug target.
	 * 
	 * @return event dispatcher
	 */
	public EventDispatcher getEventDispatcher() {
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
	protected ArrayList getThreadList() {
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
	private void setThreadList(ArrayList threads) {
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
	 * (has already started up). The VM is resumed as specified
	 * on creation.
	 * 
	 * @param event VM start event
	 */
	public void handleVMStart(VMStartEvent event) {
		if (isResumeOnStartup()) {
			try {
				setSuspended(true);
				resume();
			} catch (DebugException e) {
				logError(e);
			}
		}
	}
	 
	/**
	 * Initialize event requests and state from the underlying VM.
	 * This method is synchronized to ensure that we do not start
	 * to process an events from the target until our state is
	 * initialized.
	 */
	protected synchronized void initialize() {
		setEventDispatcher(new EventDispatcher(this));
		setRequestTimeout(JDIDebugModel.getPreferences().getInt(JDIDebugModel.PREF_REQUEST_TIMEOUT));
		initializeRequests();
		initializeState();
		initializeBreakpoints();
		getLaunch().addDebugTarget(this);
		fireCreationEvent();
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
				createRunningThread((ThreadReference) initialThreads.next());
			}
		}
		
		if (isResumeOnStartup()) {
			setSuspended(false);
		}
	}
	 
	/**
	 * Registers event handlers for thread creation,
	 * thread termination.
	 */
	protected void initializeRequests() {
		setThreadStartHandler(new ThreadStartHandler());
		new ThreadDeathHandler();		
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
	 * Returns <code>null</code> if during the creation of the thread this target
	 * is set to the disconnected state.
	 * 
	 * @param thread underlying thread
	 * @return model thread
	 */
	protected JDIThread createThread(ThreadReference thread) {
		JDIThread jdiThread= null;
		try {
			jdiThread= new JDIThread(this, thread);
		} catch (ObjectCollectedException exception) {
			// ObjectCollectionException can be thrown if the thread has already
			// completed (exited) in the VM.
			return null;
		}
		if (isDisconnected()) {
			return null;
		}
		getThreadList().add(jdiThread);
		jdiThread.fireCreationEvent();
		return jdiThread;
	}
	
	/**
	 * Creates a new JDI thread from the given ThreadReference and initializes
	 * its state to "Running." This method should be called with extreme care.
	 * 
	 * @see JDIDebugTarget#createThread(ThreadReference)
	 */
	protected JDIThread createRunningThread(ThreadReference thread) {
		JDIThread jdiThread= null;
		try {
			jdiThread= new JDIThread(this, thread);
		} catch (ObjectCollectedException exception) {
			// ObjectCollectionException can be thrown if the thread has already
			// completed (exited) in the VM.
			return null;
		}
		if (isDisconnected()) {
			return null;
		}
		jdiThread.setRunning(true); // Initialize the thread to running.
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
	 * @see ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended() && isAvailable() && !isPerformingHotCodeReplace();
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		if (!isSuspended() && isAvailable()) {
			// only allow suspend if no threads are currently suspended
			IThread[] threads= getThreads();
			for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
				if (((JDIThread)threads[i]).isSuspended()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
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
	 * Sets whether this debug target supports disconnection.
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
	protected boolean supportsTerminate() {
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
	 * @see IJavaDebugTarget#supportsHotCodeReplace()
	 */
	public boolean supportsHotCodeReplace() {
		return supportsJ9HotCodeReplace() || supportsJDKHotCodeReplace();
	}
	
	/**
	 * @see IJavaDebugTarget#supportsInstanceBreakpoints()
	 */
	public boolean supportsInstanceBreakpoints() {
		if (isAvailable() && JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] {1,4})) {
			return getVM().canUseInstanceFilters();
		}
		return false;
	}
	
	/**
	 * Returns whether this debug target supports hot code replace for the J9 VM.
	 * 
	 * @return whether this debug target supports J9 hot code replace
	 */
	public boolean supportsJ9HotCodeReplace() {
		if (isAvailable() && getVM() instanceof org.eclipse.jdi.hcr.VirtualMachine) {
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
	 * Returns whether this debug target supports hot code replace for JDK VMs.
	 * 
	 * @return whether this debug target supports JDK hot code replace
	 */
	public boolean supportsJDKHotCodeReplace() {
		if (isAvailable() && JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] {1,4})) {
			return getVM().canRedefineClasses();
		}
		return false;
	}
	
	/**
	 * Returns whether this debug target supports popping stack frames.
	 * 
	 * @return whether this debug target supports popping stack frames.
	 */
	public boolean canPopFrames() {
		if (isAvailable() && JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] {1,4})) {
			return getVM().canPopFrames();
		}
		return false;
	}

	/**
	 * @see IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {

		if (isDisconnected()) {
			// already done
			return;
		}

		if (!canDisconnect()) {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_disconnect")); //$NON-NLS-1$
		}

		try {
			getThreadStartHandler().deleteRequest();
			getVM().dispose();
		} catch (VMDisconnectedException e) {
			// if the VM disconnects while disconnecting, perform
			// normal disconnect handling
			disconnected();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_disconnecting"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}

	}
	
	/**
	 * Returns the underlying virtual machine associated with this
	 * debug target.
	 * 
	 * @return the underlying VM
	 */
	public VirtualMachine getVM() {
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
	 * Sets whether this debug target has performed a hot
	 * code replace.
	 */
	public void setHCROccurred(boolean occurred) {
		fHasHCROccurred= occurred;
	}
	
	public void removeOutOfSynchTypes(List qualifiedNames) {
		fOutOfSynchTypes.removeAll(qualifiedNames);
	}
	
	/**
	 * Sets the list of out of synch types
	 * to the given list.
	 */
	private void setOutOfSynchTypes(List qualifiedNames) {
		fOutOfSynchTypes= new HashSet();
		fOutOfSynchTypes.addAll(qualifiedNames);
	}
	
	/**
	 * The given types have failed to be reloaded by HCR.
	 * Add them to the list of out of synch types.
	 */
	public void addOutOfSynchTypes(List qualifiedNames) {
		fOutOfSynchTypes.addAll(qualifiedNames);
	}
	
	/**
	 * Returns whether the given type is out of synch in this
	 * target.
	 */
	public boolean isOutOfSynch(String qualifiedName) {
		if (fOutOfSynchTypes == null || fOutOfSynchTypes.isEmpty()) {
			return false;
		}
		return fOutOfSynchTypes.contains(qualifiedName);
	}
	
	/**
	 * @see IJavaDebugTarget#isOutOfSynch()
	 */
	public boolean isOutOfSynch() throws DebugException {
		List threadList= (List) getThreadList().clone();
		Iterator threads= threadList.iterator();
		while (threads.hasNext()) {
			JDIThread thread= (JDIThread)threads.next();
			if (thread.isOutOfSynch()) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * @see IJavaDebugTarget#mayBeOutOfSynch()
	 */
	public boolean mayBeOutOfSynch() throws DebugException {
		List threadList= (List) getThreadList().clone();
		Iterator threads= threadList.iterator();
		while (threads.hasNext()) {
			JDIThread thread= (JDIThread)threads.next();
			if (thread.mayBeOutOfSynch()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether a hot code replace attempt has failed.
	 * 
	 * HCR has failed if there are any out of synch types
	 */
	public boolean hasHCRFailed() {
		return fOutOfSynchTypes != null && !fOutOfSynchTypes.isEmpty();
	}
	
	/**
	 * Returns whether this debug target has performed
	 * a hot code replace
	 */
	public boolean hasHCROccurred() {
		return fHasHCROccurred;
	}
	
	/**
	 * Reinstall all breakpoints installed in the given resources
	 */
	public void reinstallBreakpointsIn(List resources, List classNames) {
		List breakpoints= getBreakpoints();
		IJavaBreakpoint[] copy= new IJavaBreakpoint[breakpoints.size()];
		breakpoints.toArray(copy);
		IJavaBreakpoint breakpoint= null;
		String installedType= null;
		
		for (int i= 0; i < copy.length; i++) {
			breakpoint= copy[i];
			if (breakpoint instanceof JavaLineBreakpoint) {
				try {
					installedType= breakpoint.getTypeName();
					if (classNames.contains(installedType)) {
						breakpointAdded(breakpoint);
					}
				} catch (CoreException ce) {
					logError(ce);
					continue;
				}
			}
		}		
	}

	/**
	 * Finds and returns the JDI thread for the associated thread reference, 
	 * or <code>null</code> if not found.
	 * 
	 * @param the underlying thread reference
	 * @return the associated model thread
	 */
	public JDIThread findThread(ThreadReference tr) {
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
				// execution will not reach this line, as 
				// #targetRequestFailed will throw an exception				
				return null;
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
	 * 	if the name should be retrieved from the underlying VM
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
	 * Notification the underlying VM has died. Updates
	 * the state of this target to be terminated.
	 * 
	 * @param event VM death event
	 */
	public void handleVMDeath(VMDeathEvent event) {
		terminated();
	}

	/**
	 * Notification the underlying VM has disconnected.
	 * Updates the state of this target to be terminated.
	 * 
	 * @param event disconnect event
	 */
	public void handleVMDisconnect(VMDisconnectEvent event) {
		if (isTerminating()) {
			terminated();
		} else {
			disconnected();
		}
	}
	
	/**
	 * @see ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended;
	}
	
	/**
	 * Sets whether this VM is suspended.
	 * 
	 * @param suspended whether this VM is suspended
	 */
	private void setSuspended(boolean suspended) {
		fSuspended = suspended;
	}
	
	/**
	 * Returns whether this target is available to
	 * handle VM requests
	 */
	public boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected());
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
	 * Sets whether this debug target is disconnected
	 * 
	 * @param disconnected <code>true</code> if this debug
	 *  target is disconnected, otherwise <code>false</code>
	 */
	protected void setDisconnected(boolean disconnected) {
		fDisconnected= disconnected;
	}
	
	/**
	 * @see IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return fDisconnected;
	}
	
	
	public ClassPrepareRequest createClassPrepareRequest(String classPattern) {
		return createClassPrepareRequest(classPattern, null);
	}
	
	/**
	 * Creates, enables and returns a class prepare request for the
	 * specified class name in this target, or <code>null</code> if
	 * unable to create the request. Can specify a class exclusion filter
	 * as well.
	 * This is a utility method used by event requesters that need to
	 * create class prepare requests.
	 * 
	 * @param classPattern regular expression specifying the pattern of
	 * 	class names that will cause the event request to fire. Regular
	 * 	expressions may begin with a '*', end with a '*', or be an exact
	 * 	match.
	 *  @param classExclusionPattern regular expression specifying the pattern of
	 * 	class names that will not cause the event request to fire. Regular
	 * 	expressions may begin with a '*', end with a '*', or be an exact
	 * 	match.  May be <code>null</code>.
	 */
	public ClassPrepareRequest createClassPrepareRequest(String classPattern, String classExclusionPattern) {
		EventRequestManager manager= getEventRequestManager();
		ClassPrepareRequest req= null;
		try {
			req= manager.createClassPrepareRequest();
			req.addClassFilter(classPattern);
			if (classExclusionPattern != null) {
				req.addClassExclusionFilter(classExclusionPattern);
			}
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
		resume(true);
	}
	
	/**
	 * @see ISuspendResume#resume()
	 * 
	 * Updates the state of this debug target to resumed,
	 * but does not fire notification of the resumption.
	 */
	public void resumeQuiet() throws DebugException {
		resume(false);
	}
	
	/**
	 * @see ISuspendResume#resume()
	 * 
	 * Updates the state of this debug target, but only fires
	 * notification to listeners if <code>fireNotification</code>
	 * is <code>true</code>.
	 */
	public void resume(boolean fireNotification) throws DebugException {
		if (!isSuspended()) {
			return;
		}
		try {
			setSuspended(false);
			resumeThreads();
			getVM().resume();
			if (fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (RuntimeException e) {
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_resume"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}	
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return breakpoint instanceof IJavaBreakpoint;
	}

	/**
	 * Notification a breakpoint has been added to the
	 * breakpoint manager. If the breakpoint is a Java
	 * breakpoint and this target is not terminated,
	 * the breakpoint is installed.
	 * 
	 * @param breakpoint the breakpoint added to
	 * 	the breakpoint manager
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (!isAvailable()) {
			return;
		}
		if (supportsBreakpoint(breakpoint)) {
			try {
				((JavaBreakpoint)breakpoint).addToTarget(this);
				if (!getBreakpoints().contains(breakpoint)) {
					getBreakpoints().add(breakpoint);
				}
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
		if (!isAvailable()) {
			return;
		}		
		if (supportsBreakpoint(breakpoint)) {	
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
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (!isAvailable()) {
			return;
		}		
		if (supportsBreakpoint(breakpoint)) {
			try {
				((JavaBreakpoint)breakpoint).removeFromTarget(this);
				getBreakpoints().remove(breakpoint);
				Iterator threads = getThreadList().iterator();
				while (threads.hasNext()) {
					((JDIThread)threads.next()).removeCurrentBreakpoint(breakpoint);
				}
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * @see ISuspendResume
	 */
	public void suspend() throws DebugException {
		if (isSuspended()) {
			return;
		}
		try {
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			getVM().suspend();
			suspendThreads();
		} catch (RuntimeException e) {
			setSuspended(false);
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_suspend"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
		
	}
	
	/**
	 * Notifies threads that they have been suspended
	 */
	protected void suspendThreads() {
		Iterator threads = getThreadList().iterator();
		while (threads.hasNext()) {
			((JDIThread)threads.next()).suspendedByVM();
		}
	}
	
	/**
	 * Notifies threads that they have been resumed
	 */
	protected void resumeThreads() {
		Iterator threads = getThreadList().iterator();
		while (threads.hasNext()) {
			((JDIThread)threads.next()).resumedByVM();
		}
	}	
	
	/**
	 * Notifies this VM to update its state in preparation
	 * for a suspend.
	 * 
	 * @param breakpoint the breakpoint that caused the
	 *  suspension
	 */
	public void prepareToSuspendByBreakpoint(JavaBreakpoint breakpoint) {
		setSuspended(true);
		suspendThreads();
	}
	
	/**
	 * Notifies this VM it has been suspended by the
	 * given breakpoint
	 * 
	 * @param breakpoint the breakpoint that caused the
	 *  suspension
	 */
	protected void suspendedByBreakpoint(JavaBreakpoint breakpoint, boolean queueEvent) {
		if (queueEvent) {
			queueSuspendEvent(DebugEvent.BREAKPOINT);
		} else {
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		}
	}	
	
	/**
	 * Notifies this VM suspension has been cancelled
	 * 
	 * @param breakpoint the breakpoint that caused the
	 *  suspension
	 */
	protected void cancelSuspendByBreakpoint(JavaBreakpoint breakpoint) {
		setSuspended(false);
		resumeThreads();
	}	

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (!isAvailable()) {
			return;
		}
		if (!supportsTerminate()) {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_termination")); //$NON-NLS-1$
		}
		try {
			setTerminating(true);
			getThreadStartHandler().deleteRequest();
			getVM().exit(1);
			IProcess process= getProcess();
			if (process != null) {
				process.terminate();
			}
		} catch (VMDisconnectedException e) {
			// if the VM disconnects while exiting, perform 
			// normal termination processing
			terminated();
		} catch (TimeoutException exception) {
			// if there is a timeout see if the associated process is terminated
			IProcess process = getProcess();
			if (process != null && process.isTerminated()) {
				terminated();
			} else {
				// All we can do is disconnect
				disconnected();
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_terminating"), new String[] {e.toString()}), e); //$NON-NLS-1$
		}
	}

	/**
	 * Updates the state of this target to be terminated,
	 * if not already terminated.
	 */
	protected void terminated() {
		setTerminating(false);
		if (!isTerminated()) {
			setTerminated(true);
			setDisconnected(true);
			cleanup();
			fireTerminateEvent();
		}
	}
	
	/**
	 * Updates the state of this target for disconnection
	 * from the VM.
	 */
	protected void disconnected() {
		if (!isDisconnected()) {
			setDisconnected(true);
			cleanup();
			fireTerminateEvent();
		}
	}

	/** 
	 * Cleans up the internal state of this debug
	 * target as a result of a session ending with a
	 * VM (as a result of a disconnect or termination of
	 * the VM).
	 * <p>
	 * All threads are removed from this target.
	 * This target is removed as a breakpoint listener,
	 * and all breakpoints are removed from this target.
	 * </p>
	 */
	protected void cleanup() {
		removeAllThreads();
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getBreakpointManager().removeBreakpointListener(this);
		plugin.getLaunchManager().removeLaunchListener(this);
		removeAllBreakpoints();
		fOutOfSynchTypes.clear();
		if (fEngines != null) {
			Iterator engines = fEngines.values().iterator();
			while (engines.hasNext()) {
				IAstEvaluationEngine engine = (IAstEvaluationEngine)engines.next();
				engine.dispose();
			}
			fEngines.clear();
		}
		fVirtualMachine= null;
	}

	/**
	 * Removes all threads from this target's collection
	 * of threads, firing a terminate event for each.
	 */
	protected void removeAllThreads() {
		Iterator itr= getThreadList().iterator();
		setThreadList(new ArrayList(0));
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
		Iterator breakpoints= ((ArrayList)((ArrayList)getBreakpoints()).clone()).iterator();
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
	public List jdiClassesByName(String className) {
		try {
			return getVM().classesByName(className);
		} catch (VMDisconnectedException e) {
			if (!isAvailable()) {
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
	public IJavaVariable findVariable(String varName) throws DebugException {
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			JDIThread thread = (JDIThread)threads[i];
			IJavaVariable var = thread.findVariable(varName);
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
	 * The JDIDebugPlugin is shutting down.
	 * Shutdown the event dispatcher and do local
	 * cleaup.
	 */
	public void shutdown() {
		getEventDispatcher().shutdown();
		try {
			if (supportsTerminate()) {
				terminate();
			} else if (supportsDisconnect()) {
				disconnect();
			}
		} catch (DebugException e) {
			JDIDebugPlugin.log(e);
		}
		cleanup();
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
						// execution will never reach this line, as
						// #targetRequestFailed will throw an exception						
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#getJavaTypes(String)
	 */
	public IJavaType[] getJavaTypes(String name) throws DebugException {
		try {
			// get java.lang.Class
			List classes = getVM().classesByName(name);
			if (classes.size() == 0) {
				switch (name.charAt(0)) {
					case 'b':
						if (name.equals("boolean")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(true).getJavaType()};
						} else if (name.equals("byte")) { //$NON-NLS-1$
							return new IJavaType[] {newValue((byte)1).getJavaType()};
						}
						break;
					case 'i':
						if (name.equals("int")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(1).getJavaType()};
						}
						break;
					case 'l':
						if (name.equals("long")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(1l).getJavaType()};
						}
						break;
					case 'c':
						if (name.equals("char")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(' ').getJavaType()};
						}
						break;
					case 's':
						if (name.equals("short")) { //$NON-NLS-1$
							return new IJavaType[] {newValue((short)1).getJavaType()};
						}
						break;
					case 'f':
						if (name.equals("float")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(1f).getJavaType()};
						}
						break;
					case 'd':
						if (name.equals("double")) { //$NON-NLS-1$
							return new IJavaType[] {newValue(1d).getJavaType()};
						}
						break;
				}
				return null;
			} else {
				IJavaType[] types = new IJavaType[classes.size()];
				for (int i = 0; i < types.length; i++) {
					types[i] = JDIType.createType(this, (Type)classes.get(i));
				}
				return types;
			}
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format("{0} occurred while retrieving class for name {1}", new String[]{e.toString(), name}), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}
	
	/**
	 * @see IJavaDebugTarget#newValue(boolean)
	 */
	public IJavaValue newValue(boolean value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
	
	/**
	 * @see IJavaDebugTarget#newValue(byte)
	 */
	public IJavaValue newValue(byte value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}

	/**
	 * @see IJavaDebugTarget#newValue(char)
	 */
	public IJavaValue newValue(char value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}

	/**
	 * @see IJavaDebugTarget#newValue(double)
	 */
	public IJavaValue newValue(double value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
	
	/**
	 * @see IJavaDebugTarget#newValue(float)
	 */
	public IJavaValue newValue(float value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
						
	/**
	 * @see IJavaDebugTarget#newValue(int)
	 */
	public IJavaValue newValue(int value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
	
	/**
	 * @see IJavaDebugTarget#newValue(long)
	 */
	public IJavaValue newValue(long value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}	
	
	/**
	 * @see IJavaDebugTarget#newValue(short)
	 */
	public IJavaValue newValue(short value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
	
	/**
	 * @see IJavaDebugTarget#newValue(String)
	 */
	public IJavaValue newValue(String value) {
		Value v = getVM().mirrorOf(value);
		return JDIValue.createValue(this, v);
	}
		
	/**
	 * @see IJavaDebugTarget#nullValue()
	 */
	public IJavaValue nullValue() {
		return JDIValue.createValue(this, null);
	}
	
	/**
	 * @see IJavaDebugTarget#voidValue()
	 */
	public IJavaValue voidValue() {
		return new JDIVoidValue(this);
	}
	
	protected boolean isTerminating() {
		return fTerminating;
	}

	protected void setTerminating(boolean terminating) {
		fTerminating = terminating;
	}
		
	/**
	 * An event handler for thread start events. When a thread
	 * starts in the target VM, a model thread is created.
	 */
	class ThreadStartHandler implements IJDIEventListener {
		
		protected EventRequest fRequest;
		
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
				setRequest(req);
			} catch (RuntimeException e) {
				logError(e);
			}
		}

		/**
		 * Creates a model thread for the underlying JDI thread
		 * and adds it to the collection of threads for this 
		 * debug target. As a side effect of creating the thread,
		 * a create event is fired for the model thread.
		 * The event is ignored if the underlying thread is already
		 * marked as collected.
		 * 
		 * @param event a thread start event
		 * @param target the target in which the thread started
		 * @return <code>true</code> - the thread should be resumed
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target) {
			ThreadReference thread= ((ThreadStartEvent)event).thread();
			try {
				if (thread.isCollected()) {
					return false;
				}
			} catch (VMDisconnectedException exception) {
				return false;
			}
			JDIThread jdiThread= findThread(thread);
			if (jdiThread == null) {
				jdiThread = createThread(thread);
				if (jdiThread == null) {
					return false;
				}
			} else {
				jdiThread.disposeStackFrames();
				jdiThread.fireChangeEvent(DebugEvent.CONTENT);
			}
			return !jdiThread.isSuspended();
		}
		
		/**
		 * Deregisters this event listener.
		 */
		protected void deleteRequest() {
			if (getRequest() != null) {
				removeJDIEventListener(this, getRequest());
				setRequest(null);
			}
		}
		
		protected EventRequest getRequest() {
			return fRequest;
		}

		protected void setRequest(EventRequest request) {
			fRequest = request;
		}
}
	
	/**
	 * An event handler for thread death events. When a thread
	 * dies in the target VM, its associated model thread is
	 * removed from the debug target.
	 */
	class ThreadDeathHandler implements IJDIEventListener {
		
		protected ThreadDeathHandler() {
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
		 * Locates the model thread associated with the underlying JDI thread
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
	
	protected ThreadStartHandler getThreadStartHandler() {
		return fThreadStartHandler;
	}

	protected void setThreadStartHandler(ThreadStartHandler threadStartHandler) {
		fThreadStartHandler = threadStartHandler;
	}
	
	/**
	 * Java debug targets do not support storage retrieval.
	 * 
	 * @see IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/**
	 * @see IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
		throws DebugException {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_storage_retrieval")); //$NON-NLS-1$
			// this line will not be excecuted as #notSupported(String)
			// will throw an exception
			return null;
	}

	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		if (!isAvailable()) {
			return;
		}
		if (launch.equals(getLaunch())) {
			// This target has been deregistered, but it hasn't successfully terminated.
			// Update internal state to reflect that it is disconnected
			disconnected();
		}
	}

	/**
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
	}
	
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}	

	/**
	 * Sets whether the VM should be resumed on startup.
	 * Has no effect if the VM is already running when
	 * this target is created.
	 * 
	 * @param resume whether the VM should be resumed on startup
	 */
	private void setResumeOnStartup(boolean resume) {
		fResumeOnStartup = resume;
	}
	
	/**
	 * Returns whether this VM should be resumed on startup.
	 * 
	 * @return whether this VM should be resumed on startup
	 */
	protected boolean isResumeOnStartup() {
		return fResumeOnStartup;
	}
	
	/**
	 * @see IJavaDebugTarget#getStepFilters()
	 */
	public String[] getStepFilters() {
		return fStepFilters;
	}

	/**
	 * @see IJavaDebugTarget#isFilterConstructors()
	 */
	public boolean isFilterConstructors() {
		return (fStepFilterMask & FILTER_CONSTRUCTORS) > 0;
	}

	/**
	 * @see IJavaDebugTarget#isFilterStaticInitializers()
	 */
	public boolean isFilterStaticInitializers() {
		return (fStepFilterMask & FILTER_STATIC_INITIALIZERS) > 0;
	}

	/**
	 * @see IJavaDebugTarget#isFilterSynthetics()
	 */
	public boolean isFilterSynthetics() {
		return (fStepFilterMask & FILTER_SYNTHETICS) > 0;
	}

	/**
	 * @see IJavaDebugTarget#isStepFiltersEnabled()
	 */
	public boolean isStepFiltersEnabled() {
		return (fStepFilterMask & STEP_FILTERS_ENABLED) > 0;
	}

	/**
	 * @see IJavaDebugTarget#setFilterConstructors(boolean)
	 */
	public void setFilterConstructors(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_CONSTRUCTORS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_CONSTRUCTORS ^ XOR_MASK);
		}
	}

	/**
	 * @see IJavaDebugTarget#setFilterStaticInitializers(boolean)
	 */
	public void setFilterStaticInitializers(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_STATIC_INITIALIZERS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_STATIC_INITIALIZERS ^ XOR_MASK);
		}		
	}

	/**
	 * @see IJavaDebugTarget#setFilterSynthetics(boolean)
	 */
	public void setFilterSynthetics(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_SYNTHETICS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_SYNTHETICS ^ XOR_MASK);
		}				
	}

	/**
	 * @see IJavaDebugTarget#setStepFilters(String[])
	 */
	public void setStepFilters(String[] list) {
		fStepFilters = list;
	}

	/**
	 * @see IJavaDebugTarget#setStepFiltersEnabled(boolean)
	 */
	public void setStepFiltersEnabled(boolean enabled) {
		if (enabled) {
			fStepFilterMask = fStepFilterMask | STEP_FILTERS_ENABLED;
		} else {
			fStepFilterMask = fStepFilterMask & (STEP_FILTERS_ENABLED ^ XOR_MASK);
		}				
	}

	/**
	 * @see IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return getThreadList().size() > 0;
	}
	
	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Sets the launch this target is contained in
	 * 
	 * @param launch the launch this target is contained in
	 */
	private void setLaunch(ILaunch launch) {
		fLaunch = launch;
	}
	
	/**
	 * Returns the number of suspend events that have occurred in this
	 * target.
	 * 
	 * @return the number of suspend events that have occurred in this
	 * target
	 */
	protected int getSuspendCount() {
		return fSuspendCount;
	} 
	
	/**
	 * Increments the suspend counter for this target
	 */
	protected void incrementSuspendCount() {
		fSuspendCount++;
	}
	
	/**
	 * Returns an evaluation engine for the given project, creating
	 * one if neccessary.
	 * 
	 * @param project java project
	 * @return evalaution engine
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project) {
		if (fEngines == null) {
			fEngines = new HashMap(2);
		}
		IAstEvaluationEngine engine = (IAstEvaluationEngine)fEngines.get(project);
		if (engine == null) {
			engine = EvaluationManager.newAstEvaluationEngine(project, this);
			fEngines.put(project, engine);
		}
		return engine;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#supportsMonitorInformation()
	 */
	public boolean supportsMonitorInformation() {
		return getVM().canGetCurrentContendedMonitor() && getVM().canGetMonitorInfo() && getVM().canGetOwnedMonitorInfo();
	}
	
	/**
	 * Sets whether or not this debug target is currently performing a hot code
	 * replace.
	 */
	public void setIsPerformingHotCodeReplace(boolean isPerformingHotCodeReplace) {
		fIsPerformingHotCodeReplace= isPerformingHotCodeReplace;
	}
	
	/**
	 * @see IJavaDebugTarget#isPerformingHotCodeReplace()
	 */
	public boolean isPerformingHotCodeReplace() {
		return fIsPerformingHotCodeReplace;
	}
}

