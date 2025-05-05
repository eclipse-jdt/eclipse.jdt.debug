/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 */
public class LineBreakpointTypeChange extends LineBreakpointChange {

	private final IType fDestType;

	public LineBreakpointTypeChange(IJavaLineBreakpoint breakpoint, IType destType) throws CoreException {
		super(breakpoint);
		fDestType = destType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		String msg = NLS.bind(RefactoringMessages.LineBreakpointTypeChange_1, getBreakpointLabel(getOriginalBreakpoint()));
		if(!"".equals(fDestType.getElementName())) { //$NON-NLS-1$
 			msg = NLS.bind(RefactoringMessages.LineBreakpointTypeChange_0,
					getBreakpointLabel(getOriginalBreakpoint()), fDestType.getElementName());
		}
		return msg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		IResource resource = BreakpointUtils.getBreakpointResource(fDestType);
		Map<String, Object> map = new HashMap<>();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestType);
		map.put(IInternalDebugUIConstants.WORKING_SET_NAME, getOriginalWorkingSets());// creating breakpoint in the original working set
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
