/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.propertypages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class InstanceFilterEditor {
	private IJavaBreakpoint fBreakpoint;
	private CheckboxTableViewer fInstanceViewer;
	private Composite fParent;
	private InstanceFilterContentProvider fContentProvider;
	private CheckHandler fCheckHandler;
	
	public InstanceFilterEditor(Composite parent, IJavaBreakpoint breakpoint) {
		fBreakpoint= breakpoint;
		fContentProvider= new InstanceFilterContentProvider();
		fCheckHandler= new CheckHandler();
		Label label= new Label(parent, SWT.NONE);
		label.setFont(parent.getFont());
		label.setText(PropertyPageMessages.InstanceFilterEditor_0); 
		
		fParent= parent;
		//fOuter= new Composite(parent, SWT.NONE);
		//fOuter.setFont(parent.getFont());

		//GridLayout layout = new GridLayout();
		//layout.marginWidth = 0;
		//layout.marginHeight = 0;
		//layout.numColumns = 2;
		//fOuter.setLayout(layout);
		
		//GridData data= new GridData(GridData.FILL_BOTH);
		//fOuter.setLayoutData(data);
		
		createViewer();
	}
	
	/**
	 * Create and initialize the thread filter tree viewer.
	 */
	protected void createViewer() {
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= 100;

		fInstanceViewer= CheckboxTableViewer.newCheckList(fParent, SWT.BORDER);
		fInstanceViewer.addCheckStateListener(fCheckHandler);
		fInstanceViewer.getTable().setLayoutData(data);
		fInstanceViewer.setContentProvider(fContentProvider);
		IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation();
		pres.setAttribute(JDIModelPresentation.DISPLAY_QUALIFIED_NAMES, Boolean.TRUE);
		fInstanceViewer.setLabelProvider(pres);
		fInstanceViewer.setInput(fBreakpoint);
		setInitialCheckedState();
	}

	/**
	 * Sets the initial checked state of the tree viewer.
	 * The initial state should reflect the current state
	 * of the breakpoint. If the breakpoint has a thread
	 * filter in a given thread, that thread should be
	 * checked.
	 */
	protected void setInitialCheckedState() {
		try {
			IJavaObject[] objects = fBreakpoint.getInstanceFilters();
			for (int i= 0; i < objects.length; i++) {
				fCheckHandler.checkObject(objects[i], true);
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
	
	protected void doStore() {
		try {
			IJavaObject[] objects = fBreakpoint.getInstanceFilters();
			for (int i= 0; i < objects.length; i++) {
				if (!fInstanceViewer.getChecked(objects[i])) {
					fBreakpoint.removeInstanceFilter(objects[i]);
				}
			}
		}  catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}						
	}
	
	class CheckHandler implements ICheckStateListener {	
		
		public void checkStateChanged(CheckStateChangedEvent event) {
			fInstanceViewer.setChecked(event.getElement(), event.getChecked());
		}
		
		public void checkObject(IJavaObject object, boolean checked) {
			fInstanceViewer.setChecked(object, checked);
		}
		
	}
	
	class InstanceFilterContentProvider implements ITreeContentProvider {
		
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof IJavaBreakpoint) {
				try {
					return ((IJavaBreakpoint)parent).getInstanceFilters();
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
			return new Object[0];
		}

		/**
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IJavaObject) {
				return fBreakpoint;
			}
			return null;
		}

		/**
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof IJavaBreakpoint) {
				return getChildren(element).length > 0;
			} 
			return false;
		}

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
}
