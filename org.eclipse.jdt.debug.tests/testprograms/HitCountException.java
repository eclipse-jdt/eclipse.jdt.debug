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


public class HitCountException {

	public static void main(String[] args) {
		HitCountException mte = new HitCountException();
		mte.go();
	}
	
	private void go() {
		try {
			generateNPE();
		} catch (NullPointerException npe) {
		}
		
		generateNPE();
	}
	
	void generateNPE() {
		Vector vector = null;
		if (1 > 2) {
			vector = new Vector();
		}
		vector.add("item");
	}
}
