/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Jesper Steen Moller - enhancement 254677 - filter getters/setters
 *     Andrey Loskutov <loskutov@gmx.de> - bug 5188 - breakpoint filtering
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.TimeoutException;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
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
import com.sun.jdi.ClassType;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
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
	 *
	 * TODO investigate making this a synchronized collection, to remove all this copying
	 * @see #getThreadIterator()
	 */
	private List<JDIThread> fThreads;

	/**
	 * List of thread groups in this target.
	 */
	private List<JDIThreadGroup> fGroups;

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
	 * Whether in the process of disconnecting
	 */
	private boolean fDisconnecting;

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
	private boolean fSupportsDisableGC;
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
	private EventDispatcher fEventDispatcher;

	/**
	 * The thread start event handler
	 */
	private ThreadStartHandler fThreadStartHandler;

	/**
	 * Handles changes in thread names, detected via a breakpoint in {@link java.lang.Thread#setName(String)}.
	 */
	private ThreadNameChangeHandler fThreadNameChangeHandler;

	/**
	 * Whether this VM is suspended.
	 */
	private boolean fSuspended = true;

	/**
	 * Whether the VM should be resumed on startup
	 */
	private boolean fResumeOnStartup;

	/**
	 * The launch this target is contained in
	 */
	private ILaunch fLaunch;

	/**
	 * Count of the number of suspend events in this target
	 */
	private int fSuspendCount;

	/**
	 * Evaluation engine cache by Java project. Engines are disposed when this
	 * target terminates.
	 */
	private Map<IJavaProject, IAstEvaluationEngine> fEngines;

	/**
	 * List of step filters - each string is a pattern/fully qualified name of a
	 * type to filter.
	 */
	private String[] fStepFilters;

	/**
	 * Step filter state mask.
	 */
	private int fStepFilterMask;

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
	private boolean fIsPerformingHotCodeReplace;

	/**
	 * Target specific HCR listeners
	 *
	 * @since 3.6
	 */
	private ListenerList<IJavaHotCodeReplaceListener> fHCRListeners = new ListenerList<>();

	/**
	 * Java scope of the current launch, "null" means everything is in scope
	 */
	private IJavaSearchScope fScope;

	/**
	 * Java projects of the current launch, "null" means everything is in scope
	 */
	private Set<IProject> fProjects;

	/**
	 * Java types from breakpoints with the flag if they are in scope for current launch
	 */
	private Map<String, Boolean> fKnownTypes = new HashMap<>();

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
		setDisconnecting(false);
		setName(name);
		prepareBreakpointsSearchScope();
		setBreakpoints(new ArrayList<>(5));
		setThreadList(new ArrayList<>());
		fGroups = new ArrayList<>(5);
		setOutOfSynchTypes(new ArrayList<>(0));
		setHCROccurred(false);
		initialize();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().getBreakpointManager()
				.addBreakpointManagerListener(this);
	}


	private void prepareBreakpointsSearchScope() {
		boolean enableFiltering = Platform.getPreferencesService().getBoolean(
				JDIDebugPlugin.getUniqueIdentifier(),
				JDIDebugModel.PREF_FILTER_BREAKPOINTS_FROM_UNRELATED_SOURCES,
				true,
				null);
		ILaunchConfiguration config = getLaunch().getLaunchConfiguration();
		if (!enableFiltering || config == null) {
			return;
		}
		try {
			// See IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH
			boolean defaultClasspath = config.getAttribute("org.eclipse.jdt.launching.DEFAULT_CLASSPATH", true); //$NON-NLS-1$
			if(!defaultClasspath){
				return;
			}

			IResource[] resources = config.getMappedResources();
			if (resources != null && resources.length != 0) {
				Set<IJavaProject> javaProjects = getJavaProjects(resources);
				fProjects = collectReferencedJavaProjects(javaProjects);
				fScope = createSourcesOnlyScope();
				return;
			}
			// See IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME
			String projectName = config.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", (String)null); //$NON-NLS-1$
			if(projectName != null && !projectName.isEmpty()){
				Set<IJavaProject> javaProjects = getJavaProjects(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
				fProjects = collectReferencedJavaProjects(javaProjects);
				fScope = createSourcesOnlyScope();
				return;
			}
		} catch (CoreException e) {
			logError(e);
		}
	}

	private IJavaSearchScope createSourcesOnlyScope() {
		int includeMask = IJavaSearchScope.SOURCES;
		Set<IJavaProject> javaProjects = getJavaProjects(ResourcesPlugin.getWorkspace().getRoot().getProjects());
		return SearchEngine.createJavaSearchScope(javaProjects.toArray(new IJavaElement[javaProjects.size()]), includeMask);
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
			//TODO investigate making fThreads be a synchronized collection, to remove all this copying
			threadList = new ArrayList<>(fThreads);
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
	private void setThreadList(List<JDIThread> threads) {
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
			@Override
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
		setThreadNameChangeHandler(new ThreadNameChangeHandler());
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

	@Override
	public IThread[] getThreads() {
		synchronized (fThreads) {
			return fThreads.toArray(new IThread[0]);
		}
	}

	@Override
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
			if (thread.canResume()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canSuspend() {
		if (isAvailable()) {
			// allow suspend when one or more threads are currently running
			IThread[] threads = getThreads();
			for (IThread thread : threads) {
				if (((JDIThread) thread).canSuspend()) {
					return true;
				}
			}
			return !isSuspended();
		}
		return false;
	}

	@Override
	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
	}

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

	@Override
	public boolean supportsHotCodeReplace() {
		return supportsJ9HotCodeReplace() || supportsJDKHotCodeReplace();
	}

	@Override
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
			setDisconnecting(true);
			disposeThreadHandlers();
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
	private void disposeThreadHandlers() {
		ThreadStartHandler handler = getThreadStartHandler();
		if (handler != null) {
			handler.deleteRequest();
		}
		ThreadNameChangeHandler nameChangeHandler = getThreadNameChangeHandler();
		if (nameChangeHandler != null) {
			nameChangeHandler.deleteRequest();
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
		fOutOfSynchTypes = new HashSet<>();
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

	@Override
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

	@Override
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
			if (thread.getUnderlyingThread().equals(tr)) {
				return thread;
			}
		}
		return null;
	}

	@Override
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

	@Override
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

	@Override
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
		return !(isTerminated() || isTerminating() || isDisconnected() || isDisconnecting());
	}

	@Override
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
					new VMDisconnectedException());
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

	@Override
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

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		boolean isJava = breakpoint instanceof IJavaBreakpoint;
		if(!isJava){
			return false;
		}
		if(fScope == null){
			// No checks, everything in scope: the filtering is disabled
			return true;
		}

		IJavaBreakpoint jBreakpoint = (IJavaBreakpoint) breakpoint;

		// Check if the breakpoint from resources in target scope
		IMarker marker = jBreakpoint.getMarker();
		if(marker == null) {
			// Marker not available, so don't guess and allow the breakpoint to be set
			return true;
		}

		return supportsResource(() -> jBreakpoint.getTypeName(), marker.getResource());
	}


	public boolean supportsResource(Callable<String> typeNameSupplier, IResource resource) {
		if (fScope == null) {
			// No checks, everything in scope: the filtering is disabled
			return true;
		}
		// Java exception breakpoints have wsp root as resource
		if(resource == null || resource == ResourcesPlugin.getWorkspace().getRoot()) {
			return true;
		}
		Set<IProject> projects = fProjects;
		if(projects == null){
			return true;
		}

		// Breakpoint from project known by the resource mapping
		if (projects.contains(resource.getProject())) {
			return true;
		}

		// Check if this is a resource which is linked to any of the projects
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		URI uri = resource.getLocationURI();
		if(uri != null){
			IFile[] files = root.findFilesForLocationURI(uri);
			for (IFile file : files) {
				if(projects.contains(file.getProject())){
					return true;
				}
			}
		}

		Map<String, Boolean> knownTypes = fKnownTypes;
		if(knownTypes == null){
			return true;
		}

		// breakpoint belongs to resource outside of referenced projects?
		// This can be also an incomplete resource mapping.
		// Try to see if the type available multiple times in workspace
		try {
			String typeName = typeNameSupplier.call();
			if(typeName != null){
				Boolean known = knownTypes.get(typeName);
				if(known != null){
					return known.booleanValue();
				}
				boolean supportedBreakpoint = !hasMultipleMatchesInWorkspace(typeName);
				knownTypes.put(typeName, Boolean.valueOf(supportedBreakpoint));
				return supportedBreakpoint;
			}
		}
		catch (Exception e) {
			logError(e);
		}
		// we don't know why computation failed, so let assume the breakpoint is supported.
		return true;
	}

	private Set<IJavaProject> getJavaProjects(IResource... resources) {
		Set<IJavaProject> projects = new LinkedHashSet<>();
		for (IResource resource : resources) {
			IProject project = resource.getProject();
			if(!project.isAccessible()){
				continue;
			}
			IJavaElement javaElement = JavaCore.create(project);
			if(javaElement != null) {
				projects.add(javaElement.getJavaProject());
			}
		}
		return projects;
	}

	/**
	 * @param javaProjects the set which will be updated with all referenced java projects
	 * @return corresponding resource projects
	 */
	private Set<IProject> collectReferencedJavaProjects(Set<IJavaProject> javaProjects) {
		Set<IProject> projects = new LinkedHashSet<>();
		// collect all references
		for (IJavaProject jProject : javaProjects) {
			projects.add(jProject.getProject());
			addReferencedProjects(jProject, projects);
		}
		// update java projects set with new java projects we might collected
		for (IProject project : projects) {
			IJavaProject jProject = JavaCore.create(project);
			if(jProject != null){
				javaProjects.add(jProject);
			}
		}
		return projects;
	}

	private void addReferencedProjects(IJavaProject jProject, Set<IProject> projects) {
		IClasspathEntry[] cp;
		try {
			// we want resolved classpath to get variables and containers resolved for us
			cp = jProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			// we don't care here
			return;
		}
		for (IClasspathEntry cpe : cp) {
			int entryKind = cpe.getEntryKind();
			IProject project = null;
			switch (entryKind) {
				case IClasspathEntry.CPE_LIBRARY:
					// we must check for external folders coming from other projects in the workspace
					project = getProjectOfExternalFolder(cpe);
					break;
				case IClasspathEntry.CPE_PROJECT:
					// we must add any projects referenced
					project = getProject(cpe);
					break;
				case IClasspathEntry.CPE_SOURCE:
					// we have the project already
				case IClasspathEntry.CPE_VARIABLE:
					// should not happen on resolved classpath
				case IClasspathEntry.CPE_CONTAINER:
					// should not happen on resolved classpath
				default:
					break;
			}

			if(project == null || projects.contains(project) || !project.isAccessible()){
				continue;
			}

			IJavaProject referenced = JavaCore.create(project);
			if (referenced != null) {
				// we have found new project, start recursion
				projects.add(project);
				addReferencedProjects(referenced, projects);
			}
		}
	}

	private IProject getProject(IClasspathEntry cpe) {
		IPath projectPath = cpe.getPath();
		if (projectPath == null || projectPath.isEmpty()) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectPath.lastSegment());
		if (project.isAccessible()) {
			return project;
		}
		return null;
	}

	private static IProject getProjectOfExternalFolder(IClasspathEntry cpe){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if(cpe.getContentKind() == IPackageFragmentRoot.K_BINARY){
			IPath path = cpe.getPath();
			if(path == null || path.isEmpty()){
				return null;
			}
			IProject project = root.getProject(path.segment(0));
			if(project.isAccessible()) {
				return project;
			}
		}
		return null;
	}

	/*
	 * Checks if the given type (computed from a breakpoint resource) exists multiple times in the workspace.
	 */
	private boolean hasMultipleMatchesInWorkspace(final String typeName) {
		final AtomicInteger matchCount = new AtomicInteger(0);
		String packageName = null;
		String simpleName = typeName;
		int lastDot = typeName.lastIndexOf('.');
		if(lastDot > 0 && lastDot < typeName.length() - 1){
			packageName = typeName.substring(0, lastDot);
			simpleName = typeName.substring(lastDot + 1);
		}
		// get rid of inner types, use outer type name
		final String fqName;
		int firstDoll = simpleName.indexOf('$');
		if(firstDoll > 0 && firstDoll < simpleName.length() - 1){
			simpleName = simpleName.substring(0, firstDoll);
			fqName = packageName + "." + simpleName; //$NON-NLS-1$
		} else {
			fqName = typeName;
		}

		final IProgressMonitor monitor = new NullProgressMonitor();
        TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				IType type = match.getType();
				if(fqName.equals(type.getFullyQualifiedName())){
					int count = matchCount.incrementAndGet();
					if(count > 1) {
						monitor.setCanceled(true);
					}
					return;
				}
			}
		};
		try {
			SearchEngine searchEngine = new SearchEngine();
			searchEngine.searchAllTypeNames(packageName != null? packageName.toCharArray() : null,
					SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
					simpleName.toCharArray(),
			        SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
			        IJavaSearchConstants.TYPE,
			        fScope,
			        requestor,
			        IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			        monitor);
		} catch (JavaModelException e) {
			logError(e);
			return true;
		} catch (OperationCanceledException e){
			// expected if we cancelled the search on second match
		}
		return matchCount.get() > 1;
	}

	/**
	 * Notification a breakpoint has been added to the breakpoint manager. If
	 * the breakpoint is a Java breakpoint and this target is not terminated,
	 * the breakpoint is installed.
	 *
	 * @param breakpoint
	 *            the breakpoint added to the breakpoint manager
	 */
	@Override
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
	@Override
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
	@Override
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

	@Override
	public void suspend() throws DebugException {
		if (isSuspended()) {
			IThread[] threads = getThreads();
			for (IThread thread : threads) {
				((JDIThread) thread).suspend();
			}
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

	@Override
	public void terminate() throws DebugException {
		if (!isAvailable()) {
			return;
		}
		if (!supportsTerminate()) {
			notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_termination);
		}
		try {
			setTerminating(true);
			disposeThreadHandlers();
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
		setDisconnecting(false);
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
		DebugPlugin.getDefault().getBreakpointManager().enableTriggerPoints(null, true);
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
		fKnownTypes = null;
		fProjects = null;
		fScope = null;
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
		List<IBreakpoint> list = new ArrayList<>(getBreakpoints());
		for(IBreakpoint bp : list) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) bp;
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
		List<IBreakpoint> list = new ArrayList<>(getBreakpoints());
		for(IBreakpoint bp : list) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) bp;
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

	@Override
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IJavaDebugTarget.class) {
			return (T) this;
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

	@Override
	public IJavaType[] getJavaTypes(String name) throws DebugException {
		try {
			// get java.lang.Class
			VirtualMachine vm = getVM();
			if (vm == null) {
				requestFailed(
						JDIDebugModelMessages.JDIDebugTarget_Unable_to_retrieve_types___VM_disconnected__4,
						new VMDisconnectedException());
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

	@Override
	public IJavaValue newValue(boolean value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(byte value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(char value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(double value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(float value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(int value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(long value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(short value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue newValue(String value) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			Value v = vm.mirrorOf(value);
			return JDIValue.createValue(this, v);
		}
		return null;
	}

	@Override
	public IJavaValue nullValue() {
		return JDIValue.createValue(this, null);
	}

	@Override
	public IJavaValue voidValue() {
		return new JDIVoidValue(this);
	}

	protected boolean isTerminating() {
		return fTerminating;
	}

	protected void setTerminating(boolean terminating) {
		fTerminating = terminating;
	}

	protected boolean isDisconnecting() {
		return fDisconnecting;
	}

	protected void setDisconnecting(boolean disconnecting) {
		fDisconnecting = disconnecting;
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
		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspendVote, EventSet eventSet) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			try {
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443727
				// the backing ThreadReference could be read in as null
				if (thread == null || thread.isCollected()) {
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

		@Override
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

		// terminated threads marked for removal from the fThreads list
		private Map<Event, JDIThread> toRemove = Collections.synchronizedMap(new HashMap<>());

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
		 * Locates the model thread associated with the underlying JDI thread that has terminated, and marks it for removal from the collection of
		 * threads belonging to this debug target. A terminate event is fired for the model thread.
		 *
		 * @param event
		 *            a thread death event
		 * @param target
		 *            the target in which the thread died
		 * @return <code>true</code> - the thread should be resumed
		 */
		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target,
				boolean suspend, EventSet eventSet) {
			ThreadReference ref = ((ThreadDeathEvent) event).thread();
			JDIThread thread = findThread(ref);
			if (thread == null) {
				thread = target.findThread(ref);
			}
			if (thread != null) {
				toRemove.put(event, thread);
				// triggers DebugEvent
				thread.terminated();
			}
			return true;
		}

		/**
		 * Removes the model thread associated with the underlying JDI thread that has terminated from the collection of threads belonging to this
		 * debug target.
		 *
		 * @param event
		 *            a thread death event
		 * @param target
		 *            the target in which the thread died
		 */
		@Override
		public void eventSetComplete(Event event, JDIDebugTarget target, boolean suspendVote, EventSet eventSet) {
			JDIThread thread = toRemove.remove(event);
			if (thread != null) {
				synchronized (fThreads) {
					fThreads.remove(thread);
				}
			}
		}

	}

	/**
	 * Triggers updates on a thread when {@link java.lang.Thread#setName(String)} is called on that thread, in the target JVM.
	 */
	class ThreadNameChangeHandler implements IJDIEventListener {

		/**
		 * Environment variable that can be passed down to Eclipse, to disable this listener.
		 */
		private static final String DISABLE_THREAD_NAME_CHANGE_LISTENER = "org.eclipse.jdt.internal.debug.core.model.ThreadNameChangeListener.disable"; //$NON-NLS-1$
		private static final String TYPE_NAME = "java.lang.Thread"; //$NON-NLS-1$
		private static final String METHOD_NAME = "setName"; //$NON-NLS-1$
		private static final String METHOD_SIGNATURE = "(Ljava/lang/String;)V"; //$NON-NLS-1$

		private EventRequest request;

		ThreadNameChangeHandler() {
			String disableListenerSystemProperty = System.getProperty(DISABLE_THREAD_NAME_CHANGE_LISTENER);
			boolean isDisabled = String.valueOf(Boolean.TRUE).equals(disableListenerSystemProperty);
			if (!isDisabled) {
				createRequest();
			}
		}

		/**
		 * Creates a breakpoint request at {@link java.lang.Thread#setName(String)} that doesn't suspend the target JVM.
		 */
		void createRequest() {
			EventRequestManager manager = getEventRequestManager();
			if (manager != null) {
				try {
					Location location = locationOfSetNameMethod();
					if (location != null) {
						request = manager.createBreakpointRequest(location);
						request.setSuspendPolicy(EventRequest.SUSPEND_NONE);
						request.enable();
						addJDIEventListener(this, request);
					}
				} catch (RuntimeException e) {
					String errorMessage = "Failed to add thread name change listener to debug target " + JDIDebugTarget.this; //$NON-NLS-1$
					IStatus errorStatus = new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), errorMessage, e);
					logRequestStatus(errorStatus);
				}
			}
		}

		private Location locationOfSetNameMethod() {
			List<ReferenceType> types = jdiClassesByName(TYPE_NAME);
			boolean foundThreadType = false;
			for (ReferenceType type : types) {
				if (type instanceof ClassType) {
					foundThreadType = true;
					Method method = ((ClassType) type).concreteMethodByName(METHOD_NAME, METHOD_SIGNATURE);
					if (method != null && !method.isNative()) {
						Location location = method.location();
						if (location != null && location.codeIndex() != -1) {
							return location;
						}
						logRequestWarning("Unable to find location of java.lang.Thread.setName() in debuggee JVM, for type " + type); //$NON-NLS-1$
					} else {
						logRequestWarning("Unable to find java.lang.Thread.setName() method in debuggee JVM, for type " + type); //$NON-NLS-1$
					}
				}
			}
			if (!foundThreadType) {
				logRequestWarning("Unable to find type java.lang.Thread.setName() in debuggee JVM"); //$NON-NLS-1$
			}
			return null;
		}

		void deleteRequest() {
			if (request != null) {
				removeJDIEventListener(this, request);
			}
		}

		@Override
		public boolean handleEvent(Event event, JDIDebugTarget target, boolean suspend, EventSet eventSet) {
			ThreadReference ref = ((LocatableEvent) event).thread();
			JDIThread thread = findThread(ref);
			if (thread == null) {
				thread = target.findThread(ref);
			}
			if (thread != null) {
				// trigger updates on the thread
				DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { new DebugEvent(thread, DebugEvent.CHANGE, DebugEvent.STATE) });
			}
			// we never suspend the thread
			return true;
		}

		@Override
		public void eventSetComplete(Event event, JDIDebugTarget target, boolean suspendVote, EventSet eventSet) {
			// nothing to do here, we do work in handleEvent
		}

		private void logRequestWarning(String warningMessage) {
			IStatus warningStatus = new Status(IStatus.WARNING, JDIDebugPlugin.getUniqueIdentifier(), warningMessage);
			logRequestStatus(warningStatus);
		}

		private void logRequestStatus(IStatus status) {
			if (isAvailable()) {
				JDIDebugPlugin.log(status);
			}
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

		@Override
		public boolean shouldRun() {
			return isAvailable();
		}

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

	private ThreadNameChangeHandler getThreadNameChangeHandler() {
		return fThreadNameChangeHandler;
	}

	private void setThreadNameChangeHandler(ThreadNameChangeHandler threadNameChangeHandler) {
		fThreadNameChangeHandler = threadNameChangeHandler;
	}

	/**
	 * Java debug targets do not support storage retrieval.
	 *
	 * @see IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		notSupported(JDIDebugModelMessages.JDIDebugTarget_does_not_support_storage_retrieval);
		// this line will not be executed as #notSupported(String)
		// will throw an exception
		return null;
	}

	@Override
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

	@Override
	public void launchAdded(ILaunch launch) {
	}

	@Override
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

	@Override
	public String[] getStepFilters() {
		return fStepFilters;
	}

	@Override
	public boolean isFilterConstructors() {
		return (fStepFilterMask & FILTER_CONSTRUCTORS) > 0;
	}

	@Override
	public boolean isFilterStaticInitializers() {
		return (fStepFilterMask & FILTER_STATIC_INITIALIZERS) > 0;
	}

	@Override
	public boolean isFilterSynthetics() {
		return (fStepFilterMask & FILTER_SYNTHETICS) > 0;
	}

	/*
	 * (non-Javadoc) Was added in 3.3, made API in 3.5
	 */
	@Override
	public boolean isStepThruFilters() {
		return (fStepFilterMask & STEP_THRU_FILTERS) > 0;
	}

	@Override
	public boolean isStepFiltersEnabled() {
		return (fStepFilterMask & STEP_FILTERS_ENABLED) > 0;
	}

	@Override
	public void setFilterConstructors(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_CONSTRUCTORS;
		} else {
			fStepFilterMask = fStepFilterMask
					& (FILTER_CONSTRUCTORS ^ XOR_MASK);
		}
	}

	@Override
	public void setFilterStaticInitializers(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_STATIC_INITIALIZERS;
		} else {
			fStepFilterMask = fStepFilterMask
					& (FILTER_STATIC_INITIALIZERS ^ XOR_MASK);
		}
	}

	@Override
	public void setFilterSynthetics(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_SYNTHETICS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_SYNTHETICS ^ XOR_MASK);
		}
	}

	/*
	 * (non-Javadoc) Was added in 3.3, made API in 3.5
	 */
	@Override
	public void setStepThruFilters(boolean thru) {
		if (thru) {
			fStepFilterMask = fStepFilterMask | STEP_THRU_FILTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (STEP_THRU_FILTERS ^ XOR_MASK);
		}
	}

	@Override
	public boolean isFilterGetters() {
		return (fStepFilterMask & FILTER_GETTERS) > 0;
	}

	@Override
	public void setFilterGetters(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_GETTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_GETTERS ^ XOR_MASK);
		}
	}

	@Override
	public boolean isFilterSetters() {
		return (fStepFilterMask & FILTER_SETTERS) > 0;
	}

	@Override
	public void setFilterSetters(boolean filter) {
		if (filter) {
			fStepFilterMask = fStepFilterMask | FILTER_SETTERS;
		} else {
			fStepFilterMask = fStepFilterMask & (FILTER_SETTERS ^ XOR_MASK);
		}
	}

	@Override
	public void setStepFilters(String[] list) {
		fStepFilters = list;
	}

	@Override
	public void setStepFiltersEnabled(boolean enabled) {
		if (enabled) {
			fStepFilterMask = fStepFilterMask | STEP_FILTERS_ENABLED;
		} else {
			fStepFilterMask = fStepFilterMask
					& (STEP_FILTERS_ENABLED ^ XOR_MASK);
		}
	}

	@Override
	public boolean hasThreads() {
		return fThreads.size() > 0;
	}

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
			fEngines = new HashMap<>(2);
		}
		IAstEvaluationEngine engine = fEngines
				.get(project);
		if (engine == null) {
			engine = EvaluationManager.newAstEvaluationEngine(project, this);
			fEngines.put(project, engine);
		}
		return engine;
	}

	@Override
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

	@Override
	public boolean isPerformingHotCodeReplace() {
		return fIsPerformingHotCodeReplace;
	}

	@Override
	public boolean supportsAccessWatchpoints() {
		VirtualMachine vm = getVM();
		if (isAvailable() && vm != null) {
			return vm.canWatchFieldAccess();
		}
		return false;
	}

	@Override
	public boolean supportsModificationWatchpoints() {
		VirtualMachine vm = getVM();
		if (isAvailable() && vm != null) {
			return vm.canWatchFieldModification();
		}
		return false;
	}

	@Override
	public void setDefaultStratum(String stratum) {
		VirtualMachine vm = getVM();
		if (vm != null) {
			vm.setDefaultStratum(stratum);
		}
	}

	@Override
	public String getDefaultStratum() {
		VirtualMachine vm = getVM();
		if (vm != null) {
			return vm.getDefaultStratum();
		}
		return null;
	}

	@Override
	public boolean supportsStepFilters() {
		return isAvailable();
	}

	/**
	 * When the breakpoint manager disables, remove all registered breakpoints
	 * requests from the VM. When it enables, reinstall them.
	 */
	@Override
	public void breakpointManagerEnablementChanged(boolean enabled) {
		if (!isAvailable()) {
			return;
		}
		List<IBreakpoint> list = new ArrayList<>(getBreakpoints());
		for(IBreakpoint bp : list) {
			JavaBreakpoint breakpoint = (JavaBreakpoint) bp;
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

	@Override
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

	@Override
	public IJavaThreadGroup[] getRootThreadGroups() throws DebugException {
		try {
			VirtualMachine vm = getVM();
			if (vm == null) {
				return new IJavaThreadGroup[0];
			}
			List<ThreadGroupReference> groups = vm.topLevelThreadGroups();
			List<JDIThreadGroup> modelGroups = new ArrayList<>(groups.size());
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

	@Override
	public IJavaThreadGroup[] getAllThreadGroups() throws DebugException {
		synchronized (fGroups) {
			return fGroups
					.toArray(new IJavaThreadGroup[fGroups.size()]);
		}
	}

	@Override
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

	@Override
	public boolean supportsForceReturn() {
		VirtualMachine machine = getVM();
		if (machine == null) {
			return false;
		}
		return machine.canForceEarlyReturn();
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public void refreshState() throws DebugException {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		boolean prevSuspend = isSuspended();
		int running = 0;
		List<JDIThread> toSuspend = new ArrayList<>();
		List<JDIThread> toResume = new ArrayList<>();
		List<JDIThread> toRefresh = new ArrayList<>();
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

	@Override
	public byte[] sendCommand(byte commandSet, byte commandId, byte[] data)
			throws DebugException {
		try {
			return sendJDWPCommand(commandSet, commandId, data);
		} catch (IOException e) {
			requestFailed(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void addHotCodeReplaceListener(IJavaHotCodeReplaceListener listener) {
		fHCRListeners.add(listener);
	}

	@Override
	public void removeHotCodeReplaceListener(
			IJavaHotCodeReplaceListener listener) {
		fHCRListeners.remove(listener);
	}

	/**
	 * Returns the current hot code replace listeners.
	 *
	 * @return registered hot code replace listeners
	 * @since 3.10
	 */
	public ListenerList<IJavaHotCodeReplaceListener> getHotCodeReplaceListeners() {
		return fHCRListeners;
	}

	/**
	 * Filters elements out of the given collections of resources and qualified names if there is no related resources in the given debug target. This
	 * method allows us to avoid bogus HCR attempts and "HCR failed" notifications.
	 *
	 * @param resourcesToFilter
	 *            the list of resources to filter
	 * @param qualifiedNamesToFilter
	 *            the list of qualified names to filter, which corresponds to the list of resources on a one-to-one-basis
	 */
	public void filterUnrelatedResources(List<IResource> resourcesToFilter, List<String> qualifiedNamesToFilter) {
		Iterator<IResource> resources = resourcesToFilter.iterator();
		Iterator<String> names = qualifiedNamesToFilter.iterator();
		while (resources.hasNext()) {
			boolean supported = supportsResource(() -> names.next(), resources.next());
			if (!supported) {
				resources.remove();
				names.remove();
			}
		}
	}

	/**
	 * Filters elements out of the given collections of resources and qualified names if there is no type corresponding type loaded in the given debug
	 * target. This method allows us to avoid bogus HCR attempts and "HCR failed" notifications.
	 *
	 * @param resources
	 *            the list of resources to filter
	 * @param qualifiedNames
	 *            the list of qualified names to filter, which corresponds to the list of resources on a one-to-one-basis
	 */
	public void filterNotLoadedTypes(List<IResource> resources, List<String> qualifiedNames) {
		for (int i = 0, numElements = qualifiedNames.size(); i < numElements; i++) {
			String name = qualifiedNames.get(i);
			List<ReferenceType> list = jdiClassesByName(name);
			if (list.isEmpty()) {
				// If no classes with the given name are loaded in the VM, don't
				// waste cycles trying to replace.
				qualifiedNames.remove(i);
				resources.remove(i);
				// Decrement the index and number of elements to compensate for
				// item removal
				i--;
				numElements--;
			}
		}
	}
}
