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
public class StatementStepArgument {

    public static void method1(String firstName, String lastName)
    {
        System.out.println("Method1 " +
                           " : first name: " + firstName +
                           " : last name: " + lastName);
    }

    public static void main(String[] args) {

        String firstName = "John";
        String lastName = "Smith";

        lastName = "Smith";

        method1(firstName,
                lastName
                );

        System.out.println("End call method1");

    } /* main( args ) */

} /* TestApp */