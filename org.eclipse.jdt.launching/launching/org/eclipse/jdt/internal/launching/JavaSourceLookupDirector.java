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
import java.util.Objects;
import java.util.Set;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.internal.core.AbstractClassFile;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

/**
 * Java source lookup director.
 *
 * @since 3.0
 */
public class JavaSourceLookupDirector extends AbstractSourceLookupDirector {

	private static final Set<String> fFilteredTypes;

	static {
		fFilteredTypes = new HashSet<>();
		fFilteredTypes.add(ProjectSourceContainer.TYPE_ID);
		fFilteredTypes.add(WorkspaceSourceContainer.TYPE_ID);
		// can't reference UI constant
		fFilteredTypes.add("org.eclipse.debug.ui.containerType.workingSet"); //$NON-NLS-1$
	}

	@Override
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] {new JavaSourceLookupParticipant()});
	}

	@Override
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return !fFilteredTypes.contains(type.getId());
	}

	@Override
	public boolean equalSourceElements(Object o1, Object o2) {
		if (o1 instanceof AbstractClassFile c1 && o2 instanceof AbstractClassFile c2) {
			String pathIdentifier1 = c1.getPathIdentifier();
			String pathIdentifier2 = c2.getPathIdentifier();
			return Objects.equals(pathIdentifier1, pathIdentifier2);
		}
		return super.equalSourceElements(o1, o2);
	}
}
