package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaDebugConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.*;

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
	 * Key: breakpoint (<code>IMarker</code>)
	 * Value: the event request associated with the breakpoint (one
	 * of <code>BreakpointRequest</code> or <code>MethodEntryRequest</code>).
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
		IMarker[] bps = getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
		for (int i = 0; i < bps.length; i++) {
			breakpointAdded(bps[i]);
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
	protected void defer(IMarker breakpoint, String typeName) {
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
	 * Handles a method entry event. If this method entry event is
	 * in a method that a method entry breakpoint has been set for,
	 * handle the event as a breakpoint.
	 */
	protected void handleMethodEntry(MethodEntryEvent event) {
		Method enteredMethod = event.method();
		MethodEntryRequest request = (MethodEntryRequest)event.request();
		List breakpoints = (List)request.getProperty(IDebugConstants.BREAKPOINT_MARKER);
		Iterator requestBreakpoints= breakpoints.iterator();
		IMarker breakpoint= null;
		int index= 0;
		while (requestBreakpoints.hasNext()) {
			IMarker aBreakpoint= (IMarker)requestBreakpoints.next();
			Object[] nameSignature= getMethodEntryBreakpointInfo(request, aBreakpoint, index);
			String enteredMethodName= enteredMethod.name();
			if (nameSignature != null && nameSignature[0].equals(enteredMethodName) &&
				nameSignature[1].equals(enteredMethod.signature())) {
				breakpoint= aBreakpoint;
				break;
			}
			index++;	
		}
		if (breakpoint == null) {
			handleMethodEntryResume(event.thread());
			return;
		}	
		
		List counts = (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		Integer count= (Integer)counts.get(index);
		if (count != null) {
			handleHitCountMethodEntryBreakpoint(event, breakpoint, counts, count, index);
		} else {
			// no hit count - suspend
			handleMethodEntryBreakpoint(event.thread(), breakpoint);
		}
	}
	
	protected void handleMethodEntryResume(ThreadReference thread) {
		if (!hasPendingEvents()) {
			resume(thread);
		}
	}
	
	protected void handleHitCountMethodEntryBreakpoint(MethodEntryEvent event, IMarker breakpoint, List counts, Integer count, int index) {
	// decrement count and suspend if 0
		int hitCount = count.intValue();
		if (hitCount > 0) {
			hitCount--;
			count = new Integer(hitCount);
			counts.set(index, count);
			if (hitCount == 0) {
				// the count has reached 0, breakpoint hit
				handleMethodEntryBreakpoint(event.thread(), breakpoint);
				try {
					// make a note that we auto-disabled the breakpoint
					// order is important here...see methodEntryChanged
					DebugJavaUtils.setExpired(breakpoint, true);
					getBreakpointManager().setEnabled(breakpoint, false);
				} catch (CoreException e) {
					internalError(e);
				}
			}  else {
				// count still > 0, keep running
				handleMethodEntryResume(event.thread());		
			}
		} else {
			// hit count expired, keep running
			handleMethodEntryResume(event.thread());
		}
	}
	protected String[] getMethodEntryBreakpointInfo(MethodEntryRequest request, IMarker breakpoint, int index) {
		List nameSignatures = (List)request.getProperty(BREAKPOINT_INFO);
		if (nameSignatures.get(index) != null) {
			return (String[])nameSignatures.get(index);
		}
		String[] nameSignature= new String[2];
		IMethod aMethod= DebugJavaUtils.getMethod(breakpoint); 
			try {
				if (aMethod.isConstructor()) {
					nameSignature[0]= "<init>";
				} else {
					 nameSignature[0]= aMethod.getElementName();
				}
				nameSignature[1]= aMethod.getSignature();
				nameSignatures.add(index, nameSignature);
				return nameSignature;
			} catch (JavaModelException e) {
				logError(e);
				return null;
			}
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
	 
	protected void handleMethodEntryBreakpoint(ThreadReference thread, IMarker breakpoint) {
		JDIThread jdiThread = findThread(thread);
		jdiThread.handleSuspendMethodEntry(breakpoint);
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
	 * Returns the top-level type name associated with the type 
	 * the given breakpoint is associated with, or <code>null</code>.
	 */
	protected String getTopLevelTypeName(IMarker breakpoint) {
		IType type = DebugJavaUtils.getType(breakpoint);
		if (type != null) {
			while (type.getDeclaringType() != null) {
				type = type.getDeclaringType();
			}
			return type.getFullyQualifiedName();
		}
		return null;
	}
	
	/**
	 * Installs or defers the given breakpoint
	 */
	public void breakpointAdded(IMarker breakpoint) {
		if (DebugJavaUtils.isExceptionBreakpoint(breakpoint)) {
			exceptionBreakpointAdded(breakpoint);
		} else if (DebugJavaUtils.isMethodEntryBreakpoint(breakpoint)) {
			methodEntryBreakpointAdded(breakpoint);
		} else {
			lineBreakpointAdded(breakpoint);
		}
	}
	
	protected void lineBreakpointAdded(IMarker breakpoint) {
		String topLevelName= getTopLevelTypeName(breakpoint);
		if (topLevelName == null) {
			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		
		// look for the top-level class - if it is loaded, inner classes may also be loaded
		List classes= jdiClassesByName(topLevelName);
		if (classes == null || classes.isEmpty()) {
			// defer
			defer(breakpoint, topLevelName);
		} else {
			// try to install
			ReferenceType type= (ReferenceType) classes.get(0);
			if (!installLineBreakpoint(breakpoint, type)) {
				// install did not succeed - could be an inner type not yet loaded
				defer(breakpoint, topLevelName);
			}
		}
	}

	/**
	 * Installs a line breakpoint in the given type, returning whether successful.
	 */
	protected boolean installLineBreakpoint(IMarker marker, ReferenceType type) {
		Location location= null;
		IBreakpointManager manager= getBreakpointManager();
		int lineNumber= manager.getLineNumber(marker);			
		location= determineLocation(lineNumber, type);
		if (location == null) {
			// could be an inner type not yet loaded, or line information not available
			return false;
		}
		
		if (createLineBreakpointRequest(location, marker) != null) {
			// update the install attibute on the breakpoint
			if (!fInHCR) {
				try {
					DebugJavaUtils.incrementInstallCount(marker);
				} catch (CoreException e) {
					internalError(e);
				}
			}
			return true;
		} else {
			return false;
		}
		
	}
	
	/**
	 * Creates, installs, and returns a line breakpoint request at
	 * the given location for the given breakpoint.
	 */
	protected BreakpointRequest createLineBreakpointRequest(Location location, IMarker breakpoint) {
		BreakpointRequest request = null;
		try {
			request= getEventRequestManager().createBreakpointRequest(location);
			request.putProperty(IDebugConstants.BREAKPOINT_MARKER, breakpoint);
			fInstalledBreakpoints.put(breakpoint, request);
			int hitCount= DebugJavaUtils.getHitCount(breakpoint);
			if (hitCount > 0) {
				request.addCountFilter(hitCount);
				request.putProperty(IJavaDebugConstants.HIT_COUNT, new Integer(hitCount));
				request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.FALSE);
			} 
			request.setEnabled(getBreakpointManager().isEnabled(breakpoint));
			request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
		} catch (VMDisconnectedException e) {
			fInstalledBreakpoints.remove(breakpoint);
			return null;
		} catch (RuntimeException e) {
			fInstalledBreakpoints.remove(breakpoint);
			internalError(e);
			return null;
		}
		return request;
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
	 * @see IBreakpointSupport
	 */
	public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta) {
		if (DebugJavaUtils.isExceptionBreakpoint(breakpoint)) {
			exceptionBreakpointChanged(breakpoint);
		} else if (DebugJavaUtils.isMethodEntryBreakpoint(breakpoint)) {
			methodEntryBreakpointChanged(breakpoint, delta);
		} else {
			lineBreakpointChanged(breakpoint);
		}
	}
	
	protected void lineBreakpointChanged(IMarker breakpoint) {
		BreakpointRequest request= (BreakpointRequest) fInstalledBreakpoints.get(breakpoint);
		if (request != null) {
			// already installed - could be a change in the enabled state or hit count
			//may result in a new request being generated
			request= updateHitCount(request, breakpoint);
			if (request != null) {
				updateEnabledState(request, breakpoint);
			}
			return;
		}

	}

	protected void updateMethodEntryEnabledState(MethodEntryRequest request)  {
		IBreakpointManager manager= getBreakpointManager();
		Iterator breakpoints= ((List)request.getProperty(IDebugConstants.BREAKPOINT_MARKER)).iterator();
		boolean requestEnabled= false;
		while (breakpoints.hasNext()) {
			IMarker breakpoint= (IMarker)breakpoints.next();
			if (manager.isEnabled(breakpoint)) {
				requestEnabled = true;
				break;
			}
		}
		updateEnabledState0(request, requestEnabled);
	}
	
	protected void updateEnabledState(EventRequest request, IMarker breakpoint)  {
		updateEnabledState0(request, getBreakpointManager().isEnabled(breakpoint));
		
	}
	
	private void updateEnabledState0(EventRequest request, boolean enabled) {
		if (request.isEnabled() != enabled) {
			// change the enabled state
			try {
				// if the request has expired, and is not a method entry request, do not disable.
				// BreakpointRequests that have expired cannot be deleted. However method entry 
				// requests that are expired can be deleted (since we simulate the hit count)
				if (request instanceof MethodEntryRequest || !isExpired(request)) {
					request.setEnabled(enabled);
				}
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				internalError(e);
			}
		}
	}

	protected BreakpointRequest updateHitCount(BreakpointRequest request, IMarker marker) {
	
		int hitCount= DebugJavaUtils.getHitCount(marker);
		Integer requestCount= (Integer) request.getProperty(IJavaDebugConstants.HIT_COUNT);
		int oldCount = -1;
		if (requestCount != null)  {
			oldCount = requestCount.intValue();
		} 
		
		// if the hit count has changed, or the request has expired and is being re-enabled,
		// create a new request
		if (hitCount != oldCount || (isExpired(request) && getBreakpointManager().isEnabled(marker))) {
			try {
				// delete old request
				//on JDK you cannot delete (disable) an event request that has hit its count filter
				if (!isExpired(request)) {
					getEventRequestManager().deleteEventRequest(request); // disable & remove
				}
				Location location = request.location();
				request = createLineBreakpointRequest(location, marker);
			} catch (VMDisconnectedException e) {
			} catch (RuntimeException e) {
				internalError(e);
			}
		}
		return request;
	}
	/**
	 * An exception breakpoint has been added.
	 */
	protected void exceptionBreakpointAdded(IMarker exceptionBreakpoint) {
		exceptionBreakpointChanged(exceptionBreakpoint);
	}

	/**
	 * An exception breakpoint has changed
	 */
	protected void exceptionBreakpointChanged(IMarker exceptionBreakpoint) {

		boolean caught= DebugJavaUtils.isCaught(exceptionBreakpoint);
		boolean uncaught= DebugJavaUtils.isUncaught(exceptionBreakpoint);

		if (caught || uncaught) {
			IType exceptionType = DebugJavaUtils.getType(exceptionBreakpoint);
			if (exceptionType == null) {
				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
			String exceptionName = exceptionType.getFullyQualifiedName();
			String topLevelName = getTopLevelTypeName(exceptionBreakpoint);
			if (topLevelName == null) {
				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
			List classes= jdiClassesByName(exceptionName);
			ReferenceType exClass= null;
			if (classes != null && !classes.isEmpty()) {
				exClass= (ReferenceType) classes.get(0);
			}
			if (exClass == null) {
				// defer the exception
				defer(exceptionBreakpoint, topLevelName);
			} else {
				// new or changed - first delete the old request
				if (null != fInstalledBreakpoints.get(exceptionBreakpoint))
					exceptionBreakpointRemoved(exceptionBreakpoint);
				ExceptionRequest request= null;
				try {
					request= getEventRequestManager().createExceptionRequest(exClass, caught, uncaught);
					request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					request.setEnabled(getBreakpointManager().isEnabled(exceptionBreakpoint));
				} catch (VMDisconnectedException e) {
					return;
				} catch (RuntimeException e) {
					internalError(e);
					return;
				}
				request.putProperty(IDebugConstants.BREAKPOINT_MARKER, exceptionBreakpoint);
				fInstalledBreakpoints.put(exceptionBreakpoint, request);
			}
		} else {
			exceptionBreakpointRemoved(exceptionBreakpoint);
		}
	}

	/**
	 * An exception breakpoint has been removed
	 */
	protected void exceptionBreakpointRemoved(IMarker exceptionBreakpoint) {
		IType type = DebugJavaUtils.getType(exceptionBreakpoint);
		if (type == null) {
			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		String name = type.getFullyQualifiedName();
		ExceptionRequest request= (ExceptionRequest) fInstalledBreakpoints.remove(exceptionBreakpoint);
		if (request != null) {
			try {
				getEventRequestManager().deleteEventRequest(request);
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				internalError(e);
				return;
			}
		}
		List deferred = (List)fDeferredBreakpointsByClass.get(name);
		if (deferred != null)  {
			deferred.remove(exceptionBreakpoint);
			if (deferred.isEmpty()) {
				fDeferredBreakpointsByClass.remove(name);
			}
		}
	}
	
	/**
	 * A method entry breakpoint has been added.
     * Create or update the request.
	 */
	protected void methodEntryBreakpointAdded(IMarker breakpoint) {
		IType type = DebugJavaUtils.getType(breakpoint);
		if (type == null) {
			internalError(ERROR_BREAKPOINT_NO_TYPE);
			return;
		}
		String className = type.getFullyQualifiedName();
		
		MethodEntryRequest request = getMethodEntryRequest(className);
		
		if (request == null) {
			try {
				request= getEventRequestManager().createMethodEntryRequest();
				request.addClassFilter(className);
				request.putProperty(CLASS_NAME, className);
				request.putProperty(BREAKPOINT_INFO, new ArrayList(1));
				request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
				request.setEnabled(getBreakpointManager().isEnabled(breakpoint));
				request.putProperty(IDebugConstants.BREAKPOINT_MARKER, new ArrayList(3));
				request.putProperty(IJavaDebugConstants.HIT_COUNT, new ArrayList(3));
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				internalError(e);
				return;
			}
		} else {
			//request may be disabled
			boolean enabled= getBreakpointManager().isEnabled(breakpoint);
			if (enabled && !request.isEnabled()) {
				request.setEnabled(true);
			}
		}
		
		List breakpointInfo= (List)request.getProperty(BREAKPOINT_INFO);
		breakpointInfo.add(null);
		
		List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT_MARKER);
		breakpoints.add(breakpoint);
		
		
		List hitCounts = (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		int hitCount = DebugJavaUtils.getHitCount(breakpoint);
		if (hitCount > 0) {
			hitCounts.add(new Integer(hitCount));
		} else {
			hitCounts.add(null);
		}
		try {		
			DebugJavaUtils.incrementInstallCount(breakpoint);
		} catch (CoreException e) {
			internalError(e);
		}
		fInstalledBreakpoints.put(breakpoint, request);
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
	 * A method entry breakpoint has been changed.
	 * Update the request.
	 */
	protected void methodEntryBreakpointChanged(IMarker breakpoint, IMarkerDelta delta) {
		MethodEntryRequest request = (MethodEntryRequest)fInstalledBreakpoints.get(breakpoint);
		if (request == null) {
			return;
		}
		// check the enabled state
		updateMethodEntryEnabledState(request);
		
		List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT_MARKER);
		int index= breakpoints.indexOf(breakpoint);
		// update the breakpoints hit count
		int newCount = DebugJavaUtils.getHitCount(breakpoint);
		List hitCounts= (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
		if (newCount > 0) {
			hitCounts.set(index, new Integer(newCount));
		} else {
			//back to a regular breakpoint
			hitCounts.set(index, null);			
		}
	}
	
	/**
	 * A method entry breakpoint has been removed.
	 * Update the request.
	 */
	protected void methodEntryBreakpointRemoved(IMarker breakpoint) {
		MethodEntryRequest request = (MethodEntryRequest)fInstalledBreakpoints.remove(breakpoint);
		if (request != null) {
			try {
				DebugJavaUtils.decrementInstallCount(breakpoint);
			} catch (CoreException e) {
				internalError(e);
			}
			List breakpoints= (List)request.getProperty(IDebugConstants.BREAKPOINT_MARKER);
			int index = breakpoints.indexOf(breakpoint);
			breakpoints.remove(index);
			if (breakpoints.isEmpty()) {
				try {
					getEventRequestManager().deleteEventRequest(request); // disable & remove
				} catch (VMDisconnectedException e) {
				} catch (RuntimeException e) {
					internalError(e);
				}
			} else {
				List hitCounts= (List)request.getProperty(IJavaDebugConstants.HIT_COUNT);
				hitCounts.remove(index);
			}
		}
	}

	/**
	 * @see IBreakpointSupport
	 */
	public boolean supportsBreakpoint(IMarker breakpoint) {
		return !isTerminated() && !isDisconnected() && JDIDebugModel.getPluginIdentifier().equals(getBreakpointManager().getModelIdentifier(breakpoint));
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
				IMarker marker= (IMarker) itr.next();
				breakpointAdded(marker);
			}			
		}
	}

	/**
	 * Sets all the breakpoints to be uninstalled.
	 */
	protected void uninstallAllBreakpoints() {
		Iterator markers= ((Map)((HashMap)fInstalledBreakpoints).clone()).keySet().iterator();
		while (markers.hasNext()) {
			IMarker marker= (IMarker) markers.next();
			try {
				DebugJavaUtils.decrementInstallCount(marker);				
			} catch (CoreException e) {
				internalError(e);
			}
		}
		fInstalledBreakpoints.clear();
	}

	/**
	 * @see IBreakpointSupport
	 */
	public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta) {
		if (DebugJavaUtils.isExceptionBreakpoint(breakpoint)) {
			exceptionBreakpointRemoved(breakpoint);
		} else if (DebugJavaUtils.isMethodEntryBreakpoint(breakpoint)) {
			methodEntryBreakpointRemoved(breakpoint);
		} else {
			lineBreakpointRemoved(breakpoint);
		}
	}
	
	protected void lineBreakpointRemoved(IMarker breakpoint) {		
		BreakpointRequest request= (BreakpointRequest) fInstalledBreakpoints.remove(breakpoint);
		if (request == null) {
			//deferred breakpoint
			if (!breakpoint.exists()) {
				//resource no longer exists
				return;
			}
			String name= getTopLevelTypeName(breakpoint);
			if (name == null) {
				internalError(ERROR_BREAKPOINT_NO_TYPE);
				return;
			}
			List markers= (List) fDeferredBreakpointsByClass.get(name);
			if (markers == null) {
				return;
			}

			markers.remove(breakpoint);
			if (markers.isEmpty()) {
				fDeferredBreakpointsByClass.remove(name);
			}
		} else {
			//installed breakpoint
			try {
				// cannot delete an expired request
				if (!isExpired(request)) {
					getEventRequestManager().deleteEventRequest(request); // disable & remove
				}
			} catch (VMDisconnectedException e) {
				return;
			} catch (RuntimeException e) {
				internalError(e);
			}
		}
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
				IMarker marker= (IMarker) itr.next();
				EventRequest req = (EventRequest)fInstalledBreakpoints.remove(marker);
				getEventRequestManager().deleteEventRequest(req);
				breakpointAdded(marker);
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
		Iterator threads = getChildren0().iterator();
		while (threads.hasNext()) {
			JDIThread thread = (JDIThread)threads.next();
			IVariable var = thread.findVariable(varName);
			if (var != null) {
				return var;
			}
		}
		return null;
	}
		
	/**
	 * Called by a JDI thread when a breakpoint is 
	 * encountered.
	 */
	public void expireHitCount(BreakpointEvent event) {
		BreakpointRequest request= (BreakpointRequest)event.request();
		Integer requestCount= (Integer) request.getProperty(IJavaDebugConstants.HIT_COUNT);
		if (requestCount != null) {
			IMarker breakpoint= (IMarker)request.getProperty(IDebugConstants.BREAKPOINT_MARKER);
			if (breakpoint == null) {
				return;
			}
			try {
				request.putProperty(IJavaDebugConstants.EXPIRED, Boolean.TRUE);
				getBreakpointManager().setEnabled(breakpoint, false);
				// make a note that we auto-disabled this breakpoint.
				DebugJavaUtils.setExpired(breakpoint, true);
			} catch (CoreException ce) {
				internalError(ce);
			}
		}
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
	protected Object getAdpater(Class adapter) {
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

