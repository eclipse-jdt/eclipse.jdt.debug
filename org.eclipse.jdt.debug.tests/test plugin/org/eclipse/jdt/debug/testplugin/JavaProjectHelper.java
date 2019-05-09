/*******************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S. Møller - bug 422029: [1.8] Enable debug evaluation support for default methods
 *******************************************************************************/
package org.eclipse.jdt.debug.testplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * Helper methods to set up a IJavaProject.
 */
public class JavaProjectHelper {

	public static final String SRC_DIR = "src";
	public static final String BIN_DIR = "bin";
	// public static final String J2SE_1_4_EE_NAME = "J2SE-1.4";
	// public static final String J2SE_1_5_EE_NAME = "J2SE-1.5";
	public static final String JAVA_SE_1_6_EE_NAME = "JavaSE-1.6";
	public static final String JAVA_SE_1_7_EE_NAME = "JavaSE-1.7";
	public static final String JAVA_SE_1_8_EE_NAME = "JavaSE-1.8";
	public static final String JAVA_SE_9_EE_NAME = "JavaSE-9";

	/**
	 * path to the test src for 'testprograms'
	 */
	public static final IPath TEST_SRC_DIR= new Path("testprograms");

	/**
	 * path to the 1.5 test source
	 */
	public static final IPath TEST_1_5_SRC_DIR= new Path("testsource-j2se-1.5");
	/**
	 * path to the 1.7 test source
	 */
	public static final IPath TEST_1_7_SRC_DIR= new Path("java7");
	/**
	 * path to the 1.8 test source
	 */
	public static final IPath TEST_1_8_SRC_DIR= new Path("java8");
	/**
	 * path to the 9 test source
	 */
	public static final IPath TEST_9_SRC_DIR = new Path("java9");

	/**
	 * path to the compiler error java file
	 */
	public static final IPath TEST_COMPILE_ERROR = new Path("testresources/CompilationError.java");

	public static final String JRE_CONTAINER_NAME = "org.eclipse.jdt.launching.JRE_CONTAINER";


	/**
	 * Returns if the currently running VM is version compatible with Java 8
	 *
	 * @return <code>true</code> if a Java 8 (or greater) VM is running <code>false</code> otherwise
	 */
	public static boolean isJava8Compatible() {
		return isCompatible(8);
	}

	/**
	 * Returns if the currently running VM is version compatible with Java 9
	 *
	 * @return <code>true</code> if a Java 9 (or greater) VM is running <code>false</code> otherwise
	 */
	public static boolean isJava9Compatible() {
		return isCompatible(9);
	}
	/**
	 * Returns if the currently running VM is version compatible with Java 7
	 *
	 * @return <code>true</code> if a Java 7 (or greater) VM is running <code>false</code> otherwise
	 */
	public static boolean isJava7Compatible() {
		return isCompatible(7);
	}

	/**
	 * Returns if the currently running VM is version compatible with Java 6
	 *
	 * @return <code>true</code> if a Java 6 (or greater) VM is running <code>false</code> otherwise
	 */
	public static boolean isJava6Compatible() {
		return isCompatible(6);
	}

	/**
	 * Returns if the currently running VM is version compatible with Java 5
	 *
	 * @return <code>true</code> if a Java 5 (or greater) VM is running <code>false</code> otherwise
	 */
	public static boolean isJava5Compatible() {
		return isCompatible(5);
	}

	/**
	 * Returns if the current running system is compatible with the given Java minor version
	 *
	 * @param ver the version to test - either 4, 5, 6, 7 or 8
	 * @return <code>true</code> if compatible <code>false</code> otherwise
	 */
	static boolean isCompatible(int ver) {
		String version = System.getProperty("java.specification.version");
		if (version != null) {
			String[] nums = version.split("\\.");
			if (nums.length == 2) {
				try {
					int major = Integer.parseInt(nums[0]);
					int minor = Integer.parseInt(nums[1]);
					if (major >= 1) {
						if (minor >= ver) {
							return true;
						}
					}
				} catch (NumberFormatException e) {
				}
			} else if (nums.length == 1) {
				try {
					int major = Integer.parseInt(nums[0]);
					if (major >= ver) {
						return true;
					}
				}
				catch (NumberFormatException e) {
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new {@link IProject} with the given name unless the project already exists. If it already exists
	 * the project is refreshed and opened (if closed)
	 *
	 * @param pname the desired name for the project
	 * @return the new {@link IProject} handle
	 * @throws CoreException
	 */
	public static IProject createProject(String pname) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(pname);
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		return project;
	}

	/**
	 * creates a java project with the specified name and output folder
	 * @param projectName
	 * @param binFolderName
	 * @return a new java project
	 * @throws CoreException
	 */
	public static IJavaProject createJavaProject(String projectName, String binFolderName) throws CoreException {
		IProject project = createProject(projectName);
		IPath outputLocation;
		if (binFolderName != null && binFolderName.length() > 0) {
			IFolder binFolder= project.getFolder(binFolderName);
			if (!binFolder.exists()) {
				binFolder.create(false, true, null);
			}
			outputLocation= binFolder.getFullPath();
		} else {
			outputLocation= project.getFullPath();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			addNatureToProject(project, JavaCore.NATURE_ID, null);
		}
		IJavaProject jproject= JavaCore.create(project);
		jproject.setOutputLocation(outputLocation, null);
		jproject.setRawClasspath(new IClasspathEntry[0], null);
		return jproject;
	}

	/**
	 * Creates a new Java project with the specified name
	 * @param projectName
	 * @return a new java project with the specified name
	 * @throws CoreException
	 */
	public static IJavaProject createJavaProject(String projectName) throws CoreException {
		IProject project = createProject(projectName);
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			addNatureToProject(project, JavaCore.NATURE_ID, null);
		}
		IJavaProject jproject= JavaCore.create(project);
		jproject.setRawClasspath(new IClasspathEntry[0], null);
		return jproject;
	}

	/**
	 * deletes a java project
	 * @param jproject
	 * @throws CoreException
	 */
	public static void delete(IJavaProject jproject) throws CoreException {
		jproject.setRawClasspath(new ClasspathEntry[0], jproject.getProject().getFullPath(), null);
		jproject.getProject().delete(true, true, null);
	}

	/**
	 * Adds a new source container specified by the container name to the source path of the specified project
	 * @param jproject
	 * @param containerName
	 * @return the package fragment root of the container name
	 * @throws CoreException
	 */
	public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName) throws CoreException {
		IProject project= jproject.getProject();
		IContainer container= null;
		if (containerName == null || containerName.length() == 0) {
			container= project;
		} else {
			IFolder folder= project.getFolder(containerName);
			if (!folder.exists()) {
				folder.create(false, true, null);
			}
			container= folder;
		}
		IPackageFragmentRoot root= jproject.getPackageFragmentRoot(container);

		IClasspathEntry cpe= JavaCore.newSourceEntry(root.getPath());
		addToClasspath(jproject, cpe);
		return root;
	}

	/**
	 * Adds a source container to a IJavaProject.
	 * @param jproject
	 * @param containerName
	 * @param outputName
	 * @return the package fragment root of the new source container
	 * @throws CoreException
	 */
	public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, String outputName) throws CoreException {
		IProject project= jproject.getProject();
		IContainer container= null;
		if (containerName == null || containerName.length() == 0) {
			container= project;
		} else {
			IFolder folder= project.getFolder(containerName);
			if (!folder.exists()) {
				folder.create(false, true, null);
			}
			container= folder;
		}
		IPackageFragmentRoot root= jproject.getPackageFragmentRoot(container);

		IFolder output = null;
		if (outputName!= null) {
			output = project.getFolder(outputName);
			if (!output.exists()) {
				output.create(false, true, null);
			}
		}

		IClasspathEntry cpe= JavaCore.newSourceEntry(root.getPath(), new IPath[0], output.getFullPath());

		addToClasspath(jproject, cpe);
		return root;
	}

	/**
	 * Adds a source container to a IJavaProject and imports all files contained
	 * in the given Zip file.
	 * @param jproject
	 * @param containerName
	 * @param zipFile
	 * @return the package fragment root of the new source container
	 * @throws InvocationTargetException
	 * @throws CoreException
	 */
	public static IPackageFragmentRoot addSourceContainerWithImport(IJavaProject jproject, String containerName, ZipFile zipFile) throws InvocationTargetException, CoreException {
		IPackageFragmentRoot root= addSourceContainer(jproject, containerName);
		importFilesFromZip(zipFile, root.getPath(), null);
		return root;
	}

	/**
	 * Removes a source folder from a IJavaProject.
	 * @param jproject
	 * @param containerName
	 * @throws CoreException
	 */
	public static void removeSourceContainer(IJavaProject jproject, String containerName) throws CoreException {
		IFolder folder= jproject.getProject().getFolder(containerName);
		removeFromClasspath(jproject, folder.getFullPath());
		folder.delete(true, null);
	}

	/**
	 * Adds a library entry to a IJavaProject.
	 * @param jproject
	 * @param path
	 * @return the package fragment root of the new library
	 * @throws JavaModelException
	 */
	public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path) throws JavaModelException {
		return addLibrary(jproject, path, null, null);
	}

	/**
	 * Adds a library entry with source attchment to a IJavaProject.
	 * @param jproject
	 * @param path
	 * @param sourceAttachPath
	 * @param sourceAttachRoot
	 * @return the package fragment root of the new library
	 * @throws JavaModelException
	 */
	public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot) throws JavaModelException {
		IClasspathEntry cpe= JavaCore.newLibraryEntry(path, sourceAttachPath, sourceAttachRoot);
		addToClasspath(jproject, cpe);
		return jproject.getPackageFragmentRoot(path.toString());
	}

	/**
	 * Copies the library into the project and adds it as library entry.
	 * @param jproject
	 * @param jarPath
	 * @param sourceAttachPath
	 * @param sourceAttachRoot
	 * @return the package fragment root of the new library
	 * @throws IOException
	 * @throws CoreException
	 */
	public static IPackageFragmentRoot addLibraryWithImport(IJavaProject jproject, IPath jarPath, IPath sourceAttachPath, IPath sourceAttachRoot) throws IOException, CoreException {
		IProject project= jproject.getProject();
		IFile newFile= project.getFile(jarPath.lastSegment());
		try (InputStream inputStream = new FileInputStream(jarPath.toFile())) {
			newFile.create(inputStream, true, null);
		}
		return addLibrary(jproject, newFile.getFullPath(), sourceAttachPath, sourceAttachRoot);
	}


	/**
	 * Adds a variable entry with source attachment to a IJavaProject.
	 * Can return null if variable can not be resolved.
	 * @param jproject
	 * @param path
	 * @param sourceAttachPath
	 * @param sourceAttachRoot
	 * @return the package fragment root of the new variable entry
	 * @throws JavaModelException
	 */
	public static IPackageFragmentRoot addVariableEntry(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot) throws JavaModelException {
		IClasspathEntry cpe= JavaCore.newVariableEntry(path, sourceAttachPath, sourceAttachRoot);
		addToClasspath(jproject, cpe);
		IPath resolvedPath= JavaCore.getResolvedVariablePath(path);
		if (resolvedPath != null) {
			return jproject.getPackageFragmentRoot(resolvedPath.toString());
		}
		return null;
	}

	/**
	 * Adds a container entry to the specified java project
	 * @param project
	 * @param container
	 * @throws JavaModelException
	 */
	public static void addContainerEntry(IJavaProject project, IPath container) throws JavaModelException {
		IClasspathEntry cpe = JavaCore.newContainerEntry(container, false);
		addToClasspath(project, cpe);
	}

	/**
	 * Sets the given compiler compliance on the given {@link IJavaProject}
	 * <br><br>
	 * See {@link JavaCore#VERSION_1_4}, {@link JavaCore#VERSION_1_5}, {@link JavaCore#VERSION_1_6},
	 * {@link JavaCore#VERSION_1_7} and {@link JavaCore#VERSION_1_8} for more information on accepted compliances
	 *
	 * @param project
	 * @param compliance
	 */
	public static void setCompliance(IJavaProject project, String compliance) {
		Map<String, String> map = JavaCore.getOptions();
		map.put(JavaCore.COMPILER_COMPLIANCE, compliance);
		map.put(JavaCore.COMPILER_SOURCE, compliance);
		map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compliance);
		project.setOptions(map);
	}

	/**
	 * Updates the compiler compliance project setting for the given project to match the given EE.
	 * I.e. J2SE-1.5 will set a 1.5 compliance for the compiler and the source level.
	 * @param project
	 * @param ee
	 */
	public static void updateCompliance(IJavaProject project, String ee) {
		/*
		 * if(J2SE_1_4_EE_NAME.equals(ee)) { setCompliance(project, JavaCore.VERSION_1_4); } else if(J2SE_1_5_EE_NAME.equals(ee)) {
		 * setCompliance(project, JavaCore.VERSION_1_5); } else
		 */if (JAVA_SE_1_7_EE_NAME.equals(ee)) {
			setCompliance(project, JavaCore.VERSION_1_7);
		}
		else if(JAVA_SE_1_8_EE_NAME.equals(ee)) {
			setCompliance(project, JavaCore.VERSION_1_8);
		}
	}

	/**
	 * Adds a required project entry.
	 * @param jproject
	 * @param required
	 * @throws JavaModelException
	 */
	public static void addRequiredProject(IJavaProject jproject, IJavaProject required) throws JavaModelException {
		IClasspathEntry cpe= JavaCore.newProjectEntry(required.getProject().getFullPath());
		addToClasspath(jproject, cpe);
	}

	/**
	 * Removes a specified path form the specified java project
	 * @param jproject
	 * @param path
	 * @throws JavaModelException
	 */
	public static void removeFromClasspath(IJavaProject jproject, IPath path) throws JavaModelException {
		IClasspathEntry[] oldEntries= jproject.getRawClasspath();
		int nEntries= oldEntries.length;
		ArrayList<IClasspathEntry> list= new ArrayList<>(nEntries);
		for (int i= 0 ; i < nEntries ; i++) {
			IClasspathEntry curr= oldEntries[i];
			if (!path.equals(curr.getPath())) {
				list.add(curr);
			}
		}
		IClasspathEntry[] newEntries= list.toArray(new IClasspathEntry[list.size()]);
		jproject.setRawClasspath(newEntries, null);
	}

	/**
	 * Adds the specified classpath entry to the specified java project
	 * @param jproject
	 * @param cpe
	 * @throws JavaModelException
	 */
	public static void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException {
		IClasspathEntry[] oldEntries= jproject.getRawClasspath();
		ArrayList<IClasspathEntry> entries = new ArrayList<>(oldEntries.length);
		for (int i= 0; i < oldEntries.length; i++) {
			if (oldEntries[i].equals(cpe)) {
				return;
			}
			IPath oldpath = oldEntries[i].getPath();
			if(JRE_CONTAINER_NAME.equals(oldpath.segment(0)) && JRE_CONTAINER_NAME.equals(cpe.getPath().segment(0))) {
				continue;
			}
			entries.add(oldEntries[i]);
		}
		entries.add(cpe);
		jproject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}


	/**
	 * Adds the specified nature to the specified project
	 * @param proj
	 * @param natureId
	 * @param monitor
	 * @throws CoreException
	 */
	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}

	/**
	 * Imports files from the specified zip to the specified destination
	 * @param srcZipFile
	 * @param destPath
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	private static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {
		ZipFileStructureProvider structureProvider=	new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new ImportOverwriteQuery());
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}

	/**
	 * Imports files from the specified root dir into the specified path
	 * @param rootDir
	 * @param destPath
	 * @param monitor
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	public static void importFilesFromDirectory(File rootDir, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException, IOException {
		IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
		List<File> files = new ArrayList<>(100);
		addJavaFiles(rootDir, files);
		try {
			ImportOperation op= new ImportOperation(destPath, rootDir, structureProvider, new ImportOverwriteQuery(), files);
			op.setCreateContainerStructure(false);
			op.run(monitor);
			IStatus status = op.getStatus();
			if (!status.isOK()) {
				CoreException e = new CoreException(status);
				throw new InvocationTargetException(e, "Import operation encountered problems");
			}
		} catch (InterruptedException e) {
			throw new InvocationTargetException(e, "Interrupted during files import");
		}
	}

	/**
	 * Imports the specified file into the specified path
	 * @param file
	 * @param destPath
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public static void importFile(File file, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {
		IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;
		List<File> files = new ArrayList<>(1);
		files.add(file);
		try {
			ImportOperation op= new ImportOperation(destPath, file.getParentFile(), structureProvider, new ImportOverwriteQuery(), files);
			op.setCreateContainerStructure(false);
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}

	/**
	 * Recursively adds files from the specified dir to the provided list
	 * @param dir
	 * @param collection
	 * @throws IOException
	 */
	private static void addJavaFiles(File dir, List<File> collection) throws IOException {
		File[] files = dir.listFiles();
		if(files != null) {
			List<File> subDirs = new ArrayList<>(2);
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					collection.add(files[i]);
				} else if (files[i].isDirectory() && files[i].getName().indexOf("CVS") < 0) {
					subDirs.add(files[i]);
				}
			}
			Iterator<File> iter = subDirs.iterator();
			while (iter.hasNext()) {
				File subDir = iter.next();
				addJavaFiles(subDir, collection);
			}
		}
	}

	/**
	 * Static class for an <code>IOverwriteQuery</code> implementation
	 */
	private static class ImportOverwriteQuery implements IOverwriteQuery {
		/**
		 * @see org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite(java.lang.String)
		 */
		@Override
		public String queryOverwrite(String file) {
			return ALL;
		}
	}
}
