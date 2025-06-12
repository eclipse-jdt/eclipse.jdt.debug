/*******************************************************************************
 * Copyright (c) 2008, 2025 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jdt.launching.IVMInstall2;
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
	private final Set<IJavaProject> fCleaned = new HashSet<>();

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
				if (vm instanceof IVMInstall2) {
					eeId = getCompilerCompliance((IVMInstall2) vm);
					if (eeId != null) {
						validateCompliance(eeId, project, vm);
					}
				}

			} else if (container != null) {
				IVMInstall vm = JREContainerInitializer.resolveVM(container);
				if (vm instanceof IVMInstall2) {
					eeId = getCompilerCompliance((IVMInstall2) vm);
					if (eeId != null) {
						validateCompliance(eeId, project, vm);
					}
				}
			}

		}
	}

	/**
	 * Validates if --release flag is enabled for the project.
	 *
	 * @param eeId
	 *            execution environment ID
	 * @param compId
	 *            project compliance
	 * @param project
	 *            associated project
	 * @return <code>true</code> if --release flag if enabled else <code>false</code>
	 */

	private boolean isReleaseFlagEnabled(String eeId, String compId, final IJavaProject project) {
		boolean releaseFlagEnabled = false;
		if (JavaCore.compareJavaVersions(JavaCore.VERSION_9, eeId) <= 0) {
			if (JavaCore.compareJavaVersions(JavaCore.VERSION_1_6, compId) <= 0) {
				String releaseVal = project.getOption(JavaCore.COMPILER_RELEASE, true);
				if (JavaCore.ENABLED.equals(releaseVal)) {
					releaseFlagEnabled = true;
				}
			}
		}
		return releaseFlagEnabled;
	}

	/**
	 * Validates the compliance, creating a problem marker for the project as required.
	 *
	 * @param eeId
	 *            execution environment ID
	 * @param project
	 *            associated project
	 * @param vm
	 *            VM binding resolved for the project
	 */
	private void validateCompliance(String eeId, final IJavaProject project, IVMInstall vm) {
		String id = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (!eeId.equals(id)) {
			// validate compliance only if release flag is not enabled
			if (!isReleaseFlagEnabled(eeId, id, project)) {
				IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
				IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
				IExecutionEnvironment finalEnvironment = null;
				for (IExecutionEnvironment environment : environments) {
					if (environment.getId().indexOf(id) != -1) {
						finalEnvironment = environment;
						break;
					}
				}
				if (finalEnvironment != null) {
					if (!finalEnvironment.isStrictlyCompatible(vm)) {
						String message = NLS.bind(LaunchingMessages.LaunchingPlugin_39, id, eeId);
						int sev = getSeverityLevel(JavaRuntime.PREF_COMPILER_COMPLIANCE_DOES_NOT_MATCH_JRE, project.getProject());
						if (sev != -1) {
							createProblemMarker(project, message, sev, JavaRuntime.JRE_COMPILER_COMPLIANCE_MARKER, LaunchingMessages.LaunchingPlugin_40);
						}
					}
				}
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
						environment.getId());
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
					if (vm instanceof IVMInstall2) {
						String eeId = getCompilerCompliance((IVMInstall2) vm);
						String compId = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
						if (eeId != null && isReleaseFlagEnabled(eeId, compId, project)) {
							return;
						}
					}
					message = NLS.bind(
						LaunchingMessages.LaunchingPlugin_35,
							environment.getId());
				} else {
					message = NLS.bind(
							LaunchingMessages.LaunchingPlugin_36,
							environment.getId());
				}
				int sev = getSeverityLevel(JavaRuntime.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE, project.getProject());
				if (sev != -1) {
					createJREContainerProblem(project, message, sev);
				}
			}
		}
	}

	public static String getCompilerCompliance(IVMInstall2 vMInstall) {
		String version = vMInstall.getJavaVersion();
		if (version == null) {
			return null;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_24)) {
			return JavaCore.VERSION_24;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_23)) {
			return JavaCore.VERSION_23;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_22)) {
			return JavaCore.VERSION_22;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_21)) {
			return JavaCore.VERSION_21;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_20)) {
			return JavaCore.VERSION_20;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_19)) {
			return JavaCore.VERSION_19;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_18)) {
			return JavaCore.VERSION_18;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_17)) {
			return JavaCore.VERSION_17;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_16)) {
			return JavaCore.VERSION_16;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_15)) {
			return JavaCore.VERSION_15;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_14)) {
			return JavaCore.VERSION_14;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_13)) {
			return JavaCore.VERSION_13;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_12)) {
			return JavaCore.VERSION_12;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_11)) {
			return JavaCore.VERSION_11;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_10)) {
			return JavaCore.VERSION_10;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_9)) {
			return JavaCore.VERSION_9;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_8)) {
			return JavaCore.VERSION_1_8;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_7)) {
			return JavaCore.VERSION_1_7;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_6)) {
			return JavaCore.VERSION_1_6;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_5)) {
			return JavaCore.VERSION_1_5;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_4)) {
			return JavaCore.VERSION_1_4;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_3)) {
			return JavaCore.VERSION_1_3;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_2)) {
			return JavaCore.VERSION_1_3;
		} else if (matchesMajorVersion(version, JavaCore.VERSION_1_1)) {
			return JavaCore.VERSION_1_3;
		}
		return null;
	}

	private static boolean matchesMajorVersion(String currentVersion, String knownVersion) {
		if (currentVersion.startsWith(knownVersion)) {
			int knownLength = knownVersion.length();
			return currentVersion.length() == knownLength || currentVersion.charAt(knownLength) == '.';
		}
		return false;
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
		List<IScopeContext> scopes = new ArrayList<>();
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
		if (JavaCore.INFO.equals(value)) {
			return IMarker.SEVERITY_INFO;
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
			Map<String, Object> attributes = Map.of(IMarker.MESSAGE, message, //
					IMarker.SEVERITY, Integer.valueOf(severity), //
					IMarker.LOCATION, LaunchingMessages.LaunchingPlugin_37);

			javaProject.getProject().createMarker(JavaRuntime.JRE_CONTAINER_MARKER, attributes);
		} catch (CoreException e) {
			return;
		}
	}

	/**
	 * creates a problem marker for a Java problem
	 *
	 * @param javaProject
	 *                        the {@link IJavaProject}
	 * @param message
	 *                        the message to set on the new problem
	 * @param severity
	 *                        the severity level for the new problem
	 */
	private void createProblemMarker(IJavaProject javaProject, String message, int severity, String problemId, String location) {
		try {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put(IMarker.MESSAGE, message);
			attributes.put(IMarker.SEVERITY, Integer.valueOf(severity));
			attributes.put(IMarker.LOCATION, location);

			javaProject.getProject().createMarker(problemId, attributes);
		} catch (CoreException e) {
			return;
		}
	}
}
