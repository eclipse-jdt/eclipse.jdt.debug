/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.breakpoints;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Home for breakpoint tests that don't fit elsewhere
 */
public class MiscBreakpointsTests extends AbstractDebugTest {

	private static final String COMPILE_ERROR_CONTENTS = 
	 "public class CompileError {\npublic static void main(String[] args) {\nString foo = \"foo\" + bar;\n}	\n}";

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
		
		IType type = fJavaProject.findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		IBuffer buffer = cu.getBuffer();
		buffer.setContents(COMPILE_ERROR_CONTENTS);
		cu.save(new NullProgressMonitor(), true);
        waitForBuild();
		
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
		
		IType type = fJavaProject.findType(typeName);
		ICompilationUnit cu = type.getCompilationUnit();
		IBuffer buffer = cu.getBuffer();
		buffer.setContents(COMPILE_ERROR_CONTENTS);
		cu.save(new NullProgressMonitor(), true);
        waitForBuild();
		
		IJavaDebugTarget debugTarget = null;
		try {
			debugTarget= launchAndTerminate(typeName);
		} finally {
			terminateAndRemove(debugTarget);
			removeAllBreakpoints();
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
	 * Returns the <code>JDIDebugUIPlugin</code> preference store
	 * @return
	 */
	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();		
	}
}
