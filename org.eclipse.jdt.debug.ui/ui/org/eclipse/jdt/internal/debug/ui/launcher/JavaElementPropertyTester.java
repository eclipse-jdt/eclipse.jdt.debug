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

package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * ResourceExtender provides propertyTester(s) for IResource types
 * for use in XML Expression Language syntax.
 */
public class JavaElementPropertyTester extends PropertyTester {

	private static final String PROPERTY_IS_APPLET= "isApplet";	 //$NON-NLS-1$
	private static final String PROPERTY_HAS_MAIN_TYPE= "hasMainType";	 //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.properties.IPropertyEvaluator#test(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		IJavaElement javaElement = null;
		if (receiver instanceof IAdaptable) {
			javaElement = (IJavaElement) ((IAdaptable)receiver).getAdapter(IJavaElement.class);
		}
		if (javaElement != null) {
			if (!javaElement.exists()) {
				return false;
			}
		}
		if (javaElement instanceof IJavaProject ||
			javaElement instanceof IPackageFragmentRoot ||
			javaElement instanceof IPackageFragment) {
				// optomistic
				return true;
		}
		if (javaElement != null) {
			if (PROPERTY_IS_APPLET.equals(method)) { //$NON-NLS-1$
				return isApplet(javaElement);
			} else if (PROPERTY_HAS_MAIN_TYPE.equals(method)) {
				return hasMain(javaElement);
			}
		}
		return false;
	}

	/**
	 * Check if the specified resource is an Applet.
	 * @return <code>true</code> if the target resource is an Applet,
	 * <code>false</code> otherwise.
	 */
	private boolean isApplet(IJavaElement element) {
        try {
            IType type = getType(element);
            IType[] allSuperTypes = JavaModelUtil.getAllSuperTypes(type, new NullProgressMonitor());
            for (int i = 0; i < allSuperTypes.length; i++) {
                IType superType = allSuperTypes[i];
                if (superType.getFullyQualifiedName().equals("java.applet.Applet")) { //$NON-NLS-1$
                    return true;
                }
            }
        } catch (JavaModelException e) {
        }
        return false;
	}
	
	/**
	 * Look for a Java main() method in the specified resource.
	 * @return true if the target resource has a <code>main</code> method,
	 * <code>false</code> otherwise.
	 */
	private boolean hasMain(IJavaElement element) {
		try {
            IType mainType = getType(element);
			if (mainType != null && mainType.exists() && JavaModelUtil.hasMainMethod(mainType)) {
				return true;
			}
		} catch (JavaModelException e) {
		}
		return false;
	}
    
    private IType getType(IJavaElement element) throws JavaModelException {
        IType type = null;
        if (element instanceof ICompilationUnit) {
            ICompilationUnit cu = (ICompilationUnit) element;
            type= cu.getType(Signature.getQualifier(cu.getElementName()));
        } else if (element instanceof IClassFile) {
                type = ((IClassFile)element).getType();
        } else if (element instanceof IType) {
            type = (IType) element;
        } else if (element instanceof IMember) {
            type = ((IMember)element).getDeclaringType();
        }
        return type;
    }
}
