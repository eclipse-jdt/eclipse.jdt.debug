package org.eclipse.jdt.internal.launching;

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
import java.net.MalformedURLException;
import java.net.URL;
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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a container for VM definitions such as the VM definitions that are
 * stored in the workbench preferences.  
 * <p>
 * An instance of this class may be obtained from an XML document by calling
 * <code>parseXMLIntoContainer</code>.
 * </p>
 * <p>
 * An instance of this class may be translated into an XML document by calling
 * <code>getAsXML</code>.
 * </p>
 * <p>
 * Clients may instantiate this class; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 */
public class VMDefinitionsContainer {
		
	/**
	 * Map of VMInstallTypes to Lists of corresponding VMInstalls.	 */
	private Map fVMTypeToVMMap;
	
	/**
	 * VMs managed by this container whose install locations don't actually exist.	 */
	private List fInvalidVMList;
		
	/**
	 * The number of VMs managed by this container.	 */
	private int fVMCount = 0;
	
	/**
	 * The composite identifier of the default VM.  This consists of the install type ID
	 * plus an ID for the VM.	 */
	private String fDefaultVMInstallCompositeID;
	
	/**
	 * The identifier of the connector to use for the default VM.	 */
	private String fDefaultVMInstallConnectorTypeID;
	
	/**
	 * Constructs an empty VM container 
	 */
	public VMDefinitionsContainer() {
		fVMTypeToVMMap = new HashMap(10);
		fInvalidVMList = new ArrayList(5);			
	}
		
	/**
	 * Add the specified VM to the VM definitions managed by this container.
	 * <p>
	 * If distinguishing valid from invalid VMs is important, the specified VM must
	 * have already had its install location set.  An invalid VM is one whose install
	 * location doesn't exist.
	 * </p>
	 * 
	 * @param vm the VM to be added to this container	 */
	public void addVM(IVMInstall vm) {	
		IVMInstallType vmInstallType = vm.getVMInstallType();
		List vmList = (List) fVMTypeToVMMap.get(vmInstallType);
		if (vmList == null) {
			vmList = new ArrayList(3);
			fVMTypeToVMMap.put(vmInstallType, vmList);			
		}		
		vmList.add(vm);
		if (!verifyInstallLocation(vm)) {
			fInvalidVMList.add(vm);
		}
		fVMCount++;
	}
	
	/**
	 * Add all VM's in the specified list to the VM definitions managed by this container.
	 * <p>
	 * If distinguishing valid from invalid VMs is important, the specified VMs must
	 * have already had their install locations set.  An invalid VM is one whose install
	 * location doesn't exist.
	 * </p>
	 * 
	 * @param vmList a list of VMs to be added to this container	 */
	public void addVMList(List vmList) {
		Iterator iterator = vmList.iterator();
		while (iterator.hasNext()) {
			IVMInstall vm = (IVMInstall) iterator.next();
			addVM(vm);
		}
	}
	
	/**
	 * Return <code>true</code> if the specified VM's install location exists on the
	 * file system, <code>false</code> otherwise.
	 * 	 * @param vm the instance of <code>IVMInstall</code> whose install location will be verified	 * @return boolean <code>true</code> if the specified VMs install location exists on the
	 * 			file system, <code>false</code> otherwise.	 */
	private boolean verifyInstallLocation(IVMInstall vm) {
		File installLocation = vm.getInstallLocation();
		if (installLocation.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Return a mapping of VM install types to lists of VMs.  The keys of this map are instances of
	 * <code>IVMInstallType</code>.  The values are instances of <code>java.util.List</code>
	 * which contain instances of <code>IVMInstall</code>.  
	 * 
	 * @return Map the mapping of VM install types to lists of VMs	 */
	public Map getVMTypeToVMMap() {
		return fVMTypeToVMMap;
	}
	
	/**
	 * Return a list of all VMs in this container, including any invalid VMs.  An invalid
	 * VM is one whose install location does not exist on the file system.
	 * The order of the list is not specified.
	 * 
	 * @return List the data structure containing all VMs managed by this container	 */
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
	
	/**
	 * Return a list of all valid VMs in this container.  A valid VM is one whose install
	 * location exists on the file system.  The order of the list is not specified.
	 * 
	 * @return List 	 */
	public List getValidVMList() {
		List resultList = getVMList();
		resultList.removeAll(fInvalidVMList);
		return resultList;
	}
	
	/**
	 * Returns the composite ID for the default VM.  The composite ID consists
	 * of an ID for the VM install type together with an ID for VM.  This is
	 * necessary because VM ids by themselves are not necessarily unique across
	 * VM install types.
	 * 
	 * @return String returns the composite ID of the current default VM
	 */
	public String getDefaultVMInstallCompositeID(){
		return fDefaultVMInstallCompositeID;
	}
	
	/**
	 * Sets the composite ID for the default VM.  The composite ID consists
	 * of an ID for the VM install type together with an ID for VM.  This is
	 * necessary because VM ids by themselves are not necessarily unique across
	 * VM install types.
	 * 
	 * @param id identifies the new default VM using a composite ID
	 */
	public void setDefaultVMInstallCompositeID(String id){
		fDefaultVMInstallCompositeID = id;
	}
	
	/**
	 * Return the default VM's connector type ID.
	 * 
	 * @return String the current value of the default VM's connector type ID
	 */
	public String getDefaultVMInstallConnectorTypeID() {
		return fDefaultVMInstallConnectorTypeID;
	}
	
	/**
	 * Set the default VM's connector type ID.
	 * 
	 * @param id the new value of the default VM's connector type ID
	 */
	public void  setDefaultVMInstallConnectorTypeID(String id){
		fDefaultVMInstallConnectorTypeID = id;
	}
	
	/**
	 * Return the VM definitions contained in this object as a String of XML.  The String
	 * is suitable for storing in the workbench preferences.
	 * <p>
	 * The resulting XML is compatible with the static method <code>parseXMLIntoContainer</code>.
	 * </p>
	 * @return String the results of flattening this object into XML
	 * @throws IOException if this method fails. Reasons include:<ul>
	 * <li>serialization of the XML document failed</li>
	 * </ul>
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
		
		// Java doc location
		URL url = vm.getJavadocLocation();
		if (url != null) {
			element.setAttribute("javadocURL", url.toExternalForm()); //$NON-NLS-1$
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
	 * Parse the VM definitions contained in the specified InputStream and return an instance
	 * of <code>VMDefinitionsContainer</code>.
	 * <p>
	 * The VMs in the returned container are instances of <code>VMStandin</code>.
	 * </p>
	 * <p>
	 * This method has no side-effects.  That is, no notifications are sent for VM adds,
	 * changes, deletes, and the workbench preferences are not affected.
	 * </p>
	 * <p>
	 * If the <code>getAsXML</code> method is called on the returned container object,
	 * the resulting XML will be sematically equivalent (though not necessarily syntactically equivalent) as
	 * the XML contained in <code>inputStream</code>.
	 * </p>
	 * @param inputStream the <code>InputStream</code> containing XML that declares a set of VMs and a default VM
	 * @return VMDefinitionsContainer a container for the VM objects declared in <code>inputStream</code>
	 * @throws IOException if this method fails. Reasons include:<ul>
	 * <li>the XML in <code>inputStream</code> was badly formatted</li>
	 * <li>the top-level node was not 'vmSettings'</li>
	 * </ul>
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
						
			// Create a VMStandin for the node and set its 'name' & 'installLocation' attributes
			VMStandin vmStandin = new VMStandin(vmType, id);
			vmStandin.setName(vmElement.getAttribute("name")); //$NON-NLS-1$
			File installLocation= new File(installPath);
			vmStandin.setInstallLocation(installLocation);
			container.addVM(vmStandin);
			
			// Look for subordinate nodes.  These may be 'libraryLocation',
			// 'libraryLocations' or 'versionInfo'.
			NodeList list = vmElement.getChildNodes();
			int length = list.getLength();
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element subElement = (Element)node;
					String subElementName = subElement.getNodeName();
					if (subElementName.equals("libraryLocation")) { //$NON-NLS-1$
						LibraryLocation loc = getLibraryLocation(vmStandin, subElement);
						vmStandin.setLibraryLocations(new LibraryLocation[]{loc});
						break;
					} else if (subElementName.equals("libraryLocations")) { //$NON-NLS-1$
						setLibraryLocations(vmStandin, subElement);
						break;
					}
				}
			}
			
			// javadoc URL
			String externalForm = vmElement.getAttribute("javadocURL"); //$NON-NLS-1$
			if (externalForm != null && externalForm.length() > 0) {
				try {
					vmStandin.setJavadocLocation(new URL(externalForm));
				} catch (MalformedURLException e) {
					LaunchingPlugin.log(e);
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
