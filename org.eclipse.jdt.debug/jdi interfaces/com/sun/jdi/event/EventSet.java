/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.event;


import java.util.Collection;

import com.sun.jdi.Mirror;

public interface EventSet extends Mirror , Collection {
	public EventIterator eventIterator();
	public void resume();
	public int suspendPolicy();
}
