package com.sun.jdi.connect;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public interface Connector {
	public java.util.Map defaultArguments();
	public com.sun.jdi.connect.Transport transport();
	public java.lang.String name();
	public java.lang.String description();

	public interface Argument extends java.io.Serializable {
		public java.lang.String name();
		public java.lang.String label();
		public java.lang.String description();
		public boolean isValid(java.lang.String arg1);
		public java.lang.String value();
		public void setValue(String arg1);
		public boolean mustSpecify();
	}
	
	public interface StringArgument extends com.sun.jdi.connect.Connector.Argument {
	}

	public interface IntegerArgument extends com.sun.jdi.connect.Connector.Argument {
		public void setValue(int arg1);
		public boolean isValid(int arg1);
		public java.lang.String stringValueOf(int arg1);
		public int intValue();
		public int max();
		public int min();
	}

	public interface BooleanArgument extends com.sun.jdi.connect.Connector.Argument {
		public void setValue(boolean arg1);
		public boolean isValid(String arg1);
		public java.lang.String stringValueOf(boolean arg1);
		public boolean booleanValue();
	}
	
	public interface SelectedArgument extends com.sun.jdi.connect.Connector.Argument {
		public java.util.List choices();
		public boolean isValid(java.lang.String arg1);
	}
}
