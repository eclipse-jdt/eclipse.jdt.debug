package org.eclipse.jdt.internal.debug.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class JavaVariablesFilterPreferenceDialog extends Dialog 
												implements IPreferencePageContainer {

	/**
	 * Layout for the page container.
	 */
	private class PageLayout extends Layout {
		
		/**
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
		 */
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control [] children = composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
		
		/**
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
		 */
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x= fMinimumPageSize.x;
			int y= fMinimumPageSize.y;

			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				Point size= children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x= Math.max(x, size.x);
				y= Math.max(y, size.y);
			}
			if (wHint != SWT.DEFAULT) x = wHint;
			if (hHint != SWT.DEFAULT) y = hHint;
			return new Point(x, y);
		}
	}

	private StructuredViewer fViewer;

	private JavaVariablesFilterPreferencePage fPrefPage;
	
	private Button fOkButton;
	
	private Composite fTitleArea;
	private Label fTitleImage;
	private CLabel fMessageLabel;

	private String fMessage;
	private Color fNormalMsgAreaBackground;
	private Image fErrorMsgImage;
	
	/**
	 * The Composite in which a page is shown.
	 */
	private Composite fPageContainer;

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize(Point)
	 */
	private Point fMinimumPageSize = new Point(200,200);

	/**
	 * Must declare our own images as the JFaceResource images will not be created unless
	 * a property/preference dialog has been shown
	 */
	protected static final String PREF_DLG_TITLE_IMG = "variable_filter_dialog_title_image";//$NON-NLS-1$
	protected static final String PREF_DLG_IMG_TITLE_ERROR = "variable_filter_dialog_title_error_image";//$NON-NLS-1$
	
	static {
		ImageRegistry reg = JDIDebugUIPlugin.getDefault().getImageRegistry();
		reg.put(PREF_DLG_TITLE_IMG, ImageDescriptor.createFromFile(PreferenceDialog.class, "images/pref_dialog_title.gif"));//$NON-NLS-1$
		reg.put(PREF_DLG_IMG_TITLE_ERROR, ImageDescriptor.createFromFile(Dialog.class, "images/message_error.gif"));//$NON-NLS-1$
	}

	/**
	 * Constructor
	 */
	public JavaVariablesFilterPreferenceDialog(Shell shell, StructuredViewer viewer) {
		super(shell);
		setViewer(viewer);
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Font font= parent.getFont();
		Composite composite = (Composite)super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 1;

		// Build the title area and separator line
		Composite titleComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		titleComposite.setFont(font);
		createTitleArea(titleComposite);

		Label titleBarSeparator = new Label(titleComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		titleBarSeparator.setLayoutData(gd);

		// Build the Page container
		fPageContainer = createPageContainer(composite);
		fPageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fPageContainer.setFont(parent.getFont());

		fPrefPage= new JavaVariablesFilterPreferencePage(getViewer());
		fPrefPage.setContainer(this);
		fPrefPage.createControl(fPageContainer);

		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);

		// For some reason, this seems to be necessary to set the initial title 
		// area message
		updateMessage();
		
		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Creates the dialog's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created title area composite
	 */
	private Composite createTitleArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		fTitleArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 2;

		// Get the colors for the title area
		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		Color fg = JFaceColors.getBannerForeground(display);

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		fTitleArea.setLayout(layout);
		fTitleArea.setLayoutData(layoutData);
		fTitleArea.setBackground(bg);

		// Message label
		fMessageLabel = new CLabel(fTitleArea, SWT.LEFT);
		fMessageLabel.setBackground(bg);
		fMessageLabel.setForeground(fg);
		fMessageLabel.setText(" ");//$NON-NLS-1$
		fMessageLabel.setFont(JFaceResources.getBannerFont());

		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(JFaceResources.BANNER_FONT.equals(event.getProperty()) ||
					JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
					updateMessage();
				}
			}
		};

		fMessageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});

		JFaceResources.getFontRegistry().addListener(fontListener);

		GridData gd = new GridData(GridData.FILL_BOTH);
		fMessageLabel.setLayoutData(gd);

		// Title image
		fTitleImage = new Label(fTitleArea, SWT.LEFT);
		fTitleImage.setBackground(bg);
		fTitleImage.setImage(JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_TITLE_IMG));
		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		fTitleImage.setLayoutData(gd);
		
		return fTitleArea;
	}

	/**
	 * Creates the inner page container.
	 */
	private Composite createPageContainer(Composite parent) {
		Composite result = new Composite(parent, SWT.NULL);
		result.setLayout(new PageLayout());
		return result;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		if (fOkButton != null) {
			fOkButton.setEnabled(fPrefPage.isValid());
		}
	}

	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param errorMessage the errorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null) {
			if (fMessageLabel.getImage() != null) {
				// we were previously showing an error
				fMessageLabel.setBackground(fNormalMsgAreaBackground);
				fMessageLabel.setImage(null);
				fTitleImage.setImage(JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_TITLE_IMG));
				fTitleArea.layout(true);
			}

			// show the message
			setMessage(fMessage);

		} else {
			fMessageLabel.setText(errorMessage);
			if (fMessageLabel.getImage() == null) {
				// we were not previously showing an error

				// lazy initialize the error background color and image
				if (fErrorMsgImage == null) {
					fErrorMsgImage = JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_IMG_TITLE_ERROR);
				}

				// show the error
				fNormalMsgAreaBackground = fMessageLabel.getBackground();
				fMessageLabel.setBackground(JFaceColors.getErrorBackground(fMessageLabel.getDisplay()));
				fMessageLabel.setImage(fErrorMsgImage);
				fTitleImage.setImage(null);
				fTitleArea.layout(true);
			}
		}
	}
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String newMessage) {
		fMessage = newMessage;
		if (fMessage == null) {
			fMessage = "";//$NON-NLS-1$
		}
		if (fMessageLabel.getImage() == null) {
			// we are not showing an error
			fMessageLabel.setText(fMessage);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
		String pageMessage = fPrefPage.getMessage();
		String pageErrorMessage = fPrefPage.getErrorMessage();

		// Adjust the font
		if (pageMessage == null && pageErrorMessage == null)
			fMessageLabel.setFont(JFaceResources.getBannerFont());
		else
			fMessageLabel.setFont(JFaceResources.getDialogFont());

		// Set the message and error message
		if (pageMessage == null) {
			setMessage(fPrefPage.getTitle());
		} else {
			setMessage(pageMessage);
		}
		setErrorMessage(pageErrorMessage);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		setTitle(fPrefPage.getTitle());
	}

	/**
	 * Sets the title for this dialog.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		Shell shell= getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(title);
		}
	}
	
	/**
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (fPrefPage.performOk()) {
			super.okPressed();			
		}
	}
	
	private StructuredViewer getViewer() {
		return fViewer;
	}
	
	private void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}

}
