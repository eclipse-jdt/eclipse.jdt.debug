/*******************************************************************************
 * Copyright (c) 2021, 2025 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.Set;

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Provides foreground and background colors for stack frames in a debug view. After usage, it needs to be closed, so it could unregister itself from
 * preference storage.
 *
 */
public final class StackFramePresentationProvider implements IPropertyChangeListener {

	private static final Set<String> JAR_ICON = Set.of(StackFrameCategorizer.CATEGORY_LIBRARY.name(), StackFrameCategorizer.CATEGORY_SYNTHETIC.name(), StackFrameCategorizer.CATEGORY_PLATFORM.name());
	private final IPreferenceStore store;
	private boolean collapseStackFrames;

	public StackFramePresentationProvider(IPreferenceStore store) {
		this.store = store;
		store.addPropertyChangeListener(this);
		collapseStackFrames = store.getBoolean(IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES);
	}

	public StackFramePresentationProvider() {
		this(JDIDebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @return the category specific image for the stack frame, or null, if there is no one defined.
	 */
	public ImageDescriptor getStackFrameImage(IJavaStackFrame frame) {
		if (collapseStackFrames) {
			var category = frame.getCategory();
			if (category != null) {
				if (JAR_ICON.contains(category.name())) {
					return JavaPluginImages.DESC_OBJS_JAR;
				}
			}
		}
		return null;
	}

	/**
	 * Unsubscribes from the {@link IPreferenceStore} to not receive notifications.
	 */
	public void close() {
		store.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getProperty();
		if (IJDIPreferencesConstants.PREF_COLLAPSE_STACK_FRAMES.equals(prop)) {
			collapseStackFrames = (Boolean) event.getNewValue();
		}
	}

	/**
	 * @return if stack frames should be collapsed.
	 */
	public boolean isCollapseStackFrames() {
		return collapseStackFrames;
	}

}
