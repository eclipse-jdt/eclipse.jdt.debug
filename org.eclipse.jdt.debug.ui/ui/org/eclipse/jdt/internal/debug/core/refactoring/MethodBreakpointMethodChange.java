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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 */
public class MethodBreakpointMethodChange extends MethodBreakpointChange {

	private final IMethod fDestMethod;

	public MethodBreakpointMethodChange(IJavaMethodBreakpoint breakpoint, IMethod destMethod) throws CoreException {
		super(breakpoint);
		fDestMethod = destMethod;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		return NLS.bind(RefactoringMessages.MethodBreakpointMethodChange_0,
				getBreakpointLabel(getOriginalBreakpoint()), fDestMethod.getElementName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		Map<String, Object> map = new HashMap<>();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestMethod);
		IResource resource = BreakpointUtils.getBreakpointResource(fDestMethod);
		int range[] = getNewLineNumberAndRange(fDestMethod);
		map.put(IInternalDebugUIConstants.WORKING_SET_NAME, getOriginalWorkingSets());
		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(
				resource,
				fDestMethod.getDeclaringType().getFullyQualifiedName(),
				fDestMethod.getElementName(),
				fDestMethod.getSignature(),
				isEntry(),
				isExit(),
				isNativeOnly(),
				NO_LINE_NUMBER,
				range[1],
				range[2],
				getHitCount(),
				true,
				map);
		apply(breakpoint);
		getOriginalBreakpoint().delete();
		return new DeleteBreakpointChange(breakpoint);
	}

}
