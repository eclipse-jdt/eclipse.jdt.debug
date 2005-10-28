/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.refactoring;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;

/**
 * Breakpoint participant for type rename.
 * 
 * @since 3.2
 */
public class BreakpointRenameTypeParticipant extends BreakpointRenameParticipant {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#accepts(org.eclipse.jdt.core.IJavaElement)
	 */
	protected boolean accepts(IJavaElement element) {
		return element instanceof IType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#gatherChanges(org.eclipse.core.resources.IMarker[], java.util.List, java.lang.String)
	 */
	protected void gatherChanges(IMarker[] markers, List changes, String simpleDestName) throws CoreException, OperationCanceledException {
		IType originalType = (IType) getOriginalElement();
		ICompilationUnit originalCU = originalType.getCompilationUnit();
		ICompilationUnit destCU = null;
		IJavaElement affectedContainer = null;
		if (originalType.isMember() || !(originalCU.findPrimaryType().equals(originalType))) {
			destCU = originalCU;
			affectedContainer = originalType;
		} else if (originalCU.findPrimaryType().equals(originalType)) {
			destCU = originalType.getPackageFragment().getCompilationUnit(simpleDestName + ".java"); //$NON-NLS-1$
			affectedContainer = originalCU;
		}
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = getBreakpoint(marker);
			if (breakpoint instanceof IJavaBreakpoint) {
				IJavaBreakpoint javaBreakpoint = (IJavaBreakpoint) breakpoint;
				IType breakpointType = BreakpointUtils.getType(javaBreakpoint);
				if (breakpointType != null && isContained(affectedContainer, breakpointType)) {
					IType destType = null;
					String[] names = breakpointType.getTypeQualifiedName().split("\\$"); //$NON-NLS-1$
					if (isContained(originalType, breakpointType)) {
						String[] oldNames = originalType.getTypeQualifiedName().split("\\$"); //$NON-NLS-1$
						names[oldNames.length - 1] = simpleDestName;
					}
					destType = destCU.getType(names[0]);
					for (int j = 1; j < names.length; j++) {
						destType = destType.getType(names[j]);
					}
					changes.add(createTypeChange(javaBreakpoint, destType, breakpointType));
				}
			}
		}
	}

}
