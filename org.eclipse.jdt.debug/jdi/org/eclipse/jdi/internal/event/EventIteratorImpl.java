package org.eclipse.jdi.internal.event;/*
 * JDI class Implementation
 *
 * (BB)
 * (C) Copyright IBM Corp. 2000
 */



import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import java.util.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class EventIteratorImpl implements EventIterator, Iterator {
	/** List iterator implementation of iterator. */
	private ListIterator fIterator;
	
	/**
	 * Creates new EventIteratorImpl.
	 */
	public EventIteratorImpl(ListIterator iter) {
		fIterator = iter;
	}

	/**
	 * @return Returns next Event from EventSet.
	 */	
	public Event nextEvent() {
		return (Event)fIterator.next();
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
	public Object next() {
		return fIterator.next();
	}
	
	/**
	 * @see java.util.Iterator#remove()
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public void remove() {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}
}
