/*******************************************************************************
 * Copyright (c) 2014 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/

import java.util.Arrays;
import java.util.List;

public class EvalTest18 {
	public static void main(String[] args) {
		List<String> strings = Arrays.asList("One", "Two", "Three");
		System.out.println("Count of strings in stream from array =" + strings.stream().count());
	}
}