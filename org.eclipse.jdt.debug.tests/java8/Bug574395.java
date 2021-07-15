/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
import java.util.Arrays;
import java.util.List;

public class Bug574395 {
	public static void main(String[] args) {
		final List<String> list = Arrays.asList("1");
		Arrays.asList(1, 2, 3).stream().filter(i -> {
			return match(i, list);
		}).count();
	}

	private static boolean match(Integer i, List<String> list) {
		return list.contains(i);
	}
}