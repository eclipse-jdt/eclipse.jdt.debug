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
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Pins the selected fields to the top of the active variables view, or unpins them if they are all pinned already.
 * Pins are specific to the view.
 *
 * @see PinnedFieldsManager
 */
public class PinFieldHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) {
		List<IJavaFieldVariable> fields = getFields(HandlerUtil.getCurrentSelection(event));
		String viewId = HandlerUtil.getActivePartId(event);
		if (!fields.isEmpty() && viewId != null) {
			PinnedFieldsManager manager = PinnedFieldsManager.getDefault();
			manager.setPinned(viewId, fields, !areAllPinned(manager, viewId, fields));
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		IEvaluationService service = PlatformUI.getWorkbench().getService(IEvaluationService.class);
		if (service == null) {
			return;
		}
		IEvaluationContext context = service.getCurrentState();
		List<IJavaFieldVariable> fields = getFields(context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME));
		String viewId = context.getVariable(ISources.ACTIVE_PART_ID_NAME) instanceof String id ? id : null;
		element.setChecked(!fields.isEmpty() && areAllPinned(PinnedFieldsManager.getDefault(), viewId, fields));
	}

	private static List<IJavaFieldVariable> getFields(Object selection) {
		List<IJavaFieldVariable> fields = new ArrayList<>();
		if (selection instanceof IStructuredSelection structured) {
			for (Object element : structured) {
				if (!(element instanceof IJavaFieldVariable field)) {
					return List.of();
				}
				fields.add(field);
			}
		}
		return fields;
	}

	private static boolean areAllPinned(PinnedFieldsManager manager, String viewId, List<IJavaFieldVariable> fields) {
		return fields.stream().allMatch(field -> manager.isPinned(viewId, field));
	}
}
