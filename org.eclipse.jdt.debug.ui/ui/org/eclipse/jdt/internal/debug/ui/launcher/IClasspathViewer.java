package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**********************************************************************
Copyright (c) 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

public interface IClasspathViewer {

	/**
	 * @param action
	 */
	void removeSelectionChangedListener(ISelectionChangedListener action);

	/**
	 * @param action
	 */
	void addSelectionChangedListener(ISelectionChangedListener action);

	/**
	 * @return
	 */
	IRuntimeClasspathEntry[] getEntries();

	/**
	 * @param entries
	 */
	void setEntries(IRuntimeClasspathEntry[] entries);

	/**
	 * @param selection
	 */
	void setSelection(ISelection selection);

	/**
	 * @return
	 */
	Shell getShell();

	/**
	 * @return
	 */
	boolean isEnabled();

	/**
	 * @param res
	 */
	void addEntries(IRuntimeClasspathEntry[] res);

	/**
	 * @param entry
	 */
	void refresh(Object entry);

	/**
	 * 
	 */
	void notifyChanged();

	/**
	 * @param entry
	 * @return
	 */
	int indexOf(IRuntimeClasspathEntry entry);

	/**
	 * @param i
	 * @param selection
	 * @return
	 */
	boolean updateSelection(int i, IStructuredSelection selection);

	/**
	 * @return
	 */
	ISelection getSelection();
}
