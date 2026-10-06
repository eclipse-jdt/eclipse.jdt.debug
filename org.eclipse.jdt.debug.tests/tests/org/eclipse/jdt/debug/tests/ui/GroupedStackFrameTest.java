/*******************************************************************************
 * Copyright (c) 2025 Zsombor Gegesy and others.
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
package org.eclipse.jdt.debug.tests.ui;


import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.model.GroupedStackFrame;

import junit.framework.TestCase;

public class GroupedStackFrameTest extends TestCase {

	private GroupedStackFrame groupedStackFrame;

	private IJavaStackFrame mockFrame1;
	private IJavaStackFrame mockFrame2;

	@Override
	public void setUp() throws Exception {
		groupedStackFrame = new GroupedStackFrame(null);
		mockFrame1 = JavaStackFrameMock.createFrame(JavaReferenceTypeMock.createReference("java.util.ArrayList"), false);
		mockFrame2 = JavaStackFrameMock.createFrame(JavaReferenceTypeMock.createReference("java.util.LinkedList"), false);
	}

	public void testAddFrame() {
		groupedStackFrame.add(mockFrame1);
		assertEquals(1, groupedStackFrame.getFrameCount());

		groupedStackFrame.add(mockFrame2);
		assertEquals(2, groupedStackFrame.getFrameCount());
	}

	public void testGetFrameCount() {
		assertEquals(0, groupedStackFrame.getFrameCount());

		groupedStackFrame.add(mockFrame1);
		assertEquals(1, groupedStackFrame.getFrameCount());

		groupedStackFrame.add(mockFrame2);
		assertEquals(2, groupedStackFrame.getFrameCount());
	}

	public void testGetFramesAsArray() {
		groupedStackFrame.add(mockFrame1);
		groupedStackFrame.add(mockFrame2);

		Object[] frames = groupedStackFrame.getFramesAsArray(0, 2);
		assertNotNull(frames);
		assertEquals(2, frames.length);
		assertSame(mockFrame1, frames[0]);
		assertSame(mockFrame2, frames[1]);

		frames = groupedStackFrame.getFramesAsArray(1, 1);
		assertNotNull(frames);
		assertEquals(1, frames.length);
		assertSame(mockFrame2, frames[0]);

		frames = groupedStackFrame.getFramesAsArray(2, 1);
		assertNull(frames);
	}

	public void testGetTopMostFrame() {
		assertNull(groupedStackFrame.getTopMostFrame());

		groupedStackFrame.add(mockFrame1);
		assertSame(mockFrame1, groupedStackFrame.getTopMostFrame());

		groupedStackFrame.add(mockFrame2);
		assertSame(mockFrame1, groupedStackFrame.getTopMostFrame());
	}

	public void testGetAdapter() {
		var adapterType = String.class;

		groupedStackFrame.add(mockFrame1);
		Object adapter = groupedStackFrame.getAdapter(adapterType);
		assertEquals("getAdapter called with class java.lang.String", adapter);

		groupedStackFrame = new GroupedStackFrame(null);
		adapter = groupedStackFrame.getAdapter(adapterType);
		assertNull(adapter);
	}

}
