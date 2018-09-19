/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;

/**
 * The document setup participant for the Java Snippet Editor
 */
public class SnippetDocumentSetupParticipant  implements IDocumentSetupParticipant {

	public SnippetDocumentSetupParticipant() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void setup(IDocument document) {
		if (document != null) {
			JavaTextTools tools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
			tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		}
	}
}
