package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

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
	
	private String[] fImports;
	private int[] fLocalModifiers;
	private String[] fLocalTypesNames;
	private String[] fLocalVariables;
		
	
	private String fSource;
	private String fCompilationUnitName;
	private int fStartPosition;
	
	/**
	 * Rebuild source in presence of external local variables
	 */
	public EvaluationSourceGenerator(String[] imports, int[] localModifiers, String[] localTypesNames, String[] localVariables, String codeSnippet) {
		fImports = imports;
		fLocalModifiers = localModifiers;
		fLocalTypesNames = localTypesNames;
		fLocalVariables = localVariables;
	 	fCodeSnippet= codeSnippet;
	}
	
	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}
	
	public int getStartPosition() {
		return fStartPosition;
	}

	private void createEvaluationSourceFromSource(String source, int position, boolean isLineNumber) throws DebugException {
		CompilationUnit unit= AST.parseCompilationUnit(source.toCharArray());
		SourceBasedSourceGenerator visitor= new SourceBasedSourceGenerator(unit, position, isLineNumber, fLocalModifiers, fLocalTypesNames, fLocalVariables, fCodeSnippet);
		unit.accept(visitor);
		
		setSource(visitor.getSource());
		setCompilationUnitName(visitor.getCompilationUnitName());
		setStartPosition(visitor.getStartPosition());
	}
	
	private void createEvaluationSourceFromJDIObject(BinaryBasedSourceGenerator objectToEvaluationSourceMapper) throws DebugException {
		
		setCompilationUnitName(objectToEvaluationSourceMapper.getCompilationUnitName());
		setStartPosition(objectToEvaluationSourceMapper.getBlockStar());
		setSource(objectToEvaluationSourceMapper.getSource().insert(objectToEvaluationSourceMapper.getCodeSnippetPosition(), fCodeSnippet).toString());
	}
	
	private BinaryBasedSourceGenerator getInstanceSourceMapper(JDIObjectValue objectValue, boolean isInStaticMethod) throws DebugException {
		BinaryBasedSourceGenerator objectToEvaluationSourceMapper = new BinaryBasedSourceGenerator(fLocalModifiers, fLocalTypesNames, fLocalVariables, isInStaticMethod);
		objectToEvaluationSourceMapper.buildSource(objectValue);
		return objectToEvaluationSourceMapper;
	}
	
	private BinaryBasedSourceGenerator getStaticSourceMapper(JDIClassType classType, boolean isInStaticMethod) throws DebugException {
		BinaryBasedSourceGenerator objectToEvaluationSourceMapper = new BinaryBasedSourceGenerator(fLocalModifiers, fLocalTypesNames, fLocalVariables, isInStaticMethod);
		objectToEvaluationSourceMapper.buildSource(classType);
		return objectToEvaluationSourceMapper;
	}
			
	public String getSource(IJavaStackFrame frame) throws DebugException {
		if (fSource == null) {
			try {
				String baseSource= getSourceFromFrame(frame);
				if (baseSource != null) {
					createEvaluationSourceFromSource(baseSource,  frame.getLineNumber(), true);
				} else {
					JDIObjectValue object= (JDIObjectValue)frame.getThis();
					BinaryBasedSourceGenerator mapper;
					if (object != null) {
						// Class instance context
						mapper= getInstanceSourceMapper(object, ((JDIStackFrame)frame).getUnderlyingMethod().isStatic());
					} else {
						// Static context
						mapper= getStaticSourceMapper((JDIClassType)frame.getDeclaringType(), ((JDIStackFrame)frame).getUnderlyingMethod().isStatic());
					}
					createEvaluationSourceFromJDIObject(mapper);
				}
			} catch (JavaModelException e) {
				throw new DebugException(e.getStatus());
			}
		}
		return fSource;
	}
	
	public String getSource(IJavaObject thisObject, IJavaProject javaProject) throws DebugException  {
		if (fSource == null) {
			try {
				IType type= getTypeFromProject(thisObject.getJavaType().getName() ,javaProject);
				String baseSource= null;
				if (type != null) {
					ICompilationUnit compilationUnit= type.getCompilationUnit();
					if (compilationUnit != null) {
						baseSource= compilationUnit.getSource();
					} else {
						IClassFile  classFile= type.getClassFile();
						if (classFile != null) {
							baseSource= classFile.getSource();
						}
					}
				}
				if (baseSource == null) {
					BinaryBasedSourceGenerator mapper= getInstanceSourceMapper((JDIObjectValue) thisObject, false);
					createEvaluationSourceFromJDIObject(mapper);
				} else {
					createEvaluationSourceFromSource(baseSource, type.getSourceRange().getOffset(), false);
				}
			} catch(JavaModelException e) {
				throw new DebugException(e.getStatus());
			}
		}
		return fSource;
	}
	
	protected String getSourceFromFrame(IJavaStackFrame frame) throws JavaModelException {
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
	
	protected void setCompilationUnitName(String name) {
		fCompilationUnitName= name;
	}
	
	protected void setStartPosition(int position) {
		fStartPosition= position;
	}
	
	protected void setSource(String source) {
		fSource= source;
	}
	
	/**
	 * Returns the type associated with the specified
	 * name in this evaluation engine's associated Java project.
	 * 
	 * @param typeName fully qualified name of type, for
	 *  example, <code>java.lang.String</code>
	 * @return main type associated with source file
	 * @exception DebugException if:<ul>
	 * <li>the resolved type is an inner type</li>
	 * <li>unable to resolve a type</li>
	 * <li>a lower level Java exception occurs</li>
	 * </ul>
	 */
	private IType getTypeFromProject(String typeName, IJavaProject javaProject) throws DebugException {
		String path = typeName.replace('.', IPath.SEPARATOR);
		path+= ".java";			 //$NON-NLS-1$
		IPath sourcePath =  new Path(path);
		
		IType type = null;
		try {
			IJavaElement result = javaProject.findElement(sourcePath);
			String[] typeNames = getNestedTypeNames(typeName);
			if (result != null) {
				if (result instanceof IClassFile) {
					type = ((IClassFile)result).getType();
				} else if (result instanceof ICompilationUnit) {
					type = ((ICompilationUnit)result).getType(typeNames[0]);
				} else if (result instanceof IType) {
					type = ((IType)result);
				}
			}
			if (type != null) {
				for (int i = 1; i < typeNames.length; i++) {
					type = type.getType(typeNames[i]);
				}
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		
		return type;	
	}
	
	/**
	 * Returns an array of simple type names that are
	 * part of the given type's qualified name. For
	 * example, if the given name is <code>x.y.A$B</code>,
	 * an array with <code>["A", "B"]</code> is returned.
	 * 
	 * @param typeName fully qualified type name
	 * @return array of nested type names
	 */
	protected String[] getNestedTypeNames(String typeName) {
		int index = typeName.lastIndexOf('.');
		if (index >= 0) {
			typeName= typeName.substring(index + 1);
		}
		index = typeName.indexOf('$');
		ArrayList list = new ArrayList(1);
		while (index >= 0) {
			list.add(typeName.substring(0, index));
			typeName = typeName.substring(index + 1);
			index = typeName.indexOf('$');
		}
		list.add(typeName);
		return (String[])list.toArray(new String[list.size()]);	
	}	
}
