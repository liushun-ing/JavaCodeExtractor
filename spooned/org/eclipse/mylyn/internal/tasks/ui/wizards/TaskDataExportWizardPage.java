/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.wizards;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
/**
 * Wizard Page for the Task Data Export Wizard
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Rob Elves
 */
public class TaskDataExportWizardPage extends org.eclipse.jface.wizard.WizardPage {
    private static final java.lang.String PAGE_ID = "org.eclipse.mylyn.tasklist.exportPage";// $NON-NLS-1$


    private org.eclipse.swt.widgets.Button browseButton = null;

    private org.eclipse.swt.widgets.Text destDirText = null;

    // Key values for the dialog settings object
    private static final java.lang.String SETTINGS_SAVED = "Settings saved";// $NON-NLS-1$


    private static final java.lang.String DEST_DIR_SETTING = "Destination directory setting";// $NON-NLS-1$


    public TaskDataExportWizardPage() {
        super(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.PAGE_ID);
        setPageComplete(false);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.BANNER_EXPORT);
        setTitle(Messages.TaskDataExportWizardPage_Export_Mylyn_Task_Data);
    }

    /**
     * Create the widgets on the page
     */
    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        container.setLayout(layout);
        createExportDirectoryControl(container);
        initSettings();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
        setControl(container);
        setPageComplete(validate());
    }

    /**
     * Create widgets for specifying the destination directory
     */
    private void createExportDirectoryControl(org.eclipse.swt.widgets.Composite parent) {
        parent.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));
        parent.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.NONE).setText(Messages.TaskDataExportWizardPage_File);
        org.eclipse.swt.widgets.Label l = new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.NONE);
        l.setText(org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.getBackupFileName());
        org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        l.setLayoutData(gd);
        new org.eclipse.swt.widgets.Label(parent, org.eclipse.swt.SWT.NONE).setText(Messages.TaskDataExportWizardPage_Folder);
        destDirText = new org.eclipse.swt.widgets.Text(parent, org.eclipse.swt.SWT.BORDER);
        destDirText.setEditable(false);
        destDirText.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
        destDirText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                controlChanged();
            }
        });
        browseButton = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.PUSH);
        browseButton.setText(Messages.TaskDataExportWizardPage_Browse_);
        browseButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.swt.widgets.DirectoryDialog dialog = new org.eclipse.swt.widgets.DirectoryDialog(getShell());
                dialog.setText(Messages.TaskDataExportWizardPage_Folder_Selection);
                dialog.setMessage(Messages.TaskDataExportWizardPage_Specify_the_destination_folder_for_task_data);
                java.lang.String dir = destDirText.getText();
                dialog.setFilterPath(dir);
                dir = dialog.open();
                if ((dir == null) || dir.equals("")) {
                    // $NON-NLS-1$
                    return;
                }
                destDirText.setText(dir);
                controlChanged();
            }
        });
    }

    /**
     * Initializes controls with values from the Dialog Settings object
     */
    protected void initSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        if (settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.SETTINGS_SAVED) == null) {
            destDirText.setText("");// $NON-NLS-1$

        } else {
            java.lang.String directory = settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.DEST_DIR_SETTING);
            if (directory != null) {
                destDirText.setText(settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.DEST_DIR_SETTING));
            }
        }
    }

    /**
     * Saves the control values in the dialog settings to be used as defaults the next time the page is opened
     */
    public void saveSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        settings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.DEST_DIR_SETTING, destDirText.getText());
        settings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.SETTINGS_SAVED, org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage.SETTINGS_SAVED);
    }

    /**
     * Called to indicate that a control's value has changed
     */
    public void controlChanged() {
        setPageComplete(validate());
    }

    /**
     * Returns true if the information entered by the user is valid
     */
    protected boolean validate() {
        setMessage(null);
        // Check that a destination dir has been specified
        if (destDirText.getText().equals("")) {
            // $NON-NLS-1$
            setMessage(Messages.TaskDataExportWizardPage_Please_choose_an_export_destination, org.eclipse.core.runtime.IStatus.WARNING);
            return false;
        }
        return true;
    }

    /**
     * Returns the directory where data files are to be saved
     */
    public java.lang.String getDestinationDirectory() {
        return destDirText.getText();
    }

    /**
     * For testing only. Sets controls to the specified values
     */
    public void setDestinationDirectory(java.lang.String destinationDir) {
        destDirText.setText(destinationDir);
    }
}