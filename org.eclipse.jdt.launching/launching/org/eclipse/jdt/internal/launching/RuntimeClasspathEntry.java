package org.eclipse.jdt.internal.launching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
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
 * and share in a lanuch configuration.
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
			case IClasspathEntry.CPE_CONTAINER:
				setType(LIBRARY);
			default:
				throw new IllegalArgumentException(LaunchingMessages.getString("RuntimeClasspathEntry.Illegal_classpath_entry_kind_1")); //$NON-NLS-1$
		}		
		setClasspathEntry(entry);
		initializeClasspathProperty();
		setSourceAttachmentPath(entry.getSourceAttachmentPath());
		setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
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

			switch (getType()) {
				case PROJECT :
					String name = root.getAttribute("projectName"); //$NON-NLS-1$
					if (name == null) {
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_project_name_4"), null); //$NON-NLS-1$
					} else {
						IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						setClasspathEntry(JavaCore.newProjectEntry(proj.getLocation()));
					}
					break;
				case ARCHIVE :
					String path = root.getAttribute("externalArchive"); //$NON-NLS-1$
					if (path == null) {
						// internal
						path = root.getAttribute("internalArchive"); //$NON-NLS-1$
						if (path == null) {
							abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_archive_path_5"), null); //$NON-NLS-1$
						} else {
							IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
							setClasspathEntry(JavaCore.newLibraryEntry(res.getLocation(), null, null));
						}
					} else {
						// external
						setClasspathEntry(JavaCore.newLibraryEntry(new Path(path), null, null));
					}
					break;
				case VARIABLE :
					String var = root.getAttribute("variablePath"); //$NON-NLS-1$
					if (var == null) {
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_variable_name_6"), null); //$NON-NLS-1$
					} else {
						setClasspathEntry(JavaCore.newVariableEntry(new Path(var), null, null));
					}
					break;
				case LIBRARY :
					var = root.getAttribute("variablePath"); //$NON-NLS-1$
					if (var == null) {
						abort(LaunchingMessages.getString("RuntimeClasspathEntry.Unable_to_recover_runtime_class_path_entry_-_missing_variable_name_7"), null); //$NON-NLS-1$
					} else {
						setClasspathEntry(JavaCore.newContainerEntry(new Path(var)));
					}
					break;
			}	
			// source attachment
			String path = root.getAttribute("sourceAttachmentPath"); //$NON-NLS-1$
			if (path != null && path.length() > 0) {
				setSourceAttachmentPath(new Path(path));
			}
			path = root.getAttribute("sourceRootPath"); //$NON-NLS-1$
			if (path != null && path.length() > 0) {
				setSourceAttachmentRootPath(new Path(path));
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
			case LIBRARY :
				node.setAttribute("variablePath", getPath().toString()); //$NON-NLS-1$
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
		IPath path = getPath();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath rootPath = root.getLocation();
		int match = rootPath.matchingFirstSegments(path);
		if (match == rootPath.segmentCount()) {
			path = path.removeFirstSegments(match);
			return root.findMember(path);
		} else {
			return null;
		}
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
	 * Initlaizes the classpath property based on this entry's type.
	 * 
	 * XXX: fix for libraries
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
			case LIBRARY:
				// XXX: fix for libraries
				break;
			default:
				setClasspathProperty(USER_CLASSES);
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

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IRuntimeClasspathEntry) {
			IRuntimeClasspathEntry r = (IRuntimeClasspathEntry)obj;
			if (getType() == r.getType()) {
				if (getPath().equals(r.getPath())) {
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
		return getPath().hashCode() + getType();
	}

}