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

package org.eclipse.jdt.launching.sourcelookup.containers;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ArchiveSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

import com.sun.jdi.VMDisconnectedException;

/**
 * A source lookup participant that searches for Java source code.
 * <p>
 * This class may be instantiated; this class is not intended to be
 * subclassed.
 * </p>
 * @since 3.0
 */
public class JavaSourceLookupParticipant extends AbstractSourceLookupParticipant {
	
	/**
	 * Map of delegate source containers for internal jars.
	 * Internal jars are translated to package fragment roots
	 * if possible.
	 */
	private Map fDelegateContainers;
	
	/**
	 * Returns the source name associated with the given object, or <code>null</code>
	 * if none.
	 * 
	 * @param object a Java stack frame
	 * @return the source name associated with the given object, or <code>null</code>
	 * if none
	 * @exception CoreException if unable to retrieve the source name
	 */
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof IAdaptable) {
			IJavaStackFrame frame = (IJavaStackFrame) ((IAdaptable)object).getAdapter(IJavaStackFrame.class);
			try {
				if (frame != null) {
					if (frame.isObsolete()) {
						return null;
					}
					String sourceName = frame.getSourcePath();
					// TODO: this may break fix to bug 21518
					if (sourceName == null) {
						// no debug attributes, guess at source name
						sourceName = frame.getDeclaringTypeName();
						int index = sourceName.lastIndexOf('.');
						if (index < 0) {
							index = 0;
						}
						sourceName = sourceName.replace('.', File.separatorChar);
						index = sourceName.indexOf('$');
						if (index >= 0) {
							sourceName = sourceName.substring(0, index);
						}
						if (sourceName.length() == 0) {
							// likely a proxy class (see bug 40815)
							sourceName = null;
						} else {
							sourceName = sourceName + ".java"; //$NON-NLS-1$
						}
					}
					return sourceName;	
				}
			} catch (DebugException e) {
				int code = e.getStatus().getCode();
                if (code == IJavaThread.ERR_THREAD_NOT_SUSPENDED || code == IJavaStackFrame.ERR_INVALID_STACK_FRAME ||
						e.getStatus().getException() instanceof VMDisconnectedException) {
					return null;
				}
				throw e;
			}
		}
		if (object instanceof String) {
			// assume it's a file name
			return (String)object;
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#dispose()
	 */
	public void dispose() {
		Iterator iterator = fDelegateContainers.values().iterator();
		while (iterator.hasNext()) {
			ISourceContainer container = (ISourceContainer) iterator.next();
			container.dispose();
		}
		fDelegateContainers = null;
		super.dispose();
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupParticipant#getDelegateContainer(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	protected ISourceContainer getDelegateContainer(ISourceContainer container) {
		ISourceContainer delegate = (ISourceContainer) fDelegateContainers.get(container);
		if (delegate == null) {
			return container;
		} 
		return delegate; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#init(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	public void init(ISourceLookupDirector director) {
		super.init(director);
		fDelegateContainers = new HashMap();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	public void sourceContainersChanged(ISourceLookupDirector director) {
		// use package fragment roots in place of local archives, where they exist
		fDelegateContainers.clear();
		ISourceContainer[] containers = director.getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			if (container.getType().getId().equals(ArchiveSourceContainer.TYPE_ID)) {
				IFile file = ((ArchiveSourceContainer)container).getFile();
				IProject project = file.getProject();
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject.exists()) {
					try {
						IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
						for (int j = 0; j < roots.length; j++) {
							IPackageFragmentRoot root = roots[j];
							if (file.equals(root.getUnderlyingResource())) {
								// the root was specified
								fDelegateContainers.put(container, new PackageFragmentRootSourceContainer(root));
							} else {
								IPath path = root.getSourceAttachmentPath();
								if (path != null) {
									if (file.getFullPath().equals(path)) {
										// a source attachment to a root was specified
										fDelegateContainers.put(container, new PackageFragmentRootSourceContainer(root));
									}
								}
							}
						}
					} catch (JavaModelException e) {
					}
				}
			}
		}
	}
}
