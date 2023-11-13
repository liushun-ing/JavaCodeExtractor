/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.migrator;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskDataSnapshotOperation;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForRepository;
public class ConnectorMigrationUi {
    private static final java.lang.String MIGRATE = "migrate";// $NON-NLS-1$


    private static final java.lang.String COMLETE_MIGRATION = "complete-migration";// $NON-NLS-1$


    private final org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView;

    private final org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager backupManager;

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.TasksState tasksState;

    public ConnectorMigrationUi(org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.internal.tasks.ui.TaskListBackupManager backupManager, org.eclipse.mylyn.internal.tasks.ui.migrator.TasksState tasksState) {
        this.taskListView = taskListView;
        this.backupManager = backupManager;
        this.tasksState = tasksState;
    }

    /**
     *
     * @noextend This class is not intended to be subclassed by clients.
     * @noinstantiate This class is not intended to be instantiated by clients.
     */
    protected class CompleteMigrationJob extends org.eclipse.core.runtime.jobs.Job {
        private boolean finishedCompleteMigrationWizard;

        private final org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator;

        private CompleteMigrationJob(java.lang.String name, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
            super(name);
            this.migrator = migrator;
        }

        @java.lang.Override
        protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
            if (!finishedCompleteMigrationWizard) {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    @java.lang.Override
                    public void run() {
                        if ((taskListView != null) && (taskListView.getServiceMessageControl() != null)) {
                            org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message = new org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage("") {
                                // $NON-NLS-1$
                                @java.lang.Override
                                public boolean openLink(java.lang.String link) {
                                    if (link.equals(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.COMLETE_MIGRATION)) {
                                        if (createCompleteMigrationWizard(migrator).open() == org.eclipse.jface.window.Window.OK) {
                                            finishedCompleteMigrationWizard = true;
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            };
                            message.setTitle(Messages.ConnectorMigrationUi_Connector_Migration);
                            message.setDescription(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_complete_migration_prompt_title, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.COMLETE_MIGRATION));
                            message.setImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_WARNING);
                            taskListView.getServiceMessageControl().setMessage(message);
                        }
                    }
                });
                schedule(java.util.concurrent.TimeUnit.MILLISECONDS.convert(getCompletionPromptFrequency(), java.util.concurrent.TimeUnit.SECONDS));
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }

        public void dispose() {
            finishedCompleteMigrationWizard = true;
        }
    }

    public void promptToMigrate(final org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
            @java.lang.Override
            public void run() {
                if ((taskListView != null) && (taskListView.getServiceMessageControl() != null)) {
                    org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage message = new org.eclipse.mylyn.internal.commons.notifications.feed.ServiceMessage("") {
                        // $NON-NLS-1$
                        @java.lang.Override
                        public boolean openLink(java.lang.String link) {
                            if (link.equals(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.MIGRATE)) {
                                if (createMigrationWizard(migrator).open() == org.eclipse.jface.window.Window.OK) {
                                    createPromptToCompleteMigrationJob(migrator).schedule();
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                    message.setTitle(Messages.ConnectorMigrationUi_End_of_Connector_Support);
                    message.setDescription(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_complete_migration_prompt_message, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.MIGRATE));
                    message.setImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO);
                    taskListView.getServiceMessageControl().setMessage(message);
                }
            }
        });
    }

    protected org.eclipse.core.runtime.jobs.Job createPromptToCompleteMigrationJob(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        org.eclipse.core.runtime.jobs.Job job = new org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.CompleteMigrationJob(Messages.ConnectorMigrationUi_Complete_Connector_Migration_Prompt, migrator);
        job.setUser(false);
        job.setSystem(true);
        return job;
    }

    protected org.eclipse.jface.wizard.WizardDialog createMigrationWizard(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        return createWizardDialog(new org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationWizard(migrator));
    }

    protected org.eclipse.jface.wizard.WizardDialog createCompleteMigrationWizard(org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator migrator) {
        return createWizardDialog(new org.eclipse.mylyn.internal.tasks.ui.migrator.CompleteConnectorMigrationWizard(migrator));
    }

    protected org.eclipse.jface.wizard.WizardDialog createWizardDialog(org.eclipse.jface.wizard.Wizard wizard) {
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), wizard);
        dialog.create();
        dialog.setBlockOnOpen(true);
        return dialog;
    }

    /**
     *
     * @return the frequency in seconds with which the completion prompt will be shown
     */
    protected int getCompletionPromptFrequency() {
        return 5;
    }

    public void warnOfValidationFailure(final java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> failedValidation) {
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
            @java.lang.Override
            public void run() {
                java.lang.String repositoryList = // $NON-NLS-1$
                com.google.common.base.Joiner.on("\n").join(com.google.common.collect.Iterables.transform(failedValidation, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi.repositoryToLabel()));
                org.eclipse.jface.dialogs.MessageDialog.openWarning(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.ConnectorMigrationUi_Validation_Failed, org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrationWizard_validation_failed, repositoryList));
            }
        });
    }

    private static com.google.common.base.Function<org.eclipse.mylyn.tasks.core.TaskRepository, java.lang.String> repositoryToLabel() {
        return new com.google.common.base.Function<org.eclipse.mylyn.tasks.core.TaskRepository, java.lang.String>() {
            @java.lang.Override
            public java.lang.String apply(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
                return repository.getRepositoryLabel();
            }
        };
    }

    protected void backupTaskList(final org.eclipse.core.runtime.IProgressMonitor monitor) throws java.io.IOException {
        try {
            monitor.subTask(Messages.ConnectorMigrationUi_Backing_up_task_list);
            java.lang.String backupFolder = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().getBackupFolderPath();
            new java.io.File(backupFolder).mkdirs();
            java.lang.String fileName = getBackupFileName(new java.util.Date());
            new org.eclipse.mylyn.internal.tasks.ui.util.TaskDataSnapshotOperation(backupFolder, fileName).run(new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
            // also take a snapshot because user might try to restore from snapshot
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().syncExec(new java.lang.Runnable() {
                @java.lang.Override
                public void run() {
                    backupManager.backupNow(true);
                }
            });
            monitor.worked(1);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw ((java.io.IOException) (e.getCause()));
        }
    }

    protected java.lang.String getBackupFileName(java.util.Date date) {
        // we use an underscore in the date format to prevent TaskListBackupManager from thinking this is one of its backups
        // and deleting it (TaskListBackupManager.MYLYN_BACKUP_REGEXP matches any string).
        return ("connector-migration-" + new java.text.SimpleDateFormat("yyyy_MM_dd_HHmmss").format(date)) + ".zip";// $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    }

    /**
     * Deletes the given tasks and repository, and all queries associated with the repository, while preserving the
     * credentials of <code>newRepository</code>.
     *
     * @param newRepository
     */
    protected void delete(final java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasks, final org.eclipse.mylyn.tasks.core.TaskRepository repository, final org.eclipse.mylyn.tasks.core.TaskRepository newRepository, org.eclipse.core.runtime.IProgressMonitor monitor) {
        final java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = com.google.common.collect.Sets.filter(tasksState.getTaskList().getQueries(), org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForRepository(repository));
        final org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer unsubmitted = tasksState.getTaskList().getUnsubmittedContainer(repository.getRepositoryUrl());
        try {
            monitor.subTask(Messages.ConnectorMigrationUi_Deleting_old_repository_tasks_and_queries);
            try {
                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(tasks);
                if (unsubmitted != null) {
                    org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(unsubmitted.getChildren());
                }
                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(queries);
            } catch (java.lang.Exception e) {
                // in case an error happens closing editors, we still want to delete
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e));
            }
            tasksState.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                @java.lang.Override
                public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                    for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                        delete(task);
                    }
                    if (unsubmitted != null) {
                        for (org.eclipse.mylyn.tasks.core.ITask task : unsubmitted.getChildren()) {
                            delete(task);
                        }
                    }
                    org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.performDeletion(queries);
                    java.util.Map<org.eclipse.mylyn.commons.net.AuthenticationType, org.eclipse.mylyn.commons.net.AuthenticationCredentials> credentialsMap = new java.util.HashMap<>();
                    for (org.eclipse.mylyn.commons.net.AuthenticationType type : org.eclipse.mylyn.commons.net.AuthenticationType.values()) {
                        org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = repository.getCredentials(type);
                        if (credentials != null) {
                            credentialsMap.put(type, credentials);
                        }
                    }
                    tasksState.getRepositoryManager().removeRepository(repository);
                    for (org.eclipse.mylyn.commons.net.AuthenticationType type : credentialsMap.keySet()) {
                        newRepository.setCredentials(type, credentialsMap.get(type), newRepository.getSavePassword(type));
                    }
                }
            }, monitor);
            tasksState.getRepositoryModel().clear();
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, Messages.ConnectorMigrationUi_Error_deleting_task, e));
        }
    }

    /**
     * Delete a task without deleting the context.
     */
    protected void delete(org.eclipse.mylyn.tasks.core.ITask task) {
        tasksState.getTaskList().deleteTask(task);
        try {
            tasksState.getTaskDataManager().deleteTaskData(task);
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to delete task data", e));// $NON-NLS-1$

        }
    }

    public void notifyMigrationComplete() {
        org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
            @java.lang.Override
            public void run() {
                org.eclipse.jface.dialogs.MessageDialog.openInformation(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.ConnectorMigrationUi_Connector_Migration_Complete, Messages.ConnectorMigrationUi_Connector_migration_completed_successfully_You_may_resume_using_the_task_list);
            }
        });
    }
}