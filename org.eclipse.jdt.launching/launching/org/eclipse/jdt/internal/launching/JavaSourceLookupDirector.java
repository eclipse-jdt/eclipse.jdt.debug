/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector;

/**
 * Java source lookup director.
 * 
 * @since 3.0
 */
public class JavaSourceLookupDirector extends AbstractSourceLookupDirector {
	
	/**
	 * Constructs a Java source lookup director. 
	 */
	public JavaSourceLookupDirector() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		super.initializeDefaults(configuration);
		initializeParticipants();
	}
	
	/**
	 * Adds default participants to this director 
	 * 
	 * TODO: there should likely be a method to do this in the director framework
	 */
	private void initializeParticipants() {
		addSourceLookupParticipant(new JavaSourceLookupParticipant());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento(String memento) throws CoreException {
		super.initializeFromMemento(memento);
		initializeParticipants();
	}
}
