/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
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
import org.xml.sax.helpers.DefaultHandler;

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
	 * Map of VMInstallTypes to Lists of corresponding VMInstalls.
	 */
	private Map fVMTypeToVMMap;
	
	/**
	 * Cached list of VMs in this container
	 */
	private List fVMList;
	
	/**
	 * VMs managed by this container whose install locations don't actually exist.
	 */
	private List fInvalidVMList;
			
	/**
	 * The composite identifier of the default VM.  This consists of the install type ID
	 * plus an ID for the VM.
	 */
	private String fDefaultVMInstallCompositeID;
	
	/**
	 * The identifier of the connector to use for the default VM.
	 */
	private String fDefaultVMInstallConnectorTypeID;
	
	/**
	 * Constructs an empty VM container 
	 */
	public VMDefinitionsContainer() {
		fVMTypeToVMMap = new HashMap(10);
		fInvalidVMList = new ArrayList(10);	
		fVMList = new ArrayList(10);		
	}
		
	/**
	 * Add the specified VM to the VM definitions managed by this container.
	 * <p>
	 * If distinguishing valid from invalid VMs is important, the specified VM must
	 * have already had its install location set.  An invalid VM is one whose install
	 * location doesn't exist.
	 * </p>
	 * 
	 * @param vm the VM to be added to this container
	 */
	public void addVM(IVMInstall vm) {
		if (!fVMList.contains(vm)) {	
			IVMInstallType vmInstallType = vm.getVMInstallType();
			List vmList = (List) fVMTypeToVMMap.get(vmInstallType);
			if (vmList == null) {
				vmList = new ArrayList(3);
				fVMTypeToVMMap.put(vmInstallType, vmList);			
			}
			vmList.add(vm);
			File installLocation = vm.getInstallLocation();
			if (installLocation == null || !vmInstallType.validateInstallLocation(installLocation).isOK()) {
				fInvalidVMList.add(vm);
			}
			fVMList.add(vm);
		}
	}
	
	/**
	 * Add all VM's in the specified list to the VM definitions managed by this container.
	 * <p>
	 * If distinguishing valid from invalid VMs is important, the specified VMs must
	 * have already had their install locations set.  An invalid VM is one whose install
	 * location doesn't exist.
	 * </p>
	 * 
	 * @param vmList a list of VMs to be added to this container
	 */
	public void addVMList(List vmList) {
		Iterator iterator = vmList.iterator();
		while (iterator.hasNext()) {
			IVMInstall vm = (IVMInstall) iterator.next();
			addVM(vm);
		}
	}

	/**
	 * Return a mapping of VM install types to lists of VMs.  The keys of this map are instances of
	 * <code>IVMInstallType</code>.  The values are instances of <code>java.util.List</code>
	 * which contain instances of <code>IVMInstall</code>.  
	 * 
	 * @return Map the mapping of VM install types to lists of VMs
	 */
	public Map getVMTypeToVMMap() {
		return fVMTypeToVMMap;
	}
	
	/**
	 * Return a list of all VMs in this container, including any invalid VMs.  An invalid
	 * VM is one whose install location does not exist on the file system.
	 * The order of the list is not specified.
	 * 
	 * @return List the data structure containing all VMs managed by this container
	 */
	public List getVMList() {
		return fVMList;
	}
	
	/**
	 * Return a list of all valid VMs in this container.  A valid VM is one whose install
	 * location exists on the file system.  The order of the list is not specified.
	 * 
	 * @return List 
	 */
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
	 * @throws ParserConfigurationException if creation of the XML document failed
	 * @throws TransformerException if serialization of the XML document failed
	 */
	public String getAsXML() throws ParserConfigurationException, IOException, TransformerException {
		
		// Create the Document and the top-level node
		Document doc = LaunchingPlugin.getDocument();
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
	 * Create and return a node for the specified VM install type in the specified Document.
	 */
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
	 * Create and return a node for the specified VM in the specified Document.
	 */
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
		
		if (vm instanceof IVMInstall2) {
			String vmArgs = ((IVMInstall2)vm).getVMArgs();
			if (vmArgs != null && vmArgs.length() > 0) {
				element.setAttribute("vmargs", vmArgs); //$NON-NLS-1$
			}
		} else {
			String[] vmArgs = vm.getVMArguments();
			if (vmArgs != null && vmArgs.length > 0) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < vmArgs.length; i++) {
					buffer.append(vmArgs[i] + " "); //$NON-NLS-1$
				}
				element.setAttribute("vmargs", buffer.toString()); //$NON-NLS-1$
			}
		}
		
		return element;
	}
	
	/**
	 * Create and return a 'libraryLocations' node.  This node owns subordinate nodes that
	 * list individual library locations.
	 */
	private static Element libraryLocationsAsElement(Document doc, LibraryLocation[] locations) {
		Element root = doc.createElement("libraryLocations");       //$NON-NLS-1$
		for (int i = 0; i < locations.length; i++) {
			Element element = doc.createElement("libraryLocation");  //$NON-NLS-1$
			element.setAttribute("jreJar", locations[i].getSystemLibraryPath().toString()); //$NON-NLS-1$
			element.setAttribute("jreSrc", locations[i].getSystemLibrarySourcePath().toString()); //$NON-NLS-1$

			IPath packageRootPath = locations[i].getPackageRootPath();
            if (packageRootPath != null) {
                element.setAttribute("pkgRoot", packageRootPath.toString()); //$NON-NLS-1$
            }
            
			URL url= locations[i].getJavadocLocation();
			if (url != null) {
				element.setAttribute("jreJavadoc", url.toExternalForm()); //$NON-NLS-1$
			}
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
	 */
	public static VMDefinitionsContainer parseXMLIntoContainer(InputStream inputStream) throws IOException {
		
		// Create the container to populate
		VMDefinitionsContainer container = new VMDefinitionsContainer();

		// Wrapper the stream for efficient parsing
		InputStream stream= new BufferedInputStream(inputStream);
		Reader reader= new InputStreamReader(stream);

		// Do the parsing and obtain the top-level node
		Element config= null;		
		try {
			DocumentBuilder parser= DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			config = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat); //$NON-NLS-1$
		} finally {
			reader.close();
		}
		
		// If the top-level node wasn't what we expected, bail out
		if (!config.getNodeName().equalsIgnoreCase("vmSettings")) { //$NON-NLS-1$
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat); //$NON-NLS-1$
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
			LaunchingPlugin.log(LaunchingMessages.JavaRuntime_VM_type_element_with_unknown_id_1); //$NON-NLS-1$
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
						LibraryLocation loc = getLibraryLocation(subElement);
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
			
			// vm Arguments
			String vmArgs = vmElement.getAttribute("vmargs"); //$NON-NLS-1$
			if (vmArgs != null && vmArgs.length() >0) {
				vmStandin.setVMArgs(vmArgs);
			}
		} else {
			LaunchingPlugin.log(LaunchingMessages.JavaRuntime_VM_element_specified_with_no_id_attribute_2); //$NON-NLS-1$
		}
	}	
	
	/**
	 * Create & return a LibraryLocation object populated from the attribute values
	 * in the specified node.
	 */
	private static LibraryLocation getLibraryLocation(Element libLocationElement) {
		String jreJar= libLocationElement.getAttribute("jreJar"); //$NON-NLS-1$
		String jreSrc= libLocationElement.getAttribute("jreSrc"); //$NON-NLS-1$
		String pkgRoot= libLocationElement.getAttribute("pkgRoot"); //$NON-NLS-1$
		String jreJavadoc= libLocationElement.getAttribute("jreJavadoc"); //$NON-NLS-1$
		URL javadocURL= null;
		if (jreJavadoc.length() == 0) {
			jreJavadoc= null;
		} else {
			try {
				javadocURL= new URL(jreJavadoc);
			} catch (MalformedURLException e) {
				LaunchingPlugin.log(LaunchingMessages.JavaRuntime_Library_location_element_incorrectly_specified_3); //$NON-NLS-1$
			}
		}
		if (jreJar != null && jreSrc != null && pkgRoot != null) {
			return new LibraryLocation(new Path(jreJar), new Path(jreSrc), new Path(pkgRoot), javadocURL);
		}
		LaunchingPlugin.log(LaunchingMessages.JavaRuntime_Library_location_element_incorrectly_specified_3); //$NON-NLS-1$
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
					locations.add(getLibraryLocation(libraryLocationElement));
				}
			}
		}	
		vm.setLibraryLocations((LibraryLocation[])locations.toArray(new LibraryLocation[locations.size()]));
	}
		
}
