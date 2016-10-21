/*******************************************************************************
 * Copyright (c) 2016 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.advanced;

import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.createAdvancedLaunch;
import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.getJavaagentString;
import static org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport.isAdvancedSourcelookupEnabled;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

/**
 * A launch delegate for launching local Java applications with advanced source lookup support.
 *
 * @since 3.10
 * @provisional This is part of work in progress and can be changed, moved or removed without notice
 */
public class AdvancedJavaLaunchDelegate extends JavaLaunchDelegate {

	@Override
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		if (!isAdvancedSourcelookupEnabled()) {
			return super.getVMArguments(configuration);
		}
		// TODO wish we had API similar to zt-exec or at least commons-exec
		return getJavaagentString() + " " + super.getVMArguments(configuration); //$NON-NLS-1$
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		if (!isAdvancedSourcelookupEnabled()) {
			return super.getLaunch(configuration, mode);
		}
		return createAdvancedLaunch(configuration, mode);
	}
}
