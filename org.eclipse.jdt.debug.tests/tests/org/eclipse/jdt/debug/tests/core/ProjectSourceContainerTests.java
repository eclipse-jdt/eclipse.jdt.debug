/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.internal.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests project source containers
 */
public class ProjectSourceContainerTests extends AbstractDebugTest {
	
	public ProjectSourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testProjectSourceContainerMemento() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, true);
		assertTrue(container.isSearchReferencedProjects());
		String memento = container.getType().getMemento(container);
		ProjectSourceContainer restore = (ProjectSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Project source container memento failed", container, restore);
		assertTrue(restore.isSearchReferencedProjects());
	}
	
	public void testSimpleSourceLookupPositive() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, false);
		Object[] objects = container.findSourceElements("Breakpoints.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", project.getFile("src/Breakpoints.java"), objects[0]);
	}
	
	public void testSimpleRootSourceLookupPositive() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, false);
		Object[] objects = container.findSourceElements(".classpath", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", project.getFile(".classpath"), objects[0]);
	}	
	
	public void testSimpleSourceLookupNegative() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, false);
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	

	public void testQualifiedSourceLookupPositive() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", project.getFile("src/org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}
	
	public void testQualifiedSourceLookupNegative() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, false);
		Object[] objects = container.findSourceElements("a/b/c/InfiniteLoop.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	
}
