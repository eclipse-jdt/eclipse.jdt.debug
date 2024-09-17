/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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

	private final TextConsole fConsole;
	private String originalHyperLink;

	/**
	 * Constructor
	 *
	 * @param console
	 *            the {@link TextConsole} this link detector is attached to
	 */
	public JavaStackTraceHyperlink(TextConsole console) {
		fConsole = console;
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
			originalHyperLink = linkText;
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
						MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Information_1, NLS.bind(ConsoleMessages.JavaStackTraceHyperlink_Source_not_found_for__0__2, new String[] {
								typeName }));
					} else {
						JDIDebugUIPlugin.statusDialog(ConsoleMessages.JavaStackTraceHyperlink_3, status);
					}
				} else if (source instanceof List) { // ambiguous results
					@SuppressWarnings("unchecked")
					List<Object> matches = (List<Object>) source;
					List<Object> exactMatchesFiltered = new ArrayList<>();
					String originalHyperLink2 = originalHyperLink;
					int firstMethodStartIndex = originalHyperLink2.indexOf('.');
					int firstMethodClosing = originalHyperLink2.lastIndexOf(')');

					if (firstMethodStartIndex != -1 && firstMethodClosing != -1) {
						String methodSignature = originalHyperLink2.substring(firstMethodStartIndex + 1, firstMethodClosing + 1).replaceAll(" ", ""); //$NON-NLS-1$//$NON-NLS-2$
						String methodNameExtracted = methodSignature.substring(0, methodSignature.indexOf('('));
						for (Object obj : matches) {
							if (obj instanceof IType type) {
								try {
									IMethod[] methods = type.getMethods();
									for (IMethod method : methods) {
										int indexOfClosing = method.toString().indexOf(')');
										int indexOfStart = method.toString().indexOf(method.getElementName());
										String methodName = method.toString().substring(indexOfStart, indexOfClosing + 1).replaceAll(" ", ""); //$NON-NLS-1$//$NON-NLS-2$
										int paramCount = methodSignature.substring(methodSignature.indexOf('(')
												+ 1, methodSignature.lastIndexOf(')')).split(",").length; //$NON-NLS-1$
										if (methodName.equals(methodSignature)) {
											exactMatchesFiltered.add(obj);
										} else if (methodNameExtracted.equals(method.getElementName())
												&& paramCount == method.getNumberOfParameters()) {
											// Further mining from fully qualified parameter names in method signature
											StringBuilder s = new StringBuilder(method.getElementName());
											s.append('(');
											String[] params = methodName.split(","); //$NON-NLS-1$
											for (String block : params) {
												if (block.contains("...")) { //$NON-NLS-1$ Parameter is var args
													if (params.length > 1) {
														String sub1 = block.substring(0, block.indexOf("...")); //$NON-NLS-1$
														sub1 = sub1.substring(sub1.lastIndexOf('.') + 1);
														s.append(sub1);
														s.append('.');
														s.append('.');
														s.append('.');
														s.append(',');
													}
												} else {
													if (block.indexOf('.') == -1) {
														s.append(block.substring(block.lastIndexOf('(') + 1));
														s.append(',');
													} else {
														s.append(block.substring(block.lastIndexOf('.') + 1));
														s.append(',');
													}
												}
											}
											s.deleteCharAt(s.length() - 1);

											if (s.charAt(s.length() - 1) != ')') {
												s.append(')');
											}
											if (s.toString().equals(methodSignature)) {
												exactMatchesFiltered.add(obj);
											}

											// If paramters includes innerclass
											if (methodSignature.indexOf('$') != -1) {
												StringBuilder newSignature = new StringBuilder(methodNameExtracted + "("); //$NON-NLS-1$
												String paramsExtracted = methodSignature.substring(methodSignature.indexOf('(')
														+ 1, methodSignature.indexOf(')'));
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
													if (newSignature.toString().equals(s.toString())) {
														exactMatchesFiltered.add(obj);
													}
												} else {
													String param = paramsExtracted.substring(paramsExtracted.indexOf('$') + 1);
													newSignature.append(param);
													newSignature.append(')');
													if (newSignature.toString().equals(s.toString())) {
														exactMatchesFiltered.add(obj);
													}

												}
											}
										}
									}
									if (exactMatchesFiltered.isEmpty()) {
										if (originalHyperLink2.indexOf('$') != -1) {

											IType[] inner = type.getTypes();
											while (inner.length > 0) {
												for (IType innerType : inner) {
													if (inner.length > 0) {
														inner = innerType.getTypes();
													}
													IMethod[] innerMethods = innerType.getMethods();
													for (IMethod innerMethod : innerMethods) {
														int indexOfClosing = innerMethod.toString().indexOf(')');
														int indexOfStart = innerMethod.toString().indexOf(innerMethod.getElementName());
														String methodName = innerMethod.toString().substring(indexOfStart, indexOfClosing
																+ 1).replaceAll(" ", ""); //$NON-NLS-1$//$NON-NLS-2$
														int paramCount = methodSignature.substring(methodSignature.indexOf('(')
																+ 1, methodSignature.lastIndexOf(')')).split(",").length; //$NON-NLS-1$
														if (methodName.equals(methodSignature)) {
															exactMatchesFiltered.add(obj);
														} else if (methodNameExtracted.equals(innerMethod.getElementName())
																&& paramCount == innerMethod.getNumberOfParameters()) {
															// Further mining from fully qualified parameter names in method signature
															StringBuilder s = new StringBuilder(innerMethod.getElementName());
															s.append('(');
															String[] params = methodName.split(","); //$NON-NLS-1$
															for (String block : params) {
																if (block.contains("...")) { //$NON-NLS-1$ Parameter is var args
																	if (params.length > 1) {
																		String sub1 = block.substring(0, block.indexOf("...")); //$NON-NLS-1$
																		sub1 = sub1.substring(sub1.lastIndexOf('.') + 1);
																		s.append(sub1);
																		s.append('.');
																		s.append('.');
																		s.append('.');
																		s.append(',');
																	}
																} else {
																	if (block.indexOf('.') == -1) {
																		s.append(block.substring(block.lastIndexOf('(') + 1));
																		s.append(',');
																	} else {
																		s.append(block.substring(block.lastIndexOf('.') + 1));
																		s.append(',');
																	}
																}
															}

															s.deleteCharAt(s.length() - 1);

															if (s.charAt(s.length() - 1) != ')') {
																s.append(')');
															}
															if (s.toString().equals(methodSignature)) {
																exactMatchesFiltered.add(obj);
															}
														}
													}
												}

											}

										}
									}

								} catch (JavaModelException e) {
									JDIDebugUIPlugin.log(e);
								}
							}
						}
					}
					int line = lineNumber + 1; // lineNumber starts with 0, but line with 1, see #linkActivated
					if (exactMatchesFiltered.size() == 1) {
						processSearchResult(exactMatchesFiltered.get(0), typeName, lineNumber);
						return Status.OK_STATUS;
					}
					if (exactMatchesFiltered.size() == 2) {
						try { // Occurred only in child eclipse

							if (exactMatchesFiltered.get(0) instanceof IType b1 && exactMatchesFiltered.get(1) instanceof IType b2) {

								if ((b1.getClass().getSimpleName().equals("BinaryType") && b2.getClass().getSimpleName().equals("BinaryType")) //$NON-NLS-1$ //$NON-NLS-2$
										&& (b1.getFullyQualifiedName().equals(b2.getFullyQualifiedName()))) {
									IVMInstall curr = JavaRuntime.getDefaultVMInstall();
									String vmPath = curr.getInstallLocation().getAbsolutePath();
									IVMInstall vmPath1 = JavaRuntime.getVMInstall(b1.getJavaProject());
									String path1 = vmPath1.getInstallLocation().getAbsolutePath();
									if (path1.equals(vmPath)) {
										processSearchResult(exactMatchesFiltered.get(0), typeName, lineNumber);
										return Status.OK_STATUS;
									}
									processSearchResult(exactMatchesFiltered.get(1), typeName, lineNumber);
									return Status.OK_STATUS;
								}
							}
							OpenFromClipboardAction.handleMatches(exactMatchesFiltered, line, typeName, ConsoleMessages.JavaDebugStackTraceHyperlink_dialog_title);
							return Status.OK_STATUS;

						} catch (CoreException e) {
							DebugUIPlugin.log(e);
							OpenFromClipboardAction.handleMatches(exactMatchesFiltered, line, typeName, ConsoleMessages.JavaDebugStackTraceHyperlink_dialog_title);
							return Status.OK_STATUS;
						}
					}
					OpenFromClipboardAction.handleMatches(exactMatchesFiltered, line, typeName, ConsoleMessages.JavaDebugStackTraceHyperlink_dialog_title);
					return Status.OK_STATUS;

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
}
