/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
public class DependencyTest {
	void test_java_step() {
		TestDebugHover at = new TestDebugHover();
		int a = 4;
		int bz = 5;
		int c = add(a, bz);
		System.out.println("Completed..");
	}
	int add(int x, int y) {
		return x + y;
	}

	public static void main(String[] args) {
		new DependencyTest().test_java_step();
	}
	
}
class TestDebugHover {
	public TestDebugHover() {
		System.out.println("Testing..");
	}
}