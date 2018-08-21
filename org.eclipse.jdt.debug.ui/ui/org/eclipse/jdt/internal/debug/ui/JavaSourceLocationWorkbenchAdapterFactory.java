/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;


import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * UI adapter factory for JDI Debug
 *
 * @deprecated
 */
@Deprecated
/*package*/ class JavaSourceLocationWorkbenchAdapterFactory implements IAdapterFactory {

	class SourceLocationPropertiesAdapter implements IWorkbenchAdapter {

		private JavaElementLabelProvider fJavaElementLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_BASICS);

		/**
		 * @see IWorkbenchAdapter#getChildren(Object)
		 */
		@Override
		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		/**
		 * @see IWorkbenchAdapter#getImageDescriptor(Object)
		 */
		@Override
		public ImageDescriptor getImageDescriptor(Object o) {
			if (o instanceof JavaProjectSourceLocation) {
				return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
			} else if (o instanceof DirectorySourceLocation) {
				return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
			} else if (o instanceof ArchiveSourceLocation) {
				return JavaUI.getSharedImages().getImageDescriptor(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
			}
			return null;
		}

		/**
		 * @see IWorkbenchAdapter#getLabel(Object)
		 */
		@Override
		public String getLabel(Object o) {
			if (o instanceof JavaProjectSourceLocation) {
				return fJavaElementLabelProvider.getText(((JavaProjectSourceLocation)o).getJavaProject());
			} else if (o instanceof DirectorySourceLocation) {
				try {
					return ((DirectorySourceLocation)o).getDirectory().getCanonicalPath();
				} catch (IOException e) {
					JDIDebugUIPlugin.log(e);
					return ((DirectorySourceLocation)o).getDirectory().getName();
				}
			} else if (o instanceof ArchiveSourceLocation) {
				return ((ArchiveSourceLocation)o).getName();
			}
			return null;
		}

		/**
		 * @see IWorkbenchAdapter#getParent(Object)
		 */
		@Override
		public Object getParent(Object o) {
			return null;
		}
	}

	/**
	 * @see IAdapterFactory#getAdapter(Object, Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object obj, Class<T> adapterType) {
		if (adapterType.isInstance(obj)) {
			return (T) obj;
		}
		if (adapterType == IWorkbenchAdapter.class) {
			if (obj instanceof IJavaSourceLocation) {
				return (T) new SourceLocationPropertiesAdapter();
			}
		}
		return null;
	}

	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] {
			IWorkbenchAdapter.class,
		};
	}
}


