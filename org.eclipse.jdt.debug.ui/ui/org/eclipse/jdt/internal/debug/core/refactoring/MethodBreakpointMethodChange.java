/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

import com.ibm.icu.text.MessageFormat;

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
	public String getName() {
		return MessageFormat.format(RefactoringMessages.MethodBreakpointMethodChange_0,
				new String[] {getBreakpointLabel(getOriginalBreakpoint()), fDestMethod.getElementName()});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		Map map = new HashMap();
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
