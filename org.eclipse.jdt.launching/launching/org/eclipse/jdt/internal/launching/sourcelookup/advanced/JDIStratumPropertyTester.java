/*******************************************************************************
 * Copyright (c) 2012-2016 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.sourcelookup.advanced;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.DebugException;

/**
 * {@link PropertyTester} that returns {@code true} iff testee is a JDI object that has advanced source lookup JSR-45 stratum.
 */
public class JDIStratumPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean result;
		try {
			result = IJDIHelpers.INSTANCE.getClassesLocation(receiver) != null;
		}
		catch (DebugException e) {
			result = false;
		}
		return result;
	}

}
