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

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.core.ArchiveSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.ArchiveSourceLookupTests;
import org.eclipse.jdt.debug.tests.core.ArrayTests;
import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.BreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.BreakpointLocationVerificationTests;
import org.eclipse.jdt.debug.tests.core.ClasspathContainerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ClasspathVariableTests;
import org.eclipse.jdt.debug.tests.core.ConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.DebugEventTests;
import org.eclipse.jdt.debug.tests.core.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.core.DirectorySourceContainerTests;
import org.eclipse.jdt.debug.tests.core.DirectorySourceLookupTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.core.FolderSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.HcrTests;
import org.eclipse.jdt.debug.tests.core.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.InstanceFilterTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.JavaBreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationArgumentTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.core.LaunchDelegateTests;
import org.eclipse.jdt.debug.tests.core.LaunchModeTests;
import org.eclipse.jdt.debug.tests.core.LaunchTests;
import org.eclipse.jdt.debug.tests.core.LaunchesTests;
import org.eclipse.jdt.debug.tests.core.LineTrackerTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.core.MiscBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ProcessTests;
import org.eclipse.jdt.debug.tests.core.ProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.RefreshTabTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
import org.eclipse.jdt.debug.tests.core.DefaultSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.SourceLocationTests;
import org.eclipse.jdt.debug.tests.core.SourceLookupTests;
import org.eclipse.jdt.debug.tests.core.StaticVariableTests;
import org.eclipse.jdt.debug.tests.core.StepFilterTests;
import org.eclipse.jdt.debug.tests.core.StepIntoSelectionTests;
import org.eclipse.jdt.debug.tests.core.StratumTests;
import org.eclipse.jdt.debug.tests.core.StringSubstitutionTests;
import org.eclipse.jdt.debug.tests.core.SuspendVMBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ThreadFilterBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.TypeTests;
import org.eclipse.jdt.debug.tests.core.WatchExpressionTests;
import org.eclipse.jdt.debug.tests.core.WatchpointTests;
import org.eclipse.jdt.debug.tests.core.WorkspaceSourceContainerTests;
import org.eclipse.swt.widgets.Display;

/**
 * Tests for integration and nightly builds.
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
		
		addTest(new TestSuite(LaunchModeTests.class));
		addTest(new TestSuite(LaunchDelegateTests.class));
		addTest(new TestSuite(LaunchTests.class));
		addTest(new TestSuite(LaunchesTests.class));
		addTest(new TestSuite(ClasspathVariableTests.class));
		addTest(new TestSuite(DebugEventTests.class));
		addTest(new TestSuite(ClasspathContainerTests.class));
		addTest(new TestSuite(LaunchConfigurationArgumentTests.class));
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(DeferredBreakpointTests.class));
		addTest(new TestSuite(ConditionalBreakpointsTests.class));
		addTest(new TestSuite(HitCountBreakpointsTests.class));
		addTest(new TestSuite(ThreadFilterBreakpointsTests.class));
		addTest(new TestSuite(SuspendVMBreakpointsTests.class));

		addTest(new TestSuite(StepFilterTests.class));

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
		addTest(new TestSuite(TypeTests.class));
		addTest(new TestSuite(InstanceFilterTests.class));
		addTest(new TestSuite(BreakpointListenerTests.class));
		addTest(new TestSuite(JavaBreakpointListenerTests.class));
		
		addTest(new TestSuite(SourceLookupTests.class));
		addTest(new TestSuite(FolderSourceContainerTests.class));
		addTest(new TestSuite(DirectorySourceContainerTests.class));
		addTest(new TestSuite(ProjectSourceContainerTests.class));
		addTest(new TestSuite(WorkspaceSourceContainerTests.class));		
		addTest(new TestSuite(DefaultSourceContainerTests.class));
		addTest(new TestSuite(DirectorySourceLookupTests.class));
		addTest(new TestSuite(ArchiveSourceContainerTests.class));
		addTest(new TestSuite(ArchiveSourceLookupTests.class));
		addTest(new TestSuite(MiscBreakpointsTests.class));
		// removed for M5 - see bug 46991
		//addTest(new TestSuite(WorkingDirectoryTests.class));
		addTest(new TestSuite(StepIntoSelectionTests.class));
		addTest(new TestSuite(StringSubstitutionTests.class));
		addTest(new TestSuite(RefreshTabTests.class));
		addTest(new TestSuite(WatchExpressionTests.class));
		addTest(new TestSuite(LineTrackerTests.class));
		addTest(new TestSuite(StratumTests.class));
		addTest(new TestSuite(BreakpointLocationVerificationTests.class));
		addTest(new TestSuite(ArrayTests.class));
		// HCR tests are last - they modify resources
		addTest(new TestSuite(HcrTests.class));
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

