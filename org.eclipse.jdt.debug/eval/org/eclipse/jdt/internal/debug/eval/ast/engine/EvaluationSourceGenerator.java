/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
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

	protected String getCompleteSnippet(String codeSnippet) {

		if (isExpression(codeSnippet)) {
			codeSnippet = "return " + codeSnippet + ';'; //$NON-NLS-1$
		}
		return codeSnippet;
	}

	/**
	 * Returns whether the given snippet represents an expression. This is
	 * determined by examining the snippet for non-quoted semicolons.
	 * 
	 * Returns <code>true</code> if the snippet is an expression, or
	 * <code>false</code> if the expression contains a statement.
	 */
	protected boolean isExpression(String codeSnippet) {
		boolean inString = false;
		byte[] chars = codeSnippet.getBytes();
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
				if (!inString) {
					return false;
				}
				break;
			}
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
			boolean createInAStaticMethod, IJavaProject project)
			throws DebugException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		Map<String, String> options = getCompilerOptions(project);
		String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
		parser.setCompilerOptions(options);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		SourceBasedSourceGenerator visitor = new SourceBasedSourceGenerator(
				type, createInAStaticMethod, fLocalVariableTypeNames,
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

	public String getSource(IJavaReferenceType type, IJavaProject javaProject,
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
							isStatic, javaProject);
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
