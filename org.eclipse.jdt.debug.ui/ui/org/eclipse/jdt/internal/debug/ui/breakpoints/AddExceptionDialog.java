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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
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

import com.ibm.icu.text.MessageFormat;

/**
 * This is a specialization of <code>FilteredItemsSelectionDialog</code> used to present
 * users with a listing of exceptions to select to create exception breakpoints on
 * 
 * @since 3.3
 *
 */
public class AddExceptionDialog extends FilteredItemsSelectionDialog {
	
	/**
	 * A Util class that is used to create labels for the types of the main list in the add exception dialog
	 */
	private static class TypeInfoUtil {

		private Map fLib2Name= new HashMap();
		private String[] fInstallLocations;
		private String[] fVMNames;

		public TypeInfoUtil() {
			List locations= new ArrayList();
			List labels= new ArrayList();
			IVMInstallType[] installs= JavaRuntime.getVMInstallTypes();
			for (int i= 0; i < installs.length; i++) {
				processVMInstallType(installs[i], locations, labels);
			}
			fInstallLocations= (String[]) locations.toArray(new String[locations.size()]);
			fVMNames= (String[]) labels.toArray(new String[labels.size()]);

		}
		private void processVMInstallType(IVMInstallType installType, List locations, List labels) {
			if (installType != null) {
				IVMInstall[] installs= installType.getVMInstalls();
				boolean isMac= Platform.OS_MACOSX.equals(Platform.getOS());
				final String HOME_SUFFIX= "/Home"; //$NON-NLS-1$
				for (int i= 0; i < installs.length; i++) {
					String label= getFormattedLabel(installs[i].getName());
					LibraryLocation[] libLocations= installs[i].getLibraryLocations();
					if (libLocations != null) {
						processLibraryLocation(libLocations, label);
					} else {
						String filePath= installs[i].getInstallLocation().getAbsolutePath();
						// on MacOS X install locations end in an additional
						// "/Home" segment; remove it
						if (isMac && filePath.endsWith(HOME_SUFFIX))
							filePath= filePath.substring(0, filePath.length() - HOME_SUFFIX.length() + 1);
						locations.add(filePath);
						labels.add(label);
					}
				}
			}
		}
		private void processLibraryLocation(LibraryLocation[] libLocations, String label) {
			for (int l= 0; l < libLocations.length; l++) {
				LibraryLocation location= libLocations[l];
				fLib2Name.put(location.getSystemLibraryPath().toOSString(), label);
			}
		}

		private String getFormattedLabel(String name) {
			return MessageFormat.format(BreakpointMessages.AddExceptionDialog_12, new String[] {name});
		}
		/**
		 * Returns the simple name of the underyling type from the typename match
		 * @param element
		 * @return
		 */
		public String getText(TypeNameMatch element) {
			return element.getSimpleTypeName();
		}
		/**
		 * Returns the qualified text for the specified typename match
		 * @param type
		 * @return the qualified text for the specified typename match
		 */
		public String getQualifiedText(TypeNameMatch type) {
			StringBuffer result= new StringBuffer();
			result.append(type.getSimpleTypeName());
			String containerName= type.getTypeContainerName();
			result.append(JavaElementLabels.CONCAT_STRING);
			if (containerName.length() > 0) {
				result.append(containerName);
			} else {
				result.append(BreakpointMessages.AddExceptionDialog_11);
			}
			return result.toString();
		}
		/**
		 * Retirn the fully qualified text for the specified typename match
		 * @param type
		 * @return the fully qualified text for the specified typename match
		 */
		public String getFullyQualifiedText(TypeNameMatch type) {
			StringBuffer result= new StringBuffer();
			result.append(type.getSimpleTypeName());
			String containerName= type.getTypeContainerName();
			if (containerName.length() > 0) {
				result.append(JavaElementLabels.CONCAT_STRING);
				result.append(containerName);
			}
			result.append(JavaElementLabels.CONCAT_STRING);
			result.append(getContainerName(type));
			return result.toString();
		}
		/**
		 * Returns the qualification text for an element, more specifically appends the package information to the element label
		 * @param type
		 * @return the qualification text for an element
		 */
		public String getQualificationText(TypeNameMatch type) {
			StringBuffer result= new StringBuffer();
			String containerName= type.getTypeContainerName();
			if (containerName.length() > 0) {
				result.append(containerName);
				result.append(JavaElementLabels.CONCAT_STRING);
			}
			result.append(getContainerName(type));
			return result.toString();
		}
		/**
		 * Returns the package fragment root simple container name from the typename match 
		 * @param type
		 * @return the simple text name for the package fragment root for the typename match
		 */
		private String getContainerName(TypeNameMatch type) {
			IPackageFragmentRoot root= type.getPackageFragmentRoot();
			if (root.isExternal()) {
				String name= root.getPath().toOSString();
				for (int i= 0; i < fInstallLocations.length; i++) {
					if (name.startsWith(fInstallLocations[i])) {
						return fVMNames[i];
					}
				}
				String lib= (String) fLib2Name.get(name);
				if (lib != null)
					return lib;
			}
			StringBuffer buf= new StringBuffer();
			JavaElementLabels.getPackageFragmentRootLabel(root, JavaElementLabels.ROOT_QUALIFIED | JavaElementLabels.ROOT_VARIABLE, buf);
			return buf.toString();
		}
	}
	
	/**
	 * Collects matching results into the specified content provider
	 */
	public class ExceptionTypeNameRequestor extends TypeNameMatchRequestor {
		private AbstractContentProvider fContentProvider;
		private ItemsFilter fFilter;
		
		/**
		 * Constructor
		 * @param provider
		 * @param filter
		 */
		public ExceptionTypeNameRequestor(AbstractContentProvider provider, ItemsFilter filter) {
			super();
			fContentProvider = provider;
			fFilter = filter;
		}
		/**
		 * @see org.eclipse.jdt.core.search.TypeNameMatchRequestor#acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch)
		 */
		public void acceptTypeNameMatch(TypeNameMatch match) {
			if(fFilter.matchItem(match)) {
				fContentProvider.add(match, fFilter);
			}
		}
	}
	
	/**
	 * Main list label provider
	 */
	public class ExceptionLabelProvider extends LabelProvider implements ILabelDecorator {
		HashMap fImageMap = new HashMap();
		
		/**
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			IAdaptable type = null;
			if(element instanceof TypeNameMatch) {
				type = ((TypeNameMatch)element).getType();
			}
			if(element instanceof IAdaptable) {
				type = (IAdaptable) element;
			}
			if(type != null) {
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
		/**
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (!(element instanceof TypeNameMatch)) {
				return super.getText(element);
			}
			return fTypeInfoUtil.getText((TypeNameMatch) element);
		}
		/**
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			if (!(element instanceof TypeNameMatch)) {
				return null;
			}
			return fTypeInfoUtil.getQualifiedText((TypeNameMatch) element);
		}
		/**
		 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
		 */
		public void dispose() {
			fImageMap.clear();
			fImageMap = null;
		}
		public void addListener(ILabelProviderListener listener) {}
		public boolean isLabelProperty(Object element, String property) {return false;}
		public void removeListener(ILabelProviderListener listener) {}
		public Image decorateImage(Image image, Object element) {return null;}
		
	}
	
	/**
	 * Provides a label and image for the details area of the dialog
	 */
	class ExceptionDetailsLabelProvider extends ExceptionLabelProvider {
		public String getText(Object element) {
			if (element instanceof TypeNameMatch) {
				return fTypeInfoUtil.getQualificationText((TypeNameMatch) element);
			}
			return super.getText(element);
		}
		/**
		 * Returns the narrowest enclosing <code>IJavaElement</code> which is either 
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
		/**
		 * @see org.eclipse.jdt.internal.debug.ui.breakpoints.AddExceptionDialog.ExceptionLabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if(element instanceof TypeNameMatch) {
				return super.getImage(getDeclaringContainer(((TypeNameMatch)element).getType()));
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
			if(item instanceof TypeNameMatch) {
				TypeNameMatch tname = (TypeNameMatch) item;
				IType type = tname.getType();
				if(type != null) {
					if(type.getElementName().indexOf("Exception") < 0) { //$NON-NLS-1$
						return false;
					}
					return matches(type.getElementName());
				}
			}
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
	 
	 private TypeInfoUtil fTypeInfoUtil = null;
	 
	/**
	 * Constructor
	 */
	public AddExceptionDialog() {
		super(JDIDebugUIPlugin.getShell(), false);
		fTypeInfoUtil = new TypeInfoUtil();
		setTitle(BreakpointMessages.AddExceptionAction_0);
		setMessage(BreakpointMessages.AddExceptionAction_1);
		setInitialPattern("*Exception*"); //$NON-NLS-1$
		setSelectionHistory(new ExceptionSelectionHistory());
		ExceptionLabelProvider lp = new ExceptionLabelProvider();
		setListLabelProvider(lp);
		setListSelectionLabelDecorator(lp);
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
            	if(o1 instanceof TypeNameMatch && o2 instanceof TypeNameMatch) {
            		return ((TypeNameMatch)o1).getSimpleTypeName().compareTo(((TypeNameMatch)o2).getSimpleTypeName());
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
		if(item instanceof TypeNameMatch) {
			IType type = ((TypeNameMatch) item).getType();
			try {
	            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
	            IType curr = type;
	            while (curr != null) {
	                if ("java.lang.Throwable".equals(curr.getFullyQualifiedName('.'))) { //$NON-NLS-1$
	                    return Status.OK_STATUS;
	                }
	                curr = hierarchy.getSuperclass(curr);
	            }
	        } 
	        catch (JavaModelException e) {
	        	JDIDebugUIPlugin.log(e);
	        	return Status.CANCEL_STATUS;
	        }
		}
		return new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), BreakpointMessages.AddExceptionDialog_13);
	}

}
