/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IExpression;

/**
 * ExpressionWaiter
 */
public class ExpressionWaiter extends DebugElementEventWaiter {

	public ExpressionWaiter(int kind, Object element) {
		super(kind, element);
	}
	
	public boolean accept(DebugEvent event) {
		return super.accept(event) && ((IExpression)fElement).getValue() != null;
	}
}
