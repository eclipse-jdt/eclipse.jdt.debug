/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.w3c.dom.Document;

/**
 * This class contains a number of static helper methods useful for the 'local Java' delegate.
 */
public class JavaLaunchConfigurationUtils {
																		 		
	/**
	 * Return the <code>IType</code> referenced in the specified configuration and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	public static IType getMainType(ILaunchConfiguration configuration, IJavaProject javaProject) throws CoreException {
		String mainTypeName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
		return getMainType(mainTypeName, javaProject);
	}
	
	/**
	 * Return the <code>IType</code> referenced by the specified name and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	public static IType getMainType(String mainTypeName, IJavaProject javaProject) throws CoreException {
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort(LaunchingMessages.JavaLaunchConfigurationUtils_Main_type_not_specified_3, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		IType mainType = null;
		try {
			mainType = findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
		}
		if (mainType == null) {
			abort(LaunchingMessages.JavaLaunchConfigurationUtils_Main_type_does_not_exist_4, null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		return mainType;
	}	
	
	/**
	 * Find the specified (fully-qualified) type name in the specified java project.
	 */
	public static IType findType(IJavaProject javaProject, String mainTypeName) throws JavaModelException {
		String pathStr= mainTypeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement javaElement= javaProject.findElement(new Path(pathStr));
		if (javaElement == null) {
			return null;
		} else if (javaElement instanceof IType) {
			return (IType)javaElement;
		} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(mainTypeName);
			return ((ICompilationUnit) javaElement).getType(simpleName);
		} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) javaElement).getType();
		}
		return null; 
	}
		
	/**
	 * Throws a core exception with the given message and optional
	 * exception. The exception's status code will indicate an error.
	 * 
	 * @param message error message
	 * @param exception cause of the error, or <code>null</code>
	 * @exception CoreException with the given message and underlying
	 *  exception
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(),
		  code, message, exception));
	}

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument(Document doc) throws IOException, TransformerException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		
		TransformerFactory factory= TransformerFactory.newInstance();
		Transformer transformer= factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		DOMSource source= new DOMSource(doc);
		StreamResult outputTarget= new StreamResult(s);
		transformer.transform(source, outputTarget);
		
		return s.toString("UTF8"); //$NON-NLS-1$			
	}	
}
