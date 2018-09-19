/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * Sample access rule participant.
 *
 * @since 3.3
 */
public class SecondaryAccessRuleParticipant implements IAccessRuleParticipant {

	IAccessRule[] fRules = new IAccessRule[] {
			JavaCore.newAccessRule(new Path("secondary"), IAccessRule.K_DISCOURAGED)
	};

	/**
	 * @see org.eclipse.jdt.launching.environments.IAccessRuleParticipant#getAccessRules(org.eclipse.jdt.launching.environments.IExecutionEnvironment, org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.LibraryLocation[], org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
		IAccessRule[] ar = null;
		if (environment.getId().equals("org.eclipse.jdt.debug.tests.environment.j2se14x")) {
			ar = fRules;
		} else if (environment.getId().equals("org.eclipse.jdt.debug.tests.environment.j2se15x")){
			ar = new IAccessRule[]{JavaCore.newAccessRule(new Path("**/*"), IAccessRule.K_ACCESSIBLE)};
		} else {
			ar = new IAccessRule[0];
		}
		IAccessRule[][] rules = new IAccessRule[libraries.length][];
		for (int i = 0; i < libraries.length; i++) {
			rules[i] = ar;
		}
		return rules;
	}

}
