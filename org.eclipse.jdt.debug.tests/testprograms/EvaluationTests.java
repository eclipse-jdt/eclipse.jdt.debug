import java.util.Date;
import java.util.Vector;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class EvaluationTests {

	protected int fInt= 5;
	protected String fString= "testing";
	protected static final String CONSTANT= "constant";
	private Date fADate= new Date();

	public static void main(java.lang.String[] args) {
		EvaluationTests tests= new EvaluationTests(); //line 12
		tests.method();
	}

	public void method() {
		System.out.println(returnInt());
		System.out.println(returnDate());
		int x= 5; //line 19
		System.out.println(x);
		Vector v= new Vector();
		v.isEmpty();
	}

	public int returnInt() {
		return 7;
	}

	public Date returnDate() {
		return new Date();
	}
}

