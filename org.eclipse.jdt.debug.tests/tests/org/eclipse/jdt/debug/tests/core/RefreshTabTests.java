/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Tests the refresh tab.
 */
public class RefreshTabTests extends AbstractDebugTest {
	
	public RefreshTabTests(String name) {
		super(name);
	}

	/**
	 * Sets the selected resource in the navigator view.
	 * 
	 * @param resource resource to select
	 */
	protected void setSelection(final IResource resource) {
		Runnable r = new Runnable() {
			public void run() {
				IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
				IViewPart part;
				try {
					part = page.showView("org.eclipse.ui.views.ResourceNavigator");
					part.getSite().getSelectionProvider().setSelection(new StructuredSelection(resource));
				} catch (PartInitException e) {
					assertNotNull("Failed to open navigator view", null);
				}
				
			}
		};
		DebugUIPlugin.getStandardDisplay().syncExec(r);
	}
	
	/**
	 * Tests a refresh scope of the selected resource
	 */
	public void testSelectedResource() throws CoreException {
		String scope = "${resource}";
		IResource resource = getJavaProject().getProject().getFolder("src");
		setSelection(resource);
		IResource[] result = RefreshTab.getRefreshResources(scope, null);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(resource, result[0]);		
	}
	
	/**
	 * Tests a refresh scope of the selected resource's container
	 */
	public void testSelectionsFolder() throws CoreException {
		String scope = "${container}";
		IResource resource = getJavaProject().getProject().getFolder("src");
		setSelection(resource);
		IResource[] result = RefreshTab.getRefreshResources(scope, null);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(resource.getParent(), result[0]);		
	}
	
	/**
	 * Tests a refresh scope of the selected resource's project
	 */
	public void testSelectionsProject() throws CoreException {
		String scope = "${project}";
		IResource resource = getJavaProject().getProject().getFolder("src");
		setSelection(resource);
		IResource[] result = RefreshTab.getRefreshResources(scope, null);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(resource.getProject(), result[0]);		
	}	
	
	/**
	 * Tests a refresh scope of the selected resource's project
	 */
	public void testWorkspaceScope() throws CoreException {
		String scope = "${workspace}";
		IResource[] result = RefreshTab.getRefreshResources(scope, null);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(ResourcesPlugin.getWorkspace().getRoot(), result[0]);		
	}	
	
	/**
	 * Tests a refresh scope for a specific resource (old format)
	 *
	 */
	public void testSpecificResource() throws CoreException {
		String scope = "${resource:/DebugTests/.classpath}";
		IResource resource = getJavaProject().getProject().getFile(".classpath");
		IResource[] result = RefreshTab.getRefreshResources(scope, null);
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals(resource, result[0]);				
	}
}
