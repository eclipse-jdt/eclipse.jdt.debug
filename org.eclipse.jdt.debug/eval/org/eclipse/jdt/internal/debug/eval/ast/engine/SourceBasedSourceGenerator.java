/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class SourceBasedSourceGenerator extends ASTVisitor  {
	
	private static final String RUN_METHOD_NAME= "___run"; //$NON-NLS-1$
	private static final String EVAL_METHOD_NAME= "___eval"; //$NON-NLS-1$
	private static final String EVAL_FIELD_NAME= "___field"; //$NON-NLS-1$
	
	private int[] fLocalModifiers;
	private String[] fLocalTypesNames;
	private String[] fLocalVariables;
	private String fCodeSnippet;
		
	private boolean fRightTypeFound;
	
	private boolean fIsInAStaticMethod;
	
	private boolean fEvaluateNextEndTypeDeclaration;
	
	private CompilationUnit fUnit;
	
	private int fPosition;
	
	private boolean fIsLineNumber;
	
	private StringBuffer fSource;
	
	private String fLastTypeName;
	
	private String fCompilationUnitName;
	
	private int fStartPosOffset;
	
	public SourceBasedSourceGenerator(CompilationUnit unit, int position, boolean isLineNumber, int[] localModifiers, String[] localTypesNames, String[] localVariables, String codeSnippet) {
		fRightTypeFound= false;
		fUnit= unit;
		fPosition= position;
		fLocalModifiers= localModifiers;
		fLocalTypesNames= localTypesNames;
		fLocalVariables= localVariables;
		fCodeSnippet= codeSnippet;
		fIsInAStaticMethod= false;
		fIsLineNumber= isLineNumber;
	}
	
	public String getSource() {
		return fSource.toString();
	}
	
	private CompilationUnit getCompilationUnit() {
		return fUnit;
	}
	
	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}
	
	public int getStartPosition() {
		return fStartPosOffset;
	}
	
	private int getPosition() {
		return fPosition;
	}
	
	private int getCorrespondingLineNumber(int charOffset) {
		return getCompilationUnit().lineNumber(charOffset);
	}
	
	private boolean rightTypeFound() {
		return  fRightTypeFound;
	}
	
	private void setRightTypeFound(boolean value) {
		fRightTypeFound= value;
	}
	
	private boolean isInAStaticMethod() {
		return fIsInAStaticMethod;
	}
	
	private void setIsInAStaticMethod(boolean value) {
		fIsInAStaticMethod= value;
	}
	
	private StringBuffer buildRunMethod(List bodyDeclarations) {
		StringBuffer buffer = new StringBuffer();

		if (isInAStaticMethod()) {
			buffer.append("static "); //$NON-NLS-1$
		}

		buffer.append("void "); //$NON-NLS-1$
		buffer.append(getUniqueMethodName(RUN_METHOD_NAME, bodyDeclarations));
		buffer.append('(');
		for(int i= 0, length= fLocalModifiers.length; i < length; i++) {
			if (fLocalModifiers[i] != 0) {
				buffer.append(Flags.toString(fLocalModifiers[i]));
				buffer.append(' ');
			}
			buffer.append(getDotName(fLocalTypesNames[i]));
			buffer.append(' ');
			buffer.append(fLocalVariables[i]);
			if (i + 1 < length)
				buffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(") throws Throwable {"); //$NON-NLS-1$
		buffer.append('\n');
		fStartPosOffset= buffer.length() - 2;
		String codeSnippet= new String(fCodeSnippet).trim();
		
		buffer.append(codeSnippet);

		buffer.append('\n');
		buffer.append('}').append('\n');
		return buffer;
	}
	
	private String getDotName(String typeName) {
		return typeName.replace('$', '.');
	}
	
	private boolean containsLine(ASTNode node) {
		int position= getPosition();
		if (fIsLineNumber) {
			int startLineNumber= getCorrespondingLineNumber(node.getStartPosition());
			int endLineNumber= getCorrespondingLineNumber(node.getStartPosition() + node.getLength() - 1);
			return startLineNumber <= position && position <= endLineNumber;
		} else {
			int startPosition= node.getStartPosition();
			return startPosition <= position && position <= startPosition + node.getLength();
		}
	}
	
	private StringBuffer buildTypeBody(StringBuffer buffer, List list) {
		StringBuffer source = new StringBuffer();
		
		source.append('{').append('\n');
		if (buffer != null) {
			fStartPosOffset += source.length();
			source.append(buffer);
		}
		for (Iterator iterator= list.iterator(); iterator.hasNext();) {
			BodyDeclaration bodyDeclaration= (BodyDeclaration) iterator.next();
			if (bodyDeclaration instanceof FieldDeclaration) {
				source.append(buildFieldDeclaration((FieldDeclaration) bodyDeclaration));
			} else if (bodyDeclaration instanceof MethodDeclaration) {
				source.append(buildMethodDeclaration((MethodDeclaration) bodyDeclaration));
			} else if (bodyDeclaration instanceof TypeDeclaration) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclaration;
				if (!typeDeclaration.getName().getIdentifier().equals(fLastTypeName)) {
					source.append(buildTypeDeclaration(null, (TypeDeclaration) bodyDeclaration));
				}
			}
		}
		source.append('}').append('\n');
		
		return source;
	}
	
	private StringBuffer buildFieldDeclaration(FieldDeclaration fieldDeclaration) {
		StringBuffer source = new StringBuffer();
		
		source.append(Flags.toString(fieldDeclaration.getModifiers()));
		source.append(' ');
		source.append(getDotName(getTypeName(fieldDeclaration.getType())));
		source.append(' ');
		
		boolean first= true;
		for (Iterator iterator= fieldDeclaration.fragments().iterator(); iterator.hasNext();) {
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment) iterator.next();
			if (first) {
				first = false;
			} else {
				source.append(',');
			}
			source.append(variableDeclarationFragment.getName().getIdentifier());
			for (int i= 0, dim= variableDeclarationFragment.getExtraDimensions(); i < dim; i++) {
				source.append('[').append(']');
			}
		}
		
		source.append(';').append('\n');
		
		return source;
	}
	
	private StringBuffer buildMethodDeclaration(MethodDeclaration methodDeclaration) {
		StringBuffer source = new StringBuffer();
		int modifiers= methodDeclaration.getModifiers();
		source.append(Flags.toString(modifiers));
		source.append(' ');
		
		boolean isConstructor= methodDeclaration.isConstructor();
		
		if (!isConstructor) {
			source.append(getDotName(getTypeName(methodDeclaration.getReturnType())));
			source.append(' ');
		}
		
		source.append(methodDeclaration.getName().getIdentifier());
		source.append(' ').append('(');
		
		boolean first= true;
		for (Iterator iterator = methodDeclaration.parameters().iterator(); iterator.hasNext();) {
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) iterator.next();
			if (first) {
				first = false;
			} else {
				source.append(',');
			}
			source.append(getDotName(getTypeName(singleVariableDeclaration.getType())));
			source.append(' ');
			source.append(singleVariableDeclaration.getName().getIdentifier());
		}
		
		source.append(')');
		
		first = true;
		for (Iterator iterator = methodDeclaration.thrownExceptions().iterator(); iterator.hasNext();) {
			Name name = (Name) iterator.next();
			if (first) {
				first = false;
				source.append(" throws "); //$NON-NLS-1$
			} else {
				source.append(',');
			}
			source.append(getQualifiedIdentifier(name));
		}
		
		if (Flags.isAbstract(modifiers) || Flags.isNative(modifiers)) {
			// No body for abstract and native methods
			source.append(";\n"); //$NON-NLS-1$
		} else {
			source.append('{').append('\n');
			source.append(getReturnExpression(methodDeclaration.getReturnType())); 
			source.append('}').append('\n');
		}
		
		return source;
	}
	
	private StringBuffer buildTypeDeclaration(StringBuffer buffer, TypeDeclaration typeDeclaration) {
		
		StringBuffer source = new StringBuffer();
		source.append(Flags.toString(typeDeclaration.getModifiers()));
		if (typeDeclaration.isInterface()) {
			source.append(" interface "); //$NON-NLS-1$
		} else {
			source.append(" class "); //$NON-NLS-1$
		}
		source.append(typeDeclaration.getName().getIdentifier());
		
		Name superClass = typeDeclaration.getSuperclass();
		if (superClass != null) {
			source.append(" extends "); //$NON-NLS-1$
			source.append(getQualifiedIdentifier(superClass));
		}
		
		boolean first = true;
		for (Iterator iterator = typeDeclaration.superInterfaces().iterator(); iterator.hasNext();) {
			Name name = (Name) iterator.next();
			if (first) {
				first = false;
				source.append(" implements "); //$NON-NLS-1$
			} else {
				source.append(',');
			}
			source.append(getQualifiedIdentifier(name));
		}
		
		if (buffer != null) {
			fStartPosOffset+= source.length();
		}
		source.append(buildTypeBody(buffer, typeDeclaration.bodyDeclarations()));
		
		return source;
	}

	private StringBuffer buildCompilationUnit(StringBuffer buffer, CompilationUnit compilationUnit) {
		StringBuffer source = new StringBuffer();
		
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		if (packageDeclaration != null) {
			source.append("package "); //$NON-NLS-1$
			source.append(getQualifiedIdentifier(packageDeclaration.getName()));
			source.append(";\n"); //$NON-NLS-1$
		}
		
		for (Iterator iterator = compilationUnit.imports().iterator(); iterator.hasNext();) {
			ImportDeclaration importDeclaration = (ImportDeclaration) iterator.next();
			source.append("import "); //$NON-NLS-1$
			source.append(getQualifiedIdentifier(importDeclaration.getName()));
			if (importDeclaration.isOnDemand()) {
				source.append(".*"); //$NON-NLS-1$
			}
			source.append(";\n"); //$NON-NLS-1$
		}
		
		fStartPosOffset += source.length();
		source.append(buffer);
		
		for (Iterator iterator = compilationUnit.types().iterator(); iterator.hasNext();) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) iterator.next();
			if (Flags.isPublic(typeDeclaration.getModifiers())) {
				fCompilationUnitName = typeDeclaration.getName().getIdentifier();
			}
			if (!fLastTypeName.equals(typeDeclaration.getName().getIdentifier())) {
				source.append(buildTypeDeclaration(null,typeDeclaration));
			}
		}
		if (fCompilationUnitName == null) {		
			// If no public class was found, the compilation unit
			// name doesn't matter.
			fCompilationUnitName= "Eval"; //$NON-NLS-1$
		}
		return source;
	}
	
	/**
	 * Returns a method name that will be unique in the generated source.
	 * The generated name is baseName plus as many '_' characters as necessary
	 * to not duplicate an existing method name.
	 */
	private String getUniqueMethodName(String methodName, List bodyDeclarations) {
		Iterator iter= bodyDeclarations.iterator();
		BodyDeclaration bodyDeclaration;
		MethodDeclaration method;
		String foundName;
		while (iter.hasNext()) {
			bodyDeclaration= (BodyDeclaration) iter.next();
			if (bodyDeclaration instanceof MethodDeclaration) {
				method= (MethodDeclaration)bodyDeclaration;
				foundName= method.getName().getIdentifier();
				if (foundName.startsWith(methodName)) {
					methodName= foundName + '_';
				}
			}
		}
		return methodName;
	}
	
	/**
	 * Returns a field name that will be unique in the generated source.
	 * The generated name is baseName plus as many '_' characters as necessary
	 * to not duplicate an existing method name.
	 */
	private String getUniqueFieldName(String fieldName, List bodyDeclarations) {
		Iterator iter= bodyDeclarations.iterator();
		BodyDeclaration bodyDeclaration;
		FieldDeclaration fieldDeclaration;
		String foundName;
		while (iter.hasNext()) {
			bodyDeclaration= (BodyDeclaration) iter.next();
			if (bodyDeclaration instanceof FieldDeclaration) {
				fieldDeclaration= (FieldDeclaration)bodyDeclaration;
				for (Iterator iterator= fieldDeclaration.fragments().iterator(); iterator.hasNext();) {
					foundName= ((VariableDeclarationFragment) iterator.next()).getName().getIdentifier();
					if (foundName.startsWith(fieldName)) {
						fieldName= foundName + '_';
					}
				}
			}
		}
		return fieldName;
	}
	
	private String getQualifiedIdentifier(Name name) {
		String typeName= ""; //$NON-NLS-1$
		while (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			typeName= "." + qualifiedName.getName().getIdentifier() + typeName; //$NON-NLS-1$
			name= qualifiedName.getQualifier();
		}
		if (name.isSimpleName()) {
			typeName= ((SimpleName)name).getIdentifier() + typeName;
		} else {
			return null;
		}
		return typeName;
	}
	
	public String getTypeName(Type type) {
		if (type.isSimpleType()) {
			SimpleType simpleType= (SimpleType) type;
			return getQualifiedIdentifier(simpleType.getName());
		} else if (type.isArrayType()) {
			ArrayType arrayType= (ArrayType) type;
			String res = getTypeName(arrayType.getElementType());
			for (int i = 0, dim= arrayType.getDimensions(); i < dim; i++) {
				res += "[]"; //$NON-NLS-1$
			}
			return res;
		} else if (type.isPrimitiveType()) {
			PrimitiveType primitiveType = (PrimitiveType) type;
			return primitiveType.getPrimitiveTypeCode().toString();
		}
		return null;
		
	}
	
	public String getReturnExpression(Type type) {
		if (type.isSimpleType() || type.isArrayType()) {
			return "return null;"; //$NON-NLS-1$
		} else if (type.isPrimitiveType()) {
			String typeName= ((PrimitiveType) type).getPrimitiveTypeCode().toString();
			char char0 = typeName.charAt(0);
			if (char0 == 'v') {
				return ""; //$NON-NLS-1$
			} else {
				char char1 = typeName.charAt(1);
				if (char0 == 'b' && char1 == 'o') {
					return "return false;"; //$NON-NLS-1$
				} else {
					return "return 0;"; //$NON-NLS-1$
				}
			}
		}
		return null;
	}
	

	//----------------------

	/*
	 * @see ASTVisitor#endVisit(ClassInstanceCreation)
	 */
	public void endVisit(ClassInstanceCreation node) {
		AnonymousClassDeclaration anonymousClassDeclaration = node.getAnonymousClassDeclaration();
		if (anonymousClassDeclaration != null && !rightTypeFound() && containsLine(node)) {
			setRightTypeFound(true);
			
			fSource= buildRunMethod(anonymousClassDeclaration.bodyDeclarations());
			fEvaluateNextEndTypeDeclaration = true;
		}
		
		if (rightTypeFound()) {
			
			List bodyDeclarations= anonymousClassDeclaration.bodyDeclarations();
			
			StringBuffer source = buildTypeBody(fSource, bodyDeclarations);
			
			ASTNode parent = node.getParent();
			while (!(parent instanceof MethodDeclaration || parent instanceof FieldDeclaration)) {
				parent= parent.getParent();
			}
			
			fSource= new StringBuffer();
				
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration enclosingMethodDeclaration = (MethodDeclaration) parent;
				
				if (Flags.isStatic(enclosingMethodDeclaration.getModifiers())) {
					fSource.append("static "); //$NON-NLS-1$
				}
					
				fSource.append("void "); //$NON-NLS-1$
				fSource.append(getUniqueMethodName(EVAL_METHOD_NAME, bodyDeclarations));
				fSource.append("() {\n"); //$NON-NLS-1$
				fSource.append("new "); //$NON-NLS-1$
				fSource.append(getQualifiedIdentifier(node.getName()));
				fSource.append("()"); //$NON-NLS-1$
				
				fStartPosOffset+= fSource.length();
				fSource.append(source);
				fSource.append(";}\n"); //$NON-NLS-1$
				
			} else if (parent instanceof FieldDeclaration) {
				FieldDeclaration enclosingFieldDeclaration = (FieldDeclaration) parent;
				
				if (Flags.isStatic(enclosingFieldDeclaration.getModifiers())) {
					fSource.append("static "); //$NON-NLS-1$
				}
				
				Type type= enclosingFieldDeclaration.getType();
				while (type instanceof ArrayType) {
					type= ((ArrayType)type).getComponentType();
				}
				
				fSource.append(getQualifiedIdentifier(((SimpleType)type).getName()));
				fSource.append(' ');
				fSource.append(getUniqueFieldName(EVAL_FIELD_NAME, bodyDeclarations));
				fSource.append(" = new "); //$NON-NLS-1$
				fSource.append(getQualifiedIdentifier(node.getName()));
				fSource.append("()"); //$NON-NLS-1$
				
				fStartPosOffset+= fSource.length();
				fSource.append(source);
				fSource.append(";\n"); //$NON-NLS-1$
				
			}
			fLastTypeName= ""; //$NON-NLS-1$
			
		}		
	}

	/*
	 * @see ASTVisitor#endVisit(CompilationUnit)
	 */
	public void endVisit(CompilationUnit node) {
		fSource = buildCompilationUnit(fSource, node);
	}

	/*
	 * @see ASTVisitor#endVisit(Initializer)
	 */
	public void endVisit(Initializer node) {
		if (!rightTypeFound() && containsLine(node)) {
			setIsInAStaticMethod(Flags.isStatic(node.getModifiers()));
		}
	}

	/*
	 * @see ASTVisitor#endVisit(MethodDeclaration)
	 */
	public void endVisit(MethodDeclaration node) {
		if (!rightTypeFound() && containsLine(node)) {
			setIsInAStaticMethod(Flags.isStatic(node.getModifiers()));
		}
	}

	/*
	 * @see ASTVisitor#endVisit(TypeDeclaration)
	 */
	public void endVisit(TypeDeclaration node) {
		
		if (!rightTypeFound() && containsLine(node)) {
			setRightTypeFound(true);
			
			fSource= buildRunMethod(node.bodyDeclarations());
			fEvaluateNextEndTypeDeclaration = true;
		}
		
		if (!fEvaluateNextEndTypeDeclaration) {
			fEvaluateNextEndTypeDeclaration = true;
			return;
		}
		
		if (rightTypeFound()) {
			
			StringBuffer source = buildTypeDeclaration(fSource, node);
			
			if (node.isLocalTypeDeclaration()) {
				// enclose in a method if nessecary
				
				ASTNode parent = node.getParent();
				while (!(parent instanceof MethodDeclaration)) {
					parent= parent.getParent();
				}
				MethodDeclaration enclosingMethodDeclaration = (MethodDeclaration) parent;
				
				fSource = new StringBuffer();
				
				if (Flags.isStatic(enclosingMethodDeclaration.getModifiers())) {
					fSource.append("static "); //$NON-NLS-1$
				}
				
				fSource.append("void ___eval() {\n"); //$NON-NLS-1$
				fStartPosOffset+= fSource.length();
				fSource.append(source);
				fSource.append("}\n"); //$NON-NLS-1$
				
				fLastTypeName = ""; //$NON-NLS-1$
			} else {
				fSource = source;
				fLastTypeName = node.getName().getIdentifier();
			}
		}
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		if (rightTypeFound()) {
			fEvaluateNextEndTypeDeclaration = false;
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		if (rightTypeFound()) {
			return false;
		}
		return true;
	}

}
