/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	public void addEntry(IClasspathEntry entry, Object beforeEntry) {
		if (!childEntries.contains(entry)) {
			int index = -1;
			if (beforeEntry != null) {
				index = childEntries.indexOf(beforeEntry);
			}
			if (index >= 0) {
				childEntries.add(index, entry);
			} else {
				childEntries.add(entry);
			}
		}
	}

	public void removeEntry(IClasspathEntry entry) {
		childEntries.remove(entry);
	}

	public boolean contains(IClasspathEntry entry) {
		return childEntries.contains(entry);
	}

	@Override
	public String toString() {
		return name;
	}

	public void removeAll() {
		Iterator<IClasspathEntry> iter= childEntries.iterator();
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return false;
	}
}
