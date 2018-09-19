/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

/**
 * SampleContextVariableResolver
 */
public class SampleDynamicVariableResolver implements IDynamicVariableResolver {

	/**
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) {
		if (argument == null) {
			return "no arg";
		}
		return "the arg is " + argument;
	}
}
