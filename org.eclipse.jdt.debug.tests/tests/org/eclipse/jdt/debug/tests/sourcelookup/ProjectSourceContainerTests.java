/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.sourcelookup;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

/**
 * Tests project source containers
 */
public class ProjectSourceContainerTests extends AbstractDebugTest {

	public ProjectSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns a project source container.
	 */
	protected ProjectSourceContainer getContainer(boolean referenced, boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		IProject project = get14Project().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, referenced);
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}

	/**
	 * Tests creation and restoring from a memento.
	 *
	 * @throws Exception
	 */
	public void testProjectSourceContainerMemento() throws Exception {
		ProjectSourceContainer container = getContainer(true, true);
		assertTrue(container.isSearchReferencedProjects());
		String memento = container.getType().getMemento(container);
		ProjectSourceContainer restore = (ProjectSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Project source container memento failed", container, restore);
		assertTrue(restore.isSearchReferencedProjects());
	}

	public void testSimpleSourceLookupPositive() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("Breakpoints.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", container.getProject().getFile("src/Breakpoints.java"), objects[0]);
	}

	public void testSimpleRootSourceLookupPositive() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements(".classpath");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", container.getProject().getFile(".classpath"), objects[0]);
	}

	public void testSimpleSourceLookupNegative() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testQualifiedSourceLookupPositive() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", container.getProject().getFile("src/org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}

	public void testQualifiedSourceLookupNegative() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("a/b/c/InfiniteLoop.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testCaseSensitiveQualifiedSourceLookup() throws Exception {
		ProjectSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("oRg/eClIpSe/dEbUg/tEsTs/tArGeTs/INfInItELOop.jaVa");
		if (isFileSystemCaseSensitive()) {
			// case sensitive - should not find the file
			assertEquals("Expected 0 files", 0, objects.length);
		} else {
			// case insensitive - should find the file
			assertEquals("Expected 1 result", 1, objects.length);
			assertEquals("Wrong file", container.getProject().getFile("src/org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
		}
	}
}
