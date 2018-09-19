/*******************************************************************************
 *  Copyright (c) 2004, 2008 IBM Corporation and others.
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ZipEntryStorage;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

/**
 * Tests external archive source containers
 */
public class ExternalArchiveSourceContainerTests extends AbstractDebugTest {

	public ExternalArchiveSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns the JREs source archive.
	 *
	 * @return
	 * @throws Exception
	 */
	protected ExternalArchiveSourceContainer getContainer(boolean detect, boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(JavaRuntime.getDefaultVMInstall());
		for (int i = 0; i < locations.length; i++) {
			LibraryLocation location = locations[i];
			IPath path = location.getSystemLibrarySourcePath();
			if (path != null && !path.isEmpty()) {
				ExternalArchiveSourceContainer container = new ExternalArchiveSourceContainer(path.toOSString(), detect);
				director.setSourceContainers(new ISourceContainer[]{container});
				return container;
			}
		}
		assertTrue("Did not find JRE source archive. This failure is expected if you are running the tests with a JRE that does not contain source.", false);
		return null;
	}

	/**
	 * Returns the source archive at the specified path within this plug-in.
	 */
	protected ExternalArchiveSourceContainer getContainer(String path, boolean detect, boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		IPath p = new Path(path);
		File file = JavaTestPlugin.getDefault().getFileInPlugin(p);
		assertTrue("file " + path +  " does not exist", file != null && file.exists());
		ExternalArchiveSourceContainer container = new ExternalArchiveSourceContainer(file.getAbsolutePath(), detect);
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}

	/**
	 * Tests creation and restoring from a memento.
	 *
	 * @throws Exception
	 */
	public void testArchiveSourceContainerMemento() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(true, false);
		assertFalse(container.isComposite());
		assertTrue(container.isDetectRoot());
		String memento = container.getType().getMemento(container);
		ExternalArchiveSourceContainer restore = (ExternalArchiveSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Directory source container memento failed", container, restore);
		assertFalse(restore.isComposite());
		assertTrue(restore.isDetectRoot());
	}

	public void testAutoDetectRootSourceLookupPositive() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("java/lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}

	public void testAutoDetectRootSourceLookupNegative() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(true, false);
		Object[] objects = container.findSourceElements("java/lang/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testSourceLookupPositive() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("java/lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}

	public void testSourceLookupNegative() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("java/lang/FileNotFound.java");
		assertEquals("Expected 0 files", 0, objects.length);
	}

	public void testPartiallyQualifiedSourceLookupPositive() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(false, false);
		Object[] objects = container.findSourceElements("lang/Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}

	public void testAutoDetectUnqualifiedSourceLookupPositive() throws Exception {
		ExternalArchiveSourceContainer container = getContainer(true, false);
		// force detection
		Object[] objects = container.findSourceElements("java/lang/Object.java");
		// then search for unqualified file
		objects = container.findSourceElements("Object.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Object.java", storage.getName());
	}

	public void testAutoDetectMultipleRoots() throws Exception {
		ExternalArchiveSourceContainer container = getContainer("testresources/source-test.zip", true, false);
		// find .java file
		Object[] objects = container.findSourceElements("one/two/Three.java");
		assertEquals("Expected 1 result", 1, objects.length);
		ZipEntryStorage storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "Three.java", storage.getName());
		// find .txt file
		objects = container.findSourceElements("another/file-b.txt");
		storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "file-b.txt", storage.getName());
		// find another .txt file
		objects = container.findSourceElements("folder/file-c.txt");
		storage = (ZipEntryStorage) objects[0];
		assertEquals("Wrong file", "file-c.txt", storage.getName());
	}

	/**
	 * Test for bug 515941 (ConcurrentModificationException on container dispose)
	 */
	public void testDisposed() throws Exception {
		int threads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		AtomicReference<Throwable> err = new AtomicReference<>();
		AtomicReference<ExternalArchiveSourceContainer> container = new AtomicReference<>();
		AtomicReference<Boolean> stop = new AtomicReference<>(Boolean.FALSE);
		CountDownLatch latch = new CountDownLatch(1);
		for (int i = 0; i < threads; i++) {
			exec.submit(() -> {
				try {
					latch.await();
					while (!stop.get()) {
						ExternalArchiveSourceContainer c = container.get();
						if (c != null) {
							c.dispose();
						}
					}
				}
				catch (Throwable e) {
					err.set(e);
				}
			});
		}
		latch.countDown();
		try {
			for (int i = 0; i < 5_000; i++) {
				ExternalArchiveSourceContainer cont = getContainer("testresources/source-test.zip", true, false);
				try {
					container.set(cont);
					Thread.sleep(1);
					cont.findSourceElements("one/two/Three.java");

				}
				finally {
					cont.dispose();
				}
			}
		}
		finally {
			stop.set(true);
			exec.shutdownNow();
		}
	}
}
