/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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
public class StatementStepWithOperations {

    public static void main(String[] args) {
        String s1 = "A";
        String s2 = "B"; // <-- Breakpoint here

        test(
            s1 + s2,
            s2
        );

        System.out.println("Eclipse");
    }

    static void test(String a, String b) {}
}