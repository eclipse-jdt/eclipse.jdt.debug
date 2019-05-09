/*******************************************************************************
 * Copyright (c) 2019 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.debug.tests.variables;

import org.eclipse.jdt.core.IJavaProject;

public class TestLogicalStructuresJava9 extends TestLogicalStructures {

	public TestLogicalStructuresJava9(String name) {
		super(name);
	}

	@Override
	protected IJavaProject getProjectContext() {
		return get9Project();
	}
}
