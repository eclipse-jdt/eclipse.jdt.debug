/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;

/**
 * ExpressionWaiter
 */
public class ExpressionWaiter extends DebugElementEventWaiter {

	/**
	 * Constructor
	 * @param kind
	 * @param element
	 */
	public ExpressionWaiter(int kind, Object element) {
		super(kind, element);
	}
	
	/**
	 * @see org.eclipse.jdt.debug.testplugin.DebugElementEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	public boolean accept(DebugEvent event) {
		if (event.getDetail() == DebugEvent.STATE) {
			return false;
		}
		IExpression expression = (IExpression)fElement;
		boolean pending = false;
		if (expression instanceof IWatchExpression) {
		    pending = ((IWatchExpression)expression).isPending();
		}
        return super.accept(event) && !pending && expression.getValue() != null;
	}
}
