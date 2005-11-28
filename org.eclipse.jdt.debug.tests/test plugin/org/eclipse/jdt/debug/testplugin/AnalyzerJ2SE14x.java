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
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzerDelegate;

/**
 * @since 3.2
 * 
 */
public class AnalyzerJ2SE14x implements IExecutionEnvironmentAnalyzerDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzer#analyze(org.eclipse.jdt.launching.IVMInstall,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IExecutionEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		if (vm instanceof IVMInstall2) {
			IVMInstall2 vm2 = (IVMInstall2) vm;
			String javaVersion = vm2.getJavaVersion();
			if (javaVersion.startsWith("1.4")) {
				return new IExecutionEnvironment[] { JavaRuntime.getExecutionEnvironmentsManager()
						.getEnvironment("org.eclipse.jdt.debug.tests.environment.j2se14x") };
			}
		}
		return new IExecutionEnvironment[0];
	}

}
