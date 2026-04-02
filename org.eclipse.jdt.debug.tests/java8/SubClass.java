/*******************************************************************************
 *  Copyright (c) 2026 IBM Corporation.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
public class SubClass extends SuperClass {
    private boolean fInitialized;

    public SubClass() {
        fInitialized = false;
        System.out.println("SubClass constructor");
    }

    public static void main(String[] args) {
        new SubClass();
    }
}