/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public String[] getShowInTargetIds() {
		return new String[]{JavaUI.ID_PACKAGES};
	}

}
