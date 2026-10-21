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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Unpins all the fields pinned to the top of the active variables view. Pins are specific to the view.
 *
 * @see PinnedFieldsManager
 */
public class UnpinAllFieldsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		String viewId = HandlerUtil.getActivePartId(event);
		if (viewId != null) {
			PinnedFieldsManager.getDefault().unpinAll(viewId);
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		String viewId = evaluationContext instanceof IEvaluationContext context
				&& context.getVariable(ISources.ACTIVE_PART_ID_NAME) instanceof String id ? id : null;
		setBaseEnabled(PinnedFieldsManager.getDefault().hasPinnedFields(viewId));
	}
}
