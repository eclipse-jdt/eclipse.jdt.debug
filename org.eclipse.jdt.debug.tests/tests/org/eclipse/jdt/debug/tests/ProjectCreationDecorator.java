/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper Steen Moller - Enhancement 254677 - filter getters/setters
 *******************************************************************************/
package org.eclipse.jdt.debug.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Test to close the workbench, since debug tests do not run in the UI thread.
 */
public class ProjectCreationDecorator extends AbstractDebugTest {

    /**
     * Constructor
     */
    public ProjectCreationDecorator() {
        super("Project creation decorator tests");
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.tests.AbstractDebugTest#getProjectContext()
	 */
	@Override
	protected IJavaProject getProjectContext() {
		return get14Project();
	}

    /**
     *
     */
    public void testPerspectiveSwtich() {
        DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry().findPerspectiveWithId(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
                IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
				activePage.setPerspective(descriptor);
				// hide variables and breakpoints view to reduce simultaneous conflicting requests on debug targets
                IViewReference ref = activePage.findViewReference(IDebugUIConstants.ID_VARIABLE_VIEW);
                activePage.hideView(ref);
                ref = activePage.findViewReference(IDebugUIConstants.ID_BREAKPOINT_VIEW);
                activePage.hideView(ref);
            }
        });
    }

    /**
     * test if builds completed successfully and output directory contains class
     * files.
     * @throws Exception
     */
    public void testOutputFolderNotEmpty() throws Exception {
        waitForBuild();
        IPath outputLocation = get14Project().getOutputLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IResource resource = root.findMember(outputLocation);
        assertNotNull("Project output location is null", resource);
        assertTrue("Project output location does not exist", resource.exists());
        assertTrue("Project output is not a folder", (resource.getType() == IResource.FOLDER));
        IFolder folder = (IFolder) resource;
        IResource[] children = folder.members();
        assertTrue("output folder is empty", children.length > 0);
    }

    /**
     * @throws Exception
     */
    public void testForUnexpectedErrorsInProject() throws Exception {
        waitForBuild();
        IProject project = get14Project().getProject();
        IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        int errors = 0;
        for (int i = 0; i < markers.length; i++) {
            IMarker marker = markers[i];
            Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
            if (severity != null && severity.intValue() >= IMarker.SEVERITY_ERROR) {
            	System.err.println("Found problem in 1.4 test project: "+marker.getResource().getFullPath().toString()+" "+ marker.getAttribute(IMarker.MESSAGE));
                errors++;
            }
    	}
        assertTrue("Unexpected compile errors in project. Expected 0 found " + markers.length, errors == 0);
    }

    /**
     * @throws Exception
     */
    public void testClassFilesGenerated() throws Exception {
        waitForBuild();
        IPath outputLocation = get14Project().getOutputLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IFolder folder = (IFolder) root.findMember(outputLocation);
        IResource[] children = folder.members();
        int classFiles = 0;
        for (int i = 0; i < children.length; i++) {
            IResource child = children[i];
            if (child.getType() == IResource.FILE) {
                IFile file = (IFile) child;
                String fileExtension = file.getFileExtension();
                if (fileExtension.equals("class")) {
                    classFiles++;
                }
            }
        }
        assertTrue("No class files exist", (classFiles > 0));
    }
}
