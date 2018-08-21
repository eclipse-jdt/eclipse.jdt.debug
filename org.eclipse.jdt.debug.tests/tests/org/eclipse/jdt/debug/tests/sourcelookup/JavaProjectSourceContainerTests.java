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

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaProjectSourceContainer;

/**
 * Tests Java project source containers
 */
public class JavaProjectSourceContainerTests extends AbstractDebugTest {

	public JavaProjectSourceContainerTests(String name) {
		super(name);
	}

	/**
	 * Returns a Java project source container
	 *
	 * @return
	 * @throws Exception
	 */
	protected JavaProjectSourceContainer getContainer(IJavaProject project, boolean duplicates) throws Exception {
		ISourceLookupDirector director = new JavaSourceLookupDirector();
		director.initializeParticipants();
		director.setFindDuplicates(duplicates);
		JavaProjectSourceContainer container = new JavaProjectSourceContainer(project);
		director.setSourceContainers(new ISourceContainer[]{container});
		return container;
	}

	/**
	 * Tests creation and restoring from a memento.
	 *
	 * @throws Exception
	 */
	public void testSourceContainerMemento() throws Exception {
		ISourceContainer container = getContainer(get14Project(), false);
		String memento = container.getType().getMemento(container);
		ISourceContainer restore = container.getType().createSourceContainer(memento);
		assertEquals("Directory source container memento failed", container, restore);
	}

	public void testDefaultPackageLookup() throws Exception {
		ISourceContainer container = getContainer(get14Project(), false);
		Object[] objects = container.findSourceElements("Breakpoints.java");
		assertEquals("Expected 1 result", 1, objects.length);
		IFile file = (IFile) objects[0];
		assertEquals("Wrong file", "Breakpoints.java", file.getName());
	}

	public void testQualifiedLookup() throws Exception {
		ISourceContainer container = getContainer(get14Project(), false);
		Object[] objects = container.findSourceElements("org/eclipse/debug/tests/targets/CallLoop.java");
		assertEquals("Expected 1 result", 1, objects.length);
		IFile file = (IFile) objects[0];
		assertEquals("Wrong file", "CallLoop.java", file.getName());
	}

	public void testNonJavaLookup() throws Exception {
		ISourceContainer container = getContainer(get14Project(), false);
		Object[] objects = container.findSourceElements("debug/non-java.txt");
		assertEquals("Expected 1 result", 1, objects.length);
		IFile file = (IFile) objects[0];
		assertEquals("Wrong file", "non-java.txt", file.getName());
	}
}
