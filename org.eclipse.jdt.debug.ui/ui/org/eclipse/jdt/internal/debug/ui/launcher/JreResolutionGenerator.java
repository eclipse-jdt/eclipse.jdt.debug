/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CorrectionEngine;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Generates quick fixes for unbound JREs.
 */
public class JreResolutionGenerator implements IMarkerResolutionGenerator {
	
	private final static IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		int id = marker.getAttribute(IJavaModelMarker.ID, -1);
		switch (id) {
			// unbound classpath container
			case IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND :
				String[] arguments = CorrectionEngine.getProblemArguments(marker);
				IPath path = new Path(arguments[0]);
				if (path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
					// unbound JRE_CONTAINER
					if (JREResolution.getAllVMs().length > 0) {
						IJavaProject project = getJavaProject(marker);
						return new IMarkerResolution[]{new SelectSystemLibraryQuickFix(path, project)};
					}
					// define a new JRE
					return new IMarkerResolution[]{new DefineSystemLibraryQuickFix()};
				}
				break;

			// unbound classpath variabe
			case IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND :
				arguments = CorrectionEngine.getProblemArguments(marker);
				path = new Path(arguments[0]);
				if (path.segment(0).equals(JavaRuntime.JRELIB_VARIABLE)) {
					// unbound JRE_LIB
					if (JREResolution.getAllVMs().length > 0) {
						return new IMarkerResolution[]{new SelectDefaultSystemLibraryQuickFix()};
					}
					// define a new default JRE
					return new IMarkerResolution[]{new DefineSystemLibraryQuickFix()};
				}
				break;
		}
		return NO_RESOLUTION;
	}

	protected IJavaProject getJavaProject(IMarker marker) {
		return JavaCore.create(marker.getResource().getProject());
	}

}
