/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.RuntimeClasspathAction;
import org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer;
import org.eclipse.jdt.internal.debug.ui.launcher.IEntriesChangedListener;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer implements IClasspathViewer {

	/**
	 * Entry changed listeners
	 */
	private ListenerList<IEntriesChangedListener> fListeners = new ListenerList<>();

	private IClasspathEntry fCurrentParent= null;

	private IPreferenceChangeListener fPrefListeners = new IPreferenceChangeListener() {

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			if (DebugUIPlugin.getStandardDisplay().getThread().equals(Thread.currentThread())) {
				refresh(true);
			} else {
				DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						refresh(true);
					}
				});
			}

		}
	};

	private static class RuntimeClasspathFilteredTree extends FilteredTree {

		private boolean isFiltering;

		private RuntimeClasspathFilteredTree(Composite parent, PatternFilter filter) {
			super(parent, 0, filter, true);
		}

		private boolean hasFilterTextEntered() {
			return isFiltering;
		}

		/**
		 * Called by modify listener -> implicit change listener.
		 */
		@Override
		protected void textChanged() {

			super.textChanged();

			final String filterString = getFilterString();
			if (null != filterString) {
				// REVIEW: There are several different ways used to check for empty filter texts:
				// comparing with "", comparing with IInternalDebugCoreConstants.EMPTY_STRING and checking size of trimmed value.
				isFiltering = !filterString.trim().isEmpty();
			} else {
				isFiltering = false;
			}
		}

		@Override
		protected WorkbenchJob doCreateRefreshJob() {
			final WorkbenchJob job = super.doCreateRefreshJob();

			return new WorkbenchJob("Classpath filter refresh") { //$NON-NLS-1$

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					final IStatus status = job.runInUIThread(monitor);

					if (!isFiltering) {
						getViewer().expandToLevel(2);
					}

					return status;
				}
			};
		}
	}

	private final RuntimeClasspathFilteredTree fTree;

	public TreeViewer getTreeViewer() {
		return fTree.getViewer();
	}

	/**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 */
	public RuntimeClasspathViewer(Composite parent) {

		final PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);
		fTree = new RuntimeClasspathFilteredTree(parent, filter);

		getTreeViewer().getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (updateSelection(RuntimeClasspathAction.REMOVE, (IStructuredSelection) getSelection()) && event.character == SWT.DEL
						&& event.stateMask == 0) {
					getClasspathContentProvider().removeAll(((IStructuredSelection) getSelectedEntries()).toList());
					notifyChanged();
				}
			}
		});

		fTree.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
				if(prefs != null) {
					prefs.removePreferenceChangeListener(fPrefListeners);
				}
			}
		});
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN);
		if(prefs != null) {
			prefs.addPreferenceChangeListener(fPrefListeners);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#setEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	@Override
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		resolveCurrentParent(getSelection());
		getClasspathContentProvider().removeAll(fCurrentParent);
		getClasspathContentProvider().setEntries(entries);
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getEntries()
	 */
	@Override
	public IRuntimeClasspathEntry[] getEntries() {
		return getClasspathContentProvider().getModel().getAllEntries();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#addEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void addEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		Object beforeElement = sel.getFirstElement();
		resolveCurrentParent(getSelection());
		List<IClasspathEntry> existingEntries= Arrays.asList(fCurrentParent.getEntries());
		for (int i = 0; i < entries.length; i++) {
			if (!existingEntries.contains(entries[i])) {
				getClasspathContentProvider().add(fCurrentParent, entries[i], beforeElement);
			}
		}
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}

	private boolean resolveCurrentParent(ISelection selection) {
		fCurrentParent= null;
		Iterator<?> selected= ((IStructuredSelection)selection).iterator();

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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Sets the launch configuration context for this viewer, if any
	 * @param configuration the backing {@link ILaunchConfiguration}
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		if (getTreeViewer().getLabelProvider() != null) {
			((ClasspathLabelProvider) getTreeViewer().getLabelProvider()).setLaunchConfiguration(configuration);
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
	@Override
	public void notifyChanged() {
		for (IEntriesChangedListener listener : fListeners) {
			listener.entriesChanged(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#indexOf(org.eclipse.jdt.launching.IRuntimeClasspathEntry)
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
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
	@Override
	public Shell getShell() {
		return getTreeViewer().getControl().getShell();
	}

	private ClasspathContentProvider getClasspathContentProvider() {
		return (ClasspathContentProvider) getTreeViewer().getContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean updateSelection(int actionType, IStructuredSelection selection) {

		if (selection.isEmpty()) {
			return false;
		}
		switch (actionType) {
			case RuntimeClasspathAction.ADD :
				Iterator<IClasspathEntry> selected= selection.iterator();
				while (selected.hasNext()) {
					IClasspathEntry entry = selected.next();
					if (!entry.isEditable() && entry instanceof ClasspathEntry) {
						return false;
					}
				}
				return selection.size() > 0;
			case RuntimeClasspathAction.MOVE:
				if (fTree.hasFilterTextEntered()) {
					return false;
				}
			case RuntimeClasspathAction.REMOVE :
				selected= selection.iterator();
				while (selected.hasNext()) {
					IClasspathEntry entry = selected.next();
					if (!entry.isEditable()) {
						return false;
					}
				}
				return selection.size() > 0;
			default :
				break;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getSelectedEntries()
	 */
	public ISelection getSelectedEntries() {
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		List<IClasspathEntry> entries= new ArrayList<>(selection.size() * 2);
		Iterator<IClasspathEntry> itr= selection.iterator();
		while (itr.hasNext()) {
			IClasspathEntry element = itr.next();
			if (element.hasEntries()) {
				entries.addAll(Arrays.asList(element.getEntries()));
			} else {
				entries.add(element);
			}
		}

		return new StructuredSelection(entries);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		getTreeViewer().addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return getTreeViewer().getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		getTreeViewer().removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		getTreeViewer().setSelection(selection);
	}

	@Override
	public void refresh(Object entry) {
		getTreeViewer().refresh();
	}

}
