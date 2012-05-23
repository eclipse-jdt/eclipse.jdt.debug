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
package org.eclipse.jdt.internal.debug.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * This class provides a properties page displaying all of the capabilities
 * of the VM associated with the selected <code>IDebugTarget</code> or <code>IProcess</code>
 * 
 * @since 3.3
 */
public class VMCapabilitiesPropertyPage extends PropertyPage {

	/**
	 * Provides a scrollable area for the expansion composites
	 */
	class ScrollPain extends SharedScrolledComposite {
		public ScrollPain(Composite parent) {
			super(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			setExpandHorizontal(true);
			setExpandVertical(true);
			GridLayout layout = new GridLayout(1, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);
		}
	}
	
	private List<ExpandableComposite> fExpandedComps;
	private static final String EXPANDED_STATE = "vmc_expanded_state"; //$NON-NLS-1$
	private static Font fHeadingFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	
	/**
	 * Constructor 
	 */
	public VMCapabilitiesPropertyPage() {
		fExpandedComps = new ArrayList<ExpandableComposite>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		final ScrollPain scomp = new ScrollPain(parent);
		GridData gd = new GridData(GridData.FILL_BOTH);
		scomp.setLayout(new GridLayout());
		scomp.setLayoutData(gd);
		final Composite comp = new Composite(scomp, SWT.NONE);
		comp.setLayout(new GridLayout(2, true));
		gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		scomp.setContent(comp);
		VirtualMachineImpl vm = getVM();
		if(vm == null) {
			setErrorMessage(PropertyPageMessages.VMCapabilitiesPropertyPage_0);
		}
		else {
			createExplanation(comp);
			SWTFactory.createVerticalSpacer(comp, 1);
			createHeadingLabel(comp, vm);
			SWTFactory.createVerticalSpacer(comp, 1);
			
			// breakpoints
			ExpandableComposite breakpoints = createExpandibleComposite(comp, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT, PropertyPageMessages.VMCapabilitiesPropertyPage_27, 2, GridData.FILL_HORIZONTAL);
			fExpandedComps.add(breakpoints);
			Composite bp_inner = SWTFactory.createComposite(breakpoints, comp.getFont(), 2, 2, GridData.FILL_HORIZONTAL);
			breakpoints.setClient(bp_inner);
			createCapabilityEntry(bp_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_4, vm.canUseInstanceFilters());
			createCapabilityEntry(bp_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_9, vm.canWatchFieldModification());
			createCapabilityEntry(bp_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_10, vm.canWatchFieldAccess());			
			createCapabilityEntry(bp_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_24, vm.canGetMethodReturnValues());
			createCapabilityEntry(bp_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_25, vm.canRequestMonitorEvents());
			
			// hot code replace
			ExpandableComposite hcr = createExpandibleComposite(comp, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT, PropertyPageMessages.VMCapabilitiesPropertyPage_28, 2, GridData.FILL_HORIZONTAL);
			fExpandedComps.add(hcr);
			Composite hcr_inner = SWTFactory.createComposite(hcr, comp.getFont(), 2, 2, GridData.FILL_HORIZONTAL);
			hcr.setClient(hcr_inner);
			createCapabilityEntry(hcr_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_15, vm.canRedefineClasses());
			createCapabilityEntry(hcr_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_12, vm.canAddMethod());
			createCapabilityEntry(hcr_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_16, vm.canUnrestrictedlyRedefineClasses());
			
			// stepping
			ExpandableComposite stepping = createExpandibleComposite(comp, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT, PropertyPageMessages.VMCapabilitiesPropertyPage_29, 2, GridData.FILL_HORIZONTAL);
			fExpandedComps.add(stepping);
			Composite stepping_inner = SWTFactory.createComposite(stepping, comp.getFont(), 2, 2, GridData.FILL_HORIZONTAL);
			stepping.setClient(stepping_inner);
			createCapabilityEntry(stepping_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_14, vm.canPopFrames());
			createCapabilityEntry(stepping_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_3, vm.canGetSyntheticAttribute());
			createCapabilityEntry(stepping_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_21, vm.canForceEarlyReturn());
			
			// others
			ExpandableComposite general = createExpandibleComposite(comp, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT, PropertyPageMessages.VMCapabilitiesPropertyPage_30, 2, GridData.FILL_HORIZONTAL);
			fExpandedComps.add(general);
			Composite general_inner = SWTFactory.createComposite(general, comp.getFont(), 2, 2, GridData.FILL_HORIZONTAL);
			general.setClient(general_inner);
			createCapabilityEntry(general_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_6, vm.canGetCurrentContendedMonitor() && vm.canGetOwnedMonitorInfo());
			createCapabilityEntry(general_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_18, vm.canSetDefaultStratum());
			createCapabilityEntry(general_inner, PropertyPageMessages.VMCapabilitiesPropertyPage_26, vm.canGetInstanceInfo());
			
			restoreExpansionState();
		}
        applyDialogFont(comp);
		return comp;
	}
	
	private void createExplanation(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 2, GridData.FILL_HORIZONTAL);
		Label label = new Label(comp, SWT.WRAP);
        label.setFont(parent.getFont());
        label.setText(PropertyPageMessages.VMCapabilitiesPropertyPage_31);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 
	}
	
	private void createHeadingLabel(Composite parent, VirtualMachineImpl vm) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 2, 2, GridData.FILL_HORIZONTAL);				
		SWTFactory.createLabel(comp, PropertyPageMessages.VMCapabilitiesPropertyPage_1, fHeadingFont, 1);
		Text text = SWTFactory.createText(comp, SWT.READ_ONLY, 1, vm.name()+" "+vm.version()); //$NON-NLS-1$
		text.setBackground(parent.getBackground());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.VMCAPABILITIES_PROPERTY_PAGE);
	}
	
	/**
	 * Returns the VM from the debug target
	 * @return the VM form the element
	 */
	private VirtualMachineImpl getVM() {
		Object obj = getElement();
		IDebugTarget target = null;
		if(obj instanceof IDebugElement) {
			target = (IDebugTarget) ((IDebugElement)obj).getAdapter(IDebugTarget.class);
		}
		else if(obj instanceof IProcess) {
			target = (IDebugTarget) ((IProcess)obj).getAdapter(IDebugTarget.class);
		}
		if(target != null) {
			if(!target.isTerminated() && !target.isDisconnected()) {
				IJavaDebugTarget dtarget = (IJavaDebugTarget) target.getAdapter(IJavaDebugTarget.class);
				if(dtarget instanceof JDIDebugTarget) {
					return (VirtualMachineImpl) ((JDIDebugTarget)target).getVM();
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns a new capability entry for a specified group
	 * @param parent the parent group to add this entry to
	 * @param label the text for the label
	 * @param enabled the checked state of the check button
	 */
	private void createCapabilityEntry(Composite parent, String label, boolean enabled) {
		SWTFactory.createCheckButton(parent, null, null, enabled, 1).setEnabled(false);
		SWTFactory.createLabel(parent, label, parent.getFont(), 1);
	}
	
	/**
	 * Creates an ExpandibleComposite widget
	 * @param parent the parent to add this widget to
	 * @param style the style for ExpandibleComposite expanding handle, and layout
	 * @param label the label for the widget
	 * @param hspan how many columns to span in the parent
	 * @param fill the fill style for the widget
	 * @return a new ExpandibleComposite widget
	 */
	private ExpandableComposite createExpandibleComposite(Composite parent, int style, String label, int hspan, int fill) {
		ExpandableComposite ex = SWTFactory.createExpandibleComposite(parent, style, label, hspan, fill);
		ex.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				ScrollPain sp = getParentScrollPane((ExpandableComposite) e.getSource());
				if(sp != null) {
					sp.reflow(true);
				}
			}
		});
		return ex;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return JDIDebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * save the expansion state for next time, this only happens when the user selects the OK button when closing the dialog
	 */
	private void persistExpansionState() {
		IPreferenceStore store = getPreferenceStore();
		if(store != null) {
			for (int i = 0; i < fExpandedComps.size(); i++) {
				store.setValue(EXPANDED_STATE+i, fExpandedComps.get(i).isExpanded());
			}
		}
	}
	
	/**
	 * restore the expansion state
	 */
	private void restoreExpansionState() {
		IPreferenceStore store = getPreferenceStore();
		if(store == null) {
			fExpandedComps.get(0).setExpanded(true);
		}
		else {
			ExpandableComposite ex;
			for (int i = 0; i < fExpandedComps.size(); i++) {
				ex = fExpandedComps.get(i);
				ex.setExpanded(store.getBoolean(EXPANDED_STATE+i));
			}
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean ok = super.performOk();
		persistExpansionState();
		return ok;
	}

	/**
	 * Finds the parent ScrollPain that needs to be notified that it should reFlow to show the new elements
	 * @param comp the initial comp
	 * @return the parent or null, in this case though, we will never return null
	 */
	private ScrollPain getParentScrollPane(Composite comp) {
		Control parent = comp.getParent();
		while(parent != null && !(parent instanceof ScrollPain)) {
			parent = parent.getParent();
		}
		if(parent != null) {
			return (ScrollPain)parent;
		}
		return null;
	}
}
