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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaClassPrepareBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @since 3.2
 *
 */
public class ClassPrepareBreakpointTypeChange extends ClassPrepareBreakpointChange {
	
	private IType fDestType;
	private int fStart, fEnd;

	public ClassPrepareBreakpointTypeChange(IJavaClassPrepareBreakpoint breakpoint, IType destType) throws CoreException {
		super(breakpoint);
		fDestType = destType;
		fStart = breakpoint.getMarker().getAttribute(IMarker.CHAR_START, -1);
		fEnd = breakpoint.getMarker().getAttribute(IMarker.CHAR_END, -1);	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return RefactoringMessages.ClassPrepareBreakpointTypeChange_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		IResource resource = BreakpointUtils.getBreakpointResource(fDestType);
		Map map = new HashMap();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestType);
		// TODO - start/end should be adjusted, but can access new CU from model yet
		IJavaClassPrepareBreakpoint breakpoint = JDIDebugModel.createClassPrepareBreakpoint(
				resource,
				fDestType.getFullyQualifiedName(),
				getMemberType(),
				fStart, 
				fEnd,
				true,
				map);
		apply(breakpoint);
		getOriginalBreakpoint().delete();
		// TODO: undo
		return null;
	}

}
