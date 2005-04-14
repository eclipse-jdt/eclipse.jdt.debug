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

import java.util.Vector;

/**
 * A loop adding to a collection
 */
public class VariableDetails {

	public static void main(String[] args) {
		Vector v = new Vector(200);
		for (int i = 0; i < 100; i++) {
			v.add(new Integer(i));
		}
		System.out.println(v);
	}
}
