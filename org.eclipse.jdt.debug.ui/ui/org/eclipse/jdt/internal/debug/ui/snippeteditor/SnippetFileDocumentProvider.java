/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class SnippetFileDocumentProvider extends TextFileDocumentProvider {
	public SnippetFileDocumentProvider() {
		super(new TextFileDocumentProvider(new SnippetEditorStorageDocumentProvider()));
	}
}


