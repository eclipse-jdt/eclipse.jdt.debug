/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.eval.model.ICompiledExpression;
import org.eclipse.jdt.debug.eval.model.IRuntimeContext;
import org.eclipse.jdt.debug.eval.model.IValue;
import org.eclipse.jdt.internal.debug.eval.ast.engine.*;

/**
 * @version 	1.0
 * @author
 */
public class InstructionSequence implements ICompiledExpression {

	private List fInstructions;
	/**
	 * A collection of errors (Message) that occurred while
	 * creating this expression
	 */
	private List fErrors;
	private String fSnippet;
	private CoreException fException;
	
	public InstructionSequence(String snippet) {
		fInstructions= new ArrayList(10);
		fErrors= new ArrayList();
		fSnippet= snippet;
	}
	
	/*
	 * @see ICompiledExpression#evaluate(IRuntimeContext)
	 */
	public IValue evaluate(IRuntimeContext context) {
		Interpreter interpreter= new Interpreter(getInstructions(), context);
		try {
			interpreter.execute();
		} catch (CoreException exception) {
			fException= exception;
		}
		return interpreter.getResult();
	}
	
	/**
	 * @see ICompiledExpression#getException()
	 */
	public CoreException getException() {
		return fException;
	}
	
	/**
	 * @see ICompiledExpression#getSnippet()
	 */
	public String getSnippet() {
		return fSnippet;
	}
	
	/**
	 * Adds the given error to the list of errors that occurred
	 * while compiling this instruction sequence
	 */
	public void addError(Message error) {
		fErrors.add(error);
	}
	
	/**
	 * @see ICompiledExpression#hasErrors()
	 */
	public boolean hasErrors() {
		return !fErrors.isEmpty();
	}
	
	/**
	 * @see ICompiledExpression#getErrors()
	 */
	public Message[] getErrors() {
		return (Message[])fErrors.toArray(new Message[fErrors.size()]);
	}

	/**
	 * Answers the array of instructions, or an empty array.
	 */
	private Instruction[] getInstructions() {
		int size= fInstructions.size();
		Instruction[] instructions= new Instruction[size];
		if (size > 0) {
			fInstructions.toArray(instructions);
		}
		return instructions;
	}
	
	/**
	 * Answer the instruction at the given address
	 */
	public Instruction getInstruction(int address) {
		return (Instruction)fInstructions.get(address);
	}
	
	/**
	 * Add the given instruction to the end of the list
	 */
	public void add(Instruction instruction) {
		fInstructions.add(instruction);
	}
	
	/**
	 * Answers true if there are no instructions in this sequence
	 */
	public boolean isEmpty() {
		return fInstructions.isEmpty();
	}

	/**
	 * Inserts the instruction at the given index.  If
	 * the index is less than 0 or greater than the current
	 * instruction count, the instruction is added at the end
	 * of the sequence.
	 * 
	 * Instructs the instructions to update their program counters.
	 */
	public void insert(Instruction instruction, int index) {
		fInstructions.add(index, instruction);
	}
	
	public Instruction get(int address) {
		return (Instruction)fInstructions.get(address);
	}
	
	public int getEnd() {
		return fInstructions.size() - 1;
	}
}
