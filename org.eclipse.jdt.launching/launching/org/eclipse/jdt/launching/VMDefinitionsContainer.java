package org.eclipse.jdt.launching;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationUtils;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a container for VM definitions such as the VM definitions that are
 * stored in the workbench preferences.  
 */
public class VMDefinitionsContainer {
		
	/**
	 * Map of VMInstallTypes to Lists of corresponding VMInstalls	 */
	private Map fVMTypeToVMMap;
	private int fVMCount = 0;
	
	private String fDefaultVMInstallCompositeID;
	private String fDefaultVMInstallConnectorTypeID;
	
	/**
	 * Constructor	 */
	public VMDefinitionsContainer() {
		fVMTypeToVMMap = new HashMap(10);
	}
	
	/**
	 * Add the specified VM to the VM definitions managed by this container.	 */
	public void addVM(IVMInstall vm) {	
		IVMInstallType vmInstallType = vm.getVMInstallType();
		List vmList = (List) fVMTypeToVMMap.get(vmInstallType);
		if (vmList == null) {
			vmList = new ArrayList(3);
			fVMTypeToVMMap.put(vmInstallType, vmList);			
		}		
		vmList.add(vm);
		fVMCount++;
	}
	
	/**
	 * Add all VM's in the specified list to the VM definitions managed by this container.	 */
	public void addVMList(List vmList) {
		Iterator iterator = vmList.iterator();
		while (iterator.hasNext()) {
			IVMInstall vm = (IVMInstall) iterator.next();
			addVM(vm);
		}
	}
	
	/**
	 * Return the mapping of VMInstallTypes to VMs.	 */
	public Map getVMTypeToVMMap() {
		return fVMTypeToVMMap;
	}
	
	/**
	 * Return a List of all VMs in this container.	 */
	public List getVMList() {
		List resultList = new ArrayList(fVMCount);
		
		Set keySet = getVMTypeToVMMap().keySet();
		Iterator keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
			IVMInstallType vmInstallType = (IVMInstallType) keyIterator.next();
			List vmList = (List) getVMTypeToVMMap().get(vmInstallType);
			resultList.addAll(vmList);
		}
		
		return resultList;
	}
	
	public String getDefaultVMInstallCompositeID(){
		return fDefaultVMInstallCompositeID;
	}
	
	public void setDefaultVMInstallCompositeID(String id){
		fDefaultVMInstallCompositeID = id;
	}
	
	public String getDefaultVMInstallConnectorTypeID() {
		return fDefaultVMInstallConnectorTypeID;
	}
	
	public void  setDefaultVMInstallConnectorTypeID(String id){
		fDefaultVMInstallConnectorTypeID = id;
	}
	
	/**
	 * Return the VM definitions in this object as a String of XML.  The String
	 * is suitable for storing in the workbench preferences.
	 */	public String getAsXML() throws IOException{
		
		// Create the Document and the top-level node
		Document doc = new DocumentImpl();
		Element config = doc.createElement("vmSettings");    //$NON-NLS-1$
		doc.appendChild(config);
		
		// Set the defaultVM attribute on the top-level node
		if (getDefaultVMInstallCompositeID() != null) {
			config.setAttribute("defaultVM", getDefaultVMInstallCompositeID()); //$NON-NLS-1$
		}
		
		// Set the defaultVMConnector attribute on the top-level node
		if (getDefaultVMInstallConnectorTypeID() != null) {
			config.setAttribute("defaultVMConnector", getDefaultVMInstallConnectorTypeID()); //$NON-NLS-1$
		}
				
		// Create a node for each install type represented in this container
		Set vmInstallTypeSet = getVMTypeToVMMap().keySet();
		Iterator keyIterator = vmInstallTypeSet.iterator();
		while (keyIterator.hasNext()) {
			IVMInstallType vmInstallType = (IVMInstallType) keyIterator.next();
			Element vmTypeElement = vmTypeAsElement(doc, vmInstallType);
			config.appendChild(vmTypeElement);
		}
		
		// Serialize the Document and return the resulting String
		return JavaLaunchConfigurationUtils.serializeDocument(doc);
	}
	
	/**
	 * Create and return a node for the specified VM install type in the specified Document.	 */
	private Element vmTypeAsElement(Document doc, IVMInstallType vmType) {
		
		// Create a node for the vm type and set its 'id' attribute
		Element element= doc.createElement("vmType");   //$NON-NLS-1$
		element.setAttribute("id", vmType.getId());     //$NON-NLS-1$
		
		// For each vm of the specified type, create a subordinate node for it
		List vmList = (List) getVMTypeToVMMap().get(vmType);
		Iterator vmIterator = vmList.iterator();
		while (vmIterator.hasNext()) {
			IVMInstall vm = (IVMInstall) vmIterator.next();
			Element vmElement = vmAsElement(doc, vm);
			element.appendChild(vmElement);
		}
		
		return element;
	}
	
	/**
	 * Create and return a node for the specified VM in the specified Document.	 */
	private Element vmAsElement(Document doc, IVMInstall vm) {
		
		// Create the node for the VM and set its 'id' & 'name' attributes
		Element element= doc.createElement("vm");        //$NON-NLS-1$
		element.setAttribute("id", vm.getId());	         //$NON-NLS-1$
		element.setAttribute("name", vm.getName());      //$NON-NLS-1$
		
		// Determine and set the 'path' attribute for the VM
		String installPath= "";                          //$NON-NLS-1$
		File installLocation= vm.getInstallLocation();
		if (installLocation != null) {
			installPath= installLocation.getAbsolutePath();
		}
		element.setAttribute("path", installPath);       //$NON-NLS-1$
		
		// If the 'libraryLocations' attribute is specified, create a node for it 
		LibraryLocation[] libraryLocations= vm.getLibraryLocations();
		if (libraryLocations != null) {
			Element libLocationElement = libraryLocationsAsElement(doc, libraryLocations);
			element.appendChild(libLocationElement);
		}
		
		return element;
	}
	
	/**
	 * Create and return a 'libraryLocations' node.  This node owns subordinate nodes that
	 * list individual library locations.
	 */	private static Element libraryLocationsAsElement(Document doc, LibraryLocation[] locations) {
		Element root = doc.createElement("libraryLocations");       //$NON-NLS-1$
		for (int i = 0; i < locations.length; i++) {
			Element element = doc.createElement("libraryLocation");  //$NON-NLS-1$
			element.setAttribute("jreJar", locations[i].getSystemLibraryPath().toString()); //$NON-NLS-1$
			element.setAttribute("jreSrc", locations[i].getSystemLibrarySourcePath().toString()); //$NON-NLS-1$
			element.setAttribute("pkgRoot", locations[i].getPackageRootPath().toString()); //$NON-NLS-1$
			root.appendChild(element);
		}
		return root;
	}
			
	/**
	 * Parse the VM definitions in the specified InputStream and return an instance
	 * of <code>VMDefinitionsContainer</code>.
	 * The VMs in the returned container are instances of <code>VMStandin</code>.
	 * This method has no side-effects.  That is, no notifications are sent for VM adds,
	 * changes, deletes.  
	 */	public static VMDefinitionsContainer parseXMLIntoContainer(InputStream inputStream) throws IOException {
		
		// Create the container to populate
		VMDefinitionsContainer container = new VMDefinitionsContainer();

		// Wrapper the stream for efficient parsing
		InputStream stream= new BufferedInputStream(inputStream);
		Reader reader= new InputStreamReader(stream);

		// Do the parsing and obtain the top-level node
		Element config= null;		
		try {
			DocumentBuilder parser= DocumentBuilderFactory.newInstance().newDocumentBuilder();
			config = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		} finally {
			reader.close();
		}
		
		// If the top-level node wasn't what we expected, bail out
		if (!config.getNodeName().equalsIgnoreCase("vmSettings")) { //$NON-NLS-1$
			throw new IOException(LaunchingMessages.getString("JavaRuntime.badFormat")); //$NON-NLS-1$
		}
		
		// Populate the default VM-related fields
		container.setDefaultVMInstallCompositeID(config.getAttribute("defaultVM")); //$NON-NLS-1$
		container.setDefaultVMInstallConnectorTypeID(config.getAttribute("defaultVMConnector")); //$NON-NLS-1$
		
		// Traverse the parsed structure and populate the VMType to VM Map
		NodeList list = config.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element vmTypeElement = (Element) node;
				if (vmTypeElement.getNodeName().equalsIgnoreCase("vmType")) { //$NON-NLS-1$
					populateVMTypes(vmTypeElement, container);
				}
			}
		}
		
		return container;
	}
	
	/**
	 * For the specified vm type node, parse all subordinate VM definitions and add them
	 * to the specified container.
	 */
	private static void populateVMTypes(Element vmTypeElement, VMDefinitionsContainer container) {
		
		// Retrieve the 'id' attribute and the corresponding VM type object
		String id = vmTypeElement.getAttribute("id");         //$NON-NLS-1$
		IVMInstallType vmType= JavaRuntime.getVMInstallType(id);
		if (vmType != null) {
			
			// For each VM child node, populate the container with a subordinate node
			NodeList vmNodeList = vmTypeElement.getChildNodes();
			for (int i = 0; i < vmNodeList.getLength(); ++i) {
				Node vmNode = vmNodeList.item(i);
				short type = vmNode.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element vmElement = (Element) vmNode;
					if (vmElement.getNodeName().equalsIgnoreCase("vm")) { //$NON-NLS-1$
						populateVMForType(vmType, vmElement, container);
					}
				}
			}
		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.VM_type_element_with_unknown_id_1")); //$NON-NLS-1$
		}
	}

	/**
	 * Parse the specified VM node, create a VMStandin for it, and add this to the 
	 * specified container.
	 */
	private static void populateVMForType(IVMInstallType vmType, Element vmElement, VMDefinitionsContainer container) {
		String id= vmElement.getAttribute("id"); //$NON-NLS-1$
		if (id != null) {
			
			// Retrieve the 'path' attribute.  If none, skip this node.
			String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
			if (installPath == null) {
				return;
			}
			
			// Verify something exists at the specified install path.  If not, skip this node.
			File installLocation= new File(installPath);
			if (!installLocation.exists()) {
				return;
			}
			
			// Create a VMStandin for the node and set its 'name' & 'installLocation' attributes
			VMStandin vmStandin = new VMStandin(vmType, id);
			container.addVM(vmStandin);
			vmStandin.setName(vmElement.getAttribute("name")); //$NON-NLS-1$
			vmStandin.setInstallLocation(installLocation);
			
			// Look for subordinate nodes.  These may be either 'libraryLocation' or
			// 'libraryLocations'.  
			NodeList list = vmElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element libraryLocationElement= (Element)node;
					if (libraryLocationElement.getNodeName().equals("libraryLocation")) { //$NON-NLS-1$
						LibraryLocation loc = getLibraryLocation(vmStandin, libraryLocationElement);
						vmStandin.setLibraryLocations(new LibraryLocation[]{loc});
						break;
					} else if (libraryLocationElement.getNodeName().equals("libraryLocations")) { //$NON-NLS-1$
						setLibraryLocations(vmStandin, libraryLocationElement);
						break;
					}
				}
			}
		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.VM_element_specified_with_no_id_attribute_2")); //$NON-NLS-1$
		}
	}	
	
	/**
	 * Create & return a LibraryLocation object populated from the attribute values
	 * in the specified node.	 */
	private static LibraryLocation getLibraryLocation(IVMInstall vm, Element libLocationElement) {
		String jreJar= libLocationElement.getAttribute("jreJar"); //$NON-NLS-1$
		String jreSrc= libLocationElement.getAttribute("jreSrc"); //$NON-NLS-1$
		String pkgRoot= libLocationElement.getAttribute("pkgRoot"); //$NON-NLS-1$
		if (jreJar != null && jreSrc != null && pkgRoot != null) {
			return new LibraryLocation(new Path(jreJar), new Path(jreSrc), new Path(pkgRoot));
		} else {
			LaunchingPlugin.log(LaunchingMessages.getString("JavaRuntime.Library_location_element_incorrectly_specified_3")); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Set the LibraryLocations on the specified VM, by extracting the subordinate
	 * nodes from the specified 'lirbaryLocations' node.
	 */
	private static void setLibraryLocations(IVMInstall vm, Element libLocationsElement) {
		NodeList list = libLocationsElement.getChildNodes();
		int length = list.getLength();
		List locations = new ArrayList(length);
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element libraryLocationElement= (Element)node;
				if (libraryLocationElement.getNodeName().equals("libraryLocation")) { //$NON-NLS-1$
					locations.add(getLibraryLocation(vm, libraryLocationElement));
				}
			}
		}	
		vm.setLibraryLocations((LibraryLocation[])locations.toArray(new LibraryLocation[locations.size()]));
	}
		
}
