/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.console.HyperlinkPosition;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventDetailWaiter;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.SafeRunnable;


 
/**
 * Tests for launch configurations
 */
public abstract class AbstractDebugTest extends TestCase implements  IEvaluationListener {
	
	public static final int DEFAULT_TIMEOUT = 30000;
	
	public IEvaluationResult fEvaluationResult;
	
	public static IJavaProject fJavaProject;
	
	/**
	 * The last relevent event set - for example, that caused
	 * a thread to suspend
	 */
	protected DebugEvent[] fEventSet;
	
	public AbstractDebugTest(String name) {
		super(name);
		// set error dialog to non-blocking to avoid hanging the UI during test
		ErrorDialog.AUTOMATED_MODE = true;
		SafeRunnable.setIgnoreErrors(true);
		if (!(this.getClass() == ProjectCreationDecorator.class) && !getJavaProject().exists()) {
			new TestSuite(ProjectCreationDecorator.class).run(new TestResult());
		}
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
	 * Returns the source folder with the given name in the given project.
	 * 
	 * @param project
	 * @param name source folder name
	 * @return package fragment root
	 */
	protected IPackageFragmentRoot getPackageFragmentRoot(IJavaProject project, String name) {
		IProject p = project.getProject();
		return project.getPackageFragmentRoot(p.getFolder(name));
	}
	
	protected IConsoleHyperlink getHyperlink(int offset, IDocument doc) {
		if (offset >= 0 && doc != null) {
			Position[] positions = null;
			try {
				positions = doc.getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
			} catch (BadPositionCategoryException ex) {
				// no links have been added
				return null;
			}
			for (int i = 0; i < positions.length; i++) {
				Position position = positions[i];
				if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
					return ((HyperlinkPosition)position).getHyperLink();
				}
			}
		}
		return null;
	}
	
	/**
	 * Launches the given configuration and waits for an event. Returns the
	 * source of the event. If the event is not received, the launch is
	 * terminated and an exception is thrown.
	 * 
	 * @param configuration the configuration to launch
	 * @param waiter the event waiter to use
	 * @return Object the source of the event
	 * @exception Exception if the event is never received.
	 */
	protected Object launchAndWait(ILaunchConfiguration configuration, DebugEventWaiter waiter) throws CoreException {
		ILaunch launch = configuration.launch(ILaunchManager.DEBUG_MODE, null);
		Object suspendee= waiter.waitForEvent();
		if (suspendee == null) {
			try {
				launch.terminate();
			} catch (CoreException e) {
				e.printStackTrace();
				fail("Program did not suspend, and unable to terminate launch.");
			}
		}
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend, launch terminated.", suspendee);
		return suspendee;		
	}
	
	/**
	 * Launches the type with the given name, and waits for a
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param mainTypeName the program to launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchAndSuspend(String mainTypeName) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config);
		return launchAndSuspend(config);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a 
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param config the configuration to launch
	 * @return thread in which the first suspend event occurred
	 */	
	protected IJavaThread launchAndSuspend(ILaunchConfiguration config) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		Object suspendee = launchAndWait(config, waiter);
		return (IJavaThread)suspendee;		
	}
	
	/**
	 * Launches the type with the given name, and waits for a breakpoint-caused 
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param mainTypeName the program to launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(String mainTypeName) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config);
		return launchToBreakpoint(config);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a breakpoint-caused 
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param config the configuration to launch
	 * @return thread in which the first suspend event occurred
	 */	
	protected IJavaThread launchToBreakpoint(ILaunchConfiguration config) throws CoreException {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);

		Object suspendee= launchAndWait(config, waiter);
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread);
		return (IJavaThread)suspendee;		
	}
	
	/**
	 * Launches the type with the given name, and waits for a terminate
	 * event in that program. Returns the debug target in which the suspend
	 * event occurred.
	 * 
	 * @param mainTypeName the program to launch
	 * @param timeout the number of milliseconds to wait for a terminate event
	 * @return debug target in which the terminate event occurred
	 */
	protected IJavaDebugTarget launchAndTerminate(String mainTypeName) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config);
		return launchAndTerminate(config, DEFAULT_TIMEOUT);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a terminate
	 * event in that program. Returns the debug target in which the terminate
	 * event occurred.
	 * 
	 * @param config the configuration to launch
	 * @param timeout the number of milliseconds to wait for a terminate event
	 * @return thread in which the first suspend event occurred
	 */	
	protected IJavaDebugTarget launchAndTerminate(ILaunchConfiguration config, int timeout) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.TERMINATE, IJavaDebugTarget.class);
		waiter.setTimeout(timeout);

		Object terminatee = launchAndWait(config, waiter);		
		assertNotNull("Program did not terminate.", terminatee);
		assertTrue("terminatee is not an IJavaDebugTarget", terminatee instanceof IJavaDebugTarget);
		IJavaDebugTarget debugTarget = (IJavaDebugTarget) terminatee;
		assertTrue("debug target is not terminated", debugTarget.isTerminated() || debugTarget.isDisconnected());
		return debugTarget;		
	}
	
	/**
	 * Launches the type with the given name, and waits for a line breakpoint suspend
	 * event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param mainTypeName the program to launch
	 * @param bp the breakpoint that should cause a suspend event
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToLineBreakpoint(String mainTypeName, ILineBreakpoint bp) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config);
		return launchToLineBreakpoint(config, bp);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a line breakpoint 
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 * 
	 * @param config the configuration to launch
	 * @param bp the breakpoint that should cause a suspend event
	 * @return thread in which the first suspend event occurred
	 */	
	protected IJavaThread launchToLineBreakpoint(ILaunchConfiguration config, ILineBreakpoint bp) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);

		Object suspendee= launchAndWait(config, waiter);
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread);
		IJavaThread thread = (IJavaThread) suspendee;
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("suspended, but not by breakpoint", hit);
		assertTrue("hit un-registered breakpoint", bp.equals(hit));
		assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
		ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
		int lineNumber = breakpoint.getLineNumber();
		int stackLine = thread.getTopStackFrame().getLineNumber();
		assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
		
		return thread;		
	}
	
	/**
	 * Resumes the given thread, and waits for another breakpoint-caused suspend event.
	 * Returns the thread in which the suspend event occurs.
	 * 
	 * @param thread thread to resume
	 * @return thread in which the first suspend event occurs
	 */
	protected IJavaThread resume(IJavaThread thread) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread)suspendee;
	}	
	
	/**
	 * Resumes the given thread, and waits for a suspend event caused by the specified
	 * line breakpoint.  Returns the thread in which the suspend event occurs.
	 * 
	 * @param thread thread to resume
	 * @return thread in which the first suspend event occurs
	 */
	protected IJavaThread resumeToLineBreakpoint(IJavaThread resumeThread, ILineBreakpoint bp) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		resumeThread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread);
		IJavaThread thread = (IJavaThread) suspendee;
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("suspended, but not by breakpoint", hit);
		assertTrue("hit un-registered breakpoint", bp.equals(hit));
		assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint);
		ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
		int lineNumber = breakpoint.getLineNumber();
		int stackLine = thread.getTopStackFrame().getLineNumber();
		assertTrue("line numbers of breakpoint and stack frame do not match", lineNumber == stackLine);
		
		return (IJavaThread)suspendee;
	}	
	
	/**
	 * Resumes the given thread, and waits for the debug target
	 * to terminate (i.e. finish/exit the program).
	 * 
	 * @param thread thread to resume
	 */
	protected void exit(IJavaThread thread) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.TERMINATE, IProcess.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not terminate.", suspendee);
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
	
	protected IResource getBreakpointResource(String typeName) throws Exception {
		IJavaElement element = getJavaProject().findElement(new Path(typeName + ".java"));
		IResource resource = element.getCorrespondingResource();
		if (resource == null) {
			resource = getJavaProject().getProject();
		}		
		return resource;
	}
	
	/**
	 * Creates and returns a line breakpoint at the given line number in the type with the
	 * given name.
	 * 
	 * @param lineNumber line number
	 * @param typeName type name
	 */
	protected IJavaLineBreakpoint createLineBreakpoint(int lineNumber, String typeName) throws Exception {
		return JDIDebugModel.createLineBreakpoint(getBreakpointResource(typeName), typeName, lineNumber, -1, -1, 0, true, null);
	}
	
	/**
	 * Creates and returns a line breakpoint at the given line number in the type with the
	 * given name and sets the specified condition on the breakpoint.
	 * 
	 * @param lineNumber line number
	 * @param typeName type name
	 * @param condition condition
	 */
	protected IJavaLineBreakpoint createConditionalLineBreakpoint(int lineNumber, String typeName, String condition, boolean suspendOnTrue) throws Exception {
		IJavaLineBreakpoint bp = JDIDebugModel.createLineBreakpoint(getBreakpointResource(typeName), typeName, lineNumber, -1, -1, 0, true, null);
		bp.setCondition(condition);
		bp.setConditionEnabled(true);
		bp.setConditionSuspendOnTrue(suspendOnTrue);
		return bp;
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
	protected IJavaExceptionBreakpoint createExceptionBreakpoint(String exName, boolean caught, boolean uncaught) throws Exception {
		return JDIDebugModel.createExceptionBreakpoint(getBreakpointResource(exName),exName, caught, uncaught, false, true, null);
	}
	
	/**
	 * Creates and returns a watchpoint
	 * 
	 * @param typeNmae type name
	 * @param fieldName field name
	 * @param access whether to suspend on field access
	 * @param modification whether to suspend on field modification
	 */	
	protected IJavaWatchpoint createWatchpoint(String typeName, String fieldName, boolean access, boolean modification) throws Exception {
		IJavaWatchpoint wp = JDIDebugModel.createWatchpoint(getBreakpointResource(typeName), typeName, fieldName, -1, -1, -1, 0, true, null);
		wp.setAccess(access);
		wp.setModification(modification);
		return wp;
	}
		
	/**
	 * Terminates the given thread and removes its launch
	 */
	protected void terminateAndRemove(IJavaThread thread) {
		if (thread != null) {
			terminateAndRemove((IJavaDebugTarget)thread.getDebugTarget());
		}
	}
	
	/**
	 * Terminates the given debug target and removes its launch.
	 * 
	 * NOTE: all breakpoints are removed, all threads are resumed, and then
	 * the target is terminated. This avoids defunct processes on linux.
	 */
	protected void terminateAndRemove(IJavaDebugTarget debugTarget) {
		if (debugTarget != null) {
			ILaunch launch = debugTarget.getLaunch();
			try {
				removeAllBreakpoints();
				IThread[] threads = debugTarget.getThreads();
				for (int i = 0; i < threads.length; i++) {
					IThread thread = threads[i];
					try {
						if (thread.isSuspended()) {
							thread.resume();
						}
					} catch (CoreException e) {
					}
				}
				debugTarget.getDebugTarget().terminate();
			} catch (CoreException e) {
			} finally {
				getLaunchManager().removeLaunch(launch);
			}
		}
	}
	
	/**
	 * Deletes all existing breakpoints
	 */
	protected void removeAllBreakpoints() {
		IBreakpoint[] bps = getBreakpointManager().getBreakpoints();
		try {
			getBreakpointManager().removeBreakpoints(bps, true);
		} catch (CoreException e) {
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
	
	/**
	 * Evaluates the given snippet in the context of the given stack frame and returns
	 * the result.
	 * 
	 * @param snippet code snippet
	 * @param frame stack frame context
	 * @return evaluation result
	 */
	protected IEvaluationResult evaluate(String snippet, IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		IAstEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(getJavaProject(), (IJavaDebugTarget)frame.getDebugTarget());
		engine.evaluate(snippet, frame, this, DebugEvent.EVALUATION, true);

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		engine.dispose();
		return fEvaluationResult;
	}		
	
	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	public void evaluationComplete(IEvaluationResult result) {
		fEvaluationResult = result;
	}
	
	/**
	 * Performs a step over in the given stack frame and returns when complete.
	 * 
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepOver(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		frame.stepOver();
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;
	}

	/**
	 * Performs a step into in the given stack frame and returns when complete.
	 * 
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepInto(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		frame.stepInto();
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;		
	}
	
	/**
	 * Performs a step return in the given stack frame and returns when complete.
	 * 
	 * @param frame stack frame to step return from
	 */
	protected IJavaThread stepReturn(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		frame.stepReturn();
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;
	}	
	
	/**
	 * Performs a step into with filters in the given stack frame and returns when
	 * complete.
	 * 
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepIntoWithFilters(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		// turn filters on
		try {
			DebugUITools.setUseStepFilters(true);
			frame.stepInto();
		} finally {
			// turn filters off
			DebugUITools.setUseStepFilters(false);
		}
		
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;		
	}	

	/**
	 * Performs a step return with filters in the given stack frame and returns when
	 * complete.
	 * 
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepReturnWithFilters(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		// turn filters on
		try {
			DebugUITools.setUseStepFilters(true);
			frame.stepReturn();
		} finally {
			// turn filters off
			DebugUITools.setUseStepFilters(false);
		}
		
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;		
	}	
	
	/**
	 * Performs a step over with filters in the given stack frame and returns when
	 * complete.
	 * 
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepOverWithFilters(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		
		// turn filters on
		try {
			DebugUITools.setUseStepFilters(true);
			frame.stepOver();
		} finally {
			// turn filters off
			DebugUITools.setUseStepFilters(false);
		}
		
		
		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee);
		return (IJavaThread) suspendee;		
	}	
	/**
	 * Sets the "suspend on uncaught exception" preference as specified.
	 * 	 * @param on of off	 */	
	protected void setSuspendOnUncaughtExceptionsPreference(boolean on) {
		JDIDebugUIPlugin.getDefault().getPreferenceStore().setDefault(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, on);
	}

	/**
	 * Returns the compilation unit with the given name.
	 * 
	 * @param project the project containing the CU
	 * @param root the name of the source folder in the project
	 * @param pkg the name of the package (empty string for default package)
	 * @param name the name of the CU (ex. Something.java)
	 * @return compilation unit
	 */
	protected ICompilationUnit getCompilationUnit(IJavaProject project, String root, String pkg, String name) {
		IProject p = project.getProject();
		IResource r = p.getFolder(root);
		return project.getPackageFragmentRoot(r).getPackageFragment(pkg).getCompilationUnit(name);
	}
	
	/**
	 * Wait for autobuild to occur
	 */
	public void waitForAutoBuild() {
		try {
			Platform.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (OperationCanceledException e) {
		} catch (InterruptedException e) {
		}
	}	
}

