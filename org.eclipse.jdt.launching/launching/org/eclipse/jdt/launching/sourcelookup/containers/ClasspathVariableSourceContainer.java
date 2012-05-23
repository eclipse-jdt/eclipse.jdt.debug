/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.containers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * A classpath variable source container contains a source container
 * that is the resolved value of the associated variable.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ClasspathVariableSourceContainer extends CompositeSourceContainer {
	
	private IPath fVariable;
	/**
	 * Unique identifier for Java project source container type
	 * (value <code>org.eclipse.jdt.launching.sourceContainer.classpathVariable</code>).
	 */
	public static final String TYPE_ID = LaunchingPlugin.getUniqueIdentifier() + ".sourceContainer.classpathVariable";   //$NON-NLS-1$
	
	/**
	 * Constructs a new source container on the given variable and suffix.
	 * 
	 * @param variablePath path representing a Java classpath variable.
	 *  The first segment is the variable name, and the following segments
	 *  (if any) are appended to the variable.
	 */
	public ClasspathVariableSourceContainer(IPath variablePath) {
		fVariable = variablePath;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	@Override
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		IPath path = JavaCore.getClasspathVariable(fVariable.segment(0));
		if (path == null) {
			return new ISourceContainer[0];
		}
		if (fVariable.segmentCount() > 1) {
			path = path.append(fVariable.removeFirstSegments(1));			
		}
		IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
		return JavaRuntime.getSourceContainers(new IRuntimeClasspathEntry[]{entry});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fVariable.toOSString();
	}
	
	/**
	 * Returns the variable this container references as a path. The
	 * first segment is the variable name, and the following segments
	 * are appended to the variable's value.
	 * 
	 * @return path representing the variable and suffix
	 */
	public IPath getPath() {
		return fVariable;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
}
