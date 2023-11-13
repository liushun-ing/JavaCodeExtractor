/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - layout tweaks
 *     Jeff Pound - modified for attachment input
 *     Chris Aniszczyk <caniszczyk@gmail.com> - bug 20957
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
/**
 * A wizard to input the source of the attachment.
 * <p>
 * Based on org.eclipse.compare.internal.InputPatchPage.
 */
public class AttachmentSourcePage extends org.eclipse.jface.wizard.WizardPage {
    // constants
    protected static final int SIZING_TEXT_FIELD_WIDTH = 250;

    protected static final int COMBO_HISTORY_LENGTH = 5;

    // input constants
    protected static final int CLIPBOARD = 1;

    protected static final int FILE = 2;

    protected static final int WORKSPACE = 3;

    protected static final int SCREENSHOT = 4;

    static final char SEPARATOR = java.lang.System.getProperty("file.separator").charAt(0);// $NON-NLS-1$


    private boolean showError = false;

    // SWT widgets
    private org.eclipse.swt.widgets.Button useClipboardButton;

    // private Button useScreenshotButton;
    private org.eclipse.swt.widgets.Combo fileNameField;

    private org.eclipse.swt.widgets.Button fileBrowseButton;

    private org.eclipse.swt.widgets.Button useFileButton;

    private org.eclipse.swt.widgets.Button useWorkspaceButton;

    private org.eclipse.swt.widgets.Label workspaceSelectLabel;

    private org.eclipse.jface.viewers.TreeViewer treeViewer;

    private java.lang.String clipboardContents;

    private boolean initUseClipboard = false;

    private final java.lang.String DIALOG_SETTINGS = "InputAttachmentSourcePage";// $NON-NLS-1$


    private final java.lang.String S_LAST_SELECTION = "lastSelection";// $NON-NLS-1$


    private final java.lang.String S_FILE_HISTORY = "fileHistory";// $NON-NLS-1$


    private final java.lang.String S_LAST_FILE = "lastFile";// $NON-NLS-1$


    private final org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model;

    /**
     * The last filename that was selected through the browse button.
     */
    private java.lang.String lastFilename;

    public AttachmentSourcePage(org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel model) {
        super("InputAttachmentPage");// $NON-NLS-1$

        this.model = model;
        setTitle(Messages.AttachmentSourcePage_Select_attachment_source);
        setDescription(Messages.AttachmentSourcePage_Clipboard_supports_text_and_image_attachments_only);
        // setMessage("Please select the source for the attachment");
    }

    private void restoreDialogSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            updateWidgetEnablements();
            return;
        }
        java.lang.String selection = settings.get(S_LAST_SELECTION);
        if (selection != null) {
            setInputMethod(java.lang.Integer.valueOf(selection).intValue());
        } else {
            updateWidgetEnablements();
        }
        java.lang.String[] fileNames = settings.getArray(S_FILE_HISTORY);
        if (fileNames != null) {
            // destination
            for (java.lang.String fileName : fileNames) {
                fileNameField.add(fileName);
            }
        }
        lastFilename = settings.get(S_LAST_FILE);
    }

    /* Get a path from the supplied text widget. @return
    org.eclipse.core.runtime.IPath
     */
    protected org.eclipse.core.runtime.IPath getPathFromText(org.eclipse.swt.widgets.Text textField) {
        return new org.eclipse.core.runtime.Path(textField.getText()).makeAbsolute();
    }

    public java.lang.String getAttachmentName() {
        if (getInputMethod() == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD) {
            return Messages.AttachmentSourcePage__Clipboard_;
        } else if (getInputMethod() == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE) {
            return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.getResources(treeViewer.getSelection())[0].getFullPath().toOSString();
        }
        return getAttachmentFilePath();
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        composite.setLayout(new org.eclipse.swt.layout.GridLayout());
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.VERTICAL_ALIGN_FILL | org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL);
        gd.heightHint = 800;
        composite.setLayoutData(gd);
        setControl(composite);
        initializeDialogUnits(parent);
        createAttachmentFileGroup(composite);
        // No error for dialog opening
        showError = false;
        clearErrorMessage();
        restoreDialogSettings();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
    }

    @java.lang.Override
    public org.eclipse.jface.wizard.IWizardPage getNextPage() {
        org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource source = getSource();
        model.setSource(source);
        if (source != null) {
            model.setContentType(source.getContentType());
        }
        saveDialogSettings();
        return super.getNextPage();
    }

    private void saveDialogSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        settings.put(S_LAST_SELECTION, getInputMethod());
        java.lang.String[] fileNames = settings.getArray(S_FILE_HISTORY);
        java.lang.String newFileName = fileNameField.getText().trim();
        if ((getInputMethod() == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE) && (newFileName.length() > 0)) {
            java.util.List<java.lang.String> history = new java.util.ArrayList<java.lang.String>(10);
            history.add(newFileName);
            if (fileNames != null) {
                for (int i = 0; (i < fileNames.length) && (history.size() < org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.COMBO_HISTORY_LENGTH); i++) {
                    if (!newFileName.equals(fileNames[i])) {
                        history.add(fileNames[i]);
                    }
                }
            }
            settings.put(S_FILE_HISTORY, history.toArray(new java.lang.String[0]));
        }
        settings.put(S_LAST_FILE, lastFilename);
    }

    @java.lang.Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    private void setEnableAttachmentFile(boolean enable) {
        fileNameField.setEnabled(enable);
        fileBrowseButton.setEnabled(enable);
    }

    private void setEnableWorkspaceAttachment(boolean enable) {
        workspaceSelectLabel.setEnabled(enable);
        treeViewer.getTree().setEnabled(enable);
    }

    /* Create the group for selecting the attachment file */
    private void createAttachmentFileGroup(org.eclipse.swt.widgets.Composite parent) {
        final org.eclipse.swt.widgets.Composite composite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
        org.eclipse.swt.layout.GridLayout gridLayout = new org.eclipse.swt.layout.GridLayout();
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        // new row
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
        useFileButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.RADIO);
        useFileButton.setText(Messages.AttachmentSourcePage_File);
        fileNameField = new org.eclipse.swt.widgets.Combo(composite, org.eclipse.swt.SWT.BORDER);
        gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
        gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.SIZING_TEXT_FIELD_WIDTH;
        fileNameField.setLayoutData(gd);
        fileNameField.setText("");// $NON-NLS-1$

        fileBrowseButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.PUSH);
        fileBrowseButton.setText(Messages.AttachmentSourcePage_Browse_);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = convertHorizontalDLUsToPixels(org.eclipse.jface.dialogs.IDialogConstants.BUTTON_WIDTH);
        org.eclipse.swt.graphics.Point minSize = fileBrowseButton.computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        data.widthHint = java.lang.Math.max(widthHint, minSize.x);
        fileBrowseButton.setLayoutData(data);
        // new row
        gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 3;
        useClipboardButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.RADIO);
        useClipboardButton.setText(Messages.AttachmentSourcePage_Clipboard);
        useClipboardButton.setLayoutData(gd);
        useClipboardButton.setSelection(initUseClipboard);
        // new row
        useWorkspaceButton = new org.eclipse.swt.widgets.Button(composite, org.eclipse.swt.SWT.RADIO);
        useWorkspaceButton.setText(Messages.AttachmentSourcePage_Workspace);
        gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.HORIZONTAL_ALIGN_BEGINNING);
        useWorkspaceButton.setLayoutData(gd);
        addWorkspaceControls(parent);
        // Add listeners
        useClipboardButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (!useClipboardButton.getSelection()) {
                    return;
                }
                clearErrorMessage();
                showError = true;
                storeClipboardContents();
                updateWidgetEnablements();
            }
        });
        useFileButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (!useFileButton.getSelection()) {
                    return;
                }
                // If there is anything typed in at all
                clearErrorMessage();
                showError = fileNameField.getText() != "";// $NON-NLS-1$

                updateWidgetEnablements();
            }
        });
        fileNameField.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                setSourceName(fileNameField.getText());
                updateWidgetEnablements();
            }
        });
        fileNameField.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                clearErrorMessage();
                showError = true;
                updateWidgetEnablements();
            }
        });
        fileBrowseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                clearErrorMessage();
                showError = true;
                org.eclipse.swt.widgets.FileDialog fileChooser = new org.eclipse.swt.widgets.FileDialog(composite.getShell(), org.eclipse.swt.SWT.OPEN);
                fileChooser.setText(Messages.AttachmentSourcePage_Select_File_Dialog_Title);
                if (fileNameField.getText().trim().length() > 0) {
                    lastFilename = fileNameField.getText().trim();
                }
                fileChooser.setFileName(lastFilename);
                java.lang.String file = fileChooser.open();
                if (file == null) {
                    return;
                }
                // remember the last selected directory
                lastFilename = file;
                fileNameField.setText(file);
                updateWidgetEnablements();
            }
        });
        useWorkspaceButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (!useWorkspaceButton.getSelection()) {
                    return;
                }
                clearErrorMessage();
                // If there is anything typed in at all
                showError = !treeViewer.getSelection().isEmpty();
                updateWidgetEnablements();
            }
        });
        treeViewer.addSelectionChangedListener(new org.eclipse.jface.viewers.ISelectionChangedListener() {
            public void selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent event) {
                clearErrorMessage();
                updateWidgetEnablements();
            }
        });
        treeViewer.addDoubleClickListener(new org.eclipse.jface.viewers.IDoubleClickListener() {
            public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
                org.eclipse.jface.viewers.ISelection selection = event.getSelection();
                if (selection instanceof org.eclipse.jface.viewers.TreeSelection) {
                    org.eclipse.jface.viewers.TreeSelection treeSel = ((org.eclipse.jface.viewers.TreeSelection) (selection));
                    java.lang.Object res = treeSel.getFirstElement();
                    if (res != null) {
                        if ((res instanceof org.eclipse.core.resources.IProject) || (res instanceof org.eclipse.core.resources.IFolder)) {
                            if (treeViewer.getExpandedState(res)) {
                                treeViewer.collapseToLevel(res, 1);
                            } else {
                                treeViewer.expandToLevel(res, 1);
                            }
                        } else if (res instanceof org.eclipse.core.resources.IFile) {
                            getContainer().showPage(getNextPage());
                        }
                    }
                }
            }
        });
        useFileButton.setSelection(!initUseClipboard);
        setEnableWorkspaceAttachment(false);
    }

    private void addWorkspaceControls(org.eclipse.swt.widgets.Composite composite) {
        org.eclipse.swt.widgets.Composite newComp = new org.eclipse.swt.widgets.Composite(composite, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginLeft = 16;// align w/ lable of check button

        newComp.setLayout(layout);
        newComp.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_BOTH));
        workspaceSelectLabel = new org.eclipse.swt.widgets.Label(newComp, org.eclipse.swt.SWT.LEFT);
        workspaceSelectLabel.setText(Messages.AttachmentSourcePage_Select_the_location_of_the_attachment);
        treeViewer = new org.eclipse.jface.viewers.TreeViewer(newComp, org.eclipse.swt.SWT.BORDER);
        treeViewer.getTree().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        treeViewer.setLabelProvider(new org.eclipse.ui.model.WorkbenchLabelProvider());
        treeViewer.setContentProvider(new org.eclipse.ui.model.WorkbenchContentProvider());
        treeViewer.setComparator(new org.eclipse.ui.views.navigator.ResourceComparator(org.eclipse.ui.views.navigator.ResourceComparator.NAME));
        treeViewer.setInput(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot());
    }

    /**
     * Updates the enable state of this page's controls.
     */
    private void updateWidgetEnablements() {
        java.lang.String error = null;
        boolean attachmentFound = false;
        int inputMethod = getInputMethod();
        if (inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD) {
            if (org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource.isSupportedType(getControl().getDisplay())) {
                attachmentFound = true;
            } else {
                error = Messages.AttachmentSourcePage_Clipboard_contains_an_unsupported_data;
            }
        } else if (inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.SCREENSHOT) {
            attachmentFound = true;
        } else if (inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE) {
            java.lang.String path = fileNameField.getText();
            if ((path != null) && (path.length() > 0)) {
                java.io.File file = new java.io.File(path);
                attachmentFound = (file.exists() && file.isFile()) && (file.length() > 0);
                if (!attachmentFound) {
                    error = Messages.AttachmentSourcePage_Cannot_locate_attachment_file;
                }
            } else {
                error = Messages.AttachmentSourcePage_No_file_name;
            }
        } else if (inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE) {
            // Get the selected attachment file (tree will only allow for one
            // selection)
            org.eclipse.core.resources.IResource[] resources = org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.getResources(treeViewer.getSelection());
            if ((resources == null) || (resources.length <= 0)) {
                error = Messages.AttachmentSourcePage_No_file_name;
            } else {
                org.eclipse.core.resources.IResource attachmentFile = resources[0];
                if ((attachmentFile != null) && (attachmentFile.getType() == org.eclipse.core.resources.IResource.FILE)) {
                    java.io.File actualFile = attachmentFile.getRawLocation().toFile();
                    attachmentFound = (actualFile.exists() && actualFile.isFile()) && (actualFile.length() > 0);
                    if (!attachmentFound) {
                        error = Messages.AttachmentSourcePage_Cannot_locate_attachment_file;
                    }
                }
            }
        }
        setPageComplete(attachmentFound);
        if (showError) {
            setErrorMessage(error);
        }
        setEnableAttachmentFile(inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE);
        setEnableWorkspaceAttachment(inputMethod == org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE);
    }

    /**
     * Sets the source name of the import to be the supplied path. Adds the name of the path to the list of items in the
     * source combo and selects it.
     *
     * @param path
     * 		the path to be added
     */
    protected void setSourceName(java.lang.String path) {
        if (path.length() > 0) {
            java.lang.String[] currentItems = fileNameField.getItems();
            int selectionIndex = -1;
            for (int i = 0; i < currentItems.length; i++) {
                if (currentItems[i].equals(path)) {
                    selectionIndex = i;
                }
            }
            if (selectionIndex < 0) {
                // not found in history
                int oldLength = currentItems.length;
                java.lang.String[] newItems = new java.lang.String[oldLength + 1];
                java.lang.System.arraycopy(currentItems, 0, newItems, 0, oldLength);
                newItems[oldLength] = path;
                fileNameField.setItems(newItems);
                selectionIndex = oldLength;
            }
            fileNameField.select(selectionIndex);
            // resetSelection();
        }
    }

    /* Clears the dialog message box */
    private void clearErrorMessage() {
        setErrorMessage(null);
    }

    protected int getInputMethod() {
        if (useClipboardButton == null) {
            if (initUseClipboard) {
                return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD;
            }
            return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE;
        }
        if (useClipboardButton.getSelection()) {
            return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD;
        }
        if (useFileButton.getSelection()) {
            return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE;
        }
        return org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE;
    }

    protected void setInputMethod(int input) {
        switch (input) {
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE :
                useWorkspaceButton.setSelection(true);
                useClipboardButton.setSelection(false);
                useFileButton.setSelection(false);
                break;
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD :
                storeClipboardContents();
                useClipboardButton.setSelection(true);
                useFileButton.setSelection(false);
                useWorkspaceButton.setSelection(false);
                break;
            default :
                useFileButton.setSelection(true);
                useWorkspaceButton.setSelection(false);
                useClipboardButton.setSelection(false);
                break;
        }
        updateWidgetEnablements();
    }

    private java.lang.String getAttachmentFilePath() {
        if (fileNameField != null) {
            return fileNameField.getText();
        }
        return null;
    }

    public java.lang.String getAbsoluteAttachmentPath() {
        switch (getInputMethod()) {
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD :
                return Messages.AttachmentSourcePage__Clipboard_;
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.SCREENSHOT :
                return Messages.AttachmentSourcePage__Screenshot_;
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE :
                org.eclipse.core.resources.IResource[] resources = org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.getResources(treeViewer.getSelection());
                if ((resources.length > 0) && (resources[0].getRawLocation() != null)) {
                    return resources[0].getRawLocation().toOSString();
                } else {
                    return null;
                }
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.FILE :
            default :
                return getAttachmentFilePath();
        }
    }

    /* Based on .eclipse.compare.internal.Utilities

    Convenience method: extract all accessible <code>IResources</code> from
    given selection. Never returns null.
     */
    public static org.eclipse.core.resources.IResource[] getResources(org.eclipse.jface.viewers.ISelection selection) {
        java.util.ArrayList<org.eclipse.core.resources.IResource> tmp = new java.util.ArrayList<org.eclipse.core.resources.IResource>();
        java.lang.Class<?> type = org.eclipse.core.resources.IResource.class;
        if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
            java.lang.Object[] s = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).toArray();
            for (java.lang.Object o : s) {
                org.eclipse.core.resources.IResource resource = null;
                if (type.isInstance(o)) {
                    resource = ((org.eclipse.core.resources.IResource) (o));
                } else if (o instanceof org.eclipse.core.resources.mapping.ResourceMapping) {
                    try {
                        org.eclipse.core.resources.mapping.ResourceTraversal[] travs = ((org.eclipse.core.resources.mapping.ResourceMapping) (o)).getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext.LOCAL_CONTEXT, null);
                        if (travs != null) {
                            for (org.eclipse.core.resources.mapping.ResourceTraversal trav : travs) {
                                org.eclipse.core.resources.IResource[] resources = trav.getResources();
                                for (org.eclipse.core.resources.IResource resource2 : resources) {
                                    if (type.isInstance(resource2) && resource2.isAccessible()) {
                                        tmp.add(resource2);
                                    }
                                }
                            }
                        }
                    } catch (org.eclipse.core.runtime.CoreException ex) {
                        // TODO handle error
                    }
                } else if (o instanceof org.eclipse.core.runtime.IAdaptable) {
                    org.eclipse.core.runtime.IAdaptable a = ((org.eclipse.core.runtime.IAdaptable) (o));
                    java.lang.Object adapter = a.getAdapter(org.eclipse.core.resources.IResource.class);
                    if (type.isInstance(adapter)) {
                        resource = ((org.eclipse.core.resources.IResource) (adapter));
                    }
                }
                if ((resource != null) && resource.isAccessible()) {
                    tmp.add(resource);
                }
            }
        }
        return tmp.toArray(new org.eclipse.core.resources.IResource[tmp.size()]);
    }

    private void storeClipboardContents() {
        org.eclipse.swt.widgets.Control c = getControl();
        if (c != null) {
            org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(c.getDisplay());
            java.lang.Object o = clipboard.getContents(org.eclipse.swt.dnd.TextTransfer.getInstance());
            clipboard.dispose();
            if (o instanceof java.lang.String) {
                clipboardContents = ((java.lang.String) (o)).trim();
            }
        }
    }

    public java.lang.String getClipboardContents() {
        return clipboardContents;
    }

    public void setClipboardContents(java.lang.String attachContents) {
        clipboardContents = attachContents;
    }

    public void setUseClipboard(boolean b) {
        if (useClipboardButton != null) {
            useClipboardButton.setSelection(b);
        }
        initUseClipboard = b;
    }

    @java.lang.Override
    protected org.eclipse.jface.dialogs.IDialogSettings getDialogSettings() {
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin plugin = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault();
        org.eclipse.jface.dialogs.IDialogSettings settings = plugin.getDialogSettings();
        org.eclipse.jface.dialogs.IDialogSettings section = settings.getSection(DIALOG_SETTINGS);
        if (section == null) {
            section = settings.addNewSection(DIALOG_SETTINGS);
        }
        return section;
    }

    public org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource getSource() {
        switch (getInputMethod()) {
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.CLIPBOARD :
                return new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.ClipboardTaskAttachmentSource();
            case org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.WORKSPACE :
                org.eclipse.core.resources.IResource[] resources = org.eclipse.mylyn.internal.tasks.ui.wizards.AttachmentSourcePage.getResources(treeViewer.getSelection());
                if (resources.length > 0) {
                    return new org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource(resources[0].getLocation().toFile());
                } else {
                    return null;
                }
            default :
                // FILE
                return new org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource(new java.io.File(getAttachmentFilePath()));
        }
    }
}