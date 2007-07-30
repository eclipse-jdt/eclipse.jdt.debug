/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.eclipse.jdt.debug.test.stepping.ForceReturnTests;
import org.eclipse.jdt.debug.test.stepping.StepFilterTests;
import org.eclipse.jdt.debug.test.stepping.StepIntoSelectionTests;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointListenerTests;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointLocationVerificationTests;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointWorkingSetTests;
import org.eclipse.jdt.debug.tests.breakpoints.ConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.ImportBreakpointsTest;
import org.eclipse.jdt.debug.tests.breakpoints.JavaBreakpointListenerTests;
import org.eclipse.jdt.debug.tests.breakpoints.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.MiscBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.PreLaunchBreakpointTest;
import org.eclipse.jdt.debug.tests.breakpoints.RunToLineTests;
import org.eclipse.jdt.debug.tests.breakpoints.SuspendVMBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.ThreadFilterBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.WatchpointTests;
import org.eclipse.jdt.debug.tests.console.IOConsoleTests;
import org.eclipse.jdt.debug.tests.core.ArgumentTests;
import org.eclipse.jdt.debug.tests.core.ArrayTests;
import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.ClasspathContainerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ClasspathVariableTests;
import org.eclipse.jdt.debug.tests.core.ConsoleInputTests;
import org.eclipse.jdt.debug.tests.core.ConsoleTests;
import org.eclipse.jdt.debug.tests.core.DebugEventTests;
import org.eclipse.jdt.debug.tests.core.EnvironmentTests;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExecutionEnvironmentTests;
import org.eclipse.jdt.debug.tests.core.HcrTests;
import org.eclipse.jdt.debug.tests.core.InstanceFilterTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.InstructionPointerTests;
import org.eclipse.jdt.debug.tests.core.JDWPTests;
import org.eclipse.jdt.debug.tests.core.JavaLibraryPathTests;
import org.eclipse.jdt.debug.tests.core.LineTrackerTests;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
import org.eclipse.jdt.debug.tests.core.MemoryRenderingTests;
import org.eclipse.jdt.debug.tests.core.ProcessTests;
import org.eclipse.jdt.debug.tests.core.RuntimeClasspathEntryTests;
import org.eclipse.jdt.debug.tests.core.StaticVariableTests;
import org.eclipse.jdt.debug.tests.core.StratumTests;
import org.eclipse.jdt.debug.tests.core.StringSubstitutionTests;
import org.eclipse.jdt.debug.tests.core.TypeTests;
import org.eclipse.jdt.debug.tests.core.VMInstallTests;
import org.eclipse.jdt.debug.tests.core.WatchExpressionTests;
import org.eclipse.jdt.debug.tests.core.WorkingDirectoryTests;
import org.eclipse.jdt.debug.tests.core.WorkspaceSourceContainerTests;
import org.eclipse.jdt.debug.tests.launching.AcceleratorSubstitutionTests;
import org.eclipse.jdt.debug.tests.launching.ConfigurationEncodingTests;
import org.eclipse.jdt.debug.tests.launching.ConfigurationResourceMappingTests;
import org.eclipse.jdt.debug.tests.launching.ContributedTabTests;
import org.eclipse.jdt.debug.tests.launching.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.launching.LaunchDelegateTests;
import org.eclipse.jdt.debug.tests.launching.LaunchHistoryTests;
import org.eclipse.jdt.debug.tests.launching.LaunchModeTests;
import org.eclipse.jdt.debug.tests.launching.LaunchShortcutTests;
import org.eclipse.jdt.debug.tests.launching.LaunchTests;
import org.eclipse.jdt.debug.tests.launching.LaunchesTests;
import org.eclipse.jdt.debug.tests.launching.MigrationDelegateTests;
import org.eclipse.jdt.debug.tests.launching.RefreshTabTests;
import org.eclipse.jdt.debug.tests.launching.TabGroupWrapperTests;
import org.eclipse.jdt.debug.tests.refactoring.MoveCompilationUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameCompilationUnitUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameFieldUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameInnerTypeUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameMethodUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenameNonPublicTypeUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenamePackageUnitTests;
import org.eclipse.jdt.debug.tests.refactoring.RenamePublicTypeUnitTests;
import org.eclipse.jdt.debug.tests.sourcelookup.ArchiveSourceLookupTests;
import org.eclipse.jdt.debug.tests.sourcelookup.DefaultSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.DirectorySourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.DirectorySourceLookupTests;
import org.eclipse.jdt.debug.tests.sourcelookup.ExternalArchiveSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.FolderSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.JavaProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.ProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.SourceLocationTests;
import org.eclipse.jdt.debug.tests.sourcelookup.SourceLookupTests;
import org.eclipse.jdt.debug.tests.ui.DetailPaneManagerTests;
import org.eclipse.jdt.debug.tests.ui.ViewMangementTests;
import org.eclipse.jdt.debug.tests.viewers.ChildrenUpdateTests;
import org.eclipse.jdt.debug.tests.viewers.FilterTransformTests;

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
			
	//Launching tests
		addTest(new TestSuite(LaunchModeTests.class));
		addTest(new TestSuite(LaunchDelegateTests.class));
		addTest(new TestSuite(LaunchShortcutTests.class));
		addTest(new TestSuite(LaunchTests.class));
		addTest(new TestSuite(LaunchesTests.class));
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(ContributedTabTests.class));
		addTest(new TestSuite(TabGroupWrapperTests.class));
		addTest(new TestSuite(MigrationDelegateTests.class));
		addTest(new TestSuite(RefreshTabTests.class));
		addTest(new TestSuite(LaunchHistoryTests.class));
		addTest(new TestSuite(AcceleratorSubstitutionTests.class));
		addTest(new TestSuite(ConfigurationResourceMappingTests.class));
		addTest(new TestSuite(ConfigurationEncodingTests.class));
		
	//Breakpoints tests
		addTest(new TestSuite(DeferredBreakpointTests.class));
		addTest(new TestSuite(ConditionalBreakpointsTests.class));
		addTest(new TestSuite(HitCountBreakpointsTests.class));
		addTest(new TestSuite(ThreadFilterBreakpointsTests.class));
		addTest(new TestSuite(SuspendVMBreakpointsTests.class));
		addTest(new TestSuite(PreLaunchBreakpointTest.class)); 
		addTest(new TestSuite(ImportBreakpointsTest.class));
		addTest(new TestSuite(BreakpointWorkingSetTests.class));
		addTest(new TestSuite(MethodBreakpointTests.class));
		addTest(new TestSuite(ExceptionBreakpointTests.class));
		addTest(new TestSuite(WatchpointTests.class));
		addTest(new TestSuite(PatternBreakpointTests.class));
		addTest(new TestSuite(TargetPatternBreakpointTests.class));
		addTest(new TestSuite(BreakpointListenerTests.class));
		addTest(new TestSuite(JavaBreakpointListenerTests.class));
		addTest(new TestSuite(MiscBreakpointsTests.class));
		addTest(new TestSuite(BreakpointLocationVerificationTests.class));
		addTest(new TestSuite(RunToLineTests.class));
		
	//Sourcelookup tests
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
		addTest(new TestSuite(SourceLocationTests.class));
		
	// Variable tests
		addTest(new TestSuite(InstanceVariableTests.class));
		addTest(new TestSuite(LocalVariableTests.class));
		addTest(new TestSuite(StaticVariableTests.class));
		addTest(new TestSuite(ArrayTests.class));
		
	//Stepping tests
		addTest(new TestSuite(StepFilterTests.class));
		addTest(new TestSuite(StepIntoSelectionTests.class));
		addTest(new TestSuite(InstanceFilterTests.class));
		addTest(new TestSuite(ForceReturnTests.class));
		
	//Classpath tests
		addTest(new TestSuite(JavaLibraryPathTests.class));
		addTest(new TestSuite(ClasspathVariableTests.class));
		addTest(new TestSuite(ClasspathContainerTests.class));
		addTest(new TestSuite(RuntimeClasspathEntryTests.class));
		addTest(new TestSuite(ClasspathProviderTests.class));
		addTest(new TestSuite(BootpathTests.class));
		
	//VM Install/Environment tests
		addTest(new TestSuite(VMInstallTests.class));
		addTest(new TestSuite(StringSubstitutionTests.class));
		addTest(new TestSuite(EnvironmentTests.class));
		addTest(new TestSuite(ExecutionEnvironmentTests.class));
		addTest(new TestSuite(ArgumentTests.class));
		
	//Console tests
		addTest(new TestSuite(ConsoleTests.class));
		addTest(new TestSuite(ConsoleInputTests.class));
		addTest(new TestSuite(LineTrackerTests.class));
		addTest(new TestSuite(IOConsoleTests.class));
		
	//Core tests
		addTest(new TestSuite(DebugEventTests.class));
		addTest(new TestSuite(EventSetTests.class));
		addTest(new TestSuite(ProcessTests.class));
		addTest(new TestSuite(TypeTests.class));
		addTest(new TestSuite(WatchExpressionTests.class));
		addTest(new TestSuite(StratumTests.class));
		addTest(new TestSuite(WorkingDirectoryTests.class));

	//Memory view tests
		addTest(new TestSuite(MemoryRenderingTests.class));
		
	// Viewer tests
		addTest(new TestSuite(FilterTransformTests.class));
		addTest(new TestSuite(ChildrenUpdateTests.class));

	// Refactoring tests
		//TODO: project rename
		//TODO: package move
		addTest(new TestSuite(MoveCompilationUnitTests.class));
		addTest(new TestSuite(RenameFieldUnitTests.class));
		addTest(new TestSuite(RenamePackageUnitTests.class));
		addTest(new TestSuite(RenamePublicTypeUnitTests.class));
		addTest(new TestSuite(RenameInnerTypeUnitTests.class));
		addTest(new TestSuite(RenameNonPublicTypeUnitTests.class));
		addTest(new TestSuite(RenameCompilationUnitUnitTests.class));
		addTest(new TestSuite(RenameMethodUnitTests.class));
		//addTest(new TestSuite(MoveNonPublicTypeUnitTests.class));
		//addTest(new TestSuite(MoveInnerTypeUnitTests.class));
		//addTest(new TestSuite(MovePublicTypeMethodUnitTests.class));
		//addTest(new TestSuite(MoveNonPublicTypeMethodUnitTests.class));
		//addTest(new TestSuite(MoveInnerTypeMethodUnitTests.class));
		//addTest(new TestSuite(MoveFieldUnitTests.class));
		//addTest(new TestSuite(MoveInnerTypeToNewFileUnitTests.class));
		//addTest(new TestSuite(PushDownMethodUnitTests.class));
		//addTest(new TestSuite(PushDownFieldUnitTests.class));
		//addTest(new TestSuite(ExtractMethodUnitTests.class));
		//addTest(new TestSuite(IntroduceParameterUnitTests.class));
		//addTest(new TestSuite(ChangeMethodSignatureUnitTests.class));
		//addTest(new TestSuite(ChangeAnonymousTypeMethodSignatureUnitTests.class));
		//addTest(new TestSuite(ConvertPublicAnonymousTypeToNestedUnitTests.class));
		//addTest(new TestSuite(ConvertInnerAnonymousTypeToNestedUnitTests.class));
		//addTest(new TestSuite(ConvertNonPublicAnonymousTypeToNestedUnitTests.class));
		
	// JDWP tests
		addTest(new TestSuite(JDWPTests.class));
		
	// HCR tests are last - they modify resources
		addTest(new TestSuite(HcrTests.class));
		
	// Layout tests
		addTest(new TestSuite(ViewMangementTests.class));
		
	// Leak tests
		addTest(new TestSuite(InstructionPointerTests.class));
		
	// Variables View Detail Pane tests
		addTest(new TestSuite(DetailPaneManagerTests.class));
	}
}

