import java.util.Date;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

public class InstanceVariablesTests {
	public void nop() {
	}
	
	public InstanceVariablesTests() {
		nop(); // should see this.*Str with correct values
	}

	public static void main(String[] args) {
		InstanceVariablesTests ivt = new InstanceVariablesTests();
		ivt.run();
	}
	
	public void run() {
		nop(); // should see this
		InstanceVariablesTests ivt = new IVTSubclass();
		ivt.run();
	}
	
	public String pubStr = "public";
	protected String protStr = "protected";
	/* default */ String defStr = "default";
	private String privStr = "private";
	protected String nullStr= null;
	protected Date date= new Date();
	protected Date nullDate= null;
}

class IVTSubclass extends InstanceVariablesTests {
	public void run() {
		nop();
	}
	
	public String pubStr = "redefined public";
	protected String protStr = "redefined protected";
	/* default */ String defStr = "redefined default";
}