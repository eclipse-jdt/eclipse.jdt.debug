/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.hcr.CompilationUnitDelta;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Tests the source change detection done by {@link CompilationUnitDelta}, which is used by the hot code replace to decide which stack frames are
 * affected by a class reload and therefore have to be dropped.
 * <p>
 * The delta compares the current source of a compilation unit with the source stored in the local history. If the local history is not accessible at
 * all - which is the case for {@link IFile#isContentRestricted() content restricted} files - the changes must be assumed to be relevant, so that the
 * hot code replace is not silently skipped, see {@link #testContentRestrictedFileIsAssumedToBeChanged()}.
 * </p>
 * <p>
 * Beside the unit tests of the delta itself, {@link #testHcrDropsFramesOfContentRestrictedSourceFile()} runs a real debug session and verifies that
 * the frames affected by a change of a content restricted source file are dropped.
 * </p>
 */
public class HcrCompilationUnitDeltaTests extends AbstractDebugTest {

	/**
	 * Notified when a hot code replace has been performed by the debug target.
	 */
	static class HcrListener implements IJavaHotCodeReplaceListener {

		final CountDownLatch notification = new CountDownLatch(1);
		volatile IJavaDebugTarget succeededFor;

		@Override
		public void hotCodeReplaceFailed(IJavaDebugTarget target, DebugException exception) {
			notification.countDown();
		}

		@Override
		public void hotCodeReplaceSucceeded(IJavaDebugTarget target) {
			succeededFor = target;
			notification.countDown();
		}

		@Override
		public void obsoleteMethods(IJavaDebugTarget target) {
			notification.countDown();
		}

		boolean waitNotification() {
			try {
				return notification.await(AbstractDebugTest.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
	}

	private static final String PROJECT_NAME = "HcrCompilationUnitDelta";
	private static final String CU_NAME = "HcrDeltaSource.java";
	private static final String TYPE_NAME = "HcrDeltaSource";

	private static final String ORIGINAL_SOURCE = """
			public class HcrDeltaSource {
				public void changed() {
					System.out.println("one");
				}
				public void unchanged() {
					System.out.println("same");
				}
			}
			""";

	private static final String MODIFIED_SOURCE = """
			public class HcrDeltaSource {
				public void changed() {
					System.out.println("two");
				}
				public void unchanged() {
					System.out.println("same");
				}
			}
			""";

	private IJavaProject javaProject;
	private ICompilationUnit compilationUnit;

	/**
	 * Type used by the system test, contained in the shared 1.4 test project.
	 */
	private static final String TARGET_TYPE = "org.eclipse.debug.tests.targets.HcrClass";
	private static final String TARGET_PACKAGE = "org.eclipse.debug.tests.targets";
	private static final String TARGET_CU_NAME = "HcrClass.java";
	/**
	 * The line of <code>HcrClass#four()</code> the breakpoint of the system test is set to.
	 */
	private static final int TARGET_BREAKPOINT_LINE = 42;

	public HcrCompilationUnitDeltaTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		javaProject = createProjectWithCompilationUnit();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			restoreTargetSourceFile();
			if (javaProject != null && javaProject.getProject().exists()) {
				JavaProjectHelper.delete(javaProject);
			}
		} finally {
			javaProject = null;
			compilationUnit = null;
			super.tearDown();
		}
	}

	/**
	 * The content of a content restricted file cannot be read from the local history, so it is unknown whether a method has been changed or not. In
	 * that case the method has to be reported as changed, otherwise the hot code replace would not drop any frame.
	 */
	public void testContentRestrictedFileIsAssumedToBeChanged() throws Exception {
		IFile file = (IFile) compilationUnit.getUnderlyingResource();
		file.setContentRestricted(true);
		assertTrue("The file is expected to be content restricted", file.isContentRestricted());

		CompilationUnitDelta delta = new CompilationUnitDelta(compilationUnit, System.currentTimeMillis());

		assertTrue("A method of a content restricted file must be reported as changed",
				delta.hasChanged(TYPE_NAME, "unchanged", "()V"));
	}

	/**
	 * Without any local history there is nothing to compare with, so nothing is reported as changed (optimistic assumption).
	 */
	public void testFileWithoutHistoryIsAssumedToBeUnchanged() throws Exception {
		IFile file = (IFile) compilationUnit.getUnderlyingResource();
		assertFalse("The file is not expected to be content restricted", file.isContentRestricted());
		assertEquals("The file is not expected to have a local history", 0, file.getHistory(null).length);

		CompilationUnitDelta delta = new CompilationUnitDelta(compilationUnit, System.currentTimeMillis());

		assertFalse("Without local history no change can be detected", delta.hasChanged(TYPE_NAME, "changed", "()V"));
	}

	/**
	 * If the local history is accessible, only the methods which have really been changed must be reported as changed.
	 */
	public void testChangedMethodDetectedViaLocalHistory() throws Exception {
		IFile file = (IFile) compilationUnit.getUnderlyingResource();
		setContentsKeepingHistory(file, MODIFIED_SOURCE);
		assertTrue("The file is expected to have a local history", file.getHistory(null).length > 0);

		CompilationUnitDelta delta = new CompilationUnitDelta(compilationUnit, System.currentTimeMillis());

		assertTrue("The changed method was not detected", delta.hasChanged(TYPE_NAME, "changed", "()V"));
		assertFalse("An unchanged method was reported as changed", delta.hasChanged(TYPE_NAME, "unchanged", "()V"));
	}

	/**
	 * System test: launches a debug session, suspends it at a breakpoint and changes the source of a <b>content restricted</b> file, which has no
	 * local history at all.
	 * <p>
	 * Since the previous source of such a file is not available, the changes have to be assumed to be relevant: the affected frames have to be
	 * dropped, otherwise the running program would continue to execute the old code of the already replaced class.
	 * </p>
	 */
	public void testHcrDropsFramesOfContentRestrictedSourceFile() throws Exception {
		createLineBreakpoint(TARGET_BREAKPOINT_LINE, TARGET_TYPE);
		HcrListener listener = new HcrListener();
		JDIDebugModel.addHotCodeReplaceListener(listener);
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(TARGET_TYPE);
			assertNotNull("Breakpoint not hit within timeout period", thread);

			IJavaDebugTarget target = (IJavaDebugTarget) thread.getDebugTarget();
			if (!target.supportsHotCodeReplace()) {
				System.err.println("Warning: HCR test skipped since target VM does not support HCR.");
				return;
			}
			IJavaStackFrame frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertEquals("Should be suspended in method 'four'", "four", frame.getMethodName());
			assertEquals("Should be suspended at the breakpoint", TARGET_BREAKPOINT_LINE, frame.getLineNumber());
			removeAllBreakpoints();

			IFile file = (IFile) getTargetCompilationUnit().getUnderlyingResource();
			// simulate a file whose content must not be stored anywhere: no local history is available for it
			file.clearHistory(null);
			file.setContentRestricted(true);
			assertEquals("The file is not expected to have a local history", 0, file.getHistory(null).length);

			// now do the HCR: change the code of 'HcrClass#one()' without keeping any local history
			DebugElementEventWaiter waiter = new DebugElementEventWaiter(DebugEvent.SUSPEND, thread);
			replaceInFile(file, "\"One\"", "\"Two\"");
			waitForBuild();
			assertEquals("Changing the file must not have created a local history", 0, file.getHistory(null).length);

			assertNotNull("The thread was not suspended after the hot code replace", waiter.waitForEvent());
			assertTrue("No hot code replace was performed", listener.waitNotification());
			assertNotNull("The hot code replace failed", listener.succeededFor);
			assertTrue("The thread should be suspended after the hot code replace", thread.isSuspended());

			frame = (IJavaStackFrame) thread.getTopStackFrame();
			assertNotNull("No top stack frame", frame);
			assertEquals("Should have dropped to a frame of the replaced type", "four", frame.getMethodName());
			assertTrue("The affected frame was not dropped, the thread is still suspended at line " + frame.getLineNumber()
					+ ": the changes of a content restricted file must be assumed to be relevant",
					frame.getLineNumber() < TARGET_BREAKPOINT_LINE);
		} finally {
			JDIDebugModel.removeHotCodeReplaceListener(listener);
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private ICompilationUnit getTargetCompilationUnit() throws Exception {
		ICompilationUnit cu = getCompilationUnit(get14Project(), JavaProjectHelper.SRC_DIR, TARGET_PACKAGE, TARGET_CU_NAME).getPrimary();
		assertTrue(TARGET_CU_NAME + " does not exist", cu.exists());
		return cu;
	}

	/**
	 * Reverts the source file used by the system test and removes its content restriction.
	 */
	private void restoreTargetSourceFile() throws Exception {
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(ONE_FOUR_PROJECT_NAME).exists()) {
			// the system test did not run, so there is nothing to restore
			return;
		}
		ICompilationUnit cu = getCompilationUnit(get14Project(), JavaProjectHelper.SRC_DIR, TARGET_PACKAGE, TARGET_CU_NAME).getPrimary();
		if (!cu.exists()) {
			return;
		}
		IFile file = (IFile) cu.getUnderlyingResource();
		file.setContentRestricted(false);
		if (readFile(file).contains("\"Two\"")) {
			replaceInFile(file, "\"Two\"", "\"One\"");
			waitForBuild();
		}
	}

	/**
	 * Replaces the first occurrence of the given text in the given file, without keeping any local history.
	 */
	private static void replaceInFile(IFile file, String oldText, String newText) throws Exception {
		String contents = readFile(file);
		int index = contents.indexOf(oldText);
		assertTrue("Could not find the code to replace: " + oldText, index >= 0);
		String newContents = contents.substring(0, index) + newText + contents.substring(index + oldText.length());
		file.setContents(new ByteArrayInputStream(newContents.getBytes(Charset.forName(file.getCharset()))), IResource.FORCE, null);
	}

	private static String readFile(IFile file) throws Exception {
		try (InputStream stream = file.getContents()) {
			return new String(stream.readAllBytes(), Charset.forName(file.getCharset()));
		}
	}

	private IJavaProject createProjectWithCompilationUnit() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		if (project.exists()) {
			project.delete(true, true, null);
		}
		IJavaProject javaProject = JavaProjectHelper.createJavaProject(PROJECT_NAME, JavaProjectHelper.BIN_DIR);
		IPackageFragmentRoot source = JavaProjectHelper.addSourceContainer(javaProject, JavaProjectHelper.SRC_DIR);
		JavaProjectHelper.addContainerEntry(javaProject, new Path(JavaRuntime.JRE_CONTAINER));

		IPackageFragment defaultPackage = source.getPackageFragment("");
		compilationUnit = defaultPackage.createCompilationUnit(CU_NAME, ORIGINAL_SOURCE, true, null);
		waitForBuild();
		return javaProject;
	}

	/**
	 * Replaces the content of the given file, keeping the previous content in the local history.
	 */
	private static void setContentsKeepingHistory(IFile file, String contents) throws Exception {
		file.setContents(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)),
				IResource.FORCE | IResource.KEEP_HISTORY, null);
		waitForBuild();
	}
}
