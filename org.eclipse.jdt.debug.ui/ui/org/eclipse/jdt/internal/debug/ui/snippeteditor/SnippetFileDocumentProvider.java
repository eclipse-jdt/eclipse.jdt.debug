/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class SnippetFileDocumentProvider extends FileDocumentProvider {
	
	/**
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */ 
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document= super.createDocument(element);
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		IDocumentPartitioner partitioner= tools.createDocumentPartitioner();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		return document;
	}		
}


