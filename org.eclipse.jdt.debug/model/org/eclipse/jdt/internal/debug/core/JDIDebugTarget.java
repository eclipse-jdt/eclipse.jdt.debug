package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

/**
 * Debug target for JDI debug model.
 */

public class JDIDebugTarget extends JDIDebugElement implements IJavaDebugTarget {
	
	private final static String PREFIX= "jdi_debug_target.";
	
	private final static String ERROR = PREFIX + "error.";
	private final static String ERROR_GET_NAME = ERROR + "get_name";
	private final static String ERROR_DISCONNECT_NOT_SUPPORTED = ERROR + "disconnect_not_supported";
	private final static String ERROR_DISCONNECT = ERROR + "disconnect";
	private final static String ERROR_HCR = ERROR + "hcr.exception";
	private final static String ERROR_HCR_NOT_SUPPORTED = ERROR + "hcr.not_supported";
	private final static String ERROR_HCR_FAILED = ERROR + "hcr.failed";
	private final static String ERROR_HCR_IGNORED = ERROR + "hcr.ignored";
	private final static String ERROR_BREAKPOINT_NO_TYPE = ERROR + "breakpoint.no_type";	
	private final static String ERROR_RESUME_NOT_SUPPORTED = ERROR + "resume.not_supported";
	private final static String ERROR_SUSPEND_NOT_SUPPORTED = ERROR + "suspend.not_supported";
	private final static String ERROR_TERMINATE_NOT_SUPPORTED = ERROR + "terminate.not_supported";
	private final static String ERROR_ACCESS_WATCHPOINT_NOT_SUPPORTED = ERROR + "access.not_supported";
	private final static String ERROR_MODIFICATION_WATCHPOINT_NOT_SUPPORTED = ERROR + "modification.net_supported";
	private final static String ERROR_TERMINATE = ERROR + "terminate.exception";
	private static final String ERROR_GET_CRC= ERROR + "get_crc";
	
	private static final int MAX_THREAD_DEATH_ATTEMPTS = 1;
	
	/**
	 * Key used to store the class name attribute pertinent to a
	 * specific method entry request. Used for method entry breakpoints.
	 */
	protected final static String CLASS_NAME= "className";
	/**
	 * Key used to store the name and signature
	 * attribute pertinent to a specific method 
	 * entry request breakpoint. Used for method entry breakpoints.
	 */
	protected final static String BREAKPOINT_INFO= "breakpointInfo";
	/**
	 * Associated system process, or <code>null</code> if not available.
	 */
	protected IProcess fProcess;
	/**
	 * Underlying virtual machine.
	 */
	protected VirtualMachine fVirtualMachine;
	/**
	 * Whether terminate is supported.
	 */
	protected boolean fSupportsTerminate;
	/**
	 * Whether terminated or disconnected
	 */
	protected boolean fTerminated;
	/**
	 * Whether disconnect is supported.
	 */
	protected boolean fSupportsDisconnect;
	/**
	 * Table of deferred breakpoints (cannot be installed because the
	 * corresponding class is not yet loaded). 
	 * <p>
	 * Key: the fully qualified name of the class
	 * Value: a <code>List</code> of <code>IMarker</code>s representing the
	 * deferred breakpointsin that class.
	 */
	protected Map fDeferredBreakpointsByClass;
	/**
	 * Table of class prepare requests for deferred breakpoints
	 * <p>
	 * Key: the fully qualified name of the class<br>
	 * Value: a <code>ClassPrepareRequest</code>s
	 */
	protected Map fClassPrepareRequestsByClass;
	/**
	 * Table of installed breakpoints
	 * <p>
	 * Key: breakpoint (<code>IJavaBreakpoint</code>)
	 * Value: the event request associated with the breakpoint (<code>Object</code>).
	 */
	protected HashMap fInstalledBreakpoints;
		
	/**
	 * A flag indicating that a hot code replace is being performed, and
	 * breakpoint "installed" attributes should not be altered.
	 */
	protected boolean fInHCR = false;
	 
	/**
	 * The thread death object used to interrupt threads on the target.
	 */
	protected ObjectReference fThreadDeath;
	
	/**
	 * Number of attempts to create instance of thread death.
	 */
	protected int fThreadDeathAttempts = 0;

	/**
	 * The name of this target - set by the client on creation, or retrieved from the
	 * underlying VM.
	 */
	protected String fName;
	
	/**
	 * The class prepare request used to listen for ALL class loads so that when the very
	 * first class is loaded, we can create a ThreadDeath instance.  After this, the 
	 * request is deleted.
	 */
	protected ClassPrepareRequest fUniversalClassPrepareReq;
	
	/**
	 * A cache of evaluation contexts keyed by java projects. When 
	 * an evaluation is performed, a reuseable evaulation context
	 * is cached for the associated java project. Contexts are discarded
	 * when this VM terminates.
	 */
	protected HashMap fEvaluationContexts;
	
	/**
	 * Collection of temporary files deployed to the target for evaluation.
	 * These files are deleted when this target terminates.
	 */
	protected HashMap fTempFiles;

	/**
	 * The event dispatcher for this debug target, which runs in its
	 * own thread.
	 */
	 EventDispatcher fEventDispatcher= null;
	 
	/**
	 * Creates a new JDI debug target for the given virtual machine.
	 */

	public JDIDebugTarget(VirtualMachine jvm, String name, boolean supportTerminate, boolean supportDisconnect, IProcess process) {
		super(null);
		fSupportsTerminate= supportTerminate;
		fSupportsDisconnect= supportDisconnect;
		fVirtualMachine= jvm;
		fVirtualMachine.setDebugTraceMode(VirtualMachine.TRACE_NONE);
		fProcess= process;
		fTerminated= false;
		fName= name;

		fDeferredBreakpointsByClass= new HashMap(10);
		fClassPrepareRequestsByClass= new HashMap(5);
		fInstalledBreakpoints= new HashMap(10);
		initialize();
	}

	/**
	 * This is the first event we receive from the VM.
	 * The VM is resumed. This event is not generated when
	 * an attach is made to a VM that is already running
	 * (has already started up).
	 */
	public void handleVMStart(VMStartEvent event) {
		try {
			try {
				List threads= getChildren0();
				for (int i= 0; i < threads.size(); i++) {
					((JDIThread) threads.get(i)).setRunning(true);
				}
			} catch (DebugException e) {
				internalError(e);
			}
			fVirtualMachine.resume();
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
	public synchronized void initialize() {
		initializeRequests();
		fEventDispatcher= new EventDispatcher(this);
		new Thread(fEventDispatcher, JDIDebugModel.getPluginIdentifier() + ": JDI Event Dispatcher").start();
		initializeState();
		fireCreationEvent();
		initializeBreakpoints();
	}
	
	/**
	 * Adds all of the pre-existing threads to this debug target.  
	 */
	protected void initializeState() {

		List threads= null;
		try {
			threads= fVirtualMachine.allThreads();
		} catch (RuntimeException e) {
			internalError(e);
		}
		if (threads != null) {
			Iterator initialThreads= threads.iterator();
			while (initialThreads.hasNext()) {
				createChild((ThreadReference) initialThreads.next());
			}
		}
	}
	 
	/**
	 * Starts listening for thread starts, deaths, and class loads
	 */
	protected void initializeRequests() {
		EventRequest req= null;
		EventRequestManager manager= getEventRequestManager();
		try {
			req= manager.createThreadStartRequest();
			req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			req.enable();
		} catch (RuntimeException e) {
			internalError(e);
		}
		

		try {
			req= manager.createThreadDeathRequest();
			req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			req.enable();
		} catch (RuntimeException e) {
			internalError(e);
		}
		
		// Listen for all class loads so we can create an instance of ThreadDeath to terminate threads.
		// Once the first ClassPrepareEvent is seen and the ThreadDeath instance created, this request 
		// is deleted.  Note this has no effect on more selective ClassPrepareRequests for deferred 
		// breakpoints or exceptions.  
		fUniversalClassPrepareReq= listenForClassLoad("*");
	}

	/**
	 * Installs all breakpoints that currently exist in the breakpoint manager
	 */
	protected void initializeBreakpoints() {
		getBreakpointManager().addBreakpointListener(this);
		IBreakpoint[] bps = (IBreakpoint[]) getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] instanceof IJavaBreakpoint) {
				breakpointAdded((IJavaBreakpoint)bps[i]);
			}
		}
	}
	
	/**
	 * Creates, adds and returns a child for the given underlying thread reference.
	 */
	protected JDIThread createChild(ThreadReference thread) {
		JDIThread jdiThread= new JDIThread(this, thread);
		addChild(jdiThread);
		return jdiThread;
	}
	/**
	 * @see IDebugTarget
	 */
	public int getElementType() {
		return DEBUG_TARGET;
	}
	
	/**
	 * @see ISuspendResume
	 */
	public boolean canResume() {
		return false;
	}

	/**
	 * @see ISuspendResume
	 */
	public boolean canSuspend() {
		return false;
	}

	/**
	 * @see ITerminate
	 */
	public boolean canTerminate() {
		return fSupportsTerminate && !isTerminated();
	}

	/**
	 * @see IDisconnect
	 */
	public boolean canDisconnect() {
		return fSupportsDisconnect && !isDisconnected();
	}

	/**
	 * Returns whether this debug target supports hot code replace.
	 * 
	 * @return whether this debug target supports hot code replace
	 */
	public boolean supportsHotCodeReplace() {
		if (!isTerminated() && fVirtualMachine instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			try {
				return ((org.eclipse.jdi.hcr.VirtualMachine) fVirtualMachine).canReloadClasses();
			} catch (UnsupportedOperationException e) {
			}
		}
		return false;
	}

	/**
	 * Attempts to create an instance of <code>java.lang.ThreadDeath</code> in the target VM.
	 * This instance will be used to terminate threads in the target VM.
	 * Note that if a thread death instance is not created threads will return <code>false</code>
	 * to <code>ITerminate#canTerminate()</code>.
	 */
	protected void createThreadDeathInstance(ThreadReference threadRef) {
		if (fThreadDeathAttempts == MAX_THREAD_DEATH_ATTEMPTS) {
			if (fUniversalClassPrepareReq != null) {
				try {
					getEventRequestManager().deleteEventRequest(fUniversalClassPrepareReq);
					fClassPrepareRequestsByClass.remove("*");
				} catch (RuntimeException e) {
					internalError(e);
				}
				fUniversalClassPrepareReq = null;
			}
			return;
		}
		fThreadDeathAttempts++;
		// Try to create an instance of java.lang.ThreadDeath
		// NB: This has to be done when the VM is interrupted by an event
		if (fThreadDeath == null) {
			JDIThread jt = findThread(threadRef);
			if (jt != null && jt.fInEvaluation) {
				// invalid state to perform an evaluation
				return;
			}
			//non NLS
			List classes= jdiClassesByName("java.lang.ThreadDeath");
			if (classes != null && classes.size() != 0) {
				ClassType threadDeathClass= (ClassType) classes.get(0);
				Method constructor= null;
				try {
					constructor= threadDeathClass.concreteMethodByName("<init>", "()V");
				} catch (RuntimeException e) {
					internalError(e);
					return;
				}
				try {
					fThreadDeath= threadDeathClass.newInstance(threadRef, constructor, new LinkedList(), ClassType.INVOKE_SINGLE_THREADED);
				} catch (ClassNotLoadedException e) {
					internalError(e);
				} catch (InvalidTypeException e) {
					internalError(e);
				} catch (InvocationException e) {
					internalError(e);
				} catch (IncompatibleThreadStateException e) {
					internalError(e);
				} catch (RuntimeException e) {
					internalError(e);
				}
				if (fThreadDeath != null) {
					try {
						fThreadDeath.disableCollection(); // This object is going to be used for the lifetime of the VM. 
						// If we successfully created our ThreadDeath instance, then
						// remove the "*" ClassPrepareRequest since it's no longer needed
						if (fUniversalClassPrepareReq != null) {
							getEventRequestManager().deleteEventRequest(fUniversalClassPrepareReq);
							fClassPrepareRequestsByClass.remove("*");
						}
					} catch (RuntimeException e) {
						fThreadDeath= null;
						internalError(e);
					}
				}
			}
		}
	}

	/**
	 * Defers the given breakpoint.
	 *
	 * @param breakpoint the breakpoint to defer
	 * @param typeName name of the type the given breakpoint is associated with
	 */
	protected void defer(IJavaBreakpoint breakpoint, String typeName) {
		List bps= (List) fDeferredBreakpointsByClass.get(typeName);
		if (bps == null) {
			// listen for the load of the type
			/** NOTE: Do not listen for typeName + "$*" since JDI
			    will given $* classes without asking */
			listenForClassLoad(typeName);
			bps= new ArrayList(1);
			fDeferredBreakpointsByClass.put(typeName, bps);
		}
		bps.add(breakpoint);
	}

	/**
	 * Returns a location for the line number in the given type, or any of its
	 * nested types. Returns <code>null</code> if a location cannot be determined.
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
			//possible in a nested type, fall through and traverse nested types
		} catch (VMDisconnectedException e) {
			return null;
		} catch (ClassNotPreparedException e) {
			// could be a nested type that is not yet loaded
			return null;
		} catch (RuntimeException e) {
			// not able to retrieve line info
			internalError(e);
			return null;
		}
		
		if (locations != null && locations.size() > 0) {
			return (Location) locations.get(0);
		} else {
			Iterator nestedTypes= null;
			try {
				nestedTypes= type.nestedTypes().iterator();
			} catch (RuntimeException e) {
				// not able to retrieve line info
				internalError(e);
				return null;
			}
			while (nestedTypes.hasNext()) {
				ReferenceType nestedType= (ReferenceType) nestedTypes.next();
				Location innerLocation= determineLocation(lineNumber, nestedType);
				if (innerLocation != null) {
					return innerLocation;
				}
			}
		}

		return null;
	}

	/**
	 * @see IDisconnect
	 */
	public void disconnect() throws DebugException {

		if (isTerminated() || isDisconnected()) {
			// already done
			return;
		}

		if (!canDisconnect()) {
			notSupported(ERROR_DISCONNECT_NOT_SUPPORTED);
		}

		try {
			fVirtualMachine.dispose();
		} catch (VMDisconnectedException e) {
			terminate0();
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_DISCONNECT, e);
		}

	}

	/**
	 * Notifies this target that the specified types have been changed and
	 * should be replaced. A fully qualified name of each type must
	 * be supplied.
	 *
	 * Breakpoints and caught exceptions which are reinstalled.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void typesHaveChanged(String[] typeNames) throws DebugException {
		try {
			fInHCR = true;
			
			if (supportsHotCodeReplace()) {
				org.eclipse.jdi.hcr.VirtualMachine vm= (org.eclipse.jdi.hcr.VirtualMachine) fVirtualMachine;
				int result= org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE;
				try {
					result= vm.classesHaveChanged(typeNames);
				} catch (RuntimeException e) {
					targetRequestFailed(ERROR_HCR, e);
				}
				switch (result) {
					case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_SUCCESS:
						reinstallTriggers();
						break;
					case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_IGNORED:
						reinstallTriggers();
						targetRequestFailed(ERROR_HCR_IGNORED, null);
						break;
					case org.eclipse.jdi.hcr.VirtualMachine.RELOAD_FAILURE:
						targetRequestFailed(ERROR_HCR_FAILED, null);
						break;
				}
			} else {
				notSupported(ERROR_HCR_NOT_SUPPORTED);
			}
		} finally {
			fInHCR = false;
		}
		
	}

	/**
	 * Finds and returns the JDI thread for the associated thread reference, 
	 * or <code>null</code> if not found.
	 */
	protected JDIThread findThread(ThreadReference tr) {
		List threads= null;
		try { 
			threads = getChildren0();
		} catch (DebugException e) {
			internalError(e);
			return null;
		}
		for (int i= 0; i < threads.size(); i++) {
			JDIThread t= (JDIThread) threads.get(i);
			if (t.getUnderlyingThread().equals(tr))
				return t;
		}
		return null;
	}

	/**
	 * @see IDebugElement
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			try {
				fName = fVirtualMachine.name();
			} catch (VMDisconnectedException e) {
				return getUnknownMessage();
			} catch (RuntimeException e) {
				targetRequestFailed(ERROR_GET_NAME, e);
			}
		}
		return fName;
	}
	
	/**
	 * @see IDebugTarget
	 */
	public IProcess getProcess() {
		return fProcess;
	}

	/**
	 * Returns the thread death instance used to terminate threads,
	 * possibly <code>null</code>.
	 */
	public ObjectReference getThreadDeathInstance() {
		return fThreadDeath;
	}

	/**
	 * A class has been loaded.  
	 * Attempt to install any applicable deferred breakpoints.
	 * Attempt to create a ThreadDeath instance if required.
	 */
	protected void handleClassLoad(ClassPrepareEvent event) {
		installDeferredBreakpoints(event);
		ThreadReference threadRef= event.thread();
		createThreadDeathInstance(threadRef);
		resume(threadRef);
	}

	/**
	 * Resumes the given thread
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
	 * Handles a thread death event.
	 */
	protected void handleThreadDeath(ThreadDeathEvent event) {
		JDIThread thread= findThread(event.thread());
		if (thread != null) {
			fChildren.remove(thread);
			thread.terminated();
		}
	}

	/**
	 * Handles a thread start event.
	 */
	protected void handleThreadStart(ThreadStartEvent event) {
		ThreadReference thread= event.thread();
		JDIThread jdiThread= findThread(thread);
		if (jdiThread == null) {
			jdiThread = createChild(thread);
		}
		jdiThread.setRunning(true);
	}

	/**
	 * Handles a VM death event.
	 */
	protected void handleVMDeath(VMDeathEvent event) {
		terminate0();
	}

	/**
	 * Handles a VM disconnect event.
	 */
	protected void handleVMDisconnect(VMDisconnectEvent event) {
		terminate0();
	}
	
	/**
	 * Returns whether this target is in hot code replace
	 */
	public boolean inHCR() {
		return fInHCR;
	}	
	
	/**
	 * @see ISuspendResume
	 */
	public boolean isSuspended() {
		return false;
	}

	/**
	 * @see ITerminate
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * @see IDisconnect
	 */
	public boolean isDisconnected() {
		return fTerminated;
	}
	
	/**
	 * Creates, enables and returns a class prepare request for the
	 * specified class name, or <code>null</code> if unable to
	 * create the request. Caches the request in the class prepare
	 * request table.
	 */
	private ClassPrepareRequest listenForClassLoad(String className) {
		EventRequestManager manager= getEventRequestManager();
		ClassPrepareRequest req= null;
		try {
			req= manager.createClassPrepareRequest();
			req.addClassFilter(className);
			req.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			req.enable();
		} catch (VMDisconnectedException e) {
			return null;
		} catch (RuntimeException e) {
			internalError(e);
			return null;
		}
		fClassPrepareRequestsByClass.put(className, req);
		return req;
	}

	/**
	 * @see ISuspendResume
	 */
	public void resume() throws DebugException {
		notSupported(ERROR_RESUME_NOT_SUPPORTED);
	}

	/**
	 * Installs or defers the given breakpoint
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		if (breakpoint instanceof JavaBreakpoint) {
			try {
				((JavaBreakpoint)breakpoint).addToTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}

	/**
	 * @see IBreakpointSupport
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
	 * @see IBreakpointSupport
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (isTerminated() || isDisconnected()) {
			return;
		}		
		if (breakpoint instanceof JavaBreakpoint) {		
			try {
				((JavaBreakpoint)breakpoint).removeFromTarget(this);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}
	
	/**
	 * Add a breakpoint to the installed breakpoints collection
	 */
	public void installBreakpoint(IJavaBreakpoint breakpoint, Object request) {
		fInstalledBreakpoints.put(breakpoint, request);
	}	
	
	/**
	 * Remove a breakpoint from the installed breakpoints collection
	 */
	public Object uninstallBreakpoint(JavaBreakpoint breakpoint) {
		return fInstalledBreakpoints.remove(breakpoint);		
	}
	
	/**
	 * Return the request object associated with the given breakpoint
	 */
	public Object getRequest(JavaBreakpoint breakpoint) {
		return fInstalledBreakpoints.get(breakpoint);
	}
	
	/**
	 * Return the deferred breakpoints
	 */
	public List getDeferredBreakpointsByClass(String name) {
		return (List)fDeferredBreakpointsByClass.get(name);
	}
	
	/**
	 * Remove a deferred breakpoint
	 */
	public void removeDeferredBreakpointByClass(String name) {
		fDeferredBreakpointsByClass.remove(name);
	}	

	/**
	 * Retrieve the access watchpoint request on the given field from
	 * the event request manager and return it. If no such request
	 * exists, return null
	 */
	protected AccessWatchpointRequest getAccessWatchpointRequest(Field field) {
		Iterator requests= getEventRequestManager().accessWatchpointRequests().iterator();
		while (requests.hasNext()) {
			AccessWatchpointRequest existingRequest= (AccessWatchpointRequest)requests.next();
			if (existingRequest.field() == field) {
				return existingRequest;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve the modification watchpoint request on the given field from
	 * the event request manager and return it. If no such request
	 * exists, return null
	 */
	protected ModificationWatchpointRequest getModificationWatchpointRequest(Field field) {
		Iterator requests= getEventRequestManager().modificationWatchpointRequests().iterator();
		while (requests.hasNext()) {
			ModificationWatchpointRequest existingRequest= (ModificationWatchpointRequest)requests.next();
			if (existingRequest.field() == field) {
				return existingRequest;
			}
		}
		return null;
	}
	
	protected MethodEntryRequest getMethodEntryRequest(String className) {
		Iterator requests= getEventRequestManager().methodEntryRequests().iterator();
		while (requests.hasNext()) {
			MethodEntryRequest existingRequest= (MethodEntryRequest)requests.next();
			if (className.equals(existingRequest.getProperty(CLASS_NAME))) {
				return existingRequest;
			}
		}
		return null;
	}

	/**
	 * @see IBreakpointSupport
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return !isTerminated() && !isDisconnected() && JDIDebugModel.getPluginIdentifier().equals(breakpoint.getModelIdentifier());
	}

	/**
	 * @see ISuspendResume
	 *
	 * Not supported
	 */
	public void suspend() throws DebugException {
		notSupported(ERROR_SUSPEND_NOT_SUPPORTED);
	}

	/**
	 * @see ITerminate
	 */
	public void terminate() throws DebugException {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		if (!canTerminate()) {
			notSupported(ERROR_TERMINATE_NOT_SUPPORTED);
		}
		try {
			fVirtualMachine.exit(1);
		} catch (VMDisconnectedException e) {
			terminate0();
		} catch (RuntimeException e) {
			targetRequestFailed(ERROR_TERMINATE, e);
		}
	}

	/**
	 * Does the cleanup on termination.
	 */
	protected void terminate0() {
		if (!fTerminated) {
			fTerminated= true;
			removeAllChildren();
			getBreakpointManager().removeBreakpointListener(this);
			uninstallAllBreakpoints();
			cleanupTempFiles();
			if (fEvaluationContexts != null) {
				fEvaluationContexts.clear();
			}
			fireTerminateEvent();
		}
	}

	/**
	 * Removes all of the children from this element.
	 */
	public void removeAllChildren() {
		if  (fChildren == null) {
			return;
		}
		Iterator itr= fChildren.iterator();
		fChildren= null;
		while (itr.hasNext()) {
			JDIThread child= (JDIThread) itr.next();
			child.terminated();
		}
	}
		
	/**
	 * Attempts to install a deferred breakpoint
	 * into the newly loaded class.
	 */
	private void installDeferredBreakpoints(ClassPrepareEvent event) {
		String className= jdiGetTypeName(event.referenceType());
		String topLevelName= className;
		int index= className.indexOf('$');
		if (index >= 0) {
			//inner class load...resolve the top level type name
			topLevelName= className.substring(0, index);
		}
		ArrayList markers= (ArrayList) fDeferredBreakpointsByClass.remove(topLevelName);
		if (markers != null) {
			//no longer need to listen for this class load
			ClassPrepareRequest request= (ClassPrepareRequest) fClassPrepareRequestsByClass.remove(topLevelName);
			getEventRequestManager().deleteEventRequest(request);
			Iterator itr= ((ArrayList) markers.clone()).iterator();
			while (itr.hasNext()) {
				JavaBreakpoint breakpoint= (JavaBreakpoint) itr.next();
				breakpointAdded(breakpoint);
			}			
		}
	}

	/**
	 * Sets all the breakpoints to be uninstalled.
	 */
	protected void uninstallAllBreakpoints() {
		Iterator breakpoints= ((Map)((HashMap)fInstalledBreakpoints).clone()).keySet().iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint= (JavaBreakpoint) breakpoints.next();
			try {
				breakpoint.decrementInstallCount();				
			} catch (CoreException e) {
				internalError(e);
			}
		}
		fInstalledBreakpoints.clear();
	}

	/**
	 * Reinstalls the caught exceptions and previously installed breakpoints
	 * after a hot code replace
	 */
	protected void reinstallTriggers() throws DebugException {
		if (!fInstalledBreakpoints.isEmpty()) {
			Iterator itr= ((Map)((HashMap)fInstalledBreakpoints).clone()).keySet().iterator();
			while (itr.hasNext()) {
				// do not notify the breakpoint manager of uninstall, as we
				// are in a resource change callback and cannot modify the resource tree
				JavaBreakpoint breakpoint= (JavaBreakpoint) itr.next();
				if (breakpoint instanceof JavaWatchpoint) {
					Object[] requests= (Object[])fInstalledBreakpoints.remove(breakpoint);
					for (int i=0; i<requests.length; i++) {
						EventRequest req = (EventRequest)requests[i];
						if (req != null) {
							getEventRequestManager().deleteEventRequest(req);
						}
					}
				} else {
					EventRequest req = (EventRequest)fInstalledBreakpoints.remove(breakpoint);
					getEventRequestManager().deleteEventRequest(req);
				}
				breakpointAdded(breakpoint);
			}
		}
	}

	/**
	 * Adds this child to the collection of children for this element.
	 */
	public void addChild(IDebugElement child) {
		boolean added= false;
		if (fChildren == null) {
			fChildren= new ArrayList();
		} 
		if (!fChildren.contains(child)) {
			fChildren.add(child);
			added= true;
		}

		if (added) {
			((JDIDebugElement) child).fireCreationEvent();
		}
	}

	/**
	 * Returns the name of the reference type, handling any JDI exceptions.
	 *
	 * @see com.sun.jdi.ReferenceType
	 */
	protected String jdiGetTypeName(ReferenceType type) {
		try {
			return type.name();
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
		return getUnknownMessage();
	}

	/**
	 * Returns VirtualMachine.classesByName(String), handling any JDI exceptions.
	 *
	 * @see com.sun.jdi.VirtualMachine
	 */
	protected List jdiClassesByName(String className) {
		try {
			return fVirtualMachine.classesByName(className);
		} catch (VMDisconnectedException e) {
		} catch (RuntimeException e) {
			internalError(e);
		}
		return null;
	}

	/**
	 * @see IJavaDebugTarget
	 */
	public IVariable findVariable(String varName) throws DebugException {
		IDebugElement[] threads = getChildren();
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
	 * @see IDebugElement
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}
	
	/**
	 * Returns the Java debug target adapter for this debug target
	 * 
	 * @see IAdaptable
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
	 * associated Java Project.
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
						String name = compoundName[compoundName.length - 1] + ".class";
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
	 * Deletes deployed temporary class files
	 */
	public void cleanupTempFiles() {
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
				internalError(e);
			}
			fTempFiles.clear();
		}
	}

	/**
	 * Returns an evaluation context for the given Java project, creating
	 * one if not yet created.
	 */
	public IEvaluationContext getEvaluationContext(IJavaProject project) {
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
	 * Returns whether the given request is expired
	 */
	protected boolean isExpired(EventRequest request) {
		Boolean requestExpired= (Boolean) request.getProperty(IJavaDebugConstants.EXPIRED);
		if (requestExpired == null) {
				return false;
		}
		return requestExpired.booleanValue();
	}
	
	/**
	 * The JDIDebugPlugin is shutting down.
	 * Shutdown the event dispatcher.
	 */
	protected void shutdown() {
		fEventDispatcher.shutdown();
		cleanupTempFiles();
	}
	
	public VirtualMachine getVM() {
		return fVirtualMachine;
	}
	
	/**
	 * Returns the CRC-32 of the entire class file contents associated with
	 * given type, on the target VM, or <code>null</code> if the type is
	 * not loaded, or a CRC for the type is not known.
	 * 
	 * @param typeName fully qualified name of the type for which a
	 *    CRC is required. For example, "com.example.Example".
	 * @return 32 bit CRC
	 * @exception DebugException if an exception occurs retrieving the CRC from the target
	 *     or if CRC's are not supported
	 */
	public Integer getCRC(String typeName) throws DebugException {
		if (getVM() instanceof org.eclipse.jdi.hcr.VirtualMachine) {
			List classes = jdiClassesByName(typeName);
			if (classes != null && !classes.isEmpty()) {
				ReferenceType type = (ReferenceType)classes.get(0);
				if (type instanceof org.eclipse.jdi.hcr.ReferenceType) {
					try {
						org.eclipse.jdi.hcr.ReferenceType rt = (org.eclipse.jdi.hcr.ReferenceType)type;
						if (rt.isVersionKnown()) {
							return new Integer(rt.getClassFileVersion());
						}
					} catch (VMDisconnectedException e) {
					} catch (RuntimeException e) {
						targetRequestFailed(ERROR_GET_CRC, e);
					}
				}
			}
		}
		return null;
	}
}

