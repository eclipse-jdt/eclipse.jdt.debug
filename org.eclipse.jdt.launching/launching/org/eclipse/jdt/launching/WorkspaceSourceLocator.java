/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */ 
package org.eclipse.jdt.launching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;

/**
 * Standard source code locator for Java elements.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 *
 * @deprecated	Use ProjectSourceLocator.
 *
 * @see org.eclipse.debug.core.model.ISourceLocator
 */
public class WorkspaceSourceLocator implements ISourceLocator {

	private IWorkspace fWorkspace;
	
	/**
	 * Creates a new source locator for the given workspace.
	 *
	 * @param the workspace
	 */
	public WorkspaceSourceLocator(IWorkspace ws) {
		fWorkspace= ws;
	}
	
	private List locateElements(String fileName) {
		IProject[] projects= fWorkspace.getRoot().getProjects();
		List list = new ArrayList(1);
		for (int i= 0; i < projects.length; i++) {
			List elements= locateElements(projects[i], fileName);
			Iterator iter = elements.iterator();
			while (iter.hasNext()) {
				IPackageFragment fragment = (IPackageFragment)iter.next();
				if (!list.contains(fragment))
					list.add(fragment);
			}
		}
		return list;
	}

	private List locateElements(IProject p, String fileName) {
		IJavaProject project= JavaCore.create(p);
		return findPackageFragments(project, fileName);
	}

	private List findPackageFragments(IJavaProject project, String pkgName) {
		List list = new ArrayList();
		try {
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				IPackageFragment fragment = roots[i].getPackageFragment(pkgName);
				if (fragment.exists()) {
					list.add(fragment);
				}
			}
		} catch (JavaModelException e) {
			LaunchingPlugin.log(e);
		}
		return list;
	}

	private IType locateType(String name) {
		int lastDot= name.lastIndexOf('.');
		String pkgName= ""; //$NON-NLS-1$
		String typeName= name;
		if (lastDot >= 0) {
			pkgName= name.substring(0, lastDot);
			typeName= name.substring(lastDot + 1);
		}
		List pkgs= findPackages(pkgName);
		Iterator iter = pkgs.iterator();
		while (iter.hasNext()) {
			IType type = findInPackage((IPackageFragment)iter.next(), typeName);
			if (type != null) {
				return type;
			}
		}
		return null;

	}

	private List findPackages(String name) {
		String pathString= name.replace('.', '/');
		return locateElements(pathString);
	}

	private IType findInPackage(IPackageFragment pkg, String name) {
		IClassFile cf= pkg.getClassFile(name + ".class"); //$NON-NLS-1$
		try {
			if (cf.exists())
				return cf.getType();
		} catch (JavaModelException e) {
			LaunchingPlugin.log(e);
		}
		ICompilationUnit cu= pkg.getCompilationUnit(name + ".java"); //$NON-NLS-1$
		if (cu.exists())
			return cu.getType(name);
		try {
			ICompilationUnit[] cus= pkg.getCompilationUnits();
			for (int i= 0; i < cus.length; i++) {
				IType[] types= cus[i].getAllTypes();
				for (int j= 0; j < types.length; j++) {
					if (name.equals(types[j].getTypeQualifiedName()))
						return types[j];
				}
			}
		} catch (JavaModelException e) {
			LaunchingPlugin.log(e);
		}
		return null;
	}

	/**
	 * The <code>WorkspaceSourceLocator</code> implementation of this
	 * <code>ISourceLocator</code> method returns the first <code>IType</code>
	 * that can be found using the stack frame's declaring type name.
	 * 
	 * see org.eclipse.debug.core.model.ISourceLocator.
	 */
	public Object getSourceElement(IStackFrame stackFrame) {

		IJavaStackFrame frame= (IJavaStackFrame)stackFrame.getAdapter(IJavaStackFrame.class);
		try {
			if (frame != null) {
				String name = trimInnerClassName(frame.getDeclaringTypeName());
				if (name != null) {
					return locateType(name);	
				}
			}
		} catch(DebugException e) {
			LaunchingPlugin.log(e);
		} 
		return null;
	}
	
	/**
	 * Trim off any inner class name from the given type name.
	 */
	private String trimInnerClassName(String typeName) {
		int lastDollar= typeName.lastIndexOf("$"); //$NON-NLS-1$
		if (lastDollar > -1) {
			return typeName.substring(0, lastDollar);
		} else {
			return typeName;
		}		
	}	 
}
