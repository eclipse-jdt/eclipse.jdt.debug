/*******************************************************************************
 * Copyright (c) 2007, 2012 Ecliptical Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 *     IBM Canada - review initial contribution and commit
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.search;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.PatternQuerySpecification;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;

/**
 * This class provides a search participant to find class references in the 
 * {@linkplain IJavaLaunchConfigurationConstants#ATTR_MAIN_TYPE_NAME} attribute
 * of Java launch configurations
 * 
 * @since 3.4.0
 */
public class LaunchConfigurationQueryParticipant implements IQueryParticipant {

	/**
	 * Singleton instance of the UI participant for the configuration
	 * search matches
	 */
	private UIParticipant uiParticipant;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.search.IQueryParticipant#estimateTicks(org.eclipse.jdt.ui.search.QuerySpecification)
	 */
	public int estimateTicks(QuerySpecification query) {
		if (isValid(query)) {
			return 50;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.search.IQueryParticipant#getUIParticipant()
	 */
	public synchronized IMatchPresentation getUIParticipant() {
		if (uiParticipant == null) {
			uiParticipant = new UIParticipant();
		}
		return uiParticipant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.search.IQueryParticipant#search(org.eclipse.jdt.ui.search.ISearchRequestor, org.eclipse.jdt.ui.search.QuerySpecification, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void search(ISearchRequestor requestor, QuerySpecification query, IProgressMonitor monitor) throws CoreException {
		if (!isValid(query)) {
			return;
		}
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 11); //$NON-NLS-1$
		try {
			Pattern pattern = null;
			if (query instanceof ElementQuerySpecification) {
				ElementQuerySpecification elementQuery = (ElementQuerySpecification) query;
				IJavaElement element = elementQuery.getElement();
				if(element instanceof IMember) {
					IMember member = (IMember) element;
					IType type = null;
					if(member.getElementType() == IJavaElement.TYPE) {
						type = (IType) member;
					}
					else if(member.getElementType() == IJavaElement.METHOD) {
						if(((IMethod)member).isMainMethod()) {
							type = member.getDeclaringType();
						}
					}
					if(type != null) {
						pattern = Pattern.compile(quotePattern(type.getFullyQualifiedName('$')));
					}
					else {
						return;
					}
				}
				else {
					return;
				}
			}
			if (query instanceof PatternQuerySpecification) {
				PatternQuerySpecification patternQuery = (PatternQuerySpecification) query;
				int flags = patternQuery.isCaseSensitive() ? 0
						: Pattern.CASE_INSENSITIVE;
				String quotedPattern = quotePattern(patternQuery.getPattern());
				pattern = Pattern.compile(quotedPattern, flags);
			}
			if(monitor.isCanceled()) {
				return;
			}
			monitor.worked(1);
			searchLaunchConfigurations(query.getScope(), requestor, pattern, new SubProgressMonitor(monitor, 7));
		} finally {
			monitor.done();
		}
	}

	/**
	 * Creates an adjusted pattern from the specified quote pattern
	 * @param pattern the search pattern to manipulate
	 * @return the original pattern adjusted to escape special chars in Java,and change Eclipse
	 * search chars to Java RegEx chars
	 */
	private String quotePattern(String pattern) {
		StringTokenizer t = new StringTokenizer(pattern, ".?*$", true); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		String token = null;
		while (t.hasMoreTokens()) {
			token = t.nextToken();
			switch (token.charAt(0)) {
				case '.': {
					buf.append('\\');
					break;
				}
				case '?':
				case '*': {
					buf.append('.');
					break;
				}
				case '$': {
					buf.append('\\');
					break;
				}
			}
			buf.append(token);
		}
		return buf.toString();
	}

	/**
	 * Determines if the current query should be considered or not
	 * @param query the current query
	 * @return true if the query should be considered, false otherwise
	 */
	private boolean isValid(QuerySpecification query) {
		switch(query.getLimitTo()) {
			case IJavaSearchConstants.REFERENCES:
			case IJavaSearchConstants.ALL_OCCURRENCES: {
				break;
			}
			default: {
				return false;
			}
		}
		if (query instanceof ElementQuerySpecification) {
			IJavaElement element = ((ElementQuerySpecification) query).getElement();
			return element.getElementType() == IJavaElement.TYPE || element.getElementType() == IJavaElement.METHOD;
		}
		if (query instanceof PatternQuerySpecification) {
			PatternQuerySpecification patternQuery = (PatternQuerySpecification) query;
			switch (patternQuery.getSearchFor()) {
				case IJavaSearchConstants.UNKNOWN:
				case IJavaSearchConstants.TYPE:
				case IJavaSearchConstants.CLASS:
				case IJavaSearchConstants.CLASS_AND_INTERFACE:
				case IJavaSearchConstants.CLASS_AND_ENUM: {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the success of the the pattern matching 
	 * @param scope the search scope
	 * @param config the backing {@link ILaunchConfiguration}
	 * @param pattern the search pattern
	 * @return true if there were matching elements for the pattern, false otherwise
	 * @throws CoreException if an exception occurs
	 */
	private boolean matches(IJavaSearchScope scope, ILaunchConfiguration config, Pattern pattern) throws CoreException {
		if(!config.exists() || !config.getType().isPublic() || !DebugUIPlugin.doLaunchConfigurationFiltering(config)) {
			return false;
		}
		String mainTypeName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String) null);
		if (mainTypeName == null) {
			return false;
		}
		try {
			mainTypeName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(mainTypeName);
		}
		catch(CoreException ce) {
			return false;
		}
		if (! pattern.matcher(mainTypeName).matches()) {
			return false;
		}
		IResource[] resources = config.getMappedResources();
		if (resources != null) {
			for (int i = 0; i < resources.length; i++) {
				if (scope.encloses(resources[i].getFullPath().toString())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Searches for configurations matching the specified pattern
	 * @param scope the search scope
	 * @param requestor the requester
	 * @param pattern the search pattern
	 * @param monitor for progress monitoring
	 * @throws CoreException if an exception occurs
	 */
	private void searchLaunchConfigurations(IJavaSearchScope scope, ISearchRequestor requestor, Pattern pattern, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
		monitor.beginTask("Searching for launch configurations", configs.length); //$NON-NLS-1$
		try {
			for (int i = 0; i < configs.length; ++i) {
				if (monitor.isCanceled()) {
					return;
				}
				monitor.worked(1);
				if (matches(scope, configs[i], pattern)) {
					requestor.reportMatch(new Match(configs[i], 0, 0));
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * The UI participant for showing configuration matches in the search view
	 * 
	 * @since 3.4.0
	 */
	private static class UIParticipant implements IMatchPresentation {

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.ui.search.IMatchPresentation#createLabelProvider()
		 */
		public ILabelProvider createLabelProvider() {
			return DebugUITools.newDebugModelPresentation();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.ui.search.IMatchPresentation#showMatch(org.eclipse.search.ui.text.Match, int, int, boolean)
		 */
		public void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
			Object o = match.getElement();
			if (o instanceof ILaunchConfiguration) {
				if (activate) {
					try {
						ILaunchConfiguration config = (ILaunchConfiguration) o;
						Set<Set<String>> modes =  config.getType().getSupportedModeCombinations();
						ILaunchGroup group = null;
						Set<String> mode = null;
						for (Iterator<Set<String>> iter = modes.iterator(); iter.hasNext();) {
							mode = iter.next();
							if(mode.size() == 1) {
								group = DebugUITools.getLaunchGroup(config, mode.iterator().next());
								if (group != null) {
									break;
								}
							}
						}
						if (group == null || !config.exists()) {
							return;
						}
						DebugUITools.openLaunchConfigurationDialog(JDIDebugUIPlugin.getActiveWorkbenchShell(), config, group.getIdentifier(), Status.OK_STATUS);
					}
					catch(CoreException ce) {
						JDIDebugUIPlugin.log(ce);
					}
				}
			}
		}
	}
}
