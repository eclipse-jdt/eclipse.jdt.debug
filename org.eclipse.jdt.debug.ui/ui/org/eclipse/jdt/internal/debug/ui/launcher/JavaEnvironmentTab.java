package org.eclipse.jdt.internal.debug.ui.launcher;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.JavaDebugUI;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Launch configuration tab for local java launches that presents the various paths (bootpath,
 * classpath and extension dirs path) and the environment variables to the user.
 */
public class JavaEnvironmentTab implements ILaunchConfigurationTab {

	// The launch configuration dialog that owns this tab
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	// Flag that when true, prevents the owning dialog's status area from getting updated.
	// Used when multiple config attributes are getting updated at once.
	private boolean fBatchUpdate = false;
	
	// Paths UI widgets
	private TabFolder fPathTabFolder;
	private TabItem fBootPathTabItem;
	private TabItem fClassPathTabItem;
	private TabItem fExtensionPathTabItem;
	private List fBootPathList;
	private List fClassPathList;
	private List fExtensionPathList;
	private Button fPathAddArchiveButton;
	private Button fPathAddDirectoryButton;
	private Button fPathRemoveButton;
	private Button fPathMoveUpButton;
	private Button fPathMoveDownButton;
	
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
	
	// The launch config working copy providing the values shown on this tab
	private ILaunchConfigurationWorkingCopy fWorkingCopy;

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
	
	protected void setLaunchDialog(ILaunchConfigurationDialog dialog) {
		fLaunchConfigurationDialog = dialog;
	}
	
	protected ILaunchConfigurationDialog getLaunchDialog() {
		return fLaunchConfigurationDialog;
	}
	
	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}
	
	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(ILaunchConfigurationDialog, TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);		
		GridData gd;
		
		createVerticalSpacer(comp, 2);
		
		fPathTabFolder = new TabFolder(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		fPathTabFolder.setLayoutData(gd);
		
		fBootPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fBootPathList.setLayoutData(gd);
		fBootPathList.setData(JavaDebugUI.BOOTPATH_ATTR);
		fBootPathList.addSelectionListener(getListSelectionAdapter());
		fBootPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 0);
		fBootPathTabItem.setText("Bootpath");
		fBootPathTabItem.setControl(fBootPathList);
		fBootPathTabItem.setData(fBootPathList);
		
		fClassPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fClassPathList.setLayoutData(gd);
		fClassPathList.setData(JavaDebugUI.CLASSPATH_ATTR);
		fClassPathList.addSelectionListener(getListSelectionAdapter());
		fClassPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 1);
		fClassPathTabItem.setText("Classpath");
		fClassPathTabItem.setControl(fClassPathList);
		fClassPathTabItem.setData(fClassPathList);
		
		fExtensionPathList = new List(fPathTabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		fExtensionPathList.setLayoutData(gd);
		fExtensionPathList.setData(JavaDebugUI.EXTPATH_ATTR);
		fExtensionPathList.addSelectionListener(getListSelectionAdapter());
		fExtensionPathTabItem = new TabItem(fPathTabFolder, SWT.NONE, 2);
		fExtensionPathTabItem.setText("Extension path");
		fExtensionPathTabItem.setControl(fExtensionPathList);
		fExtensionPathTabItem.setData(fExtensionPathList);
		
		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		pathButtonComp.setLayoutData(gd);
		
		createVerticalSpacer(pathButtonComp, 1);
		
		fPathAddArchiveButton = new Button(pathButtonComp, SWT.PUSH);
		fPathAddArchiveButton.setText("Add archive");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathAddArchiveButton.setLayoutData(gd);
		fPathAddArchiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddArchiveButtonSelected();
			}
		});
		
		fPathAddDirectoryButton = new Button(pathButtonComp, SWT.PUSH);
		fPathAddDirectoryButton.setText("Add folder");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathAddDirectoryButton.setLayoutData(gd);
		fPathAddDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathAddDirectoryButtonSelected();
			}
		});
		
		fPathRemoveButton = new Button(pathButtonComp, SWT.PUSH);
		fPathRemoveButton.setText("Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathRemoveButton.setLayoutData(gd);
		fPathRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathRemoveButtonSelected();
			}
		});
		
		fPathMoveUpButton = new Button(pathButtonComp, SWT.PUSH);
		fPathMoveUpButton.setText("Move up");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathMoveUpButton.setLayoutData(gd);
		fPathMoveUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(true);
			}
		});
		
		fPathMoveDownButton = new Button(pathButtonComp, SWT.PUSH);
		fPathMoveDownButton.setText("Move down");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPathMoveDownButton.setLayoutData(gd);
		fPathMoveDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handlePathMoveButtonSelected(false);
			}
		});
		
		setPathButtonsEnableState();
		
		createVerticalSpacer(comp, 2);
		
		fEnvLabel = new Label(comp, SWT.NONE);
		fEnvLabel.setText("Environment variables");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fEnvLabel.setLayoutData(gd);
		
		fEnvTable = new Table(comp, SWT.BORDER | SWT.MULTI);
		fEnvTable.setData(JavaDebugUI.ENVIRONMENT_VARIABLES_ATTR);
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
	
		Composite envButtonComp = new Composite(comp, SWT.NONE);
		GridLayout envButtonLayout = new GridLayout();
		envButtonLayout.marginHeight = 0;
		envButtonLayout.marginWidth = 0;
		envButtonComp.setLayout(envButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		envButtonComp.setLayoutData(gd);
		
		fEnvAddButton = new Button(envButtonComp, SWT.PUSH);
		fEnvAddButton.setText("Add");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fEnvAddButton.setLayoutData(gd);
		fEnvAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvAddButtonSelected();
			}
		});
		
		fEnvEditButton = new Button(envButtonComp, SWT.PUSH);
		fEnvEditButton.setText("Edit");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fEnvEditButton.setLayoutData(gd);
		fEnvEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvEditButtonSelected();
			}
		});
		
		fEnvRemoveButton = new Button(envButtonComp, SWT.PUSH);
		fEnvRemoveButton.setText("Remove");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fEnvRemoveButton.setLayoutData(gd);
		fEnvRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleEnvRemoveButtonSelected();
			}
		});
		
		setEnvButtonsEnableState();
		
		return comp;
	}

	protected void refreshStatus() {
		if (!isBatchUpdate()) {
			getLaunchDialog().refreshStatus();
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	protected void updateConfigFromPathList(List listWidget) {
		if (getWorkingCopy() != null) {
			try {
				String xmlString = getListEntriesAsXML(listWidget);
				String attributeID = (String) listWidget.getData();
				getWorkingCopy().setAttribute(attributeID, xmlString);			
				refreshStatus();
			} catch (IOException ioe) {
			}
		}
	}
	
	/**
	 * Generate & return XML that marks up all of the entries in the specified List widget.
	 */
	protected String getListEntriesAsXML(List listWidget) throws IOException {
		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement(PATH_XML_ENTRIES); 
		doc.appendChild(configRootElement);
		
		// add all children to the document
		String[] listItems = listWidget.getItems();
		for (int i = 0; i < listItems.length; i++) {
			String item = listItems[i];
			Element element = doc.createElement(PATH_XML_ENTRY); 
			element.setAttribute(PATH_XML_PATH, item); 
			configRootElement.appendChild(element);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
		return writer.toString();							
	}

	protected void updateConfigFromEnvTable(Table tableWidget) {
		if (getWorkingCopy() != null) {
			try {
				String xmlString = getTableEntriesAsXML(tableWidget);
				String attributeID = (String) tableWidget.getData();
				getWorkingCopy().setAttribute(attributeID, xmlString);			
				refreshStatus();
			} catch (IOException ioe) {
			}
		}
	}
	
	/**
	 * Generate & return XML that marks up all of the entries in the specified Table widget.
	 */
	protected String getTableEntriesAsXML(Table tableWidget) throws IOException {
		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement(ENV_XML_ENTRIES); 
		doc.appendChild(configRootElement);
		
		// add all children to the document
		TableItem[] tableItems = tableWidget.getItems();		
		for (int i = 0; i < tableItems.length; i++) {
			TableItem item = tableItems[i];
			Element element = doc.createElement(ENV_XML_ENTRY); 
			element.setAttribute(ENV_XML_NAME, item.getText(0)); 
			element.setAttribute(ENV_XML_VALUE, item.getText(1)); 
			configRootElement.appendChild(element);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
		return writer.toString();							
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
		if (launchConfiguration.equals(getWorkingCopy())) {
			return;
		}
		
		setBatchUpdate(true);
		updateWidgetsFromConfig(launchConfiguration);
		setBatchUpdate(false);

		setWorkingCopy(launchConfiguration);
	}

	/**
	 * Set values for all UI widgets in this tab using values kept in the specified
	 * launch configuration.
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updateBootPathFromConfig(config);
		updateClassPathFromConfig(config);
		updateExtensionPathFromConfig(config);
		updateEnvVarsFromConfig(config);
	}
	
	protected void updateBootPathFromConfig(ILaunchConfiguration config) {
		String bootpath = null;
		try {
			bootpath = config.getAttribute(JavaDebugUI.BOOTPATH_ATTR, null);
			Element root = getRootNodeFromAttributeString(bootpath);		
			if (root != null) {
				updatePathListFromXML(root, fBootPathList);
			}
		} catch (CoreException ce) {			
		}
	}
	
	protected void updateClassPathFromConfig(ILaunchConfiguration config) {
		String classpath = null;
		try {
			classpath = config.getAttribute(JavaDebugUI.CLASSPATH_ATTR, null);
			Element root = getRootNodeFromAttributeString(classpath);		
			if (root != null) {
				updatePathListFromXML(root, fClassPathList);
			}
		} catch (CoreException ce) {			
		}		
	}
	
	protected void updateExtensionPathFromConfig(ILaunchConfiguration config) {
		String extpath = null;
		try {
			extpath = config.getAttribute(JavaDebugUI.EXTPATH_ATTR, null);
			Element root = getRootNodeFromAttributeString(extpath);			
			if (root != null) {
				updatePathListFromXML(root, fExtensionPathList);					
			}
		} catch (CoreException ce) {			
		}				
	}
	
	protected void updateEnvVarsFromConfig(ILaunchConfiguration config) {
		String envVars = null;
		try {
			envVars = config.getAttribute(JavaDebugUI.ENVIRONMENT_VARIABLES_ATTR, null);
			Element root = getRootNodeFromAttributeString(envVars);
			if (root != null) {
				updateEnvTableFromXML(root, fEnvTable);
			}
		} catch (CoreException ce) {
		}
	}
	
	/**
	 * Parse the given String as an XML document and return its root node.
	 */
	protected Element getRootNodeFromAttributeString(String attString) throws CoreException {
		if ((attString == null) || (attString.length() < 1)) {
			return null;
		}

		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(attString.getBytes());
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Element root = parser.parse(new InputSource(stream)).getDocumentElement();
			return root;
		} catch (ParserConfigurationException pce) {
			throw createDebugException(MessageFormat.format("{0} occurred while reading attribute XML", new String[]{pce.toString()}), pce);
		} catch (SAXException se) {
			throw createDebugException(MessageFormat.format("{0} occurred while reading attribute XML", new String[]{se.toString()}), se);
		} catch (IOException ioe) {
			throw createDebugException(MessageFormat.format("{0} occurred while reading attribute XML", new String[]{ioe.toString()}), ioe);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ioe) {
					throw createDebugException(MessageFormat.format("{0} occurred while reading attribute XML", new String[]{ioe.toString()}), ioe);					
				}
			}
		}
	}
	
	/**
	 * Set the items of the specified List widget to the path attributes contained in the 
	 * child elements of the specified XML root node.
	 */
	protected void updatePathListFromXML(Element root, List listWidget) throws CoreException {
		if (!root.getNodeName().equalsIgnoreCase(PATH_XML_ENTRIES)) { //$NON-NLS-1$
			throw createDebugException("Invalid path specification - no 'pathEntries' root node",null);
		}
		
		NodeList nodeList = root.getChildNodes();
		int length = nodeList.getLength();
		ArrayList pathList = new ArrayList(length);
		for (int i = 0; i < length; ++i) {
			Node node = nodeList.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				String nodeName = entry.getNodeName();
				if (!nodeName.equalsIgnoreCase(PATH_XML_ENTRY)) { //$NON-NLS-1$
					throw createDebugException("Invalid path specification - " + nodeName + " unrecognized node", null);
				}
				String path = entry.getAttribute(PATH_XML_PATH); //$NON-NLS-1$
				if (path == null) {
					throw createDebugException("Invalid path specification - no 'path' attribute found", null);
				}
				pathList.add(path);
			}
		}
		String[] pathArray = new String[pathList.size()];
		pathList.toArray(pathArray);
		listWidget.setItems(pathArray);
	}
	
	/**
	 * Set the items of the specified Table widget to the name value pairs contained in the
	 * child elements of the specified XML root node.
	 */
	protected void updateEnvTableFromXML(Element root, Table tableWidget) throws CoreException {
		if (!root.getNodeName().equalsIgnoreCase(ENV_XML_ENTRIES)) { //$NON-NLS-1$
			throw createDebugException("Invalid env var specification - no 'envEntries' root node",null);
		}
		
		NodeList nodeList = root.getChildNodes();
		int length = nodeList.getLength();
		HashMap envMap = new HashMap(length);
		for (int i = 0; i < length; ++i) {
			Node node = nodeList.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				String nodeName = entry.getNodeName();
				if (!nodeName.equalsIgnoreCase(ENV_XML_ENTRY)) { //$NON-NLS-1$
					throw createDebugException("Invalid env var specification - " + nodeName + " unrecognized node", null);
				}
				String name = entry.getAttribute(ENV_XML_NAME); //$NON-NLS-1$
				if (name == null) {
					throw createDebugException("Invalid env var specification - no 'name' attribute found", null);
				}
				String value = entry.getAttribute(ENV_XML_VALUE); //$NON-NLS-1$
				if (value == null) {
					throw createDebugException("Invalid env var specification - no 'value' attribute found", null);
				}
				envMap.put(name, value);
			}
		}
		tableWidget.removeAll();
		Iterator iterator = envMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) envMap.get(key);
			TableItem tableItem = new TableItem(tableWidget, SWT.NONE);
			tableItem.setText(new String[] {key, value});
		}
	}
	
	/**
	 * Convenience method to return an instance of DebugException configured with the 
	 * specified message and (possible <code>null</code>) wrappered exception.
	 */
	protected DebugException createDebugException(String message, Exception wrappedException) {
		return new DebugException(
					new Status(
					 Status.ERROR, JDIDebugUIPlugin.getPluginId(),
					 DebugException.REQUEST_FAILED, message, wrappedException 
					)
				);		
	}
	
	protected void setBatchUpdate(boolean update) {
		fBatchUpdate = update;
	}
	
	protected boolean isBatchUpdate() {
		return fBatchUpdate;
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
		dialog.setText("Select archive file");
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
		
		// Update the config
		updateConfigFromPathList(listWidget);
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
		dialog.setMessage("Select directory to add to path");
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
		
		// Update the config
		updateConfigFromPathList(listWidget);
	}

	/**
	 * Remove all selected list items.
	 */
	protected void handlePathRemoveButtonSelected() {
		List listWidget = getActiveListWidget();
		int[] selectedIndices = listWidget.getSelectionIndices();
		listWidget.remove(selectedIndices);
		setPathButtonsEnableState();
		updateConfigFromPathList(listWidget);
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
		updateConfigFromPathList(listWidget);
		listWidget.setSelection(targetIndex);
		setPathButtonsEnableState();
	}
	
	/**
	 * Set the enabled state of the four path-related buttons based on the
	 * selection in the active List widget.
	 */
	protected void setPathButtonsEnableState() {
		List listWidget = getActiveListWidget();
		int selectCount = listWidget.getSelectionIndices().length;
		if (selectCount < 1) {
			fPathRemoveButton.setEnabled(false);
			fPathMoveUpButton.setEnabled(false);
			fPathMoveDownButton.setEnabled(false);
		} else {
			fPathRemoveButton.setEnabled(true);
			if (selectCount == 1) {
				int selectedIndex = listWidget.getSelectionIndex();
				if (selectedIndex == 0) {
					fPathMoveUpButton.setEnabled(false);
				} else {
					fPathMoveUpButton.setEnabled(true);					
				}
				if (selectedIndex == (listWidget.getItemCount() - 1)) {
					fPathMoveDownButton.setEnabled(false);			
				} else {
					fPathMoveDownButton.setEnabled(true);			
				}
			} else {
				fPathMoveUpButton.setEnabled(false);
				fPathMoveDownButton.setEnabled(false);			
			}		
		} 
		fPathAddArchiveButton.setEnabled(true);
		fPathAddDirectoryButton.setEnabled(true);
	}
	
	protected void handleEnvAddButtonSelected() {
		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
												"Add new environment variable", 
												new String[] {"Name", "Value"}, 
												new String[] {"", ""});
		doEnvVarDialog(dialog);
	}
	
	protected void handleEnvEditButtonSelected() {
		TableItem selectedItem = fEnvTable.getSelection()[0];
		String name = selectedItem.getText(0);
		String value = selectedItem.getText(1);
		NameValuePairDialog dialog = new NameValuePairDialog(getShell(), 
												"Edit environment variable", 
												new String[] {"Name", "Value"}, 
												new String[] {name, value});
		doEnvVarDialog(dialog);		
	}
	
	/**
	 * Show the specified dialog and update the env var table based on its results.
	 */
	protected void doEnvVarDialog(NameValuePairDialog dialog) {
		if (dialog.open() != Window.OK) {
			return;
		}
		String[] nameValuePair = dialog.getNameValuePair();
		TableItem tableItem = getTableItemForName(nameValuePair[0]);
		if (tableItem != null) {
			tableItem.setText(1, nameValuePair[1]);
		} else {
			tableItem = new TableItem(fEnvTable, SWT.NONE);
			tableItem.setText(nameValuePair);
		}
		fEnvTable.setSelection(new TableItem[] {tableItem});
		updateConfigFromEnvTable(fEnvTable);		
	}

	protected void handleEnvRemoveButtonSelected() {
		int[] selectedIndices = fEnvTable.getSelectionIndices();
		fEnvTable.remove(selectedIndices);
		updateConfigFromEnvTable(fEnvTable);
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
	 * Convenience method to get the shell.  It is important that the shell be the one 
	 * associated with the launch configuration dialog, and not the active workbench
	 * window.
	 */
	private Shell getShell() {
		return fEnvLabel.getShell();
	}
	
}
