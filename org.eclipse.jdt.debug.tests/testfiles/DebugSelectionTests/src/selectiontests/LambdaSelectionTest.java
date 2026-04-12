/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
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
package selectiontests;

import java.util.Arrays;
import java.util.List;

/**
 * The test resumes at lambda chain and at each resume expects selecting the next lambda expression.
 */
public class LambdaSelectionTest {

	public static void main(String[] main) {
		List<String> list = Arrays.asList("A");
		list.stream()
			.map(s -> s.toLowerCase()).filter(s -> s.equals("b")).forEach(System.out::println); // line 27, test breakpoint is set here
	}
}
