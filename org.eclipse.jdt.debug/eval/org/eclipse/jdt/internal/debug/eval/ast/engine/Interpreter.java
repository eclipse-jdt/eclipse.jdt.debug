/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.Instruction;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

public class Interpreter {
	private Instruction[] fInstructions;
	private int fInstructionCounter;
	private IRuntimeContext fContext;
	private Stack fStack;
	private IJavaValue fLastValue;
	
	/**
	 * The list of internal variables
	 */
	private Map fInternalVariables;
	
	private boolean fStopped= false;
	
	public Interpreter(InstructionSequence instructions, IRuntimeContext context) {
		fInstructions= instructions.getInstructions();
		fContext= context;
		fInternalVariables= new HashMap();
	}
	
	public void execute() throws CoreException {
		reset();
		while(fInstructionCounter < fInstructions.length && !fStopped) {
			Instruction instruction= fInstructions[fInstructionCounter++];
			instruction.setInterpreter(this);
			instruction.execute();
			instruction.setInterpreter(null);
		}
	}
	
	public void stop() {
		fStopped= true;
	}

	private void reset() {
		fStack= new Stack();
		fInstructionCounter= 0;
	}
	
	/**
	 * Jumps to a given address
	 */
	public void jump(int offset) {
		fInstructionCounter+= offset;
	}		
	
	/**
	 * Pushes an object onto the stack
	 */
	public void push(Object object) {
		fStack.push(object);
	}

	/**
	 * Peeks at the top object of the stack
	 */
	public Object peek() {
		return fStack.peek();
	}		
	
	/**
	 * Pops an object off of the stack
	 */
	public Object pop() {
		return fStack.pop();
	}
	
	/**
	 * Answers the context for the interpreter
	 */
	public IRuntimeContext getContext() {
		return fContext;
	}
	
	public IJavaValue getResult() {
		if (fStack == null || fStack.isEmpty()) {
			if (fLastValue == null) {
				return getContext().getVM().voidValue();
			}
			return fLastValue;
		}
		Object top= fStack.peek();
		if (top instanceof IJavaVariable) {
			try {
				return (IJavaValue)((IJavaVariable)top).getValue();
			} catch (CoreException exception) {
				return getContext().getVM().newValue(exception.getStatus().getMessage());
			}
		}
		if (top instanceof IJavaValue) {
			return (IJavaValue)top;
		}
		// XXX: exception
		return null;		
	}
	
	public void setLastValue(IJavaValue value) {
		fLastValue= value;
	}
	
	/**
	 * Create a new variable in the interpreter with the given name
	 * and the given type.
	 * 
	 * @param name the name of the variable to create.
	 * @param type the type of the variable to create.
	 * @return the created variable.
	 */
	public IVariable createInternalVariable(String name, IJavaType referencType) {
		IVariable var= new InterpreterVariable(name, referencType, fContext.getVM());
		fInternalVariables.put(name, var);
		return var;
	}
	
	/**
	 * Return the variable with the given name.
	 * This method only looks in the list of internal variable (i.e. created by
	 * Interpreter#createInternalVariable(String, IJavaType))
	 * 
	 * @param name the name of the variable to retrieve.
	 * @return the corresponding variable, or <code>null</code> if there is none.
	 */	
	public IVariable getInternalVariable(String name) {
		return (IVariable)fInternalVariables.get(name);
	}
}
