/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sun.jdi.connect;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.html
 */
public interface Connector {
	public Map<String, Connector.Argument> defaultArguments();
	public String description();
	public String name();
	public Transport transport();
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.Argument.html
	 */
	public interface Argument extends Serializable {
		public String description();
		public boolean isValid(String arg1);
		public String label();
		public boolean mustSpecify();
		public String name();
		public void setValue(String arg1);
		public String value();
	}
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.StringArgument.html
	 */
	public interface StringArgument extends Connector.Argument {
		public boolean isValid(String arg1);
	}
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.IntegerArgument.html
	 */
	public interface IntegerArgument extends Connector.Argument {
		public int intValue();
		public boolean isValid(int arg1);
		public boolean isValid(String arg1);
		public int max();
		public int min();
		public void setValue(int arg1);
		public String stringValueOf(int arg1);
	}
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.BooleanArgument.html
	 */
	public interface BooleanArgument extends Connector.Argument {
		public boolean booleanValue();
		public boolean isValid(String arg1);
		public void setValue(boolean arg1);
		public String stringValueOf(boolean arg1);
	}
	/**
	 * See http://docs.oracle.com/javase/6/docs/jdk/api/jpda/jdi/com/sun/jdi/connect/Connector.SelectedArgument.html
	 */
	public interface SelectedArgument extends Connector.Argument {
		public List<String> choices();
		public boolean isValid(String arg1);
	}
}
