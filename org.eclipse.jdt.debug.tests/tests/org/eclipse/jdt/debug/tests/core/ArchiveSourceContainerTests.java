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

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.internal.launching.JavaSourceLookupParticipant;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Tests archive source containers
 */
public class ArchiveSourceContainerTests extends AbstractDebugTest {
	
	public ArchiveSourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Returns the JREs source archive.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected ArchiveSourceContainer getContainer(boolean detect, boolean duplicates) throws Exception {
		JavaSourceLookupDirector director = new JavaSourceLookupDirector();
		director.setFindDuplicates(duplicates);
		director.addSourceLookupParticipant(new JavaSourceLookupParticipant());
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(JavaRuntime.getDefaultVMInstall());
		for (int i = 0; i < locations.length; i++) {
			LibraryLocation location = locations[i];
			IPath path = location.getSystemLibrarySourcePath();
			if (path != null && !path.isEmpty()) {
				ArchiveSourceContainer container = new ArchiveSourceContainer(path.toOSString(), detect);
				director.setSourceContainers(new ISourceContainer[]{container});
				return container;
			}
		}
		assertTrue("Did not find JRE source archive", false);
		return null;
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testArchiveSourceContainerMemento() throws Exception {
		ArchiveSourceContainer container = getContainer(true, false);
		assertFalse(container.isComposite());
		assertTrue(container.isDetectRoot());
		String memento = container.getType().getMemento(container);
		ArchiveSourceContainer restore = (ArchiveSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Directory source container memento failed", container, restore);
		assertFalse(restore.isComposite());
		assertTrue(restore.isDetectRoot());
	}	

	public void testAutoDetectRootSourceLookupPositive() throws Exception {
		ArchiveSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("java/lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}
	
	public void testAutoDetectRootSourceLookupNegative() throws Exception {
		ArchiveSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("java/lang/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}	
	
	public void testSourceLookupPositive() throws Exception {
		ArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("java/lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}
	
	public void testSourceLookupNegative() throws Exception {
		ArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("java/lang/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}
		
	public void testPartiallyQualifiedSourceLookupPositive() throws Exception {
		ArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}	
}
