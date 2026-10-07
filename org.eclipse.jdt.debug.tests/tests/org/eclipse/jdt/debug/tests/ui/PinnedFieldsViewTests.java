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
package org.eclipse.jdt.debug.tests.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.testplugin.JavaTestPlugin;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.variables.JavaContentProviderFilter;
import org.eclipse.jdt.internal.debug.ui.variables.PinnedFieldsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;

/**
 * Tests pinning fields to the top of the Variables and Expressions views through their context menu.
 */
public class PinnedFieldsViewTests extends AbstractDebugUiTests {

	private static final String TYPE_NAME = "InstanceVariablesTests";
	private static final String PIN_TO_TOP = "Pin to Top";
	private static final String MOVE_PIN_UP = "Move Pin Up";
	private static final String MOVE_PIN_DOWN = "Move Pin Down";
	private static final String UNPIN_ALL = "Unpin All Fields";
	private static final long TIMEOUT = 30_000;
	private static final String TESTS_BUNDLE = JavaTestPlugin.getDefault().getBundle().getSymbolicName();

	/**
	 * Words identifying the errors caused by the "Pin to Top" contributions
	 */
	private static final List<String> PIN_ERROR_MARKERS = List.of("isPinned", "selectionIsPinnedField", "selectionIsJavaFields",
			"PinnedField", "PinField", "MovePin", "UnpinAll");

	private IJavaThread fThread;
	/**
	 * The suspended frame and its <code>this</code>, captured on suspend: the top frame of the thread is not available
	 * while the views evaluate the details of the values
	 */
	private IJavaStackFrame fFrame;
	private IJavaObject fThis;
	private String fViewId;
	private IViewPart fPart;
	private final List<IStatus> fLoggedErrors = new CopyOnWriteArrayList<>();
	/**
	 * Collects the logged errors, except those of the tests themselves: they are prefixed with the name of the running test,
	 * which contains some of the {@link #PIN_ERROR_MARKERS}
	 */
	private final ILogListener fLogListener = (status, _) -> {
		if (status.matches(IStatus.ERROR) && !TESTS_BUNDLE.equals(status.getPlugin())) {
			fLoggedErrors.add(status);
		}
	};

	public PinnedFieldsViewTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Platform.addLogListener(fLogListener);
		unpinAll();
		resetDebugPerspective();
		processUiEvents(100);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			unpinAll();
			terminateAndRemove(fThread);
			removeAllBreakpoints();
			processUiEvents(100);
			Platform.removeLogListener(fLogListener);
			assertNoPinError();
		} finally {
			super.tearDown();
		}
	}

	private static void unpinAll() {
		PinnedFieldsManager.getDefault().unpinAll(IDebugUIConstants.ID_VARIABLE_VIEW);
		PinnedFieldsManager.getDefault().unpinAll(IDebugUIConstants.ID_EXPRESSION_VIEW);
	}

	/**
	 * Fails if an error caused by the "Pin to Top" contributions was logged during the test.
	 */
	private void assertNoPinError() {
		for (IStatus status : fLoggedErrors) {
			StringWriter text = new StringWriter();
			text.append(status.getMessage());
			if (status.getException() != null) {
				status.getException().printStackTrace(new PrintWriter(text));
			}
			for (String marker : PIN_ERROR_MARKERS) {
				if (text.toString().contains(marker)) {
					fail("Error logged: " + text);
				}
			}
		}
	}

	@Override
	protected boolean enableUIEventLoopProcessingInWaiter() {
		return true;
	}

	public void testPinToTop() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		List<String> initial = waitForChildren(viewer);
		assertTrue("privStr should not be the first field: " + initial, initial.indexOf("privStr") > 0);
		Image unpinnedImage = getChildImage(viewer, "privStr");

		withContextMenu(viewer, children(viewer, "privStr"), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
			assertTrue("'Pin to Top' should be enabled", pin.isEnabled());
			assertFalse("'Pin to Top' should not be checked", pin.getSelection());
			assertNull("'Move Pin Up' should be hidden for an unpinned field", findItem(menu, MOVE_PIN_UP));
			click(pin);
		});

		waitForFirstChildren(viewer, "privStr");
		assertNotSame("Pinned field should be decorated", unpinnedImage, getChildImage(viewer, "privStr"));

		withContextMenu(viewer, children(viewer, "privStr"), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
			assertTrue("'Pin to Top' should be checked for a pinned field", pin.getSelection());
			MenuItem moveUp = findItem(menu, MOVE_PIN_UP);
			assertNotNull("'Move Pin Up' should be shown for a pinned field", moveUp);
			assertFalse("'Move Pin Up' should be disabled for the only pinned field", moveUp.isEnabled());
			click(pin);
		});

		waitFor("Unpinned field should be back to its place: " + initial, () -> initial.equals(getChildNames(viewer)), () -> getChildNames(viewer));
		assertFalse(PinnedFieldsManager.getDefault().hasPinnedFields(fViewId));
	}

	public void testMovePin() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		waitForChildren(viewer);

		pinFromContextMenu(viewer, "privStr");
		pinFromContextMenu(viewer, "date");
		waitForFirstChildren(viewer, "privStr", "date");

		withContextMenu(viewer, children(viewer, "date"), menu -> {
			assertFalse("'Move Pin Down' should be disabled for the last pinned field", findItem(menu, MOVE_PIN_DOWN).isEnabled());
			MenuItem moveUp = findItem(menu, MOVE_PIN_UP);
			assertTrue("'Move Pin Up' should be enabled for the last pinned field", moveUp.isEnabled());
			click(moveUp);
		});
		waitForFirstChildren(viewer, "date", "privStr");

		withContextMenu(viewer, children(viewer, "date"), menu -> click(findItem(menu, MOVE_PIN_DOWN)));
		waitForFirstChildren(viewer, "privStr", "date");
	}

	public void testUnpinAll() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		List<String> initial = waitForChildren(viewer);
		pinFromContextMenu(viewer, "date");
		waitForFirstChildren(viewer, "date");

		withContextMenu(viewer, children(viewer, "pubStr"), menu -> {
			MenuItem unpinAll = findItem(menu, UNPIN_ALL);
			assertNotNull("Missing 'Unpin All Fields' in " + itemTexts(menu), unpinAll);
			assertTrue("'Unpin All Fields' should be enabled", unpinAll.isEnabled());
			click(unpinAll);
		});

		waitFor("Fields should be back to their place: " + initial, () -> initial.equals(getChildNames(viewer)), () -> getChildNames(viewer));
		withContextMenu(viewer, children(viewer, "pubStr"), menu -> assertFalse("'Unpin All Fields' should be disabled", findItem(menu, UNPIN_ALL).isEnabled()));
	}

	public void testMultipleSelection() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		waitForChildren(viewer);
		pinFromContextMenu(viewer, "date");
		waitForFirstChildren(viewer, "date");

		// mixed selection: the command pins all the selected fields
		withContextMenu(viewer, children(viewer, "date", "privStr"), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
			assertFalse("'Pin to Top' should not be checked for a mixed selection", pin.getSelection());
			assertNull("'Move Pin Up' should be hidden for a multiple selection", findItem(menu, MOVE_PIN_UP));
			click(pin);
		});
		waitForFirstChildren(viewer, "date", "privStr");

		// all selected fields pinned: the command unpins them
		withContextMenu(viewer, children(viewer, "date", "privStr"), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertTrue("'Pin to Top' should be checked when all the selected fields are pinned", pin.getSelection());
			click(pin);
		});
		waitFor("Fields should be unpinned", () -> !PinnedFieldsManager.getDefault().hasPinnedFields(fViewId));
	}

	public void testLocalVariableCannotBePinned() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		withContextMenu(viewer, root(viewer, "ivt"), menu -> {
			assertNull("'Pin to Top' should be hidden for a local variable", findItem(menu, PIN_TO_TOP));
			assertNull("'Unpin All Fields' should be hidden for a local variable", findItem(menu, UNPIN_ALL));
		});
	}

	public void testNoErrorForOtherSelections() throws Exception {
		TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
		waitForChildren(viewer);
		IViewPart debugView = openView(IDebugUIConstants.ID_DEBUG_VIEW);
		sync(() -> {
			getActivePage().activate(debugView);
			TreePath path = new TreePath(new Object[] { fThread.getLaunch(), fThread.getDebugTarget(), fThread, fFrame });
			debugView.getAdapter(IDebugView.class).getViewer().setSelection(new TreeSelection(path), true);
			return null;
		});
		processUiEvents(500);
		// the handlers of the "Pin to Top" commands are evaluated against the stack frame selection: nothing must be logged
		assertNoPinError();
	}

	public void testMovePinOfStaticFieldsInStaticFrame() throws Exception {
		// in a static method, the static fields of the declaring type are root elements of the Variables view
		IPreferenceStore store = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		String showStatics = IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES;
		String showConstants = IDebugUIConstants.ID_VARIABLE_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_CONSTANTS;
		boolean showedStatics = store.getBoolean(showStatics);
		boolean showedConstants = store.getBoolean(showConstants);
		store.setValue(showStatics, true);
		store.setValue(showConstants, true);
		try {
			ILineBreakpoint bp = createLineBreakpoint(54, "PinnedFieldsTarget");
			fThread = launchToLineBreakpoint("PinnedFieldsTarget", bp);
			assertNotNull("Launch unsuccessful", fThread);
			fPart = openView(IDebugUIConstants.ID_VARIABLE_VIEW);
			TreeViewer viewer = (TreeViewer) fPart.getAdapter(IDebugView.class).getViewer();
			sync(() -> getActivePage().activate(fPart));
			waitFor("Static fields not shown", () -> getRootNames(viewer).containsAll(List.of("staticCounter", "CONSTANT")),
					() -> getRootNames(viewer));

			pinRootFromContextMenu(viewer, "staticCounter");
			pinRootFromContextMenu(viewer, "CONSTANT");
			waitFor("Pinned static fields should be first", () -> startsWith(getRootNames(viewer), "staticCounter", "CONSTANT"),
					() -> getRootNames(viewer));

			withContextMenu(viewer, root(viewer, "CONSTANT"), menu -> {
				MenuItem moveUp = findItem(menu, MOVE_PIN_UP);
				assertNotNull("Missing 'Move Pin Up' in " + itemTexts(menu), moveUp);
				assertTrue("'Move Pin Up' should be enabled for the last pinned root field", moveUp.isEnabled());
				click(moveUp);
			});
			waitFor("Pinned static field should have moved up", () -> startsWith(getRootNames(viewer), "CONSTANT", "staticCounter"),
					() -> getRootNames(viewer));
		} finally {
			store.setValue(showStatics, showedStatics);
			store.setValue(showConstants, showedConstants);
		}
	}

	public void testPinInExpressionsView() throws Exception {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpression expression = expressionManager.newWatchExpression("this");
		expressionManager.addExpression(expression);
		try {
			TreeViewer viewer = launchAndShowThis(IDebugUIConstants.ID_EXPRESSION_VIEW);
			List<String> initial = waitForChildren(viewer);
			assertTrue("privStr should not be the first field: " + initial, initial.indexOf("privStr") > 0);

			pinFromContextMenu(viewer, "privStr");

			waitForFirstChildren(viewer, "privStr");
		} finally {
			expressionManager.removeExpression(expression);
		}
	}

	/**
	 * Pins are view specific: pinning or unpinning fields in the Expressions view must not change the Variables view.
	 */
	public void testPinsAreViewSpecific() throws Exception {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		IWatchExpression expression = expressionManager.newWatchExpression("this");
		expressionManager.addExpression(expression);
		try {
			TreeViewer variables = launchAndShowThis(IDebugUIConstants.ID_VARIABLE_VIEW);
			IViewPart variablesPart = fPart;
			List<String> pinnedInVariables = new ArrayList<>(waitForChildren(variables));
			pinnedInVariables.remove("privStr");
			pinnedInVariables.add(0, "privStr");
			pinFromContextMenu(variables, "privStr");
			waitFor("Pinned field should be first: " + pinnedInVariables, () -> pinnedInVariables.equals(getChildNames(variables)), () -> getChildNames(variables));

			// the field pinned in the Variables view is not pinned in the Expressions view
			TreeViewer expressions = showThis(IDebugUIConstants.ID_EXPRESSION_VIEW);
			List<String> initial = waitForChildren(expressions);
			withContextMenu(expressions, children(expressions, "privStr"), menu -> {
				MenuItem pin = findItem(menu, PIN_TO_TOP);
				assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
				assertFalse("'Pin to Top' should not be checked for a field pinned in another view", pin.getSelection());
				assertNull("'Move Pin Up' should be hidden for a field pinned in another view", findItem(menu, MOVE_PIN_UP));
				assertFalse("'Unpin All Fields' should be disabled when the fields are pinned in another view", findItem(menu, UNPIN_ALL).isEnabled());
			});

			// unpinning a field of the Expressions view does not unpin the fields of the Variables view
			pinFromContextMenu(expressions, "date");
			waitForFirstChildren(expressions, "date");
			pinFromContextMenu(expressions, "date");
			waitFor("Unpinned field should be back to its place: " + initial, () -> initial.equals(getChildNames(expressions)), () -> getChildNames(expressions));
			assertTrue("Field should still be pinned in the Variables view", PinnedFieldsManager.getDefault().hasPinnedFields(IDebugUIConstants.ID_VARIABLE_VIEW));

			// nor does "Unpin All Fields"
			pinFromContextMenu(expressions, "date");
			waitForFirstChildren(expressions, "date");
			withContextMenu(expressions, children(expressions, "pubStr"), menu -> click(findItem(menu, UNPIN_ALL)));
			waitFor("Fields should be back to their place: " + initial, () -> initial.equals(getChildNames(expressions)), () -> getChildNames(expressions));
			assertFalse(PinnedFieldsManager.getDefault().hasPinnedFields(IDebugUIConstants.ID_EXPRESSION_VIEW));

			fPart = variablesPart;
			fViewId = IDebugUIConstants.ID_VARIABLE_VIEW;
			sync(() -> getActivePage().activate(fPart));
			// the Variables view was refreshed by the changes of the Expressions view, and still shows its own pin only
			waitFor("Variables view should still show its own pin only: " + pinnedInVariables, () -> pinnedInVariables.equals(getChildNames(variables)), () -> getChildNames(variables));
			withContextMenu(variables, children(variables, "privStr"), menu -> assertTrue("'Pin to Top' should still be checked in the Variables view", findItem(menu, PIN_TO_TOP).getSelection()));
		} finally {
			expressionManager.removeExpression(expression);
		}
	}

	/**
	 * Launches the test program, suspends it where <code>this</code> is an <code>InstanceVariablesTests</code> and shows
	 * the given view.
	 */
	private TreeViewer launchAndShowThis(String viewId) throws Exception {
		ILineBreakpoint bp = createLineBreakpoint(33, TYPE_NAME);
		fThread = launchToLineBreakpoint(TYPE_NAME, bp);
		assertNotNull("Launch unsuccessful", fThread);
		fFrame = (IJavaStackFrame) fThread.getTopStackFrame();
		assertNotNull("Missing top frame", fFrame);
		fThis = fFrame.getThis();
		assertNotNull("'this' is null", fThis);
		return showThis(viewId);
	}

	/**
	 * Shows the given view, which becomes the view under test, and waits for <code>this</code> to be shown in it.
	 */
	private TreeViewer showThis(String viewId) throws Exception {
		fViewId = viewId;
		fPart = openView(viewId);
		TreeViewer viewer = (TreeViewer) fPart.getAdapter(IDebugView.class).getViewer();
		sync(() -> getActivePage().activate(fPart));
		waitFor("'this' not shown in " + viewId, () -> findRootItem(viewer) != null);
		return viewer;
	}

	private void pinRootFromContextMenu(TreeViewer viewer, String field) throws Exception {
		withContextMenu(viewer, root(viewer, field), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
			click(pin);
		});
	}

	private void pinFromContextMenu(TreeViewer viewer, String field) throws Exception {
		withContextMenu(viewer, children(viewer, field), menu -> {
			MenuItem pin = findItem(menu, PIN_TO_TOP);
			assertNotNull("Missing 'Pin to Top' in " + itemTexts(menu), pin);
			click(pin);
		});
	}

	private interface MenuCallback {
		void run(Menu menu) throws Exception;
	}

	/**
	 * Elements to select in a view, as tree paths computed in the UI thread.
	 */
	private record Selected(String description, Callable<TreePath[]> paths) {
		@Override
		public String toString() {
			return description;
		}
	}

	private Selected children(TreeViewer viewer, String... fields) {
		return new Selected(List.of(fields).toString(), () -> {
			TreeItem root = findRootItem(viewer);
			List<TreePath> paths = new ArrayList<>();
			for (String field : fields) {
				TreeItem item = findChildItem(viewer, field);
				if (root == null || item == null) {
					return null;
				}
				paths.add(new TreePath(new Object[] { root.getData(), item.getData() }));
			}
			return paths.toArray(new TreePath[paths.size()]);
		});
	}

	private Selected root(TreeViewer viewer, String name) {
		return new Selected(name, () -> {
			TreeItem item = findRootItem(viewer, name);
			return item == null ? null : new TreePath[] { new TreePath(new Object[] { item.getData() }) };
		});
	}

	/**
	 * Selects the given elements and shows the context menu of the viewer, as a right click does, then runs the
	 * callback on its items. The selection and the menu are done in the same UI runnable, so that the view cannot
	 * restore its previous selection (as it does asynchronously after a refresh) before the menu is evaluated.
	 */
	private void withContextMenu(TreeViewer viewer, Selected selected, MenuCallback callback) throws Exception {
		waitFor("Cannot open the context menu on " + selected, () -> {
			TreePath[] paths = selected.paths().call();
			if (paths == null) {
				// not shown yet
				return false;
			}
			getActivePage().activate(fPart);
			viewer.setSelection(new TreeSelection(paths), true);
			Set<Object> expected = new HashSet<>();
			for (TreePath path : paths) {
				expected.add(path.getLastSegment());
			}
			if (!expected.equals(new HashSet<>(Arrays.asList(viewer.getStructuredSelection().toArray())))) {
				// the view did not apply the selection yet
				return false;
			}
			Menu menu = viewer.getControl().getMenu();
			assertNotNull("No context menu", menu);
			menu.notifyListeners(SWT.Show, new Event());
			try {
				callback.run(menu);
			} finally {
				menu.notifyListeners(SWT.Hide, new Event());
			}
			return true;
		});
		processUiEvents(100);
	}

	private static void click(MenuItem item) {
		assertNotNull("Missing menu item", item);
		if ((item.getStyle() & SWT.CHECK) != 0) {
			item.setSelection(!item.getSelection());
		}
		item.notifyListeners(SWT.Selection, new Event());
	}

	private static MenuItem findItem(Menu menu, String text) {
		for (MenuItem item : menu.getItems()) {
			if (text.equals(cleanText(item))) {
				return item;
			}
		}
		return null;
	}

	private static List<String> itemTexts(Menu menu) {
		List<String> texts = new ArrayList<>();
		for (MenuItem item : menu.getItems()) {
			texts.add(cleanText(item));
		}
		return texts;
	}

	private static String cleanText(MenuItem item) {
		String text = item.getText().replace("&", "");
		int tab = text.indexOf('\t');
		return tab < 0 ? text : text.substring(0, tab);
	}

	/**
	 * Waits until the view shows all the fields of <code>this</code>, in the order computed from the model while no
	 * field is pinned, and returns that order.
	 */
	private List<String> waitForChildren(TreeViewer viewer) throws Exception {
		assertFalse("No field should be pinned yet", PinnedFieldsManager.getDefault().hasPinnedFields(fViewId));
		List<String> expected = new ArrayList<>();
		for (Object field : JavaContentProviderFilter.filterVariables(fThis.getVariables(), new PresentationContext(fViewId))) {
			expected.add(((IVariable) field).getName());
		}
		assertTrue("Unexpected fields: " + expected, expected.indexOf("privStr") > 0 && expected.contains("date"));
		waitFor("Fields of 'this' not all shown, expected " + expected, () -> {
			// the view may be refreshed while it is being populated, so expand until the fields are shown
			TreeItem root = findRootItem(viewer);
			if (root != null && !root.getExpanded()) {
				viewer.expandToLevel(new TreePath(new Object[] { root.getData() }), 1);
			}
			return expected.equals(getChildNames(viewer));
		}, () -> getChildNames(viewer));
		return expected;
	}

	private void waitForFirstChildren(TreeViewer viewer, String... fields) throws Exception {
		waitFor("Expected first fields " + List.of(fields), () -> startsWith(getChildNames(viewer), fields), () -> getChildNames(viewer));
	}

	private Image getChildImage(TreeViewer viewer, String field) throws Exception {
		return sync(() -> findChildItem(viewer, field).getImage());
	}

	private void waitFor(String message, Callable<Boolean> condition) throws Exception {
		waitFor(message, condition, () -> null);
	}

	/**
	 * Waits for the condition, and fails with the given message and the state described by the given callable.
	 */
	private void waitFor(String message, Callable<Boolean> condition, Callable<Object> state) throws Exception {
		long end = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < end) {
			if (Boolean.TRUE.equals(sync(condition))) {
				return;
			}
			processUiEvents(100);
		}
		Object shown = sync(state);
		fail(shown == null ? message : message + ", shown: " + shown);
	}

	private static TreeItem findRootItem(TreeViewer viewer) throws DebugException {
		return findRootItem(viewer, "this");
	}

	private static TreeItem findRootItem(TreeViewer viewer, String name) throws DebugException {
		for (TreeItem item : viewer.getTree().getItems()) {
			if (item.getData() instanceof IVariable variable && variable.getName().equals(name)) {
				return item;
			}
			if (item.getData() instanceof IWatchExpression expression && expression.getExpressionText().equals(name)) {
				return item;
			}
		}
		return null;
	}

	private static TreeItem findChildItem(TreeViewer viewer, String field) throws DebugException {
		TreeItem root = findRootItem(viewer);
		if (root != null) {
			for (TreeItem item : root.getItems()) {
				if (item.getData() instanceof IVariable variable && variable.getName().equals(field)) {
					return item;
				}
			}
		}
		return null;
	}

	private static boolean startsWith(List<String> names, String... first) {
		return names.size() >= first.length && names.subList(0, first.length).equals(List.of(first));
	}

	private static List<String> getRootNames(TreeViewer viewer) throws DebugException {
		List<String> names = new ArrayList<>();
		for (TreeItem item : viewer.getTree().getItems()) {
			if (item.getData() instanceof IVariable variable) {
				names.add(variable.getName());
			}
		}
		return names;
	}

	private static List<String> getChildNames(TreeViewer viewer) throws DebugException {
		List<String> names = new ArrayList<>();
		TreeItem root = findRootItem(viewer);
		if (root != null) {
			for (TreeItem item : root.getItems()) {
				if (item.getData() instanceof IVariable variable) {
					names.add(variable.getName());
				}
			}
		}
		return names;
	}
}
