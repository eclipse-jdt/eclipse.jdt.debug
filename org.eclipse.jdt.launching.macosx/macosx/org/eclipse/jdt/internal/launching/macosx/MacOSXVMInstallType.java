/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.AbstractVMInstallType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;

/**
 * This plugins into the org.eclipse.jdt.launching.vmInstallTypes extension point
 */
public class MacOSXVMInstallType extends AbstractVMInstallType {
	
	private static final String JAVA_VM_NAME= "Java HotSpot(TM) Client VM";	//$NON-NLS-1$
	
	/*
	 * The directory structure for Java VMs is as follows:
	 * 	/System/Library/Frameworks/JavaVM.framework/Versions/
	 * 		1.3.1
	 * 			Home
	 * 		1.4.1
	 * 			Home
	 * 		CurrentJDK -> 1.3.1
	 */
	
	/** The OS keeps all the JVM versions in this directory */
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	/** The name of a Unix link to MacOS X's default VM */
	private static final String CURRENT_JVM= "CurrentJDK";	//$NON-NLS-1$
	/** The root of a JVM */
	private static final String JVM_ROOT= "Home";	//$NON-NLS-1$
	/** The doc (for all JVMs) lives here (if the developer kit has been expanded)*/
	private static final String JAVADOC_LOC= "/Developer/Documentation/Java/Reference/";	//$NON-NLS-1$
	
	private String fDefaultJDKID;
	
	public IVMInstall doCreateVMInstall(String id) {
		return new MacOSXVMInstall(this, id);
	}
	
	public String getName() {
		return MacOSXLauncherMessages.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	public IStatus validateInstallLocation(File installLocation) {
		String id= MacOSXLaunchingPlugin.getUniqueIdentifier();
		File java= new File(installLocation, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (java.isFile())
			return new Status(IStatus.OK, id, 0, "ok", null); //$NON-NLS-1$
		return new Status(IStatus.ERROR, id, 0, MacOSXLauncherMessages.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
	}

	/**
	 * @see IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		
			String javaVMName= System.getProperty("java.vm.name");	//$NON-NLS-1$
			if (javaVMName == null || !JAVA_VM_NAME.equals(javaVMName)) 
				return null;
	
			// find all installed VMs
			File versionDir= new File(JVM_VERSION_LOC);
			if (versionDir.exists() && versionDir.isDirectory()) {
				File currentJDK= new File(versionDir, CURRENT_JVM);
				try {
					currentJDK= currentJDK.getCanonicalFile();
				} catch (IOException ex) {
				}
				File[] versions= versionDir.listFiles();
				for (int i= 0; i < versions.length; i++) {
					String version= versions[i].getName();
					File home=  new File(versions[i], JVM_ROOT);
					if (home.exists() && findVMInstall(version) == null && !CURRENT_JVM.equals(version)) {
						VMStandin vm= new VMStandin(this, version);
						vm.setInstallLocation(home);
						String format= MacOSXLauncherMessages.getString(version.equals(fDefaultJDKID)
													? "MacOSXVMType.jvmDefaultName"		//$NON-NLS-1$
													: "MacOSXVMType.jvmName");				//$NON-NLS-1$
						vm.setName(MessageFormat.format(format, new Object[] { version } ));
						vm.setLibraryLocations(getDefaultLibraryLocations(home));
						URL doc= getDefaultJavaDocLocation(version);
						if (doc != null)
							vm.setJavadocLocation(doc);
						
						IVMInstall vm2= vm.convertToRealVM();
						if (currentJDK.equals(versions[i])) {
							try {
								JavaRuntime.setDefaultVMInstall(vm2, null);
							} catch (CoreException e) {
								// exception intentionally ignored
							}
						}
					}
				}
			}
		return null;
	}

	/**
	 * @see IVMInstallType#getDefaultSystemLibraryDescription(File)
	 */
	public LibraryLocation[] getDefaultLibraryLocations(File installLocation) {
		
		// HACK
//		String id= "1.4.1";
//		URL url= getDefaultJavaDocLocation(id);
//		if (url != null) {
//			IVMInstall vm= findVMInstall(id);
//			if (vm != null)
//				vm.setJavadocLocation(url);
//		}
		
		IPath libHome= new Path(installLocation.toString()); //$NON-NLS-1$
		libHome= libHome.append(".."); //$NON-NLS-1$
		libHome= libHome.append("Classes"); //$NON-NLS-1$
		IPath lib= libHome.append("classes.jar"); //$NON-NLS-1$
		IPath uilib= libHome.append("ui.jar"); //$NON-NLS-1$
		
		IPath source= new Path(installLocation.toString());
		source= source.append("src.jar"); //$NON-NLS-1$
		
		IPath srcPkgRoot= new Path("src"); //$NON-NLS-1$
		
		return new LibraryLocation[] {
				new LibraryLocation(lib, source, srcPkgRoot),
				new LibraryLocation(uilib, source, srcPkgRoot)
		};
	}
	
	private URL getDefaultJavaDocLocation(String id) {
		URL doc= null;
		
		// first try in local filesystem
		File docLocation= new File(JAVADOC_LOC + id);
		if (docLocation.exists()) {
			try {
				doc= new URL("file", "", JAVADOC_LOC + id);	//$NON-NLS-1$ //$NON-NLS-2$
			} catch (MalformedURLException ex) {
			}
		}
		if (doc == null) {
			// now try in a standard place on the web
			String version= id;
			if ("1.3.1".equals(id))	//$NON-NLS-1$
				version= "1.3";		//$NON-NLS-1$
			try {
				doc= new URL("http://java.sun.com/j2se/"+version+"/docs/api/");	//$NON-NLS-1$ //$NON-NLS-2$
			} catch (MalformedURLException ex) {
			}			
		}
		return doc;
	}
}
