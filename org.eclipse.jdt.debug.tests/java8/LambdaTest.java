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

import java.util.function.Function;

public class LambdaTest {
    public static void main(String[] args) {
        try {
            Function<String, String> messengerA = createMessenger("A");
            Function<String, String> messengerB = createMessenger("B");
            
            System.out.println(messengerA.apply("Hi Matt"));
            System.out.println(messengerB.apply("Hi Raja"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static public Function<String, String> createMessenger(String messengerName) {
        return message -> {
            System.out.println("Inside Transformer " + messengerName);
            return message + " - From Transformer " + messengerName;
        };
    }
}
