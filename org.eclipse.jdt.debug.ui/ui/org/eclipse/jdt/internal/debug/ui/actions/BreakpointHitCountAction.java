package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class BreakpointHitCountAction extends Action implements IViewActionDelegate {

	private static final String INITIAL_VALUE= "1"; //$NON-NLS-1$

	protected IStructuredSelection fCurrentSelection;

	/**
	 * A dialog that sets the focus to the text area.
	 */
	class HitCountDialog extends InputDialog {
		
		private Button fEnabledButton;
		private boolean fHitCountEnabled;
		
		protected  HitCountDialog(Shell parentShell,
									String dialogTitle,
									String dialogMessage,
									String initialValue,
									IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		/**
		 * @see Window#close()
		 */
		public boolean close() {
			setHitCountEnabled(getEnabledButton().getSelection());
			return super.close();
		}
		/**
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite area= (Composite)super.createDialogArea(parent);
			Button b= new Button(area, SWT.CHECK);
			GridData data = new GridData(
				GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);;
			b.setLayoutData(data);
			b.setFont(parent.getFont());
			b.setText(ActionMessages.getString("BreakpointHitCountAction.Enable_Hit_Count_1")); //$NON-NLS-1$
			b.setSelection(true);
			b.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					boolean enabled= getEnabledButton().getSelection();
					getText().setEnabled(enabled);
					if (enabled) {
						validateInput();
					} else {
						getOkButton().setEnabled(true);
						getErrorMessageLabel().setText(""); //$NON-NLS-1$
					}
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			setEnabledButton(b);
			return area;
		}

		protected Button getEnabledButton() {
			return fEnabledButton;
		}

		protected void setEnabledButton(Button enabledButton) {
			fEnabledButton = enabledButton;
		}

		protected boolean isHitCountEnabled() {
			return fHitCountEnabled;
		}

		protected void setHitCountEnabled(boolean hitCountEnabled) {
			fHitCountEnabled = hitCountEnabled;
		}

}
	
	public BreakpointHitCountAction() {
		setEnabled(false);
	}

	/**
	 * Returns the plugin's breakpoint manager
	 */
	protected IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getStructuredSelection();
		Iterator enum= selection.iterator();
		if (!enum.hasNext()) {
			return;
		}

		while (enum.hasNext()) {
			IJavaBreakpoint breakpoint= (IJavaBreakpoint)enum.next();
			try {
				int oldHitCount= breakpoint.getHitCount();
				int newHitCount= hitCountDialog(breakpoint);
				if (newHitCount != -1) {					
					if (oldHitCount == newHitCount && newHitCount == 0) {
						return;
					}
					breakpoint.setHitCount(newHitCount);
				}
			} catch (CoreException ce) {
				JDIDebugUIPlugin.logError(ce);
			}
		}
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		run(null);
	}

	protected int hitCountDialog(IJavaBreakpoint breakpoint) {
		String title= ActionMessages.getString("BreakpointHitCountAction.Set_Breakpoint_Hit_Count_2"); //$NON-NLS-1$
		String message= ActionMessages.getString("BreakpointHitCountAction.&Enter_the_new_hit_count_for_the_breakpoint__3"); //$NON-NLS-1$
		IInputValidator validator= new IInputValidator() {
			int hitCount= -1;
			public String isValid(String value) {
				try {
					hitCount= Integer.valueOf(value.trim()).intValue();
				} catch (NumberFormatException nfe) {
					hitCount= -1;
				}
				if (hitCount < 1) {
					return ActionMessages.getString("BreakpointHitCountAction.Value_is_not_a_valid_hit_count_4"); //$NON-NLS-1$
				}
				//no error
				return null;
			}
		};

		int currentHitCount= 0;
		try {
			currentHitCount = breakpoint.getHitCount();
		} catch (CoreException e) {
		}
		String initialValue;
		if (currentHitCount > 0) {
			initialValue= Integer.toString(currentHitCount);
		} else {
			initialValue= INITIAL_VALUE;
		}
		Shell activeShell= JDIDebugUIPlugin.getActiveWorkbenchWindow().getShell();
		HitCountDialog dialog= new HitCountDialog(activeShell, title, message, initialValue, validator);
		if (dialog.open() != dialog.OK) {
			return -1;
		}
		if (dialog.isHitCountEnabled()) {
			return Integer.parseInt(dialog.getValue().trim());
		} else {
			return 0;
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fCurrentSelection= (IStructuredSelection)sel;
			Object[] elements= fCurrentSelection.toArray();
			action.setEnabled(elements.length == 1 && isEnabledFor(elements[0]));
		}
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}

	protected IStructuredSelection getStructuredSelection() {
		return fCurrentSelection;
	}

	public boolean isEnabledFor(Object element) {
		return element instanceof IJavaBreakpoint;
	}

}
