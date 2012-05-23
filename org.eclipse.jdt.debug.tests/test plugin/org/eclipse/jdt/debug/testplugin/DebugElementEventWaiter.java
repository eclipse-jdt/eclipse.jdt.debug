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

/**
 * Waits for an event on a specific element
 */
public class DebugElementEventWaiter extends DebugEventWaiter {
	
	protected Object fElement;
	
	/**
	 * Constructor
	 * @param kind
	 * @param element
	 */
	public DebugElementEventWaiter(int kind, Object element) {
		super(kind);
		fElement = element;
	}
	
	/**
	 * @see org.eclipse.jdt.debug.testplugin.DebugEventWaiter#accept(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fElement == event.getSource();
	}

}
