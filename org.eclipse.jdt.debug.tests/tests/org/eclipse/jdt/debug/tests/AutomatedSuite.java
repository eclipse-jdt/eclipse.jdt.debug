/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.debug.tests;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.jdt.debug.testplugin.TestPluginLauncher;
import org.eclipse.jdt.debug.tests.core.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.core.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
import org.eclipse.jdt.debug.tests.core.StaticVariableTests;
import org.eclipse.jdt.debug.tests.core.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.WatchpointTests;
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

