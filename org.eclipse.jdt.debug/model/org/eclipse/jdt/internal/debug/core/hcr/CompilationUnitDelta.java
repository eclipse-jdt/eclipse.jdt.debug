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
package org.eclipse.jdt.internal.debug.core.hcr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * A <code>CompilationUnitDelta</code> represents the source code changes
 * between a CU in the workspace and the same CU at some point in the past (from
 * the local history).
 * <p>
 * This functionality is used in the context of Hot Code Replace to determine
 * which stack frames are affected (and need to be dropped) by a class reload in
 * the Java VM.
 * <p>
 * Typically a <code>CompilationUnitDelta</code> object is generated for a CU
 * when the associated class is replaced in the VM.
 */
public class CompilationUnitDelta {

	/**
	 * AST of the current source code
	 */
	private CompilationUnit fCurrentAst;
	/**
	 * AST of the previous source code
	 */
	private CompilationUnit fPrevAst;

	/**
	 * AST parser
	 */
	private ASTParser fParser = null;

	/**
	 * AST matcher
	 */
	private ASTMatcher fMatcher = null;

	private boolean fHasHistory = false;

	/**
	 * Creates a new
	 * <code>CompilationUnitDelta object that calculates and stores
	 * the changes of the given CU since some point in time.
	 */
	public CompilationUnitDelta(ICompilationUnit cu, long timestamp)
			throws CoreException {

		if (cu.isWorkingCopy()) {
			cu = cu.getPrimary();
		}

		// find underlying file
		IFile file = (IFile) cu.getUnderlyingResource();

		// get available editions
		IFileState[] states = file.getHistory(null);
		if (states == null || states.length <= 0) {
			return;
		}
		fHasHistory = true;

		IFileState found = null;
		// find edition just before the given time stamp
		for (IFileState state : states) {
			long d = state.getModificationTime();
			if (d < timestamp) {
				found = state;
				break;
			}
		}

		if (found == null) {
			found = states[states.length - 1];
		}

		InputStream oldContents = null;
		InputStream newContents = null;
		try {
			oldContents = found.getContents();
			newContents = file.getContents();
		} catch (CoreException ex) {
			return;
		}

		fPrevAst = parse(oldContents, cu);
		fCurrentAst = parse(newContents, cu);
	}

	/**
	 * Returns <code>true</code>
	 * <ul>
	 * <li>if the source of the given member has been changed, or
	 * <li>if the element has been deleted, or
	 * <li>if the element has been newly created
	 * </ul>
	 * after the initial timestamp.
	 */
	public boolean hasChanged(String methodName, String signature) {
		if (!fHasHistory) {
			return false; // optimistic: we have no history, so assume that
							// member hasn't changed
		}
		if (fPrevAst == null || fCurrentAst == null) {
			return true; // pessimistic: unable to build parse trees
		}
		MethodSearchVisitor visitor = new MethodSearchVisitor();
		MethodDeclaration prev = findMethod(fPrevAst, visitor, methodName,
				signature);
		if (prev != null) {
			MethodDeclaration curr = findMethod(fCurrentAst, visitor,
					methodName, signature);
			if (curr != null) {
				return !getMatcher().match(prev, curr);
			}
		}
		return true;
	}

	private MethodDeclaration findMethod(CompilationUnit cu,
			MethodSearchVisitor visitor, String name, String signature) {
		visitor.setTargetMethod(name, signature);
		cu.accept(visitor);
		return visitor.getMatch();
	}

	// ---- private stuff
	// ----------------------------------------------------------------

	/**
	 * Parses the given input stream and returns a tree of JavaNodes or a null
	 * in case of failure.
	 */
	private CompilationUnit parse(InputStream input, ICompilationUnit cu) {

		char[] buffer = readString(input);
		if (buffer != null) {
			if (fParser == null) {
				fParser = ASTParser.newParser(AST.JLS4);
			}
			fParser.setSource(buffer);
			fParser.setProject(cu.getJavaProject());
			fParser.setResolveBindings(true);
			fParser.setKind(ASTParser.K_COMPILATION_UNIT);
			fParser.setUnitName(cu.getElementName());
			return (CompilationUnit) fParser.createAST(null);
		}
		return null;
	}

	/**
	 * Returns an AST matcher
	 * 
	 * @return AST matcher
	 */
	private ASTMatcher getMatcher() {
		if (fMatcher == null) {
			fMatcher = new ASTMatcher();
		}
		return fMatcher;
	}

	/**
	 * Returns null if an error occurred.
	 */
	private char[] readString(InputStream is) {
		if (is == null) {
			return null;
		}
		BufferedReader reader = null;
		try {
			StringBuffer buffer = new StringBuffer();
			char[] part = new char[2048];
			int read = 0;
			reader = new BufferedReader(new InputStreamReader(is,
					ResourcesPlugin.getEncoding()));

			while ((read = reader.read(part)) != -1) {
				buffer.append(part, 0, read);
			}

			char[] b = new char[buffer.length()];
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
