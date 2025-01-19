/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import java.util.function.Function;

import org.eclipse.jdt.internal.debug.ui.console.JavaDebugStackTraceHyperlink.LinkSubstring;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Creates hyper-links of type {@link JavaDebugStackTraceHyperlink}.
 */
public class JavaDebugStackTraceConsoleTracker extends JavaConsoleTracker {
	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			// add a hyperlink at "line: 123"
			addHyperlinkAtContent(event, JavaDebugStackTraceHyperlink::extractLineText);
			// add a hyperlink at the type
			addHyperlinkAtContent(event, JavaDebugStackTraceHyperlink::extractTypeName);
		} catch (BadLocationException e) {
		}
	}

	private void addHyperlinkAtContent(PatternMatchEvent event, Function<String, LinkSubstring> contentSupplier) throws BadLocationException {
		int offset = event.getOffset();
		int length = event.getLength();

		TextConsole console = getConsole();
		IDocument document = console.getDocument();
		String line = document.get(offset, length);
		LinkSubstring linkSubstring = contentSupplier.apply(line);

		if (linkSubstring != null) {
			int hyperlinkStartIndex = offset + linkSubstring.startIndex;
			int hyperlinkLength = linkSubstring.substring.length();
			IHyperlink link = new JavaDebugStackTraceHyperlink(console);
			console.addHyperlink(link, hyperlinkStartIndex, hyperlinkLength);
		}
	}

}
