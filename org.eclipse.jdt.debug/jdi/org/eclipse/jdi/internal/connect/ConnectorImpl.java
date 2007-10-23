/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ivan Popov - Bug 184211: JDI connectors throw NullPointerException if used separately
 *     			from Eclipse
 *******************************************************************************/
package org.eclipse.jdi.internal.connect;


import java.io.IOException;
import java.util.List;

import org.eclipse.jdi.Bootstrap;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.VirtualMachineManagerImpl;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Transport;
import com.sun.jdi.connect.spi.Connection;


/**
 * this class implements the corresponding interfaces
 * declared by the JDI specification. See the com.sun.jdi package
 * for more information.
 *
 */
public abstract class ConnectorImpl implements Connector {
	/** Virtual machine manager that created this connector. */
	private VirtualMachineManagerImpl fVirtualMachineManager;

	/** Transport that is used for communication. */
	protected Transport fTransport;
	/** Virtual Machine that is connected. */
	protected VirtualMachineImpl fVirtualMachine;
	
	/**
	 * Creates a new Connector.
	 */	
	public ConnectorImpl(VirtualMachineManagerImpl virtualMachineManager) {
		fVirtualMachineManager = virtualMachineManager;
	}

	/**
	 * @return Returns Virtual Machine Manager.
	 */
	public VirtualMachineManagerImpl virtualMachineManager() {
		return fVirtualMachineManager;
	}
	
	/**
	 * @return Returns Virtual Machine Manager.
	 */
	public VirtualMachineImpl virtualMachine() {
		return fVirtualMachine;
	}

	/**
	 * @return Returns a human-readable description of this connector and its purpose.
	 */	
	public abstract String description();
	
	/**
	 * @return Returns a short identifier for the connector.
	 */	
	public abstract String name();
	
	/**
	 * Assigns Transport.
	 */	
	/*package*/ void setTransport(Transport transport) {
		fTransport = transport;
	}

	/**
	 * @return Returns the transport mechanism used by this connector to establish connections with a target VM.
	 */	
	public Transport transport() {
		return fTransport;
	}
	
	/**
	 * Closes connection with Virtual Machine.
	 */	
	/*package*/ synchronized void close() {
		virtualMachineManager().removeConnectedVM(fVirtualMachine);
	}
	
	/**
	 * @return Returns a connected Virtual Machine.
	 */	
	protected VirtualMachine establishedConnection(Connection connection) throws IOException {
	    fVirtualMachine = (VirtualMachineImpl) Bootstrap.virtualMachineManager().createVirtualMachine(connection);
		return fVirtualMachine;
	}
	
	/**
	 * Argument class for arguments that are used to establish a connection.
	 */
	public abstract class ArgumentImpl implements com.sun.jdi.connect.Connector.Argument {
		/**
		 * Serial version id. 
		 */
		private static final long serialVersionUID = 8850533280769854833L;
		
		private String fName;
		private String fDescription;
		private String fLabel;
		private boolean fMustSpecify;

	 	protected ArgumentImpl(String name, String description, String label, boolean mustSpecify) {
	 		fName = name;
	 		fLabel = label;
	 		fDescription = description;
	 		fMustSpecify = mustSpecify;
	 	}

		public String name() { 
			return fName;
		}
		
		public String description() {
			return fDescription;
		}
		
		public String label() {
			return fLabel;
		}
		
		public boolean mustSpecify() {
			return fMustSpecify;
		}
		
		public abstract String value();
		public abstract void setValue(String value);
		public abstract boolean isValid(String value);
		public abstract String toString();
	}
	
	public class StringArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.StringArgument {
        private static final long serialVersionUID = 6009335074727417445L;

        private String fValue;

	 	protected StringArgumentImpl(String name, String description, String label, boolean mustSpecify) {
	 		super(name, description, label, mustSpecify);
	 	}
	 	
		public String value() {
			return fValue;
		}
		
		public void setValue(String value) {
			fValue = value;
		}

		public boolean isValid(String value) {
			return true;
		}

		public String toString() {
			return fValue;
		}

	}
	
	public class IntegerArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.IntegerArgument {
        private static final long serialVersionUID = 6009335074727417445L;
        private Integer fValue;
		private int fMin;
		private int fMax;

	 	protected IntegerArgumentImpl(String name, String description, String label, boolean mustSpecify, int min, int max) {
	 		super(name, description, label, mustSpecify);
	 		fMin = min;
	 		fMax = max;
	 	}
	 	
		public String value() {
			return (fValue == null) ? null : fValue.toString();
		}
		
		public void setValue(String value) {
			fValue = new Integer(value);
		}

		public boolean isValid(String value) {
			Integer val;
			try {
				val = new Integer(value);
			} catch (NumberFormatException e) {
				return false;
			}
			return isValid(val.intValue());
		}

		public String toString() {
			return value();
		}

		public int intValue() {
			return fValue.intValue();
		}
		
		public void setValue(int value) {
			fValue = new Integer(value);
		}

		public int min() {
			return fMin;
		}
		
		public int max() {
			return fMax;
		}
		
		public boolean isValid(int value) {
			return fMin <= value && value <= fMax;
		}

		public String stringValueOf(int value) {
			return new Integer(value).toString();
		}
	}
	
	public class BooleanArgumentImpl extends ArgumentImpl implements com.sun.jdi.connect.Connector.BooleanArgument {
	    private static final long serialVersionUID = 6009335074727417445L;
        private Boolean fValue;
		
	 	protected BooleanArgumentImpl(String name, String description, String label, boolean mustSpecify) {
	 		super(name, description, label, mustSpecify);
	 	}
	 	
		public String value() {
			return (fValue == null) ? null : fValue.toString();
		}
		
		public void setValue(String value) {
			fValue = Boolean.valueOf(value);
		}

		public boolean isValid(String value) {
			return true;
		}
		
		public String toString() {
			return value();
		}

		public boolean booleanValue() {
			return fValue.booleanValue();
		}

		public void setValue(boolean value) {
			fValue = Boolean.valueOf(value);
		}
		
		public String stringValueOf(boolean value) {
			return Boolean.valueOf(value).toString();
		}
	}
	
	public class SelectedArgumentImpl extends StringArgumentImpl implements com.sun.jdi.connect.Connector.SelectedArgument {
        private static final long serialVersionUID = 6009335074727417445L;
        private List fChoices;
		
	 	protected SelectedArgumentImpl(String name, String description, String label, boolean mustSpecify, List choices) {
	 		super(name, description, label, mustSpecify);
	 		fChoices = choices;
	 	}
	 	
		public List choices() {
			return fChoices;
		}
		
		public boolean isValid(java.lang.String value) {
			return fChoices.contains(value);
		}
	}
}
