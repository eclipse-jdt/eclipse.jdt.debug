/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.eval.ICompiledExpression;

public class InstructionSequence implements ICompiledExpression {

	private final List<Instruction> fInstructions;
	/**
	 * A collection of error messages (<code>String</code>) that occurred while
	 * creating this expression
	 */
	private final List<String> fErrors;
	private final String fSnippet;
	private CoreException fException;
	private final List<Integer> fProblemIDs;

	public InstructionSequence(String snippet) {
		fInstructions = new ArrayList<>(10);
		fErrors = new ArrayList<>();
		fSnippet = snippet;
		fProblemIDs = new ArrayList<>();
	}

	/**
	 * Returns the runtime exception that occurred while evaluating this
	 * expression or <code>null</code> if no exception occurred.
	 */
	public CoreException getException() {
		return fException;
	}

	/**
	 * @see ICompiledExpression#getSnippet()
	 */
	@Override
	public String getSnippet() {
		return fSnippet;
	}

	/**
	 * Adds the given error to the list of errors that occurred while compiling
	 * this instruction sequence
	 */
	public void addError(String error) {
		fErrors.add(error);
	}

	/**
	 * @see ICompiledExpression#hasErrors()
	 */
	@Override
	public boolean hasErrors() {
		return !fErrors.isEmpty();
	}

	/**
	 * @see ICompiledExpression#getErrors()
	 * @deprecated
	 */
	@Override
	@Deprecated
	public Message[] getErrors() {
		Message[] messages = new Message[fErrors.size()];
		int i = 0;
		for (String errorMsg : fErrors) {
			messages[i++] = new Message(errorMsg, -1);
		}
		return messages;
	}

	/**
	 * @see org.eclipse.jdt.debug.eval.ICompiledExpression#getErrorMessages()
	 */
	@Override
	public String[] getErrorMessages() {
		return fErrors.toArray(new String[fErrors.size()]);
	}

	/**
	 * Answers the array of instructions, or an empty array.
	 */
	public Instruction[] getInstructions() {
		int size = fInstructions.size();
		Instruction[] instructions = new Instruction[size];
		if (size > 0) {
			fInstructions.toArray(instructions);
		}
		return instructions;
	}

	/**
	 * Answer the instruction at the given address
	 */
	public Instruction getInstruction(int address) {
		return fInstructions.get(address);
	}

	/**
	 * Add the given instruction to the end of the list
	 */
	public void add(Instruction instruction) {
		fInstructions.add(instruction);
	}

	public int indexOf(Instruction instruction) {
		return fInstructions.indexOf(instruction);
	}

	/**
	 * Answers true if there are no instructions in this sequence
	 */
	public boolean isEmpty() {
		return fInstructions.isEmpty();
	}

	/**
	 * Inserts the instruction at the given index. If the index is less than 0
	 * or greater than the current instruction count, the instruction is added
	 * at the end of the sequence.
	 *
	 * Instructs the instructions to update their program counters.
	 */
	public void insert(Instruction instruction, int index) {
		fInstructions.add(index, instruction);
	}

	public Instruction get(int address) {
		return fInstructions.get(address);
	}

	public int getEnd() {
		return fInstructions.size() - 1;
	}

	/**
	 * Adds the <code>IProblem<code> id of the error.
	 */
	public void addProblemID(int probID) {
		fProblemIDs.add(probID);
	}

	/**
	 * @see org.eclipse.jdt.debug.eval.ICompiledExpression#getProblemIDs()
	 */
	@Override
	public List<Integer> getProblemIDs() {
		return fProblemIDs;
	}

}
