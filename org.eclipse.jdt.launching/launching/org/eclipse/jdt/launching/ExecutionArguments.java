/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.internal.launching.*;


/**
 * The execution arguments for running a Java VM. The execution arguments are
 * separated into two parts: arguments to the VM itself, and arguments to the Java 
 * main program.
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public class ExecutionArguments {
	private String fVMArgs;
	private String fProgramArgs;
	
	private static final String PROPERTY= "launcher.executionArguments"; //$NON-NLS-1$
		
	/**
	 * Creates a new execution arguments object.
	 *
	 * @param vmArgs command line argument string passed to the VM
	 * @param programArgs command line argument string passed to the program
	 */
	public ExecutionArguments(String vmArgs, String programArgs) {
		if (vmArgs == null || programArgs == null)
			throw new IllegalArgumentException();
		fVMArgs= vmArgs;
		fProgramArgs= programArgs;
	}
	
	/**
	 * Returns the execution arguments for a given type.
	 * <p>
	 * [Issue: Rationalize the exceptions thrown by this method.]
	 * </p>
	 *
	 * @param type an existing Java class or interface
	 * @return the execution arguments stored for this type, or <code>null</code> if none 
	 * @throws JavaModelException if an error occurred while finding a resource to read
	 *    the property
	 */
	public static ExecutionArguments getArguments(IType type) throws JavaModelException {
		IJavaElement relativeRoot= getPropertyHolder(type);
		String path= getRelativePath(relativeRoot, type);
		try {
			Map table= getAllForType(relativeRoot, type);
			return (ExecutionArguments)table.get(path);
		} catch (CoreException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.CORE_EXCEPTION);
		}
	}

	/**
	 * Sets the execution arguments for the given type.
	 * <p>
	 * [Issue: Rationalize the exceptions thrown by this method.]
	 * </p>
	 *
	 * @param type an existing Java class or interface
	 * @param args the execution arguments for this type, or <code>null</code> to clear
	 * @throws JavaModelException if an error occurred while finding a resource to read
	 *    the property
	 */
	public static void setArguments(IType type, ExecutionArguments args) throws JavaModelException {
		IJavaElement relativeRoot= getPropertyHolder(type);
		String path= getRelativePath(relativeRoot, type);
		Map table;
		try {
			table= getAllForType(relativeRoot, type);
		} catch (CoreException e) {
			table= new HashMap(1);
		}
		table.put(path, args);
		try {
			putAllForType(relativeRoot, type, table);
		} catch (CoreException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.CORE_EXCEPTION);
		}
	}
	

	/**
	 * Returns the VM arguments.
	 *
	 * @return the VM arguments
	 */
	public String getVMArguments() {
		return fVMArgs;
	}
	
	/**
	 * Returns the program arguments.
	 *
	 * @return the program arguments
	 */
	public String getProgramArguments() {
		return fProgramArgs;
	}
	
	/**
	 * Returns the VM arguments as array
	 *
	 * @return the VM arguments
	 */
	public String[] getVMArgumentsArray() {
		return parseArguments(fVMArgs);
	}
	
	/**
	 * Returns the program arguments as array
	 *
	 * @return the program arguments
	 */
	public String[] getProgramArgumentsArray() {
		return parseArguments(fProgramArgs);
	}	
	

	private static Map getAllForType(IJavaElement propertyHolder, IType type) throws CoreException, JavaModelException  {
		String property= getPropertyFrom(propertyHolder, type);
		if (property == null)
			return new Hashtable(0);
		return convertToHashtable(property);
	}
	
	private static IJavaElement getPropertyHolder(IType type) throws JavaModelException {
		IJavaElement element= null;
		ICompilationUnit cu= type.getCompilationUnit();
		if (cu != null) {
			if (cu.isWorkingCopy()) {
				element= cu.getOriginalElement();
			} else {
				element= cu;
			}
		} else {
			element= type.getClassFile();
		}
		while (element != null && element.getCorrespondingResource() == null) {
			element= element.getParent();
		}
		return element;
	}
	
	private static String getRelativePath(IJavaElement ancestor, IJavaElement element) {
		if (element.equals(ancestor))
			return ""; //$NON-NLS-1$
		if (ancestor instanceof ICompilationUnit) {
			// we know we can only have one runnable type in the CU
			return ""; //$NON-NLS-1$
		}
		return getRelativePath(ancestor, element.getParent())+"|"+element.getElementName(); //$NON-NLS-1$
	}
	
	private static String getPropertyFrom(IJavaElement xmlHolder, IType type) throws CoreException, JavaModelException  {
		IResource res= xmlHolder.getCorrespondingResource();
		return res.getPersistentProperty(new QualifiedName("org.eclipse.jdt.ui", PROPERTY)); //$NON-NLS-1$
	}
	

	private static Map convertToHashtable(String contents) throws JavaModelException {
		StringTokenizer tokens= new StringTokenizer(contents, "\n\t", false); //$NON-NLS-1$
		Map ht= new HashMap(tokens.countTokens() / 3);
		if (tokens.countTokens() % 3 != 0)
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
		while (tokens.hasMoreTokens()) {
			String typeName= tokens.nextToken();
			typeName= typeName.substring(1, typeName.length()-1);
			String vmArgs= tokens.nextToken();
			vmArgs= vmArgs.substring(1, vmArgs.length()-1);
			String programArgs= tokens.nextToken();
			programArgs= programArgs.substring(1, programArgs.length()-1);
				
			ht.put(typeName, new ExecutionArguments(vmArgs, programArgs));
		}
		return ht;
	}	

	private static String convertToString(Map ht) {
		StringBuffer buf= new StringBuffer();
		Iterator types= ht.keySet().iterator();
		while (types.hasNext()) {
			String type= (String)types.next();
			buf.append('/');
			buf.append(type);
			buf.append("/\t"); //$NON-NLS-1$
			
			ExecutionArguments arguments= (ExecutionArguments)ht.get(type);

			buf.append('/');
			buf.append(arguments.getVMArguments());
			buf.append("/\t"); //$NON-NLS-1$
			
			buf.append('/');
			buf.append(arguments.getProgramArguments());
			buf.append("/\n"); //$NON-NLS-1$
		}
		return buf.toString();
	}
	
	private static void putAllForType(IJavaElement xmlHolder, IType type, Map ht) throws CoreException, JavaModelException  {
		String xmlString= convertToString(ht);
		IResource res= xmlHolder.getCorrespondingResource();
		// we know that if the xmlHolder is != null, it will have a corresponding resource
		// it has been selected because of this.
		res.setPersistentProperty(new QualifiedName("org.eclipse.jdt.ui", PROPERTY), xmlString); //$NON-NLS-1$
	}
	
	private static class ArgumentParser {
		private String fArgs;
		private int fIndex= 0;
		private int ch= -1;
		
		public ArgumentParser(String args) {
			fArgs= args;
		}
		
		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}
		
		public String[] parseArguments() {
			ArrayList v= new ArrayList();
			
			ch= getNext();
			while (ch > 0) {
				while (Character.isWhitespace((char)ch))
					ch= getNext();	
				
				if (ch == '"') {
					v.add(parseString());
				} else {
					v.add(parseToken());
				}
			}
	
			String[] result= new String[v.size()];
			v.toArray(result);
			return result;
		}
		
		public String parseString() {
			StringBuffer buf= new StringBuffer();
			buf.append((char)ch);
			ch= getNext();
			while (ch > 0 && ch != '"') {
				buf.append((char)ch);
				ch= getNext();
			}
			if (ch > 0)
				buf.append((char)ch);
			ch= getNext();
				
			return buf.toString();
		}
		
		public String parseToken() {
			StringBuffer buf= new StringBuffer();
			
			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '"')
					buf.append(parseString());
				else {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			return buf.toString();
		}
	}
	
	private static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser= new ArgumentParser(args);
		String[] res= parser.parseArguments();
		
		return res;
	}
	
}