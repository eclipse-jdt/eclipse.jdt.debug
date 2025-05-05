/*******************************************************************************
 *  Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 */
public class MethodBreakpointTypeChange extends MethodBreakpointChange {

	private final IType fDestType;

	public MethodBreakpointTypeChange(IJavaMethodBreakpoint breakpoint, IType destType) throws CoreException {
		super(breakpoint);
		fDestType = destType;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		String msg = NLS.bind(RefactoringMessages.MethodBreakpointTypeChange_1, getBreakpointLabel(getOriginalBreakpoint()));
		if(!"".equals(fDestType.getElementName())) { //$NON-NLS-1$
			msg = NLS.bind(RefactoringMessages.MethodBreakpointTypeChange_0,
					getBreakpointLabel(getOriginalBreakpoint()), fDestType.getElementName());
		}
		return msg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		String[] parameterTypes = Signature.getParameterTypes(getSignature());
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = parameterTypes[i].replace('/', '.');

		}
		IMethod destMethod = fDestType.getMethod(getMethodName(), parameterTypes);
		if (!destMethod.exists()) {
			// find the similar method, as source methods have unqualified signatures
			IMethod[] methods = fDestType.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].isSimilar(destMethod)) {
					destMethod = methods[i];
					break;
				}
			}
		}
		Map<String, Object> map = new HashMap<>();
		BreakpointUtils.addJavaBreakpointAttributes(map, destMethod);
		map.put(IInternalDebugUIConstants.WORKING_SET_NAME, getOriginalWorkingSets());// creating breakpoint in the original working set
		IResource resource = BreakpointUtils.getBreakpointResource(destMethod);
		int[] range = getNewLineNumberAndRange(destMethod);
		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(
				resource,
				fDestType.getFullyQualifiedName(),
				getMethodName(),
				getSignature(),
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
