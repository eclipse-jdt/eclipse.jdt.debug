package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

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
	 	fCodeSnippet= getCompleteSnippet(codeSnippet);
	}

	protected String getCompleteSnippet(String codeSnippet) {
		boolean isAnExpression= codeSnippet.indexOf(';') == -1 && codeSnippet.indexOf('{') == -1 && codeSnippet.indexOf('}') == -1 && codeSnippet.indexOf("return") == -1; //$NON-NLS-1$

		if (isAnExpression) {
			codeSnippet = "return " + codeSnippet + ';'; //$NON-NLS-1$
		}
		return codeSnippet;
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
				String baseSource= getTypeSourceFromProject(thisObject.getJavaType().getName(), javaProject);
				int lineNumber= getLineNumber((JDIObjectValue) thisObject);
				if (baseSource == null || lineNumber == -1) {
					BinaryBasedSourceGenerator mapper= getInstanceSourceMapper((JDIObjectValue) thisObject, false);
					createEvaluationSourceFromJDIObject(mapper);
				} else {
					createEvaluationSourceFromSource(baseSource, lineNumber, true);
				}
		}
		return fSource;
	}

	private int getLineNumber(JDIObjectValue objectValue) {
		ReferenceType referenceType= objectValue.getUnderlyingObject().referenceType();
		String referenceTypeName= referenceType.name();
		Location location;
		Hashtable lineNumbers= new Hashtable();
		try {
			for (Iterator iterator = referenceType.allLineLocations().iterator(); iterator.hasNext();) {
				lineNumbers.put(new Integer(((Location)iterator.next()).lineNumber()), this);
			}
			for (Iterator iterator = referenceType.allLineLocations().iterator(); iterator.hasNext();) {
				location= (Location)iterator.next();
				if (!location.declaringType().name().equals(referenceTypeName)) {
					lineNumbers.remove(new Integer(((Location)iterator.next()).lineNumber()));
				}
			}
			if (lineNumbers.size() > 0) {
				return ((Integer)lineNumbers.keys().nextElement()).intValue();
			}
			return -1;
		} catch(AbsentInformationException e) {
			return -1;
		}
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

	private String getTypeSourceFromProject(String typeName, IJavaProject javaProject) throws DebugException {
		String path = typeName;
		int pos = path.indexOf('$');
		if (pos != -1) {
			path= path.substring(0, pos);
		}
		pos++;
		path = path.replace('.', IPath.SEPARATOR);
		path+= ".java";			 //$NON-NLS-1$
		IPath sourcePath =  new Path(path);
		
		String source= null;
		try {
			IJavaElement result = javaProject.findElement(sourcePath);
			if (result != null) {
				if (result instanceof IClassFile) {
					source = ((IClassFile)result).getSource();
				} else if (result instanceof ICompilationUnit) {
					source = ((ICompilationUnit)result).getSource();
				}
			}
		} catch (JavaModelException e) {
			throw new DebugException(e.getStatus());
		}
		
		return source;	
	}

}
