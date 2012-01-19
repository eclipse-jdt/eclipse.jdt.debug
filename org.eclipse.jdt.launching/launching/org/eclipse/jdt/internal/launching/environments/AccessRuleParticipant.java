/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.environments;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.environments.IAccessRuleParticipant;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * Proxy to an access rule participant for an execution environment.
 * 
 * @since 3.3
 */
class AccessRuleParticipant implements IAccessRuleParticipant {

	private IConfigurationElement fElement;
	
	private IAccessRuleParticipant fDelegate;
	
	/**
	 * Constructs a proxy to a rule participant contributed with the 
	 * given configuration element. The element may be an
	 * <code>executionEnvironment</code> element or a <code>ruleParticipant</code>
	 * extension.
	 * 
	 * @param element the configuration element
	 */
	AccessRuleParticipant(IConfigurationElement element) {
		fElement = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.environments.IAccessRuleParticipant#getAccessRules(org.eclipse.jdt.launching.environments.IExecutionEnvironment, org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.LibraryLocation[], org.eclipse.jdt.core.IJavaProject)
	 */
	public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
		try {
			return getDelegate().getAccessRules(environment, vm, libraries, project);
		} catch (CoreException e) {
			LaunchingPlugin.log(e.getStatus());
		}
		IAccessRule[][] rules = new IAccessRule[libraries.length][];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = new IAccessRule[0];
		}
		return rules;
	}
	
	private IAccessRuleParticipant getDelegate() throws CoreException {
		if (fDelegate == null) {
			if (fElement.getName().equals(EnvironmentsManager.ENVIRONMENT_ELEMENT)) {
				fDelegate = (IAccessRuleParticipant) fElement.createExecutableExtension(EnvironmentsManager.RULE_PARTICIPANT_ELEMENT);
			} else {
				fDelegate = (IAccessRuleParticipant) fElement.createExecutableExtension("class"); //$NON-NLS-1$
			}
		}
		return fDelegate;
	}
	
	/**
	 * Returns the id of this participant.
	 * 
	 * @return participant id
	 */
	String getId() {
		if (fElement.getName().equals(EnvironmentsManager.ENVIRONMENT_ELEMENT)) {
			return fElement.getAttribute(EnvironmentsManager.RULE_PARTICIPANT_ELEMENT);
		}
		return fElement.getAttribute("id"); //$NON-NLS-1$
	}
	
	private String getDelegateClassName() {
		if (fElement.getName().equals(EnvironmentsManager.ENVIRONMENT_ELEMENT)) {
			return fElement.getAttribute(EnvironmentsManager.RULE_PARTICIPANT_ELEMENT);
		}
		return fElement.getAttribute("class"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessRuleParticipant) {
			AccessRuleParticipant participant = (AccessRuleParticipant) obj;
			return participant.getDelegateClassName().equals(getDelegateClassName());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getDelegateClassName().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDelegateClassName();
	}
}
