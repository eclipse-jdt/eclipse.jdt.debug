/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.eclipse.core.runtime.preferences.PreferenceModifyListener;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Manages import of installed JREs. Merges valid impoted JREs with existing JREs.
 * 
 * @since 3.1
 */
public class JREPreferenceModifyListener extends PreferenceModifyListener {
	
	class Visitor implements IPreferenceNodeVisitor {

		public boolean visit(IEclipsePreferences node) throws BackingStoreException {
			if (node.name().equals(LaunchingPlugin.getUniqueIdentifier())) {
				String jresXML = node.get(JavaRuntime.PREF_VM_XML, null);
				if (jresXML != null) {
					VMDefinitionsContainer vms = new VMDefinitionsContainer();
					String pref = LaunchingPlugin.getDefault().getPluginPreferences().getString(JavaRuntime.PREF_VM_XML);
					Set names = new HashSet();
					Set locations = new HashSet();
					if (pref.length() > 0) {
						try {
							VMDefinitionsContainer container = VMDefinitionsContainer.parseXMLIntoContainer(new ByteArrayInputStream(pref.getBytes()));
							List validVMList = container.getValidVMList();
							Iterator iterator = validVMList.iterator();
							while (iterator.hasNext()) {
								IVMInstall vm = (IVMInstall) iterator.next();
								names.add(vm.getName());
								locations.add(vm.getInstallLocation());
								vms.addVM(vm);
							}
							vms.setDefaultVMInstallCompositeID(container.getDefaultVMInstallCompositeID());
							vms.setDefaultVMInstallConnectorTypeID(container.getDefaultVMInstallConnectorTypeID());
						} catch (IOException e) {
							LaunchingPlugin.log(e);
							return false;
						}
					}
					// merge valid VMs with existing VMs
					ByteArrayInputStream inputStream = new ByteArrayInputStream(jresXML.getBytes());
					try {
						VMDefinitionsContainer container = VMDefinitionsContainer.parseXMLIntoContainer(inputStream);
						List validVMList = container.getValidVMList();
						Iterator iterator = validVMList.iterator();
						while (iterator.hasNext()) {
							IVMInstall vm = (IVMInstall) iterator.next();
							if (!names.contains(vm.getName()) && !locations.contains(vm.getInstallLocation())) {
								vms.addVM(vm);
							}
						}
					} catch (IOException e) {
						LaunchingPlugin.log(e);
						return false;
					}
					try {
						String xml = vms.getAsXML();
						node.put(JavaRuntime.PREF_VM_XML, xml);
					} catch (ParserConfigurationException e) {
						LaunchingPlugin.log(e);
						return false;
					} catch (IOException e) {
						LaunchingPlugin.log(e);
						return false;
					} catch (TransformerException e) {
						LaunchingPlugin.log(e);
						return false;
					}
				}
				return false;
			}
			return true;
		}
		
	}

	public IEclipsePreferences preApply(IEclipsePreferences node) {
		try {
			node.accept(new Visitor());
		} catch (BackingStoreException e) {
            LaunchingPlugin.log(e);
		}
		return node;
	}


}
