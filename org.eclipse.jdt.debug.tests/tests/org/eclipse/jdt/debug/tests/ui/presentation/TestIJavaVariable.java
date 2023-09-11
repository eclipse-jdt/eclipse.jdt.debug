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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class TestIJavaVariable implements IJavaVariable {

	private final String name;
	private IValue value;

	public TestIJavaVariable(String name, IValue value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public IValue getValue() throws DebugException {
		return value;
	}

	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return null;
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return null;
	}

	@Override
	public IDebugTarget getDebugTarget() {
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
	public void setValue(String expression) throws DebugException {

	}

	@Override
	public void setValue(IValue value) throws DebugException {
		this.value = value;
	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPublic() throws DebugException {
		return false;
	}

	@Override
	public boolean isPrivate() throws DebugException {
		return false;
	}

	@Override
	public boolean isProtected() throws DebugException {
		return false;
	}

	@Override
	public boolean isPackagePrivate() throws DebugException {
		return false;
	}

	@Override
	public boolean isFinal() throws DebugException {
		return false;
	}

	@Override
	public boolean isStatic() throws DebugException {
		return false;
	}

	@Override
	public boolean isSynthetic() throws DebugException {
		return false;
	}

	@Override
	public String getSignature() throws DebugException {
		return null;
	}

	@Override
	public String getGenericSignature() throws DebugException {
		return null;
	}

	@Override
	public IJavaType getJavaType() throws DebugException {
		return null;
	}

	@Override
	public boolean isLocal() throws DebugException {
		return false;
	}

}
