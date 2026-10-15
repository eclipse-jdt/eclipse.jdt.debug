/*******************************************************************************
 * Copyright (c) 2026 Hélios Gilles and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios Gilles - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Keeps track of the fields pinned to the top of the variables views ("Pin to Top").
 * <p>
 * Pins are specific to a view (e.g. the Variables view or the Expressions view), identified by its id: pinning a field
 * in a view does not affect the other views.
 * </p>
 * <p>
 * A pinned field is identified by the fully qualified name of its declaring type and its name, so pinning a field
 * applies to every instance of the declaring type and of its subtypes, in every debug session. Pinned fields are shown
 * first, in the order of the pins: a new pin goes last, and pins can be moved up or down. The pins of a view are
 * persisted, in order, in the JDT Debug UI preference store under {@link #getPreferenceKey(String)}, which is a display
 * option of the variables views (see {@link JavaDebugOptionsManager}).
 * </p>
 * <p>
 * Read methods are thread safe and cheap when nothing is pinned, as they are called from content and label jobs.
 * Write methods are expected to be called from the UI thread.
 * </p>
 */
public final class PinnedFieldsManager implements IPropertyChangeListener {

	private static final char FIELD_SEPARATOR = '#';

	private static final String PREFERENCE_SUFFIX = "." + IJDIPreferencesConstants.PREF_PINNED_FIELDS; //$NON-NLS-1$

	private static PinnedFieldsManager fgDefault;

	private final IPreferenceStore fStore;

	/**
	 * Immutable snapshots of the pinned field keys, in pin order, by view id. A snapshot is read from the preference
	 * store on first use and replaced on every change.
	 */
	private final Map<String, List<String>> fPinned = new ConcurrentHashMap<>();

	private PinnedFieldsManager(IPreferenceStore store) {
		fStore = store;
		store.addPropertyChangeListener(this);
	}

	/**
	 * Returns the shared instance, backed by the JDT Debug UI preference store.
	 *
	 * @return the shared instance
	 */
	public static synchronized PinnedFieldsManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new PinnedFieldsManager(JDIDebugUIPlugin.getDefault().getPreferenceStore());
		}
		return fgDefault;
	}

	/**
	 * Disposes the shared instance, if any. Called when the plug-in stops.
	 */
	public static synchronized void shutdown() {
		if (fgDefault != null) {
			fgDefault.fStore.removePropertyChangeListener(fgDefault);
			fgDefault = null;
		}
	}

	/**
	 * Returns the key of the preference holding the fields pinned in the given view.
	 *
	 * @param viewId
	 *            the id of a view
	 * @return the key of the view specific preference
	 */
	public static String getPreferenceKey(String viewId) {
		return viewId + PREFERENCE_SUFFIX;
	}

	/**
	 * Returns whether the given preference holds the fields pinned in a view.
	 *
	 * @param property
	 *            the key of a preference
	 * @return whether the preference holds the fields pinned in a view
	 */
	public static boolean isPinnedFieldsPreference(String property) {
		return property.endsWith(PREFERENCE_SUFFIX) && property.length() > PREFERENCE_SUFFIX.length();
	}

	/**
	 * Returns whether at least one field is pinned in the given view.
	 *
	 * @param viewId
	 *            the id of a view, may be <code>null</code>
	 * @return whether at least one field is pinned in the view
	 */
	public boolean hasPinnedFields(String viewId) {
		return !getPinned(viewId).isEmpty();
	}

	/**
	 * Returns whether the given field is pinned in the given view.
	 *
	 * @param viewId
	 *            the id of a view, may be <code>null</code>
	 * @param field
	 *            a field variable
	 * @return whether the given field is pinned in the view
	 */
	public boolean isPinned(String viewId, IJavaFieldVariable field) {
		return getRank(getPinned(viewId), field) >= 0;
	}

	/**
	 * Pins or unpins the given fields in the given view.
	 *
	 * @param viewId
	 *            the id of a view
	 * @param fields
	 *            the fields to update
	 * @param pin
	 *            <code>true</code> to pin the fields, <code>false</code> to unpin them
	 */
	public void setPinned(String viewId, Collection<? extends IJavaFieldVariable> fields, boolean pin) {
		Set<String> keys = new LinkedHashSet<>(getPinned(viewId));
		boolean changed = false;
		for (IJavaFieldVariable field : fields) {
			String key = getKey(field);
			if (key != null) {
				changed |= pin ? keys.add(key) : keys.remove(key);
			}
		}
		if (changed) {
			update(viewId, new ArrayList<>(keys));
		}
	}

	/**
	 * Returns whether the given field can be displayed one row higher (or lower) among the pinned fields displayed with
	 * it in the given view.
	 *
	 * @param viewId
	 *            the id of a view, may be <code>null</code>
	 * @param field
	 *            a field
	 * @param siblings
	 *            the children displayed with the field (including the field itself)
	 * @param up
	 *            <code>true</code> to move the field up, <code>false</code> to move it down
	 * @return whether {@link #movePin(String, IJavaFieldVariable, Object[], boolean)} would change the order of the pins
	 */
	public boolean canMovePin(String viewId, IJavaFieldVariable field, Object[] siblings, boolean up) {
		List<String> pinned = getPinned(viewId);
		return findNeighbor(pinned, getRank(pinned, field), siblings, up) >= 0;
	}

	/**
	 * Moves the pin of the given field before (or after) the pin of the previous (or next) pinned field among its
	 * siblings, so that the field is displayed one row higher (or lower). Pins of fields that are not among the
	 * siblings are left untouched. Only the pins of the given view are concerned.
	 *
	 * @param viewId
	 *            the id of a view
	 * @param field
	 *            a pinned field
	 * @param siblings
	 *            the children displayed with the field (including the field itself)
	 * @param up
	 *            <code>true</code> to move the field up, <code>false</code> to move it down
	 * @return whether the order of the pins changed
	 */
	public boolean movePin(String viewId, IJavaFieldVariable field, Object[] siblings, boolean up) {
		List<String> pinned = getPinned(viewId);
		int rank = getRank(pinned, field);
		int neighbor = findNeighbor(pinned, rank, siblings, up);
		if (neighbor < 0) {
			return false;
		}
		List<String> keys = new ArrayList<>(pinned);
		Collections.swap(keys, rank, neighbor);
		update(viewId, keys);
		return true;
	}

	/**
	 * Returns the rank of the closest pin before (or after) the given rank among the pins of the siblings.
	 *
	 * @return the rank of the neighbor pin, or <code>-1</code> if none (or if the given rank is <code>-1</code>)
	 */
	private static int findNeighbor(List<String> pinned, int rank, Object[] siblings, boolean up) {
		if (rank < 0) {
			return -1;
		}
		int neighbor = -1;
		for (Object sibling : siblings) {
			if (sibling instanceof IJavaFieldVariable siblingField) {
				int siblingRank = getRank(pinned, siblingField);
				if (up ? siblingRank >= 0 && siblingRank < rank && siblingRank > neighbor
						: siblingRank > rank && (neighbor < 0 || siblingRank < neighbor)) {
					neighbor = siblingRank;
				}
			}
		}
		return neighbor;
	}

	/**
	 * Unpins all the fields pinned in the given view.
	 *
	 * @param viewId
	 *            the id of a view
	 */
	public void unpinAll(String viewId) {
		fPinned.put(viewId, List.of());
		fStore.setToDefault(getPreferenceKey(viewId));
	}

	/**
	 * Returns the given children with the fields pinned in the given view moved first, in the order of the pins. The
	 * relative order of the other children is preserved.
	 *
	 * @param viewId
	 *            the id of a view, may be <code>null</code>
	 * @param children
	 *            children of a variable, an expression or a stack frame
	 * @return the reordered children, or the given array itself when no child is pinned
	 */
	public Object[] movePinnedFirst(String viewId, Object[] children) {
		List<String> keys = getPinned(viewId);
		if (keys.isEmpty() || children.length < 2) {
			return children;
		}
		Object[] pinned = new Object[keys.size()];
		List<Object> others = new ArrayList<>(children.length);
		boolean found = false;
		for (Object child : children) {
			int rank = child instanceof IJavaFieldVariable field ? getRank(keys, field) : -1;
			if (rank >= 0 && pinned[rank] == null) {
				pinned[rank] = child;
				found = true;
			} else {
				others.add(child);
			}
		}
		if (!found) {
			return children;
		}
		List<Object> result = new ArrayList<>(children.length);
		for (Object child : pinned) {
			if (child != null) {
				result.add(child);
			}
		}
		result.addAll(others);
		return result.toArray();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (isPinnedFieldsPreference(property)) {
			// e.g. preferences imported
			String viewId = property.substring(0, property.length() - PREFERENCE_SUFFIX.length());
			fPinned.put(viewId, parse(fStore.getString(property)));
		}
	}

	/**
	 * Returns the keys of the fields pinned in the given view, in pin order.
	 *
	 * @param viewId
	 *            the id of a view, may be <code>null</code>
	 * @return an immutable list of keys
	 */
	private List<String> getPinned(String viewId) {
		if (viewId == null) {
			return List.of();
		}
		List<String> pinned = fPinned.get(viewId);
		if (pinned == null) {
			pinned = fPinned.computeIfAbsent(viewId, id -> parse(fStore.getString(getPreferenceKey(id))));
		}
		return pinned;
	}

	private void update(String viewId, List<String> keys) {
		fPinned.put(viewId, List.copyOf(keys));
		// JavaDebugOptionsManager refreshes the variables views when this preference changes
		fStore.setValue(getPreferenceKey(viewId), JavaDebugOptionsManager.serializeList(keys.toArray(String[]::new)));
	}

	/**
	 * Returns the rank of the pin of the given field.
	 *
	 * @param pinned
	 *            the pinned keys
	 * @param field
	 *            a field variable
	 * @return the rank of the pin of the field, or <code>-1</code> if the field is not pinned
	 */
	private static int getRank(List<String> pinned, IJavaFieldVariable field) {
		if (pinned.isEmpty()) {
			return -1;
		}
		String key = getKey(field);
		return key == null ? -1 : pinned.indexOf(key);
	}

	/**
	 * Returns the key identifying the given field in the preference.
	 *
	 * @param field
	 *            a field variable
	 * @return the key of the field, or <code>null</code> if it cannot be computed (e.g. the target is disconnected)
	 */
	private static String getKey(IJavaFieldVariable field) {
		try {
			IJavaType declaringType = field.getDeclaringType();
			return declaringType == null ? null : declaringType.getName() + FIELD_SEPARATOR + field.getName();
		} catch (DebugException e) {
			return null;
		}
	}

	private static List<String> parse(String value) {
		Set<String> keys = new LinkedHashSet<>();
		for (String key : JavaDebugOptionsManager.parseList(value)) {
			if (key.indexOf(FIELD_SEPARATOR) > 0) {
				keys.add(key);
			}
		}
		return List.copyOf(keys);
	}
}
