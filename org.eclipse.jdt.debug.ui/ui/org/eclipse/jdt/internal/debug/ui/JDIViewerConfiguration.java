package org.eclipse.jdt.internal.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.debug.ui.display.DisplayViewContentAssistant;
import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jdt.internal.ui.text.JavaPartitionScanner;
import org.eclipse.jdt.internal.ui.text.java.JavaAutoIndentStrategy;
import org.eclipse.jdt.internal.ui.text.java.JavaDoubleClickSelector;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.DefaultAutoIndentStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.RuleBasedDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

public abstract class JDIViewerConfiguration extends SourceViewerConfiguration {

	private JavaTextTools fJavaTextTools;

	public JDIViewerConfiguration(JavaTextTools tools) {
		fJavaTextTools = tools;
	}

	protected RuleBasedScanner getCodeScanner() {
		return fJavaTextTools.getCodeScanner();
	}

	protected RuleBasedScanner getJavaDocScanner() {
		return fJavaTextTools.getJavaDocScanner();
	}

	protected IColorManager getColorManager() {
		return fJavaTextTools.getColorManager();
	}

	/**
	 * @see SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourcePart) {
		IColorManager manager = getColorManager();
		PresentationReconciler reconciler = new PresentationReconciler();

		RuleBasedDamagerRepairer dr = new RuleBasedDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new RuleBasedDamagerRepairer(getJavaDocScanner());
		reconciler.setDamager(dr, JavaPartitionScanner.JAVA_DOC);
		reconciler.setRepairer(dr, JavaPartitionScanner.JAVA_DOC);

		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(
			new Token(
				new TextAttribute(
					manager.getColor(IJavaColorConstants.JAVA_MULTI_LINE_COMMENT))));
		dr = new RuleBasedDamagerRepairer(scanner);
		reconciler.setDamager(dr, JavaPartitionScanner.JAVA_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, JavaPartitionScanner.JAVA_MULTI_LINE_COMMENT);

		scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(
			new Token(
				new TextAttribute(
					manager.getColor(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT))));
		dr = new RuleBasedDamagerRepairer(scanner);
		reconciler.setDamager(dr, JavaPartitionScanner.JAVA_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, JavaPartitionScanner.JAVA_SINGLE_LINE_COMMENT);

		return reconciler;
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			JavaPartitionScanner.JAVA_DOC,
			JavaPartitionScanner.JAVA_MULTI_LINE_COMMENT };
	}

	/**
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new DisplayViewContentAssistant();
		assistant.setContentAssistProcessor(
			getContentAssistantProcessor(),
			IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant);

		assistant.setContextInformationPopupOrientation(assistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(
			getInformationControlCreator(sourceViewer));

		return assistant;
	}

	/**
	 * @see SourceViewerConfiguration#getAutoIndentStrategy(ISourceViewer, String)
	 */
	public IAutoIndentStrategy getAutoIndentStrategy(
		ISourceViewer sourcePart,
		String contentType) {
		return (
			IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)
				? new JavaAutoIndentStrategy()
				: new DefaultAutoIndentStrategy());
	}

	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourcePart,
		String contentType) {
		return new JavaDoubleClickSelector();
	}

	/**
	 * @see SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	public String getDefaultPrefix(ISourceViewer sourcePart, String contentType) {
		return (contentType == null ? "//" : null); //$NON-NLS-1$
	}

	/**
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(
		ISourceViewer sourcePart,
		String contentType) {
		return new String[] { "\t", "    " }; //$NON-NLS-2$ //$NON-NLS-1$
	}

	/**
	 * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, new HTMLTextPresenter());
			}
		};
	}
	
	/**
	 * Returns the content assist processor used by this configuration.
	 */
	public abstract IContentAssistProcessor getContentAssistantProcessor();
}