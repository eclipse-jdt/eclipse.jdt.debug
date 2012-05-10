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

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import org.eclipse.core.runtime.IProgressMonitor;


public class BundleBuilder implements BundleAttributes {
	
	private List<Process> fProcesses= new ArrayList<Process>();
	private BundleDescription fBundleDescription;
	
	
	/**
	 * Create a new bundle
	 * @param bd the new description
	 * @param pm progress monitor
	 * @throws IOException if something happens
	 */
	public void createBundle(BundleDescription bd, IProgressMonitor pm) throws IOException {
		
		fBundleDescription= bd;
		
		File tmp_dir= new File(bd.get(DESTINATIONDIRECTORY));
		String app_dir_name= bd.get(APPNAME) + ".app";	//$NON-NLS-1$
		File app_dir= new File(tmp_dir, app_dir_name);
		if (app_dir.exists())
			deleteDir(app_dir);
		app_dir= createDir(tmp_dir, app_dir_name, false);
		
		File contents_dir= createDir(app_dir, "Contents", false);	//$NON-NLS-1$
		createPkgInfo(contents_dir);

		File macos_dir= createDir(contents_dir, "MacOS", false);	//$NON-NLS-1$
		String launcher_path= bd.get(LAUNCHER);
		if (launcher_path == null)
			throw new IOException();		
		String launcher= copyFile(macos_dir, launcher_path, null);
		
		File resources_dir= createDir(contents_dir, "Resources", false);	//$NON-NLS-1$
		File java_dir= createDir(resources_dir, "Java", false);	//$NON-NLS-1$
				
		createInfoPList(contents_dir, resources_dir, java_dir, launcher);
		
		Iterator<Process> iter= fProcesses.iterator();
		while (iter.hasNext()) {
			Process p= iter.next();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// silently ignore
			}
		}		
	}
	
	private void createInfoPList(File contents_dir, File resources_dir, File java_dir, String launcher) throws IOException {
		DocumentBuilder docBuilder= null;
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		try {   	
			docBuilder= factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			System.err.println("createInfoPList: could not get XML builder"); //$NON-NLS-1$
			throw new IOException("Could not get XML builder"); //$NON-NLS-1$
		}
		Document doc= docBuilder.newDocument();
		
		Element plist= doc.createElement("plist"); //$NON-NLS-1$
		doc.appendChild(plist);
		plist.setAttribute("version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Element dict= doc.createElement("dict"); //$NON-NLS-1$
		plist.appendChild(dict);
		
		pair(dict, "CFBundleExecutable", null, launcher); //$NON-NLS-1$
		pair(dict, "CFBundleGetInfoString", GETINFO, null); //$NON-NLS-1$
		pair(dict, "CFBundleInfoDictionaryVersion", null, "6.0"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String iconName= null;
		String appName= fBundleDescription.get(APPNAME, null);
		if (appName != null)
			iconName= appName + ".icns"; //$NON-NLS-1$
		String fname= copyFile(resources_dir, fBundleDescription.get(ICONFILE, null), iconName);
		if (fname != null)
			pair(dict, "CFBundleIconFile", null, fname); //$NON-NLS-1$
		
		pair(dict, "CFBundleIdentifier", IDENTIFIER, null); //$NON-NLS-1$
		pair(dict, "CFBundleName", APPNAME, null); //$NON-NLS-1$
		pair(dict, "CFBundlePackageType", null, "APPL"); //$NON-NLS-1$ //$NON-NLS-2$
		pair(dict, "CFBundleShortVersionString", VERSION, null); //$NON-NLS-1$
		pair(dict, "CFBundleSignature", SIGNATURE, "????"); //$NON-NLS-1$ //$NON-NLS-2$
		pair(dict, "CFBundleVersion", null, "1.0.1"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Element jdict= doc.createElement("dict"); //$NON-NLS-1$
		add(dict, "Java", jdict); //$NON-NLS-1$
		
		pair(jdict, "JVMVersion", JVMVERSION, null); //$NON-NLS-1$
		pair(jdict, "MainClass", MAINCLASS, null); //$NON-NLS-1$
		pair(jdict, "WorkingDirectory", WORKINGDIR, null); //$NON-NLS-1$
		
		if (fBundleDescription.get(USES_SWT, false))
			addTrue(jdict, "StartOnMainThread"); //$NON-NLS-1$
		
		String arguments= fBundleDescription.get(ARGUMENTS, null);
		if (arguments != null) {
			Element argArray= doc.createElement("array");	//$NON-NLS-1$
			add(jdict, "Arguments", argArray);	//$NON-NLS-1$
			StringTokenizer st= new StringTokenizer(arguments);	
			while (st.hasMoreTokens()) {
				String arg= st.nextToken();
				Element type= doc.createElement("string"); //$NON-NLS-1$
				argArray.appendChild(type);	
				type.appendChild(doc.createTextNode(arg));			
			}
		}
		
		pair(jdict, "VMOptions", VMOPTIONS, null); //$NON-NLS-1$
		
		int[] id= new int[] { 0 };
		ResourceInfo[] ris= fBundleDescription.getResources(true);
		if (ris.length > 0) {
			StringBuffer cp= new StringBuffer();
			for (int i= 0; i < ris.length; i++) {
				ResourceInfo ri= ris[i];
				String e= processClasspathEntry(java_dir, ri.fPath, id);
				if (cp.length() > 0)
					cp.append(':');
				cp.append(e);
			}
			add(jdict, "ClassPath", cp.toString()); //$NON-NLS-1$
		}

		ris= fBundleDescription.getResources(false);
		if (ris.length > 0) {
			for (int i= 0; i < ris.length; i++) {
				ResourceInfo ri= ris[i];
				processClasspathEntry(java_dir, ri.fPath, id);
			}
		}

		File info= new File(contents_dir, "Info.plist"); //$NON-NLS-1$
		FileOutputStream fos= new FileOutputStream(info);
		BufferedOutputStream fOutputStream= new BufferedOutputStream(fos);
		try {
			// Write the document to the stream
			Transformer transformer= TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//Apple Computer//DTD PLIST 1.0//EN"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.apple.com/DTDs/PropertyList-1.0.dtd"); //$NON-NLS-1$
 			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			DOMSource source= new DOMSource(doc);
			StreamResult result= new StreamResult(fOutputStream);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			System.err.println("createInfoPList: could not transform to XML"); //$NON-NLS-1$
		}
		finally {
			fOutputStream.close();
		}
	}
	
	private void add(Element dict, String key, Element value) {
		Document document= dict.getOwnerDocument();
		Element k= document.createElement("key"); //$NON-NLS-1$
		dict.appendChild(k);
		k.appendChild(document.createTextNode(key));
		dict.appendChild(value);
	}
	
	private void create(Element parent, String s) {
		Document document= parent.getOwnerDocument();
		Element type= document.createElement("string"); //$NON-NLS-1$
		parent.appendChild(type);	
		type.appendChild(document.createTextNode(s));
	}

	private void createTrue(Element parent) {
		Document document= parent.getOwnerDocument();
		Element type= document.createElement("true"); //$NON-NLS-1$
		parent.appendChild(type);	
	}

	private void add(Element dict, String key, String value) {
		Document document= dict.getOwnerDocument();
		Element k= document.createElement("key"); //$NON-NLS-1$
		dict.appendChild(k);
		k.appendChild(document.createTextNode(key));
		create(dict, value);
	}
	
	private void addTrue(Element dict, String key) {
		Document document= dict.getOwnerDocument();
		Element k= document.createElement("key"); //$NON-NLS-1$
		dict.appendChild(k);
		k.appendChild(document.createTextNode(key));
		createTrue(dict);
	}
	
	private void pair(Element dict, String outkey, String inkey, String dflt) {
		String value= null;
		if (inkey != null)
			value= fBundleDescription.get(inkey, dflt);
		else
			value= dflt;
		if (value != null && value.trim().length() > 0) {
			add(dict, outkey, value);
		}
	}
	
	private String processClasspathEntry(File java_dir, String name, int[] id_ref) throws IOException {
		File f= new File(name);
		if (f.isDirectory()) {
			int id= id_ref[0]++;
			String archivename= "jar_" + id + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
			File to= new File(java_dir, archivename);
			zip(name, to.getAbsolutePath());
			name= archivename;
		} else {
			name= copyFile(java_dir, name, null);
		}
		return "$JAVAROOT/" + name; //$NON-NLS-1$
	}
	
	private void createPkgInfo(File contents_dir) throws IOException {
		File pkgInfo= new File(contents_dir, "PkgInfo"); //$NON-NLS-1$
		FileOutputStream os= new FileOutputStream(pkgInfo);
		os.write(("APPL" + fBundleDescription.get(SIGNATURE, "????")).getBytes());	//$NON-NLS-1$ //$NON-NLS-2$
		os.close();
	}
		
	private static void deleteDir(File dir) {
		File[] files= dir.listFiles();
		if (files != null) {
			for (int i= 0; i < files.length; i++)
				deleteDir(files[i]);
		}
		dir.delete();
	}
	
	private File createDir(File parent_dir, String dir_name, boolean remove) throws IOException {
		File dir= new File(parent_dir, dir_name);
		if (dir.exists()) {
			if (!remove)
				return dir;
			deleteDir(dir);
		}
		if (! dir.mkdir())
			throw new IOException("cannot create dir " + dir_name); //$NON-NLS-1$
		return dir;
	}
	
	private String copyFile(File todir, String fromPath, String toname) throws IOException {
		if (toname == null) {
			int pos= fromPath.lastIndexOf('/');
			if (pos >= 0)
				toname= fromPath.substring(pos+1);
			else
				toname= fromPath;
		}
		File to= new File(todir, toname);
		fProcesses.add(Runtime.getRuntime().exec(new String[] { "/bin/cp", fromPath, to.getAbsolutePath() }));	//$NON-NLS-1$
		return toname;
	}

	private void zip(String dir, String dest) throws IOException {
		fProcesses.add(Runtime.getRuntime().exec(new String[] { "/usr/bin/jar", "cf", dest, "-C", dir, "." })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
