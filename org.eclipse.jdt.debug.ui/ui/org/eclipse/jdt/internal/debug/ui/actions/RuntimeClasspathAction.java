package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.debug.ui.launcher.RuntimeClasspathViewer;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import sun.security.action.GetBooleanAction;

/**
 * Action used with a runtime classpath viewer.
 */
public abstract class RuntimeClasspathAction extends SelectionListenerAction {
	
	private RuntimeClasspathViewer fViewer;
	private Button fButton;
	
	public RuntimeClasspathAction(String label, RuntimeClasspathViewer viewer) {
		super(label);
		setViewer(viewer);
	}

	/**
	 * Sets the viewer on which this action operates.
	 * 
	 * @param viewer the viewer on which this action operates
	 */
	public void setViewer(RuntimeClasspathViewer viewer) {
		if (fViewer != null) {
			fViewer.removeSelectionChangedListener(this);
		}
		fViewer = viewer;
		if (fViewer != null) {
			fViewer.addSelectionChangedListener(this);
			update();
		}
	}
	
	/**
	 * Returns the viewer on which this action operates.
	 * 
	 * @return the viewer on which this action operates
	 */
	protected RuntimeClasspathViewer getViewer() {
		return fViewer;
	}

	/**
	 * Returns the selected items in the list, in the order they are
	 * displayed.
	 * 
	 * @return targets for an action
	 */
	protected List getOrderedSelection() {
		List targets = new ArrayList();
		List selection = ((IStructuredSelection)getViewer().getSelection()).toList();
		IRuntimeClasspathEntry[] entries = getViewer().getEntries();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry target = entries[i];
			if (selection.contains(target)) {
				targets.add(target);
			}
		}
		return targets;		
	}
	
	/**
	 * Returns a list (copy) of the entries in the viewer
	 */
	protected List getEntiresAsList() {
		IRuntimeClasspathEntry[] entries = getViewer().getEntries();
		List list = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			list.add(entries[i]);
		}
		return list;
	}
	
	/**
	 * Updates the entries to the entries in the given list
	 */
	protected void setEntries(List list) {
		getViewer().setEntries((IRuntimeClasspathEntry[])list.toArray(new IRuntimeClasspathEntry[list.size()]));
		// update all selection listeners
		getViewer().setSelection(getViewer().getSelection());
	}
	
	/**
	 * Returns whether the item at the given index in the list
	 * (visually) is selected.
	 */
	protected boolean isIndexSelected(IStructuredSelection selection, int index) {
		if (selection.isEmpty()) {
			return false;
		}
		Iterator entries = selection.iterator();
		List list = getEntiresAsList();
		while (entries.hasNext()) {
			Object next = entries.next();
			if (list.indexOf(next) == index) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Sets the button that invokes this action
	 */
	public void setButton(Button button) {
		fButton = button;
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				run();
			}
		});
	}
	/**
	 * @see IAction#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (fButton != null) {
			fButton.setEnabled(enabled);
		}
	}

	/**
	 * Updates the enabled state.
	 */
	protected void update() {
		selectionChanged((IStructuredSelection)getViewer().getSelection());
	}
	
	/**
	 * Returns the shell associated with the viewer
	 */
	protected Shell getShell() {
		return getViewer().getControl().getShell();
	}
}
