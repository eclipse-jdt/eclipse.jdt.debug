package org.eclipse.jdt.debug.tests;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.testplugin.TestPluginLauncher;
import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.core.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.core.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ProcessTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
import org.eclipse.jdt.debug.tests.core.SourceLocationTests;
import org.eclipse.jdt.debug.tests.core.StaticVariableTests;
import org.eclipse.jdt.debug.tests.core.SuspendVMBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ThreadFilterBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.WatchpointTests;
import org.eclipse.jdt.debug.tests.eval.TestsArrays;
import org.eclipse.jdt.debug.tests.eval.TestsNestedTypes1;
import org.eclipse.jdt.debug.tests.eval.TestsNestedTypes2;
import org.eclipse.jdt.debug.tests.eval.TestsOperators1;
import org.eclipse.jdt.debug.tests.eval.TestsOperators2;
import org.eclipse.jdt.debug.tests.eval.TestsTypeHierarchy1;
import org.eclipse.jdt.debug.tests.eval.TestsTypeHierarchy2;
import org.eclipse.swt.widgets.Display;

/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {
	
	/**
	 * Flag that indicates test are in progress
	 */
	protected boolean fTesting = true;

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
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(DeferredBreakpointTests.class));
		addTest(new TestSuite(ConditionalBreakpointsTests.class));
		addTest(new TestSuite(HitCountBreakpointsTests.class));
		addTest(new TestSuite(ThreadFilterBreakpointsTests.class));
		addTest(new TestSuite(SuspendVMBreakpointsTests.class));

		// This test suite is commented out because it references preferences
		// in a UI plugin.  This causes the UI plugin to get loaded as soon
		// as this class is created, resulting in random timing-related
		// failures in other tests.  
		//addTest(new TestSuite(StepFilterTests.class));

		addTest(new TestSuite(InstanceVariableTests.class));
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
		
		// Evaluation tests
		addTest(new TestSuite(TestsOperators1.class));
		addTest(new TestSuite(TestsOperators2.class));
		addTest(new TestSuite(TestsArrays.class));
		addTest(new TestSuite(TestsNestedTypes1.class));
		addTest(new TestSuite(TestsNestedTypes2.class));
		addTest(new TestSuite(TestsTypeHierarchy1.class));
		addTest(new TestSuite(TestsTypeHierarchy2.class));
		
		// This test suite is commented out because it references preferences
		// in a UI plugin.  This causes the UI plugin to get loaded as soon
		// as this class is created, resulting in random timing-related
		// failures in other tests.  This can be uncommented when bug
		// 15737 is fixed.		
		//addTest(new TestSuite(MiscBreakpointsTests.class));
	}
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), AutomatedSuite.class, args);
	}		

	/**
	 * Runs the tests and collects their result in a TestResult.
	 * The debug tests cannot be run in the UI thread or the event
	 * waiter blocks the UI when a resource changes.
	 */
	public void run(final TestResult result) {
		final Display display = Display.getCurrent();
		Thread thread = null;
		try {
			Runnable r = new Runnable() {
				public void run() {
					for (Enumeration e= tests(); e.hasMoreElements(); ) {
				  		if (result.shouldStop() )
				  			break;
						Test test= (Test)e.nextElement();
						runTest(test, result);
					}					
					fTesting = false;
					display.wake();
				}
			};
			thread = new Thread(r);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		while (fTesting) {
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}		
	}

}

