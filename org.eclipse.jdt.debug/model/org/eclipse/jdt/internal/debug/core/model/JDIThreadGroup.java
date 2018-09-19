/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

/**
 * @since 3.2
 *
 */
public class JDIThreadGroup extends JDIDebugElement implements
		IJavaThreadGroup, ITerminate {

	private ThreadGroupReference fGroup = null;
	private String fName = null;

	/**
	 * Constructs a new thread group in the given target based on the underlying
	 * thread group reference.
	 *
	 * @param target
	 *            debug target
	 * @param group
	 *            thread group reference
	 */
	public JDIThreadGroup(JDIDebugTarget target, ThreadGroupReference group) {
		super(target);
		fGroup = group;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#getThreads()
	 */
	@Override
	public synchronized IJavaThread[] getThreads() throws DebugException {
		try {
			List<ThreadReference> threads = fGroup.threads();
			List<JDIThread> modelThreads = new ArrayList<>(threads.size());
			Iterator<ThreadReference> iterator = threads.iterator();
			while (iterator.hasNext()) {
				ThreadReference ref = iterator.next();
				JDIThread thread = getJavaDebugTarget().findThread(ref);
				if (thread != null) {
					modelThreads.add(thread);
				}
			}
			return modelThreads
					.toArray(new IJavaThread[modelThreads.size()]);
		} catch (VMDisconnectedException e) {
			// terminated/disconnected, return empty collection
			return new IJavaThread[0];
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_0, e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#getThreadGroup()
	 */
	@Override
	public IJavaThreadGroup getThreadGroup() throws DebugException {
		try {
			ThreadGroupReference reference = fGroup.parent();
			if (reference != null) {
				return getJavaDebugTarget().findThreadGroup(reference);
			}
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_1, e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#getThreadGroups()
	 */
	@Override
	public IJavaThreadGroup[] getThreadGroups() throws DebugException {
		try {
			List<ThreadGroupReference> groups = fGroup.threadGroups();
			List<JDIThreadGroup> modelGroups = new ArrayList<>(groups.size());
			Iterator<ThreadGroupReference> iterator = groups.iterator();
			while (iterator.hasNext()) {
				ThreadGroupReference ref = iterator
						.next();
				JDIThreadGroup group = getJavaDebugTarget()
						.findThreadGroup(ref);
				if (group != null) {
					modelGroups.add(group);
				}
			}
			return modelGroups
					.toArray(new IJavaThreadGroup[modelGroups.size()]);
		} catch (VMDisconnectedException e) {
			return new IJavaThreadGroup[0];
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_2, e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#getName()
	 */
	@Override
	public synchronized String getName() throws DebugException {
		if (fName == null) {
			try {
				fName = fGroup.name();
			} catch (RuntimeException e) {
				targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_3, e);
			}
		}
		return fName;
	}

	ThreadGroupReference getUnderlyingThreadGroup() {
		return fGroup;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#hasThreadGroups()
	 */
	@Override
	public boolean hasThreadGroups() throws DebugException {
		try {
			List<ThreadGroupReference> groups = fGroup.threadGroups();
			return groups.size() > 0;
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_4, e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.debug.core.IJavaThreadGroup#hasThreads()
	 */
	@Override
	public boolean hasThreads() throws DebugException {
		try {
			List<ThreadReference> threads = fGroup.threads();
			return threads.size() > 0;
		} catch (RuntimeException e) {
			targetRequestFailed(JDIDebugModelMessages.JDIThreadGroup_5, e);
		}
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		// the group can terminate if the target can terminate
		return getDebugTarget().canTerminate();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	/**
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		getDebugTarget().terminate();
	}

}
