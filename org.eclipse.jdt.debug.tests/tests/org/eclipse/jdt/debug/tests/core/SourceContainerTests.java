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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests source containers
 */
public class SourceContainerTests extends AbstractDebugTest {
	
	public SourceContainerTests(String name) {
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
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testProjectSourceContainerMemento() throws Exception {
		IProject project = getJavaProject().getProject();
		ProjectSourceContainer container = new ProjectSourceContainer(project, true);
		assertTrue(container.isSearchReferencedProjects());
		String memento = container.getType().getMemento(container);
		ProjectSourceContainer restore = (ProjectSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Project source container memento failed", container, restore);
		assertTrue(restore.isSearchReferencedProjects());
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
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testDefaultSourceContainerMemento() throws Exception {
		ILaunchConfiguration configuration = getLaunchConfiguration("Breakpoints");
		DefaultSourceContainer container = new DefaultSourceContainer(configuration);
		String memento = container.getType().getMemento(container);
		DefaultSourceContainer restore = (DefaultSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Default source container memento failed", container, restore);
		assertEquals(configuration, restore.getLaunchConfiguration());
	}			
}
