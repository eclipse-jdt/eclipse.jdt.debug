/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.BreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathContainerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ClasspathVariableTests;
import org.eclipse.jdt.debug.tests.core.CommandArgumentTests;
import org.eclipse.jdt.debug.tests.core.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.core.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.InstanceFilterTests;
import org.eclipse.jdt.debug.tests.core.JavaBreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.core.MiscBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ProcessTests;
import org.eclipse.jdt.debug.tests.core.RemoteAttachTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
import org.eclipse.jdt.debug.tests.core.SourceLocationTests;
import org.eclipse.jdt.debug.tests.core.StaticVariableTests;
import org.eclipse.jdt.debug.tests.core.StepFilterTests;
import org.eclipse.jdt.debug.tests.core.SuspendVMBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ThreadFilterBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.TypeTests;
import org.eclipse.jdt.debug.tests.core.WatchpointTests;
import org.eclipse.jdt.debug.tests.core.WorkingDirectoryTests;

/**
 * Test all areas of the JDT Debugger.
 * 
 * To run this test suite:
 * <ol>
 * <li>Create a new Run-time Workbench launch configuration</li>
 * <li>Append "org.eclipse.jdt.debug.tests.AutomatedSuite" to the Program Arguments</li>
 * <li>Set the Application Name to "org.eclipse.jdt.debug.tests.app"</li>
 * <li>Run the launch configuration. Output from the tests will be written to the debug console</li>
 * </ol>
 */
public class AutomatedSuite extends DebugSuite {
	
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		addTest(new TestSuite(ProjectCreationDecorator.class));
		
		addTest(new TestSuite(ClasspathVariableTests.class));
		addTest(new TestSuite(ClasspathContainerTests.class));
		addTest(new TestSuite(CommandArgumentTests.class));
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(DeferredBreakpointTests.class));
		//addTest(new TestSuite(ConditionalBreakpointsTests.class));
		addTest(new TestSuite(HitCountBreakpointsTests.class));
		addTest(new TestSuite(ThreadFilterBreakpointsTests.class));
		addTest(new TestSuite(SuspendVMBreakpointsTests.class));

		addTest(new TestSuite(StepFilterTests.class));

		//addTest(new TestSuite(InstanceVariableTests.class));
		addTest(new TestSuite(LocalVariableTests.class));
		addTest(new TestSuite(StaticVariableTests.class));
		addTest(new TestSuite(MethodBreakpointTests.class));
		addTest(new TestSuite(ExceptionBreakpointTests.class));
		addTest(new TestSuite(WatchpointTests.class));
		addTest(new TestSuite(PatternBreakpointTests.class));
		addTest(new TestSuite(TargetPatternBreakpointTests.class));
		addTest(new TestSuite(EventSetTests.class));
		addTest(new TestSuite(RuntimeClasspathEntryTests.class));
		addTest(new TestSuite(ClasspathProviderTests.class));
		addTest(new TestSuite(SourceLocationTests.class));
		addTest(new TestSuite(ProcessTests.class));
		addTest(new TestSuite(BootpathTests.class));
		addTest(new TestSuite(TypeTests.class));
		addTest(new TestSuite(InstanceFilterTests.class));
		addTest(new TestSuite(BreakpointListenerTests.class));
		addTest(new TestSuite(JavaBreakpointListenerTests.class));
		
		// Evaluation tests
//		addTest(new TestSuite(TestsOperators1.class));
//		addTest(new TestSuite(TestsOperators2.class));
//		addTest(new TestSuite(TestsArrays.class));
//		addTest(new TestSuite(TestsNestedTypes1.class));
//		addTest(new TestSuite(TestsNestedTypes2.class));
//		addTest(new TestSuite(TestsTypeHierarchy1.class));
//		addTest(new TestSuite(TestsTypeHierarchy2.class));
		
		addTest(new TestSuite(MiscBreakpointsTests.class));
		addTest(new TestSuite(WorkingDirectoryTests.class));
		addTest(new TestSuite(RemoteAttachTests.class));
	}
}

