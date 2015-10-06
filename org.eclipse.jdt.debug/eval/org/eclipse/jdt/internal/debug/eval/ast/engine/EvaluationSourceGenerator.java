/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/**
	 * Rebuild source in presence of external local variables
	 */
	public EvaluationSourceGenerator(String[] localVariableTypesNames,
			String[] localVariableNames, String codeSnippet) {
		fLocalVariableTypeNames = localVariableTypesNames;
		fLocalVariableNames = localVariableNames;
		fCodeSnippet = getCompleteSnippet(codeSnippet);
	}

	public EvaluationSourceGenerator(String codeSnippet) {
		this(new String[0], new String[0], codeSnippet);
	}

	/**
	 * Returns the completed codeSnippet by adding required semicolon and
	 * return 
	 */
	protected String getCompleteSnippet(String codeSnippet) {
		codeSnippet = codeSnippet.trim(); // remove whitespaces at the end
		boolean inString = false;
		byte[] chars = codeSnippet.getBytes();
		
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
		StringBuffer wordBuffer = new StringBuffer();
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
		String returnString = "return ";//$NON-NLS-1$
		// don't add return if it there in some condition
		int index = codeSnippet.lastIndexOf(returnString); 
		if (index > i){
			if (!Character.isWhitespace(chars[index-1]) || !(Character.isWhitespace(chars[index+6]) || chars[index+6] == '}'))
				wordBuffer.append(returnString); 
		} else if (chars[chars.length -1] !='}' && ( i+7 > chars.length || (i + 7 <= chars.length && !codeSnippet.substring(i, i+6).equals("return")))){ //$NON-NLS-1$
			// add return if last statement does not have return
				wordBuffer.append("return "); //$NON-NLS-1$
		}
		for (; i < chars.length; i++) {
			// copy the last statement
			wordBuffer.append(codeSnippet.charAt(i));
		}
		// add semicolon at the end if missing
		if (chars[chars.length -1] !=';' && chars[chars.length -1] !='}')
			wordBuffer.append(';');
		return wordBuffer.toString();
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
			if (JavaCore.ERROR.equals(value) || JavaCore.WARNING.equals(value)) {
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
