/*******************************************************************************
 * Copyright (c) 2014, 2019 Igor Fedorenko
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
 *      IBM Corporation - ASM version update
 *******************************************************************************/
package org.eclipse.jdt.launching.internal.weaving;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassfileTransformer {

	// must match JDIHelpers.STRATA_ID
	private static final String STRATA_ID = "jdt"; //$NON-NLS-1$

	/** max supported java class format major version, must match {@link #ASM_API} below **/
	public static final int MAX_CLASS_MAJOR = Opcodes.V12;

	/** supported ASM API version, must match {@link #MAX_CLASS_MAJOR} above */
	private static final int ASM_API = Opcodes.ASM7;

	public byte[] transform(byte[] classfileBuffer, final String location) {

		final ClassReader r = new ClassReader(classfileBuffer, 0, classfileBuffer.length);
		final ClassWriter w = new ClassWriter(0);

		r.accept(new ClassVisitor(ASM_API, w) {
			@Override
			public void visitSource(String source, String debug) {
				String javaSource = source;
				// TODO merge SMAP if present (always present when used together with Equinox weaver)
				if (debug == null) {
					StringBuilder smap = new StringBuilder();
					smap.append("SMAP\n"); //$NON-NLS-1$
					smap.append(javaSource).append("\n"); //$NON-NLS-1$
					// default strata name
					smap.append("Java\n"); //$NON-NLS-1$
					smap.append("*S " + STRATA_ID + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					smap.append("*F\n"); //$NON-NLS-1$
					smap.append("1 ").append(source).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
					smap.append("2 ").append(location).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
					// JSR-045, StratumSection
					// "One FileSection and one LineSection (in either order) must follow the StratumSection"
					smap.append("*L\n"); //$NON-NLS-1$
					smap.append("*E\n"); //$NON-NLS-1$
					debug = smap.toString();
				}

				super.visitSource(javaSource, debug);
			}
		}, 0);

		return w.toByteArray();
	}
}
