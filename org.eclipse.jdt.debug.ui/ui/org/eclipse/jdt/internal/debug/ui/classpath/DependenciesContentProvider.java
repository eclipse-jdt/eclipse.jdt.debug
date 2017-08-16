/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.classpath;


import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

/**
 * Content provider that maintains a list of classpath entries which are shown in a tree
 * viewer.
 */
public class DependenciesContentProvider extends ClasspathContentProvider {

	public DependenciesContentProvider(JavaClasspathTab tab) {
		super(tab);
	}



	@Override
	public IClasspathEntry[] getUserClasspathEntries() {
		return model.getEntries(DependencyModel.MODULE_PATH);
	}

	@Override
	public IClasspathEntry[] getBootstrapClasspathEntries() {
		return model.getEntries(DependencyModel.CLASS_PATH);
	}



	@Override
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		model.removeAll();
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < entries.length; i++) {
			entry= entries[i];
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.MODULE_PATH:
					model.addEntry(DependencyModel.MODULE_PATH, entry);
					break;
				default:
					model.addEntry(DependencyModel.CLASS_PATH, entry);
					break;
			}
		}
		refresh();
	}
}
