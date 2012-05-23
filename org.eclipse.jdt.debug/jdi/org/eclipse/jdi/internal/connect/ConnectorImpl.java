/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
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
	 * @return Returns a human-readable description of this connector and its
	 *         purpose.
	 */
	public abstract String description();

	/**
	 * @return Returns a short identifier for the connector.
	 */
	public abstract String name();

	/**
	 * Assigns Transport.
	 */
	/* package */void setTransport(Transport transport) {
		fTransport = transport;
	}

	/**
	 * @return Returns the transport mechanism used by this connector to
	 *         establish connections with a target VM.
	 */
	public Transport transport() {
		return fTransport;
	}

	/**
	 * Closes connection with Virtual Machine.
	 */
	/* package */synchronized void close() {
		virtualMachineManager().removeConnectedVM(fVirtualMachine);
	}

	/**
	 * @return Returns a connected Virtual Machine.
	 */
	protected VirtualMachine establishedConnection(Connection connection)
			throws IOException {
		fVirtualMachine = (VirtualMachineImpl) Bootstrap
				.virtualMachineManager().createVirtualMachine(connection);
		return fVirtualMachine;
	}

	/**
	 * Argument class for arguments that are used to establish a connection.
	 */
	public abstract class ArgumentImpl implements
			com.sun.jdi.connect.Connector.Argument {
		/**
		 * Serial version id.
		 */
		private static final long serialVersionUID = 8850533280769854833L;

		private String fName;
		private String fDescription;
		private String fLabel;
		private boolean fMustSpecify;

		protected ArgumentImpl(String name, String description, String label,
				boolean mustSpecify) {
			fName = name;
			fLabel = label;
			fDescription = description;
			fMustSpecify = mustSpecify;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#name()
		 */
		public String name() {
			return fName;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#description()
		 */
		public String description() {
			return fDescription;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#label()
		 */
		public String label() {
			return fLabel;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#mustSpecify()
		 */
		public boolean mustSpecify() {
			return fMustSpecify;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#value()
		 */
		public abstract String value();

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#setValue(java.lang.String)
		 */
		public abstract void setValue(String value);

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.Argument#isValid(java.lang.String)
		 */
		public abstract boolean isValid(String value);

		@Override
		public abstract String toString();
	}

	public class StringArgumentImpl extends ArgumentImpl implements StringArgument {
		private static final long serialVersionUID = 6009335074727417445L;

		private String fValue;

		protected StringArgumentImpl(String name, String description,
				String label, boolean mustSpecify) {
			super(name, description, label, mustSpecify);
		}

		@Override
		public String value() {
			return fValue;
		}

		@Override
		public void setValue(String value) {
			fValue = value;
		}

		@Override
		public boolean isValid(String value) {
			return true;
		}

		@Override
		public String toString() {
			return fValue;
		}

	}

	public class IntegerArgumentImpl extends ArgumentImpl implements IntegerArgument {
		private static final long serialVersionUID = 6009335074727417445L;
		private Integer fValue;
		private int fMin;
		private int fMax;

		protected IntegerArgumentImpl(String name, String description,
				String label, boolean mustSpecify, int min, int max) {
			super(name, description, label, mustSpecify);
			fMin = min;
			fMax = max;
		}

		@Override
		public String value() {
			return (fValue == null) ? null : fValue.toString();
		}

		@Override
		public void setValue(String value) {
			fValue = new Integer(value);
		}

		@Override
		public boolean isValid(String value) {
			Integer val;
			try {
				val = new Integer(value);
			} catch (NumberFormatException e) {
				return false;
			}
			return isValid(val.intValue());
		}

		@Override
		public String toString() {
			return value();
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#intValue()
		 */
		public int intValue() {
			return fValue.intValue();
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#setValue(int)
		 */
		public void setValue(int value) {
			fValue = new Integer(value);
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#min()
		 */
		public int min() {
			return fMin;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#max()
		 */
		public int max() {
			return fMax;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#isValid(int)
		 */
		public boolean isValid(int value) {
			return fMin <= value && value <= fMax;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.IntegerArgument#stringValueOf(int)
		 */
		public String stringValueOf(int value) {
			return new Integer(value).toString();
		}
	}

	public class BooleanArgumentImpl extends ArgumentImpl implements BooleanArgument {
		private static final long serialVersionUID = 6009335074727417445L;
		private Boolean fValue;

		protected BooleanArgumentImpl(String name, String description,
				String label, boolean mustSpecify) {
			super(name, description, label, mustSpecify);
		}

		@Override
		public String value() {
			return (fValue == null) ? null : fValue.toString();
		}

		@Override
		public void setValue(String value) {
			fValue = Boolean.valueOf(value);
		}

		@Override
		public boolean isValid(String value) {
			return true;
		}

		@Override
		public String toString() {
			return value();
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.BooleanArgument#booleanValue()
		 */
		public boolean booleanValue() {
			return fValue.booleanValue();
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.BooleanArgument#setValue(boolean)
		 */
		public void setValue(boolean value) {
			fValue = Boolean.valueOf(value);
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.BooleanArgument#stringValueOf(boolean)
		 */
		public String stringValueOf(boolean value) {
			return Boolean.valueOf(value).toString();
		}
	}

	public class SelectedArgumentImpl extends StringArgumentImpl implements	SelectedArgument {
		private static final long serialVersionUID = 6009335074727417445L;
		private List<String> fChoices;

		protected SelectedArgumentImpl(String name, String description,
				String label, boolean mustSpecify, List<String> choices) {
			super(name, description, label, mustSpecify);
			fChoices = choices;
		}

		/* (non-Javadoc)
		 * @see com.sun.jdi.connect.Connector.SelectedArgument#choices()
		 */
		public List<String> choices() {
			return fChoices;
		}

		@Override
		public boolean isValid(String value) {
			return fChoices.contains(value);
		}
	}
}
