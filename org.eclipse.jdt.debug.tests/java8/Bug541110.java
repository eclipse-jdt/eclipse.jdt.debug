/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
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

import java.util.HashMap;
import java.util.Map;

public class Bug541110 {

	public final Map<String, String> map = new HashMap<>();
	public void breakpointMethod(final String key, final String value) {
		map.compute(key, (k, v) -> value);
	}

	public static void main(String[] args) {
		Bug541110 delta = new Bug541110();
		delta.breakpointMethod("someKey", "someValue");
	}
}
