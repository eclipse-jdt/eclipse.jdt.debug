/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.debug.testplugin.efs.WrapperFileSystem;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.hcr.JavaHotCodeReplaceManager;

/**
 * Tests the detection of changed class files done by the
 * <code>JavaHotCodeReplaceManager.ChangedClassFilesVisitor</code>, which is the first (and mandatory) step of every hot code replace attempt.
 * <p>
 * The visitor has to be able to read the byte code of every changed class file resource, no matter if the resource is stored in the local file system
 * or in an arbitrary (EFS based) file system, see {@link #testChangedClassFilesInNonLocalProject()}.
 * </p>
 */
public class HcrChangedClassFilesTests extends AbstractDebugTest {

	/**
	 * Type used to produce the "original" content of the class file under test.
	 */
	static class ClassOne {
		@Override
		public String toString() {
			return "One";
		}
	}

	/**
	 * Type used to produce the "changed" content of the class file under test.
	 */
	static class ClassTwo {
		@Override
		public String toString() {
			return "Two";
		}
	}

	/**
	 * Collects the class files detected by the HCR manager for all POST_BUILD events it receives.
	 */
	static class ChangedClassFilesCollector implements IResourceChangeListener {

		final List<IResource> resources = Collections.synchronizedList(new ArrayList<>());
		final List<String> names = Collections.synchronizedList(new ArrayList<>());
		final AtomicInteger events = new AtomicInteger();
		volatile Exception error;

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			events.incrementAndGet();
			try {
				Object visitor = getChangedClassFiles(event);
				if (visitor == null) {
					return;
				}
				resources.addAll(this.<IResource> invoke(visitor, "getChangedClassFiles"));
				names.addAll(this.<String> invoke(visitor, "getQualifiedNamesList"));
			} catch (Exception e) {
				if (error == null) {
					error = e;
				}
			}
		}

		/**
		 * Calls the (protected) <code>JavaHotCodeReplaceManager#getChangedClassFiles(IResourceChangeEvent)</code>, which computes the class files to
		 * be replaced. Note that the returned visitor type is not accessible, so its (public) methods have to be called reflectively too.
		 */
		private static Object getChangedClassFiles(IResourceChangeEvent event) throws Exception {
			Method method = JavaHotCodeReplaceManager.class.getDeclaredMethod("getChangedClassFiles", IResourceChangeEvent.class);
			method.setAccessible(true);
			return method.invoke(JavaHotCodeReplaceManager.getDefault(), event);
		}

		@SuppressWarnings("unchecked")
		private <T> List<T> invoke(Object visitor, String methodName) throws Exception {
			Method method = visitor.getClass().getDeclaredMethod(methodName);
			method.setAccessible(true);
			List<T> result = (List<T>) method.invoke(visitor);
			return result == null ? Collections.<T> emptyList() : result;
		}
	}

	private IProject project;
	private File projectLocation;

	public HcrChangedClassFilesTests(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (project != null && project.exists()) {
				project.delete(true, true, null);
			}
			if (projectLocation != null) {
				delete(projectLocation);
			}
		} finally {
			project = null;
			projectLocation = null;
			super.tearDown();
		}
	}

	/**
	 * A changed class file of a project stored in the local file system must be detected.
	 */
	public void testChangedClassFilesInLocalProject() throws Exception {
		doTestChangedClassFileDetected(false);
	}

	/**
	 * A changed class file of a project which is <b>not</b> stored in the local file system must be detected as well: such resources have no local
	 * location, so the byte code has to be read via the resources API.
	 */
	public void testChangedClassFilesInNonLocalProject() throws Exception {
		doTestChangedClassFileDetected(true);
	}

	private void doTestChangedClassFileDetected(boolean nonLocal) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile classFile = createProjectWithClassFile(nonLocal);

		if (nonLocal) {
			assertNull("The class file is expected to have no location in the local file system", classFile.getLocation());
		} else {
			assertNotNull("The class file is expected to have a location in the local file system", classFile.getLocation());
		}

		// consume the deltas created while setting the test up
		build();

		ChangedClassFilesCollector collector = new ChangedClassFilesCollector();
		workspace.addResourceChangeListener(collector, IResourceChangeEvent.POST_BUILD);
		try {
			classFile.setContents(new ByteArrayInputStream(getClassFileBytes(ClassTwo.class)), IResource.FORCE, null);
			build();
		} finally {
			workspace.removeResourceChangeListener(collector);
		}

		assertNull("Failed to compute the changed class files: " + collector.error, collector.error);
		assertTrue("No POST_BUILD event was received at all", collector.events.get() > 0);
		assertTrue("The changed class file was not detected, detected files: " + collector.resources,
				collector.resources.contains(classFile));
		assertTrue("Unexpected fully qualified type names detected: " + collector.names,
				collector.names.contains(ClassTwo.class.getName()));
	}

	/**
	 * Creates a project containing a single class file, holding the byte code of {@link ClassOne}.
	 *
	 * @param nonLocal
	 *            if <code>true</code> the project is created in a file system which is not the local one
	 * @return the created class file
	 */
	private IFile createProjectWithClassFile(boolean nonLocal) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = nonLocal ? "HcrNonLocalClassFiles" : "HcrLocalClassFiles";
		project = workspace.getRoot().getProject(name);
		if (project.exists()) {
			project.delete(true, true, null);
		}
		IProjectDescription description = workspace.newProjectDescription(name);
		if (nonLocal) {
			projectLocation = Files.createTempDirectory(name).toFile();
			URI localUri = URIUtil.toURI(new Path(projectLocation.getAbsolutePath()));
			description.setLocationURI(WrapperFileSystem.toWrapperURI(localUri));
		}
		project.create(description, null);
		project.open(null);

		IFolder folder = project.getFolder("bin");
		folder.create(true, true, null);
		IFile classFile = folder.getFile("HcrTestClass.class");
		classFile.create(new ByteArrayInputStream(getClassFileBytes(ClassOne.class)), true, null);
		return classFile;
	}

	/**
	 * Returns the byte code of the given (already compiled) type.
	 */
	private static byte[] getClassFileBytes(Class<?> clazz) throws IOException {
		String resource = clazz.getName().replace('.', '/') + ".class";
		try (InputStream stream = clazz.getClassLoader().getResourceAsStream(resource)) {
			assertNotNull("Unable to find the class file of " + clazz.getName(), stream);
			return stream.readAllBytes();
		}
	}

	/**
	 * Runs a build and waits until all (possibly asynchronously running) builds are done, so that all POST_BUILD events are delivered.
	 */
	private static void build() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		waitForBuild();
	}

	private static void delete(File file) {
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				delete(child);
			}
		}
		file.delete();
	}
}
