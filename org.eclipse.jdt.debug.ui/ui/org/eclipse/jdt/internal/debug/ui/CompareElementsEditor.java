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

	public final List<Map<String, Object>> results;
	public final List<String> objectNames;

	public final String elementTypeName;

	public final List<String> fieldNames;

	public CompareElementsEditor(List<Map<String, Object>> results, List<String> objectsName, String elementTypeNames) {
		this.objectNames = objectsName;
		this.results = results;
		this.elementTypeName = elementTypeNames;
		fieldNames = null;
	}

	public CompareElementsEditor(List<Map<String, Object>> results, List<String> objectsName, String elementTypeName, List<String> fieldNames) {
		this.objectNames = objectsName;
		this.results = results;
		this.elementTypeName = elementTypeName;
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
		return DebugUIMessages.ObjectComparisonTitle_short;
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
