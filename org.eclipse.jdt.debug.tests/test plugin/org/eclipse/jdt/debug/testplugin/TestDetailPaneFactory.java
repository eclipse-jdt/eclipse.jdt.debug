/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This is a detail pane factory that produces <code>TestDetailPane</code>'s.
 * Included in the test plugin by being contributed to the 
 * org.eclipse.debug.ui.detailPaneFactories extension point.
 * 
 * @see org.eclipse.jdt.debug.tests.ui.DetailPaneManagerTests.java
 */
public class TestDetailPaneFactory implements IDetailPaneFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
	 */
	public IDetailPane createDetailPane(String id) {
		return new TestDetailPane();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDetailsTypes(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public Set getDetailPaneTypes(IStructuredSelection selection) {
		Set possibleIDs = new HashSet(1);
		if (selection != null){
			possibleIDs.add(TestDetailPane.ID);
		}
		return possibleIDs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof String){
			if (((String)selection.getFirstElement()).equals("test pane is default")){
				return TestDetailPane.ID;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
	 */
	public String getDetailPaneName(String id) {
		if (id.equals(TestDetailPane.ID)){
			return "Test Pane";
		}
		else{
			return null;
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
	 */
	public String getDetailPaneDescription(String id) {
		if (id.equals(TestDetailPane.ID)){
			return "Test Pane Description";
		}
		else{
			return null;
		}
		
	}



}
