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
import org.eclipse.jdi.internal.*;
import org.eclipse.jdi.internal.request.*;
import java.io.*;
import java.util.*;

/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public class EventSetImpl extends MirrorImpl implements EventSet {
	/** Set that is used to store events. */
	private ArrayList fEvents;
	/** Which threads were suspended by this composite event. */
	private byte fSuspendPolicy;

   	/**
	 * Creates new EventSetImpl.
	 */
	private EventSetImpl(VirtualMachineImpl vmImpl) {
		super("EventSet", vmImpl);
	}

   	/**
	 * Creates new EventSetImpl with events in a given array.
	 */
	public EventSetImpl(VirtualMachineImpl vmImpl, EventImpl[] events) {
		this(vmImpl);
		fEvents = new ArrayList(events.length);
		for (int i = 0; i < events.length; i++)
			fEvents.add(events[i]);
	}

   	/**
	 * Creates new EventSetImpl with given event.
	 */
	public EventSetImpl(VirtualMachineImpl vmImpl, EventImpl event) {
		this(vmImpl);
		fEvents = new ArrayList(1);
		fEvents.add(event);
	}

	/**
	 * @return Returns iterator over events.
	 */
	public EventIterator eventIterator() {
		return new EventIteratorImpl(fEvents.listIterator());
	}

	/**
	 * @return Returns which threads were suspended by this composite event.
	 */
	public int suspendPolicy() {
		switch(fSuspendPolicy) {
			case EventRequestImpl.SUSPENDPOL_NONE_JDWP:
				return EventRequest.SUSPEND_NONE;
			case EventRequestImpl.SUSPENDPOL_EVENT_THREAD_JDWP:
				return EventRequest.SUSPEND_EVENT_THREAD;
			case EventRequestImpl.SUSPENDPOL_ALL_JDWP:
				return EventRequest.SUSPEND_ALL;
			default:
				throw new InternalException("Invalid suspend policy encountered: " + fSuspendPolicy);
		}
	}
	
	/**
	 * Resumes threads that were suspended by this event set.
	 */
	public void resume() {
		switch(fSuspendPolicy) {
			case EventRequestImpl.SUSPENDPOL_NONE_JDWP:
				break;
			case EventRequestImpl.SUSPENDPOL_EVENT_THREAD_JDWP:
				resumeThreads();
				break;
			case EventRequestImpl.SUSPENDPOL_ALL_JDWP:
				virtualMachineImpl().resume();
				break;
			default:
				throw new InternalException("Invalid suspend policy encountered: " + fSuspendPolicy);
		}
	}
	
	/**
	 * Resumes threads that were suspended by this event set.
	 */
	private void resumeThreads() {
		Iterator iter = fEvents.iterator();
		while (iter.hasNext()) {
			EventImpl event = (EventImpl)iter.next();
			if (event.thread() != null) {
				event.thread().resume();
			}
		}
	}
	
	/**
	 * @return Returns EventSetImpl that was read from InputStream.
	 */
	public static EventSetImpl read(MirrorImpl target, DataInputStream in) throws IOException {
		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
		EventSetImpl eventSet = new EventSetImpl(vmImpl);
		
		// Read suspend policy.
		eventSet.fSuspendPolicy = target.readByte("suspendPolicy", EventRequestImpl.suspendPolicyMap(), in);
		// Read size.
		int size = target.readInt("size", in);
		// Create event list.
		eventSet.fEvents = new ArrayList(size);
		
		while (size-- > 0) {
			EventImpl event = EventImpl.read(target, in);
			
			// If event == null than it is an event that must not be given to the application.
			// See ClassPrepareEvent.
			if (event == null)
				continue;

			EventRequestImpl request = (EventRequestImpl)event.request();
			
			// Check if the request corresponding to the event was not generated from inside this JDI implementation.
			if (request == null || !request.isGeneratedInside())
				eventSet.fEvents.add(event);

		}
		return eventSet;
	}

	/**
	 * @see java.util.Collection
	 */
	public boolean contains(Object event) {
		return fEvents.contains(event);
	}
	
	/**
	 * @see java.util.Collection
	 */
	public boolean containsAll(Collection events) {
		return fEvents.containsAll(events);
	}
	
	/**
	 * @see java.util.Collection
	 */
	public boolean equals(Object object) {
		return object != null && object.getClass().equals(this.getClass()) && fEvents.equals(((EventSetImpl)object).fEvents);
	}
	
	/**
	 * @see java.util.Collection
	 */
	public int hashCode() {
		return fEvents.hashCode();
	}
	
	/**
	 * @see java.util.Collection
	 */
	public boolean isEmpty() {
		return fEvents.isEmpty();
	}
	
	/**
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return fEvents.iterator();
	}
	
	/**
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return fEvents.size();
	}

	/**
	 * @see java.util.Collection#toArray()
	 */
	public Object toArray()[] {
		return fEvents.toArray();
	}

	/**
	 * @see java.util.Collection#toArray(Object[])
	 */
	public Object[] toArray(Object[] events) {
		return fEvents.toArray(events);
	}

	/**
	 * @see java.util.Collection#add(Object).
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public boolean add(Object arg1) {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}

	/**
	 * @see java.util.Collection#addAll(Collection)
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public boolean addAll(Collection arg1) {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}

	/**
	 * @see java.util.Collection#clear()
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public void clear() {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}

	/**
	 * @see java.util.Collection#remove(Object)
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public boolean remove(Object arg1) {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}

	/**
	 * @see java.util.Collection#removeAll(Collection)
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public boolean removeAll(Collection arg1) {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}

	/**
	 * @see java.util.Collection#retainAll(Collection)
	 * @exception UnsupportedOperationException always thrown since EventSets are unmodifiable.
	 */
	public boolean retainAll(Collection arg1) {
		throw new UnsupportedOperationException("EventSets are unmodifiable");
	}
}
