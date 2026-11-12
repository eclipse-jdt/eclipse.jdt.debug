/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.variables;

import java.util.Optional;

import org.eclipse.debug.ui.IWatchExpressionCellEditorFactory;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.contentassist.CurrentFrameContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public class JavaWatchExpressionCellEditorFactory implements IWatchExpressionCellEditorFactory {

	@Override
	public Optional<CellEditor> createCellEditor(Composite parent) {
		return Optional.of(new JDISourceViewerCellEditor(parent));
	}

	@Override
	public void configureSourceViewer(SourceViewer viewer) {
		viewer.configure(new DisplayViewerConfiguration() {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return new JavaDebugContentAssistProcessor(new CurrentFrameContext());
			}
		});
	}

	@Override
	public void prepareDocument(IDocument document) {
		JDIDebugUIPlugin.getDefault().getJavaTextTools()
				.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
	}
}
