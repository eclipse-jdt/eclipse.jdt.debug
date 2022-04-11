/*******************************************************************************
 * Copyright (c) 2022 Gayan Perera and others.
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
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public final class LambdaLocationLocatorHelper {

	private LambdaLocationLocatorHelper() {
	}

	/**
	 * Return of the signature of the lambda method. The signature is computed to 
	 * be compatible with the final lambda method with method arguments and outer 
	 * local variables in debugger.
	 */
	public static String toMethodSignature(IMethodBinding methodBinding) {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		if (methodBinding.getParameterTypes().length > 0 || methodBinding.getSyntheticOuterLocals().length > 0) {
			builder.append(Stream.of(methodBinding.getSyntheticOuterLocals())
					.map(b -> Signature.createTypeSignature(qualifiedName(b.getType()), true))
					.collect(Collectors.joining()));

			builder.append(Stream.of(methodBinding.getParameterTypes())
					.map(b -> Signature.createTypeSignature(qualifiedName(b), true))
					.collect(Collectors.joining()));
		}
		builder.append(')');
		builder.append(Signature.createTypeSignature(qualifiedName(methodBinding.getReturnType()), true));
		return builder.toString();
	}

	/**
	 * Return the lambda method name from the given method binding.
	 */
	public static String toMethodName(IMethodBinding methodBinding) {
		String key = methodBinding.getKey();
		return key.substring(key.indexOf('.') + 1, key.indexOf('('));
	}

	private static String qualifiedName(ITypeBinding binding) {
		return binding.getQualifiedName().replace('.', '/');
	}
}
