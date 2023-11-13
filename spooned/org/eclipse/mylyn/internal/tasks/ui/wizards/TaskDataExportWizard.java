/**
 * *****************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
/**
 * Wizard for exporting tasklist data files to the file system. This wizard uses a single page: TaskDataExportWizardPage
 *
 * @author Wesley Coelho
 * @author Mik Kersten
 */
public class TaskDataExportWizard extends org.eclipse.jface.wizard.Wizard implements org.eclipse.ui.IExportWizard {
    /**
     * The name of the dialog store's section associated with the task data export wizard
     */
    private static final java.lang.String SETTINGS_SECTION = "org.eclipse.mylyn.tasklist.ui.exportWizard";// $NON-NLS-1$


    private org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage exportPage = null;

    public TaskDataExportWizard() {
        org.eclipse.jface.dialogs.IDialogSettings masterSettings = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getDialogSettings();
        setDialogSettings(getSettingsSection(masterSettings));
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.TaskDataExportWizard_Export);
    }

    /**
     * Finds or creates a dialog settings section that is used to make the dialog control settings persistent
     */
    private org.eclipse.jface.dialogs.IDialogSettings getSettingsSection(org.eclipse.jface.dialogs.IDialogSettings master) {
        org.eclipse.jface.dialogs.IDialogSettings settings = master.getSection(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizard.SETTINGS_SECTION);
        if (settings == null) {
            settings = master.addNewSection(org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizard.SETTINGS_SECTION);
        }
        return settings;
    }

    @java.lang.Override
    public void addPages() {
        exportPage = new org.eclipse.mylyn.internal.tasks.ui.wizards.TaskDataExportWizardPage();
        addPage(exportPage);
    }

    public void init(org.eclipse.ui.IWorkbench workbench, org.eclipse.jface.viewers.IStructuredSelection selection) {
        // no initialization needed
    }

    /**
     * Called when the user clicks finish. Saves the task data. Waits until all overwrite decisions have been made
     * before starting to save files. If any overwrite is canceled, no files are saved and the user must adjust the
     * dialog.
     */
    @java.lang.Override
    public boolean performFinish() {
        java.lang.String destDir = exportPage.getDestinationDirectory();
        final java.io.File destZipFile = new java.io.File((destDir + java.io.File.separator) + org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager.getBackupFileName());
        org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation job = new org.eclipse.mylyn.internal.tasks.ui.util.TaskDataExportOperation(exportPage.getDestinationDirectory(), destZipFile.getName());
        try {
            if (getContainer() != null) {
                getContainer().run(true, true, job);
            } else {
                org.eclipse.ui.progress.IProgressService service = org.eclipse.ui.PlatformUI.getWorkbench().getProgressService();
                service.run(true, true, job);
            }
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e);
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.TaskDataExportWizard_export_failed, status);
        } catch (java.lang.InterruptedException e) {
            // user canceled
        }
        exportPage.saveSettings();
        return true;
    }
}