/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
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

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.InternalException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
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

public class JDIDebugTarget extends JDIDebugElement implements
		IJavaDebugTarget, ILaunchListener, IBreakpointManagerListener,
		IDebugEventSetListener {

	/**
	 * Threads contained in this debug target. When a thread starts it is added
	 * to the list. When a thread ends it is removed from the list.
	 */
	private ArrayList<JDIThread> fThreads;

	/**
	 * List of thread groups in this target.
	 */
	private ArrayList<JDIThreadGroup> fGroups;

	/**
	 * Associated system process, or <code>null</code> if not available.
	 */
	private IProcess fProcess;
	/**
	 * Underlying virtual machine.
	 */
	private VirtualMachine fVirtualMachine;
	/**
	 * Whether terminate is supported. Not all targets support terminate. For
	 * example, a VM that was attached to remotely may not allow the user to
	 * terminate it.
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
	 * Whether enable/disable object GC is allowed
	 */
	private boolean fSupportsDisableGC = false;
	/**
	 * Collection of breakpoints added to this target. Values are of type
	 * <code>IJavaBreakpoint</code>.
	 */
	private List<IBreakpoint> fBreakpoints;

	/**
	 * Collection of types that have attempted HCR, but failed. The types are
	 * stored by their fully qualified names.
	 */
	private Set<String> fOutOfSynchTypes;
	/**
	 * Whether or not this target has performed a hot code replace.
	 */
	private boolean fHasHCROccurred;

	/**
	 * The name of this target - set by the client on creation, or retrieved
	 * from the underlying VM.
	 */
	private String fName;

	/**
	 * The event dispatcher for this debug target, which runs in its own thread.
	 */
	private EventDispatcher fEventDispatcher = null;

	/**
	 * The thread start event handler
	 */
	private ThreadStartHandler fThreadStartHandler = null;

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
	 * Evaluation engine cache by Java project. Engines are disposed when this
	 * target terminates.
	 */
	private HashMap<IJavaProject, IAstEvaluationEngine> fEngines;

	/**
	 * List of step filters - each string is a pattern/fully qualified name of a
	 * type to filter.
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
	 * Step filter bit mask - indicates if synthetic methods are filtered.
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
	 * When a step lands in a filtered location, this indicates whether stepping
	 * should proceed "through" to an unfiltered location or step return.
	 * 
	 * @since 3.3
	 */
	private static final int STEP_THRU_FILTERS = 0x010;

	/**
	 * Step filter bit mask - indicates if simple getters are filtered.
	 * 
	 * @since 3.7
	 */
	private static final int FILTER_GETTERS = 0x020;

	/**
	 * Step filter bit mask - indicates if simple setters are filtered.
	 * 
	 * @since 3.7
	 */
	private static final int FILTER_SETTERS = 0x040;

	/**
	 * Mask used to flip individual bit masks via XOR
	 */
	private static final int XOR_MASK = 0xFFF;
	/**
	 * Whether this debug target is currently performing a hot code replace
	 */
	private boolean fIsPerformingHotCodeReplace = false;

	/**
	 * Target specific HCR listeners
	 * 
	 * @since 3.6
	 */
	private ListenerList fHCRListeners = new ListenerList();

	/**
	 * Creates a new JDI debug target for the given virtual machine.
	 * 
	 * @param jvm
	 *            the underlying VM
	 * @param name
	 *            the name to use for this VM, or <code>null</code> if the name
	 *            should be retrieved from the underlying VM
	 * @param supportsTerminate
	 *            whether the terminate action is supported by this debug target
	 * @param supportsDisconnect
	 *            whether the disconnect action is supported by this debug
	 *            target
	 * @param process
	 *            the system process associated with the underlying VM, or
	 *            <code>null</code> if no system process is available (for
	 *            example, a remote VM)
	 * @param resume
	 *            whether the VM should be resumed on startup. Has no effect if
	 *            the VM is already resumed/running when the connection is made.
	 */
	public JDIDebugTarget(ILaunch launch, VirtualMachine jvm, String name,
			boolean supportTerminate, boolean supportDisconnect,
			IProcess process, boolean resume) {
		super(null);
		setLaunch(launch);
		setResumeOnStartup(resume);
		setSupportsTerminate(supportTerminate);
		setSupportsDisconnect(supportDisconnect);
		setVM(jvm);
		jvm.setDebugTraceMode(VirtualMachine.TRACE_NONE);
		setProcess(process);
		setTerminated(false);
		setTerminating(false);
		setDisconnected(false);
		setName(name);
		setBreakpoints(new ArrayList<IBreakpoint>(5));
		setThreadList(new ArrayList<JDIThread>(5));
		fGroups = new ArrayList<JDIThreadGroup>(5);
		setOutOfSynchTypes(new ArrayList<String>(0));
		setHCROccurred(false);
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().getBreakpointManager()
				.addBreakpointManagerListener(this);
	}

	/**
	 * Returns the event dispatcher for this debug target. There is one event
	 * dispatcher per debug target.
	 * 
	 * @return event dispatcher
	 */
	public EventDispatcher getEventDispatcher() {
		return fEventDispatcher;
	}

	/**
	 * Sets the event dispatcher for this debug target. Set once at
	 * initialization.
	 * 
	 * @param dispatcher
	 *            event dispatcher
	 * @see #initialize()
	 */
	private void setEventDispatcher(EventDispatcher dispatcher) {
		fEventDispatcher = dispatcher;
	}

	/**
	 * Returns an iterator over the collection of threads. The returned iterator
	 * is made on a copy of the thread list so that it is thread safe. This
	 * method should always be used instead of getThreadList().iterator()
	 * 
	 * @return an iterator over the collection of threads
	 */
	private Iterator<JDIThread> getThreadIterator() {
		List<JDIThread> threadList;
		synchronized (fThreads) {
			threadList = (List<JDIThread>) fThreads.clone();
		}
		return threadList.iterator();
	}

	/**
	 * Sets the list of threads contained in this debug target. Set to an empty
	 * collection on creation. Threads are added and removed as they start and
	 * end. On termination this collection is set to the immutable singleton
	 * empty list.
	 * 
	 * @param threads
	 *            empty list
	 */
	private void setThreadList(ArrayList<JDIThread> threads) {
		fThreads = threads;
	}

	/**
	 * Returns the collection of breakpoints installed in this debug target.
	 * 
	 * @return list of installed breakpoints - instances of
	 *         <code>IJavaBreakpoint</code>
	 */
	public List<IBreakpoint> getBreakpoints() {
		return fBreakpoints;
	}

	/**
	 * Sets the list of breakpoints installed in this debug target. Set to an
	 * empty list on creation.
	 * 
	 * @param breakpoints
	 *            empty list
	 */
	private void setBreakpoints(List<IBreakpoint> breakpoints) {
		fBreakpoints = breakpoints;
	}

	/**
	 * Notifies this target that the underlying VM has started. This is the
	 * first event received from the VM. The VM is resumed. This event is not
	 * generated when an attach is made to a VM that is already running (has
	 * already started up). The VM is resumed as specified on creation.
	 * 
	 * @param event
	 *            VM start event
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
		// If any threads have resumed since thread collection was initialized,
		// update their status (avoid concurrent modification - use
		// #getThreads())
		IThread[] threads = getThreads();
		for (IThread thread2 : threads) {
			JDIThread thread = (JDIThread) thread2;
			if (thread.isSuspended()) {
				try {
					boolean suspended = thread.getUnderlyingThread()
							.isSuspended();
					if (!suspended) {
						thread.setRunning(true);
						thread.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
					}
				} catch (VMDisconnectedException e) {
				} catch (ObjectCollectedException e) {
				} catch (RuntimeException e) {
					logError(e);
				}
			}
		}

	}

	/**
	 * Initialize event requests and state from the underlying VM. This method
	 * is synchronized to ensure that we do not start to process an events from
	 * the target until our state is initialized.
	 */
	protected synchronized void initialize() {
		setEventDispatcher(new EventDispatcher(this));
		setRequestTimeout(Platform.getPreferencesService().getInt(
				JDIDebugPlugin.getUniqueIdentifier(),
				JDIDebugModel.PREF_REQUEST_TIMEOUT,
				JDIDebugModel.DEF_REQUEST_TIMEOUT,
				null));
		initializeRequests();
		initializeState();
		initializeBreakpoints();
		getLaunch().addDebugTarget(this);
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
		fireCreationEvent();
		// begin handling/dispatching events after the creation event is handled
		// by all listeners
		plugin.asyncExec(new Runnable() {
			public void run() {
				EventDispatcher dispatcher = getEventDispatcher();
				if (dispatcher != null) {
					Thread t = new Thread(
							dispatcher,
							JDIDebugModel.getPluginIdentifier()
									+ JDIDebugModelMessages.JDIDebugTarget_JDI_Event_Dispatcher);
					t.setDaemon(true);
					t.start();
				}
			}
		});
	}

	/**
	 * Adds all of the pre-existing threads to this debug target.
	 */
	protected void initializeState() {

		List<ThreadReference> threads = null;
		VirtualMachine vm = getVM();
		if (vm != null) {
			try {
				String name = vm.name();
				fSupportsDisableGC = !name.equals("Classic VM"); //$NON-NLS-1$
			} catch (RuntimeException e) {
				internalError(e);
			}
			try {
				threads = vm.allThreads();
			} catch (RuntimeException e) {
				internalError(e);
			}
			if (threads != null) {
				Iterator<ThreadReference> initialThreads = threads.iterator();
				while (initialThreads.hasNext()) {
					createThread(initialThreads.next());
				}
			}
		}

		if (isResumeOnStartup()) {
			setSuspended(false);
		}
	}

	/**
	 * Registers event handlers for thread creation, thread termination.
	 */
	protected void initializeRequests() {
		setThreadStartHandler(new ThreadStartHandler());
		new ThreadDeathHandler();
	}

	/**
	 * Installs all Java breakpoints that currently exist in the breakpoint
	 * manager
	 */
	protected void initializeBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		manager.addBreakpointListener(this);
		IBreakpoint[] bps = manager.getBreakpoints(JDIDebugModel
				.getPluginIdentifier());
		for (IBreakpoint bp : bps) {
			if (bp instanceof IJavaBreakpoint) {
				breakpointAdded(bp);
			}
		}
	}

	/**
	 * Creates, adds and returns a thread for the given underlying thread
	 * reference. A creation event is fired for the thread. Returns
	 * <code>null</code> if during the creation of the thread this target is set
	 * to the disconnected state.
	 * 
	 * @param thread
	 *            underlying thread
	 * @return model thread
	 */
	protected JDIThread createThread(ThreadReference thread) {
		JDIThread jdiThread = newThread(thread);
		if (jdiThread == null) {
			return null;
		}
		if (isDisconnected()) {
			return null;
		}
		synchronized (fThreads) {
			fThreads.add(jdiThread);
		}
		jdiThread.fireCreationEvent();
		return jdiThread;
	}

	/**
	 * Factory method for creating new threads. Creates and returns a new thread
	 * object for the underlying thread reference, or <code>null</code> if none
	 * 
	 * @param reference
	 *            thread reference
	 * @return JDI model thread
	 */
	protected JDIThread newThread(ThreadReference reference) {
		try {
			return new JDIThread(this, reference);
		} catch (ObjectCollectedException exception) {
			// ObjectCollectionException can be thrown if the thread has already
			// completed (exited) in the VM.
		}
		return null;
	}

	/**
	 * @see IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() {
		synchronized (fThreads) {
			return fThreads.toArray(new IThread[0]);
		}
	}

	/**
	 * @see ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return (isSuspended() || canResumeThreads()) && isAvailable()
				&& !isPerformingHotCodeReplace();
	}

	/**
	 * Returns whether this target has any threads which can be resumed.
	 * 
	 * @return true if any thread can be resumed, false otherwise
	 * @since 3.2
	 */
	private boolean canResumeThreads() {
		Iterator<JDIThread> it = getThreadIterator();
		while (it.hasNext()) {
			IThread thread = it.next();
			if (thread.canResume())
				return true;
		}
		return false;
	}

	/**
	 * @see ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		if (!isSuspended() && isAvailable()) {
			// allow suspend when one or more threads are currently running
			IThread[] threads = getThreads();
			for (IThread thread : threads) {
				if (((JDIThread) thread).canSuspend()) {
					return true;
				}
			}
			return false;
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
	@Override
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
	 * Sets whether this debug target supports disconnection. Set on creation.
	 * 
	 * @param supported
	 *            <code>true</code> if this target supports disconnection,
	 *            otherwise <code>false</code>
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
	 * Sets whether this debug target supports termination. Set on creation.
	 * 
	 * @param supported
	 *            <code>true</code> if this target supports termination,
	 *            otherwise <code>false</code>
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
		if (isAvailable()
				&& JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] { 1,
						4 })) {
			VirtualMachine vm = getVM();
			if (vm != null) {
				return vm.canUseInstanceFilters();
			}
		}
		return false;
	}

	/**
	 * Returns whether this debug target supports hot code replace for the J9
	 * VM.
	 * 
	 * @return whether this debug target supports J9 hot code replace
	 */
	public boolean supportsJ9HotCodeReplace() {
		VirtualMachine vm = getVM();
		if (isAvailable() && vm instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			try {
				return ((org.eclipse.jdi.hcr.VirtualMachine) vm)
						.canReloadClasses();
			} catch (UnsupportedOperationException e) {
				// This is not an error condition -
				// UnsupportedOperationException is thrown when a VM does
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
		if (isAvailable()
				&& JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] { 1,
						4 })) {
			VirtualMachine vm = getVM();
			if (vm != null) {
				return vm.canRedefineClasses();
			}
		}
		return false;
	}

	/**
	 * Returns whether this debug target supports popping stack frames.
	 * 
	 * @return whether this debug target supports popping stack frames.
	 */
	public boolean canPopFrames() {
		if (isAvailable()
				&& JDIDebugPlugin.isJdiVersionGreaterThanOrEqual(new int[] { 1,
						4 })) {
			VirtualMachine vm = getVM();
			if (vm != null) {
				return vm.canPopFrames();
			}
		}
		return false;
	}

	/**
	 * @see IDisconnect#disconnect()
	 */
	@Override
	public void disconnect() throws DebugException {

		if (!isAvailable()) {
			// already done
			return;
		}

		if (!canDisconnect()) {
			notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_disconnect);
		}

		try {
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.dispose();
			}
		} catch (VMDisconnectedException e) {
			// if the VM disconnects while disconnecting, perform
			// normal disconnect handling
			disconnected();
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat.format(
							JDIDebugModelMessages.JDIDebugTarget_exception_disconnecting,
							e.toString()), e);
		}

	}

	/**
	 * Allows for ThreadStartHandler to do clean up/disposal.
	 */
	private void disposeThreadHandler() {
		ThreadStartHandler handler = getThreadStartHandler();
		if (handler != null) {
			handler.deleteRequest();
		}
	}

	/**
	 * Returns the underlying virtual machine associated with this debug target,
	 * or <code>null</code> if none (disconnected/terminated)
	 * 
	 * @return the underlying VM or <code>null</code>
	 */
	@Override
	public VirtualMachine getVM() {
		return fVirtualMachine;
	}

	/**
	 * Sets the underlying VM associated with this debug target. Set on
	 * creation.
	 * 
	 * @param vm
	 *            underlying VM
	 */
	private void setVM(VirtualMachine vm) {
		fVirtualMachine = vm;
	}

	/**
	 * Sets whether this debug target has performed a hot code replace.
	 */
	public void setHCROccurred(boolean occurred) {
		fHasHCROccurred = occurred;
	}

	public void removeOutOfSynchTypes(List<String> qualifiedNames) {
		fOutOfSynchTypes.removeAll(qualifiedNames);
	}

	/**
	 * Sets the list of out of synch types to the given list.
	 */
	private void setOutOfSynchTypes(List<String> qualifiedNames) {
		fOutOfSynchTypes = new HashSet<String>();
		fOutOfSynchTypes.addAll(qualifiedNames);
	}

	/**
	 * The given types have failed to be reloaded by HCR. Add them to the list
	 * of out of synch types.
	 */
	public void addOutOfSynchTypes(List<String> qualifiedNames) {
		fOutOfSynchTypes.addAll(qualifiedNames);
	}

	/**
	 * Returns whether the given type is out of synch in this target.
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
		Iterator<JDIThread> threads = getThreadIterator();
		while (threads.hasNext()) {
			JDIThread thread = threads.next();
			if (thread.isOutOfSynch()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see IJavaDebugTarget#mayBeOutOfSynch()
	 */
	public boolean mayBeOutOfSynch() {
		Iterator<JDIThread> threads = getThreadIterator();
		while (threads.hasNext()) {
			JDIThread thread = threads.next();
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
	 * Returns whether this debug target has performed a hot code replace
	 */
	public boolean hasHCROccurred() {
		return fHasHCROccurred;
	}

	/**
	 * Reinstall all breakpoints installed in the given resources
	 * @param resources
	 * @param classNames
	 */
	public void reinstallBreakpointsIn(List<IResource> resources, List<String> classNames) {
		List<IBreakpoint> breakpoints = getBreakpoints();
		IJavaBreakpoint[] copy = new IJavaBreakpoint[breakpoints.size()];
		breakpoints.toArray(copy);
		IJavaBreakpoint breakpoint = null;
		String installedType = null;

		for (IJavaBreakpoint element : copy) {
			breakpoint = element;
			if (breakpoint instanceof JavaLineBreakpoint) {
				try {
					installedType = breakpoint.getTypeName();
					if (classNames.contains(installedType)) {
						breakpointRemoved(breakpoint, null);
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
	 * Finds and returns the JDI thread for the associated thread reference, or
	 * <code>null</code> if not found.
	 * 
	 * @param the
	 *            underlying thread reference
	 * @return the associated model thread
	 */
	public JDIThread findThread(ThreadReference tr) {
		Iterator<JDIThread> iter = getThreadIterator();
		while (iter.hasNext()) {
			JDIThread thread = iter.next();
			if (thread.getUnderlyingThread().equals(tr))
				return thread;
		}
		return null;
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			setName(getVMName());
		}
		return fName;
	}

	/**
	 * Sets the name of this debug target. Set on creation, and if set to
	 * <code>null</code> the name will be retrieved lazily from the underlying
	 * VM.
	 * 
	 * @param name
	 *            the name of this VM or <code>null</code> if the name should be
	 *            retrieved from the underlying VM
	 */
	protected void setName(String name) {
		fName = name;
	}

	/**
	 * Sets the process associated with this debug target, possibly
	 * <code>null</code>. Set on creation.
	 * 
	 * @param process
	 *            the system process associated with the underlying VM, or
	 *            <code>null</code> if no process is associated with this debug
	 *            target (for example, a remote VM).
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
	 * Notification the underlying VM has died. Updates the state of this target
	 * to be terminated.
	 * 
	 * @param event
	 *            VM death event
	 */
	public void handleVMDeath(VMDeathEvent event) {
		terminated();
	}

	/**
	 * Notification the underlying VM has disconnected. Updates the state of
	 * this target to be terminated.
	 * 
	 * @param event
	 *            disconnect event
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
	 * @param suspended
	 *            whether this VM is suspended
	 */
	private void setSuspended(boolean suspended) {
		fSuspended = suspended;
	}

	/**
	 * Returns whether this target is available to handle VM requests
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
	 * @param terminated
	 *            <code>true</code> if this debug target is terminated,
	 *            otherwise <code>false</code>
	 */
	protected void setTerminated(boolean terminated) {
		fTerminated = terminated;
	}

	/**
	 * Sets whether this debug target is disconnected
	 * 
	 * @param disconnected
	 *            <code>true</code> if this debug target is disconnected,
	 *            otherwise <code>false</code>
	 */
	protected void setDisconnected(boolean disconnected) {
		fDisconnected = disconnected;
	}

	/**
	 * @see IDisconnect#isDisconnected()
	 */
	@Override
	public boolean isDisconnected() {
		return fDisconnected;
	}

	/**
	 * Creates, enables and returns a class prepare request for the specified
	 * class name in this target.
	 * 
	 * @param classPattern
	 *            regular expression specifying the pattern of class names that
	 *            will cause the event request to fire. Regular expressions may
	 *            begin with a '*', end with a '*', or be an exact match.
	 * @exception CoreException
	 *                if unable to create the request
	 */
	public ClassPrepareRequest createClassPrepareRequest(String classPattern)
			throws CoreException {
		return createClassPrepareRequest(classPattern, null);
	}

	/**
	 * Creates, enables and returns a class prepare request for the specified
	 * class name in this target. Can specify a class exclusion filter as well.
	 * This is a utility method used by event requesters that need to create
	 * class prepare requests.
	 * 
	 * @param classPattern
	 *            regular expression specifying the pattern of class names that
	 *            will cause the event request to fire. Regular expressions may
	 *            begin with a '*', end with a '*', or be an exact match.
	 * @param classExclusionPattern
	 *            regular expression specifying the pattern of class names that
	 *            will not cause the event request to fire. Regular expressions
	 *            may begin with a '*', end with a '*', or be an exact match.
	 *            May be <code>null</code>.
	 * @exception CoreException
	 *                if unable to create the request
	 */
	public ClassPrepareRequest createClassPrepareRequest(String classPattern,
			String classExclusionPattern) throws CoreException {
		return createClassPrepareRequest(classPattern, classExclusionPattern,
				true);
	}

	/**
	 * Creates, enables and returns a class prepare request for the specified
	 * class name in this target. Can specify a class exclusion filter as well.
	 * This is a utility method used by event requesters that need to create
	 * class prepare requests.
	 * 
	 * @param classPattern
	 *            regular expression specifying the pattern of class names that
	 *            will cause the event request to fire. Regular expressions may
	 *            begin with a '*', end with a '*', or be an exact match.
	 * @param classExclusionPattern
	 *            regular expression specifying the pattern of class names that
	 *            will not cause the event request to fire. Regular expressions
	 *            may begin with a '*', end with a '*', or be an exact match.
	 *            May be <code>null</code>.
	 * @param enabled
	 *            whether to enable the event request
	 * @exception CoreException
	 *                if unable to create the request
	 * @since 3.3
	 */
	public ClassPrepareRequest createClassPrepareRequest(String classPattern,
			String classExclusionPattern, boolean enabled) throws CoreException {
		return createClassPrepareRequest(classPattern, classExclusionPattern,
				enabled, null);
	}

	/**
	 * Creates, enables and returns a class prepare request for the specified
	 * class name in this target. Can specify a class exclusion filter as well.
	 * This is a utility method used by event requesters that need to create
	 * class prepare requests.
	 * 
	 * @param classPattern
	 *            regular expression specifying the pattern of class names that
	 *            will cause the event request to fire. Regular expressions may
	 *            begin with a '*', end with a '*', or be an exact match. May be
	 *            <code>null</code> if sourceName is specified
	 * @param classExclusionPattern
	 *            regular expression specifying the pattern of class names that
	 *            will not cause the event request to fire. Regular expressions
	 *            may begin with a '*', end with a '*', or be an exact match.
	 *            May be <code>null</code>.
	 * @param enabled
	 *            whether to enable the event request
	 * @param sourceName
	 *            source name pattern to match or <code>null</code> if
	 *            classPattern is specified
	 * @exception CoreException
	 *                if unable to create the request
	 * @since 3.3
	 */
	public ClassPrepareRequest createClassPrepareRequest(String classPattern,
			String classExclusionPattern, boolean enabled, String sourceName)
			throws CoreException {
		EventRequestManager manager = getEventRequestManager();
		if (manager == null || !isAvailable()) {
			requestFailed(
					JDIDebugModelMessages.JDIDebugTarget_Unable_to_create_class_prepare_request___VM_disconnected__2,
					null);
		}
		ClassPrepareRequest req = null;
		try {
			req = manager.createClassPrepareRequest();
			if (classPattern != null) {
				req.addClassFilter(classPattern);
			}
			if (classExclusionPattern != null) {
				req.addClassExclusionFilter(classExclusionPattern);
			}
			req.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			if (sourceName != null) {
				req.addSourceNameFilter(sourceName);
			}
			if (enabled) {
				req.enable();
			}
		} catch (RuntimeException e) {
			targetRequestFailed(
					JDIDebugModelMessages.JDIDebugTarget_Unable_to_create_class_prepare_request__3,
					e);
			// execution will not reach here
			return null;
		}
		return req;
	}

	/**
	 * @see ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		// if a client calls resume, then we should resume on a VMStart event in
		// case
		// it has not yet been received, and the target was created with the
		// "resume"
		// flag as "false". See bug 32372.
		setResumeOnStartup(true);
		resume(true);
	}

	/**
	 * @see ISuspendResume#resume()
	 * 
	 *      Updates the state of this debug target to resumed, but does not fire
	 *      notification of the resumption.
	 */
	public void resumeQuiet() throws DebugException {
		resume(false);
	}

	/**
	 * @see ISuspendResume#resume()
	 * 
	 *      Updates the state of this debug target, but only fires notification
	 *      to listeners if <code>fireNotification</code> is <code>true</code>.
	 */
	protected void resume(boolean fireNotification) throws DebugException {
		if ((!isSuspended() && !canResumeThreads()) || !isAvailable()) {
			return;
		}
		try {
			setSuspended(false);
			resumeThreads();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.resume();
			}
			if (fireNotification) {
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
		} catch (VMDisconnectedException e) {
			disconnected();
			return;
		} catch (RuntimeException e) {
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIDebugTarget_exception_resume,
					e.toString()), e);
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return breakpoint instanceof IJavaBreakpoint;
	}

	/**
	 * Notification a breakpoint has been added to the breakpoint manager. If
	 * the breakpoint is a Java breakpoint and this target is not terminated,
	 * the breakpoint is installed.
	 * 
	 * @param breakpoint
	 *            the breakpoint added to the breakpoint manager
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (!isAvailable()) {
			return;
		}
		if (supportsBreakpoint(breakpoint)) {
			try {
				JavaBreakpoint javaBreakpoint = (JavaBreakpoint) breakpoint;
				if (!getBreakpoints().contains(breakpoint)) {
					if (!javaBreakpoint.shouldSkipBreakpoint()) {
						// If the breakpoint should be skipped, don't add the
						// breakpoint
						// request to the VM. Just add the breakpoint to the
						// collection so
						// we have it if the manager is later enabled.
						javaBreakpoint.addToTarget(this);
					}
					getBreakpoints().add(breakpoint);
				}
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * Notification that one or more attributes of the given breakpoint has
	 * changed. If the breakpoint is a Java breakpoint, the associated event
	 * request in the underlying VM is updated to reflect the new state of the
	 * breakpoint.
	 * 
	 * @param breakpoint
	 *            the breakpoint that has changed
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/**
	 * Notification that the given breakpoint has been removed from the
	 * breakpoint manager. If this target is not terminated, the breakpoint is
	 * removed from the underlying VM.
	 * 
	 * @param breakpoint
	 *            the breakpoint has been removed from the breakpoint manager.
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (!isAvailable()) {
			return;
		}
		if (supportsBreakpoint(breakpoint)) {
			try {
				((JavaBreakpoint) breakpoint).removeFromTarget(this);
				getBreakpoints().remove(breakpoint);
				Iterator<JDIThread> threads = getThreadIterator();
				while (threads.hasNext()) {
					threads.next()
							.removeCurrentBreakpoint(breakpoint);
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
			VirtualMachine vm = getVM();
			prepareThreadsForClientSuspend();
			if (vm != null) {
				vm.suspend();
			}
			suspendThreads();
			setSuspended(true);
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
		} catch (RuntimeException e) {
			setSuspended(false);
			resumeThreads();
			fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIDebugTarget_exception_suspend,
					e.toString()), e);
		}

	}

	/**
	 * Prepares threads to suspend (terminates evaluations, waits for
	 * invocations, etc.).
	 * 
	 * @exception DebugException
	 *                if a thread times out
	 */
	protected void prepareThreadsForClientSuspend() throws DebugException {
		Iterator<JDIThread> threads = getThreadIterator();
		while (threads.hasNext()) {
			threads.next().prepareForClientSuspend();
		}
	}

	/**
	 * Notifies threads that they have been suspended
	 */
	protected void suspendThreads() {
		Iterator<JDIThread> threads = getThreadIterator();
		while (threads.hasNext()) {
			threads.next().suspendedByVM();
		}
	}

	/**
	 * Notifies threads that they have been resumed
	 */
	protected void resumeThreads() throws DebugException {
		Iterator<JDIThread> threads = getThreadIterator();
		while (threads.hasNext()) {
			threads.next().resumedByVM();
		}
	}

	/**
	 * Notifies this VM to update its state in preparation for a suspend.
	 * 
	 * @param breakpoint
	 *            the breakpoint that caused the suspension
	 */
	public void prepareToSuspendByBreakpoint(JavaBreakpoint breakpoint) {
		setSuspended(true);
		suspendThreads();
	}

	/**
	 * Notifies this VM it has been suspended by the given breakpoint
	 * 
	 * @param breakpoint
	 *            the breakpoint that caused the suspension
	 */
	protected void suspendedByBreakpoint(JavaBreakpoint breakpoint,
			boolean queueEvent, EventSet set) {
		if (queueEvent) {
			queueSuspendEvent(DebugEvent.BREAKPOINT, set);
		} else {
			fireSuspendEvent(DebugEvent.BREAKPOINT);
		}
	}

	/**
	 * Notifies this VM suspension has been cancelled
	 * 
	 * @param breakpoint
	 *            the breakpoint that caused the suspension
	 */
	protected void cancelSuspendByBreakpoint(JavaBreakpoint breakpoint)
			throws DebugException {
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
			notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_termination);
		}
		try {
			setTerminating(true);
			disposeThreadHandler();
			VirtualMachine vm = getVM();
			if (vm != null) {
				vm.exit(1);
			}
			IProcess process = getProcess();
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
			targetRequestFailed(MessageFormat.format(
					JDIDebugModelMessages.JDIDebugTarget_exception_terminating,
					e.toString()), e);
		}
	}

	/**
	 * Updates the state of this target to be terminated, if not already
	 * terminated.
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
	 * Updates the state of this target for disconnection from the VM.
	 */
	@Override
	protected void disconnected() {
		if (!isDisconnected()) {
			setDisconnected(true);
			cleanup();
			fireTerminateEvent();
		}
	}

	/**
	 * Cleans up the internal state of this debug target as a result of a
	 * session ending with a VM (as a result of a disconnect or termination of
	 * the VM).
	 * <p>
	 * All threads are removed from this target. This target is removed as a
	 * breakpoint listener, and all breakpoints are removed from this target.
	 * </p>
	 */
	protected void cleanup() {
		removeAllThreads();
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getBreakpointManager().removeBreakpointListener(this);
		plugin.getLaunchManager().removeLaunchListener(this);
		plugin.getBreakpointManager().removeBreakpointManagerListener(this);
		plugin.removeDebugEventListener(this);
		removeAllBreakpoints();
		fOutOfSynchTypes.clear();
		if (fEngines != null) {
			Iterator<IAstEvaluationEngine> engines = fEngines.values().iterator();
			while (engines.hasNext()) {
				IAstEvaluationEngine engine = engines
						.next();
				engine.dispose();
			}
			fEngines.clear();
		}
		fVirtualMachine = null;
		setThreadStartHandler(null);
		setEventDispatcher(null);
		setStepFilters(new String[0]);
		fHCRListeners.clear();
	}

	/**
	 * Removes all threads from this target's collection of threads, firing a
	 * terminate event for each.
	 */
	protected void removeAllThreads() {
		Iterator<JDIThread> itr = getThreadIterator();
		while (itr.hasNext()) {
			JDIThread child = itr.next();
			child.terminated();
		}
		synchronized (fThreads) {
			fThreads.clear();
		}
	}

	/**
	 * Removes all breakpoints from this target, such that each breakpoint can
	 * update its install count. This target's collection of breakpoints is
	 * cleared.
	 */
	protected void removeAllBreakpoints() {
		Iterator<IBreakpoint> breakpoints = ((ArrayList<IBreakpoint>) ((ArrayList<IBreakpoint>) getBreakpoints())
				.clone()).iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) breakpoints.next();
			try {
				breakpoint.removeFromTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
		getBreakpoints().clear();
	}

	/**
	 * Adds all the breakpoints in this target's collection to this debug
	 * target.
	 */
	protected void reinstallAllBreakpoints() {
		Iterator<IBreakpoint> breakpoints = ((ArrayList<IBreakpoint>) ((ArrayList<IBreakpoint>) getBreakpoints())
				.clone()).iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) breakpoints.next();
			try {
				breakpoint.addToTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * Returns VirtualMachine.classesByName(String), logging any JDI exceptions.
	 * 
	 * @see com.sun.jdi.VirtualMachine
	 */
	public List<ReferenceType> jdiClassesByName(String className) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			try {
				return vm.classesByName(className);
			} catch (VMDisconnectedException e) {
				if (!isAvailable()) {
					return Collections.EMPTY_LIST;
				}
				logError(e);
			} catch (RuntimeException e) {
				internalError(e);
			}
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see IJavaDebugTarget#findVariable(String)
	 */
	public IJavaVariable findVariable(String varName) throws DebugException {
		IThread[] threads = getThreads();
		for (IThread thread2 : threads) {
			IJavaThread thread = (IJavaThread) thread2;
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
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IJavaDebugTarget.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * The JDIDebugPlugin is shutting down. Shutdown the event dispatcher and do
	 * local cleanup.
	 */
	public void shutdown() {
		EventDispatcher dispatcher = ((JDIDebugTarget) getDebugTarget())
				.getEventDispatcher();
		if (dispatcher != null) {
			dispatcher.shutdown();
		}
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
	 * given type, on the target VM, or <code>null</code> if the type is not
	 * loaded, or a CRC for the type is not known.
	 * 
	 * @param typeName
	 *            fully qualified name of the type for which a CRC is required.
	 *            For example, "com.example.Example".
	 * @return 32 bit CRC, or <code>null</code>
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 */
	protected Integer getCRC(String typeName) throws DebugException {
		if (getVM() instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			List<ReferenceType> classes = jdiClassesByName(typeName);
			if (!classes.isEmpty()) {
				ReferenceType type = classes.get(0);
				if (type instanceof org.eclipse.jdi.hcr.ReferenceType) {
					try {
						org.eclipse.jdi.hcr.ReferenceType rt = (org.eclipse.jdi.hcr.ReferenceType) type;
						if (rt.isVersionKnown()) {
							return new Integer(rt.getClassFileVersion());
						}
					} catch (RuntimeException e) {
						targetRequestFailed(
								MessageFormat.format(
										JDIDebugModelMessages.JDIDebugTarget_exception_retrieving_version_information,
										e.toString(), type.name()), e);
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
			VirtualMachine vm = getVM();
			if (vm == null) {
				requestFailed(
						JDIDebugModelMessages.JDIDebugTarget_Unable_to_retrieve_types___VM_disconnected__4,
						null);
			}
			List<ReferenceType> classes = vm.classesByName(name);
			if (classes.size() == 0) {
				switch (name.charAt(0)) {
				case 'b':
					if (name.equals("boolean")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(true).getJavaType() };
					} else if (name.equals("byte")) { //$NON-NLS-1$
						return new IJavaType[] { newValue((byte) 1)
								.getJavaType() };
					}
					break;
				case 'i':
					if (name.equals("int")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(1).getJavaType() };
					}
					break;
				case 'l':
					if (name.equals("long")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(1l).getJavaType() };
					}
					break;
				case 'c':
					if (name.equals("char")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(' ').getJavaType() };
					}
					break;
				case 's':
					if (name.equals("short")) { //$NON-NLS-1$
						return new IJavaType[] { newValue((short) 1)
								.getJavaType() };
					}
					break;
				case 'f':
					if (name.equals("float")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(1f).getJavaType() };
					}
					break;
				case 'd':
					if (name.equals("double")) { //$NON-NLS-1$
						return new IJavaType[] { newValue(1d).getJavaType() };
					}
					break;
				}
				return null;
			}
			IJavaType[] types = new IJavaType[classes.size()];
			for (int i = 0; i < types.length; i++) {
				types[i] = JDIType.createType(this, classes.get(i));
			}
			return types;
		} catch (RuntimeException e) {
			targetRequestFailed(
					MessageFormat
							.format("{0} occurred while retrieving class for name {1}", e.toString(), name), e); //$NON-NLS-1$
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/**
	 * @see IJavaDebugTarget#newValue(boolean)
	 */
	public IJavaValue newValue(boolean value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(byte)
	 */
	public IJavaValue newValue(byte value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(char)
	 */
	public IJavaValue newValue(char value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(double)
	 */
	public IJavaValue newValue(double value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(float)
	 */
	public IJavaValue newValue(float value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(int)
	 */
	public IJavaValue newValue(int value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(long)
	 */
	public IJavaValue newValue(long value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(short)
	 */
	public IJavaValue newValue(short value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget#newValue(String)
	 */
	public IJavaValue newValue(String value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
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
	 * An event handler for thread start events. When a thread starts in the
	 * target VM, a model thread is created.
	 */
	class ThreadStartHandler implements IJDIEventListener {

		protected EventRequest fRequest;

		protected ThreadStartHandler() {
			createRequest();
		}

		/**
		 * Creates and registers a request to handle all thread start events
		 */
		protected void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if (manager != null) {
				try {
					EventRequest req = manager.createThreadStartRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addJDIEventListener(this, req);
					setRequest(req);
				} catch (RuntimeException e) {
					logError(e);
				}
			}
		}

		/**
		 * Creates a model thread for the underlying JDI thread and adds it to
		 * the collection of threads for this debug target. As a side effect of
		 * creating the thread, a create event is fired for the model thread.
		 * The event is ignored if the underlying thread is already marked as
		 * collected.
		 * 
		 * @param event
		 *            a thread start event
		 * @param target
		 *            the target in which the thread started
		 * @return <code>true</code> - the thread should be resumed
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			try {
				if (thread.isCollected()) {
					return false;
				}
			} catch (VMDisconnectedException exception) {
				return false;
			} catch (ObjectCollectedException e) {
				return false;
			} catch (TimeoutException e) {
				// continue - attempt to create the thread
			}
			JDIThread jdiThread = findThread(thread);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.debug.core.IJDIEventListener#eventSetComplete
		 * (com.sun.jdi.event.Event,
		 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, boolean)
		 */
		public void eventSetComplete(Event event, JDIDebugTarget target,
				boolean suspend, EventSet eventSet) {
			// do nothing
		}

		/**
		 * unregisters this event listener.
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
	 * An event handler for thread death events. When a thread dies in the
	 * target VM, its associated model thread is removed from the debug target.
	 */
	class ThreadDeathHandler implements IJDIEventListener {

		protected ThreadDeathHandler() {
			createRequest();
		}

		/**
		 * Creates and registers a request to listen to thread death events.
		 */
		protected void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if (manager != null) {
				try {
					EventRequest req = manager.createThreadDeathRequest();
					req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
					req.enable();
					addJDIEventListener(this, req);
				} catch (RuntimeException e) {
					logError(e);
				}
			}
		}

		/**
		 * Locates the model thread associated with the underlying JDI thread
		 * that has terminated, and removes it from the collection of threads
		 * belonging to this debug target. A terminate event is fired for the
		 * model thread.
		 * 
		 * @param event
		 *            a thread death event
		 * @param target
		 *            the target in which the thread died
		 * @return <code>true</code> - the thread should be resumed
		 */
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			ThreadReference ref = ((ThreadDeathEvent) event).thread();
			JDIThread thread = findThread(ref);
			if (thread == null) {
				// wait for any thread start event sets to complete processing
				// see bug 272494
				try {
					Job.getJobManager().join(ThreadStartEvent.class, null);
				} catch (OperationCanceledException e) {
				} catch (InterruptedException e) {
				}
				thread = target.findThread(ref);
			}
			if (thread != null) {
				synchronized (fThreads) {
					fThreads.remove(thread);
				}
				thread.terminated();
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.debug.core.IJDIEventListener#eventSetComplete
		 * (com.sun.jdi.event.Event,
		 * org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget, boolean)
		 */
		public void eventSetComplete(Event event, JDIDebugTarget target,
				boolean suspend, EventSet eventSet) {
			// do nothing
		}

	}

	class CleanUpJob extends Job {

		/**
		 * Constructs a job to cleanup a hanging target.
		 */
		public CleanUpJob() {
			super(JDIDebugModelMessages.JDIDebugTarget_0);
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime
		 * .IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (isAvailable()) {
				if (fEventDispatcher != null) {
					fEventDispatcher.shutdown();
				}
				disconnected();
			}
			return Status.OK_STATUS;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		@Override
		public boolean shouldRun() {
			return isAvailable();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.internal.jobs.InternalJob#shouldSchedule()
		 */
		@Override
		public boolean shouldSchedule() {
			return isAvailable();
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
		notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_storage_retrieval);
		// this line will not be executed as #notSupported(String)
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
			// This target has been unregistered, but it hasn't successfully
			// terminated.
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
	 * Sets whether the VM should be resumed on startup. Has no effect if the VM
	 * is already running when this target is created.
	 * 
	 * @param resume
	 *            whether the VM should be resumed on startup
	 */
	private synchronized void setResumeOnStartup(boolean resume) {
		fResumeOnStartup = resume;
	}

	/**
	 * Returns whether this VM should be resumed on startup.
	 * 
	 * @return whether this VM should be resumed on startup
	 */
	protected synchronized boolean isResumeOnStartup() {
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

	/*
	 * (non-Javadoc) Was added in 3.3, made API in 3.5
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#isStepThruFilters()
	 */
	public boolean isStepThruFilters() {
		return (fStepFilterMask & STEP_THRU_FILTERS) > 0;
	}

	/**
	 * @see IJavaDebugTarget#isStepFiltersEnabled()
	 */
	@Override
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
			fStepFilterMask = fStepFilterMask
					& (FILTER_CONSTRUCTORS ^ XOR_MASK);
		}
	}

	/**
	 * @see IJavaDebugTarget#setFilterStaticInitializers(boolean)
	 */
	public void setFilterStaticInitializers(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_STATIC_INITIALIZERS;
		} else {
			fStepFilterMask = fStepFilterMask
					& (FILTER_STATIC_INITIALIZERS ^ XOR_MASK);
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

	/*
	 * (non-Javadoc) Was added in 3.3, made API in 3.5
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#setStepThruFilters(boolean)
	 */
	public void setStepThruFilters(boolean thru) {
		if (thru) {
			fStepFilterMask = fStepFilterMask | STEP_THRU_FILTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (STEP_THRU_FILTERS ^ XOR_MASK);
		}
	}

	public boolean isFilterGetters() {
		return (fStepFilterMask & FILTER_GETTERS) > 0;
	}

	public void setFilterGetters(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_GETTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_GETTERS ^ XOR_MASK);
		}
	}

	public boolean isFilterSetters() {
		return (fStepFilterMask & FILTER_SETTERS) > 0;
	}

	public void setFilterSetters(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_SETTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_SETTERS ^ XOR_MASK);
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
			fStepFilterMask = fStepFilterMask
					& (STEP_FILTERS_ENABLED ^ XOR_MASK);
		}
	}

	/**
	 * @see IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() {
		return fThreads.size() > 0;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Sets the launch this target is contained in
	 * 
	 * @param launch
	 *            the launch this target is contained in
	 */
	private void setLaunch(ILaunch launch) {
		fLaunch = launch;
	}

	/**
	 * Returns the number of suspend events that have occurred in this target.
	 * 
	 * @return the number of suspend events that have occurred in this target
	 */
	protected int getSuspendCount() {
		return fSuspendCount;
	}

	/**
	 * Increments the suspend counter for this target based on the reason for
	 * the suspend event. The suspend count is not updated for implicit
	 * evaluations.
	 * 
	 * @param eventDetail
	 *            the reason for the suspend event
	 */
	protected void incrementSuspendCount(int eventDetail) {
		if (eventDetail != DebugEvent.EVALUATION_IMPLICIT) {
			fSuspendCount++;
		}
	}

	/**
	 * Returns an evaluation engine for the given project, creating one if
	 * necessary.
	 * 
	 * @param project
	 *            java project
	 * @return evaluation engine
	 */
	public IAstEvaluationEngine getEvaluationEngine(IJavaProject project) {
		if (fEngines == null) {
			fEngines = new HashMap<IJavaProject, IAstEvaluationEngine>(2);
		}
		IAstEvaluationEngine engine = fEngines
				.get(project);
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
		if (!isAvailable()) {
			return false;
		}
		VirtualMachine vm = getVM();
		if (vm != null) {
			return vm.canGetCurrentContendedMonitor() && vm.canGetMonitorInfo()
					&& vm.canGetOwnedMonitorInfo();
		}
		return false;
	}

	/**
	 * Sets whether or not this debug target is currently performing a hot code
	 * replace.
	 */
	public void setIsPerformingHotCodeReplace(boolean isPerformingHotCodeReplace) {
		fIsPerformingHotCodeReplace = isPerformingHotCodeReplace;
	}

	/**
	 * @see IJavaDebugTarget#isPerformingHotCodeReplace()
	 */
	public boolean isPerformingHotCodeReplace() {
		return fIsPerformingHotCodeReplace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#supportsAccessWatchpoints()
	 */
	public boolean supportsAccessWatchpoints() {
		VirtualMachine vm = getVM();
		if (isAvailable() && vm != null) {
			return vm.canWatchFieldAccess();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#supportsModificationWatchpoints
	 * ()
	 */
	public boolean supportsModificationWatchpoints() {
		VirtualMachine vm = getVM();
		if (isAvailable() && vm != null) {
			return vm.canWatchFieldModification();
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#setDefaultStratum()
	 */
	public void setDefaultStratum(String stratum) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			vm.setDefaultStratum(stratum);
		}
	}

	public String getDefaultStratum() {
		VirtualMachine vm = getVM();
		if (vm != null) {
			return vm.getDefaultStratum();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStepFilters#supportsStepFilters()
	 */
	public boolean supportsStepFilters() {
		return isAvailable();
	}

	/**
	 * When the breakpoint manager disables, remove all registered breakpoints
	 * requests from the VM. When it enables, reinstall them.
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		if (!isAvailable()) {
			return;
		}
		Iterator<IBreakpoint> breakpoints = ((ArrayList<IBreakpoint>) ((ArrayList<IBreakpoint>) getBreakpoints())
				.clone()).iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) breakpoints.next();
			try {
				if (enabled) {
					breakpoint.addToTarget(this);
				} else if (breakpoint.shouldSkipBreakpoint()) {
					breakpoint.removeFromTarget(this);
				}
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse
	 * .debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (events.length == 1) {
			DebugEvent event = events[0];
			if (event.getSource().equals(getProcess())
					&& event.getKind() == DebugEvent.TERMINATE) {
				// schedule a job to clean up the target in case we never get a
				// terminate/disconnect
				// event from the VM
				int timeout = getRequestTimeout();
				if (timeout < 0) {
					timeout = 3000;
				}
				new CleanUpJob().schedule(timeout);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	/**
	 * Adds the given thread group to the list of known thread groups. Also adds
	 * any parent thread groups that have not already been added to the list.
	 * 
	 * @param group
	 *            thread group to add
	 */
	void addThreadGroup(ThreadGroupReference group) {
		ThreadGroupReference currentGroup = group;
		while (currentGroup != null) {
			synchronized (fGroups) {
				if (findThreadGroup(currentGroup) == null) {
					JDIThreadGroup modelGroup = new JDIThreadGroup(this,
							currentGroup);
					fGroups.add(modelGroup);
					currentGroup = currentGroup.parent();
				} else {
					currentGroup = null;
				}
			}
		}
	}

	JDIThreadGroup findThreadGroup(ThreadGroupReference group) {
		synchronized (fGroups) {
			Iterator<JDIThreadGroup> groups = fGroups.iterator();
			while (groups.hasNext()) {
				JDIThreadGroup modelGroup = groups.next();
				if (modelGroup.getUnderlyingThreadGroup().equals(group)) {
					return modelGroup;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#getThreadGroups()
	 */
	public IJavaThreadGroup[] getRootThreadGroups() throws DebugException {
		try {
			VirtualMachine vm = getVM();
			if (vm == null) {
				return new IJavaThreadGroup[0];
			}
			List<ThreadGroupReference> groups = vm.topLevelThreadGroups();
			List<JDIThreadGroup> modelGroups = new ArrayList<JDIThreadGroup>(groups.size());
			for(ThreadGroupReference ref : groups) {
				JDIThreadGroup group = findThreadGroup(ref);
				if (group != null) {
					modelGroups.add(group);
				}
			}
			return modelGroups.toArray(new IJavaThreadGroup[modelGroups.size()]);
		} catch (VMDisconnectedException e) {
			// if the VM has disconnected, there are no thread groups
			return new IJavaThreadGroup[0];
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIDebugTarget_1, e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#getAllThreadGroups()
	 */
	public IJavaThreadGroup[] getAllThreadGroups() throws DebugException {
		synchronized (fGroups) {
			return fGroups
					.toArray(new IJavaThreadGroup[fGroups.size()]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#supportsInstanceRetrieval()
	 */
	public boolean supportsInstanceRetrieval() {
		VirtualMachine vm = getVM();
		if (vm != null) {
			return vm.canGetInstanceInfo();
		}
		return false;
	}

	/**
	 * Sends a JDWP command to the back end and returns the JDWP reply packet as
	 * bytes. This method creates an appropriate command header and packet id,
	 * before sending to the back end.
	 * 
	 * @param commandSet
	 *            command set identifier as defined by JDWP
	 * @param commandId
	 *            command identifier as defined by JDWP
	 * @param data
	 *            any bytes required for the command that follow the command
	 *            header or <code>null</code> for commands that have no data
	 * @return raw reply packet as bytes defined by JDWP
	 * @exception IOException
	 *                if an error occurs sending the packet or receiving the
	 *                reply
	 * @since 3.3
	 */
	public byte[] sendJDWPCommand(byte commandSet, byte commandId, byte[] data)
			throws IOException {
		int command = (256 * commandSet) + commandId;
		JdwpReplyPacket reply = ((VirtualMachineImpl) getVM()).requestVM(
				command, data);
		return reply.getPacketAsBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#supportsForceReturn()
	 */
	public boolean supportsForceReturn() {
		VirtualMachine machine = getVM();
		if (machine == null) {
			return false;
		}
		return machine.canForceEarlyReturn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#
	 * supportsSelectiveGarbageCollection()
	 */
	public boolean supportsSelectiveGarbageCollection() {
		return fSupportsDisableGC;
	}

	/**
	 * Sets whether this target supports selectively disabling/enabling garbage
	 * collection of specific objects.
	 * 
	 * @param enableGC
	 *            whether this target supports selective GC
	 */
	void setSupportsSelectiveGarbageCollection(boolean enableGC) {
		fSupportsDisableGC = enableGC;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#getVMName()
	 */
	public String getVMName() throws DebugException {
		VirtualMachine vm = getVM();
		if (vm == null) {
			requestFailed(JDIDebugModelMessages.JDIDebugTarget_2,
					new VMDisconnectedException());
		}
		try {
			return vm.name();
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIDebugTarget_2, e);
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#getVersion()
	 */
	public String getVersion() throws DebugException {
		VirtualMachine vm = getVM();
		if (vm == null) {
			requestFailed(JDIDebugModelMessages.JDIDebugTarget_4,
					new VMDisconnectedException());
		}
		try {
			return vm.version();
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIDebugTarget_4, e);
			// execution will not reach this line, as
			// #targetRequestFailed will throw an exception
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#refreshState()
	 */
	public void refreshState() throws DebugException {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		boolean prevSuspend = isSuspended();
		int running = 0;
		List<JDIThread> toSuspend = new ArrayList<JDIThread>();
		List<JDIThread> toResume = new ArrayList<JDIThread>();
		List<JDIThread> toRefresh = new ArrayList<JDIThread>();
		Iterator<JDIThread> iterator = getThreadIterator();
		while (iterator.hasNext()) {
			JDIThread thread = iterator.next();
			boolean modelSuspended = thread.isSuspended();
			ThreadReference reference = thread.getUnderlyingThread();
			try {
				boolean realSuspended = reference.isSuspended();
				if (realSuspended) {
					if (modelSuspended) {
						// Even if the model is suspended, it might be in a
						// different location so refresh
						toRefresh.add(thread);
					} else {
						// The thread is actually suspended, refresh frames and
						// fire suspend event.
						toSuspend.add(thread);
					}
				} else {
					running++;
					if (modelSuspended) {
						// thread is actually running, model is suspended,
						// resume model
						toResume.add(thread);
					}
					// else both are running - OK
				}
			} catch (InternalException e) {
				requestFailed(e.getMessage(), e);
			}
		}
		// if the entire target changed state/fire events at target level, else
		// fire thread events
		boolean targetLevelEvent = false;
		if (prevSuspend) {
			if (running > 0) {
				// was suspended, but now a thread is running
				targetLevelEvent = true;
			}
		} else {
			if (running == 0) {
				// was running, but now all threads are suspended
				targetLevelEvent = true;
			}
		}
		if (targetLevelEvent) {
			iterator = toSuspend.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.suspendedByVM();
			}
			iterator = toResume.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.resumedByVM();
			}
			iterator = toRefresh.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.preserveStackFrames();
			}
			if (running == 0) {
				synchronized (this) {
					setSuspended(true);
				}
				fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			} else {
				synchronized (this) {
					setSuspended(false);
				}
				fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
		} else {
			iterator = toSuspend.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.preserveStackFrames();
				thread.setRunning(false);
				thread.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			}
			iterator = toResume.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.setRunning(true);
				thread.fireResumeEvent(DebugEvent.CLIENT_REQUEST);
			}
			iterator = toRefresh.iterator();
			while (iterator.hasNext()) {
				JDIThread thread = iterator.next();
				thread.preserveStackFrames();
				thread.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.debug.core.IJavaDebugTarget#sendCommand(byte, byte,
	 * byte[])
	 */
	public byte[] sendCommand(byte commandSet, byte commandId, byte[] data)
			throws DebugException {
		try {
			return sendJDWPCommand(commandSet, commandId, data);
		} catch (IOException e) {
			requestFailed(e.getMessage(), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#addHotCodeReplaceListener
	 * (org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener)
	 */
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fHCRListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.debug.core.IJavaDebugTarget#removeHotCodeReplaceListener
	 * (org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener)
	 */
	public void removeHotCodeReplaceListener(
			IJavaHotCodeReplaceListener listener) {
		fHCRListeners.remove(listener);
	}

	/**
	 * Returns an array of current hot code replace listeners.
	 * 
	 * @return registered hot code replace listeners
	 * @since 3.6
	 */
	public Object[] getHotCodeReplaceListeners() {
		return fHCRListeners.getListeners();
	}
}
