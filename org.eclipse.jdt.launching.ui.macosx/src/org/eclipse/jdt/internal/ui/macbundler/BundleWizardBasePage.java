/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.macbundler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.wizard.*;


public abstract class BundleWizardBasePage extends DialogPage implements IWizardPage, BundleAttributes, IPropertyChangeListener {
	
	/**
	 * The page that was shown right before this page became visible;
	 * <code>null</code> if none.
	 */
	private IWizardPage fPreviousPage;
	/**
	 * This page's message key.
	 */
	private String fKey;
	/**
	 * The wizard to which this page belongs; <code>null</code>
	 * if this page has yet to be added to a wizard.
	 */
	private IWizard fWizard;
	
	BundleDescription fBundleDescription;

	
	BundleWizardBasePage(String key, BundleDescription bd) {
		super(Util.getString(key + ".title")); //$NON-NLS-1$
		fKey= key;
		fBundleDescription= bd;
		//setMessage(Util.getString(fKey + ".message")); //$NON-NLS-1$
		setDescription(Util.getString(fKey + ".description")); //$NON-NLS-1$
		
		bd.addListener(this);
	}
	
	/* (non-Javadoc)
	 * Method declared in WizardPage
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible)
			enterPage();
		else
			leavePage();
		super.setVisible(visible);
	}
	
	void enterPage() {
		//System.out.println("enterPage: " + getName());
	}

	void leavePage() {
		//System.out.println("leavePage: " + getName());
	}

    @Override
	public Image getImage() {
        Image result = super.getImage();

        if (result == null && fWizard != null)
            return fWizard.getDefaultPageImage();

        return result;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	final public void createControl(Composite parent) {
		
		Composite c= new Composite(parent, SWT.NULL);
		c.setLayout(new GridLayout(1, false));
		setControl(c);
		
		createContents(c);

		checkIfPageComplete();
	}
	
	abstract public void createContents(Composite parent);
	
	static void setHeightHint(Control control, int height) {
		GridData gd1= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd1.heightHint= height;
		control.setLayoutData(gd1);
	}

	static Label createLabel(Composite parent, String text, int align) {
		Label l= new Label(parent, SWT.NONE);
		l.setText(text);
		l.setLayoutData(new GridData(align));
		return l;
	}

	static Composite createComposite(Composite parent, int columns) {
		Composite c= new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl= new GridLayout(columns, false);
		gl.marginWidth= 0;
		c.setLayout(gl);
		return c;
	}

	Text createText(Composite parent, String key, int lines) {
		Text t= new Text(parent, SWT.BORDER);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		if (lines == 2)
			gd.heightHint= 30;
		t.setLayoutData(gd);	
		hookField(t, key);
		return t;
	}

	Combo createCombo(Composite parent, String key) {
		Combo c= new Combo(parent, SWT.BORDER);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hookField(c, key);
		return c;
	}

	static Group createGroup(Composite parent, String text, int columns) {
		Group g= new Group(parent, SWT.NONE);
		g.setText(text);
		g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		g.setLayout(new GridLayout(columns, false));
		return g;
	}

	Button createButton(Composite parent, int flags, String text) {
		Button b= new Button(parent, flags);
		if (text != null)
			b.setText(text);
		return b;
	}

	static Composite createHBox(Composite parent) {
		Composite c= new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl= new GridLayout(2, false);
		gl.marginWidth= gl.marginHeight= 0;
		c.setLayout(gl);
		return c;
	}
	
	void hookField(final Text tf, final String key) {
		tf.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fBundleDescription.setValue(key, tf.getText());
				checkIfPageComplete();
			}
		});
	}
		
	void hookField(final Combo tf, final String key) {
		tf.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fBundleDescription.setValue(key, tf.getText());
				checkIfPageComplete();
			}
		});
	}
	
	void hookButton(final Button b, final String key) {
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fBundleDescription.setValue(key, new Boolean(b.getSelection()));
				checkIfPageComplete();
			}
		});
	}
	
	final void checkIfPageComplete() {
		IWizardContainer c= (fWizard != null) ? fWizard.getContainer() : null;
		if (c != null && this == c.getCurrentPage())
			c.updateButtons();
	}

	/////////////////////////////////////////////////////////
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete() && getNextPage() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return Util.getString(fKey + ".title"); //$NON-NLS-1$;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 */
	public IWizardPage getNextPage() {
		if (fWizard == null)
			return null;
		return fWizard.getNextPage(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
	 */
	public IWizardPage getPreviousPage() {
		if (fPreviousPage != null)
			return fPreviousPage;
		if (fWizard != null)
			return fWizard.getPreviousPage(this);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getWizard()
	 */
	public IWizard getWizard() {
		return fWizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#setPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public void setPreviousPage(IWizardPage page) {
		fPreviousPage= page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
	 */
	public void setWizard(IWizard newWizard) {
		fWizard= newWizard;
	}
}
