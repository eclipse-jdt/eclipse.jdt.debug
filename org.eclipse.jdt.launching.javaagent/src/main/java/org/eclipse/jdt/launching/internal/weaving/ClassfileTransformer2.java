/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.internal.weaving;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.attribute.SourceDebugExtensionAttribute;
import java.nio.charset.StandardCharsets;

public class ClassfileTransformer2 {

	// must match JDIHelpers.STRATA_ID
	private static final String STRATA_ID = "jdt"; //$NON-NLS-1$

	public byte[] transform(byte[] classfileBuffer, final String location) {
		ClassModel model = ClassFile.of().parse(classfileBuffer);

		if (model.findAttribute(Attributes.sourceDebugExtension()).isPresent()) {
			return classfileBuffer;
		}

		String source = model.findAttribute(Attributes.sourceFile()).map(a -> a.sourceFile().stringValue()).orElse(null);

		StringBuilder smap = new StringBuilder();
		smap.append("SMAP\n"); //$NON-NLS-1$
		smap.append(source).append("\n"); //$NON-NLS-1$
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

		byte[] smapBytes = smap.toString().getBytes(StandardCharsets.UTF_8);

		ClassTransform addSmap = ClassTransform.endHandler(cb -> cb.with(SourceDebugExtensionAttribute.of(smapBytes)));

		return ClassFile.of().transformClass(model, addSmap);
	}
}