/*******************************************************************************
 * Copyright (c) 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.Assert;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * ResourceExtender provides propertyTester(s) for IResource types
 * for use in XML Expression Language syntax.
 */
public class ResourceExtender extends PropertyTester {

	private static final String PROPERTY_IS_APPLET= "isApplet";	 //$NON-NLS-1$
	private static final String PROPERTY_HAS_MAIN_TYPE= "hasMainType";	 //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.properties.IPropertyEvaluator#test(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
		IResource resource= (IResource)receiver;
		if (PROPERTY_IS_APPLET.equals(method)) { //$NON-NLS-1$
			return isApplet(resource);
		} else if (PROPERTY_HAS_MAIN_TYPE.equals(method)) {
			return hasMain(resource);
		}
		Assert.isTrue(false);
		return false;
	}

	/**
	 * Check if the specified resource is an Applet.
	 * @return <code>true</code> if the target resource is an Applet,
	 * <code>false</code> otherwise.
	 */
	private boolean isApplet(IResource resource) {
		if (resource != null) {
			try {
				Set result= new HashSet();
				AppletLaunchConfigurationUtils.collectTypes(resource, new NullProgressMonitor(), result);
				if (result.size() > 0) {
					return true;
				}
			} catch (JavaModelException e) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Look for a Java main() method in the specified resource.
	 * @return true if the target resource has a <code>main</code> method,
	 * <code>false</code> otherwise.
	 */
	private boolean hasMain(IResource target) {
		if (target != null) {
			IJavaElement element = JavaCore.create(target);
			if (element instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) element;
				IType mainType= cu.getType(Signature.getQualifier(cu.getElementName()));
				try {
					if (mainType.exists() && JavaModelUtil.hasMainMethod(mainType)) {
						return true;
					}
				} catch (JavaModelException e) {
					return false;
				}
			} else if (element instanceof IClassFile) {
				IType mainType;
				try {
					mainType = ((IClassFile)element).getType();
					if (JavaModelUtil.hasMainMethod(mainType)) {
						return true;
					}
				} catch (JavaModelException e) {
					return false;
				}
			}
		}
		return false;
	}
}