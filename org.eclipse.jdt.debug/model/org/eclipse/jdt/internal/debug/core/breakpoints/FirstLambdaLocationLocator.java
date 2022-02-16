/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;

public class FirstLambdaLocationLocator extends ASTVisitor {
	private int fNodeLength = -1;
	private int fNodeOffset = -1;
	private int fLineOffset = -1;
	private int fLineEndPosition = -1;
	private String fLambdaMethodName;
	private boolean fLocationFound = false;

	public FirstLambdaLocationLocator(int lineOffset, int lineEndPosition) {
		fLineOffset = lineOffset;
		fLineEndPosition = lineEndPosition;
	}

	/**
	 * Return of the name of the lambda method where the valid location is.
	 */
	public String getLambdaMethodName() {
		return fLambdaMethodName;
	}

	public int getNodeLength() {
		return fNodeLength;
	}

	public int getNodeOffset() {
		return fNodeOffset;
	}

	@Override
	public boolean visit(LambdaExpression node) {
		if (fLocationFound) {
			return false;
		}
		if (node.getStartPosition() < fLineOffset || node.getStartPosition() > fLineEndPosition) {
			return false;
		}
		fNodeLength = node.getLength();
		fNodeOffset = node.getStartPosition();
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding != null) {
			fLambdaMethodName = toMethodName(methodBinding);
			fLocationFound = true;
		}
		return false;
	}

	private String toMethodName(IMethodBinding methodBinding) {
		String key = methodBinding.getKey();
		return key.substring(key.indexOf('.') + 1, key.indexOf('('));
	}

}
