/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching.environments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.osgi.util.NLS;

/**
 * Resolves variables of the form {@code ${ee_home:<id>}}
 */
public class ExecutionEnvironmentVariableResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, EnvironmentMessages.ExecutionEnvironmentVariableResolver_0));
		}
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment env = manager.getEnvironment(argument);
		if (env == null) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, NLS.bind(EnvironmentMessages.ExecutionEnvironmentVariableResolver_1, argument)));
		}
		IPath path = JavaRuntime.newJREContainerPath(env);
		IVMInstall jre = JavaRuntime.getVMInstall(path);
		if (jre == null) {
			throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, NLS.bind(EnvironmentMessages.ExecutionEnvironmentVariableResolver_2, argument)));
		}
		return jre.getInstallLocation().getAbsolutePath();
	}

}
