/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.debug.ui.breakpoints.JavaBreakpointConditionEditor;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaClassPrepareBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.breakpoints.AbstractJavaBreakpointEditor;
import org.eclipse.jdt.internal.debug.ui.breakpoints.CompositeBreakpointEditor;
import org.eclipse.jdt.internal.debug.ui.breakpoints.ExceptionBreakpointEditor;
import org.eclipse.jdt.internal.debug.ui.breakpoints.MethodBreakpointEditor;
import org.eclipse.jdt.internal.debug.ui.breakpoints.StandardJavaBreakpointEditor;
import org.eclipse.jdt.internal.debug.ui.breakpoints.WatchpointEditor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Property page for configuring IJavaBreakpoints.
 */
public class JavaBreakpointPage extends PropertyPage {

	protected JavaElementLabelProvider fJavaLabelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	protected Button fEnabledButton;
	/*
	 * protected Button fTriggerPointButton; protected Button fTriggerPointButtonActive;
	 */
	protected List<String> fErrorMessages= new ArrayList<>();
	protected String fPrevMessage = null;
	private AbstractJavaBreakpointEditor fEditor;

	/**
	 * Attribute used to indicate that a breakpoint should be deleted
	 * when cancel is pressed.
	 */
	public static final String ATTR_DELETE_ON_CANCEL = JDIDebugUIPlugin.getUniqueIdentifier() + ".ATTR_DELETE_ON_CANCEL";  //$NON-NLS-1$

	/**
	 * Attribute used to indicate resetting the enable attribute if cancel is pressed.
	 */
	public static final String ATTR_ENABLED_SETTING_ON_CANCEL = JDIDebugUIPlugin.getUniqueIdentifier() + ".ATTR_ENABLED_SETTING_ON_CANCEL"; //$NON-NLS-1$

	/**
	 * Constant for the empty string
	 */
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Store the breakpoint properties.
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IJavaBreakpoint breakpoint = getBreakpoint();
				boolean delOnCancel = breakpoint.getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null;
				if (delOnCancel) {
				    // if this breakpoint is being created, remove the "delete on cancel" attribute
				    // and register with the breakpoint manager
					breakpoint.getMarker().setAttribute(ATTR_DELETE_ON_CANCEL, (String)null);
					breakpoint.setRegistered(true);
				}
				breakpoint.getMarker().setAttribute(ATTR_ENABLED_SETTING_ON_CANCEL, (String) null);
				doStore();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wr, null, 0, null);
		}
		catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(e.getStatus());
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
	protected void addErrorMessage(String message) {
		fErrorMessages.remove(message);
		fErrorMessages.add(message);
		setErrorMessage(message);
		setValid(message == null);
	}

	/**
	 * Removes the given error message from the errors currently displayed on this page.
	 * When an error message is removed, the page displays the error that was added
	 * before the given message. This is akin to popping the message from a stack.
	 * Clients should call this method instead of setErrorMessage(null).
	 * @param message the error message to clear
	 */
	protected void removeErrorMessage(String message) {
		fErrorMessages.remove(message);
		if (fErrorMessages.isEmpty()) {
			addErrorMessage(null);
		} else {
			addErrorMessage(fErrorMessages.get(fErrorMessages.size() - 1));
		}
	}

	/**
	 * Stores the values configured in this page. This method
	 * should be called from within a workspace runnable to
	 * reduce the number of resource deltas.
	 * @throws CoreException if an exception occurs
	 */
	protected void doStore() throws CoreException {
		IJavaBreakpoint breakpoint = getBreakpoint();
		// storeTriggerPoint(breakpoint);
		// storeTriggerPointActive(breakpoint);
		storeEnabled(breakpoint);
		if (fEditor.isDirty()) {
			fEditor.doSave();
		}
	}

	/**
	 * Stores the value of the enabled state in the breakpoint.
	 * @param breakpoint the breakpoint to update
	 * @throws CoreException if an exception occurs while setting
	 *  the enabled state
	 */
	private void storeEnabled(IJavaBreakpoint breakpoint) throws CoreException {
		breakpoint.setEnabled(fEnabledButton.getSelection());
	}

	/**
	 * Creates the labels and editors displayed for the breakpoint.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite mainComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL, 0, 0);
		createLabels(mainComposite);
		createLabel(mainComposite, ""); //$NON-NLS-1$ // spacer

		Composite composite = SWTFactory.createComposite(mainComposite, parent.getFont(), 4, 1, 0, 0, 0);
		createEnabledButton(composite);

		createTypeSpecificEditors(mainComposite);
		setValid(true);
		// if this breakpoint is being created, change the shell title to indicate 'creation'
		try {
            if (getBreakpoint().getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null) {
            	getShell().addShellListener(new ShellListener() {
                    @Override
					public void shellActivated(ShellEvent e) {
                        Shell shell = (Shell)e.getSource();
						shell.setText(NLS.bind(PropertyPageMessages.JavaBreakpointPage_10, getName(getBreakpoint())));
                        shell.removeShellListener(this);
                    }
                    @Override
					public void shellClosed(ShellEvent e) {
                    }
                    @Override
					public void shellDeactivated(ShellEvent e) {
                    }
                    @Override
					public void shellDeiconified(ShellEvent e) {
                    }
                    @Override
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
     * @param element the element
     * @return the name of the element
     */
    private String getName(IAdaptable element) {
		IWorkbenchAdapter adapter = element.getAdapter(IWorkbenchAdapter.class);
        if (adapter != null) {
            return adapter.getLabel(element);
        }
        return EMPTY_STRING;
    }

	/**
	 * Creates the labels displayed for the breakpoint.
	 * @param parent the parent composite
	 */
	protected void createLabels(Composite parent) {
		Composite labelComposite = SWTFactory.createComposite(parent, parent.getFont(), 2, 1, GridData.FILL_HORIZONTAL, 0, 0);
		try {
			String typeName = ((IJavaBreakpoint) getElement()).getTypeName();
			if (typeName != null) {
				createLabel(labelComposite, PropertyPageMessages.JavaBreakpointPage_3);
				Text text = SWTFactory.createText(labelComposite, SWT.READ_ONLY, 1, typeName);
				text.setBackground(parent.getBackground());
			}
			createTypeSpecificLabels(labelComposite);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
	}

	/**
	 * Creates the button to toggle enablement of the breakpoint
	 * @param parent the parent composite
	 */
	protected void createEnabledButton(Composite parent) {
		fEnabledButton = createCheckButton(parent, PropertyPageMessages.JavaBreakpointPage_5);
		try {
			fEnabledButton.setSelection(getBreakpoint().isEnabled());
		}
		catch(CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
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
	 * @param parent the parent composite
	 */
	protected void createTypeSpecificLabels(Composite parent) {
		// Line number
		IJavaBreakpoint jb = getBreakpoint();
		if (jb instanceof IJavaLineBreakpoint) {
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) jb;
			StringBuilder lineNumber = new StringBuilder(4);
			try {
				int lNumber = breakpoint.getLineNumber();
				if (lNumber > 0) {
					lineNumber.append(lNumber);
				}
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce);
			}
			if (lineNumber.length() > 0) {
				createLabel(parent, PropertyPageMessages.JavaLineBreakpointPage_2);
				Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, lineNumber.toString());
				text.setBackground(parent.getBackground());
			}
			// Member
			try {
				IMember member = BreakpointUtils.getMember(breakpoint);
				if (member == null) {
					return;
				}
				String label = PropertyPageMessages.JavaLineBreakpointPage_3;
				if (breakpoint instanceof IJavaMethodBreakpoint) {
					label = PropertyPageMessages.JavaLineBreakpointPage_4;
				} else if (breakpoint instanceof IJavaWatchpoint) {
					label = PropertyPageMessages.JavaLineBreakpointPage_5;
				}
				createLabel(parent, label);
				Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1, fJavaLabelProvider.getText(member));
				text.setBackground(parent.getBackground());
			}
			catch (CoreException exception) {JDIDebugUIPlugin.log(exception);}
		}
	}

	/**
	* Allows subclasses to add type specific editors to the common Java
	* breakpoint page.
	* @param parent the parent composite
	*/
   protected void createTypeSpecificEditors(Composite parent) {
	   try {
		String type = getBreakpoint().getMarker().getType();
		if (JavaClassPrepareBreakpoint.JAVA_CLASS_PREPARE_BREAKPOINT.equals(type)) {
			setTitle(PropertyPageMessages.JavaBreakpointPage_11);
			fEditor = new StandardJavaBreakpointEditor();
		} else if (JavaLineBreakpoint.JAVA_LINE_BREAKPOINT.equals(type)) {
			setTitle(PropertyPageMessages.JavaLineBreakpointPage_18);
			JavaBreakpointConditionEditor javaBpConditionEditor = new JavaBreakpointConditionEditor(null);
			fEditor = new CompositeBreakpointEditor(new AbstractJavaBreakpointEditor[]
			{ new StandardJavaBreakpointEditor(javaBpConditionEditor), javaBpConditionEditor });
		} else if (JavaExceptionBreakpoint.JAVA_EXCEPTION_BREAKPOINT.equals(type)) {
			setTitle(PropertyPageMessages.JavaExceptionBreakpointPage_5);
			fEditor = new ExceptionBreakpointEditor();
		} else if (JavaWatchpoint.JAVA_WATCHPOINT.equals(type)) {
			setTitle(PropertyPageMessages.JavaLineBreakpointPage_19);
				fEditor = new CompositeBreakpointEditor(new AbstractJavaBreakpointEditor[] { new WatchpointEditor(),
						new JavaBreakpointConditionEditor(null) });
		} else if (JavaMethodBreakpoint.JAVA_METHOD_BREAKPOINT.equals(type)) {
			setTitle(PropertyPageMessages.JavaLineBreakpointPage_20);
			fEditor = new CompositeBreakpointEditor(new AbstractJavaBreakpointEditor[]
			    {new MethodBreakpointEditor(), new JavaBreakpointConditionEditor(null)});
		} else {
			// use standard editor for any other kind of breakpoint (@see bug 325161)
			fEditor = new StandardJavaBreakpointEditor();
		}
		fEditor.createControl(parent);
		fEditor.addPropertyListener(new IPropertyListener() {
			@Override
			public void propertyChanged(Object source, int propId) {
				IStatus status = fEditor.getStatus();
				if (status.isOK()) {
					if (fPrevMessage != null) {
						removeErrorMessage(fPrevMessage);
						fPrevMessage = null;
					}
				} else {
					fPrevMessage = status.getMessage();
					addErrorMessage(fPrevMessage);
				}
			}
		});
		fEditor.setInput(getBreakpoint());
	} catch (CoreException e) {
		setErrorMessage(e.getMessage());
	}
   }

	/**
	 * Creates a fully configured text editor with the given initial value
	 * @param parent the parent composite
	 * @param initialValue the initial {@link String} value
	 * @return the configured text editor
	 */
	protected Text createText(Composite parent, String initialValue) {
		return SWTFactory.createText(parent, SWT.SINGLE | SWT.BORDER, 1, initialValue);
	}

	/**
	 * Creates a fully configured check button with the given text.
	 * @param parent the parent composite
	 * @param text the label of the returned check button
	 * @return a fully configured check button
	 */
	protected Button createCheckButton(Composite parent, String text) {
		return SWTFactory.createCheckButton(parent, text, null, false, 1);
	}

	/**
	 * Creates a fully configured label with the given text.
	 * @param parent the parent composite
	 * @param text the test of the returned label
	 * @return a fully configured label
	 */
	protected Label createLabel(Composite parent, String text) {
		return SWTFactory.createLabel(parent, text, 1);
	}

	/**
	 * Creates a fully configured radio button with the given text.
	 * @param parent the parent composite
	 * @param text the label of the returned radio button
	 * @return a fully configured radio button
	 */
	protected Button createRadioButton(Composite parent, String text) {
		return SWTFactory.createRadioButton(parent, text, 1);
	}

	/**
	 * Check to see if the breakpoint should be deleted.
	 * @return <code>true</code> if the page was canceled, <code>false</code> othewise
	 */
	@Override
	public boolean performCancel() {
		try {
			String enableSetting = getBreakpoint().getMarker().getAttribute(ATTR_ENABLED_SETTING_ON_CANCEL, null);
			if (enableSetting != null) {
				// if this is an old breakpoint we must reset enablement setting
				boolean enabled = Boolean.parseBoolean(enableSetting);
				getBreakpoint().setEnabled(enabled);
			}
			if (getBreakpoint().getMarker().getAttribute(ATTR_DELETE_ON_CANCEL) != null) {
			    // if this breakpoint is being created, delete on cancel
				getBreakpoint().delete();
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(PropertyPageMessages.JavaBreakpointPage_9, e.getStatus());
		}
		return super.performCancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_BREAKPOINT_PROPERTY_PAGE);
	}
}
