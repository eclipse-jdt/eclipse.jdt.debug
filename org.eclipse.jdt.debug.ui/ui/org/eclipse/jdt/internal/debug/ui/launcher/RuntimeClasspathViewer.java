package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer extends TableViewer {
	
	/**
	 * Whether enabled/editable.
	 */
	private boolean fEnabled = true;
	
	/**
	 * Entry changed listeners
	 */
	private ListenerList fListeners = new ListenerList(3);
	
	/**
	 * The launch configuration context for this viewer, or <code>null</code>
	 */
	private ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * The runtime classpath entries displayed in this viewer
	 */
	protected List fEntries = new ArrayList();
	
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
		lp.setLaunchConfiguration(fLaunchConfiguration);
		setLabelProvider(lp);
		setInput(fEntries);
	}	

	/**
	 * Sets the entries in this viewer to the given runtime classpath
	 * entries
	 * 
	 * @param entries runtime classpath entries
	 */
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		fEntries.clear();
		for (int i = 0; i < entries.length; i++) {
			fEntries.add(entries[i]);
		}
		setInput(fEntries);
		notifyChanged();
	}
	
	/**
	 * Returns the entries in this viewer
	 * 
	 * @return the entries in this viewer
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		return (IRuntimeClasspathEntry[])fEntries.toArray(new IRuntimeClasspathEntry[fEntries.size()]);
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
		IStructuredSelection sel = (IStructuredSelection)getSelection();
		if (sel.isEmpty()) {
			for (int i = 0; i < entries.length; i++) {
				fEntries.add(entries[i]);
			}
		} else {
			int index = fEntries.indexOf(sel.getFirstElement());
			for (int i = 0; i < entries.length; i++) {
				fEntries.add(index, entries[i]);
				index++;
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
		// fire selection change to upate actions
		setSelection(getSelection());
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
		fLaunchConfiguration = configuration;
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
	
	protected void notifyChanged() {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IEntriesChangedListener)listeners[i]).entriesChanged(this);
		}
	}
	
	/**
	 * Returns the index of an equivalent entry, or -1 if none.
	 * 
	 * @return the index of an equivalent entry, or -1 if none
	 */
	public int indexOf(IRuntimeClasspathEntry entry) {
		return fEntries.indexOf(entry);
	}
}