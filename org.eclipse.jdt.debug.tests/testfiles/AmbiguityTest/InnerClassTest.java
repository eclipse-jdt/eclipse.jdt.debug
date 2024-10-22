/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
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
/**
 * Test class
 */
class InnerClassTest {
	public static void main(String[] ar) {

	}
	class innerL1 {
		void check() {
			System.out.println("EXPECTED_INNERCLASS");
		}
		class innerL2 {
			void check2() {
				System.out.println("EXPECTED_INNER-INNER_CLASS");
			}
		}
	}
}
	 