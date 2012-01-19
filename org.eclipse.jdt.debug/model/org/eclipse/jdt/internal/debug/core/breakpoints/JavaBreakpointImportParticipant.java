/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.breakpoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IBreakpointImportParticipant;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;

/**
 * Default implementation covering the import of all platform Java breakpoints
 * 
 * @since 3.5
 */
public class JavaBreakpointImportParticipant implements
		IBreakpointImportParticipant {

	class BreakpointVerifier extends ASTVisitor {
		final int TYPE = 0;
		final int METHOD = 1;
		final int FIELD = 2;

		String fTypename = null;
		String fName = null;
		String fSignature = null;
		IBreakpoint fBreakpoint = null;
		CompilationUnit fUnit = null;
		Stack<String> fTypeNameStack = null;

		/**
		 * Constructor
		 * 
		 * @param breakpoint
		 * @param unit
		 */
		public BreakpointVerifier(IBreakpoint breakpoint, CompilationUnit unit) {
			fTypename = getBreakpointTypeName(breakpoint);
			fName = getMemberName(breakpoint);
			fSignature = getMemberSignature(breakpoint);
			fBreakpoint = breakpoint;
			fUnit = unit;
			fTypeNameStack = new Stack<String>();
		}

		/**
		 * Returns the value of the {@link JavaBreakpoint#TYPE_NAME} attribute
		 * from the breakpoint or <code>null</code>
		 * 
		 * @param breakpoint
		 * @return the value of the type name attribute
		 */
		String getBreakpointTypeName(IBreakpoint breakpoint) {
			return breakpoint.getMarker().getAttribute(
					JavaBreakpoint.TYPE_NAME, null);
		}

		/**
		 * Returns the name of the member from the breakpoint attributes. The
		 * name will be one of (1) {@link JavaWatchpoint#FIELD_NAME}, if the
		 * breakpoint is a watchpoint, or (2)
		 * {@link JavaMethodBreakpoint#METHOD_NAME} if the breakpoint is a
		 * method or method entry breakpoint (3) <code>null</code> if there is
		 * no member name
		 * 
		 * @param breakpoint
		 * @return the member name or <code>null</code>
		 */
		String getMemberName(IBreakpoint breakpoint) {
			if (breakpoint instanceof IJavaWatchpoint) {
				return breakpoint.getMarker().getAttribute(
						JavaWatchpoint.FIELD_NAME, null);
			}
			return breakpoint.getMarker().getAttribute(
					JavaMethodBreakpoint.METHOD_NAME, null);
		}

		/**
		 * Returns the signature of the member, defined with the
		 * {@link JavaMethodBreakpoint#METHOD_SIGNATURE} attribute, or
		 * <code>null</code>
		 * 
		 * @param breakpoint
		 * @return the signature of the member or <code>null</code>
		 */
		String getMemberSignature(IBreakpoint breakpoint) {
			return breakpoint.getMarker().getAttribute(
					JavaMethodBreakpoint.METHOD_SIGNATURE, null);
		}

		/**
		 * Returns the fully qualified name of the enclosing type for the given
		 * node
		 * 
		 * @param node
		 * @return the fully qualified name of the enclosing type
		 */
		private String getTypeName(ASTNode node) {
			return getTypeName(node, new StringBuffer());
		}

		/**
		 * Constructs the qualified name of the enclosing parent type
		 * 
		 * @param node
		 *            the node to get the parent name for
		 * @param buffer
		 *            the buffer to write the name into
		 * @return the fully qualified name of the parent
		 */
		private String getTypeName(ASTNode node, StringBuffer buffer) {
			switch (node.getNodeType()) {
			case ASTNode.COMPILATION_UNIT: {
				CompilationUnit unit = (CompilationUnit) node;
				PackageDeclaration packageDeclaration = unit.getPackage();
				if (packageDeclaration != null) {
					buffer.insert(0, '.');
					buffer.insert(0, packageDeclaration.getName()
							.getFullyQualifiedName());
				}
				return String.valueOf(buffer);
			}
			default: {
				if (node instanceof AbstractTypeDeclaration) {
					AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) node;
					ITypeBinding binding = typeDeclaration.resolveBinding();
					if (binding != null) {
						return binding.getBinaryName();
					}
					if (typeDeclaration.isPackageMemberTypeDeclaration()) {
						buffer.insert(0, typeDeclaration.getName()
								.getIdentifier());
					} else {
						buffer.insert(0, typeDeclaration.getName()
								.getFullyQualifiedName());
						buffer.insert(0, '$');
					}
				}
			}
			}
			return getTypeName(node.getParent(), buffer);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
		 */
		@Override
		public boolean visit(TypeDeclaration node) {
			return doTypeVisit(node);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeDeclaration)
		 */
		@Override
		public void endVisit(TypeDeclaration node) {
			doEndTypeVisit();
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EnumDeclaration)
		 */
		@Override
		public boolean visit(EnumDeclaration node) {
			return doTypeVisit(node);
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.EnumDeclaration)
		 */
		@Override
		public void endVisit(EnumDeclaration node) {
			doEndTypeVisit();
		}

		/**
		 * Cleans up after a type visit has ended
		 */
		private void doEndTypeVisit() {
			if (!fTypeNameStack.isEmpty()) {
				fTypeNameStack.pop();
			}
		}

		/**
		 * Visits the type node and return if children should be visited
		 * 
		 * @param node
		 * @return true if child nodes should be visited false otherwise
		 */
		private boolean doTypeVisit(AbstractTypeDeclaration node) {
			SimpleName name = node.getName();
			String typename = getTypeName(node);
			fTypeNameStack.push(typename);
			if (!fTypename.startsWith(typename)) {
				// we are examining the wrong type stop and process other types
				return false;
			}
			if (fBreakpoint instanceof JavaClassPrepareBreakpoint
					&& name != null && typename.equals(fTypename)) {
				int charstart = name.getStartPosition();
				IMarker marker = fBreakpoint.getMarker();
				try {
					marker.setAttribute(IMarker.CHAR_START, charstart);
					marker.setAttribute(IMarker.CHAR_END,
							charstart + name.getLength());
				} catch (CoreException ce) {
				}
				// found the node we were looking for, do not visit children
				return false;
			}
			return fTypename.indexOf('$') > -1 || name != null;
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
		 */
		@Override
		public boolean visit(FieldDeclaration node) {
			if (!fTypename.equals(fTypeNameStack.peek())) {
				return false;
			}
			List<VariableDeclarationFragment> fragments = node.fragments();
			SimpleName name = null;
			IMarker marker = fBreakpoint.getMarker();
			int currentstart = marker.getAttribute(IMarker.CHAR_START, -1);
			for (VariableDeclarationFragment fragment : fragments) {
				name = fragment.getName();
				if (name != null && name.getFullyQualifiedName().equals(fName)) {
					// found field update the charstart / charend
					int charstart = name.getStartPosition();
					if (currentstart != charstart) {
						try {
							marker.setAttribute(IMarker.CHAR_START, charstart);
							marker.setAttribute(IMarker.CHAR_END, charstart
									+ name.getLength());
						} catch (CoreException ce) {
						}
					}
				}
			}
			return false;
		}

		/**
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
		 */
		@Override
		public boolean visit(MethodDeclaration node) {
			SimpleName name = node.getName();
			String typename = fTypeNameStack.peek();
			if (!fTypename.equals(typename) && !fTypename.startsWith(typename)) {
				return false;
			}
			if (name != null && name.getFullyQualifiedName().equals(fName)) {
				String sig = getMethodSignatureFromNode(node);
				if (sig != null) {
					sig = sig.replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
					if (sig.equals(fSignature)) {
						IMarker marker = fBreakpoint.getMarker();
						int currentstart = marker.getAttribute(
								IMarker.CHAR_START, -1);
						int charstart = name.getStartPosition();
						if (currentstart != charstart) {
							try {
								marker.setAttribute(IMarker.CHAR_START,
										charstart);
								marker.setAttribute(IMarker.CHAR_END, charstart
										+ name.getLength());
							} catch (CoreException ce) {
							}
						}
					}
				}
			}
			// visit children in the event we have a class load breakpoint on a
			// local type
			return fBreakpoint instanceof JavaClassPrepareBreakpoint;
		}

		/**
		 * Creates a method signature from a specified {@link MethodDeclaration}
		 * 
		 * @param node
		 * @return the signature for the given method node or <code>null</code>
		 */
		private String getMethodSignatureFromNode(MethodDeclaration node) {
			Assert.isNotNull(node);
			List<SingleVariableDeclaration> params = node.parameters();
			List<String> rparams = getParametersTypeNames(params);
			if (rparams.size() == params.size()) {
				if (!node.isConstructor()) {
					Type returnType = node.getReturnType2();
					if (returnType != null) {
						String rtype = getTypeSignature(returnType);
						if (rtype != null) {
							return Signature
									.createMethodSignature(
											rparams
													.toArray(new String[rparams
															.size()]), rtype);
						}
					}
				} else {
					StringBuffer buffer = new StringBuffer();
					buffer.append("<init>"); //$NON-NLS-1$
					collectSyntheticParam(node, rparams);
					buffer.append(Signature.createMethodSignature(
							rparams.toArray(new String[rparams
									.size()]), Signature.SIG_VOID));
					return buffer.toString();
				}
			}
			return null;
		}

		/**
		 * Returns the listing of the signatures of the parameters passed in
		 * 
		 * @param rawparams
		 * @return a listing of signatures for the specified parameters
		 */
		private List<String> getParametersTypeNames(List<SingleVariableDeclaration> rawparams) {
			List<String> rparams = new ArrayList<String>(rawparams.size());
			String pname = null;
			for (SingleVariableDeclaration param : rawparams) {
				pname = getTypeSignature(param.getType());
				if (pname != null) {
					rparams.add(pname);
				}
			}
			return rparams;
		}

		/**
		 * Processes the signature for the given {@link Type}
		 * 
		 * @param type
		 *            the type to process
		 * @return the signature for the type or <code>null</code> if one could
		 *         not be derived
		 */
		private String getTypeSignature(Type type) {
			ITypeBinding binding = type.resolveBinding();
			if (binding == null) {
				return null;
			}
			switch (type.getNodeType()) {
			case ASTNode.PRIMITIVE_TYPE:
			case ASTNode.QUALIFIED_TYPE:
			case ASTNode.SIMPLE_TYPE: {
				return Signature.createTypeSignature(
						binding.getQualifiedName(), true);
			}
			case ASTNode.ARRAY_TYPE: {
				ArrayType a = (ArrayType) type;
				return Signature
						.createArraySignature(
								getTypeSignature(a.getElementType()),
								a.getDimensions());
			}
			case ASTNode.PARAMETERIZED_TYPE: {
				// we don't need to care about the other scoping types only the
				// base type
				return getTypeSignature(((ParameterizedType) type).getType());
			}
			}
			return null;
		}

		/**
		 * Collects the synthetic parameter of the fully qualified name of the
		 * enclosing context for a constructor of an inner type
		 * 
		 * @param method
		 *            the constructor declaration
		 * @param rparams
		 *            the listing of parameters to add to
		 */
		private void collectSyntheticParam(final MethodDeclaration method,
				List<String> rparams) {
			Assert.isNotNull(method);
			if (isInTopLevelType(method)) {
				return;
			}
			ASTNode parent = method.getParent();
			StringBuffer name = new StringBuffer();
			while (parent != null) {
				parent = parent.getParent();
				if (parent instanceof AbstractTypeDeclaration) {
					AbstractTypeDeclaration type = (AbstractTypeDeclaration) parent;
					name.insert(0, type.getName().getFullyQualifiedName());
					if (type.isMemberTypeDeclaration()) {
						name.insert(0, '$');
					}
					continue;
				}
				if (parent instanceof CompilationUnit) {
					CompilationUnit cunit = (CompilationUnit) parent;
					PackageDeclaration pdec = cunit.getPackage();
					if (pdec != null) {
						name.insert(0, '.');
						name.insert(0, cunit.getPackage().getName()
								.getFullyQualifiedName());
					}
				}
			}
			name.insert(0, "L"); //$NON-NLS-1$
			name.append(';');
			if (name.length() > 2) {
				rparams.add(0, name.toString());
			}
		}

		/**
		 * Determines if the given {@link MethodDeclaration} is present in a top
		 * level type
		 * 
		 * @param method
		 * @return
		 */
		private boolean isInTopLevelType(final MethodDeclaration method) {
			TypeDeclaration type = (TypeDeclaration) method.getParent();
			return type != null && type.isPackageMemberTypeDeclaration();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IBreakpointImportParticipant#matches(java
	 * .util.Map, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean matches(Map attributes, IBreakpoint breakpoint)
			throws CoreException {
		if (attributes == null || breakpoint == null) {
			return false;
		}
		String type = (String) attributes.get("type"); //$NON-NLS-1$
		if (type == null) {
			return false;
		}
		if (!breakpoint.getMarker().getType().equals(type)) {
			return false;
		}
		if (breakpoint instanceof JavaClassPrepareBreakpoint) {
			return matchesClassBreakpoint(attributes,
					(JavaClassPrepareBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaExceptionBreakpoint) {
			return matchesExceptionBreakpoint(attributes,
					(JavaExceptionBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaMethodBreakpoint) {
			return matchesMethodBreakpoint(attributes,
					(JavaMethodBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaMethodEntryBreakpoint) {
			return matchesMethodEntryBreakpoint(attributes,
					(JavaMethodEntryBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaWatchpoint) {
			return matchesWatchpoint(attributes, (JavaWatchpoint) breakpoint);
		}
		if (breakpoint instanceof JavaStratumLineBreakpoint) {
			return matchesStratumLineBreakpoint(attributes,
					(JavaStratumLineBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaPatternBreakpoint) {
			return matchesPatternBreakpoint(attributes,
					(JavaPatternBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaTargetPatternBreakpoint) {
			return matchesTargetPatternBreakpoint(attributes,
					(JavaTargetPatternBreakpoint) breakpoint);
		}
		if (breakpoint instanceof JavaLineBreakpoint) {
			return matchesLineBreakpoint(attributes,
					(JavaLineBreakpoint) breakpoint);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IBreakpointImportParticipant#verify(org.
	 * eclipse.debug.core.model.IBreakpoint)
	 */
	public void verify(IBreakpoint breakpoint) throws CoreException {
		IResource resource = breakpoint.getMarker().getResource();
		CompilationUnit unit = null;
		if (resource != null && resource.getType() == IResource.FILE) {
			ICompilationUnit cunit = JavaCore
					.createCompilationUnitFrom((IFile) resource);
			if (cunit != null) {
				ASTParser parser = ASTParser.newParser(AST.JLS4);
				parser.setSource(cunit);
				parser.setResolveBindings(true);
				unit = (CompilationUnit) parser
						.createAST(new NullProgressMonitor());
			}
		}
		if (unit != null) {
			if (breakpoint instanceof JavaClassPrepareBreakpoint
					|| breakpoint instanceof JavaWatchpoint
					|| breakpoint instanceof JavaMethodEntryBreakpoint
					|| breakpoint instanceof JavaMethodBreakpoint) {
				unit.accept(new BreakpointVerifier(breakpoint, unit));
			} else if (breakpoint instanceof JavaLineBreakpoint) {
				JavaLineBreakpoint bp = (JavaLineBreakpoint) breakpoint;
				// line breakpoint use the ValidBreakpointLocationLocator to
				// (re)place it
				int currentline = bp.getLineNumber();
				ValidBreakpointLocationLocator locator = new ValidBreakpointLocationLocator(
						unit, currentline, true, true);
				unit.accept(locator);
				int newline = locator.getLineLocation();
				if (locator.getLocationType() == ValidBreakpointLocationLocator.LOCATION_LINE) {
					if (currentline != newline) {
						bp.getMarker().setAttribute(JavaBreakpoint.TYPE_NAME,
								locator.getFullyQualifiedTypeName());
						bp.getMarker().setAttribute(IMarker.LINE_NUMBER,
								newline);
						int length = bp.getCharEnd() - bp.getCharStart();
						int pos = unit.getPosition(newline, 1);
						bp.getMarker().setAttribute(IMarker.CHAR_START, pos);
						bp.getMarker().setAttribute(IMarker.CHAR_END,
								pos + length);
					}
				} else {
					// the line breakpoint will not be a line breakpoint anymore
					// get rid of it
					throw new CoreException(Status.CANCEL_STATUS);
				}
			}
		}
	}

	/**
	 * Compares two attributes in a <code>null</code> safe way
	 * 
	 * @param attr1
	 *            the first attribute
	 * @param attr2
	 *            the second attribute
	 * @return true if the attributes are equal, false otherwise. If both
	 *         attributes are <code>null</code> they are considered to be equal
	 */
	private boolean attributesEqual(Object attr1, Object attr2) {
		if (attr1 == null) {
			return attr2 == null;
		}
		return attr1.equals(attr2);
	}

	/**
	 * Returns if the given map of attributes matches the given line breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesLineBreakpoint(Map<String, Object> attributes,
			JavaLineBreakpoint breakpoint) throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		return breakpoint.getLineNumber() == (line == null ? -1 : line.intValue())
				&& attributesEqual(breakpoint.getTypeName(), attributes.get(JavaBreakpoint.TYPE_NAME));
	}

	/**
	 * Returns if the given map of attributes matches the given class prepare
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesClassBreakpoint(Map<String, Object> attributes,
			JavaClassPrepareBreakpoint breakpoint) throws CoreException {
		Integer type = (Integer) attributes
				.get(JavaClassPrepareBreakpoint.MEMBER_TYPE);
		return attributesEqual(breakpoint.getTypeName(),
				attributes.get(JavaBreakpoint.TYPE_NAME))
				&& breakpoint.getMemberType() == (type == null ? -1 : type
						.intValue());
	}

	/**
	 * Returns if the given map of attributes matches the given exception
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesExceptionBreakpoint(Map<String, Object> attributes,
			JavaExceptionBreakpoint breakpoint) throws CoreException {
		return attributesEqual(breakpoint.getTypeName(),
				attributes.get(JavaBreakpoint.TYPE_NAME));

	}

	/**
	 * Returns if the given map of attributes matches the given method
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesMethodBreakpoint(Map<String, Object> attributes,
			JavaMethodBreakpoint breakpoint) throws CoreException {
		return attributesEqual(breakpoint.getTypeName(),
				attributes.get(JavaBreakpoint.TYPE_NAME))
				&& attributesEqual(breakpoint.getMethodName(),
						attributes.get(JavaMethodBreakpoint.METHOD_NAME))
				&& attributesEqual(breakpoint.getMethodSignature(),
						attributes.get(JavaMethodBreakpoint.METHOD_SIGNATURE));
	}

	/**
	 * Returns if the given map of attributes matches the given method entry
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesMethodEntryBreakpoint(Map<String, Object> attributes,
			JavaMethodEntryBreakpoint breakpoint) throws CoreException {
		return attributesEqual(breakpoint.getTypeName(),
				attributes.get(JavaBreakpoint.TYPE_NAME))
				&& attributesEqual(breakpoint.getMethodName(),
						attributes.get(JavaMethodBreakpoint.METHOD_NAME))
				&& attributesEqual(breakpoint.getMethodSignature(),
						attributes.get(JavaMethodBreakpoint.METHOD_SIGNATURE));
	}

	/**
	 * Returns if the given map of attributes matches the given watchpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the watchpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesWatchpoint(Map<String, Object> attributes, JavaWatchpoint watchpoint)
			throws CoreException {
		return watchpoint.getFieldName().equals(
				attributes.get(JavaWatchpoint.FIELD_NAME))
				&& attributesEqual(watchpoint.getTypeName(),
						attributes.get(JavaBreakpoint.TYPE_NAME));
	}

	/**
	 * Returns if the given map of attributes matches the given stratum line
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesStratumLineBreakpoint(Map<String, Object> attributes,
			JavaStratumLineBreakpoint breakpoint) throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		return breakpoint.getLineNumber() == (line == null ? -1 : line
				.intValue())
				&& attributesEqual(breakpoint.getSourceName(),
						attributes.get(JavaLineBreakpoint.SOURCE_NAME))
				&& attributesEqual(breakpoint.getStratum(),
						attributes.get(JavaStratumLineBreakpoint.STRATUM))
				&& attributesEqual(breakpoint.getSourcePath(),
						attributes.get(JavaStratumLineBreakpoint.SOURCE_PATH));
	}

	/**
	 * Returns if the given map of attributes matches the given pattern
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesPatternBreakpoint(Map<String, Object> attributes,
			JavaPatternBreakpoint breakpoint) throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		return breakpoint.getLineNumber() == (line == null ? -1 : line
				.intValue())
				&& attributesEqual(breakpoint.getSourceName(),
						attributes.get(JavaLineBreakpoint.SOURCE_NAME)) &&
				// TDOD comparing pattern too restrictive??
				breakpoint.getPattern().equals(
						attributes.get(JavaPatternBreakpoint.PATTERN));
	}

	/**
	 * Returns if the given map of attributes matches the given target pattern
	 * breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoints' attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesTargetPatternBreakpoint(Map<String, Object> attributes,
			JavaTargetPatternBreakpoint breakpoint) throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		return breakpoint.getLineNumber() == (line == null ? -1 : line
				.intValue())
				&& attributesEqual(breakpoint.getTypeName(),
						attributes.get(JavaBreakpoint.TYPE_NAME))
				&& attributesEqual(breakpoint.getSourceName(),
						attributes.get(JavaLineBreakpoint.SOURCE_NAME));
	}
}
