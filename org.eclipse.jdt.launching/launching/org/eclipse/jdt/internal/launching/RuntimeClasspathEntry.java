package org.eclipse.jdt.internal.launching;

/**********************************************************************
Copyright (c) 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
				throw new IllegalArgumentException(MessageFormat.format(LaunchingMessages.getString("RuntimeClasspathEntry.Illegal_classpath_entry_{0}_1"), new String[] {entry.toString()})); //$NON-NLS-1$
		}
		setClasspathEntry(entry);
		initializeClasspathProperty();
	}
	
	/**
	 * Constructs a new container entry in the context of the given project
	 * 
	 * @param entry classpath entry
	 * @param classpathProperty this entry's classpath property
	 * @exception CoreException if unable to resolve the given entry
	 */
	public RuntimeClasspathEntry(IClasspathEntry entry, int classpathProperty) throws CoreException {
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_CONTAINER:
				setType(CONTAINER);
				break;
			default:
				throw new IllegalArgumentException(MessageFormat.format(LaunchingMessages.getString("RuntimeClasspathEntry.Illegal_classpath_entry_{0}_1"), new String[] {entry.toString()})); //$NON-NLS-1$
		}
		setClasspathEntry(entry);
		setClasspathProperty(classpathProperty);
	}
	
	/**
	 * Reconstructs a runtime classpath entry from the given
	 * memento.
	 * 
	 * @param memento a memento created by this class
	 * @exception CoreException if unable to restore from the given memento
	 */
	public RuntimeClasspathEntry(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			try {
				setType(Integer.parseInt(root.getAttribute("type"))); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_type_2"), e); //$NON-NLS-1$
			}
			try {
				setClasspathProperty(Integer.parseInt(root.getAttribute("path"))); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_location_3"), e); //$NON-NLS-1$
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
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_project_name_4"), null); //$NON-NLS-1$
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
							abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_archive_path_5"), null); //$NON-NLS-1$
						} else {
							IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
							if (res == null) {
								abort(LaunchingMessages.getString("RuntimeClasspathEntry.Internal_archive_no_longer_exists___1") + path, null); //$NON-NLS-1$
							}
							setClasspathEntry(JavaCore.newLibraryEntry(res.getFullPath(), sourcePath, rootPath));
						}
					} else {
						// external
						setClasspathEntry(JavaCore.newLibraryEntry(new Path(path), sourcePath, rootPath));
					}
					break;
				case VARIABLE :
					String var = root.getAttribute("containerPath"); //$NON-NLS-1$
					if (isEmpty(var)) {
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_variable_name_6"), null); //$NON-NLS-1$
					} else {
						setClasspathEntry(JavaCore.newVariableEntry(new Path(var), sourcePath, rootPath));
					}
					break;
				case CONTAINER :
					var = root.getAttribute("containerPath"); //$NON-NLS-1$
					if (isEmpty(var)) {
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_variable_name_6"), null); //$NON-NLS-1$
					} else {
						setClasspathEntry(JavaCore.newContainerEntry(new Path(var)));
					}
					break;
			}	
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_parsing_error_8"), ex);	 //$NON-NLS-1$
	}
	
	/**
	 * Throws an internal error exception
	 */
	protected void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
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
	 * Clears the cache of the resolved entry.
	 *
	 * @param entry the classpath entry associated with this runtime classpath entry
	 */
	private void setClasspathEntry(IClasspathEntry entry) {
		fClasspathEntry = entry;
		fResolvedEntry = null;
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
	public String getMemento() throws CoreException {
		
		Document doc = new DocumentImpl();
		Element node = doc.createElement("runtimeClasspathEntry"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("type", (new Integer(getType())).toString()); //$NON-NLS-1$
		node.setAttribute("path", (new Integer(getClasspathProperty())).toString()); //$NON-NLS-1$
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
		
		try {
			return JavaLaunchConfigurationUtils.serializeDocument(doc);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, LaunchingMessages.getString("RuntimeClasspathEntry.An_exception_occurred_generating_runtime_classpath_memento_8"), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
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
		switch (getType()) {
			case CONTAINER:
			case VARIABLE:
				return null;
			default:
				return root.findMember(getPath());
		}
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	public IPath getSourceAttachmentPath() {
		return getClasspathEntry().getSourceAttachmentPath();
	}

	/**
	 * @see IRuntimeClasspathEntry#setSourceAttachmentPath(IPath)
	 */
	public void setSourceAttachmentPath(IPath path) {
		updateClasspathEntry(getPath(), path, getSourceAttachmentRootPath());
	}
	
	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	public IPath getSourceAttachmentRootPath() {
		IPath path = getClasspathEntry().getSourceAttachmentRootPath();
		if (path == null && getSourceAttachmentPath() != null) {
			return Path.EMPTY;
		} else {
			return path;
		}
	}

	/**
	 * @see IRuntimeClasspathEntry#setSourceAttachmentPath(IPath)
	 */
	public void setSourceAttachmentRootPath(IPath path) {
		updateClasspathEntry(getPath(), getSourceAttachmentPath(), path);		
	}
	
	/**
	 * Initlaizes the classpath property based on this entry's type.
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
	public void setClasspathProperty(int location) {
		fClasspathProperty = location;
	}

	/**
	 * @see IRuntimeClasspathEntry#setClasspathProperty(int)
	 */
	public int getClasspathProperty() {
		return fClasspathProperty;
	}

	/**
	 * @see IRuntimeClasspathEntry#getLocation()
	 */
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
	 * Returns the OS path for the given aboslute or workspace relative path
	 */
	protected String resolveToOSPath(IPath path) {
		if (path != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = root.findMember(path);
			if (res == null) {
				return path.toOSString();
			} else {
				return res.getLocation().toOSString();
			}
		}
		return null;		
	}

	/**
	 * @see IRuntimeClasspathEntry#getVariableName()
	 */
	public String getVariableName() {
		if (getType() == IRuntimeClasspathEntry.VARIABLE || getType() == IRuntimeClasspathEntry.CONTAINER) {
			return getPath().segment(0);
		} else {
			return null;
		}
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IRuntimeClasspathEntry) {
			IRuntimeClasspathEntry r = (IRuntimeClasspathEntry)obj;
			if (getType() == r.getType() && getClasspathProperty() == r.getClasspathProperty()) {
				if (getType() == IRuntimeClasspathEntry.CONTAINER) {
					// containers are equal if their ID is equal
					return getPath().equals(r.getPath());
				} else if (getPath().equals(r.getPath())) {
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
	 */
	protected boolean equal(Object one, Object two) {
		if (one == null) {
			return two == null;
		} else {
			return one.equals(two);
		}
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		if (getType() == CONTAINER) {
			return getPath().segment(0).hashCode() + getType();
		} else {
			return getPath().hashCode() + getType();
		}
	}

	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentLocation()
	 */
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
	 */
	protected void updateClasspathEntry(IPath path, IPath sourcePath, IPath rootPath) {
		IClasspathEntry entry = null;
		switch (getType()) {
			case ARCHIVE:
				entry = JavaCore.newLibraryEntry(path, sourcePath, rootPath);
				break;
			case VARIABLE:
				entry = JavaCore.newVariableEntry(path, sourcePath, rootPath);
				break;
			default:
				return;
		}		
		setClasspathEntry(entry);		
	}
	
	/**
	 * Returns the resolved classpath entry associated with this runtime
	 * entry, resolving if required.
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
}