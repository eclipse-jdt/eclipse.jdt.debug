package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Generates quick fix for unbound JRE container.
 */
public class JreResolutionGenerator implements IMarkerResolutionGenerator {

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		String variable = marker.getAttribute(IJavaModelMarker.UNBOUND_CONTAINER, null);
		if (variable != null) {
			IPath path = new Path(variable);
			if (path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
				// unbound JRE_CONTAINER
				if (JREResolution.getAllVMs().length > 0) {
					IJavaProject project = getJavaProject(marker);
					return new IMarkerResolution[]{new JREContainerResolution(path, project)};
				}
			}
		}
		return new IMarkerResolution[0];
	}
	
	protected IJavaProject getJavaProject(IMarker marker) {
		return JavaCore.create(marker.getResource().getProject());
	}

}
