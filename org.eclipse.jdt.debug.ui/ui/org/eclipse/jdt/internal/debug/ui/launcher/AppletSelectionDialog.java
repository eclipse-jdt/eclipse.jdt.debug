package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * A dialog to select a type that extends <code>java.applet.Applet</code>.
 */
public class AppletSelectionDialog extends TwoPaneElementSelector {

	private IRunnableContext fRunnableContext;
	private IJavaProject fProject;
	
	private static final String fgAppletClass = "java.applet.Applet"; //$NON-NLS-1$
	
	private static class PackageRenderer extends JavaElementLabelProvider {
		public PackageRenderer() {
			super(JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_POST_QUALIFIED | JavaElementLabelProvider.SHOW_ROOT);	
		}

		public Image getImage(Object element) {
			return super.getImage(((IType)element).getPackageFragment());
		}
		
		public String getText(Object element) {
			return super.getText(((IType)element).getPackageFragment());
		}
	}
	
	public AppletSelectionDialog(Shell shell, IRunnableContext context, IJavaProject project) {
		super(shell, 
				new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_BASICS | JavaElementLabelProvider.SHOW_OVERLAY_ICONS), 
				new PackageRenderer());

		Assert.isNotNull(context);
		Assert.isNotNull(project);

		fRunnableContext= context;
		fProject= project;
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
		IType[] types = null;
		Set result = AppletLaunchConfigurationUtils.collectAppletTypesInProject(new NullProgressMonitor(), fProject);
		types = (IType[]) result.toArray(new IType[result.size()]);
		if (types != null) {
			setElements(types);
		}
		return super.open();
	}
	
}