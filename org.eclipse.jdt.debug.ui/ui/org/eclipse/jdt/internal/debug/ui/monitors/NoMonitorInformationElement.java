/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Dummy element for display when a VM doesn't support monitor information
 */
public class NoMonitorInformationElement extends DebugElement {

    public NoMonitorInformationElement(IDebugTarget target) {
    	super(target);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
     */
    @Override
	public String getModelIdentifier() {
        return getDebugTarget().getModelIdentifier();
    }

}
