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
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Launch configuration tab for local java launches that presents the various paths (bootpath,
 * classpath and extension dirs path) and the environment variables to the user.
 */
public class JavaEnvironmentTab extends JavaLaunchConfigurationTab implements IAddVMDialogRequestor {
	
	// Paths UI widgets
	private TabFolder fPathTabFolder;
	private TabItem fBootPathTabItem;
	private TabItem fClassPathTabItem;
	private TabItem fExtensionPathTabItem;
	private TabItem fJRETabItem;
	private List fBootPathList;
	private List fClassPathList;
	private List fExtensionPathList;
	private Button fClassPathDefaultButton;
	private Button fPathAddArchiveButton;
	private Button fPathAddDirectoryButton;
	private Button fPathRemoveButton;
	private Button fPathMoveUpButton;
	private Button fPathMoveDownButton;
	private Combo fJRECombo;
	private Button fJREAddButton;
	
	// Collections used to populating the JRE Combo box
	private IVMInstallType[] fVMTypes;
	private java.util.List fVMStandins;	
	
	// Environment variables UI widgets
	private Label fEnvLabel;
	private Table fEnvTable;
	private Button fEnvAddButton;
	private Button fEnvEditButton;
	private Button fEnvRemoveButton;
	
	// Remember last directory when browsing for archives or directories
	private String fLastBrowsedDirectory;
	
	// Listener for list selection events
	private SelectionAdapter fListSelectionAdapter;
	
	private static final String EMPTY_STRING = "";

	// Constants used in reading & persisting XML documents containing path entries
	private static final String PATH_XML_ENTRIES = "pathEntries";
	private static final String PATH_XML_ENTRY = "pathEntry";
	private static final String PATH_XML_PATH = "path";
	
	// Constants used in reading & persisting XML documents containing env. vars.
	private static final String ENV_XML_ENTRIES = "envVarEntries";
	private static final String ENV_XML_ENTRY = "envVarEntry";
	private static final String ENV_XML_NAME = "envVarName";
	private static final String ENV_XML_VALUE = "envVarValue";
		
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
		fClassPathTabItem.setText("Class&path");
		fClassPathTabItem.setControl(classPathComp);
		fClassPathTabItem.setData(fClassPathList);
		
		fClassPathDefaultButton = new Button(classPathComp, SWT.CHECK);
		fClassPathDefaultButton.setText("Use defau&lt classpath");
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
		fBootPathTabItem.setText("&Bootpath");
		fBootPathTabItem.setControl(fBootPathList);
		fBootPathTabItem.setData(fBootPathList);
		
		fExtensionPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fExtensionPathList.setLayoutData(gd);
		fExtensionPathList.setData(IJavaLaunchConfigurationConstants.ATTR_EXTPATH);
		fExtensionPathList.addSelectionListener(getListSelectionAdapter());
		fExtensionPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 2);
		fExtensionPathTabItem.setText("E&xtension Path");
		fExtensionPathTabItem.setControl(fExtensionPathList);
		fExtensionPathTabItem.setData(fExtensionPathList);
		
		// JRE
		Composite jreComp = new Composite(fPathTabFolder, SWT.NONE);
		GridLayout jreLayout = new GridLayout();
		jreLayout.numColumns = 2;
		jreLayout.marginHeight = 0;
		jreLayout.marginWidth = 0;
		jreComp.setLayout(jreLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		jreComp.setLayoutData(gd);
		
		createVerticalSpacer(jreComp, 2);
		
		fJRECombo = new Combo(jreComp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fJRECombo.setLayoutData(gd);
		initializeJREComboBox();
		fJRECombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fJREAddButton =createPushButton(jreComp,"N&ew...", null);
		fJREAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleJREAddButtonSelected();
			}
		});
				
		fJRETabItem = new TabItem(fPathTabFolder, SWT.NONE);
		fJRETabItem.setText("&JRE");
		fJRETabItem.setControl(jreComp);
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);
		
		createVerticalSpacer(pathButtonComp, 1);
		
		fPathAddArchiveButton = createPushButton(pathButtonComp,"Add Ja&r...", null);
		fPathAddArchiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddArchiveButtonSelected();
			}
		});
		gd = (GridData)fPathAddArchiveButton.getLayoutData();
		
		fPathAddDirectoryButton = createPushButton(pathButtonComp, "Add &Folder...", null);
		fPathAddDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddDirectoryButtonSelected();
			}
		});
		
		fPathRemoveButton = createPushButton(pathButtonComp,"R&emove", null);
		fPathRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathRemoveButtonSelected();
			}
		});
				
		fPathMoveUpButton = createPushButton(pathButtonComp, "Move &Up", null);
		fPathMoveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(true);
			}
		});
		
		fPathMoveDownButton = createPushButton(pathButtonComp, "Move D&own", null);
		fPathMoveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(false);
			}
		});
		
		setPathButtonsEnableState();
		
		createVerticalSpacer(comp, 2);
		
		fEnvLabel = new Label(comp, SWT.NONE);
		fEnvLabel.setText("Environment &Variables:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fEnvLabel.setLayoutData(gd);
		
		fEnvTable = new Table(comp, SWT.BORDER | SWT.MULTI);
		fEnvTable.setData(IJavaLaunchConfigurationConstants.ATTR_ENVIRONMENT_VARIABLES);
		TableLayout tableLayout = new TableLayout();
		fEnvTable.setLayout(tableLayout);
		gd = new GridData(GridData.FILL_BOTH);
		fEnvTable.setLayoutData(gd);
		TableColumn column1 = new TableColumn(fEnvTable, SWT.NONE);
		column1.setText("Name");
		TableColumn column2 = new TableColumn(fEnvTable, SWT.NONE);
		column2.setText("Value");		
		tableLayout.addColumnData(new ColumnWeightData(100));
		tableLayout.addColumnData(new ColumnWeightData(100));
		fEnvTable.setHeaderVisible(true);
		fEnvTable.setLinesVisible(true);
		fEnvTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setEnvButtonsEnableState();
			}
		});
		fEnvTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				setEnvButtonsEnableState();
				if (fEnvEditButton.isEnabled()) {
					handleEnvEditButtonSelected();
				}
			}
		});
	
		Composite envButtonComp = new Composite(comp, SWT.NONE);
		GridLayout envButtonLayout = new GridLayout();
		envButtonLayout.marginHeight = 0;
		envButtonLayout.marginWidth = 0;
		envButtonComp.setLayout(envButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		envButtonComp.setLayoutData(gd);
		
		fEnvAddButton = createPushButton(envButtonComp ,"A&dd...", null);
		fEnvAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvAddButtonSelected();
			}
		});
		
		fEnvEditButton = createPushButton(envButtonComp, "Ed&it...", null);
		fEnvEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvEditButtonSelected();
			}
		});
		
		fEnvRemoveButton = createPushButton(envButtonComp, "Rem&ove", null);
		fEnvRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvRemoveButtonSelected();
			}
		});
		
		fPathTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setPathButtonsEnableState();
			}			
		});
	
		setEnvButtonsEnableState();
		
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
		updateExtensionPathFromConfig(config);
		updateEnvVarsFromConfig(config);
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
			JDIDebugUIPlugin.log(ce.getStatus());		
		}
	}
	
	protected void updateExtensionPathFromConfig(ILaunchConfiguration config) {		
		try {
			java.util.List extpath = null;
			if (config != null) {
				extpath = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_EXTPATH, (java.util.List)null);
			}
			updatePathList(extpath, fExtensionPathList);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce.getStatus());
		}
	}
	
	protected void updateEnvVarsFromConfig(ILaunchConfiguration config) {
		Map envVars = null;
		try {
			if (config != null) {
				envVars = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ENVIRONMENT_VARIABLES, (Map)null);
			}
			updateTable(envVars, fEnvTable);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce.getStatus());
		}
	}
	
	protected void updatePathList(java.util.List listStructure, List listWidget) {
		if (listStructure == null) {
			return;
		}
		String[] stringArray = new String[listStructure.size()];
		listStructure.toArray(stringArray);
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
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
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
		dialog.setText("Select Archive File");
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
		dialog.setMessage("Select &directory to add to path:");
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
				JDIDebugUIPlugin.log(ce.getStatus());			
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
	 * Returns the Java project for this tab - retrieves
	 * it from the main tab.
	 */
	protected IJavaProject getJavaProject() {
		ILaunchConfigurationTab[] tabs = getLaunchConfigurationDialog().getTabs();
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i] instanceof JavaMainTab) {
				return ((JavaMainTab)tabs[i]).getJavaProject();
			}
		}
		return null;
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
	
	protected void handleEnvAddButtonSelected() {
		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
												"Add Environment Variable", 
												new String[] {"&Name:", "&Value:"}, 
												new String[] {"", ""});
		doEnvVarDialog(dialog, null);
		setEnvButtonsEnableState();
	}
	
	protected void handleEnvEditButtonSelected() {
		TableItem selectedItem = fEnvTable.getSelection()[0];
		String name = selectedItem.getText(0);
		String value = selectedItem.getText(1);
		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
												"Edit Environment Variable", 
												new String[] {"&Name", "&Value"}, 
												new String[] {name, value});
		doEnvVarDialog(dialog, selectedItem);		
	}
	
	/**
	 * Show the specified dialog and update the env var table based on its results.
	 * 
	 * @param updateItem the item to update, or <code>null</code> if
	 *  adding a new item
	 */
	protected void doEnvVarDialog(NameValuePairDialog dialog, TableItem updateItem) {
		if (dialog.open() != Window.OK) {
			return;
		}
		String[] nameValuePair = dialog.getNameValuePair();
		TableItem tableItem = updateItem;
		if (tableItem == null) {
			tableItem = getTableItemForName(nameValuePair[0]);
			if (tableItem == null) {
				tableItem = new TableItem(fEnvTable, SWT.NONE);
			}
		}
		tableItem.setText(nameValuePair);
		fEnvTable.setSelection(new TableItem[] {tableItem});
		updateLaunchConfigurationDialog();	
	}

	protected void handleEnvRemoveButtonSelected() {
		int[] selectedIndices = fEnvTable.getSelectionIndices();
		fEnvTable.remove(selectedIndices);
		setEnvButtonsEnableState();
	}
	
	/**
	 * Set the enabled state of the three environment variable-related buttons based on the
	 * selection in the Table widget.
	 */
	protected void setEnvButtonsEnableState() {
		int selectCount = fEnvTable.getSelectionIndices().length;
		if (selectCount < 1) {
			fEnvEditButton.setEnabled(false);
			fEnvRemoveButton.setEnabled(false);
		} else {
			fEnvRemoveButton.setEnabled(true);
			if (selectCount == 1) {
				fEnvEditButton.setEnabled(true);
			} else {
				fEnvEditButton.setEnabled(false);
			}
		}		
		fEnvAddButton.setEnabled(true);
	}
	
	/**
	 * Helper method that indicates whether the specified env var name is already present 
	 * in the env var table.
	 */
	protected TableItem getTableItemForName(String candidateName) {
		TableItem[] items = fEnvTable.getItems();
		for (int i = 0; i < items.length; i++) {
			String name = items[i].getText(0);
			if (name.equals(candidateName)) {
				return items[i];
			}
		}
		return null;
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
	 * @see ILaunchConfigurationTab#isPageComplete()
	 */
	public boolean isValid() {
		
		setErrorMessage(null);
		setMessage(null);
		
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			IVMInstall vm = vmStandin.convertToRealVM();
			File location = vm.getInstallLocation();
			if (location == null) {
				setErrorMessage("JRE home directory not specified.");
				return false;
			}
			if (!location.exists()) {
				setErrorMessage("JRE home directory does not exist.");
				return false;
			}			
		} else {
			setErrorMessage("JRE not specified.");
			return false;
		}		
		
		return true;
	}
	
	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateBootPathFromConfig(configuration);
		updateClassPathFromConfig(configuration);
		updateEnvVarsFromConfig(configuration);
		updateExtensionPathFromConfig(configuration);
		updateJREFromConfig(configuration);
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		updateConfigFromEnvTable(fEnvTable, configuration);
		if (isUseDefaultClasspath()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (String)null);
		} else {
			updateConfigFromPathList(fClassPathList, configuration);
		}
		updateConfigFromPathList(fBootPathList, configuration);
		updateConfigFromPathList(fExtensionPathList, configuration);
		
		int vmIndex = fJRECombo.getSelectionIndex();
		if (vmIndex > -1) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(vmIndex);
			String vmID = vmStandin.getId();
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, vmID);
			String vmTypeID = vmStandin.getVMInstallType().getId();
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
		}		
	}
	
	/**
	 * Returns whether the 'use default classpath' button is checked
	 */
	protected boolean isUseDefaultClasspath() {
		return fClassPathDefaultButton.getSelection();
	}


	/**
	 * Load the JRE related collections, and use these to set the values on the combo box
	 */
	protected void initializeJREComboBox() {
		fVMTypes= JavaRuntime.getVMInstallTypes();
		fVMStandins= createFakeVMInstalls(fVMTypes);
		populateJREComboBox();		
	}
	
	/**
	 * Show a dialog that lets the user add a new JRE definition
	 */
	protected void handleJREAddButtonSelected() {
		AddVMDialog dialog= new AddVMDialog(this, getShell(), fVMTypes, null);
		dialog.setTitle(LauncherMessages.getString("vmPreferencePage.editJRE.title")); //$NON-NLS-1$
		if (dialog.open() != dialog.OK) {
			return;
		}
	}	
	
	/**
	 * @see IAddVMDialogRequestor#isDuplicateName(IVMInstallType, String)
	 */
	public boolean isDuplicateName(IVMInstallType type, String name) {
		for (int i= 0; i < fVMStandins.size(); i++) {
			IVMInstall vm= (IVMInstall)fVMStandins.get(i);
			if (vm.getVMInstallType() == type) {
				if (vm.getName().equals(name))
					return true;
			}
		}
		return false;
	}

	/**
	 * @see IAddVMDialogRequestor#vmAdded(IVMInstall)
	 */
	public void vmAdded(IVMInstall vm) {
		((VMStandin)vm).convertToRealVM();		
		try {
			JavaRuntime.saveVMConfiguration();
		} catch(CoreException e) {
			JDIDebugUIPlugin.log(e.getStatus());
		}
		fVMStandins.add(vm);
		populateJREComboBox();
		selectJREComboBoxEntry(vm.getId());
	}	
	
	/**
	 * Set the available items on the JRE combo box
	 */
	protected void populateJREComboBox() {
		String[] vmNames = new String[fVMStandins.size()];
		Iterator iterator = fVMStandins.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			VMStandin standin = (VMStandin)iterator.next();
			String vmName = standin.getName();
			vmNames[index] = vmName;
			index++;
		}
		fJRECombo.setItems(vmNames);
	}	
	
	/**
	 * Cause the VM with the specified ID to be selected in the JRE combo box.
	 * This relies on the fact that the items set on the combo box are done so in 
	 * the same order as they in the <code>fVMStandins</code> list.
	 */
	protected void selectJREComboBoxEntry(String vmID) {
		//VMStandin selectedVMStandin = null;
		int index = -1;
		for (int i = 0; i < fVMStandins.size(); i++) {
			VMStandin vmStandin = (VMStandin)fVMStandins.get(i);
			if (vmStandin.getId().equals(vmID)) {
				index = i;
				//selectedVMStandin = vmStandin;
				break;
			}
		}
		if (index > -1) {
			fJRECombo.select(index);
			//fJRECombo.setData(JavaDebugUI.VM_INSTALL_TYPE_ATTR, selectedVMStandin.getVMInstallType().getId());
		} else {
			clearJREComboBoxEntry();
		}
	}	
	
	/**
	 * Convenience method to remove any selection in the JRE combo box
	 */
	protected void clearJREComboBoxEntry() {
		fJRECombo.deselectAll();
	}	
	
	private java.util.List createFakeVMInstalls(IVMInstallType[] vmTypes) {
		ArrayList vms= new ArrayList();
		for (int i= 0; i < vmTypes.length; i++) {
			IVMInstall[] vmInstalls= vmTypes[i].getVMInstalls();
			for (int j= 0; j < vmInstalls.length; j++) 
				vms.add(new VMStandin(vmInstalls[j]));
		}
		return vms;
	}	
	
	protected void updateJREFromConfig(ILaunchConfiguration config) {
		String vmID = null;
		try {
			vmID = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL, EMPTY_STRING);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce.getStatus());			
		}
		if (vmID == null) {
			clearJREComboBoxEntry();
		} else {
			selectJREComboBoxEntry(vmID);
		}
	}	
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "En&vironment";
	}	
}
