import java.io.*;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

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
