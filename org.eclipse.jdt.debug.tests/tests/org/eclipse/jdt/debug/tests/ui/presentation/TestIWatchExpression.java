/*******************************************************************************
 * Copyright (c) 2022 Zsombor Gegesy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Zsombor Gegesy - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.debug.tests.ui.presentation;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpression;

public class TestIWatchExpression implements IWatchExpression {
	private final IValue value;
	private final String expressionText;

	public TestIWatchExpression(String expressionText, IValue value) {
		this.expressionText = expressionText;
		this.value = value;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

	@Override
	public String[] getErrorMessages() {
		return null;
	}

	@Override
	public String getExpressionText() {
		return expressionText;
	}

	@Override
	public IValue getValue() {
		return value;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return null;
	}

	@Override
	public void dispose() {

	}

	@Override
	public String getModelIdentifier() {
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void evaluate() {

	}

	@Override
	public void setExpressionContext(IDebugElement context) {

	}

	@Override
	public void setExpressionText(String expressionText) {

	}

	@Override
	public boolean isPending() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

}
