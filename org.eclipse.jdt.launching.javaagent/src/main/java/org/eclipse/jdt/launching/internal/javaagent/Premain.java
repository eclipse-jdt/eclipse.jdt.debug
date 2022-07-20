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

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.eclipse.jdt.launching.internal.weaving.ClassfileTransformer;

public class Premain {
	private static final ClassfileTransformer transformer = new ClassfileTransformer();

	public static void premain(final String agentArgs, final Instrumentation inst) {
		final boolean debuglog = "debuglog".equals(agentArgs); //$NON-NLS-1$

		// disable instrumentation if Object.class class format is not supported
		short major = readJavaLangObjectMajor(debuglog);
		if (major < 0 || major > ClassfileTransformer.MAX_CLASS_MAJOR) {
			String vendor = System.getProperty("java.vendor"); //$NON-NLS-1$
			String version = System.getProperty("java.version"); //$NON-NLS-1$
			System.err.printf("JRE %s/%s is not supported, advanced source lookup disabled.\n Eclipse debugger will use less precise source lookup implementation for this debug session, but everything else will continue to work otherwise.\n" //$NON-NLS-1$
						+ "Upgrading Eclipse to the latest version will likely make this warning go away.", vendor, version); //$NON-NLS-1$
			return;
		}

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

	private static short readJavaLangObjectMajor(boolean debuglog) {
		// https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-4.html
		// We need class major_version, i.e. the u2 field starting at offset 6

		final int offset = 6;

		try (InputStream is = ClassLoader.getSystemResourceAsStream("java/lang/Object.class")) {//$NON-NLS-1$ )
			if (is == null) {
				printErrorMessage("Could not open java/lang/Object.class system resource stream.", null, debuglog);//$NON-NLS-1$
				return -1;
			}
			byte[] bytes = new byte[offset + 2];
			if (is.read(bytes) < bytes.length) {
				printErrorMessage("Could not read java/lang/Object.class system resource stream.", null, debuglog);//$NON-NLS-1$
				return -1;
			}

			int magic = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
			if (magic != 0xCAFEBABE) {
				printErrorMessage("Invalid java/lang/Object.class magic.", null, debuglog);//$NON-NLS-1$
				return -1;
			}

			return (short) (((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF));
		}
		catch (Exception ex) {
			printErrorMessage("Could not open java/lang/Object.class system resource stream.", ex, debuglog);//$NON-NLS-1$
			return -1;
		}
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
