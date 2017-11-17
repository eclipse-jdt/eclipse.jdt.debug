/*******************************************************************************
 * Copyright (c) 2011-2016 Igor Fedorenko
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
