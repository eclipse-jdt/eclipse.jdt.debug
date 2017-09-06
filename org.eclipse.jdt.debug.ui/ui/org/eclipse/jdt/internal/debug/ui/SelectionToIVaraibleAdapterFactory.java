package org.eclipse.jdt.internal.debug.ui;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIPlaceholderVariable;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class SelectionToIVaraibleAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (!(adaptableObject instanceof TextSelection)) {
			return null;
		}
		if (!adapterType.isAssignableFrom(IVariable.class)) {
			return null;
		}
		TextSelection selection = (TextSelection) adaptableObject;
		IDocument document = null;
		try {
			Field documentField = TextSelection.class.getDeclaredField("fDocument"); //$NON-NLS-1$
			documentField.setAccessible(true);
			document = (Document) documentField.get(selection);
		}
		catch (Exception e) {
			JavaPlugin.log(e);
		}
		if (document == null) {
			return null;
		}
		return (T) getVariable(document, new Region(selection.getOffset(), selection.getLength()));
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IVariable.class };
	}

	/**
	 * Returns the stack frame in which to search for variables, or <code>null</code> if none.
	 *
	 * @return the stack frame in which to search for variables, or <code>null</code> if none
	 */
	protected IJavaStackFrame getFrame() {
		IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable != null) {
			return adaptable.getAdapter(IJavaStackFrame.class);
		}
		return null;
	}

	private IVariable getVariable(IDocument document, IRegion region) {
		IJavaStackFrame frame = getFrame();
		if (frame == null) {
			return null;
		}
		String variableName = null;
		try {
			variableName = document.get(region.getOffset(), region.getLength());
		}
		catch (BadLocationException e) {
			return null;
		}
		if (variableName.equals("this")) { //$NON-NLS-1$
			try {
				IJavaVariable variable = frame.findVariable(variableName);
				if (variable != null) {
					return variable;
				}
			}
			catch (DebugException e) {
				JavaPlugin.log(e);
				return null;
			}
		}
		ICodeAssist codeAssist = getCodeAssist(document);
		if (codeAssist == null) {
			return findLocalVariable(frame, variableName);
		}

		IJavaElement[] resolve = null;
		try {
			resolve = codeAssist.codeSelect(region.getOffset(), 0);
		}
		catch (JavaModelException e1) {
			resolve = new IJavaElement[0];
		}
		try {
			for (int i = 0; i < resolve.length; i++) {
				IJavaElement javaElement = resolve[i];
				if (javaElement instanceof IField) {
					IField field = (IField) javaElement;
					IJavaVariable variable = null;
					IJavaDebugTarget debugTarget = (IJavaDebugTarget) frame.getDebugTarget();
					if (Flags.isStatic(field.getFlags())) {
						IJavaType[] javaTypes = debugTarget.getJavaTypes(field.getDeclaringType().getFullyQualifiedName());
						if (javaTypes != null) {
							for (int j = 0; j < javaTypes.length; j++) {
								IJavaType type = javaTypes[j];
								if (type instanceof IJavaReferenceType) {
									IJavaReferenceType referenceType = (IJavaReferenceType) type;
									variable = referenceType.getField(field.getElementName());
								}
								if (variable != null) {
									break;
								}
							}
						}
						if (variable == null) {
							// the class is not loaded yet, but may be an in-lined primitive constant
							Object constant = field.getConstant();
							if (constant != null) {
								IJavaValue value = null;
								if (constant instanceof Integer) {
									value = debugTarget.newValue(((Integer) constant).intValue());
								} else if (constant instanceof Byte) {
									value = debugTarget.newValue(((Byte) constant).byteValue());
								} else if (constant instanceof Boolean) {
									value = debugTarget.newValue(((Boolean) constant).booleanValue());
								} else if (constant instanceof Character) {
									value = debugTarget.newValue(((Character) constant).charValue());
								} else if (constant instanceof Double) {
									value = debugTarget.newValue(((Double) constant).doubleValue());
								} else if (constant instanceof Float) {
									value = debugTarget.newValue(((Float) constant).floatValue());
								} else if (constant instanceof Long) {
									value = debugTarget.newValue(((Long) constant).longValue());
								} else if (constant instanceof Short) {
									value = debugTarget.newValue(((Short) constant).shortValue());
								} else if (constant instanceof String) {
									value = debugTarget.newValue((String) constant);
								}
								if (value != null) {
									variable = new JDIPlaceholderVariable(field.getElementName(), value);
								}
							}
							if (variable == null) {
								return null; // class not loaded yet and not a constant
							}
						}
					} else {
						if (!frame.isStatic() && !frame.isNative()) {
							// ensure that we only resolve a field access on 'this':
							if (!(codeAssist instanceof ITypeRoot)) {
								return null;
							}
							ITypeRoot typeRoot = (ITypeRoot) codeAssist;
							ASTNode root = SharedASTProvider.getAST(typeRoot, SharedASTProvider.WAIT_NO, null);
							if (root == null) {
								ASTParser parser = ASTParser.newParser(AST.JLS4);
								parser.setSource(typeRoot);
								parser.setFocalPosition(region.getOffset());
								root = parser.createAST(null);
							}
							ASTNode node = NodeFinder.perform(root, region.getOffset(), region.getLength());
							if (node == null) {
								return null;
							}
							StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
							if (locationInParent == FieldAccess.NAME_PROPERTY) {
								FieldAccess fieldAccess = (FieldAccess) node.getParent();
								if (!(fieldAccess.getExpression() instanceof ThisExpression)) {
									return null;
								}
							} else if (locationInParent == QualifiedName.NAME_PROPERTY) {
								return null;
							}

							String typeSignature = Signature.createTypeSignature(field.getDeclaringType().getFullyQualifiedName(), true);
							typeSignature = typeSignature.replace('.', '/');
							IJavaObject ths = frame.getThis();
							if (ths != null) {
								variable = ths.getField(field.getElementName(), typeSignature);
							}
						}
					}
					if (variable != null) {
						return variable;
					}
					break;
				}
				if (javaElement instanceof ILocalVariable) {
					ILocalVariable var = (ILocalVariable) javaElement;
					IJavaElement parent = var.getParent();
					while (!(parent instanceof IMethod) && parent != null) {
						parent = parent.getParent();
					}
					if (parent instanceof IMethod) {
						IMethod method = (IMethod) parent;
						boolean equal = false;
						if (method.isBinary()) {
							// compare resolved signatures
							if (method.getSignature().equals(frame.getSignature())) {
								equal = true;
							}
						} else {
							// compare unresolved signatures

							// Frames in classes with generics have declaringTypeName like class<V>
							// We must get rid of this '<V>' for proper comparison
							String frameDeclaringTypeName = frame.getDeclaringTypeName();
							int genericPartOffset = frameDeclaringTypeName.indexOf('<');
							if (genericPartOffset != -1) {
								frameDeclaringTypeName = frameDeclaringTypeName.substring(0, genericPartOffset);
							}

							if (((frame.isConstructor() && method.isConstructor()) || frame.getMethodName().equals(method.getElementName()))
									&& frameDeclaringTypeName.endsWith(method.getDeclaringType().getElementName())
									&& frame.getArgumentTypeNames().size() == method.getNumberOfParameters()) {
								equal = true;
							} else { // Finding variables in anonymous class
								int index = frame.getDeclaringTypeName().indexOf('$');
								if (index > 0) {
									String name = frame.getDeclaringTypeName().substring(index + 1);
									try {
										Integer.getInteger(name);
										return findLocalVariable(frame, ASTEvaluationEngine.ANONYMOUS_VAR_PREFIX + var.getElementName());
									}
									catch (NumberFormatException ex) {
									}
								}
							}
						}
						// find variable if equal or method is a Lambda Method
						if (equal || method.isLambdaMethod()) {
							return findLocalVariable(frame, var.getElementName());
						}
					}
					break;
				}
			}
		}
		catch (CoreException e) {
			JDIDebugPlugin.log(e);
		}
		return null;
	}

	private ICodeAssist getCodeAssist(IEditorInput input) {
		if (input == null) {
			return null;
		}
		Object element = JavaUI.getWorkingCopyManager().getWorkingCopy(input);
		if (element == null) {
			element = input.getAdapter(IClassFile.class);
		}
		if (element instanceof ICodeAssist) {
			return ((ICodeAssist) element);
		}
		return null;
	}

	private ICodeAssist getCodeAssist(IDocument document) {
		ITextFileBuffer textFileBuffer = ITextFileBufferManager.DEFAULT.getTextFileBuffer(document);
		if (textFileBuffer == null) {
			return null;
		}
		Optional<ICodeAssist> codeAssist = Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows())
				.flatMap(window -> Arrays.stream(window.getPages()))
				.flatMap(page -> Arrays.stream(page.getEditorReferences())).filter(editor -> {
					try {
						return matches(editor.getEditorInput(), textFileBuffer);
					}
					catch (PartInitException e1) {
						JavaPlugin.log(e1);
						return false;
					}
				})
				.filter(editor -> editor.getEditor(false) != null) // editor that are initialized only
				.map(ref -> {
					try {
						return ref.getEditorInput();
					}
					catch (PartInitException e) {
						JavaPlugin.log(e);
						return null;
					}
				}).map(this::getCodeAssist)
				.filter(Objects::nonNull)
				.distinct()
				.findAny();
		if (codeAssist.isPresent()) {
			return codeAssist.get();
		}
		return null;
	}

	private boolean matches(IEditorInput input, ITextFileBuffer textFileBuffer) {
		if (input instanceof IFileEditorInput && textFileBuffer.getLocation().equals(((IFileEditorInput) input).getFile().getFullPath())) {
			return true;
		}
		// TODO maybe map URIs and other kinds of input
		return false;
	}

	/**
	 * Returns a local variable in the given frame based on the the given name or <code>null</code> if none.
	 *
	 * @return local variable or <code>null</code>
	 */
	private IVariable findLocalVariable(IJavaStackFrame frame, String variableName) {
		if (frame != null) {
			try {
				return frame.findVariable(variableName);
			}
			catch (DebugException x) {
				if (x.getStatus().getCode() != IJavaThread.ERR_THREAD_NOT_SUSPENDED) {
					JDIDebugUIPlugin.log(x);
				}
			}
		}
		return null;
	}


}
