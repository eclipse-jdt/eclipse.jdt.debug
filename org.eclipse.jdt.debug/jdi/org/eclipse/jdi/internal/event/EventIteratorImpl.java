/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdi.internal.event;

import java.util.ListIterator;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 *
 */
public class EventIteratorImpl implements EventIterator {
	/** List iterator implementation of iterator. */
	private ListIterator<Event> fIterator;

	/**
	 * Creates new EventIteratorImpl.
	 */
	public EventIteratorImpl(ListIterator<Event> iter) {
		fIterator = iter;
	}

	/**
	 * @return Returns next Event from EventSet.
	 */
	@Override
	public Event nextEvent() {
		return fIterator.next();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return fIterator.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Event next() {
		return fIterator.next();
	}

	/**
	 * @see java.util.Iterator#remove()
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				EventMessages.EventIteratorImpl_EventSets_are_unmodifiable_1);
	}
}
