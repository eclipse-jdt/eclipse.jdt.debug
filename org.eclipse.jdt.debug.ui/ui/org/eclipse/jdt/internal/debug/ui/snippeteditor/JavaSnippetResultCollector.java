package org.eclipse.jdt.internal.debug.ui.snippeteditor;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.ui.text.java.ResultCollector;

public class JavaSnippetResultCollector extends ResultCollector {

	/**
	 * @see ICompletionRequestor#acceptAnonymousType(char[], char[], char[][], char[][], char[][], char[], int, int, int)
	 */
	public void acceptAnonymousType(char[] superTypePackageName, char[] superTypeName, char[][] parameterPackageNames, char[][] parameterTypeNames,
		char[][] parameterNames, char[] completionName, int modifiers, int completionStart, int completionEnd) {

		//currently not supported in the Java Snippet Editor
		//@see bug 8265
	}
}