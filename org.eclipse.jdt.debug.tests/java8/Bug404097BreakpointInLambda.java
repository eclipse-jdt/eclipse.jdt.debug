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

import java.util.function.Consumer;

public class Bug404097BreakpointInLambda {

	public static void breakpointMethod(final String methodParameter) {
		// StringBuilder to make sure the compiler doesn't optimize "methodVariable" out of the compiled code.
		final StringBuilder methodVariable = new StringBuilder("methodVariable");
		Consumer<String> r = lambdaParameter -> {
			String lambdaVariable = "lambdaVariable";
			System.out.println("method parameter: " + methodParameter);
			System.out.println("method variable: " + methodVariable);
			System.out.println("lambda parameter: " + lambdaParameter);
			System.out.println("lambda variable: " + lambdaVariable);
		};
		r.accept("lambdaParameter");
	}

	public static void main(String[] args) {
		breakpointMethod("methodParameter");
	}
}
