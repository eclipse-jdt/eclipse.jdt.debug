/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.classpath;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;

public class ClasspathEntry extends AbstractClasspathEntry implements IRuntimeClasspathEntry, IAdaptable {

	private IRuntimeClasspathEntry entry= null;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getJavaProject()
	 */
	@Override
	public IJavaProject getJavaProject() {
		return entry.getJavaProject();
	}
	public ClasspathEntry(IRuntimeClasspathEntry entry, IClasspathEntry parent) {
		this.parent= parent;
		this.entry= entry;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathEntry) {
			ClasspathEntry other= (ClasspathEntry)obj;
			if (entry != null) {
				return entry.equals(other.entry);
			}
		} else if (obj instanceof IRuntimeClasspathEntry) {
			return entry.equals(obj);
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return entry.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entry.getPath().toOSString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
	@Override
	public int getType() {
		return entry.getType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getMemento()
	 */
	@Override
	public String getMemento() throws CoreException {
		return entry.getMemento();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getPath()
	 */
	@Override
	public IPath getPath() {
		return entry.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getResource()
	 */
	@Override
	public IResource getResource() {
		return entry.getResource();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	@Override
	public IPath getSourceAttachmentPath() {
		return entry.getSourceAttachmentPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setSourceAttachmentPath(IPath path) {
		entry.setSourceAttachmentPath(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	@Override
	public IPath getSourceAttachmentRootPath() {
		return entry.getSourceAttachmentRootPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentRootPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setSourceAttachmentRootPath(IPath path) {
		entry.setSourceAttachmentRootPath(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getExternalAnnotationsPath()
	 */
	@Override
	public IPath getExternalAnnotationsPath() {
		return entry.getExternalAnnotationsPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setExternalAnnotationsPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setExternalAnnotationsPath(IPath path) {
		entry.setExternalAnnotationsPath(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathProperty()
	 */
	@Override
	public int getClasspathProperty() {
		return entry.getClasspathProperty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#setClasspathProperty(int)
	 */
	@Override
	public void setClasspathProperty(int location) {
		entry.setClasspathProperty(location);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getLocation()
	 */
	@Override
	public String getLocation() {
		return entry.getLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentLocation()
	 */
	@Override
	public String getSourceAttachmentLocation() {
		return entry.getSourceAttachmentLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootLocation()
	 */
	@Override
	public String getSourceAttachmentRootLocation() {
		return entry.getSourceAttachmentRootLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getVariableName()
	 */
	@Override
	public String getVariableName() {
		return entry.getVariableName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathEntry()
	 */
	@Override
	public org.eclipse.jdt.core.IClasspathEntry getClasspathEntry() {
		return entry.getClasspathEntry();
	}

	public IRuntimeClasspathEntry getDelegate() {
		return entry;
	}

	public boolean hasChildren() {
		IRuntimeClasspathEntry rpe = getDelegate();
		return rpe instanceof IRuntimeClasspathEntry2 &&
		 ((IRuntimeClasspathEntry2)rpe).isComposite();
	}

	public IClasspathEntry[] getChildren(ILaunchConfiguration configuration) {
		IRuntimeClasspathEntry rpe = getDelegate();
		if (rpe instanceof IRuntimeClasspathEntry2) {
			IRuntimeClasspathEntry2 r2 = (IRuntimeClasspathEntry2) rpe;
			try {
				IRuntimeClasspathEntry[] entries = r2.getRuntimeClasspathEntries(configuration);
				IClasspathEntry[] cps = new IClasspathEntry[entries.length];
				for (int i = 0; i < entries.length; i++) {
					IRuntimeClasspathEntry childEntry = entries[i];
					cps[i] = new ClasspathEntry(childEntry, this);
				}
				return cps;
			} catch (CoreException e) {
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return getParent() instanceof ClasspathGroup;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (getDelegate() instanceof IAdaptable) {
			return ((IAdaptable) getDelegate()).getAdapter(adapter);
		}
		return null;
	}

	@Override
	public boolean isAutomodule() {
		return this.entry.isAutomodule();
	}
}
