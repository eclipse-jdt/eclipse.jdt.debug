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
package org.eclipse.jdt.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class MainMethodSearchEngine{
	
	private class MethodCollector extends SearchRequestor {
		private List<IType> fResult;

		public MethodCollector() {
			fResult = new ArrayList<IType>(200);
		}

		public List<IType> getResult() {
			return fResult;
		}	

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 */
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object enclosingElement = match.getElement();
			if (enclosingElement instanceof IMethod) { // defensive code
				try {
					IMethod curr= (IMethod) enclosingElement;
					if (curr.isMainMethod()) {
						IType declaringType = curr.getDeclaringType();
						fResult.add(declaringType);
					}
				} catch (JavaModelException e) {
					JDIDebugUIPlugin.log(e.getStatus());
				}
			}
		}
	}

	/**
	 * Searches for all main methods in the given scope.
	 * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
	 * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
	 * 
	 * @param pm progress monitor
	 * @param scope search scope
	 * @param includeSubtypes whether to consider types that inherit a main method
	 */	
	public IType[] searchMainMethods(IProgressMonitor pm, IJavaSearchScope scope, boolean includeSubtypes) {
		pm.beginTask(LauncherMessages.MainMethodSearchEngine_1, 100); 
		int searchTicks = 100;
		if (includeSubtypes) {
			searchTicks = 25;
		}
		
		SearchPattern pattern = SearchPattern.createPattern("main(String[]) void", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE); //$NON-NLS-1$
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		MethodCollector collector = new MethodCollector();
		IProgressMonitor searchMonitor = new SubProgressMonitor(pm, searchTicks);
		try {
			new SearchEngine().search(pattern, participants, scope, collector, searchMonitor);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}

		List<IType> result = collector.getResult();
		if (includeSubtypes) {
			IProgressMonitor subtypesMonitor = new SubProgressMonitor(pm, 75);
			subtypesMonitor.beginTask(LauncherMessages.MainMethodSearchEngine_2, result.size()); 
			Set<IType> set = addSubtypes(result, subtypesMonitor, scope);
			return set.toArray(new IType[set.size()]);
		}
		return result.toArray(new IType[result.size()]);
	}

	/**
	 * Adds subtypes and enclosed types to the listing of 'found' types 
	 * @param types the list of found types thus far
	 * @param monitor progress monitor
	 * @param scope the scope of elements
	 * @return as set of all types to consider
	 */
	private Set<IType> addSubtypes(List<IType> types, IProgressMonitor monitor, IJavaSearchScope scope) {
		Iterator<IType> iterator = types.iterator();
		Set<IType> result = new HashSet<IType>(types.size());
		IType type = null;
		ITypeHierarchy hierarchy = null;
		IType[] subtypes = null;
		while (iterator.hasNext()) {
			type = iterator.next();
			if (result.add(type)) {
				try {
					hierarchy = type.newTypeHierarchy(monitor);
					subtypes = hierarchy.getAllSubtypes(type);
					for (int i = 0; i < subtypes.length; i++) {
						if (scope.encloses(subtypes[i])) {
							result.add(subtypes[i]);
						}
					}				
				} 
				catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
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
	public IType[] searchMainMethods(IRunnableContext context, final IJavaSearchScope scope, final boolean includeSubtypes) throws InvocationTargetException, InterruptedException  {		
		final IType[][] res= new IType[1][];
		
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				res[0]= searchMainMethods(pm, scope, includeSubtypes);
			}
		};
		context.run(true, true, runnable);
		
		return res[0];
	}
			
}
