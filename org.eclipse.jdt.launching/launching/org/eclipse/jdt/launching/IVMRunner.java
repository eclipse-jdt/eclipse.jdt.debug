/*******************************************************************************
 * Copyright (c) 2000, 20018 IBM Corporation and others.
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

package org.eclipse.jdt.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;



/**
 * A VM runner starts a Java VM running a Java program.
 * <p>
 * Clients may implement this interface to launch a new kind of VM.
 * </p>
 */
public interface IVMRunner {

	/**
	 * Launches a Java VM as specified in the given configuration,
	 * contributing results (debug targets and processes), to the
	 * given launch.
	 *
	 * @param configuration the configuration settings for this run
	 * @param launch the launch to contribute to
	 * @param monitor progress monitor or <code>null</code> A cancelable progress monitor is provided by the Job
	 *  framework. It should be noted that the setCanceled(boolean) method should never be called on the provided
	 *  monitor or the monitor passed to any delegates from this method; due to a limitation in the progress monitor
	 *  framework using the setCanceled method can cause entire workspace batch jobs to be canceled, as the canceled flag
	 *  is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been started.
	 * @exception CoreException if an exception occurs while launching
	 */
	public void run(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException;

	/**
	 * Gets the command line required to launch a Java VM as specified in the given configuration, contributing results (debug targets and processes),
	 * to the given launch.
	 *
	 * @param configuration
	 *            the configuration settings for this run
	 * @param launch
	 *            the launch to contribute to
	 * @param monitor
	 *            progress monitor or <code>null</code> A cancelable progress monitor is provided by the Job framework. It should be noted that the
	 *            setCanceled(boolean) method should never be called on the provided monitor or the monitor passed to any delegates from this method;
	 *            due to a limitation in the progress monitor framework using the setCanceled method can cause entire workspace batch jobs to be
	 *            canceled, as the canceled flag is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been
	 *            started.
	 * @return the command line string
	 * @exception CoreException
	 *                if an exception occurs while getting the command line
	 * @since 3.11
	 */
	public default String showCommandLine(VMRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		return ""; //$NON-NLS-1$
	}

}
