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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Breakpoint participant for field rename.
 * 
 * @since 3.2
 */
public class BreakpointFieldRenameParticipant extends RenameParticipant {

	/**
	 * Field being renamed
	 */
	private IField fField;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	protected boolean initialize(Object element) {
		if (element instanceof IField) {
			fField = (IField) element;
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	public String getName() {
		return RefactoringMessages.BreakpointFieldRenameParticipant_0;
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
		Change[] changes = gatherChanges(fField, getArguments().getNewName());
		if (changes.length > 1) {
			return new CompositeChange(RefactoringMessages.BreakpointFieldRenameParticipant_1, changes);
		} else if (changes.length == 1) {
			return changes[0];
		}
		return null;
	}
	
	/**
	 * Returns all breakpoint changes required by renaming the given field.
	 *
	 * @return require changes
	 */
	protected Change[] gatherChanges(IField originalField, String destFieldName) throws CoreException {
		ICompilationUnit originalCU = originalField.getCompilationUnit();
		List changes = new ArrayList();
		IResource resource = originalCU.getResource();
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_ZERO);
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = manager.getBreakpoint(marker);
			if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint watchpoint = (IJavaWatchpoint) breakpoint;
				IType breakpointType = BreakpointUtils.getType(watchpoint);
				if (breakpointType != null && originalField.getDeclaringType().equals(breakpointType)) {
					IField destField = originalField.getDeclaringType().getField(destFieldName);
					changes.add(new WatchpointFieldChange(watchpoint, destField));
				}
			}
		}
		return (Change[]) changes.toArray(new Change[changes.size()]);
	}		

}
