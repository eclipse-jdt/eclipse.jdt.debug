/*******************************************************************************
 * Copyright (c) Mar 6, 2013 IBM Corporation and others.
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
public class bug401270 {
    public static void main(String[] args) {
    	boolean b = (true==true==true==true==true);
    	b = !(true==true==true==true==true);
    	b = (true&&true&&true&&true&&true);
    	b = !(true&&true&&true&&true&&true);
    	b = true&&true||false;
    	b = (1<=2==true||false);
        b = !(1<=2==true||false);
        b = (true != false && false);
        b = !(true != false && false);
        b = (true||true||true||true||true);
        b = !(true||true||true||true||true);
        b = (true==true||true!=true&&true);
        b = !(true==true||true!=true&&true);
    }
}