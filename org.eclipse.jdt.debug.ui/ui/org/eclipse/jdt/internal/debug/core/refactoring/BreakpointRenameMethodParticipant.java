/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Breakpoint participant for method rename.
 * 
 * @since 3.2
 */
public class BreakpointRenameMethodParticipant extends BreakpointRenameParticipant {

	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#accepts(org.eclipse.jdt.core.IJavaElement)
	 */
	@Override
	protected boolean accepts(IJavaElement element) {
		return element instanceof IMethod;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#gatherChanges(org.eclipse.core.resources.IMarker[], java.util.List, java.lang.String)
	 */
	@Override
	protected void gatherChanges(IMarker[] markers, List<Change> changes, String destMethodName) throws CoreException, OperationCanceledException {
		IMethod originalMethod = (IMethod) getOriginalElement();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = getBreakpoint(marker);
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoint;
				//ensure we only update the marker that corresponds to the method being renamed
				//https://bugs.eclipse.org/bugs/show_bug.cgi?id=280518
				if(methodBreakpoint.getMethodName().equals(originalMethod.getElementName()) &&
						methodBreakpoint.getMethodSignature().equals(originalMethod.getSignature())) {
					IType breakpointType = BreakpointUtils.getType(methodBreakpoint);
					if (breakpointType != null && originalMethod.getDeclaringType().equals(breakpointType)) {
						IMethod destMethod = originalMethod.getDeclaringType().getMethod(destMethodName, originalMethod.getParameterTypes());
						changes.add(new MethodBreakpointMethodChange(methodBreakpoint, destMethod));
					}
				}
			}
		}
	}
	
}
