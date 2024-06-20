/*******************************************************************************
 *  Copyright (c) 2006, 2018 IBM Corporation and others.
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
package org.eclipse.jdt.internal.launching.environments;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.osgi.framework.Constants;

/**
 * Creates default access rules for standard execution environments
 * based on OSGi profiles.
 *
 * @since 3.3
 */
public class DefaultAccessRuleParticipant implements IAccessRuleParticipant {

	/**
	 * Cache of access rules per environment. Re-use rules between projects.
	 */
	private static Map<String, IAccessRule[][]> fgRules = new HashMap<>();

	@Override
	public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
		Map<String, String> complianceOptions = environment.getComplianceOptions();
		if (complianceOptions != null) {
			String compliance = complianceOptions.get(JavaCore.COMPILER_COMPLIANCE);
			if (JavaCore.compareJavaVersions(compliance, "9") >= 0) { //$NON-NLS-1$
				return new IAccessRule[0][]; // in 9+ access rules are superseded by limit-modules
			}
		}
		IAccessRule[][] allRules = fgRules.get(environment.getId());
		if (allRules == null || allRules.length != libraries.length) {
			// if a different number of libraries, create a new set of rules
			String[] packages = retrieveSystemPackages(environment);
			IAccessRule[] packageRules;
			if (packages.length > 0) {
				packageRules = new IAccessRule[packages.length + 1];
				for (int i = 0; i < packages.length; i++) {
					IPath pattern = IPath.fromPortableString(packages[i].replace('.', IPath.SEPARATOR));
					packageRules[i] = JavaCore.newAccessRule(pattern, IAccessRule.K_ACCESSIBLE);
				}
				// add IGNORE_IF_BETTER flag in case another explicit entry allows access (see bug 228488)
				packageRules[packages.length] = JavaCore.newAccessRule(ExecutionEnvironment.ALL_PATTERN, IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER);
			} else {
				packageRules = new IAccessRule[0];
			}
			allRules = new IAccessRule[libraries.length][];
			for (int i = 0; i < allRules.length; i++) {
				allRules[i] = packageRules;
			}
			fgRules.put(environment.getId(), allRules);
		}
		return allRules;
	}

	private static final Pattern COMMA = Pattern.compile(","); //$NON-NLS-1$

	private String[] retrieveSystemPackages(IExecutionEnvironment environment) {
		Properties profile = environment.getProfileProperties();
		if (profile != null) {
			String packages = profile.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES);
			if (packages != null) {
				return Stream.concat(Stream.of("java.**"), //$NON-NLS-1$
						COMMA.splitAsStream(packages).map(p -> p.trim() + ".*"))//$NON-NLS-1$
						.toArray(String[]::new);
			}
		}
		return new String[0];
	}
}
