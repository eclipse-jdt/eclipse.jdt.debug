package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.StringWriter;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
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
	 * Source attachment path
	 */
	private IPath fSourceAttachmentPath = null;
	
	/**
	 * Root source path
	 */
	private IPath fRootSourcePath = null;

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
		setSourceAttachmentPath(entry.getSourceAttachmentPath());
		setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
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
	public String getMemento() throws CoreException {
		
		Document doc = new DocumentImpl();
		Element node = doc.createElement("runtimeClasspathEntry"); //$NON-NLS-1$
		node.setAttribute("type", (new Integer(getType())).toString()); //$NON-NLS-1$
		node.setAttribute("path", (new Integer(getClasspathProperty())).toString()); //$NON-NLS-1$
		switch (getType()) {
			case PROJECT :
				node.setAttribute("projectName", getResource().getName()); //$NON-NLS-1$
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
				node.setAttribute("variableName", getVariableName()); //$NON-NLS-1$
				break;
			case LIBRARY :
				node.setAttribute("libraryName", getVariableName()); //$NON-NLS-1$
				break;
		}		
		if (getSourceAttachmentPath() != null) {
			node.setAttribute("sourceAttachmentPath", getSourceAttachmentPath().toString()); //$NON-NLS-1$
		}
		if (getSourceAttachmentRootPath() != null) {
			node.setAttribute("sourceRootPath", getSourceAttachmentRootPath().toString()); //$NON-NLS-1$
		}
		
		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		
		try {
			serializer.asDOMSerializer().serialize(node);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, LaunchingMessages.getString("RuntimeClasspathEntry.An_exception_occurred_generating_runtime_classpath_memento_8"), e); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return writer.toString();
				
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
	 * @see IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	public IPath getSourceAttachmentPath() {
		return fSourceAttachmentPath;
	}

	/**
	 * @see IRuntimeClasspathEntry#setSourceAttachmentPath(IPath)
	 */
	public void setSourceAttachmentPath(IPath path) {
		fSourceAttachmentPath = path;
	}
	
	/**
	 * @see IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	public IPath getSourceAttachmentRootPath() {
		return fRootSourcePath;
	}

	/**
	 * @see IRuntimeClasspathEntry#setSourceAttachmentPath(IPath)
	 */
	public void setSourceAttachmentRootPath(IPath path) {
		fRootSourcePath = path;
	}
	
	/**
	 * @see IRuntimeClasspathEntry#getClasspathProperty()
	 * 
	 * [XXX: to be fixed for libraries]
	 */
	public int getClasspathProperty() {
		switch (getType()) {
			case VARIABLE:
				if (getVariableName().equals(JavaRuntime.JRELIB_VARIABLE)) {
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

	/**
	 * @see IRuntimeClasspathEntry#getVariableName()
	 */
	public String getVariableName() {
		if (getType() == IRuntimeClasspathEntry.VARIABLE || getType() == IRuntimeClasspathEntry.LIBRARY) {
			return getPath().segment(0);
		} else {
			return null;
		}
	}

}