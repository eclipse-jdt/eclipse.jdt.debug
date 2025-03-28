/*******************************************************************************
 *  Copyright (c) 2006, 2024 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.monitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame.Category;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.GroupedStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.StackFramePresentationProvider;

/**
 * Java thread presentation adapter.
 *
 * @since 3.3
 */
public class JavaThreadContentProvider extends JavaElementContentProvider {

	private StackFramePresentationProvider stackFrameProvider;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	@Override
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (element instanceof GroupedStackFrame groupedFrame) {
			return groupedFrame.getFrameCount();
		}
		IJavaThread thread = (IJavaThread)element;
		if (!thread.isSuspended()) {
			return 0;
		}
		int childCount = getFrameCount(thread);
		if (isDisplayMonitors()) {
			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				childCount+= thread.getOwnedMonitors().length;
				if (thread.getContendedMonitor() != null) {
					childCount++;
				}
			} else {
				// unavailable notice
				childCount++;
			}
		}
		return childCount;
	}

	/**
	 * return the number of child objects for the given {@link IJavaThread}, either the number of frames, or if frame grouping is enabled, the
	 * {@link GroupedStackFrame}s are properly accounted.
	 */
	private int getFrameCount(IJavaThread thread) throws DebugException {
		if (getStackFrameProvider().isCollapseStackFrames()) {
			return getStackFrames(thread).size();
		}
		return thread.getFrameCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (parent instanceof GroupedStackFrame groupedFrame) {
			return groupedFrame.getFramesAsArray(index, length);
		}
		IJavaThread thread = (IJavaThread)parent;
		if (!thread.isSuspended()) {
			return EMPTY;
		}
		return getElements(getChildren(thread), index, length);
	}

	protected Object[] getChildren(IJavaThread thread) {
		try {
			if (thread instanceof JDIThread) {
				JDIThread jThread = (JDIThread) thread;
				if (!jThread.getDebugTarget().isSuspended() ) {
					if (jThread.isSuspendVoteInProgress()) {
						return EMPTY;
					}
				}
			}
			List<IDebugElement> frames = getStackFrames(thread);
			if (!isDisplayMonitors()) {
				return frames.toArray();
			}

			if (((IJavaDebugTarget) thread.getDebugTarget()).supportsMonitorInformation()) {
				IDebugElement[] ownedMonitors = JavaDebugUtils.getOwnedMonitors(thread);
				IDebugElement contendedMonitor = JavaDebugUtils.getContendedMonitor(thread);
				if (contendedMonitor != null) {
					// Insert the contended monitor after the owned monitors
					frames.add(0, contendedMonitor);
				}
				if (ownedMonitors.length > 0) {
					frames.addAll(0, Arrays.asList(ownedMonitors));
				}
			} else {
				frames.add(0, new NoMonitorInformationElement(thread.getDebugTarget()));
			}
			return frames.toArray();
		} catch (DebugException e) {
			return EMPTY;
		}
	}

	/**
	 * Return the stack frames for the given {@link IJavaThread}. If stack frames grouping is not switched on, it just returns all the stack frames
	 * reported by the JVM. When stack frame grouping enabled, all frames that are not considered as {@link Category#PRODUCTION},
	 * {@link Category#TEST} and {@link Category#CUSTOM_FILTERED} grouped into {@link GroupedStackFrame}s, those are folded by default, but can be
	 * expanded on demand by the user.
	 */
	private List<IDebugElement> getStackFrames(IJavaThread thread) throws DebugException {
		IStackFrame[] frames = thread.getStackFrames();
		var stackFrameProvider = getStackFrameProvider();
		var result = new ArrayList<IDebugElement>(frames.length);
		if (!stackFrameProvider.isCollapseStackFrames()) {
			result.addAll(Arrays.asList(frames));
			return result;
		}
		GroupedStackFrame lastGroupping = null;
		boolean first = true;
		for (var frame : frames) {
			if (first) {
				result.add(frame);
				first = false;
			} else {
				if (frame instanceof JDIStackFrame javaFrame) {
					var category = javaFrame.getCategory();
					if (category == null || !category.hideWhenCollapse()) {
						if (lastGroupping != null) {
							if (lastGroupping.getFrameCount() > 1) {
								result.add(lastGroupping);
							} else {
								result.add(lastGroupping.getTopMostFrame());
							}
						}
						result.add(javaFrame);
						lastGroupping = null;
					} else {
						if (lastGroupping == null) {
							lastGroupping = new GroupedStackFrame(javaFrame.getJavaDebugTarget());
						}
						lastGroupping.add(javaFrame);
					}
				} else {
					result.add(frame);
				}
			}
		}
		if (lastGroupping != null) {
			result.add(lastGroupping);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (element instanceof JDIThread) {
			JDIThread jThread = (JDIThread) element;
			if (!jThread.getDebugTarget().isSuspended()) {
				if (jThread.isSuspendVoteInProgress()) {
					return false;
				}
			}
		}
		if (element instanceof GroupedStackFrame groupedFrame) {
			return groupedFrame.getFrameCount() > 0;
		}
		return ((IJavaThread)element).hasStackFrames() ||
			(isDisplayMonitors() && ((IJavaThread)element).hasOwnedMonitors());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getRule(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate[])
	 */
	@Override
	protected ISchedulingRule getRule(IChildrenCountUpdate[] updates) {
		return getThreadRule(updates);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getRule(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate[])
	 */
	@Override
	protected ISchedulingRule getRule(IChildrenUpdate[] updates) {
		return getThreadRule(updates);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getRule(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate[])
	 */
	@Override
	protected ISchedulingRule getRule(IHasChildrenUpdate[] updates) {
		return getThreadRule(updates);
	}

	/**
	 * Returns a scheduling rule to ensure we aren't trying to get thread content
	 * while executing an implicit evaluation (like toString() for the details
	 * pane).
	 *
	 * @param updates viewer updates
	 * @return scheduling rule or <code>null</code>
	 */
	private ISchedulingRule getThreadRule(IViewerUpdate[] updates) {
		if (updates.length > 0) {
			Object element = updates[0].getElement();
			if (element instanceof JDIThread) {
				return ((JDIThread)element).getThreadRule();
			}
		}
		return null;
	}

	/**
	 * @return a {@link StackFramePresentationProvider} to provide classification of the stack frames.
	 */
	private synchronized StackFramePresentationProvider getStackFrameProvider() {
		if (stackFrameProvider == null) {
			stackFrameProvider = new StackFramePresentationProvider();
		}
		return stackFrameProvider;
	}

}
