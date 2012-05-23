/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdi.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to cache values. It uses soft references to store cached
 * values. Once a value is garbage collected by the VM, the corresponding entry
 * is removed from the cache on the next invocation of put() or get().
 * 
 * Note that WeakHashMap can't be used for this purpose because in WeakHashMap
 * soft references are only used for the keys, and values may not have 'strong'
 * references to keys otherwise they will never be garbage collected.
 * 
 */
public class ValueCache {
	/**
	 * Map to store <key, Reference> pairs, where Reference is a soft reference
	 * to an Object.
	 */
	private Map<Object, SoftReference<Object>> cacheTable = new Hashtable<Object, SoftReference<Object>>();
	/**
	 * Map to store <Reference, key> pairs, to find the cacheTable-key of a
	 * garbage collected Reference.
	 */
	private Map<SoftReference<Object>, Object> refTable = new Hashtable<SoftReference<Object>, Object>();

	/**
	 * The reference-queue that is registered with the soft references. The
	 * garbage collector will enqueue soft references that are garbage
	 * collected.
	 */
	private ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();

	/**
	 * Clean up all entries from the table for which the values were garbage
	 * collected.
	 */
	private void cleanup() {
		Reference<?> ref;
		while ((ref = refQueue.poll()) != null) {
			Object key = refTable.get(ref);
			if (key != null)
				cacheTable.remove(key);
			refTable.remove(ref);
		}
	}

	/**
	 * Put a new entry in the cache under the given key.
	 */
	public void put(Object key, Object value) {
		cleanup();
		SoftReference<Object> ref = new SoftReference<Object>(value, refQueue);
		cacheTable.put(key, ref);
		refTable.put(ref, key);
	}

	/**
	 * Get entry from the cache.
	 * 
	 * @return Returns value that is cached under the given key, or null of one
	 *         of the following is true: - The value has not been cached. - The
	 *         value had been cached but is garbage collected.
	 */
	public Object get(Object key) {
		cleanup();
		Object value = null;
		SoftReference<?> ref = cacheTable.get(key);
		if (ref != null) {
			value = ref.get();
		}
		return value;
	}

	/**
	 * Returns a Collection view of the values contained in this cache.
	 */
	public Collection<Object> values() {
		cleanup();
		List<Object> returnValues = new ArrayList<Object>();
		synchronized (cacheTable) {
			Iterator<SoftReference<Object>> iter = cacheTable.values().iterator();
			SoftReference<Object> ref;
			Object value;
			while (iter.hasNext()) {
				ref = iter.next();
				value = ref.get();
				if (value != null) {
					returnValues.add(value);
				}
			}
		}
		return returnValues;
	}

	/**
	 * Returns a Collection view of the values contained in this cache that have
	 * the same runtime class as the given Class.
	 */
	public Collection<Object> valuesWithType(Class<?> type) {
		cleanup();
		List<Object> returnValues = new ArrayList<Object>();
		synchronized (cacheTable) {
			Iterator<SoftReference<Object>> iter = cacheTable.values().iterator();
			SoftReference<Object> ref;
			Object value;
			while (iter.hasNext()) {
				ref = iter.next();
				value = ref.get();
				if (value != null && value.getClass().equals(type)) {
					returnValues.add(value);
				}
			}
		}
		return returnValues;
	}

	/**
	 * Removes the key and its corresponding value from this cache.
	 * 
	 * @return Returns The value to which the key had been mapped in this
	 *         hashtable, or null if the key did not have a mapping.
	 */
	public Object remove(Object key) {
		cleanup();
		Object value = null;
		SoftReference<?> ref = cacheTable.get(key);
		if (ref != null) {
			value = ref.get();
			refTable.remove(ref);
		}
		cacheTable.remove(key);
		return value;
	}
}
