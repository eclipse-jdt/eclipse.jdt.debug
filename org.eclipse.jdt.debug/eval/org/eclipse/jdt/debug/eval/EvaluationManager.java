package org.eclipse.jdt.debug.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.eval.LocalEvaluationEngine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;

/**
 * The evaluation manager provides factory methods for
 * creating evaluation engines.
 * <p>
 * Clients are not intended subclass or instantiate this
 * class.
 * </p>
 * @see org.eclipse.jdt.debug.eval.IEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IAstEvaluationEngine
 * @see org.eclipse.jdt.debug.eval.IEvaluationResult
 * @see org.eclipse.jdt.debug.eval.IEvaluationListener
 * @since 2.0
 */
public class EvaluationManager {
		
	
	/**
	 * Not to be instantiated
	 */
	private EvaluationManager() {
	}
				
	/**
	 * Creates and returns a new evaluation engine that
	 * performs evaluations for local Java applications
	 * by deploying class files.
	 * 
	 * @param project the java project in which snippets
	 *  are to be compiled
	 * @param vm the java debug target in which snippets
	 *  are to be evaluated
	 * @param directory the directory where support class files
	 *  are deployed to assist in the evaluation. The directory
	 *  must exist.
	 */
	public static IClassFileEvaluationEngine newClassFileEvaluationEngine(IJavaProject project, IJavaDebugTarget vm, File directory) {
		return new LocalEvaluationEngine(project, vm, directory);
	}
	 
	/**
	 * Creates and returns a new evaluation engine that performs
	 * evaluations by creating an abstract syntax tree (AST) represention
	 * of an expression.
	 */
	public static ASTEvaluationEngine newAstEvaluationEngine(IJavaProject project, IJavaDebugTarget target) {
		return new ASTEvaluationEngine(project, target);
	}

}

