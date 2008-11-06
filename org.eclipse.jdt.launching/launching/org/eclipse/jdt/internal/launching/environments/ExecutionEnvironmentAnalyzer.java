/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.launching.EEVMInstall;
import org.eclipse.jdt.internal.launching.EEVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.CompatibleEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentAnalyzerDelegate;
/**
 * Environment analyzer for standard execution environments.
 * 
 * @since 3.3
 */
public class ExecutionEnvironmentAnalyzer implements IExecutionEnvironmentAnalyzerDelegate {
	
	private static final String JavaSE_1_6 = "JavaSE-1.6"; //$NON-NLS-1$
	private static final String J2SE_1_5 = "J2SE-1.5"; //$NON-NLS-1$
	private static final String J2SE_1_4 = "J2SE-1.4"; //$NON-NLS-1$
	private static final String J2SE_1_3 = "J2SE-1.3"; //$NON-NLS-1$
	private static final String J2SE_1_2 = "J2SE-1.2"; //$NON-NLS-1$
	private static final String JRE_1_1 = "JRE-1.1"; //$NON-NLS-1$
	
	private static final String CDC_FOUNDATION_1_1 = "CDC-1.1/Foundation-1.1"; //$NON-NLS-1$
	private static final String CDC_FOUNDATION_1_0 = "CDC-1.0/Foundation-1.0"; //$NON-NLS-1$
		
	private static final String JAVA_SPEC_VERSION = "java.specification.version"; //$NON-NLS-1$
	private static final String JAVA_SPEC_NAME = "java.specification.name"; //$NON-NLS-1$
	private static final String JAVA_VERSION = "java.version"; //$NON-NLS-1$
	
	private static final String[] VM_PROPERTIES = {JAVA_SPEC_NAME, JAVA_SPEC_VERSION, JAVA_VERSION};
	private static final String FOUNDATION = "foundation"; //$NON-NLS-1$

	public CompatibleEnvironment[] analyze(IVMInstall vm, IProgressMonitor monitor) throws CoreException {
		ArrayList result = new ArrayList();
		if (!(vm instanceof IVMInstall2))
			return new CompatibleEnvironment[0];
		IVMInstall2 vm2 = (IVMInstall2) vm;
		String eeId = null;
		if (EEVMType.ID_EE_VM_TYPE.equals(vm.getVMInstallType().getId())) {
			eeId = ((EEVMInstall)vm).getAttribute(EEVMInstall.ATTR_EXECUTION_ENVIRONMENT_ID);
		}
		boolean strictMatch = true;
		if (eeId == null) {
			String javaVersion = vm2.getJavaVersion();
			if (javaVersion == null) {
				// We have a contributed VM type. Check to see if its a foundation VM, if we can.
				if ((vm instanceof IVMInstall3) && isFoundation1_0((IVMInstall3) vm)) 
					eeId = CDC_FOUNDATION_1_0;
				else if ((vm instanceof IVMInstall3) && isFoundation1_1((IVMInstall3) vm)) 
					eeId = CDC_FOUNDATION_1_1;
			} else {
				if (javaVersion.startsWith("1.7")) { //$NON-NLS-1$
					eeId = JavaSE_1_6; // there is no 1.7 EE defined yet
					strictMatch = false; // 1.7 is not a strict match for 1.6
				} else if (javaVersion.startsWith("1.6")) //$NON-NLS-1$
					eeId = JavaSE_1_6;
				else if (javaVersion.startsWith("1.5")) //$NON-NLS-1$
					eeId = J2SE_1_5;
				else if (javaVersion.startsWith("1.4")) //$NON-NLS-1$
					eeId = J2SE_1_4;
				else if (javaVersion.startsWith("1.3")) //$NON-NLS-1$
					eeId = J2SE_1_3;
				else if (javaVersion.startsWith("1.2")) //$NON-NLS-1$
					eeId = J2SE_1_2;
				else if (javaVersion.startsWith("1.1")) { //$NON-NLS-1$
					if ((vm instanceof IVMInstall3) && isFoundation1_1((IVMInstall3) vm))
						eeId = CDC_FOUNDATION_1_1;
					else
						eeId = JRE_1_1;
				} else if (javaVersion.startsWith("1.0")) { //$NON-NLS-1$
					if ((vm instanceof IVMInstall3) && isFoundation1_0((IVMInstall3) vm)) 
						eeId = CDC_FOUNDATION_1_0;
				}
			}
		}

		if (eeId != null) {
			IExecutionEnvironment env = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(eeId);
			if (env != null) {
				IExecutionEnvironment[] subEnvironments = env.getSubEnvironments();
				for (int i=0; i < subEnvironments.length; i++)
					addEnvironment(result, subEnvironments[i], false);
				addEnvironment(result, env, strictMatch);
			}
		}
		return (CompatibleEnvironment[])result.toArray(new CompatibleEnvironment[result.size()]);
	}

	/*
	 * Check a couple of known system properties for the word "foundation".
	 */
	private boolean isFoundation(Map properties) {
		for (int i=0; i < VM_PROPERTIES.length; i++) {
			String value = (String) properties.get(VM_PROPERTIES[i]);
			if (value == null)
				continue;
			for (StringTokenizer tokenizer = new StringTokenizer(value); tokenizer.hasMoreTokens(); )
				if (FOUNDATION.equalsIgnoreCase(tokenizer.nextToken()))
					return true;
		}
		return false;
	}

	private boolean isFoundation1_0(IVMInstall3 vm) throws CoreException {
		Map map = vm.evaluateSystemProperties(VM_PROPERTIES, null);
		return isFoundation(map) ? "1.0".equals(map.get(JAVA_SPEC_VERSION)) : false; //$NON-NLS-1$
	}

	private boolean isFoundation1_1(IVMInstall3 vm) throws CoreException {
		Map map = vm.evaluateSystemProperties(VM_PROPERTIES, null);
		return isFoundation(map) ? "1.1".equals(map.get(JAVA_SPEC_VERSION)) : false; //$NON-NLS-1$
	}

	private void addEnvironment(ArrayList result, IExecutionEnvironment env, boolean strict) {
		result.add(new CompatibleEnvironment(env, strict));
	}

}
