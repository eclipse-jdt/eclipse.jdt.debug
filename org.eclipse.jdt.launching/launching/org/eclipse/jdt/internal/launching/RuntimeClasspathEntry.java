/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     BEA - Daniel R Somerfield - Bug 88939
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.util.Arrays;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An entry on the runtime classpath that the user can manipulate
 * and share in a launch configuration.
 *
 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry
 * @since 2.0
 */
public class RuntimeClasspathEntry implements IRuntimeClasspathEntry {

	/**
	 * This entry's type - must be set on creation.
	 */
	private int fType = -1;

	/**
	 * This entry's classpath property.
	 */
	private int fClasspathProperty = -1;

	/**
	 * This entry's associated build path entry.
	 */
	private IClasspathEntry fClasspathEntry = null;

	/**
	 * The entry's resolved entry (lazily initialized)
	 */
	private IClasspathEntry fResolvedEntry = null;

	/**
	 * Associated Java project, or <code>null</code>
	 */
	private IJavaProject fJavaProject = null;

	/**
	 * The path if the entry was invalid and fClasspathEntry is null
	 */
	private IPath fInvalidPath;

	/**
	 * Constructs a new runtime classpath entry based on the
	 * (build) classpath entry.
	 *
	 * @param entry the associated classpath entry
	 */
	public RuntimeClasspathEntry(IClasspathEntry entry) {
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_PROJECT:
				setType(PROJECT);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				setType(ARCHIVE);
				break;
			case IClasspathEntry.CPE_VARIABLE:
				setType(VARIABLE);
				break;
			default:
				throw new IllegalArgumentException(NLS.bind(LaunchingMessages.RuntimeClasspathEntry_Illegal_classpath_entry__0__1, entry.toString()));
		}
		setClasspathEntry(entry);
		initializeClasspathProperty();
	}

	/**
	 * Constructs a new container entry in the context of the given project
	 *
	 * @param entry classpath entry
	 * @param classpathProperty this entry's classpath property
	 */
	public RuntimeClasspathEntry(IClasspathEntry entry, int classpathProperty) {
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
				setType(CONTAINER);
				break;
			case IClasspathEntry.CPE_PROJECT:
				setType(PROJECT);
				break;
			case IClasspathEntry.CPE_LIBRARY:
				setType(ARCHIVE);
				break;
			case IClasspathEntry.CPE_VARIABLE:
				setType(VARIABLE);
				break;
			default:
				throw new IllegalArgumentException(NLS.bind(LaunchingMessages.RuntimeClasspathEntry_Illegal_classpath_entry__0__1, entry.toString()));
		}
		setClasspathEntry(entry);
		setClasspathProperty(classpathProperty);
	}

	/**
	 * Reconstructs a runtime classpath entry from the given
	 * XML document root not.
	 *
	 * @param root a memento root doc element created by this class
	 * @exception CoreException if unable to restore from the given memento
	 */
	public RuntimeClasspathEntry(Element root) throws CoreException {
		try {
			setType(Integer.parseInt(root.getAttribute("type"))); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry_type_2, e);
		}
		try {
			setClasspathProperty(Integer.parseInt(root.getAttribute("path"))); //$NON-NLS-1$
		} catch (NumberFormatException e) {
			abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry_location_3, e);
		}

		// source attachment
		IPath sourcePath = null;
		IPath rootPath = null;
		String path = root.getAttribute("sourceAttachmentPath"); //$NON-NLS-1$
		if (path != null && path.length() > 0) {
			sourcePath = new Path(path);
		}
		path = root.getAttribute("sourceRootPath"); //$NON-NLS-1$
		if (path != null && path.length() > 0) {
			rootPath = new Path(path);
		}

		switch (getType()) {
			case PROJECT :
				String name = root.getAttribute("projectName"); //$NON-NLS-1$
				if (isEmpty(name)) {
					abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry___missing_project_name_4, null);
				} else {
					IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
					setClasspathEntry(JavaCore.newProjectEntry(proj.getFullPath()));
				}
				break;
			case ARCHIVE :
				path = root.getAttribute("externalArchive"); //$NON-NLS-1$
				if (isEmpty(path)) {
					// internal
					path = root.getAttribute("internalArchive"); //$NON-NLS-1$
					if (isEmpty(path)) {
						abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry___missing_archive_path_5, null);
					} else {
						setClasspathEntry(createLibraryEntry(sourcePath, rootPath, path));
					}
				} else {
					// external
					setClasspathEntry(createLibraryEntry(sourcePath, rootPath, path));
				}
				break;
			case VARIABLE :
				String var = root.getAttribute("containerPath"); //$NON-NLS-1$
				if (isEmpty(var)) {
					abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry___missing_variable_name_6, null);
				} else {
					setClasspathEntry(JavaCore.newVariableEntry(new Path(var), sourcePath, rootPath));
				}
				break;
			case CONTAINER :
				var = root.getAttribute("containerPath"); //$NON-NLS-1$
				if (isEmpty(var)) {
					abort(LaunchingMessages.RuntimeClasspathEntry_Unable_to_recover_runtime_class_path_entry___missing_variable_name_6, null);
				} else {
					setClasspathEntry(JavaCore.newContainerEntry(new Path(var)));
				}
				break;
		}

		String name = root.getAttribute("javaProject"); //$NON-NLS-1$
		if (isEmpty(name)) {
			fJavaProject = null;
		} else {
			IProject project2 = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			fJavaProject = JavaCore.create(project2);
		}
	}

	private IClasspathEntry createLibraryEntry(IPath sourcePath, IPath rootPath, String path) {
		Path p = new Path(path);
		if (!p.isAbsolute())
		{
			fInvalidPath = p;
			return null;
			//abort("There was a problem with path \" " + path + "\": paths must be absolute.", null);
		}
		return JavaCore.newLibraryEntry(p, sourcePath, rootPath);
	}

	/**
	 * Throws an internal error exception
	 * @param message the message
	 * @param e the error
	 * @throws CoreException the new {@link CoreException}
	 */
	protected void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);
	}

	/**
	 * @see IRuntimeClasspathEntry#getType()
	 */
	@Override
	public int getType() {
		return fType;
	}

	/**
	 * Sets this entry's type
	 *
	 * @param type this entry's type
	 */
	private void setType(int type) {
		fType = type;
	}

	/**
	 * Sets the classpath entry associated with this runtime classpath entry.
	 * Clears the cache of the resolved entry.
	 *
	 * @param entry the classpath entry associated with this runtime classpath entry
	 */
	private void setClasspathEntry(IClasspathEntry entry) {
		fClasspathEntry = entry;
		fResolvedEntry = null;
	}

	/**
	 * @see IRuntimeClasspathEntry#getClasspathEntry()
	 */
	@Override
	public IClasspathEntry getClasspathEntry() {
		return fClasspathEntry;
	}

	/**
	 * @see IRuntimeClasspathEntry#getMemento()
	 */
	@Override
	public String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element node = doc.createElement("runtimeClasspathEntry"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("type", Integer.toString(getType())); //$NON-NLS-1$
		node.setAttribute("path", Integer.toString(getClasspathProperty())); //$NON-NLS-1$
		switch (getType()) {
			case PROJECT :
				node.setAttribute("projectName", getPath().lastSegment()); //$NON-NLS-1$
				break;
			case ARCHIVE :
				IResource res = getResource();
				if (res == null) {
					node.setAttribute("externalArchive", getPath().toString()); //$NON-NLS-1$
				} else {
					node.setAttribute("internalArchive", res.getFullPath().toString()); //$NON-NLS-1$
				}
				break;
			case VARIABLE :
			case CONTAINER :
				node.setAttribute("containerPath", getPath().toString()); //$NON-NLS-1$
				break;
		}
		if (getSourceAttachmentPath() != null) {
			node.setAttribute("sourceAttachmentPath", getSourceAttachmentPath().toString()); //$NON-NLS-1$
		}
		if (getSourceAttachmentRootPath() != null) {
			node.setAttribute("sourceRootPath", getSourceAttachmentRootPath().toString()); //$NON-NLS-1$
		}
		if (getExternalAnnotationsPath() != null) {
			node.setAttribute("externalAnnotationsPath", getExternalAnnotationsPath().toString()); //$NON-NLS-1$
		}
		if (getJavaProject() != null) {
			node.setAttribute("javaProject", getJavaProject().getElementName()); //$NON-NLS-1$
		}
		return DebugPlugin.serializeDocument(doc);
	}

	/**
	 * @see IRuntimeClasspathEntry#getPath()
	 */
	@Override
	public IPath getPath() {
		IClasspathEntry entry = getClasspathEntry();
		return entry != null ? entry.getPath() : fInvalidPath;
	}

	/**
	 * @see IRuntimeClasspathEntry#getResource()
	 */
	@Override
	public IResource getResource() {
		switch (getType()) {
			case CONTAINER:
			case VARIABLE:
				return null;
			default:
				return getResource(getPath());
		}
	}

	/**
	 * Returns the resource in the workspace associated with the given
	 * absolute path, or <code>null</code> if none. The path may have
	 * a device.
	 *
	 * @param path absolute path, or <code>null</code>
	 * @return resource or <code>null</code>
	 */
	protected IResource getResource(IPath path) {
		if (path != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (getType() == PROJECT || getType() == ARCHIVE) {
				// try the fastest lookup first:
				IResource member = root.findMember(path);
				if (member != null) {
					return member;
				}
			}

			// look for files or folders with the given path
			IFile file = root.getFileForLocation(path);
			if (file != null) {
				return file;
			}
			if (getType() != ARCHIVE) {
				IContainer container = root.getContainerForLocation(path);
				if (container != null) {
					return container;
				}
			}

			@SuppressWarnings("deprecation")
			IFile[] files = root.findFilesForLocation(path);
			if (files.length > 0) {
				return files[0];
			}

			if (getType() != ARCHIVE) {
				@SuppressWarnings("deprecation")
				IContainer[] containers = root.findContainersForLocation(path);
				if (containers.length > 0) {
					return containers[0];
				}
			}

			return root.findMember(path);
		}
		return null;
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	@Override
	public IPath getSourceAttachmentPath() {
		IClasspathEntry entry = getClasspathEntry();
		return entry != null ? entry.getSourceAttachmentPath() : null;
	}

	/**
	 * @see IRuntimeClasspathEntry#setSourceAttachmentPath(IPath)
	 */
	@Override
	public void setSourceAttachmentPath(IPath path) {
		if (path != null && path.isEmpty()) {
			path = null;
		}
		updateClasspathEntry(getPath(), path, getSourceAttachmentRootPath(), getExternalAnnotationsPath());
	}

	@Override
	public IPath getExternalAnnotationsPath() {
		IClasspathEntry entry = getClasspathEntry();
		if (null != entry) {
			String s = findClasspathAttribute(entry.getExtraAttributes(), IClasspathAttribute.EXTERNAL_ANNOTATION_PATH);
			if (null != s) {
				return new Path(s);
			}
		}
		return null;
	}

	private static String findClasspathAttribute(IClasspathAttribute[] attributes, String name) {
		for(int i = attributes.length; --i >= 0;) {
			if(name.equals(attributes[i].getName())) {
				return attributes[i].getValue();
			}
		}
		return null;
	}

	@Override
	public void setExternalAnnotationsPath(IPath path) {
		if (path != null && path.isEmpty()) {
			path = null;
		}
		updateClasspathEntry(getPath(), getSourceAttachmentPath(), getSourceAttachmentRootPath(), path);
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	@Override
	public IPath getSourceAttachmentRootPath() {
		IClasspathEntry entry = getClasspathEntry();
		IPath path = entry != null ? getClasspathEntry().getSourceAttachmentRootPath() : null;
		if (path == null && getSourceAttachmentPath() != null) {
			return Path.EMPTY;
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentRootPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setSourceAttachmentRootPath(IPath path) {
		if (path != null && path.isEmpty()) {
			path = null;
		}
		updateClasspathEntry(getPath(), getSourceAttachmentPath(), path, getExternalAnnotationsPath());
	}

	/**
	 * Initializes the classpath property based on this entry's type.
	 */
	private void initializeClasspathProperty() {
		switch (getType()) {
			case VARIABLE:
				if (getVariableName().equals(JavaRuntime.JRELIB_VARIABLE)) {
					setClasspathProperty(STANDARD_CLASSES);
				} else {
					setClasspathProperty(USER_CLASSES);
				}
				break;
			case PROJECT:
			case ARCHIVE:
				setClasspathProperty(USER_CLASSES);
				break;
			default:
				break;
		}
	}


	/**
	 * @see IRuntimeClasspathEntry#setClasspathProperty(int)
	 */
	@Override
	public void setClasspathProperty(int location) {
		fClasspathProperty = location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathProperty()
	 */
	@Override
	public int getClasspathProperty() {
		return fClasspathProperty;
	}

	/**
	 * @see IRuntimeClasspathEntry#getLocation()
	 */
	@Override
	public String getLocation() {

		IPath path = null;
		switch (getType()) {
			case PROJECT :
				IJavaProject pro = (IJavaProject) JavaCore.create(getResource());
				if (pro != null) {
					try {
						path = pro.getOutputLocation();
					} catch (JavaModelException e) {
						LaunchingPlugin.log(e);
					}
				}
				break;
			case ARCHIVE :
				path = getPath();
				break;
			case VARIABLE :
				IClasspathEntry resolved = getResolvedClasspathEntry();
				if (resolved != null) {
					path = resolved.getPath();
				}
				break;
			case CONTAINER :
				break;
		}
		return resolveToOSPath(path);
	}

	/**
	 * Returns the OS path for the given absolute or workspace relative path
	 * @param path the path
	 * @return the OS path
	 */
	protected String resolveToOSPath(IPath path) {
		if (path != null) {
			IResource res = null;
			if (path.getDevice() == null) {
				// if there is no device specified, find the resource
				res = getResource(path);
			}
			if (res == null) {
				return path.toOSString();
			}
			IPath location = res.getLocation();
			if (location != null) {
				return location.toOSString();
			}
		}
		return null;
	}

	/**
	 * @see IRuntimeClasspathEntry#getVariableName()
	 */
	@Override
	public String getVariableName() {
		if (getType() == IRuntimeClasspathEntry.VARIABLE || getType() == IRuntimeClasspathEntry.CONTAINER) {
			return getPath().segment(0);
		}
		return null;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRuntimeClasspathEntry r) {
			if (getType() == r.getType() && getClasspathProperty() == r.getClasspathProperty()) {
				if (getType() == IRuntimeClasspathEntry.CONTAINER) {
					String id = getPath().segment(0);
					ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(id);
					IJavaProject javaProject1 = getJavaProject();
					IJavaProject javaProject2 = r.getJavaProject();
					if (initializer == null || javaProject1 == null || javaProject2 == null) {
						// containers are equal if their ID is equal by default
						return getPath().equals(r.getPath());
					}
					Object comparisonID1 = initializer.getComparisonID(getPath(), javaProject1);
					Object comparisonID2 = initializer.getComparisonID(r.getPath(), javaProject2);
					return comparisonID1.equals(comparisonID2);
				} else if (getPath() != null && getPath().equals(r.getPath())) {
					IPath sa1 = getSourceAttachmentPath();
					IPath root1 = getSourceAttachmentRootPath();
					IPath sa2 = r.getSourceAttachmentPath();
					IPath root2 = r.getSourceAttachmentRootPath();
					return equal(sa1, sa2) && equal(root1, root2);
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether the given objects are equal, accounting for null
	 * @param one the object to compare
	 * @param two the object to compare to
	 * @return the equality of the objects
	 */
	protected boolean equal(Object one, Object two) {
		if (one == null) {
			return two == null;
		}
		return one.equals(two);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (getType() == CONTAINER) {
			return getPath().segment(0).hashCode() + getType();
		}
		return getPath().hashCode() + getType();
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentLocation()
	 */
	@Override
	public String getSourceAttachmentLocation() {
		IPath path = null;
		switch (getType()) {
			case VARIABLE :
			case ARCHIVE :
				IClasspathEntry resolved = getResolvedClasspathEntry();
				if (resolved != null) {
					path = resolved.getSourceAttachmentPath();
				}
				break;
			default :
				break;
		}
		return resolveToOSPath(path);
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentRootLocation()
	 */
	@Override
	public String getSourceAttachmentRootLocation() {
		IPath path = null;
		switch (getType()) {
			case VARIABLE :
			case ARCHIVE :
				IClasspathEntry resolved = getResolvedClasspathEntry();
				if (resolved != null) {
					path = resolved.getSourceAttachmentRootPath();
				}
				break;
			default :
				break;
		}
		if (path != null) {
			return path.toOSString();
		}
		return null;
	}

	/**
	 * Creates a new underlying classpath entry for this runtime classpath entry
	 * with the given paths, due to a change in source attachment.
	 * @param path the path
	 * @param sourcePath the source path
	 * @param rootPath the root path
	 */
	protected void updateClasspathEntry(IPath path, IPath sourcePath, IPath rootPath, IPath annotationsPath) {
		IClasspathEntry entry = null;
		IClasspathEntry original = getClasspathEntry();
		switch (getType()) {
			case ARCHIVE:
				IClasspathAttribute[] extraAttributes = original.getExtraAttributes();
				if (annotationsPath != null) {
					extraAttributes = setClasspathAttribute(extraAttributes, IClasspathAttribute.EXTERNAL_ANNOTATION_PATH, annotationsPath.toPortableString());
				}

				entry = JavaCore.newLibraryEntry(path, sourcePath, rootPath, original.getAccessRules(), extraAttributes, original.isExported());
				break;
			case VARIABLE:
				entry = JavaCore.newVariableEntry(path, sourcePath, rootPath);
				break;
			default:
				return;
		}
		setClasspathEntry(entry);
	}

	private static IClasspathAttribute[] setClasspathAttribute(IClasspathAttribute[] attributes, String name, String value) {
		for (int i = attributes.length; --i >= 0;) {
			if (name.equals(attributes[i].getName())) {
				IClasspathAttribute[] nw = Arrays.copyOf(attributes, attributes.length);
				nw[i] = JavaCore.newClasspathAttribute(name, value);
				return nw;
			}
		}
		IClasspathAttribute[] nw = Arrays.copyOf(attributes, attributes.length + 1);
		nw[attributes.length] = JavaCore.newClasspathAttribute(name, value);
		return nw;
	}

	/**
	 * Returns the resolved classpath entry associated with this runtime
	 * entry, resolving if required.
	 * @return the resolved {@link IClasspathEntry}
	 */
	protected IClasspathEntry getResolvedClasspathEntry() {
		if (fResolvedEntry == null) {
			fResolvedEntry = JavaCore.getResolvedClasspathEntry(getClasspathEntry());
		}
		return fResolvedEntry;
	}

	protected boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	@Override
	public String toString() {
		if (fClasspathEntry != null) {
			return fClasspathEntry.toString();
		}
		return super.toString();

	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getJavaProject()
	 */
	@Override
	public IJavaProject getJavaProject() {
		return fJavaProject;
	}

	/**
	 * Sets the Java project associated with this classpath entry.
	 *
	 * @param project Java project
	 */
	public void setJavaProject(IJavaProject project) {
		fJavaProject = project;
	}

	@Override
	public boolean isAutomodule() {
		IClasspathAttribute[] extraAttributes = getClasspathEntry().getExtraAttributes();
		for (IClasspathAttribute attribute : extraAttributes) {
			if (IClasspathAttribute.MODULE.equals(attribute.getName()) && Boolean.TRUE.toString().equals(attribute.getValue())) {
				return true;
			}
		}
		return false;
	}
}
