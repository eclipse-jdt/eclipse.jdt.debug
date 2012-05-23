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
package org.eclipse.jdt.internal.debug.ui.launcher;

 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer extends TableViewer implements IClasspathViewer {
	
	/**
	 * Whether enabled/editable.
	 */
	private boolean fEnabled = true;
	
	/**
	 * Entry changed listeners
	 */
	private ListenerList fListeners = new ListenerList();
	
	/**
	 * The runtime classpath entries displayed in this viewer
	 */
	protected List<IRuntimeClasspathEntry> fEntries = new ArrayList<IRuntimeClasspathEntry>();
	
	class ContentProvider implements IStructuredContentProvider {
			
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getEntries();
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
		
	/**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 */
	public RuntimeClasspathViewer(Composite parent) {
		super(parent);
		setContentProvider(new ContentProvider());
		RuntimeClasspathEntryLabelProvider lp = new RuntimeClasspathEntryLabelProvider();
		setLabelProvider(lp);
		setInput(fEntries);
		
		getTable().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (isEnabled() && event.character == SWT.DEL && event.stateMask == 0) {
					List<?> selection= getSelectionFromWidget();
					fEntries.removeAll(selection);
					setInput(fEntries);
					notifyChanged();
				}
			}
		});	
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#setEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		fEntries.clear();
		for (int i = 0; i < entries.length; i++) {
			fEntries.add(entries[i]);
		}
		setInput(fEntries);
		notifyChanged();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getEntries()
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		return fEntries.toArray(new IRuntimeClasspathEntry[fEntries.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#addEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public void addEntries(IRuntimeClasspathEntry[] entries) {
		IStructuredSelection sel = (IStructuredSelection)getSelection();
		if (sel.isEmpty()) {
			for (int i = 0; i < entries.length; i++) {
				if (!fEntries.contains(entries[i])) {
					fEntries.add(entries[i]);
				}
			}
		} else {
			int index = fEntries.indexOf(sel.getFirstElement());
			for (int i = 0; i < entries.length; i++) {
				if (!fEntries.contains(entries[i])) {
					fEntries.add(index, entries[i]);
					index++;
				}
			}
		}
		setSelection(new StructuredSelection(entries));
		refresh();
		notifyChanged();
	}	
	
	/**
	 * Enables/disables this viewer. Note the control is not disabled, since
	 * we still want the user to be able to scroll if required to see the
	 * existing entries. Just actions should be disabled.
	 */
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		// fire selection change to update actions
		setSelection(getSelection());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#isEnabled()
	 */
	public boolean isEnabled() {
		return fEnabled;
	}
	
	/**
	 * Sets the launch configuration context for this viewer, if any
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		if (getLabelProvider() != null) {
			((RuntimeClasspathEntryLabelProvider)getLabelProvider()).setLaunchConfiguration(configuration);
		}
	}
	
	public void addEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.add(listener);
	}
	
	public void removeEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#notifyChanged()
	 */
	public void notifyChanged() {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IEntriesChangedListener)listeners[i]).entriesChanged(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#indexOf(org.eclipse.jdt.launching.IRuntimeClasspathEntry)
	 */
	public int indexOf(IRuntimeClasspathEntry entry) {
		return fEntries.indexOf(entry);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getShell()
	 */
	public Shell getShell() {
		return getControl().getShell();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(int actionType, IStructuredSelection selection) {
		return isEnabled();
	}
}
