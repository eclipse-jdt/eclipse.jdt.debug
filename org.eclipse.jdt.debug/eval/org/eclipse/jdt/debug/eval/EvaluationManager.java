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
package org.eclipse.jdt.debug.eval;

import java.io.File;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.eval.LocalEvaluationEngine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;

/**
 * The evaluation manager provides factory methods for creating evaluation
 * engines.
 * 
 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IEvaluationResult
 * @see org.eclipse.jdt.debug.eval.IEvaluationListener
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class EvaluationManager {

	/**
	 * Not to be instantiated
	 */
	private EvaluationManager() {
	}

	/**
	 * Creates and returns a new evaluation engine that performs evaluations for
	 * local Java applications by deploying class files.
	 * 
	 * @param project
	 *            the Java project in which expressions are to be compiled
	 * @param target
	 *            the Java debug target in which expressions are to be evaluated
	 * @param directory
	 *            the directory where support class files are deployed to assist
	 *            in the evaluation. The directory must exist.
	 * @return an evaluation engine
	 */
	public static IClassFileEvaluationEngine newClassFileEvaluationEngine(
			IJavaProject project, IJavaDebugTarget target, File directory) {
		return new LocalEvaluationEngine(project, target, directory);
	}

	/**
	 * Creates and returns a new evaluation engine that performs evaluations by
	 * compiling expressions into abstract syntax trees (ASTs), and interpreting
	 * the AST over a JDI connection. This type of evaluation engine is capable
	 * of performing remote evaluations.
	 * 
	 * @param project
	 *            the Java project in which expressions are to be compiled
	 * @param target
	 *            the Java debug target in which expressions are to be evaluated
	 * @return an evaluation engine
	 */
	public static IAstEvaluationEngine newAstEvaluationEngine(
			IJavaProject project, IJavaDebugTarget target) {
		return new ASTEvaluationEngine(project, target);
	}
}
