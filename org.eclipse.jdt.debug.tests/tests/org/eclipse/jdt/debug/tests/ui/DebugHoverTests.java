/*******************************************************************************
 *  Copyright (c) 2017 Andrey Loskutov and others.
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
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.jdi.internal.StringReferenceImpl;
import org.eclipse.jdt.core.IJavaProject;
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
