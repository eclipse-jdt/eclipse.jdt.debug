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
package org.eclipse.jdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookLauncher;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * Manages the current evaluation context (stack frame) for evaluation actions.
 * In each page, the selection is tracked in each debug view (if any). When a stack
 * frame selection exists, the "debuggerActive" System property is set to true. When
 * a scrapbook becomes active, the "scrapbookActive" System property is set to true. 
 */
public class EvaluationContextManager implements IWindowListener, IPageListener, ISelectionListener, IPartListener2 {

	private static EvaluationContextManager fgManager;
	
	/**
	 * System property indicating a stack frame is selected in the debug view with an
	 * <code>IJavaStackFrame</code> adapter.
	 */
	private static final String DEBUGGER_ACTIVE = JDIDebugUIPlugin.getUniqueIdentifier() + ".debuggerActive"; //$NON-NLS-1$
	/**
	 * System property indicating an element is selected in the debug view that is
	 * an instanceof <code>IJavaStackFrame</code> or <code>IJavaThread</code>.
	 */
	private static final String INSTANCE_OF_IJAVA_STACK_FRAME = JDIDebugUIPlugin.getUniqueIdentifier() + ".instanceof.IJavaStackFrame"; //$NON-NLS-1$
	
	private Map fContextsByPage = null;
	
	private IWorkbenchWindow fActiveWindow;
	
	private EvaluationContextManager() {
	}
	
	public static void startup() {
		Runnable r = new Runnable() {
			public void run() {
				if (fgManager == null) {
					fgManager = new EvaluationContextManager();
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						fgManager.windowOpened(windows[i]);	
					}
					workbench.addWindowListener(fgManager);
					fgManager.fActiveWindow = workbench.getActiveWorkbenchWindow();
				}				
			}
		};
		JDIDebugUIPlugin.getStandardDisplay().asyncExec(r);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		fActiveWindow = window;
		windowOpened(window);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		for (int i = 0; i < pages.length; i++) {
			window.addPageListener(this);
			pageOpened(pages[i]);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		pageOpened(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		page.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		page.removePartListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
		page.addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		page.addPartListener(this);
		IWorkbenchPartReference ref = page.getActivePartReference();
		if (ref != null) {
			partActivated(ref);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IWorkbenchPage page = part.getSite().getPage();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				Object element = ss.getFirstElement();
				if (element instanceof IAdaptable) {
					IJavaStackFrame frame = (IJavaStackFrame)((IAdaptable)element).getAdapter(IJavaStackFrame.class);
					boolean instOf = element instanceof IJavaStackFrame || element instanceof IJavaThread;
					if (frame != null) {
						// do not consider scrapbook frames
						if (frame.getLaunch().getAttribute(ScrapbookLauncher.SCRAPBOOK_LAUNCH) == null) {
							setContext(page, frame, instOf);
							return;
						}
					}
				}
			}
		}
		// no context in the given view
		removeContext(page);
	}

	/**
	 * Sets the evaluation context for the given page, and notes that
	 * a valid execution context exists.
	 * 
	 * @param page
	 * @param frame
	 */
	private void setContext(IWorkbenchPage page, IJavaStackFrame frame, boolean instOf) {
		if (fContextsByPage == null) {
			fContextsByPage = new HashMap();
		}
		fContextsByPage.put(page, frame);
		System.setProperty(DEBUGGER_ACTIVE, "true"); //$NON-NLS-1$
		if (instOf) {
			System.setProperty(INSTANCE_OF_IJAVA_STACK_FRAME, "true"); //$NON-NLS-1$
		} else {
			System.setProperty(INSTANCE_OF_IJAVA_STACK_FRAME, "false"); //$NON-NLS-1$
		}
	}

	/**
	 * Removes an evaluation context for the given page, and determines if
	 * any valid execution context remain.
	 * 
	 * @param page
	 */
	private void removeContext(IWorkbenchPage page) {
		if (fContextsByPage != null) {
			fContextsByPage.remove(page);
			if (fContextsByPage.isEmpty()) {
				System.setProperty(DEBUGGER_ACTIVE, "false"); //$NON-NLS-1$
				System.setProperty(INSTANCE_OF_IJAVA_STACK_FRAME, "false"); //$NON-NLS-1$
			}
		}
	}
	
	private static IJavaStackFrame getContext(IWorkbenchPage page) {
		if (fgManager != null) {
			if (fgManager.fContextsByPage != null) {
				return (IJavaStackFrame)fgManager.fContextsByPage.get(page);
			}
		}
		return null;
	}
	
	/**
	 * Returns the evaluation context for the given part, or <code>null</code> if none.
	 * The evaluation context corresponds to the selected stack frame in the following
	 * priority order:<ol>
	 * <li>stack frame in the same page</li>
	 * <li>stack frame in the same window</li>
	 * <li>stack frame in active page of other window</li>
	 * <li>stack frame in page of other windows</li>
	 * </ol>
	 * 
	 * @param part the part that the evaluation action was invoked from
	 * @return the stack frame that supplies an evaluation context, or <code>null</code>
	 *   if none
	 */
	public static IJavaStackFrame getEvaluationContext(IWorkbenchPart part) {
		IWorkbenchPage page = part.getSite().getPage();
		IJavaStackFrame frame = getContext(page);
		if (frame == null) {
			return getEvaluationContext(page.getWorkbenchWindow());
		}
		return frame;
	}

	/**
	 * Returns the evaluation context for the given window, or <code>null</code> if none.
	 * The evaluation context corresponds to the selected stack frame in the following
	 * priority order:<ol>
	 * <li>stack frame in active page of the window</li>
	 * <li>stack frame in another page of the window</li>
	 * <li>stack frame in active page of another window</li>
	 * <li>stack frame in a page of another window</li>
	 * </ol>
	 * 
	 * @param window the window that the evaluation action was invoked from, or
	 *  <code>null</code> if the current window should be consulted
	 * @return the stack frame that supplies an evaluation context, or <code>null</code>
	 *   if none
	 * @return IJavaStackFrame
	 */
	public static IJavaStackFrame getEvaluationContext(IWorkbenchWindow window) {
		List alreadyVisited= new ArrayList();
		if (window == null) {
			window = fgManager.fActiveWindow;
		}
		return getEvaluationContext(window, alreadyVisited);
	}
	
	private static IJavaStackFrame getEvaluationContext(IWorkbenchWindow window, List alreadyVisited) {
		IWorkbenchPage activePage = window.getActivePage();
		IJavaStackFrame frame = null;
		if (activePage != null) {
			frame = getContext(activePage);
		}
		if (frame == null) {
			IWorkbenchPage[] pages = window.getPages();
			for (int i = 0; i < pages.length; i++) {
				if (activePage != pages[i]) {
					frame = getContext(pages[i]);
					if (frame != null) {
						return frame;
					}
				}
			}
			
			alreadyVisited.add(window);
			
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				if (!alreadyVisited.contains(windows[i])) {
					frame = getEvaluationContext(windows[i], alreadyVisited);
					if (frame != null) {
						return frame;
					}
				}
			}
			return null;
		}
		return frame;
	}
	
	/**
	 * @see IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference ref) {
		if ("org.eclipse.jdt.debug.ui.SnippetEditor".equals(ref.getId())) { //$NON-NLS-1$
			System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @see IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference ref) {
	}

	/**
	 * @see IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference ref) {
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(ref.getId())) {
			removeContext(ref.getPage());
		}
	}

	/**
	 * @see IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference ref) {
	}

	/**
	 * @see IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */ 
	public void partOpened(IWorkbenchPartReference ref) {
	}

	/**
	 * @see IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */	
	public void partHidden(IWorkbenchPartReference ref) {
	}

	/**
	 * @see IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference ref) {
	}

	/**
	 * @see IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference ref) {
	}
	
}
