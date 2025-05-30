/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;

/**
 * Collects lambda expressions in a given line range
 */
public class LambdaCollector extends ASTVisitor {
	private final int fLineOffset;
	private final int fLineEndPosition;
	private final List<LambdaExpression> lambdaExpressions;

	public LambdaCollector(int lineOffset, int lineEndPosition) {
		fLineOffset = lineOffset;
		fLineEndPosition = lineEndPosition;
		lambdaExpressions = new ArrayList<>();
	}

	@Override
	public boolean visit(LambdaExpression node) {
		if (node.getStartPosition() < fLineOffset || node.getStartPosition() > fLineEndPosition) {
			return false;
		}
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding != null) {
			lambdaExpressions.add(node);
		}
		return false;
	}

	public List<LambdaExpression> getLambdaExpressions() {
		return lambdaExpressions;
	}
}
