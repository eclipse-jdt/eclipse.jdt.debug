/*******************************************************************************
 *  Copyright (c) 2006, 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.internal.ui.views.variables.details.DefaultDetailPane;
import org.eclipse.debug.internal.ui.views.variables.details.DetailPaneManager;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.testplugin.detailpane.SimpleDetailPane;
import org.eclipse.jdt.debug.testplugin.detailpane.TableDetailPane;
import org.eclipse.jdt.debug.tests.AbstractDebugTest;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugElement;
import org.eclipse.jdt.internal.debug.ui.variables.JavaVariablesDetailPane;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.test.OrderedTestSuite;

import junit.framework.Test;

/**
 * Tests the detail pane functionality by testing <code>DetailPaneManager</code>.
 * The manager is responsible for the default detail pane contributed by the
 * platform and detail panes contributed at the org.eclipse.debug.ui.detailPaneFactories.
 * The manager must keep track of which is the preferred detail pane to display and
 * know what detail panes can be displayed for certain types of selections.
 */
public class DetailPaneManagerTests extends AbstractDebugTest {

	DetailPaneManager fManager;

	/**
	 * Initializes the test class and gets the singleton detail pane manager.
	 * @param name
	 */
	public DetailPaneManagerTests(String name) {
		super(name);
		fManager = DetailPaneManager.getDefault();
		// Make sure that the default pane has been loaded.
		fManager.getPreferredPaneFromSelection(null);
	}

	public static Test suite() {
		return new OrderedTestSuite(DetailPaneManagerTests.class, new String[] { "testGetUserPreferredDetailPane", "testSetPreferredDetailPane",
				"testGetPreferredPaneFromSelection", "testGetAvailablePaneIDs", "testGetDetailPaneFromID", "testGetNameFromID",
				"testGetDescriptionFromID"
		});
	}

	/**
	 * Tests that the manager makes correct default selection when
	 * one or more detail panes are available.
	 */
	public void testGetUserPreferredDetailPane() {
		String id = fManager.getUserPreferredDetailPane(new HashSet<String>());
		assertEquals("Incorrect pane ID", null, id);

		Set<String> detailPanes = new HashSet<>();
		detailPanes.add("NewPane1");
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", null, id);

		detailPanes.add("NewPane2");
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", null, id);

		detailPanes.add(DefaultDetailPane.ID);
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", null, id);

		detailPanes.clear();
		detailPanes.add(DefaultDetailPane.ID);
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);
	}

	/**
	 * Tests that the manager can remember which detail pane is
	 * currently the preferred pane.
	 */
	public void testSetPreferredDetailPane() {
		fManager.setPreferredDetailPane(null, null);
		String id = fManager.getUserPreferredDetailPane(null);
		assertEquals("Incorrect pane ID", null, id);

		fManager.setPreferredDetailPane(null, "Example");
		id = fManager.getUserPreferredDetailPane(null);
		assertEquals("Incorrect pane ID", null, id);

		Set<String> detailPanes = new HashSet<>();
		detailPanes.add("Example1");
		fManager.setPreferredDetailPane(detailPanes, "Example1");
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", "Example1", id);

		detailPanes.add("Example2");
		fManager.setPreferredDetailPane(detailPanes, "Example2");
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", "Example2", id);

		detailPanes.add(DefaultDetailPane.ID);
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", null, id);
		fManager.setPreferredDetailPane(detailPanes, "Example2");
		id = fManager.getUserPreferredDetailPane(detailPanes);
		assertEquals("Incorrect pane ID", "Example2", id);
	}

	/**
	 * Tests that the manager can determine the preferred pane
	 * given a selection.
	 */
	public void testGetPreferredPaneFromSelection() {
		IStructuredSelection selection = null;
		String id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);

		selection = new StructuredSelection();
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);

		selection = new StructuredSelection(new String[]{"example selection"});
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);

		selection = new StructuredSelection(new IJavaVariable[]{new JDIPlaceholderVariable("test var",null)});
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES, id);

		// The factory sets the Table detail pane as the default if the first string is "test pane is default".
		selection = new StructuredSelection(new String[]{"test pane is default","example selection"});
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", TableDetailPane.ID, id);

		selection = new StructuredSelection(new String[]{"test pane is default","example selection"});
		fManager.setPreferredDetailPane(fManager.getAvailablePaneIDs(selection), DefaultDetailPane.ID);
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);

		selection = new StructuredSelection(new String[]{"String1","String2","String3"});
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);

		selection = new StructuredSelection(new JDIDebugElement[]{});
		id = fManager.getPreferredPaneFromSelection(selection);
		assertEquals("Incorrect pane ID", DefaultDetailPane.ID, id);
	}

	/**
	 * Tests that the manager returns all possible panes that can be used
	 * for a given selection.
	 */
	public void testGetAvailablePaneIDs() {
		IStructuredSelection selection = null;
		Set<String> result = fManager.getAvailablePaneIDs(selection);
		assertTrue("Set was incorrect",result.size()==1 && result.contains(DefaultDetailPane.ID));

		selection = new StructuredSelection(new String[]{"example selection"});
		result = fManager.getAvailablePaneIDs(selection);
		assertTrue("Set was incorrect",result.size()==1 && result.contains(DefaultDetailPane.ID));

		selection = new StructuredSelection(new String[]{"test pane is default","example selection"});
		result = fManager.getAvailablePaneIDs(selection);
		assertTrue("Set was incorrect",result.size()==2 && result.contains(DefaultDetailPane.ID) && result.contains(TableDetailPane.ID));

		selection = new StructuredSelection(new Object[]{new JDIPlaceholderVariable("test var",null)});
		result = fManager.getAvailablePaneIDs(selection);
		assertTrue("Set was incorrect", result.size() == 3 && result.contains(DefaultDetailPane.ID) && result.contains(SimpleDetailPane.ID)
				&& result.contains(JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES));

		// Simple detail pane only available if selection has length of 1, containing a java variable
		selection = new StructuredSelection(new Object[]{"String1",new JDIPlaceholderVariable("test var",null),"String3"});
		result = fManager.getAvailablePaneIDs(selection);
		assertTrue("Set was incorrect",result.size()==2 && result.contains(DefaultDetailPane.ID) && result.contains(TableDetailPane.ID));
	}

	/**
	 * Checks that the manager can query the correct factory to produce
	 * a detail pane with the given ID.
	 */
	public void testGetDetailPaneFromID() {
		IDetailPane pane = fManager.getDetailPaneFromID(null);
		assertNull("Incorrect pane returned",pane);

		pane = fManager.getDetailPaneFromID("ThisPaneDoesNotExist");
		assertNull("Incorrect pane returned",pane);

		pane = fManager.getDetailPaneFromID(DefaultDetailPane.ID);
		assertNotNull("Incorrect pane returned",pane);

		pane = fManager.getDetailPaneFromID(SimpleDetailPane.ID);
		assertNotNull("Incorrect pane returned",pane);
	}

	/**
	 * Checks that the manager can query the correct factory to produce
	 * the name of the detail pane with the given ID.
	 */
	public void testGetNameFromID() {
		String name = fManager.getNameFromID(null);
		assertEquals("Incorrect name returned",null,name);

		name = fManager.getNameFromID("ThisPaneDoesNotExist");
		assertEquals("Incorrect name returned",null,name);

		name = fManager.getNameFromID(DefaultDetailPane.ID);
		assertEquals("Incorrect name returned",DefaultDetailPane.NAME,name);

		name = fManager.getNameFromID(SimpleDetailPane.ID);
		assertEquals("Incorrect name returned","Example Pane: Colorful Detail Pane",name);

	}

	/**
	 * Checks that the manager can query the correct factory to produce
	 * the description of the detail pane with the given ID.
	 */
	public void testGetDescriptionFromID() {
		String description = fManager.getDescriptionFromID(null);
		assertEquals("Incorrect name returned",null,description);

		description = fManager.getDescriptionFromID("ThisPaneDoesNotExist");
		assertEquals("Incorrect name returned",null,description);

		description = fManager.getDescriptionFromID(DefaultDetailPane.ID);
		assertEquals("Incorrect name returned",DefaultDetailPane.DESCRIPTION,description);

		description = fManager.getDescriptionFromID(SimpleDetailPane.ID);
		assertEquals("Incorrect name returned","Example pane that displays a color for variables depending on their access level.",description);
	}

}
