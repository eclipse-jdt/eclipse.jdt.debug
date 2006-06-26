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
package org.eclipse.jdt.internal.debug.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * This class is used to determine when the VM Capabilities property page should be added to the 
 * debug element properties page
 *
 *	@since 3.3
 */
public class VMCapabilitiesPageTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object arg0, String arg1, Object[] arg2, Object arg3) {
		IDebugTarget target = null;
		if(arg0 instanceof IProcess) {
			target = (IDebugTarget) ((IProcess)arg0).getAdapter(IDebugTarget.class);
		}
		else if(arg0 instanceof IDebugElement) {
			target = (IDebugTarget) ((IDebugElement)arg0).getAdapter(IDebugTarget.class);
		}
		if(target != null) {
			if(!target.isTerminated() && !target.isDisconnected()) {
				if(target instanceof JDIDebugTarget) {
					return true;
				}
			}
		}
		return false;
	}

}
