/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.launching;

import java.util.ArrayList;


/**
 * The execution arguments for running a Java VM. The execution arguments are
 * separated into two parts: arguments to the VM itself, and arguments to the Java 
 * main program.
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public class ExecutionArguments {
	private String fVMArgs;
	private String fProgramArgs;
		
	/**
	 * Creates a new execution arguments object.
	 *
	 * @param vmArgs command line argument string passed to the VM
	 * @param programArgs command line argument string passed to the program
	 */
	public ExecutionArguments(String vmArgs, String programArgs) {
		if (vmArgs == null || programArgs == null)
			throw new IllegalArgumentException();
		fVMArgs= vmArgs;
		fProgramArgs= programArgs;
	}
	
	/**
	 * Returns the VM arguments.
	 *
	 * @return the VM arguments
	 */
	public String getVMArguments() {
		return fVMArgs;
	}
	
	/**
	 * Returns the program arguments.
	 *
	 * @return the program arguments
	 */
	public String getProgramArguments() {
		return fProgramArgs;
	}
	
	/**
	 * Returns the VM arguments as array
	 *
	 * @return the VM arguments
	 */
	public String[] getVMArgumentsArray() {
		return parseArguments(fVMArgs);
	}
	
	/**
	 * Returns the program arguments as array
	 *
	 * @return the program arguments
	 */
	public String[] getProgramArgumentsArray() {
		return parseArguments(fProgramArgs);
	}	
			
	private static class ArgumentParser {
		private String fArgs;
		private int fIndex= 0;
		private int ch= -1;
		
		public ArgumentParser(String args) {
			fArgs= args;
		}
		
		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}
		
		public String[] parseArguments() {
			ArrayList v= new ArrayList();
			
			ch= getNext();
			while (ch > 0) {
				while (Character.isWhitespace((char)ch))
					ch= getNext();	
				
				if (ch == '"') {
					v.add(parseString());
				} else {
					v.add(parseToken());
				}
			}
	
			String[] result= new String[v.size()];
			v.toArray(result);
			return result;
		}
		
		public String parseString() {
			StringBuffer buf= new StringBuffer();
			buf.append((char)ch);
			ch= getNext();
			while (ch > 0 && ch != '"') {
				buf.append((char)ch);
				ch= getNext();
			}
			if (ch > 0)
				buf.append((char)ch);
			ch= getNext();
				
			return buf.toString();
		}
		
		public String parseToken() {
			StringBuffer buf= new StringBuffer();
			
			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '"')
					buf.append(parseString());
				else {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			return buf.toString();
		}
	}
	
	private static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser= new ArgumentParser(args);
		String[] res= parser.parseArguments();
		
		return res;
	}
	
}