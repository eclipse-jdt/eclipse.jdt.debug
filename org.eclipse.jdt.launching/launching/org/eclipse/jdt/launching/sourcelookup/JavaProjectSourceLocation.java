package org.eclipse.jdt.launching.sourcelookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationUtils;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
/**
 * Locates source elements in a Java project. Returns
 * instances of <code>ICompilationUnit</code> and
 * </code>IClassFile</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @see IJavaSourceLocation
 * @since 2.0
 */
public class JavaProjectSourceLocation extends PlatformObject implements IJavaSourceLocation {

	/**
	 * The project associated with this source location
	 */
	private IJavaProject fProject;
	
	/**
	 * Constructs a new empty source location to be initialized
	 * by a memento.
	 */
	public JavaProjectSourceLocation() {
	}
	
	/**
	 * Constructs a new source location that will retrieve source
	 * elements from the given Java project.
	 * 
	 * @param project Java project
	 */
	public JavaProjectSourceLocation(IJavaProject project) {
		setJavaProject(project);
	}	
	
	/**
	 * @see IJavaSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement(String name) throws CoreException {
		if (getJavaProject() != null) {
			String pathStr= name.replace('.', '/') + ".java"; //$NON-NLS-1$
			IJavaElement jelement= getJavaProject().findElement(new Path(pathStr));
			if (jelement == null) {
				// maybe an inner type
				int dotIndex= pathStr.lastIndexOf('/');
				int dollarIndex= pathStr.indexOf('$', dotIndex + 1);
				if (dollarIndex != -1) {
					jelement= getJavaProject().findElement(new Path(pathStr.substring(0, dollarIndex) + ".java")); //$NON-NLS-1$
				}
			}
			return jelement;
		} else {
			return null;
		}
	}

	/**
	 * Sets the Java project in which source elements will
	 * be searched for.
	 * 
	 * @param project Java project
	 */
	private void setJavaProject(IJavaProject project) {
		fProject = project;
	}
	
	/**
	 * Returns the Java project associated with this source
	 * location.
	 * 
	 * @return Java project
	 */
	public IJavaProject getJavaProject() {
		return fProject;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {		
		return object instanceof JavaProjectSourceLocation &&
			 getJavaProject().equals(((JavaProjectSourceLocation)object).getJavaProject());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getJavaProject().hashCode();
	}		
	/**
	 * @see IJavaSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document doc = new DocumentImpl();
		Element node = doc.createElement("javaProjectSourceLocation"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("name", getJavaProject().getElementName()); //$NON-NLS-1$
		
		try {
			return JavaLaunchConfigurationUtils.serializeDocument(doc);
		} catch (IOException e) {
			abort(MessageFormat.format(LaunchingMessages.getString("JavaProjectSourceLocation.Unable_to_create_memento_for_Java_project_source_location_{0}_1"), new String[] {getJavaProject().getElementName()}), e); //$NON-NLS-1$
		}
		// execution will not reach here
		return null;
	}

	/**
	 * @see IJavaSourceLocation#initializeFrom(String)
	 */
	public void initializeFrom(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
												
			String name = root.getAttribute("name"); //$NON-NLS-1$
			if (isEmpty(name)) {
				abort(LaunchingMessages.getString("JavaProjectSourceLocation.Unable_to_initialize_source_location_-_missing_project_name_3"), null); //$NON-NLS-1$
			} else {
				IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				setJavaProject(JavaCore.create(proj));
			}
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(LaunchingMessages.getString("JavaProjectSourceLocation.Exception_occurred_initializing_source_location._4"), ex); //$NON-NLS-1$
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
