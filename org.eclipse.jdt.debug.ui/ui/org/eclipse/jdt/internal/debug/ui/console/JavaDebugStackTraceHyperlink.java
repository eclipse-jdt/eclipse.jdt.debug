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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.TextConsole;

/**
 * A hyperlink from a stack trace line of the form:
 *
 * <pre>
 *     Test.main(String[]) line: 6
 * </pre>
 */
public class JavaDebugStackTraceHyperlink extends JavaStackTraceHyperlink {

	static final String LINE_PREFIX = "line: "; //$NON-NLS-1$

	private static final Pattern LINE_PATTERN = Pattern.compile(".*(" + LINE_PREFIX + "\\d+)"); //$NON-NLS-1$ //$NON-NLS-2$

	public JavaDebugStackTraceHyperlink(TextConsole console) {
		super(console);
	}

	@Override
	protected String getLinkText() throws CoreException {
		try {
			TextConsole console = getConsole();
			IDocument document = console.getDocument();
			IRegion region = console.getRegion(this);
			int regionOffset = region.getOffset();

			int lineNumber = document.getLineOfOffset(regionOffset);
			IRegion lineInformation = document.getLineInformation(lineNumber);
			int lineOffset = lineInformation.getOffset();
			String line = document.get(lineOffset, lineInformation.getLength());
			line = line.trim();
			return line;
		} catch (BadLocationException e) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_retrieve_hyperlink_text__8, e);
			throw new CoreException(status);
		}
	}

	@Override
	protected int getLineNumber(String linkText) {
		LinkSubstring lineText = extractLineText(linkText);
		if (lineText != null) {
			try {
				String lineNumberText = lineText.substring.substring(LINE_PREFIX.length());
				return Integer.parseInt(lineNumberText);
			} catch (NumberFormatException e) {
				// ignore, we couldn't parse the line number
			}
		}
		return -1;
	}

	@Override
	protected String getTypeName(String linkText) throws CoreException {
		LinkSubstring linkSubstring = extractTypeName(linkText);
		if (linkSubstring != null) {
			return linkSubstring.substring;
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null);
		throw new CoreException(status);
	}

	public static LinkSubstring extractLineText(String linkText) {
		LinkSubstring lineText = null;
		Matcher matcher = LINE_PATTERN.matcher(linkText);
		if (matcher.matches()) {
			int groupCount = matcher.groupCount();
			int groupIndex = 1;
			if (groupIndex <= groupCount) {
				String lineNumberText = matcher.group(groupIndex);
				int startIndex = matcher.start(groupIndex);
				lineText = new LinkSubstring(lineNumberText, startIndex);
			}
		}
		return lineText;
	}

	public static LinkSubstring extractTypeName(String linkText) {
		boolean hasLeadingSubtype = false;
		int startIndex = 0;
		int indexOfOpeningBracket = linkText.indexOf('(');
		int endIndex = indexOfOpeningBracket;
		// check if we have format "Subtype(Type).method()", if so we want "Type"
		int indexOfSecondOpeningBracket = linkText.indexOf('(', endIndex + 1);
		int indexOfClosingBracket = linkText.indexOf(')', endIndex);
		if (indexOfSecondOpeningBracket != -1 && indexOfClosingBracket != -1 && indexOfClosingBracket < indexOfSecondOpeningBracket) {
			startIndex = endIndex + 1;
			endIndex = indexOfClosingBracket;
			hasLeadingSubtype = true;
		}
		if (endIndex >= 0) {
			String substring = linkText.substring(startIndex, endIndex);
			if (!hasLeadingSubtype) {
				// remove the method name
				int dotIndex = substring.lastIndexOf('.');
				if (dotIndex != -1) {
					endIndex = startIndex + dotIndex;
					substring = linkText.substring(startIndex, endIndex);
				}
			}
			// check if we have format "Type<ParameterType>.method()", if so we want "Type"
			int indexOfTriangleBracket = substring.indexOf('<');
			if (indexOfTriangleBracket != -1) {
				endIndex = startIndex + indexOfTriangleBracket;
				substring = linkText.substring(startIndex, endIndex);
			}
			int innerClassIndex = substring.indexOf('$');
			if (innerClassIndex != -1) {
				endIndex = startIndex + innerClassIndex;
			}
		}
		String typeName = linkText.substring(startIndex, endIndex);
		return new LinkSubstring(typeName, startIndex);
	}

	static class LinkSubstring {

		final String substring;
		final int startIndex;

		private LinkSubstring(String substring, int startIndex) {
			this.substring = substring;
			this.startIndex = startIndex;
		}
	}
}
