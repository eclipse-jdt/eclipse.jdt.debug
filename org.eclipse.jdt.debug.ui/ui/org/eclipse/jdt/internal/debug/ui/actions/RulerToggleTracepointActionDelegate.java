/*******************************************************************************
 * Copyright (c) 2020, Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class RulerToggleTracepointActionDelegate extends AbstractRulerActionDelegate implements IActionDelegate2 {

	private static final String TOGGLE_TRACEPOINT_COMMAND = "org.eclipse.jdt.debug.ui.commands.ToggleTracepoint"; //$NON-NLS-1$
	private IEditorPart currentEditor;
	private IAction dummyAction;

	@Override
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		dummyAction = new Action() {
			// empty implementation to make compiler happy
		};
		return dummyAction;
	}

	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		currentEditor = targetEditor;
	}

	@Override
	public void init(IAction action) {
	}

	@Override
	public void dispose() {
		currentEditor = null;
		dummyAction = null;
		super.dispose();
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		if (currentEditor == null) {
			return;
		}
		IWorkbenchPartSite partSite = currentEditor.getSite();
		IHandlerService hservice = partSite.getService(IHandlerService.class);
		ICommandService cservice = partSite.getService(ICommandService.class);
		try {
			Command command = cservice.getCommand(TOGGLE_TRACEPOINT_COMMAND);
			ExecutionEvent exevent = hservice.createExecutionEvent(command, event);
			command.executeWithChecks(exevent);
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			DebugUIPlugin.log(e);
		}
	}

}
