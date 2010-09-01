/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action delegate for Open from Clipboard action.
 * 
 * @since 3.7
 */
public class OpenFromClipboardAction implements IWorkbenchWindowActionDelegate {

	/**
	 * Pattern to match a simple name e.g. <code>OpenFromClipboardAction</code>
	 */
	private static final String SIMPLE_NAME_PATTERN = "\\w+"; //$NON-NLS-1$

	/**
	 * Pattern to match a qualified name e.g.
	 * <code>org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction</code>
	 */
	private static final String QUALIFIED_NAME_PATTERN = "(" + SIMPLE_NAME_PATTERN //$NON-NLS-1$
			+ "\\.)*" + SIMPLE_NAME_PATTERN; //$NON-NLS-1$

	/**
	 * Pattern to match whitespace characters.
	 */
	private static final String WS = "\\s*"; //$NON-NLS-1$

	/**
	 * Pattern to match a java file name e.g. <code>OpenFromClipboardAction.java</code>
	 */
	private static final String JAVA_FILE_PATTERN = SIMPLE_NAME_PATTERN + "\\.java"; //$NON-NLS-1$

	/**
	 * Pattern to match a java file name followed by line number e.g.
	 * <code>OpenFromClipboardAction.java : 21</code>
	 */
	private static final String JAVA_FILE_LINE_PATTERN = JAVA_FILE_PATTERN + WS + ":" + WS + "\\d+"; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Pattern to match a qualified name followed by line number e.g.
	 * <code>org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction : 21</code>
	 */
	private static final String TYPE_LINE_PATTERN = QUALIFIED_NAME_PATTERN + WS + ":" + WS + "\\d+"; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Pattern to match a line from a stack trace e.g.
	 * <code> at org.eclipse.core.internal.runtime.Assert.isLegal(Assert.java:41)</code>
	 */
	private static final String STACK_TRACE_LINE_PATTERN = ".*\\(" //$NON-NLS-1$
			+ WS + JAVA_FILE_LINE_PATTERN + WS + "\\).*"; //$NON-NLS-1$

	/**
	 * Pattern to match a method e.g.
	 * <code>org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction.run(IAction)</code> ,
	 * <code>Worker.run()</code>
	 */
	private static final String METHOD_PATTERN = QUALIFIED_NAME_PATTERN + "\\(.*\\)"; //$NON-NLS-1$

	/**
	 * Pattern to match a stack element e.g. <code>java.lang.String.valueOf(char) line: 1456</code>
	 */
	private static final String STACK_PATTERN = METHOD_PATTERN + ".*\\d+"; //$NON-NLS-1$

	/**
	 * Pattern to match a member (field or method) of a type e.g.
	 * <code>OpenFromClipboardAction#run</code>, <code>Worker#run</code>
	 */
	private static final String MEMBER_PATTERN = QUALIFIED_NAME_PATTERN + "#" //$NON-NLS-1$
			+ SIMPLE_NAME_PATTERN;

	/*
	 * Constants to indicate the pattern matched
	 */
	private static final int INVALID = 0;

	private static final int QUALIFIED_NAME = 1;

	private static final int JAVA_FILE = 2;

	private static final int JAVA_FILE_LINE = 3;

	private static final int TYPE_LINE = 4;

	private static final int STACK_TRACE_LINE = 5;

	private static final int METHOD = 6;

	private static final int STACK = 7;

	private static final int MEMBER = 8;

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		TextTransfer textTransfer = TextTransfer.getInstance();
		final String inputText = (String) clipboard.getContents(textTransfer);
		if (inputText == null || inputText.length() == 0) {
			openInputEditDialog(""); //$NON-NLS-1$
			return;
		}

		if (isSingleLineInput(inputText)) {
			handleSingleLineInput(inputText);
			return;
		}
		handleMultipleLineInput(inputText);

		return;
	}

	private static JavaStackTraceConsole getJavaStackTraceConsole() {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if (consoles[i] instanceof JavaStackTraceConsole) {
				return (JavaStackTraceConsole) consoles[i];
			}
		}
		return null;
	}

	private static void handleMultipleLineInput(String inputText) {
		// multiple lines - simply paste to the console and open it
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		JavaStackTraceConsole console = getJavaStackTraceConsole();
		if (console != null) {
			console.getDocument().set(inputText);
			consoleManager.showConsoleView(console);
		} else {
			JavaStackTraceConsoleFactory javaStackTraceConsoleFactory = new JavaStackTraceConsoleFactory();
			javaStackTraceConsoleFactory.openConsole(inputText);
			console = getJavaStackTraceConsole();
		}
		IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(IJDIPreferencesConstants.PREF_AUTO_FORMAT_JSTCONSOLE)) {
			console.format();
		}
	}

	private static boolean isSingleLineInput(String inputText) {
		String lineDelimiter = System.getProperty("line.separator"); //$NON-NLS-1$
		String s = inputText.trim();
		return s.indexOf(lineDelimiter) == -1;
	}

	public static int getMatchingPattern(String s) {
		if (s.matches(JAVA_FILE_LINE_PATTERN))
			return JAVA_FILE_LINE;
		if (s.matches(JAVA_FILE_PATTERN))
			return JAVA_FILE;
		if (s.matches(TYPE_LINE_PATTERN))
			return TYPE_LINE;
		if (s.matches(STACK_TRACE_LINE_PATTERN))
			return STACK_TRACE_LINE;
		if (s.matches(METHOD_PATTERN))
			return METHOD;
		if (s.matches(STACK_PATTERN))
			return STACK;
		if (s.matches(MEMBER_PATTERN))
			return MEMBER;
		if (s.matches(QUALIFIED_NAME_PATTERN))
			return QUALIFIED_NAME;
		return INVALID;
	}

	private static void handleSingleLineInput(String inputText) {
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		String s = inputText;
		switch (getMatchingPattern(s)) {
		case JAVA_FILE_LINE: {
			int index = s.indexOf(':');
			String typeName = s.substring(0, index);
			typeName = s.substring(0, typeName.indexOf(".java")); //$NON-NLS-1$
			String lineNumber = s.substring(index + 1, s.length());
			lineNumber = lineNumber.trim();
			int line = (Integer.valueOf(lineNumber)).intValue();

			handleMatches(getTypeMatches(typeName, progressMonitor), line, inputText);
			break;
		}
		case JAVA_FILE: {
			String typeName = s.substring(0, s.indexOf(".java")); //$NON-NLS-1$
			handleMatches(getTypeMatches(typeName, progressMonitor), -1, inputText);
			break;
		}
		case TYPE_LINE: {
			int index = s.indexOf(':');
			String typeName = s.substring(0, index);
			typeName = typeName.trim();
			String lineNumber = s.substring(index + 1, s.length());
			lineNumber = lineNumber.trim();
			int line = (Integer.valueOf(lineNumber)).intValue();
			handleMatches(getTypeMatches(typeName, progressMonitor), line, inputText);
			break;
		}
		case STACK_TRACE_LINE: {
			int index1 = s.indexOf('(');
			int index2 = s.indexOf(')');
			s = s.substring(index1 + 1, index2);

			int index = s.indexOf(':');
			String typeName = s.substring(0, index);
			typeName = s.substring(0, typeName.indexOf(".java")); //$NON-NLS-1$
			String lineNumber = s.substring(index + 1, s.length());
			lineNumber = lineNumber.trim();
			int line = (Integer.valueOf(lineNumber)).intValue();

			handleMatches(getTypeMatches(typeName, progressMonitor), line, inputText);
			break;
		}
		case METHOD: {
			handleMatches(getMethodMatches(s, progressMonitor), -1, inputText);
			break;
		}
		case STACK: {
			int index = s.indexOf(')');
			String method = s.substring(0, index + 1);
			index = s.indexOf(':');
			String lineNumber = s.substring(index + 1).trim();
			int line = (Integer.valueOf(lineNumber)).intValue();
			handleMatches(getMethodMatches(method, progressMonitor), line, inputText);
			break;
		}
		case MEMBER:
			handleMatches(getMemberMatches(s.replace('#', '.'), progressMonitor), -1, inputText);
			break;
		case QUALIFIED_NAME:
			handleMatches(getNameMatches(s, progressMonitor), -1, inputText);
			break;
		case INVALID:
			openInputEditDialog(inputText);
			break;
		}
	}

	/**
	 * Perform a Java search for the type and return the corresponding Java elements.
	 * 
	 * @param typeName
	 *            the Type name
	 * @param progressMonitor
	 *            the Progress Monitor
	 * @return matched Java elements
	 */
	private static List getTypeMatches(String typeName, IProgressMonitor progressMonitor) {
		final List matches = new ArrayList();
		SearchPattern pattern = createSearchPattern(typeName, IJavaSearchConstants.TYPE);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		try {
			IProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 95, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
			searchEngine.search(pattern, createSearchParticipant(), scope, requestor, monitor);
			return matches;
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Perform a Java search for methods and constructors and return the corresponding Java
	 * elements.
	 * 
	 * @param s
	 *            the method pattern
	 * @param progressMonitor
	 *            the Progress Monitor
	 * @return matched Java elements
	 */
	private static List getMethodMatches(String s, IProgressMonitor progressMonitor) {
		final List matches = new ArrayList();
		SearchPattern methodPattern = createSearchPattern(s, IJavaSearchConstants.METHOD);
		SearchPattern constructorPattern = createSearchPattern(s, IJavaSearchConstants.CONSTRUCTOR);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		try {
			IProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 95, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
			searchEngine.search(methodPattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(constructorPattern, createSearchParticipant(), scope, requestor, monitor);

			return matches;
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Perform a Java search for fields, methods and constructors and return the corresponding Java
	 * elements.
	 * 
	 * @param s
	 *            the member pattern
	 * @param progressMonitor
	 *            the Progress Monitor
	 * @return matched Java elements
	 */
	private static List getMemberMatches(String s, IProgressMonitor progressMonitor) {
		final List matches = new ArrayList();
		SearchPattern methodPattern = createSearchPattern(s, IJavaSearchConstants.METHOD);
		SearchPattern constructorPattern = createSearchPattern(s, IJavaSearchConstants.CONSTRUCTOR);
		SearchPattern fieldPattern = createSearchPattern(s, IJavaSearchConstants.FIELD);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		try {
			IProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 95, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
			searchEngine.search(methodPattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(constructorPattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(fieldPattern, createSearchParticipant(), scope, requestor, monitor);

			return matches;
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Perform a Java search for types, fields, methods and constructors and return the
	 * corresponding Java elements.
	 * 
	 * @param s
	 *            the member pattern
	 * @param progressMonitor
	 *            the Progress Monitor
	 * @return matched Java elements
	 */
	private static List getNameMatches(String s, IProgressMonitor progressMonitor) {
		final List matches = new ArrayList();
		SearchPattern typePattern = createSearchPattern(s, IJavaSearchConstants.TYPE);
		SearchPattern methodPattern = createSearchPattern(s, IJavaSearchConstants.METHOD);
		SearchPattern constructorPattern = createSearchPattern(s, IJavaSearchConstants.CONSTRUCTOR);
		SearchPattern fieldPattern = createSearchPattern(s, IJavaSearchConstants.FIELD);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchRequestor requestor = createSearchRequestor(matches);

		SearchEngine searchEngine = new SearchEngine();
		try {
			IProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 95, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
			searchEngine.search(typePattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(methodPattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(constructorPattern, createSearchParticipant(), scope, requestor, monitor);
			searchEngine.search(fieldPattern, createSearchParticipant(), scope, requestor, monitor);
			return matches;
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

	private static void handleMatches(List matches, int line, String inputText) {
		if (matches.size() > 1) {
			int flags = JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_QUALIFIED | JavaElementLabelProvider.SHOW_ROOT;
			IWorkbenchWindow window = JDIDebugUIPlugin.getActiveWorkbenchWindow();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), new JavaElementLabelProvider(flags));
			dialog.setTitle(ActionMessages.OpenFromClipboardAction_OpenFromClipboard);
			dialog.setMessage(ActionMessages.OpenFromClipboardAction_SelectOrEnterTheElementToOpen);
			dialog.setElements(matches.toArray());
			dialog.setMultipleSelection(true);

			int result = dialog.open();
			if (result != IDialogConstants.OK_ID)
				return;

			Object[] elements = dialog.getResult();
			if (elements != null && elements.length > 0) {
				openJavaElements(elements, line);
			}
		} else if (matches.size() == 1) {
			openJavaElements(matches.toArray(), line);
		} else if (matches.size() == 0) {
			openInputEditDialog(inputText);
		}
	}

	/**
	 * Opens each specified Java element in a Java editor and navigates to the specified line
	 * number.
	 * 
	 * @param elements
	 *            the Java elements
	 * @param line
	 *            the line number
	 */
	private static void openJavaElements(Object[] elements, int line) {
		for (int i = 0; i < elements.length; i++) {
			Object ob = elements[i];
			if (ob instanceof IJavaElement) {
				IJavaElement element = (IJavaElement) ob;
				try {
					IEditorPart editorPart = JavaUI.openInEditor(element);
					gotoLine(editorPart, line, element);
				} catch (PartInitException e) {
					JDIDebugUIPlugin.log(e);
				} catch (JavaModelException e) {
					JDIDebugUIPlugin.log(e);
				}
			}
		}
	}

	/**
	 * Jumps to the given line in the editor if the line number lies within the given Java element.
	 * 
	 * @param editorPart
	 *            the Editor part
	 * @param line
	 *            the line to jump to
	 * @param element
	 *            the Java Element
	 * @throws JavaModelException
	 */
	private static void gotoLine(IEditorPart editorPart, int line, IJavaElement element) throws JavaModelException {
		if (line <= 0) {
			return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		try {
			if (element instanceof IMethod) {
				ISourceRange sourceRange = ((IMethod) element).getSourceRange();
				int start = sourceRange.getOffset();
				int end = start + sourceRange.getLength();
				start = document.getLineOfOffset(start);
				end = document.getLineOfOffset(end);
				if (start > line || end < line) {
					return;
				}
			}
			int start = document.getLineOffset(line - 1);
			editor.selectAndReveal(start, 0);
			IWorkbenchPage page = editor.getSite().getPage();
			page.activate(editor);
		} catch (BadLocationException e) {
			// ignore
		}
	}

	/**
	 * Opens an text input dialog to let the user refine the input text.
	 * 
	 * @param inputText
	 *            the input text
	 */
	private static void openInputEditDialog(String inputText) {
		IWorkbenchWindow window = JDIDebugUIPlugin.getActiveWorkbenchWindow();
		IInputValidator validator = new IInputValidator() {
			public String isValid(String newText) {
				return newText.length() == 0 ? "" : null; //$NON-NLS-1$
			}
		};
		InputDialog dialog = new InputDialog(window.getShell(), ActionMessages.OpenFromClipboardAction_OpenFromClipboard, ActionMessages.OpenFromClipboardAction_ElementToOpen, inputText, validator);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;

		inputText = dialog.getValue();
		handleSingleLineInput(inputText);
	}

	private static SearchPattern createSearchPattern(String s, int searchFor) {
		return SearchPattern.createPattern(s, searchFor, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
	}

	private static SearchRequestor createSearchRequestor(final List matches) {
		return new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				if (match.getAccuracy() == SearchMatch.A_ACCURATE)
					matches.add(match.getElement());
			}
		};
	}

	private static SearchParticipant[] createSearchParticipant() {
		return new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action .IAction,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui. IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
}
