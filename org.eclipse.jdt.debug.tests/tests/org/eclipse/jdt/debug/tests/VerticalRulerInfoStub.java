/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests;

import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class VerticalRulerInfoStub implements IVerticalRulerInfo {

	private int fLineNumber = -1;

	public VerticalRulerInfoStub(int line) {
		fLineNumber = line;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getControl()
	 */
	@Override
	public Control getControl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	@Override
	public int getLineOfLastMouseButtonActivity() {
		return fLineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getWidth()
	 */
	@Override
	public int getWidth() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	@Override
	public int toDocumentLineNumber(int y_coordinate) {
		return 0;
	}

}
