/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class DependencyModel extends ClasspathModel {

	public static final int MODULE_PATH= 0;
	public static final int CLASS_PATH= 1;

	private ClasspathGroup modulepathEntries;
	private ClasspathGroup classpathEntries;


	@Override
	public Object addEntry(int entryType, IRuntimeClasspathEntry entry) {
		IClasspathEntry entryParent= null;
		switch (entryType) {
			case MODULE_PATH :
				entryParent= getModulepathEntry();
				break;
			case CLASS_PATH :
				entryParent = getClasspathEntry();
				break;
			default :
				break;
		}

		ClasspathEntry newEntry= createEntry(entry, entryParent);
		Iterator<IClasspathEntry> entries= childEntries.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof ClasspathGroup) {
				if(((ClasspathGroup)element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		if (entryParent != null) {
			((ClasspathGroup)entryParent).addEntry(newEntry, null);
		} else {
			childEntries.add(newEntry);
		}
		return newEntry;
	}

	/**
	 * Returns the entries of the given type, or an empty
	 * collection if none.
	 *
	 * @param entryType the kind of the entries to get
	 * @return the entries of the given type, or an empty
	 * collection if none
	 */
	@Override
	public IClasspathEntry[] getEntries(int entryType) {
		switch (entryType) {
			case MODULE_PATH :
				if (modulepathEntries != null) {
					return modulepathEntries.getEntries();
				}
				break;
			case CLASS_PATH :
				if (classpathEntries != null) {
					return classpathEntries.getEntries();
				}
				break;
		}
		return new IClasspathEntry[0];
	}

	@Override
	public IRuntimeClasspathEntry[] getAllEntries() {
		IClasspathEntry[] boot = getEntries(MODULE_PATH);
		IClasspathEntry[] user = getEntries(CLASS_PATH);
		IRuntimeClasspathEntry[] all = new IRuntimeClasspathEntry[boot.length + user.length];
		if (boot.length > 0) {
			System.arraycopy(boot, 0, all, 0, boot.length);
		}
		if (user.length > 0) {
			System.arraycopy(user, 0, all, boot.length, user.length);
		}
		return all;
	}

	@Override
	public void removeAll() {
		if (modulepathEntries != null) {
			modulepathEntries.removeAll();
		}
		if (classpathEntries != null) {
			classpathEntries.removeAll();
		}
	}

	public IClasspathEntry getModulepathEntry() {
		if (modulepathEntries == null) {
			String name = ClasspathMessages.DependencyModel_0;
			modulepathEntries = createGroupEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return modulepathEntries;
	}

	public IClasspathEntry getClasspathEntry() {
		if (classpathEntries == null) {
			String name = ClasspathMessages.DependencyModel_1;
			classpathEntries = createGroupEntry(new IRuntimeClasspathEntry[0], null, name, false, true);
		}
		return classpathEntries;
	}

	/**
	 * Constructs a new classpath model with root entries
	 */
	public DependencyModel() {
		super();
	}

	@Override
	public void createEntries() {
		getModulepathEntry();
		getClasspathEntry();
	}


}
