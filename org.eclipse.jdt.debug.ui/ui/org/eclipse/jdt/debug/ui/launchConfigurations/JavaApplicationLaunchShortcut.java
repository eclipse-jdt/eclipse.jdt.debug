/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

/**
 * Launch shortcut for local Java applications.
 * <p>
 * This class may be instantiated or sub-classed.
 * </p>
 * @since 3.3
 */
public class JavaApplicationLaunchShortcut extends JavaLaunchShortcut {
	/**
	 * Test if a type is from a location marked as test code (from the perspective of the project where it is defined.)
	 *
	 * @param type
	 *            the type that is examined
	 * @return false, if the corresponding class path entry is found and is not marked as test, otherwise true
	 * @throws JavaModelException
	 *             when access to the classpath entry corresponding to the given type fails.
	 */
	private static boolean isTestCode(IType type) throws JavaModelException {
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) type.getPackageFragment().getParent();
		IJavaProject javaProject = packageFragmentRoot.getJavaProject();
		if (javaProject != null) {
			IClasspathEntry entry = javaProject.getClasspathEntryFor(packageFragmentRoot.getPath());
			if (entry != null && !entry.isTest()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the Java elements corresponding to the given objects. Members are translated to corresponding declaring types where possible.
	 *
	 * @param objects
	 *            selected objects
	 * @return corresponding Java elements
	 * @since 3.5
	 */
	protected IJavaElement[] getJavaElements(Object[] objects) {
		List<IJavaElement> list= new ArrayList<>(objects.length);
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof IAdaptable) {
				IJavaElement element = ((IAdaptable) object).getAdapter(IJavaElement.class);
				if (element != null) {
					if (element instanceof IMember) {
						// Use the declaring type if available
						IJavaElement type= ((IMember)element).getDeclaringType();
						if (type != null) {
							element= type;
						}
					}
					list.add(element);
				}
			}
		}
		return list.toArray(new IJavaElement[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#createConfiguration(org.eclipse.jdt.core.IType)
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			if (!isTestCode(type)) {
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE, true);
			}
			wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});
			config = wc.doSave();
		} catch (CoreException exception) {
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), LauncherMessages.JavaLaunchShortcut_3, exception.getStatus().getMessage());
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getConfigurationType()
	 */
	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
	}

	/**
	 * Returns the singleton launch manager.
	 *
	 * @return launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#findTypes(java.lang.Object[], org.eclipse.jface.operation.IRunnableContext)
	 */
	@Override
	protected IType[] findTypes(Object[] elements, IRunnableContext context) throws InterruptedException, CoreException {
		try {
			if(elements.length == 1) {
				IType type = isMainMethod(elements[0]);
				if(type != null) {
					return new IType[] {type};
				}
			}
			IJavaElement[] javaElements = getJavaElements(elements);
			MainMethodSearchEngine engine = new MainMethodSearchEngine();
			int constraints = IJavaSearchScope.SOURCES;
			constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaElements, constraints);
			return engine.searchMainMethods(context, scope, true);
		} catch (InvocationTargetException e) {
			throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), e.getMessage(), e));
		}
	}

	/**
	 * Returns the smallest enclosing <code>IType</code> if the specified object is a main method, or <code>null</code>
	 * @param o the object to inspect
	 * @return the smallest enclosing <code>IType</code> of the specified object if it is a main method or <code>null</code> if it is not
	 */
	private IType isMainMethod(Object o) {
		if(o instanceof IAdaptable) {
			IAdaptable adapt = (IAdaptable) o;
			IJavaElement element = adapt.getAdapter(IJavaElement.class);
			if(element != null && element.getElementType() == IJavaElement.METHOD) {
				try {
					IMethod method = (IMethod) element;
					if(method.isMainMethod()) {
						return method.getDeclaringType();
					}
				}
				catch (JavaModelException jme) {JDIDebugUIPlugin.log(jme);}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getTypeSelectionTitle()
	 */
	@Override
	protected String getTypeSelectionTitle() {
		return LauncherMessages.JavaApplicationLaunchShortcut_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getEditorEmptyMessage()
	 */
	@Override
	protected String getEditorEmptyMessage() {
		return LauncherMessages.JavaApplicationLaunchShortcut_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getSelectionEmptyMessage()
	 */
	@Override
	protected String getSelectionEmptyMessage() {
		return LauncherMessages.JavaApplicationLaunchShortcut_2;
	}
}
