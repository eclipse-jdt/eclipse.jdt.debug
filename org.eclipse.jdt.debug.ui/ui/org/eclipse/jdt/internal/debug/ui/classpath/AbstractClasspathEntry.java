/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractClasspathEntry implements IClasspathEntry {

	protected List<IClasspathEntry> childEntries = new ArrayList<IClasspathEntry>();
	protected IClasspathEntry parent = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#moveChild(int)
	 */
	public void moveChild(boolean up, IClasspathEntry child) {
		int index= childEntries.indexOf(child);
		int direction= 1;
		if (up) {
			direction= -1;
		}
		IClasspathEntry moved= childEntries.get(index+direction);
		childEntries.set(index + direction, child);
		childEntries.set(index, moved);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#getEntries()
	 */
	public IClasspathEntry[] getEntries() {
		return childEntries.toArray(new IClasspathEntry[childEntries.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.IClasspathEntry#hasEntries()
	 */
	public boolean hasEntries() {
		return !childEntries.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.IClasspathEntry#getParent()
	 */
	public IClasspathEntry getParent() {
		return parent;
	}
	
	/**
	 * @param parent The parent to set.
	 */
	public void setParent(IClasspathEntry parent) {
		this.parent = parent;
	}
}
