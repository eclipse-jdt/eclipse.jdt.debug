/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * @since 3.0
 */
public class SnippetEditorStorageDocumentProvider extends StorageDocumentProvider {
	
	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object,
	 *      org.eclipse.jface.text.IDocument)
	 */
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
			IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}
}
