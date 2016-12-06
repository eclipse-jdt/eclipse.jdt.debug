/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.launching.JavaSourceLookupUtil;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.sourcelookup.containers.PackageFragmentRootSourceContainer;

/**
 * Tests for finding / showing source from jar files from related projects
 */
public class JarSourceLookupTests extends AbstractDebugTest {

	public static final String A_RUN_JAR = "a.RunJar";
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return fgJarProject;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		IPath testrpath = new Path("testresources");
		createProjectClone(fJarProject, testrpath.append(fJarProject).toString(), false);
		fgJarProject = createJavaProjectClone(RefPjName, testrpath.append(RefPjName).toString(), JavaProjectHelper.J2SE_1_4_EE_NAME, false);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		removeAllBreakpoints();
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
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, config);
		ISourceContainer[] containers = JavaSourceLookupUtil.translate(resolved);
		try {
			assertTrue("There must be computed containers", containers.length > 0);
			//the number of containers is M + 2, where M is unknown across JREs, 1 for the project container and 1 for the JAR we are looking for
			assertTrue("There should be at least 2 containers returned", containers.length >= 2);
			for (int i = 0; i < containers.length; i++) {
				if("sample.jar".equals(containers[i].getName()) &&
						containers[i] instanceof PackageFragmentRootSourceContainer) {
					PackageFragmentRootSourceContainer container = (PackageFragmentRootSourceContainer) containers[i];
					if("/JarProject/lib/sample.jar".equals(container.getPackageFragmentRoot().getPath().toString())) {
						return;
					}
				}
			}
			fail("We did not find a source container that was a PackageFragmentRootSourceContainer and had the name /JarProject/lib/sample.jar");
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
		createLineBreakpoint(16, A_RUN_JAR);
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
		createLineBreakpoint(16, A_RUN_JAR);
		ILaunchConfiguration config = getLaunchConfiguration(fgJarProject, LAUNCHCONFIGURATIONS, A_RUN_JAR);
		IJavaThread thread = null;
		try {
			 thread = launchToBreakpoint(config);
			 IStackFrame frame = thread.getTopStackFrame();
			 assertNotNull("The top stack frame cannot be null", frame);
			 assertTrue("The found frame should be an IJavaStackFrame", frame instanceof IJavaStackFrame);
			 Object source = lookupSource(frame);
			 assertNotNull("We should have found source for the main class a.RunJar", source);
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
