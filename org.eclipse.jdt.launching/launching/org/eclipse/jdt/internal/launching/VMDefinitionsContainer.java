/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Frits Jalvingh - Contribution for Bug 459831 - [launching] Support attaching
 *     	external annotations to a JRE container
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.eclipse.osgi.util.NLS;
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
	private final Map<IVMInstallType, List<IVMInstall>> fVMTypeToVMMap;

	/**
	 * Cached list of VMs in this container
	 */
	private final List<IVMInstall> fVMList;

	/**
	 * VMs managed by this container whose install locations don't actually exist.
	 */
	private final List<IVMInstall> fInvalidVMList;

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
	 * Contains any error/information status of parsing XML
	 */
	private MultiStatus fStatus;

	/**
	 * Constructs an empty VM container
	 */
	public VMDefinitionsContainer() {
		fVMTypeToVMMap = new HashMap<>(10);
		fInvalidVMList = new ArrayList<>(10);
		fVMList = new ArrayList<>(10);
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
			List<IVMInstall> vmList = fVMTypeToVMMap.get(vmInstallType);
			if (vmList == null) {
				vmList = new ArrayList<>(3);
				fVMTypeToVMMap.put(vmInstallType, vmList);
			}
			vmList.add(vm);
			File installLocation = vm.getInstallLocation();
			if (installLocation == null || vmInstallType.validateInstallLocation(installLocation).getSeverity() == IStatus.ERROR) {
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
	public void addVMList(List<IVMInstall> vmList) {
		Iterator<IVMInstall> iterator = vmList.iterator();
		while (iterator.hasNext()) {
			addVM(iterator.next());
		}
	}

	/**
	 * Return a mapping of VM install types to lists of VMs.  The keys of this map are instances of
	 * <code>IVMInstallType</code>.  The values are instances of <code>java.util.List</code>
	 * which contain instances of <code>IVMInstall</code>.
	 *
	 * @return Map the mapping of VM install types to lists of VMs
	 */
	public Map<IVMInstallType, List<IVMInstall>> getVMTypeToVMMap() {
		return fVMTypeToVMMap;
	}

	/**
	 * Return a list of all VMs in this container, including any invalid VMs.  An invalid
	 * VM is one whose install location does not exist on the file system.
	 * The order of the list is not specified.
	 *
	 * @return List the data structure containing all VMs managed by this container
	 */
	public List<IVMInstall> getVMList() {
		return fVMList;
	}

	/**
	 * Return a list of all valid VMs in this container.  A valid VM is one whose install
	 * location exists on the file system.  The order of the list is not specified.
	 *
	 * @return List
	 */
	public List<IVMInstall> getValidVMList() {
		List<IVMInstall> vms = getVMList();
		List<IVMInstall> resultList = new ArrayList<>(vms.size());
		resultList.addAll(vms);
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
	 * @throws CoreException if serialization of the XML document failed
	 */
	public String getAsXML() throws CoreException {

		// Create the Document and the top-level node
		Document doc = DebugPlugin.newDocument();
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
		Set<IVMInstallType> vmInstallTypeSet = getVMTypeToVMMap().keySet();
		Iterator<IVMInstallType> keyIterator = vmInstallTypeSet.iterator();
		while (keyIterator.hasNext()) {
			IVMInstallType vmInstallType = keyIterator.next();
			Element vmTypeElement = vmTypeAsElement(doc, vmInstallType);
			config.appendChild(vmTypeElement);
		}

		// Serialize the Document and return the resulting String
		return DebugPlugin.serializeDocument(doc);
	}

	/**
	 * Create and return a node for the specified VM install type in the specified Document.
	 *
	 * @param doc the backing {@link Document}
	 * @param vmType the {@link IVMInstallType} to create an {@link Element} for
	 * @return the new {@link Element}
	 */
	private Element vmTypeAsElement(Document doc, IVMInstallType vmType) {

		// Create a node for the VM type and set its 'id' attribute
		Element element= doc.createElement("vmType");   //$NON-NLS-1$
		element.setAttribute("id", vmType.getId());     //$NON-NLS-1$

		// For each VM of the specified type, create a subordinate node for it
		List<IVMInstall> vmList = getVMTypeToVMMap().get(vmType);
		Iterator<IVMInstall> vmIterator = vmList.iterator();
		while (vmIterator.hasNext()) {
			IVMInstall vm = vmIterator.next();
			Element vmElement = vmAsElement(doc, vm);
			element.appendChild(vmElement);
		}

		return element;
	}

	/**
	 * Create and return a node for the specified VM in the specified Document.
	 *
	 * @param doc the backing {@link Document}
	 * @param vm the {@link IVMInstall} to create an {@link Element} for
	 * @return the new {@link Element} representing the given {@link IVMInstall}
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
				StringBuilder buffer = new StringBuilder();
				for (int i = 0; i < vmArgs.length; i++) {
					buffer.append(vmArgs[i] + " "); //$NON-NLS-1$
				}
				element.setAttribute("vmargs", buffer.toString()); //$NON-NLS-1$
			}
		}

		// VM attributes
		if (vm instanceof AbstractVMInstall) {
			Map<String, String> attributes = ((AbstractVMInstall)vm).getAttributes();
			if (!attributes.isEmpty()) {
				Element attrElement = doc.createElement("attributeMap"); //$NON-NLS-1$
				Iterator<Entry<String, String>> iterator = attributes.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					Element entryElement = doc.createElement("entry"); //$NON-NLS-1$
					entryElement.setAttribute("key", entry.getKey()); //$NON-NLS-1$
					entryElement.setAttribute("value", entry.getValue()); //$NON-NLS-1$
					attrElement.appendChild(entryElement);
				}
				element.appendChild(attrElement);
			}
		}

		return element;
	}

	/**
	 * Create and return a 'libraryLocations' node.  This node owns subordinate nodes that
	 * list individual library locations.
	 *
	 * @param doc the backing {@link Document}
	 * @param locations the array of {@link LibraryLocation}s to create an {@link Element} for
	 * @return the new {@link Element} for the given {@link LibraryLocation}s
	 */
	private static Element libraryLocationsAsElement(Document doc, LibraryLocation[] locations) {
		Element root = doc.createElement("libraryLocations");       //$NON-NLS-1$
		for (int i = 0; i < locations.length; i++) {
			Element element = doc.createElement("libraryLocation");  //$NON-NLS-1$
			element.setAttribute("jreJar", locations[i].getSystemLibraryPath().toString()); //$NON-NLS-1$
			element.setAttribute("jreSrc", locations[i].getSystemLibrarySourcePath().toString()); //$NON-NLS-1$
			IPath annotationsPath = locations[i].getExternalAnnotationsPath();
			if (null != annotationsPath && !annotationsPath.isEmpty()) {
				element.setAttribute("jreExternalAnns", annotationsPath.toString()); //$NON-NLS-1$
			}

			IPath packageRootPath = locations[i].getPackageRootPath();
            if (packageRootPath != null) {
                element.setAttribute("pkgRoot", packageRootPath.toString()); //$NON-NLS-1$
            }

			URL javadocURL= locations[i].getJavadocLocation();
			if (javadocURL != null) {
				element.setAttribute("jreJavadoc", javadocURL.toExternalForm()); //$NON-NLS-1$
			}
			URL indexURL = locations[i].getIndexLocation();
			if(indexURL != null) {
				element.setAttribute("jreIndex", indexURL.toExternalForm()); //$NON-NLS-1$
			}
			root.appendChild(element);
		}
		return root;
	}

	public static VMDefinitionsContainer parseXMLIntoContainer(InputStream inputStream) throws IOException {
		VMDefinitionsContainer container = new VMDefinitionsContainer();
		parseXMLIntoContainer(inputStream, container);
		return container;
	}

	/**
	 * Parse the VM definitions contained in the specified InputStream into the
	 * specified container.
	 * <p>
	 * The VMs in the returned container are instances of <code>VMStandin</code>.
	 * </p>
	 * <p>
	 * This method has no side-effects.  That is, no notifications are sent for VM adds,
	 * changes, deletes, and the workbench preferences are not affected.
	 * </p>
	 * <p>
	 * If the <code>getAsXML</code> method is called on the returned container object,
	 * the resulting XML will be semantically equivalent (though not necessarily syntactically equivalent) as
	 * the XML contained in <code>inputStream</code>.
	 * </p>
	 * @param inputStream the <code>InputStream</code> containing XML that declares a set of VMs and a default VM
	 * @param container the container to add the VM definitions to
	 * @throws IOException if this method fails. Reasons include:<ul>
	 * <li>the XML in <code>inputStream</code> was badly formatted</li>
	 * <li>the top-level node was not 'vmSettings'</li>
	 * </ul>
	 * @since 3.2
	 */
	public static void parseXMLIntoContainer(InputStream inputStream, VMDefinitionsContainer container) throws IOException {


		// Do the parsing and obtain the top-level node
		Element config= null;
		// Wrapper the stream for efficient parsing
		try (InputStream stream = new BufferedInputStream(inputStream)) {
			@SuppressWarnings("restriction")
			DocumentBuilder parser = org.eclipse.core.internal.runtime.XmlProcessorFactory.createDocumentBuilderWithErrorOnDOCTYPE();
			parser.setErrorHandler(new DefaultHandler());
			config = parser.parse(new InputSource(stream)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat);
		} catch (ParserConfigurationException e) {
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat);
		}

		// If the top-level node wasn't what we expected, bail out
		if (!config.getNodeName().equalsIgnoreCase("vmSettings")) { //$NON-NLS-1$
			throw new IOException(LaunchingMessages.JavaRuntime_badFormat);
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
	}

	/**
	 * For the specified VM type node, parse all subordinate VM definitions and add them
	 * to the specified container.
	 *
	 * @param vmTypeElement the {@link Element} to populate the {@link VMDefinitionsContainer} from
	 * @param container the {@link VMDefinitionsContainer} to populate from the {@link Element}
	 */
	private static void populateVMTypes(Element vmTypeElement, VMDefinitionsContainer container) {
		// Retrieve the 'id' attribute and the corresponding VM type object
		String id = vmTypeElement.getAttribute("id");         //$NON-NLS-1$
		IVMInstallType vmType= JavaRuntime.getVMInstallType(id);
		if (vmType != null) {
			// For each VM child node, populate the container with a subordinate node
			NodeList vmNodeList = vmTypeElement.getElementsByTagName("vm"); //$NON-NLS-1$
			for (int i = 0; i < vmNodeList.getLength(); ++i) {
				populateVMForType(vmType, (Element) vmNodeList.item(i), container);
			}
		} else {
			// status information for removed VMs (missing VM type)
			NodeList vmNodeList = vmTypeElement.getElementsByTagName("vm"); //$NON-NLS-1$
			for (int i = 0; i < vmNodeList.getLength(); ++i) {
				Element vmElement = (Element) vmNodeList.item(i);
				String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
				String name = vmElement.getAttribute("name"); //$NON-NLS-1$
				IStatus status = null;
				if (name != null) {
					status = new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN,
							NLS.bind(LaunchingMessages.VMDefinitionsContainer_0, name));
				} else if (installPath != null) {
					status = new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN,
							NLS.bind(LaunchingMessages.VMDefinitionsContainer_0, installPath));
				} else {
					status = new Status(IStatus.INFO, LaunchingPlugin.ID_PLUGIN,
							NLS.bind(LaunchingMessages.VMDefinitionsContainer_2, id));
				}
				container.addStatus(status);
			}
		}
	}

	/**
	 * Parse the specified VM node, create a VMStandin for it, and add this to the
	 * specified container.
	 *
	 * @param vmType VM type
	 * @param vmElement XML element
	 * @param container container to add VM to
	 */
	private static void populateVMForType(IVMInstallType vmType, Element vmElement, VMDefinitionsContainer container) {
		String id= vmElement.getAttribute("id"); //$NON-NLS-1$
		if (id != null) {

			// Retrieve the 'path' attribute.  If none, skip this node.
			String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
			String name = vmElement.getAttribute("name"); //$NON-NLS-1$
			if (name == null) {
				if (installPath == null) {
					container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
							NLS.bind(LaunchingMessages.VMDefinitionsContainer_3, vmType.getName())));
					return;
				}
				container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN, NLS.bind(LaunchingMessages.VMDefinitionsContainer_4, installPath)));
				return;
			}
			if (installPath == null) {
				container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
						NLS.bind(LaunchingMessages.VMDefinitionsContainer_5, name)));
				return;
			}

			// Create a VMStandin for the node and set its 'name' & 'installLocation' attributes
			VMStandin vmStandin = new VMStandin(vmType, id);
			vmStandin.setName(name);
			File installLocation= new File(installPath);
			vmStandin.setInstallLocation(installLocation);
			String install = installLocation.getAbsolutePath();
			//only consider a VM changed it is a standard VM
			boolean changed = StandardVMType.ID_STANDARD_VM_TYPE.equals(vmType.getId()) &&
										LaunchingPlugin.timeStampChanged(install);
			container.addVM(vmStandin);

			// Look for subordinate nodes.  These may be 'libraryLocation',
			// 'libraryLocations' or 'versionInfo'.
			if(!changed) {
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
						} else if (subElementName.equals("libraryLocations")) { //$NON-NLS-1$
							setLibraryLocations(vmStandin, subElement);
						} else if (subElementName.equals("attributeMap")) { //$NON-NLS-1$
							NodeList entries = subElement.getElementsByTagName("entry"); //$NON-NLS-1$
							for (int j = 0; j < entries.getLength(); j++) {
								Node entryNode = entries.item(j);
								if (entryNode instanceof Element entryElement) {
									String key = entryElement.getAttribute("key"); //$NON-NLS-1$
									String value = entryElement.getAttribute("value"); //$NON-NLS-1$
									if (key != null && value != null) {
										vmStandin.setAttribute(key, value);
									}
								}
							}
						}
					}

					// javadoc URL
					String externalForm = vmElement.getAttribute("javadocURL"); //$NON-NLS-1$
					if (externalForm != null && externalForm.length() > 0) {
						try {
							vmStandin.setJavadocLocation(new URL(externalForm));
						} catch (MalformedURLException e) {
							container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
									NLS.bind(LaunchingMessages.VMDefinitionsContainer_6, name), e));
						}
					}
				}
			}
			// VM Arguments
			String vmArgs = vmElement.getAttribute("vmargs"); //$NON-NLS-1$
			if (vmArgs != null && vmArgs.length() >0) {
				vmStandin.setVMArgs(vmArgs);
			}
		} else {
			String installPath= vmElement.getAttribute("path"); //$NON-NLS-1$
			String name = vmElement.getAttribute("name"); //$NON-NLS-1$
			if (name != null) {
				container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
						NLS.bind(LaunchingMessages.VMDefinitionsContainer_7, name)));
			} else if (installPath != null) {
				container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
						NLS.bind(LaunchingMessages.VMDefinitionsContainer_7, installPath)));
			} else {
				container.addStatus(new Status(IStatus.ERROR, LaunchingPlugin.ID_PLUGIN,
						NLS.bind(LaunchingMessages.VMDefinitionsContainer_9, vmType.getName())));
			}
		}
	}

	/**
	 * Create & return a LibraryLocation object populated from the attribute values
	 * in the specified node.
	 *
	 * @param libLocationElement the {@link Element} to parse the {@link LibraryLocation} from
	 * @return the new {@link LibraryLocation} or <code>null</code> if the {@link Element} was malformed
	 */
	private static LibraryLocation getLibraryLocation(Element libLocationElement) {
		String jreJar= libLocationElement.getAttribute("jreJar"); //$NON-NLS-1$
		String jreSrc= libLocationElement.getAttribute("jreSrc"); //$NON-NLS-1$
		String pkgRoot= libLocationElement.getAttribute("pkgRoot"); //$NON-NLS-1$
		String jreJavadoc= libLocationElement.getAttribute("jreJavadoc"); //$NON-NLS-1$
		String jreIndex= libLocationElement.getAttribute("jreIndex"); //$NON-NLS-1$
		String externalAnns = libLocationElement.getAttribute("jreExternalAnns"); //$NON-NLS-1$
		// javadoc URL
		URL javadocURL= null;
		if (jreJavadoc.length() == 0) {
			jreJavadoc= null;
		} else {
			try {
				javadocURL= new URL(jreJavadoc);
			} catch (MalformedURLException e) {
				LaunchingPlugin.log("Library location javadoc element is specified incorrectly.");  //$NON-NLS-1$
			}
		}
		// index URL
		URL indexURL= null;
		if (jreIndex.length() == 0) {
			jreIndex= null;
		} else {
			try {
				indexURL= new URL(jreIndex);
			} catch (MalformedURLException e) {
				LaunchingPlugin.log("Library location jre index element is specified incorrectly.");  //$NON-NLS-1$
			}
		}
		if (jreJar != null && jreSrc != null && pkgRoot != null) {
			return new LibraryLocation(new Path(jreJar), new Path(jreSrc), new Path(pkgRoot), javadocURL, indexURL
					, externalAnns == null ? null : new Path(externalAnns));
		}
		LaunchingPlugin.log("Library location element is specified incorrectly.");  //$NON-NLS-1$
		return null;
	}

	/**
	 * Set the LibraryLocations on the specified VM, by extracting the subordinate
	 * nodes from the specified 'lirbaryLocations' node.
	 *
	 * @param vm the {@link IVMInstall} to populate from the given {@link Element}
	 * @param libLocationsElement the {@link Element} to populate the {@link IVMInstall} with
	 */
	private static void setLibraryLocations(IVMInstall vm, Element libLocationsElement) {
		NodeList list = libLocationsElement.getChildNodes();
		int length = list.getLength();
		List<LibraryLocation> locations = new ArrayList<>(length);
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
		vm.setLibraryLocations(locations.toArray(new LibraryLocation[locations.size()]));
	}

	/**
	 * Removes the VM from this container.
	 *
	 * @param vm VM install
	 */
	public void removeVM(IVMInstall vm) {
		fVMList.remove(vm);
		fInvalidVMList.remove(vm);
		List<IVMInstall> list = fVMTypeToVMMap.get(vm.getVMInstallType());
		if (list != null) {
			list.remove(vm);
		}
	}

	private void addStatus(IStatus status) {
		if (fStatus == null) {
			fStatus = new MultiStatus(LaunchingPlugin.ID_PLUGIN, 0, LaunchingMessages.VMDefinitionsContainer_10, null);
		}
		fStatus.add(status);
	}

	/**
	 * Returns status from parsing VM installs or <code>null</code> if none.
	 *
	 * @return status or <code>null</code>
	 */
	public IStatus getStatus() {
		return fStatus;
	}
}
