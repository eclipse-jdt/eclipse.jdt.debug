/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.ui.PlatformUI;
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
	private static final String SIMPLE_NAME_PATTERN= "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"; //$NON-NLS-1$

	/**
	 * Pattern to match a qualified name e.g.
	 * <code>org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction</code>, or match a
	 * simple name e.g. <code>OpenFromClipboardAction</code>.
	 */
	private static final String QUALIFIED_NAME_PATTERN = "(" + SIMPLE_NAME_PATTERN //$NON-NLS-1$
			+ "\\.)*" + SIMPLE_NAME_PATTERN; //$NON-NLS-1$

	/**
	 * Pattern to match a qualified name e.g.
	 * <code>org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction</code>.
	 */
	private static final String STRICT_QUALIFIED_NAME_PATTERN = "(" + SIMPLE_NAME_PATTERN //$NON-NLS-1$
			+ "\\.)+" + SIMPLE_NAME_PATTERN; //$NON-NLS-1$

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
	 * <code> at org.eclipse.core.runtime.Assert.isLegal(Assert.java:41)</code>
	 */
	private static final String STACK_TRACE_LINE_PATTERN = ".*\\(" //$NON-NLS-1$
			+ WS + JAVA_FILE_LINE_PATTERN + WS + "\\)"; //$NON-NLS-1$

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

	/**
	 * Pattern to match a method e.g. <code>OpenFromClipboardAction#run(IAction)</code>,
	 * <code>Worker#run()</code>
	 */
	private static final String METHOD_JAVADOC_REFERENCE_PATTERN = QUALIFIED_NAME_PATTERN + "#" //$NON-NLS-1$
			+ SIMPLE_NAME_PATTERN + "\\(.*\\)"; //$NON-NLS-1$

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

	private static final int METHOD_JAVADOC_REFERENCE = 9;

	private static final String TASK_NAME = ActionMessages.OpenFromClipboardAction_OpeningFromClipboard;

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

	private static int getMatchingPattern(String s) {
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
		if (s.matches(METHOD_JAVADOC_REFERENCE_PATTERN))
			return METHOD_JAVADOC_REFERENCE;
		if (s.matches(QUALIFIED_NAME_PATTERN))
			return QUALIFIED_NAME;
		return INVALID;
	}

	private static void handleSingleLineInput(String inputText) {
		List<Object> matches = new ArrayList<Object>();
		try {
			int line= getJavaElementMatches(inputText, matches);
			handleMatches(matches, line, inputText);
		} catch (InterruptedException ex) {
			// Do nothing
		}
	}

	/**
	 * Parse the input text and search for the corresponding Java elements.
	 * 
	 * @param inputText the line number
	 * @param matches matched Java elements
	 * @return the line number
	 * @throws InterruptedException if canceled by the user
	 */
	private static int getJavaElementMatches(String inputText, List<Object> matches) throws InterruptedException {
		String s = inputText.trim();
		switch (getMatchingPattern(s)) {
		case JAVA_FILE_LINE: {
			int index = s.indexOf(':');
			String typeName = s.substring(0, index);
			typeName = s.substring(0, typeName.indexOf(".java")); //$NON-NLS-1$
			String lineNumber = s.substring(index + 1, s.length());
			lineNumber = lineNumber.trim();
			int line = (Integer.valueOf(lineNumber)).intValue();
			getTypeMatches(typeName, matches);
			return line;
		}
		case JAVA_FILE: {
			String typeName = s.substring(0, s.indexOf(".java")); //$NON-NLS-1$
			getTypeMatches(typeName, matches);
			return -1;
		}
		case TYPE_LINE: {
			int index = s.indexOf(':');
			String typeName = s.substring(0, index);
			typeName = typeName.trim();
			String lineNumber = s.substring(index + 1, s.length());
			lineNumber = lineNumber.trim();
			int line = (Integer.valueOf(lineNumber)).intValue();
			getTypeMatches(typeName, matches);
			return line;
		}
		case STACK_TRACE_LINE: {
			int index1 = s.indexOf('(');
			int index2 = s.indexOf(')');
			String typeLine = s.substring(index1 + 1, index2).trim();
			int index = typeLine.indexOf(':');
			String lineNumber = typeLine.substring(index + 1, typeLine.length()).trim();
			int line = (Integer.valueOf(lineNumber)).intValue();

			Pattern pattern = Pattern.compile(STRICT_QUALIFIED_NAME_PATTERN + WS + "\\("); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(s);
			if (matcher.find()) {
				String qualifiedName = matcher.group();
				index = qualifiedName.lastIndexOf('.');
				qualifiedName = qualifiedName.substring(0, index);
				getTypeMatches(qualifiedName, matches);
			} else {
				String typeName = typeLine.substring(0, index);
				typeName = typeLine.substring(0, typeName.indexOf(".java")); //$NON-NLS-1$
				getTypeMatches(typeName, matches);
			}
			return line;
		}
		case METHOD: {
			getMethodMatches(s, matches);
			return -1;
		}
		case STACK: {
			int index = s.indexOf(')');
			String method = s.substring(0, index + 1);
			index = s.indexOf(':');
			String lineNumber = s.substring(index + 1).trim();
			int line = (Integer.valueOf(lineNumber)).intValue();
			getMethodMatches(method, matches);
			return line;
		}
		case MEMBER:
			getMemberMatches(s.replace('#', '.'), matches);
			return -1;
		case METHOD_JAVADOC_REFERENCE:
			getMethodMatches(s.replace('#', '.'), matches);
			return -1;
		case QUALIFIED_NAME:
			getNameMatches(s, matches);
			return -1;
		default:
			return -1;
		}
	}

	/**
	 * Perform a Java search for the type and return the corresponding Java elements.
	 * 
	 * @param typeName the Type name
	 * @param matches matched Java elements
	 * @throws InterruptedException if canceled by the user
	 */
	private static void getTypeMatches(final String typeName, final List<Object> matches) throws InterruptedException {
		executeRunnable(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				doTypeSearch(typeName, matches, monitor);
			}
		});
	}

	/**
	 * Perform a Java search for methods and constructors and return the corresponding Java
	 * elements.
	 * 
	 * @param s the method pattern
	 * @param matches matched Java elements
	 * @throws InterruptedException if canceled by the user
	 */
	private static void getMethodMatches(final String s, final List<Object> matches) throws InterruptedException {
		executeRunnable(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				doMemberSearch(s, matches, true, true, false, monitor, 100);
			}
		});
	}

	/**
	 * Perform a Java search for fields, methods and constructors and return the corresponding Java
	 * elements.
	 * 
	 * @param s the member pattern
	 * @param matches matched Java elements
	 * @throws InterruptedException if canceled by the user
	 */
	private static void getMemberMatches(final String s, final List<Object> matches) throws InterruptedException {
		executeRunnable(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				doMemberSearch(s, matches, true, true, true, monitor, 100);
			}
		});
	}

	/**
	 * Perform a Java search for types, fields and methods and return the corresponding Java
	 * elements.
	 * 
	 * @param s the qualified name pattern
	 * @param matches matched Java elements
	 * @throws InterruptedException if canceled by the user
	 */
	private static void getNameMatches(final String s, final List<Object> matches) throws InterruptedException {
		executeRunnable(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SubMonitor progress = SubMonitor.convert(monitor, 100);
				progress.beginTask(TASK_NAME, 100);
				doTypeSearch(s, matches, progress.newChild(34));
				doMemberSearch(s, matches, true, false, true, progress.newChild(34), 66);
			}
		});
	}

	private static void executeRunnable(IRunnableWithProgress runnableWithProgress) throws InterruptedException {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnableWithProgress);
		} catch (InvocationTargetException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Handles the given matches.
	 * 
	 * @param matches matched Java elements
	 * @param line the line number
	 * @param inputText the input text
	 * @throws InterruptedException if canceled by the user
	 */
	private static void handleMatches(List<Object> matches, int line, String inputText) throws InterruptedException {
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
	 * @param editorPart the Editor part
	 * @param line the line to jump to
	 * @param element the Java Element
	 * @throws JavaModelException if fetching the Java element's source range fails
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
	 * @param inputText the input text
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
		return SearchPattern.createPattern(s, searchFor, IJavaSearchConstants.DECLARATIONS, getSearchFlags());
	}

	private static int getSearchFlags() {
		return SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
	}

	private static SearchRequestor createSearchRequestor(final List<Object> matches) {
		return new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) {
				if (match.getAccuracy() == SearchMatch.A_ACCURATE)
					matches.add(match.getElement());
			}
		};
	}

	private static SearchParticipant[] createSearchParticipant() {
		return new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
	}

	/**
	 * Perform a Java search for the type and return the corresponding Java elements.
	 * 
	 * <p>
	 * TODO: Because of faster performance SearchEngine.searchAllTypeNames(...) is used to do the
	 * Java Search, instead of the usual SearchEngine.search(...) API. This logic should be moved to
	 * JDT/Core.
	 * </p>
	 * 
	 * @param typeName
	 *            the Type Name
	 * @param matches
	 *            matched Java Elements
	 * @param monitor
	 *            the Progress Monitor
	 */
	private static void doTypeSearch(String typeName, final List<Object> matches, IProgressMonitor monitor) {
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchEngine searchEngine = new SearchEngine();

		String packageName = null;
		int index = typeName.lastIndexOf('.');
		if (index != -1) {
			packageName = typeName.substring(0, index);
			typeName = typeName.substring(index + 1);
		}
		try {
			searchEngine.searchAllTypeNames(packageName == null ? null : packageName.toCharArray(), packageName == null ? SearchPattern.R_EXACT_MATCH : getSearchFlags(), typeName.toCharArray(),
					getSearchFlags(), IJavaSearchConstants.TYPE, scope, new TypeNameMatchRequestor() {
						@Override
						public void acceptTypeNameMatch(TypeNameMatch match) {
							matches.add(match.getType());
						}
					}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * Perform a Java search for one or more of fields, methods and constructors and return the
	 * corresponding Java elements.
	 * 
	 * <p>
	 * TODO: Because of faster performance, if the type name is available
	 * SearchEngine.searchAllTypeNames(...) is used to narrow the scope of Java Search. This logic
	 * should be moved to JDT/Core.
	 * </p>
	 * 
	 * @param memberName
	 *            the Member Name
	 * @param matches
	 *            matched Java Elements
	 * @param searchForMethods
	 *            if <code>true</code>, a method search is performed
	 * @param searchForConstructors
	 *            if <code>true</code>, a constructor search is performed
	 * @param searchForFields
	 *            if <code>true</code>, a field search is performed
	 * @param monitor
	 *            the Progress Monitor
	 * @param work
	 *            the remaining Work
	 */
	private static void doMemberSearch(String memberName, final List<Object> matches, boolean searchForMethods, boolean searchForConstructors, boolean searchForFields, IProgressMonitor monitor, int work) {
		int noOfSearches = 0;
		noOfSearches = searchForMethods ? noOfSearches + 1 : noOfSearches;
		noOfSearches = searchForConstructors ? noOfSearches + 1 : noOfSearches;
		noOfSearches = searchForFields ? noOfSearches + 1 : noOfSearches;
		if (noOfSearches == 0) {
			return;
		}

		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask(TASK_NAME, work);

		IJavaSearchScope scope = null;
		SearchRequestor requestor = createSearchRequestor(matches);
		SearchEngine searchEngine = new SearchEngine();

		String typeName = null;
		int index = memberName.lastIndexOf('.');
		if (index != -1) {
			typeName = memberName.substring(0, index);
			memberName = memberName.substring(index + 1);
			final List<Object> typeMatches = new ArrayList<Object>();
			noOfSearches++;
			doTypeSearch(typeName, typeMatches, progress.newChild(work / noOfSearches));
			IType[] types = new IType[typeMatches.size()];
			for (int i = 0; i < typeMatches.size(); i++) {
				types[i] = (IType) typeMatches.get(i);
			}
			scope = SearchEngine.createJavaSearchScope(types);
		} else {
			scope = SearchEngine.createWorkspaceScope();
		}
		try {
			int workPerSearch = work / noOfSearches;
			if (searchForMethods) {
				doMemberSearch(searchEngine, memberName, IJavaSearchConstants.METHOD, scope, requestor, progress.newChild(workPerSearch));
			}
			if (searchForConstructors) {
				doMemberSearch(searchEngine, memberName, IJavaSearchConstants.CONSTRUCTOR, scope, requestor, progress.newChild(workPerSearch));
			}
			if (searchForFields) {
				doMemberSearch(searchEngine, memberName, IJavaSearchConstants.FIELD, scope, requestor, progress.newChild(workPerSearch));
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	private static void doMemberSearch(SearchEngine searchEngine, String memberName, int searchFor, IJavaSearchScope scope, SearchRequestor requestor, SubMonitor progressMonitor) throws CoreException {
		SearchPattern pattern = createSearchPattern(memberName, searchFor);
		if (pattern != null) {
			searchEngine.search(pattern, createSearchParticipant(), scope, requestor, progressMonitor);
		}
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
