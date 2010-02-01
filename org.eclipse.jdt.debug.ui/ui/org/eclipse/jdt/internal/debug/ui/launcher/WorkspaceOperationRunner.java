/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.texteditor.ISchedulingRuleProvider;

/**
 * @since 3.0
 */
class WorkspaceOperationRunner implements IRunnableContext {
	
	private IProgressMonitor fProgressMonitor;
	
	public WorkspaceOperationRunner() {
	}
	
	/**
	 * Sets the progress monitor.
	 * 
	 * @param progressMonitor the progress monitor to set
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		fProgressMonitor= progressMonitor;
	}

	/**
	 * Returns the progress monitor. It there is no progress monitor the monitor\
	 * is set to the <code>NullProgressMonitor</code>.
	 * 
	 * @return the progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		if (fProgressMonitor == null)
			fProgressMonitor= new NullProgressMonitor();
		return fProgressMonitor;
	}

	/*
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (runnable instanceof ISchedulingRuleProvider)
			run(fork, cancelable, runnable, ((ISchedulingRuleProvider)runnable).getSchedulingRule());
		else
			run(fork, cancelable, runnable, ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Runs a new {@link WorkspaceModifyDelegatingOperation}
	 * @param fork
	 * @param cancelable
	 * @param runnable
	 * @param schedulingRule
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable, ISchedulingRule schedulingRule) throws InvocationTargetException, InterruptedException {
		WorkspaceModifyDelegatingOperation operation= new WorkspaceModifyDelegatingOperation(runnable, schedulingRule);
		operation.run(getProgressMonitor());
	}
}
