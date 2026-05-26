/*******************************************************************************
 * Copyright (c) 2009, 2026 IBM Corporation and others.
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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.ui.breakpoints.JavaBreakpointConditionEditor;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @since 3.6
 */
public class StandardJavaBreakpointEditor extends AbstractJavaBreakpointEditor {

	private IJavaBreakpoint fBreakpoint;
	private Button fHitCountButton;
	private Text fHitCountText;
	private Button fSuspendThread;
	private Button fResumeOnHit;
	private Button fSuspendVM;
	protected Button fTriggerPointButton;
	protected Button fDisableOnHit;
	protected Button waitForBreakpoint;
	protected Link dependentBreakpoint;
	private IDebugModelPresentation fPresentation;

	private final JavaBreakpointConditionEditor javaBpConditionEditor;

	/**
     * Property id for hit count enabled state.
     */
    public static final int PROP_HIT_COUNT_ENABLED = 0x1005;

	/**
     * Property id for breakpoint hit count.
     */
    public static final int PROP_HIT_COUNT = 0x1006;

	/**
     * Property id for suspend policy.
     */
    public static final int PROP_SUSPEND_POLICY = 0x1007;

	/**
	 * Property id for trigger point.
	 */
	public static final int PROP_TRIGGER_POINT = 0x1008;

	public StandardJavaBreakpointEditor() {
		this(null);
	}

	public StandardJavaBreakpointEditor(JavaBreakpointConditionEditor jb) {
		javaBpConditionEditor = jb;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		createTriggerPointButton(parent);
		return createStandardControls(parent);
	}

	protected Button createCheckButton(Composite parent, String text) {
		return SWTFactory.createCheckButton(parent, text, null, false, 1);
	}

	/**
	 * Creates the button to toggle Triggering point property of the breakpoint
	 *
	 * @param parent
	 *            the parent composite
	 */
	protected void createTriggerPointButton(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, 0, 0, 0);
		fTriggerPointButton = createCheckButton(composite, PropertyPageMessages.JavaBreakpointPage_12);
		fTriggerPointButton.setEnabled(true);
		fTriggerPointButton.setSelection(isTriggerPoint());
		fTriggerPointButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IJavaBreakpoint breakpoint = getBreakpoint();
				if (breakpoint != null) {
					try {
						if (breakpoint.getSuspendPolicy() == IJavaBreakpoint.RESUME_ON_HIT) {
							fResumeOnHit.setSelection(true);
						}
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
				boolean resumeOnHitEnabled = fResumeOnHit.isEnabled();
				if (resumeOnHitEnabled) {
					fResumeOnHit.setSelection(false);
					fResumeOnHit.setEnabled(false);
				} else {
					fResumeOnHit.setEnabled(true);
				}
				if (isTriggerPoint()) {
					if (suspendVmAndTreadNotSelected()) {
						fSuspendThread.setSelection(true);
						setConditionTextToSuspend();
					}
				} else {
					if (resumeOnHitEnabled) {
						if (suspendVmAndTreadNotSelected()) {
							fSuspendThread.setSelection(true);
							setConditionTextToSuspend();
						}
					} else {
						setConditionTextToSuspend();
					}
				}

				setDirty(PROP_TRIGGER_POINT);
			}

			private boolean suspendVmAndTreadNotSelected() {
				return !fSuspendThread.getSelection() && !fSuspendVM.getSelection();
			}
		});
		fResumeOnHit = SWTFactory.createRadioButton(composite, PropertyPageMessages.BreakpointResumeOnHit, 1);
		fResumeOnHit.setLayoutData(new GridData());
		fResumeOnHit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (javaBpConditionEditor != null) {
					if (fResumeOnHit.isEnabled()) {
						javaBpConditionEditor.updateConditionTextOnResume();
					}
					if (fResumeOnHit.getSelection()) {
						javaBpConditionEditor.setResumeOnHit(true);
					}
				}
				setDirty(PROP_SUSPEND_POLICY);
				fSuspendThread.setSelection(false);
				fSuspendVM.setSelection(false);
				fResumeOnHit.setEnabled(true);
			}
		});
	}

	protected Control createStandardControls(Composite parent) {
		fPresentation = DebugUITools.newDebugModelPresentation();
		parent.addDisposeListener(e -> {
			if (fPresentation != null) {
				fPresentation.dispose();
				fPresentation = null;
			}
		});
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 0, 0, 0);
		fDisableOnHit = SWTFactory.createCheckButton(comp, PropertyPageMessages.BreakpointDisableOnHit, null, false, 2);
		Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 4, 1, 0, 0, 0);
		fHitCountButton = SWTFactory.createCheckButton(composite, processMnemonics(PropertyPageMessages.JavaBreakpointPage_4), null, false, 1);
		fHitCountButton.setLayoutData(new GridData());
		fHitCountButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean enabled = fHitCountButton.getSelection();
				fHitCountText.setEnabled(enabled);
				if(enabled) {
					fHitCountText.setFocus();
				}
				setDirty(PROP_HIT_COUNT_ENABLED);
			}
		});
		fHitCountText = SWTFactory.createSingleText(composite, 1);
		GridData gd = (GridData) fHitCountText.getLayoutData();
		gd.minimumWidth = 50;
		fHitCountText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setDirty(PROP_HIT_COUNT);
			}
		});

		SWTFactory.createLabel(composite, "", 1); // spacer //$NON-NLS-1$
		Composite radios = SWTFactory.createComposite(composite, composite.getFont(), 3, 1, GridData.FILL_HORIZONTAL, 0, 0);
		fSuspendThread = SWTFactory.createRadioButton(radios, processMnemonics(PropertyPageMessages.JavaBreakpointPage_7), 1);
		fSuspendThread.setLayoutData(new GridData());
		fSuspendVM = SWTFactory.createRadioButton(radios, processMnemonics(PropertyPageMessages.JavaBreakpointPage_8), 1);
		fSuspendVM.setLayoutData(new GridData());
		Composite composite2 = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
		composite2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		waitForBreakpoint = SWTFactory.createCheckButton(composite2, PropertyPageMessages.WaitForBreakpointLabel, null, false, 1);

		Composite dependentComp = SWTFactory.createComposite(composite2, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL, 0, 0);
		dependentComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		dependentBreakpoint = new Link(dependentComp, SWT.NONE);
		dependentBreakpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		dependentBreakpoint.setText(PropertyPageMessages.WaitForBreakpointDefaultSelection);
		dependentBreakpoint.setEnabled(false);

		waitForBreakpoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dependentBreakpoint.setEnabled(waitForBreakpoint.getSelection());
				if (waitForBreakpoint.getSelection()) {
					IJavaBreakpoint breakpoint = getBreakpoint();
					try {
						if (breakpoint != null && !breakpoint.hasDependentBreakpoint()) {
							openBreakpointSelectionDialog(dependentBreakpoint.getShell());
						} else {
							breakpoint.setDependencyEnabled(true);
						}
					} catch (CoreException e1) {
						JDIDebugUIPlugin.log(e1);
					}
				} else {
					IJavaBreakpoint breakpoint = getBreakpoint();
					try {
						if (breakpoint != null && breakpoint.isDependencyEnabled()) {
							breakpoint.setDependencyEnabled(false);
						}
					} catch (CoreException e1) {
						JDIDebugUIPlugin.log(e1);
					}
				}
			}
		});

		dependentBreakpoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openBreakpointSelectionDialog(dependentBreakpoint.getShell());
			}
		});

		fSuspendThread.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setConditionTextToSuspend();
				setDirty(PROP_SUSPEND_POLICY);
			}
		});
		fSuspendVM.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setConditionTextToSuspend();
				setDirty(PROP_SUSPEND_POLICY);
			}
		});
		fDisableOnHit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean enabled = fDisableOnHit.getSelection();
				if (fBreakpoint instanceof JavaBreakpoint javaBp) {
					javaBp.setDisableOnHit(enabled);
				}
			}
		});
		composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		return composite;
	}

	private void openBreakpointSelectionDialog(Shell shell) {
		IBreakpoint[] bps = Arrays.stream(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()).filter(bp -> bp instanceof IJavaBreakpoint).filter(bp -> !bp.equals(fBreakpoint)).toArray(IBreakpoint[]::new);

		BreakpointSelectionDialog dialog = new BreakpointSelectionDialog(shell, new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Breakpoint breakpoint) {
					return fPresentation.getText(breakpoint);
				}
				return fPresentation.getText(element);
			}
		});

		dialog.setTitle(PropertyPageMessages.WaitForBreakpointSelectionTitle);
		dialog.setMessage(PropertyPageMessages.WaitForBreakpointSelectionSearch);
		dialog.setElements(bps);
		dialog.setMultipleSelection(false);

		int result = dialog.open();

		if (result == IDialogConstants.OK_ID) {

			if (dialog.getFirstResult() instanceof IJavaBreakpoint breakpoint) {

				try {
					fBreakpoint.setDependentBreakpoint(breakpoint);
					breakpoint.setDependencyBreakpoint(true);
					if (dialog.isChecked()) {
						breakpoint.setSuspendPolicy(IJavaBreakpoint.RESUME_ON_HIT);
					}
					dependentBreakpoint.setText(NLS.bind(PropertyPageMessages.WaitForBreakpointSelection, fPresentation.getText(breakpoint)));
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}

			}
		}

		if (result == IDialogConstants.CANCEL_ID) {
			try {
				fBreakpoint.setDependencyEnabled(fBreakpoint.isDependencyEnabled());
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}

		if (result == IDialogConstants.CLIENT_ID) {

			if (getBreakpoint() instanceof IJavaBreakpoint breakpoint) {
				try {
					if (breakpoint.hasDependentBreakpoint()) {
						IJavaBreakpoint existingDependentBp = breakpoint.getDependentBreakpoint();
						existingDependentBp.setDependencyBreakpoint(false);
						breakpoint.removeDependentBreakpoint();
						breakpoint.setDependencyEnabled(false);
						dependentBreakpoint.setText(PropertyPageMessages.WaitForBreakpointDefaultSelection);
						waitForBreakpoint.setSelection(false);
						dependentBreakpoint.setEnabled(false);
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}

			}
		}
	}

	private static final class BreakpointSelectionDialog extends ElementListSelectionDialog {

		private Button check;
		private boolean checked;

		public BreakpointSelectionDialog(Shell shell, LabelProvider labelProvider) {
			super(shell, labelProvider);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Control area = super.createDialogArea(parent);
			check = new Button((Composite) area, SWT.CHECK);
			check.setText(PropertyPageMessages.WaitForBreakpointSelectionResumeThread);
			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CLIENT_ID, PropertyPageMessages.WaitForBreakpointSelectionRemove, false);
			super.createButtonsForButtonBar(parent);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.CLIENT_ID) {
				setReturnCode(IDialogConstants.CLIENT_ID);
				close();
				return;
			}
			super.buttonPressed(buttonId);
		}

		@Override
		protected void okPressed() {
			checked = check.getSelection();
			super.okPressed();
		}

		public boolean isChecked() {
			return checked;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object breakpoint) throws CoreException {
		try {
			suppressPropertyChanges(true);
			if (breakpoint instanceof IJavaBreakpoint) {
				setBreakpoint((IJavaBreakpoint) breakpoint);
			} else {
				setBreakpoint(null);
			}
		} finally {
			suppressPropertyChanges(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getInput()
	 */
	@Override
	public Object getInput() {
		return fBreakpoint;
	}

	/**
	 * Sets the breakpoint to edit. The same editor can be used iteratively for different breakpoints.
	 *
	 * @param breakpoint the breakpoint to edit or <code>null</code> if none
	 * @exception CoreException if unable to access breakpoint attributes
	 */
	protected void setBreakpoint(IJavaBreakpoint breakpoint) throws CoreException {
		fBreakpoint = breakpoint;
		boolean enabled = false;
		boolean hasHitCount = false;
		String text = Util.ZERO_LENGTH_STRING;
		boolean suspendThread = true;
		boolean resumeOnHit = false;
		boolean isDisableOnHit = false;
		boolean hasDependency = false;
		boolean isWaitingEnabled = false;
		boolean enableContinueOnHit = false;
		String dependentBreakpointLabel = PropertyPageMessages.WaitForBreakpointDefaultSelection;
		if (breakpoint != null) {
			enabled = true;
			int hitCount = breakpoint.getHitCount();
			if (hitCount > 0) {
				text = Integer.toString(hitCount);
				hasHitCount = true;
			}
			suspendThread = breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD;
			resumeOnHit = breakpoint.getSuspendPolicy() == IJavaBreakpoint.RESUME_ON_HIT && isTriggerPoint();
			isDisableOnHit = breakpoint.isDisableOnHit();
			isWaitingEnabled = breakpoint.isDependencyEnabled();
			hasDependency = breakpoint.hasDependentBreakpoint();
			if (hasDependency) {
				IJavaBreakpoint depend = breakpoint.getDependentBreakpoint();
				if (depend != null) {
					dependentBreakpointLabel = NLS.bind(PropertyPageMessages.WaitForBreakpointSelection, fPresentation.getText(breakpoint.getDependentBreakpoint()));
				}
			}
			if (breakpoint.isDependencyBreakpoint() && breakpoint.getSuspendPolicy() == IJavaBreakpoint.RESUME_ON_HIT) {
				enableContinueOnHit = true;
			}
		}
		fHitCountButton.setEnabled(enabled);
		fHitCountButton.setSelection(enabled && hasHitCount);
		fHitCountText.setEnabled(hasHitCount);
		fHitCountText.setText(text);
		fSuspendThread.setEnabled(enabled);
		fSuspendVM.setEnabled(enabled);
		fResumeOnHit.setEnabled(isTriggerPoint());
		fResumeOnHit.setSelection(resumeOnHit || enableContinueOnHit);
		fSuspendThread.setSelection(suspendThread && !resumeOnHit);
		fSuspendVM.setSelection(!suspendThread && !resumeOnHit);
		fTriggerPointButton.setEnabled(enabled);
		fTriggerPointButton.setSelection(isTriggerPoint());
		fDisableOnHit.setEnabled(!isTriggerPoint() && enabled);
		fDisableOnHit.setSelection(isDisableOnHit);
		waitForBreakpoint.setEnabled(enabled);
		waitForBreakpoint.setSelection(isWaitingEnabled);
		dependentBreakpoint.setText(dependentBreakpointLabel);
		dependentBreakpoint.setEnabled(waitForBreakpoint.getSelection());
		setDirty(false);
	}

	/**
	 * Returns the current breakpoint being edited or <code>null</code> if none.
	 *
	 * @return breakpoint or <code>null</code>
	 */
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#setFocus()
	 */
	@Override
	public void setFocus() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#doSave()
	 */
	@Override
	public void doSave() throws CoreException {
		if (fBreakpoint != null) {
			int suspendPolicy = IJavaBreakpoint.SUSPEND_THREAD;
			if(fSuspendVM.getSelection()) {
				suspendPolicy = IJavaBreakpoint.SUSPEND_VM;
			}
			if (fResumeOnHit.getSelection() && fTriggerPointButton.getSelection()) {
				suspendPolicy = IJavaBreakpoint.RESUME_ON_HIT;
			}
			fBreakpoint.setSuspendPolicy(suspendPolicy);
			int hitCount = -1;
			if (fHitCountButton.getSelection()) {
				try {
					hitCount = Integer.parseInt(fHitCountText.getText());
				}
				catch (NumberFormatException e) {
					throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, e));
				}
			}
			fBreakpoint.setHitCount(hitCount);
			storeTriggerPoint(fBreakpoint);

		}
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		if (fHitCountButton.getSelection()) {
			String hitCountText= fHitCountText.getText();
			int hitCount= -1;
			try {
				hitCount = Integer.parseInt(hitCountText);
			} catch (NumberFormatException e1) {
				return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, null);
			}
			if (hitCount < 1) {
				return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, PropertyPageMessages.JavaBreakpointPage_0, null);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates and returns a check box button with the given text.
	 *
	 * @param parent parent composite
	 * @param text label
	 * @param propId property id to fire on modification
	 * @return check box
	 */
	protected Button createSusupendPropertyEditor(Composite parent, String text, final int propId) {
		Button button = new Button(parent, SWT.CHECK);
		button.setFont(parent.getFont());
		button.setText(text);
		GridData gd = new GridData(SWT.BEGINNING);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(propId);
			}
		});
		return button;
	}

	private boolean isTriggerPoint() {
		try {
			if (getBreakpoint() != null) {
				return getBreakpoint().isTriggerPoint();
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return false;

	}


	/**
	 * Stores the value of the trigger point state in the breakpoint manager.
	 *
	 * @param breakpoint
	 *            the breakpoint to be compared with trigger point in the workspace
	 * @throws CoreException
	 *             if an exception occurs while setting the enabled state
	 */
	private void storeTriggerPoint(IJavaBreakpoint breakpoint) throws CoreException {
		boolean oldSelection = breakpoint.isTriggerPoint();
		if (oldSelection == fTriggerPointButton.getSelection()) {
			return;
		}
		breakpoint.setTriggerPoint(fTriggerPointButton.getSelection());
		DebugPlugin.getDefault().getBreakpointManager().refreshTriggerpointDisplay();
	}

	private void setConditionTextToSuspend() {
		if (fResumeOnHit.isEnabled()) {
			fResumeOnHit.setSelection(false);
		}
		if (javaBpConditionEditor != null) {
			javaBpConditionEditor.setResumeOnHit(false);
			javaBpConditionEditor.updateConditionTextOnSuspend();
		}
	}

}
