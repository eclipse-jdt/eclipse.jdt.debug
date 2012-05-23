/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * An interface an object implements on a Java debug target.
 * 
 * @see IJavaValue
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IJavaInterfaceType extends IJavaReferenceType {

	/**
	 * Returns the class objects associated with the implementors of this
	 * interface type. Returns an empty array if there are none.
	 * 
	 * @return the class objects associated with the implementors of this
	 *         interface type
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 * @since 3.0
	 */
	public IJavaClassType[] getImplementors() throws DebugException;

	/**
	 * Returns the interface objects associated with the sub-interfaces of this
	 * interface type. Returns an empty array if there are none. The
	 * sub-interfaces are those interfaces that directly extend this interface,
	 * that is, those interfaces that declared this interface in their
	 * <code>extends</code> clause.
	 * 
	 * @return the interface objects associated with the sub-interfaces of this
	 *         interface type
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 * @since 3.0
	 */
	public IJavaInterfaceType[] getSubInterfaces() throws DebugException;

	/**
	 * Returns the interface objects associated with the super-interfaces of
	 * this interface type. Returns an empty array if there are none. The
	 * super-interfaces are those interfaces that are directly extended by this
	 * interface, that is, those interfaces that this interface declared to be
	 * extended.
	 * 
	 * @return the interface objects associated with the super-interfaces of
	 *         this interface type
	 * @exception DebugException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure communicating with the VM. The
	 *                DebugException's status code contains the underlying
	 *                exception responsible for the failure.</li>
	 *                </ul>
	 * @since 3.0
	 */
	public IJavaInterfaceType[] getSuperInterfaces() throws DebugException;
}
