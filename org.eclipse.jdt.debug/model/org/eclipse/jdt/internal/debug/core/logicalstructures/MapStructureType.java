/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Logical strucuture type for maps.
 */
public class MapStructureType extends LogicalObjectStructureInterfaceType {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.logicalstructures.LogicalObjectStructureType#getEvaluation()
	 */
	protected IEvaluationRunnable getEvaluation() {
		return new IEvaluationRunnable() {
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.debug.core.IEvaluationRunnable#run(org.eclipse.jdt.debug.core.IJavaThread, org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IJavaThread thread, IProgressMonitor monitor) throws DebugException {
				IJavaObject entrySet = (IJavaObject) getObject().sendMessage("entrySet", "()Ljava/util/Set;", null, thread, false);  //$NON-NLS-1$//$NON-NLS-2$
				setLogicalStructure(entrySet.sendMessage("toArray", "()[Ljava/lang/Object;", null, thread, false)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.core.logicalstructures.LogicalObjectStructureType#getTargetInterfaceName()
	 */
	protected String getTargetInterfaceName() {
		return "java.util.Map"; //$NON-NLS-1$
	}

}
