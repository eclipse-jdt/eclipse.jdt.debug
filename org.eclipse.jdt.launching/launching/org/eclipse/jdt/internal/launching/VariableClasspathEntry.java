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

package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class VariableClasspathEntry extends AbstractRuntimeClasspathEntry {
	public static final String TYPE_ID = "org.eclipse.jdt.launching.classpathentry.variableClasspathEntry"; //$NON-NLS-1$
	private String variableString;
	
	public VariableClasspathEntry() {
	}
	
	public VariableClasspathEntry(String variableString) {
		this.variableString = variableString;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry#buildMemento(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	protected void buildMemento(Document document, Element memento) throws CoreException {
		memento.setAttribute("variableString", variableString); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#initializeFrom(org.w3c.dom.Element)
	 */
	public void initializeFrom(Element memento) throws CoreException {
		variableString = memento.getAttribute("variableString"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getTypeId()
	 */
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(ILaunchConfiguration configuration) throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getName()
	 */
	public String getName() {
		return variableString; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
	public int getType() {
		return OTHER; 
	}
	/**
	 * @return Returns the variableString.
	 */
	public String getVariableString() {
		return variableString;
	}
	/**
	 * @param variableString The variableString to set.
	 */
	public void setVariableString(String variableString) {
		this.variableString = variableString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getPath()
	 */
//	public IPath getPath() {
//		try {
//			String path = StringVariableManager.getDefault().performStringSubstitution(variableString);
//			return new Path(path);
//		} catch (CoreException ce) {
//			return null;
//		}
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (variableString != null)
			return variableString.hashCode();
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof VariableClasspathEntry) {
			VariableClasspathEntry other= (VariableClasspathEntry)obj;
			if (variableString != null) {
				return variableString.equals(other.variableString);
			}
		}
		return false;
	}

}
