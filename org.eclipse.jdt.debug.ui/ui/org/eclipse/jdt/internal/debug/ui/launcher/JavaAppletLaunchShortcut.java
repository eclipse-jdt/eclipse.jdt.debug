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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.AppletParametersTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableContext;

public class JavaAppletLaunchShortcut extends JavaLaunchShortcut {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#createConfiguration(org.eclipse.jdt.core.IType)
	 */
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(type.getElementName())); 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, AppletParametersTab.DEFAULT_APPLET_WIDTH);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, AppletParametersTab.DEFAULT_APPLET_HEIGHT);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, ""); //$NON-NLS-1$
			config = wc.doSave();		
		} catch (CoreException ce) {
			reportErorr(ce);			
		}
		return config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#getConfigurationType()
	 */
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager lm= DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#findTypes(java.lang.Object[])
	 */
	protected IType[] findTypes(Object[] elements, IRunnableContext context) throws InterruptedException, CoreException {
		try {
			return AppletLaunchConfigurationUtils.findApplets(context, elements);
		} catch (InvocationTargetException e) {
			throw (CoreException)e.getTargetException();
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#getTypeSelectionTitle()
	 */
	protected String getTypeSelectionTitle() {
		return LauncherMessages.JavaAppletLaunchShortcut_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#getEditorEmptyMessage()
	 */
	protected String getEditorEmptyMessage() {
		return LauncherMessages.JavaAppletLaunchShortcut_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchShortcut#getSelectionEmptyMessage()
	 */
	protected String getSelectionEmptyMessage() {
		return LauncherMessages.JavaAppletLaunchShortcut_2;
	}	
}
