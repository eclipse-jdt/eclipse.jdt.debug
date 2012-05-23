/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Evaluates system properties passed as program arguments.
 * 
 * @since 3.2
 */
public class SystemProperties {

	public static void main(String[] args) {
		try {
			Document doc = newDocument();
			Element properties = doc.createElement("systemProperties");    //$NON-NLS-1$
			doc.appendChild(properties);
			for (int i = 0; i < args.length; i++) {
				String name = args[i];
				String value = System.getProperty(name);
				if (value != null) {
					Element property = doc.createElement("property"); //$NON-NLS-1$
					property.setAttribute("name", name); //$NON-NLS-1$
					property.setAttribute("value", value); //$NON-NLS-1$
					properties.appendChild(property);
				}
			}
			String text = serializeDocument(doc);
			System.out.print(text);
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		} catch (TransformerException e) {
		}
	}
	
	/**
	 * Returns a a new XML document
	 * @return document
	 * @throws ParserConfigurationException if an exception occurs creating the document builder
	 */
	private static Document newDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dfactory= DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder= dfactory.newDocumentBuilder();
		Document doc= docBuilder.newDocument();
		return doc;
	}	
	
	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 * @throws IOException if the document cannot be created
	 * @throws TransformerException if here is an exception reading the XML
	 */
	private static String serializeDocument(Document doc) throws IOException, TransformerException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		
		TransformerFactory factory= TransformerFactory.newInstance();
		Transformer transformer= factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		DOMSource source= new DOMSource(doc);
		StreamResult outputTarget= new StreamResult(s);
		transformer.transform(source, outputTarget);
		
		return s.toString("UTF8"); //$NON-NLS-1$			
	}	
}
