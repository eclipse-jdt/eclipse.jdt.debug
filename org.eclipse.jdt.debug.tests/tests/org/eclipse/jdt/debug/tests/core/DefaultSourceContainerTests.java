/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.core;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;

/**
 * Tests default source containers
 */
public class DefaultSourceContainerTests extends AbstractDebugTest {
	
	public DefaultSourceContainerTests(String name) {
		super(name);
	}
	
	/**
	 * Tests creation and restoring from a memento.
	 * 
	 * @throws Exception
	 */
	public void testDefaultSourceContainerMemento() throws Exception {
		ILaunchConfiguration configuration = getLaunchConfiguration("Breakpoints");
		DefaultSourceContainer container = new DefaultSourceContainer(configuration);
		String memento = container.getType().getMemento(container);
		DefaultSourceContainer restore = (DefaultSourceContainer) container.getType().createSourceContainer(memento);
		assertEquals("Default source container memento failed", container, restore);
		assertEquals(configuration, restore.getLaunchConfiguration());
	}			
}
