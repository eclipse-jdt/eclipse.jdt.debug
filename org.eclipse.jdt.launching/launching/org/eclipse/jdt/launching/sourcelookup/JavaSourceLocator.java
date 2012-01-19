/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Locates source for a Java debug session by searching
 * a configurable set of source locations.
 * <p>
 * This class may be instantiated.
 * </p>
 * @see org.eclipse.debug.core.model.ISourceLocator
 * @since 2.0
 * @deprecated In 3.0, the debug platform provides source lookup facilities that
 *  should be used in place of the Java source lookup support provided in 2.0.
 *  The new facilities provide a source lookup director that coordinates source
 *  lookup among a set of participants, searching a set of source containers.
 *  See the following packages: <code>org.eclipse.debug.core.sourcelookup</code>
 *  and <code>org.eclipse.debug.core.sourcelookup.containers</code>. This class
 *  has been replaced by a Java source lookup director and Java source lookup
 *  participant. To migrate to the new source lookup support clients should
 *  add two new attributes to their launch configuration type extensions:<ul>
 *  <li>sourceLocatorId="org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector"</li>
 *  <li>sourcePathComputerId="org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer"</li>
 *  </ul>
 *  The source locator id attribute specifies to use the Java source lookup director
 *  for launch configurations of the associated type, and the source path computer id
 *  attribute specifies the class to use when computing a default source lookup
 *  path for a launch configuration. The path computer referenced/provided (by the
 *  above id), computes a default source lookup path based on the support provided in
 *  the 2.0 release - i.e. a configuration's <code>ATTR_SOURCE_PATH_PROVIDER</code>
 *  attribute (if present), or a default source lookup path based on a configuration's
 *  runtime classpath. This class has been replaced by the Java source lookup
 *  director which is an internal class, but can be used via the
 *  <code>sourceLocatorId</code> attribute on a launch configuration type extension.  
 * @noextend This class is not intended to be sub-classed by clients.
 */
@Deprecated
public class JavaSourceLocator implements IPersistableSourceLocator {
	
	/**
	 * Identifier for the 'Java Source Locator' extension
	 * (value <code>"org.eclipse.jdt.launching.javaSourceLocator"</code>).
	 */
	public static final String ID_JAVA_SOURCE_LOCATOR = LaunchingPlugin.getUniqueIdentifier() + ".javaSourceLocator"; //$NON-NLS-1$

	/**
	 * A collection of the source locations to search
	 */
	private IJavaSourceLocation[] fLocations;

	/**
	 * Constructs a new empty JavaSourceLocator.
	 */
	public JavaSourceLocator() {
		setSourceLocations(new IJavaSourceLocation[0]);
	}
	
	/**
	 * Constructs a new Java source locator that looks in the
	 * specified project for source, and required projects, if
	 * <code>includeRequired</code> is <code>true</code>.
	 * 
	 * @param projects the projects in which to look for source
	 * @param includeRequired whether to look in required projects
	 * 	as well
	 * @throws CoreException if a new locator fails to be created
	 */
	public JavaSourceLocator(IJavaProject[] projects, boolean includeRequired) throws CoreException {
		ArrayList<IJavaProject> requiredProjects = new ArrayList<IJavaProject>();
		for (int i= 0; i < projects.length; i++) {
			if (includeRequired) {
				collectRequiredProjects(projects[i], requiredProjects);
			} else {
				if (!requiredProjects.contains(projects[i])) {
					requiredProjects.add(projects[i]);
				}
			}
		}
		
		// only add external entries with the same location once
		HashMap<IPath, IPath> external = new HashMap<IPath, IPath>();
		ArrayList<PackageFragmentRootSourceLocation> list = new ArrayList<PackageFragmentRootSourceLocation>();
		// compute the default locations for each project, and add unique ones
		Iterator<IJavaProject> iter = requiredProjects.iterator();
		while (iter.hasNext()) {
			IJavaProject p = iter.next();
			IPackageFragmentRoot[] roots = p.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].isExternal()) {
					IPath location = roots[i].getPath();
					if (external.get(location) == null) {
						external.put(location, location);
						list.add(new PackageFragmentRootSourceLocation(roots[i]));
					}
				} else {
					list.add(new PackageFragmentRootSourceLocation(roots[i]));
				}
			}
		}
		IJavaSourceLocation[] locations = list.toArray(new IJavaSourceLocation[list.size()]);
		setSourceLocations(locations);
	}	
	
	/**
	 * Constructs a new JavaSourceLocator that searches the
	 * specified set of source locations for source elements.
	 * 
	 * @param locations the source locations to search for
	 *  source, in the order they should be searched
	 */
	public JavaSourceLocator(IJavaSourceLocation[] locations) {
		setSourceLocations(locations);
	}
	
	/**
	 * Constructs a new JavaSourceLocator that searches the
	 * default set of source locations for the given Java project.
	 * 
	 * @param project Java project
	 * @exception CoreException if an exception occurs reading
	 *  the classpath of the given or any required project
	 */
	public JavaSourceLocator(IJavaProject project) throws CoreException {
		setSourceLocations(getDefaultSourceLocations(project));
	}	
	
	/**
	 * Sets the locations that will be searched, in the order
	 * to be searched.
	 * 
	 * @param locations the locations that will be searched, in the order
	 *  to be searched
	 */
	public void setSourceLocations(IJavaSourceLocation[] locations) {
		fLocations = locations;
	}
	
	/**
	 * Returns the locations that this source locator is currently
	 * searching, in the order that they are searched.
	 * 
	 * @return the locations that this source locator is currently
	 * searching, in the order that they are searched
	 */
	public IJavaSourceLocation[] getSourceLocations() {
		return fLocations;
	}
	
	/**
	 * Returns all source elements that correspond to the type associated with
	 * the given stack frame, or <code>null</code> if none.
	 * 
	 * @param stackFrame stack frame
	 * @return all source elements that correspond to the type associated with
	 * the given stack frame, or <code>null</code> if none
	 * @since 2.1
	 */
	public Object[] getSourceElements(IStackFrame stackFrame) {
		if (stackFrame instanceof IJavaStackFrame) {
			IJavaStackFrame frame = (IJavaStackFrame)stackFrame;
			String name = null;
			try {
				name = getFullyQualfiedName(frame);
				if (name == null) {
					return null;
				}
			} catch (CoreException e) {
				// if the thread has since resumed, return null
				if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					LaunchingPlugin.log(e);
				}
				return null;
			}
			List<Object> list = new ArrayList<Object>();
			IJavaSourceLocation[] locations = getSourceLocations();
			for (int i = 0; i < locations.length; i++) {
				try {
					Object sourceElement = locations[i].findSourceElement(name);
					if (sourceElement != null) {
						list.add(sourceElement);
					}
				} catch (CoreException e) {
					// log the error and try the next source location
					LaunchingPlugin.log(e);
				}
			}
			return list.toArray();
		}
		return null;
	}	
			
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(org.eclipse.debug.core.model.IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		if (stackFrame instanceof IJavaStackFrame) {
			IJavaStackFrame frame = (IJavaStackFrame)stackFrame;
			String name = null;
			try {
				name = getFullyQualfiedName(frame);
				if (name == null) {
					return null;
				}
			} catch (CoreException e) {
				// if the thread has since resumed, return null
				if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					LaunchingPlugin.log(e);
				}
				return null;
			}
			IJavaSourceLocation[] locations = getSourceLocations();
			for (int i = 0; i < locations.length; i++) {
				try {
					Object sourceElement = locations[i].findSourceElement(name);
					if (sourceElement != null) {
						return sourceElement;
					}
				} catch (CoreException e) {
					// log the error and try the next source location
					LaunchingPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	private String getFullyQualfiedName(IJavaStackFrame frame) throws CoreException {
		String name = null;
		if (frame.isObsolete()) {
			return null;
		}
		String sourceName = frame.getSourceName();
		if (sourceName == null) {
			// no debug attributes, guess at source name
			name = frame.getDeclaringTypeName();
		} else {
			// build source name from debug attributes using
			// the source file name and the package of the declaring
			// type
					
			// @see bug# 21518 - remove absolute path prefix
			int index = sourceName.lastIndexOf('\\');
			if (index == -1) {
				index = sourceName.lastIndexOf('/');
			}
			if (index >= 0) {
				sourceName = sourceName.substring(index + 1);
			}
					
			String declName= frame.getDeclaringTypeName();
			index = declName.lastIndexOf('.');
			if (index >= 0) {
				name = declName.substring(0, index + 1);
			} else {
				name = ""; //$NON-NLS-1$
			}
			index = sourceName.lastIndexOf('.');
			if (index >= 0) {
				name += sourceName.substring(0, index) ;
			}					
		}
		return name;		
	}
	
	/**
	 * Adds all projects required by <code>proj</code> to the list
	 * <code>res</code>
	 * 
	 * @param proj the project for which to compute required
	 *  projects
	 * @param res the list to add all required projects too
	 * @throws JavaModelException if there is a problem with the backing Java model
	 */
	protected static void collectRequiredProjects(IJavaProject proj, ArrayList<IJavaProject> res) throws JavaModelException {
		if (!res.contains(proj)) {
			res.add(proj);
			
			IJavaModel model= proj.getJavaModel();
			
			IClasspathEntry[] entries= proj.getRawClasspath();
			for (int i= 0; i < entries.length; i++) {
				IClasspathEntry curr= entries[i];
				if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IJavaProject ref= model.getJavaProject(curr.getPath().segment(0));
					if (ref.exists()) {
						collectRequiredProjects(ref, res);
					}
				}
			}
		}
	}	
	
	/**
	 * Returns a default collection of source locations for
	 * the given Java project. Default source locations consist
	 * of the given project and all of its required projects .
	 * 
	 * @param project Java project
	 * @return a collection of source locations for all required
	 *  projects
	 * @exception CoreException if an exception occurs reading
	 *  computing the default locations
	 */
	public static IJavaSourceLocation[] getDefaultSourceLocations(IJavaProject project) throws CoreException {
		// create a temporary launch config
		ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		ILaunchConfigurationWorkingCopy config = type.newInstance(null, project.getElementName());
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
		JavaSourceLocator locator = new JavaSourceLocator();
		locator.initializeDefaults(config);
		return locator.getSourceLocations();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element node = doc.createElement("javaSourceLocator"); //$NON-NLS-1$
		doc.appendChild(node);
		
		IJavaSourceLocation[] locations = getSourceLocations();
		for (int i = 0; i < locations.length; i++) {
			Element child = doc.createElement("javaSourceLocation"); //$NON-NLS-1$
			child.setAttribute("class", locations[i].getClass().getName()); //$NON-NLS-1$
			child.setAttribute("memento", locations[i].getMemento()); //$NON-NLS-1$
			node.appendChild(child);
		}
		return DebugPlugin.serializeDocument(doc); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(configuration);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
		setSourceLocations(getSourceLocations(resolved));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			if (!root.getNodeName().equalsIgnoreCase("javaSourceLocator")) {  //$NON-NLS-1$
				abort(LaunchingMessages.JavaSourceLocator_Unable_to_restore_Java_source_locator___invalid_format__6, null); 
			}
	
			List<IJavaSourceLocation> sourceLocations = new ArrayList<IJavaSourceLocation>();
			ClassLoader classLoader = LaunchingPlugin.getDefault().getDescriptor().getPluginClassLoader(); 
			
			NodeList list = root.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element entry = (Element) node;
					if (entry.getNodeName().equalsIgnoreCase("javaSourceLocation")) { //$NON-NLS-1$
						String className = entry.getAttribute("class"); //$NON-NLS-1$
						String data = entry.getAttribute("memento"); //$NON-NLS-1$
						if (isEmpty(className)) {
							abort(LaunchingMessages.JavaSourceLocator_Unable_to_restore_Java_source_locator___invalid_format__10, null); 
						}
						Class<?> clazz  = null;
						try {
							clazz = classLoader.loadClass(className);
						} catch (ClassNotFoundException e) {
							abort(NLS.bind(LaunchingMessages.JavaSourceLocator_Unable_to_restore_source_location___class_not_found___0__11, new String[] {className}), e); 
						}
						
						IJavaSourceLocation location = null;
						try {
							location = (IJavaSourceLocation)clazz.newInstance();
						} catch (IllegalAccessException e) {
							abort(LaunchingMessages.JavaSourceLocator_Unable_to_restore_source_location__12, e); 
						} catch (InstantiationException e) {
							abort(LaunchingMessages.JavaSourceLocator_Unable_to_restore_source_location__12, e); 
						}
						location.initializeFrom(data);
						sourceLocations.add(location);
					} else {
						abort(LaunchingMessages.JavaSourceLocator_Unable_to_restore_Java_source_locator___invalid_format__14, null); 
					}
				}
			}
			setSourceLocations(sourceLocations.toArray(new IJavaSourceLocation[sourceLocations.size()]));
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.JavaSourceLocator_Exception_occurred_initializing_source_locator__15, ex); 
	}
	
	/**
	 * Returns source locations that are associated with the given runtime classpath
	 * entries.
	 * @param entries the entries to compute source locations for
	 * @return the array of {@link IJavaSourceLocation}
	 */
	private static IJavaSourceLocation[] getSourceLocations(IRuntimeClasspathEntry[] entries) {
		List<IJavaSourceLocation> locations = new ArrayList<IJavaSourceLocation>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			IJavaSourceLocation location = null;
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					IProject project = (IProject)entry.getResource();
					if (project != null && project.exists() && project.isOpen()) {
						location = new JavaProjectSourceLocation(JavaCore.create(project));
					}
					break;
				case IRuntimeClasspathEntry.ARCHIVE:
					// check if the archive is in the workspace as a package fragment root
					location = getArchiveSourceLocation(entry);
					if (location == null) {
						String path = entry.getSourceAttachmentLocation();
						if (path == null) {
							// if there is no source attachment, look in the archive itself
							path = entry.getLocation();
						}
						if (path != null) {
							File file = new File(path);
							if (file.exists()) {
								if (file.isDirectory()) {
									location = new DirectorySourceLocation(file);
								} else {
									location = new ArchiveSourceLocation(path, entry.getSourceAttachmentRootLocation());
								}
							}
						}
					}
					break;
				case IRuntimeClasspathEntry.VARIABLE:
					String source = entry.getSourceAttachmentLocation();
					if (source != null) {
						location = new ArchiveSourceLocation(source, entry.getSourceAttachmentRootLocation());
					}
					break;
				case IRuntimeClasspathEntry.CONTAINER:
					throw new IllegalArgumentException(LaunchingMessages.JavaSourceLocator_Illegal_to_have_a_container_resolved_to_a_container_1); 
			}
			if (location != null) {
				locations.add(location);
			}
		}
		return locations.toArray(new IJavaSourceLocation[locations.size()]);		
	}
	
	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	/**
	 * Throws an internal error exception
	 * @param message the message
	 * @param e the error
	 * @throws CoreException the new {@link CoreException} 
	 */
	private void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
	}	
	
	/**
	 * Returns whether the given objects are equal, allowing
	 * for <code>null</code>.
	 * 
	 * @param a the first item
	 * @param b the item to compare
	 * @return whether the given objects are equal, allowing
	 *   for <code>null</code>
	 */
	private static boolean equalOrNull(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return a.equals(b);
	}
	
	/**
	 * Returns whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal.
	 * 
	 * @param root package fragment root
	 * @param entry runtime classpath entry
	 * @return whether the source attachments of the given package fragment
	 * root and runtime classpath entry are equal
	 * @throws JavaModelException if there is a problem with the backing Java model
	 */
	private static boolean isSourceAttachmentEqual(IPackageFragmentRoot root, IRuntimeClasspathEntry entry) throws JavaModelException {
		return equalOrNull(root.getSourceAttachmentPath(), entry.getSourceAttachmentPath());
	}
	
	/**
	 * Determines if the given archive runtime classpath entry exists
	 * in the workspace as a package fragment root. Returns the associated
	 * package fragment root source location if possible, otherwise
	 * <code>null</code>.
	 *  
	 * @param entry archive runtime classpath entry
	 * @return IJavaSourceLocation or <code>null</code>
	 */
	private static IJavaSourceLocation getArchiveSourceLocation(IRuntimeClasspathEntry entry) {
		IResource resource = entry.getResource();
		if (resource == null) { 
			// Check all package fragment roots for case of external archive.
			// External jars are shared, so it does not matter which project it
			// originates from
			IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			try {
				IJavaProject[] jps = model.getJavaProjects();
				for (int i = 0; i < jps.length; i++) {
					IPackageFragmentRoot[] allRoots = jps[i].getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						IPackageFragmentRoot root = allRoots[j];
						if (root.isExternal() && root.getPath().equals(new Path(entry.getLocation()))) {
							if (isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return new PackageFragmentRootSourceLocation(root);
							}							
						}
					}
				}
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}
		} else {
			// check if the archive is a package fragment root
			IProject project = resource.getProject();
			IJavaProject jp = JavaCore.create(project);
			try {
				if (jp != null && jp.exists()) {
					IPackageFragmentRoot root = jp.getPackageFragmentRoot(resource);
					IPackageFragmentRoot[] allRoots = jp.getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						if (allRoots[j].equals(root)) {
							// ensure source attachment paths match
							if (isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return new PackageFragmentRootSourceLocation(root);
							}
						}
					}

				}
				// check all other java projects to see if another project references
				// the archive
				IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
				IJavaProject[] jps = model.getJavaProjects();
				for (int i = 0; i < jps.length; i++) {
					IPackageFragmentRoot[] allRoots = jps[i].getPackageFragmentRoots();
					for (int j = 0; j < allRoots.length; j++) {
						IPackageFragmentRoot root = allRoots[j];
						if (!root.isExternal() && root.getPath().equals(entry.getPath())) {
							if (isSourceAttachmentEqual(root, entry)) {
								// use package fragment root
								return new PackageFragmentRootSourceLocation(root);
							}							
						}
					}
				}
			} catch (JavaModelException e) {
				LaunchingPlugin.log(e);
			}		
		}		
		return null;
	}
	
}
