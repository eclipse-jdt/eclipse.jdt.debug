/*******************************************************************************
 * Copyright (c) 2011, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaProjectHelper;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.launching.JavaSourceLookupUtil;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;

/**
 * Tests for finding / showing source from jar files from related projects
 */
public class JarSourceLookupTests extends AbstractDebugTest {

	private static final String SAMPLE_JAR_PATH = "/JarProject/lib/sample.jar";
	public static final String A_RUN_JAR = "testJar.RunJar";
	static IJavaProject fgJarProject = null;

	String RefPjName = "JarRefProject";
	String fJarProject = "JarProject";

	/**
	 * Constructor
	 */
	public JarSourceLookupTests() {
		super("JarSourceLookupTests");
	}

	/**
	 * Disposes all source containers after a test, ensures no containers are still holding open Jar references, which can lead to {@link ResourceException}s
	 * when we try to delete / setup following tests
	 * @param containers
	 */
	void disposeContainers(ISourceContainer[] containers) {
		if(containers != null) {
			for (int i = 0; i < containers.length; i++) {
				containers[i].dispose();
			}
		}
	}

	@Override
	protected IJavaProject getProjectContext() {
		return fgJarProject;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TestUtil.cleanUp(getName());
		IPath testrpath = new Path("testresources");
		IProject jarProject = createProjectClone(fJarProject, testrpath.append(fJarProject).toString(), true);

		IFile jar = jarProject.getFile("lib/sample.jar");
		assertTrue("lib/sample.jar is missing in project: " + jarProject.getName(), jar.exists());

		fgJarProject = createJavaProjectClone(RefPjName, testrpath.append(RefPjName).toString(), JavaProjectHelper.JAVA_SE_1_7_EE_NAME, true);

		IProject jarRefProject = fgJarProject.getProject();
		IFile cp = jarRefProject.getFile(".classpath");
		assertTrue(".classpath is missing in project: " + jarRefProject.getName(), cp.exists());
		java.nio.file.Path path = cp.getLocation().toFile().toPath();
		List<String> lines = Files.readAllLines(path);
		boolean foundJar = false;
		for (String line : lines) {
			if (line.contains(SAMPLE_JAR_PATH)) {
				foundJar = true;
				break;
			}
		}
		if (!foundJar) {
			fail("The .classpath from project " + jarRefProject + " is unexpected and does not have an entry for " + SAMPLE_JAR_PATH + ": "
					+ new String(Files.readAllBytes(path)));
		}
		waitForBuild();
	}

	@Override
	protected void tearDown() throws Exception {
		removeAllBreakpoints();
		if (fgJarProject.exists()) {
			IProject project = fgJarProject.getProject();
			// Before deleting, let indexer to finish his work to avoid error below (see 516351)
			TestUtil.waitForJobs(getName(), 100, 3000);
			try {
				project.delete(true, null);
			}
			catch (ResourceException e) {
				// Indexer still running on our jars?
				TestUtil.waitForJobs(getName(), 1000, 5000);
				if (project.exists()) {
					project.delete(true, null);
				}
			}
		}
		super.tearDown();
	}

	/**
	 * Ensures the translation of source containers yields the correct containers
	 *
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=346116
	 *
	 * @throws Exception
	 */
	public void testTranslateContainers() throws Exception {
		createLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		ILaunchConfiguration config = getLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(config);
		if (JavaRuntime.isModularConfiguration(config)) {
			// There is no DefaultProjectClasspath entry so 2 entries inside it becomes 2 separate entries now
			assertEquals("There should be 3 containers returned (JRE and classpath)", 3, entries.length);
		} else {
			assertEquals("There should be 2 containers returned (JRE and classpath)", 2, entries.length);
		}
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, config);
		ISourceContainer[] containers = JavaSourceLookupUtil.translate(resolved);
		try {
			assertTrue("There must be computed containers", containers.length > 0);
			//the number of containers is M + 2, where M is unknown across JREs, 1 for the project container and 1 for the JAR we are looking for
			assertTrue("There should be at least 2 containers returned", containers.length >= 2);
			for (int i = 0; i < containers.length; i++) {
				ISourceContainer sourceContainer = containers[i];
				if ("sample.jar".equals(sourceContainer.getName()) && sourceContainer instanceof PackageFragmentRootSourceContainer) {
					PackageFragmentRootSourceContainer container = (PackageFragmentRootSourceContainer) sourceContainer;
					if (SAMPLE_JAR_PATH.equals(container.getPackageFragmentRoot().getPath().toString())) {
						return;
					}
				}
			}
			StringBuilder dump = new StringBuilder();
			for (ISourceContainer sc : containers) {
				dump.append(sc.getName());
				if (sc instanceof PackageFragmentRootSourceContainer) {
					PackageFragmentRootSourceContainer pfsc = (PackageFragmentRootSourceContainer) sc;
					dump.append(" with path: ").append(pfsc.getPath());
				}
				dump.append(", ");
			}
			dump.setLength(dump.length() - 2);
			dump.append(".\n Those containers were resolved from: ");
			for (IRuntimeClasspathEntry cpe : resolved) {
				dump.append(cpe);
				dump.append(", ");
			}

			dump.setLength(dump.length() - 2);
			fail("We did not find a source container that was a PackageFragmentRootSourceContainer "
					+ "and had the name " + SAMPLE_JAR_PATH + ", but found source containers: " + dump);
		}
		finally {
			disposeContainers(containers);
		}
	}

	/**
	 * Tests that the class file is found as source when the lookup is done from a jar from another project
	 *
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=346116
	 *
	 * @throws Exception
	 */
	public void testInspectClassFileFromJar() throws Exception {
		createLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		createLineBreakpoint(21, A_RUN_JAR);
		ILaunchConfiguration config = getLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		IJavaThread thread = null;
		try {
			 thread = launchToBreakpoint(config);
			 IStackFrame frame = thread.getTopStackFrame();
			 assertTrue("The found frame should be an IJavaStackFrame", frame instanceof IJavaStackFrame);
			 stepInto((IJavaStackFrame)frame);
			 assertNotNull("the stack frame from the thread cannot be null", frame);
			 IValue value = doEval(thread, "this");
			 assertNotNull("The evaluation result cannot be null", value);
			 assertEquals("the name of the type being inspected must be a.JarClass", "a.JarClass", value.getReferenceTypeName());
		}
		finally {
			terminateAndRemove(thread);
		}
	}

	/**
	 * Tests that the class file is found as source when the lookup is done from a jar from another project
	 *
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=346116
	 *
	 * @throws Exception
	 */
	public void testShowClassFileFromJar() throws Exception {
		createLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		createLineBreakpoint(21, A_RUN_JAR);
		ILaunchConfiguration config = getLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		IJavaThread thread = null;
		try {
			 thread = launchToBreakpoint(config);
			 IStackFrame frame = thread.getTopStackFrame();
			 assertNotNull("The top stack frame cannot be null", frame);
			 assertTrue("The found frame should be an IJavaStackFrame", frame instanceof IJavaStackFrame);
			 Object source = lookupSource(frame);
			assertNotNull("We should have found source for the main class testJar.RunJar", source);
			 assertTrue("The found source should be an IFile", source instanceof IFile);
			 assertEquals("We should have found a file named RunJar.java", ((IFile)source).getName(), "RunJar.java");

			 stepInto((IJavaStackFrame)frame);
			 frame = thread.getTopStackFrame();
			 assertNotNull("The top stack frame cannot be null", frame);

			 source = lookupSource(frame);
			 assertNotNull("We should have found source for the jar class a.JarClass", source);
			 assertTrue("The found source should be a ClassFile", source instanceof ClassFile);
			 assertEquals("we should have found a file named a.JarClass.class", ((ClassFile)source).getElementName(), "JarClass.class");
		}
		finally {
			terminateAndRemove(thread);
		}
	}

	/**
	 * Looks up source for the given frame using its backing {@link ISourceLocator} from its {@link ILaunch}
	 * @param frame the frame to look up source for
	 * @return the source object or <code>null</code>
	 */
	Object lookupSource(IStackFrame frame) {
		ISourceLocator locator = frame.getLaunch().getSourceLocator();
		assertNotNull("The default Java source locator cannot be null", locator);
		return locator.getSourceElement(frame);
	}
}
