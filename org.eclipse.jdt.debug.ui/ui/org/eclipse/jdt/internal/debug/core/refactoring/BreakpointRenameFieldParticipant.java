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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;

/**
 * Breakpoint participant for field rename.
 * 
 * @since 3.2
 */
public class BreakpointRenameFieldParticipant extends BreakpointRenameParticipant {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#accepts(org.eclipse.jdt.core.IJavaElement)
	 */
	protected boolean accepts(IJavaElement element) {
		return element instanceof IField;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.refactoring.BreakpointRenameParticipant#gatherChanges(org.eclipse.core.resources.IMarker[], java.util.List, java.lang.String)
	 */
	protected void gatherChanges(IMarker[] markers, List changes, String destFieldName) throws CoreException, OperationCanceledException {
		IField originalField = (IField) getOriginalElement();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IBreakpoint breakpoint = getBreakpoint(marker);
			if (breakpoint instanceof IJavaWatchpoint) {
				IJavaWatchpoint watchpoint = (IJavaWatchpoint) breakpoint;
				IType breakpointType = BreakpointUtils.getType(watchpoint);
				if (breakpointType != null && originalField.getDeclaringType().equals(breakpointType)) {
					IField destField = originalField.getDeclaringType().getField(destFieldName);
					changes.add(new WatchpointFieldChange(watchpoint, destField));
				}
			}
		}
	}


}
