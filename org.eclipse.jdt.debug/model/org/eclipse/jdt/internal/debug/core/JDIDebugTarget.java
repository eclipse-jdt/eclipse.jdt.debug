package org.eclipse.jdt.internal.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

/**
 * Debug target for JDI debug model.
 */

public class JDIDebugTarget extends JDIDebugElement implements IJavaDebugTarget {
	
	private static final int MAX_THREAD_DEATH_ATTEMPTS = 1;
	
	/**
	 * Threads contained in this debug target.
	 */
	protected List fThreads;
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
	 * Table of class prepare requests for deferred breakpoints
	 * <p>
	 * Key: the fully qualified name of the class<br>
	 * Value: a <code>ClassPrepareRequest</code>s
	 */
	protected Map fClassPrepareRequestsByClass;	
	/**
	 * Collection of breakpoints added to this target. Values are of type <code>IJavaBreakpoint</code>.
	 */
	protected List fBreakpoints;
	 
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
		fDebugTarget = this;
		fSupportsTerminate= supportTerminate;
		fSupportsDisconnect= supportDisconnect;
		fVirtualMachine= jvm;
		fVirtualMachine.setDebugTraceMode(VirtualMachine.TRACE_NONE);
		fProcess= process;
		fTerminated= false;
		fName= name;

		fClassPrepareRequestsByClass= new HashMap(5);
		fBreakpoints = new ArrayList(5);
		fThreads = new ArrayList(5);
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
			for (int i= 0; i < fThreads.size(); i++) {
				((JDIThread) fThreads.get(i)).setRunning(true);
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
		new Thread(fEventDispatcher, JDIDebugModel.getPluginIdentifier() + JDIDebugModelMessages.getString("JDIDebugTarget.JDI_Event_Dispatcher")).start(); //$NON-NLS-1$
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
				createThread((ThreadReference) initialThreads.next());
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
		fUniversalClassPrepareReq= listenForClassLoad("*"); //$NON-NLS-1$
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
	
	protected EventRequestManager getEventRequestManager() {
		return fVirtualMachine.eventRequestManager();
	}
	
	/**
	 * Creates, adds and returns a thread for the given underlying thread reference.
	 */
	protected JDIThread createThread(ThreadReference thread) {
		JDIThread jdiThread= new JDIThread(this, thread);
		fThreads.add(jdiThread);
		jdiThread.fireCreationEvent();
		return jdiThread;
	}
	
	/**
	 * @see IDebugTarget
	 */
	public IThread[] getThreads() {
		return (IThread[])fThreads.toArray(new IThread[fThreads.size()]);
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
					fClassPrepareRequestsByClass.remove("*"); //$NON-NLS-1$
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
			List classes= jdiClassesByName("java.lang.ThreadDeath"); //$NON-NLS-1$
			if (classes != null && classes.size() != 0) {
				ClassType threadDeathClass= (ClassType) classes.get(0);
				Method constructor= null;
				try {
					constructor= threadDeathClass.concreteMethodByName("<init>", "()V"); //$NON-NLS-2$ //$NON-NLS-1$
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
							fClassPrepareRequestsByClass.remove("*"); //$NON-NLS-1$
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
	 * @see IDisconnect
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
			fVirtualMachine.dispose();
		} catch (VMDisconnectedException e) {
			terminate0();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_disconnecting"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
			
		if (supportsHotCodeReplace()) {
			org.eclipse.jdi.hcr.VirtualMachine vm= (org.eclipse.jdi.hcr.VirtualMachine) fVirtualMachine;
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
	 */
	protected JDIThread findThread(ThreadReference tr) {
		for (int i= 0; i < fThreads.size(); i++) {
			JDIThread t= (JDIThread) fThreads.get(i);
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
				targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_retrieving_name"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
		ThreadReference threadRef= event.thread();
		createThreadDeathInstance(threadRef);
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
			fThreads.remove(thread);
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
			jdiThread = createThread(thread);
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
	 * Creates, enables and returns a class prepare request for the
	 * specified class name, or <code>null</code> if unable to
	 * create the request. Caches the request in the class prepare
	 * request table.
	 */
	protected ClassPrepareRequest createClassPrepareRequest(String className) {
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
		return req;
	}

	/**
	 * @see ISuspendResume
	 */
	public void resume() throws DebugException {
		notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_resume")); //$NON-NLS-1$
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
				((JavaBreakpoint)breakpoint).addToTarget(JDIDebugTarget.this);
				fBreakpoints.add(breakpoint);
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
				((JavaBreakpoint)breakpoint).changeForTarget(JDIDebugTarget.this);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}
	
	/**
	 * @see IBreakpointSupport
	 */
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {
		if (isTerminated() || isDisconnected()) {
			return;
		}		
		if (breakpoint instanceof JavaBreakpoint) {
			try {
				((JavaBreakpoint)breakpoint).removeFromTarget(JDIDebugTarget.this);
				fBreakpoints.remove(breakpoint);
			} catch (CoreException e) {
				logError(e);
			}
		}
	}
	
	protected void run(IWorkspaceRunnable wr) throws CoreException {
		ResourcesPlugin.getWorkspace().run(wr, null);
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
		notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_suspend")); //$NON-NLS-1$
	}

	/**
	 * @see ITerminate
	 */
	public void terminate() throws DebugException {
		if (isTerminated() || isDisconnected()) {
			return;
		}
		if (!canTerminate()) {
			notSupported(JDIDebugModelMessages.getString("JDIDebugTarget.does_not_support_termination")); //$NON-NLS-1$
		}
		try {
			fVirtualMachine.exit(1);
		} catch (VMDisconnectedException e) {
			terminate0();
		} catch (RuntimeException e) {
			targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_terminating"), new String[] {e.toString()}), e); //$NON-NLS-1$
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
		Iterator itr= fThreads.iterator();
		fThreads= Collections.EMPTY_LIST;
		while (itr.hasNext()) {
			JDIThread child= (JDIThread) itr.next();
			child.terminated();
		}
	}

	/**
	 * Sets all the breakpoints to be uninstalled.
	 */
	protected void uninstallAllBreakpoints() {
		Iterator breakpoints= fBreakpoints.iterator();
		while (breakpoints.hasNext()) {
			JavaBreakpoint breakpoint= (JavaBreakpoint) breakpoints.next();
			try {
				breakpoint.removeFromTarget(this);
			} catch (CoreException e) {
				internalError(e);
			}
		}
		fBreakpoints.clear();
	}

	/**
	 * Adds this child to the collection of children for this element.
	 */
	public void addChild(IDebugElement child) {

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
						targetRequestFailed(MessageFormat.format(JDIDebugModelMessages.getString("JDIDebugTarget.exception_retrieving_version_information"), new String[] {e.toString(), type.name()}), e); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}
	
	protected void addJDIEventListener(IJDIEventListener listener, EventRequest request) {
		fEventDispatcher.addJDIEventListener(listener, request);
	}
	
	protected void removeJDIEventListener(IJDIEventListener listener, EventRequest request) {
		fEventDispatcher.removeJDIEventListener(listener, request);
	}
	
}

