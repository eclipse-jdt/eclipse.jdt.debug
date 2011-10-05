/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, boolean superSend)
			throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#sendMessage(java.lang.String, java.lang.String, org.eclipse.jdt.debug.core.IJavaValue[], org.eclipse.jdt.debug.core.IJavaThread, java.lang.String)
	 */
	public IJavaValue sendMessage(String selector, String signature, IJavaValue[] args, IJavaThread thread, String typeSignature) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String, boolean)
	 */
	public IJavaFieldVariable getField(String name, boolean superField)	throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getField(java.lang.String, java.lang.String)
	 */
	public IJavaFieldVariable getField(String name, String typeSignature) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getWaitingThreads()
	 */
	public IJavaThread[] getWaitingThreads() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getOwningThread()
	 */
	public IJavaThread getOwningThread() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getReferringObjects(long)
	 */
	public IJavaObject[] getReferringObjects(long max) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#enableCollection()
	 */
	public void enableCollection() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#disableCollection()
	 */
	public void disableCollection() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.core.IJavaObject#getUniqueId()
	 */
	public long getUniqueId() throws DebugException {
		return 9999;
	}
}
