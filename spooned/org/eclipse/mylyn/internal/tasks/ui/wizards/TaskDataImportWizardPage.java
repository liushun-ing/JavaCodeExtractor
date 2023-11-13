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
import com.ibm.icu.text.DateFormat;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
/**
 * Wizard Page for the Task Data Import Wizard
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 * @author Rob Elves (Adaption to Import wizard)
 */
public class TaskDataImportWizardPage extends org.eclipse.jface.wizard.WizardPage {
    private org.eclipse.swt.widgets.Button browseButtonZip = null;

    private org.eclipse.swt.widgets.Text sourceZipText = null;

    private org.eclipse.swt.widgets.Button importViaBackupButton;

    private org.eclipse.swt.widgets.Button importViaZipButton;

    private org.eclipse.swt.widgets.Table backupFilesTable;

    // Key values for the dialog settings object
    private static final java.lang.String SETTINGS_SAVED = Messages.TaskDataImportWizardPage_Import_Settings_saved;

    private static final java.lang.String SOURCE_ZIP_SETTING = Messages.TaskDataImportWizardPage_Import_Source_zip_file_setting;

    private static final java.lang.String IMPORT_ZIPMETHOD_SETTING = Messages.TaskDataImportWizardPage_Import_method_zip;

    private static final java.lang.String IMPORT_BACKUPMETHOD_SETTING = Messages.TaskDataImportWizardPage_Import_method_backup;

    public TaskDataImportWizardPage() {
        super("org.eclipse.mylyn.tasklist.importPage");// $NON-NLS-1$

        setPageComplete(false);
        setMessage(Messages.TaskDataImportWizardPage_Importing_overwrites_current_tasks_and_repositories, org.eclipse.jface.dialogs.IMessageProvider.WARNING);
        setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.BANNER_IMPORT);
        setTitle(Messages.TaskDataImportWizardPage_Restore_tasks_from_history);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite container = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(3, false);
        layout.verticalSpacing = 15;
        container.setLayout(layout);
        createImportFromZipControl(container);
        createImportFromBackupControl(container);
        addRadioListeners();
        initSettings();
        org.eclipse.jface.dialogs.Dialog.applyDialogFont(container);
        setControl(container);
        setPageComplete(validate());
    }

    private void addRadioListeners() {
        org.eclipse.swt.events.SelectionListener radioListener = new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                browseButtonZip.setEnabled(importViaZipButton.getSelection());
                backupFilesTable.setEnabled(importViaBackupButton.getSelection());
                sourceZipText.setEnabled(importViaZipButton.getSelection());
                controlChanged();
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }
        };
        importViaZipButton.addSelectionListener(radioListener);
        importViaBackupButton.addSelectionListener(radioListener);
    }

    /**
     * Create widgets for specifying the source zip
     */
    private void createImportFromZipControl(org.eclipse.swt.widgets.Composite parent) {
        importViaZipButton = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.RADIO);
        importViaZipButton.setText(Messages.TaskDataImportWizardPage_From_zip_file);
        sourceZipText = new org.eclipse.swt.widgets.Text(parent, org.eclipse.swt.SWT.BORDER);
        sourceZipText.setEditable(true);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).hint(250, org.eclipse.swt.SWT.DEFAULT).applyTo(sourceZipText);
        sourceZipText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                controlChanged();
            }
        });
        browseButtonZip = new org.eclipse.swt.widgets.Button(parent, org.eclipse.swt.SWT.PUSH);
        browseButtonZip.setText(Messages.TaskDataImportWizardPage_Browse_);
        browseButtonZip.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(getShell());
                dialog.setText(Messages.TaskDataImportWizardPage_Zip_File_Selection);
                java.lang.String dir = sourceZipText.getText();
                dialog.setFilterPath(dir);
                dir = dialog.open();
                if ((dir == null) || dir.equals("")) {
                    // $NON-NLS-1$
                    return;
                }
                sourceZipText.setText(dir);
            }
        });
    }

    private void createImportFromBackupControl(org.eclipse.swt.widgets.Composite container) {
        importViaBackupButton = new org.eclipse.swt.widgets.Button(container, org.eclipse.swt.SWT.RADIO);
        importViaBackupButton.setText(Messages.TaskDataImportWizardPage_From_snapshot);
        addBackupFileView(container);
    }

    private void addBackupFileView(org.eclipse.swt.widgets.Composite composite) {
        backupFilesTable = new org.eclipse.swt.widgets.Table(composite, org.eclipse.swt.SWT.BORDER);
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().span(2, org.eclipse.swt.SWT.DEFAULT).grab(true, true).applyTo(backupFilesTable);
        org.eclipse.swt.widgets.TableColumn filenameColumn = new org.eclipse.swt.widgets.TableColumn(backupFilesTable, org.eclipse.swt.SWT.LEFT);
        filenameColumn.setWidth(200);
        java.util.SortedMap<java.lang.Long, java.io.File> backupFilesMap = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getBackupManager().getBackupFiles();
        for (java.lang.Long time : backupFilesMap.keySet()) {
            java.io.File file = backupFilesMap.get(time);
            org.eclipse.swt.widgets.TableItem item = new org.eclipse.swt.widgets.TableItem(backupFilesTable, org.eclipse.swt.SWT.NONE);
            item.setData(file.getAbsolutePath());
            item.setText(com.ibm.icu.text.DateFormat.getDateTimeInstance().format(time));
        }
        backupFilesTable.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @java.lang.Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                controlChanged();
            }
        });
    }

    /**
     * Initializes controls with values from the Dialog Settings object
     */
    protected void initSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        if (settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.SETTINGS_SAVED) == null) {
            importViaZipButton.setSelection(true);
            sourceZipText.setEnabled(true);
            backupFilesTable.setEnabled(false);
        } else {
            // Retrieve previous values from the dialog settings
            importViaZipButton.setSelection(settings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.IMPORT_ZIPMETHOD_SETTING));
            importViaBackupButton.setSelection(settings.getBoolean(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.IMPORT_BACKUPMETHOD_SETTING));
            browseButtonZip.setEnabled(importViaZipButton.getSelection());
            sourceZipText.setEnabled(importViaZipButton.getSelection());
            backupFilesTable.setEnabled(importViaBackupButton.getSelection());
            java.lang.String zipFile = settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.SOURCE_ZIP_SETTING);
            if (zipFile != null) {
                sourceZipText.setText(settings.get(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.SOURCE_ZIP_SETTING));
            }
        }
    }

    @java.lang.Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (importViaZipButton.getSelection()) {
                sourceZipText.setFocus();
            } else {
                importViaBackupButton.setFocus();
            }
        }
        super.setVisible(visible);
    }

    /**
     * Saves the control values in the dialog settings to be used as defaults the next time the page is opened
     */
    public void saveSettings() {
        org.eclipse.jface.dialogs.IDialogSettings settings = getDialogSettings();
        settings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.IMPORT_ZIPMETHOD_SETTING, importViaZipButton.getSelection());
        settings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.IMPORT_BACKUPMETHOD_SETTING, importViaBackupButton.getSelection());
        settings.put(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.SETTINGS_SAVED, org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage.SETTINGS_SAVED);
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
        if (importViaZipButton.getSelection() && sourceZipText.getText().equals("")) {
            // $NON-NLS-1$
            return false;
        }
        if (importViaBackupButton.getSelection() && (backupFilesTable.getSelection().length == 0)) {
            return false;
        }
        return true;
    }

    public java.lang.String getSourceZipFile() {
        if (importViaZipButton.getSelection()) {
            return sourceZipText.getText();
        } else if (backupFilesTable.getSelectionIndex() != (-1)) {
            return ((java.lang.String) (backupFilesTable.getSelection()[0].getData()));
        }
        return Messages.TaskDataImportWizardPage__unspecified_;
    }

    /**
     * For testing only. Sets controls to the specified values
     */
    public void setSource(boolean zip, java.lang.String sourceZip) {
        sourceZipText.setText(sourceZip);
        importViaZipButton.setSelection(zip);
    }
}