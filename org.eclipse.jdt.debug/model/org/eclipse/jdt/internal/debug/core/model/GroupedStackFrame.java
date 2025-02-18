/*******************************************************************************
 * Copyright (c) 2022, 2025 Zsombor Gegesy and others.
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
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Class to collect multiple stack frame.
 *
 */
public class GroupedStackFrame extends JDIDebugElement {
	private final List<IJavaStackFrame> stackFrames = new ArrayList<>();

	public GroupedStackFrame(JDIDebugTarget target) {
		super(target);
	}

	public void add(IJavaStackFrame frame) {
		stackFrames.add(frame);
	}

	public int getFrameCount() {
		return stackFrames.size();
	}

	/**
	 * Return an array of child stack frames from the internally stored frames.
	 */
	public Object[] getFramesAsArray(int index, int length) {
		var subList = stackFrames.subList(index, Math.min(index + length, stackFrames.size()));
		return subList.isEmpty() ? null : subList.toArray();
	}

	/**
	 * @return the top most frame from the stack frames grouped inside.
	 */
	public IJavaStackFrame getTopMostFrame() {
		return !stackFrames.isEmpty() ? stackFrames.get(0) : null;
	}

	/**
	 * Delegates actions, and everything to the top most stack frame.
	 */
	@Override
	public <T> T getAdapter(Class<T> adapterType) {
		var adapter = super.getAdapter(adapterType);
		if (adapter == null && !stackFrames.isEmpty()) {
			adapter = stackFrames.get(0).getAdapter(adapterType);
		}
		return adapter;
	}
}
