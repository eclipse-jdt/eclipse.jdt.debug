/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.ui.presentation;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 *
 */
public class TestIJavaObjectValue extends TestIJavaValue implements IJavaObject {

	/**
	 * Constructor
	 * @param type
	 * @param sig
	 * @param gsig
	 * @param rtname
	 * @param vstring
	 */
	public TestIJavaObjectValue(IJavaType type, String sig, String gsig, String rtname, String vstring) {
		super(type, sig, gsig, rtname, vstring);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String, java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[], org.eclipse.jdt.debug.core.IJavaThread, boolean)
	 */
	@Override
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend)
			throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String, java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[], org.eclipse.jdt.debug.core.IJavaThread, java.lang.String)
	 */
	@Override
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, String typeSignature) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String, boolean)
	 */
	@Override
	public IJavaFieldVariable getField(String name, boolean superField)	throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String, java.lang.String)
	 */
	@Override
	public IJavaFieldVariable getField(String name, String typeSignature) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getWaitingThreads()
	 */
	@Override
	public IJavaThread[] getWaitingThreads() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getOwningThread()
	 */
	@Override
	public IJavaThread getOwningThread() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getReferringObjects(long)
	 */
	@Override
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#enableCollection()
	 */
	@Override
	public void enableCollection() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#disableCollection()
	 */
	@Override
	public void disableCollection() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getUniqueId()
	 */
	@Override
	public long getUniqueId() throws DebugException {
		return 9999;
	}
}
