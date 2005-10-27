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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Breakpoint participant for type rename.
 * 
 * @since 3.2
 */
public class BreakpointTypeRenameParticipant extends RenameParticipant {

	/**
	 * Type being renamed
	 */
	private IType fType;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			fType = (IType) element;
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	public String getName() {
		return RefactoringMessages.BreakpointTypeRenameParticipant_0;
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
		Change[] changes = gatherChanges(fType, getArguments().getNewName());
		if (changes.length > 1) {
			return new CompositeChange(RefactoringMessages.BreakpointTypeRenameParticipant_1, changes);
		} else if (changes.length == 1) {
			return changes[0];
		}
		return null;
	}
	
	/**
	 * Returns all breakpoint changes required by renaming the given type.
	 * <p>
	 * When a type is renamed, all breakpoints contained within the type are
	 * effected. As well, if the type is a public top-level type, the compilation
	 * unit will be renamed, which affects all breakpoints in the compilation
	 * unit in other top-level, non-public types (i.e. those breakpoints need to
	 * be re-created in the new file).
	 * </p>
	 * @return require changes
	 */
	protected Change[] gatherChanges(IType originalType, String simpleDestName) throws CoreException {
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
		List changes = new ArrayList();
		IResource resource = originalCU.getResource();
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_ZERO);
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = manager.getBreakpoint(marker);
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
					if (javaBreakpoint instanceof IJavaWatchpoint) {
						changes.add(new WatchpointTypeChange((IJavaWatchpoint) javaBreakpoint, destType, breakpointType));
					} else if (javaBreakpoint instanceof IJavaClassPrepareBreakpoint) {
						changes.add(new ClassPrepareBreakpointTypeChange((IJavaClassPrepareBreakpoint) javaBreakpoint, destType, breakpointType));
					} else if (javaBreakpoint instanceof IJavaMethodBreakpoint) {
						changes.add(new MethodBreakpointTypeChange((IJavaMethodBreakpoint) breakpoint, destType, breakpointType));
					} else if (javaBreakpoint instanceof IJavaExceptionBreakpoint) {
						changes.add(new ExceptionBreakpointTypeChange((IJavaExceptionBreakpoint) javaBreakpoint, destType, breakpointType));
					} else if (javaBreakpoint instanceof IJavaLineBreakpoint) {
						changes.add(new LineBreakpointTypeChange((IJavaLineBreakpoint) javaBreakpoint, destType, breakpointType));
					}
				}
			}
		}
		return (Change[]) changes.toArray(new Change[changes.size()]);
	}		
	
	/**
	 * Returns whether the given target type is contained in the specified container type.
	 * 
	 * @param container
	 * @param target
	 * @return
	 */
	protected boolean isContained(IJavaElement container, IType type) {
		IJavaElement parent = type;
		while (parent != null) {
			if (parent.equals(container)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}
}
