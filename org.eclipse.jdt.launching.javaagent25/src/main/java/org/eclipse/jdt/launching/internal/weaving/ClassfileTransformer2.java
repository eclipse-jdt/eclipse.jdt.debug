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

import java.io.ByteArrayOutputStream;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.attribute.SourceDebugExtensionAttribute;

public class ClassfileTransformer2 {

	// must match JDIHelpers.STRATA_ID
	private static final String STRATA_ID = "jdt"; //$NON-NLS-1$

	public byte[] transform(byte[] classfileBuffer, final String location) {
		ClassModel model = ClassFile.of().parse(classfileBuffer);

		if (model.findAttribute(Attributes.sourceDebugExtension()).isPresent()) {
			return classfileBuffer;
		}

		String source = model.findAttribute(Attributes.sourceFile()).map(a -> a.sourceFile().stringValue()).orElse(null);
		if (source == null) {
			return classfileBuffer;
		}
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

		byte[] smapBytes = toModifiedUtf8(smap.toString());

		ClassTransform addSmap = ClassTransform.endHandler(cb -> cb.with(SourceDebugExtensionAttribute.of(smapBytes)));

		return ClassFile.of().transformClass(model, addSmap);
	}

	private static byte[] toModifiedUtf8(String s) {
		// DataOutputStream.writeUTF rejects payloads > 65535 bytes because it uses a
		// 2-byte length prefix.  SourceDebugExtension uses a 4-byte attribute length
		// and has no such limit, so encode modified UTF-8 directly.
		ByteArrayOutputStream baos = new ByteArrayOutputStream(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\u0000') {
				// null encoded as two-byte sequence so the result stays null-free
				baos.write(0xC0);
				baos.write(0x80);
			} else if (c <= 0x7F) {
				baos.write(c);
			} else if (c <= 0x7FF) {
				baos.write(0xC0 | (c >> 6));
				baos.write(0x80 | (c & 0x3F));
			} else {
				baos.write(0xE0 | (c >> 12));
				baos.write(0x80 | ((c >> 6) & 0x3F));
				baos.write(0x80 | (c & 0x3F));
			}
		}
		return baos.toByteArray();
	}
}