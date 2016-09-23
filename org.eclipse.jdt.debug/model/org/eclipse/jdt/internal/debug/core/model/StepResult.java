/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.debug.core.model;

import com.sun.jdi.Method;
import com.sun.jdi.Value;

public class StepResult {

	public StepResult(Method method, int targetFrameCount, Value value, boolean isReturnValue) {
		this.fMethod = method;
		this.fTargetFrameCount = targetFrameCount;
		this.fValue = value;
		this.fIsReturnValue = isReturnValue;
	}

	/**
	 * The method that was being stepped-over or step-returned from.
	 */
	public final Method fMethod;

	/**
	 * If a step-return or step-over is in progress, this is the stack size at which the result value is expected.
	 */
	public final int fTargetFrameCount;

	/**
	 * Returned value or thrown exception after a step-return or step-over.
	 */
	public final Value fValue;

	/**
	 * Whether {@link #fValue} was returned or thrown
	 */
	public final boolean fIsReturnValue;
}