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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
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
public class ThreadFilterEditor {

	private JavaBreakpointAdvancedPage fPage;
	private CheckboxTreeViewer fThreadViewer;
	private ThreadFilterContentProvider fContentProvider;
	private CheckHandler fCheckHandler;
	private static String MAIN= "main"; //$NON-NLS-1$
	
	public ThreadFilterEditor(Composite parent, JavaBreakpointAdvancedPage page) {
		fPage= page;
		fContentProvider= new ThreadFilterContentProvider();
		fCheckHandler= new CheckHandler();		
		createThreadViewer(parent);
	}
	
	private void createThreadViewer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(PropertyPageMessages.ThreadFilterEditor_1); 
		label.setFont(parent.getFont());
		label.setLayoutData(new GridData());
		
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= 100;
		fThreadViewer= new CheckboxTreeViewer(parent, SWT.BORDER);
		fThreadViewer.addCheckStateListener(fCheckHandler);
		fThreadViewer.getTree().setLayoutData(data);
		fThreadViewer.getTree().setFont(parent.getFont());
		fThreadViewer.setContentProvider(fContentProvider);
		fThreadViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		fThreadViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
		setInitialCheckedState();
	}
	
	protected void doStore() {
		IDebugTarget[] targets= getDebugTargets();
		IJavaDebugTarget target;
		IThread[] threads;
		IJavaThread thread;
		for (int i= 0, numTargets= targets.length; i < numTargets; i++) {
			target = (IJavaDebugTarget)targets[i].getAdapter(IJavaDebugTarget.class);
			if (target != null) {
				try {
					if (fThreadViewer.getChecked(target)) {
						threads= target.getThreads();
						for (int j=0, numThreads= threads.length; j < numThreads; j++) {
							thread= (IJavaThread)threads[j];
							if (fThreadViewer.getChecked(thread)) {
								// thread selected for filtering
								fPage.getBreakpoint().setThreadFilter(thread);
								break; // Can only set one filtered thread.
							}
						}
					} else {
						fPage.getBreakpoint().removeThreadFilter(target);
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
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
			IDebugTarget[] targets= getDebugTargets();
			for (int i= 0, numTargets= targets.length; i < numTargets; i++) {
				IJavaDebugTarget target = (IJavaDebugTarget)targets[i].getAdapter(IJavaDebugTarget.class);
				if (target != null) {
					IJavaThread filteredThread= fPage.getBreakpoint().getThreadFilter(target);
					if (filteredThread != null) {
						fCheckHandler.checkThread(filteredThread, true);
					}
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}
	
	/**
	 * Returns the debug targets that appear in the tree
	 */
	protected IDebugTarget[] getDebugTargets() {
		Object input= fThreadViewer.getInput();
		if (!(input instanceof ILaunchManager)) {
			return new IJavaDebugTarget[0];
		}
		ILaunchManager launchManager= (ILaunchManager)input;
		return launchManager.getDebugTargets();
	}
	
	class CheckHandler implements ICheckStateListener {	
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element= event.getElement();
			if (element instanceof IDebugTarget) {
				checkTarget((IDebugTarget)element, event.getChecked());
			} else if (element instanceof IThread) {
				checkThread((IThread)element, event.getChecked());
			}
			verifyCheckedState();
		}
		
		/**
		 * Check or uncheck a debug target in the tree viewer.
		 * When a debug target is checked, attempt to
		 * check one of the target's threads by default.
		 * When a debug target is unchecked, uncheck all
		 * its threads.
		 */
		protected void checkTarget(IDebugTarget target, boolean checked) {
			fThreadViewer.setChecked(target, checked);
			if (checked) {
				fThreadViewer.expandToLevel(target, AbstractTreeViewer.ALL_LEVELS);
				IThread[] threads;
				try {
					threads= target.getThreads();
				} catch (DebugException exception) {
					JDIDebugUIPlugin.log(exception);
					return;
				}
				IThread thread;
				boolean checkedThread= false;
				// Try to check the "main" thread by default
				for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
					thread= threads[i];
					String name= null;
					try {
						name= thread.getName();
					} catch (DebugException exception) {
						JDIDebugUIPlugin.log(exception);
					}
					if (MAIN.equals(name)) {
						checkedThread= fThreadViewer.setChecked(thread, true);
					}
				}
				// If the main thread couldn't be checked, check the first
				// available thread
				if (!checkedThread) {
					for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
						if (fThreadViewer.setChecked(threads[i], true)) {
							break;
						}
					}
				}
			} else { // Unchecked
				IThread[] threads;
				try {
					threads= target.getThreads();
				} catch (DebugException exception) {
					JDIDebugUIPlugin.log(exception);
					return;
				}
				for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
					fThreadViewer.setChecked(threads[i], false);
				}
			}
		}
	
		/**
		 * Check or uncheck a thread.
		 * When a thread is checked, make sure its debug
		 * target is also checked.
		 * When a thread is unchecked, uncheck its debug
		 * target.
		 */
		protected void checkThread(IThread thread, boolean checked) {	
			fThreadViewer.setChecked(thread, checked);
			IDebugTarget target= (thread).getDebugTarget();
			if (checked) {
				// When a thread is checked, make sure the target
				// is checked and all other threads are
				// unchecked (simulate radio button behavior)
				if (!fThreadViewer.getChecked(target)) {					
					fThreadViewer.setChecked(target, true);
				}
				IThread[] threads;
				try {
					threads= target.getThreads();
				} catch (DebugException exception) {
					JDIDebugUIPlugin.log(exception);
					return;
				}
				for (int i= 0, numThreads= threads.length; i < numThreads; i++) {
					if (threads[i] != thread) {
						// Uncheck all threads other than the selected thread
						fThreadViewer.setChecked(threads[i], false);
					}
				}
			} else {
				// When a thread is unchecked, uncheck the target
				fThreadViewer.setChecked(target, false);
			}
		}
	
		/**
		 * Verify the state of the tree viewer.
		 * If the user selects a debug target, they must select
		 * a thread.
		 */
		protected void verifyCheckedState() {
			IDebugTarget[] targets= getDebugTargets();
			IDebugTarget target;
			IThread[] threads;
			boolean checkedThread;
			for (int i= 0, numTargets= targets.length; i < numTargets; i++) {
				target= targets[i];
				if (!fThreadViewer.getChecked(target)) {
					continue;
				}
				try {
					threads= target.getThreads();
				} catch (DebugException exception) {
					JDIDebugUIPlugin.log(exception);
					continue;
				}
				checkedThread= false;
				for (int j= 0, numThreads= threads.length; j < numThreads; j++) {
					if (fThreadViewer.getChecked(threads[j])) {
						checkedThread= true;
						break;
					}
				}
				if (checkedThread) {
					fPage.setErrorMessage(null);
				} else {
					fPage.setErrorMessage(PropertyPageMessages.ThreadFilterEditor_2); 
				}
			}
		}
		
	}
	
	class ThreadFilterContentProvider implements ITreeContentProvider {
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof IDebugTarget) {
				IJavaDebugTarget target = (IJavaDebugTarget)((IDebugTarget)parent).getAdapter(IJavaDebugTarget.class);
				if (target != null) {
					try {
						return ((IJavaDebugTarget)parent).getThreads();
					} catch (DebugException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
			}		
			if (parent instanceof ILaunchManager) {
				List<IJavaDebugTarget> children= new ArrayList<IJavaDebugTarget>();
				ILaunch[] launches= ((ILaunchManager) parent).getLaunches();
				IDebugTarget[] targets;
				IJavaDebugTarget target;
				for (int i= 0, numLaunches= launches.length; i < numLaunches; i++) {
					targets= launches[i].getDebugTargets();
					for (int j= 0, numTargets= targets.length; j < numTargets; j++) {
						target= (IJavaDebugTarget)targets[j].getAdapter(IJavaDebugTarget.class);
						if (target != null && !target.isDisconnected() && !target.isTerminated()) {
							children.add(target);
						}
					}
				}
				return children.toArray();
			}
			return new Object[0];
		}

		/**
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IThread) {
				return ((IThread)element).getDebugTarget();
			}
			if (element instanceof IDebugTarget) {
				return ((IDebugElement)element).getLaunch();
			}
			if (element instanceof ILaunch) {
				return DebugPlugin.getDefault().getLaunchManager();
			}
			return null;
		}

		/**
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof IStackFrame) {
				return false;
			}
			if (element instanceof IDebugElement) {
				return getChildren(element).length > 0;
			} 
			if (element instanceof ILaunch) {
				return true;
			}
			if (element instanceof ILaunchManager) {
				return ((ILaunchManager) element).getLaunches().length > 0;
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
