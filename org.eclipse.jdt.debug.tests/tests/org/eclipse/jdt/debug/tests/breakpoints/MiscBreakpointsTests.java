/*******************************************************************************
 *  Copyright (c) 2000, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Home for breakpoint tests that don't fit elsewhere
 */
public class MiscBreakpointsTests extends AbstractDebugTest {

	private static final String COMPILE_ERROR_CONTENTS =
	 "public class CompileError {\npublic static void main(String[] args) {\nString foo = \"foo\" + bar;\n}	\n}";

	private static final String ORIGINAL_CONTENTS = "public class CompileError {\npublic static void main(String[] args) {\n}\n}";

	/**
	 * Constructor
	 * @param name
	 */
	public MiscBreakpointsTests(String name) {
		super(name);
	}

	/**
	 * This method DEPENDS on the default setting of the 'suspend on uncaught exceptions'
	 * preference being TRUE.
	 * @throws Exception
	 */
	public void testSuspendOnUncaughtExceptions() throws Exception {
		String typeName = "ThrowsNPE";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, true);

		IJavaThread javaThread = null;
		try {
			javaThread= launchAndSuspend(typeName);

			int stackLine = javaThread.getTopStackFrame().getLineNumber();
			assertTrue("line number should be '26', but was " + stackLine, stackLine == 26);

		} finally {
            getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
		}
	}

	/**
	 * This method DEPENDS on the default setting of the 'suspend on compilation errors'
	 * preference being TRUE.
	 * @throws Exception
	 */
	public void testSuspendOnCompilationErrors() throws Exception {
		String typeName = "CompileError";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, true);

		IType type = get14Project().findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		setFileContents(cu, COMPILE_ERROR_CONTENTS);

		IJavaThread javaThread = null;
		try {
			javaThread= launchAndSuspend(typeName);

			int stackLine = javaThread.getTopStackFrame().getLineNumber();
			assertTrue("line number should be '3', but was " + stackLine, stackLine == 3);

		} finally {
            getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
            getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, false);
			terminateAndRemove(javaThread);
			removeAllBreakpoints();
			//restore the file to remove compile errors
			setFileContents(cu, ORIGINAL_CONTENTS);
		}
	}

	/**
	 * Tests that the program will not suspend on uncaught exceptions or errors if the corresponding
	 * preferences are set to false
	 * @throws Exception
	 */
	public void testDontSuspendOnCompilationErrors() throws Exception {
		String typeName = "CompileError";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, false);

		IType type = get14Project().findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		setFileContents(cu, COMPILE_ERROR_CONTENTS);

		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
			setFileContents(cu, ORIGINAL_CONTENTS);
		}
	}

	/**
	 * Tests that the program will not suspend on uncaught exceptions if the corresponding
	 * preference is set to false
	 * @throws Exception
	 */
	public void testDontSuspendOnUncaughtExceptions() throws Exception {
		String typeName = "ThrowsNPE";
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);

		IJavaDebugTarget debugTarget= null;
		try {
			debugTarget = launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that we listen to thread name changes and send a debug event if that happens
	 *
	 * @throws Exception
	 */
	public void testListenToThreadNameChange() throws Exception {
		String typeName = "ThreadNameChange";
		final int bpLine1 = 36;
		final int bpLine2 = 40;

		IJavaLineBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(bpLine2, "", typeName + ".java", typeName);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		bp2.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		boolean oldValue = getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES, true);
		AtomicReference<List<DebugEvent>> events = new AtomicReference<>(new ArrayList<DebugEvent>());
		IDebugEventSetListener listener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] e) {
				events.get().addAll(Arrays.asList(e));
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(listener);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);
			TestUtil.waitForJobs(getName(), 100, 3000);

			// expect that thread with name "1" is started
			IThread second = findThread(thread, "1");
			assertNotNull(second);
			events.get().clear();

			resumeToLineBreakpoint(thread, bp2);
			TestUtil.waitForJobs(getName(), 100, 3000);

			// expect one single "CHANGE" event for second thread
			List<DebugEvent> changeEvents = getStateChangeEvents(events, second);
			assertEquals(1, changeEvents.size());

			// expect that thread name is changed to "2"
			assertEquals("2", second.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES, oldValue);
		}
	}

	/**
	 * Tests that we do not listen to thread name changes if the corresponding preference is set to false
	 *
	 * @throws Exception
	 */
	public void testListenToThreadNameChangeDisabled() throws Exception {
		String typeName = "ThreadNameChange";
		final int bpLine1 = 36;
		final int bpLine2 = 40;

		IJavaLineBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName);
		IJavaLineBreakpoint bp2 = createLineBreakpoint(bpLine2, "", typeName + ".java", typeName);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		bp2.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		boolean oldValue = getPrefStore().getBoolean(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES, false);
		AtomicReference<List<DebugEvent>> events = new AtomicReference<>(new ArrayList<DebugEvent>());
		IDebugEventSetListener listener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] e) {
				events.get().addAll(Arrays.asList(e));
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(listener);
		IJavaThread thread = null;
		try {
			thread = launchToLineBreakpoint(typeName, bp1);
			TestUtil.waitForJobs(getName(), 100, 3000);

			// expect that thread with name "1" is started
			IThread second = findThread(thread, "1");
			assertNotNull(second);
			events.get().clear();

			resumeToLineBreakpoint(thread, bp2);
			TestUtil.waitForJobs(getName(), 100, 3000);

			// expect no "CHANGE" events
			List<DebugEvent> changeEvents = getStateChangeEvents(events, second);
			assertEquals(0, changeEvents.size());

			// expect that thread name is changed to "2"
			assertEquals("2", second.getName());
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			getPrefStore().setValue(IJDIPreferencesConstants.PREF_LISTEN_ON_THREAD_NAME_CHANGES, oldValue);
		}
	}

	private List<DebugEvent> getStateChangeEvents(AtomicReference<List<DebugEvent>> events, IThread second) {
		List<DebugEvent> list = events.get();
		Stream<DebugEvent> filtered = list.stream().filter(x -> x.getKind() == DebugEvent.CHANGE && x.getDetail() == DebugEvent.STATE
				&& x.getSource() == second);
		return filtered.collect(Collectors.toList());
	}

	private IThread findThread(IJavaThread thread, String name) throws DebugException {
		IThread t = Arrays.stream(thread.getDebugTarget().getThreads()).filter(x -> {
			try {
				return x.getName().equals(name);
			}
			catch (DebugException e1) {
			}
			return false;
		}).findFirst().get();
		return t;
	}

	/**
	 * Returns the <code>JDIDebugUIPlugin</code> preference store
	 * @return
	 */
	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}
}
