/*******************************************************************************
 * Copyright (c) 2015-2016 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.sourcelookup.advanced;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

/**
 * {@code sourcelookup_javaagent} dynamic variable resolver.
 */
public class JavaagentVariableResolver implements IDynamicVariableResolver {
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		return AdvancedSourceLookupSupport.getJavaagentLocation();
	}
}
