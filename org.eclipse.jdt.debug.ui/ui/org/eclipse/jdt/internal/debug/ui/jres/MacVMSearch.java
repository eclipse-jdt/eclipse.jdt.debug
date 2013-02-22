/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.jres;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.internal.launching.MacInstalledJREs;
import org.eclipse.jdt.launching.VMStandin;

/**
 * Searches for installed JREs on the MAC, in known location.
 */
public class MacVMSearch {

	/**
	 * Returns an array of {@link VMStandin}s found at the standard Mac OS location
	 * or an empty listing, never <code>null</code>
	 * @param monitor
	 * @return a listing of {@link VMStandin}s at the standard Mac OS location or an empty listing
	 */
	public VMStandin[] search(IProgressMonitor monitor) {
		SubMonitor localmonitor = SubMonitor.convert(monitor, JREMessages.MacVMSearch_0, 5);
		try {
			return MacInstalledJREs.getInstalledJREs(localmonitor);
		}
		catch(CoreException ce) {
			return MacInstalledJREs.NO_VMS;
		}
	}
}
