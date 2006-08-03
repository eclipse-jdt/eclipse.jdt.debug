/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.ui.heapwalking;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.VariablesViewModelPresentation;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A popup that can be used to browse references to an object.
 * 
 * @since 3.3
 */
public class ReferencesPopupDialog extends DebugPopup {
    private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 90, 10 };

    private static final int MIN_WIDTH = 250;

    private static final int MIN_HEIGHT = 200;

    private ReferencesViewer fViewer;

    private StyledText fValueDisplay;

    private SashForm fSashForm;

    private IDebugModelPresentation fModelPresentation;
    
    private Tree fTree;

    private IJavaObject fRoot;
    
    private IDebugView fView;

    /**
     * Creates a new inspect popup.
     * 
     * @param shell The parent shell
     * @param view view to anchor the popup on
     * @param commandId The command id to be used for persistence of 
     * the dialog (possibly <code>null</code>)
     * @param root object to browse references to
     */
    public ReferencesPopupDialog(Shell shell, IDebugView view, String commandId, IJavaObject root) {
        super(shell, getAnchor(view), commandId);
        fRoot = root;
        fView = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, parent.getStyle());
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        fSashForm = new SashForm(composite, parent.getStyle());
        fSashForm.setOrientation(SWT.VERTICAL);
        fSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        fViewer = new ReferencesViewer(fSashForm, SWT.NO_TRIM | SWT.VIRTUAL);
        
        fValueDisplay = new StyledText(fSashForm, SWT.NO_TRIM | SWT.WRAP | SWT.V_SCROLL);
        fValueDisplay.setEditable(false);

        fTree = fViewer.getTree();
        fModelPresentation = new VariablesViewModelPresentation();
        fTree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selections = fTree.getSelection();
                if (selections.length > 0) {
                    Object data = selections[selections.length - 1].getData();

                    IValue val = null;
                    if (data instanceof IndexedVariablePartition) {
                        // no details for partitions
                        return;
                    }
                    if (data instanceof IValue) {
                        val = (IValue) data;
                    }
                    if (val == null) {
                        return;
                    }

                    updateValueDisplay(val);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        Map map = ((AbstractDebugView)fView).getPresentationAttributes(fRoot.getModelIdentifier());
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            fModelPresentation.setAttribute(key, map.get(key));
        }

        // sashForm.setWeights(getInitialSashWeights());
        fSashForm.setWeights(DEFAULT_SASH_WEIGHTS);

        fViewer.setInput(fRoot);
        return fTree;
    }
    
    private void updateValueDisplay(IValue val) {
        IValueDetailListener valueDetailListener = new IValueDetailListener() {
            public void detailComputed(IValue value, final String result) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (!fValueDisplay.isDisposed()) {
                            String text = result;
                            int max = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
                            if (max > 0 && result.length() > max) {
                                text = result.substring(0, max) + "..."; //$NON-NLS-1$
                            }
                            fValueDisplay.setText(text);
                        }
                    }
                });
            }
        };
        fModelPresentation.computeDetail(val, valueDetailListener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#close()
     */
    public boolean close() {
    	if(fViewer != null) {
    		fViewer.dispose();
    	}
    	if(fModelPresentation != null) {
    		fModelPresentation.dispose();
    	}
		return super.close();
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.DebugPopup#getActionText()
     */
    protected String getActionText() {
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.PopupDialog#getInitialSize()
     */
    protected Point getInitialSize() {
        Point initialSize = super.getInitialSize();
        initialSize.x = Math.max(initialSize.x, MIN_WIDTH);
        initialSize.y = Math.max(initialSize.y, MIN_HEIGHT);
        return initialSize;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.PopupDialog#getBackgroundColorExclusions()
	 */
	protected List getBackgroundColorExclusions() {
		List list = super.getBackgroundColorExclusions();
		list.add(fSashForm);
		return list;
	}
    
    protected static Point getAnchor(IDebugView view) {
		Control control = view.getViewer().getControl();
		if (control instanceof Tree) {
			Tree tree = (Tree) control;
			TreeItem[] selection = tree.getSelection();
			if (selection.length > 0) {
				Rectangle bounds = selection[0].getBounds();
				return tree.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
			}
		}
		return control.toDisplay(0, 0);    	
    }
}
