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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
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
import org.eclipse.jdt.core.Signature;

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
	 * name for the HAS_LIBRARY_REF property
	 */
	private static final String PROPERTY_HAS_LIBRARY_REF = "hasLibraryRef"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_HAS_ITEM_ON_BUILD_PATH property
	 */
	private static final String PROPERTY_HAS_ITEM_ON_BUILD_PATH = "hasItemOnBuildPath"; //$NON-NLS-1$

	/**
	 * name for the PROPERTY_MATCHES_EXTENSION property
	 */
	private static final String PROPERTY_MATCHES_EXTENSION = "matchesJavaFileExtension"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_MATCHES_CONTENT
	 */
	private static final String PROPERTY_MATCHES_CONTENT = "matchesContentType"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_PROJECT_NATURE property
	 */
	private static final String PROPERTY_PROJECT_NATURE = "hasProjectNature"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_IMPLEMENT_INTERFACE property
	 */
	private static final String PROPERTY_IMPLEMENTS_INTERFACE = "implementsInterface"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_HAS_SWT_ON_BUILD_PATH property
	 */
	private static final String PROPERTY_HAS_SWT_ON_BUILD_PATH = "hasSwtOnBuildPath"; //$NON-NLS-1$
	
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
				}//end if
			}//end if
		}//end if
		if(element instanceof IPackageFragmentRoot || element instanceof IPackageFragment || element instanceof IJavaProject) {
			return true;
		}//end if
		if(PROPERTY_HAS_LIBRARY_REF.equals(property)) {
			return hasLibraryRef(element, (String)expectedValue);
		}//end if
		else if(PROPERTY_HAS_METHOD.equals(property)) {
			//check to ensure arguments are correct length
			if(args.length != 2) {
				return false;
			}//end if
			return hasMethod(element, (String)args[0], (String)args[1]);
		}//end if
		else if(PROPERTY_HAS_SUPERCLASS.equals(property)) {
			return hasSuperClass(element, (String)expectedValue);
		}//end if
		else if(PROPERTY_HAS_ITEM_ON_BUILD_PATH.equals(property)) {
			return hasItemOnBuildPath(element, (String)expectedValue);
		}//end if
		else if(PROPERTY_HAS_SWT_ON_BUILD_PATH.equals(property)) {
			return hasSwtOnBuildPath(element);
		}//end if
		else if(PROPERTY_MATCHES_EXTENSION.equals(property)) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if(resource != null) {
				return matchesJavaFileExtension(resource);
			}//end if
		}//end if
		else if(PROPERTY_MATCHES_CONTENT.equals(property)) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if(resource != null) {
				return matchesContentType(resource, (String)expectedValue);
			}//end if
		}//end if
		else if(PROPERTY_PROJECT_NATURE.equals(property)) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if(resource != null) {
				return hasProjectNature(resource, (String)expectedValue);
			}//end if
		}//end if
		else if(PROPERTY_IMPLEMENTS_INTERFACE.equals(property)) {
			return implementsInterface(element, (String)expectedValue);
		}//end if
		return false;
	}//end test
	
	/**
	 * Determines is the java elements contains the specified method, described with its name and signature
	 * @param element the element to check for the method 
	 * @param name the name of the method
	 * @param signature the signature of the method
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasMethod(IJavaElement element, String name, String signature) {
		try {
            IType type = getType(element);
			if (type != null && type.exists()) {
				IMethod[] methods = type.getMethods();
				for (int i= 0; i < methods.length; i++) {
					if(name.equals(methods[i].getElementName()) && signature.equals(methods[i].getSignature())) {
						return true;
					}//end if
				}//end for
			}//end if
		}//end try 
		catch (JavaModelException e) {}
		return false;
	}//end hasMethod
	
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
				IType[] stypes = type.newSupertypeHierarchy(new NullProgressMonitor()).getAllSupertypes(type);
				for(int i = 0; i < stypes.length; i++) {
					if(stypes[i].getFullyQualifiedName().equals(qname) || stypes[i].getElementName().equals(qname)) {
						return true;
					}//end if
				}//end for
			}//end if
		}//end try
		catch(JavaModelException e) {}
		return false;
	}//end isSubclass
	
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
					}//end if
				}//end for
			}//end if
		}//end try
		catch(JavaModelException e) {}
		return false;
	}//end implementsInterface
	
	/**
	 * Determines if the element has pname as a reference (import)
	 * @param element the element to search for the reference
	 * @param pname the fully qualified package name
	 * @return true if the package is referenced, false otherwise
	 */
	private boolean hasLibraryRef(IJavaElement element, String pname) {
		try {
			IType type = getType(element);
			if(type != null) {
				ICompilationUnit cunit = type.getCompilationUnit();
				if(cunit != null) {
					return (cunit.getImport(pname) != null);
				}//end if
			}//end if
		}//end try
		catch(JavaModelException e) {}
		return false;
	}//end hasLibraryRef

	/**
	 * determines if the SWT libraries are included in the build path
	 * @param element the element whose project is checked to determine if the the SWT library is present
	 * @return true if any of the SWT libraries are present, false otherwise
	 */
	private boolean hasSwtOnBuildPath(IJavaElement element) {
	    try {
	        if (element != null) {
	            IJavaProject project = element.getJavaProject();
	            if(project != null) {
	                IClasspathEntry[] entries = project.getResolvedClasspath(true);
	                for(int i = 0; i < entries.length; i++) {
	                    IPath path = entries[i].getPath();
	                    String spath = path.toPortableString();
	                    if((spath.lastIndexOf("swt.jar") != -1) || (spath.lastIndexOf("org.eclipse.swt") != -1)){ //$NON-NLS-1$ //$NON-NLS-2$
	                        return true;
	                    }//end if
	                }//end for
	            }//end if
	        }
	    }//end try
	    catch (JavaModelException e) {}
	    return false;
	}//end hasSwtOnBuildPath
	
	/**
	 * determines if the pattern is included on the build path
	 * @param element the element whose project is checked to determine if the pattern is present on its build path
	 * @param pattern the pattern to search path enetries for
	 * @return true if the pattern is present, false otherwise
	 */
	private boolean hasItemOnBuildPath(IJavaElement element, String pattern) {
	    try {
	        if (element != null) {
	            IJavaProject project = element.getJavaProject();
	            if(project != null) {
	                IClasspathEntry[] entries = project.getResolvedClasspath(true);
	                for(int i = 0; i < entries.length; i++) {
	                    IPath path = entries[i].getPath();
	                    if(path.toPortableString().lastIndexOf(pattern) != -1){
	                        return true;
	                    }//end if
	                }//end for
	            }//end if
	        }
	    }//end try
	    catch (JavaModelException e) {}
	    return false;
	}
	
	/**
	 * matches the file extension to see if the resource is a java class or source file
	 * @param resource the resource 
	 * @return true if the resource has a java associated file extension, false otherwise
	 */
	private boolean matchesJavaFileExtension(IResource resource) {
		String extension = resource.getFileExtension();
		if(extension != null) {
			return extension.equals("java") || extension.equals("class");  //$NON-NLS-1$//$NON-NLS-2$
		}//end if
        return false;
	}//end matchesJavaFileExtension
	
	/**
     * Returns whether or not the given file's content type matches the
     * specified content type.
     * 
     * Content types are looked up in the content type registry.
     * 
     * @return whether or not the given resource has the given content type
     */
    private boolean matchesContentType(IResource resource, String ctype) {
        if ((resource instanceof IFile) & resource.exists()) {
	        IFile file = (IFile) resource;
	        IContentDescription description;
	        try {
	            description = file.getContentDescription();
	        }//end try 
	        catch (CoreException e) {return false;}
	        if (description != null) {
	            IContentType type = description.getContentType();
	            return type != null && ctype.equals(type.getId());
	        }//end if
        }//end if
        return false;
    }//end matchesContentType
    
    /**
     * determines if the project selected has the specified nature
     * @param resource the resource to get the project for
     * @param ntype the specified nature type
     * @return true if the specified nature matches the project, false otherwise
     */
    private boolean hasProjectNature(IResource resource, String ntype) {
    	try {
            IProject proj = resource.getProject();
            return proj.isAccessible() && proj.hasNature(ntype);
        }//end try 
    	catch (CoreException e) {return false;}
    }//end projectNature
    
	/**
	 * gets the type of the IJavaElement
	 * @param element the element to inspect
	 * @return the type
	 * @throws JavaModelException
	 */
	private IType getType(IJavaElement element) throws JavaModelException {
        IType type = null;
        if (element instanceof ICompilationUnit) {
            ICompilationUnit cu = (ICompilationUnit) element;
            type= cu.getType(Signature.getQualifier(cu.getElementName()));
        }//end if 
        else if (element instanceof IClassFile) {
                type = ((IClassFile)element).getType();
        }//end if 
        else if (element instanceof IType) {
            type = (IType) element;
        }//end if 
        else if (element instanceof IMember) {
            type = ((IMember)element).getDeclaringType();
        }//end if
        return type;
    }//end getType
	
}//end class
