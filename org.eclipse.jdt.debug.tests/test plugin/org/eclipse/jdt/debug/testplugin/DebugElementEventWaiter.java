package org.eclipse.jdt.debug.testplugin;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.debug.core.DebugEvent;

/**
 * Waits for an event on a specific element
 */

public class DebugElementEventWaiter extends DebugEventWaiter {
	
	protected Object fElement;
	
	public DebugElementEventWaiter(int kind, Object element) {
		super(kind);
		fElement = element;
	}
	
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fElement == event.getSource();
	}

}