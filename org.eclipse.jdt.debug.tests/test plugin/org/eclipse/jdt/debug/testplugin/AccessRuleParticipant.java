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
public class AccessRuleParticipant implements IAccessRuleParticipant {

	IAccessRule[] fRules = new IAccessRule[] {
			JavaCore.newAccessRule(new Path("discouraged"), IAccessRule.K_DISCOURAGED),
			JavaCore.newAccessRule(new Path("accessible"), IAccessRule.K_ACCESSIBLE),
			JavaCore.newAccessRule(new Path("non_accessible"), IAccessRule.K_NON_ACCESSIBLE),
			};

	/**
	 * @see org.eclipse.jdt.launching.environments.IAccessRuleParticipant#getAccessRules(org.eclipse.jdt.launching.environments.IExecutionEnvironment, org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.LibraryLocation[], org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
		IAccessRule[][] rules = new IAccessRule[libraries.length][];
		for (int i = 0; i < libraries.length; i++) {
			rules[i] = fRules;
		}
		return rules;
	}

}
