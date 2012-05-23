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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class LineBreakpointTypeChange extends LineBreakpointChange {
	
	private IType fDestType;
	
	public LineBreakpointTypeChange(IJavaLineBreakpoint breakpoint, IType destType) throws CoreException {
		super(breakpoint);
		fDestType = destType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		String msg =  NLS.bind(RefactoringMessages.LineBreakpointTypeChange_1, new String[] {getBreakpointLabel(getOriginalBreakpoint())});
		if(!"".equals(fDestType.getElementName())) { //$NON-NLS-1$
 			msg = NLS.bind(RefactoringMessages.LineBreakpointTypeChange_0,
				new String[] {getBreakpointLabel(getOriginalBreakpoint()), fDestType.getElementName()});
		}
		return msg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		IResource resource = BreakpointUtils.getBreakpointResource(fDestType);
		Map<String, Object> map = new HashMap<String, Object>();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestType);
		IJavaLineBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(
				resource,
				fDestType.getFullyQualifiedName(),
				getLineNumber(),
				getCharStart(), 
				getCharEnd(),
				0, 
				true, 
				map);
		apply(breakpoint);
		getOriginalBreakpoint().delete();
		return new DeleteBreakpointChange(breakpoint);
	}

}
