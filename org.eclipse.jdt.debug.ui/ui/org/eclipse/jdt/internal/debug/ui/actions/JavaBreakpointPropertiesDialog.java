package org.eclipse.jdt.internal.debug.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaExceptionBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class JavaBreakpointPropertiesDialog extends Dialog implements IPreferencePageContainer {

	/**
	 * Layout for the page container.
	 *
	 * @see #pageContainer
	 */
	private class PageLayout extends Layout {
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control [] children = composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x= fMinimumPageSize.x;
			int y= fMinimumPageSize.y;
			
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				Point size= children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x= Math.max(x, size.x);
				y= Math.max(y, size.y);
			}				
			if (wHint != SWT.DEFAULT) x = wHint;
			if (hHint != SWT.DEFAULT) y = hHint;
			return new Point(x, y);
		}	
	}

	private Composite fTitleArea;
	private Label fTitleImage;
	private CLabel fMessageLabel;
	private Color fTitleAreaColor;
	
	private String fMessage;
	private Color fNormalMsgAreaBackground;
	private Color fErrorMsgAreaBackground;
	private Image fErrorMsgImage;
	
	private JavaBreakpointPreferencePage fPage;
	
	private Button fOkButton;
	
	public static final String PREF_DLG_TITLE_IMG = "breakpoint_preference_dialog_title_image";//$NON-NLS-1$
	public static final String PREF_DLG_IMG_TITLE_ERROR = "breakpoint_preference_dialog_title_error_image";//$NON-NLS-1$
	static {
		ImageRegistry reg = JDIDebugUIPlugin.getDefault().getImageRegistry();
		reg.put(PREF_DLG_TITLE_IMG, ImageDescriptor.createFromFile(PreferenceDialog.class, "images/pref_dialog_title.gif"));//$NON-NLS-1$
		reg.put(PREF_DLG_IMG_TITLE_ERROR, ImageDescriptor.createFromFile(PreferenceDialog.class, "images/title_error.gif"));//$NON-NLS-1$
	}
	private static final RGB ERROR_BACKGROUND_RGB = new RGB(230, 226, 221);
	
	/**
	 * The Composite in which a page is shown.
	 */
	private Composite fPageContainer;

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize(Point)
	 */
	private Point fMinimumPageSize = new Point(200,200);
	
	private IJavaBreakpoint fBreakpoint;
	
	/**
	 * A preference store that presents the state of the properties
	 * of a Java breakpoint.
	 */
	protected class JavaBreakpointPreferenceStore implements IPreferenceStore {
		
		protected final static String ENABLED= "ENABLED";
		protected final static String HIT_COUNT= "HIT_COUNT";
		protected final static String HIT_COUNT_ENABLED= "HIT_COUNT_ENABLED";
		protected final static String PERSISTED= "PERSISTED";
		protected final static String SUSPEND_POLICY= "SUSPEND_POLICY";
		protected final static String METHOD_EXIT= "METHOD_EXIT";
		protected final static String METHOD_ENTRY= "METHOD_ENTRY";
		protected final static String CAUGHT= "CAUGHT";
		protected final static String UNCAUGHT= "UNCAUGHT";
		protected final static String ACCESS= "ACCESS";
		protected final static String MODIFICATION= "MODIFICATION";
		
		protected HashMap fProperties;
		private boolean fIsDirty= false; 
		
		private ListenerList fListeners;
		
		protected JavaBreakpointPreferenceStore() {
			fProperties= new HashMap(10);
			fListeners= new ListenerList();
		}
		/**
		 * @see IPreferenceStore#addPropertyChangeListener(IPropertyChangeListener)
		 */
		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			fListeners.add(listener);
		}

		/**
		 * @see IPreferenceStore#contains(String)
		 */
		public boolean contains(String name) {
			return fProperties.containsKey(name);
		}

		/**
		 * @see IPreferenceStore#firePropertyChangeEvent(String, Object, Object)
		 */
		public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
			Object[] listeners = fListeners.getListeners();
			// Do we need to fire an event.
			if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
				PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
				for (int i = 0; i < listeners.length; ++i) {
					IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
					l.propertyChange(pe);
				}
			}
		}

		/**
		 * @see IPreferenceStore#getBoolean(String)
		 */
		public boolean getBoolean(String name) {
			Object b= fProperties.get(name);
			if (b instanceof Boolean) {
				return ((Boolean)b).booleanValue();
			}
			return false;
		}

		/**
		 * @see IPreferenceStore#getDefaultBoolean(String)
		 */
		public boolean getDefaultBoolean(String name) {
			return false;
		}

		/**
		 * @see IPreferenceStore#getDefaultDouble(String)
		 */
		public double getDefaultDouble(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getDefaultFloat(String)
		 */
		public float getDefaultFloat(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getDefaultInt(String)
		 */
		public int getDefaultInt(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getDefaultLong(String)
		 */
		public long getDefaultLong(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getDefaultString(String)
		 */
		public String getDefaultString(String name) {
			return null;
		}

		/**
		 * @see IPreferenceStore#getDouble(String)
		 */
		public double getDouble(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getFloat(String)
		 */
		public float getFloat(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getInt(String)
		 */
		public int getInt(String name) {
			Object i= fProperties.get(name);
			if (i instanceof Integer) {
				return ((Integer)i).intValue();
			}
			return 1;
		}

		/**
		 * @see IPreferenceStore#getLong(String)
		 */
		public long getLong(String name) {
			return 0;
		}

		/**
		 * @see IPreferenceStore#getString(String)
		 */
		public String getString(String name) {
			Object str= fProperties.get(name);
			if (str instanceof String) {
				return (String)str;
			}
			return null;
		}

		/**
		 * @see IPreferenceStore#isDefault(String)
		 */
		public boolean isDefault(String name) {
			return false;
		}

		/**
		 * @see IPreferenceStore#needsSaving()
		 */
		public boolean needsSaving() {
			return fIsDirty;
		}

		/**
		 * @see IPreferenceStore#putValue(String, String)
		 */
		public void putValue(String name, String newValue) {
			Object oldValue= fProperties.get(name);
			if (oldValue == null || !oldValue.equals(newValue)) {
				fProperties.put(name, newValue);
				setDirty(true);
			}
		}

		/**
		 * @see IPreferenceStore#removePropertyChangeListener(IPropertyChangeListener)
		 */
		public void removePropertyChangeListener(IPropertyChangeListener listener) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, boolean)
		 */
		public void setDefault(String name, boolean value) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, double)
		 */
		public void setDefault(String name, double value) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, float)
		 */
		public void setDefault(String name, float value) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, int)
		 */
		public void setDefault(String name, int value) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, long)
		 */
		public void setDefault(String name, long value) {
		}

		/**
		 * @see IPreferenceStore#setDefault(String, String)
		 */
		public void setDefault(String name, String defaultObject) {
		}

		/**
		 * @see IPreferenceStore#setToDefault(String)
		 */
		public void setToDefault(String name) {
		}

		/**
		 * @see IPreferenceStore#setValue(String, boolean)
		 */
		public void setValue(String name, boolean newValue) {
			boolean oldValue= getBoolean(name);
			if (oldValue != newValue) {
				fProperties.put(name,new Boolean(newValue));
				setDirty(true);
				firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(newValue));
			}
		}

		/**
		 * @see IPreferenceStore#setValue(String, double)
		 */
		public void setValue(String name, double value) {
			
		}

		/**
		 * @see IPreferenceStore#setValue(String, float)
		 */
		public void setValue(String name, float value) {
		}

		/**
		 * @see IPreferenceStore#setValue(String, int)
		 */
		public void setValue(String name, int newValue) {
			int oldValue= getInt(name);
			if (oldValue != newValue) {
				fProperties.put(name,new Integer(newValue));
				setDirty(true);
				firePropertyChangeEvent(name, new Integer(oldValue), new Integer(newValue));
			}
		}

		/**
		 * @see IPreferenceStore#setValue(String, long)
		 */
		public void setValue(String name, long value) {
		}

		/**
		 * @see IPreferenceStore#setValue(String, String)
		 */
		public void setValue(String name, String newValue) {
			Object oldValue= fProperties.get(name);
			if (oldValue == null || !oldValue.equals(newValue)) {
				fProperties.put(name, newValue);
				setDirty(true);
				firePropertyChangeEvent(name, oldValue, newValue);
			}
		}

		protected void setDirty(boolean isDirty) {
			fIsDirty = isDirty;
		}
}
	
	private JavaBreakpointPreferenceStore fJavaBreakpointPreferenceStore;
	
	protected JavaBreakpointPropertiesDialog(Shell parentShell, IJavaBreakpoint breakpoint) {
		super(parentShell);
		setBreakpoint(breakpoint);
		fJavaBreakpointPreferenceStore= new JavaBreakpointPreferenceStore();
	}

	/**
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	/**
	 * @see Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
	}

	/**
	 * @see Window#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}

	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		final List changedProperties= new ArrayList(5);
		getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {
			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				changedProperties.add(event.getProperty());
			}
		});
		fPage.performOk();
		setBreakpointProperties(changedProperties);
		super.okPressed();
	}
	
	protected void setBreakpointProperties(final List changedProperties) {
		IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
		boolean newEnabled= false;
		IJavaBreakpoint breakpoint= getBreakpoint();
		Iterator changed= changedProperties.iterator();
		while (changed.hasNext()) {
			String property = (String) changed.next();
			switch (property.charAt(0)) {
				case 'A': //access
					IJavaWatchpoint jWatchpoint= (IJavaWatchpoint)breakpoint;
					jWatchpoint.setAccess(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.ACCESS));
					break;
				case 'C': //caught
					IJavaExceptionBreakpoint jeBreakpoint= (IJavaExceptionBreakpoint)breakpoint;
					jeBreakpoint.setCaught(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.CAUGHT));
					break;
				case 'E'://enabled
					newEnabled= true;
					break;
				case 'H':
					if (property.charAt(property.length() - 1) == 'T') {
						//hitcount
						breakpoint.setHitCount(getPreferenceStore().getInt(JavaBreakpointPreferenceStore.HIT_COUNT));
					} else {
						if (getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.HIT_COUNT_ENABLED)) {
							 if (!changedProperties.contains(JavaBreakpointPreferenceStore.HIT_COUNT)) {
								//enabled the hit count but did not change the hit count value
								breakpoint.setHitCount(getPreferenceStore().getInt(JavaBreakpointPreferenceStore.HIT_COUNT));
							}
						} else {
							//disable the hitCount
							breakpoint.setHitCount(0);	
						}
					}
					break;
				case 'M':
					char lastChar= property.charAt(property.length() - 1);
					if (lastChar == 'T') {
						//exit
						IJavaMethodBreakpoint jmBreakpoint= (IJavaMethodBreakpoint)breakpoint;
						jmBreakpoint.setExit(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.METHOD_EXIT));
					} else if (lastChar == 'Y') {
						//entry
						IJavaMethodBreakpoint jmBreakpoint= (IJavaMethodBreakpoint)breakpoint;
						jmBreakpoint.setEntry(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.METHOD_ENTRY));
					} else {
						jWatchpoint= (IJavaWatchpoint)breakpoint;
						jWatchpoint.setModification(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.MODIFICATION));
					}
					break;
				case 'P':
					breakpoint.setPersisted(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.PERSISTED));
					break;
				case 'S':
					String value= getPreferenceStore().getString(JavaBreakpointPreferenceStore.SUSPEND_POLICY);
					if (value.equals(JavaBreakpointPreferencePage.VM_SUSPEND_POLICY)) {
						breakpoint.setSuspendPolicy(IJavaBreakpoint.SUSPEND_VM);
					} else {
						breakpoint.setSuspendPolicy(IJavaBreakpoint.SUSPEND_THREAD);
					}
					break;
				case 'U':
					jeBreakpoint= (IJavaExceptionBreakpoint)breakpoint;
					jeBreakpoint.setUncaught(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.UNCAUGHT));
					break;
			}
			
		}
		if (newEnabled) {
			//some of the other attributes auto enable the breakpoint
			//ensure that the breakpoint is set as the user specified
			breakpoint.setEnabled(getPreferenceStore().getBoolean(JavaBreakpointPreferenceStore.ENABLED));
		}
		}};
		
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException ce) {
			JDIDebugUIPlugin.logError(ce);
		}	
		}
	/**
	 * Sets the title for this dialog.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		Shell shell= getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(title);
		}
	}
	
	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite composite = (Composite)super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 1;
		
		// Build the title area and separator line
		Composite titleComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createTitleArea(titleComposite);
	
		Label titleBarSeparator = new Label(titleComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		titleBarSeparator.setLayoutData(gd);
	
		// Build the Page container
		fPageContainer = createPageContainer(composite, 2);
		fPageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fPageContainer.setFont(parent.getFont());

		fPage= new JavaBreakpointPreferencePage(getBreakpoint());
		fPage.setContainer(this);
		fPage.createControl(fPageContainer);
			
		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
	
		return composite;
	}
	
	/**
	 * Creates the wizard's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created title area composite
	 */
	private Composite createTitleArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		fTitleArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 2;
		
		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		fTitleArea.setLayout(layout);
		fTitleArea.setLayoutData(layoutData);
		fTitleArea.setBackground(bg);
	
		// Add a dispose listener
		fTitleArea.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (fTitleAreaColor != null)
					fTitleAreaColor.dispose();
				if (fErrorMsgAreaBackground != null)
					fErrorMsgAreaBackground.dispose();
			}
		});
	
	
		// Message label
		fMessageLabel = new CLabel(fTitleArea, SWT.LEFT);
		fMessageLabel.setBackground(bg);
		fMessageLabel.setText(" ");//$NON-NLS-1$
		fMessageLabel.setFont(JFaceResources.getBannerFont());
		
		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(JFaceResources.BANNER_FONT.equals(event.getProperty()) ||
					JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
					updateMessage();
				}
			}
		};
		
		fMessageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});
		
		JFaceResources.getFontRegistry().addListener(fontListener);
		
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		fMessageLabel.setLayoutData(gd);
	
		// Title image
		fTitleImage = new Label(fTitleArea, SWT.LEFT);
		fTitleImage.setBackground(bg);
		fTitleImage.setImage(JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_TITLE_IMG));
		gd = new GridData(); 
		gd.horizontalAlignment = gd.END;
		fTitleImage.setLayoutData(gd);
	
		return fTitleArea;
	}

	/**
	 * Creates the inner page container.
	 */
	private Composite createPageContainer(Composite parent, int numColumns) {
		Composite result = new Composite(parent, SWT.NULL);
		result.setLayout(new PageLayout());
		return result;
	}

	/**
	 * Sets the minimum page size.
	 *
	 * @param size the page size encoded as
	 *   <code>new Point(width,height)</code>
	 * @see #setMinimumPageSize(int,int)
	 */
	public void setMinimumPageSize(Point size) {
		fMinimumPageSize.x = size.x;
		fMinimumPageSize.y = size.y;
	}
	
	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param errorMessage the errorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null) {
			if (fMessageLabel.getImage() != null) {
				// we were previously showing an error
				fMessageLabel.setBackground(fNormalMsgAreaBackground);
				fMessageLabel.setImage(null);
				fTitleImage.setImage(JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_TITLE_IMG));
				fTitleArea.layout(true);
			}
	
			// show the message
			setMessage(fMessage);
	
		} else {
			fMessageLabel.setText(errorMessage);
			if (fMessageLabel.getImage() == null) {
				// we were not previously showing an error
							
				// lazy initialize the error background color and image
				if (fErrorMsgAreaBackground == null) {
					fErrorMsgAreaBackground = new Color(fMessageLabel.getDisplay(), ERROR_BACKGROUND_RGB);
					fErrorMsgImage = JDIDebugUIPlugin.getDefault().getImageRegistry().get(PREF_DLG_IMG_TITLE_ERROR);
				}
	
				// show the error	
				fNormalMsgAreaBackground = fMessageLabel.getBackground();
				fMessageLabel.setBackground(fErrorMsgAreaBackground);
				fMessageLabel.setImage(fErrorMsgImage);
				fTitleImage.setImage(null);
				fTitleArea.layout(true);
			}
		}
	}
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String newMessage) {
		fMessage = newMessage;
		if (fMessage == null) {
			fMessage = "";//$NON-NLS-1$
		}
		if (fMessageLabel.getImage() == null) {
			// we are not showing an error
			fMessageLabel.setText(fMessage);
		}
	}
	
	
	public void updateMessage() {
		String pageMessage = fPage.getMessage();
		String pageErrorMessage = fPage.getErrorMessage();

		// Adjust the font
		if (pageMessage == null && pageErrorMessage == null)
			fMessageLabel.setFont(JFaceResources.getBannerFont());
		else
			fMessageLabel.setFont(JFaceResources.getDialogFont());

		// Set the message and error message	
		if (pageMessage == null) {
			setMessage(fPage.getTitle());
		} else {
			setMessage(pageMessage);
		}
		setErrorMessage(pageErrorMessage);
	}
	
	protected IJavaBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	protected void setBreakpoint(IJavaBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	/**
	 * @see IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		return fJavaBreakpointPreferenceStore;
	}

	/**
	 * @see IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		if (fOkButton != null) {
			fOkButton.setEnabled(fPage.isValid());
		}
	}

	/**
	 * @see IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		setTitle(fPage.getTitle());
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
