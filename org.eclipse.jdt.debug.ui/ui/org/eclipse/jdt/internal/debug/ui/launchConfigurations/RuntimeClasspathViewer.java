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
package org.eclipse.jdt.internal.debug.ui.launchConfigurations;

 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.debug.ui.launcher.IEntriesChangedListener;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer extends TreeViewer{// implements IClasspathViewer {
	
	/**
	 * Whether enabled/editable.
	 */
	private boolean fEnabled = true;
	
	/**
	 * Entry changed listeners
	 */
	private ListenerList fListeners = new ListenerList(3);
	
	private IClasspathEntry fCurrentParent= null;
		
	/**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 */
	public RuntimeClasspathViewer(Composite parent) {
		super(parent);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = getTree().getItemHeight();
		getTree().setLayoutData(data);
		
		getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (isEnabled() && event.character == SWT.DEL && event.stateMask == 0) {
					List selection= getSelectionFromWidget();
					getClasspathContentProvider().removeAll(selection);
					notifyChanged();
				}
			}
		});
	}	

	/**
	 * Sets the entries in this viewer to the given runtime classpath
	 * entries
	 * 
	 * @param entries runtime classpath entries
	 */
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		resolveCurrentParent(getSelection());
		getClasspathContentProvider().removeAll(fCurrentParent);
		getClasspathContentProvider().setEntries(entries);
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}
	
	/**
	 * Returns the entries in this viewer that are the children of the parent element
	 * associated with the selected item(s)
	 * 
	 * 
	 * @return the entries in this viewer
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		resolveCurrentParent(getSelection());
		if (fCurrentParent == null) {
			return new IRuntimeClasspathEntry[0];
		}
		
		IClasspathEntry[] entries= fCurrentParent.getEntries();
		List runtimeEntries= new ArrayList(entries.length * 2);
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.hasEntries()) {
				runtimeEntries.addAll(Arrays.asList(entry.getEntries()));
			} else {
				runtimeEntries.add(entry);
			}
		}
		return (IRuntimeClasspathEntry[])runtimeEntries.toArray(new IRuntimeClasspathEntry[runtimeEntries.size()]);
	}
	
	/**
	 * Adds the given entries to the list. If there is no selection
	 * in the list, the entries are added at the end of the list, 
	 * otherwise the new entries are added before the (first) selected
	 * entry. The new entries are selected.
	 * 
	 * @param entries additions
	 */
	public void addEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		resolveCurrentParent(getSelection());
		List existingEntries= Arrays.asList(fCurrentParent.getEntries());
		for (int i = 0; i < entries.length; i++) {
			if (!existingEntries.contains(entries[i])) {
				getClasspathContentProvider().add(fCurrentParent, entries[i]);
			}
		} 
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}
	
	private boolean resolveCurrentParent(ISelection selection) {
		fCurrentParent= null;
		Iterator selected= ((IStructuredSelection)selection).iterator();
		
		while (selected.hasNext()) {
			Object element = selected.next();
			if (element instanceof ClasspathEntry) {
				IClasspathEntry parent= ((IClasspathEntry)element).getParent();
				if (fCurrentParent != null) {
					if (!fCurrentParent.equals(parent)) {
						return false;
					}
				} else {
					fCurrentParent= parent;
				}
			} else {
				if (fCurrentParent != null) {
					if (!fCurrentParent.equals(element)) {
						return false;
					}
				} else {
					fCurrentParent= (IClasspathEntry)element;
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns whether this viewer is enabled
	 */
	public boolean isEnabled() {
		return fEnabled;
	}
	
	/**
	 * Sets the launch configuration context for this viewer, if any
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		if (getLabelProvider() != null) {
			((ClasspathLabelProvider)getLabelProvider()).setLaunchConfiguration(configuration);
		}
	}
	
	public void addEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.add(listener);
	}
	
	public void removeEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.remove(listener);
	}
	
	public void notifyChanged() {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
		//	((IEntriesChangedListener)listeners[i]).entriesChanged(this);
		}
	}
	
	/**
	 * Returns the index of an equivalent entry, or -1 if none.
	 * 
	 * @return the index of an equivalent entry, or -1 if none
	 */
	public int indexOf(IRuntimeClasspathEntry entry) {
		IClasspathEntry[] entries= getClasspathContentProvider().getBootstrapClasspathEntries();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry existingEntry = entries[i];
			if (existingEntry.equals(entry)) {
				return 1;
			}
		}
		entries=  getClasspathContentProvider().getUserClasspathEntries();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry existingEntry = entries[i];
			if (existingEntry.equals(entry)) {
				return 1;
			}
		}
		
		return -1;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getShell()
	 */
	public Shell getShell() {
		return getControl().getShell();
	}
	
	private ClasspathContentProvider getClasspathContentProvider() {
		return (ClasspathContentProvider)super.getContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(int actionType, IStructuredSelection selection) {
		
//		if (selection.isEmpty()) {
//			return false;
//		}
//		switch (actionType) {
//			case RuntimeClasspathAction.ADD :
//				resolveCurrentParent(selection);
//				if (fCurrentParent instanceof ClasspathGroup) {
//					return !((ClasspathGroup)fCurrentParent).canBeRemoved();
//				} else {
//					return fCurrentParent != null;
//				}
//			case RuntimeClasspathAction.REMOVE :
//				Iterator selected= selection.iterator();
//				while (selected.hasNext()) {
//					Object element = selected.next();
//					if (element instanceof ClasspathGroup) {
//						return ((ClasspathGroup)element).canBeRemoved();
//					}
//				}
//				return true;
//			case RuntimeClasspathAction.MOVE :
//				selected= selection.iterator();
//				while (selected.hasNext()) {
//					Object element = selected.next();
//					if (element instanceof ClasspathGroup) {
//						return ((ClasspathGroup)element).canBeRemoved();
//					}
//				}
//				return resolveCurrentParent(selection);
//			default :
//				break;
//		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getSelectedEntries()
	 */
	public ISelection getSelectedEntries() {
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		List entries= new ArrayList(selection.size() * 2);
		Iterator itr= selection.iterator();
		while (itr.hasNext()) {
			IClasspathEntry element = (IClasspathEntry) itr.next();
			if (element.hasEntries()) {
				entries.addAll(Arrays.asList(element.getEntries()));
			} else {
				entries.add(element);
			}
		}
		
		return new StructuredSelection(entries);
	}
}