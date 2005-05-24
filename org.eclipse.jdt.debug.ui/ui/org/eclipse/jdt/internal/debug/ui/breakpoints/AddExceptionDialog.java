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
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;

import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;

/**
 * A dialog to create an exception breakpoint
 */
public class AddExceptionDialog extends TypeSelectionDialog2 {

    private Button fCaughtButton;

    private Button fUncaughtButton;

    private IJavaExceptionBreakpoint[] fExisting;

    /** The dialog location. */
    private Point fLocation;

    /** The dialog size. */
    private Point fSize;

    public static final int CHECKED_EXCEPTION = 0;

    public static final int UNCHECKED_EXCEPTION = 1;

    public static final int NO_EXCEPTION = -1;

    private static final String DIALOG_SETTINGS = "AddExceptionDialog"; //$NON-NLS-1$

    private static final String SETTING_CAUGHT_CHECKED = "caughtChecked"; //$NON-NLS-1$

    private static final String SETTING_UNCAUGHT_CHECKED = "uncaughtChecked"; //$NON-NLS-1$

    public AddExceptionDialog(Shell parent, IRunnableContext context) {
        super(parent, false, context, null, IJavaSearchConstants.CLASS);
        setFilter("*Exception*", CARET_BEGINNING); //$NON-NLS-1$
    }

    /*
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaDebugHelpContextIds.ADD_EXCEPTION_DIALOG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    public Control createDialogArea(Composite parent) {
        Composite contents = (Composite) super.createDialogArea(parent);
        createExceptionArea(contents);
        return contents;
    }

    /**
     * Create an area to mark as caught/uncaught
     * 
     * @param contents
     *            area to create controls in
     */
    private void createExceptionArea(Composite contents) {
        IDialogSettings section = getDialogSettings();
        boolean c = section.getBoolean(SETTING_CAUGHT_CHECKED);
        boolean u = section.getBoolean(SETTING_UNCAUGHT_CHECKED);

        fCaughtButton = new Button(contents, SWT.CHECK);
        fCaughtButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fCaughtButton.setFont(contents.getFont());
        fCaughtButton.setText(BreakpointMessages.AddExceptionDialog_15); //$NON-NLS-1$
        fCaughtButton.setSelection(c);

        fUncaughtButton = new Button(contents, SWT.CHECK);
        fUncaughtButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fUncaughtButton.setFont(contents.getFont());
        fUncaughtButton.setText(BreakpointMessages.AddExceptionDialog_16); //$NON-NLS-1$
        fUncaughtButton.setSelection(u);
    }

    protected boolean createBreakpoint() {
    	TypeInfo[] selection= getSelectedTypes();
    	// can only happen when the dash line is selected.
    	if (selection.length != 1)
    		return false;
        TypeInfo typeRef = selection[0];
        IType type = null;
        try {
            type = typeRef.resolveType(SearchEngine.createWorkspaceScope());
        } catch (JavaModelException e) {
            updateStatus(e.getStatus());
            return false;
        }
        if (type == null) {
            updateStatus(new StatusInfo(IStatus.ERROR, BreakpointMessages.AddExceptionDialog_17)); //$NON-NLS-1$
            return false;
        }
        final int exType = getExceptionType(type);

        if (exType == NO_EXCEPTION) {
            updateStatus(new StatusInfo(IStatus.ERROR, BreakpointMessages.AddExceptionDialog_17)); //$NON-NLS-1$
            return false;
        }

        final Map attributes = new HashMap(10);
        BreakpointUtils.addJavaBreakpointAttributes(attributes, type);

        final IType finalType = type;
        final boolean caught = fCaughtButton.getSelection();
        final boolean uncaught = fUncaughtButton.getSelection();
        new Job(BreakpointMessages.AddExceptionDialog_0) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    IJavaExceptionBreakpoint breakpoint = JDIDebugModel.createExceptionBreakpoint(BreakpointUtils.getBreakpointResource(finalType), finalType.getFullyQualifiedName(), caught, uncaught, exType == CHECKED_EXCEPTION, true, attributes);
                    final List list = new ArrayList(1);
                    list.add(breakpoint);
                    Runnable r = new Runnable() {
                        public void run() {
                            IViewPart part = JDIDebugUIPlugin.getActivePage().findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
                            if (part instanceof IDebugView) {
                                Viewer viewer = ((IDebugView) part).getViewer();
                                if (viewer instanceof StructuredViewer) {
                                    StructuredViewer sv = (StructuredViewer) viewer;
                                    sv.setSelection(new StructuredSelection(list), true);
                                }
                            }
                        }
                    };
                    JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);

                    return Status.OK_STATUS;
                } catch (CoreException e) {
                    updateStatus(e.getStatus());
                    return Status.CANCEL_STATUS;
                }

            }
        }.schedule();
        return true;
    }

    protected boolean validateBreakpoint() {
    	TypeInfo[] selection= getSelectedTypes();
    	if (selection.length != 1)
    		return false;
        TypeInfo typeRef = selection[0];
        if (typeRef == null) {
            return false;
        }
        IType type = null;
        try {
            type = typeRef.resolveType(SearchEngine.createWorkspaceScope());
        } catch (JavaModelException e) {
            updateStatus(e.getStatus());
            return false;
        }
        if (type == null) {
            updateStatus(new StatusInfo(IStatus.ERROR, BreakpointMessages.AddExceptionDialog_17)); //$NON-NLS-1$
            return false;
        }
        int exType = getExceptionType(type);

        if (exType == NO_EXCEPTION) {
            updateStatus(new StatusInfo(IStatus.ERROR, BreakpointMessages.AddExceptionDialog_17)); //$NON-NLS-1$
            return false;
        }

        String name = type.getFullyQualifiedName();
        IJavaExceptionBreakpoint[] breakpoints = getExistingBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            IJavaExceptionBreakpoint breakpoint = breakpoints[i];
            try {
                if (breakpoint.getTypeName().equals(name)) {
                    updateStatus(new StatusInfo(IStatus.INFO, BreakpointMessages.AddExceptionDialog_21)); //$NON-NLS-1$
                    return false;
                }
            } catch (CoreException e) {
                updateStatus(e.getStatus());
                return false;
            }
        }
        updateStatus(new StatusInfo(IStatus.OK, null));
        return true;
    }

    public static int getExceptionType(final IType type) {
        final int[] exceptionType = new int[1];
        exceptionType[0] = NO_EXCEPTION;

        BusyIndicatorRunnableContext context = new BusyIndicatorRunnableContext();
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor pm) {
                try {
                    ITypeHierarchy hierarchy = type.newSupertypeHierarchy(pm);
                    IType curr = type;
                    while (curr != null) {
                        String name = JavaModelUtil.getFullyQualifiedName(curr);

                        if ("java.lang.Throwable".equals(name)) { //$NON-NLS-1$
                            exceptionType[0] = CHECKED_EXCEPTION;
                            return;
                        }
                        if ("java.lang.RuntimeException".equals(name) || "java.lang.Error".equals(name)) { //$NON-NLS-2$ //$NON-NLS-1$
                            exceptionType[0] = UNCHECKED_EXCEPTION;
                            return;
                        }
                        curr = hierarchy.getSuperclass(curr);
                    }
                } catch (JavaModelException e) {
                    JDIDebugUIPlugin.log(e);
                }
            }
        };
        try {
            context.run(false, false, runnable);
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
            JDIDebugUIPlugin.log(e);
        }
        return exceptionType[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        if (createBreakpoint()) {
            super.okPressed();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#updateOkState()
     */
    protected void updateOkState() {
        getButton(IDialogConstants.OK_ID).setEnabled(validateBreakpoint());
    }

    /**
     * Stores it current configuration in the dialog store.
     */
    private void writeSettings() {
        IDialogSettings s = getDialogSettings();

        Point location = getShell().getLocation();
        s.put("x", location.x); //$NON-NLS-1$
        s.put("y", location.y); //$NON-NLS-1$

        Point size = getShell().getSize();
        s.put("width", size.x); //$NON-NLS-1$
        s.put("height", size.y); //$NON-NLS-1$

        s.put(SETTING_CAUGHT_CHECKED, fCaughtButton.getSelection());
        s.put(SETTING_UNCAUGHT_CHECKED, fUncaughtButton.getSelection());
    }

    /**
     * Returns the dialog settings object used to share state between several
     * find/replace dialogs.
     * 
     * @return the dialog settings to be used
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings allSetttings = JDIDebugUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = allSetttings.getSection(DIALOG_SETTINGS);
        if (section == null) {
            section = allSetttings.addNewSection(DIALOG_SETTINGS);
            section.put(SETTING_CAUGHT_CHECKED, true);
            section.put(SETTING_UNCAUGHT_CHECKED, true);
        }
        return section;
    }

    /**
     * Initializes itself from the dialog settings with the same state as at the
     * previous invocation.
     */
    private void readSettings() {
        IDialogSettings s = getDialogSettings();
        try {
            int x = s.getInt("x"); //$NON-NLS-1$
            int y = s.getInt("y"); //$NON-NLS-1$
            fLocation = new Point(x, y);
        } catch (NumberFormatException e) {
            fLocation = null;
        }
        try {
            int width = s.getInt("width"); //$NON-NLS-1$
            int height = s.getInt("height"); //$NON-NLS-1$
            fSize = new Point(width, height);
        } catch (NumberFormatException e) {
        	// use an acceptable default size for the dialog
            fSize = new Point(420, 460);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#getInitialSize()
     */
    protected Point getInitialSize() {
        Point result = super.getInitialSize();
        if (fSize != null) {
            result.x = Math.max(result.x, fSize.x);
            result.y = Math.max(result.y, fSize.y);
            Rectangle display = getShell().getDisplay().getClientArea();
            result.x = Math.min(result.x, display.width);
            result.y = Math.min(result.y, display.height);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    protected Point getInitialLocation(Point initialSize) {
        Point result = super.getInitialLocation(initialSize);
        if (fLocation != null) {
            result.x = fLocation.x;
            result.y = fLocation.y;
            Rectangle display = getShell().getDisplay().getClientArea();
            int xe = result.x + initialSize.x;
            if (xe > display.width) {
                result.x -= xe - display.width;
            }
            int ye = result.y + initialSize.y;
            if (ye > display.height) {
                result.y -= ye - display.height;
            }
        }
        return result;
    }

    /*
     * @see Window#close()
     */
    public boolean close() {
        writeSettings();
        return super.close();
    }

    /*
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        readSettings();
        return control;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
     */
    protected void computeResult() {
        // result is computed when OK is pressed in #createBreakpoint
    }

    protected IJavaExceptionBreakpoint[] getExistingBreakpoints() {
        if (fExisting == null) {
            List list = new ArrayList();
            IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(JDIDebugModel.getPluginIdentifier());
            for (int i = 0; i < breakpoints.length; i++) {
                IBreakpoint breakpoint = breakpoints[i];
                if (breakpoint instanceof IJavaExceptionBreakpoint) {
                    list.add(breakpoint);
                }
            }
            fExisting = (IJavaExceptionBreakpoint[]) list.toArray(new IJavaExceptionBreakpoint[list.size()]);
        }
        return fExisting;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#handleDefaultSelected()
     */
    protected void handleDefaultSelected(TypeInfo[] selection) {
        if (getButton(IDialogConstants.OK_ID).isEnabled()) {
            super.handleDefaultSelected(selection);
        }
    }
    
    protected void handleWidgetSelected(TypeInfo[] selection) {
    	updateStatus(new Status(IStatus.OK, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.OK, "", null)); //$NON-NLS-1$
    	super.handleWidgetSelected(selection);
    }
}
