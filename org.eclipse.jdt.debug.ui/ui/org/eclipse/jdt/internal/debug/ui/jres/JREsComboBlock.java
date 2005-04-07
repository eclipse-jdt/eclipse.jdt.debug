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
package org.eclipse.jdt.internal.debug.ui.jres;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.internal.debug.ui.launcher.DefineSystemLibraryQuickFix;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A composite that displays installed JRE's in a combo box, with a 'manage...'
 * button to modify installed JREs.
 * <p>
 * This block implements ISelectionProvider - it sends selection change events
 * when the checked JRE in the table changes, or when the "use default" button
 * check state changes.
 * </p>
 */
public class JREsComboBlock implements ISelectionProvider {
	
	/**
	 * This block's control
	 */
	private Composite fControl;
	
	/**
	 * VMs being displayed
	 */
	private List fVMs = new ArrayList(); 
	
	/**
	 * The main control
	 */ 
	private Combo fCombo;
	
	// Action buttons
	private Button fManageButton;
		
	/**
	 * Selection listeners (checked JRE changes)
	 */
	private ListenerList fSelectionListeners = new ListenerList();
	
	/**
	 * Previous selection
	 */
	private ISelection fPrevSelection = new StructuredSelection();
	
	/**
	 * Default JRE descriptor or <code>null</code> if none.
	 */
	private JREDescriptor fDefaultDescriptor = null;
	
	/**
	 * Specific JRE descriptor or <code>null</code> if none.
	 */
	private JREDescriptor fSpecificDescriptor = null;

	/**
	 * Default JRE radio button or <code>null</code> if none
	 */
	private Button fDefaultButton = null;
	
	/**
	 * Selected JRE radio button
	 */
	private Button fSpecificButton = null;
	
	/**
	 * The title used for the JRE block
	 */
	private String fTitle = null;
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		IVMInstall vm = getJRE();
		if (vm == null) {
			return new StructuredSelection();
		}
		return new StructuredSelection(vm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(fPrevSelection)) {
				fPrevSelection = selection;
				if (selection.isEmpty()) {
					fCombo.setText(""); //$NON-NLS-1$
					fCombo.select(-1);
					// need to do this to clear the old text
					fCombo.setItems(new String[]{});
					fillWithWorkspaceJREs();
				} else {
					Object jre = ((IStructuredSelection)selection).getFirstElement();
					int index = fVMs.indexOf(jre);
					if (index >= 0) {
						fCombo.select(index);		
					}
				}
				fireSelectionChanged();
			}
		}
	}

	/**
	 * Creates this block's control in the given control.
	 * 
	 * @param anscestor containing control
	 */
	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite comp = new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fControl = comp;
		comp.setFont(font);
		
		Group group= new Group(comp, SWT.NULL);
		layout= new GridLayout();
		layout.numColumns= 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setFont(font);	
		
		GridData data;
		
		if (fTitle == null) {
			fTitle = JREMessages.JREsComboBlock_3; //$NON-NLS-1$
		}
		group.setText(fTitle);
		
		// display a 'use default JRE' check box
		if (fDefaultDescriptor != null) {
			fDefaultButton = new Button(group, SWT.RADIO);
			fDefaultButton.setText(fDefaultDescriptor.getDescription());
			fDefaultButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (fDefaultButton.getSelection()) {
						setUseDefaultJRE();
					}
				}
			});
			data = new GridData();
			data.horizontalSpan = 3;
			fDefaultButton.setLayoutData(data);
			fDefaultButton.setFont(font);
		}
		
		fSpecificButton = new Button(group, SWT.RADIO);
		if (fSpecificDescriptor != null) {
			fSpecificButton.setText(fSpecificDescriptor.getDescription());
		} else {
			fSpecificButton.setText(JREMessages.JREsComboBlock_1); //$NON-NLS-1$
		}
		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (fSpecificButton.getSelection()) {
					fCombo.setEnabled(true);
					fManageButton.setEnabled(true);
					if (fCombo.getText().length() == 0 && !fVMs.isEmpty()) {
						fCombo.select(0);
					}
					fireSelectionChanged();
				}
			}
		});
		fSpecificButton.setFont(font);
		data = new GridData(GridData.BEGINNING);
		fSpecificButton.setLayoutData(data);
		
		fCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		fCombo.setFont(font);
		data= new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		fCombo.setLayoutData(data);
		ControlAccessibleListener.addListener(fCombo, fSpecificButton.getText());
		
		fCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setJRE(getJRE());
			}
		});
				
		fManageButton = createPushButton(group, JREMessages.JREsComboBlock_2); //$NON-NLS-1$
		fManageButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IVMInstall oldSelection = getJRE();
				int oldIndex = -1;
				if (oldSelection != null) {
					oldIndex = fVMs.indexOf(oldSelection);
				}
				DefineSystemLibraryQuickFix fix = new DefineSystemLibraryQuickFix();
				fix.run(null);
				fillWithWorkspaceJREs();
				int newIndex = -1;
				if (oldSelection != null) {
					newIndex = fVMs.indexOf(oldSelection);
				}
				if (newIndex != oldIndex) {
					// clear the old selection so that a selection changed is fired
					fPrevSelection = null;
				}
				// update text
				setDefaultJREDescriptor(fDefaultDescriptor);
				if (isDefaultJRE()) {
					// reset in case default has changed
					setUseDefaultJRE();
				} else {
					// restore selection
					if (newIndex >= 0) {
						fCombo.select(newIndex);
					} else {
						// select the first JRE
						fCombo.select(0);
					}
					setJRE(getJRE());
				}
			}
		});
		
		fillWithWorkspaceJREs();
	}
	
	/**
	 * Fire current selection
	 */
	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Object[] listeners = fSelectionListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
			listener.selectionChanged(event);
		}	
	}	
	
	protected Button createPushButton(Composite parent, String label) {
		return SWTUtil.createPushButton(parent, label, null);
	}
	
	/**
	 * Returns this block's control
	 * 
	 * @return control
	 */
	public Control getControl() {
		return fControl;
	}
	
	/**
	 * Sets the JREs to be displayed in this block
	 * 
	 * @param vms JREs to be displayed
	 */
	protected void setJREs(List jres) {
		fVMs.clear();
		fVMs.addAll(jres);
		// sort by name
		Collections.sort(fVMs, new Comparator() {
			public int compare(Object o1, Object o2) {
				IVMInstall left = (IVMInstall)o1;
				IVMInstall right = (IVMInstall)o2;
				return left.getName().compareToIgnoreCase(right.getName());
			}

			public boolean equals(Object obj) {
				return obj == this;
			}
		});
		// now make an array of names
		String[] names = new String[fVMs.size()];
		Iterator iter = fVMs.iterator();
		int i = 0;
		while (iter.hasNext()) {
			IVMInstall vm = (IVMInstall)iter.next();
			names[i] = vm.getName();
			i++;
		}
		fCombo.setItems(names);
	}
	
	/**
	 * Returns the JREs currently being displayed in this block
	 * 
	 * @return JREs currently being displayed in this block
	 */
	public IVMInstall[] getJREs() {
		return (IVMInstall[])fVMs.toArray(new IVMInstall[fVMs.size()]);
	}
	
	protected Shell getShell() {
		return getControl().getShell();
	}

	/**
	 * Sets the selected JRE, or <code>null</code>
	 * 
	 * @param vm JRE or <code>null</code>
	 */
	public void setJRE(IVMInstall vm) {
		fSpecificButton.setSelection(true);
		fDefaultButton.setSelection(false);
		fCombo.setEnabled(true);
		fManageButton.setEnabled(true);
		if (vm == null) {
			setSelection(new StructuredSelection());	
		} else {
			setSelection(new StructuredSelection(vm));
		}
	}
	
	/**
	 * Returns the selected JRE or <code>null</code> if none.
	 * 
	 * @return the selected JRE or <code>null</code> if none
	 */
	public IVMInstall getJRE() {
		int index = fCombo.getSelectionIndex();
		if (index >= 0) {
			return (IVMInstall)fVMs.get(index);
		}
		return null;
	}
	
	/**
	 * Populates the JRE table with existing JREs defined in the workspace.
	 */
	protected void fillWithWorkspaceJREs() {
		// fill with JREs
		List standins = new ArrayList();
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				standins.add(new VMStandin(install));
			}
		}
		setJREs(standins);	
	}
	
	/**
	 * Sets the Default JRE Descriptor for this block.
	 * 
	 * @param descriptor default JRE descriptor
	 */
	public void setDefaultJREDescriptor(JREDescriptor descriptor) {
		fDefaultDescriptor = descriptor;
		setButtonTextFromDescriptor(fDefaultButton, descriptor);
	}
	
	private void setButtonTextFromDescriptor(Button button, JREDescriptor descriptor) {
		if (button != null) {
			//update the description & JRE in case it has changed
			String currentText = button.getText();
			String newText = descriptor.getDescription();
			if (!newText.equals(currentText)) {
				button.setText(newText);
				fControl.layout();
			}
		}
	}

	/**
	 * Sets the specific JRE Descriptor for this block.
	 * 
	 * @param descriptor specific JRE descriptor
	 */
	public void setSpecificJREDescriptor(JREDescriptor descriptor) {
		fSpecificDescriptor = descriptor;
		setButtonTextFromDescriptor(fSpecificButton, descriptor);
	}
	
	/**
	 * Returns whether the 'use default JRE' button is checked.
	 * 
	 * @return whether the 'use default JRE' button is checked
	 */
	public boolean isDefaultJRE() {
		if (fDefaultButton != null) {
			return fDefaultButton.getSelection();
		}
		return false;
	}
	
	/**
	 * Sets this control to use the 'default' JRE.
	 */
	public void setUseDefaultJRE() {
		if (fDefaultDescriptor != null) {
			fDefaultButton.setSelection(true);
			fSpecificButton.setSelection(false);
			fCombo.setEnabled(false);
			fManageButton.setEnabled(false);
			fPrevSelection = null;
			fireSelectionChanged();
		}
	}
	
	/**
	 * Sets the title used for this JRE block
	 * 
	 * @param title title for this JRE block 
	 */
	public void setTitle(String title) {
		fTitle = title;
	}

	/**
	 * Refresh the default JRE description.
	 */
	public void refresh() {
		setDefaultJREDescriptor(fDefaultDescriptor);
	}
	
}
