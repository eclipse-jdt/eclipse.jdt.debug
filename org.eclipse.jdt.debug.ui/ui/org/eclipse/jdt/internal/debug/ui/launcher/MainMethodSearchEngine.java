/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

public class MainMethodSearchEngine{
	
	private static class MethodCollector implements IJavaSearchResultCollector {
			private List fResult;
			private int fStyle;
			private IProgressMonitor fProgressMonitor;

			public MethodCollector(int style, IProgressMonitor progressMonitor) {
				fResult = new ArrayList(200);
				fStyle= style;
				fProgressMonitor= progressMonitor;
			}
			
			public List getResult() {
				return fResult;
			}

			private boolean considerExternalJars() {
				return (fStyle & IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS) != 0;
			}
					
			private boolean considerBinaries() {
				return (fStyle & IJavaElementSearchConstants.CONSIDER_BINARIES) != 0;
			}		
			
			public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy) {
				if (enclosingElement instanceof IMethod) { // defensive code
					try {
						IMethod curr= (IMethod) enclosingElement;
						if (curr.isMainMethod()) {
							if (!considerExternalJars()) {
								IPackageFragmentRoot root= getPackageFragmentRoot(curr);
								if (root == null || root.isArchive()) {
									return;
								}
							}
							if (!considerBinaries() && curr.isBinary()) {
								return;
							}
							IType declaringType = curr.getDeclaringType();
							fResult.add(declaringType);
						}
					} catch (JavaModelException e) {
						JDIDebugUIPlugin.log(e.getStatus());
					}
				}
			}
							
			public IProgressMonitor getProgressMonitor() {
				return fProgressMonitor;
			}
			
			public void aboutToStart() {
			}
			
			public void done() {
			}
	}

	/**
	 * Searches for all main methods in the given scope.
	 * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
	 * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
	 * 
	 * @param pm progress monitor
	 * @param scope search scope
	 * @param style search style
	 * @param includeSubtypes whether to consider types that inherit a main method
	 */	
	public IType[] searchMainMethods(IProgressMonitor pm, IJavaSearchScope scope, int style, boolean includeSubtypes) throws JavaModelException {
		pm.beginTask(LauncherMessages.getString("MainMethodSearchEngine.1"), 100); //$NON-NLS-1$
		int searchTicks = 100;
		if (includeSubtypes) {
			searchTicks = 25;
		}
		IProgressMonitor searchMonitor = new SubProgressMonitor(pm, searchTicks);
		MethodCollector collector= new MethodCollector(style, searchMonitor);				
		new SearchEngine().search(ResourcesPlugin.getWorkspace(), "main(String[]) void", IJavaSearchConstants.METHOD,  //$NON-NLS-1$
			IJavaSearchConstants.DECLARATIONS, scope, collector); //$NON-NLS-1$
			
		List result = collector.getResult();
		if (includeSubtypes) {
			IProgressMonitor subtypesMonitor = new SubProgressMonitor(pm, 75);
			subtypesMonitor.beginTask(LauncherMessages.getString("MainMethodSearchEngine.2"), result.size()); //$NON-NLS-1$
			Set set = addSubtypes(result, subtypesMonitor);
			return (IType[]) set.toArray(new IType[set.size()]);
		} else {
			return (IType[]) result.toArray(new IType[result.size()]);
		}
		
	}

	private Set addSubtypes(List types, IProgressMonitor monitor) {
		Iterator iterator = types.iterator();
		Set result = new HashSet(types.size());
		while (iterator.hasNext()) {
			IType type = (IType) iterator.next();
			if (result.add(type)) {
				ITypeHierarchy hierarchy = null;
				try {
					hierarchy = type.newTypeHierarchy(monitor);
					IType[] subtypes = hierarchy.getAllSubtypes(type);
					for (int i = 0; i < subtypes.length; i++) {
						IType t = subtypes[i];
						result.add(t);
					}				
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			monitor.worked(1);
		}
		return result;
	}
	
	
	/**
	 * Returns the package fragment root of <code>IJavaElement</code>. If the given
	 * element is already a package fragment root, the element itself is returned.
	 */
	public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		return (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
	}	
	
	/**
	 * Searches for all main methods in the given scope.
	 * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
	 * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
	 * 
	 * @param includeSubtypes whether to consider types that inherit a main method
	 */
	public IType[] searchMainMethods(IRunnableContext context, final IJavaSearchScope scope, final int style, final boolean includeSubtypes) throws InvocationTargetException, InterruptedException  {
		int allFlags=  IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS | IJavaElementSearchConstants.CONSIDER_BINARIES;
		Assert.isTrue((style | allFlags) == allFlags);
		
		final IType[][] res= new IType[1][];
		
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				try {
					res[0]= searchMainMethods(pm, scope, style, includeSubtypes);
				} catch (JavaModelException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		context.run(true, true, runnable);
		
		return res[0];
	}
			
}
