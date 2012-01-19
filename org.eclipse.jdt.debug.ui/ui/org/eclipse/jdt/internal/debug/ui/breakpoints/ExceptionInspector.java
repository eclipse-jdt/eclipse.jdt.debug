/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.PopupInspectAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * This class is used to show the inspect pop-up when a thread is suspended due to an exception being thrown
 */
public class ExceptionInspector implements IDebugContextListener, IPreferenceChangeListener {
	
	/**
	 * Constructor
	 */
	public ExceptionInspector() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		if(prefs != null) {
			prefs.addPreferenceChangeListener(this);
		}
		boolean doit = Platform.getPreferencesService().getBoolean(
				JDIDebugPlugin.getUniqueIdentifier(), 
				IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION, 
				false, 
				null);
		if (doit) {
			DebugUITools.getDebugContextManager().addDebugContextListener(this);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			IWorkbenchPart part = event.getDebugContextProvider().getPart();
			if (part != null) {
				IWorkbenchPartSite site = part.getSite();
				if (site != null && IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
					IWorkbenchPage page = site.getWorkbenchWindow().getActivePage(); 
					if (page != null && page.isPartVisible(part)) {
						ISelection selection = event.getContext();
						if (selection instanceof IStructuredSelection) {
							IStructuredSelection ss = (IStructuredSelection) selection;
							if (ss.size() == 1) {
								Object firstElement = ss.getFirstElement();
								if (firstElement instanceof IAdaptable) {
									IJavaStackFrame frame = (IJavaStackFrame) ((IAdaptable)firstElement).getAdapter(IJavaStackFrame.class);
									if (frame != null) {
										IJavaThread thread = (IJavaThread)frame.getThread();
										try {
											if (frame.equals(thread.getTopStackFrame())) {
												IBreakpoint[] breakpoints = thread.getBreakpoints();
												if (breakpoints.length == 1) {
													if (breakpoints[0] instanceof IJavaExceptionBreakpoint) {
														IJavaExceptionBreakpoint exception = (IJavaExceptionBreakpoint) breakpoints[0];
														IJavaObject lastException = ((JavaExceptionBreakpoint)exception).getLastException();
														if (lastException != null) {
															IExpression exp = new JavaInspectExpression(exception.getExceptionTypeName(), lastException);
															Tree tree = (Tree) ((IDebugView)part).getViewer().getControl();
															TreeItem[] selection2 = tree.getSelection();
															Rectangle bounds = selection2[0].getBounds();
															Point point = tree.toDisplay(bounds.x, bounds.y + bounds.height);
															InspectPopupDialog dialog = new InspectPopupDialog(part.getSite().getShell(),
																	point, PopupInspectAction.ACTION_DEFININITION_ID, exp);
															dialog.open();
														}
													}
												}
											} 
										} catch (DebugException e) {}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
		if (IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION.equals(event.getKey())) {
			IDebugContextManager manager = DebugUITools.getDebugContextManager();
			boolean doit = Platform.getPreferencesService().getBoolean(
					JDIDebugPlugin.getUniqueIdentifier(), 
					IJDIPreferencesConstants.PREF_OPEN_INSPECT_POPUP_ON_EXCEPTION, 
					false, 
					null);
			if (doit) {
				manager.addDebugContextListener(this);
 			} else {
 				manager.removeDebugContextListener(this);
 			}
		}
	}
}