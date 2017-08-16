/*******************************************************************************
 *  Copyright (c) 2017 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default Project dependencies entries for a Java project
 */
public class DefaultProjectDependencies extends AbstractRuntimeClasspathEntry {

	public static final String TYPE_ID = "org.eclipse.jdt.launching.classpathentry.defaultDependencies"; //$NON-NLS-1$

	/**
	 * Whether only exported entries should be on the runtime classpath.
	 * By default all entries are on the runtime classpath.
	 */
	private boolean fExportedEntriesOnly = false;

	/**
	 * Default constructor need to instantiate extensions
	 */
	public DefaultProjectDependencies() {
		setClasspathProperty(IRuntimeClasspathEntry.MODULE_PATH);
	}

	/**
	 * Constructs a new classpath entry for the given project.
	 *
	 * @param project Java project
	 */
	public DefaultProjectDependencies(IJavaProject project) {
		setJavaProject(project);
		setClasspathProperty(IRuntimeClasspathEntry.MODULE_PATH);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry#buildMemento(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	protected void buildMemento(Document document, Element memento) throws CoreException {
		memento.setAttribute("project", getJavaProject().getElementName()); //$NON-NLS-1$
		memento.setAttribute("exportedEntriesOnly", Boolean.toString(fExportedEntriesOnly)); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#initializeFrom(org.w3c.dom.Element)
	 */
	@Override
	public void initializeFrom(Element memento) throws CoreException {
		String name = memento.getAttribute("project"); //$NON-NLS-1$
		if (name == null) {
			abort(LaunchingMessages.DefaultProjectClasspathEntry_3, null);
		}
		IJavaProject project = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(name));
		setJavaProject(project);
		name = memento.getAttribute("exportedEntriesOnly"); //$NON-NLS-1$
		if (name != null) {
			fExportedEntriesOnly = Boolean.valueOf(name).booleanValue();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
	@Override
	public int getType() {
		return OTHER;
	}

	protected IProject getProject() {
		return getJavaProject().getProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getLocation()
	 */
	@Override
	public String getLocation() {
		return getProject().getLocation().toOSString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getPath()
	 */
	@Override
	public IPath getPath() {
		return getProject().getFullPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getResource()
	 */
	@Override
	public IResource getResource() {
		return getProject();
	}

	public IRuntimeClasspathEntry[] getDefualtDependencies() throws CoreException {
		IClasspathEntry entry = JavaCore.newProjectEntry(getJavaProject().getProject().getFullPath());
		List<Object> classpathEntries = new ArrayList<>(5);
		expandProject(entry, classpathEntries);
		IRuntimeClasspathEntry[] runtimeEntries = new IRuntimeClasspathEntry[classpathEntries.size()];
		for (int i = 0; i < runtimeEntries.length; i++) {
			Object e = classpathEntries.get(i);
			if (e instanceof IClasspathEntry) {
				IClasspathEntry cpe = (IClasspathEntry) e;
				runtimeEntries[i] = new RuntimeClasspathEntry(cpe);
			} else {
				runtimeEntries[i] = (IRuntimeClasspathEntry) e;
			}
		}
		return runtimeEntries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(ILaunchConfiguration configuration) throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}

	/**
	 * Returns the transitive closure of classpath entries for the
	 * given project entry.
	 *
	 * @param projectEntry project classpath entry
	 * @param expandedPath a list of entries already expanded, should be empty
	 * to begin, and contains the result
	 * @param expanding a list of projects that have been or are currently being
	 * expanded (to detect cycles)
	 * @exception CoreException if unable to expand the classpath
	 */
	private void expandProject(IClasspathEntry projectEntry, List<Object> expandedPath) throws CoreException {
		IPath projectPath = projectEntry.getPath();
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(projectPath.lastSegment());
		if (res == null) {
			// add project entry and return
			expandedPath.add(projectEntry);
			return;
		}
		IJavaProject project = (IJavaProject)JavaCore.create(res);
		if (project == null || !project.getProject().isOpen() || !project.exists()) {
			// add project entry and return
			expandedPath.add(projectEntry);
			return;
		}

		expandedPath.add(projectEntry);
		IClasspathEntry[] buildPath1 = project.getResolvedClasspath(true);
		for (IClasspathEntry iClasspathEntry : buildPath1) {
			if (iClasspathEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				IRuntimeClasspathEntry r = JavaRuntime.newRuntimeContainerClasspathEntry(iClasspathEntry, project);
				expandedPath.add(r);
			}
		}
		return;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#isComposite()
	 */
	@Override
	public boolean isComposite() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getName()
	 */
	@Override
	public String getName() {
		if (isExportedEntriesOnly()) {
			return NLS.bind(LaunchingMessages.DefaultProjectClasspathEntry_2, new String[] {getJavaProject().getElementName()});
		}
		return NLS.bind(LaunchingMessages.DefaultProjectClasspathEntry_4, new String[] {getJavaProject().getElementName()});
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DefaultProjectDependencies) {
			DefaultProjectDependencies entry = (DefaultProjectDependencies) obj;
			return entry.getJavaProject().equals(getJavaProject()) &&
				entry.isExportedEntriesOnly() == isExportedEntriesOnly();
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getJavaProject().hashCode();
	}

	/**
	 * Sets whether the runtime classpath computation should only
	 * include exported entries in referenced projects.
	 *
	 * @param exportedOnly if the runtime classpath computation should only
	 * include exported entries in referenced projects.
	 * @since 3.2
	 */
	public void setExportedEntriesOnly(boolean exportedOnly) {
		fExportedEntriesOnly = exportedOnly;
	}

	/**
	 * Returns whether the classpath computation only includes exported
	 * entries in referenced projects.
	 *
	 * @return if the classpath computation only includes exported
	 * entries in referenced projects.
	 * @since 3.2
	 */
	public boolean isExportedEntriesOnly() {
		return fExportedEntriesOnly | Platform.getPreferencesService().getBoolean(
				LaunchingPlugin.ID_PLUGIN,
				JavaRuntime.PREF_ONLY_INCLUDE_EXPORTED_CLASSPATH_ENTRIES,
				false,
				null);
	}
}