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
import java.util.Vector;

public class ConsoleStackTrace {
	
	public static void main(String[] args) {
		Vector v = null;
		int repeat = Integer.parseInt(args[0]);
		try {
		    v.toString();
		} catch (NullPointerException e) {
		    System.out.println("---- BEGIN ----");
		    for (int i = 0; i < repeat; i++) {
		        e.printStackTrace();
		    }
		    System.out.println("---- END ----");
		}
	}

}
