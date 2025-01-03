/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class CompareElementsEditor implements IEditorInput {

	public final List<Map<String, Object>> resultsList;
	public final List<String> objectsName;

	public final String elementsType;

	public final List<String> fieldNames;

	public CompareElementsEditor(List<Map<String, Object>> resultsList, List<String> objectsName, String elementsType) {
		this.objectsName = objectsName;
		this.resultsList = resultsList;
		this.elementsType = elementsType;
		fieldNames = null;
	}

	public CompareElementsEditor(List<Map<String, Object>> resultsList, List<String> objectsName, String elementsType, List<String> fieldNames) {
		this.objectsName = objectsName;
		this.resultsList = resultsList;
		this.elementsType = elementsType;
		this.fieldNames = fieldNames;
	}
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return DebugUIMessages.ObjectComparisonTitle_2;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

}
