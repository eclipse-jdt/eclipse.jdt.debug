/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.breakpoints;

import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.SWTUtil;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This is a specialization of <code>FilteredItemsSelectionDialog</code> used to present
 * users with a listing of exceptions to select to create exception breakpoints on
 * 
 * @since 3.3
 *
 */
public class AddExceptionDialog extends FilteredItemsSelectionDialog {
	
	/**
	 * Collects matching results into the specified content provider
	 */
	public class ExceptionTypeNameRequestor extends TypeNameMatchRequestor {
		private AbstractContentProvider fContentProvider;
		private ItemsFilter fFilter;
		
		public ExceptionTypeNameRequestor(AbstractContentProvider provider, ItemsFilter filter) {
			super();
			fContentProvider = provider;
			fFilter = filter;
		}
		public void acceptTypeNameMatch(TypeNameMatch match) {
			if(fFilter.matchItem(match)) {
				fContentProvider.add(match.getType(), fFilter);
			}
		}
	}
	
	/**
	 * Main list label provider
	 */
	public class ExceptionLabelProvider implements ILabelProvider {
		HashMap fImageMap = new HashMap();

		public Image getImage(Object element) {
			if(element instanceof IAdaptable) {
				IAdaptable type = (IAdaptable) element;
				IWorkbenchAdapter adapter = (IWorkbenchAdapter) type.getAdapter(IWorkbenchAdapter.class);
				if(adapter != null) {
					ImageDescriptor descriptor = adapter.getImageDescriptor(type);
					Image image = (Image) fImageMap.get(descriptor);
					if(image == null) {
						image = descriptor.createImage();
						fImageMap.put(descriptor, image);
					}
					return image;
				}
			}
			return null;
		}
		public String getText(Object element) {
			if(element instanceof IType) {
				IType type = (IType) element;
				String label = type.getElementName();
				String container = getDeclaringContainerName(type);
				if(container != null && !"".equals(container)) { //$NON-NLS-1$
					label += " - "+container; //$NON-NLS-1$
				}
				return label;
			}
			return null;
		}
		
		/**
		 * Returns the name of the declaring container name
		 * @param type the type to find the container name for
		 * @return the container name for the specified type
		 */
		protected String getDeclaringContainerName(IType type) {
			IType outer = type.getDeclaringType();
			if(outer != null) {
				return outer.getFullyQualifiedName('.');
			}
			else {
				String name = type.getPackageFragment().getElementName();
				if("".equals(name)) { //$NON-NLS-1$
					name = BreakpointMessages.AddExceptionDialog_14;
				}
				return name;
			}
		}
		
		/**
		 * Returns the norrowest enclosing <code>IJavaElement</code> which is either 
		 * an <code>IType</code> (enclosing) or an <code>IPackageFragment</code> (contained in)
		 * @param type the type to find the enclosing <code>IJavaElement</code> for.
		 * @return the enclosing element or <code>null</code> if none
		 */
		protected IJavaElement getDeclaringContainer(IType type) {
			IJavaElement outer = type.getDeclaringType();
			if(outer == null) {
				outer = type.getPackageFragment();
			}
			return outer;
		}

		public void dispose() {
			fImageMap.clear();
			fImageMap = null;
		}
		public void addListener(ILabelProviderListener listener) {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	/**
	 * Provides a label and image for the details area of the dialog
	 */
	class ExceptionDetailsLabelProvider extends ExceptionLabelProvider {
		public String getText(Object element) {
			if(element instanceof IType) {
				IType type = (IType) element;
				String name = getDeclaringContainerName(type);
				if(name != null) {
					if(name.equals(BreakpointMessages.AddExceptionDialog_14)) {
						IJavaProject project = type.getJavaProject();
						if(project != null) {
							try {
								return project.getOutputLocation().toOSString().substring(1)+" - "+name; //$NON-NLS-1$
							} 
							catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
						}
					}
					else {
						return name;
					}
				}
			}
			return null;
		}
		public Image getImage(Object element) {
			if(element instanceof IType) {
				return super.getImage(getDeclaringContainer((IType)element));
			}
			return super.getImage(element);
		}
	}
	
	/**
	 * Simple items filter
	 */
	class ExceptionItemsFilter extends ItemsFilter {
		public boolean isConsistentItem(Object item) {
			return item instanceof IType;
		}
		public boolean matchItem(Object item) {
			IType type = null;
			if(item instanceof TypeNameMatch) {
				TypeNameMatch tname = (TypeNameMatch) item;
				type = tname.getType();
			}
			else if(item instanceof IType) {
				type = (IType) item;
			}
			if(type != null) {
				if(!isException(type)) {
					return false;
				}
				return matches(type.getElementName());
			}
			return false;
		}
		/**
		 * Returns if the specified type is an exception. Determination is made based on
		 * whether the type is a subclass of <code>java.lang.Throwable</code>.
		 * @param type
		 * @return if the specified type is an exception
		 */
		protected boolean isException(IType type) {
	    	try {
	            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
	            IType curr = type;
	            while (curr != null) {
	                if ("java.lang.Throwable".equals(curr.getFullyQualifiedName('.'))) { //$NON-NLS-1$
	                    return true;
	                }
	                curr = hierarchy.getSuperclass(curr);
	            }
	        } 
	        catch (JavaModelException e) {JDIDebugUIPlugin.log(e);}
	        return false;
	    }
	}
	
	/**
	 * The selection history for the dialog
	 */
	class ExceptionSelectionHistory extends SelectionHistory {
		protected Object restoreItemFromMemento(IMemento memento) {
			IJavaElement element = JavaCore.create(memento.getTextData()); 
			return (element instanceof IType ? element : null);
		}
		protected void storeItemToMemento(Object item, IMemento memento) {
			if(item instanceof IType) {
				memento.putTextData(((IType) item).getHandleIdentifier());
			}
		}
	}
	
	/**
	 * Constants
	 */
	private static final String SETTINGS_ID = JDIDebugUIPlugin.getUniqueIdentifier() + ".ADD_EXCEPTION_DIALOG"; //$NON-NLS-1$
	public static final String SETTING_CAUGHT_CHECKED = "caughtChecked"; //$NON-NLS-1$
	public static final String SETTING_UNCAUGHT_CHECKED = "uncaughtChecked"; //$NON-NLS-1$
	
	 /**
	  * widgets
	  */
	 private Button fCaughtButton;
	 private Button fUncaughtButton;
	 private boolean fCaught = false;
	 private boolean fUncaught = false;
	 
	/**
	 * Constructor
	 */
	public AddExceptionDialog() {
		super(JDIDebugUIPlugin.getShell(), false);
		setTitle(BreakpointMessages.AddExceptionAction_0);
		setMessage(BreakpointMessages.AddExceptionAction_1);
		setInitialPattern("*Exception*"); //$NON-NLS-1$
		setSelectionHistory(new ExceptionSelectionHistory());
		
		setListLabelProvider(new ExceptionLabelProvider());
		setDetailsLabelProvider(new ExceptionDetailsLabelProvider());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control ctrl = super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(ctrl, IJavaDebugHelpContextIds.ADD_EXCEPTION_DIALOG);
		return ctrl;
	}

	/**
	 * Returns if the breakpoint should be set to suspend when the associated exception is thrown, but caught
	 * @return if the breakpoint should be set to suspend when the associated exception is thrown, but caught
	 */
	public boolean shouldHandleCaughtExceptions() {
		return fCaught;
	}
	
	/**Returns if the breakpoint should be set to suspend when the associated exception is thrown, but not caught
	 * @return if the breakpoint should be set to suspend when the associated exception is thrown, but not caught
	 */
	public boolean shouldHandleUncaughtExceptions() {
		return fUncaught;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createExtendedContentArea(Composite parent) {
		Composite comp = SWTUtil.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL);
		fCaughtButton = SWTUtil.createCheckButton(comp, BreakpointMessages.AddExceptionDialog_15, getDialogSettings().getBoolean(SETTING_CAUGHT_CHECKED));
		fCaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fCaught = fCaughtButton.getSelection();
			}
		});
		fUncaughtButton = SWTUtil.createCheckButton(comp, BreakpointMessages.AddExceptionDialog_16, getDialogSettings().getBoolean(SETTING_UNCAUGHT_CHECKED));
		fUncaughtButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fUncaught = fUncaughtButton.getSelection();
			}
		});
		return comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#okPressed()
	 */
	protected void okPressed() {
		fCaught = fCaughtButton.getSelection();
		fUncaught = fUncaughtButton.getSelection();
		IDialogSettings settings = getDialogSettings();
		settings.put(SETTING_CAUGHT_CHECKED, fCaught);
		settings.put(SETTING_UNCAUGHT_CHECKED, fUncaught);
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter()
	 */
	protected ItemsFilter createFilter() {
		return new ExceptionItemsFilter();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractContentProvider, org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fillContentProvider(AbstractContentProvider contentProvider,	ItemsFilter itemsFilter, IProgressMonitor progressMonitor)	throws CoreException {
		if(progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		progressMonitor.setTaskName("Searching for Exception types"); //$NON-NLS-1$
		SearchEngine engine = new SearchEngine((WorkingCopyOwner) null);
		engine.searchAllTypeNames((char[])null, SearchPattern.R_PATTERN_MATCH, "*Exception*".toCharArray(),  //$NON-NLS-1$
				SearchPattern.R_PATTERN_MATCH, IJavaSearchConstants.CLASS, SearchEngine.createWorkspaceScope(), 
				new ExceptionTypeNameRequestor(contentProvider, itemsFilter), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, progressMonitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = JDIDebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
			section.put(SETTING_CAUGHT_CHECKED, true);
	        section.put(SETTING_UNCAUGHT_CHECKED, true);
		}
		return section;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementName(java.lang.Object)
	 */
	public String getElementName(Object item) {
		if(item instanceof IType) {
			return ((IType)item).getElementName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
	 */
	protected Comparator getItemsComparator() {
		Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
            	if(o1 instanceof IType && o2 instanceof IType) {
            		return ((IType)o1).getElementName().compareTo(((IType)o2).getElementName());
            	}
                return -1;
            }
        };
        return comp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	protected IStatus validateItem(Object item) {
		return (item instanceof IType ? Status.OK_STATUS : new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), BreakpointMessages.AddExceptionDialog_13));
	}

}
