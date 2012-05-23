/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.util.*;
import org.eclipse.core.runtime.ListenerList;

import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;


class BundleDescription implements BundleAttributes {
	
	private static final String STUB= "/System/Library/Frameworks/JavaVM.framework/Versions/A/Resources/MacOS/JavaApplicationStub"; //$NON-NLS-1$
	private static final String ICON= "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Resources/GenericApp.icns"; //$NON-NLS-1$
	private static  Set<String> RUN_MODE;
	{
		RUN_MODE = new HashSet<String>();
		RUN_MODE.add(ILaunchManager.RUN_MODE);
	}
		
	private ListenerList fListeners= new ListenerList();
	private Properties fProperties= new Properties();
	private List<ResourceInfo> fClassPath= new ArrayList<ResourceInfo>();
	private List<ResourceInfo> fResources= new ArrayList<ResourceInfo>();
	Properties fProperties2= new Properties();
	
	
	BundleDescription() {
		clear();
	}
	
	void clear() {
		fProperties.clear();
		fClassPath.clear();
		fResources.clear();
		fProperties2.clear();
		fProperties.put(SIGNATURE, "????"); //$NON-NLS-1$
		fProperties.put(ICONFILE, ICON);
	}
	
	void addResource(ResourceInfo ri, boolean onClasspath) {
		if (onClasspath)
			fClassPath.add(ri);
		else
			fResources.add(ri);
	}
	
	boolean removeResource(ResourceInfo ri, boolean onClasspath) {
		if (onClasspath)
			return fClassPath.remove(ri);
		return fResources.remove(ri);	
	}

	ResourceInfo[] getResources(boolean onClasspath) {
		if (onClasspath)
			return fClassPath.toArray(new ResourceInfo[fClassPath.size()]);
		return fResources.toArray(new ResourceInfo[fResources.size()]);
	}
	
	void addListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}
	
	void removeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}
	
	String get(String key) {
		return fProperties.getProperty(key);
	}
	
	public String get(String key, String dflt) {
		return fProperties.getProperty(key, dflt);
	}
	
	public boolean get(String key, boolean dflt) {
		Boolean v= (Boolean) fProperties.get(key);
		if (v == null)
			return dflt;
		return v.booleanValue();
	}
	
	void setValue(String key, Object value) {
		fProperties.put(key, value);
	}
	
	private static AbstractJavaLaunchConfigurationDelegate getDelegate(ILaunchConfiguration lc) throws CoreException {
		ILaunchDelegate[] delegates = lc.getType().getDelegates(RUN_MODE);
		for (int i = 0; i < delegates.length; i++) {
			if (delegates[i].getDelegate() instanceof AbstractJavaLaunchConfigurationDelegate) {
				return (AbstractJavaLaunchConfigurationDelegate) delegates[i].getDelegate();
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, MacOSXUILaunchingPlugin.getUniqueIdentifier(), "Internal Error: missing Java launcher")); //$NON-NLS-1$
	}
	
	void inititialize(ILaunchConfiguration lc) {
		AbstractJavaLaunchConfigurationDelegate lcd;
		try {
			lcd= getDelegate(lc);
		} catch (CoreException e) {
			return;
		}
		
		String appName= lc.getName();
		fProperties.put(APPNAME, appName);
		fProperties.put(GETINFO, appName + Util.getString("BundleDescription.copyright.format")); //$NON-NLS-1$
		
		try {
			fProperties.put(MAINCLASS, lcd.getMainTypeName(lc));
		} catch (CoreException e) {
			fProperties.put(MAINCLASS, ""); //$NON-NLS-1$
		}
		try {
			fProperties.put(ARGUMENTS, lcd.getProgramArguments(lc));
		} catch (CoreException e) {
			fProperties.put(ARGUMENTS, ""); //$NON-NLS-1$
		}
		String wd= null;
		try {
			wd= lcd.getWorkingDirectory(lc).getAbsolutePath();
//			fProperties.put(WORKINGDIR, wd); //$NON-NLS-1$
		} catch (CoreException e) {
//			fProperties.put(WORKINGDIR, ""); //$NON-NLS-1$
		}
		try {
			fProperties.put(MAINCLASS, lcd.getMainTypeName(lc));
		} catch (CoreException e) {
			fProperties.put(MAINCLASS, ""); //$NON-NLS-1$
		}
		
		try {
			String[] classpath= lcd.getClasspath(lc);
			for (int i= 0; i < classpath.length; i++)
				addResource(new ResourceInfo(classpath[i]), true);
		} catch (CoreException e) {
			//
		}
		
		String vmOptions2= ""; //$NON-NLS-1$
		String vmOptions= null;
		try {
			vmOptions= lcd.getVMArguments(lc);
		} catch (CoreException e) {
			//
		}
		if (vmOptions != null) {
			StringTokenizer st= new StringTokenizer(vmOptions);
			while (st.hasMoreTokens()) {
				String token= st.nextToken();
				int pos= token.indexOf('=');
				if (pos > 2 && token.startsWith("-D")) { //$NON-NLS-1$
					String key= token.substring(2, pos).trim();
					String value= token.substring(pos+1).trim();
					int l= value.length();
					if (l >= 2 && value.charAt(0) == '"' && value.charAt(l-1) == '"')
						value= value.substring(1, l-1);
					if ("java.library.path".equals(key)) { //$NON-NLS-1$
						addDllDir(wd, value);
					} else {
						fProperties2.put(key, value);
					}
				} else {
					vmOptions2= vmOptions2 + token + ' ';
				}
			}
		}

		fProperties.put(VMOPTIONS, vmOptions2);
		
		boolean isSWT= false;
		Iterator<ResourceInfo> iter= fResources.iterator();
		while (iter.hasNext()) {
			ResourceInfo ri= iter.next();
			if (ri.fPath.indexOf("libswt-carbon") >= 0) {	//$NON-NLS-1$
				isSWT= true;
				break;
			}
		}
		fProperties.put(USES_SWT, Boolean.valueOf(isSWT));
		
		String launcher= null;
		if (isSWT)
			launcher= System.getProperty("org.eclipse.swtlauncher");	//$NON-NLS-1$
		
		if (launcher == null) {
			setValue(JVMVERSION, "1.4*"); //$NON-NLS-1$
			launcher= STUB;		
		}
		setValue(LAUNCHER, launcher);

		
		IJavaProject p= null;
		try {
			p= lcd.getJavaProject(lc);
		} catch (CoreException e) {
			// ignore
		}
		if (p != null)
			fProperties.put(IDENTIFIER, p.getElementName());
		else
			fProperties.put(IDENTIFIER, ""); //$NON-NLS-1$
				
		fireChange();
	}
	
	void fireChange() {
		PropertyChangeEvent e= new PropertyChangeEvent(this, ALL, null, null);
		Object[] listeners= fListeners.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IPropertyChangeListener)listeners[i]).propertyChange(e);
	}

	private void addDllDir(String wd, String path) {
		File lib_dir;
		if (path.startsWith("../")) { //$NON-NLS-1$
			lib_dir= new File(wd, path);
		} else {
			lib_dir= new File(path);			
		}
		if (lib_dir.isDirectory()) {
			File[] dlls= lib_dir.listFiles();
			for (int j= 0; j < dlls.length; j++) {
				try {
					String name= dlls[j].getCanonicalPath();
					if (name.endsWith(".jnilib")) //$NON-NLS-1$
						addResource(new ResourceInfo(name), false);
				} catch (IOException e) {
					// NeedWork Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	static boolean verify(ILaunchConfiguration lc) {
		String name= lc.getName();
		if (name.indexOf("jpage") >= 0) //$NON-NLS-1$
			return false;
		AbstractJavaLaunchConfigurationDelegate lcd;
		try {
			lcd= getDelegate(lc);
			if (lcd.getMainTypeName(lc) == null)
				return false;
			return true;
		} catch (CoreException e) {
			return false;
		}
	}
	
	static boolean matches(ILaunchConfiguration lc, IJavaProject project) {
		AbstractJavaLaunchConfigurationDelegate lcd;
		try {
			lcd= getDelegate(lc);
		} catch (CoreException e) {
			return false;
		}
		IJavaProject p= null;
		try {
			p= lcd.getJavaProject(lc);
		} catch (CoreException e) {
			return false;
		}
		return project != null && project.equals(p);
	}
}
