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

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

/**
 * Tests directory source containers
 */
public class DirectorySourceContainerTests extends AbstractDebugTest {

	public DirectorySourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns a directory source container.
	 */
	protected DirectorySourceContainer getContainer(boolean subfolders, boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		File folder = get14Project().getProject().getFolder("src").getLocation().toFile();
		DirectorySourceContainer container = new DirectorySourceContainer(folder, subfolders);
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}

	/**
	 * Tests creation and restoring from a memento.
	 *
	 * @throws Exception
	 */
	public void testDirectorySourceContainerMemento() throws Exception {
		DirectorySourceContainer container = getContainer(true, true);
		assertTrue(container.isComposite());
		String memento = container.getType().getMemento(container);
		ISourceContainer restore = container.getType().createSourceContainer(memento);
		assertEquals("Directory source container memento failed", container, restore);
		assertTrue(restore.isComposite());
	}

	public void testSimpleSourceLookupPositive() throws Exception {
		DirectorySourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("Breakpoints.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(container.getDirectory(), "Breakpoints.java"), ((LocalFileStorage)objects[0]).getFile());
	}

	public void testSimpleSourceLookupNegative() throws Exception {
		DirectorySourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testSimpleNestedSourceLookupPositive() throws Exception {
		DirectorySourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(container.getDirectory(), "org/eclipse/debug/tests/targets/InfiniteLoop.java"), ((LocalFileStorage)objects[0]).getFile());
	}

	public void testSimpleNestedSourceLookupNegative() throws Exception {
		DirectorySourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testQualifiedSourceLookupPositive() throws Exception {
		DirectorySourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(container.getDirectory(), "org/eclipse/debug/tests/targets/InfiniteLoop.java"), ((LocalFileStorage)objects[0]).getFile());
	}

	public void testQualifiedSourceLookupNegative() throws Exception {
		DirectorySourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("a/b/c/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testPartiallyQualifiedNestedSourceLookupPositive() throws Exception {
		DirectorySourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("debug/tests/targets/InfiniteLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		assertEquals("Wrong file", new File(container.getDirectory(), "org/eclipse/debug/tests/targets/InfiniteLoop.java"), ((LocalFileStorage)objects[0]).getFile());
	}

	public void testCaseSensitiveQualifiedSourceLookup() throws Exception {
		DirectorySourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("oRg/eClIpSe/dEbUg/tEsTs/tArGeTs/INfInItELOop.jaVa");
		if (isFileSystemCaseSensitive()) {
			// case sensitive - should not find the file
			assertEquals("Expected 0 files", 0, objects.length);
		} else {
			// case insensitive - should find the file
			assertEquals("Expected 1 result", 1, objects.length);
			if (Platform.OS_MACOSX.equals(Platform.getOS())) {
				assertEquals("Wrong file", new File(container.getDirectory(), "oRg/eClIpSe/dEbUg/tEsTs/tArGeTs/INfInItELOop.jaVa"), ((LocalFileStorage)objects[0]).getFile());
			} else {
				assertEquals("Wrong file", new File(container.getDirectory(), "org/eclipse/debug/tests/targets/InfiniteLoop.java"), ((LocalFileStorage)objects[0]).getFile());
			}
		}
	}
}
