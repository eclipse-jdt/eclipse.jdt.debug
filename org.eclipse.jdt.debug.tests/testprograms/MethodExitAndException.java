/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/

class MyException extends Exception {

    public final int value;

    public MyException(int value) {
        this.value = value;
    }
}

public class MethodExitAndException {
    public static void main(String[] args) {
        int x = f();
        try {
            g(x);
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    private static int f() {
        return 123;
    }

    private static void g(int i) throws MyException {
        throw new MyException(i);
    }
}
