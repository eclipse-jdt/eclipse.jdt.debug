/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.PreferenceModifyListener;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Manages import of installed JREs. Merges valid imported JREs with existing JREs.
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
					String pref = InstanceScope.INSTANCE.getNode(LaunchingPlugin.ID_PLUGIN).get(JavaRuntime.PREF_VM_XML, ""); //$NON-NLS-1$
					// names -> existing vm's
					Map<String, IVMInstall> names = new HashMap<String, IVMInstall>();
					Set<String> ids = new HashSet<String>();
					if (pref.length() > 0) {
						try {
							VMDefinitionsContainer container = VMDefinitionsContainer.parseXMLIntoContainer(new ByteArrayInputStream(pref.getBytes("UTF8"))); //$NON-NLS-1$
							List<IVMInstall> validVMList = container.getValidVMList();
							Iterator<IVMInstall> iterator = validVMList.iterator();
							while (iterator.hasNext()) {
								IVMInstall vm = iterator.next();
								names.put(vm.getName(), vm);
								ids.add(vm.getId());
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
					try {
						ByteArrayInputStream inputStream = new ByteArrayInputStream(jresXML.getBytes("UTF8")); //$NON-NLS-1$
						VMDefinitionsContainer container = VMDefinitionsContainer.parseXMLIntoContainer(inputStream);
						List<IVMInstall> validVMList = container.getValidVMList();
						Iterator<IVMInstall> iterator = validVMList.iterator();
						while (iterator.hasNext()) {
							IVMInstall vm = iterator.next();
							IVMInstall existing = names.get(vm.getName());
							if (existing != null) {
								// VM with same name already exists - replace with imported VM
								vms.removeVM(existing);
								ids.remove(existing.getId());
							}
							boolean collision = ids.contains(vm.getId());
							if (collision) {
								// conflicting id, create a new one with unique id
								long unique = System.currentTimeMillis();	
								while(ids.contains(String.valueOf(unique))){
									unique++;
								}
								vm = new VMStandin(vm, String.valueOf(unique));
								ids.add(vm.getId()); 
							}
							vms.addVM(vm);
						}
						// update default VM if it exists
						String defaultVMInstallCompositeID = container.getDefaultVMInstallCompositeID();
						validVMList = vms.getValidVMList();
						iterator = validVMList.iterator();
						while (iterator.hasNext()) {
							IVMInstall vm = iterator.next();
							if (JavaRuntime.getCompositeIdFromVM(vm).equals(defaultVMInstallCompositeID)) {
								vms.setDefaultVMInstallCompositeID(defaultVMInstallCompositeID);
								break;
							}
						}
					} catch (IOException e) {
						LaunchingPlugin.log(e);
						return false;
					}
					try {
						String xml = vms.getAsXML();
						node.put(JavaRuntime.PREF_VM_XML, xml);
					}  catch (CoreException e) {
						LaunchingPlugin.log(e);
						return false;
					}
				}
				return false;
			}
			return true;
		}
		
	}

	@Override
	public IEclipsePreferences preApply(IEclipsePreferences node) {
		try {
			// force VMs to be initialized before we import the new VMs
			JavaRuntime.getVMInstallTypes();
			node.accept(new Visitor());
		} catch (BackingStoreException e) {
            LaunchingPlugin.log(e);
		}
		return node;
	}


}
