package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * An entry on the runtime classpath that the user can manipulate
 * and share in a lanuch configuration.
 * 
 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry
 * @since 2.0
 */
public class RuntimeClasspathEntry implements IRuntimeClasspathEntry {

	/**
	 * The entry's type - must be set on creation.
	 */
	private int fType = -1;

	/**
	 * This entry's associated build path entry.
	 */
	private IClasspathEntry fClasspathEntry = null;

	/**
	 * Constructs a new runtime classpath entry.
	 * 
	 * @param type the type of entry
	 * @param entry the associated classpath entry
	 */
	public RuntimeClasspathEntry(int type, IClasspathEntry entry) {
		setType(type);
		setClasspathEntry(entry);
	}

	/**
	 * @see IRuntimeClasspathEntry#getType()
	 */
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
	 *
	 * @param entry the classpath entry associated with this runtime classpath entry
	 */
	private void setClasspathEntry(IClasspathEntry entry) {
		fClasspathEntry = entry;
	}

	/**
	 * Returns the classpath entry associated with this runtime classpath entry,
	 * or <code>null</code> if none.
	 *
	 * @return classpath entry associated with this runtime classpath entry,
	 *  or <code>null</code> if none
	 */
	protected IClasspathEntry getClasspathEntry() {
		return fClasspathEntry;
	}

	/**
	 * @see IRuntimeClasspathEntry#getMemento()
	 */
	public String getMemento() {
		return null;
	}

	/**
	 * @see IRuntimeClasspathEntry#getPath()
	 */
	public IPath getPath() {
		return getClasspathEntry().getPath();
	}

	/**
	 * @see IRuntimeClasspathEntry#getResource()
	 */
	public IResource getResource() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(getPath());
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentPaths()
	 * 
	 * [XXX: fix for libraries
	 */
	public IPath[] getSourceAttachmentPaths() {
		IPath path = null;
		switch (getType()) {
			case ARCHIVE:
				path = getClasspathEntry().getSourceAttachmentPath();
				break;
			case VARIABLE:
				IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(getClasspathEntry());
				if (resolved != null) {
					path = resolved.getSourceAttachmentPath();
					break;
				}
			default:
				return null;
		}
		if (path != null) {
			return new IPath[] {path};
		}
		return null;
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentRootPaths()
	 * 
	 * [XXX: fix for libraries
	 */
	public IPath[] getSourceAttachmentRootPaths() {
		IPath path = null;
		switch (getType()) {
			case ARCHIVE:
				path = getClasspathEntry().getSourceAttachmentRootPath();
				break;
			case VARIABLE:
				IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(getClasspathEntry());
				if (resolved != null) {
					path = resolved.getSourceAttachmentRootPath();
					break;
				}
			default:
				return null;
		}
		if (path != null) {
			return new IPath[] {path};
		}
		return null;		
	}

	/**
	 * @see IRuntimeClasspathEntry#getClasspathProperty()
	 * 
	 * [XXX: to be fixed for libraries]
	 */
	public int getClasspathProperty() {
		switch (getType()) {
			case VARIABLE:
				String name = getPath().segment(0);
				if (name.equals(JavaRuntime.JRELIB_VARIABLE)) {
					return STANDARD_CLASSES;
				} else {
					return USER_CLASSES;
				}
			default:
				return USER_CLASSES;
		}
	}

	/**
	 * @see IRuntimeClasspathEntry#getResolvedPaths()
	 */
	public String[] getResolvedPaths() {

		IPath path = null;
		switch (getType()) {
			case PROJECT :
				IJavaProject pro = (IJavaProject) JavaCore.create(getResource());
				try {
					path = pro.getOutputLocation();
				} catch (JavaModelException e) {
					LaunchingPlugin.log(e);
				}
				break;
			case ARCHIVE :
				path = getPath();
				break;
			case VARIABLE :
				IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(getClasspathEntry());
				if (resolved != null) {
					path = resolved.getPath();
				}
				break;
			case LIBRARY :
				break;
		}
		if (path != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = root.findMember(path);
			if (res == null) {
				return new String[] { path.toOSString() };
			} else {
				return new String[] { res.getLocation().toOSString()};
			}
		}
		return null;
	}

}