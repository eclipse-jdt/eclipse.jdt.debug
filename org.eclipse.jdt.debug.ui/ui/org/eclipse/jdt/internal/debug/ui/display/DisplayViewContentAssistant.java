package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

/**
 * Java content assistant for the variable view details area.
 */
public class DisplayViewContentAssistant extends ContentAssistant {

	private DisplayViewAction fContentAssistAction = null;
	
	private VerifyKeyListener fListener = null;

	/*
	 * @see IContentAssist#install
	 */
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		
		fContentAssistAction= new DisplayViewAction(textViewer.getTextOperationTarget(), ISourceViewer.CONTENTASSIST_PROPOSALS);
		fContentAssistAction.setText(DisplayMessages.getString("DisplayView.Co&ntent_Assist@Ctrl+Space_1")); //$NON-NLS-1$
		fContentAssistAction.setDescription(DisplayMessages.getString("DisplayView.Content_Assist_2")); //$NON-NLS-1$
		fContentAssistAction.setToolTipText(DisplayMessages.getString("DisplayView.Content_Assist_2")); //$NON-NLS-1$
		
		addVerifyKeyListener(textViewer);
	}
	
	protected void addVerifyKeyListener(ITextViewer viewer) {
		fListener = new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				//do code assist for CTRL-SPACE
				if (event.stateMask == SWT.CTRL && event.keyCode == 0) {
					if (event.character == 0x20) {
						if(fContentAssistAction.isEnabled()) {
							fContentAssistAction.run();
							event.doit= false;
						}
					}
/*					
					//do display for CTRL-D
					if (event.character == 0x4) {
						if(fDisplayAction.isEnabled()) {
							fDisplayAction.run();
							event.doit= false;
						}
					}
					//do inspect for CTRL-Q
					if (event.character == 0x11) {
						if(fInspectAction.isEnabled()) {
							fInspectAction.run();
							event.doit= false;
						}
					}
*/					
				}
			}
		};
		
		viewer.getTextWidget().addVerifyKeyListener(fListener);
	}	
}
