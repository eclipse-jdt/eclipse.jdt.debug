/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Breakpoint participant for a rename refactoring.
 * 
 * @since 3.2
 */
public abstract class BreakpointRenameParticipant extends RenameParticipant {
	
	/**
	 * Element being renamed
	 */
	private IJavaElement fElement;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	protected boolean initialize(Object element) {
		if (element instanceof IJavaElement && accepts((IJavaElement)element)) {
			fElement = (IJavaElement) element;
		} else {
			return false;
		}
		return true;
	}	
	
	/**
	 * Returns the element this refactoring is operating on.
	 * 
	 * @return
	 */
	protected IJavaElement getOriginalElement() {
		return fElement;
	}
	
	/**
	 * Returns whether this given element is a valid target for this operation.
	 * 
	 * @param element
	 * @return
	 */
	protected abstract boolean accepts(IJavaElement element);

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	public String getName() {
		return RefactoringMessages.BreakpointRenameParticipant_0;
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
		List changes = new ArrayList();
		IResource resource = getBreakpointContainer();
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		gatherChanges(markers, changes, getArguments().getNewName());
		if (changes.size() > 1) {
			return new CompositeChange(RefactoringMessages.BreakpointRenameParticipant_1, (Change[]) changes.toArray(new Change[changes.size()]));
		} else if (changes.size() == 1) {
			return (Change) changes.get(0);
		}
		return null;
	}
	
	/**
	 * Gathers refactoring specific changes. Subclasses must override.
	 * 
	 * @param breakpoint markers to consider during the change
	 * @param list to add changes to 
	 * @param name of the element being renamed
	 * @return changes for this refactoring.
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	protected abstract void gatherChanges(IMarker[] markers, List changes, String destName) throws CoreException, OperationCanceledException;
	
	/**
	 * Returns the resource that should be considered when searching for affected breakpoints.
	 * 
	 * @return resource to search for breakpoint markers.
	 */
	protected IResource getBreakpointContainer() {
		return fElement.getResource();
	}
	
	/**
	 * Returns the breakpoint associated with the given marker.
	 * 
	 * @param marker breakpoint marker
	 * @return breakpoint or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}
	
	/**
	 * Creates a specific type of change for a breakpoint that is changing types.
	 * 
	 * @return type change or <code>null</code>
	 */
	protected Change createTypeChange(IJavaBreakpoint breakpoint, IType destType, IType originalType) throws CoreException {
		if (breakpoint instanceof IJavaWatchpoint) {
			return new WatchpointTypeChange((IJavaWatchpoint) breakpoint, destType, originalType);
		} else if (breakpoint instanceof IJavaClassPrepareBreakpoint) {
			return new ClassPrepareBreakpointTypeChange((IJavaClassPrepareBreakpoint) breakpoint, destType);
		} else if (breakpoint instanceof IJavaMethodBreakpoint) {
			return new MethodBreakpointTypeChange((IJavaMethodBreakpoint) breakpoint, destType);
		} else if (breakpoint instanceof IJavaExceptionBreakpoint) {
			return new ExceptionBreakpointTypeChange((IJavaExceptionBreakpoint) breakpoint, destType);
		} else if (breakpoint instanceof IJavaLineBreakpoint) {
			return new LineBreakpointTypeChange((IJavaLineBreakpoint) breakpoint, destType);
		}
		return null;
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
