package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;

/**
 * @author jburns
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ReturnInstruction extends CompoundInstruction {

	/**
	 * Constructor for ReturnInstruction.
	 * @param start
	 */
	public ReturnInstruction(int start) {
		super(start);
	}

	/**
	 * @see Instruction#execute()
	 */
	public void execute() throws CoreException {
		stop();
	}

}
