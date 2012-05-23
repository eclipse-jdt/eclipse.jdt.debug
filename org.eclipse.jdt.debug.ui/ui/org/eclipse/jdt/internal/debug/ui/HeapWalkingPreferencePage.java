/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.core.HeapWalkingManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Provides a page for changing the heap walking options. Interacts
 * with the HeapWalkingManager to get/set options.
 * 
 * @see HeapWalkingManager
 * @since 3.3
 */
public class HeapWalkingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button fShowReferencesInVarView;
	private Text fAllReferencesMaxCount;
	private Text fAllInstancesMaxCount;
	private Map<Object, String> fErrorMessages;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		fErrorMessages = new HashMap<Object, String>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		// TODO: Help must be updated
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.JAVA_HEAPWALKING_PREFERENCE_PAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		
		SWTFactory.createWrapLabel(comp, DebugUIMessages.HeapWalkingPreferencePage_0, 1, 300);
		SWTFactory.createVerticalSpacer(comp, 2);
		
		fShowReferencesInVarView = SWTFactory.createCheckButton(comp, DebugUIMessages.HeapWalkingPreferencePage_5, null, HeapWalkingManager.getDefault().isShowReferenceInVarView(), 1);
		SWTFactory.createVerticalSpacer(comp, 2);
		
		Group group = SWTFactory.createGroup(comp, DebugUIMessages.HeapWalkingPreferencePage_3, 2, 1, GridData.FILL_HORIZONTAL);
		SWTFactory.createLabel(group, DebugUIMessages.HeapWalkingPreferencePage_4, 2);
		
		SWTFactory.createLabel(group, DebugUIMessages.HeapWalkingPreferencePage_1, 1);
		fAllInstancesMaxCount = SWTFactory.createSingleText(group, 1);
		fAllInstancesMaxCount.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try{
					int result = Integer.parseInt(fAllInstancesMaxCount.getText());
					if (result < 0) throw new NumberFormatException();
					clearErrorMessage(fAllInstancesMaxCount);
				} catch (NumberFormatException exception){
					setErrorMessage(fAllInstancesMaxCount, DebugUIMessages.HeapWalkingPreferencePage_6);
				}
			}
		});
		fAllInstancesMaxCount.setText("" + HeapWalkingManager.getDefault().getAllInstancesMaxCount()); //$NON-NLS-1$
		
		SWTFactory.createLabel(group, DebugUIMessages.HeapWalkingPreferencePage_2, 1);
		fAllReferencesMaxCount = SWTFactory.createSingleText(group, 1);
		fAllReferencesMaxCount.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				try{
					int result = Integer.parseInt(fAllReferencesMaxCount.getText());
					if (result < 0) throw new NumberFormatException();
					clearErrorMessage(fAllReferencesMaxCount);
				} catch (NumberFormatException exception){
					setErrorMessage(fAllReferencesMaxCount, DebugUIMessages.HeapWalkingPreferencePage_6);
				}
			}
		});
		fAllReferencesMaxCount.setText("" + HeapWalkingManager.getDefault().getAllReferencesMaxCount()); //$NON-NLS-1$
		
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result){
			HeapWalkingManager.getDefault().setShowReferenceInVarView(fShowReferencesInVarView.getSelection());
			try{
				int maxReferences = Integer.parseInt(fAllReferencesMaxCount.getText());
				HeapWalkingManager.getDefault().setAllReferencesMaxCount(maxReferences);
			} catch (NumberFormatException exception){
				setErrorMessage(fAllReferencesMaxCount,DebugUIMessages.HeapWalkingPreferencePage_6);
				return false;
			}
			try{
				int maxReferences = Integer.parseInt(fAllInstancesMaxCount.getText());
				HeapWalkingManager.getDefault().setAllInstancesMaxCount(maxReferences);
			} catch (NumberFormatException exception){
				setErrorMessage(fAllInstancesMaxCount,DebugUIMessages.HeapWalkingPreferencePage_6);
				result = false;
			}
			HeapWalkingManager.getDefault().setShowReferenceInVarView(fShowReferencesInVarView.getSelection());
		}
		return result;
	}
	
	/**
	 * Sets an error message associated with a specific field.  Allows several
	 * fields to have their own error message.  The dialog's error message will
	 * be set to the given message.
	 * 
	 * @param cause The field that the message is associated with
	 * @param message The error message to display to the user
	 */
	private void setErrorMessage(Object cause, String message){
		fErrorMessages.put(cause,message);
		setErrorMessage(message);
		setValid(false);
	}
	
	/**
	 * Clears the error message associated with the given field.  If there are other
	 * error messages, one will be displayed to the user.  If there are no more error
	 * messages, the page becomes valid.
	 * 
	 * @param cause The field associated with a current error message.
	 */
	private void clearErrorMessage(Object cause){
		fErrorMessages.remove(cause);
		Iterator<String> iter = fErrorMessages.values().iterator();
		if (iter.hasNext()){
			setErrorMessage(iter.next());
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		HeapWalkingManager.getDefault().resetToDefaultSettings();
		fAllReferencesMaxCount.setText(Integer.toString(HeapWalkingManager.getDefault().getAllReferencesMaxCount()));
		fAllInstancesMaxCount.setText(Integer.toString(HeapWalkingManager.getDefault().getAllReferencesMaxCount())); 
		fShowReferencesInVarView.setSelection(HeapWalkingManager.getDefault().isShowReferenceInVarView());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		fErrorMessages.clear();
	}

}
