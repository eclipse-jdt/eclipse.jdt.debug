package org.eclipse.jdt.debug.tests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.TestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;


 
/**
 * Tests for launch configurations
 */
public abstract class AbstractDebugTest extends TestCase {
	
	public static final int DEFAULT_TIMEOUT = 30000;
	
	/**
	 * The last relevent event set - for example, that caused
	 * a thread to suspend
	 */
	protected DebugEvent[] fEventSet;
	
	public AbstractDebugTest(String name) {
		super(name);
	}
	
	/**
	 * Sets the last relevant event set
	 *
	 * @param set event set
	 */
	protected void setEventSet(DebugEvent[] set) {
		fEventSet = set;
	}
	
	/**
	 * Returns the last relevant event set
	 * 
	 * @return event set
	 */
	protected DebugEvent[] getEventSet() {
		return fEventSet;
	}
	
	/**
	 * Returns the launch manager
	 * 
	 * @return launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Returns the breakpoint manager
	 * 
	 * @return breakpoint manager
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}	
	
	/**
	 * Returns the 'DebugTests' project.
	 * 
	 * @return the test project
	 */
	protected IJavaProject getJavaProject() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("DebugTests");
		return JavaCore.create(project);
	}
	
	/**
	 * Launches the type with the given name, and waits for a suspend
	 * event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param mainTypeName the program to launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launch(String mainTypeName) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config);
		config.launch(getLaunchManager().DEBUG_MODE, null);

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread)suspendee;
	}
	
	/**
	 * Resumes the given thread, and waits for another suspend event.
	 * Returns the thread in which the suspend event occurrs.
	 * 
	 * @param thread thread to resume
	 * @return thread in which the first suspend event occurrs
	 */
	protected IJavaThread resume(IJavaThread thread) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread)suspendee;
	}	
	
	/**
	 * Resumes the given thread, and waits the associated debug
	 * target to terminate.
	 * 
	 * @param thread thread to resume
	 * @return the terminated debug target
	 */
	protected IJavaDebugTarget resumeAndExit(IJavaThread thread) throws Exception {
		DebugEventWaiter waiter= new DebugElementEventWaiter(DebugEvent.TERMINATE, thread.getDebugTarget());
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not terminate.", suspendee);
		IJavaDebugTarget target = (IJavaDebugTarget)suspendee;
		assertTrue("program should have exited", target.isTerminated() || target.isDisconnected());
		return target;
	}	
		
	/**
	 * Returns the launch configuration for the given main type
	 * 
	 * @param mainTypeName program to launch
	 * @see ProjectCreationDecorator
	 */
	protected ILaunchConfiguration getLaunchConfiguration(String mainTypeName) {
		IFile file = getJavaProject().getProject().getFolder("launchConfigurations").getFile(mainTypeName + ".launch");
		ILaunchConfiguration config = getLaunchManager().getLaunchConfiguration(file);
		assertTrue("Could not find launch configuration for " + mainTypeName, config.exists());
		return config;
	}
	
	/**
	 * Creates and returns a line breakpoint at the given line number in the type with the
	 * given name.
	 * 
	 * @param lineNumber line number
	 * @param typeName type name
	 */
	protected IJavaLineBreakpoint createLineBreakpoint(int lineNumber, String typeName) throws Exception {
		return JDIDebugModel.createLineBreakpoint(getJavaProject().getProject(), typeName, lineNumber, -1, -1, 0, true, null);
	}
	
	/**
	 * Creates and returns a pattern breakpoint at the given line number in the
	 * source file with the given name.
	 * 
	 * @param lineNumber line number
	 * @param sourceName name of source file
	 * @param pattern the pattern of the class file name
	 */
	protected IJavaPatternBreakpoint createPatternBreakpoint(int lineNumber, String sourceName, String pattern) throws Exception {
		return JDIDebugModel.createPatternBreakpoint(getJavaProject().getProject(), sourceName, pattern, lineNumber, -1, -1, 0, true, null);
	}
	
	/**
	 * Creates and returns a target pattern breakpoint at the given line number in the
	 * source file with the given name.
	 * 
	 * @param lineNumber line number
	 * @param sourceName name of source file
	 */
	protected IJavaTargetPatternBreakpoint createTargetPatternBreakpoint(int lineNumber, String sourceName) throws Exception {
		return JDIDebugModel.createTargetPatternBreakpoint(getJavaProject().getProject(), sourceName, lineNumber, -1, -1, 0, true, null);
	}	
		
	/**
	 * Creates and returns a method breakpoint
	 * 
	 * @param typeNamePattern type name pattern
	 * @param methodName method name
	 * @param methodSignature method signature
	 * @param entry whether to break on entry
	 * @param exit whether to break on exit
	 */
	protected IJavaMethodBreakpoint createMethodBreakpoint(String typeNamePattern, String methodName, String methodSignature, boolean entry, boolean exit) throws Exception {
		return JDIDebugModel.createMethodBreakpoint(getJavaProject().getProject(), typeNamePattern, methodName, methodSignature, entry, exit,false, -1, -1, -1, 0, true, null);
	}	
	
	/**
	 * Creates and returns an exception breakpoint
	 * 
	 * @param exName exception name
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
	 */	
	protected IJavaExceptionBreakpoint createExceptionBreakpoint(String exName, boolean caught, boolean uncaught) throws CoreException {
		return JDIDebugModel.createExceptionBreakpoint(getJavaProject().getProject(),exName, caught, uncaught, false, true, null);
	}
	
	/**
	 * Creates and returns a watchpoint
	 * 
	 * @param typeNmae type name
	 * @param fieldName field name
	 * @param access whether to suspend on field access
	 * @param modification whether to suspend on field modification
	 */	
	protected IJavaWatchpoint createWatchpoint(String typeName, String fieldName, boolean access, boolean modification) throws CoreException {
		IJavaWatchpoint wp = JDIDebugModel.createWatchpoint(getJavaProject().getProject(), typeName, fieldName, -1, -1, -1, 0, true, null);
		wp.setAccess(access);
		wp.setModification(modification);
		return wp;
	}
		
	/**
	 * Terminates the given thread and removes its launch
	 */
	protected void terminateAndRemove(IJavaThread thread) {
		ILaunch launch = thread.getLaunch();
		try {
			thread.getDebugTarget().terminate();
		} catch (CoreException e) {
		} finally {
			getLaunchManager().removeLaunch(launch);
		}
	}
	
	/**
	 * Deletes all existing breakpoints
	 */
	protected void removeAllBreakpoints() {
		IBreakpoint[] bps = getBreakpointManager().getBreakpoints();
		for (int i = 0; i < bps.length; i++) {
			try {
				bps[i].delete();
			} catch (CoreException e) {
			}
		}
	}
	
	/**
	 * Returns the first breakpoint the given thread is suspended
	 * at, or <code>null</code> if none.
	 * 
	 * @return the first breakpoint the given thread is suspended
	 * at, or <code>null</code> if none
	 */
	protected IBreakpoint getBreakpoint(IThread thread) {
		IBreakpoint[] bps = thread.getBreakpoints();
		if (bps.length > 0) {
			return bps[0];
		}
		return null;
	}
}

