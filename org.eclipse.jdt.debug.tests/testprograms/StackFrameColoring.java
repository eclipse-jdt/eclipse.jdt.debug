/*******************************************************************************
 * Copyright (c) 2025 Zsombor Gegesy and others.
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StackFrameColoring {

	public static void main(String[] args) {
		new StackFrameColoring().run();
	}

	void run() {
		List<String> result = Arrays.asList("hello", "world").stream().map(value -> {
			breakpointMethod();
			return value;
		}).collect(Collectors.toList());
		System.out.println("StackFrameColoring.run called: "+ result);
	}

	public void breakpointMethod() {
		System.out.println("set a breakpoint here");
	}

}
