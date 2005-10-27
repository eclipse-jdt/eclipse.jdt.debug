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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Breakpoint participant for method rename.
 * 
 * @since 3.2
 */
public class BreakpointMethodRenameParticipant extends RenameParticipant {

	/**
	 * Method being renamed
	 */
	private IMethod fMethod;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	protected boolean initialize(Object element) {
		if (element instanceof IMethod) {
			fMethod = (IMethod) element;
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	public String getName() {
		return RefactoringMessages.BreakpointMethodRenameParticipant_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#checkConditions(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
	 */
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		Change[] changes = gatherChanges(fMethod, getArguments().getNewName());
		if (changes.length > 1) {
			return new CompositeChange(RefactoringMessages.BreakpointMethodRenameParticipant_1, changes);
		} else if (changes.length == 1) {
			return changes[0];
		}
		return null;
	}
	
	/**
	 * Returns all breakpoint changes required by renaming the given method.
	 *
	 * @return require changes
	 */
	protected Change[] gatherChanges(IMethod originalMethod, String destMethodName) throws CoreException {
		ICompilationUnit originalCU = originalMethod.getCompilationUnit();
		List changes = new ArrayList();
		IResource resource = originalCU.getResource();
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_ZERO);
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = manager.getBreakpoint(marker);
			if (breakpoint instanceof IJavaMethodBreakpoint) {
				IJavaMethodBreakpoint methodBreakpoint = (IJavaMethodBreakpoint) breakpoint;
				IType breakpointType = BreakpointUtils.getType(methodBreakpoint);
				if (breakpointType != null && originalMethod.getDeclaringType().equals(breakpointType)) {
					IMethod destMethod = originalMethod.getDeclaringType().getMethod(destMethodName, originalMethod.getParameterTypes());
					changes.add(new MethodBreakpointMethodChange(methodBreakpoint, destMethod, originalMethod));
				}
			}
		}
		return (Change[]) changes.toArray(new Change[changes.size()]);
	}		
	
}
