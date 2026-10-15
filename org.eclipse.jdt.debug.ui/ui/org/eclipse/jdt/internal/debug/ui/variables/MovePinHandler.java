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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Moves the pin of the selected field up or down, so that the field is displayed one row higher or lower among the
 * pinned fields of the active view. The direction is given by the executable extension data: <code>up</code> or
 * <code>down</code>.
 *
 * @see PinnedFieldsManager#movePin(String, IJavaFieldVariable, Object[], boolean)
 */
public class MovePinHandler extends AbstractHandler implements IExecutableExtension {

	private boolean fUp = true;

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		fUp = !"down".equals(data); //$NON-NLS-1$
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		boolean enabled = false;
		if (evaluationContext instanceof IEvaluationContext context) {
			Object selection = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			Object part = context.getVariable(ISources.ACTIVE_PART_NAME);
			try {
				Target target = getTarget(selection, part instanceof IWorkbenchPart workbenchPart ? workbenchPart : null);
				enabled = target != null && PinnedFieldsManager.getDefault().canMovePin(target.viewId(), target.field(), target.siblings(), fUp);
			} catch (DebugException e) {
				// the target is not available any more
			}
		}
		setBaseEnabled(enabled);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Target target = getTarget(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));
			if (target != null) {
				PinnedFieldsManager.getDefault().movePin(target.viewId(), target.field(), target.siblings(), fUp);
			}
		} catch (DebugException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * The selected field and the children displayed with it in the view.
	 */
	private record Target(String viewId, IJavaFieldVariable field, Object[] siblings) {
	}

	private static Target getTarget(Object selection, IWorkbenchPart part) throws DebugException {
		if (part == null || !(selection instanceof ITreeSelection treeSelection) || treeSelection.size() != 1
				|| !(treeSelection.getFirstElement() instanceof IJavaFieldVariable field)) {
			return null;
		}
		String viewId = part.getSite().getId();
		TreePath parentPath = treeSelection.getPaths()[0].getParentPath();
		// a root element is a child of the input of the view, e.g. a static field of a static frame
		Object parent = parentPath.getSegmentCount() == 0 ? getInput(part) : parentPath.getLastSegment();
		// only the children actually displayed in the view count
		Object[] siblings = JavaContentProviderFilter.filterVariables(getChildren(parent), new PresentationContext(viewId));
		return new Target(viewId, field, siblings);
	}

	private static Object getInput(IWorkbenchPart part) {
		IDebugView view = part.getAdapter(IDebugView.class);
		Viewer viewer = view == null ? null : view.getViewer();
		return viewer == null ? null : viewer.getInput();
	}

	private static Object[] getChildren(Object parent) throws DebugException {
		if (parent instanceof IStackFrame frame) {
			return frame.getVariables();
		}
		IValue value = null;
		if (parent instanceof IVariable variable) {
			value = variable.getValue();
		} else if (parent instanceof IExpression expression) {
			value = expression.getValue();
		}
		return value == null ? new Object[0] : value.getVariables();
	}
}
