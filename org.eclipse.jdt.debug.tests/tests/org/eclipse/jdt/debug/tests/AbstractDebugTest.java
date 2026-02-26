/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *     Jesper Steen Møller - bug 422029: [1.8] Enable debug evaluation support for default methods
 *     Jesper Steen Møller - bug 426903: [1.8] Cannot evaluate super call to default method
 *     Jesper Steen Møller - bug 341232
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.core.LaunchDelegate;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaStratumLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaTargetPatternBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
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
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.core.LiteralTests17;
import org.eclipse.jdt.debug.tests.refactoring.MemberParser;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;

import com.sun.jdi.InternalException;
import com.sun.jdi.InvocationException;

import junit.framework.TestCase;

/**
 * Tests for launch configurations
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDebugTest extends TestCase implements  IEvaluationListener {

	private static boolean setupFirstTest = false;

	public static final String MULTI_OUTPUT_PROJECT_NAME = "MultiOutput";
	public static final String BOUND_EE_PROJECT_NAME = "BoundEE";
	public static final String ONE_FOUR_PROJECT_NAME = "DebugTests";
	public static final String ONE_FOUR_PROJECT_CLOSED_NAME = "ClosedDebugTests";
	public static final String ONE_FIVE_PROJECT_NAME = "OneFive";
	public static final String ONE_SEVEN_PROJECT_NAME = "OneSeven";
	public static final String ONE_EIGHT_PROJECT_NAME = "OneEight";
	public static final String NINE_PROJECT_NAME = "Nine";
	public static final String ONESIX_PROJECT_NAME = "One_Six";
	public static final String TWENTYONE_PROJECT_NAME = "Two_One";
	public static final String TWENTYTHREE_PROJECT_NAME = "Two_Three";
	public static final String TWENTYFOUR_PROJECT_NAME = "Two_Four";
	public static final String TWENTYFIVE_PROJECT_NAME = "Two_Five";
	public static final String BOUND_JRE_PROJECT_NAME = "BoundJRE";
	public static final String MR_PROJECT_NAME = "MR";
	public static final String CLONE_SUFFIX = "Clone";

	final String[] LAUNCH_CONFIG_NAMES_1_4 = { "LargeSourceFile", "LotsOfFields",
			"Breakpoints",
			"InstanceVariablesTests",
			"LocalVariablesTests", "LocalVariableTests2", "StaticVariablesTests",
			"DropTests", "ThrowsNPE", "ThrowsException", "org.eclipse.debug.tests.targets.Watchpoint",
			"org.eclipse.debug.tests.targets.BreakpointsLocationBug344984", "org.eclipse.debug.tests.targets.CallLoop", "A",
			"HitCountLooper", "CompileError", "MultiThreadedLoop", "HitCountException", "MultiThreadedException", "MultiThreadedList", "MethodLoop", "StepFilterOne",
			"StepFilterFour", "EvalArrayTests", "EvalSimpleTests", "EvalTypeTests", "EvalNestedTypeTests", "EvalTypeHierarchyTests",
			"EvalAnonymousClassVariableTests", "WorkingDirectoryTest",
			"OneToTen", "OneToTenPrint", "FloodConsole", "ConditionalStepReturn", "VariableChanges", "DefPkgReturnType", "InstanceFilterObject", "org.eclipse.debug.tests.targets.CallStack",
			"org.eclipse.debug.tests.targets.ThreadStack", "org.eclipse.debug.tests.targets.HcrClass", "org.eclipse.debug.tests.targets.StepIntoSelectionClass",
			"WatchItemTests", "ArrayTests", "ByteArrayTests", "PerfLoop", "Console80Chars", "ConsoleStackTrace", "ConsoleVariableLineLength", "StackTraces",
			"ConsoleInput", "PrintConcatenation", "VariableDetails", "org.eclipse.debug.tests.targets.ArrayDetailTests", "ArrayDetailTestsDef", "ForceReturnTests",
			"ForceReturnTestsTwo", "LogicalStructures", "BreakpointListenerTest", "LaunchHistoryTest", "LaunchHistoryTest2", "RunnableAppletImpl", "java6.AllInstancesTests",
			"bug329294", "bug401270", "org.eclipse.debug.tests.targets.HcrClass2", "org.eclipse.debug.tests.targets.HcrClass3", "org.eclipse.debug.tests.targets.HcrClass4",
			"org.eclipse.debug.tests.targets.HcrClass5", "org.eclipse.debug.tests.targets.HcrClass6", "org.eclipse.debug.tests.targets.HcrClass7", "org.eclipse.debug.tests.targets.HcrClass8",
			"org.eclipse.debug.tests.targets.HcrClass9", "TestContributedStepFilterClass", "TerminateAll_01", "TerminateAll_02", "StepResult1",
			"StepResult2", "StepResult3", "StepUncaught", "TriggerPoint_01", "BulkThreadCreationTest", "MethodExitAndException",
			"Bug534319earlyStart", "Bug534319lateStart", "Bug534319singleThread", "Bug534319startBetwen", "MethodCall", "Bug538303", "Bug540243",
			"OutSync", "OutSync2", "ConsoleOutputUmlaut", "ErrorRecurrence", "ModelPresentationTests", "Bug565982",
			"SuspendVMConditionalBreakpointsTestSnippet", "FileConditionSnippet2", "compare.CompareObjectsStringTest", "compare.CompareListObjects",
			"compare.CompareMapObjects", "compare.CompareSetObjects", "compare.CompareNormalObjects", "compare.CompareArrayObjects" };

	/**
	 * the default timeout
	 */
	public static final int DEFAULT_TIMEOUT = 30000;

	//constants
	protected static final String JAVA = "java"; //$NON-NLS-1$
	protected static final String JAVA_EXTENSION = ".java"; //$NON-NLS-1$
	protected static final String LAUNCHCONFIGURATIONS = "launchConfigurations"; //$NON-NLS-1$
	protected static final String LAUNCH_EXTENSION = ".launch"; //$NON-NLS-1$
	protected static final String LOCAL_JAVA_APPLICATION_TYPE_ID = "org.eclipse.jdt.launching.localJavaApplication"; //$NON-NLS-1$
	protected static final String JAVA_LAUNCH_SHORTCUT_ID = "org.eclipse.jdt.debug.ui.localJavaShortcut"; //$NON-NLS-1$
	protected static final String TEST_LAUNCH_SHORTCUT = "org.eclipse.jdt.debug.tests.testShortCut";

	/**
	 * an evaluation result
	 */
	public IEvaluationResult fEvaluationResult;

	/**
	 * The last relevant event set - for example, that caused
	 * a thread to suspend
	 */
	protected DebugEvent[] fEventSet;

	private static boolean loadedPrefs = false;
	private static boolean loaded14 = false;
	private static boolean loaded15 = false;
	private static boolean loaded17 = false;
	private static boolean loaded18 = false;
	private static boolean loaded9 = false;
	private static boolean loaded16_ = false;
	private static boolean loaded21 = false;
	private static boolean loaded23 = false;
	private static boolean loaded24 = false;
	private static boolean loaded25 = false;
	private static boolean loadedEE = false;
	private static boolean loadedJRE = false;
	private static boolean loadedMulti = false;
	private static boolean loadedMR;
	private static boolean welcomeClosed = false;

	/**
	 * Constructor
	 */
	public AbstractDebugTest(String name) {
		super(name);
		// set error dialog to non-blocking to avoid hanging the UI during test
		ErrorDialog.AUTOMATED_MODE = true;
		SafeRunnable.setIgnoreErrors(true);
	}

	@Override
	protected void setUp() throws Exception {
		if (!setupFirstTest) {
			setupFirstTest = true;
			TestUtil.logInfo("SETTING UP TESTS");
			JavaRuntime.addVMInstallChangedListener(new LogVMInstallChanges());
		}
		TestUtil.logInfo("SETUP " + getClass().getSimpleName() + "." + getName());
		super.setUp();
		setPreferences();
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ONE_FOUR_PROJECT_NAME);
		loaded14 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ONE_FIVE_PROJECT_NAME);
		loaded15 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ONE_SEVEN_PROJECT_NAME);
		loaded17 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ONE_EIGHT_PROJECT_NAME);
		loaded18 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(NINE_PROJECT_NAME);
		loaded9 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ONESIX_PROJECT_NAME);
		loaded16_ = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(TWENTYONE_PROJECT_NAME);
		loaded21 = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(BOUND_JRE_PROJECT_NAME);
		loadedJRE = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(BOUND_EE_PROJECT_NAME);
		loadedEE = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(MULTI_OUTPUT_PROJECT_NAME);
		loadedMulti = pro.exists();
		pro = ResourcesPlugin.getWorkspace().getRoot().getProject(MR_PROJECT_NAME);
		loadedMR = pro.exists();
		assertWelcomeScreenClosed();
	}

	synchronized void setPreferences() throws BackingStoreException {
		if(!loadedPrefs) {
	        IPreferenceStore debugUIPreferences = DebugUIPlugin.getDefault().getPreferenceStore();
	        // Don't prompt for perspective switching
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.ALWAYS);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.ALWAYS);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, MessageDialogWithToggle.NEVER);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_WAIT_FOR_BUILD, MessageDialogWithToggle.ALWAYS);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_CONTINUE_WITH_COMPILE_ERROR, MessageDialogWithToggle.ALWAYS);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH, MessageDialogWithToggle.NEVER);

	        String property = System.getProperty("debug.workbenchActivation");
	        boolean activate = property != null && property.equals("on");
	        debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, activate);
	        debugUIPreferences.setValue(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, activate);
	        debugUIPreferences.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, activate);
	        debugUIPreferences.setValue(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, activate);

	        IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
	        // Turn off suspend on uncaught exceptions
	        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);
	        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_COMPILATION_ERRORS, false);
	        // Don't warn about HCR failures
	        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_FAILED, false);
	        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_HCR_NOT_SUPPORTED, false);
	        jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_ALERT_OBSOLETE_METHODS, false);
	        // Set the timeout preference to a high value, to avoid timeouts while
	        // testing
	        JDIDebugModel.getPreferences().setDefault(JDIDebugModel.PREF_REQUEST_TIMEOUT, 10000);
	        // turn off monitor information
	        jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, false);

	        //make sure we are auto-refreshing external workspace changes
	        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES);
	        if(node != null) {
	        	node.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, true);
	        	node.putBoolean(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, true);
	        	node.flush();
	        }

			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.urischeme");
			if (prefs != null) {
				prefs.putBoolean("skipAutoRegistration", true);
			}
	        loadedPrefs = true;
		}
    }

	/**
	 * Creates the Java 1.4 compliant project
	 */
	synchronized void assert14Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
        try {
	        if (!loaded14) {
	        	try {
	        		jp = JavaProjectHelper.createJavaProject(ONE_FOUR_PROJECT_CLOSED_NAME);
	        		jp.getProject().close(null);
	        	}
	        	catch(Exception e) {
	        		handleProjectCreationException(e, ONE_FOUR_PROJECT_CLOSED_NAME, jp);
	        	}
				jp = createProject(ONE_FOUR_PROJECT_NAME, JavaProjectHelper.TEST_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
	        	IPackageFragmentRoot src = jp.findPackageFragmentRoot(new Path(ONE_FOUR_PROJECT_NAME).append(JavaProjectHelper.SRC_DIR).makeAbsolute());
	        	assertNotNull("The 'src' package fragment root should not be null", src);
	        	File root = JavaTestPlugin.getDefault().getFileInPlugin(new Path("testjars"));
		        JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
		        IPath path = src.getPath().append("A.jar");
		        JavaProjectHelper.addLibrary(jp, path);

		        //add a closed project optional classpath entry
		        //see https://bugs.eclipse.org/bugs/show_bug.cgi?id=380918
		        IClasspathEntry entry = JavaCore.newProjectEntry(
		        		new Path(ONE_FOUR_PROJECT_CLOSED_NAME).makeAbsolute(),
		        		new IAccessRule[0],
		        		false,
		        		new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, Boolean.TRUE.toString())},
		        		false);
		        JavaProjectHelper.addToClasspath(jp, entry);

		        // create launch configurations
		        for (int i = 0; i < LAUNCH_CONFIG_NAMES_1_4.length; i++) {
		        	cfgs.add(createLaunchConfiguration(jp, LAUNCH_CONFIG_NAMES_1_4[i]));
				}
		        loaded14 = true;
		        waitForBuild();
	        }
        }
        catch(Exception e) {
        	try {
        		if(jp != null) {
        			jp.getProject().delete(true,  true, null);
        		}
	        	for (int i = 0; i < cfgs.size(); i++) {
					cfgs.get(i).delete();
				}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, ONE_FOUR_PROJECT_NAME, jp);
        }
    }

	/**
	 * Creates the Java 1.5 compliant project
	 */
	void assert15Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
        try {
	        if (!loaded15) {
				jp = createProject(ONE_FIVE_PROJECT_NAME, JavaProjectHelper.TEST_1_5_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_1_8_EE_NAME, true);
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.MethodBreakpoints"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.IntegerAccess"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.StepIntoSelectionWithGenerics"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.ConditionalsNearGenerics"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.bug329294WithGenerics"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.bug403028"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.bug484686"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.PrimitivesTest"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.GenericMethodEntryTest"));
				cfgs.add(createLaunchConfiguration(jp, "org.eclipse.debug.tests.targets.HcrClass", true));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.Bug570988"));
				cfgs.add(createLaunchConfiguration(jp, "a.b.c.ExceptionDefaultTest"));
				loaded15 = true;
				waitForBuild();
	        }
        }
        catch(Exception e) {
        	try {
        		if(jp != null) {
		        	jp.getProject().delete(true,  true, null);
		        	for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, ONE_FIVE_PROJECT_NAME, jp);
        }
	}

	/**
	 * Creates the Java 1.7 compliant project
	 */
	synchronized void assert17Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
        try {
	        if (!loaded17) {
				jp = createProject(ONE_SEVEN_PROJECT_NAME, JavaProjectHelper.TEST_1_7_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
	    		cfgs.add(createLaunchConfiguration(jp, LiteralTests17.LITERAL_TYPE_NAME));
				cfgs.add(createLaunchConfiguration(jp, "ThreadNameChange"));
				cfgs.add(createLaunchConfiguration(jp, "Bug567801"));
				cfgs.add(createLaunchConfiguration(jp, "Bug572782"));
				cfgs.add(createLaunchConfiguration(jp, "Bug576829"));
	    		loaded17 = true;
	    		waitForBuild();
	        }
        }
        catch(Exception e) {
        	try {
        		if(jp != null) {
		        	jp.getProject().delete(true,  true, null);
		        	for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, ONE_SEVEN_PROJECT_NAME, jp);
        }
	}

	/**
	 * Creates the Java 1.8 compliant project
	 */
	synchronized void assert18Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
        try {
	        if (!loaded18) {
	        	jp = createProject(ONE_EIGHT_PROJECT_NAME, JavaProjectHelper.TEST_1_8_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_1_8_EE_NAME, false);
				IPath lib = new Path(JavaTestPlugin.getDefault().getFileInPlugin(new Path("testjars").append("gh275").append("debug-lib.jar")).getAbsolutePath());
				JavaProjectHelper.addLibrary(jp, lib);

	    		cfgs.add(createLaunchConfiguration(jp, "EvalTest18"));
	    		cfgs.add(createLaunchConfiguration(jp, "FunctionalCaptureTest18"));
	    		cfgs.add(createLaunchConfiguration(jp, "EvalTestIntf18"));
				cfgs.add(createLaunchConfiguration(jp, "EvalIntfSuperDefault"));
				cfgs.add(createLaunchConfiguration(jp, "DebugHoverTest18"));
				cfgs.add(createLaunchConfiguration(jp, "DebugHoverTest2Lambdas"));
				cfgs.add(createLaunchConfiguration(jp, "Bug317045"));
				cfgs.add(createLaunchConfiguration(jp, "Bug549394"));
				cfgs.add(createLaunchConfiguration(jp, "Bug541110"));
				cfgs.add(createLaunchConfiguration(jp, "ClosureVariableTest_Bug542989"));
				cfgs.add(createLaunchConfiguration(jp, "Bug404097BreakpointInLocalClass"));
				cfgs.add(createLaunchConfiguration(jp, "Bug404097BreakpointInAnonymousLocalClass"));
				cfgs.add(createLaunchConfiguration(jp, "Bug404097BreakpointInLambda"));
				cfgs.add(createLaunchConfiguration(jp, "Bug404097BreakpointUsingInnerClass"));
				cfgs.add(createLaunchConfiguration(jp, "Bug404097BreakpointUsingLocalClass"));
				cfgs.add(createLaunchConfiguration(jp, "Bug560392"));
				cfgs.add(createLaunchConfiguration(jp, "Bug561715"));
				cfgs.add(createLaunchConfiguration(jp, "Bug562056"));
				cfgs.add(createLaunchConfiguration(jp, "RemoteEvaluator"));
				cfgs.add(createLaunchConfiguration(jp, "AnonymousEvaluator"));
				cfgs.add(createLaunchConfiguration(jp, "Bug564486"));
				cfgs.add(createLaunchConfiguration(jp, "Bug564801"));
				cfgs.add(createLaunchConfiguration(jp, "Bug567801"));
				cfgs.add(createLaunchConfiguration(jp, "Bug571230"));
				cfgs.add(createLaunchConfiguration(jp, "Bug572629"));
				cfgs.add(createLaunchConfiguration(jp, "Bug569413"));
				cfgs.add(createLaunchConfiguration(jp, "Bug573589"));
				cfgs.add(createLaunchConfiguration(jp, "Bug574395"));
				cfgs.add(createLaunchConfiguration(jp, "Bug571310"));
				cfgs.add(createLaunchConfiguration(jp, "Bug573547"));
				cfgs.add(createLaunchConfiguration(jp, "Bug575551"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145NestedLambda"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145LambdaInConstructor"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145LambdaInFieldDeclaration"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145LambdaInStaticInitializer"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145LambdaInAnonymous"));
				cfgs.add(createLaunchConfiguration(jp, "Bug578145LambdaOnChainCalls"));
				cfgs.add(createLaunchConfiguration(jp, "LambdaBreakpoints1"));
				cfgs.add(createLaunchConfiguration(jp, "GH275"));
				cfgs.add(createLaunchConfiguration(jp, "LambdaTest"));
	    		loaded18 = true;
	    		waitForBuild();
	        }
        }
        catch(Exception e) {
        	try {
        		if(jp != null) {
		        	jp.getProject().delete(true,  true, null);
		        	for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, ONE_EIGHT_PROJECT_NAME, jp);
        }
	}

	/**
	 * Creates the Java 9 compliant project
	 */
	synchronized void assert9Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded9) {
				jp = createProject(NINE_PROJECT_NAME, JavaProjectHelper.TEST_9_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_9_EE_NAME, false);
				cfgs.add(createLaunchConfiguration(jp, "LogicalStructures"));
				cfgs.add(createLaunchConfiguration(jp, "Bug575039"));
				loaded9 = true;
				waitForBuild();
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, NINE_PROJECT_NAME, jp);
		}
	}

	/**
	 * Creates the Java 16 compliant project
	 */
	synchronized void assert16_Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded16_) {
				jp = createProject(ONESIX_PROJECT_NAME, JavaProjectHelper.TEST_16_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_16_EE_NAME, false);
				cfgs.add(createLaunchConfiguration(jp, "RecordTests"));
				loaded16_ = true;
				waitForBuild();
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, ONESIX_PROJECT_NAME, jp);
		}
	}

	/**
	 * Creates the Java 21 compliant project
	 */
	synchronized void assert21Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded21) {
				jp = createProject(TWENTYONE_PROJECT_NAME, JavaProjectHelper.TEST_21_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_21_EE_NAME, false);
				jp.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				cfgs.add(createLaunchConfiguration(jp, "Main1"));
				cfgs.add(createLaunchConfiguration(jp, "Main2"));
				loaded21 = true;
				waitForBuild();
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, TWENTYONE_PROJECT_NAME, jp);
		}
	}

	synchronized void assertMRProject() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loadedMR) {
				jp = createProject(MR_PROJECT_NAME, JavaProjectHelper.TEST_MR_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_21_EE_NAME, false);
				jp.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
				jp.setOption(JavaCore.COMPILER_COMPLIANCE, "11");
				jp.setOption(JavaCore.COMPILER_SOURCE, "11");
				jp.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "11");
				IPackageFragmentRoot src17 = JavaProjectHelper.addSourceContainer(jp, "src17", JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "17"));
				IPackageFragmentRoot src21 = JavaProjectHelper.addSourceContainer(jp, "src21", JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "21"));
				File root = JavaTestPlugin.getDefault().getFileInPlugin(JavaProjectHelper.TEST_MR_SRC_DIR);
				JavaProjectHelper.importFilesFromDirectory(new File(root, src17.getPath().lastSegment()), src17.getPath(), null);
				JavaProjectHelper.importFilesFromDirectory(new File(root, src21.getPath().lastSegment()), src21.getPath(), null);
				cfgs.add(createLaunchConfiguration(jp, "p.Main"));
				loadedMR = true;
				waitForBuild();
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, MR_PROJECT_NAME, jp);
		}
	}

	synchronized void assert23Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded23) {
				jp = createProject(TWENTYTHREE_PROJECT_NAME, JavaProjectHelper.TEST_23_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_23_EE_NAME, false);
				jp.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_23);
				jp.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_23);
				cfgs.add(createLaunchConfiguration(jp, "Main21"));
				loaded23 = true;
				waitForBuild();
				assertNoErrorMarkersExist(jp.getProject());
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, TWENTYTHREE_PROJECT_NAME, jp);
		}
	}

	/**
	 * Creates the Java 24 compliant project
	 */
	synchronized void assert24Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded24) {
				jp = createProject(TWENTYFOUR_PROJECT_NAME, JavaProjectHelper.TEST_24_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_24_EE_NAME, false);
				jp.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				jp.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_24);
				jp.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_24);
				jp.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_24);
				cfgs.add(createLaunchConfiguration(jp, "Main21"));
				loaded24 = true;
				waitForBuild();
				assertNoErrorMarkersExist(jp.getProject());
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, TWENTYFOUR_PROJECT_NAME, jp);
		}
	}

	/**
	 * Creates the Java 25 compliant project
	 */
	synchronized void assert25Project() {
		IJavaProject jp = null;
		ArrayList<ILaunchConfiguration> cfgs = new ArrayList<>(1);
		try {
			if (!loaded25) {
				jp = createProject(TWENTYFIVE_PROJECT_NAME, JavaProjectHelper.TEST_25_SRC_DIR.toString(), JavaProjectHelper.JAVA_SE_25_EE_NAME, false);
				jp.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				jp.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_25);
				jp.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_25);
				jp.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_25);
				cfgs.add(createLaunchConfiguration(jp, "Main1"));
				cfgs.add(createLaunchConfiguration(jp, "Main2"));
				loaded25 = true;
				waitForBuild();
				assertNoErrorMarkersExist(jp.getProject());
			}
		} catch (Exception e) {
			try {
				if (jp != null) {
					jp.getProject().delete(true, true, null);
					for (int i = 0; i < cfgs.size(); i++) {
						cfgs.get(i).delete();
					}
				}
			} catch (CoreException ce) {
				// ignore
			}
			handleProjectCreationException(e, TWENTYFIVE_PROJECT_NAME, jp);
		}
	}

	/**
	 * Creates the 'BoundJRE' project used for the JRE testing
	 */
	synchronized void assertBoundJreProject() {
		IJavaProject jp = null;
		try {
	        if (!loadedJRE) {
		        jp =JavaProjectHelper.createJavaProject(BOUND_JRE_PROJECT_NAME);
		        JavaProjectHelper.addSourceContainer(jp, JavaProjectHelper.SRC_DIR, JavaProjectHelper.BIN_DIR);
		        // add VM specific JRE container
		        IPath path = JavaRuntime.newJREContainerPath(JavaRuntime.getDefaultVMInstall());
		        JavaProjectHelper.addContainerEntry(jp, path);
		        loadedJRE = true;
		        waitForBuild();
	        }
		}
		catch(Exception e) {
        	try {
        		if(jp != null) {
        			jp.getProject().delete(true,  true, null);
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, BOUND_JRE_PROJECT_NAME, jp);
        }
	}

	/**
	 * Creates the 'BoundEE' project for EE testing
	 */
	void assertBoundeEeProject() {
		IJavaProject jp = null;
		try {
	        if(!loadedEE) {
		        // create project with two src folders and output locations
		        jp = JavaProjectHelper.createJavaProject(BOUND_EE_PROJECT_NAME);
		        JavaProjectHelper.addSourceContainer(jp, JavaProjectHelper.SRC_DIR, JavaProjectHelper.BIN_DIR);

		        // add VM specific JRE container
				IExecutionEnvironment javase1_8 = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(JavaProjectHelper.JAVA_SE_1_8_EE_NAME);
				assertNotNull("Missing JavaSE-1.8 environment", javase1_8);
				IPath path = JavaRuntime.newJREContainerPath(javase1_8);
		        JavaProjectHelper.addContainerEntry(jp, path);
		        loadedEE = true;
		        waitForBuild();
	        }
		}
		catch(Exception e) {
        	try {
        		if(jp != null) {
        			jp.getProject().delete(true,  true, null);
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, BOUND_EE_PROJECT_NAME, jp);
        }
	}

	/**
	 * Creates the 'MultiOutput' project for source / binary output testing
	 */
	synchronized void assertMultioutputProject() {
		IJavaProject jp = null;
		try {
	        if(!loadedMulti) {
		        // create project with two src folders and output locations
		        jp = JavaProjectHelper.createJavaProject(MULTI_OUTPUT_PROJECT_NAME);
		        JavaProjectHelper.addSourceContainer(jp, "src1", "bin1");
		        JavaProjectHelper.addSourceContainer(jp, "src2", "bin2");

		        // add rt.jar
		        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		        assertNotNull("No default JRE", vm);
		        JavaProjectHelper.addContainerEntry(jp, new Path(JavaRuntime.JRE_CONTAINER));
		        loadedMulti = true;
		        waitForBuild();
	        }
		}
		catch(Exception e) {
        	try {
        		if(jp != null) {
        			jp.getProject().delete(true,  true, null);
        		}
        	}
        	catch (CoreException ce) {
        		//ignore
			}
			handleProjectCreationException(e, MULTI_OUTPUT_PROJECT_NAME, jp);
        }
	}

	/**
	 * Ensure the welcome screen is closed because in 4.x the debug perspective opens a giant fast-view causing issues
	 *
	 * @since 3.8
	 */
	protected final void assertWelcomeScreenClosed() throws Exception {
		if(!welcomeClosed && PlatformUI.isWorkbenchRunning()) {
			final IWorkbench wb = PlatformUI.getWorkbench();
			if (wb == null) {
				return;
			}
			// In UI thread we don't need to run a job
			if (Display.getCurrent() != null) {
				closeIntro(wb);
				return;
			}

			UIJob job = new UIJob("close welcome screen for debug test suite") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					closeIntro(wb);
					return Status.OK_STATUS;
				}

			};
			job.setPriority(Job.INTERACTIVE);
			job.setSystem(true);
			job.schedule();
		}
	}

	private static void closeIntro(final IWorkbench wb) {
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		if (window != null) {
			IIntroManager im = wb.getIntroManager();
			IIntroPart intro = im.getIntro();
			if (intro != null) {
				welcomeClosed = im.closeIntro(intro);
			}
		}
	}

	protected void handleProjectCreationException(Exception e, String pname, IJavaProject jp) {
		StringWriter buf = new StringWriter();
		String msg = e.getMessage();
    	if(msg == null) {
			msg = "could not acquire message for exception class: " + e.getClass();
    	}
		buf.write("Failed to create the '" + pname + "' test project.\n");
		buf.write("'jp' is " + (jp != null ? "not " : "") + " 'null'\n");
		buf.write("Stack tace:\n");
		e.printStackTrace(new PrintWriter(buf));
		fail(buf.toString());
	}

	/**
	 * Sets the contents of the given {@link ICompilationUnit} to be the new contents provided
	 * @param contents the new {@link String} contents, cannot be <code>null</code>
	 */
	protected void setFileContents(ICompilationUnit unit, String contents) throws JavaModelException {
		assertNotNull("You cannot set the new contents of an ICompilationUnit to null", contents);
		IBuffer buffer = unit.getBuffer();
		buffer.setContents(contents);
		unit.save(null, true);
		waitForBuild();
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
	 * Returns the singleton instance of the <code>LaunchConfigurationManager</code>
	 *
	 * @return the singleton instance of the <code>LaunchConfigurationManager</code>
	 * @since 3.3
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
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
	 * Returns the project context for the current test - each
	 * test must implement this method
	 */
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

	/**
	 * Returns the 'DebugTests' project.
	 *
	 * @return the test project
	 */
	protected IJavaProject get16_Project() {
		assert16_Project();
		return getJavaProject(ONESIX_PROJECT_NAME);
	}

	/**
	 * Returns the 'DebugTests' project.
	 *
	 * @return the test project
	 */
	protected IJavaProject get14Project() {
		assert14Project();
		return getJavaProject(ONE_FOUR_PROJECT_NAME);
	}

	/**
	 * Returns the {@link IBreakpointOrganizer} with the given id or <code>null</code>
	 * if no such organizer exists
	 * @return the {@link IBreakpointOrganizer} or <code>null</code>
	 * @since 3.8.100
	 */
	protected IBreakpointOrganizer getOrganizer(String id) {
		return BreakpointOrganizerManager.getDefault().getOrganizer(id);
	}

	/**
	 * Returns the 'OneFive' project.
	 *
	 * @return the test project
	 */
	protected IJavaProject get15Project() {
		assert15Project();
		return getJavaProject(ONE_FIVE_PROJECT_NAME);
	}

	/**
	 * Returns the 'OneSeven' project.
	 *
	 * @return the test project
	 */
	protected IJavaProject get17Project() {
		assert17Project();
		return getJavaProject(ONE_SEVEN_PROJECT_NAME);
	}

	/**
	 * Returns the 'OneEight' project.
	 *
	 * @return the test project
	 */
	protected IJavaProject get18Project() {
		assert18Project();
		return getJavaProject(ONE_EIGHT_PROJECT_NAME);
	}

	/**
	 * Returns the 'Nine' project, used for Java 9 tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject get9Project() {
		assert9Project();
		return getJavaProject(NINE_PROJECT_NAME);
	}

	 /**
	 * Returns the 'Two_One' project, used for Java 21 tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject get21Project() {
		assert21Project();
		return getJavaProject(TWENTYONE_PROJECT_NAME);
	}

	/**
	 * Returns the 'multirelease' project, used for Multirelease tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject getMultireleaseProject() {
		assertMRProject();
		return getJavaProject(MR_PROJECT_NAME);
	}

	/**
	 * Returns the 'Two_Three' project, used for Java 23 tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject get23Project() {
		assert23Project();
		return getJavaProject(TWENTYTHREE_PROJECT_NAME);
	}

	/**
	 * Returns the 'Two_Four' project, used for Java 24 tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject get24Project() {
		assert24Project();
		return getJavaProject(TWENTYFOUR_PROJECT_NAME);
	}

	/**
	 * Returns the 'Two_Five' project, used for Java 25 tests.
	 *
	 * @return the test project
	 */
	protected IJavaProject get25Project() {
		assert25Project();
		return getJavaProject(TWENTYFIVE_PROJECT_NAME);
	}

	/**
	 * Returns the 'BoundJRE' project
	 *
	 * @return the test project
	 */
	protected IJavaProject getBoundJreProject() {
		assertBoundJreProject();
		return getJavaProject(BOUND_JRE_PROJECT_NAME);
	}

	/**
	 * Returns the 'BoundEE' project
	 *
	 * @return the test project
	 */
	protected IJavaProject getBoundEeProject() {
		assertBoundeEeProject();
		return getJavaProject(BOUND_EE_PROJECT_NAME);
	}

	/**
	 * Returns the 'MultiOutput' project
	 *
	 * @return the test project
	 */
	protected IJavaProject getMultiOutputProject() {
		assertMultioutputProject();
		return getJavaProject(MULTI_OUTPUT_PROJECT_NAME);
	}

	/**
	 * Returns the Java project with the given name.
	 *
	 * @param name project name
	 * @return the Java project with the given name
	 */
	protected IJavaProject getJavaProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		return JavaCore.create(project);
	}

	/**
	 * Creates a new {@link IJavaProject} with the given name and optionally initializing it from the given resource path from the testing bundle.
	 * <br>
	 * <br>
	 * The project has the default <code>src</code> and <code>bin</code> folders. It is also created with a default <code>launchConfigurations</code>
	 * folder.
	 *
	 * @param name
	 *            the name for the project
	 * @param contentpath
	 *            the path within the jdt.debug test bundle to initialize the source from
	 * @param ee
	 *            the level of execution environment to use
	 * @param delete
	 *            if an existing project should be deleted
	 * @return the new Java project
	 */
	protected IJavaProject createProject(String name, String contentpath, String ee, boolean delete) throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (pro.exists() && delete) {
        	try {
        		pro.delete(true, true, null);
        	}
        	catch(Exception e) {}
        }
        // create project and import source
        IJavaProject jp = JavaProjectHelper.createJavaProject(name, JavaProjectHelper.BIN_DIR);
        IPackageFragmentRoot src = JavaProjectHelper.addSourceContainer(jp, JavaProjectHelper.SRC_DIR);
        File root = JavaTestPlugin.getDefault().getFileInPlugin(new Path(contentpath));
        File srcInRoot = new File(root, src.getPath().lastSegment());
        if (srcInRoot.isDirectory()) {
            JavaProjectHelper.importFilesFromDirectory(srcInRoot, src.getPath(), null);
        } else {
            JavaProjectHelper.importFilesFromDirectory(root, src.getPath(), null);
        }

        // add the EE library
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        assertNotNull("No default JRE", vm);
        IExecutionEnvironment environment = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(ee);
        assertNotNull("The EE ["+ee+"] does not exist", environment);
		IPath containerPath = JavaRuntime.newJREContainerPath(environment);
        JavaProjectHelper.addContainerEntry(jp, containerPath);
        pro = jp.getProject();

        JavaProjectHelper.updateCompliance(jp, ee);

        // create launch configuration folder
        IFolder folder = pro.getFolder("launchConfigurations");
        if (!folder.exists()) {
        	folder.create(true, true, null);
        }
        return jp;
	}

	/**
	 * Creates a new {@link IJavaProject} with the given name and initializes the contents from the given resource path from the testing bundle. <br>
	 * <br>
	 * The project has the default <code>src</code> and <code>bin</code> folders. It is also created with a default <code>launchConfigurations</code>
	 * folder.
	 *
	 * @param name
	 *            the name for the project
	 * @param contentpath
	 *            the path within the jdt.debug test bundle to initialize the source from
	 * @param ee
	 *            the level of execution environment to use
	 * @param delete
	 *            if an existing project should be deleted
	 * @return the new Java project
	 */
	protected IJavaProject createJavaProjectClone(String name, String contentpath, String ee, boolean delete) throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		String owner = "Creating project: " + name;
        if (pro.exists() && delete) {
			pro.delete(true, true, null);
			TestUtil.waitForJobs(owner, 100, 10000);
			TestUtil.runEventLoop();
        }
        // create project and import source
        IJavaProject jp = JavaProjectHelper.createJavaProject(name, JavaProjectHelper.BIN_DIR);
		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();

        JavaProjectHelper.addSourceContainer(jp, JavaProjectHelper.SRC_DIR);
		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();

        File root = JavaTestPlugin.getDefault().getFileInPlugin(new Path(contentpath));
        JavaProjectHelper.importFilesFromDirectory(root, jp.getPath(), null);

		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();

        // add the EE library
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        assertNotNull("No default JRE", vm);
        IExecutionEnvironment environment = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(ee);
        assertNotNull("The EE ["+ee+"] does not exist", environment);
		IPath containerPath = JavaRuntime.newJREContainerPath(environment);
        JavaProjectHelper.addContainerEntry(jp, containerPath);
        pro = jp.getProject();

        // create launch configuration folder
        IFolder folder = pro.getFolder("launchConfigurations");
        if (!folder.exists()) {
        	folder.create(true, true, null);
        }
		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();
        return jp;
	}

	/**
	 * Creates a new {@link IProject} with the given name and initializes the contents from the given resource path from the testing bundle.
	 *
	 * @param name
	 *            the name for the project
	 * @param contentpath
	 *            the path within the jdt.debug test bundle to initialize the source from
	 * @param delete
	 *            if an existing project should be deleted
	 * @return the new project
	 */
	protected IProject createProjectClone(String name, String contentpath, boolean delete) throws Exception {
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		String owner = "Creating project: " + name;
        if (pro.exists() && delete) {
			pro.delete(true, true, null);
			TestUtil.waitForJobs(owner, 100, 10000);
			TestUtil.runEventLoop();
        }
        // create project and import source
        IProject pj = JavaProjectHelper.createProject(name);
		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();

        File root = JavaTestPlugin.getDefault().getFileInPlugin(new Path(contentpath));
        JavaProjectHelper.importFilesFromDirectory(root, pj.getFullPath(), null);
		TestUtil.waitForJobs(owner, 100, 10000);
		TestUtil.runEventLoop();
        return pj;
	}

	/**
	 * Returns the launch shortcut with the given id
	 * @return the <code>LaunchShortcutExtension</code> with the given id,
	 * or <code>null</code> if none
	 *
	 * @since 3.3
	 */
	protected LaunchShortcutExtension getLaunchShortcutExtension(String id) {
		List<?> exts = getLaunchConfigurationManager().getLaunchShortcuts();
		LaunchShortcutExtension ext = null;
		for (int i = 0; i < exts.size(); i++) {
			ext = (LaunchShortcutExtension) exts.get(i);
			if(ext.getId().equals(id)) {
				return ext;
			}
		}
		return null;
	}

	/**
	 * New to 3.3 is the ability to have multiple delegates for a variety of overlapping mode combinations.
	 * As such, for tests that launch specific configurations, must be check to ensure that there is a preferred
	 * launch delegate available for the launch in the event there are duplicates. Otherwise the tests
	 * will hang waiting for a user to select a resolution action.
	 * @throws CoreException
	 *
	 * @since 3.3
	 */
	protected void ensurePreferredDelegate(ILaunchConfiguration configuration, Set<String> modes) throws CoreException {
		ILaunchConfigurationType type = configuration.getType();
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		if(delegates.length > 1) {
			type.setPreferredDelegate(modes, getDelegateById(type.getIdentifier(), LOCAL_JAVA_APPLICATION_TYPE_ID));
		}
	}

	/**
	 * Returns the LaunchDelegate for the specified ID
	 * @param delegateId the id of the delegate to search for
	 * @return the <code>LaunchDelegate</code> associated with the specified id or <code>null</code> if not found
	 * @since 3.3
	 */
	protected ILaunchDelegate getDelegateById(String typeId, String delegateId) {
		LaunchManager lm = (LaunchManager) getLaunchManager();
		LaunchDelegate[] delegates = lm.getLaunchDelegates(typeId);
		for(int i = 0; i < delegates.length; i++) {
			if(delegates[i].getId().equals(delegateId)) {
				return delegates[i];
			}
		}
		return null;
	}

	/**
	 * Returns the source folder with the given name in the given project.
	 *
	 * @param name source folder name
	 * @return package fragment root
	 */
	protected IPackageFragmentRoot getPackageFragmentRoot(IJavaProject project, String name) {
		IProject p = project.getProject();
		return project.getPackageFragmentRoot(p.getFolder(name));
	}

	/**
	 * Returns the <code>IHyperLink</code> at the given offset in the specified document
	 * or <code>null</code> if the offset does not point to an <code>IHyperLink</code>
	 * @return the <code>IHyperLink</code> at the given offset or <code>null</code>
	 */
	protected IHyperlink getHyperlink(int offset, IDocument doc) {
		if (offset >= 0 && doc != null) {
			Position[] positions = null;
			try {
				positions = doc.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
			} catch (BadPositionCategoryException ex) {
				// no links have been added
				return null;
			}
			for (int i = 0; i < positions.length; i++) {
				Position position = positions[i];
				if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
					return ((ConsoleHyperlinkPosition)position).getHyperLink();
				}
			}
		}
		return null;
	}

	/**
	 * Launches the given configuration and waits for an event. Returns the source of the event. If the event is not received, the launch is
	 * terminated and an exception is thrown.
	 *
	 * @param configuration
	 *            the configuration to launch
	 * @param waiter
	 *            the event waiter to use
	 * @return Object the source of the event
	 * @throws CoreException
	 *             if the event is never received.
	 */
	protected Object launchAndWait(ILaunchConfiguration configuration, DebugEventWaiter waiter) throws CoreException {
	    return launchAndWait(configuration, waiter, true);
	}

	/**
	 * Launches the given configuration in debug mode and waits for an event. Returns the source of the event. If the event is not received, the
	 * launch is terminated and an exception is thrown.
	 *
	 * @param configuration
	 *            the configuration to launch
	 * @param waiter
	 *            the event waiter to use
	 * @param register
	 *            whether to register the launch
	 * @return Object the source of the event
	 * @throws CoreException
	 *             if the event is never received.
	 */
	protected Object launchAndWait(ILaunchConfiguration configuration, DebugEventWaiter waiter, boolean register) throws CoreException {
		return launchAndWait(configuration, ILaunchManager.DEBUG_MODE, waiter, register);
	}

	/**
	 * Launches the given configuration and waits for an event. Returns the source of the event. If the event is not received, the launch is
	 * terminated and an exception is thrown.
	 *
	 * @param configuration
	 *            the configuration to launch
	 * @param mode
	 *            the mode to launch the configuration in
	 * @param waiter
	 *            the event waiter to use
	 * @param register
	 *            whether to register the launch
	 * @return Object the source of the event
	 * @throws CoreException
	 *             if the event is never received.
	 */
	protected Object launchAndWait(ILaunchConfiguration configuration, String mode, DebugEventWaiter waiter, boolean register) throws CoreException {
		ILaunch launch = configuration.launch(mode, new TimeoutMonitor(DEFAULT_TIMEOUT), false, register);
		Object suspendee= waiter.waitForEvent();
		if (suspendee == null) {
			StringBuilder buf = new StringBuilder();
            buf.append("Test case: "); //$NON-NLS-1$
            buf.append(getName());
            buf.append("\n"); //$NON-NLS-1$
            buf.append("Never received event: "); //$NON-NLS-1$
            buf.append(waiter.getEventKindName());
            buf.append("\n"); //$NON-NLS-1$
            if (launch.isTerminated()) {
            	buf.append("Process exit value: "); //$NON-NLS-1$
            	buf.append(launch.getProcesses()[0].getExitValue());
                buf.append("\n"); //$NON-NLS-1$
            }
            IConsole console = DebugUITools.getConsole(launch.getProcesses()[0]);
            if (console instanceof TextConsole) {
                TextConsole textConsole = (TextConsole)console;
                String string = textConsole.getDocument().get();
                buf.append("Console output follows:\n"); //$NON-NLS-1$
                buf.append(string);
            }
            buf.append("\n"); //$NON-NLS-1$
            DebugPlugin.log(new Status(IStatus.ERROR, "org.eclipse.jdt.debug.ui.tests", buf.toString())); //$NON-NLS-1$
			try {
				launch.terminate();
			} catch (CoreException e) {
				e.printStackTrace();
				fail("Program did not suspend, and unable to terminate launch."); //$NON-NLS-1$
			}
			throw new TestAgainException("Program did not suspend, launch terminated.");
		}
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend, launch terminated.", suspendee); //$NON-NLS-1$
		// Bug 575131: resuming and suspending threads on breakpoint hit is done in a job, wait for this job
		try {
			Job.getJobManager().join(JDIDebugTarget.class, new NullProgressMonitor());
		} catch (OperationCanceledException | InterruptedException e) {
			throw new AssertionError("Failed ot wait for JDIDebugTarget jobs", e);
		}
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
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config); //$NON-NLS-1$
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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());
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
		return launchToBreakpoint(getProjectContext(), mainTypeName);
	}

	/**
	 * Launches the type with the given name, and waits for a breakpoint-caused
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 *
	 * @param project the project the type is in
	 * @param mainTypeName the program to launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(IJavaProject project, String mainTypeName) throws Exception {
		return launchToBreakpoint(project, mainTypeName, true);
	}

	/**
	 * Launches the type with the given name, and waits for a breakpoint-caused
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 *
	 * @param mainTypeName the program to launch
	 * @param register whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(String mainTypeName, boolean register) throws Exception {
		return launchToBreakpoint(getProjectContext(), mainTypeName, register);
	}

	/**
	 * Launches the type with the given name, and waits for a breakpoint-caused
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 *
	 * @param mainTypeName the program to launch
	 * @param register whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(IJavaProject project, String mainTypeName, boolean register) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(project, mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config); //$NON-NLS-1$
		return launchToBreakpoint(config, register);
	}

	/**
	 * Launches the type with the given name, and waits for a breakpoint-caused
	 * suspend event in that program. Returns the thread in which the suspend
	 * event occurred.
	 *
	 * @param mainTypeName the program to launch
	 * @param register whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(IJavaProject project, String mainTypeName, String launchName, boolean register) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(project, launchName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config); //$NON-NLS-1$
		return launchToBreakpoint(config, register);
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
	    return launchToBreakpoint(config, true);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a breakpoint-caused suspend event in that program. Returns the thread in which
	 * the suspend event occurred.
	 *
	 * @param config
	 *            the configuration to launch
	 * @param register
	 *            whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToBreakpoint(ILaunchConfiguration config, boolean register) throws CoreException {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		Object suspendee= launchAndWait(config, waiter, register);
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread); //$NON-NLS-1$
		return (IJavaThread)suspendee;
	}

	/**
	 * Launches the type with the given name, and waits for a terminate
	 * event in that program. Returns the debug target in which the suspend
	 * event occurred.
	 *
	 * @param mainTypeName the program to launch
	 * @return debug target in which the terminate event occurred
	 */
	protected IJavaDebugTarget launchAndTerminate(String mainTypeName) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config); //$NON-NLS-1$
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
		return launchAndTerminate(config, timeout, true);
	}

	/**
	 * Launches the given configuration in debug mode, and waits for a terminate
	 * event in that program. Returns the debug target in which the terminate
	 * event occurred.
	 *
	 * @param config the configuration to launch
	 * @param timeout the number of milliseconds to wait for a terminate event
	 * @param register whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaDebugTarget launchAndTerminate(ILaunchConfiguration config, int timeout, boolean register) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.TERMINATE, IJavaDebugTarget.class);
		waiter.setTimeout(timeout);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		Object terminatee = launchAndWait(config, waiter, register);
		assertNotNull("Program did not terminate.", terminatee); //$NON-NLS-1$
		assertTrue("terminatee is not an IJavaDebugTarget", terminatee instanceof IJavaDebugTarget); //$NON-NLS-1$
		IJavaDebugTarget debugTarget = (IJavaDebugTarget) terminatee;
		assertTrue("debug target is not terminated", debugTarget.isTerminated() || debugTarget.isDisconnected()); //$NON-NLS-1$
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
		return launchToLineBreakpoint(mainTypeName, bp, true);
	}

	/**
	 * Launches the type with the given name, and waits for a line breakpoint suspend
	 * event in that program. Returns the thread in which the suspend
	 * event occurred.
	 *
	 * @param mainTypeName the program to launch
	 * @param bp the breakpoint that should cause a suspend event
	 * @param register whether to register the launch
	 * @return thread in which the first suspend event occurred
	 */
	protected IJavaThread launchToLineBreakpoint(String mainTypeName, ILineBreakpoint bp, boolean register) throws Exception {
		ILaunchConfiguration config = getLaunchConfiguration(mainTypeName);
		assertNotNull("Could not locate launch configuration for " + mainTypeName, config); //$NON-NLS-1$
		return launchToLineBreakpoint(config, bp, register);
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
	protected IJavaThread launchToLineBreakpoint(ILaunchConfiguration config, ILineBreakpoint bp, boolean register) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		Object suspendee= launchAndWait(config, waiter, register);
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread); //$NON-NLS-1$
		IJavaThread thread = (IJavaThread) suspendee;
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("suspended, but not by breakpoint", hit); //$NON-NLS-1$
		assertEquals("hit un-registered breakpoint", bp, hit); //$NON-NLS-1$
		assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint); //$NON-NLS-1$
		ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
		int lineNumber = breakpoint.getLineNumber();
		int stackLine = thread.getTopStackFrame().getLineNumber();
		assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine); //$NON-NLS-1$
		return thread;
	}

	/**
	 * Returns the standard java launch tab group
	 * @return the standard java launch tab group
	 * @throws CoreException
	 *
	 * @since 3.3
	 */
	protected ILaunchConfigurationTabGroup getJavaLaunchGroup() throws CoreException {
		ILaunchConfigurationType javaType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationTabGroup standardGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(javaType, ILaunchManager.DEBUG_MODE);
		return standardGroup;
	}

	/**
	 * Returns an instance of the launch configuration dialog on the the specified launch mode
	 * @param modeid the id of the mode to open the launch dialog on
	 * @return an new instance of <code>IlaunchConfigurationDialog</code>
	 *
	 * @since 3.3
	 */
	protected ILaunchConfigurationDialog getLaunchConfigurationDialog(String modeid) {
		return new LaunchConfigurationsDialog(null, DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(modeid));
	}

	/**
	 * Resumes the given thread, and waits for another breakpoint-caused suspend event.
	 * Returns the thread in which the suspend event occurs.
	 *
	 * @param thread thread to resume
	 * @return thread in which the first suspend event occurs
	 */
	protected IJavaThread resume(IJavaThread thread) throws Exception {
	    return resume(thread, DEFAULT_TIMEOUT);
	}

	/**
	 * Resumes the given thread, and waits for another breakpoint-caused suspend event.
	 * Returns the thread in which the suspend event occurs.
	 *
	 * @param thread thread to resume
	 * @param timeout timeout in milliseconds
	 * @return thread in which the first suspend event occurs
	 */
	protected IJavaThread resume(IJavaThread thread, int timeout) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(timeout);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread)suspendee;
	}

	/**
	 * Resumes the given thread, and waits for a suspend event caused by the specified line breakpoint. Returns the thread in which the suspend event
	 * occurs.
	 *
	 * @param resumeThread
	 *            thread to resume
	 * @return thread in which the first suspend event occurs
	 */
	protected IJavaThread resumeToLineBreakpoint(IJavaThread resumeThread, ILineBreakpoint bp) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		resumeThread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		assertTrue("suspendee was not an IJavaThread", suspendee instanceof IJavaThread); //$NON-NLS-1$
		IJavaThread thread = (IJavaThread) suspendee;
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("suspended, but not by breakpoint", hit); //$NON-NLS-1$
		assertEquals("hit un-registered breakpoint", bp, hit); //$NON-NLS-1$
		assertTrue("suspended, but not by line breakpoint", hit instanceof ILineBreakpoint); //$NON-NLS-1$
		ILineBreakpoint breakpoint= (ILineBreakpoint) hit;
		int lineNumber = breakpoint.getLineNumber();
		int stackLine = thread.getTopStackFrame().getLineNumber();
		assertEquals("line numbers of breakpoint and stack frame do not match", lineNumber, stackLine); //$NON-NLS-1$

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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not terminate.", suspendee); //$NON-NLS-1$
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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		thread.resume();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not terminate.", suspendee); //$NON-NLS-1$
		IJavaDebugTarget target = (IJavaDebugTarget)suspendee;
		assertTrue("program should have exited", target.isTerminated() || target.isDisconnected()); //$NON-NLS-1$
		return target;
	}

	/**
	 * Returns the launch configuration for the given main type
	 *
	 * @param mainTypeName program to launch
	 * @see ProjectCreationDecorator
	 */
	protected ILaunchConfiguration getLaunchConfiguration(String mainTypeName) {
		return getLaunchConfiguration(getProjectContext(), mainTypeName);
	}

	/**
	 * Returns the launch configuration for the given main type
	 *
	 * @param mainTypeName program to launch
	 * @see ProjectCreationDecorator
	 */
	protected ILaunchConfiguration getLaunchConfiguration(IJavaProject project, String mainTypeName) {
		IFile file = project.getProject().getFolder(LAUNCHCONFIGURATIONS).getFile(mainTypeName + LAUNCH_EXTENSION);
		ILaunchConfiguration config = getLaunchManager().getLaunchConfiguration(file);
		assertNotNull("the configuration cannot be null", config);
		assertTrue("Could not find launch configuration for " + mainTypeName, config.exists()); //$NON-NLS-1$
		return config;
	}

	/**
	 * Returns the launch configuration in the specified folder in the given project, for the given main type
	 *
	 * @param project the project to look in
	 * @param containername the name of the container in the specified project to look for the config
	 * @param mainTypeName program to launch
	 * @see ProjectCreationDecorator
	 */
	protected ILaunchConfiguration getLaunchConfiguration(IJavaProject project, String containername, String mainTypeName) {
		IFile file = project.getProject().getFolder(containername).getFile(mainTypeName + LAUNCH_EXTENSION);
		ILaunchConfiguration config = getLaunchManager().getLaunchConfiguration(file);
		assertNotNull("the configuration cannot be null", config);
		assertTrue("Could not find launch configuration for " + mainTypeName, config.exists()); //$NON-NLS-1$
		return config;
	}

	/**
	 * Returns the corresponding <code>IResource</code> from the <code>IJavaElement</code> with the
	 * specified name
	 * @param typeName the name of the <code>IJavaElement</code> to get the resource for
	 * @return the corresponding <code>IResource</code> from the <code>IJavaElement</code> with the
	 * specified name
	 */
	protected IResource getBreakpointResource(String typeName) throws Exception {
		IJavaElement element = getProjectContext().findElement(new Path(typeName + JAVA_EXTENSION));
		IResource resource = element.getCorrespondingResource();
		if (resource == null) {
			resource = getProjectContext().getProject();
		}
		return resource;
	}

	/**
	 * Returns the resource from the specified type or the project from the testing java project in the
	 * event there is no resource from the specified type
	 */
	protected IResource getBreakpointResource(IType type) throws Exception {
		if (type == null) {
			return getProjectContext().getProject();
		}
		IResource resource = type.getResource();
		if (resource == null) {
			resource = type.getJavaProject().getProject();
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
		IType type = getType(typeName);
		assertNotNull("Could not find the requested IType: "+typeName, type);
		return createLineBreakpoint(type, lineNumber);
	}

	/**
	 * Creates am  new java line breakpoint
	 * @return a new line breakpoint
	 */
	protected IJavaLineBreakpoint createLineBreakpoint(int lineNumber, String root, String packageName, String cuName,
			String fullTargetName) throws Exception{
		IJavaProject javaProject = getProjectContext();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		assertNotNull("did not find requested Compilation Unit", cunit); //$NON-NLS-1$
		IType targetType = (IType)(new MemberParser()).getDeepest(cunit,fullTargetName);
		assertNotNull("did not find requested type", targetType); //$NON-NLS-1$
		assertTrue("did not find type to install breakpoint in", targetType.exists()); //$NON-NLS-1$

		return createLineBreakpoint(targetType, lineNumber);
	}


	/**
	 * Creates a line breakpoint in the given type (may be a top level non public type)
	 *
	 * @param lineNumber line number to create the breakpoint at
	 * @param packageName fully qualified package name containing the type, example "a.b.c"
	 * @param cuName simple name of compilation unit containing the type, example "Something.java"
	 * @param typeName $ qualified type name, example "Something" or "NonPublic" or "Something$Inner"
	 * @return line breakpoint
	 */
	protected IJavaLineBreakpoint createLineBreakpoint(int lineNumber, String packageName, String cuName, String typeName) throws Exception {
		IType type = getType(packageName, cuName, typeName);
		assertNotNull("Could not find the requested IType: "+typeName, type);
		return createLineBreakpoint(type, lineNumber);
	}

	/**
	 * Creates a line breakpoint in the given type at the given line number.
	 *
	 * @param type type in which to install the breakpoint
	 * @param lineNumber line number to install the breakpoint at
	 * @return line breakpoint
	 */
	protected IJavaLineBreakpoint createLineBreakpoint(IType type, int lineNumber) throws Exception {
		assertNotNull("You cannot create a line breakpoint for a null IType", type);
		IMember member = null;
		IJavaElement sourceElement = null;
		String source = null;
		if (type.isBinary()) {
			IClassFile classFile = type.getClassFile();
			source = classFile.getSource();
			sourceElement = classFile;
		} else {
			ICompilationUnit unit = type.getCompilationUnit();
			source = unit.getSource();
			sourceElement = unit;
		}
		// translate line number to offset
		if (source != null) {
			Document document = new Document(source);
			IRegion region = document.getLineInformation(lineNumber);
			if (sourceElement instanceof ICompilationUnit) {
				member = (IMember) ((ICompilationUnit)sourceElement).getElementAt(region.getOffset());
			} else {
				member = (IMember) ((IClassFile)sourceElement).getElementAt(region.getOffset());
			}
		}
		Map<String, Object> map = getExtraBreakpointAttributes(member);
		IJavaLineBreakpoint bp = JDIDebugModel.createLineBreakpoint(getBreakpointResource(type), type.getFullyQualifiedName(), lineNumber, -1, -1, 0, true, map);
		forceDeltas(bp);
		return bp;
	}

	/**
	 * Forces marker deltas to be sent based on breakpoint creation.
	 */
	private void forceDeltas(IBreakpoint breakpoint) throws CoreException {
		IProject project = breakpoint.getMarker().getResource().getProject();
		if (project != null) {
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		}
	}

	/**
	 * Returns the type in the test project based on the given name. The type name may refer to a
	 * top level non public type.
	 *
	 * @param packageName package name, example "a.b.c"
	 * @param cuName simple compilation unit name within the package, example "Something.java"
	 * @param typeName simple dot qualified type name, example "Something" or "NonPublic" or "Something.Inner"
	 * @return associated type or <code>null</code> if none
	 */
	protected IType getType(String packageName, String cuName, String typeName) throws Exception {
		IPackageFragment[] packageFragments = getProjectContext().getPackageFragments();
		for (int i = 0; i < packageFragments.length; i++) {
			IPackageFragment fragment = packageFragments[i];
			if (fragment.getElementName().equals(packageName)) {
				ICompilationUnit compilationUnit = fragment.getCompilationUnit(cuName);
				String[] names = typeName.split("\\$"); //$NON-NLS-1$
				IType type = compilationUnit.getType(names[0]);
				for (int j = 1; j < names.length; j++) {
					type = type.getType(names[j]);
				}
				if (type.exists()) {
					return type;
				}
			}
		}
		return null;
	}

	/**
	 * Creates and returns a map of java element breakpoint attributes for a breakpoint on the
	 * given java element, or <code>null</code> if none
	 *
	 * @param element java element the breakpoint is associated with
	 * @return map of breakpoint attributes or <code>null</code>
	 */
	protected Map<String, Object> getExtraBreakpointAttributes(IMember element) throws Exception {
		if (element != null && element.exists()) {
			Map<String, Object> map = new HashMap<>();
			ISourceRange sourceRange = element.getSourceRange();
			int start = sourceRange.getOffset();
			int end = start + sourceRange.getLength();
			IType type = null;
			if (element instanceof IType) {
				type = (IType) element;
			} else {
				type = element.getDeclaringType();
			}
			BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(map, type, start, end);
			return map;
		}
		return null;
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
		IJavaLineBreakpoint bp = createLineBreakpoint(lineNumber, typeName);
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
		return JDIDebugModel.createPatternBreakpoint(getProjectContext().getProject(), sourceName, pattern, lineNumber, -1, -1, 0, true, null);
	}

	/**
	 * Creates and returns a target pattern breakpoint at the given line number in the
	 * source file with the given name.
	 *
	 * @param lineNumber line number
	 * @param sourceName name of source file
	 */
	protected IJavaTargetPatternBreakpoint createTargetPatternBreakpoint(int lineNumber, String sourceName) throws Exception {
		return JDIDebugModel.createTargetPatternBreakpoint(getProjectContext().getProject(), sourceName, lineNumber, -1, -1, 0, true, null);
	}

	/**
	 * Creates and returns a stratum breakpoint at the given line number in the
	 * source file with the given name.
	 *
	 * @param lineNumber line number
	 * @param sourceName name of source file
	 * @param stratum the stratum of the source file
	 */
	protected IJavaStratumLineBreakpoint createStratumBreakpoint(int lineNumber, String sourceName, String stratum) throws Exception {
		return JDIDebugModel.createStratumBreakpoint(getProjectContext().getProject(), stratum, sourceName, null, null, lineNumber, -1, -1, 0, true, null);
	}

	/**
	 * Creates and returns a method breakpoint
	 *
	 * @param typeNamePattern type name pattern
	 * @param methodName method name
	 * @param methodSignature method signature or <code>null</code>
	 * @param entry whether to break on entry
	 * @param exit whether to break on exit
	 */
	protected IJavaMethodBreakpoint createMethodBreakpoint(String typeNamePattern, String methodName, String methodSignature, boolean entry, boolean exit) throws Exception {
		return createMethodBreakpoint(getProjectContext(), typeNamePattern, methodName, methodSignature, entry, exit);
	}

	/**
	 * Creates and returns a method breakpoint
	 *
	 * @param project java project
	 * @param typeNamePattern type name pattern
	 * @param methodName method name
	 * @param methodSignature method signature or <code>null</code>
	 * @param entry whether to break on entry
	 * @param exit whether to break on exit
	 */
	protected IJavaMethodBreakpoint createMethodBreakpoint(IJavaProject project, String typeNamePattern, String methodName, String methodSignature, boolean entry, boolean exit) throws Exception {
		IMethod method= null;
		IResource resource = project.getProject();
		if (methodSignature != null && methodName != null) {
			IType type = project.findType(typeNamePattern);
			if (type != null ) {
				resource = getBreakpointResource(type);
				method = type.getMethod(methodName, Signature.getParameterTypes(methodSignature));
			}
		}
		Map<String, Object> map = getExtraBreakpointAttributes(method);
		IJavaMethodBreakpoint bp = JDIDebugModel.createMethodBreakpoint(resource, typeNamePattern, methodName, methodSignature, entry, exit,false, -1, -1, -1, 0, true, map);
		forceDeltas(bp);
		return bp;
	}

	/**
	 * Creates a method breakpoint in a fully specified type (potentially non public).
	 *
	 * @param packageName package name containing type to install breakpoint in, example "a.b.c"
	 * @param cuName simple compilation unit name within package, example "Something.java"
	 * @param typeName $ qualified type name within compilation unit, example "Something" or
	 *  "NonPublic" or "Something$Inner"
	 * @param methodName method or <code>null</code> for all methods
	 * @param methodSignature JLS method signature or <code>null</code> for all methods with the given name
	 * @param entry whether to break on entry
	 * @param exit whether to break on exit
	 * @return method breakpoint
	 */
	protected IJavaMethodBreakpoint createMethodBreakpoint(String packageName, String cuName, String typeName, String methodName, String methodSignature, boolean entry, boolean exit) throws Exception {
		IType type = getType(packageName, cuName, typeName);
		assertNotNull("did not find type to install breakpoint in", type); //$NON-NLS-1$
		IMethod method= null;
		if (methodSignature != null && methodName != null) {
			if (type != null ) {
				method = type.getMethod(methodName, Signature.getParameterTypes(methodSignature));
			}
		}
		Map<String, Object> map = getExtraBreakpointAttributes(method);
		IJavaMethodBreakpoint bp = JDIDebugModel.createMethodBreakpoint(getBreakpointResource(type), type.getFullyQualifiedName(), methodName, methodSignature, entry, exit,false, -1, -1, -1, 0, true, map);
		forceDeltas(bp);
		return bp;
	}


	/**
	 * Creates a MethodBreakPoint on the method specified at the given path.
	 * Syntax:
	 * Type$InnerType$MethodNameAndSignature$AnonymousTypeDeclarationNumber$FieldName
	 * eg:<code>
	 * public class Foo{
	 * 		class Inner
	 * 		{
	 * 			public void aMethod()
	 * 			{
	 * 				Object anon = new Object(){
	 * 					int anIntField;
	 * 					String anonTypeMethod() {return "an Example";}
	 * 				}
	 * 			}
	 * 		}
	 * }</code>
	 * Syntax to get the anonymous toString would be: Foo$Inner$aMethod()V$1$anonTypeMethod()QString
	 * so, createMethodBreakpoint(packageName, cuName, "Foo$Inner$aMethod()V$1$anonTypeMethod()QString",true,false);
	 */
	protected IJavaMethodBreakpoint createMethodBreakpoint(String root, String packageName, String cuName,
									String fullTargetName, boolean entry, boolean exit) throws Exception {

		IJavaProject javaProject = getProjectContext();
		ICompilationUnit cunit = getCompilationUnit(javaProject, root, packageName, cuName);
		assertNotNull("did not find requested Compilation Unit", cunit); //$NON-NLS-1$
		IMethod targetMethod = (IMethod)(new MemberParser()).getDeepest(cunit,fullTargetName);
		assertNotNull("did not find requested method", targetMethod); //$NON-NLS-1$
		assertTrue("Given method does not exist", targetMethod.exists()); //$NON-NLS-1$
		IType methodParent = (IType)targetMethod.getParent();//safe - method's only parent = Type
		assertNotNull("did not find type to install breakpoint in", methodParent); //$NON-NLS-1$

		Map<String, Object> map = getExtraBreakpointAttributes(targetMethod);
		IJavaMethodBreakpoint bp = JDIDebugModel.createMethodBreakpoint(getBreakpointResource(methodParent), methodParent.getFullyQualifiedName(),targetMethod.getElementName(), targetMethod.getSignature(), entry, exit,false, -1, -1, -1, 0, true, map);
		forceDeltas(bp);
		return bp;
	}

	/**
	 * @param cu the Compilation where the target resides
	 * @param target the full name of the target, as per MemberParser syntax
	 * @return the requested Member
	 */
	protected IMember getMember(ICompilationUnit cu, String target) {
		IMember toReturn = (new MemberParser()).getDeepest(cu,target);
		return toReturn;
	}

	/**
	 * Delegate method to get a resource with a specific name from the testing workspace 'src' folder
	 * @param name the name of the <code>IResource</code> to get
	 * @return the specified <code>IResource</code> or <code>null</code> if it does not exist
	 *
	 * @since 3.4
	 */
	protected IResource getResource(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(new Path("/DebugTests/src/"+name));
	}

	/**
	 * Creates and returns a class prepare breakpoint on the type with the given fully qualified name.
	 *
	 * @param typeName type on which to create the breakpoint
	 * @return breakpoint
	 */
	protected IJavaClassPrepareBreakpoint createClassPrepareBreakpoint(String typeName) throws Exception {
		return createClassPrepareBreakpoint(getType(typeName));
	}

	/**
	 * Creates and returns a class prepare breakpoint on the type with the given fully qualified name.
	 *
	 * @return breakpoint
	 */
	protected IJavaClassPrepareBreakpoint createClassPrepareBreakpoint(String root,
			String packageName, String cuName, String fullTargetName) throws Exception {
		ICompilationUnit cunit = getCompilationUnit(getProjectContext(), root, packageName, cuName);
		IType type = (IType)getMember(cunit,fullTargetName);
		assertTrue("Target type not found", type.exists()); //$NON-NLS-1$
		return createClassPrepareBreakpoint(type);
	}

	/**
	 * Creates a class prepare breakpoint in a fully specified type (potentially non public).
	 *
	 * @param packageName package name containing type to install breakpoint in, example "a.b.c"
	 * @param cuName simple compilation unit name within package, example "Something.java"
	 * @param typeName $ qualified type name within compilation unit, example "Something" or
	 *  "NonPublic" or "Something$Inner"
	 */
	protected IJavaClassPrepareBreakpoint createClassPrepareBreakpoint(String packageName, String cuName, String typeName) throws Exception {
		return createClassPrepareBreakpoint(getType(packageName, cuName, typeName));
	}

	/**
	 * Creates a class prepare breakpoint for the given type
	 *
	 * @param type type
	 * @return class prepare breakpoint
	 */
	protected IJavaClassPrepareBreakpoint createClassPrepareBreakpoint(IType type) throws Exception {
		assertNotNull("type not specified for class prepare breakpoint", type); //$NON-NLS-1$
		int kind = IJavaClassPrepareBreakpoint.TYPE_CLASS;
		if (type.isInterface()) {
			kind = IJavaClassPrepareBreakpoint.TYPE_INTERFACE;
		}
		Map<String, Object> map = getExtraBreakpointAttributes(type);
		IJavaClassPrepareBreakpoint bp = JDIDebugModel.createClassPrepareBreakpoint(getBreakpointResource(type), type.getFullyQualifiedName(), kind, -1, -1, true, map);
		forceDeltas(bp);
		return bp;
	}

	/**
	 * Returns the Java model type from the test project with the given name or <code>null</code>
	 * if none.
	 *
	 * @return type or <code>null</code>
	 */
	protected IType getType(String typeName) throws Exception {
		return getProjectContext().findType(typeName);
	}

	/**
	 * Creates and returns a watchpoint
	 *
	 * @param typeName
	 *            type name
	 * @param fieldName
	 *            field name
	 * @param access
	 *            whether to suspend on field access
	 * @param modification
	 *            whether to suspend on field modification
	 */
	protected IJavaWatchpoint createWatchpoint(String typeName, String fieldName, boolean access, boolean modification) throws Exception {
		IType type = getType(typeName);
		return createWatchpoint(type, fieldName, access, modification);
	}

	/**
	 * Creates and returns an exception breakpoint
	 *
	 * @param exName exception name
	 * @param caught whether to suspend in caught locations
	 * @param uncaught whether to suspend in uncaught locations
	 */
	protected IJavaExceptionBreakpoint createExceptionBreakpoint(String exName, boolean caught, boolean uncaught) throws Exception {
		IType type = getType(exName);
		Map<String, Object> map = getExtraBreakpointAttributes(type);
		IJavaExceptionBreakpoint bp = JDIDebugModel.createExceptionBreakpoint(getBreakpointResource(type),exName, caught, uncaught, false, true, map);
		forceDeltas(bp);
		return bp;
	}

	/**
	 * Creates and returns a watchpoint
	 *
	 * @param typeNmae type name
	 * @param fieldName field name
	 * @param access whether to suspend on field access
	 * @param modification whether to suspend on field modification
	 */
/*	protected IJavaWatchpoint createWatchpoint(String typeName, String fieldName, boolean access, boolean modification) throws Exception {
		IType type = getType(typeName);
		return createWatchpoint(type, fieldName, access, modification);
	}*/


	/**
	 * Creates a WatchPoint on the field specified at the given path.
	 * Will create watchpoints on fields within anonymous types, inner types,
	 * local (non-public) types, and public types.
	 * @param packageName package name containing type to install breakpoint in, example "a.b.c"
	 * @param cuName simple compilation unit name within package, example "Something.java"
	 * @param fullTargetName - see below
	 * @param access whether to suspend on access
	 * @param modification whether to suspend on modification
	 * @return a watchpoint
	 * @throws CoreException
	 *
	 * <pre>
	 * Syntax example:
	 * Type$InnerType$MethodNameAndSignature$AnonymousTypeDeclarationNumber$FieldName
	 * eg:
	 * public class Foo{
	 * 		class Inner
	 * 		{
	 * 			public void aMethod()
	 * 			{
	 * 				Object anon = new Object(){
	 * 					int anIntField;
	 * 					String anonTypeMethod() {return "an Example";}
	 * 				}
	 * 			}
	 * 		}
	 * }</pre>
	 * To get the anonymous toString, syntax of fullTargetName would be: <code>Foo$Inner$aMethod()V$1$anIntField</code>
	 */
	protected IJavaWatchpoint createNestedTypeWatchPoint(String root, String packageName, String cuName,
			String fullTargetName, boolean access, boolean modification) throws Exception, CoreException {

		ICompilationUnit cunit = getCompilationUnit(getProjectContext(), root, packageName, cuName);
		IField field = (IField)getMember(cunit,fullTargetName);
		assertNotNull("Path to field is not valid", field); //$NON-NLS-1$
		assertTrue("Field is not valid", field.exists()); //$NON-NLS-1$
		IType type = (IType)field.getParent();
		return createWatchpoint(type, field.getElementName(), access, modification);
	}


	/**
	 * Creates a watchpoint in a fully specified type (potentially non public).
	 *
	 * @param packageName package name containing type to install breakpoint in, example "a.b.c"
	 * @param cuName simple compilation unit name within package, example "Something.java"
	 * @param typeName $ qualified type name within compilation unit, example "Something" or
	 *  "NonPublic" or "Something$Inner"
	 * @param fieldName name of the field
	 * @param access whether to suspend on access
	 * @param modification whether to suspend on modification
	 */
	protected IJavaWatchpoint createWatchpoint(String packageName, String cuName, String typeName, String fieldName, boolean access, boolean modification) throws Exception {
		IType type = getType(packageName, cuName, typeName);
		return createWatchpoint(type, fieldName, access, modification);
	}

	/**
	 * Creates a watchpoint on the specified field.
	 *
	 * @param type type containing the field
	 * @param fieldName name of the field
	 * @param access whether to suspend on access
	 * @param modification whether to suspend on modification
	 * @return watchpoint
	 */
	protected IJavaWatchpoint createWatchpoint(IType type, String fieldName, boolean access, boolean modification) throws Exception, CoreException {
		assertNotNull("type not specified for watchpoint", type); //$NON-NLS-1$
		IField field = type.getField(fieldName);
		Map<String, Object> map = getExtraBreakpointAttributes(field);
		IJavaWatchpoint wp = JDIDebugModel.createWatchpoint(getBreakpointResource(type), type.getFullyQualifiedName(), fieldName, -1, -1, -1, 0, true, map);
		wp.setAccess(access);
		wp.setModification(modification);
		forceDeltas(wp);
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
	 * the target is terminated. This avoids defunct processes on Linux.
	 */
	protected void terminateAndRemove(IJavaDebugTarget debugTarget) {
		assertNotNull(getName()+" - you cannot terminate and remove a null debug target", debugTarget);
	    ILaunch launch = debugTarget.getLaunch();
		if (!(debugTarget.isTerminated() || debugTarget.isDisconnected())) {
			IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
			jdiUIPreferences.setValue(IJDIPreferencesConstants.PREF_SUSPEND_ON_UNCAUGHT_EXCEPTIONS, false);

			DebugEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.TERMINATE, debugTarget);
			waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());
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
				waiter.waitForEvent();
			} catch (CoreException e) {
			}
		}
		TestUtil.waitForJobs(getName(), 100, 10000);
		TestUtil.runEventLoop();
		getLaunchManager().removeLaunch(launch);
        // ensure event queue is flushed
        DebugEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.MODEL_SPECIFIC, this);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());
        DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(this, DebugEvent.MODEL_SPECIFIC)});
        waiter.waitForEvent();
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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		IAstEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(getProjectContext(), (IJavaDebugTarget)frame.getDebugTarget());
		try {
			engine.evaluate(snippet, frame, this, DebugEvent.EVALUATION, true);

			Object suspendee= waiter.waitForEvent();
			setEventSet(waiter.getEventSet());
			tryAgain(() -> assertNotNull("Program did not suspend evaluating: \n\n" + snippet, suspendee)); //$NON-NLS-1$
			return fEvaluationResult;
		}
		finally {
			engine.dispose();
		}
	}

	/**
	 * Runs an evaluation using an embedded listener and the {@link #DEFAULT_TIMEOUT} for the operation
	 * @param snippet the snippet to evaluate
	 * @param thread the suspended thread to run the evaluation on
	 * @return the {@link IEvaluationResult}
	 * @since 3.1.200
	 */
	protected IEvaluationResult evaluate(String snippet, IJavaThread thread) throws Exception {
		class Listener implements IEvaluationListener {
			volatile IEvaluationResult fResult;
			@Override
			public void evaluationComplete(IEvaluationResult result) {
				fResult= result;
			}
		}
		Listener listener= new Listener();
		IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
		assertNotNull("There should be a stackframe", frame);
		ASTEvaluationEngine engine = new ASTEvaluationEngine(getProjectContext(), (IJavaDebugTarget) thread.getDebugTarget());
		try {
			engine.evaluate(snippet, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
			long timeoutNanos = System.nanoTime() + DEFAULT_TIMEOUT * 1_000_000L;
			while (listener.fResult == null && System.nanoTime() < timeoutNanos) {
				Thread.sleep(1);
			}
			return listener.fResult;
		}
		finally {
			engine.dispose();
		}
	}

	/**
	 * @see IEvaluationListener#evaluationComplete(IEvaluationResult)
	 */
	@Override
	public void evaluationComplete(IEvaluationResult result) {
		fEvaluationResult = result;
	}

	/**
	 * Performs a step over in the given stack frame and returns when complete.
	 *
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepOver(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.STEP_END);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		frame.stepOver();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread) suspendee;
	}

	/**
	 * Performs a step over in the given stack frame and returns when a breakpoint is hit.
	 *
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepOverToBreakpoint(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		frame.stepOver();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread) suspendee;
	}

	/**
	 * Performs a step into in the given stack frame and returns when complete.
	 *
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepInto(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.STEP_END);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		frame.stepInto();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread) suspendee;
	}

	/**
	 * Performs a step return in the given stack frame and returns when complete.
	 *
	 * @param frame stack frame to step return from
	 */
	protected IJavaThread stepReturn(IJavaStackFrame frame) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.STEP_END);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		frame.stepReturn();

		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread) suspendee;
	}

	/**
	 * Performs a step into with filters in the given stack frame and returns when
	 * complete.
	 *
	 * @param frame stack frame to step in
	 */
	protected IJavaThread stepIntoWithFilters(IJavaStackFrame frame) throws Exception {
		return stepIntoWithFilters(frame, true);
	}

	/**
	 * Performs a step into with filters in the given stack frame and returns when complete.
	 *
	 * @param frame
	 *            stack frame to step in
	 * @param stepThru
	 *            whether to step thru or step return from a filtered location
	 */
	protected IJavaThread stepIntoWithFilters(IJavaStackFrame frame, boolean stepThru) throws Exception {
		DebugEventWaiter waiter= new DebugElementKindEventWaiter(DebugEvent.SUSPEND, IJavaThread.class);
		waiter.setTimeout(DEFAULT_TIMEOUT);
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		// turn filters on
		IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
		try {
			target.setStepFiltersEnabled(true);
			target.setStepThruFilters(stepThru);
			frame.stepInto();
			Object suspendee= waiter.waitForEvent();
			setEventSet(waiter.getEventSet());
			assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
			return (IJavaThread) suspendee;
		} catch (DebugException e) {
			tryTestAgain(e);
		} finally {
			// turn filters off
			target.setStepFiltersEnabled(false);
			target.setStepThruFilters(true);
		}
		return null;
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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		// turn filters on
		IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
		try {
			target.setStepFiltersEnabled(true);
			frame.stepReturn();
		} catch (DebugException e) {
			tryTestAgain(e);
		} finally {
			// turn filters off
			target.setStepFiltersEnabled(false);
		}


		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
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
		waiter.setEnableUIEventLoopProcessing(enableUIEventLoopProcessingInWaiter());

		// turn filters on
		IJavaDebugTarget target = (IJavaDebugTarget) frame.getDebugTarget();
		try {
			target.setStepFiltersEnabled(true);
			frame.stepOver();
		} catch (DebugException e) {
			tryTestAgain(e);
		} finally {
			// turn filters off
			target.setStepFiltersEnabled(false);
		}


		Object suspendee= waiter.waitForEvent();
		setEventSet(waiter.getEventSet());
		assertNotNull("Program did not suspend.", suspendee); //$NON-NLS-1$
		return (IJavaThread) suspendee;
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
     * Wait for builds to complete
     */
    public static void waitForBuild() {
        boolean wasInterrupted = false;
        do {
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
                Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
				// Let also all other pending jobs proceed, ignore console jobs
				TestUtil.waitForJobs("waitForBuild", 100, 5000, ProcessConsole.class);
                wasInterrupted = false;
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                wasInterrupted = true;
            }
        } while (wasInterrupted);
    }


    /**
     * Finds the specified variable within the context of the specified stackframe. Returns null if a variable with
     * the given name does not exist
     * @return the <code>IJavaVariable</code> with the given name or <code>null</code> if it
     * does not exist
     */
    protected IJavaVariable findVariable(IJavaStackFrame frame, String name) throws DebugException {
        IJavaVariable variable = frame.findVariable(name);
        if (variable == null) {
            // dump visible variables
            IDebugModelPresentation presentation = DebugUIPlugin.getModelPresentation();
            System.out.println("Could not find variable '" + name + "' in frame: " + presentation.getText(frame)); //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println("Visible variables are:"); //$NON-NLS-1$
            IVariable[] variables = frame.getVariables();
            for (int i = 0; i < variables.length; i++) {
                IVariable variable2 = variables[i];
                System.out.println("\t" + presentation.getText(variable2)); //$NON-NLS-1$
            }
			if (!frame.isStatic() && !frame.isNative()) {
				IJavaObject ths = frame.getThis();
				if (ths != null) {
					variables = ths.getVariables();
					for (int i = 0; i < variables.length; i++) {
						IVariable variable2 = variables[i];
						System.out.println("\t" + presentation.getText(variable2)); //$NON-NLS-1$
					}
				}
            }
        }
        return variable;
    }

	/**
	 * Returns if the local filesystem is case-sensitive or not
	 * @return true if the local filesystem is case-sensitive, false otherwise
	 */
	protected boolean isFileSystemCaseSensitive() {
		return Platform.OS.isMac() ? false : new File("a").compareTo(new File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
	}

    /**
     * Creates a shared launch configuration for the type with the given name.
     */
    protected ILaunchConfiguration createLaunchConfiguration(String mainTypeName) throws Exception {
        return createLaunchConfiguration(getProjectContext(), mainTypeName);
    }

    /**
     * Creates a shared launch configuration for the type with the given name.
     */
    protected ILaunchConfiguration createLaunchConfiguration(IJavaProject project, String mainTypeName) throws Exception {
		return createLaunchConfiguration(project, mainTypeName, false);
	}

	/**
	 * Creates a shared launch configuration for the type with the given name.
	 *
	 * @param clone
	 *            true if the launch config name should be different from the main type name
	 */
	protected ILaunchConfiguration createLaunchConfiguration(IJavaProject project, String mainTypeName, boolean clone) throws Exception {
        ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		String configName = clone ? mainTypeName + CLONE_SUFFIX : mainTypeName;
		ILaunchConfigurationWorkingCopy config = type.newInstance(project.getProject().getFolder(LAUNCHCONFIGURATIONS), configName);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
        setEnvironment(config);
		Set<String> modes = new HashSet<>();
		modes.add(ILaunchManager.RUN_MODE);
		config.setPreferredLaunchDelegate(modes, LOCAL_JAVA_APPLICATION_TYPE_ID);
		modes = new HashSet<>();
		modes.add(ILaunchManager.DEBUG_MODE);
		config.setPreferredLaunchDelegate(modes, LOCAL_JAVA_APPLICATION_TYPE_ID);
        // use 'java' instead of 'javaw' to launch tests (javaw is problematic
        // on JDK1.4.2)
        Map<String, String> map = new HashMap<>(1);
        map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, JAVA);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
        return config.doSave();
    }

    /**
     * Creates a shared launch configuration for the type with the given name.
     */
    protected ILaunchConfiguration createLaunchConfiguration(IJavaProject project, String containername, String mainTypeName) throws Exception {
        ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        ILaunchConfigurationWorkingCopy config = type.newInstance(project.getProject().getFolder(containername), mainTypeName);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
        setEnvironment(config);
		Set<String> modes = new HashSet<>();
		modes.add(ILaunchManager.RUN_MODE);
		config.setPreferredLaunchDelegate(modes, LOCAL_JAVA_APPLICATION_TYPE_ID);
		modes = new HashSet<>();
		modes.add(ILaunchManager.DEBUG_MODE);
		config.setPreferredLaunchDelegate(modes, LOCAL_JAVA_APPLICATION_TYPE_ID);
        // use 'java' instead of 'javaw' to launch tests (javaw is problematic
        // on JDK1.4.2)
        Map<String, String> map = new HashMap<>(1);
        map.put(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND, JAVA);
        config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
        return config.doSave();
    }

    private void setEnvironment(ILaunchConfigurationWorkingCopy workingCopy) {
      Map<String, String> env = getLaunchManager().getNativeEnvironment().entrySet().stream()
        .filter(e -> !"JAVA_TOOL_OPTIONS".equals(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      workingCopy.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
      workingCopy.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
    }

	/**
	 * Exception to indicate a test should be run again when it fails.
	 */
	protected static class TestAgainException extends RuntimeException {
		private static final long serialVersionUID = 1848804390493463729L;

		public TestAgainException(String string) {
			super(string);
		}

		public TestAgainException(Throwable cause) {
			super("Try test again because of random fail.", cause);
		}
	}

	protected void tryAgain(Runnable run) {
		try {
			run.run();
		} catch (AssertionError e) {
			throw new TestAgainException(e);
		}
	}

	/**
	 * When a test throws the 'try again' exception, try it again.
	 *
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {
		boolean tryAgain = true;
		int attempts = 0;
		while (tryAgain) {
			try {
				attempts++;
				super.runBare();
				tryAgain = false;
			} catch (TestAgainException e) {
				TestUtil.log(IStatus.ERROR, getName(), "Test failed attempt " + attempts + ". Re-testing.", e);
				TestUtil.cleanUp(getName());
				if (attempts > 4) {
					// the next attempt will fail
					break;
				}
			}
		}
		if (tryAgain) {
			// last attempt and if it fails then we should fail, see bug 515988
			super.runBare();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		TestUtil.logInfo("TDOWN " + getClass().getSimpleName() + "." + getName());
		shutdownDebugTargets();
		TestUtil.cleanUp(getName());
		super.tearDown();
	}

	protected void shutdownDebugTargets() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] targets = launchManager.getDebugTargets();
		for (IDebugTarget target : targets) {
			if (target instanceof JDIDebugTarget) {
				((JDIDebugTarget) target).shutdown();
			}
		}
	}

	/**
	 * Returns the version level of the class files being run, based on the system property <code>java.class.version</code>
	 * @return the version level of the class files being run in the current VM
	 *
	 *  @since 3.6
	 */
	protected String getClassFileVersion() {
		String version = System.getProperty("java.class.version");
		if(version.compareTo("48.0") <= 0) {
			return JavaCore.VERSION_1_4;
		}
		if(version.compareTo("49.0") <= 0) {
			return JavaCore.VERSION_1_5;
		}
		return JavaCore.VERSION_1_6;
	}

	/**
	 * Determines if the test should be attempted again based on the error code.
	 * See bug 297071.
	 *
	 * @param e Debug Exception
	 */
	protected void tryTestAgain(DebugException e) throws Exception {
		Throwable cause = e.getCause();
		if (cause instanceof InternalException) {
			int code = ((InternalException)cause).errorCode();
			if (code == 13) {
				throw new TestAgainException(e);
			}
		}
		throw e;
	}

	/**
	 * Perform the actual evaluation (inspect)
	 * @return the result of the evaluation
	 */
	protected IValue doEval(IJavaThread thread, String snippet) throws Exception{
		return this.doEval(thread, () -> (IJavaStackFrame) thread.getTopStackFrame(), snippet);
	}

	/**
	 * Perform the actual evaluation (inspect)
	 *
	 * @param frameSupplier
	 *            The frame supplier which provides the frame for the evaluation
	 * @return the result of the evaluation
	 */
	protected IValue doEval(IJavaThread thread, StackFrameSupplier frameSupplier, String snippet) throws Exception {
		class Listener implements IEvaluationListener {
			volatile IEvaluationResult fResult;

			@Override
			public void evaluationComplete(IEvaluationResult result) {
				fResult= result;
			}

			public IEvaluationResult getResult() {
				return fResult;
			}
		}
		Listener listener = new Listener();
		IJavaStackFrame frame = frameSupplier.get();
		assertNotNull("There should be a stackframe", frame);
		ASTEvaluationEngine engine = new ASTEvaluationEngine(getProjectContext(), (IJavaDebugTarget) thread.getDebugTarget());
		try {
			engine.evaluate(snippet, frame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
			long timeoutNanos = System.nanoTime() + 5000 * 1_000_000L;
			while (listener.getResult() == null && System.nanoTime() < timeoutNanos) {
				Thread.sleep(1);
			}
			IEvaluationResult result = listener.getResult();
			assertNotNull("The evaluation should have result: ", result);
			assertNull("Evaluation of '" + snippet + "' should not have exception : " + findCause(result.getException()), result.getException());

			String firstError = result.hasErrors() ? result.getErrorMessages()[0] : "";
			assertFalse("The evaluation of '\" + snippet + \"'  should not have errors : " + firstError, result.hasErrors());
			return listener.getResult().getValue();
		}
		finally {
			engine.dispose();
		}
	}

	private static Object findCause(DebugException problem) {
		if (problem == null) {
			return null;
		}
		Throwable cause = problem.getCause();
		if (cause instanceof InvocationException) {
			return ((InvocationException)cause).exception().toString();
		}
		return cause;
	}

	/**
	 * @return true if the UI event loop should be proicessed during wait operations on UI thread
	 */
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return false;
	}

	protected void assertNoErrorMarkersExist() throws Exception {
		IJavaProject javaProject = getProjectContext();
		assertNotNull("Java test project cannot be null", javaProject);
		IProject project = javaProject.getProject();
		assertNotNull("test project cannot be null", project);
		IProject[] projects = { project };
		assertNoErrorMarkersExist(projects);
	}

	protected void assertNoErrorMarkersExist(IProject[] projects) throws Exception {
		for (IProject project : projects) {
			assertNoErrorMarkersExist(project);
		}
	}

	protected void assertNoErrorMarkersExist(IProject project) throws Exception {
		if (project.isAccessible()) {
			IMarker[] projectMarkers = project.findMarkers(null, false, IResource.DEPTH_INFINITE);
			List<IMarker> errorMarkers = Arrays.stream(projectMarkers).filter(marker -> isErrorMarker(marker)).collect(Collectors.toList());
			String projectErrors = toString(errorMarkers);
			assertEquals("found errors on project " + project + ":" + System.lineSeparator() + projectErrors, Collections.EMPTY_LIST, errorMarkers);
		}
	}

	private static boolean isErrorMarker(IMarker marker) {
		return marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR;
	}

	private static String toString(Collection<IMarker> markers) {
		StringBuilder markersInfo = new StringBuilder();
		for (IMarker marker : markers) {
			markersInfo.append(marker);
		}
		return markersInfo.toString();
	}

	/**
	 * JDT tests run in different environments where different major JVM installations might be selected as "default" JVM for a specific Execution
	 * Environment (EE). Some test cases projects requires JavaSE-N EE, which can be resolved to e.g. Java 11, 17 or 21, depending on the installed
	 * JVMs. JVM modules vary between Java major versions, while we need a stable set of modules for the test case. Therefore we "pin" the JVM used
	 * for the JavaSE-N EE to the JVM on which the tests are executed - to avoid tests failing in different test environments.
	 *
	 * @param environmentId The ID of the EE, e.g.: "JavaSE-9"
	 * @return The default VM install for the EE, before we change it.
	 */
	protected static IVMInstall prepareExecutionEnvironment(String environmentId) {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		IExecutionEnvironment environment = getExecutionEnvironment(environmentId);
		IVMInstall defaultVM = environment.getDefaultVM();
		environment.setDefaultVM(vm);
		TestUtil.logInfo("Set VM \"" + vm.getName() + "\" for execution environments: " + environment.getId());
		return defaultVM;
	}

	/**
	 * Set the default VM of an EE.
	 *
	 * @param environmentId The ID of the EE, e.g.: "JavaSE-9"
	 * @param defaultVM The default VM to set.
	 */
	protected static void setExecutionEnvironment(String environmentId, IVMInstall defaultVM) {
		IExecutionEnvironment environment = getExecutionEnvironment(environmentId);
		environment.setDefaultVM(defaultVM);
		TestUtil.logInfo("Set default VM for execution environment: " + environment.getId());
	}

	private static IExecutionEnvironment getExecutionEnvironment(String environmentId) {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
		return Arrays.stream(environments).filter(e -> environmentId.equals(e.getId())).findFirst().orElseThrow();
	}

	public interface StackFrameSupplier {
		IJavaStackFrame get() throws Exception;
	}

	private static void logVMChange(String message, IVMInstall vm) {
		String detailed = message + " " + vm.getName() + ", location: " + vm.getInstallLocation();
		IStatus status = new Status(IStatus.INFO, JDIDebugPlugin.getUniqueIdentifier(), detailed, null);
		JDIDebugPlugin.log(status);
	}

	private static class LogVMInstallChanges implements IVMInstallChangedListener {

		@Override
		public void vmRemoved(IVMInstall vm) {
			logVMChange("VM removed", vm);
		}

		@Override
		public void vmChanged(PropertyChangeEvent event) {
		}

		@Override
		public void vmAdded(IVMInstall vm) {
			logVMChange("VM added", vm);

		}

		@Override
		public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
			logVMChange("Default VM changed", current);
		}

	}
}
