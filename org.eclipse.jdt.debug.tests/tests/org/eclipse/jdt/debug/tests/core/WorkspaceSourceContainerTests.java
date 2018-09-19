/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

/**
 * Tests source containers
 */
public class WorkspaceSourceContainerTests extends AbstractDebugTest {

	public WorkspaceSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns a workspace source container.
	 */
	protected WorkspaceSourceContainer getContainer(boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}
	/**
	 * Tests creation and restoring from a memento.
	 *
	 * @throws Exception
	 */
	public void testWorkspaceSourceContainerMemento() throws Exception {
		WorkspaceSourceContainer container = getContainer(true);
		String memento = container.getType().getMemento(container);
		ISourceContainer restore = container.getType().createSourceContainer(memento);
		assertEquals("Workspace source container memento failed", container, restore);
	}

	public void testSimpleSourceLookupPositive() throws Exception {
		IFolder folder = get14Project().getProject().getFolder("src");
		WorkspaceSourceContainer container = getContainer(false);
		Object[] objects = container.findSourceElements("Breakpoints.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile("Breakpoints.java"), objects[0]);
	}

	public void testSimpleSourceLookupNegative() throws Exception {
		WorkspaceSourceContainer container = getContainer(false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testQualifiedSourceLookupPositive() throws Exception {
		IFolder folder = get14Project().getProject().getFolder("src");
		WorkspaceSourceContainer container = getContainer(false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile("org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}

	public void testQualifiedSourceLookupNegative() throws Exception {
		WorkspaceSourceContainer container = getContainer(false);
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testCaseSensitiveQualifiedSourceLookup() throws Exception {
		IFolder folder = get14Project().getProject().getFolder("src");
		WorkspaceSourceContainer container = getContainer(false);
		Object[] objects = container.findSourceElements("oRg/eClIpSe/dEbUg/tEsTs/tArGeTs/INfInItELOop.jaVa");
		if (isFileSystemCaseSensitive()) {
			// case sensitive - should not find the file
			assertEquals("Expected 0 files", 0, objects.length);
		} else {
			// case insensitive - should find the file
			assertEquals("Expected 1 result", 1, objects.length);
			assertEquals("Wrong file", folder.getFile("org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
		}
	}
}
