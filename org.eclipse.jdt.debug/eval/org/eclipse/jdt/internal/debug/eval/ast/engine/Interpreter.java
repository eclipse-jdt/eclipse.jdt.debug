/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.engine;

import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.eval.ast.model.IRuntimeContext;
import org.eclipse.jdt.debug.eval.ast.model.IValue;
import org.eclipse.jdt.debug.eval.ast.model.IVariable;
import org.eclipse.jdt.debug.eval.ast.model.IVirtualMachine;

/**
 * @version 	1.0
 * @author
 */
public class Interpreter {
	private Instruction[] fInstructions;
	private int fInstructionCounter;
	private IRuntimeContext fContext;
	private Stack fStack;
	
	public Interpreter(Instruction[] instructions, IRuntimeContext context) {
		fInstructions= instructions;
		fContext= context;
	}
	
	public void execute() throws CoreException {
		reset();
		while(fInstructionCounter < fInstructions.length) {
			Instruction instruction= fInstructions[fInstructionCounter++];
			instruction.setInterpreter(this);
			instruction.execute();
			instruction.setInterpreter(null);
		}
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
	
	public IValue getResult() {
		if (fStack.isEmpty())
			return getContext().getVM().voidValue();
		Object top= fStack.peek();
		if (top instanceof IVariable) {
			try {
				return ((IVariable)top).getValue();
			} catch (CoreException exception) {
				return getContext().getVM().newValue(exception.getStatus().getMessage());
			}
		}
		if (top instanceof IValue) {
			return (IValue)top;
		}
		// XXX: exception
		return null;		
	}	
}
