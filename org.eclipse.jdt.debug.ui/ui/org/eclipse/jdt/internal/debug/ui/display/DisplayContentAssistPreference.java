package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


public class DisplayContentAssistPreference {
	
	private final static String VISIBILITY= "org.eclipse.jdt.core.codeComplete.visibilityCheck";
	private final static String ENABLED= "enabled";
	private final static String DISABLED= "disabled";
	
	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	private static Color getColor(IPreferenceStore store, String key) {
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		return getColor(store, key, textTools.getColorManager());
	}
	
	private static DisplayCompletionProcessor getDisplayProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof DisplayCompletionProcessor)
			return  (DisplayCompletionProcessor) p;
		return null;
	}
	
	private static void configureDisplayProcessor(ContentAssistant assistant, IPreferenceStore store) {
		DisplayCompletionProcessor dcp= getDisplayProcessor(assistant);
		if (dcp == null) {
			return;
		}
			
		String triggers= store.getString(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null) {
			dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		}
			
		boolean enabled= store.getBoolean(ContentAssistPreference.SHOW_VISIBLE_PROPOSALS);
		restrictProposalsToVisibility(enabled);
		
		enabled= store.getBoolean(ContentAssistPreference.CASE_SENSITIVITY);
		restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(ContentAssistPreference.ORDER_PROPOSALS);
		dcp.orderProposalsAlphabetically(enabled);
	}
	
	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant, IPreferenceStore store) {
		
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		IColorManager manager= textTools.getColorManager();		
		
		boolean enabled= store.getBoolean(ContentAssistPreference.AUTOACTIVATION);
		assistant.enableAutoActivation(enabled);
		
		int delay= store.getInt(ContentAssistPreference.AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);
		
		Color c= getColor(store, ContentAssistPreference.PROPOSALS_FOREGROUND, manager);
		assistant.setProposalSelectorForeground(c);
		
		c= getColor(store, ContentAssistPreference.PROPOSALS_BACKGROUND, manager);
		assistant.setProposalSelectorBackground(c);
		
		c= getColor(store, ContentAssistPreference.PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c);
		
		c= getColor(store, ContentAssistPreference.PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		
		enabled= store.getBoolean(ContentAssistPreference.AUTOINSERT);
		assistant.enableAutoInsert(enabled);

		configureDisplayProcessor(assistant, store);
	}
	
	
	private static void changeDisplayProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		DisplayCompletionProcessor dcp= getDisplayProcessor(assistant);
		if (dcp == null) {
			return;
		}
		if (ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
			String triggers= store.getString(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_JAVA);
			if (triggers != null) {
				dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			}
		} else if (ContentAssistPreference.ORDER_PROPOSALS.equals(key)) {
			boolean enable= store.getBoolean(ContentAssistPreference.ORDER_PROPOSALS);
			dcp.orderProposalsAlphabetically(enable);
		}
	}
	
	/**
	 * Changes the configuration of the given content assistant according to the given property
	 * change event and the given preference store.
	 */
	public static void changeConfiguration(ContentAssistant assistant, IPreferenceStore store, PropertyChangeEvent event) {
		
		String p= event.getProperty();
		
		if (ContentAssistPreference.AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(ContentAssistPreference.AUTOACTIVATION);
			assistant.enableAutoActivation(enabled);
		} else if (ContentAssistPreference.AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(ContentAssistPreference.AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (ContentAssistPreference.PROPOSALS_FOREGROUND.equals(p)) {
			Color c= getColor(store, ContentAssistPreference.PROPOSALS_FOREGROUND);
			assistant.setProposalSelectorForeground(c);
		} else if (ContentAssistPreference.PROPOSALS_BACKGROUND.equals(p)) {
			Color c= getColor(store, ContentAssistPreference.PROPOSALS_BACKGROUND);
			assistant.setProposalSelectorBackground(c);
		} else if (ContentAssistPreference.PARAMETERS_FOREGROUND.equals(p)) {
			Color c= getColor(store, ContentAssistPreference.PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
		} else if (ContentAssistPreference.PARAMETERS_BACKGROUND.equals(p)) {
			Color c= getColor(store, ContentAssistPreference.PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
		} else if (ContentAssistPreference.AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(ContentAssistPreference.AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		}
		
		changeDisplayProcessor(assistant, store, p);
	}
	
	/**
	 * Tells this processor to restrict its proposal to those element
	 * visible in the actual invocation context.
	 * 
	 * @param restrict <code>true</code> if proposals should be restricted
	 */
	private static void restrictProposalsToVisibility(boolean restrict) {
		Hashtable options= JavaCore.getOptions();
		Object value= options.get(VISIBILITY);
		if (value instanceof String) {
			String newValue= restrict ? ENABLED : DISABLED;
			if (!newValue.equals((String) value)) {
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
}
