package org.eclipse.jdt.internal.debug.ui.display;

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;

/**
 * An implementation of a data display for a text viewer
 */
public class DataDisplay implements IDataDisplay {
	
	/**
	 * The text viewer this data display works on
	 */
	private ITextViewer fTextViewer;
	
	/**
	 * Constructs a data display for the given text viewer.
	 * 
	 * @param viewer text viewer
	 */
	public DataDisplay(ITextViewer viewer) {
		setTextViewer(viewer);
	}

	/**
	 * @see IDataDisplay#clear()
	 */
	public void clear() {
		IDocument document= getTextViewer().getDocument();
		if (document != null) {
			document.set(""); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see IDataDisplay#displayExpression(String)
	 */
	public void displayExpression(String expression) {
		ITextSelection selection= (ITextSelection)getTextViewer().getSelectionProvider().getSelection();
		int offset= selection.getOffset();
		expression= expression.trim();
		StringBuffer buffer= new StringBuffer(expression);
		buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
		buffer.append('\t');
		expression= buffer.toString();
		try {
			getTextViewer().getDocument().replace(offset, selection.getLength(), expression);	
			getTextViewer().setSelectedRange(offset + expression.length(), 0);	
			getTextViewer().revealRange(offset, expression.length());
		} catch (BadLocationException ble) {
			JDIDebugUIPlugin.logError(ble);
		}
	}		
	
	/**
	 * @see IDataDisplay#displayExpressionValue(String)
	 */
	public void displayExpressionValue(String value) {
		value= value + System.getProperty("line.separator"); //$NON-NLS-1$
		ITextSelection selection= (ITextSelection)getTextViewer().getSelectionProvider().getSelection();

		int offset= selection.getOffset();
		int length= value.length();
		int replace= selection.getLength() - offset;
		if (replace < 0) {
			replace= 0;
		}
		try {
			getTextViewer().getDocument().replace(offset, replace, value);	
		} catch (BadLocationException ble) {
			JDIDebugUIPlugin.logError(ble);
		}
		getTextViewer().setSelectedRange(offset + length, 0);	
		getTextViewer().revealRange(offset, length);
	}

	/**
	 * Sets the text viewer for this data display
	 * 
	 * @param viewer text viewer
	 */
	private void setTextViewer(ITextViewer viewer) {
		fTextViewer = viewer;
	}
	
	/**
	 * Returns the text viewer for this data display
	 * 
	 * @return text viewer
	 */
	protected ITextViewer getTextViewer() {
		return fTextViewer;
	}	
}
