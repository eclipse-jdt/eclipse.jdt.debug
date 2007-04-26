/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;

/**
 * Creates the source code necessary to evaluate a code snippet.
 * The (simplified) structure of the source is as follows:
 * [package <package name>;]
 * [import <import name>;]*
 * public class <code snippet class name> extends <global variable class name> {
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
	public EvaluationSourceGenerator(String[] localVariableTypesNames, String[] localVariableNames, String codeSnippet) {
		fLocalVariableTypeNames = localVariableTypesNames;
		fLocalVariableNames = localVariableNames;
	 	fCodeSnippet= getCompleteSnippet(codeSnippet);
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
	 * Returns whether the given snippet represents an expression.
	 * This is determined by examining the snippet for non-quoted semicolons.
	 * 
	 * Returns <code>true</code> if the snippet is an expression, or
	 * <code>false</code> if the expresssion contains a statement.
	 */
	protected boolean isExpression(String codeSnippet) {
		boolean inString= false;
		byte[] chars= codeSnippet.getBytes();
		for (int i= 0, numChars= chars.length; i < numChars; i++) {
			switch (chars[i]) {
				case '\\':
					if (inString) { // skip the char after an escape char
						i++;
					}
					break;
				case '\"':
				case '\'':
					inString= !inString;
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
		fSnippetStartPosition= position;
	}
	protected void setRunMethodStart(int position) {
		fRunMethodStartPosition= position;
	}
	protected void setRunMethodLength(int length) {
		fRunMethodLength= length;
	}
	
	public String getSnippet() {
		return fCodeSnippet;
	}

	private void createEvaluationSourceFromSource(String source, String typeName, int position, boolean createInAStaticMethod, IJavaProject project) throws DebugException {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		Map options=JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, project.getOption(JavaCore.COMPILER_COMPLIANCE, true));
		String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
		options.put(JavaCore.COMPILER_SOURCE, sourceLevel);
		parser.setCompilerOptions(options);
		CompilationUnit unit= (CompilationUnit)parser.createAST(null);
		SourceBasedSourceGenerator visitor= new SourceBasedSourceGenerator(unit, typeName, position, createInAStaticMethod, fLocalVariableTypeNames, fLocalVariableNames, fCodeSnippet, sourceLevel);
		unit.accept(visitor);
		
		if (visitor.hasError()) {
			throw new DebugException(new Status(IStatus.ERROR, JDIDebugPlugin.getUniqueIdentifier(), IStatus.OK, visitor.getError(), null));
		}
		
		String sourceRes= visitor.getSource();
		if (sourceRes == null) {
			return;
		}
		setSource(sourceRes);
		setCompilationUnitName(visitor.getCompilationUnitName());
		setSnippetStart(visitor.getSnippetStart());
		setRunMethodStart(visitor.getRunMethodStart());
		setRunMethodLength(visitor.getRunMethodLength());
	}
	
	private void createEvaluationSourceFromJDIObject(BinaryBasedSourceGenerator objectToEvaluationSourceMapper) {
		
		setCompilationUnitName(objectToEvaluationSourceMapper.getCompilationUnitName());
		setSnippetStart(objectToEvaluationSourceMapper.getSnippetStart());
		setRunMethodStart(objectToEvaluationSourceMapper.getRunMethodStart());
		setRunMethodLength(fCodeSnippet.length() + objectToEvaluationSourceMapper.getRunMethodLength());
		setSource(objectToEvaluationSourceMapper.getSource().insert(objectToEvaluationSourceMapper.getCodeSnippetPosition(), fCodeSnippet).toString());
	}
	
	private BinaryBasedSourceGenerator getInstanceSourceMapper(JDIReferenceType referenceType, boolean isInStaticMethod, IJavaProject project) {
		String sourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
		BinaryBasedSourceGenerator objectToEvaluationSourceMapper = new BinaryBasedSourceGenerator(fLocalVariableTypeNames, fLocalVariableNames, isInStaticMethod, sourceLevel);
		objectToEvaluationSourceMapper.buildSource(referenceType);
		return objectToEvaluationSourceMapper;
	}
	
	public String getSource(IJavaReferenceType type, IJavaProject javaProject, boolean isStatic) throws CoreException {
		if (fSource == null) {
			String baseSource= getTypeSourceFromProject(type, javaProject);
			int lineNumber= getLineNumber((JDIReferenceType)type);
			if (baseSource != null && lineNumber != -1) {
				createEvaluationSourceFromSource(baseSource, type.getName(), lineNumber, isStatic, javaProject);
			}
			if (fSource == null) {
				BinaryBasedSourceGenerator mapper= getInstanceSourceMapper((JDIReferenceType) type, isStatic, javaProject);
				createEvaluationSourceFromJDIObject(mapper);
			}
		}
		return fSource;
	}

	private int getLineNumber(JDIReferenceType type) {
		ReferenceType referenceType= (ReferenceType) type.getUnderlyingType();
		try {
			List allLineLocations = referenceType.allLineLocations();
			if (!allLineLocations.isEmpty()) {
				return ((Location)allLineLocations.get(0)).lineNumber();
			}
		} catch (AbsentInformationException e) {
		}
		return -1;
	}

	protected void setCompilationUnitName(String name) {
		fCompilationUnitName= name;
	}
	
	protected void setSource(String source) {
		fSource= source;
	}

	private String getTypeSourceFromProject(IJavaReferenceType type, IJavaProject javaProject) throws CoreException {
		String[] sourcePaths = type.getSourcePaths(null);
		IJavaElement element = null;
		if (sourcePaths != null && sourcePaths.length > 0) {
			element = javaProject.findElement(new Path(sourcePaths[0]));
		} else {
			// must guess at source name when debug attribute not present
			element = JavaDebugUtils.findElement(type.getName(), javaProject);
		}	
		return resolveSource(element);
	}
	
	/**
	 * Returns source for the given class file or compilation unit
	 * or <code>null</code> if none.
	 *  
	 * @param element Java element or <code>null</code> 
	 */
	private String resolveSource(IJavaElement element) throws DebugException {
		String source= null;
		try {
			if (element instanceof IClassFile) {
				source = ((IClassFile)element).getSource();
			} else if (element instanceof ICompilationUnit) {
				source = ((ICompilationUnit)element).getSource();
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		return source;			
	}

}
