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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
 * Breakpoint participant for package rename.
 * 
 * @since 3.2
 */
public class BreakpointPackageRenameParticipant extends RenameParticipant {

	/**
	 * Type being renamed
	 */
	private IPackageFragment fPackage;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#initialize(java.lang.Object)
	 */
	protected boolean initialize(Object element) {
		if (element instanceof IPackageFragment) {
			fPackage = (IPackageFragment) element;
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#getName()
	 */
	public String getName() {
		return RefactoringMessages.BreakpointPackageRenameParticipant_0;
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
		Change[] changes = gatherChanges(fPackage, getArguments().getNewName());
		if (changes.length > 1) {
			return new CompositeChange(RefactoringMessages.BreakpointPackageRenameParticipant_1, changes);
		} else if (changes.length == 1) {
			return changes[0];
		}
		return null;
	}
	
	/**
	 * Returns all breakpoint changes required by renaming the given pacakge.
	 * <p>
	 * When a package is renamed, all breakpoints contained within the package
	 * and subpcakges are affacted.
	 * </p>
	 * @return require changes
	 */
	protected Change[] gatherChanges(IPackageFragment originalPackage, String destPackageName) throws CoreException {
		List changes = new ArrayList();
		String originalPackageName = originalPackage.getElementName();
		IPackageFragmentRoot root = (IPackageFragmentRoot)originalPackage.getParent();
		IResource resource = originalPackage.getResource();
		IMarker[] markers= resource.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = manager.getBreakpoint(marker);
			if (breakpoint instanceof IJavaBreakpoint) {
				IJavaBreakpoint javaBreakpoint = (IJavaBreakpoint) breakpoint;
				IType breakpointType = BreakpointUtils.getType(javaBreakpoint);
				if (breakpointType != null) {
					String breakpointPackageName = breakpointType.getPackageFragment().getElementName();
					String destBreakpointPackageName = destPackageName;
					if (breakpointPackageName.length() > originalPackageName.length()) {
						destBreakpointPackageName += breakpointPackageName.substring(originalPackageName.length());
					}
					IPackageFragment destBreakpointPackage = root.getPackageFragment(destBreakpointPackageName);
					ICompilationUnit cu = destBreakpointPackage.getCompilationUnit(breakpointType.getCompilationUnit().getElementName());
					String[] typeNames = breakpointType.getTypeQualifiedName().split("\\$"); //$NON-NLS-1$
					IType destType = cu.getType(typeNames[0]);
					for (int j = 1; j < typeNames.length; j++) {
						destType = destType.getType(typeNames[j]);
					}
					if (javaBreakpoint instanceof IJavaWatchpoint) {
						changes.add(new WatchpointTypeChange((IJavaWatchpoint) javaBreakpoint, destType));
					} else if (javaBreakpoint instanceof IJavaClassPrepareBreakpoint) {
						changes.add(new ClassPrepareBreakpointTypeChange((IJavaClassPrepareBreakpoint) javaBreakpoint, destType));
					} else if (javaBreakpoint instanceof IJavaMethodBreakpoint) {
						changes.add(new MethodBreakpointTypeChange((IJavaMethodBreakpoint) breakpoint, destType));
					} else if (javaBreakpoint instanceof IJavaExceptionBreakpoint) {
						changes.add(new ExceptionBreakpointTypeChange((IJavaExceptionBreakpoint) javaBreakpoint, destType));
					} else if (javaBreakpoint instanceof IJavaLineBreakpoint) {
						changes.add(new LineBreakpointTypeChange((IJavaLineBreakpoint) javaBreakpoint, destType));
					}
				}
			}
		}
		return (Change[]) changes.toArray(new Change[changes.size()]);
	}		
	
}
