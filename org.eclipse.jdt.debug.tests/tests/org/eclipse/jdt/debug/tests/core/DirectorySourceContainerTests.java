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

import java.io.File;

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests directory source containers
 */
public class DirectorySourceContainerTests extends AbstractDebugTest {
	
	public DirectorySourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testDirectorySourceContainerMemento() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, true);
		assertTrue(container.isComposite());
		String memento = container.getType().getMemento(container);
		ISourceContainer restore = container.getType().createSourceContainer(memento);
		assertEquals("Directory source container memento failed", container, restore);
		assertTrue(restore.isComposite());
	}	

	public void testSimpleSourceLookupPositive() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, false);
		Object[] objects = container.findSourceElements("Breakpoints.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(folder, "Breakpoints.java"), objects[0]);
	}
	
	public void testSimpleSourceLookupNegative() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, false);
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}	
	
	public void testSimpleNestedSourceLookupPositive() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, true);
		Object[] objects = container.findSourceElements("InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(folder, "org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);		
	}
	
	public void testSimpleNestedSourceLookupNegative() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, true);
		Object[] objects = container.findSourceElements("FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);		
	}
	
	public void testQualifiedSourceLookupPositive() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(folder, "org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}
	
	public void testQualifiedSourceLookupNegative() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, false);
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java", false);
		assertEquals("Expected 0 files", 0, objects.length);
	}
	
	public void testPartiallyQualifiedNestedSourceLookupPositive() throws Exception {
		File folder = getJavaProject().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, true);
		Object[] objects = container.findSourceElements("debug/tests/targets/InfiniteLoop.java", false);
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(folder, "org/eclipse/debug/tests/targets/InfiniteLoop.java"), objects[0]);
	}	
}
