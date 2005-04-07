/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Property page for configuring IJavaBreakpoints.
 */
public class JavaBreakpointPage extends PropertyPage {
	
	protected JavaElementLabelProvider fJavaLabelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	protected Button fEnabledButton;
	protected Button fHitCountButton;
	protected Button fSuspendThreadButton;
	protected Button fSuspendVMButton;
	protected Text fHitCountText;
	
	protected List fErrorMessages= new ArrayList();
	
	/**
	 * Attribute used to indicate that a breakpoint should be deleted
	 * when cancel is pressed.
	 */
	public static final String ATTR_DELETE_ON_CANCEL = JDIDebugUIPlugin.getUniqueIdentifier() + ".ATTR_DELETE_ON_CANCEL";  //$NON-NLS-1$
	
	private static final String fgHitCountErrorMessage= PropertyPageMessages.JavaBreakpointPage_0; //$NON-NLS-1$
	
	/**
	 * Store the breakpoint properties.
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IJavaBreakpoint breakpoint = getBreakpoint();
				boolean delOnCancel = breakpoint.getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null;
				if (delOnCancel) {
				    // if this breakpoint is being created, remove the "delete on cancel" attribute
				    // and register with the breakpoint manager
					breakpoint.getMarker().setAttribute(ATTR_DELETE_ON_CANCEL, (String)null);
					breakpoint.setRegistered(true);
				}
				doStore();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, null, 0, null);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(PropertyPageMessages.JavaBreakpointPage_1, e); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
		return super.performOk();
	}
	
	/**
	 * Adds the given error message to the errors currently displayed on this page.
	 * The page displays the most recently added error message.
	 * Clients should retain messages that are passed into this method as the
	 * message should later be passed into removeErrorMessage(String) to clear the error.
	 * This method should be used instead of setErrorMessage(String).
	 * @param message the error message to display on this page.
	 */
	public void addErrorMessage(String message) {
		if (message == null) {
			return;
		}
		fErrorMessages.remove(message);
		fErrorMessages.add(message);
		setErrorMessage(message);
		setValid(false);
	}
	
	/**
	 * @deprecated Call addErrorMessage(String message) instead.
	 * @see org.eclipse.jface.dialogs.DialogPage#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
	}
	
	/**
	 * Removes the given error message from the errors currently displayed on this page.
	 * When an error message is removed, the page displays the error that was added
	 * before the given message. This is akin to popping the message from a stack.
	 * Clients should call this method instead of setErrorMessage(null).
	 * @param message the error message to clear
	 */
	public void removeErrorMessage(String message) {
		fErrorMessages.remove(message);
		if (fErrorMessages.isEmpty()) {
			setErrorMessage(null);
			setValid(true);
		} else {
			setErrorMessage((String) fErrorMessages.get(fErrorMessages.size() - 1));
		}
	}
	
	/**
	 * Stores the values configured in this page. This method
	 * should be called from within a workspace runnable to
	 * reduce the number of resource deltas.
	 */
	protected void doStore() throws CoreException {
		IJavaBreakpoint breakpoint= getBreakpoint();
		storeSuspendPolicy(breakpoint);
		storeHitCount(breakpoint);
		storeEnabled(breakpoint);
	}

	/**
	 * Stores the value of the enabled state in the breakpoint.
	 * @param breakpoint the breakpoint to update
	 * @throws CoreException if an exception occurs while setting
	 *  the enabled state
	 */
	private void storeEnabled(IJavaBreakpoint breakpoint) throws CoreException {
		boolean enabled= fEnabledButton.getSelection();
		breakpoint.setEnabled(enabled);
	}

	/**
	 * Stores the value of the hit count in the breakpoint.
	 * @param breakpoint the breakpoint to update
	 * @throws CoreException if an exception occurs while setting
	 *  the hit count
	 */
	private void storeHitCount(IJavaBreakpoint breakpoint) throws CoreException {
		boolean hitCountEnabled= fHitCountButton.getSelection();
		int hitCount= -1;
		if (hitCountEnabled) {
			try {
				hitCount= Integer.parseInt(fHitCountText.getText());
			} catch (NumberFormatException e) {
				JDIDebugUIPlugin.log(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, MessageFormat.format(PropertyPageMessages.JavaBreakpointPage_2, new String[] { fHitCountText.getText() }), e)); //$NON-NLS-1$
			}
		}
		breakpoint.setHitCount(hitCount);
	}

	/**
	 * Stores the value of the suspend policy in the breakpoint.
	 * @param breakpoint the breakpoint to update
	 * @throws CoreException if an exception occurs while setting the suspend policy
	 */
	private void storeSuspendPolicy(IJavaBreakpoint breakpoint) throws CoreException {
		int suspendPolicy= IJavaBreakpoint.SUSPEND_VM;
		if (fSuspendThreadButton.getSelection()) {
			suspendPolicy= IJavaBreakpoint.SUSPEND_THREAD;
		}
		breakpoint.setSuspendPolicy(suspendPolicy);
	}

	/**
	 * Creates the labels and editors displayed for the breakpoint.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite mainComposite= createComposite(parent, 1);
		createLabels(mainComposite);
		try {
			createEnabledButton(mainComposite);
			createHitCountEditor(mainComposite);
			createTypeSpecificEditors(mainComposite);
			createSuspendPolicyEditor(mainComposite); // Suspend policy is considered uncommon. Add it last.
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		setValid(true);
		// if this breakpoint is being created, change the shell title to indicate 'creation'
		try {
            if (getBreakpoint().getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null) {
            	getShell().addShellListener(new ShellListener() {
                    public void shellActivated(ShellEvent e) {
                        Shell shell = (Shell)e.getSource();
                        shell.setText(MessageFormat.format(PropertyPageMessages.JavaBreakpointPage_10, new String[]{getName(getBreakpoint())})); //$NON-NLS-1$
                        shell.removeShellListener(this);
                    }
                    public void shellClosed(ShellEvent e) {
                    }
                    public void shellDeactivated(ShellEvent e) {
                    }
                    public void shellDeiconified(ShellEvent e) {
                    }
                    public void shellIconified(ShellEvent e) {
                    }
                });
            }
        } catch (CoreException e) {
        }
		return mainComposite;
	}
	
    /**
     * Returns the name of the given element.
     * 
     * @param element
     *            the element
     * @return the name of the element
     */
    private String getName(IAdaptable element) {
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) element
                .getAdapter(IWorkbenchAdapter.class);
        if (adapter != null) {
            return adapter.getLabel(element);
        } 
        return "";//$NON-NLS-1$
    }	
	
	/**
	 * Creates the labels displayed for the breakpoint.
	 * @param parent
	 */
	protected void createLabels(Composite parent) {
		IJavaBreakpoint breakpoint= (IJavaBreakpoint) getElement();
		Composite labelComposite= createComposite(parent, 2);
		try {
			String typeName = breakpoint.getTypeName();
			if (typeName != null) {
				createLabel(labelComposite, PropertyPageMessages.JavaBreakpointPage_3); //$NON-NLS-1$
				createLabel(labelComposite, typeName);
			}
			createTypeSpecificLabels(labelComposite);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
	}

	/**
	 * Creates the editor for configuring the suspend policy (suspend
	 * VM or suspend thread) of the breakpoint.
	 * @param parent the composite in which the suspend policy
	 * 		editor will be created.
	 */
	private void createSuspendPolicyEditor(Composite parent) throws CoreException {
		IJavaBreakpoint breakpoint= getBreakpoint();
		createLabel(parent, PropertyPageMessages.JavaBreakpointPage_6); //$NON-NLS-1$
		boolean suspendThread= breakpoint.getSuspendPolicy() == IJavaBreakpoint.SUSPEND_THREAD;
		Composite radioComposite= createComposite(parent, 2);
		fSuspendThreadButton= createRadioButton(radioComposite, PropertyPageMessages.JavaBreakpointPage_7); //$NON-NLS-1$
		fSuspendThreadButton.setSelection(suspendThread);
		fSuspendVMButton= createRadioButton(radioComposite, PropertyPageMessages.JavaBreakpointPage_8); //$NON-NLS-1$
		fSuspendVMButton.setSelection(!suspendThread);
	}

	/**
	 * @param parent the composite in which the hit count editor
	 * 		will be created
	 */
	private void createHitCountEditor(Composite parent) throws CoreException {
		IJavaBreakpoint breakpoint= getBreakpoint();
		Composite hitCountComposite= createComposite(parent, 2);
		fHitCountButton= createCheckButton(hitCountComposite, PropertyPageMessages.JavaBreakpointPage_4); //$NON-NLS-1$
		fHitCountButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				fHitCountText.setEnabled(fHitCountButton.getSelection());
				hitCountChanged();
			}
		});
		int hitCount= breakpoint.getHitCount();
		String hitCountString= ""; //$NON-NLS-1$
		if (hitCount > 0) {
			hitCountString= new Integer(hitCount).toString();
			fHitCountButton.setSelection(true);
		} else {
			fHitCountButton.setSelection(false);
		}
		fHitCountText= createText(hitCountComposite, hitCountString); //$NON-NLS-1$
		if (hitCount <= 0) {
			fHitCountText.setEnabled(false);
		}
		fHitCountText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				hitCountChanged();
			}
		});
	}
	
	/**
	 * Validates the current state of the hit count editor.
	 * Hit count value must be a positive integer.
	 */
	private void hitCountChanged() {
		if (!fHitCountButton.getSelection()) {
			removeErrorMessage(fgHitCountErrorMessage);
			return;
		}
		String hitCountText= fHitCountText.getText();
		int hitCount= -1;
		try {
			hitCount= Integer.parseInt(hitCountText);
		} catch (NumberFormatException e1) {
			addErrorMessage(fgHitCountErrorMessage);
			return;
		}
		if (hitCount < 1) {
			addErrorMessage(fgHitCountErrorMessage);
		} else {
			if (fgHitCountErrorMessage.equals(getErrorMessage())) {
				removeErrorMessage(fgHitCountErrorMessage);
			}
		}
	}

	/**
	 * Creates the button to toggle enablement of the breakpoint
	 * @param parent
	 * @throws CoreException
	 */
	protected void createEnabledButton(Composite parent) throws CoreException {
		fEnabledButton= createCheckButton(parent, PropertyPageMessages.JavaBreakpointPage_5); //$NON-NLS-1$
		fEnabledButton.setSelection(getBreakpoint().isEnabled());
	}
	
	/**
	 * Returns the breakpoint that this preference page configures
	 * @return the breakpoint this page configures
	 */
	protected IJavaBreakpoint getBreakpoint() {
		return (IJavaBreakpoint) getElement();
	}
	
	/**
	 * Allows subclasses to add type specific labels to the common Java
	 * breakpoint page.
	 * @param parent
	 */
	protected void createTypeSpecificLabels(Composite parent) {
		// Do nothing
	}
	/**
	* Allows subclasses to add type specific editors to the common Java
	* breakpoint page.
	* @param parent
	*/
   protected void createTypeSpecificEditors(Composite parent) throws CoreException {
   		// Do nothing
   }
	
	/**
	 * Creates a fully configured text editor with the given initial value
	 * @param parent
	 * @param initialValue
	 * @return the configured text editor
	 */
	protected Text createText(Composite parent, String initialValue) {
		Composite textComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		textComposite.setLayout(layout);
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textComposite.setFont(parent.getFont());
		Text text= new Text(textComposite, SWT.SINGLE | SWT.BORDER);
		text.setText(initialValue);
		text.setFont(parent.getFont());
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}
	
	/**
	 * Creates a fully configured composite with the given number of columns
	 * @param parent
	 * @param numColumns
	 * @return the configured composite
	 */
	protected Composite createComposite(Composite parent, int numColumns) {
		Composite composit= new Composite(parent, SWT.NONE);
		composit.setFont(parent.getFont());
		GridLayout layout= new GridLayout();
		layout.numColumns= numColumns;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composit.setLayout(layout);
		composit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composit;
	}

	/**
	 * Creates a fully configured check button with the given text.
	 * @param parent the parent composite
	 * @param text the label of the returned check button
	 * @return a fully configured check button
	 */
	protected Button createCheckButton(Composite parent, String text) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(text);
		button.setFont(parent.getFont());
		button.setLayoutData(new GridData());
		return button;
	}

	/**
	 * Creates a fully configured label with the given text.
	 * @param parent the parent composite
	 * @param text the test of the returned label
	 * @return a fully configured label
	 */
	protected Label createLabel(Composite parent, String text) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(text);
		label.setFont(parent.getFont());
		label.setLayoutData(new GridData());
		return label;
	}

	/**
	 * Creates a fully configured radio button with the given text.
	 * @param parent the parent composite
	 * @param text the label of the returned radio button
	 * @return a fully configured radio button
	 */
	protected Button createRadioButton(Composite parent, String text) {
		Button button= new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(text);
		button.setFont(parent.getFont());
		button.setLayoutData(new GridData());
		return button;
	}
	
	/**
	 * Check to see if the breakpoint should be deleted.
	 */
	public boolean performCancel() {
		try {
			if (getBreakpoint().getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null) {
			    // if this breakpoint is being created, delete on cancel
				getBreakpoint().delete();
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog(PropertyPageMessages.JavaBreakpointPage_9, e.getStatus()); //$NON-NLS-1$
		}
		return super.performCancel();
	}
}
