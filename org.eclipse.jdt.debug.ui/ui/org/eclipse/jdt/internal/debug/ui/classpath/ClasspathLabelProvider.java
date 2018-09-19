/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathEntryLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for classpath elements
 */
public class ClasspathLabelProvider implements ILabelProvider {

	private RuntimeClasspathEntryLabelProvider runtimeClasspathLabelProvider= new RuntimeClasspathEntryLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof ClasspathEntry) {
			ClasspathEntry entry = (ClasspathEntry) element;
			return runtimeClasspathLabelProvider.getImage(entry);
		}

		return JavaClasspathTab.getClasspathImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof ClasspathEntry) {
			ClasspathEntry entry = (ClasspathEntry) element;
			return runtimeClasspathLabelProvider.getText(entry.getDelegate());
		}
		return element.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		runtimeClasspathLabelProvider.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * @param configuration
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		runtimeClasspathLabelProvider.setLaunchConfiguration(configuration);
	}
}
