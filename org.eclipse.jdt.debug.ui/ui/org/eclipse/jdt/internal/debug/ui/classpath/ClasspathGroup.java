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

package org.eclipse.jdt.internal.debug.ui.classpath;

import java.util.Iterator;

public class ClasspathGroup extends AbstractClasspathEntry {
	private String name;
	
	private boolean canBeRemoved= true;
	
	public ClasspathGroup(String name, IClasspathEntry parent, boolean canBeRemoved) {
		this.parent= parent;
		this.name= name;
		this.canBeRemoved= canBeRemoved;
	}
		
	public void addEntry(IClasspathEntry entry) {
		if (!childEntries.contains(entry)) {
			childEntries.add(entry);
		}
	}
	
	public void removeEntry(IClasspathEntry entry) {
		childEntries.remove(entry);
	}
	
	public boolean contains(IClasspathEntry entry) {
		return childEntries.contains(entry);
	}
	
	public String toString() {
		return name;
	}

	public void removeAll() {
		Iterator iter= childEntries.iterator();
		while (iter.hasNext()) {
			Object entry = iter.next();
			if (entry instanceof ClasspathGroup) {
				((ClasspathGroup)entry).removeAll();
			}
		}
		childEntries.clear();
	}
	
	public boolean canBeRemoved() {
		return canBeRemoved;
	}
}
