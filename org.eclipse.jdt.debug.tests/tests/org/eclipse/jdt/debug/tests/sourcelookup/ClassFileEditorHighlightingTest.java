/*******************************************************************************
 * Copyright (c) 2026, Daniel Schmid and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Daniel Schmid - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.tests.ui.AbstractDebugUiTests;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Assume;

public class ClassFileEditorHighlightingTest extends AbstractDebugUiTests {

	private static final long TIMEOUT = 10_000L;

	private static final String CLASS_NAME = "NoSources";
	private static final String CLASS_CONTENTS = """
			public class NoSources {
				private static int i = 0;
				public static void main(String[] args) {
					i++;
					System.out.println(i);
				}
			}
			""";

	public ClassFileEditorHighlightingTest(String name) {
		super(name);
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return true;
	}

	@Override
	public void tearDown() throws Exception {
		try {
			closeAllEditors();
		} finally {
			super.tearDown();
		}
	}

	public void testDisplaySourceWithClassFileEditorHighlightsLine() throws Exception {
		IJavaProject project = createProjectWithNoSources(CLASS_NAME, CLASS_CONTENTS, true);
		IJavaThread thread = null;
		try {
			ILaunchConfiguration config = createLaunchConfiguration(project, CLASS_NAME);
			createLineBreakpoint(project.findType(CLASS_NAME), 4);
			thread = launchToBreakpoint(config);

			stepOver((IJavaStackFrame) thread.getTopStackFrame());
			expectHighlightedText("     8  getstatic java.lang.System.out : java.io.PrintStream [13]", TIMEOUT);

			stepOver((IJavaStackFrame) thread.getTopStackFrame());
			expectHighlightedText("    17  return", TIMEOUT);

			resumeAndExit(thread);
			expectNothingHighlighted(TIMEOUT);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			project.getProject().delete(true, null);
		}
	}

	public void testContinueAfterEditorClosed() throws Exception {
		IJavaProject project = createProjectWithNoSources(CLASS_NAME, CLASS_CONTENTS, true);
		IJavaThread thread = null;
		List<IStatus> errors = Collections.synchronizedList(new ArrayList<>());
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				if (status.matches(IStatus.ERROR)) {
					errors.add(status);
				}
			}
		};
		try {
			ILaunchConfiguration config = createLaunchConfiguration(project, CLASS_NAME);
			createLineBreakpoint(project.findType(CLASS_NAME), 4);
			thread = launchToBreakpoint(config);

			expectHighlightedText("     0  getstatic NoSources.i : int [7]", TIMEOUT);

			closeAllEditors();
			Platform.addLogListener(logListener);
			resumeAndExit(thread);
			assertEquals(List.of(), errors);

		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			project.getProject().delete(true, null);
			Platform.removeLogListener(logListener);
		}
	}

	public void testConstructorInPackage() throws Exception {
		IJavaProject project = createProjectWithNoSources("ClassOne", """
				public class ClassOne {

					public static void main(String[] args) {
						ClassOne co = new ClassOne();
						co.method1();
					}

					public void method1() {
						method2();
					}

					public void method2() {
						method3();
					}

					public void method3() {
						System.out.println("ClassOne, method3");
					}
				}
				""");
		createMethodBreakpoint(project.findType("ClassOne"), "<init>", "()V", true, false);
		IJavaThread thread = null;
		try {
			ILaunchConfiguration config = createLaunchConfiguration(project, "ClassOne");
			thread = launchToBreakpoint(config);
			expectHighlightedText("    0  aload_0 [this]", TIMEOUT);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			project.getProject().delete(true, null);
		}
	}

	public void testDisplaySourceWithClassFileEditorHighlightsLineInConstructor() throws Exception {
		IJavaProject project = createProjectWithNoSources("MethodCall", """
				public class MethodCall {

					private int i;
					private int sum = 0;

					public static void main(String[] args) {
						MethodCall mc = new MethodCall();
						mc.go();
					}

					public void go() {
						calculateSum();
					}

					protected void calculateSum() {
						sum += i;
					}
				}
				""");
		createMethodBreakpoint(project.findType("MethodCall"), "<init>", "()V", true, false);
		IJavaThread thread = null;
		try {
			ILaunchConfiguration config = createLaunchConfiguration(project, "MethodCall");
			thread = launchToBreakpoint(config);
			expectHighlightedText("     0  aload_0 [this]", TIMEOUT);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			project.getProject().delete(true, null);
		}
	}

	public void testClassFileWithoutDebuggingInformation() throws Exception {
		IJavaProject project = createProjectWithNoSources(CLASS_NAME, CLASS_CONTENTS);
		IJavaThread thread = null;
		try {
			ILaunchConfiguration config = createLaunchConfiguration(project, CLASS_NAME);
			ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
			config = workingCopy.doSave();
			thread = launchToBreakpoint(config);

			String[] expectedHighlights = {
					     "     0  getstatic NoSources.i : int [7]",
					     "     3  iconst_1",
					     "     4  iadd",
					     "     5  putstatic NoSources.i : int [7]",
					     "     8  getstatic java.lang.System.out : java.io.PrintStream [13]",
					     "    11  getstatic NoSources.i : int [7]",
					     "    14  invokevirtual java.io.PrintStream.println(int) : void [19]",
					     "    17  return",
			};

			for (int i = 0; i < expectedHighlights.length; i++) {
				expectHighlightedText(expectedHighlights[i], TIMEOUT);

				if (i < expectedHighlights.length - 1) {
					stepOver((IJavaStackFrame) thread.getTopStackFrame());
				}
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			project.getProject().delete(true, null);
		}
	}

	private IJavaProject createProjectWithNoSources(String className, String contents) throws Exception {
		return createProjectWithNoSources(className, contents, false);
	}

	private IJavaProject createProjectWithNoSources(String className, String contents, boolean generateLineInfo) throws Exception {
		IJavaProject javaProject = JavaProjectHelper.createJavaProject("ClassFileEditorHighlightingTest", JavaProjectHelper.BIN_DIR);
		IProject project = javaProject.getProject();
		project.getFolder(LAUNCHCONFIGURATIONS).create(true, true, null);

		IFile classFile = project.getFile(className + ".class");

		String debug = generateLineInfo ? "lines" : "none";
		compileWithJavac(className, contents, List.of("-g:" + debug, "-d", project.getLocation().toString(), "--release", "8"));
		classFile.refreshLocal(IResource.DEPTH_ONE, null);

		IFile lib = project.getFile("lib.jar");
		jarClassFile(lib, classFile);

		JavaProjectHelper.addLibrary(javaProject, lib.getFullPath());
		waitForBuild();
		return javaProject;
	}

	private static void jarClassFile(IFile jar, IFile classFile) {
		JarPackageData data = new JarPackageData();
		data.setJarLocation(jar.getLocation());
		data.setBuildIfNeeded(true);
		data.setOverwrite(true);
		data.setElements(new Object[] { classFile });
		data.setExportClassFiles(true);

		IStatus status = callInUi(() -> {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IJarExportRunnable op = data.createJarExportRunnable(window.getShell());
			window.run(false, false, op);
			return op.getStatus();
		});
		if (status.getSeverity() == IStatus.ERROR) {
			fail("Creating jar failed: " + status.getMessage());
		}
	}

	private static void expectHighlightedText(String expectedHighlightedText, long timeout) throws InterruptedException {
		long s = System.currentTimeMillis();
		while (System.currentTimeMillis() - s < timeout) {
			List<String> highlighted = callInUi(ClassFileEditorHighlightingTest::getClassEditorHighlightedText);
			if (List.of(expectedHighlightedText).equals(highlighted)) {
				break;
			}
			TestUtil.runEventLoop();
			Thread.sleep(50L);
		}
		List<String> highlighted = callInUi(ClassFileEditorHighlightingTest::getClassEditorHighlightedText);
		assertEquals("Timed out while waiting on highlighting", Arrays.asList(expectedHighlightedText), highlighted);
	}

	private static void expectNothingHighlighted(long timeout) throws InterruptedException {
		long s = System.currentTimeMillis();
		while (System.currentTimeMillis() - s < timeout) {
			List<String> highlighted = callInUi(ClassFileEditorHighlightingTest::getClassEditorHighlightedText);
			if (highlighted.isEmpty()) {
				break;
			}
			TestUtil.runEventLoop();
			Thread.sleep(50L);
		}
		List<String> highlighted = callInUi(ClassFileEditorHighlightingTest::getClassEditorHighlightedText);
		assertEquals("Timed out while waiting on highlighting", List.of(), highlighted);
	}

	private static List<String> getClassEditorHighlightedText() {
		List<String> highlighted = new ArrayList<>();
		StyledText noSourceTextWidget = null;
		ClassFileEditor editor = (ClassFileEditor) getActivePage().getActiveEditor();
		if (editor != null) {
			noSourceTextWidget = editor.getNoSourceTextWidget();
		}
		if (noSourceTextWidget != null) {
			StyleRange[] styleRanges = noSourceTextWidget.getStyleRanges();
			StyledTextContent content = noSourceTextWidget.getContent();
			for (StyleRange styleRange : styleRanges) {
				String highlightedText = content.getTextRange(styleRange.start, styleRange.length);
				highlighted.add(highlightedText);
			}
		}
		return highlighted;
	}

	private static void compileWithJavac(String className, String source, List<String> compilerOptions) throws IOException {
		JavaFileObject fileObject = new SourceJavaFileObject(className, source);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		Assume.assumeNotNull(compiler);

		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
			DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
			CompilationTask task = compiler.getTask(null, fileManager, collector, compilerOptions, null, List.of(fileObject));
			Boolean result = task.call();
			assertTrue(String.valueOf(collector.getDiagnostics()), result);
		}
	}

	private static class SourceJavaFileObject extends SimpleJavaFileObject {

		private final String code;

		protected SourceJavaFileObject(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return code;
		}
	}
}
