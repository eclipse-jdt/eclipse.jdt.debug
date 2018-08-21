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

public class ConsoleVariableLineLength {
	
	public static void main(String[] args) {
		int repeat = Integer.parseInt(args[0]);
		System.out.println("---- START ----");
	    for (int i = 0; i < repeat; i++) {
	        System.out.println("---------1---------2---------3---------4---------5---------6");
	        System.out.println("---------1---------2---------3---------4---------5---------6---------7---------8");
	        System.out.println("---------1---------2---------3---------4---------5---------6---------7---------8-");
	        System.out.println("---------1---------2---------3---------4---------5---------6---------7---------8---------9");
	    }
	    System.out.println("---- END ----");
	}

}
