package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.plugins.PluginClassLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationUtils;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Locates source for a Java debug session by searching
 * a configurable set of source locations.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 *
 * @see org.eclipse.debug.core.model.ISourceLocator
 * @since 2.0
 */
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
	 */
	public JavaSourceLocator(IJavaProject[] projects, boolean includeRequired) throws JavaModelException {
		
		ArrayList requiredProjects = new ArrayList();
		for (int i= 0; i < projects.length; i++) {
			if (includeRequired) {
				collectRequiredProjects(projects[i], requiredProjects);
			} else {
				if (!requiredProjects.contains(projects[i])) {
					requiredProjects.add(projects[i]);
				}
			}
		}
		IJavaSourceLocation[] locations = new IJavaSourceLocation[requiredProjects.size()];
		for (int i = 0; i < requiredProjects.size(); i++) {
			locations[i] = new JavaProjectSourceLocation((IJavaProject)requiredProjects.get(i));
		}	
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
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		if (stackFrame instanceof IJavaStackFrame) {
			IJavaStackFrame frame = (IJavaStackFrame)stackFrame;
			String name = null;
			try {
				String sourceName = frame.getSourceName();
				if (sourceName == null) {
					// no debug attributes, guess at source name
					name = frame.getDeclaringTypeName();
				} else {
					// build source name from debug attributes using
					// the source file name and the package of the declaring
					// type
					
					// @see bug# 12966 - remove absolute path prefix
					int index = sourceName.lastIndexOf(File.pathSeparatorChar);
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
				IJavaSourceLocation[] locations = getSourceLocations();
				for (int i = 0; i < locations.length; i++) {
					Object sourceElement = locations[i].findSourceElement(name);
					if (sourceElement != null) {
						return sourceElement;
					}
				}
			} catch (CoreException e) {
				// if the thread has since resumed, return null
				if (e.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					LaunchingPlugin.log(e);
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds all projects required by <code>proj</code> to the list
	 * <code>res</code>
	 * 
	 * @param proj the project for which to compute required
	 *  projects
	 * @param res the list to add all required projects too
	 */
	protected static void collectRequiredProjects(IJavaProject proj, ArrayList res) throws JavaModelException {
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
	 * If the project uses an alternate runtime JRE than it has
	 * been built with, the alternate JRE source zip is added
	 * as the first source location (if this source zip exists).
	 * 
	 * @param project Java project
	 * @return a collection of source locations for all required
	 *  projects
	 * @exception CoreException if an exception occurs reading
	 *  the classpath of the given or any required project
	 */
	public static IJavaSourceLocation[] getDefaultSourceLocations(IJavaProject project) throws CoreException {
		return getSourceLocations(JavaRuntime.computeUnresolvedRuntimeClasspath(project));
	}
	
	/**
	 * @see IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = new DocumentImpl();
		Element node = doc.createElement("javaSourceLocator"); //$NON-NLS-1$
		
		IJavaSourceLocation[] locations = getSourceLocations();
		for (int i = 0; i < locations.length; i++) {
			Element child = doc.createElement("javaSourceLocation"); //$NON-NLS-1$
			child.setAttribute("class", locations[i].getClass().getName()); //$NON-NLS-1$
			child.setAttribute("memento", locations[i].getMemento()); //$NON-NLS-1$
			node.appendChild(child);
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
			abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_create_memento_for_Java_source_locator._4"), e); //$NON-NLS-1$
		}
		return writer.toString();
	}

	/**
	 * @see IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedSourceLookupPath(configuration);
		IRuntimeClasspathEntry[] resolved = JavaRuntime.resolveSourceLookupPath(entries, configuration);
		setSourceLocations(getSourceLocations(resolved));
	}

	/**
	 * @see IPersistableSourceLocator#initiatlizeFromMemento(String)
	 */
	public void initiatlizeFromMemento(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			if (!root.getNodeName().equalsIgnoreCase("javaSourceLocator")) {  //$NON-NLS-1$
				abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_Java_source_locator_-_invalid_format._6"), null); //$NON-NLS-1$
			}
	
			List sourceLocations = new ArrayList();
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
							abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_Java_source_locator_-_invalid_format._10"), null); //$NON-NLS-1$
						}
						Class clazz  = null;
						try {
							clazz = classLoader.loadClass(className);
						} catch (ClassNotFoundException e) {
							abort(MessageFormat.format(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_source_location_-_class_not_found__{0}_11"), new String[] {className}), e); //$NON-NLS-1$
						}
						
						IJavaSourceLocation location = null;
						try {
							location = (IJavaSourceLocation)clazz.newInstance();
						} catch (IllegalAccessException e) {
							abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_source_location._12"), e); //$NON-NLS-1$
						} catch (InstantiationException e) {
							abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_source_location._13"), e); //$NON-NLS-1$
						}
						location.initializeFrom(data);
						sourceLocations.add(location);
					} else {
						abort(LaunchingMessages.getString("JavaSourceLocator.Unable_to_restore_Java_source_locator_-_invalid_format._14"), null); //$NON-NLS-1$
					}
				}
			}
			setSourceLocations((IJavaSourceLocation[])sourceLocations.toArray(new IJavaSourceLocation[sourceLocations.size()]));
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.getString("JavaSourceLocator.Exception_occurred_initializing_source_locator._15"), ex); //$NON-NLS-1$
	}
	
	/**
	 * Returns source locations that are associted with the given runtime classpath
	 * entries.
	 */
	private static IJavaSourceLocation[] getSourceLocations(IRuntimeClasspathEntry[] entries) {
		List locations = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			IJavaSourceLocation location = null;
			switch (entry.getType()) {
				case IRuntimeClasspathEntry.PROJECT:
					IProject project = (IProject)entry.getResource();
					if (project.exists() && project.isOpen()) {
						location = new JavaProjectSourceLocation(JavaCore.create(project));
					}
					break;
				case IRuntimeClasspathEntry.ARCHIVE:
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
								try {
									location = new ArchiveSourceLocation(path, entry.getSourceAttachmentRootLocation());
								} catch (IOException e) {
									LaunchingPlugin.log(e);
								}
							}
						}
					}
					break;
				case IRuntimeClasspathEntry.VARIABLE:
					try {
						String source = entry.getSourceAttachmentLocation();
						if (source != null) {
							location = new ArchiveSourceLocation(source, entry.getSourceAttachmentRootLocation());
						}
					} catch (IOException e) {
						LaunchingPlugin.log(e);
					}
					break;
				case IRuntimeClasspathEntry.CONTAINER:
					throw new IllegalArgumentException(LaunchingMessages.getString("JavaSourceLocator.Illegal_to_have_a_container_resolved_to_a_container_1")); //$NON-NLS-1$
			}
			if (location != null) {
				locations.add(location);
			}
		}
		return (IJavaSourceLocation[])locations.toArray(new IJavaSourceLocation[locations.size()]);		
	}
	
	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	/**
	 * Throws an internal error exception
	 */
	private void abort(String message, Throwable e)	throws CoreException {
		IStatus s = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message, e);
		throw new CoreException(s);		
	}	
}