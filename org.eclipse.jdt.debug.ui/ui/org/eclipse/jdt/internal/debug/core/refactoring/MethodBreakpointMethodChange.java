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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @since 3.2
 *
 */
public class MethodBreakpointMethodChange extends MethodBreakpointChange {

	private IMethod fDestMethod, fOriginalMethod;
	
	public MethodBreakpointMethodChange(IJavaMethodBreakpoint breakpoint, IMethod destMethod, IMethod originalMethod) throws CoreException {
		super(breakpoint);
		fDestMethod = destMethod;
		fOriginalMethod = originalMethod;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return RefactoringMessages.MethodBreakpointMethodChange_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		Map map = new HashMap();
		BreakpointUtils.addJavaBreakpointAttributes(map, fDestMethod);
		IResource resource = BreakpointUtils.getBreakpointResource(fDestMethod);
		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(
				resource,
				fDestMethod.getDeclaringType().getFullyQualifiedName(),
				fDestMethod.getElementName(),
				fDestMethod.getSignature(),
				isEntry(),
				isExit(),
				isNativeOnly(),
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
