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
package org.eclipse.jdt.debug.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.debug.tests.console.IOConsoleTests;
import org.eclipse.jdt.debug.tests.core.ArchiveSourceLookupTests;
import org.eclipse.jdt.debug.tests.core.ArgumentTests;
import org.eclipse.jdt.debug.tests.core.ArrayTests;
import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.BreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.BreakpointLocationVerificationTests;
import org.eclipse.jdt.debug.tests.core.ClasspathContainerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ClasspathVariableTests;
import org.eclipse.jdt.debug.tests.core.ConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.ConsoleInputTests;
import org.eclipse.jdt.debug.tests.core.ConsoleTests;
import org.eclipse.jdt.debug.tests.core.DebugEventTests;
import org.eclipse.jdt.debug.tests.core.DefaultSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.core.DirectorySourceContainerTests;
import org.eclipse.jdt.debug.tests.core.DirectorySourceLookupTests;
import org.eclipse.jdt.debug.tests.core.EnvironmentTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.core.ExecutionEnvironmentTests;
import org.eclipse.jdt.debug.tests.core.ExternalArchiveSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.FolderSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.HcrTests;
import org.eclipse.jdt.debug.tests.core.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.InstanceFilterTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.JavaBreakpointListenerTests;
import org.eclipse.jdt.debug.tests.core.JavaLibraryPathTests;
import org.eclipse.jdt.debug.tests.core.JavaProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.core.LaunchDelegateTests;
import org.eclipse.jdt.debug.tests.core.LaunchModeTests;
import org.eclipse.jdt.debug.tests.core.LaunchTests;
import org.eclipse.jdt.debug.tests.core.LaunchesTests;
import org.eclipse.jdt.debug.tests.core.LineTrackerTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MemoryRenderingTests;
import org.eclipse.jdt.debug.tests.core.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.core.MiscBreakpointsTests;
import org.eclipse.jdt.debug.tests.core.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.core.PreLaunchBreakpointTest;
import org.eclipse.jdt.debug.tests.core.ProcessTests;
import org.eclipse.jdt.debug.tests.core.ProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.core.RefreshTabTests;
import org.eclipse.jdt.debug.tests.core.RunToLineTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
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
import org.eclipse.jdt.debug.tests.core.VMInstallTests;
import org.eclipse.jdt.debug.tests.core.WatchExpressionTests;
import org.eclipse.jdt.debug.tests.core.WatchpointTests;
import org.eclipse.jdt.debug.tests.core.WorkspaceSourceContainerTests;
import org.eclipse.jdt.debug.tests.refactoring.MoveCompilationUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameCompilationUnitUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameFieldUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameInnerTypeUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameMethodUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameNonPublicTypeUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenamePackageUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenamePublicTypeUnitTests;
import org.eclipse.jdt.debug.tests.ui.BreakpointWorkingSetTests;
import org.eclipse.jdt.debug.tests.ui.ImportBreakpointsTest;
import org.eclipse.jdt.debug.tests.ui.MigrationDelegateTests;
import org.eclipse.jdt.debug.tests.ui.ViewMangementTests;

/**
 * Tests for integration and nightly builds.
 */
public class AutomatedSuite extends DebugSuite {
	
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return the test
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
        addTest(new TestSuite(VMInstallTests.class));
		addTest(new TestSuite(ArgumentTests.class));
		addTest(new TestSuite(ConsoleTests.class));
		addTest(new TestSuite(ConsoleInputTests.class));
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(DeferredBreakpointTests.class));
		addTest(new TestSuite(ConditionalBreakpointsTests.class));
		addTest(new TestSuite(HitCountBreakpointsTests.class));
		addTest(new TestSuite(ThreadFilterBreakpointsTests.class));
		addTest(new TestSuite(SuspendVMBreakpointsTests.class));
		addTest(new TestSuite(PreLaunchBreakpointTest.class)); 
		addTest(new TestSuite(ImportBreakpointsTest.class));
		addTest(new TestSuite(MigrationDelegateTests.class));
		addTest(new TestSuite(BreakpointWorkingSetTests.class));
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
		addTest(new TestSuite(ExternalArchiveSourceContainerTests.class));
		addTest(new TestSuite(ArchiveSourceLookupTests.class));
		addTest(new TestSuite(JavaProjectSourceContainerTests.class));
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
		addTest(new TestSuite(IOConsoleTests.class));
		addTest(new TestSuite(RunToLineTests.class));
		addTest(new TestSuite(MemoryRenderingTests.class));
		addTest(new TestSuite(JavaLibraryPathTests.class));
		addTest(new TestSuite(EnvironmentTests.class));
		addTest(new TestSuite(ExecutionEnvironmentTests.class));
	
		// refactoring tests
		addTest(new TestSuite(MoveCompilationUnitTests.class));
		addTest(new TestSuite(RenameFieldUnitTests.class));
		addTest(new TestSuite(RenamePackageUnitTests.class));
		addTest(new TestSuite(RenamePublicTypeUnitTests.class));
		addTest(new TestSuite(RenameInnerTypeUnitTests.class));
		addTest(new TestSuite(RenameNonPublicTypeUnitTests.class));
		addTest(new TestSuite(RenameCompilationUnitUnitTests.class));
		addTest(new TestSuite(RenameMethodUnitTests.class));
		//TODO: project rename
		//TODO: package move
		// most refactoring tests are disabled pending bug fixes in breakpoint refactoring
//		addTest(new TestSuite(MoveNonPublicTypeUnitTests.class));
//		addTest(new TestSuite(MoveInnerTypeUnitTests.class));
//		addTest(new TestSuite(MovePublicTypeMethodUnitTests.class));
//		addTest(new TestSuite(MoveNonPublicTypeMethodUnitTests.class));
//		addTest(new TestSuite(MoveInnerTypeMethodUnitTests.class));
//		addTest(new TestSuite(MoveFieldUnitTests.class));
//		addTest(new TestSuite(MoveInnerTypeToNewFileUnitTests.class));
//		addTest(new TestSuite(PushDownMethodUnitTests.class));
//		addTest(new TestSuite(PushDownFieldUnitTests.class));
//		addTest(new TestSuite(PullUpMethodUnitTests.class));
//		addTest(new TestSuite(PullUpFieldUnitTests.class));
//		addTest(new TestSuite(ExtractMethodUnitTests.class));
//		addTest(new TestSuite(IntroduceParameterUnitTests.class));
//		addTest(new TestSuite(ChangeMethodSignatureUnitTests.class));
//		addTest(new TestSuite(ChangeAnonymousTypeMethodSignatureUnitTests.class));
//		addTest(new TestSuite(ConvertPublicAnonymousTypeToNestedUnitTests.class));
//		addTest(new TestSuite(ConvertInnerAnonymousTypeToNestedUnitTests.class));
//		addTest(new TestSuite(ConvertNonPublicAnonymousTypeToNestedUnitTests.class));
		
		// HCR tests are last - they modify resources
		addTest(new TestSuite(HcrTests.class));
		
		// and these tests modify UI layout
		addTest(new TestSuite(ViewMangementTests.class));
		
		
	}
}

