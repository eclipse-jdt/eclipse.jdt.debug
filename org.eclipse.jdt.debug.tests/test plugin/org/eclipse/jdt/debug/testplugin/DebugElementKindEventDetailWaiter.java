package org.eclipse.jdt.debug.testplugin;

import org.eclipse.debug.core.DebugEvent;

/**
 * Wait for the specified event with the specified from the specified element.
 */
public class DebugElementKindEventDetailWaiter extends DebugElementKindEventWaiter {

	protected int fDetail;

	public DebugElementKindEventDetailWaiter(int eventKind, Class elementClass, int detail) {
		super(eventKind, elementClass);
		fDetail = detail;
	}
	
	public boolean accept(DebugEvent event) {
		return super.accept(event) && fDetail == event.getDetail();
	}
	
}
