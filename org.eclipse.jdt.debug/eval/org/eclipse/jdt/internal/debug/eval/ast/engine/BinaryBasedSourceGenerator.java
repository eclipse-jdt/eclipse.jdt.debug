package org.eclipse.jdt.internal.debug.eval.ast.engine;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

public class BinaryBasedSourceGenerator {
	
	private static final String RUN_METHOD_NAME= "___run";
	private static final String EVAL_METHOD_NAME= "___eval"; //$NON-NLS-1$
	
	private int[] fLocalModifiers;
	
	private String[] fLocalTypesNames;
	
	private String[] fLocalVariables;
	
	private boolean fIsInStaticMethod;
	
	private StringBuffer fSource;
	
	private int fCodeSnippetPosition;
	
	private String fCompilationUnitName;
	
	public BinaryBasedSourceGenerator(int[] localModifiers, String[] localTypesNames, String[] localVariables, boolean isInStaticMethod) throws DebugException {
		fLocalModifiers= localModifiers;
		fLocalTypesNames= localTypesNames;
		fLocalVariables= localVariables;
		fIsInStaticMethod= isInStaticMethod;
	}
	
	/**
	 * Build source for an object value (instance context)
	 */
	public void buildSource(JDIObjectValue object) {
		ObjectReference reference= object.getUnderlyingObject();
		fSource= buildTypeDeclaration(reference, buildRunMethod(reference.referenceType()), null);
	}
	
	/**
	 * Build source for a class type (static context)
	 */
	public void buildSource(JDIClassType type) {
		Type underlyingType= type.getUnderlyingType();
		if (!(underlyingType instanceof ReferenceType)) {
			return;
		}
		ReferenceType refType= (ReferenceType)underlyingType;
		fSource= buildTypeDeclaration(refType, buildRunMethod(refType), null);
		String packageName = getPackageName(refType.name());
		if (packageName != null) {
			fSource.insert(0, "package " + packageName + ";\n");
			fCodeSnippetPosition += 10 + packageName.length();
		}
		fCompilationUnitName= getSimpleName(refType.name());
	}
	
	protected String getUniqueMethodName(String methodName, ReferenceType type) {
		List methods= type.methodsByName(methodName);
		while (!methods.isEmpty()) {
			methodName += '_';
			methods= type.methodsByName(methodName);
		}
		return methodName;
	}
	
	private StringBuffer buildRunMethod(ReferenceType type) {
		StringBuffer source = new StringBuffer();
		
		if (isInStaticMethod()) {
			source.append("static ");
		}

		source.append("void ");
		source.append(getUniqueMethodName(RUN_METHOD_NAME, type));
		source.append('(');
		for(int i= 0, length= fLocalModifiers.length; i < length; i++) {
			if (fLocalModifiers[i] != 0) {
				source.append(Flags.toString(fLocalModifiers[i]));
				source.append(' ');
			}
			source.append(getDotName(fLocalTypesNames[i]));
			source.append(' ');
			source.append(fLocalVariables[i]);
			if (i + 1 < length)
				source.append(", ");
		}
		source.append(") throws Throwable {");
		source.append('\n');
		fCodeSnippetPosition= source.length();

		source.append('\n');
		source.append('}').append('\n');
		return source;
	}
	
	private StringBuffer buildTypeDeclaration(ObjectReference object, StringBuffer buffer, String nestedTypeName) {
		
		ReferenceType referenceType = object.referenceType();
		
		StringBuffer source = buildTypeDeclaration(referenceType, buffer, nestedTypeName);
		
		Field thisField= null;
		
		List fields= referenceType.visibleFields();
		for (Iterator iterator= fields.iterator(); iterator.hasNext();) {
			Field field= (Field) iterator.next();
			if (field.name().startsWith("this$")) {
				thisField = field;
				break;
			}
		}
		
		if (thisField == null) {
			String packageName = getPackageName(referenceType.name());
			if (packageName != null) {
				source.insert(0, "package " + packageName + ";\n");
				fCodeSnippetPosition += 10 + packageName.length();
			}
			fCompilationUnitName= getSimpleName(referenceType.name());
		} else {
			ObjectReference thisObject= (ObjectReference)object.getValue(thisField);
			return buildTypeDeclaration(thisObject, source, referenceType.name());
		}
		
		return source;
	}

	private StringBuffer buildTypeDeclaration(ReferenceType referenceType, StringBuffer buffer, String nestedTypeName) {
		StringBuffer source= new StringBuffer();
		
		String typeName= referenceType.name();
		
		boolean isAnonymousType= isAnonymousTypeName(typeName);
		
		if (isAnonymousType) {
			ClassType classType= (ClassType) referenceType;
			
			source.append("void ");
			source.append(getUniqueMethodName(EVAL_METHOD_NAME, referenceType));
			source.append("() {\n");
			source.append("new ").append(getDotName(classType.superclass().name())).append("()");
			
		} else {
			if (referenceType.isFinal()) {
				source.append("final ");
			}
			
			if (referenceType.isStatic()) {
				source.append("static ");
			}
			
			if (referenceType instanceof ClassType) {
				ClassType classType= (ClassType) referenceType;
				
				if (classType.isAbstract()) {
					source.append("abstract ");
				}
			
				source.append("class ");
				
				source.append(getSimpleName(typeName)).append(' ');
				
				ClassType superClass= classType.superclass();
				if (superClass != null) {
					source.append("extends ").append(getDotName(superClass.name())).append(' ');
				}
				
				List interfaces= classType.interfaces();
				if (interfaces.size() != 0) {
					source.append("implements ");
					Iterator iterator= interfaces.iterator();
					InterfaceType interface_= (InterfaceType)iterator.next();
					source.append(interface_.name());
					while (iterator.hasNext()) {
						source.append(',').append(((InterfaceType)iterator.next()).name());
					}
				}
			} else if (referenceType instanceof InterfaceType) {
				InterfaceType interfaceType= (InterfaceType) referenceType;
				
				source.append("interface ");
				
				source.append(getSimpleName(typeName));
				
				List interfaces= interfaceType.superinterfaces();
				if (interfaces.size() != 0) {
					source.append("extends ");
					Iterator iterator= interfaces.iterator();
					InterfaceType interface_= (InterfaceType)iterator.next();
					source.append(interface_.name());
					while (iterator.hasNext()) {
						source.append(',').append(interface_.name());
					}
				}
				
			}
		}
		
		source.append(" {\n");
		
		if (buffer != null) {
			fCodeSnippetPosition += source.length();
			source.append(buffer);
		}
		
		List fields= referenceType.fields();
		for (Iterator iterator= fields.iterator(); iterator.hasNext();) {
			Field field= (Field) iterator.next();
			if (!field.name().startsWith("this$")) {
				source.append(buildFieldDeclaration(field));
			}
		}
		
		List methods= referenceType.methods();
		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
			Method method= (Method) iterator.next();
			if (!method.isConstructor() && !method.isStaticInitializer()) {
				source.append(buildMethodDeclaration(method));
			}
		}
		
		List nestedTypes= referenceType.nestedTypes();
		if (nestedTypeName == null) {
			for (Iterator iterator = nestedTypes.iterator(); iterator.hasNext();) {
				ReferenceType nestedType= (ReferenceType) iterator.next();
				if (isADirectInnerType(typeName, nestedType.name())) {
					source.append(buildTypeDeclaration(nestedType, null, null));
				}
			}
		} else {
			for (Iterator iterator = nestedTypes.iterator(); iterator.hasNext();) {
				ReferenceType nestedType= (ReferenceType) iterator.next();
				if (!nestedTypeName.equals(nestedType.name()) && isADirectInnerType(typeName, nestedType.name())) {
					source.append(buildTypeDeclaration(nestedType, null, null));
				}
			}
		}
		
		if (isAnonymousType) {
			source.append("};\n");
		}
		
		source.append("}\n");
		
		return source;
	}
	
	private StringBuffer buildFieldDeclaration(Field field) {
		StringBuffer source = new StringBuffer();
		
		if (field.isFinal()) {
			source.append("final ");
		}
		
		if (field.isStatic()) {
			source.append("static ");
		}
		
		if (field.isPublic()) {
			source.append("public ");
		} else if (field.isPrivate()) {
			source.append("private ");
		} else if (field.isProtected()) {
			source.append("protected ");
		}
		
		source.append(field.typeName()).append(' ').append(field.name()).append(';').append('\n');
		
		return source;
	}
	
	private StringBuffer buildMethodDeclaration(Method method) {
		StringBuffer source= new StringBuffer();
		
		if (method.isFinal()) {
			source.append("final ");
		}
		
		if (method.isStatic()) {
			source.append("static ");
		}
		
		if (method.isPublic()) {
			source.append("public ");
		} else if (method.isPrivate()) {
			source.append("private ");
		} else if (method.isProtected()) {
			source.append("protected ");
		}
		
		source.append(method.returnTypeName()).append(' ').append(method.name()).append('(');
		
		List arguments= method.argumentTypeNames();
		int i= 0;
		if (arguments.size() != 0) {
			Iterator iterator= arguments.iterator();
			source.append((String) iterator.next()).append(" arg").append(i++);
			while (iterator.hasNext()) {
				source.append(',').append(getDotName((String) iterator.next())).append(" arg").append(i++);
			}
		}
		source.append(") {\n");
		
		source.append(getReturnStatement(method.returnTypeName()));
		
		source.append("}\n");

		return source;
	}
	
	private String getReturnStatement(String returnTypeName) {
		String typeName= getSimpleName(returnTypeName);
		if (typeName.charAt(typeName.length() - 1) == ']') {
			return "return null;\n";
		}
		switch (typeName.charAt(0)) {
			case 'v':
				return "";
			case 'b':
				if (typeName.charAt(1) == 'o') {
					return "return false;\n";
				}
			case 's':
			case 'c':
			case 'i':
			case 'l':
			case 'd':
			case 'f':
				return "return 0;\n";
			default:
				return "return null;\n";
		}
	}
	
	private String getDotName(String typeName) {
		return typeName.replace('$', '.');
	}
	
	private boolean isAnonymousTypeName(String typeName) {
		char char0 = getSimpleName(typeName).charAt(0);
		return '0' <= char0 && char0 <= '9';
	}

	private String getSimpleName(String qualifiedName) {
		int pos = qualifiedName.lastIndexOf('$');
		if (pos == -1) {
			pos = qualifiedName.lastIndexOf('.');
		}
		return ((pos == -1)? qualifiedName : qualifiedName.substring(pos + 1));
	}
	
	private String getPackageName(String qualifiedName) {
		int pos = qualifiedName.lastIndexOf('.');
		return ((pos == -1)? null : qualifiedName.substring(0, pos));
	}
	
	private boolean isADirectInnerType(String typeName, String nestedTypeName) {
		String end= nestedTypeName.substring(typeName.length() + 1);
		return end.indexOf('$') == -1;
	}
	
	private boolean isInStaticMethod() {
		return fIsInStaticMethod;
	}
	
	public StringBuffer getSource() {
		return fSource;
	}
	
	public int getCodeSnippetPosition() {
		return fCodeSnippetPosition;
	}
	
	public String getCompilationUnitName() {
		return fCompilationUnitName;
	}
	
	public int getBlockStar() {
		return fCodeSnippetPosition - 2;
	}

}
