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


import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.ibm.icu.text.Bidi;

/**
 * A source viewer configured to display Java source. This
 * viewer obeys the font and color preferences specified in
 * the Java UI plugin.
 */
public class JDISourceViewer extends SourceViewer implements IPropertyChangeListener {
	
	/**
	 * BIDI delimtiers.
	 * 
	 * @since 3.4
	 */
	private static final String BIDI_DELIMITERS= "[ \\p{Punct}&&[^_]]"; //$NON-NLS-1$

	
	private Font fFont;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private IPreferenceStore fStore;
	private DisplayViewerConfiguration fConfiguration;


	public JDISourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		this(parent, ruler, null, false, styles);
	}


	public JDISourceViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles) {
		super(parent, ruler, overviewRuler, isOverviewRulerVisible, styles);
		StyledText text= this.getTextWidget();
		final int baseLevel= (styles & SWT.RIGHT_TO_LEFT) != 0 ? Bidi.DIRECTION_RIGHT_TO_LEFT : Bidi.DIRECTION_LEFT_TO_RIGHT;
		text.addBidiSegmentListener(new  BidiSegmentListener() {
			public void lineGetSegments(BidiSegmentEvent event) {
				try {
					event.segments= getBidiLineSegments(getDocument(), baseLevel, widgetOffset2ModelOffset(event.lineOffset), event.lineText);
				} catch (BadLocationException x) {
					// ignore
				}
			}
		});
	}
	
	/**
	 * Updates the viewer's font to match the preferences.
	 */
	private void updateViewerFont() {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			FontData data= null;
			if (store.contains(JFaceResources.TEXT_FONT) && !store.isDefault(JFaceResources.TEXT_FONT)) {
				data= PreferenceConverter.getFontData(store, JFaceResources.TEXT_FONT);
			} else {
				data= PreferenceConverter.getDefaultFontData(store, JFaceResources.TEXT_FONT);
			}
			if (data != null) {
				Font font= new Font(getTextWidget().getDisplay(), data);
				applyFont(font);
				if (getFont() != null) {
					getFont().dispose();
				}
				setFont(font);
				return;
			}
		}
		// if all the preferences failed
		applyFont(JFaceResources.getTextFont());
	}
	
	/**
	 * Sets the current font.
	 * 
	 * @param font the new font
	 */
	private void setFont(Font font) {
		fFont= font;
	}
	
	/**
	 * Returns the current font.
	 * 
	 * @return the current font
	 */
	private Font getFont() {
		return fFont;
	}
	
	/**
	 * Sets the font for the given viewer sustaining selection and scroll position.
	 * 
	 * @param font the font
	 */
	private void applyFont(Font font) {
		IDocument doc= getDocument();
		if (doc != null && doc.getLength() > 0) {
			Point selection= getSelectedRange();
			int topIndex= getTopIndex();
			
			StyledText styledText= getTextWidget();
			styledText.setRedraw(false);
			
			styledText.setFont(font);
			setSelectedRange(selection.x , selection.y);
			setTopIndex(topIndex);
			
			styledText.setRedraw(true);
		} else {
			getTextWidget().setFont(font);
		}
	}
	
	/**
	 * Updates the given viewer's colors to match the preferences.
	 */
	public void updateViewerColors() {
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			StyledText styledText= getTextWidget();
			Color color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);
			if (getForegroundColor() != null) {
				getForegroundColor().dispose();
			}
			setForegroundColor(color);
			
			color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);
			if (getBackgroundColor() != null) {
				getBackgroundColor().dispose();
			}
			setBackgroundColor(color);
		}
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
		RGB rgb= null;
		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb= PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb= PreferenceConverter.getColor(store, key);
			}
			if (rgb != null) {
				return new Color(display, rgb);
			}
		}
		return null;
	}
	
	/**
	 * Returns the current background color.
	 * 
	 * @return the current background color
	 */
	protected Color getBackgroundColor() {
		return fBackgroundColor;
	}

	/**
	 * Sets the current background color.
	 * 
	 * @param backgroundColor the new background color
	 */
	protected void setBackgroundColor(Color backgroundColor) {
		fBackgroundColor = backgroundColor;
	}

	/**
	 * Returns the current foreground color.
	 * 
	 * @return the current foreground color
	 */
	protected Color getForegroundColor() {
		return fForegroundColor;
	}

	/**
	 * Sets the current foreground color.
	 * 
	 * @param foregroundColor the new foreground color
	 */
	protected void setForegroundColor(Color foregroundColor) {
		fForegroundColor = foregroundColor;
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		IContentAssistant assistant= getContentAssistant();
		if (assistant instanceof ContentAssistant) {
			JDIContentAssistPreference.changeConfiguration((ContentAssistant) assistant, event);
		}
		String property= event.getProperty();
		
		if (JFaceResources.TEXT_FONT.equals(property)) {
			updateViewerFont();
		}
		if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property) || AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
			AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property) ||	AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)) {
			updateViewerColors();
		}
		if (fConfiguration != null) {
			if (fConfiguration.affectsTextPresentation(event)) {
				fConfiguration.handlePropertyChangeEvent(event);
				invalidateTextPresentation();
			}
		}
	}
	
	/**
	 * Returns the current content assistant.
	 * 
	 * @return the current content assistant
	 */
	public IContentAssistant getContentAssistant() {
		return fContentAssistant;
	}

	/**
	 * Disposes the system resources currently in use by this viewer.
	 */
	public void dispose() {
		if (getFont() != null) {
			getFont().dispose();
			setFont(null);
		}
		if (getBackgroundColor() != null) {
			getBackgroundColor().dispose();
			setBackgroundColor(null);
		}
		if (getForegroundColor() != null) {
			getForegroundColor().dispose();
			setForegroundColor(null);
		}
		if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
	 */
	@Override
	public void configure(SourceViewerConfiguration configuration) {
		super.configure(configuration);
		if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}
		if (configuration instanceof DisplayViewerConfiguration) {
			fConfiguration = (DisplayViewerConfiguration) configuration;
			fStore = fConfiguration.getTextPreferenceStore();
			fStore.addPropertyChangeListener(this);
		}
		updateViewerFont();
		updateViewerColors();
	}

	/**
	 * Returns the preference store used to configure this source viewer or
	 * <code>null</code> if none;
	 */
	private IPreferenceStore getPreferenceStore() {
		return fStore;
	}
	
	/**
	 * Returns a segmentation of the line of the given document appropriate for Bidi rendering.
	 * 
	 * @param document the document
	 * @param baseLevel the base level of the line
	 * @param lineStart the offset of the line
	 * @param lineText Text of the line to retrieve Bidi segments for
	 * @return the line's Bidi segmentation
	 * @throws BadLocationException in case lineOffset is not valid in document
	 */
	protected static int[] getBidiLineSegments(IDocument document, int baseLevel, int lineStart, String lineText) throws BadLocationException {

		if (lineText == null || document == null)
			return null;

		int lineLength= lineText.length();
		if (lineLength <= 2)
			return null;

		// Have ICU compute embedding levels. Consume these levels to reduce
		// the Bidi impact, by creating selective segments (preceding
		// character runs with a level mismatching the base level).
		// XXX: Alternatively, we could apply TextLayout. Pros would be full
		// synchronization with the underlying StyledText's (i.e. native) Bidi
		// implementation. Cons are performance penalty because of
		// unavailability of such methods as isLeftToRight and getLevels.

		Bidi bidi= new Bidi(lineText, baseLevel);
		if (bidi.isLeftToRight())
			// Bail out if this is not Bidi text.
			return null;

		IRegion line= document.getLineInformationOfOffset(lineStart);
		ITypedRegion[] linePartitioning= TextUtilities.computePartitioning(document, IJavaPartitions.JAVA_PARTITIONING, lineStart, line.getLength(), false);
		if (linePartitioning == null || linePartitioning.length < 1)
			return null;

		int segmentIndex= 1;
		int[] segments= new int[lineLength + 1];
		byte[] levels= bidi.getLevels();
		int nPartitions= linePartitioning.length;
		for (int partitionIndex= 0; partitionIndex < nPartitions; partitionIndex++) {

			ITypedRegion partition= linePartitioning[partitionIndex];
			int lineOffset= partition.getOffset() - lineStart;
			//Assert.isTrue(lineOffset >= 0 && lineOffset < lineLength);

			if (lineOffset > 0 && isMismatchingLevel(levels[lineOffset], baseLevel) && isMismatchingLevel(levels[lineOffset - 1], baseLevel)) {
				// Indicate a Bidi segment at the partition start - provided
				// levels of both character at the current offset and its
				// preceding character mismatch the base paragraph level.
				// Partition end will be covered either by the start of the next
				// partition, a delimiter inside a next partition, or end of line.
				segments[segmentIndex++]= lineOffset;
			}
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				int partitionEnd= Math.min(lineLength, lineOffset + partition.getLength());
				while (++lineOffset < partitionEnd) {
					if (isMismatchingLevel(levels[lineOffset], baseLevel) && String.valueOf(lineText.charAt(lineOffset)).matches(BIDI_DELIMITERS)) {
						// For default content types, indicate a segment before
						// a delimiting character with a mismatching embedding
						// level.
						segments[segmentIndex++]= lineOffset;
					}
				}
			}
		}
		if (segmentIndex <= 1)
			return null;

		segments[0]= 0;
		if (segments[segmentIndex - 1] != lineLength)
			segments[segmentIndex++]= lineLength;

		if (segmentIndex == segments.length)
			return segments;

		int[] newSegments= new int[segmentIndex];
		System.arraycopy(segments, 0, newSegments, 0, segmentIndex);
		return newSegments;
	}

	/**
	 * Checks if the given embedding level is consistent with the base level.
	 * 
	 * @param level Character embedding level to check.
	 * @param baseLevel Base level (direction) of the text.
	 * @return <code>true</code> if the character level is odd and the base level is even OR the character level is even and the base level is odd, and return <code>false</code> otherwise.
	 * 
	 * @since 3.4
	 */
	private static boolean isMismatchingLevel(int level, int baseLevel) {
		return ((level ^ baseLevel) & 1) != 0;
	}

}
