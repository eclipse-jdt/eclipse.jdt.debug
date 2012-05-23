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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class MethodBreakpointMethodChange extends MethodBreakpointChange {

	private IMethod fDestMethod;
	
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
				new String[] {getBreakpointLabel(getOriginalBreakpoint()), fDestMethod.getElementName()});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		Map<String, Object> map = new HashMap<String, Object>();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestMethod);
		IResource resource = BreakpointUtils.getBreakpointResource(fDestMethod);
		int range[] = getNewLineNumberAndRange(fDestMethod);
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
