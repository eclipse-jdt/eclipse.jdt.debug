/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Generalized property tester class to determine enablement of context launching menu artifacts
 * 
 * @since 3.2
 */
public class JavaLaunchableTester extends PropertyTester {

	/**
	 * name for the HAS_METHOD property
	 */
	private static final String PROPERTY_HAS_METHOD = "hasMethod"; //$NON-NLS-1$
	/**
	 * name for the IS_SUBCLASS property
	 */
	private static final String PROPERTY_HAS_SUPERCLASS = "hasSuperClass"; //$NON-NLS-1$

	/**
	 * name for the PROPERTY_MATCHES_EXTENSION property
	 */
	private static final String PROPERTY_MATCHES_EXTENSION = "matchesJavaFileExtension"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_PROJECT_NATURE property
	 */
	private static final String PROPERTY_PROJECT_NATURE = "hasProjectNature"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_IMPLEMENT_INTERFACE property
	 */
	private static final String PROPERTY_IMPLEMENTS_INTERFACE = "implementsInterface"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_HAS_SWT_ON_PATH property
	 */
	private static final String PROPERTY_HAS_ITEM_ON_PATH = "hasItemOnBuildPath"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_HAS_ALL_ITEMS_ON_PATH property
	 */
	private static final String PROPERTY_HAS_ALL_ITEMS_ON_PATH = "hasAllItemsOnBuildPath"; //$NON-NLS-1$

	
	/**
	 * gets the type of the IJavaElement
	 * @param element the element to inspect
	 * @return the type
	 * @throws JavaModelException
	 */
	private IType getType(IJavaElement element) throws JavaModelException {
        IType type = null;
        if (element instanceof ICompilationUnit) {
            type= ((ICompilationUnit) element).findPrimaryType();
        }
        else if (element instanceof IClassFile) {
            type = ((IClassFile)element).getType();
        }
        else if (element instanceof IType) {
            type = (IType) element;
        }
        else if (element instanceof IMember) {
            type = ((IMember)element).getDeclaringType();
        }
        return type;
    }
	
	/**
	 * Determines is the java elements contains the specified method, described with its name and signature
	 * @param element the element to check for the method 
	 * @param name the name of the method
	 * @param signature the signature of the method
	 * @param flags method modifiers or <code>null</code>
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasMethod(IJavaElement element, String name, String signature, Integer flags) {
		try {
            IType type = getType(element);
			if (type != null && type.exists()) {
				IMethod[] methods = type.getMethods();
				for (int i= 0; i < methods.length; i++) {
					if(name.equals(methods[i].getElementName()) && signature.equals(methods[i].getSignature())) {
						if (flags == null || (flags.intValue() == methods[i].getFlags())) {
							return true;
						}
					}
				}
			}
		}
		catch (JavaModelException e) {}
		return false;
	}
	
	/**
     * determines if the project selected has the specified nature
     * @param resource the resource to get the project for
     * @param ntype the specified nature type
     * @return true if the specified nature matches the project, false otherwise
     */
    private boolean hasProjectNature(IJavaElement element, String ntype) {
    	try {
	    	if(element != null) {
	    		IResource resource = element.getResource();
	    		if(resource != null) {
		            IProject proj = resource.getProject();
		            return proj.isAccessible() && proj.hasNature(ntype);
	    		}
    		}
	    	return false;
        }
    	catch (CoreException e) {return false;}
    }
	
	/**
	 * Determines if the element has qname as a parent class
	 * @param element the element to check for the parent class definition
	 * @param qname the fully qualified name of the (potential) parent class
	 * @return true if qname is a parent class, false otherwise
	 */
	private boolean hasSuperClass(IJavaElement element, String qname) {
		try {
			IType type = getType(element);
			if(type != null) {
				IType[] stypes = type.newSupertypeHierarchy(new NullProgressMonitor()).getAllSuperclasses(type);
				for(int i = 0; i < stypes.length; i++) {
					if(stypes[i].getFullyQualifiedName().equals(qname) || stypes[i].getElementName().equals(qname)) {
						return true;
					}
				}
			} 
		}
		catch(JavaModelException e) {}
		return false; 
	}
	
	/**
	 * Determines if an item or list of items are found on the buildpath. 
	 * Once any one single items matches though, the method returns true, this method is intended 
	 * to be used in OR like situations, where we do not care if all of the items are on the build path, only that one
	 * of them is.
	 * 
	 * @param element the element whose build path should be checked
	 * @param args the value(s) to search for on the build path
	 * @return true if any one of the args is found on the build path
	 */
	private boolean hasItemOnBuildPath(IJavaElement element, Object[] args) {
		try {
			if(element != null && args != null) {
				IJavaProject project = element.getJavaProject();
	            if(project != null && project.exists()) {
	                IClasspathEntry[] entries = project.getResolvedClasspath(true);
	                for(int i = 0; i < entries.length; i++) {
	                    IPath path = entries[i].getPath();
	                    String spath = path.toPortableString();
	                    for(int j = 0; j < args.length; j++) {
	                    	if(spath.lastIndexOf((String)args[j]) != -1) {
	                    		return true;
	                    	}
	                    }
	                }
	            }
			}
		}
		catch(JavaModelException e) {DebugPlugin.log(e);}
		return false;
	}
	
	/**
	 * Determines if all item from the list of items are found on the buildpath. 
	 * Once any one single items matches FALSE, the method returns false, this method is intended 
	 * to be used in AND like situations, where we want to ensure all of the items are on the build path.
	 * 
	 * @param element the element whose build path should be checked
	 * @param args the value(s) to search for on the build path
	 * @return true if all of the args are found on the build path
	 */
	private boolean hasAllItemsOnBuildPath(IJavaElement element, Object[] args) {
		boolean value = true; //optimistic
		try {
			if(element != null && args != null) {
				IJavaProject project = element.getJavaProject();
	            if(project != null && project.exists()) {
	                IClasspathEntry[] entries = project.getResolvedClasspath(true);
	                IPath path = null;
	                String spath = null;
	                for(int i = 0; i < args.length; i++) {
	                	boolean val = false;
	                    for(int j = 0; j < entries.length; j++) {
	                    	path = entries[j].getPath();
		                    spath = path.toPortableString();
	                    	if(spath.lastIndexOf((String)args[i]) != -1) {
	                    		val = true;
	                    	}
	                    }
	                    value &= val;
	                }
	            }
	            else {
	            	value = false;
	            }
			}
			else {
				value = false;
			}
		}
		catch(JavaModelException e) {DebugPlugin.log(e);}
		return value;
	}
	
	/**
	 * determines if the element implements a given interface
	 * @param element the element to check for the interface
	 * @param qname the fully qualified name of the interface to check for
	 * @return true if the element does implement the interface, false otherwise
	 */
	private boolean implementsInterface(IJavaElement element, String qname) {
		try {
			IType type = getType(element);
			if(type != null) {
				IType[] itypes = type.newSupertypeHierarchy(new NullProgressMonitor()).getAllInterfaces();
				for(int i = 0; i < itypes.length; i++) {
					if(itypes[i].getFullyQualifiedName().equals(qname)) {
						return true;
					}
				} 
			}
		}
		catch(JavaModelException e) {}
		return false;
	}

    /**
	 * matches the file extension to see if the resource is a java class or source file
	 * @param resource the resource 
	 * @return true if the resource has a java associated file extension, false otherwise
	 */
	private boolean matchesJavaFileExtension(IJavaElement element) {
		if(element != null) {
			IResource resource = element.getResource();
			if(resource != null) {
				String extension = resource.getFileExtension();
				if(extension != null) {
					return extension.equals("java") || extension.equals("class");  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}
        return false;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IJavaElement element = null;
		if (receiver instanceof IAdaptable) {
			element = (IJavaElement) ((IAdaptable)receiver).getAdapter(IJavaElement.class);
			if(element != null) {
				if(!element.exists()) {
					return false;
				}
			}
		}
		if(element instanceof IPackageFragmentRoot || element instanceof IPackageFragment || element instanceof IJavaProject) {
			if(property.equals(PROPERTY_PROJECT_NATURE)) {
				return hasProjectNature(element, (String)expectedValue);
			}
			return true;
		}
		if(PROPERTY_MATCHES_EXTENSION.equals(property)) {
			return matchesJavaFileExtension(element);
		}
		if(PROPERTY_HAS_METHOD.equals(property)) {
			if (args.length == 3) {
				return hasMethod(element, (String)args[0], (String)args[1], (Integer)args[2]);
			}
			return false;
		}
		if(PROPERTY_HAS_ITEM_ON_PATH.equals(property)) {
			return hasItemOnBuildPath(element, args);
		}
		if(PROPERTY_HAS_ALL_ITEMS_ON_PATH.equals(property)) {
			return hasAllItemsOnBuildPath(element, args);
		}
		if(PROPERTY_HAS_SUPERCLASS.equals(property)) {
			return hasSuperClass(element, (String)expectedValue);
		}
		if(PROPERTY_PROJECT_NATURE.equals(property)) {
			return hasProjectNature(element, (String)expectedValue);
		}
		if(PROPERTY_IMPLEMENTS_INTERFACE.equals(property)) {
			return implementsInterface(element, (String)expectedValue);
		}
		return false;
	}
}//end class
