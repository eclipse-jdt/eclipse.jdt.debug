/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * StackTraces
 */
public class StackTraces {

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            try {
                String fred = null;
                fred.toString();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
