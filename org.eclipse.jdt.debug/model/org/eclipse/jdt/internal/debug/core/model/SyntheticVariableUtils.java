/*******************************************************************************
 * Copyright (c) 2020 Gayan Perera and others.
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
package org.eclipse.jdt.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;

/**
 * Utility class for handling Synthetic Variables.
 */
public final class SyntheticVariableUtils {
	private static final String ENCLOSING_INSTANCE_PREFIX = "this$"; //$NON-NLS-1$
	private static final String ANONYMOUS_VAR_PREFIX = ASTEvaluationEngine.ANONYMOUS_VAR_PREFIX;

	private SyntheticVariableUtils() {
	}

	/**
	 * When many anonymous objects are nested as below
	 *
	 * <pre>
	 * public Bar exec(Predicate&lt;String&gt; predicate) {
	 *
	 * 	return new Bar() {
	 * 		private Object bar;
	 *
	 * 		&#64;Override
	 * 		public Foo bar(String vbar) {
	 * 			return new Foo() {
	 * 				private Object foo;
	 *
	 * 				&#64;Override
	 * 				public String foo(String vfoo) {
	 * 					predicate.test("vfoo");
	 * 					return vfoo;
	 * 				}
	 * 			};
	 * 		}
	 * 	};
	 * }
	 * </pre>
	 *
	 * The outermost method parameters are not available as synthetic variables in the bottom most object. This method try to extract those variables
	 * from the variable graph. This method looks for synthetic variables in enclosing instances and traverse variables of those instances and collect
	 * synthetic outer local variables and return them.
	 *
	 * @param variables
	 *            variable to search on
	 * @return array of variables
	 */
	public static IVariable[] findSyntheticVariables(IVariable[] variables) throws DebugException {
		ArrayList<IVariable> extracted = new ArrayList<>();
		for (IVariable variable : variables) {
			if (variable.getName().startsWith(ANONYMOUS_VAR_PREFIX)) {
				extracted.add(variable);
			}

			if (variable.getName().startsWith(ENCLOSING_INSTANCE_PREFIX)) {
				extracted.addAll(Arrays.asList(findSyntheticVariables(variable.getValue().getVariables())));
			}
		}
		return extracted.toArray(new IVariable[extracted.size()]);
	}

}
