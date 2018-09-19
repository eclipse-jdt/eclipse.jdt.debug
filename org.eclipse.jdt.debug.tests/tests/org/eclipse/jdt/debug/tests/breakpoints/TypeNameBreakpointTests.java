/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.jdt.debug.tests.breakpoints;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.testplugin.DebugElementKindEventDetailWaiter;
import org.eclipse.jdt.debug.testplugin.DebugEventWaiter;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.debug.tests.TestAgainException;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaClassPrepareBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaExceptionBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaMethodEntryBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaStratumLineBreakpoint;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

/**
 * Tests getting and setting the type name for Java breakpoints
 *
 * @since 3.8.200
 */
public class TypeNameBreakpointTests extends AbstractDebugTest {

	class TestJDIPresentation extends JDIModelPresentation {
		@Override
		public String getMarkerTypeName(IJavaBreakpoint breakpoint, boolean qualified) throws CoreException {
			return super.getMarkerTypeName(breakpoint, qualified);
		}
	}

	TestJDIPresentation fPresentation = new TestJDIPresentation();

	/**
	 * Constructor
	 * @param name
	 */
	public TypeNameBreakpointTests(String name) {
		super(name);
	}

	@Override
	protected IJavaThread launchToBreakpoint(ILaunchConfiguration config, boolean register) throws CoreException {
		DebugEventWaiter waiter = new DebugElementKindEventDetailWaiter(DebugEvent.SUSPEND, IJavaThread.class, DebugEvent.BREAKPOINT);
		waiter.setTimeout(7000);
		Object suspendee = null;
		try {
			suspendee = launchAndWait(config, waiter, register);
		}
		catch (TestAgainException tae) {
			return null;
		}
		return (IJavaThread) suspendee;
	}

	/**
	 * Tests the {@link JavaDebugUtils#typeNamesEqual(String, String)} method
	 *
	 * @throws Exception
	 */
	public void testTypeNamesEqual() throws Exception {
		assertTrue("The type names should be equal when both null", JavaDebugUtils.typeNamesEqual(null, null));
		assertTrue("The type names should be equal when the same string", JavaDebugUtils.typeNamesEqual("type", "type"));
		assertFalse("The type names should not match when one is null", JavaDebugUtils.typeNamesEqual(null, "type"));
		assertFalse("The type names should not match when one is null", JavaDebugUtils.typeNamesEqual("type", null));
	}

	/**
	 * Returns the {@link IResource} for the class HitCountLooper
	 *
	 * @return the {@link IResource} for HitCountLooper
	 */
	IResource getTestResource() throws Exception {
		return get14Project().getProject().getFile("src/HitCountLooper.java");
	}

	/**
	 * Util to get a line breakpoint with a null type name
	 *
	 * @return
	 * @throws Exception
	 */
	JavaLineBreakpoint getNullTypeLineBreakpoint() throws Exception {
		JavaLineBreakpoint bp = (JavaLineBreakpoint) JDIDebugModel.createLineBreakpoint(getTestResource(), null, 16, -1, -1, 0, true, null);
		assertNotNull("The null type breakpoint should exist", bp);
		return bp;
	}

	/**
	 * Util to get an exception breakpoint with a null type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaExceptionBreakpoint getNullTypeExBreakpoint() throws Exception {
		JavaExceptionBreakpoint bp = (JavaExceptionBreakpoint) JDIDebugModel.createExceptionBreakpoint(getTestResource(), null, true, false, true, true, null);
		assertNotNull("The exception breakpoint should not be null", bp);
		return bp;
	}

	/**
	 * Util to get a method entry breakpoint with a null type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaMethodEntryBreakpoint getNullTypeMethodEntryBreakpoint() throws Exception {
		JavaMethodEntryBreakpoint bp = (JavaMethodEntryBreakpoint) JDIDebugModel.createMethodEntryBreakpoint(getTestResource(), null, null, null, 15, -1, -1, 0, true, null);
		assertNotNull("The method entry breakpoint should not be null", bp);
		return bp;
	}

	/**
	 * Util to get a stratum line breakpoint with no type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaStratumLineBreakpoint getNullTypeStratumLineBreakpoint() throws Exception {
		JavaStratumLineBreakpoint bp = (JavaStratumLineBreakpoint) JDIDebugModel.createStratumBreakpoint(getTestResource(), null, null, null, null, 15, -1, -1, 0, true, null);
		assertNotNull("The stratum breakpoint should not be null", bp);
		return bp;
	}

	/**
	 * Util to get a class prepare breakpoint with a null type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaClassPrepareBreakpoint getNullTypeClassPrepareBreakpoint() throws Exception {
		JavaClassPrepareBreakpoint bp = (JavaClassPrepareBreakpoint) JDIDebugModel.createClassPrepareBreakpoint(getTestResource(), null, 1, -1, -1, true, null);
		assertNotNull("The class prepare breakpoint should not be null", bp);
		return bp;
	}

	/**
	 * Util to get a watchpoint with a null type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaWatchpoint getNullTypeWatchpoint() throws Exception {
		JavaWatchpoint bp = (JavaWatchpoint) JDIDebugModel.createWatchpoint(getTestResource(), null, null, 15, -1, -1, 0, true, null);
		bp.setModification(true);
		bp.setAccess(true);
		assertNotNull("The watchpoint should not be null", bp);
		return bp;
	}

	/**
	 * Util to get a method breakpoint with a null type
	 *
	 * @return
	 * @throws Exception
	 */
	JavaMethodBreakpoint getNullTypeMethodBreakpoint() throws Exception {
		JavaMethodBreakpoint bp = (JavaMethodBreakpoint) JDIDebugModel.createMethodBreakpoint(getTestResource(), null, null, null, true, true, false, 15, -1, -1, 0, true, null);
		assertNotNull("The method breakpoint should not be null", bp);
		return bp;
	}

	/**
	 * Tests {@link JDIDebugModel#createLineBreakpoint(org.eclipse.core.resources.IResource, String, int, int, int, int, boolean, java.util.Map)} with
	 * a null type name
	 *
	 * @throws Exception
	 */
	public void testCreateLineBPNullTypeName() throws Exception {
		try {
			JavaLineBreakpoint bp = getNullTypeLineBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the
	 * {@link JDIDebugModel#createMethodBreakpoint(IResource, String, String, String, boolean, boolean, boolean, int, int, int, int, boolean, java.util.Map)}
	 * method with null type infos
	 *
	 * @throws Exception
	 */
	public void testCreateMethodBPNullTypeName() throws Exception {
		try {
			JavaMethodBreakpoint bp = getNullTypeMethodBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#createExceptionBreakpoint(IResource, String, boolean, boolean, boolean, boolean, java.util.Map)} method with
	 * null type infos
	 *
	 * @throws Exception
	 */
	public void testCreateExceptionBPNullTypeName() throws Exception {
		try {
			JavaExceptionBreakpoint bp = getNullTypeExBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#createClassPrepareBreakpoint(IResource, String, int, int, int, boolean, java.util.Map)} method with null type
	 * infos
	 *
	 * @throws Exception
	 */
	public void testCreateClassPrepareBPNullTypeName() throws Exception {
		try {
			JavaClassPrepareBreakpoint bp = getNullTypeClassPrepareBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#createWatchpoint(IResource, String, String, int, int, int, int, boolean, java.util.Map)} method with null type
	 * infos
	 *
	 * @throws Exception
	 */
	public void testCreateWatchpointBPNullTypeName() throws Exception {
		try {
			JavaWatchpoint bp = getNullTypeWatchpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#createStratumBreakpoint(IResource, String, String, String, String, int, int, int, int, boolean, java.util.Map)}
	 * method with null type infos
	 *
	 * @throws Exception
	 */
	public void testCreateStratumBPNullTypeName() throws Exception {
		try {
			JavaStratumLineBreakpoint bp = getNullTypeStratumLineBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#createMethodEntryBreakpoint(IResource, String, String, String, int, int, int, int, boolean, java.util.Map)}
	 * method with null type infos
	 *
	 * @throws Excpetion
	 */
	public void testCreateMethodEntryBPNullTypeName() throws Exception {
		try {
			JavaMethodEntryBreakpoint bp = getNullTypeMethodEntryBreakpoint();
			assertNull("The type name should be null", bp.getTypeName());
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getMarkerTypeName}
	 *
	 * @throws Exception
	 */
	public void testGetPresentationNullTypeName() throws Exception {
		try {
			JavaBreakpoint bp = getNullTypeLineBreakpoint();
			String value = fPresentation.getMarkerTypeName(bp, true);
			assertNull("The value should be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getExceptionBreakpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull2() throws Exception {
		try {
			JavaExceptionBreakpoint bp = getNullTypeExBreakpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getLineBreakpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull3() throws Exception {
		try {
			JavaLineBreakpoint bp = getNullTypeLineBreakpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getClassPrepareBreakpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull4() throws Exception {
		try {
			JavaClassPrepareBreakpoint bp = getNullTypeClassPrepareBreakpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getWatchpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull5() throws Exception {
		try {
			JavaWatchpoint bp = getNullTypeWatchpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getMethodBreakpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull6() throws Exception {
		try {
			JavaMethodBreakpoint bp = getNullTypeMethodBreakpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIModelPresentation#getStratumLineBreakpointText} with a null type name
	 *
	 * @throws Exception
	 */
	public void testGetPresentationTypeNameNull7() throws Exception {
		try {
			JavaStratumLineBreakpoint bp = getNullTypeStratumLineBreakpoint();
			String value = fPresentation.getText(bp);
			assertNotNull("The value should not be null", value);
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#lineBreakpointExists(String, int)} method
	 *
	 * @throws Exception
	 */
	public void testJDIDebugModelTypeName2() throws Exception {
		try {
			getNullTypeLineBreakpoint(); // create one
			assertNotNull("The null typed line breakpoint should exist", JDIDebugModel.lineBreakpointExists(null, 16));
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests the {@link JDIDebugModel#lineBreakpointExists(String, int)} method with a null type name
	 *
	 * @throws Exception
	 */
	public void testJDIModelTypeNameNull1() throws Exception {
		try {
			getNullTypeLineBreakpoint();
			assertNotNull("The null typed line breakpoint should exist", JDIDebugModel.lineBreakpointExists(getTestResource(), null, 16));
		}
		finally {
			removeAllBreakpoints();
		}
	}

	/**
	 * Tests that launching with a null typed breakpoint does not suspend and does not cause a failure while trying to create requests
	 *
	 * @throws Exception
	 */
	public void testLaunchNullTypeLineBreakpoint() throws Exception {
		getNullTypeLineBreakpoint();
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(getLaunchConfiguration("HitCountLooper"));
			assertNull("The program should not suspend or cause an excpetion", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeLaunch();
		}
	}

	/**
	 * Tests launching with an exception breakpoint with a null type name
	 *
	 * @throws Exception
	 */
	public void testLaunchNullTypeExceptionBreakpoint() throws Exception {
		getNullTypeExBreakpoint();
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint("ThrowsException");
			assertNull("Breakpoint should not cause suspend or excpetion", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeLaunch();
		}
	}

	/**
	 * Tests launching with a watchpoint with a null type
	 *
	 * @throws Exception
	 */
	public void testLaunchNullTypeWatchpoint() throws Exception {
		getNullTypeWatchpoint();
		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint("org.eclipse.debug.tests.targets.Watchpoint");
			assertNull("Breakpoint should not cause suspend or exception", thread);
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
			removeLaunch();
		}
	}

	private void removeLaunch() {
		ILaunchManager launchManager = getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		assertEquals("expected exactly 1 launch but got: " + Arrays.toString(launches), 1, launches.length);
		launchManager.removeLaunch(launches[0]);
	}
}
