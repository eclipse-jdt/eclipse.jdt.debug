/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.AppletLaunchConfigurationUtils;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

/**
 * 
 * Launch shortcut for Java applets.
 * <p>
 * This class may be instantiated or subclassed.
 * </p>
 * @since 3.3
 */
public class JavaAppletLaunchShortcut extends JavaLaunchShortcut {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#createConfiguration(org.eclipse.jdt.core.IType)
	 */
	@Override
	protected ILaunchConfiguration createConfiguration(IType type) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(type.getElementName())); 
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_WIDTH, AppletParametersTab.DEFAULT_APPLET_WIDTH);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_HEIGHT, AppletParametersTab.DEFAULT_APPLET_HEIGHT);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_APPLET_NAME, ""); //$NON-NLS-1$
			wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});
			config = wc.doSave();		
		} catch (CoreException ce) {
			MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), LauncherMessages.JavaLaunchShortcut_3, ce.getStatus().getMessage());	
		}
		return config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getConfigurationType()
	 */
	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager lm= DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLET);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#findTypes(java.lang.Object[], org.eclipse.jface.operation.IRunnableContext)
	 */
	@Override
	protected IType[] findTypes(Object[] elements, IRunnableContext context) throws InterruptedException, CoreException {
		try {
			return AppletLaunchConfigurationUtils.findApplets(context, elements);
		} catch (InvocationTargetException e) {
			throw (CoreException)e.getTargetException();
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getTypeSelectionTitle()
	 */
	@Override
	protected String getTypeSelectionTitle() {
		return LauncherMessages.JavaAppletLaunchShortcut_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getEditorEmptyMessage()
	 */
	@Override
	protected String getEditorEmptyMessage() {
		return LauncherMessages.JavaAppletLaunchShortcut_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#getSelectionEmptyMessage()
	 */
	@Override
	protected String getSelectionEmptyMessage() {
		return LauncherMessages.JavaAppletLaunchShortcut_2;
	}	
}
