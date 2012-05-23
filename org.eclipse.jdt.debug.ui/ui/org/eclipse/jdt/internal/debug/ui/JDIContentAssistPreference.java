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
package org.eclipse.jdt.internal.debug.ui;


import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetCompletionProcessor;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


public class JDIContentAssistPreference {
	
	private final static String VISIBILITY= "org.eclipse.jdt.core.codeComplete.visibilityCheck"; //$NON-NLS-1$
	private final static String ENABLED= "enabled"; //$NON-NLS-1$
	private final static String DISABLED= "disabled"; //$NON-NLS-1$
	
	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	private static Color getColor(IPreferenceStore store, String key) {
		JavaTextTools textTools= JDIDebugUIPlugin.getDefault().getJavaTextTools();
		return getColor(store, key, textTools.getColorManager());
	}
	
	private static JavaDebugContentAssistProcessor getDisplayProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof JavaDebugContentAssistProcessor)
			return  (JavaDebugContentAssistProcessor) p;
		return null;
	}
	
	private static JavaSnippetCompletionProcessor getJavaSnippetProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof JavaSnippetCompletionProcessor)
			return  (JavaSnippetCompletionProcessor) p;
		return null;
	}
	
	private static void configureDisplayProcessor(ContentAssistant assistant, IPreferenceStore store) {
		JavaDebugContentAssistProcessor dcp= getDisplayProcessor(assistant);
		if (dcp == null) {
			return;
		}
		String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null) {
			dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		}
			
		boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
		restrictProposalsToVisibility(enabled);
		
		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY);
		restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SORTER);
		dcp.orderProposalsAlphabetically(enabled);
	}
	
	private static void configureJavaSnippetProcessor(ContentAssistant assistant, IPreferenceStore store) {
		JavaSnippetCompletionProcessor cp= getJavaSnippetProcessor(assistant);
		if (cp == null) {
			return;
		}
			
		String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null) {
			cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		}
			
		boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
		restrictProposalsToVisibility(enabled);
		
		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY);
		restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SORTER);
		cp.orderProposalsAlphabetically(enabled);
	}
	
	/**
	 * Configure the given content assistant from the preference store.
	 */
	public static void configure(ContentAssistant assistant, IColorManager manager) {
		
		IPreferenceStore store= getPreferenceStore();
		
		boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
		assistant.enableAutoActivation(enabled);
		
		int delay= store.getInt(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);
		
		Color c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c);
		assistant.setContextSelectorForeground(c);
		
		c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		assistant.setContextSelectorBackground(c);
		
		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOINSERT);
		assistant.enableAutoInsert(enabled);

		configureDisplayProcessor(assistant, store);
		configureJavaSnippetProcessor(assistant, store);
	}
	
	
	private static void changeDisplayProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		JavaDebugContentAssistProcessor dcp= getDisplayProcessor(assistant);
		if (dcp == null) {
			return;
		}
		if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
			String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
			if (triggers != null) {
				dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			}
		} else if (PreferenceConstants.CODEASSIST_SORTER.equals(key)) {
			boolean enable= store.getBoolean(PreferenceConstants.CODEASSIST_SORTER);
			dcp.orderProposalsAlphabetically(enable);
		}
	}
	
	private static void changeJavaSnippetProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		JavaSnippetCompletionProcessor cp= getJavaSnippetProcessor(assistant);
		if (cp == null) {
			return;
		}
		if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
			String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
			if (triggers != null) {
				cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			}
		} else if (PreferenceConstants.CODEASSIST_SORTER.equals(key)) {
			boolean enable= store.getBoolean(PreferenceConstants.CODEASSIST_SORTER);
			cp.orderProposalsAlphabetically(enable);
		}
	}
	
	
	/**
	 * Changes the configuration of the given content assistant according to the given property
	 * change event.
	 */
	public static void changeConfiguration(ContentAssistant assistant, PropertyChangeEvent event) {
		
		IPreferenceStore store= getPreferenceStore();
		String p= event.getProperty();
		
		if (PreferenceConstants.CODEASSIST_AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
			assistant.enableAutoActivation(enabled);
		} else if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND.equals(p)) {
			Color c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} else if (PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		} else if (PreferenceConstants.CODEASSIST_AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		}
		
		changeDisplayProcessor(assistant, store, p);
		changeJavaSnippetProcessor(assistant, store, p);
	}
	
	/**
	 * Tells this processor to restrict its proposal to those element
	 * visible in the actual invocation context.
	 * 
	 * @param restrict <code>true</code> if proposals should be restricted
	 */
	private static void restrictProposalsToVisibility(boolean restrict) {
		Hashtable<String, String> options= JavaCore.getOptions();
		Object value= options.get(VISIBILITY);
		if (value instanceof String) {
			String newValue= restrict ? ENABLED : DISABLED;
			if (!newValue.equals(value)) {
				options.put(VISIBILITY, newValue);
				JavaCore.setOptions(options);
			}
		}
	}
	
	/**
	 * Tells this processor to restrict is proposals to those
	 * starting with matching cases.
	 * 
	 * @param restrict <code>true</code> if proposals should be restricted
	 */
	private static void restrictProposalsToMatchingCases(boolean restrict) {
		// XXX not yet supported
	}
	
	private static IPreferenceStore getPreferenceStore() {
		return PreferenceConstants.getPreferenceStore();
	}
}
