/**********************************************************************
Copyright (c) 2000, 2004 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * The document factory for Java Snippet Editor
 */
public class SnippetDocumentFactory  implements IDocumentFactory {

	public SnippetDocumentFactory() {
	}
		
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new Document();
	}
}