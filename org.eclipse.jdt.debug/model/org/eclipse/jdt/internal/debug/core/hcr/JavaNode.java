package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * Comparable Java elements are represented as JavaNodes.
 */
class JavaNode {
	
	public static final int CU= 0;
	public static final int PACKAGE= 1;
	public static final int IMPORT_CONTAINER= 2;
	public static final int IMPORT= 3;
	public static final int INTERFACE= 4;
	public static final int CLASS= 5;
	public static final int FIELD= 6;
	public static final int INIT= 7;
	public static final int CONSTRUCTOR= 8;
	public static final int METHOD= 9;

	private String fID;
	private int fTypeCode;
	private char[] fBuffer;
	private int fStart;
	private int fLength;
	private ArrayList fChildren;
	private int fInitializerCount= 1;

	/**
	 * Creates a new <code>JavaNode</code> for the given range within the specified
	 * buffer. The <code>typeCode</code> is uninterpreted client data. The ID is used when comparing
	 * two nodes with each other: i.e. the differencing engine performs a content compare 
	 * on two nodes if their IDs are equal.
	 *
	 * @param typeCode a type code for this node
	 * @param id an identifier for this node
	 * @param buffer buffer on which this node is based on
	 * @param start start position of range within document
	 * @param length length of range
	 */
	JavaNode(int typeCode, String id, char[] buffer, int start, int length) {
		fTypeCode= typeCode;
		fID= id;
		fBuffer= buffer;
		fStart= start;
		fLength= length;
	}

	/**
	 * Creates a JavaNode under the given parent.
	 * @param type the Java elements type. Legal values are from the range CU to METHOD of this class.
	 * @param name the name of the Java element
	 * @param start the starting position of the java element in the underlying document
	 * @param length the number of characters of the java element in the underlying document
	 */
	JavaNode(JavaNode parent, int type, String name, int start, int length) {
		this(type, buildID(type, name), parent.fBuffer, start, length);
		if (parent != null) {
			parent.addChild(this);
		}
	}	
	
	/**
	 * Creates a JavaNode for a CU. It represents the root of a
	 * JavaNode tree, so its parent is null.
	 * @param document the document which contains the Java element
	 */
	JavaNode(char[] buffer) {
		this(CU, buildID(CU, "root"), buffer, 0, buffer.length); //$NON-NLS-1$
	}	

	/**
	 * Returns a name which identifies the given typed name.
	 * The type is encoded as a single character at the beginning of the string.
	 */
	private static String buildID(int type, String name) {
		StringBuffer sb= new StringBuffer();
		switch (type) {
		case JavaNode.CU:
			sb.append(JavaElement.JEM_COMPILATIONUNIT);
			break;
		case JavaNode.CLASS:
		case JavaNode.INTERFACE:
			sb.append(JavaElement.JEM_TYPE);
			sb.append(name);
			break;
		case JavaNode.FIELD:
			sb.append(JavaElement.JEM_FIELD);
			sb.append(name);
			break;
		case JavaNode.CONSTRUCTOR:
		case JavaNode.METHOD:
			sb.append(JavaElement.JEM_METHOD);
			sb.append(name);
			break;
		case JavaNode.INIT:
			sb.append(JavaElement.JEM_INITIALIZER);
			sb.append(name);
			break;
		case JavaNode.PACKAGE:
			sb.append(JavaElement.JEM_PACKAGEDECLARATION);
			break;
		case JavaNode.IMPORT:
			sb.append(JavaElement.JEM_IMPORTDECLARATION);
			sb.append(name);
			break;
		case JavaNode.IMPORT_CONTAINER:
			sb.append('<');
			break;
		default:
			//Assert.isTrue(false);
			break;
		}
		return sb.toString();
	}

	public String getInitializerCount() {
		return Integer.toString(fInitializerCount++);
	}
		
	public int getStart() {
		return fStart;
	}
	
	/**
	 * Returns the type code of this node.
	 * The type code is uninterpreted client data which can be set in the constructor.
	 *
	 * @return the type code of this node
	 */
	public int getTypeCode() {
		return fTypeCode;
	}
	
	/**
	 * Returns this node's id.
	 * It is used in <code>equals</code> and <code>hashcode</code>.
	 *
	 * @return the node's id
	 */
	public String getId() {
		return fID;
	}

	/**
	 * Sets this node's id.
	 * It is used in <code>equals</code> and <code>hashcode</code>.
	 *
	 * @param id the new id for this node
	 */
	public void setId(String id) {
		fID= id;
	}

	/**
	 * Adds the given node as a child.
	 *
	 * @param node the node to add as a child
	 */
	public void addChild(JavaNode node) {
		if (fChildren == null) {
			fChildren= new ArrayList();
		}
		fChildren.add(node);
	}

	/* (non Javadoc)
	 * see IStructureComparator.getChildren
	 */
	public Object[] getChildren() {
		if (fChildren != null) {
			return fChildren.toArray(); 
		}
		return null;
	}

	/**
	 * Sets the length of the range of this node.
	 *
	 * @param length the length of the range
	 */
	public void setLength(int length) {
		fLength= length;
	}

	/**
	 * Implementation based on <code>getID</code>.
	 */
	public boolean equals(Object other) {
		if (other != null && other.getClass() == getClass()) {
			JavaNode tn= (JavaNode) other;
			return fTypeCode == tn.fTypeCode && fID.equals(tn.fID);
		}
		return super.equals(other);
	}

	/**
	 * Implementation based on <code>getID</code>.
	 */
	public int hashCode() {
		return fID.hashCode();
	}

/* This version of getContents will be affected by whitespace changes.
	public String getContents() {
		char[] b= new char[fLength];
		System.arraycopy(fBuffer, fStart, b, 0, fLength);
		return new String(b);
	}
*/
	
     public String getContents() {
		char[] b= new char[fLength];
		System.arraycopy(fBuffer, fStart, b, 0, fLength);
		
		boolean ignoreWhiteSpace= true;
		if (ignoreWhiteSpace) {
			// replace comments and whitespace by a single blank
			StringBuffer buf= new StringBuffer();
			
			// to avoid the trouble when dealing with Unicode
			// we use the Java scanner to extract non-whitespace and non-comment tokens
			IScanner scanner= ToolFactory.createScanner(true, true, false, false);  // however we request Whitespace and Comments
			scanner.setSource(b);
			try {
				int token;
				while ((token= scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
					switch (token) {
						case ITerminalSymbols.TokenNameWHITESPACE:
						case ITerminalSymbols.TokenNameCOMMENT_BLOCK:
						case ITerminalSymbols.TokenNameCOMMENT_JAVADOC:
						case ITerminalSymbols.TokenNameCOMMENT_LINE:
							int l= buf.length();
							if (l > 0 && buf.charAt(l-1) != ' ') {
								buf.append(' ');
							}
							break;
						default:
							buf.append(b, scanner.getCurrentTokenStartPosition(), (scanner.getCurrentTokenEndPosition() + 1) - scanner.getCurrentTokenStartPosition());
							buf.append(' ');
							break;
					}
				}
				return buf.toString(); // success! 
			} catch (InvalidInputException ex) {
			}
		}
		return new String(b);      // return original source
	}
}

