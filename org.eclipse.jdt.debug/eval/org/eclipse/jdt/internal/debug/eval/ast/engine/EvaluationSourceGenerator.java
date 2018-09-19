/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Jesper Steen Moller - bug 341232
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;

/**
 * Creates the source code necessary to evaluate a code snippet. The
 * (simplified) structure of the source is as follows: [package <package name>;]
 * [import <import name>;]* public class
 * <code snippet class name> extends <global variable class name> {
 *   public void run() {
 *     <code snippet>
 *   }
 * }
 */
public class EvaluationSourceGenerator {

	private String fCodeSnippet;

	private String[] fLocalVariableTypeNames;
	private String[] fLocalVariableNames;

	private String fSource;
	private String fCompilationUnitName;
	private int fSnippetStartPosition;
	private int fRunMethodStartPosition;
	private int fRunMethodLength;
	private IJavaProject fJavaProject;

	/**
	 * Rebuild source in presence of external local variables
	 */
	public EvaluationSourceGenerator(String[] localVariableTypesNames,
			String[] localVariableNames, String codeSnippet, IJavaProject javaProject) {
		fLocalVariableTypeNames = localVariableTypesNames;
		fLocalVariableNames = localVariableNames;
		fJavaProject = javaProject;
		fCodeSnippet = getCompleteSnippet(codeSnippet);
	}

	public EvaluationSourceGenerator(String codeSnippet, IJavaProject javaProject) {
		this(new String[0], new String[0], codeSnippet, javaProject);
	}


	/**
	 * Returns the completed codeSnippet by adding required semicolon and
	 * return
	 */
	protected String getCompleteSnippet(String codeSnippet) {
		codeSnippet = codeSnippet.trim(); // remove whitespaces at the end
		boolean inString = false;
		char[] chars = codeSnippet.toCharArray();

		int semicolonIndex = -1;
		int lastSemilcolonIndex = -1;
		for (int i = 0, numChars = chars.length; i < numChars; i++) {
			switch (chars[i]) {
			case '\\':
				if (inString) { // skip the char after an escape char
					i++;
				}
				break;
			case '\"':
			case '\'':
				inString = !inString;
				break;
			case ';':
				if (!inString) { // mark the last 2 semicolon
					semicolonIndex = lastSemilcolonIndex;
					lastSemilcolonIndex = i;
				}
				break;
			}
		}
		StringBuilder wordBuffer = new StringBuilder();
		// if semicolon missing at the end
		if (lastSemilcolonIndex != chars.length-1)
			semicolonIndex = lastSemilcolonIndex;
		int i ;
		for (i=0; i < chars.length; i++) {
			// copy everything before the last statement or if whitespace
			if (i<= semicolonIndex || Character.isWhitespace(chars[i]) || chars[i] == '}'){
				wordBuffer.append(codeSnippet.charAt(i));
			}
			else
				break;
		}
		String lastSentence  = codeSnippet.substring(i);
		// don't add return if it there in some condition
		String returnString = "return "; //$NON-NLS-1$
		int index = codeSnippet.lastIndexOf(returnString);
		if (index == -1){
			if (needsReturn(lastSentence))
				wordBuffer.append(returnString);
		}
		else if (index > i){
			if (!Character.isWhitespace(chars[index-1]) || !(Character.isWhitespace(chars[index+6]) || chars[index+6] == '}')){
				if (needsReturn(lastSentence))
					wordBuffer.append(returnString);
			}
		} else if (chars[chars.length -1] !='}' && ( i+7 > chars.length || (i + 7 <= chars.length && !codeSnippet.substring(i, i+7).equals(returnString)))){
			// add return if last statement does not have return
			if (needsReturn(lastSentence))
				wordBuffer.append(returnString);
		}
		for (; i < chars.length; i++) {
			// copy the last statement
			wordBuffer.append(codeSnippet.charAt(i));
		}
		// add semicolon at the end if missing
		if (chars[chars.length -1] !=';' && chars[chars.length -1] !='}')
			wordBuffer.append(';');
		else if (chars[chars.length -1] !=';' && chars[chars.length -1] =='}'){
			int j = lastSentence.lastIndexOf('=') ;
			int k = lastSentence.lastIndexOf("==") ; //$NON-NLS-1$
			if ( j != -1 && (j!=k))
				wordBuffer.append(';');
		}
		return wordBuffer.toString();
	}


	/**
	 * Returns <code>true</code> if the snippet required return to be added, or
	 * <code>false</code> if the snippet does not require return to be added.
	 */

	private boolean needsReturn(String codeSnippet){
		if ( codeSnippet.length() == 0) {
			return false;
		}
		IScanner scanner = ToolFactory.createScanner(false, false, false, fJavaProject.getOption(JavaCore.COMPILER_SOURCE, true), fJavaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		scanner.setSource(codeSnippet.toCharArray());
		int token;
		try {
			token = scanner.getNextToken();
			int count = 0;
			while (token != ITerminalSymbols.TokenNameEOF) {
				if (count == 0 && (token == ITerminalSymbols.TokenNameIdentifier || token == ITerminalSymbols.TokenNameint || token == ITerminalSymbols.TokenNamefloat ||
						token == ITerminalSymbols.TokenNamedouble || token == ITerminalSymbols.TokenNameboolean || token == ITerminalSymbols.TokenNamechar ||
						token == ITerminalSymbols.TokenNameshort || token == ITerminalSymbols.TokenNamelong)) {
					int currentToken = token;
					token = scanner.getNextToken();
					if ( token == ITerminalSymbols.TokenNameEOF && currentToken == ITerminalSymbols.TokenNameIdentifier ){
						return true;
					}
					count = 1;
				}
				else if ( count == 0 && (token == ITerminalSymbols.TokenNamestatic || token == ITerminalSymbols.TokenNamefinal || token == ITerminalSymbols.TokenNamepackage ||
						token == ITerminalSymbols.TokenNameprivate || token == ITerminalSymbols.TokenNameprotected || token == ITerminalSymbols.TokenNamepublic)){
					token = scanner.getNextToken();
				}
				else if (count == 0 && (token == ITerminalSymbols.TokenNamethrow)){
					return false;
				}
				else if ( count == 0 && (token == ITerminalSymbols.TokenNameif || token == ITerminalSymbols.TokenNamewhile || token == ITerminalSymbols.TokenNamedo)) {
					return false;
				}
				else if (count ==1 && (token == ITerminalSymbols.TokenNameLBRACE || token == ITerminalSymbols.TokenNameEQUAL)){
					return true;
				}
				else if (count ==1 && (token == ITerminalSymbols.TokenNameLESS || token == ITerminalSymbols.TokenNameLBRACKET)){
					int currentToken = token;
					token = scanner.getNextToken();
					if ( currentToken == ITerminalSymbols.TokenNameLESS && ( token == ITerminalSymbols.TokenNameIdentifier || (token >= ITerminalSymbols.TokenNameIntegerLiteral && token <= ITerminalSymbols.TokenNameStringLiteral))){
						token = scanner.getNextToken();
						if (token == ITerminalSymbols.TokenNameEOF) {
							return true;
						}
					}
					count = 2;
				}
				else if (count ==2 && (token == ITerminalSymbols.TokenNameGREATER || token == ITerminalSymbols.TokenNameRBRACKET)){
					int currentToken = token;
					token = scanner.getNextToken();
					if ( token == ITerminalSymbols.TokenNameEOF && currentToken == ITerminalSymbols.TokenNameRBRACKET ){
						return true;
					}
					if ( currentToken == ITerminalSymbols.TokenNameGREATER && ( token == ITerminalSymbols.TokenNameIdentifier || (token >= ITerminalSymbols.TokenNameIntegerLiteral && token <= ITerminalSymbols.TokenNameStringLiteral))){
						token = scanner.getNextToken();
						if (token == ITerminalSymbols.TokenNameEOF) {
							return true;
						}
					}
					count = 3;
				}
				else if (count == 2)
					token = scanner.getNextToken();
				else if ( (count == 3 || count == 1 ) && token == ITerminalSymbols.TokenNameIdentifier ){
					 return false;
				}
				else {
					return true;
				}

			}
		}
		catch (InvalidInputException e) {
			// ignore
		}

		return true;
	}

	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}

	public int getSnippetStart() {
		return fSnippetStartPosition;
	}

	public int getRunMethodStart() {
		return fRunMethodStartPosition;
	}

	public int getRunMethodLength() {
		return fRunMethodLength;
	}

	protected void setSnippetStart(int position) {
		fSnippetStartPosition = position;
	}

	protected void setRunMethodStart(int position) {
		fRunMethodStartPosition = position;
	}

	protected void setRunMethodLength(int length) {
		fRunMethodLength = length;
	}

	public String getSnippet() {
		return fCodeSnippet;
	}

	private void createEvaluationSourceFromSource(String source, IType type,
			int line, boolean createInAStaticMethod, IJavaProject project)
			throws DebugException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		Map<String, String> options = getCompilerOptions(project);
		String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
		parser.setCompilerOptions(options);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		SourceBasedSourceGenerator visitor = new SourceBasedSourceGenerator(
				type, line, createInAStaticMethod, fLocalVariableTypeNames,
				fLocalVariableNames, fCodeSnippet, sourceLevel);
		unit.accept(visitor);

		if (visitor.hasError()) {
			throw new DebugException(new Status(IStatus.ERROR,
					JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK,
					visitor.getError(), null));
		}

		String sourceRes = visitor.getSource();
		if (sourceRes == null) {
			return;
		}
		setSource(sourceRes);
		setCompilationUnitName(visitor.getCompilationUnitName());
		setSnippetStart(visitor.getSnippetStart());
		setRunMethodStart(visitor.getRunMethodStart());
		setRunMethodLength(visitor.getRunMethodLength());
	}

	/**
	 * Returns the compiler options used for compiling the expression.
	 * <p>
	 * Turns all errors and warnings into ignore and disables task tags. The
	 * customizable set of compiler options only contains additional Eclipse
	 * options. The standard JDK compiler options can't be changed anyway.
	 *
	 * @param element
	 *            an element (not the Java model)
	 * @return compiler options
	 */
	public static Map<String, String> getCompilerOptions(IJavaProject project) {
		Map<String, String> options = project.getOptions(true);
		for (Iterator<String> iter = options.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			String value = options.get(key);
			if (JavaCore.ERROR.equals(value) || JavaCore.WARNING.equals(value) || JavaCore.INFO.equals(value)) {
				options.put(key, JavaCore.IGNORE);
			}
		}
		options.put(JavaCore.COMPILER_TASK_TAGS, ""); //$NON-NLS-1$
		return options;
	}

	private void createEvaluationSourceFromJDIObject(
			BinaryBasedSourceGenerator objectToEvaluationSourceMapper) {

		setCompilationUnitName(objectToEvaluationSourceMapper
				.getCompilationUnitName());
		setSnippetStart(objectToEvaluationSourceMapper.getSnippetStart());
		setRunMethodStart(objectToEvaluationSourceMapper.getRunMethodStart());
		setRunMethodLength(fCodeSnippet.length()
				+ objectToEvaluationSourceMapper.getRunMethodLength());
		setSource(objectToEvaluationSourceMapper
				.getSource()
				.insert(objectToEvaluationSourceMapper.getCodeSnippetPosition(),
						fCodeSnippet).toString());
	}

	private BinaryBasedSourceGenerator getInstanceSourceMapper(
			JDIReferenceType referenceType, boolean isInStaticMethod,
			IJavaProject project) {
		String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
		BinaryBasedSourceGenerator objectToEvaluationSourceMapper = new BinaryBasedSourceGenerator(
				fLocalVariableTypeNames, fLocalVariableNames, isInStaticMethod,
				sourceLevel);
		objectToEvaluationSourceMapper.buildSource(referenceType);
		return objectToEvaluationSourceMapper;
	}

	public String getSource(IJavaReferenceType type, int line, IJavaProject javaProject,
			boolean isStatic) throws CoreException {
		if (fSource == null) {
			IType iType = JavaDebugUtils.resolveType(type);
			if (iType != null && !iType.isInterface()) {
				String baseSource = null;
				if (iType.isBinary()) {
					baseSource = iType.getClassFile().getSource();
				} else {
					baseSource = iType.getCompilationUnit().getSource();
				}
				if (baseSource != null) {
					createEvaluationSourceFromSource(baseSource, iType,
							line, isStatic, javaProject);
				}
			}
			if (fSource == null) {
				BinaryBasedSourceGenerator mapper = getInstanceSourceMapper(
						(JDIReferenceType) type, isStatic, javaProject);
				createEvaluationSourceFromJDIObject(mapper);
			}
		}
		return fSource;
	}

	protected void setCompilationUnitName(String name) {
		fCompilationUnitName = name;
	}

	protected void setSource(String source) {
		fSource = source;
	}

}
