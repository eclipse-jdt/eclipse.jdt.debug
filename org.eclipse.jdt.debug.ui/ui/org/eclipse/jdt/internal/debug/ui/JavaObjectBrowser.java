/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.DefaultObjectBrowser;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.debug.ui.IJavaObjectBrowser;

public class JavaObjectBrowser extends DefaultObjectBrowser {

	/**
	 * Mapping of fully qualified type names to <code>IConfigurationElements</code>
	 * that describe Java object browsers.
	 */
	private Map fConfigElementMap;
	
	/**
	 * Mapping of fully qualified type names to <code>IJavaObjectBrowser</code>s.
	 */
	private Map fObjectBrowserMap;
	
	public JavaObjectBrowser() {
		loadConfigElementMap();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IObjectBrowser#getChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.debug.core.model.IValue)
	 */
	public IVariable[] getChildren(IValue value) throws DebugException {
		IJavaObjectBrowser objectBrowser = getObjectBrowser(value);		
		if (objectBrowser == null) {
			return super.getChildren(value);
		}
		IJavaVariable[] result = objectBrowser.getChildren((IJavaValue)value);
		// If specified content provider can't get children, defer to the default content provider
		if (result == null) {
			return super.getChildren(value);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IObjectBrowser#hasChildren(org.eclipse.debug.ui.IDebugView, org.eclipse.debug.core.model.IValue)
	 */
	public boolean hasChildren(IValue value) throws DebugException {
		IJavaObjectBrowser objectBrowser = getObjectBrowser(value);		
		if (objectBrowser == null) {
			return super.hasChildren(value);
		}
		return objectBrowser.hasChildren((IJavaValue)value);
	}

	protected IJavaObjectBrowser getObjectBrowser(IValue value) throws DebugException {
		IJavaValue javaValue = (IJavaValue) value;
		IJavaObjectBrowser objectBrowser = getObjectBrowserForValue(javaValue);
		return objectBrowser;
	}

	/**
	 * Look for a registered content provider for the specified java value.  Do this by working
	 * up the class and interface hierarchy at a time.  This ensures that the content provider
	 * 'closest' to the type of the specified value is returned first.
	 */
	protected IJavaObjectBrowser getObjectBrowserForValue(IJavaValue javaValue) throws DebugException {
		Set checkedInterfaces = new HashSet();
		
		// Only consider values whose types are classes.  Contributed content providers are
		// not supported for arrays and primitives
		IJavaType javaType = javaValue.getJavaType();
		if (!(javaType instanceof IJavaClassType)) {
			return null;
		}
		IJavaClassType javaClassType = (IJavaClassType) javaType;
		while (javaClassType != null) {
			
			// Retrieve the content provider for the current class, if one was declared
			IJavaObjectBrowser objectBrowser = getExecutableExtension(javaClassType);
			if (objectBrowser != null) {
				return objectBrowser;			
			}
			
			// Start by checking the interfaces the current class directly implements
			IJavaInterfaceType[] interfaces = javaClassType.getInterfaces();
			List nextInterfacesList = new ArrayList();

			// Work up the interface hierarchy one level at a time
			while (interfaces.length > 0) {
				
				// Check each interface
				for (int i = 0; i < interfaces.length; i++) {
					
					// Make sure we haven't already considered this interface
					IJavaInterfaceType interfaceType = interfaces[i];
					String interfaceName = interfaceType.getName();
					if (checkedInterfaces.contains(interfaceName)) {
						continue;
					}
					
					// If we get a match on this interface, we're done
					objectBrowser = getExecutableExtension(interfaceType);
					if (objectBrowser != null) {
						return objectBrowser;
					}
					
					// Make sure we don't consider this interface again
					checkedInterfaces.add(interfaceName);
					
					// Now, add the directly extended super interfaces to the list to be checked,
					// assuming they haven't already been checked
					IJavaInterfaceType[] superInterfaces = interfaceType.getSuperInterfaces();
					for (int j = 0; j < superInterfaces.length; j++) {
						String superInterfaceName = superInterfaces[j].getName();
						if (!checkedInterfaces.contains(superInterfaceName) &&
						    !nextInterfacesList.contains(superInterfaceName)) {
							nextInterfacesList.add(superInterfaces[j]);
						}
					}					
				}
				
				// Now get ready to check the immediate super interfaces of all the interfaces just checked
				IJavaInterfaceType[] tempInterfaces = new IJavaInterfaceType[nextInterfacesList.size()];
				interfaces = (IJavaInterfaceType[]) nextInterfacesList.toArray(tempInterfaces);
				nextInterfacesList = new ArrayList();
			}			
			
			// Move up 1 level in the class hierarchy and try again
			javaClassType = javaClassType.getSuperclass();
		}
		return null;
	}
	
	/**
	 * Return an instance of <code>IJavaObjectBrowser</code> that corresponds
	 * to the specified Java type.
	 */
	private IJavaObjectBrowser getExecutableExtension(IJavaType javaType) throws DebugException {
		
		// Get the config element.  If there is none, then the specified java type
		// has not been registered
		String typeName = javaType.getName();
		IConfigurationElement configElement = getConfigElement(typeName);
		if (configElement == null) {
			return null;
		}
		
		// Retrieve the content provider.  If none is found, create one.
		IJavaObjectBrowser objectBrowser = getObjectBrowserByTypeName(typeName);
		if (objectBrowser == null) {	
			Object executable = null;		
			try {
				executable = JDIDebugUIPlugin.createExtension(configElement, "class"); //$NON-NLS-1$
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce);
				return null;
			}
			if (!(executable instanceof IJavaObjectBrowser)) {
				String classAttribute = configElement.getAttribute("class"); //$NON-NLS-1$
				String message= MessageFormat.format(DebugUIMessages.getString("JavaObjectBrowser.4"), new String[]{classAttribute, configElement.getDeclaringExtension().getUniqueIdentifier()}); //$NON-NLS-1$
				JDIDebugUIPlugin.logErrorMessage(message);
				return null;
			}	
			objectBrowser = (IJavaObjectBrowser) executable;
			fObjectBrowserMap.put(typeName, objectBrowser);		
		}
		return objectBrowser;
	}

	private IConfigurationElement getConfigElement(String typeName) {
		return (IConfigurationElement) fConfigElementMap.get(typeName);
	}
	
	private IJavaObjectBrowser getObjectBrowserByTypeName(String typeName) {
		if (fObjectBrowserMap == null) {
			fObjectBrowserMap = new HashMap();
		}
		return (IJavaObjectBrowser) fObjectBrowserMap.get(typeName);		
	}

	/**
	 * Load the mapping of fully qualified Java type names to configuration elements.
	 */
	protected void loadConfigElementMap() {
		IPluginDescriptor descriptor = JDIDebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IJavaDebugUIConstants.EXTENSION_POINT_JAVA_OBJECT_BROWSERS);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		
		fConfigElementMap = new HashMap(10);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configElement = infos[i];
			String typeNameList = configElement.getAttribute("types"); //$NON-NLS-1$
			String[] typeNames = parseTypeNameList(typeNameList);
			for (int j = 0; j < typeNames.length; j++) {
				fConfigElementMap.put(typeNames[j], configElement);
			}
		}						
	}

	private String[] parseTypeNameList(String typeNameList) {
		StringTokenizer tokenizer = new StringTokenizer(typeNameList, " \t\n\r,"); //$NON-NLS-1$
		List tokenList = new ArrayList();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			tokenList.add(token);
		}
		String[] tokenArray = new String[tokenList.size()];
		tokenArray = (String[]) tokenList.toArray(tokenArray);
		return tokenArray;
	}

}
