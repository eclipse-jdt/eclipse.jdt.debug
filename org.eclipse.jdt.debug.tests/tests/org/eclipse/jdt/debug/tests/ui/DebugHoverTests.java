/*******************************************************************************
 *  Copyright (c) 2017, 2024 Andrey Loskutov and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import static org.junit.Assert.assertNotEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdi.internal.StringReferenceImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.tests.TestUtil;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.model.JDIModificationVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugHover;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.test.OrderedTestSuite;

import junit.framework.Test;

/**
 * Tests for debug view.
 */
public class DebugHoverTests extends AbstractDebugUiTests {

	private static final String VAL_PREFIX = new String(org.eclipse.jdt.internal.compiler.lookup.TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX);

	public static Test suite() {
		return new OrderedTestSuite(DebugHoverTests.class);
	}

	private boolean showMonitorsOriginal;

	public DebugHoverTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		showMonitorsOriginal = jdiUIPreferences.getBoolean(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO);
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, true);
		resetPerspective(DebugViewPerspectiveFactory.ID);
		processUiEvents(100);
	}

	@Override
	protected void tearDown() throws Exception {
		IPreferenceStore jdiUIPreferences = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		jdiUIPreferences.setValue(IJavaDebugUIConstants.PREF_SHOW_MONITOR_THREAD_INFO, showMonitorsOriginal);
		sync(() -> getActivePage().closeAllEditors(false));
		processUiEvents(100);
		super.tearDown();
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get18Project();
	}

	public void testResolveInLambda() throws Exception {
		final String typeName = "DebugHoverTest18";
		final String expectedMethod1 = "lambda$0";
		final String expectedMethod2 = "lambda$1";
		final int framesNumber1 = 6;
		final int framesNumber2 = 9;
		final int bpLine1 = 34;
		final int bpLine2 = 41;

		IJavaBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName);
		IJavaBreakpoint bp2 = createLineBreakpoint(bpLine2, "", typeName + ".java", typeName);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		bp2.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp1.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod1, framesNumber1, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			Map<String, Region> offsets = new LinkedHashMap<>();
			offsets.put(VAL_PREFIX + "arg", new Region(1059, "arg".length()));
			offsets.put(VAL_PREFIX + "var1", new Region(1030, "var1".length()));
			offsets.put(/* local */ "var2", new Region(1001, "var2".length()));

			Set<Entry<String, Region>> entrySet = offsets.entrySet();
			int startLine = bpLine1;
			int valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, valueIndex++, part, hover, varData);
			}

			resumeToLineBreakpoint(thread, (ILineBreakpoint) bp2);
			part = openEditorAndValidateStack(expectedMethod2, framesNumber2, file, thread);

			offsets = new LinkedHashMap<>();
			offsets.put(VAL_PREFIX + "arg", new Region(1216, "arg".length()));
			offsets.put(VAL_PREFIX + "var1", new Region(1186, "var1".length()));
			offsets.put(VAL_PREFIX + "var2", new Region(1156, "var2".length()));
			offsets.put(/* local */ "var3", new Region(1126, "var3".length()));

			entrySet = offsets.entrySet();
			startLine = bpLine2;
			valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, valueIndex++, part, hover, varData);
			}
		}
		finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testResolveInStaticInit() throws Exception {
		final String typeName = "Bug549394";
		final String expectedMethod1 = "<clinit>";
		final int framesNumber1 = 1;
		final int bpLine1 = 18;

		IJavaBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp1.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod1, framesNumber1, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			Map<String, Region> offsets = new LinkedHashMap<>();
			offsets.put("local", new Region(657, "local".length()));

			Set<Entry<String, Region>> entrySet = offsets.entrySet();
			int startLine = bpLine1;
			int valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, valueIndex++, part, hover, varData);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testResolveInInner() throws Exception {
		final String typeName = "Bug317045";
		final String typeName1 = typeName + "$Class0";
		final String typeName2 = typeName;
		final String typeName3 = typeName + "$Class2";
		final String expectedMethod1 = "run0";
		final String expectedMethod2 = "run11";
		final String expectedMethod3 = "run2";
		final int framesNumber1 = 3;
		final int framesNumber2 = 4;
		final int framesNumber3 = 3;
		final int bpLine1 = 61;
		final int bpLine2 = 39;
		final int bpLine3 = 71;

		IJavaBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName1);
		IJavaBreakpoint bp2 = createLineBreakpoint(bpLine2, "", typeName + ".java", typeName2);
		IJavaBreakpoint bp3 = createLineBreakpoint(bpLine3, "", typeName + ".java", typeName3);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		bp2.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		bp3.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp1.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod1, framesNumber1, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			Map<String, Region> offsets = new LinkedHashMap<>();
			offsets.put("var3", new Region(1832, "var3".length()));
			offsets.put("var2", new Region(1803, "var2".length()));
			offsets.put("var1", new Region(1774, "var1".length()));
			offsets.put("var0", new Region(1745, "var0".length()));
			String[] values = { "3", "2", "1", "00" };
			Set<Entry<String, Region>> entrySet = offsets.entrySet();
			int startLine = bpLine1;
			int valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, part, hover, varData, values[valueIndex++]);
			}

			resumeToLineBreakpoint(thread, (ILineBreakpoint) bp2);
			part = openEditorAndValidateStack(expectedMethod2, framesNumber2, file, thread);

			offsets = new LinkedHashMap<>();
			offsets.put("var3", new Region(1242, "var3".length()));
			offsets.put("var2", new Region(1210, "var2".length()));
			offsets.put("var1", new Region(1178, "var1".length()));
			offsets.put("var0", new Region(1146, "var0".length()));
			values = new String[] { "3", "21", "11", "00" };

			entrySet = offsets.entrySet();
			startLine = bpLine2;
			valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, part, hover, varData, values[valueIndex++]);
			}

			offsets = new LinkedHashMap<>();
			offsets.put("var3", new Region(1030, "var3".length()));
			offsets.put("var2", new Region(1000, "var2".length()));
			offsets.put("var1", new Region(970, "var1".length()));
			offsets.put("var0", new Region(940, "var0".length()));
			values = new String[] { "3", "2", "11", "00" };

			entrySet = offsets.entrySet();
			startLine = 32;
			valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, part, hover, varData, values[valueIndex++]);
			}

			resumeToLineBreakpoint(thread, (ILineBreakpoint) bp3);
			part = openEditorAndValidateStack(expectedMethod3, framesNumber3, file, thread);

			offsets = new LinkedHashMap<>();
			offsets.put("var3", new Region(2040, "var3".length()));
			offsets.put("var2", new Region(2011, "var2".length()));
			offsets.put("var1", new Region(1982, "var1".length()));
			offsets.put("var0", new Region(1953, "var0".length()));
			values = new String[] { "3", "22", "1", "00" };
			entrySet = offsets.entrySet();
			startLine = bpLine3;
			valueIndex = 0;
			for (Entry<String, Region> varData : entrySet) {
				// select variables and validate the hover, going backwards from the breakpoint
				validateLine(startLine--, part, hover, varData, values[valueIndex++]);
			}
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainFieldHover_2Chains_ExpectValueFromChain() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payload";
			int offset = part.getViewer().getDocument().get().indexOf("other.payload") + "other.".length();
			IRegion region = new Region(offset, "payload".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("other.payload", info.getName());
			assertEquals("r", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainFieldHover_ArrayLengthChainsOnThisExpression_ExpectValueFromChain() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("this.payloads.length") + "this.payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("this.payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainFieldHover_ArrayLengthChainsOnVariableExpression_ExpectValueFromChain() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("other.payloads.length") + "other.payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("other.payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainFieldHover_ArrayLengthChainsOnVariableThisExpression_ExpectValueFromChain() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("this.payloads.length") + "this.payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("this.payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}
	public void testBug572629_ChainFieldHover_3Chains_ExpectValueFromChainAtMiddle() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payloads";
			int offset = part.getViewer().getDocument().get().indexOf("other.payloads.length") + "other.".length();
			IRegion region = new Region(offset, "payloads".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("other.payloads", info.getName());
			assertTrue(info.getValue() instanceof IJavaArray);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainFieldHover_ArrayLengthOnStaticField_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 36;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("PAYLOADS.length") + "PAYLOADS.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("PAYLOADS.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_LocalVariableHover_ArrayLength_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOverLocal";
		final int frameNumber = 2;
		final int bpLine = 44;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("name.length") + "name.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("name.length", info.getName());
			assertEquals("4", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ParameterVariableHover_ArrayLength_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOverLocal";
		final int frameNumber = 2;
		final int bpLine = 46;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("names.length") + "names.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("names.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_ChainedLocalVariableHover_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOverLocal";
		final int frameNumber = 2;
		final int bpLine = 45;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payload";
			int offset = part.getViewer().getDocument().get().indexOf("object.payload") + "object.".length();
			IRegion region = new Region(offset, "payload".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("object.payload", info.getName());
			assertEquals("p", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_LocalArrayVariableLengthHoverInSideLambda_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 48;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().indexOf("a.length") + "a.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("a.length", info.getName());
			assertEquals("4", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573553_ChainFieldHover_DeepChain_OnThisObjectPrimitive_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOnThis";
		final int frameNumber = 2;
		final int bpLine = 53;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "age";
			int offset = part.getViewer().getDocument().get().indexOf("this.parent.child.age") + "this.parent.child.".length();
			IRegion region = new Region(offset, "age".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("this.parent.child.age", info.getName());
			assertEquals("5", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573553_ChainFieldHover_DeepChain_OnThisObject_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOnThis";
		final int frameNumber = 2;
		final int bpLine = 54;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "name";
			int offset = part.getViewer().getDocument().get().indexOf("this.parent.child.name") + "this.parent.child.".length();
			IRegion region = new Region(offset, "name".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("this.parent.child.name", info.getName());
			assertEquals("name", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573553_ChainFieldHover_DeepChain_Primitive_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOnThis";
		final int frameNumber = 2;
		final int bpLine = 55;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "age";
			int offset = part.getViewer().getDocument().get().lastIndexOf("parent.child.age") + "parent.child.".length();
			IRegion region = new Region(offset, "age".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("parent.child.age", info.getName());
			assertEquals("5", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573553_ChainFieldHover_DeepChain_Object_ExpectValue() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "hoverOnThis";
		final int frameNumber = 2;
		final int bpLine = 56;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "name";
			int offset = part.getViewer().getDocument().get().lastIndexOf("parent.child.name") + "parent.child.".length();
			IRegion region = new Region(offset, "name".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("parent.child.name", info.getName());
			assertEquals("name", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug572629_onThisExpression() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "equals";
		final int frameNumber = 2;
		final int bpLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payload";
			int offset = part.getViewer().getDocument().get().indexOf("return this.payload") + "return this.".length();
			IRegion region = new Region(offset, "payload".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("payload", info.getName());
			assertEquals("p", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573223_onLocalVarChain_onArrayField() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "main";
		final int frameNumber = 1;
		final int bpLine = 64;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "names";
			int offset = part.getViewer().getDocument().get().lastIndexOf("local.names") + "local.".length();
			IRegion region = new Region(offset, "names".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("local.names", info.getName());
			assertTrue("Not an array variable", info.getValue() instanceof IJavaArray);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573223_onLocalVarChain_onArrayLength() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "main";
		final int frameNumber = 1;
		final int bpLine = 64;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().lastIndexOf("local.names.length") + "local.names.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, bpLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("local.names.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug574969_onChainHover_preserveEditorSelection() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug572629";
		final String expectedMethod = "main";
		final int frameNumber = 1;
		final int bpLine = 64;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			sync(() -> part.selectAndReveal(part.getViewer().getDocument().get().lastIndexOf("local.names"), "local.names".length()));

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			int offset = part.getViewer().getDocument().get().lastIndexOf("local.names.length") + "local.names.".length();
			IRegion region = new Region(offset, "length".length());
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("local.names.length", info.getName());
			assertEquals("1", info.getValue().getValueString());

			ITextSelection selection = sync(() -> {
				processUiEvents(100);
				return (ITextSelection) part.getSelectionProvider().getSelection();
			});
			assertEquals(selection.getText(), "local.names");
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeLocalVariableChain() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 34;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payload";
			int offset = part.getViewer().getDocument().get().indexOf("System.out.println(object.payload")
					+ "System.out.println(object.".length();
			IRegion region = new Region(offset, "payload".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("object.payload", info.getName());
			assertEquals("p", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeMemberVariable() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 38;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payloads";
			int offset = part.getViewer().getDocument().get().lastIndexOf("System.out.println(payloads") + "System.out.println(".length();
			IRegion region = new Region(offset, "payloads".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("payloads", info.getName());
			assertTrue("Not an array variable", info.getValue() instanceof IJavaArray);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeMemberVariable_withThis() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "payloads";
			int offset = part.getViewer().getDocument().get().lastIndexOf("System.out.println(this.payloads") + "System.out.println(this.".length();
			IRegion region = new Region(offset, "payloads".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("payloads", info.getName());
			assertTrue("Not an array variable", info.getValue() instanceof IJavaArray);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeStaticVariable() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 22;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "PAYLOADS";
			int offset = part.getViewer().getDocument().get().lastIndexOf("private static String[] PAYLOADS") + "private static String[] ".length();
			IRegion region = new Region(offset, "payloads".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("PAYLOADS", info.getName());
			assertTrue("Not an array variable", info.getValue() instanceof IJavaArray);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeMemberVariable_onLength() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 38;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().lastIndexOf("System.out.println(payloads.length")
					+ "System.out.println(payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeMemberVariable_withThis_onLength() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 37;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().lastIndexOf("System.out.println(this.payloads.length")
					+ "System.out.println(this.payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("this.payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeLocalVariable_onLength() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 36;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "length";
			int offset = part.getViewer().getDocument().get().lastIndexOf("System.out.println(object.payloads.length")
					+ "System.out.println(object.payloads.".length();
			IRegion region = new Region(offset, "length".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("object.payloads.length", info.getName());
			assertEquals("1", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_insideLambda_onOuterScopeVariable_whileOnPreviousFrame() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "lambda$0";
		final int frameNumber = 6;
		final int bpLine = 41;
		final int hoverLine = 36;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);

			selectFrame(part, thread.getStackFrames()[4]);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "object";
			int offset = part.getViewer().getDocument().get().lastIndexOf("/*Root*/System.out.println(object")
					+ "/*Root*/System.out.println(".length();
			IRegion region = new Region(offset, "object".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("object", info.getName());
			assertEquals("Bug573547", info.getValue().getReferenceTypeName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_inNestedMethodInvocation_useCorrectFrameForSelectedVariable() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "nestedHover";
		final int frameNumber = 3;
		final int bpLine = 48;
		final int hoverLine = 48;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);
			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "object";
			int offset = part.getViewer().getDocument().get().lastIndexOf("/*Nested1*/System.out.println(object")
					+ "/*Nested1*/System.out.println(".length();
			IRegion region = new Region(offset, "object".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(info);
			assertEquals("object", info.getName());
			assertEquals("1234", info.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_onVariableOutOfExecutionStack_expectNoHoverInfo() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "nestedHover";
		final int frameNumber = 3;
		final int bpLine = 48;
		final int hoverLine = 60;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);
			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String variableName = "object";
			int offset = part.getViewer().getDocument().get().lastIndexOf("/*Nested2*/System.out.println(object")
					+ "/*Nested2*/System.out.println(".length();
			IRegion region = new Region(offset, "object".length());
			String text = selectAndReveal(part, hoverLine, region);
			assertEquals(variableName, text);
			IVariable info = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNull(info);
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	public void testBug573547_inNestedMethodInvocation_inNestedClasses_useCorrectFrameForSelectedVariables() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		final String typeName = "Bug573547";
		final String expectedMethod = "nestedHover";
		final int frameNumber = 4;
		final int bpLine = 60;
		final int hoverLineVar = 60;
		final int hoverLineField = 56;

		IJavaBreakpoint bp = createLineBreakpoint(bpLine, "", typeName + ".java", typeName);
		bp.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod, frameNumber, file, thread);
			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			// local variable
			String variableNameVar = "object";
			int offsetVar = part.getViewer().getDocument().get().lastIndexOf("/*Nested2*/System.out.println(object")
					+ "/*Nested1*/System.out.println(".length();
			IRegion regionVar = new Region(offsetVar, "object".length());
			String textVar = selectAndReveal(part, hoverLineVar, regionVar);
			assertEquals(variableNameVar, textVar);
			IVariable infoVar = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), regionVar));

			assertNotNull(infoVar);
			assertEquals("object", infoVar.getName());
			assertEquals("1234n", infoVar.getValue().getValueString());

			// field
			String variableNameField = "payload";
			int offsetField = part.getViewer().getDocument().get().lastIndexOf("/*Nested2*/private String payload")
					+ "/*Nested2*/private String ".length();
			IRegion regionField = new Region(offsetField, "payload".length());
			String textField = selectAndReveal(part, hoverLineField, regionField);
			assertEquals(variableNameField, textField);
			IVariable infoField = (IVariable) sync(() -> hover.getHoverInfo2(part.getViewer(), regionField));

			assertNotNull(infoField);
			assertEquals("payload", infoField.getName());
			assertEquals("np", infoField.getValue().getValueString());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

	private CompilationUnitEditor openEditorAndValidateStack(final String expectedMethod, final int expectedFramesNumber, IFile file, IJavaThread thread) throws Exception, DebugException {
		// Let now all pending jobs proceed, ignore console jobs
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));
		CompilationUnitEditor part = (CompilationUnitEditor) sync(() -> openEditor(file));
		processUiEvents(100);

		// Prepare breakpoint and check everything below UI is OK
		IBreakpoint hit = getBreakpoint(thread);
		assertNotNull("Suspended, but not by breakpoint", hit);

		IJavaStackFrame topFrame = (IJavaStackFrame) thread.getTopStackFrame();
		assertNotNull("There should be a stackframe", topFrame);
		assertEquals(expectedMethod, topFrame.getMethodName());

		IStackFrame[] frames = topFrame.getThread().getStackFrames();
		assertEquals(expectedFramesNumber, frames.length);
		return part;
	}

	private void validateLine(final int line, int valueIndex, CompilationUnitEditor part, JavaDebugHover hover, Entry<String, Region> varData) throws Exception, DebugException {
		String debugVarName = varData.getKey();
		String variableName = debugVarName.startsWith(VAL_PREFIX) ? debugVarName.substring(VAL_PREFIX.length()) : debugVarName;
		IRegion region = varData.getValue();
		String text = selectAndReveal(part, line, region);
		assertEquals(variableName, text);
		Object args = sync(() -> hover.getHoverInfo2(part.getViewer(), region));

		assertNotNull(args);
		JDIModificationVariable var = (JDIModificationVariable) args;
		assertEquals(debugVarName, var.getName());
		JDIValue value = (JDIValue) var.getValue();
		assertEquals(JDIObjectValue.class, value.getClass());
		JDIObjectValue valueObj = (JDIObjectValue) var.getValue();
		StringReferenceImpl object = (StringReferenceImpl) valueObj.getUnderlyingObject();
		assertEquals("v" + valueIndex, object.value());
	}

	private void validateLine(final int line, CompilationUnitEditor part, JavaDebugHover hover, Entry<String, Region> varData, String varValue) throws Exception, DebugException {
		String debugVarName = varData.getKey();
		String variableName = debugVarName;
		IRegion region = varData.getValue();
		String text = selectAndReveal(part, line, region);
		assertEquals(variableName, text);
		Object args = sync(() -> hover.getHoverInfo2(part.getViewer(), region));

		assertNotNull(args);
		JDIModificationVariable var = (JDIModificationVariable) args;
		assertEquals(debugVarName, var.getName());
		JDIValue value = (JDIValue) var.getValue();
		assertEquals(JDIObjectValue.class, value.getClass());
		JDIObjectValue valueObj = (JDIObjectValue) var.getValue();
		StringReferenceImpl object = (StringReferenceImpl) valueObj.getUnderlyingObject();
		assertEquals(varValue, object.value());
	}

	String selectAndReveal(CompilationUnitEditor editor, int line, IRegion region) throws Exception {
		ITextSelection selection = sync(() -> {
			getActivePage().activate(editor);
			editor.selectAndReveal(region.getOffset(), region.getLength());
			processUiEvents(100);
			return (ITextSelection) editor.getSelectionProvider().getSelection();
		});
		int actualLine = sync(() -> selection.getStartLine() + 1);
		assertEquals(line, actualLine);
		return sync(() -> selection.getText());
	}

	private void selectFrame(CompilationUnitEditor editor, IStackFrame frame) throws Exception {
		LaunchView debugView = sync(() -> (LaunchView) getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW));
		assertNotNull("expected Debug View to be open", debugView);

		TreeSelection selection = sync(() -> (TreeSelection) debugView.getViewer().getSelection());
		TreePath path = selection.getPaths()[0];
		TreePath newPath = path.getParentPath().createChildPath(frame);
		TreeSelection newSelection = new TreeSelection(newPath);

		// frames uses 1 based line number, subtract 1 to line up with editor line numbers
		int targetLineNumber = frame.getLineNumber() - 1;
		int initialLineNumber = sync(() -> ((ITextSelection) editor.getSelectionProvider().getSelection()).getStartLine());
		assertNotEquals("selectFrame cannot detect when it has"
				+ "completed because selecting frame doesn't change the line number.", initialLineNumber, targetLineNumber);
		final int timeoutms = 10000;
		int selectedLineNumer = sync(() -> {
			int lineNumber;
			long timeoutNanos = System.nanoTime() + timeoutms * 1_000_000L;
			debugView.getViewer().setSelection(newSelection, true);
			do {
				TestUtil.runEventLoop();
				lineNumber = ((ITextSelection) editor.getSelectionProvider().getSelection()).getStartLine();
			} while (lineNumber != targetLineNumber && System.nanoTime() < timeoutNanos);
			return lineNumber;
		});
		assertEquals("After waiting " + timeoutms
				+ "ms the editor selection was not moved to the expected line", targetLineNumber, selectedLineNumer);
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return true;
	}

	public void testResolveIn2Lambdas() throws Exception {
		sync(() -> TestUtil.waitForJobs(getName(), 1000, 10000, ProcessConsole.class));

		final String typeName = "DebugHoverTest2Lambdas";
		final String expectedMethod1 = "lambda$0";
		final int framesNumber1 = 13;
		final int bpLine1 = 31;

		IJavaBreakpoint bp1 = createLineBreakpoint(bpLine1, "", typeName + ".java", typeName);
		bp1.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
		IFile file = (IFile) bp1.getMarker().getResource();
		assertEquals(typeName + ".java", file.getName());

		IJavaThread thread = null;
		try {
			thread = launchToBreakpoint(typeName);
			CompilationUnitEditor part = openEditorAndValidateStack(expectedMethod1, framesNumber1, file, thread);

			JavaDebugHover hover = new JavaDebugHover();
			hover.setEditor(part);

			String debugVarName = VAL_PREFIX + "pattern";
			String variableName = "pattern";
			IRegion region = new Region(1054, "pattern".length());
			String text = selectAndReveal(part, bpLine1, region);
			assertEquals(variableName, text);
			Object args = sync(() -> hover.getHoverInfo2(part.getViewer(), region));

			assertNotNull(args);
			JDIModificationVariable var = (JDIModificationVariable) args;
			assertEquals(debugVarName, var.getName());
		} finally {
			terminateAndRemove(thread);
			removeAllBreakpoints();
		}
	}

}
