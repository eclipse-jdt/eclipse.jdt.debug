/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.ui.propertypages.PropertyPageMessages;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Detail pane factory for Java variable.
 *
 * @since 3.10
 */
public class JavaVariablesDetailPaneFactory implements IDetailPaneFactory {

	/**
	 * Maps pane IDs to names
	 */
	private Map<String, String> fNameMap;

	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		HashSet<String> set = new HashSet<>();
		if (selection.size() == 1) {
			IJavaVariable b = (IJavaVariable) selection.getFirstElement();
			if (b != null) {
				set.add(JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES);
			}
		}
		return set;
	}

	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		if (selection.size() == 1) {
			IJavaVariable b = (IJavaVariable) selection.getFirstElement();
			if (b != null) {
				return JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES;
			}
		}
		return null;
	}

	@Override
	public IDetailPane createDetailPane(String paneID) {
		if (JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES.equals(paneID)) {
			return new JavaVariablesDetailPane();
		}
		return null;
	}

	@Override
	public String getDetailPaneName(String paneID) {
		return getNameMap().get(paneID);
	}

	@Override
	public String getDetailPaneDescription(String paneID) {
		return getNameMap().get(paneID);
	}

	private Map<String, String> getNameMap() {
		if (fNameMap == null) {
			fNameMap = new HashMap<>();
			fNameMap.put(JavaVariablesDetailPane.JAVA_VARIABLE_DETAIL_PANE_VARIABLES, PropertyPageMessages.JavaVariableDetailsPane_settings);
		}
		return fNameMap;
	}

}
