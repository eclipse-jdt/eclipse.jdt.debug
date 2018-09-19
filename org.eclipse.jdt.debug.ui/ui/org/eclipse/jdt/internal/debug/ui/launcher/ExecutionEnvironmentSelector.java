/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.launcher;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.ui.stringsubstitution.IArgumentSelector;
import org.eclipse.jdt.internal.debug.ui.jres.ExecutionEnvironmentsLabelProvider;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Used to select from available execution environments.
 */
public class ExecutionEnvironmentSelector implements IArgumentSelector {

	/**
	 * Constructs a new selector
	 */
	public ExecutionEnvironmentSelector() {
	}

	@Override
	public String selectArgument(IStringVariable variable, Shell shell) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new ExecutionEnvironmentsLabelProvider());
		dialog.setTitle(LauncherMessages.ExecutionEnvironmentSelector_0);
		dialog.setMultipleSelection(false);
		dialog.setMessage(LauncherMessages.ExecutionEnvironmentSelector_1);
		dialog.setElements(JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments());
		if (dialog.open() == Window.OK) {
			return (((IExecutionEnvironment)dialog.getResult()[0]).getId());
		}
		return null;
	}

}
