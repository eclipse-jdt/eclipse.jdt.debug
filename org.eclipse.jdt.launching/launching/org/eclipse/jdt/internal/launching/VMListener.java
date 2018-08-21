/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.PropertyChangeEvent;

/**
 * Simple VM listener that reports whether VM settings have changed.
 *
 * @since 3.2
 *
 */
public class VMListener implements IVMInstallChangedListener {

	private boolean fChanged = false;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.eclipse.jdt.launching.IVMInstall, org.eclipse.jdt.launching.IVMInstall)
	 */
	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		fChanged = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmAdded(org.eclipse.jdt.launching.IVMInstall)
	 */
	@Override
	public void vmAdded(IVMInstall vm) {
		fChanged = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmChanged(org.eclipse.jdt.launching.PropertyChangeEvent)
	 */
	@Override
	public void vmChanged(PropertyChangeEvent event) {
		fChanged = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IVMInstallChangedListener#vmRemoved(org.eclipse.jdt.launching.IVMInstall)
	 */
	@Override
	public void vmRemoved(IVMInstall vm) {
		fChanged = true;
	}

	public boolean isChanged() {
		return fChanged;
	}

}
