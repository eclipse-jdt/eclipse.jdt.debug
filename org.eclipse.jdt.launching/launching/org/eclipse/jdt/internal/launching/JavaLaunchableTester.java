/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

/**
 * Generalized property tester class to determine enablement of context launching menu artifacts
 * 
 * @since 3.2
 */
public class JavaLaunchableTester extends PropertyTester {

	/**
	 * name for the "has main" property
	 */
	private static final String PROPERTY_HAS_MAIN = "hasMain"; //$NON-NLS-1$
	
	/**
	 * name for the "has method" property
	 */
	private static final String PROPERTY_HAS_METHOD = "hasMethod"; //$NON-NLS-1$	
	
	/**
	 * name for the "has method with annotation" property
	 */
	private static final String PROPERTY_HAS_METHOD_WITH_ANNOTATION = "hasMethodWithAnnotation"; //$NON-NLS-1$	

	/**
	 * name for the "has type with annotation" property
	 */
	private static final String PROPERTY_HAS_TYPE_WITH_ANNOTATION = "hasTypeWithAnnotation"; //$NON-NLS-1$	
	
	/**
	 * name for the "has super type with annotation" property
	 * @since 3.3
	 */
	private static final String PROPERTY_HAS_SUPER_TYPE_WITH_ANNOTATON = "hasSuperTypeWithAnnotation"; //$NON-NLS-1$
	
	/**
	 * name for the "has parent method with annotation" property
	 * @since 3.3
	 */
	private static final String PROPERTY_HAS_PARENT_METHOD_WITH_ANNOTATION = "hasParentMethodWithAnnotation"; //$NON-NLS-1$
	
	/**
	 * name for the "extends class" property
	 */
	private static final String PROPERTY_EXTENDS_CLASS = "extendsClass"; //$NON-NLS-1$
	
	/**
	 * "is container" property
	 */
	private static final String PROPERTY_IS_CONTAINER = "isContainer"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_PROJECT_NATURE property
	 */
	private static final String PROPERTY_PROJECT_NATURE = "hasProjectNature"; //$NON-NLS-1$
	
	/**
	 * name for the "extends interface" property
	 */
	private static final String PROPERTY_EXTENDS_INTERFACE = "extendsInterface"; //$NON-NLS-1$
	
	/**
	 * name for the PROPERTY_HAS_SWT_ON_PATH property
	 */
	private static final String PROPERTY_BUILDPATH_REFERENCE = "buildpathReference"; //$NON-NLS-1$
	
    /**
     * Map of modifier text to corresponding flag (Integer)
     */
    private static Map fgModifiers = new HashMap();
    
    private static final int FLAGS_MASK= Flags.AccPublic | Flags.AccProtected | Flags.AccPrivate | Flags.AccStatic
    	| Flags.AccFinal | Flags.AccSynchronized | Flags.AccAbstract | Flags.AccNative;
    
    
    static {
        fgModifiers.put("public",       new Integer(Flags.AccPublic)); //$NON-NLS-1$
        fgModifiers.put("protected",    new Integer(Flags.AccProtected)); //$NON-NLS-1$
        fgModifiers.put("private",      new Integer(Flags.AccPrivate)); //$NON-NLS-1$
        fgModifiers.put("static",       new Integer(Flags.AccStatic)); //$NON-NLS-1$
        fgModifiers.put("final",        new Integer(Flags.AccFinal)); //$NON-NLS-1$
        fgModifiers.put("synchronized", new Integer(Flags.AccSynchronized)); //$NON-NLS-1$
        fgModifiers.put("abstract",     new Integer(Flags.AccAbstract)); //$NON-NLS-1$
        fgModifiers.put("native",       new Integer(Flags.AccNative)); //$NON-NLS-1$
    }
    	
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
	 * Determines is the java element contains a main method.
	 * 
	 * @param element the element to check for the method 
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasMain(IJavaElement element) {
		try {
            IType type = getType(element);
			if (type != null && type.exists()) {
				IMethod[] methods = type.getMethods();
				for (int i= 0; i < methods.length; i++) {
					if(methods[i].isMainMethod()) {
							return true;
					}
				}
			}
		}
		catch (JavaModelException e) {}
		return false;
	}
	
	/**
	 * Determines is the java element contains a specific method.
     * <p>
     * The syntax for the property tester is of the form: methodname,
     * signature, modifiers.
	 * </p>
     * <ol>
     * <li>methodname - case sensitive method name, required. For example,
     *  <code>toString</code>.</li>
     * <li>signature - JLS style method signature, required. For example,
     *  <code>(QString;)V</code>.</li>
     * <li>modifiers - optional space seperated list of modifiers, for
     *  example, <code>public static</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param args first arg is method name, secondary args are parameter types signatures
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasMethod(IJavaElement element, Object[] args) {
		try {
			if (args.length > 1) {
	            IType type = getType(element);
				if (type != null && type.exists()) {
					String name = (String) args[0];
					String signature = (String) args[1];
                    String[] parms = Signature.getParameterTypes(signature);
                    String returnType = Signature.getReturnType(signature);
					IMethod candidate = type.getMethod(name, parms);
					if (candidate.exists()) {
                        // check return type
                        if (candidate.getReturnType().equals(returnType)) {
                            // check modifiers
                            if (args.length > 2) {
                                String modifierText = (String) args[2];
                                String[] modifiers = modifierText.split(" "); //$NON-NLS-1$
                                int flags = 0;
                                Integer flag = null;
                                for (int j = 0; j < modifiers.length; j++) {
                                    flag = (Integer) fgModifiers.get(modifiers[j]);
                                    if (flag != null) {
                                        flags = flags | flag.intValue();
                                    }
                                }
                                if (candidate.getFlags() == flags) {
                                    return true;
                                }
                            }
                        }
					}
				}
			}
		}
		catch (JavaModelException e) {}
		return false;
	}
	
	/**
	 * Determines if the java element contains a type with a specific annotation.
     * <p>
     * The syntax for the property tester is of the form: qualified or unqualified annotation name
     * <li>qualified or unqualified annotation name, required. For example,
     *  <code>org.junit.JUnit</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param annotationName the qualified or unqualified name of the annotation to look for
	 * @return true if the type is found in the element, false otherwise
	 */
	private boolean hasTypeWithAnnotation(IJavaElement element, String annotationType) {
		try {			
			IType type= getType(element);
			if (type != null && type.exists()) {
				IBuffer buffer = null;
				IOpenable openable = type.getOpenable();
				if (openable instanceof ICompilationUnit) {
					buffer = ((ICompilationUnit) openable).getBuffer();
				} else if (openable instanceof IClassFile) {
					buffer = ((IClassFile) openable).getBuffer();
				}
				if (buffer != null) {
					ISourceRange sourceRange = type.getSourceRange();
					ISourceRange nameRange = type.getNameRange();
					if (sourceRange != null && nameRange != null) {
						IScanner scanner= ToolFactory.createScanner(false, false, true, false);
						scanner.setSource(buffer.getCharacters());
						scanner.resetTo(sourceRange.getOffset(), nameRange.getOffset());
						if (findAnnotation(scanner, annotationType)) {
							return true;
						}
					}
				}
			}
		} 
		catch (JavaModelException e) {} 
		catch (InvalidInputException e) {}
		return false;
	}	

	/**
	 * Determines if the super types of the specified java element contains a type with a specific annotation.
     * <p>
     * The syntax for the property tester is of the form: qualified or unqualified annotation name
     * <li>qualified or unqualified annotation name, required. For example,
     *  <code>org.junit.JUnit</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param annotationName the qualified or unqualified name of the annotation to look for
	 * @return true if the type is found in the element, false otherwise
	 * @since 3.3
	 */
	private boolean hasSuperTypeWithAnnotation(IJavaElement element, String annotation) {
		try {
			IType type = getType(element);
			if(type != null && type.exists()) {
				IType[] stypes = type.newSupertypeHierarchy(new NullProgressMonitor()).getAllSuperclasses(type);
				for(int i = 0; i < stypes.length; i++) {
					if(hasTypeWithAnnotation(stypes[i], annotation)) {
						return true;
					}
				}
			} 
		}
		catch(JavaModelException jme) {}
		return false;
	}
	
	/**
	 * Determines is the parent java element contains a method with a specific annotation.
     * <p>
     * The syntax for the property tester is of the form: qualified or unqualified annotation name, modifiers
     * <li>qualified or unqualified annotation name, required. For example,
     *  <code>org.junit.JUnit</code>.</li>
     * <li>modifiers - optional space seperated list of modifiers, for
     *  example, <code>public static</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param annotationName the qualified or unqualified name of the annotation to look for
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasParentMethodWithAnnotation(IJavaElement element, Object[] args) {
		try {
			IType type = getType(element);
			if(type != null && type.exists()) {
				IType[] stypes = type.newSupertypeHierarchy(new NullProgressMonitor()).getAllSuperclasses(type);
				for(int i = 0; i < stypes.length; i++) {
					if(hasMethodWithAnnotation(stypes[i], args)) {
						return true;
					}
				}
			}
		}
		catch(JavaModelException jme){}
		return false;
	}
	
	/**
	 * Determines is the java element contains a method with a specific annotation.
     * <p>
     * The syntax for the property tester is of the form: qualified or unqualified annotation name, modifiers
     * <li>qualified or unqualified annotation name, required. For example,
     *  <code>org.junit.JUnit</code>.</li>
     * <li>modifiers - optional space seperated list of modifiers, for
     *  example, <code>public static</code>.</li>
     * </ol>
	 * @param element the element to check for the method 
	 * @param annotationName the qualified or unqualified name of the annotation to look for
	 * @return true if the method is found in the element, false otherwise
	 */
	private boolean hasMethodWithAnnotation(IJavaElement element, Object[] args) {
		try {
			String annotationType= (String) args[0];
            int flags = 0;
			if (args.length > 1) {
				String[] modifiers = ((String) args[1]).split(" "); //$NON-NLS-1$
				Integer flag = null;
                for (int j = 0; j < modifiers.length; j++) {
                    flag = (Integer) fgModifiers.get(modifiers[j]);
                    if (flag != null) {
                        flags = flags | flag.intValue();
                    }
                }
			} else {
				flags= -1;
			}
			IType type= getType(element);
			if(type != null && type.exists()) {
				IMethod[] methods= type.getMethods();
				if (methods.length > 0) {
					IBuffer buffer = null;
					IOpenable openable = type.getOpenable();
					if (openable instanceof ICompilationUnit) {
						buffer= ((ICompilationUnit) openable).getBuffer();
					} else if (openable instanceof IClassFile) {
						buffer = ((IClassFile) openable).getBuffer();
					}
					if (buffer != null) {
						IScanner scanner = null; // delay initialization
						ISourceRange sourceRange = null;
						ISourceRange nameRange = null;
						IMethod curr = null;
						for (int i= 0; i < methods.length; i++) {
							curr = methods[i];
							if (curr.isConstructor() || (flags != -1 && flags != (curr.getFlags() & FLAGS_MASK))) {
								continue;
							}
							sourceRange = curr.getSourceRange();
							nameRange = curr.getNameRange();
							if (sourceRange != null && nameRange != null) {
								if (scanner == null) {
									scanner= ToolFactory.createScanner(false, false, true, false);
									scanner.setSource(buffer.getCharacters());
								}
								scanner.resetTo(sourceRange.getOffset(), nameRange.getOffset());
								if (findAnnotation(scanner, annotationType)) {
									return true;
								}
							}
						}
					}
				}
			}
		} 
		catch (JavaModelException e) {} 
		catch (InvalidInputException e) {}
		return false;
	}	
	
	/**
	 * Searches for an annotation with the specified name using the given scanner
	 * @param scanner the scanner to get tokens from to search for the specified annotation name
	 * @param annotationName the name of the annotation to search for
	 * @return true if one is found, false otherwise
	 * @throws InvalidInputException
	 */
	private boolean findAnnotation(IScanner scanner, String annotationName) throws InvalidInputException {
		String simpleName = Signature.getSimpleName(annotationName);
		StringBuffer buf = new StringBuffer();
		int tok = scanner.getNextToken();
		String name = null;
		while (tok != ITerminalSymbols.TokenNameEOF) {
			if (tok == ITerminalSymbols.TokenNameAT) {
				buf.setLength(0);
				tok = readName(scanner, buf);
				name = buf.toString();
				if (name.equals(annotationName) || name.equals(simpleName) || name.endsWith('.' + simpleName)) {
					return true;
				}
			} else {
				tok = scanner.getNextToken();
			}
		}
		return false;
	}

	/**
	 * Reads a qualified name from the specified scanner
	 * @param scanner the scanner to read tokens from 
	 * @param buf the string buffer to write the found name into
	 * @return the token id of the last token read
	 * @throws InvalidInputException
	 */
	private int readName(IScanner scanner, StringBuffer buf) throws InvalidInputException {
		int tok = scanner.getNextToken();
		while (tok == ITerminalSymbols.TokenNameIdentifier) {
			buf.append(scanner.getCurrentTokenSource());
			tok = scanner.getNextToken();
			if (tok != ITerminalSymbols.TokenNameDOT) {
				return tok;
			}
			buf.append('.');
			tok = scanner.getNextToken();
		}
		return tok;
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
	private boolean hasSuperclass(IJavaElement element, String qname) {
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
		if(element != null && args != null) {
			IJavaProject project = element.getJavaProject();
			Set searched = new HashSet();
			searched.add(project);
			return hasItemsOnBuildPath(project, searched, args);
		}
		return false;
	}
	
	/**
	 * Returns if the specified project has the specified items on the build path
	 * @param project the project to examine
	 * @param searched a set of projects already searched (to avoid cycles in referenced projects
	 * @param args the buildpath items to search for
	 * @return true 
	 */
	private boolean hasItemsOnBuildPath(IJavaProject project, Set searched, Object[] args) {
		try {
			List projects = new ArrayList();
	        if(project != null && project.exists()) {
	            IClasspathEntry[] entries = project.getResolvedClasspath(true);
	            String spath = null;
	            String name = null;
	            IJavaProject javaProject = null;
	            for(int i = 0; i < entries.length; i++) {
	                spath = entries[i].getPath().toPortableString();
	                for(int j = 0; j < args.length; j++) {
	                	if(spath.lastIndexOf((String)args[j]) != -1) {
	                		return true;
	                	}
	                }
	                if (entries[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
	                	name = entries[i].getPath().lastSegment();
	                	javaProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getProject(name));
	                	if (!searched.contains(javaProject)) {
	                		projects.add(javaProject);
	                	}
	                }
	            }
	        }
	        // search referenced projects
	        Iterator iterator = projects.iterator();
	        IJavaProject jp = null;
	        while (iterator.hasNext()) {
	        	jp = (IJavaProject) iterator.next();
	        	searched.add(jp);
	        	if (hasItemsOnBuildPath(jp, searched, args)) {
	        		return true;
	        	}
	        }
		} 
		catch (JavaModelException e) {}
		return false;
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
	 * Method runs the tests defined from extension points for Run As... and Debug As... menu items.
	 * Currently this test optimisitically considers everything not a source file. In this context we 
	 * consider an optimistic approach to mean that the test will always return true.
	 * 
	 * There are many reasons for the optimistic choice some of them are outlined below.
	 * <ul>
	 * <li>Performance (in terms of time neede to display menu) cannot be preserved. To know what to allow
	 * in any one of the menus we would have to search all of the children of the container to determine what it contains
	 * and what can be launched by what.</li>
	 * <li>If inspection of children of containers were done, a user might want to choose a different launch type, even though our tests
	 * filter it out.</li>
	 * </ul>
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 * @since 3.2
	 * @return true if the specified tests pass, or the context is a container, false otherwise
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (PROPERTY_IS_CONTAINER.equals(property)) {
			if (receiver instanceof IAdaptable) {
				IResource resource = (IResource)((IAdaptable)receiver).getAdapter(IResource.class);
				if (resource != null) {
					return resource instanceof IContainer;
				}
			}
			return false;
		}
		IJavaElement element = null;
		if (receiver instanceof IAdaptable) {
			element = (IJavaElement) ((IAdaptable)receiver).getAdapter(IJavaElement.class);
			if(element != null) {
				if(!element.exists()) {
					return false;
				}
			}
		}
		if(PROPERTY_HAS_MAIN.equals(property)) {
			return hasMain(element);
		}
		if (PROPERTY_HAS_METHOD.equals(property)) {
			return hasMethod(element, args);
		}
		if (PROPERTY_HAS_METHOD_WITH_ANNOTATION.equals(property)) {
			return hasMethodWithAnnotation(element, args);
		}
		if(PROPERTY_HAS_PARENT_METHOD_WITH_ANNOTATION.equals(property)) {
			return hasParentMethodWithAnnotation(element, args);
		}
		if (PROPERTY_HAS_TYPE_WITH_ANNOTATION.equals(property)) {
			return hasTypeWithAnnotation(element, (String)args[0]);
		}
		if(PROPERTY_HAS_SUPER_TYPE_WITH_ANNOTATON.equals(property)) {
			return hasSuperTypeWithAnnotation(element, (String) args[0]);
		}
		if(PROPERTY_BUILDPATH_REFERENCE.equals(property)) {
			return hasItemOnBuildPath(element, args);
		}
		if(PROPERTY_EXTENDS_CLASS.equals(property)) {
			return hasSuperclass(element, (String)args[0]);
		}
		if(PROPERTY_PROJECT_NATURE.equals(property)) {
			return hasProjectNature(element, (String)args[0]);
		}
		if(PROPERTY_EXTENDS_INTERFACE.equals(property)) {
			return implementsInterface(element, (String)args[0]);
		}
		return false;
	}
	
}
