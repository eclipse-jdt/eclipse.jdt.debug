package org.eclipse.jdt.internal.launching;

/*******************************************************************************
 * Copyright (c) 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Compares lists of runtime classpath entry mementos
 */
public class RuntimeClasspathEntryListComparator implements Comparator {

	/**
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		List list1 = (List)o1;
		List list2 = (List)o2;
		
		if (list1.size() == list2.size()) {
			for (int i = 0; i < list1.size(); i++) {
				String memento1 = (String)list1.get(i);
				String memento2 = (String)list2.get(i);
				try {
					IRuntimeClasspathEntry entry1 = JavaRuntime.newRuntimeClasspathEntry(memento1);
					IRuntimeClasspathEntry entry2 = JavaRuntime.newRuntimeClasspathEntry(memento2);
					if (!entry1.equals(entry2)) {
						return -1;
					}
				} catch (CoreException e) {
					LaunchingPlugin.log(e);
					return -1;
				}
			}
			return 0;
		}
		return -1;
	}

}
