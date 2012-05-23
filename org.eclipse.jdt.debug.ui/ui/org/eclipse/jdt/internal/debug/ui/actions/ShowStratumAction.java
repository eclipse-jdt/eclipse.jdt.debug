/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Allows the user to choose what source to display from available strata
 */
public class ShowStratumAction implements IObjectActionDelegate, IMenuCreator {
    
    private IStructuredSelection fSelection;
    private IWorkbenchPart fPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    	fPart = targetPart;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) selection;
            action.setMenuCreator(this);
        } else {
            action.setMenuCreator(null);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
		//Create the new menu. The menu will get filled when it is about to be shown. see fillMenu(Menu).
		Menu menu = new Menu(parent);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
		return menu;
    }

    /**
     * Fills the given menu with available stratum.
     * 
     * @param m
     */
    private void fillMenu(Menu m) {
        IStackFrame frame = (IStackFrame) fSelection.getFirstElement();
        final IJavaStackFrame javaStackFrame = (IJavaStackFrame) frame.getAdapter(IJavaStackFrame.class);
        if (javaStackFrame != null) {
	        try {
	            IJavaReferenceType declaringType = javaStackFrame.getReferenceType();
	            final IJavaDebugTarget target = (IJavaDebugTarget) javaStackFrame.getDebugTarget();
	            String currentStratum = target.getDefaultStratum();
	            String[] strata = declaringType.getAvailableStrata();
	            for (int i = 0; i < strata.length; i++) {
	                final String stratum = strata[i];
	                MenuItem item = createMenuItem(m, stratum, javaStackFrame, target);
	                item.setSelection(stratum.equals(currentStratum));
	            }
	            MenuItem item = createMenuItem(m, null, javaStackFrame, target);
	            item.setSelection(currentStratum == null);
	        } catch (DebugException e) {
	        }
        }
    }
    
    private MenuItem createMenuItem(Menu m, final String stratum, final IJavaStackFrame frame, final IJavaDebugTarget target) {
        MenuItem item = new MenuItem(m, SWT.CHECK);
        if (stratum == null) {
            item.setText(ActionMessages.ShowStratumAction_0); 
        } else {
            item.setText(stratum);
        }
        item.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                if (((MenuItem)e.getSource()).getSelection()) {
                    target.setDefaultStratum(stratum);
                } else {
                    target.setDefaultStratum(null);
                }
                DebugEvent event = new DebugEvent(frame, DebugEvent.CHANGE, DebugEvent.CONTENT);
                DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{event});
                ISourceDisplay display = (ISourceDisplay) frame.getAdapter(ISourceDisplay.class);
                if (display != null) {
                	display.displaySource(frame, fPart.getSite().getPage(), true);
                }
            }
        });
        return item;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
    }
    

}
