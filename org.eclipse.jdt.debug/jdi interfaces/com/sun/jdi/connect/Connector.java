/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.connect;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Connector {
	public Map defaultArguments();
	public Transport transport();
	public String name();
	public String description();

	public interface Argument extends Serializable {
		public String name();
		public String label();
		public String description();
		public boolean isValid(String arg1);
		public String value();
		public void setValue(String arg1);
		public boolean mustSpecify();
	}
	
	public interface StringArgument extends Connector.Argument {
	}

	public interface IntegerArgument extends Connector.Argument {
		public void setValue(int arg1);
		public boolean isValid(int arg1);
		public String stringValueOf(int arg1);
		public int intValue();
		public int max();
		public int min();
	}

	public interface BooleanArgument extends Connector.Argument {
		public void setValue(boolean arg1);
		public boolean isValid(String arg1);
		public String stringValueOf(boolean arg1);
		public boolean booleanValue();
	}
	
	public interface SelectedArgument extends Connector.Argument {
		public List choices();
		public boolean isValid(String arg1);
	}
}
