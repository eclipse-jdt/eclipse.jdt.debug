package org.eclipse.jdt.debug.tests.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
*********************************************************************/

import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Step filtering tests
 * This test forces the UI plugins to load.
 */
public class StepFilterTests extends AbstractDebugTest {

	private String fOriginalActiveFilters;
	private String fOriginalInactiveFilters;

	public StepFilterTests(String name) {
		super(name);
		fOriginalActiveFilters = getPrefStore().getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST);
		fOriginalInactiveFilters = getPrefStore().getString(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST);		
	}

	public void testSimpleStepFilter() throws Exception {
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
		String typeName = "StepFilterOne";
		ILineBreakpoint bp = createLineBreakpoint(21, typeName);
		bp.setEnabled(true);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread = stepInto(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			String recTypeName = stackFrame.getReceivingTypeName();
			assertTrue("Receiving type name should have been 'StepFilterOne' but was " + recTypeName, recTypeName.equals("StepFilterOne"));
			int lineNumber = stackFrame.getLineNumber();
			assertTrue("Line number should have been 22, but was " + lineNumber, lineNumber == 22);			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			resetStepFilters();
		}				
	}
	
	public void testInactiveStepFilter() throws Exception {
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo");
		String typeName = "StepFilterOne";
		ILineBreakpoint bp = createLineBreakpoint(21, typeName);
		bp.setEnabled(true);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread = stepInto(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			String recTypeName = stackFrame.getReceivingTypeName();
			assertTrue("Receiving type name should have been 'StepFilterTwo' but was " + recTypeName, recTypeName.equals("StepFilterTwo"));
			int lineNumber = stackFrame.getLineNumber();
			assertTrue("Line number should have been 25, but was " + lineNumber, lineNumber == 25);			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			resetStepFilters();
		}						
	}
	
	public void testDeepStepFilter() throws Exception {
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters + ",StepFilterTwo," + fOriginalInactiveFilters);
		String typeName = "StepFilterOne";
		ILineBreakpoint bp = createLineBreakpoint(22, typeName);
		bp.setEnabled(true);
		
		IJavaThread thread = null;
		try {
			thread= launchToLineBreakpoint(typeName, bp);
			IJavaStackFrame stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			thread = stepInto(stackFrame);
			stackFrame = (IJavaStackFrame) thread.getTopStackFrame();
			String recTypeName = stackFrame.getReceivingTypeName();
			assertTrue("Receiving type name should have been 'StepFilterThree' but was " + recTypeName, recTypeName.equals("StepFilterThree"));
			int lineNumber = stackFrame.getLineNumber();
			assertTrue("Line number should have been 19, but was " + lineNumber, lineNumber == 19);			
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			resetStepFilters();
		}				
	}	

	protected void resetStepFilters() {
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, fOriginalActiveFilters);
		getPrefStore().setValue(IJDIPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, fOriginalInactiveFilters);
	}

	protected IPreferenceStore getPrefStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();		
	}
}

