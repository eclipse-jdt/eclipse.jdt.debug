package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Waits for a type of event on a kind of element.  Compare this to SpecificDebugElementEventWaiter which is
 * used to wait for a type of event on a specific debug element object.
 */

public class DebugElementKindEventWaiter extends DebugEventWaiter {
	
	protected Class fElementClass;
	
	public DebugElementKindEventWaiter(int eventKind, Class elementClass) {
		super(eventKind);
		fElementClass = elementClass;
	}
	
	public boolean accept(DebugEvent event) {
		Object o = event.getSource();
		return super.accept(event) && fElementClass.isInstance(o);
	}

}


