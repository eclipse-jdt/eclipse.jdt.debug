package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * A generic Toggle view action delegate, meant to be subclassed to provide
 * a specific filter.
 */
public abstract class ToggleDelegateAction implements IViewActionDelegate, IPropertyChangeListener, IActionDelegate2 {

	/**
	 * The viewer that this action works for
	 */
	private StructuredViewer fViewer;
	
	private IViewPart fView;
	
	private IAction fAction;

	public void dispose() {
		JDIDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		IDebugView adapter= (IDebugView) view.getAdapter(IDebugView.class);
		if (adapter != null && adapter.getViewer() instanceof StructuredViewer) {
			setViewer((StructuredViewer)adapter.getViewer());
		}
		JDIDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//do nothing.."run" will occur from the property change
		//this allows for setting the checked state of the IAction
		//to drive the execution of this delegate.
		//see propertyChange(PropertyChangeEvent)
	}
	
	protected abstract void valueChanged(boolean on);
	
	protected StructuredViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	protected IAction getAction() {
		return fAction;
	}

	public void init(IAction action) {
		fAction = action;
		action.addPropertyChangeListener(this);
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(getAction().getId())) {
			getAction().setChecked(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(getAction().getId()));
		} else if (event.getProperty().equals(IAction.CHECKED)) {
			JDIDebugUIPlugin.getDefault().getPreferenceStore().setValue(getAction().getId(), getAction().isChecked());
			valueChanged(getAction().isChecked());
		}
	}
	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart view) {
		fView = view;
	}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	
}
