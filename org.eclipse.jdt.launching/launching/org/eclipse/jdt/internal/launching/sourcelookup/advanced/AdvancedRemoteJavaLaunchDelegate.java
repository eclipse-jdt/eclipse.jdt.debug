/*******************************************************************************
 * Copyright (c) 2011-2016 Igor Fedorenko
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

import org.eclipse.jdt.internal.launching.JavaRemoteApplicationLaunchConfigurationDelegate;

public class AdvancedRemoteJavaLaunchDelegate extends JavaRemoteApplicationLaunchConfigurationDelegate {

	public AdvancedRemoteJavaLaunchDelegate() {
		allowAdvancedSourcelookup();
	}

}
