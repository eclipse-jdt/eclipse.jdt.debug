/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.debug.testplugin.detailpane;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.tests.ui.DetailPaneManagerTests;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Test detail pane factory, used by the test suite to test custom detail pane functionality.
 *
 * @since 3.3
 * @see DetailPaneManagerTests
 * @see SimpleDetailPane
 * @see TableDetailPane
 * @see IDetailPaneFactory
 */
public class TestDetailPaneFactory implements IDetailPaneFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
	 */
	@Override
	public IDetailPane createDetailPane(String id) {
		if (id.equals(TableDetailPane.ID)){
			return new TableDetailPane();
		}
		if (id.equals(SimpleDetailPane.ID)){
			return new SimpleDetailPane();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDetailsTypes(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<>(2);
		if (selection != null){
			if (selection.size() == 1 && selection.getFirstElement() instanceof IJavaVariable){
				possibleIDs.add(SimpleDetailPane.ID);
			}
			if (selection.size() > 1 && selection.getFirstElement() instanceof String){
				possibleIDs.add(TableDetailPane.ID);
			}
		}
		return possibleIDs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (selection != null){
			if (selection.size() > 1 && selection.getFirstElement() instanceof String){
				if (((String)selection.getFirstElement()).equals("test pane is default")){
					return TableDetailPane.ID;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
	 */
	@Override
	public String getDetailPaneName(String id) {
		if (id.equals(TableDetailPane.ID)){
			return TableDetailPane.NAME;
		}
		if (id.equals(SimpleDetailPane.ID)){
			return SimpleDetailPane.NAME;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
	 */
	@Override
	public String getDetailPaneDescription(String id) {
		if (id.equals(TableDetailPane.ID)){
			return TableDetailPane.DESCRIPTION;
		}
		if (id.equals(SimpleDetailPane.ID)){
			return SimpleDetailPane.DESCRIPTION;
		}
		return null;
	}
}
