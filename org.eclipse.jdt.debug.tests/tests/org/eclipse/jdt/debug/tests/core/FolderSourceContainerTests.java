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
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests folder source containers
 */
public class FolderSourceContainerTests extends AbstractDebugTest {
	
	public FolderSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testFolderSourceContainerMemento() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, true);
		assertTrue(container.isComposite());
		String memento = container.getType().getMemento(container);
		FolderSourceContainer restore = (FolderSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Folder source container memento failed", container, restore);
		assertTrue(restore.isComposite());
	}
	
	public void testSimpleSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, false);
		Object[] objects = container.findSourceElements("Breakpoints.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile("Breakpoints.java"), objects[0]);
	}
	
	public void testSimpleSourceLookupNegative() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, false);
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	
	
	public void testSimpleNestedSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, true);
		Object[] objects = container.findSourceElements("InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);		
	}
	
	public void testSimpleNestedSourceLookupNegative() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, true);
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);		
	}
	
	public void testQualifiedSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);
	}
	
	public void testQualifiedSourceLookupNegative() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, false);
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}
	
	public void testPartiallyQualifiedNestedSourceLookupPositive() throws Exception {
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, true);
		Object[] objects = container.findSourceElements("debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", folder.getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);
	}	
}
