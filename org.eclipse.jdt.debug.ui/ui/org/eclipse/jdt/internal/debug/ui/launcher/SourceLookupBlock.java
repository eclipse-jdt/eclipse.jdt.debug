package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.zip.ZipFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.internal.ui.wizards.swt.MGridData;
import org.eclipse.jdt.internal.ui.wizards.swt.MGridLayout;
import org.eclipse.jdt.launching.ProjectSourceLocator;
import org.eclipse.jdt.launching.sourcelookup.ArchiveSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.DirectorySourceLocation;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SourceLookupBlock {
	
	private IJavaProject fJavaProject;
	
	private SelectionButtonDialogField fUseDefaultRadioButton;
	private SelectionButtonDialogField fUseDefinedRadioButton;
	private CheckedListDialogField fProjectList;
	private StringDialogField fZipSourceRootField;
	
	/**
	 * Cache of source roots for zip files. Kept in 
	 * a cache so the user may cancel.
	 */
	private HashMap fSourceRoots = new HashMap(3);
	
	private Control fSWTControl;

	private class SourceLookupAdapter implements IListAdapter, IDialogFieldListener {
		public void customButtonPressed(DialogField field, int index) {
			if (index == 6) {
				addDirectoryPressed();
			} else if (index == 7) {
				addZipPressed();
			}
		}

		public void selectionChanged(DialogField field) {
			List selection = ((ListDialogField)field).getSelectedElements();
			
			// update remove button - can only remove directories
			// and zips
			boolean enabled = false;
			Iterator iter = selection.iterator();
			while (iter.hasNext()) {
				Object selected = iter.next();
				if (selected instanceof JavaProjectSourceLocation) {
					enabled = false;
					break;
				}
				enabled = true;
			}
			((ListDialogField)field).enableButton(8, enabled);
			
			// enable & update source root field
			if (selection.size() == 1 && selection.get(0) instanceof ArchiveSourceLocation) {
				ArchiveSourceLocation location = (ArchiveSourceLocation)selection.get(0);
				fZipSourceRootField.setEnabled(true);
				IPath path = location.getRootPath();
				String name = ""; //$NON-NLS-1$
				if (path != null) {
					name = path.toString();
				}
				fZipSourceRootField.setText(name);
			} else {
				fZipSourceRootField.setEnabled(false);
			}
		}

		public void dialogFieldChanged(DialogField field) {
			buttonPressed();
			updateSourceRoot();
		}
	}
	
	public SourceLookupBlock(IJavaProject project) {
		fJavaProject= project;
		
		SourceLookupAdapter adapter= new SourceLookupAdapter();
		
		fUseDefaultRadioButton= new SelectionButtonDialogField(SWT.RADIO);
		fUseDefaultRadioButton.setDialogFieldListener(adapter);
		fUseDefaultRadioButton.setLabelText(LauncherMessages.getString("SourceLookupBlock.default.label")); //$NON-NLS-1$

		fUseDefinedRadioButton= new SelectionButtonDialogField(SWT.RADIO);
		fUseDefinedRadioButton.setDialogFieldListener(adapter);
		fUseDefinedRadioButton.setLabelText(LauncherMessages.getString("SourceLookupBlock.defined.label")); //$NON-NLS-1$

		String[] buttonLabels= new String[] {
			/* 0 */ LauncherMessages.getString("SourceLookupBlock.projects.checkall"), //$NON-NLS-1$
			/* 1 */ LauncherMessages.getString("SourceLookupBlock.projects.uncheckall"), //$NON-NLS-1$
			/* 2 */ null,
			/* 3 */ LauncherMessages.getString("SourceLookupBlock.projects.up"), //$NON-NLS-1$
			/* 4 */ LauncherMessages.getString("SourceLookupBlock.projects.down"), //$NON-NLS-1$
			/* 5 */ null,
			/* 6 */ LauncherMessages.getString("SourceLookupBlock.Add_Directory"), //$NON-NLS-1$
			/* 7 */ LauncherMessages.getString("SourceLookupBlock.Add_Zip"), //$NON-NLS-1$
			/* 8 */ LauncherMessages.getString("SourceLookupBlock.Remove") //$NON-NLS-1$
		};
		
		fProjectList= new CheckedListDialogField(adapter, buttonLabels, new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_BASICS));
		fProjectList.setLabelText(LauncherMessages.getString("SourceLookupBlock.projects.label")); //$NON-NLS-1$
		fProjectList.setCheckAllButtonIndex(0);
		fProjectList.setUncheckAllButtonIndex(1);
		fProjectList.setUpButtonIndex(3);
		fProjectList.setDownButtonIndex(4);
		fProjectList.setRemoveButtonIndex(8);
		
		fZipSourceRootField = new StringDialogField();
		fZipSourceRootField.setLabelText(LauncherMessages.getString("SourceLookupBlock.Source_root")); //$NON-NLS-1$
		fZipSourceRootField.setDialogFieldListener(adapter);
		
		initializeFields();
	}

	public void initializeFields() {
		ArrayList allLocations = new ArrayList();
		ArrayList checked= new ArrayList();
		boolean useClasspath= true;
		
		try {
			IJavaSourceLocation[] locations = ProjectSourceLocator.getPersistedSourceLocations(fJavaProject);
			if (locations != null) {
				allLocations.addAll(Arrays.asList(locations));
				useClasspath= false;
			} else {
				IJavaProject[] projects = ProjectSourceLocator.getSourceLookupPath(fJavaProject);
				if (projects == null) {
					allLocations.addAll(Arrays.asList(JavaSourceLocator.getDefaultSourceLocations(fJavaProject)));
				} else {
					for (int i = 0; i < projects.length; i++) {
						allLocations.add(new JavaProjectSourceLocation(projects[i]));
					}
				}
			}
			checked= new ArrayList(allLocations);
			IJavaProject[] allProjects= fJavaProject.getJavaModel().getJavaProjects();
			for (int i= 0; i < allProjects.length; i++) {
				IJavaSourceLocation curr= new JavaProjectSourceLocation(allProjects[i]);
				if (!allLocations.contains(curr)) {
					allLocations.add(curr);
				}
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		
		fUseDefaultRadioButton.setSelection(useClasspath);
		fUseDefinedRadioButton.setSelection(!useClasspath);
		fProjectList.setElements(allLocations);
		fProjectList.setCheckedElements(checked);
		fZipSourceRootField.setEnabled(false);
	}
	
	/**
	 * Returns all java project source locations
	 */
	protected List getAllJavaProjectSourceLocations() throws JavaModelException {
		IJavaProject[] allProjects= fJavaProject.getJavaModel().getJavaProjects();
		ArrayList allLocations = new ArrayList(allProjects.length);
		for (int i= 0; i < allProjects.length; i++) {
			allLocations.add(new JavaProjectSourceLocation(allProjects[i]));
		}		
		return allLocations;
	}

	/*
	 * Create the content.
	 */
	public Control createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		
		MGridLayout layout= new MGridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		
		fUseDefaultRadioButton.doFillIntoGrid(composite, 2);
		fUseDefinedRadioButton.doFillIntoGrid(composite, 2);
		
		fProjectList.doFillIntoGrid(composite, 3);
		LayoutUtil.setHorizontalSpan(fProjectList.getLabelControl(null), 2);
		
		Composite c2 = new Composite(composite, SWT.NONE);
		layout = new MGridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		c2.setLayout(layout);
		MGridData gd = new MGridData(MGridData.FILL_HORIZONTAL);
		c2.setLayoutData(gd);
				
		fZipSourceRootField.doFillIntoGrid(c2, 2);
		
		fSWTControl= composite;
		return composite;
	}

	
	private Shell getShell() {
		if (fSWTControl != null) {
			return fSWTControl.getShell();
		}
		return JDIDebugUIPlugin.getActiveWorkbenchShell();
	}
		
	
	private void buttonPressed() {
		if (fUseDefaultRadioButton.isSelected()) {
			try {
				IJavaSourceLocation[] defaultLocations = JavaSourceLocator.getDefaultSourceLocations(fJavaProject);
				List list = new ArrayList(defaultLocations.length);
				for (int i = 0; i < defaultLocations.length; i++) {
					list.add(defaultLocations[i]);
				}
				fProjectList.setElements(list);
				fProjectList.setCheckedElements(list);
				fProjectList.addElements(getAllJavaProjectSourceLocations());
			} catch (CoreException e) {
				JDIDebugUIPlugin.log(e);
			}
		}
		fProjectList.setEnabled(!fUseDefaultRadioButton.isSelected());

	}
	
	private void getProjectsFromClaspath(IJavaProject project, ArrayList res) throws JavaModelException {
		if (res.contains(project)) {
			return;
		}
		res.add(project);
		IJavaModel model= project.getJavaModel();
		
		IClasspathEntry[] entries= project.getRawClasspath();
		for (int i= 0; i < entries.length; i++) {
			IClasspathEntry curr= entries[i];
			if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				IJavaProject jproject= model.getJavaProject(curr.getPath().lastSegment());
				if (jproject.exists()) {
					getProjectsFromClaspath(jproject, res);
				}
			}
		}
	}
	
	
	public void applyChanges() throws JavaModelException {
		if (fUseDefaultRadioButton.isSelected()) {
			ProjectSourceLocator.setSourceLookupPath(fJavaProject, null);
			ProjectSourceLocator.setPersistedSourceLocations(fJavaProject, null);
		} else {
			Iterator allLocations = fProjectList.getElements().iterator();
			List orderedLocations = new ArrayList();
			while (allLocations.hasNext()) {
				Object location = allLocations.next();
				if (fProjectList.isChecked(location)) {
					orderedLocations.add(location);
					String root = (String)fSourceRoots.get(location);
					if (root != null) {
						((ArchiveSourceLocation)location).setRootPath(root);
					}
				}
			}
			ProjectSourceLocator.setPersistedSourceLocations(fJavaProject, (IJavaSourceLocation[]) orderedLocations.toArray(new IJavaSourceLocation[orderedLocations.size()]));
		}	
	}
	
	protected void addDirectoryPressed() {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		dialog.setText(LauncherMessages.getString("SourceLookupBlock.Choose_a_root_directoy_containing_source_packages")); //$NON-NLS-1$
		//dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res != null) {
			DirectorySourceLocation location = new DirectorySourceLocation(new File(res));
			fProjectList.addElement(location);
			fProjectList.setChecked(location, true);
		}		
	}
	
	protected void addZipPressed() {
		FileDialog dialog= new FileDialog(getShell());
		dialog.setText(LauncherMessages.getString("SourceLookupBlock.Choose_source_zip")); //$NON-NLS-1$
		//dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res != null) {
			try {
				ArchiveSourceLocation location = new ArchiveSourceLocation(res, null);
				fProjectList.addElement(location);
				fProjectList.setChecked(location, true);
			} catch (IOException e) {
				JDIDebugUIPlugin.log(e);
			}
		}		
	}	

	protected void updateSourceRoot() {
		if (fZipSourceRootField.isEnabled()) {
			List list = fProjectList.getSelectedElements();
			if (list.size() == 1 && list.get(0) instanceof ArchiveSourceLocation) {
				ArchiveSourceLocation location = (ArchiveSourceLocation)list.get(0);
				fSourceRoots.put(location, fZipSourceRootField.getText());
			}
		}
	}
}

