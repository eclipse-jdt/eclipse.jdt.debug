package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles the global preference flag that controls whether the active step filters
 * defined in the Java Debug Options preference page are used.
 */
public class ToggleStepFilterAction implements IViewActionDelegate, IPartListener ,IPropertyChangeListener {

	private boolean fSetInitialState = false;

	private IViewPart fView;
	private IAction fAction;
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		view.getSite().getPage().addPartListener(this);
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		boolean newStepFilterState = !store.getBoolean(IJDIPreferencesConstants.PREF_USE_FILTERS);		
		store.setValue(IJDIPreferencesConstants.PREF_USE_FILTERS, newStepFilterState);
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (!fSetInitialState) {
			action.setChecked(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_USE_FILTERS));
			fSetInitialState = true;
			setAction(action);
		}
	}
	
	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart view) {
		fView = view;
	}
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		IAction action= getAction();
		if (action == null) {
			return;
		}
		if (event.getProperty().equals(IJDIPreferencesConstants.PREF_USE_FILTERS)) {
			action.setChecked(JDIDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IJDIPreferencesConstants.PREF_USE_FILTERS));
		}
		
	}
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == getView()) {
			getView().getSite().getPage().removePartListener(this);
			IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
			store.removePropertyChangeListener(this);
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}
}

