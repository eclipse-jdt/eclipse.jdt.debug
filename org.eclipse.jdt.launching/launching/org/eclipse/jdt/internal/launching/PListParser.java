/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses an XML property list into its associated objects.
 */
public class PListParser {
	
	/**
	 * Constants for XML element names and attributes
	 */
	private static final String PLIST_ELEMENT = "plist"; //$NON-NLS-1$
	private static final String KEY_ELEMENT = "key"; //$NON-NLS-1$
	private static final String DICT_ELEMENT = "dict"; //$NON-NLS-1$
	private static final String ARRAY_ELEMENT = "array"; //$NON-NLS-1$
	private static final String TRUE_ELEMENT = "true"; //$NON-NLS-1$
	private static final String FALSE_ELEMENT = "false"; //$NON-NLS-1$
	private static final String INT_ELEMENT = "integer"; //$NON-NLS-1$
	private static final String STRING_ELEMENT = "string"; //$NON-NLS-1$
	
	/**
	 * Parses the given input stream which corresponds to an XML plist. See the DTD
	 * here: http://www.apple.com/DTDs/PropertyList-1.0.dtd
	 * 
	 * @param stream XML plist input stream
	 * @return Object(s) in the stream
	 * @throws CoreException if an error occurs
	 */
	public Object parse(InputStream stream) throws CoreException {
		try {
			stream = new BufferedInputStream(stream);
			return parseXML(stream);
		} catch (FileNotFoundException e) {
			abort(e);					
		} catch (SAXException e) {
			abort(e);					
		} catch (ParserConfigurationException e) {
			abort(e);					
		} catch (IOException e) {
			abort(e);					
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					abort(e);					
				}
			}
		}
		return null;
	}
	
	/**
	 * Return a LaunchConfigurationInfo object initialized from XML contained in
	 * the specified stream.  Simply pass out any exceptions encountered so that
	 * caller can deal with them.  This is important since caller may need access to the
	 * actual exception.
	 * @param stream the stream
	 * @return the result of the parse
	 * @throws CoreException if an error occurs
	 * @throws ParserConfigurationException if the parser set-up fails
	 * @throws IOException if reading the stream fails
	 * @throws SAXException if parsing itself fails
	 */
	private Object parseXML(InputStream stream) throws CoreException, ParserConfigurationException, IOException, SAXException {
		Element root = null;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
		parser.setErrorHandler(new DefaultHandler());
		root = parser.parse(new InputSource(stream)).getDocumentElement();
		if (!root.getNodeName().equalsIgnoreCase(PLIST_ELEMENT)) { 
			throw getInvalidFormatException();
		}
		NodeList list = root.getChildNodes();
		Node node = null;
		Element element = null;
		for (int i = 0; i < list.getLength(); ++i) {
			node = list.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				element = (Element) node;
				return parseObject(element);
			}
		}
		return null;
	}
	
	/**
	 * Returns an invalid format exception
	 * 
	 * @return an invalid format exception
	 */
	private CoreException getInvalidFormatException() {
		return new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, "Invalid plist XML", null)); //$NON-NLS-1$
	}
	
	/**
	 * Parses and returns an object from the given root element, possibly <code>null</code>.
	 * 
	 * @param element the root node from the XML document
	 * @return parsed object or <code>null</code>
	 * @throws CoreException if an error occurs
	 */
	private Object parseObject(Element element) throws CoreException {
		String nodeName = element.getNodeName();
		if (nodeName.equalsIgnoreCase(ARRAY_ELEMENT)) { 
			return parseArray(element);
		} else if (nodeName.equalsIgnoreCase(DICT_ELEMENT)) { 
			return parseDictionary(element);
		} else if (nodeName.equalsIgnoreCase(KEY_ELEMENT))  { 
			return getText(element);
		} else if (nodeName.equalsIgnoreCase(TRUE_ELEMENT)) {   
			return Boolean.TRUE;			
		} else if (nodeName.equalsIgnoreCase(FALSE_ELEMENT)) {    
			return Boolean.FALSE;								
		} else if(nodeName.equalsIgnoreCase(INT_ELEMENT)) {
			try {
				return new Integer(Integer.parseInt(getText(element)));
			} catch (NumberFormatException e) {
				abort(e);
			}
		} else if (nodeName.equalsIgnoreCase(STRING_ELEMENT)) {
			return getText(element);
		}
		return null;
	}	
	
	/**
	 * Parses and returns an array from the given root element, possibly empty.
	 * 
	 * @param root the root array node from the XML document
	 * @return parsed array or <code>null</code>
	 * @throws CoreException if an error occurs
	 */
	private Object[] parseArray(Element root) throws CoreException {
		List<Object> collection = new ArrayList<Object>();
		NodeList list = root.getChildNodes();
		Node node = null;
		Element element = null;
		for (int i = 0; i < list.getLength(); ++i) {
			node = list.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				element = (Element) node;
				Object obj = parseObject(element);
				if (obj != null) {
					collection.add(obj);
				}
			}
		}
		return collection.toArray();
	}	
	
	/**
	 * Parses and returns a map from the given dictionary element, possibly empty.
	 * 
	 * @param root the root dictionary node from the XML document
	 * @return parsed map or <code>null</code>
	 * @throws CoreException if an error occurs
	 */
	private Map<String, Object> parseDictionary(Element root) throws CoreException {
		Map<String, Object> dict = new HashMap<String, Object>();
		NodeList list = root.getChildNodes();
		Node node = null;
		Element element = null;
		String nodeName = null;
		String key = null;
		for (int i = 0; i < list.getLength(); ++i) {
			node = list.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				element = (Element) node;
				nodeName = element.getNodeName();
				if (nodeName.equalsIgnoreCase(KEY_ELEMENT)) { 
					key = getText(element);
				} else {
					dict.put(key, parseObject(element));
				}
			}
		}
		return dict;
	}		
	
	/**
	 * Returns the value of the first child text node from the given element,
	 * or <code>null</code>.
	 * 
	 * @param root the root element
	 * @return its text or <code>null</code> if none
	 */
	private String getText(Element root) {
		NodeList list = root.getChildNodes();
		Node node = null;
		for (int i = 0; i < list.getLength(); ++i) {
			node = list.item(i);
			short nodeType = node.getNodeType();
			if (nodeType == Node.TEXT_NODE) {
				return ((Text) node).getNodeValue();
			}
		}
		return null;
	}
	
	private void abort(Throwable t) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, "Exception occurred parsing property list", t)); //$NON-NLS-1$
	}
}
