/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Manages breakpoint listener extensions.
 * 
 * @since 3.5
 */
public class BreakpointListenerManager {

	/**
	 * Map java breakpoint listeners by id
	 */
	private static Map<String, JavaBreakpointListenerProxy> fgJavaBreakpointListenersMap;

	/**
	 * Global listeners
	 */
	private static IJavaBreakpointListener[] fgGlobalListeners;

	private static final String VALUE_GLOBAL = "*"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_FILTER = "filter"; //$NON-NLS-1$

	/**
	 * Proxy to a breakpoint listener
	 */
	private class JavaBreakpointListenerProxy implements
			IJavaBreakpointListener {

		private IConfigurationElement fConfigElement;
		private IJavaBreakpointListener fDelegate;

		public JavaBreakpointListenerProxy(IConfigurationElement element) {
			fConfigElement = element;
		}

		/**
		 * Returns the underlying delegate or <code>null</code> if none/error
		 * 
		 * @return breakpoint listener extension
		 */
		private synchronized IJavaBreakpointListener getDelegate() {
			if (fDelegate == null) {
				try {
					fDelegate = (IJavaBreakpointListener) fConfigElement
							.createExecutableExtension(ATTR_CLASS);
				} catch (CoreException e) {
					JDIDebugPlugin.log(e);
				}
			}

			return fDelegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.core.IJavaBreakpointListener#addingBreakpoint
		 * (org.eclipse.jdt.debug.core.IJavaDebugTarget,
		 * org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void addingBreakpoint(IJavaDebugTarget target,
				IJavaBreakpoint breakpoint) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				delegate.addingBreakpoint(target, breakpoint);
			}
		}

		/**
		 * Whether this listener is for all breakpoints.
		 * 
		 * @return whether for all breakpoints
		 */
		boolean isGlobal() {
			String filter = fConfigElement.getAttribute(ATTR_FILTER);
			if (filter != null && filter.equals(VALUE_GLOBAL)) {
				return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#
		 * breakpointHasCompilationErrors
		 * (org.eclipse.jdt.debug.core.IJavaLineBreakpoint,
		 * org.eclipse.jdt.core.dom.Message[])
		 */
		public void breakpointHasCompilationErrors(
				IJavaLineBreakpoint breakpoint, Message[] errors) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				delegate.breakpointHasCompilationErrors(breakpoint, errors);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.debug.core.IJavaBreakpointListener#
		 * breakpointHasRuntimeException
		 * (org.eclipse.jdt.debug.core.IJavaLineBreakpoint,
		 * org.eclipse.debug.core.DebugException)
		 */
		public void breakpointHasRuntimeException(
				IJavaLineBreakpoint breakpoint, DebugException exception) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				delegate.breakpointHasRuntimeException(breakpoint, exception);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointHit(
		 * org.eclipse.jdt.debug.core.IJavaThread,
		 * org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				return delegate.breakpointHit(thread, breakpoint);
			}
			return IJavaBreakpointListener.DONT_CARE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointInstalled
		 * (org.eclipse.jdt.debug.core.IJavaDebugTarget,
		 * org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void breakpointInstalled(IJavaDebugTarget target,
				IJavaBreakpoint breakpoint) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				delegate.breakpointInstalled(target, breakpoint);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.core.IJavaBreakpointListener#breakpointRemoved
		 * (org.eclipse.jdt.debug.core.IJavaDebugTarget,
		 * org.eclipse.jdt.debug.core.IJavaBreakpoint)
		 */
		public void breakpointRemoved(IJavaDebugTarget target,
				IJavaBreakpoint breakpoint) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				delegate.breakpointRemoved(target, breakpoint);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.debug.core.IJavaBreakpointListener#installingBreakpoint
		 * (org.eclipse.jdt.debug.core.IJavaDebugTarget,
		 * org.eclipse.jdt.debug.core.IJavaBreakpoint,
		 * org.eclipse.jdt.debug.core.IJavaType)
		 */
		public int installingBreakpoint(IJavaDebugTarget target,
				IJavaBreakpoint breakpoint, IJavaType type) {
			IJavaBreakpointListener delegate = getDelegate();
			if (delegate != null) {
				return delegate.installingBreakpoint(target, breakpoint, type);
			}
			return IJavaBreakpointListener.DONT_CARE;
		}

	}

	/**
	 * Load extensions.
	 */
	private synchronized void init() {
		if (fgJavaBreakpointListenersMap == null) {
			fgJavaBreakpointListenersMap = new HashMap<String, JavaBreakpointListenerProxy>();
			List<JavaBreakpointListenerProxy> global = new ArrayList<JavaBreakpointListenerProxy>();
			IExtensionPoint extensionPoint = Platform
					.getExtensionRegistry()
					.getExtensionPoint(
							JDIDebugPlugin.getUniqueIdentifier(),
							JDIDebugPlugin.EXTENSION_POINT_JAVA_BREAKPOINT_LISTENERS);
			IConfigurationElement[] actionDelegateElements = extensionPoint
					.getConfigurationElements();
			for (IConfigurationElement actionDelegateElement : actionDelegateElements) {
				try {
					String id = actionDelegateElement.getAttribute(ATTR_ID);
					if (id == null)
						throw new CoreException(
								new Status(IStatus.ERROR, JDIDebugPlugin
										.getUniqueIdentifier(),
										"Java breakpoint listener requires an  identifier attribute.")); //$NON-NLS-1$
					JavaBreakpointListenerProxy listener = new JavaBreakpointListenerProxy(
							actionDelegateElement);
					fgJavaBreakpointListenersMap.put(id, listener);
					if (listener.isGlobal()) {
						global.add(listener);
					}
				} catch (CoreException e) {
					JDIDebugPlugin.log(e);
				}
			}
			fgGlobalListeners = global
					.toArray(new IJavaBreakpointListener[global.size()]);
		}
	}

	/**
	 * Returns the listener registered with the given identifier or
	 * <code>null</code> if none.
	 * 
	 * @param id
	 *            extension identifier
	 * @return breakpoint listener or <code>null</code>
	 */
	public IJavaBreakpointListener getBreakpointListener(String id) {
		init();
		return fgJavaBreakpointListenersMap.get(id);
	}

	/**
	 * Returns breakpoint listener extensions registered to listen for changes
	 * to all breakpoints.
	 * 
	 * @return global listeners
	 */
	public IJavaBreakpointListener[] getGlobalListeners() {
		init();
		return fgGlobalListeners;
	}
}
