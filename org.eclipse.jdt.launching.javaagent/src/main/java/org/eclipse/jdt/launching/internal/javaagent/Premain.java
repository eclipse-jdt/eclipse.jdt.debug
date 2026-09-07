/*******************************************************************************
 * Copyright (c) 2011, 2019 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *      IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.jdt.launching.internal.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.eclipse.jdt.launching.internal.weaving.ClassfileTransformer2;

public class Premain {
	private static final ClassfileTransformer2 transformer = new ClassfileTransformer2();

	public static void premain(final String agentArgs, final Instrumentation inst) {
		final boolean debuglog = "debuglog".equals(agentArgs); //$NON-NLS-1$

		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				try {
					if (protectionDomain == null) {
						return null;
					}

					if (className == null) {
						return null;
					}

					final CodeSource codeSource = protectionDomain.getCodeSource();
					if (codeSource == null) {
						return null;
					}

					final URL locationUrl = codeSource.getLocation();
					if (locationUrl == null) {
						return null;
					}

					final String location = locationUrl.toExternalForm();

					return transformer.transform(classfileBuffer, location);
				}
				catch (Exception e) {
					System.err.printf("Could not instrument class %s: %s.\n", className, e.getMessage()); //$NON-NLS-1$
					if (debuglog) {
						e.printStackTrace(System.err);
					}
				}
				return null;
			}
		});

		printErrorMessage("Advanced source lookup enabled.", null, debuglog);//$NON-NLS-1$
	}

	private static void printErrorMessage(String errorMessage, Exception ex, boolean debuglog) {
		if (debuglog) {
			System.err.println(errorMessage);
			if (ex != null) {
				ex.printStackTrace(System.err);
			}
		}
	}

}
