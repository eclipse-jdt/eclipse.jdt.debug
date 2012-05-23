/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.osgi.util.NLS;

/**
 * Creates build path errors related to execution environment bindings.
 * 
 * @since 3.5
 */
public class EECompilationParticipant extends CompilationParticipant {
	
	/**
	 * A set of projects that have been cleaned. When the build finishes for
	 * a project that has been cleaned, we check for EE problems.
	 */
	private Set<IJavaProject> fCleaned = new HashSet<IJavaProject>();

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public boolean isActive(IJavaProject project) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#cleanStarting(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void cleanStarting(IJavaProject project) {
		super.cleanStarting(project);
		fCleaned.add(project);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#buildFinished(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void buildFinished(IJavaProject project) {
		super.buildFinished(project);
		if (fCleaned.remove(project)) {
			String eeId = null;
			IPath container = null;
			try {
				IClasspathEntry[] rawClasspath = project.getRawClasspath();
				for (int j = 0; j < rawClasspath.length; j++) {
					IClasspathEntry entry = rawClasspath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path = entry.getPath();
						if (JavaRuntime.JRE_CONTAINER.equals(path.segment(0))) {
							container = path;
							eeId = JREContainerInitializer.getExecutionEnvironmentId(path);
						}
					}
				}
			} catch (CoreException e) {
				LaunchingPlugin.log(e);
			}
			if (container != null && eeId != null) {
				IVMInstall vm = JREContainerInitializer.resolveVM(container);
				validateEnvironment(eeId, project, vm);
				
			}
		}
	}
	
	/**
	 * Validates the environment, creating a problem marker for the project as required.
	 * 
	 * @param id execution environment ID
	 * @param project associated project
	 * @param vm VM binding resolved for the project
	 */
	private void validateEnvironment(String id, final IJavaProject project, IVMInstall vm) {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		final IExecutionEnvironment environment = manager.getEnvironment(id);
		if (environment != null) {
			if (vm == null) {
				String message = NLS.bind(
						LaunchingMessages.LaunchingPlugin_38,
						new String[]{environment.getId()});
				createJREContainerProblem(project, message, IMarker.SEVERITY_ERROR);
			} else if (!environment.isStrictlyCompatible(vm)) {
				// warn that VM does not match EE
				// first determine if there is a strictly compatible JRE available
				IVMInstall[] compatibleVMs = environment.getCompatibleVMs();
				int exact = 0;
				for (int i = 0; i < compatibleVMs.length; i++) {
					if (environment.isStrictlyCompatible(compatibleVMs[i])) {
						exact++;
					}
				}
				String message = null;
				if (exact == 0) {
					message = NLS.bind(
						LaunchingMessages.LaunchingPlugin_35,
						new String[]{environment.getId()});
				} else {
					message = NLS.bind(
							LaunchingMessages.LaunchingPlugin_36,
							new String[]{environment.getId()});
				}
				int sev = getSeverityLevel(JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE, project.getProject());
				if (sev != -1) {
					createJREContainerProblem(project, message, sev);
				}
			}
		}
	}
	
	/**
	 * Returns the severity for the specific key from the given {@link IProject},
	 * or -1 if the problem should be ignored.
	 * If the project does not have project specific settings, the workspace preference
	 * is returned. If <code>null</code> is passed in as the project the workspace
	 * preferences are consulted.
	 * 
	 * @param prefkey the given preference key
	 * @param project the given project or <code>null</code>
	 * @return the severity level for the given preference key or -1
	 */
	private int getSeverityLevel(String prefkey, IProject project) {
		IPreferencesService service = Platform.getPreferencesService();
		List<IScopeContext> scopes = new ArrayList<IScopeContext>();
		scopes.add(InstanceScope.INSTANCE);
		if(project != null) {
			scopes.add(new ProjectScope(project));
		}
		String value = service.getString(LaunchingPlugin.ID_PLUGIN, prefkey, null, scopes.toArray(new IScopeContext[scopes.size()]));
		if(value == null) {
			value = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).get(prefkey, null);
		}
		if (JavaCore.ERROR.equals(value)) {
			return IMarker.SEVERITY_ERROR;
		}
		if (JavaCore.WARNING.equals(value)) {
			return IMarker.SEVERITY_WARNING;
		}
		return -1;
	}	
	
	/**
	 * creates a problem marker for a JRE container problem
	 * @param javaProject the {@link IJavaProject}
	 * @param message the message to set on the new problem
	 * @param severity the severity level for the new problem
	 */
	private void createJREContainerProblem(IJavaProject javaProject, String message, int severity) {
		try {
			IMarker marker = javaProject.getProject().createMarker(JavaRuntime.JRE_CONTAINER_MARKER);
			marker.setAttributes(
				new String[] {
						IMarker.MESSAGE,
						IMarker.SEVERITY,
						IMarker.LOCATION},
					new Object[] {
						message,
						new Integer(severity),
						LaunchingMessages.LaunchingPlugin_37
					});
		} catch (CoreException e) {
			return;
		}
	}
}
