/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
