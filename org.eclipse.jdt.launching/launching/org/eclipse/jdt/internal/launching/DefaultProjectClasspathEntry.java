/*******************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeContainerComparator;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default user classpath entries for a Java project
 */
@SuppressWarnings("deprecation")
public class DefaultProjectClasspathEntry extends AbstractRuntimeClasspathEntry {

	public static final String TYPE_ID = "org.eclipse.jdt.launching.classpathentry.defaultClasspath"; //$NON-NLS-1$

	/**
	 * Whether only exported entries should be on the runtime classpath.
	 * By default all entries are on the runtime classpath.
	 */
	private boolean fExportedEntriesOnly = false;

	/**
	 * Default constructor need to instantiate extensions
	 */
	public DefaultProjectClasspathEntry() {
	}

	/**
	 * Constructs a new classpath entry for the given project.
	 *
	 * @param project Java project
	 */
	public DefaultProjectClasspathEntry(IJavaProject project) {
		setJavaProject(project);
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
			fExportedEntriesOnly = Boolean.parseBoolean(name);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(ILaunchConfiguration configuration) throws CoreException {
		boolean excludeTestCode = configuration != null
				&& configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, false);
		return getRuntimeClasspathEntries(excludeTestCode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(boolean)
	 */
	@Override
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(boolean excludeTestCode) throws CoreException {
		IClasspathEntry entry = JavaCore.newProjectEntry(getJavaProject().getProject().getFullPath());
		List<Object> classpathEntries = new ArrayList<>(5);
		List<IClasspathEntry> expanding = new ArrayList<>(5);
		expandProject(entry, classpathEntries, expanding, excludeTestCode, isExportedEntriesOnly(), getJavaProject(), false);
		IRuntimeClasspathEntry[] runtimeEntries = new IRuntimeClasspathEntry[classpathEntries.size()];
		for (int i = 0; i < runtimeEntries.length; i++) {
			Object e = classpathEntries.get(i);
			if (e instanceof IClasspathEntry cpe) {
				runtimeEntries[i] = new RuntimeClasspathEntry(cpe);
			} else {
				runtimeEntries[i] = (IRuntimeClasspathEntry)e;
			}
		}
		// remove bootpath entries - this is a default user classpath
		List<IRuntimeClasspathEntry> ordered = new ArrayList<>(runtimeEntries.length);
		for (int i = 0; i < runtimeEntries.length; i++) {
			if (runtimeEntries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				ordered.add(runtimeEntries[i]);
			}
		}
		return ordered.toArray(new IRuntimeClasspathEntry[ordered.size()]);
	}

	/**
	 * Returns the transitive closure of classpath entries for the given project entry.
	 *
	 * @param projectEntry
	 *            project classpath entry
	 * @param expandedPath
	 *            a list of entries already expanded, should be empty to begin, and contains the result
	 * @param expanding
	 *            a list of projects that have been or are currently being expanded (to detect cycles)
	 * @param excludeTestCode
	 *            if true, test dependencies will be excluded
	 * @param exportedEntriesOnly
	 *            if true, only add exported transitive dependencies
	 * @param rootProject
	 *            the root project for which the classpath is computed
	 * @param isModularJVM
	 *            if jvm is java 9 or later
	 * @exception CoreException
	 *                if unable to expand the classpath
	 */
	public static void expandProject(IClasspathEntry projectEntry, List<Object> expandedPath, List<IClasspathEntry> expanding, boolean excludeTestCode, boolean exportedEntriesOnly, IJavaProject rootProject, boolean isModularJVM) throws CoreException {
		final Set<Object> visitedEntries = new HashSet<>();
		expandProjectInternal(projectEntry, expandedPath, visitedEntries, expanding, excludeTestCode, exportedEntriesOnly, rootProject, isModularJVM);
	}

	public static void expandProjectInternal(IClasspathEntry projectEntry, List<Object> expandedPath, Set<Object> visitedEntries, List<IClasspathEntry> expanding, boolean excludeTestCode, boolean exportedEntriesOnly, IJavaProject rootProject, boolean isModularJVM) throws CoreException {
		expanding.add(projectEntry);
		// 1. Get the raw classpath
		// 2. Replace source folder entries with a project entry
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

		IClasspathEntry[] buildPath = project.getRawClasspath();
		List<IClasspathEntry> unexpandedPath = new ArrayList<>(buildPath.length);
		boolean projectAdded = false;
		for (int i = 0; i < buildPath.length; i++) {
			IClasspathEntry classpathEntry = buildPath[i];
			if (excludeTestCode && classpathEntry.isTest()) {
				continue;
			}
			if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (!projectAdded) {
					projectAdded = true;
					unexpandedPath.add(projectEntry);
				}
			} else {
				// add exported entries, as configured
				if (classpathEntry.isExported()) {
					unexpandedPath.add(classpathEntry);
				} else if (!exportedEntriesOnly || project.equals(rootProject)) {
					// add non exported entries from root project or if we are including all entries
					unexpandedPath.add(classpathEntry);
				}
			}
		}
		// 3. expand each project entry (except for the root project)
		// 4. replace each container entry with a runtime entry associated with the project
		Iterator<IClasspathEntry> iter = unexpandedPath.iterator();
		while (iter.hasNext()) {
			IClasspathEntry entry = iter.next();
			if (entry == projectEntry) {
				expandedPath.add(entry);
			} else {
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_PROJECT:
						if (!expanding.contains(entry)) {
							expandProjectInternal(entry, expandedPath, visitedEntries, expanding, excludeTestCode, exportedEntriesOnly, rootProject, isModularJVM);
						}
						break;
					case IClasspathEntry.CPE_CONTAINER:
						IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
						int property = -1;
						if (container != null) {
							switch (container.getKind()) {
								case IClasspathContainer.K_APPLICATION:
									if (isModularJVM) {
										if (Arrays.stream(entry.getExtraAttributes()).anyMatch(attribute -> IClasspathAttribute.MODULE.equals(attribute.getName())
												&& Boolean.TRUE.toString().equals(attribute.getValue()))) {
											property = IRuntimeClasspathEntry.MODULE_PATH;
										} else {
											property = IRuntimeClasspathEntry.CLASS_PATH;
										}
									} else {
										property = IRuntimeClasspathEntry.USER_CLASSES;
									}
									break;
								case IClasspathContainer.K_DEFAULT_SYSTEM:
									property = IRuntimeClasspathEntry.STANDARD_CLASSES;
									break;
								case IClasspathContainer.K_SYSTEM:
									property = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
									break;
							}
							IRuntimeClasspathEntry r = JavaRuntime.newRuntimeContainerClasspathEntry(entry.getPath(), property, project);
							// check for duplicate/redundant entries
							boolean duplicate = false;
							ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(r.getPath().segment(0));
							for (int i = 0; i < expandedPath.size(); i++) {
								Object o = expandedPath.get(i);
								if (o instanceof IRuntimeClasspathEntry re) {
									if (re.getType() == IRuntimeClasspathEntry.CONTAINER) {
										if (container instanceof IRuntimeContainerComparator) {
											duplicate = ((IRuntimeContainerComparator)container).isDuplicate(re.getPath());
										} else {
											ClasspathContainerInitializer initializer2 = JavaCore.getClasspathContainerInitializer(re.getPath().segment(0));
											Object id1 = null;
											Object id2 = null;
											if (initializer == null) {
												id1 = r.getPath().segment(0);
											} else {
												id1 = initializer.getComparisonID(r.getPath(), project);
											}
											if (initializer2 == null) {
												id2 = re.getPath().segment(0);
											} else {
												IJavaProject context = re.getJavaProject();
												if (context == null) {
													context = project;
												}
												id2 = initializer2.getComparisonID(re.getPath(), context);
											}
											if (id1 == null) {
												duplicate = id2 == null;
											} else {
												duplicate = id1.equals(id2);
											}
										}
										if (duplicate) {
											break;
										}
									}
								}
							}
							if (!duplicate) {
								expandedPath.add(r);
							}
						}
						break;
					case IClasspathEntry.CPE_VARIABLE:
						IRuntimeClasspathEntry r = JavaRuntime.newVariableRuntimeClasspathEntry(entry.getPath());
						if (isModularJVM) {
							adjustClasspathProperty(r, entry);
						}
						r.setSourceAttachmentPath(entry.getSourceAttachmentPath());
						r.setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
						if (!expandedPath.contains(r)) {
							expandedPath.add(r);
						}
						break;
					default:
						if (!expandedPath.contains(entry)) {
							// resolve project relative paths - @see bug 57732 & bug 248466
							if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
								if (!visitedEntries.contains(entry)) {
									visitedEntries.add(entry);
									IPackageFragmentRoot[] roots = project.findPackageFragmentRoots(entry);
									for (int i = 0; i < roots.length; i++) {
										IPackageFragmentRoot root = roots[i];
										r = JavaRuntime.newArchiveRuntimeClasspathEntry(root.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), entry.getExtraAttributes(), entry.isExported());
										if (isModularJVM) {
											adjustClasspathProperty(r, entry);
										}
										r.setSourceAttachmentPath(entry.getSourceAttachmentPath());
										r.setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
										if (!expandedPath.contains(r)) {
											expandedPath.add(r);
										}
									}
								}
							} else {
								expandedPath.add(entry);
							}
						}
						break;
				}
			}
		}
		return;
	}

	public static void adjustClasspathProperty(IRuntimeClasspathEntry r, IClasspathEntry entry) {
		if (r.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
			if (Arrays.stream(entry.getExtraAttributes()).anyMatch(attribute -> IClasspathAttribute.MODULE.equals(attribute.getName())
					&& Boolean.TRUE.toString().equals(attribute.getValue()))) {
				r.setClasspathProperty(IRuntimeClasspathEntry.MODULE_PATH);
			} else {
				r.setClasspathProperty(IRuntimeClasspathEntry.CLASS_PATH);
			}
		}
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
			return NLS.bind(LaunchingMessages.DefaultProjectClasspathEntry_2, getJavaProject().getElementName());
		}
		return NLS.bind(LaunchingMessages.DefaultProjectClasspathEntry_4, getJavaProject().getElementName());
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DefaultProjectClasspathEntry entry) {
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