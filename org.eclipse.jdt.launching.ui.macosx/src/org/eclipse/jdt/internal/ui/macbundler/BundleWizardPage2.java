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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.util.PropertyChangeEvent;


public class BundleWizardPage2 extends BundleWizardBasePage {
	
	Text fWorkingDir;
	Table fClassPath;
	Table fResources;
	

	protected BundleWizardPage2(BundleDescription bd) {
		super("page2", bd); //$NON-NLS-1$
	}

	@Override
	public void createContents(Composite parent) {
		
		Composite c= createComposite(parent, 2);
			createLabel(c, Util.getString("page2.workingDirectory.label"), GridData.VERTICAL_ALIGN_CENTER); //$NON-NLS-1$
			fWorkingDir= createText(c, WORKINGDIR, 1);

		fClassPath= createTableGroup(parent, Util.getString("page2.addToClasspath.group.label"), true); //$NON-NLS-1$
		fResources= createTableGroup(parent, Util.getString("page2.addToBundle.group.label"), false); //$NON-NLS-1$
	}
	
	Table createTableGroup(Composite parent, String groupName, final boolean onClasspath) {
		Group g1= createGroup(parent, groupName, 1);
			final Table table= new Table(g1, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
			setHeightHint(table, 80);
			Composite c1= createComposite(g1, 3);
			final Button addButton1= createButton(c1, SWT.NONE, Util.getString("page2.addFile.button.label")); //$NON-NLS-1$
			addButton1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fd= new FileDialog(addButton1.getShell(), SWT.OPEN);
					fd.setText(Util.getString("page2.chooseFileDialog.title")); //$NON-NLS-1$
					String path= fd.open();
					if (path != null) {
						ResourceInfo ri= new ResourceInfo(path);
						fBundleDescription.addResource(ri, onClasspath);
						add(table, ri);
					}
				}
			});
			final Button addButton2= createButton(c1, SWT.NONE, Util.getString("page2.addFolder.button.label")); //$NON-NLS-1$
			addButton2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog fd= new DirectoryDialog(addButton2.getShell(), SWT.OPEN);
					fd.setText(Util.getString("page2.chooseFolder.dialog.title")); //$NON-NLS-1$
					String path= fd.open();
					if (path != null) {
						ResourceInfo ri= new ResourceInfo(path);
						fBundleDescription.addResource(ri, onClasspath);
						add(table, ri);
					}
				}
			});
			final Button removeButton= createButton(c1, SWT.NONE, Util.getString("page2.remove.button.label")); //$NON-NLS-1$
			removeButton.setEnabled(false);
			removeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					remove(table, onClasspath, removeButton);
				}
			});
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeButton.setEnabled(table.getSelectionCount() > 0);
				}
			});
		return table;
	}
	
	private void add(Table t, ResourceInfo ri) {
		TableItem ti= new TableItem(t, SWT.NONE);
		ti.setData(ri);
		ti.setText(ri.fPath);
	}
		
	private void remove(Table table, boolean b, Button removeButton) {
		TableItem[] selection= table.getSelection();
		for (int i= 0; i < selection.length; i++) {
			TableItem ti= selection[i];
			ResourceInfo ri= (ResourceInfo) ti.getData();
			if (fBundleDescription.removeResource(ri, b)) {
				int ix= table.indexOf(ti);
				if (ix >= 0)
					table.remove(ix);
			}
		}
		removeButton.setEnabled(table.getSelectionCount() > 0);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (fWorkingDir != null)
			fWorkingDir.setText(fBundleDescription.get(WORKINGDIR, "")); //$NON-NLS-1$

		if (fClassPath != null) {
			fClassPath.removeAll();
			ResourceInfo[] ris= fBundleDescription.getResources(true);
			for (int i= 0; i < ris.length; i++)
				add(fClassPath, ris[i]);
		}
		
		if (fResources != null) {
			fResources.removeAll();
			ResourceInfo[] ris= fBundleDescription.getResources(false);
			for (int i= 0; i < ris.length; i++)
				add(fResources, ris[i]);
		}
	}
		
	public boolean isPageComplete() {
		return true;
	}
}
