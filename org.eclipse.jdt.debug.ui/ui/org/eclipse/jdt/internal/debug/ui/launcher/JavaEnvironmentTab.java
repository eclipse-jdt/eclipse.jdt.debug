package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.JavaLaunchConfigurationUtils;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Launch configuration tab for local java launches that presents the various paths (bootpath,
 * classpath and extension dirs path) and the environment variables to the user.
 */
public class JavaEnvironmentTab extends JavaLaunchConfigurationTab {
	
	// Paths UI widgets
	protected TabFolder fPathTabFolder;
	protected TabItem fBootPathTabItem;
	protected TabItem fClassPathTabItem;
	protected TabItem fJRETabItem;
	protected List fBootPathList;
	protected List fClassPathList;
	protected Button fClassPathDefaultButton;
	protected Button fPathAddArchiveButton;
	protected Button fPathAddDirectoryButton;
	protected Button fPathRemoveButton;
	protected Button fPathMoveUpButton;
	protected Button fPathMoveDownButton;
	// Remember last directory when browsing for archives or directories
	protected String fLastBrowsedDirectory;
	
	// Listener for list selection events
	protected SelectionAdapter fListSelectionAdapter;
	
	// Java project context
	protected IJavaProject fJavaProject;
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	// Constants used in reading & persisting XML documents containing path entries
	protected static final String PATH_XML_ENTRIES = "pathEntries"; //$NON-NLS-1$
	protected static final String PATH_XML_ENTRY = "pathEntry"; //$NON-NLS-1$
	protected static final String PATH_XML_PATH = "path"; //$NON-NLS-1$
			
	/**
	 * @see ILaunchConfigurationTab#createTabControl(ILaunchConfigurationDialog, TabItem)
	 */
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp, 2);
		
		fPathTabFolder = new TabFolder(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		fPathTabFolder.setLayoutData(gd);
		
		Composite classPathComp = new Composite(fPathTabFolder, SWT.NONE);
		GridLayout classPathLayout = new GridLayout();
		classPathComp.setLayout(classPathLayout);
		
		fClassPathList = new List(classPathComp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fClassPathList.setLayoutData(gd);
		fClassPathList.setData(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
		fClassPathList.addSelectionListener(getListSelectionAdapter());
		fClassPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 0);
		fClassPathTabItem.setText(LauncherMessages.getString("JavaEnvironmentTab.Class&path_9")); //$NON-NLS-1$
		fClassPathTabItem.setControl(classPathComp);
		fClassPathTabItem.setData(fClassPathList);
		
		fClassPathDefaultButton = new Button(classPathComp, SWT.CHECK);
		fClassPathDefaultButton.setText(LauncherMessages.getString("JavaEnvironmentTab.Use_defau&lt_classpath_10")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fClassPathDefaultButton.setLayoutData(gd);
		fClassPathDefaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleClassPathDefaultButtonSelected();
			}
		});
		
		fBootPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fBootPathList.setLayoutData(gd);
		fBootPathList.setData(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
		fBootPathList.addSelectionListener(getListSelectionAdapter());
		fBootPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 1);
		fBootPathTabItem.setText(LauncherMessages.getString("JavaEnvironmentTab.&Bootpath_11")); //$NON-NLS-1$
		fBootPathTabItem.setControl(fBootPathList);
		fBootPathTabItem.setData(fBootPathList);
				
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		
		createVerticalSpacer(pathButtonComp, 1);
		
		fPathAddArchiveButton = createPushButton(pathButtonComp,LauncherMessages.getString("JavaEnvironmentTab.Add_Ja&r_15"), null); //$NON-NLS-1$
		fPathAddArchiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddArchiveButtonSelected();
			}
		});
		gd = (GridData)fPathAddArchiveButton.getLayoutData();
		
		fPathAddDirectoryButton = createPushButton(pathButtonComp, LauncherMessages.getString("JavaEnvironmentTab.Add_&Folder_16"), null); //$NON-NLS-1$
		fPathAddDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddDirectoryButtonSelected();
			}
		});
		
		fPathRemoveButton = createPushButton(pathButtonComp,LauncherMessages.getString("JavaEnvironmentTab.R&emove_17"), null); //$NON-NLS-1$
		fPathRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathRemoveButtonSelected();
			}
		});
				
		fPathMoveUpButton = createPushButton(pathButtonComp, LauncherMessages.getString("JavaEnvironmentTab.&Up_18"), null); //$NON-NLS-1$
		fPathMoveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(true);
			}
		});
		
		fPathMoveDownButton = createPushButton(pathButtonComp, LauncherMessages.getString("JavaEnvironmentTab.D&own_19"), null); //$NON-NLS-1$
		fPathMoveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(false);
			}
		});
		
		setPathButtonsEnableState();
		
		
		fPathTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setPathButtonsEnableState();
			}			
		});
			
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	protected void updateConfigFromPathList(List listWidget, ILaunchConfigurationWorkingCopy config) {
		String[] items = listWidget.getItems();
		java.util.List listDataStructure = createListFromArray(items);
		String attributeID = (String) listWidget.getData();
		if (listDataStructure.isEmpty()) {
			config.setAttribute(attributeID, (java.util.List )null);			
		} else {
			config.setAttribute(attributeID, listDataStructure);
		}
	}
	
	protected java.util.List createListFromArray(Object[] array) {
		java.util.List list = new ArrayList(array.length);
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}
	
	protected void updateConfigFromEnvTable(Table tableWidget, ILaunchConfigurationWorkingCopy config) {
		TableItem[] items = tableWidget.getItems();
		Map map = createMapFromTableItems(items);
		String attributeID = (String) tableWidget.getData();
		// no need to update if empty or null and still empty or null.
		// this avoid the nasty "save changes" prompt when there are no changes
		try {
			Map previousMap = config.getAttribute(attributeID, (Map)null);
			if ((previousMap == null || previousMap.isEmpty()) && (map == null || map.isEmpty())) {
				return;
			} 
		} catch (CoreException e) {
			// ignore
		}
		config.setAttribute(attributeID, map);			
	}
	
	protected Map createMapFromTableItems(TableItem[] items) {
		if (items.length == 0) {
			return null;
		}
		Map map = new HashMap(items.length);
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String key = item.getText(0);
			String value = item.getText(1);
			map.put(key, value);
		}		
		return map;
	}
	
	/**
	 * @see JavaLaunchConfigurationTab#updateWidgetsFromConfig(ILaunchConfiguration)
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updateBootPathFromConfig(config);
		updateClassPathFromConfig(config);
	}
	
	protected void updateBootPathFromConfig(ILaunchConfiguration config) {
		try {
			java.util.List bootpath = null;
			if (config != null) {
				bootpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (java.util.List)null);
			}
			updatePathList(bootpath, fBootPathList);
		} catch (CoreException ce) {			
		}
	}
	
	protected void updateClassPathFromConfig(ILaunchConfiguration config) {		
		try {
			java.util.List classpath = null;
			if (config != null) {
				classpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (java.util.List)null);
			}
			if (classpath == null) {
				fClassPathDefaultButton.setSelection(true);
				handleClassPathDefaultButtonSelected();
			} else {
				updatePathList(classpath, fClassPathList);
			}
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);		
		}
	}
			
	protected void updatePathList(java.util.List listStructure, List listWidget) {
		listWidget.removeAll();
		if (listStructure == null) {
			return;
		}
		String[] stringArray = new String[listStructure.size()];
		stringArray = (String[])listStructure.toArray(stringArray);
		listWidget.setItems(stringArray);
	}
	
	protected void updateTable(Map map, Table tableWidget) {
		tableWidget.removeAll();
		if (map == null) {
			return;
		}
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			TableItem tableItem = new TableItem(tableWidget, SWT.NONE);
			tableItem.setText(new String[] {key, value});			
		}
	}
	
	/**
	 * Handle list selection events (for the active list only).
	 */
	protected void handleListSelectionChanged(SelectionEvent evt) {
		List listWidget = (List) evt.widget;
		if (getActiveListWidget() != listWidget) {
			return;
		}		
		setPathButtonsEnableState();
	}

	/**
	 * Show the user a file dialog, and let them choose one or more archive files.
	 * All selected files are added to the end of the active list.
	 */
	protected void handlePathAddArchiveButtonSelected() {
		// Get current list entries, and use first selection as insertion point
		List listWidget = getActiveListWidget();
		java.util.List previousItems = Arrays.asList(listWidget.getItems());
		int[] selectedIndices = listWidget.getSelectionIndices();
		int insertAtIndex = listWidget.getItemCount();
		if (selectedIndices.length > 0) {
			insertAtIndex = selectedIndices[0];
		}
		
		// Show the dialog and get the results
		FileDialog dialog= new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setFilterPath(fLastBrowsedDirectory);
		dialog.setText(LauncherMessages.getString("JavaEnvironmentTab.Select_Archive_File_26")); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] { "*.jar;*.zip"});   //$NON-NLS-1$
		dialog.open();
		String[] results = dialog.getFileNames();
		int resultCount = results.length;
		if ((results == null) || (resultCount < 1)) {
			return;
		}
		
		// Insert the results at the insertion point
		fLastBrowsedDirectory = dialog.getFilterPath();
		int nextInsertionPoint = insertAtIndex;
		for (int i = 0; i < resultCount; i++) {
			String result = results[i];
			if (!previousItems.contains(result)) {
				File file = new File(fLastBrowsedDirectory, result);
				listWidget.add(file.getAbsolutePath(), nextInsertionPoint++);
			}
		}
		if (insertAtIndex != nextInsertionPoint) {
			listWidget.setSelection(insertAtIndex, nextInsertionPoint - 1);
			setPathButtonsEnableState();
		}
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Show the user a directory dialog, and let them choose a directory.
	 * The selected directory is added to the end of the active list.
	 */
	protected void handlePathAddDirectoryButtonSelected() {
		// Get current list entries, and use first selection as insertion point
		List listWidget = getActiveListWidget();
		java.util.List previousItems = Arrays.asList(listWidget.getItems());
		int[] selectedIndices = listWidget.getSelectionIndices();
		int insertAtIndex = listWidget.getItemCount();
		if (selectedIndices.length > 0) {
			insertAtIndex = selectedIndices[0];
		}
		
		// Show the dialog and get the result
		DirectoryDialog dialog= new DirectoryDialog(getShell(), SWT.OPEN);
		dialog.setFilterPath(fLastBrowsedDirectory);
		dialog.setMessage(LauncherMessages.getString("JavaEnvironmentTab.Select_&directory_to_add_to_path__27")); //$NON-NLS-1$
		String result = dialog.open();
		if (result == null) {
			return;
		}
		
		// Insert the result at the insertion point
		if (!previousItems.contains(result)) {
			listWidget.add(result, insertAtIndex);
			listWidget.setSelection(insertAtIndex);
			setPathButtonsEnableState();
			fLastBrowsedDirectory = result;
		}
		
	}

	/**
	 * Remove all selected list items.
	 */
	protected void handlePathRemoveButtonSelected() {
		List listWidget = getActiveListWidget();
		int[] selectedIndices = listWidget.getSelectionIndices();
		listWidget.remove(selectedIndices);
		setPathButtonsEnableState();
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Move the (single) selected list item up or down one position.
	 */
	protected void handlePathMoveButtonSelected(boolean moveUp) {
		List listWidget = getActiveListWidget();
		int selectedIndex = listWidget.getSelectionIndex();
		int targetIndex = moveUp ? selectedIndex -1 : selectedIndex + 1;
		if ((targetIndex < 0) || (targetIndex >= listWidget.getItemCount())) {
			return;
		}
		String targetText = listWidget.getItem(targetIndex);
		String selectedText = listWidget.getItem(selectedIndex);
		listWidget.setItem(targetIndex, selectedText);
		listWidget.setItem(selectedIndex, targetText);
		listWidget.setSelection(targetIndex);
		setPathButtonsEnableState();
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Toggle the state of the class path default checkbox.
	 */
	protected void handleClassPathDefaultButtonSelected() {
		boolean selected = fClassPathDefaultButton.getSelection();
		if (selected) {
			fClassPathList.setEnabled(false);
		} else {
			fClassPathList.setEnabled(true);	
		}

		if (selected) {
			try {
				IJavaProject javaProject = getJavaProject();
				String[] defaultClassPath = new String[0];
				if (javaProject != null) {
					defaultClassPath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
					defaultClassPath = removeRtJarFromClasspath(defaultClassPath);
				}
				fClassPathList.setItems(defaultClassPath);
			} catch (CoreException ce) {
				JDIDebugUIPlugin.log(ce);			
			}
		}
		setPathButtonsEnableState();
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Remove any entry in the argument that corresponds to an 'rt.jar' file.
	 */
	protected String[] removeRtJarFromClasspath(String[] classpath) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < classpath.length; i++) {
			if (classpath[i].endsWith("rt.jar")) { //$NON-NLS-1$
				File file = new File(classpath[i]);
				if ("rt.jar".equals(file.getName())) { //$NON-NLS-1$
					continue;
				}
			}
			list.add(classpath[i]);
		}
		list.trimToSize();
		String[] stringArray = new String[list.size()];
		list.toArray(stringArray);
		return stringArray;
	}
	
	/**
	 * Returns the Java project from the last time this tab
	 * was entered.
	 */
	protected IJavaProject getJavaProject() {
		return fJavaProject;
	}
	
	/**
	 * Set the enabled state of the four path-related buttons based on the
	 * selection in the active List widget.
	 */
	protected void setPathButtonsEnableState() {
		List listWidget = getActiveListWidget();
		boolean useDefault = isUseDefaultClasspath();
		TabItem[] tabSelection = fPathTabFolder.getSelection();
		boolean isClasspathTab = tabSelection != null && tabSelection.length == 1 && tabSelection[0].equals(fClassPathTabItem);
		
		int selectCount = 0;
		if (listWidget != null) {
			selectCount = listWidget.getSelectionIndices().length;
		}
		boolean selection = selectCount > 0;
		boolean singleSelection = selectCount == 1;
		
		boolean firstSelected = false;
		boolean lastSelcted = false;
		if (listWidget != null) {
			int selectedIndex = listWidget.getSelectionIndex();
			firstSelected = selectedIndex == 0;
			lastSelcted = selectedIndex == (listWidget.getItemCount() - 1);
		}
		
		boolean enabledList = (!useDefault || !isClasspathTab) && listWidget != null;
		
		fPathRemoveButton.setEnabled(enabledList && selection);
		fPathMoveUpButton.setEnabled(enabledList && singleSelection && !firstSelected);
		fPathMoveDownButton.setEnabled(enabledList && singleSelection && !lastSelcted);
		fPathAddArchiveButton.setEnabled(enabledList);
		fPathAddDirectoryButton.setEnabled(enabledList);
	
	}
			
	/**
	 * Return the List widget that is currently visible in the paths tab folder.
	 */
	protected List getActiveListWidget() {
		int selectedTabIndex = fPathTabFolder.getSelectionIndex();
		if (selectedTabIndex < 0) {
			return null;
		}
		TabItem tabItem = fPathTabFolder.getItem(selectedTabIndex);
		List listWidget = (List)tabItem.getData();
		return listWidget;
	}
	
	/**
	 * Practice lazy creation the singleton selection adapter for all path lists.
	 */
	protected SelectionAdapter getListSelectionAdapter() {
		if (fListSelectionAdapter == null) {
			fListSelectionAdapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					handleListSelectionChanged(evt);
				}
			};
		}
		return fListSelectionAdapter;
	}
		
	/**
	 * Initialize defaults based on the given java element.
	 */
	protected void initializeDefaults(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {		
		initializeHardCodedDefaults(config);
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeDefaults(javaElement, config);
		} else {
			initializeHardCodedDefaults(config);	
		}
	}
	
	/**
	 * Initialize those attributes whose default values are independent of any context.
	 */
	protected void initializeHardCodedDefaults(ILaunchConfigurationWorkingCopy config) {					
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return isValid();
	}

	/**
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {
		return true;
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setProjectFrom(configuration);
		updateBootPathFromConfig(configuration);
		updateClassPathFromConfig(configuration);
	}
	
	protected void setProjectFrom(ILaunchConfiguration config) {
		try {
			fJavaProject = JavaLaunchConfigurationUtils.getJavaProject(config);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (isUseDefaultClasspath()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
		} else {
			updateConfigFromPathList(fClassPathList, configuration);
		}
		updateConfigFromPathList(fBootPathList, configuration);		
	}
	
	/**
	 * Returns whether the 'use default classpath' button is checked
	 */
	protected boolean isUseDefaultClasspath() {
		return fClassPathDefaultButton.getSelection();
	}


	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.getString("JavaEnvironmentTab.En&vironment_39"); //$NON-NLS-1$
	}	
}
