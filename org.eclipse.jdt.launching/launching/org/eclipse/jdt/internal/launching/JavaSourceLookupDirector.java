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
package org.eclipse.jdt.internal.launching;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

/**
 * Java source lookup director.
 *
 * @since 3.0
 */
public class JavaSourceLookupDirector extends AbstractSourceLookupDirector {

	private static Set<String> fFilteredTypes;

	static {
		fFilteredTypes = new HashSet<>();
		fFilteredTypes.add(ProjectSourceContainer.TYPE_ID);
		fFilteredTypes.add(WorkspaceSourceContainer.TYPE_ID);
		// can't reference UI constant
		fFilteredTypes.add("org.eclipse.debug.ui.containerType.workingSet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {new JavaSourceLookupParticipant()});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType)
	 */
	@Override
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return !fFilteredTypes.contains(type.getId());
	}
}
