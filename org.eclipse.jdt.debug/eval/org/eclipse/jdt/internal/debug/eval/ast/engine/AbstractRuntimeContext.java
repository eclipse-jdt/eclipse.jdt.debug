/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdi.internal.TypeImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionsEvaluationMessages;

import com.ibm.icu.text.MessageFormat;
import com.sun.jdi.InvocationException;

/**
 * Common runtime context code for class loading and cache of class
 * loader/java.lang.Class.
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

	public static final String CLASS = "java.lang.Class"; //$NON-NLS-1$
	public static final String FOR_NAME = "forName"; //$NON-NLS-1$
	public static final String FOR_NAME_SIGNATURE = "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"; //$NON-NLS-1$

	public AbstractRuntimeContext(IJavaProject project) {
		fProject = project;
	}

	/**
	 * Returns the class loader used to load classes for this runtime context or
	 * <code>null</code> when loaded by the bootstrap loader
	 * 
	 * @return the class loader used to load classes for this runtime context or
	 *         <code>null</code> when loaded by the bootstrap loader
	 * @throws CoreException
	 *             if unable to resolve a class loader
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
	 * @throws CoreException
	 *             if unable to retrive the type
	 */
	protected IJavaClassType getJavaLangClass() throws CoreException {
		if (fJavaLangClass == null) {
			IJavaType[] types = getVM().getJavaTypes(CLASS);
			if (types == null || types.length != 1) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JDIDebugPlugin.getUniqueIdentifier(),
								IStatus.OK,
								MessageFormat
										.format(InstructionsEvaluationMessages.Instruction_No_type,
												new Object[] { CLASS }), null));
			}
			fJavaLangClass = (IJavaClassType) types[0];
		}
		return fJavaLangClass;
	}

	/**
	 * Invokes Class.classForName(String, boolean, ClassLoader) on the target to
	 * force load the specified class.
	 * 
	 * @param qualifiedName
	 *            name of class to load
	 * @param loader
	 *            the class loader to use or <code>null</code> if the bootstrap
	 *            loader
	 * @return the loaded class
	 * @throws CoreException
	 *             if loading fails
	 */
	protected IJavaClassObject classForName(String qualifiedName,
			IJavaObject loader) throws CoreException {
		String tname = qualifiedName;
		if (tname.startsWith("[")) { //$NON-NLS-1$
			tname = TypeImpl.signatureToName(qualifiedName);
		}
		IJavaType[] types = getVM().getJavaTypes(tname);
		if (types != null && types.length > 0) {
			// find the one with the right class loader
			for (IJavaType type2 : types) {
				IJavaReferenceType type = (IJavaReferenceType) type2;
				IJavaObject cloader = type.getClassLoaderObject();
				if (isCompatibleLoader(loader, cloader)) {
					return type.getClassObject();
				}
			}
		}
		IJavaValue loaderArg = loader;
		if (loader == null) {
			loaderArg = getVM().nullValue();
		}
		// prevent the name string from being collected during the class lookup
		// call
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=301412
		final IJavaValue name = getVM().newValue(qualifiedName);
		((IJavaObject) name).disableCollection();
		IJavaValue[] args = new IJavaValue[] { name, getVM().newValue(true),
				loaderArg };
		try {
			return (IJavaClassObject) getJavaLangClass().sendMessage(FOR_NAME,
					FOR_NAME_SIGNATURE, args, getThread());
		} catch (CoreException e) {
			if (e.getStatus().getException() instanceof InvocationException) {
				// Don't throw ClassNotFoundException
				if (((InvocationException) e.getStatus().getException())
						.exception().referenceType().name()
						.equals("java.lang.ClassNotFoundException")) { //$NON-NLS-1$
					return null;
				}
			}
			throw e;
		} finally {
			((IJavaObject) name).enableCollection();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#classForName
	 * (java.lang.String)
	 */
	public IJavaClassObject classForName(String name) throws CoreException {
		return classForName(name, getClassLoaderObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.eval.ast.engine.IRuntimeContext#getProject
	 * ()
	 */
	public IJavaProject getProject() {
		return fProject;
	}

	/**
	 * Returns whether the class loaded by the <code>otherLoader</code> is
	 * compatible with the receiver's class loader. To be compatible, the
	 * other's loader must be the same or a parent of the receiver's loader.
	 * 
	 * @param recLoader
	 *            class loader of receiver
	 * @param otherLoader
	 *            class loader of other class
	 * @return whether compatible
	 */
	private boolean isCompatibleLoader(IJavaObject recLoader,
			IJavaObject otherLoader) throws CoreException {
		if (recLoader == null || otherLoader == null) {
			// if either class is a bootstrap loader, then they are compatible
			// since all loaders
			// stem from the bootstrap loader
			return true;
		}
		if (recLoader.equals(otherLoader)) {
			return true;
		}
		// check parent loaders
		IJavaObject parent = getParentLoader(recLoader);
		while (parent != null) {
			if (parent.equals(otherLoader)) {
				return true;
			}
			parent = getParentLoader(parent);
		}
		return false;
	}

	/**
	 * Returns the parent class loader of the given class loader object or
	 * <code>null</code> if none.
	 * 
	 * @param loader
	 *            class loader object
	 * @return parent class loader or <code>null</code>
	 * @throws CoreException
	 */
	private IJavaObject getParentLoader(IJavaObject loader)
			throws CoreException {
		// to avoid message send, first check for 'parent' field
		IJavaFieldVariable field = loader.getField("parent", false); //$NON-NLS-1$
		if (field != null) {
			IJavaValue value = (IJavaValue) field.getValue();
			if (value.isNull()) {
				return null;
			}
			return (IJavaObject) value;
		}
		IJavaValue result = loader
				.sendMessage(
						"getParent", "()Ljava/lang/ClassLoader;", null, getThread(), false); //$NON-NLS-1$ //$NON-NLS-2$
		if (result.isNull()) {
			return null;
		}
		return (IJavaObject) result;
	}
}
