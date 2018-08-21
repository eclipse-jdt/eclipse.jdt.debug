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
package org.eclipse.jdt.internal.debug.ui.sourcelookup;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.part.IShowInTargetList;

/**
 * @since 3.2
 *
 */
public class StackFrameShowInTargetListAdapter implements IShowInTargetList {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTargetList#getShowInTargetIds()
	 */
	@Override
	public String[] getShowInTargetIds() {
		return new String[]{JavaUI.ID_PACKAGES};
	}

}
