package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;

/**
 * Waits for an event on a specific element
 */

public class DebugElementEventWaiter extends DebugEventWaiter {
	
	protected Object fElement;
	
	public DebugElementEventWaiter(int kind, Object element) {
		super(kind);
		fElement = element;
	}
	
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fElement == event.getSource();
	}

}