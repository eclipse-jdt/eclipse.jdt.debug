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

import org.eclipse.core.resources.IFolder;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests source containers
 */
public class WorkspaceSourceContainerTests extends AbstractDebugTest {
	
	public WorkspaceSourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testWorkspaceSourceContainerMemento() throws Exception {
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		String memento = container.getType().getMemento(container);
		ISourceContainer restore = container.getType().createSourceContainer(memento);
		assertEquals("Workspace source container memento failed", container, restore);
	}		

	public void testSimpleSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		Object[] objects = container.findSourceElements("Breakpoints.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile("Breakpoints.java"), objects[0]);
	}
	
	public void testSimpleSourceLookupNegative() throws Exception {
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	
	
	public void testQualifiedSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile("org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}
	
	public void testQualifiedSourceLookupNegative() throws Exception {
		WorkspaceSourceContainer container = new WorkspaceSourceContainer();
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	
}
