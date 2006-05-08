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
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionsEvaluationMessages;

import com.sun.jdi.InvocationException;

/**
 * Common runtime context code for class loading and cache of
 * class loader/java.lang.Class.
 * 
 * @since 3.2
 */

public abstract class AbstractRuntimeContext implements IRuntimeContext {
    
    /**
     * Cache of class loader for this runtime context
     */
    private IJavaObject fClassLoader;
    
    /**
     * Cache of java.lang.Class type
     */
    private IJavaClassType fJavaLangClass;

    /**
     * Java project context
     */
	protected IJavaProject fProject;
    
    public static final String CLASS= "java.lang.Class"; //$NON-NLS-1$
    public static final String FOR_NAME= "forName"; //$NON-NLS-1$
    public static final String FOR_NAME_SIGNATURE= "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"; //$NON-NLS-1$


    public AbstractRuntimeContext(IJavaProject project) {
    	fProject = project;
    }
    
    /**
     * Returns the class loader used to load classes for this runtime context
     * or <code>null</code> when loaded by the bootstrap loader
     * 
     * @return the class loader used to load classes for this runtime context or
     *  <code>null</code> when loaded by the bootstrap loader
     * @throws CoreException if unable to resolve a class loader
     */
    protected IJavaObject getClassLoaderObject() throws CoreException {
        if (fClassLoader == null) {
            fClassLoader = getReceivingType().getClassLoaderObject();
        }
        return fClassLoader;
    } 
    
    /**
     * Return the java.lang.Class type.
     * 
     * @return the java.lang.Class type
     * @throws CoreException if unable to retrive the type
     */
    protected IJavaClassType getJavaLangClass() throws CoreException {
        if (fJavaLangClass == null) {
            IJavaType[] types= getVM().getJavaTypes(CLASS);
            if (types == null || types.length != 1) {
                throw new CoreException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, MessageFormat.format(InstructionsEvaluationMessages.Instruction_No_type, new String[]{CLASS}), null)); 
            }
            fJavaLangClass = (IJavaClassType) types[0];
        }
        return fJavaLangClass;
    }
    
    /**
     * Invokes Class.classForName(String, boolean, ClassLoader) on the target
     * to force load the specified class.
     * 
     * @param qualifiedName name of class to load
     * @param loader the class loader to use or <code>null</code> if the bootstrap loader
     * @return the loaded class
     * @throws CoreException if loading fails
     */
    protected IJavaClassObject classForName(String qualifiedName, IJavaObject loader) throws CoreException {
    	IJavaValue loaderArg = loader;
    	if (loader == null) {
    		loaderArg = getVM().nullValue();
    	}
        IJavaValue[] args = new IJavaValue[] {getVM().newValue(qualifiedName), getVM().newValue(true), loaderArg};
        try {
            return (IJavaClassObject) getJavaLangClass().sendMessage(FOR_NAME, FOR_NAME_SIGNATURE, args, getThread());
        } catch (CoreException e) {
            if (e.getStatus().getException() instanceof InvocationException) {
                // Don't throw ClassNotFoundException
                if (((InvocationException)e.getStatus().getException()).exception().referenceType().name().equals("java.lang.ClassNotFoundException")) { //$NON-NLS-1$
                    return null;
                }
            }
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#classForName(java.lang.String)
     */
    public IJavaClassObject classForName(String name) throws CoreException {
        return classForName(name, getClassLoaderObject());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getProject()
	 */
	public IJavaProject getProject() {
		return fProject;
	}
}
