/*******************************************************************************
 * Copyright (c) Aug 28, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class TestContributedStepFilterClass {
	
	public static void main(String[] args) {
		StepFilterTwo two = new StepFilterTwo();
		two.go();
		two.contributed();
		two.go();
	}
}
