/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.logicalstructures;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILogicalStructureTypeDelegate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IEvaluationRunnable;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Common facilities for logical structure types for instances of an interface
 */
public abstract class LogicalObjectStructureInterfaceType implements ILogicalStructureTypeDelegate {
	
	private IJavaObject fObject; // the map to provide structure for
	
	private IValue fResult; // the resulting structure
	
	private boolean fDone = false; // done the evaluation
	
	private static IStatus fgNeedThread = new Status(IStatus.INFO, JDIDebugPlugin.getUniqueIdentifier(), LogicalObjectStructureInterfaceType.INFO_EVALUATION_THREAD, "Provides thread context for an evaluation", null); //$NON-NLS-1$
	private static IStatusHandler fgThreadProvider;

	/**
	 * Status code used by the debug model to retrieve a thread to use
	 * for evalutaions, via a status handler. A status handler is contributed by
	 * the Java debug UI. When not present, the debug model uses any suspended thread.
	 * 
	 * @since 3.0 
	 */
	public static final int INFO_EVALUATION_THREAD = 110;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureType#providesLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public boolean providesLogicalStructure(IValue value) {
		if (value instanceof IJavaObject) {
			IJavaObject object = (IJavaObject) value;
			try {
				IJavaType type = object.getJavaType();
				if (type instanceof IJavaClassType) {
					IJavaClassType classType = (IJavaClassType) type;
					IJavaInterfaceType[] interfaceTypes = classType.getAllInterfaces();
					String targetInterface = getTargetInterfaceName();
					for (int i = 0; i < interfaceTypes.length; i++) {
						IJavaInterfaceType inter = interfaceTypes[i];
						if (inter.getName().equals(targetInterface)) {
							return true;
						}
					}
				}
			} catch (DebugException e) {
			}
		}
		return false;
	}
	
	/**
	 * Returns the name of an interface that an object must implement for this
	 * structure type to be appropriate.
	 * 
	 * @return the name of an interface that an object must implement for this
	 * structure type to be appropriate
	 */
	protected abstract String getTargetInterfaceName();
	
	/**
	 * Returns the evaluation that computes the logical object structure for this
	 * strucutre type.
	 * 
	 * @return the evaluation that computes the logical object structure for this
	 * strucutre type
	 */
	protected abstract IEvaluationRunnable getEvaluation();

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILogicalStructureType#getLogicalStructure(org.eclipse.debug.core.model.IValue)
	 */
	public synchronized IValue getLogicalStructure(IValue value) throws CoreException {
		final IJavaThread thread = getThread(value);
		if (thread == null) {
			// can't do it
			throw new CoreException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), JDIDebugPlugin.INTERNAL_ERROR, LogicalStructuresMessages.LogicalObjectStructureType_1, null)); //$NON-NLS-1$
		}
		setObject((IJavaObject)value);
		final IEvaluationRunnable evaluation = getEvaluation();
		final CoreException[] ex = new CoreException[1];
		final Object lock = this;
		fDone = false;
		if (thread.isPerformingEvaluation() && thread.isSuspended()) {
			return value;
		}
		thread.queueRunnable(new Runnable() {
			public void run() {
				try {
					thread.runEvaluation(evaluation, null, DebugEvent.EVALUATION_IMPLICIT, false);
				} catch (DebugException e) {
					ex[0] = e;
				}
				synchronized (lock) {
					fDone = true;
					lock.notifyAll();
				}
			}
		});
		try {
			synchronized (lock) {
				if (!fDone) {
					lock.wait();
				}
			}
		} catch (InterruptedException e) {
		}
		if (ex[0] != null) {
			throw ex[0];
		}
		return fResult;
	}

	private IJavaThread getThread(IValue value) throws CoreException {
		IStatusHandler handler = getThreadProvider();
		if (handler != null) {
			IJavaThread thread = (IJavaThread)handler.handleStatus(fgNeedThread, value);
			if (thread != null) {
				return thread;
			}
		}
		IDebugTarget target = value.getDebugTarget();
		IJavaDebugTarget javaTarget = (IJavaDebugTarget) target.getAdapter(IJavaDebugTarget.class);
		if (javaTarget != null) {
			IThread[] threads = javaTarget.getThreads();
			for (int i = 0; i < threads.length; i++) {
				IThread thread = threads[i];
				if (thread.isSuspended()) {
					return (IJavaThread)thread;
				}
			}
		}
		return null;
	}
	
	private static IStatusHandler getThreadProvider() {
		if (fgThreadProvider == null) {
			fgThreadProvider = DebugPlugin.getDefault().getStatusHandler(fgNeedThread);
		}
		return fgThreadProvider;
	}

	/**
	 * Sets the object for which a logical structure is to be provided.
	 * 
	 * @param object the object for which a logical structure is to be provided
	 */
	private void setObject(IJavaObject object) {
		fObject = object;
	}
	
	/**
	 * Returns the object for which a logical structure is to be provided
	 * 
	 * @return the object for which a logical structure is to be provided
	 */
	protected IJavaObject getObject() {
		return fObject;
	}
	
	/**
	 * Sets the object representing the logical structure.
	 *  
	 * @param result the object representing the logical structure
	 */
	protected void setLogicalStructure(IValue result) {
		fResult = result;
	}
	
}
