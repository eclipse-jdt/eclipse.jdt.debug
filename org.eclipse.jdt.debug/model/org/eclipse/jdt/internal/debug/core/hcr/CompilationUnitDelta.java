/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.core.hcr;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * A <code>CompilationUnitDelta</code> represents the source code changes between
 * a CU in the workspace and the same CU at some point in the past
 * (from the local history).
 * <p>
 * This functionality is used in the context of Hot Code Replace
 * to determine which stack frames are affected (and need to be dropped)
 * by a class reload in the Java VM.
 * <p>
 * Typically a <code>CompilationUnitDelta</code> object is generated for a CU
 * when the associated class is replaced in the VM.
 * The <code>CompilationUnitDelta</code> calculates
 * the differences between the current version of the CU and the version in the
 * local history that was effective at the given <bold>last</bold> build time.
 * The differences are stored as a tree which allows for an efficient implementation
 * of a <code>hasChanged(IMember)</code> method.
 */
public class CompilationUnitDelta {
	
	private static final boolean DEBUG= false;
	
	private static class SimpleJavaElement {
		
		private String fName;
		private HashMap fChildren;
		
		SimpleJavaElement(SimpleJavaElement parent, int changeType, String name) {
			fName= name;
			if (parent != null) {
				if (parent.fChildren == null) {
					parent.fChildren= new HashMap();
				}
				parent.fChildren.put(name, this);
			}
		}
		
		void dump(int level) {
			for (int i= 0; i < level; i++)	
				System.out.print("  ");	//$NON-NLS-1$
			System.out.println(fName);
			
			if (fChildren != null) {
				Iterator iter= fChildren.values().iterator();
				while (iter.hasNext()) {
					SimpleJavaElement e= (SimpleJavaElement) iter.next();
					e.dump(level+1);
				}
			}
		}
		
		boolean find(String[] path, int start) {
			if (start >= path.length) {
				return true;
			}
			String key= path[start];
			if (fChildren != null) {
				SimpleJavaElement child= (SimpleJavaElement) fChildren.get(key);
				if (child != null) {
					return child.find(path, start+1);
				}
			}
			return false;
		}
	}
	
	/**
	 * Used to bail out from ProblemFactory.
	 */
	private static class ParseError extends Error {
	}
	
	/**
	 * This problem factory aborts parsing on first error.
	 */
	private static class ProblemFactory implements IProblemFactory {
		
		public IProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber) {
			throw new ParseError();
		}
		
		public Locale getLocale() {
			return Locale.getDefault();
		}
		
		public String getLocalizedMessage(int problemId, String[] problemArguments) {
			return "" + problemId; //$NON-NLS-1$
		}
	}

	private ICompilationUnit fCompilationUnit;
	private SimpleJavaElement fRoot;
	private boolean fHasHistory= false;
	
	/**
	 * Creates a new <code>CompilationUnitDelta object that calculates and stores
	 * the changes of the given CU since some point in time.
	 */
	public CompilationUnitDelta(ICompilationUnit cu, long timestamp) throws CoreException {
		
		if (cu.isWorkingCopy()) {
			cu= (ICompilationUnit) cu.getOriginalElement();
		}

		fCompilationUnit= cu;
		
		// find underlying file
		IFile file= (IFile) cu.getUnderlyingResource();

		// get available editions
		IFileState[] states= file.getHistory(null);
		if (states == null || states.length <= 0) {
			return;
		}
		fHasHistory= true;
		
		IFileState found= null;
		// find edition just before the given time stamp
		for (int i= 0; i < states.length; i++) {
			IFileState state= states[i];
			long d= state.getModificationTime();
			if (d < timestamp) {
				found= state;
				break;
			}
		}
		
		if (found == null) {
			found= states[states.length-1];
		}
		
		InputStream oldContents= null;
		InputStream newContents= null;
		try {
			oldContents= found.getContents();
			newContents= file.getContents();
		} catch (CoreException ex) {
			return;
		}
		
		JavaNode oldStructure= parse(oldContents);
		JavaNode newStructure= parse(newContents);
		
		final boolean[] memberDeleted= new boolean[1];	// visitor returns result here
		
		Differencer differencer= new Differencer() {
			protected Object visit(Object data, int result, Object ancestor, Object left, Object right) {
				String name= null;
				switch (result) {
				case Differencer.CHANGE:
					name= ((JavaNode)left).getId();
					break;
				case Differencer.ADDITION:
					name= ((JavaNode)right).getId();
					break;
				case Differencer.DELETION:
					name= ((JavaNode)left).getId();
					memberDeleted[0]= true;
					break;
				default:
					break;
				}
				if (name != null) {
					return new SimpleJavaElement((SimpleJavaElement) data, result, name);
				}
				return null;
			}
			protected boolean contentsEqual(Object o1, Object o2) {
				String s1= ((JavaNode) o1).getContents();
				String s2= ((JavaNode) o2).getContents();
				return s1.equals(s2);
			}
			protected Object[] getChildren(Object input) {
				if (input instanceof JavaNode) {
					return ((JavaNode)input).getChildren();
				}
				return null;
			}
		};
		
		fRoot= (SimpleJavaElement) differencer.findDifferences(false, null, null, null, oldStructure, newStructure);
		fHasHistory= (fRoot != null); // if no changes pretend that we had no history

		if (DEBUG) {
			if (fRoot != null) {
				fRoot.dump(0);
			}
		}
			
		if (memberDeleted[0]) {	// shape change because of deleted members
			fRoot= null;	// throw diffs away since hasChanged(..) must always return true
		}
	}
	
	/**
	 * Returns <code>true</code>
	 * <ul>
	 * <li>if the source of the given member has been changed, or
	 * <li>if the element has been deleted, or
	 * <li>if the element has been newly created
	 * </ul>
	 * after the initial timestamp.
	 * 
	 * @exception AssertionFailedException if member is null or member is not a member of this CU.
	 */
	public boolean hasChanged(IMember member) {
		//Assert.isNotNull(member);
		ICompilationUnit cu= member.getCompilationUnit();
		if (cu.isWorkingCopy()) {
			cu= (ICompilationUnit) cu.getOriginalElement();
		}
		//Assert.isTrue(cu.equals(fCompilationUnit));
		
		if (fRoot == null) {
			if (fHasHistory) {
				return true;	// pessimistic: we have a history but we couldn't use it for some reason
			}
			return false;	// optimistic: we have no history, so assume that member hasn't changed
		}
		
		String[] path= createPath(member);
		
		if (DEBUG) {
			for (int i= 0; i < path.length; i++) {	
				System.out.print(path[i] + ' ');
			}
			System.out.println();
		}
		
		return fRoot.find(path, 0);
	}
	
	//---- private stuff ----------------------------------------------------------------
	
	/**
	 * Parses the given input stream and returns a tree of JavaNodes
	 * or a null in case of failure.
	 */
	private static JavaNode parse(InputStream input) {
		
		char[] buffer= readString(input);		
		if (buffer != null) {									
			JavaNode root= new JavaNode(buffer);
			JavaParseTreeBuilder builder= new JavaParseTreeBuilder(root, buffer);
			SourceElementParser parser= new SourceElementParser(builder, new ProblemFactory(), new CompilerOptions(JavaCore.getOptions()));
			try {
				parser.parseCompilationUnit(builder, false);
			} catch (ParseError ex) {
				// parse error: bail out
				return null;
			}
			return root;
		} 
		return null;
	}
			
	private static String[] createPath(IJavaElement je) {
			
		// build a path starting at the given Java element and walk
		// up the parent chain until we reach a IWorkingCopy or ICompilationUnit
		List args= new ArrayList();
		while (je != null) {
			// each path component has a name that uses the same
			// conventions as a JavaNode name
			String name= getJavaElementID(je);
			if (name == null)
				return null;
			args.add(name);
			if (je instanceof IWorkingCopy || je instanceof ICompilationUnit)
				break;
			je= je.getParent();
		}
		
		// revert the path
		int n= args.size();
		String[] path= new String[n];
		for (int i= 0; i < n; i++) {
			path[i]= (String) args.get(n-1-i);
		}
			
		return path;
	}
	
	/**
	 * Returns a name for the given Java element that uses the same conventions
	 * as the JavaNode name of a corresponding element.
	 */
	private static String getJavaElementID(IJavaElement je) {
		
		if (je instanceof IMember && ((IMember)je).isBinary()) {
			return null;
		}
			
		StringBuffer sb= new StringBuffer();
		
		switch (je.getElementType()) {
		case JavaElement.COMPILATION_UNIT:
			sb.append(JavaElement.JEM_COMPILATIONUNIT);
			break;
		case JavaElement.TYPE:
			sb.append(JavaElement.JEM_TYPE);
			sb.append(je.getElementName());
			break;
		case JavaElement.FIELD:
			sb.append(JavaElement.JEM_FIELD);
			sb.append(je.getElementName());
			break;
		case JavaElement.METHOD:
			sb.append(JavaElement.JEM_METHOD);
			
			IMethod method= (IMethod) je;			
			sb.append(method.getElementName());
			
			// parameters
			sb.append('(');
			
			String[] types= method.getParameterTypes();
			int nParams= types != null ? types.length : 0;
			
			for (int i= 0; i < nParams; i++) {
				if (i > 0)
					sb.append(", "); //$NON-NLS-1$
				if (types != null)
					sb.append(unqualifyName(Signature.getSimpleName(Signature.toString(types[i]))));
			}
			sb.append(')');
			break;
		case JavaElement.INITIALIZER:
			String id= je.getHandleIdentifier();
			int pos= id.lastIndexOf(JavaElement.JEM_INITIALIZER);
			if (pos >= 0)
				sb.append(id.substring(pos));
			break;
		case JavaElement.PACKAGE_DECLARATION:
			sb.append(JavaElement.JEM_PACKAGEDECLARATION);
			break;
		case JavaElement.IMPORT_CONTAINER:
			sb.append('<');
			break;
		case JavaElement.IMPORT_DECLARATION:
			sb.append(JavaElement.JEM_IMPORTDECLARATION);
			sb.append(je.getElementName());			
			break;
		default:
			return null;
		}
		return sb.toString();
	}
	
	private static String unqualifyName(String qualifiedName) {
		int index= qualifiedName.lastIndexOf('/');
		if (index > -1) {
			return qualifiedName.substring(index + 1);
		}
		return qualifiedName;
	}

	/**
	 * Returns null if an error occurred.
	 */
	private static char[] readString(InputStream is) {
		if (is == null) {
			return null;
		}
		BufferedReader reader= null;
		try {
			StringBuffer buffer= new StringBuffer();
			char[] part= new char[2048];
			int read= 0;
			reader= new BufferedReader(new InputStreamReader(is, ResourcesPlugin.getEncoding()));

			while ((read= reader.read(part)) != -1) {
				buffer.append(part, 0, read);
			}
			
			char[] b= new char[buffer.length()];
			buffer.getChars(0, b.length, b, 0);
			return b;
			
		} catch (IOException ex) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}
}
