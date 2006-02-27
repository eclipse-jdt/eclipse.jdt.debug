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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * @since 3.2
 *
 */
public class WatchpointTypeChange extends WatchpointChange {
	
	private IType fDestType, fOriginalType;
	
	public WatchpointTypeChange(IJavaWatchpoint watchpoint, IType destType, IType originalType) throws CoreException {
		super(watchpoint);
		fDestType = destType;
		fOriginalType = originalType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		return RefactoringMessages.WatchpointTypeChange_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		IField destField = fDestType.getField(getFieldName());
		Map map = new HashMap();
		BreakpointUtils.addJavaBreakpointAttributes(map, destField);
		IResource resource = BreakpointUtils.getBreakpointResource(destField);
		IJavaWatchpoint breakpoint = JDIDebugModel.createWatchpoint(
				resource,
				fDestType.getFullyQualifiedName(),
				getFieldName(),
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

	public IType getDestinationType() {
		return fDestType;
	}

	public IType getOriginalType() {
		return fOriginalType;
	}

}
