/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     SAP SE - Support hyperlinks for stack entries with method signature
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.debug.test.stepping.ForceReturnTests;
import org.eclipse.jdt.debug.test.stepping.StepFilterTests;
import org.eclipse.jdt.debug.test.stepping.StepIntoSelectionTests;
import org.eclipse.jdt.debug.test.stepping.StepIntoSelectionWithGenerics;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointListenerTests;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointLocationVerificationTests;
import org.eclipse.jdt.debug.tests.breakpoints.BreakpointWorkingSetTests;
import org.eclipse.jdt.debug.tests.breakpoints.ConditionalBreakpointsInJava8Tests;
import org.eclipse.jdt.debug.tests.breakpoints.ConditionalBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.ConditionalBreakpointsWithGenerics;
import org.eclipse.jdt.debug.tests.breakpoints.DeferredBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.ExceptionBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.HitCountBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.ImportBreakpointsTest;
import org.eclipse.jdt.debug.tests.breakpoints.JavaBreakpointListenerTests;
import org.eclipse.jdt.debug.tests.breakpoints.JavaThreadEventHandlerTests;
import org.eclipse.jdt.debug.tests.breakpoints.LambdaBreakpointsInJava8Tests;
import org.eclipse.jdt.debug.tests.breakpoints.MethodBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.MethodBreakpointTests15;
import org.eclipse.jdt.debug.tests.breakpoints.MiscBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.PatternBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.PreLaunchBreakpointTest;
import org.eclipse.jdt.debug.tests.breakpoints.RunToLineTests;
import org.eclipse.jdt.debug.tests.breakpoints.SuspendVMBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.TargetPatternBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.TestToggleBreakpointsTarget;
import org.eclipse.jdt.debug.tests.breakpoints.TestToggleBreakpointsTarget8;
import org.eclipse.jdt.debug.tests.breakpoints.ThreadFilterBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.ThreadNameChangeTests;
import org.eclipse.jdt.debug.tests.breakpoints.TriggerPointBreakpointsTests;
import org.eclipse.jdt.debug.tests.breakpoints.TypeNameBreakpointTests;
import org.eclipse.jdt.debug.tests.breakpoints.WatchpointTests;
import org.eclipse.jdt.debug.tests.connectors.MultipleConnectionsTest;
import org.eclipse.jdt.debug.tests.console.ConsoleTerminateAllActionTests;
import org.eclipse.jdt.debug.tests.console.IOConsoleTests;
import org.eclipse.jdt.debug.tests.console.JavaStackTraceConsoleTest;
import org.eclipse.jdt.debug.tests.core.AlternateStratumTests;
import org.eclipse.jdt.debug.tests.core.ArgumentTests;
import org.eclipse.jdt.debug.tests.core.ArrayTests;
import org.eclipse.jdt.debug.tests.core.BootpathTests;
import org.eclipse.jdt.debug.tests.core.ClasspathContainerTests;
import org.eclipse.jdt.debug.tests.core.ClasspathProviderTests;
import org.eclipse.jdt.debug.tests.core.ClasspathVariableTests;
import org.eclipse.jdt.debug.tests.core.ConsoleInputTests;
import org.eclipse.jdt.debug.tests.core.ConsoleTests;
import org.eclipse.jdt.debug.tests.core.DebugEventTests;
import org.eclipse.jdt.debug.tests.core.EEDefinitionTests;
import org.eclipse.jdt.debug.tests.core.EnvironmentTests;
import org.eclipse.jdt.debug.tests.core.EventDispatcherTest;
import org.eclipse.jdt.debug.tests.core.EventSetTests;
import org.eclipse.jdt.debug.tests.core.ExecutionEnvironmentTests;
import org.eclipse.jdt.debug.tests.core.HcrTests;
import org.eclipse.jdt.debug.tests.core.InstanceFilterTests;
import org.eclipse.jdt.debug.tests.core.InstanceVariableTests;
import org.eclipse.jdt.debug.tests.core.InstructionPointerTests;
import org.eclipse.jdt.debug.tests.core.JDWPTests;
import org.eclipse.jdt.debug.tests.core.JavaDebugTargetTests;
import org.eclipse.jdt.debug.tests.core.JavaLibraryPathTests;
import org.eclipse.jdt.debug.tests.core.LineTrackerTests;
import org.eclipse.jdt.debug.tests.core.LiteralTests17;
import org.eclipse.jdt.debug.tests.core.LocalVariableTests;
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
import org.eclipse.jdt.debug.tests.eval.GeneralEvalTests;
import org.eclipse.jdt.debug.tests.eval.GenericsEvalTests;
import org.eclipse.jdt.debug.tests.launching.ClasspathShortenerTests;
import org.eclipse.jdt.debug.tests.launching.ConfigurationEncodingTests;
import org.eclipse.jdt.debug.tests.launching.ConfigurationResourceMappingTests;
import org.eclipse.jdt.debug.tests.launching.ContributedTabTests;
import org.eclipse.jdt.debug.tests.launching.LaunchConfigurationManagerTests;
import org.eclipse.jdt.debug.tests.launching.LaunchConfigurationTests;
import org.eclipse.jdt.debug.tests.launching.LaunchDelegateTests;
import org.eclipse.jdt.debug.tests.launching.LaunchModeTests;
import org.eclipse.jdt.debug.tests.launching.LaunchShortcutTests;
import org.eclipse.jdt.debug.tests.launching.LaunchTests;
import org.eclipse.jdt.debug.tests.launching.LaunchesTests;
import org.eclipse.jdt.debug.tests.launching.LongClassPathTests;
import org.eclipse.jdt.debug.tests.launching.LongModulePathTests;
import org.eclipse.jdt.debug.tests.launching.MigrationDelegateTests;
import org.eclipse.jdt.debug.tests.launching.PListParserTests;
import org.eclipse.jdt.debug.tests.launching.ProjectClasspathVariableTests;
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
import org.eclipse.jdt.debug.tests.sourcelookup.JarSourceLookupTests;
import org.eclipse.jdt.debug.tests.sourcelookup.JavaProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.ProjectSourceContainerTests;
import org.eclipse.jdt.debug.tests.sourcelookup.SourceLocationTests;
import org.eclipse.jdt.debug.tests.sourcelookup.SourceLookupTests;
import org.eclipse.jdt.debug.tests.sourcelookup.TypeResolutionTests;
import org.eclipse.jdt.debug.tests.state.RefreshStateTests;
import org.eclipse.jdt.debug.tests.ui.DebugHoverTests;
import org.eclipse.jdt.debug.tests.ui.DebugViewTests;
import org.eclipse.jdt.debug.tests.ui.DetailPaneManagerTests;
import org.eclipse.jdt.debug.tests.ui.OpenFromClipboardTests;
import org.eclipse.jdt.debug.tests.ui.ViewManagementTests;
import org.eclipse.jdt.debug.tests.ui.presentation.ModelPresentationTests;
import org.eclipse.jdt.debug.tests.ui.presentation.ModelPresentationTests18;
import org.eclipse.jdt.debug.tests.variables.DetailFormatterTests;
import org.eclipse.jdt.debug.tests.variables.TestAnonymousInspect;
import org.eclipse.jdt.debug.tests.variables.TestInstanceRetrieval;
import org.eclipse.jdt.debug.tests.variables.TestIntegerAccessUnboxing15;
import org.eclipse.jdt.debug.tests.variables.TestLogicalStructures;
import org.eclipse.jdt.debug.tests.variables.TestLogicalStructuresJava9;

import junit.framework.Test;
import junit.framework.TestSuite;

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
		addTest(new TestSuite(ContributedTabTests.class));
		addTest(new TestSuite(TabGroupWrapperTests.class));
		addTest(new TestSuite(MigrationDelegateTests.class));
		addTest(new TestSuite(ConfigurationResourceMappingTests.class));
		addTest(new TestSuite(ConfigurationEncodingTests.class));
		addTest(new TestSuite(LaunchConfigurationManagerTests.class));
		addTest(new TestSuite(LaunchConfigurationTests.class));
		addTest(new TestSuite(ProjectClasspathVariableTests.class));

	//mac specific tests
		if(Platform.OS_MACOSX.equals(Platform.getOS())) {
			addTest(new TestSuite(PListParserTests.class));
		}

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
		addTest(new TestSuite(TypeResolutionTests.class));
		addTest(new TestSuite(JarSourceLookupTests.class));

	// Variable tests
		addTest(new TestSuite(InstanceVariableTests.class));
		addTest(new TestSuite(LocalVariableTests.class));
		addTest(new TestSuite(StaticVariableTests.class));
		addTest(new TestSuite(ArrayTests.class));
		addTest(new TestSuite(TestLogicalStructures.class));
		if (JavaProjectHelper.isJava9Compatible()) {
			addTest(new TestSuite(TestLogicalStructuresJava9.class));
		}
		addTest(new TestSuite(TestInstanceRetrieval.class));
		addTest(new TestSuite(TestAnonymousInspect.class));
		if(JavaProjectHelper.isJava7Compatible()) {
			addTest(new TestSuite(LiteralTests17.class));
		}

	//Stepping tests
		addTest(new TestSuite(StepFilterTests.class));
		addTest(new TestSuite(StepIntoSelectionTests.class));
		addTest(new TestSuite(InstanceFilterTests.class));
		if (JavaProjectHelper.isJava6Compatible()) {
			addTest(new TestSuite(ForceReturnTests.class));
		}

	//Classpath tests
		addTest(new TestSuite(JavaLibraryPathTests.class));
		addTest(new TestSuite(ClasspathVariableTests.class));
		addTest(new TestSuite(ClasspathContainerTests.class));
		addTest(new TestSuite(RuntimeClasspathEntryTests.class));
		addTest(new TestSuite(ClasspathProviderTests.class));
		if (!JavaProjectHelper.isJava9Compatible()) {
			addTest(new TestSuite(BootpathTests.class));
		}
		addTest(new TestSuite(EEDefinitionTests.class));

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
		addTest(new TestSuite(JavaStackTraceConsoleTest.class));
		addTest(new TestSuite(IOConsoleTests.class));
		addTest(new TestSuite(ConsoleTerminateAllActionTests.class));

	//Core tests
		addTest(new TestSuite(DebugEventTests.class));
		addTest(new TestSuite(EventSetTests.class));
		addTest(new TestSuite(ProcessTests.class));
		addTest(new TestSuite(TypeTests.class));
		addTest(new TestSuite(WatchExpressionTests.class));
		addTest(new TestSuite(StratumTests.class));
		addTest(new TestSuite(JavaDebugTargetTests.class));
		addTest(new TestSuite(WorkingDirectoryTests.class));
		addTest(new TestSuite(EventDispatcherTest.class));

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
		addTest(new TestSuite(MultipleConnectionsTest.class));
	// Refresh state tests
		addTest(new TestSuite(RefreshStateTests.class));

	// HCR tests are last - they modify resources
		addTest(new TestSuite(HcrTests.class));

	// Layout tests
		addTest(new TestSuite(ViewManagementTests.class));

	// Debug view tests
		// Test was not stable, see bug 516024, the hope is that fix for bug 535686 helps
		addTest(new TestSuite(DebugViewTests.class));

	// Debug hover tests
		addTest(new TestSuite(DebugHoverTests.class));

	// Leak tests
		addTest(new TestSuite(InstructionPointerTests.class));

	// Variables View Detail Pane tests
		addTest(DetailPaneManagerTests.suite());
		addTest(new TestSuite(ModelPresentationTests.class));

	// Open from Clipboard action tests - Need to use #suite() because the test has a custom setup
		addTest(OpenFromClipboardTests.suite());

	//add the complete eval suite
		addTest(new TestSuite(GeneralEvalTests.class));
		//addTest(EvalTestSuite.suite());

		// long classpath tests
		addTest(new TestSuite(ClasspathShortenerTests.class));
		addTest(LongClassPathTests.suite());
		if (JavaProjectHelper.isJava9Compatible()) {
			addTest(new TestSuite(LongModulePathTests.class));
		}

		// Breakpoints tests
		addTest(new TestSuite(TypeNameBreakpointTests.class));
		addTest(new TestSuite(DeferredBreakpointTests.class));
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
		addTest(new TestSuite(ThreadNameChangeTests.class));
		addTest(new TestSuite(BreakpointLocationVerificationTests.class));
		addTest(new TestSuite(RunToLineTests.class));
		addTest(new TestSuite(TestToggleBreakpointsTarget.class));
		addTest(new TestSuite(TriggerPointBreakpointsTests.class));
		addTest(new TestSuite(JavaThreadEventHandlerTests.class));

		if (JavaProjectHelper.isJava8Compatible()) {
			addTest(new TestSuite(TestToggleBreakpointsTarget8.class));
			addTest(new TestSuite(ModelPresentationTests18.class));
			addTest(new TestSuite(ConditionalBreakpointsInJava8Tests.class));
			addTest(new TestSuite(LambdaBreakpointsInJava8Tests.class));
		}
		if (JavaProjectHelper.isJava5Compatible()) {
			addTest(new TestSuite(MethodBreakpointTests15.class));
			addTest(new TestSuite(TestIntegerAccessUnboxing15.class));
			addTest(new TestSuite(StepIntoSelectionWithGenerics.class));
			addTest(new TestSuite(ConditionalBreakpointsWithGenerics.class));
			addTest(new TestSuite(GenericsEvalTests.class));
			addTest(new TestSuite(DetailFormatterTests.class));
			addTest(new TestSuite(AlternateStratumTests.class));
		}
		addTest(new TestSuite(ConditionalBreakpointsTests.class));
	}
}
