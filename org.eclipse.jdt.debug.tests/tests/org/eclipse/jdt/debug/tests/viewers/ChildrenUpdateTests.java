/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.viewers;

import org.eclipse.debug.internal.ui.viewers.model.ChildrenUpdate;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jface.viewers.TreePath;

/**
 * Tests coalescing of children update requests.
 * 
 * @since 3.3
 */
public class ChildrenUpdateTests extends AbstractDebugTest {
	
	
	/**
	 * @param name
	 */
	public ChildrenUpdateTests(String name) {
		super(name);
	}
	
	/**
	 * Tests coalescing of requests
	 */
	public void testCoalesce () {
		Object element = new Object();
		ChildrenUpdate update1 = new ChildrenUpdate(null, TreePath.EMPTY, element, 1, null, null);
		ChildrenUpdate update2 = new ChildrenUpdate(null, TreePath.EMPTY, element, 2, null, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 2, update1.getLength());
		
		update2 = new ChildrenUpdate(null, TreePath.EMPTY, element, 3, null, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());
		
		update2 = new ChildrenUpdate(null, TreePath.EMPTY, element, 2, null, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());		
		
		update2 = new ChildrenUpdate(null, TreePath.EMPTY, element, 5, null, null);
		assertFalse("Should not coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());
	}
}
