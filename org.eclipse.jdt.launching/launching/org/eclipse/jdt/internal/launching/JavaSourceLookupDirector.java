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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.internal.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.internal.core.sourcelookup.containers.ProjectSourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.containers.WorkspaceSourceContainerType;

/**
 * Java source lookup director.
 * 
 * @since 3.0
 */
public class JavaSourceLookupDirector extends AbstractSourceLookupDirector {
	
	private static Set fFilteredTypes;
	
	static {
		fFilteredTypes = new HashSet();
		fFilteredTypes.add(ProjectSourceContainerType.TYPE_ID);
		fFilteredTypes.add(WorkspaceSourceContainerType.TYPE_ID);
		// can't reference UI constant
		fFilteredTypes.add("org.eclipse.debug.ui.containerType.workingSet"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {new JavaSourceLookupParticipant()});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType)
	 */
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return !fFilteredTypes.contains(type.getId());
	}
}
