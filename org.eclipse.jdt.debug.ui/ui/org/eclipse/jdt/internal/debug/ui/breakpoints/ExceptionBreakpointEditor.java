/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint.SuspendOnRecurrenceStrategy;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.6
 */
public class ExceptionBreakpointEditor extends StandardJavaBreakpointEditor {

	/**
     * Property id's
     */
    public static final int PROP_CAUGHT = 0x1020;
    public static final int PROP_UNCAUGHT = 0x1021;
    public static final int PROP_SUBCLASSES = 0x1022;
	public static final int PROP_RECURRENCE = 0x1023;

	// editors
	private Button fCaught;
	private Button fUncaught;
	private Button fSubclasses;
	private Combo fRecurrence;
	private Map<SuspendOnRecurrenceStrategy, String> fRecurrenceOptions;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		Composite container = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 0, 0, 0);
		// add standard controls
		super.createControl(container);
		Composite composite = SWTFactory.createComposite(container, parent.getFont(), 5, 1, 0, 0, 0);
//		SWTFactory.createLabel(composite, PropertyPageMessages.ExceptionBreakpointEditor_0, 1);
		fCaught = createSusupendPropertyEditor(composite, processMnemonics(PropertyPageMessages.ExceptionBreakpointEditor_1), PROP_CAUGHT);
		fUncaught = createSusupendPropertyEditor(composite, processMnemonics(PropertyPageMessages.ExceptionBreakpointEditor_2), PROP_UNCAUGHT);
		fSubclasses = createSusupendPropertyEditor(composite, processMnemonics(PropertyPageMessages.ExceptionBreakpointEditor_3), PROP_SUBCLASSES);
		composite = SWTFactory.createComposite(container, parent.getFont(), 2, 1, 0, 0, 0);
		fRecurrence = createRecurrenceEditor(composite, processMnemonics(PropertyPageMessages.ExceptionBreakpointEditor_recurrence_label), PROP_RECURRENCE);
		return container;
	}

	private Combo createRecurrenceEditor(Composite parent, String labelText, int propId) {
		fRecurrenceOptions = new HashMap<>();
		fRecurrenceOptions.put(SuspendOnRecurrenceStrategy.RECURRENCE_UNCONFIGURED, PropertyPageMessages.ExceptionBreakpointEditor_recurrence_unconfigured);
		fRecurrenceOptions.put(SuspendOnRecurrenceStrategy.SUSPEND_ALWAYS, PropertyPageMessages.ExceptionBreakpointEditor_recurrence_always);
		fRecurrenceOptions.put(SuspendOnRecurrenceStrategy.SKIP_RECURRENCES, PropertyPageMessages.ExceptionBreakpointEditor_recurrence_onlyOnce);
		SWTFactory.createLabel(parent, labelText, 1);
		Combo box = SWTFactory.createCombo(parent, SWT.READ_ONLY, 1, GridData.FILL_HORIZONTAL, fRecurrenceOptions.values().toArray(new String[fRecurrenceOptions.size()]));
		box.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(propId);
			}
		});
		return box;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#setBreakpoint(org.eclipse.jdt.debug.core.IJavaBreakpoint)
	 */
	@Override
	protected void setBreakpoint(IJavaBreakpoint breakpoint) throws CoreException {
		super.setBreakpoint(breakpoint);
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			IJavaExceptionBreakpoint ex = (IJavaExceptionBreakpoint) breakpoint;
			fCaught.setEnabled(true);
			fUncaught.setEnabled(true);
			fSubclasses.setEnabled(true);
			fRecurrence.setEnabled(true);
			fCaught.setSelection(ex.isCaught());
			fUncaught.setSelection(ex.isUncaught());
			fSubclasses.setSelection(((JavaExceptionBreakpoint)ex).isSuspendOnSubclasses());
			fRecurrence.setText(fRecurrenceOptions.get(ex.getSuspendOnRecurrenceStrategy()));
		} else {
			fCaught.setEnabled(false);
			fUncaught.setEnabled(false);
			fSubclasses.setEnabled(false);
			fRecurrence.setEnabled(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor#doSave()
	 */
	@Override
	public void doSave() throws CoreException {
		super.doSave();
		IJavaBreakpoint breakpoint = getBreakpoint();
		if (breakpoint instanceof IJavaExceptionBreakpoint) {
			IJavaExceptionBreakpoint ex = (IJavaExceptionBreakpoint) breakpoint;
			ex.setCaught(fCaught.getSelection());
			ex.setUncaught(fUncaught.getSelection());
			((JavaExceptionBreakpoint)ex).setSuspendOnSubclasses(fSubclasses.getSelection());
			ex.setSuspendOnRecurrenceStrategy(stringToRecurrence(fRecurrence.getText()));
		}
	}

	private SuspendOnRecurrenceStrategy stringToRecurrence(String text) {
		for (Entry<SuspendOnRecurrenceStrategy, String> entry : fRecurrenceOptions.entrySet()) {
			if (entry.getValue().equals(text)) {
				return entry.getKey();
			}
		}
		return SuspendOnRecurrenceStrategy.RECURRENCE_UNCONFIGURED;
	}
}
