/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
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
import com.ibm.icu.text.SimpleDateFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.commons.core.ZipFileUtil;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.externalization.AbstractExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Rob Elves
 */
@java.lang.SuppressWarnings("restriction")
public class TaskDataImportWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.IImportWizard {
    private org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage importPage = null;

    public TaskDataImportWizard() {
        org.eclipse.jface.dialogs.IDialogSettings masterSettings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        setDialogSettings(getSettingsSection(masterSettings));
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.TaskDataImportWizard_Import);
    }

    /**
     * Finds or creates a dialog settings section that is used to make the dialog control settings persistent
     */
    private org.eclipse.jface.dialogs.IDialogSettings getSettingsSection(org.eclipse.jface.dialogs.IDialogSettings master) {
        org.eclipse.jface.dialogs.IDialogSettings settings = master.getSection("org.eclipse.mylyn.tasklist.ui.importWizard");// $NON-NLS-1$

        if (settings == null) {
            settings = master.addNewSection("org.eclipse.mylyn.tasklist.ui.importWizard");// $NON-NLS-1$

        }
        return settings;
    }

    @java.lang.Override
    public void addPages() {
        importPage = new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizardPage();
        importPage.setWizard(this);
        addPage(importPage);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
        // no initialization needed
    }

    @java.lang.Override
    public boolean canFinish() {
        return importPage.isPageComplete();
    }

    @java.lang.Override
    public boolean performFinish() {
        java.lang.String sourceZip = importPage.getSourceZipFile();
        final java.io.File sourceZipFile = new java.io.File(sourceZip);
        if (!sourceZipFile.exists()) {
            org.eclipse.jface.dialogs.MessageDialog.openError(getShell(), Messages.TaskDataImportWizard_File_not_found, sourceZipFile.toString() + Messages.TaskDataImportWizard_could_not_be_found);
            return false;
        } else if ((!org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) && (!org.eclipse.jface.dialogs.MessageDialog.openConfirm(getShell(), Messages.TaskDataImportWizard_confirm_overwrite, Messages.TaskDataImportWizard_existing_task_data_about_to_be_erased_proceed))) {
            return false;
        }
        if (org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard.performFinish(sourceZipFile, getContainer())) {
            importPage.saveSettings();
            return true;
        } else {
            return false;
        }
    }

    public static boolean performFinish(final java.io.File sourceZipFile, final org.eclipse.jface.wizard.IWizardContainer container) {
        org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask());
        try {
            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.setEnabled(false);
            if (container != null) {
                org.eclipse.mylyn.commons.ui.CommonUiUtil.run(container, new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard.FileCopyJob(sourceZipFile));
            } else {
                org.eclipse.mylyn.commons.workbench.WorkbenchUtil.busyCursorWhile(new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard.FileCopyJob(sourceZipFile));
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Problems encountered importing task data: {0}", e.getMessage()), e);// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.TaskDataImportWizard_task_data_import_failed, status);
        } catch (org.eclipse.core.runtime.OperationCanceledException e) {
            // canceled
        } finally {
            org.eclipse.mylyn.internal.tasks.ui.workingsets.TaskWorkingSetUpdater.setEnabled(true);
        }
        return true;
    }

    /**
     * Job that performs the file copying and zipping
     */
    static class FileCopyJob implements org.eclipse.mylyn.commons.core.ICoreRunnable {
        private static final java.lang.String PREFIX_BACKUP = ".backup-";// $NON-NLS-1$


        private final java.lang.String JOB_LABEL = Messages.TaskDataImportWizard_Importing_Data;

        private final java.io.File sourceZipFile;

        public FileCopyJob(java.io.File sourceZipFile) {
            this.sourceZipFile = sourceZipFile;
        }

        public void run(final org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
            try {
                boolean hasDefaultTaskList = false;
                int numEntries = 0;
                // determine properties of backup
                java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(sourceZipFile, java.util.zip.ZipFile.OPEN_READ);
                try {
                    java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        java.util.zip.ZipEntry entry = entries.nextElement();
                        if (entry.getName().equals(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.DEFAULT_TASK_LIST_FILE)) {
                            hasDefaultTaskList = true;
                        }
                        numEntries++;
                    } 
                } finally {
                    zipFile.close();
                }
                if (numEntries > 0) {
                    monitor.beginTask(JOB_LABEL, numEntries);
                    org.eclipse.core.runtime.jobs.Job.getJobManager().beginRule(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE, monitor);
                    if (monitor.isCanceled()) {
                        return;
                    }
                    org.eclipse.mylyn.internal.commons.core.ZipFileUtil.unzipFiles(sourceZipFile, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory(), monitor);
                    if (!hasDefaultTaskList) {
                        renameTaskList();
                    }
                    readTaskListData();
                }
            } catch (java.io.IOException e) {
                org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e);
                throw new org.eclipse.core.runtime.CoreException(status);
            } finally {
                org.eclipse.core.runtime.jobs.Job.getJobManager().endRule(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE);
                monitor.done();
            }
        }

        private void readTaskListData() {
            if (!org.eclipse.mylyn.commons.core.CoreUtil.TEST_MODE) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().syncExec(new java.lang.Runnable() {
                    public void run() {
                        try {
                            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().reloadDataDirectory();
                        } catch (org.eclipse.core.runtime.CoreException e) {
                            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.TaskDataImportWizard_Import_Error, e.getStatus());
                        }
                    }
                });
            } else {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().initializeDataSources();
            }
        }

        /**
         * Rename existing task list file to avoid loading that instead of the restored old one.
         */
        private void renameTaskList() {
            com.ibm.icu.text.SimpleDateFormat format = new com.ibm.icu.text.SimpleDateFormat(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.FILENAME_TIMESTAMP_FORMAT, java.util.Locale.ENGLISH);
            java.lang.String date = format.format(new java.util.Date());
            java.io.File taskListFile = new java.io.File(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory(), org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.DEFAULT_TASK_LIST_FILE);
            if (taskListFile.exists()) {
                taskListFile.renameTo(new java.io.File(taskListFile.getParentFile(), (taskListFile.getName() + org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard.FileCopyJob.PREFIX_BACKUP) + date));
            }
            java.io.File taskListFileSnapshot = new java.io.File(org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDataDirectory(), org.eclipse.mylyn.internal.tasks.core.externalization.AbstractExternalizationParticipant.SNAPSHOT_PREFIX + org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.DEFAULT_TASK_LIST_FILE);
            if (taskListFileSnapshot.exists()) {
                taskListFileSnapshot.renameTo(new java.io.File(taskListFile.getParentFile(), (taskListFileSnapshot.getName() + org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataImportWizard.FileCopyJob.PREFIX_BACKUP) + date));
            }
        }
    }
}