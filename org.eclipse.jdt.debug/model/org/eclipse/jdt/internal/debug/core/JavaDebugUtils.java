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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

import com.sun.jdi.VMDisconnectedException;

/**
 * A Utilities class.
 * 
 * @since 3.2
 */
public class JavaDebugUtils {

    /**
     * Resolves and returns a type from the Java model that corresponds to the
     * declaring type of the given stack frame, or <code>null</code> if none.
     *  
     * @param frame frame to resolve declaring type for
     * @return correponding Java model type or <code>null</code>
     * @exception CoreException if an exception occurrs during the resolution
     * @since 3.2
     */
    public static IType resolveDeclaringType(IJavaStackFrame frame) throws CoreException {
        IJavaElement javaElement = resolveJavaElement(frame, frame.getLaunch());
        if (javaElement != null) {
        	return resolveType(frame.getDeclaringTypeName(), javaElement);
        }
        return null;
    }
    
    /**
     * Resolves and returns a type from the Java model that corresponds to the
     * type of the given value, or <code>null</code> if none.
     *  
     * @param value value to resolve type for
     * @return correponding Java model type or <code>null</code>
     * @exception CoreException if an exception occurrs during the resolution
     */
    public static IType resolveType(IJavaValue value) throws CoreException {
        IJavaElement javaElement = resolveJavaElement(value, value.getLaunch());
        if (javaElement != null) {
        	return resolveType(value.getJavaType().getName(), javaElement);
        }
        return null;
    }   
    
    /**
     * Resolves and returns the type with the given name in the given context,
     * or <code>null</code> if none.
     * 
     * @param qualifiedName fully qaulified type name
     * @param context launch to resolve in
     * @return type or <code>null</code>
     * @throws CoreException
     */
    public static IType resolveType(String qualifiedName, ILaunch context) throws CoreException {
    	IJavaElement element = resolveJavaElement(generateSourceName(qualifiedName), context);
    	if (element != null ) {
    		return resolveType(qualifiedName, element);
    	}
    	return null;
    }
    
	/**
	 * Returns the source name associated with the given object, or <code>null</code>
	 * if none.
	 * 
	 * @param object an object with an <code>IJavaStackFrame</code> adapter, an IJavaValue
	 *  or an IJavaType 
	 * @return the source name associated with the given object, or <code>null</code>
	 * if none
	 * @exception CoreException if unable to retrieve the source name
	 */
	public static String getSourceName(Object object) throws CoreException {
		if (object instanceof String) {
			// assume it's a file name
			return (String)object;
		}
		IJavaStackFrame frame = null;
		if (object instanceof IAdaptable) {
			frame = (IJavaStackFrame) ((IAdaptable)object).getAdapter(IJavaStackFrame.class);
		}
		String typeName = null;
		try {
			if (frame != null) {
				if (frame != null) {
					if (frame.isObsolete()) {
						return null;
					}
					String sourceName = frame.getSourcePath();
					// TODO: this may break fix to bug 21518
					if (sourceName == null) {
						// no debug attributes, guess at source name
						typeName = frame.getDeclaringTypeName();
					} else {
						return sourceName;
					}
				}
			} else {
				if (object instanceof IJavaValue) {
					typeName = ((IJavaValue)object).getJavaType().getName();
				} else if (object instanceof IJavaType) {
					typeName = ((IJavaType)object).getName();
				}
			}
		} catch (DebugException e) {
			int code = e.getStatus().getCode();
            if (code == IJavaThread.ERR_THREAD_NOT_SUSPENDED || code == IJavaStackFrame.ERR_INVALID_STACK_FRAME ||
					e.getStatus().getException() instanceof VMDisconnectedException) {
				return null;
			}
			throw e;
		}
		if (typeName != null) {
			return generateSourceName(typeName);
		}
		return null;
	}    
    
	/**
	 * Generates and returns a source file path based on a qualified type name.
	 * For example, when <code>java.lang.String</code> is provided,
	 * the returned source name is <code>java/lang/String.java</code>.
	 * 
	 * @param qualifiedTypeName fully qualified type name that may contain inner types
	 *  denoted with <code>$</code> character
	 * @return a source file path corresponding to the type name
	 */
	private static String generateSourceName(String qualifiedTypeName) {
		int index = qualifiedTypeName.lastIndexOf('.');
		if (index < 0) {
			index = 0;
		}
		qualifiedTypeName = qualifiedTypeName.replace('.', File.separatorChar);
		index = qualifiedTypeName.indexOf('$');
		if (index >= 0) {
			qualifiedTypeName = qualifiedTypeName.substring(0, index);
		}
		if (qualifiedTypeName.length() == 0) {
			// likely a proxy class (see bug 40815)
			qualifiedTypeName = null;
		} else {
			qualifiedTypeName = qualifiedTypeName + ".java"; //$NON-NLS-1$
		}
		return qualifiedTypeName;
	}    
    
    /**
     * Resolves the type corresponding to the given name contained in the given top-level
     * Java element (class file, compilation unit, or type).
     * 
     * @param qualifiedName fully qualified type name
     * @param javaElement java element containing the type
     * @return type
     * @throws CoreException
     */
    private static IType resolveType(String qualifiedName, IJavaElement javaElement) throws CoreException {
    	IType type = null;
        String[] typeNames = getNestedTypeNames(qualifiedName);
        if (javaElement instanceof IClassFile) {
            type = ((IClassFile)javaElement).getType();
        } else if (javaElement instanceof ICompilationUnit) {
            type = ((ICompilationUnit)javaElement).getType(typeNames[0]);
        } else if (javaElement instanceof IType) {
            type = (IType)javaElement;
        }
        if (type != null) {
            for (int i = 1; i < typeNames.length; i++) {
                String innerTypeName= typeNames[i];
                try {
                    Integer.parseInt(innerTypeName);
                    return type;
                } catch (NumberFormatException e) {
                }
                type = type.getType(innerTypeName);
            }
        }
        return type;     	
    }
    
    /**
     * Returns the Java element corresponding to the given object or <code>null</code>
     * if none, in the context of the given launch.
     * 
     * @param launch provides source locator
     * @param object object to resolve Java model element for
     * @return corresponding Java element or <code>null</code>
     * @throws CoreException
     */
    private static IJavaElement resolveJavaElement(Object object, ILaunch launch) throws CoreException {
    	Object sourceElement = resolveSourceElement(object, launch);
        IJavaElement javaElement = null;
        if (sourceElement instanceof IJavaElement) {
            javaElement = (IJavaElement) sourceElement;
        } else if (sourceElement instanceof IAdaptable) {
            javaElement = (IJavaElement) ((IAdaptable)sourceElement).getAdapter(IJavaElement.class);
        }
        if (javaElement == null && sourceElement instanceof IResource) {
            javaElement= JavaCore.create((IResource)sourceElement);
        }
        if (javaElement == null) {
            return null;
        }
        if (!javaElement.exists()) {
            return null;
        }
        return javaElement;
    }  
    
    /**
     * Returns the source element corresponding to the given object or <code>null</code>
     * if none, in the context of the given launch.
     * 
     * @param launch provides source locator
     * @param object object to resolve source element for
     * @return corresponding source element or <code>null</code>
     * @throws CoreException
     */
    public static Object resolveSourceElement(Object object, ILaunch launch) throws CoreException {
    	ISourceLocator sourceLocator = launch.getSourceLocator();
		if (sourceLocator instanceof ISourceLookupDirector) {
			ISourceLookupDirector director = (ISourceLookupDirector) sourceLocator;
			Object[] objects = director.findSourceElements(object);
			if (objects.length > 0) {
				 return objects[0];
			}
		}
		return null;
    }      

    /**
     * Returns an array of simple type names that are
     * part of the given type's qualified name. For
     * example, if the given name is <code>x.y.A$B</code>,
     * an array with <code>["A", "B"]</code> is returned.
     * 
     * @param typeName fully qualified type name
     * @return array of nested type names
     */
    private static String[] getNestedTypeNames(String typeName) {
        int index = typeName.lastIndexOf('.');
        if (index >= 0) {
            typeName= typeName.substring(index + 1);
        }
        index = typeName.indexOf('$');
        List list = new ArrayList(1);
        while (index >= 0) {
            list.add(typeName.substring(0, index));
            typeName = typeName.substring(index + 1);
            index = typeName.indexOf('$');
        }
        list.add(typeName);
        return (String[])list.toArray(new String[list.size()]); 
    } 
}
