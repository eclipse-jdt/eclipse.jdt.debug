/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     SAP SE - Support hyperlinks for stack entries with method signature
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.console;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.OpenFromClipboardAction;
import org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A hyper-link from a stack trace line of the form "*(*.java:*)"
 */
public class JavaStackTraceHyperlink implements IHyperlink {
	final static String ANSI_ESCAPE_REGEX = "\033\\[[\\d;]*[A-HJKSTfimnsu]"; //$NON-NLS-1$
	private final TextConsole fConsole;
	private final AtomicReference<String> generatedLink;
	private static final String REGEX_FOR_NORMAL = "([a-zA-Z0-9\\$]+)\\.([a-zA-Z0-9]+)\\(([^)]*)\\)"; //$NON-NLS-1$
	private static final String REGEX_FOR_GENERICS = "([a-zA-Z0-9\\$]+(?:<[a-zA-Z0-9,<>]+>)?)\\.([a-zA-Z0-9]+)\\(([^)]*)\\)"; //$NON-NLS-1$
	private static final String REGEX_FOR_INNER_CLASS = "([a-zA-Z0-9\\$]+(?:\\([a-zA-Z0-9]+\\))?)\\.([a-zA-Z0-9]+)\\(([^)]*)\\)"; //$NON-NLS-1$
	private static final String METHOD_SIGNATURE_REGEX = "\\w+\\([^)]*\\)"; //$NON-NLS-1$
	private static final String METHOD_ARGUMENTS_REGEX = "\\(([^)]*)\\)"; //$NON-NLS-1$
	private static final String INNER_CLASS_ARGUMENTS_REGEX = "\\(([^)]+)\\)"; //$NON-NLS-1$
	/**
	 * Constructor
	 *
	 * @param console
	 *            the {@link TextConsole} this link detector is attached to
	 */
	public JavaStackTraceHyperlink(TextConsole console) {
		fConsole = console;
		generatedLink = new AtomicReference<>();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkEntered()
	 */
	@Override
	public void linkEntered() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkExited()
	 */
	@Override
	public void linkExited() {
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
	 */
	@Override
	public void linkActivated() {
		String typeName;
		int lineNumber;
		try {
			String linkText = getLinkText();
			linkText = linkText.replaceAll(ANSI_ESCAPE_REGEX, ""); //$NON-NLS-1$
			generatedLink.set(linkText);
			typeName = getTypeName(linkText);
			lineNumber = getLineNumber(linkText);
		} catch (CoreException e1) {
			ErrorDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Error, ConsoleMessages.JavaStackTraceHyperlink_Error, e1.getStatus());
			return;
		}

		// documents start at 0
		if (lineNumber > 0) {
			lineNumber--;
		}
		startSourceSearch(typeName, lineNumber);
	}

	/**
	 * Starts a search for the type with the given name. Reports back to 'searchCompleted(...)'.
	 *
	 * @param typeName
	 *            the type to search for
	 * @param lineNumber
	 *            the line number to open the editor on
	 */
	protected void startSourceSearch(final String typeName, final int lineNumber) {
		Job search = new Job(ConsoleMessages.JavaStackTraceHyperlink_2) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ILaunch launch = getLaunch();
				Object result = null;
				try {
					// search for the type in the workspace
					result = OpenTypeAction.findTypeInWorkspace(typeName, true);
					if (result == null && launch != null) {
						result = JavaDebugUtils.resolveSourceElement(JavaDebugUtils.generateSourceName(typeName), getLaunch());
					}
					if (result == null) {
						// search for all types in the workspace
						List<IType> types = findTypesInWorkspace(typeName);
						if (types.isEmpty()) {
							result = null;
						} else {
							if (types.size() == 1) {
								result = types.get(0);
							} else {
								result = types;
							}
						}
					}
					searchCompleted(result, typeName, lineNumber, null);
				} catch (CoreException e) {
					searchCompleted(null, typeName, lineNumber, e.getStatus());
				}
				return Status.OK_STATUS;
			}

		};
		search.schedule();
	}

	private static List<IType> findTypesInWorkspace(String typeName) throws CoreException {
		int dot = typeName.lastIndexOf('.');
		char[][] qualifications;
		String simpleName;
		if (dot != -1) {
			qualifications = new char[][] { typeName.substring(0, dot).toCharArray() };
			simpleName = typeName.substring(dot + 1);
		} else {
			qualifications = null;
			simpleName = typeName;
		}
		char[][] typeNames = new char[][] { simpleName.toCharArray() };
		List<IType> matchingTypes = new ArrayList<>();
		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				matchingTypes.add(match.getType());
			}
		};
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.searchAllTypeNames(qualifications, typeNames, SearchEngine.createWorkspaceScope(), requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		return matchingTypes;
	}

	/**
	 * Reported back to from {@link JavaStackTraceHyperlink#startSourceSearch(String, int)} when results are found
	 *
	 * @param source
	 *            the source object
	 * @param typeName
	 *            the fully qualified type name
	 * @param lineNumber
	 *            the line number in the type
	 * @param status
	 *            the error status or <code>null</code> if none
	 */
	protected void searchCompleted(final Object source, final String typeName, final int lineNumber, final IStatus status) {
		UIJob job = new UIJob("link search complete") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (source == null) {
					if (status == null) {
						// did not find source
						MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Information_1, NLS.bind(ConsoleMessages.JavaStackTraceHyperlink_Source_not_found_for__0__2,
								typeName));
					} else {
						JDIDebugUIPlugin.statusDialog(ConsoleMessages.JavaStackTraceHyperlink_3, status);
					}
				} else if (source instanceof List) { // ambiguous

					@SuppressWarnings("unchecked")
					List<Object> matches = (List<Object>) source;
					int line = lineNumber + 1; // lineNumber starts with 0, but line with 1, see #linkActivated
					String link = generatedLink.get();
					if (link == null) { // Handles invalid links (without line number)
						return openClipboard(matches, line, typeName);
					}

					try {
						return processAmbiguousResults(matches, typeName, lineNumber, link);
					} catch(Exception e) {
						StringBuilder temp = new StringBuilder();
						temp.append("Unable to parse \"" + link + "\" \n "); //$NON-NLS-1$ //$NON-NLS-2$
						temp.append(e.getClass().getSimpleName());
						exceptionHandler(temp.toString(), e);
					}

				} else {
					processSearchResult(source, typeName, lineNumber);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

	}

	/**
	 * Process received classes to extract exact class by mining methods
	 *
	 * @param matches
	 *            Initial match results
	 * @param typeName
	 *            the fully qualified type name
	 * @param line
	 *            the line number in the type
	 * @param link
	 *            the generated link from stack trace
	 * @return Returns the status of search results
	 */
	public IStatus processAmbiguousResults(List<Object> matches, String typeName, int line, String link) {
		List<Object> exactMatchesFiltered = new ArrayList<>();
		Pattern pattern = Pattern.compile(REGEX_FOR_NORMAL);
		Matcher matcher = pattern.matcher(link);
		String methodSignature = null;
		if (matcher.find()) {
			methodSignature = matcher.group(2) + "(" + matcher.group(3) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (methodSignature == null) {
			pattern = Pattern.compile(REGEX_FOR_GENERICS);
			matcher = pattern.matcher(link);
			if (matcher.find()) {
				methodSignature = matcher.group(2) + "(" + matcher.group(3) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (methodSignature == null) {
			pattern = Pattern.compile(REGEX_FOR_INNER_CLASS);
			matcher = pattern.matcher(link);
			if (matcher.find()) {
				methodSignature = matcher.group(2) + "(" + matcher.group(3) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (methodSignature == null) {
			return openClipboard(matches, line, typeName);
		}
		methodSignature = methodSignature.replace(" ", ""); //$NON-NLS-1$//$NON-NLS-2$ ;
		String methodNameExtracted = methodSignature.substring(0, methodSignature.indexOf('('));
		for (Object obj : matches) {
			if (filterClasses(obj, methodSignature, methodNameExtracted, link)) {
				exactMatchesFiltered.add(obj);
			}
		}
		if (exactMatchesFiltered.size() == 1) {
			processSearchResult(exactMatchesFiltered.get(0), typeName, line);
			return Status.OK_STATUS;
		} else if (exactMatchesFiltered.size() > 0) {
			return openClipboard(exactMatchesFiltered, ++line, typeName);
		} else {
			return openClipboard(matches, ++line, typeName);
		}
	}

	/**
	 * Handles exceptions details
	 *
	 * @param message
	 *            String regarding exception details
	 * @param e
	 *            Thrown exception
	 */
	public void exceptionHandler(String message, Exception e) {
		throw new RuntimeException(message, e);
	}

	/**
	 * Filter classes based on matching method name
	 *
	 * @param obj
	 *            Objects of initial results
	 * @param methodSignature
	 *            entire method declaration
	 * @param methodNameExtracted
	 *            method name
	 * @param link
	 *            Generated link from stack trace
	 * @return returns <code>true</code> if a found an exact method inside the class, or <code>false</code> if there's no matching methods
	 */
	private boolean filterClasses(Object obj, String methodSignature, String methodNameExtracted, String link) {
		if (obj instanceof IType type) {
			try {
				if (extractFromResults(type, methodSignature, methodNameExtracted)) {
					return true;
				} else if (link.indexOf('$') != -1) { // checks for inner class
					if (extractFromInnerClassResults(type.getTypes(), methodSignature, methodNameExtracted, link)) {
						return true;
					}
				}

			} catch (Exception e) {
				DebugUIPlugin.log(e);
				return false;
			}
		}
		return false;

	}

	/**
	 * Opens the clipboard action pop-up if there are multiple results
	 *
	 * @param results
	 *            Search results
	 * @param lineNumber
	 *            Line number from given stack trace
	 * @param type
	 *            Unqualified class name
	 * @return Returns a standard OK status with an "ok" message.
	 */
	private IStatus openClipboard(List<Object> results, int lineNumber, String type) {
		results = filterBinaryTypes(results, generatedLink.get());
		OpenFromClipboardAction.handleMatches(results, lineNumber, type, ConsoleMessages.JavaDebugStackTraceHyperlink_dialog_title);
		return Status.OK_STATUS;
	}

	/**
	 * Additional Filtering of classes if the IType has inner classes
	 *
	 * @param innerClass
	 *            Array of inner classes - IType[] arrays
	 * @param methodSignature
	 *            entire method declaration
	 * @param methodNameExtracted
	 *            method name
	 * @param link
	 *            Generated link from stack trace
	 * @throws JavaModelException
	 * @return returns <code>true</code> if a found an exact method inside the class, or <code>false</code> if there's no matching methods
	 */
	private boolean extractFromInnerClassResults(IType[] innerClass, String methodSignature, String methodNameExtracted, String link) throws JavaModelException {
		int innerClasslevel = innerClassLevels(link);
		int levelTravelled = 0;
		while (innerClass.length > 0) {
			for (IType innerType : innerClass) {
				if (innerClass.length > 0) {
					innerClass = innerType.getTypes();
				}
				levelTravelled++;
				if (extractFromResults(innerType, methodSignature, methodNameExtracted) && levelTravelled == innerClasslevel) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks the levels of inner class
	 *
	 * @param genLink
	 *            Generated link
	 * @return returns <code>count of levels</code>
	 */

	private int innerClassLevels(String genLink) {
		int level = 0;
		for (Character c : genLink.toCharArray()) {
			if (c == '$') {
				level++;
			}
		}
		return level;
	}

	/**
	 * Checks if there's any matching methods for the given IType
	 *
	 * @param type
	 *            Type of the matches
	 * @param methodSignature
	 *            entire method declaration
	 * @param methodNameExtracted
	 *            method name
	 * @throws JavaModelException
	 * @return returns <code>true</code> if a found an exact method inside the class, or <code>false</code> if there's no matching methods
	 */
	private boolean extractFromResults(IType type, String methodSignature, String methodNameExtracted) throws JavaModelException {
		if (methodSignature.indexOf('(') == -1 || methodSignature.lastIndexOf(')') == -1) {
			return false;
		}
		boolean paramDetailsNotIncluded = methodSignature.contains(".java:"); //$NON-NLS-1$
		try {
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (paramDetailsNotIncluded && method.getElementName().equals(methodNameExtracted)) {
					return true;
				}
				String methodDetails = method.toString();
				Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
				Matcher matcher = pattern.matcher(methodDetails);
				if (!matcher.find()) {
					return false;
				}
				String methodName = matcher.group();
				methodName = methodName.replace(" ", ""); //$NON-NLS-1$//$NON-NLS-2$
				pattern = Pattern.compile(METHOD_ARGUMENTS_REGEX);
				matcher = pattern.matcher(methodSignature);
				if (!matcher.find()) {
					return false;
				}
				int paramCount = matcher.group(1).split(",").length; //$NON-NLS-1$
				if (methodName.equals(methodSignature)) {
					return true;
				} else if (methodNameExtracted.equals(method.getElementName()) && paramCount == method.getNumberOfParameters()) {
					// Further mining from fully qualified parameter names in method signature
					String methodSignatureGen = methodSignatureGenerator(method.getElementName(), methodName);
					if (methodSignatureGen.equals(methodSignature)) {
						return true;
					}
					// If parameters includes innerclass
					if (methodSignature.indexOf('$') != -1) {
						String methodSignatureInnerClass = innerClassMethodSignatureGen(methodNameExtracted, methodSignature);
						if (methodSignatureInnerClass.equals(methodSignatureGen)) {
							return true;
						}
						String paramsExtracted = methodSignature.substring(methodSignature.indexOf('('));
						String param = paramsExtracted.substring(paramsExtracted.indexOf('$') + 1);
						methodSignatureInnerClass = methodSignatureInnerClass.concat(param);
						if (methodSignatureInnerClass.equals(methodSignatureGen)) {
							return true;
						}
					}
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Additional method signature generation for inner classes
	 *
	 * @param extractedMethodName
	 *            Extracted method name from input
	 * @param methodSignature
	 *            generated method signature
	 * @return returns generated <code>String</code> suitable for inner classes
	 */
	private String innerClassMethodSignatureGen(String extractedMethodName, String methodSignature) {
		StringBuilder newSignature = new StringBuilder(extractedMethodName + "("); //$NON-NLS-1$
		Pattern pattern = Pattern.compile(INNER_CLASS_ARGUMENTS_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		matcher.find();
		String paramsExtracted = matcher.group(1);
		if (paramsExtracted.indexOf(',') != -1) {
			String[] parameters = paramsExtracted.split(","); //$NON-NLS-1$
			for (String param : parameters) {
				newSignature.append(param.substring(param.indexOf('$') + 1));
				newSignature.append(","); //$NON-NLS-1$
			}
			newSignature.deleteCharAt(newSignature.length() - 1);
			if (newSignature.charAt(newSignature.length() - 1) != ')') {
				newSignature.append(')');
			}
		}
		return newSignature.toString();
	}

	/**
	 * Method signature generation for normal classes
	 *
	 * @param methodName
	 *            Extracted method name from input
	 * @param targetMethod
	 *            already extracted method signature
	 * @return returns generated <code>String</code> suitable for normal classes
	 */
	private String methodSignatureGenerator(String methodName, String targetMethod) {
		StringBuilder methodSignatureBuilder = new StringBuilder(methodName);
		methodSignatureBuilder.append('(');
		String[] params = targetMethod.split(","); //$NON-NLS-1$
		for (String block : params) {
			if (block.contains("...")) { //$NON-NLS-1$ Parameter is var args
				if (params.length > 1) {
					methodSignatureBuilder = varArgsParamBuilder(block, methodSignatureBuilder);
				}
			} else {
				if (block.indexOf('.') == -1) {
					methodSignatureBuilder.append(block.substring(block.lastIndexOf('(') + 1));
					methodSignatureBuilder.append(',');
				} else {
					methodSignatureBuilder.append(block.substring(block.lastIndexOf('.') + 1));
					methodSignatureBuilder.append(',');
				}
			}
		}
		methodSignatureBuilder.deleteCharAt(methodSignatureBuilder.length() - 1);

		if (methodSignatureBuilder.charAt(methodSignatureBuilder.length() - 1) != ')') {
			methodSignatureBuilder.append(')');
		}
		return methodSignatureBuilder.toString();
	}

	/**
	 * Variable argument's parameter creation
	 *
	 * @param parameter
	 *            Input parameter
	 * @param current
	 *            Initial parameters
	 * @return returns generated <code>Stringbuilder</code> for parameter
	 */
	private StringBuilder varArgsParamBuilder(String parameter, StringBuilder current) {
		String sub1 = parameter.substring(0, parameter.indexOf("...")); //$NON-NLS-1$
		sub1 = sub1.substring(sub1.lastIndexOf('.') + 1);
		current.append(sub1);
		current.append("...,"); //$NON-NLS-1$
		return current;
	}

	/**
	 * The search succeeded with the given result
	 *
	 * @param source
	 *            resolved source object for the search
	 * @param typeName
	 *            type name searched for
	 * @param lineNumber
	 *            line number on link
	 */
	protected void processSearchResult(Object source, String typeName, int lineNumber) {
		IDebugModelPresentation presentation = JDIDebugUIPlugin.getDefault().getModelPresentation();
		IEditorInput editorInput = presentation.getEditorInput(source);
		if (editorInput != null) {
			String editorId = presentation.getEditorId(editorInput, source);
			if (editorId != null) {
				try {
					IEditorPart editorPart = JDIDebugUIPlugin.getActivePage().openEditor(editorInput, editorId);
					if (editorPart instanceof ITextEditor && lineNumber >= 0) {
						ITextEditor textEditor = (ITextEditor) editorPart;
						IDocumentProvider provider = textEditor.getDocumentProvider();
						provider.connect(editorInput);
						IDocument document = provider.getDocument(editorInput);
						try {
							IRegion line = document.getLineInformation(lineNumber);
							textEditor.selectAndReveal(line.getOffset(), line.getLength());
						} catch (BadLocationException e) {

							MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_0, NLS.bind("{0}{1}{2}", new String[] { //$NON-NLS-1$
									(lineNumber + 1) + "", ConsoleMessages.JavaStackTraceHyperlink_1, typeName })); //$NON-NLS-1$
						}
						provider.disconnect(editorInput);
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.statusDialog(e.getStatus());
				}
			}
		}
	}

	/**
	 * Returns the launch associated with this hyper-link, or <code>null</code> if none
	 *
	 * @return the launch associated with this hyper-link, or <code>null</code> if none
	 */
	private ILaunch getLaunch() {
		IProcess process = (IProcess) getConsole().getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
		if (process != null) {
			return process.getLaunch();
		}
		return null;
	}

	/**
	 * Returns the fully qualified name of the type to open
	 *
	 * @param linkText
	 *            the complete text of the link to be parsed
	 * @return fully qualified type name
	 * @exception CoreException
	 *                if unable to parse the type name
	 */
	protected String getTypeName(String linkText) throws CoreException {
		int start = linkText.lastIndexOf('(');
		int end = linkText.indexOf(':');
		if (start >= 0 && end > start) {
			// linkText could be something like packageA.TypeB(TypeA.java:45)
			// need to look in packageA.TypeA for line 45 since TypeB is defined
			// in TypeA.java
			// Inner classes can be ignored because we're using file and line number

			// get File name (w/o .java)
			String typeName = linkText.substring(start + 1, end);
			typeName = JavaCore.removeJavaLikeExtension(typeName);
			typeName = removeModuleInfo(typeName);

			String qualifier = linkText.substring(0, start);
			// remove the method name
			start = qualifier.lastIndexOf('.');

			if (start >= 0) {
				// remove the class name
				start = new String((String) qualifier.subSequence(0, start)).lastIndexOf('.');
				if (start == -1) {
					start = 0; // default package
				}
			}

			if (start >= 0) {
				qualifier = qualifier.substring(0, start);
			}

			if (qualifier.length() > 0) {
				typeName = qualifier + "." + typeName; //$NON-NLS-1$
			}
			// Remove the module name if exists
			int index = typeName.lastIndexOf('/');
			if (index != -1) {
				typeName = typeName.substring(index + 1);
			}
			return typeName;
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null);
		throw new CoreException(status);
	}

	/**
	 * Returns the line number associated with the stack trace or -1 if none.
	 *
	 * @param linkText
	 *            the complete text of the link to be parsed
	 * @return the line number for the stack trace or -1 if one cannot be computed or has not been provided
	 * @exception CoreException
	 *                if unable to parse the number
	 */
	protected int getLineNumber(String linkText) throws CoreException {
		int index = linkText.lastIndexOf(':');
		if (index >= 0) {
			String numText = linkText.substring(index + 1);
			index = numText.indexOf(')');
			if (index >= 0) {
				numText = numText.substring(0, index);
			}
			try {
				return Integer.parseInt(numText);
			} catch (NumberFormatException e) {
				IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__6, e);
				throw new CoreException(status);
			}
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__6, null);
		throw new CoreException(status);
	}

	/**
	 * Returns the console this link is contained in.
	 *
	 * @return console
	 */
	protected TextConsole getConsole() {
		return fConsole;
	}

	/**
	 * Returns this link's text
	 *
	 * @return the complete text of the link, never <code>null</code>
	 * @exception CoreException
	 *                if unable to retrieve the text
	 */
	protected String getLinkText() throws CoreException {
		try {
			IDocument document = getConsole().getDocument();
			IRegion region = getConsole().getRegion(this);
			int regionOffset = region.getOffset();

			int lineNumber = document.getLineOfOffset(regionOffset);
			IRegion lineInformation = document.getLineInformation(lineNumber);
			int lineOffset = lineInformation.getOffset();
			String line = document.get(lineOffset, lineInformation.getLength());

			int regionOffsetInLine = regionOffset - lineOffset;

			int linkEnd = line.indexOf(')', regionOffsetInLine);
			int linkStart = line.lastIndexOf(' ', regionOffsetInLine);
			if (linkStart == -1) {
				linkStart = line.lastIndexOf('\t', regionOffsetInLine);
			}
			String extractedTrace = line.substring(linkStart == -1 ? 0 : linkStart + 1, linkEnd + 1).trim();
			if (extractedTrace.charAt(0) == '(' && line.startsWith("at")) { //$NON-NLS-1$
				int lastOpen = line.lastIndexOf('(');
				if (lastOpen > 0) {
					if (Character.isWhitespace(line.charAt(lastOpen - 1))) {
						extractedTrace = line.substring(0, lastOpen - 1).trim() + line.substring(lastOpen);
						linkStart = extractedTrace.lastIndexOf(' ', regionOffsetInLine);
						linkEnd = extractedTrace.indexOf(')', linkStart);
						return extractedTrace.substring(linkStart == -1 ? 0 : linkStart + 1, linkEnd + 1).trim();
					}
				}
			}
			return line.substring(linkStart == -1 ? 0 : linkStart + 1, linkEnd + 1).trim();
		} catch (BadLocationException e) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0, ConsoleMessages.JavaStackTraceHyperlink_Unable_to_retrieve_hyperlink_text__8, e);
			throw new CoreException(status);
		}
	}

	/**
	 * {@code jstack} can produce stack trace lines such as:
	 *
	 * <pre>
	 *     at java.util.StringJoiner.compactElts(java.base@11.0.10/StringJoiner.java:248)
	 * </pre>
	 *
	 * We remove the module part, since otherwise the type is not determined correctly by this class.
	 */
	private static String removeModuleInfo(String typeName) {
		int atIndex = typeName.lastIndexOf('@');
		int slashIndex = typeName.lastIndexOf('/');
		if (atIndex >= 0 && atIndex < slashIndex && slashIndex + 1 < typeName.length()) {
			typeName = typeName.substring(slashIndex + 1);
		}
		return typeName;
	}

	/**
	 * Intermediate method to filter Binary Types based on given version
	 *
	 * @return List of Objects with filtered Binary types
	 * @param extracted
	 *            Filtered or Processed Binary/Source Types
	 * @param link
	 *            Stack trace from the console
	 */
	private List<Object> filterBinaryTypes(List<Object> extracted, String link) {
		if (link != null) {
			List<Object> filteredResults = new ArrayList<>();
			int binaryInserted = 0;
			try {
				Pattern pattern = Pattern.compile("@(.*?)\\/"); //$NON-NLS-1$
				Matcher match = pattern.matcher(link);
				if (match.find()) {
					String jdkVersion = match.group(1);
					for (Object ob : extracted) {
						if (ob instanceof IType type && type.isBinary()) {
							IVMInstall installedJava = JavaRuntime.getVMInstall(type.getJavaProject());
							String jdkAvailable = installedJava.getInstallLocation().getAbsolutePath();
							if (jdkAvailable.contains(jdkVersion)) {
								filteredResults.add(ob);
								binaryInserted++;
							}
						} else {
							filteredResults.add(ob);
						}
					}
				}
			} catch (CoreException e) {
				return extracted;
			}
			if (binaryInserted == 0) {
				return extracted;
			}
			return filteredResults;
		}
		return extracted;
	}
}