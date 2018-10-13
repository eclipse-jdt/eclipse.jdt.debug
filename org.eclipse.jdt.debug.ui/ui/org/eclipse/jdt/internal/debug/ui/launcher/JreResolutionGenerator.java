/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CorrectionEngine;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.jres.ExecutionEnvironmentsPreferencePage;
import org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage;
import org.eclipse.jdt.internal.launching.JREContainerInitializer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.java.hover.ConfigureProblemSeverityAction;
import org.eclipse.jdt.internal.ui.text.java.hover.ConfigureProblemSeverityAction.PreferencePage;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.IMarkerResolutionRelevance;

/**
 * Generates quick fixes for unbound JREs.
 */
public class JreResolutionGenerator implements IMarkerResolutionGenerator2 {

	private final static IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

	/**
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			if(JavaRuntime.JRE_CONTAINER_MARKER.equals(marker.getType())) {
				OpenPreferencePageResolution openPreferencePageResolution = new OpenPreferencePageResolution(ExecutionEnvironmentsPreferencePage.ID, new String[] {
						ExecutionEnvironmentsPreferencePage.ID,
						JREsPreferencePage.ID }, LauncherMessages.JreResolutionGenerator_open_ee_prefs, LauncherMessages.JreResolutionGenerator_opens_ee_prefs);
				IJavaProject project = getJavaProject(marker);
				IPath container = null;
				for (IClasspathEntry entry : project.getRawClasspath()) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path = entry.getPath();
						if (JavaRuntime.JRE_CONTAINER.equals(path.segment(0))) {
							container = path;
						}
					}
				}
				boolean noVM = container == null || JREContainerInitializer.resolveVM(container) == null;
				if (noVM) {
					return new IMarkerResolution[] { openPreferencePageResolution };
				}
				ConfigureSeverityResolution configureSeverityResolution = new ConfigureSeverityResolution(project, JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE);
				return new IMarkerResolution[] { openPreferencePageResolution, configureSeverityResolution };
			} else if (JavaRuntime.JRE_COMPILER_COMPLIANCE_MARKER.equals(marker.getType())) {
				IJavaProject project = getJavaProject(marker);
				OpenPropertyPageResolution openPropertyPageResolution = new OpenPropertyPageResolution(project, JavaUI.ID_COMPILER_COMPLIANCE_PROPERTY_PAGE, new String[] {
						JavaUI.ID_COMPILER_COMPLIANCE_PROPERTY_PAGE,
						JavaUI.ID_JAVA_BUILD_PREFERENCE_PROPERTY_PAGE }, LauncherMessages.JreResolutionGenerator_open_cc_props, LauncherMessages.JreResolutionGenerator_opens_cc_props);
				ConfigureSeverityResolution configureSeverityResolution = new ConfigureSeverityResolution(project, JavaRuntime.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE);
				return new IMarkerResolution[] { openPropertyPageResolution, configureSeverityResolution };
			}
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

				// unbound classpath variable
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
				// deprecated JRE library variables
				case IJavaModelStatusConstants.DEPRECATED_VARIABLE :
					arguments = CorrectionEngine.getProblemArguments(marker);
					path = new Path(arguments[0]);
					if (path.segment(0).equals(JavaRuntime.JRELIB_VARIABLE) ||
							path.segment(0).equals(JavaRuntime.JRE_CONTAINER)) {
						IJavaProject project = getJavaProject(marker);
						return new IMarkerResolution[] {new SelectSystemLibraryQuickFix(path, project)};
					}
					break;
			}
		}
		catch(CoreException ce) {}
		return NO_RESOLUTION;
	}

	/**
	 * Returns the java project from the specified marker, or <code>null</code> if the marker
	 * does not have an associated java project
	 * @param marker
	 * @return the associated java project or <code>null</code>
	 */
	protected IJavaProject getJavaProject(IMarker marker) {
		return JavaCore.create(marker.getResource().getProject());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public boolean hasResolutions(IMarker marker) {
		try {
			String type = marker.getType();
			return IJavaModelMarker.BUILDPATH_PROBLEM_MARKER.equals(type) ||
				   JavaRuntime.JRE_CONTAINER_MARKER.equals(type) ||
				   JavaRuntime.JRE_COMPILER_COMPLIANCE_MARKER.equals(type);
		} catch (CoreException ce) {}
		return false;
	}

	private static class ConfigureSeverityResolution implements IMarkerResolution2, IMarkerResolutionRelevance {
		private final IJavaProject fProject;
		private String fOptionId;

		public ConfigureSeverityResolution(IJavaProject project, String optionId) {
			fProject = project;
			fOptionId = optionId;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public Image getImage() {
			return JavaPluginImages.get(JavaPluginImages.IMG_CONFIGURE_PROBLEM_SEVERITIES);
		}

		@Override
		public String getLabel() {
			return CorrectionMessages.ConfigureProblemSeveritySubProcessor_name;
		}

		@Override
		public int getRelevanceForResolution() {
			return -1;
		}

		@Override
		public void run(IMarker marker) {
			ConfigureProblemSeverityAction problemSeverityAction = new ConfigureProblemSeverityAction(fProject, fOptionId, JavaRuntime.ID_PLUGIN, PreferencePage.BUILDING, null);
			problemSeverityAction.run();
		}
	}
}
