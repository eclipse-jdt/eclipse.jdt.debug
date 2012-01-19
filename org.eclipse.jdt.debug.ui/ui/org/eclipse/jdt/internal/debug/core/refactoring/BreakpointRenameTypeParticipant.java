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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.RenameTypeArguments;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Breakpoint participant for type rename.
 * 
 * @since 3.2
 */
public class BreakpointRenameTypeParticipant extends BreakpointRenameParticipant {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#accepts(org.eclipse.jdt.core.IJavaElement)
     */
    @Override
	protected boolean accepts(IJavaElement element) {
        return element instanceof IType;
    }
    
	@Override
	protected Change createTypeChange(IJavaBreakpoint breakpoint, IType destType, IType originalType) throws CoreException {
		if (breakpoint instanceof IJavaWatchpoint) {
			return new WatchpointTypeRenameChange((IJavaWatchpoint) breakpoint, destType, originalType, getProcessor(), (RenameTypeArguments) getArguments());
		}
		return super.createTypeChange(breakpoint, destType, originalType);
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#gatherChanges(org.eclipse.core.resources.IMarker[],
     *      java.util.List, java.lang.String)
     */
    @Override
	protected void gatherChanges(IMarker[] markers, List<Change> changes, String simpleDestName) throws CoreException, OperationCanceledException {
        IType originalType = (IType) getOriginalElement();
        ICompilationUnit originalCU = originalType.getCompilationUnit();
        ICompilationUnit destCU = null;

        IType primaryType = originalCU.findPrimaryType();
        if (originalType.isMember() || primaryType == null || !primaryType.equals(originalType)) {
            destCU = originalCU;
        } else if (primaryType.equals(originalType)) {
            String ext = ".java"; //$NON-NLS-1$
            // assume extension is same as original
            IResource res = originalCU.getResource();
            if (res != null) {
                ext = '.' + res.getFileExtension();
            }
            destCU = originalType.getPackageFragment().getCompilationUnit(simpleDestName + ext);
        }
        
        // newType is the type that is changing - it may contain nested members with breakpoints
        IType newType = BreakpointChange.getType(originalType.getParent(), simpleDestName);
        newType = (IType) BreakpointChange.findElement(destCU, newType);
        

        for (int i = 0; i < markers.length; i++) {
            IMarker marker = markers[i];
            IBreakpoint breakpoint = getBreakpoint(marker);
            if (breakpoint instanceof IJavaBreakpoint) {
                IJavaBreakpoint javaBreakpoint = (IJavaBreakpoint) breakpoint;
                IType breakpointType = BreakpointUtils.getType(javaBreakpoint);
                IType destType = null;
                if (breakpointType != null) {
                	IJavaElement element = null;
                	if (isContained(originalType, breakpointType)) {
                		element = BreakpointChange.findElement(newType, breakpointType);
                	} else if (isContained(originalCU, breakpointType)) {
                		// non public, or other type in the CU
                		element = BreakpointChange.findElement(destCU, breakpointType);
                	}
                	if (element instanceof IType) {
                		destType = (IType) element;
                		changes.add(createTypeChange(javaBreakpoint, destType, breakpointType));
                	}
                }
                	
            }
        }

    }

}
