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
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.internal.launching.JavaSourceLookupParticipant;

/**
 * Tests folder source containers
 */
public class FolderSourceContainerTests extends AbstractDebugTest {
	
	public FolderSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns a folder source container.
	 */
	protected FolderSourceContainer getContainer(boolean subfolders, boolean duplicates) throws Exception {
		JavaSourceLookupDirector director = new JavaSourceLookupDirector();
		director.setFindDuplicates(duplicates);
		director.addSourceLookupParticipant(new JavaSourceLookupParticipant());
		IFolder folder = getJavaProject().getProject().getFolder("src");
		FolderSourceContainer container = new FolderSourceContainer(folder, subfolders);
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}
	protected IFolder getFolder(FolderSourceContainer container) {
		return (IFolder)container.getContainer();
	}
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testFolderSourceContainerMemento() throws Exception {
		FolderSourceContainer container = getContainer(true, true);
		assertTrue(container.isComposite());
		String memento = container.getType().getMemento(container);
		FolderSourceContainer restore = (FolderSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Folder source container memento failed", container, restore);
		assertTrue(restore.isComposite());
	}
	
	public void testSimpleSourceLookupPositive() throws Exception {
		FolderSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("Breakpoints.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", getFolder(container).getFile("Breakpoints.java"), objects[0]);
	}
	
	public void testSimpleSourceLookupNegative() throws Exception {
		FolderSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}	
	
	public void testSimpleNestedSourceLookupPositive() throws Exception {
		FolderSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", getFolder(container).getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);		
	}
	
	public void testSimpleNestedSourceLookupNegative() throws Exception {
		FolderSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);		
	}
	
	public void testQualifiedSourceLookupPositive() throws Exception {
		FolderSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", getFolder(container).getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);
	}
	
	public void testQualifiedSourceLookupNegative() throws Exception {
		FolderSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}
	
	public void testPartiallyQualifiedNestedSourceLookupPositive() throws Exception {
		FolderSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", getFolder(container).getFile(new Path("org/eclipse/debug/tests/targets/InfiniteLoop.java")), objects[0]);
	}	
}
