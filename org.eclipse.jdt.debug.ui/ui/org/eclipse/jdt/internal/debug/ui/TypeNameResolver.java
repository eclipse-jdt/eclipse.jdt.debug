/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.debug.internal.core.variables.ResourceResolver;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

/**
 * Variable resolver which returns the fully qualified name of the
 * primary type in the selected resource.
 */
public class TypeNameResolver extends ResourceResolver {
	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		IResource resource = getSelectedResource(variable);
		IJavaElement javaElement = JavaCore.create(resource);
		if (javaElement != null) {
			IType type= getType(javaElement);
			if (type != null) {
				return type.getFullyQualifiedName();
			}	
		}
		abort(DebugUIMessages.TypeNameResolver_0, null); 
		return null;
	}
	
	/**
	 * Returns the primary type in the given Java element
	 * or <code>null</code> if none.
	 * 
	 * @param element the Java element
	 * @return the primary type in the given Java element
	 */
	public static IType getType(IJavaElement element) {
		IType type= null;
		int elementType= element.getElementType();
		switch (elementType) {
			case IJavaElement.CLASS_FILE :
				type= ((IClassFile) element).getType();
				break;
			case IJavaElement.COMPILATION_UNIT :
				type= ((ICompilationUnit) element).findPrimaryType();
				break;
		}
		return type;
	}
}
