/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

public class StepFilterTwo {

	private StepFilterThree sf3;

	public StepFilterTwo() {
		sf3 = new StepFilterThree();
	}

	protected void go() {
		sf3.go();
	}
	
	void test() {
		for (int i = 0; i < 10; i++);
	}
	
	/**
	 * This test method should only be called by the contributed step filter tests
	 * @see TestContributedStepFilter
	 */
	void contributed() {
	}
}

