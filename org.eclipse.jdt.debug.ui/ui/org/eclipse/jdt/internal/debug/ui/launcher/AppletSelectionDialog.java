/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
	private static final IType[] EMPTY_TYPE_ARRAY = new IType[] {};

	private static class PackageRenderer extends JavaElementLabelProvider {
		public PackageRenderer() {
			super(JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_POST_QUALIFIED | JavaElementLabelProvider.SHOW_ROOT);
		}

		@Override
		public Image getImage(Object element) {
			return super.getImage(((IType) element).getPackageFragment());
		}

		@Override
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
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/**
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open() {
		IType[] types = getAppletTypes();
		if (types == null) {
			return CANCEL;
		}
		setElements(types);
		return super.open();
	}
	
	/**
	 * Return all types extending <code>java.lang.Applet</code> in the project, or
	 * all types extending Applet in the workspace if the project is <code>null</code>.
	 * If the search is canceled, return <code>null</code>.
	 * @return the array of {@link IType}s
	 */
	private IType[] getAppletTypes() {
		// Populate an array of java projects with either the project specified in
		// the constructor, or ALL projects in the workspace if no project was specified
		final IJavaProject[] javaProjects;
		if (fProject == null) {
			try {
				javaProjects = getJavaModel().getJavaProjects();
			} catch (JavaModelException jme) {
				return EMPTY_TYPE_ARRAY;
			}
		} else {
			javaProjects = new IJavaProject[] {fProject};
		}
		
		// For each java project, calculate the Applet types it contains and add 
		// them to the results
		final int projectCount = javaProjects.length;
		final Set<IType> results = new HashSet<IType>(projectCount);
		boolean canceled = false;
		try {
			fRunnableContext.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {	
					monitor.beginTask(LauncherMessages.AppletSelectionDialog_Searching____1, projectCount); 
					for (int i = 0; i < projectCount; i++) {
						IJavaProject javaProject = javaProjects[i];
						SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1); 
						results.addAll(AppletLaunchConfigurationUtils.collectAppletTypesInProject(subMonitor, javaProject));
						monitor.worked(1);
					}					
					monitor.done();
				}
			});
		} catch (InvocationTargetException ite) {
		} catch (InterruptedException ie) {
			canceled = true;
		}

		// Convert the results to an array and return it
		if (canceled) {
			return null;
		}
		IType[] types = null;
		types = results.toArray(new IType[results.size()]);		
		return types; 
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		applyDialogFont(control);
		return control;
	}

	/**
	 * Convenience method to get access to the java model.
	 * @return the current Java model
	 */
	private IJavaModel getJavaModel() {
		return JavaCore.create(getWorkspaceRoot());
	}

	/**
	 * Convenience method to get the workspace root.
	 * @return the {@link IWorkspaceRoot}
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
}
