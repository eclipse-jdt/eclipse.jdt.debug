/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;


import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * A dialog to select a type that extends <code>java.applet.Applet</code>.
 */
public class AppletSelectionDialog extends TwoPaneElementSelector {

	private IRunnableContext fRunnableContext;
	private IJavaProject fProject;

	private static class PackageRenderer extends JavaElementLabelProvider {
		public PackageRenderer() {
			super(JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_POST_QUALIFIED | JavaElementLabelProvider.SHOW_ROOT);
		}

		public Image getImage(Object element) {
			return super.getImage(((IType) element).getPackageFragment());
		}

		public String getText(Object element) {
			return super.getText(((IType) element).getPackageFragment());
		}
	}

	public AppletSelectionDialog(Shell shell, IRunnableContext context, IJavaProject project) {
		super(shell, new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_BASICS | JavaElementLabelProvider.SHOW_OVERLAY_ICONS), new PackageRenderer());

		Assert.isNotNull(context);

		fRunnableContext = context;
		fProject = project;
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/**
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		IType[] types = getAppletTypes();
		if (types != null) {
			setElements(types);
		}
		return super.open();
	}
	
	/**
	 * Return all types extending <code>java.lang.Applet</code> in the project, or
	 * all types extending Applet in the workspace if the project is <code>null</code>.
	 */
	private IType[] getAppletTypes() {
		// Populate an array of java projects with either the project specified in
		// the constructor, or ALL projects in the workspace if it is null
		IJavaProject[] javaProjects = null;
		if (fProject == null) {
			try {
				javaProjects = getJavaModel().getJavaProjects();
			} catch (JavaModelException e) {
				return null;
			}
		} else {
			javaProjects = new IJavaProject[] {fProject};
		}
		
		// For each java project, collect the Applet types it contains and add 
		// them the results
		Set results = null;
		for (int i = 0; i < javaProjects.length; i++) {
			IJavaProject javaProject = javaProjects[i];
			Set applets = AppletLaunchConfigurationUtils.collectAppletTypesInProject(new NullProgressMonitor(), javaProject);
			if (results == null) {
				results = applets;
			} else {
				results.addAll(applets);
			}
		}

		// Convert the results to an array and return it
		if (results != null) {
			IType[] types = null;
			types = (IType[]) results.toArray(new IType[results.size()]);		
			return types;
		} else {
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	public Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		applyDialogFont(control);
		return control;
	}

	/**
	 * Convenience method to get access to the java model.
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}
