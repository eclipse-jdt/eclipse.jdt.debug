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
import java.io.*;


public class TestIO {
	
	public static void main(String[] args) {
		TestIO tio = new TestIO();
		try {
			tio.testBaby();
		} catch (EOFException e) {
		}
	}

	public void testBaby() throws EOFException {
		throw new EOFException("test");
	}
}
