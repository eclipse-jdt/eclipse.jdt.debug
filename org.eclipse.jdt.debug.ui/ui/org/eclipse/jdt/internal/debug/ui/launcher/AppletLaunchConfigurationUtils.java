package org.eclipse.jdt.internal.debug.ui.launcher;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IFileEditorInput;

public class AppletLaunchConfigurationUtils {
	
	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	public static void abort(String message, Throwable exception, int code)
		throws CoreException {
		throw new CoreException(
			new Status(
				IStatus.ERROR,
				JDIDebugUIPlugin.getUniqueIdentifier(),
				code,
				message,
				exception));
	}

	/**
	 * Return the <code>IType</code> referenced by the specified name and contained in 
	 * the specified project or throw a <code>CoreException</code> whose message explains why 
	 * this couldn't be done.
	 */
	public static IType getMainType(String mainTypeName, IJavaProject javaProject) throws CoreException {
		if ((mainTypeName == null) || (mainTypeName.trim().length() < 1)) {
			abort(LauncherMessages.getString("appletlauncher.utils.error.main_type_not_specified"), null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		IType mainType = null;
		try {
			mainType = findType(javaProject, mainTypeName);
		} catch (JavaModelException jme) {
		}
		if (mainType == null) {
			abort(LauncherMessages.getString("appletlauncher.utils.error.main_type_does_not_exist"), null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
		}
		return mainType;
	}		
	

	/**
	 * Find the specified (fully-qualified) type name in the specified java project.
	 */
	public static IType findType(IJavaProject javaProject, String mainTypeName) throws JavaModelException {
		String pathStr= mainTypeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement javaElement= javaProject.findElement(new Path(pathStr));
		if (javaElement == null) {
			return null;
		} else if (javaElement instanceof IType) {
			return (IType)javaElement;
		} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String simpleName= Signature.getSimpleName(mainTypeName);
			return ((ICompilationUnit) javaElement).getType(simpleName);
		} else if (javaElement.getElementType() == IJavaElement.CLASS_FILE) {
			return ((IClassFile) javaElement).getType();
		}
		return null; 
	}

	/**
	 * 	 */
	public static Set collectAppletTypesInProject(IProgressMonitor monitor, IJavaProject project) {
		IType[] types;
		HashSet result = null;
		try {
			IType javaLangApplet = AppletLaunchConfigurationUtils.getMainType("java.applet.Applet", project); //$NON-NLS-1$
			ITypeHierarchy hierarchy = javaLangApplet.newTypeHierarchy(project, new SubProgressMonitor(monitor, 1));
			types = hierarchy.getAllSubtypes(javaLangApplet);
			int length = types.length;
			if (length != 0) {
				result = new HashSet(length);
				for (int i = 0; i < length; i++) {
					if (!types[i].isBinary()) {
						result.add(types[i]);
					}
				}
			}
		} catch(JavaModelException jme) {
		} catch(CoreException e) {
		}
		monitor.done();
		return result;
	}
	
	private static void collectTypes(Object element, IProgressMonitor monitor, Set result) throws JavaModelException/*, InvocationTargetException*/ {
		element= computeScope(element);
		while((element instanceof IJavaElement) && !(element instanceof ICompilationUnit) && (element instanceof ISourceReference)) {
			if(element instanceof IType) {
				if (isSubclassOfApplet(monitor, (IType)element)) {
					result.add(element);
					monitor.done();
					return;
				}
			}
			element= ((IJavaElement)element).getParent();
		}
		if (element instanceof ICompilationUnit) {
			ICompilationUnit cu= (ICompilationUnit)element;
			IType[] types= cu.getAllTypes();
			for (int i= 0; i < types.length; i++) {
				if (isSubclassOfApplet(monitor, types[i])) {
					result.add(types[i]);
				}
			}
			monitor.done();
			return;
		} else if (element instanceof IJavaElement) {
			List found= searchSubclassesOfApplet(monitor, (IJavaElement)element);
			result.addAll(found);
			monitor.done();
		}
	}

	private static List searchSubclassesOfApplet(IProgressMonitor pm, IJavaElement javaElement) throws JavaModelException {
		return new ArrayList(collectAppletTypesInProject(pm, javaElement.getJavaProject()));
	}
	
	private static boolean isSubclassOfApplet(IProgressMonitor pm, IType type) {
		return collectAppletTypesInProject(pm, type.getJavaProject()).contains(type);
	}
	
	private static Object computeScope(Object element) throws JavaModelException {
		if (element instanceof IFileEditorInput) {
			element= ((IFileEditorInput)element).getFile();
		}
		if (element instanceof IResource) {
			element= JavaCore.create((IResource)element);
		}
		return element;
	}

	public static IType[] findApplets(IRunnableContext context, final Object[] elements) throws InvocationTargetException, InterruptedException {
		final Set result= new HashSet();
	
		if (elements.length > 0) {
			IRunnableWithProgress runnable= new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) throws InterruptedException {
					int nElements= elements.length;
					pm.beginTask(LauncherMessages.getString("appletlauncher.search.task.inprogress"), nElements); //$NON-NLS-1$
					try {
						for (int i= 0; i < nElements; i++) {
							try {
								collectTypes(elements[i], new SubProgressMonitor(pm, 1), result);
							} catch (JavaModelException jme) {
								JDIDebugUIPlugin.log(jme.getStatus());
							}
							if (pm.isCanceled()) {
								throw new InterruptedException();
							}
						}
					} finally {
						pm.done();
					}
				}
			};
			context.run(true, true, runnable);			
		}
		return (IType[]) result.toArray(new IType[result.size()]) ;
	}
}

