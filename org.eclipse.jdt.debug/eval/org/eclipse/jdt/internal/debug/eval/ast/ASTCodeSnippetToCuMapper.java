package org.eclipse.jdt.internal.debug.eval.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.List;
import java.util.Stack;

import org.eclipse.core.internal.resources.LocalMetaArea;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.eval.EvaluationConstants;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Maps back and forth a code snippet to a compilation unit.
 * The structure of the compilation unit is as follows:
 * [package <package name>;]
 * [import <import name>;]*
 * public class <code snippet class name> extends <global variable class name> {
 *   public void run() {
 *     <code snippet>
 *   }
 * }
 */
public class ASTCodeSnippetToCuMapper implements EvaluationConstants/*, IConstants*/ {

	private IJavaStackFrame fFrame;
	private String fCodeSnippet;
	
	private String[] fImports;
	private String fTypeName;
	private int[] fLocalModifiers;
	private String[] fLocalTypesNames;
	private String[] fLocalVariables;
		
	
	private String fSource;
	private String fCompilationUnitName;
	private int fStartPosition;
	
	/**
	 * Rebuild source in presence of external local variables
	 */
	public ASTCodeSnippetToCuMapper(String[] imports, String declaringTypeName, int[] localModifiers, String[] localTypesNames, String[] localVariables, IJavaStackFrame frame, String codeSnippet) {
		fImports = imports;
		fTypeName = declaringTypeName;
		fLocalModifiers = localModifiers;
		fLocalTypesNames = localTypesNames;
		fLocalVariables = localVariables;
		fFrame= frame;
	 	fCodeSnippet= codeSnippet;
	}
	
	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}
	
	public int getStartPosition() {
		return fStartPosition;
	}

	private void createMagicCodeFromSource(String source) throws DebugException {
		CompilationUnit unit= AST.parseCompilationUnit(source.toCharArray());
		ASTCuToMagicSourceMapper visitor= new ASTCuToMagicSourceMapper(unit, fFrame.getLineNumber(), fLocalModifiers, fLocalTypesNames, fLocalVariables, fCodeSnippet);
		unit.accept(visitor);
		
		setSource(visitor.getSource());
		setCompilationUnitName(visitor.getCompilationUnitName());
		setStartPosition(visitor.getStartPosition());
	}
	
	private void createMagicCodeFromJDIObject(JDIStackFrame frame) throws DebugException {
		JDIStackFrameToMagicSourceMapper objectToMagicSourceMapper = new JDIStackFrameToMagicSourceMapper(frame, fLocalModifiers, fLocalTypesNames, fLocalVariables);
		objectToMagicSourceMapper.buildSource();

		String codeSnippet = fCodeSnippet;		
		boolean isAnExpression= codeSnippet.indexOf(';') == -1 && codeSnippet.indexOf('{') == -1 && codeSnippet.indexOf('}') == -1 && codeSnippet.indexOf("return") == -1;

		if (isAnExpression) {
			codeSnippet = "return " + codeSnippet + ';';
		}
		
		setCompilationUnitName(objectToMagicSourceMapper.getCompilationUnitName());
		setStartPosition(objectToMagicSourceMapper.getBlockStar());
		setSource(objectToMagicSourceMapper.getSource().insert(objectToMagicSourceMapper.getCodeSnippetPosition(), codeSnippet).toString());
	}
			
	public String getSource() throws DebugException {
		if (fSource == null) {
			try {
				String baseSource= getSourceFormFrame();
				if (baseSource != null) {
					createMagicCodeFromSource(baseSource);
				} else {
					createMagicCodeFromJDIObject((JDIStackFrame) fFrame);
				}
			} catch (JavaModelException e) {
				throw new DebugException(e.getStatus());
			}
		}
		return fSource;
	}
	
	protected String getSourceFormFrame() throws JavaModelException {
		IJavaStackFrame frame= getJavaStackFrame(); 
		ILaunch launch= frame.getLaunch();
		if (launch == null) {
			return null;
		}
		ISourceLocator locator= launch.getSourceLocator();
		if (locator == null) {
			return null;
		}
		Object sourceElement= locator.getSourceElement(frame);
		if (sourceElement == null) {
			return null;
		}
		if (sourceElement instanceof IType) {
			return ((IType)sourceElement).getCompilationUnit().getSource();
		}
		if (sourceElement instanceof ICompilationUnit) {
			return ((ICompilationUnit)sourceElement).getSource();
		}
		return null;
	}
	
	protected IJavaStackFrame getJavaStackFrame() {
		return fFrame;
	}
	
	protected void setCompilationUnitName(String name) {
		fCompilationUnitName= name;
	}
	
	protected void setStartPosition(int position) {
		fStartPosition= position;
	}
	
	protected void setSource(String source) {
		fSource= source;
	}
	
//	/**
//	 * Returns the compilation unit associated with this
//	 * Java stack frame. Returns <code>null</code> for a binary
//	 * stack frame.
//	 */
//	protected ICompilationUnit getCompilationUnit(IJavaStackFrame frame) {
//		ILaunch launch= frame.getLaunch();
//		if (launch == null) {
//			return null;
//		}
//		ISourceLocator locator= launch.getSourceLocator();
//		if (locator == null) {
//			return null;
//		}
//		Object sourceElement= locator.getSourceElement(frame);
//		if (sourceElement instanceof IType) {
//			return (ICompilationUnit)((IType)sourceElement).getCompilationUnit();
//		}
//		if (sourceElement instanceof ICompilationUnit) {
//			return (ICompilationUnit)sourceElement;
//		}
//		return null;
//	}
	
	/**
	 * Returns a completion requestor that wraps the given requestor and shift the results
	 * according to the start offset and line number offset of the code snippet in the generated compilation unit. 
	 */
//	public ICompletionRequestor getCompletionRequestor(final ICompletionRequestor originalRequestor) {
//		final int startPosOffset = this.startPosOffset;
//		final int lineNumberOffset = this.lineNumberOffset;
//		return new ICompletionRequestor() {
//			public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd){
//				originalRequestor.acceptAnonymousType(superTypePackageName, superTypeName, parameterPackageNames, parameterTypeNames, parameterNames, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			
//			public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd) {
//				// Remove completion on generated class name or generated global variable class name
//				if (CharOperation.equals(packageName, ASTCodeSnippetToCuMapper.this.packageName) 
//						&& (CharOperation.equals(className, ASTCodeSnippetToCuMapper.this.className)
//							|| CharOperation.equals(className, ASTCodeSnippetToCuMapper.this.varClassName))) return;
//				originalRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptError(IMarker problemMarker) {
//	
//				try {
//					String attr = (String) problemMarker.getAttribute(IMarker.CHAR_START);
//					int start = Integer.parseInt(attr);
//					problemMarker.setAttribute(IMarker.CHAR_START, start - startPosOffset);	
//				} catch(CoreException e){
//				} catch(NumberFormatException e){
//				}
//				try {
//					String attr = (String) problemMarker.getAttribute(IMarker.CHAR_END);
//					int end = Integer.parseInt(attr);
//					problemMarker.setAttribute(IMarker.CHAR_END, end - startPosOffset);	
//				} catch(CoreException e){
//				} catch(NumberFormatException e){
//				}
//				try {
//					String attr = (String) problemMarker.getAttribute(IMarker.LINE_NUMBER);
//					int line = Integer.parseInt(attr);
//					problemMarker.setAttribute(IMarker.LINE_NUMBER, line - lineNumberOffset);	
//				} catch(CoreException e){
//				} catch(NumberFormatException e){
//				}
//				originalRequestor.acceptError(problemMarker);
//			}
//			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
//				originalRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
//				originalRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd) {
//				originalRequestor.acceptKeyword(keywordName, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptLabel(char[] labelName, int completionStart, int completionEnd) {
//				originalRequestor.acceptLabel(labelName, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd) {
//				originalRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
//				// Remove completion on generated method
//				if (CharOperation.equals(declaringTypePackageName, ASTCodeSnippetToCuMapper.this.packageName) 
//						&& CharOperation.equals(declaringTypeName, ASTCodeSnippetToCuMapper.this.className)
//						&& CharOperation.equals(selector, "run".toCharArray())) return; //$NON-NLS-1$
//				originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptMethodDeclaration(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
//				// Remove completion on generated method
//				if (CharOperation.equals(declaringTypePackageName, ASTCodeSnippetToCuMapper.this.packageName) 
//						&& CharOperation.equals(declaringTypeName, ASTCodeSnippetToCuMapper.this.className)
//						&& CharOperation.equals(selector, "run".toCharArray())) return;//$NON-NLS-1$
//				originalRequestor.acceptMethodDeclaration(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptModifier(char[] modifierName, int completionStart, int completionEnd) {
//				originalRequestor.acceptModifier(modifierName, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd) {
//				originalRequestor.acceptPackage(packageName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd) {
//				// Remove completion on generated class name or generated global variable class name
//				if (CharOperation.equals(packageName, ASTCodeSnippetToCuMapper.this.packageName) 
//						&& (CharOperation.equals(className, ASTCodeSnippetToCuMapper.this.className)
//							|| CharOperation.equals(className, ASTCodeSnippetToCuMapper.this.varClassName))) return;
//				originalRequestor.acceptType(packageName, typeName, completionName, completionStart - startPosOffset, completionEnd - startPosOffset);
//			}
//			public void acceptVariableName(char[] typePackageName, char[] typeName, char[] name, char[] completionName, int completionStart, int completionEnd){
//				originalRequestor.acceptVariableName(typePackageName, typeName, name, completionName, completionStart, completionEnd);
//			}
//		};
//	}
	
	/**
	 * Returns the type of evaluation that corresponds to the given line number in the generated compilation unit.
	 */
//	public int getEvaluationType(int lineNumber) {
//		int currentLine = 1;
//	
//		// check package declaration	
//		if (this.packageName != null && this.packageName.length != 0) {
//			if (lineNumber == 1) {
//				return EvaluationResult.T_PACKAGE;
//			}
//			currentLine++;
//		}
//	
//		// check imports
//		char[][] imports = this.imports;
//		if ((currentLine <= lineNumber) && (lineNumber < (currentLine + imports.length))) {
//			return EvaluationResult.T_IMPORT;
//		}
//		currentLine += imports.length + 1; // + 1 to skip the class declaration line
//	
//		// check generated fields
//		currentLine +=
//			(this.declaringTypeName == null ? 0 : 1) 
//			+ (this.localVarNames == null ? 0 : this.localVarNames.length);
//		if (currentLine > lineNumber) {
//			return EvaluationResult.T_INTERNAL;
//		}
//		currentLine ++; // + 1 to skip the method declaration line
//	
//		// check code snippet
//		if (currentLine >= this.lineNumberOffset) {
//			return EvaluationResult.T_CODE_SNIPPET;
//		}
//	
//		// default
//		return EvaluationResult.T_INTERNAL;
//	}
	/**
	 * Returns the import defined at the given line number. 
	 */
//	public char[] getImport(int lineNumber) {
//		int importStartLine = this.lineNumberOffset - 2 - this.imports.length;
//		return this.imports[lineNumber - importStartLine];
//	}
	/**
	 * Returns a selection requestor that wraps the given requestor and shift the problems
	 * according to the start offset and line number offset of the code snippet in the generated compilation unit. 
	 */
//	public ISelectionRequestor getSelectionRequestor(final ISelectionRequestor originalRequestor) {
//		final int startPosOffset = this.startPosOffset;
//		final int lineNumberOffset = this.lineNumberOffset;
//		return new ISelectionRequestor() {
//			public void acceptClass(char[] packageName, char[] className, boolean needQualification) {
//				originalRequestor.acceptClass(packageName, className, needQualification);
//			}
//			public void acceptError(IProblem error) {
//				error.setSourceLineNumber(error.getSourceLineNumber() - lineNumberOffset);
//				error.setSourceStart(error.getSourceStart() - startPosOffset);
//				error.setSourceEnd(error.getSourceEnd() - startPosOffset);
//				originalRequestor.acceptError(error);
//			}
//			public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name) {
//				originalRequestor.acceptField(declaringTypePackageName, declaringTypeName, name);
//			}
//			public void acceptInterface(char[] packageName, char[] interfaceName, boolean needQualification) {
//				originalRequestor.acceptInterface(packageName, interfaceName, needQualification);
//			}
//			public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames) {
//				originalRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);
//			}
//			public void acceptPackage(char[] packageName) {
//				originalRequestor.acceptPackage(packageName);
//			}
//		};
//	}
}
