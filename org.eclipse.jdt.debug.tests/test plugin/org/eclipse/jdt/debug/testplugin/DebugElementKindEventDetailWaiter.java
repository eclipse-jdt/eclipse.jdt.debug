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
 * Wait for the specified event with the specified from the specified element.
 */
public class DebugElementKindEventDetailWaiter extends DebugElementKindEventWaiter {

	protected int fDetail;

	public DebugElementKindEventDetailWaiter(int eventKind, Class elementClass, int detail) {
		super(eventKind, elementClass);
		fDetail = detail;
	}
	
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fDetail == event.getDetail();
	}
	
}
