package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

public class ModificationWatchpointToggleAction extends WatchpointAction {

	/**
	 * @see WatchpointAction#getToggleState(IMarker)
	 */
	protected boolean getToggleState(IJavaWatchpoint watchpoint) throws CoreException {
		return watchpoint.isModification();
	}

	/**
	 * @see WatchpointAction#doAction(IMarker)
	 */
	public void doAction(IJavaWatchpoint watchpoint) throws CoreException {
		watchpoint.setModification(!watchpoint.isModification());
	}

}

