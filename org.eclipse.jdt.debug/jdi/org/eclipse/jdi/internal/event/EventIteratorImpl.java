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
	public Event nextEvent() {
		return fIterator.next();
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return fIterator.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Event next() {
		return fIterator.next();
	}

	/**
	 * @see java.util.Iterator#remove()
	 * @exception UnsupportedOperationException
	 *                always thrown since EventSets are unmodifiable.
	 */
	public void remove() {
		throw new UnsupportedOperationException(
				EventMessages.EventIteratorImpl_EventSets_are_unmodifiable_1);
	}
}
