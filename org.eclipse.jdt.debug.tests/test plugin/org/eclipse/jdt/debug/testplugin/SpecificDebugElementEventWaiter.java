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
import org.eclipse.debug.core.model.IDebugElement;

/**
 * This event waiter is used to wait for a certain type of event (create, terminate, suspend, etc.) 
 * on a *specific* debug element.  Contrast this with DebugElementKindEventWaiter which is similar, 
 * but is used to wait for a certain type of event on a *kind* of debug element (thread, debug target, etc.)
 */
public class SpecificDebugElementEventWaiter extends DebugEventWaiter {

	protected IDebugElement fDebugElement;
	
	public SpecificDebugElementEventWaiter(int eventKind, IDebugElement element) {
		super(eventKind);
		fDebugElement = element;
	}
	
	public boolean accept(DebugEvent event) {
		Object o = event.getSource();
		if (o instanceof IDebugElement) {
			return super.accept(event) && ((IDebugElement)o).equals(fDebugElement);
		} else {
			return false;
		}
	}


}