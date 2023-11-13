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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.sync.DeleteTasksJob;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
/**
 *
 * @author Mik Kersten
 */
public class DeleteAction extends org.eclipse.ui.actions.BaseSelectionListenerAction {
    public static final java.lang.String ID = "org.eclipse.mylyn.tasklist.actions.delete";// $NON-NLS-1$


    public DeleteAction() {
        super(Messages.DeleteAction_Delete);
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.ID);
        setImageDescriptor(org.eclipse.ui.internal.WorkbenchImages.getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_TOOL_DELETE));
        setActionDefinitionId(org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds.DELETE);
    }

    @java.lang.Override
    public void run() {
        doDelete(getStructuredSelection().toList());
    }

    protected void doDelete(final java.util.List<?> toDelete) {
        boolean allLocalTasks = true;
        boolean allSupportRepositoryDeletion = true;
        boolean allElementsAreTasks = true;
        // determine what repository elements are to be deleted so that we can present the correct message to the user
        for (java.lang.Object object : toDelete) {
            if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (object));
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector repositoryConnector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(task.getConnectorKind());
                org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(task.getConnectorKind(), task.getRepositoryUrl());
                if ((repository != null) && (repositoryConnector != null)) {
                    allSupportRepositoryDeletion &= repositoryConnector.canDeleteTask(repository, task);
                } else {
                    allSupportRepositoryDeletion = false;
                }
                allLocalTasks &= task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask;
            } else {
                allElementsAreTasks = false;
            }
        }
        java.lang.String elements = buildElementListString(toDelete);
        java.lang.String message = buildMessage(toDelete, elements, allElementsAreTasks);
        if (toDelete.isEmpty()) {
            org.eclipse.jface.dialogs.MessageDialog.openError(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.DeleteAction_Delete_failed, Messages.DeleteAction_Nothing_selected);
        } else {
            boolean deleteConfirmed = false;
            boolean deleteOnServer = false;
            final boolean allTasksDeletable = allSupportRepositoryDeletion;
            if (allLocalTasks || (!allElementsAreTasks)) {
                deleteConfirmed = org.eclipse.jface.dialogs.MessageDialog.openQuestion(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.DeleteAction_Delete_Tasks, message);
            } else {
                java.lang.String toggleMessage = Messages.DeleteAction_Also_delete_from_repository_X;
                if (allTasksDeletable) {
                    toggleMessage = org.eclipse.osgi.util.NLS.bind(toggleMessage, "");// $NON-NLS-1$

                } else {
                    toggleMessage = org.eclipse.osgi.util.NLS.bind(toggleMessage, Messages.DeleteAction_Not_supported);
                }
                final org.eclipse.jface.dialogs.MessageDialogWithToggle dialog = new org.eclipse.jface.dialogs.MessageDialogWithToggle(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.DeleteAction_Delete_Tasks, null, message, org.eclipse.jface.dialogs.MessageDialog.QUESTION, new java.lang.String[]{ org.eclipse.jface.dialogs.IDialogConstants.YES_LABEL, org.eclipse.jface.dialogs.IDialogConstants.NO_LABEL }, 0, toggleMessage, false) {
                    @java.lang.Override
                    protected org.eclipse.swt.widgets.Control createContents(org.eclipse.swt.widgets.Composite parent) {
                        org.eclipse.swt.widgets.Control createdControl = super.createContents(parent);
                        getToggleButton().setEnabled(allTasksDeletable);
                        return createdControl;
                    }
                };
                deleteConfirmed = dialog.open() == org.eclipse.jface.dialogs.IDialogConstants.YES_ID;
                deleteOnServer = dialog.getToggleState() && allTasksDeletable;
            }
            if (deleteConfirmed) {
                deleteElements(toDelete, deleteOnServer);
            }
        }
    }

    private void deleteElements(final java.util.List<?> toDelete, final boolean deleteOnServer) {
        org.eclipse.mylyn.commons.core.ICoreRunnable op = new org.eclipse.mylyn.commons.core.ICoreRunnable() {
            public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                try {
                    monitor.beginTask(Messages.DeleteAction_Delete_in_progress, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                    org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(toDelete);
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                        public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                            org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.performDeletion(toDelete);
                            if (deleteOnServer) {
                                performDeletionFromServer(toDelete);
                            }
                        }
                    }, monitor);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            org.eclipse.mylyn.commons.workbench.WorkbenchUtil.runInUi(op, null);
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Problems encountered deleting task list elements: {0}", e.getMessage()), e);// $NON-NLS-1$

            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.DeleteTaskRepositoryAction_Delete_Task_Repository_Failed, status);
        } catch (org.eclipse.core.runtime.OperationCanceledException e) {
            // canceled
        }
    }

    private java.lang.String buildMessage(final java.util.List<?> toDelete, java.lang.String elements, boolean allElementsAreTasks) {
        java.lang.String message;
        if (toDelete.size() == 1) {
            java.lang.Object object = toDelete.get(0);
            if (object instanceof org.eclipse.mylyn.tasks.core.ITask) {
                if (((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (object)) instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask) {
                    message = Messages.DeleteAction_Permanently_delete_from_task_list;
                } else {
                    message = Messages.DeleteAction_Delete_task_from_task_list_context_planning_deleted;
                }
            } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                message = Messages.DeleteAction_Permanently_delete_the_category;
            } else if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                message = Messages.DeleteAction_Permanently_delete_the_query;
            } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
                message = Messages.DeleteAction_Delete_the_planning_information_and_context_of_all_unmatched_tasks;
            } else if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer) {
                message = Messages.DeleteAction_Delete_all_of_the_unsubmitted_tasks;
            } else {
                message = Messages.DeleteAction_Permanently_delete_the_element_listed_below;
            }
        } else if (allElementsAreTasks) {
            message = Messages.DeleteAction_Delete_tasks_from_task_list_context_planning_deleted;
        } else {
            message = Messages.DeleteAction_Delete_elements_from_task_list_context_planning_deleted;
        }
        message += "\n\n" + elements;// $NON-NLS-1$

        return message;
    }

    private java.lang.String buildElementListString(final java.util.List<?> toDelete) {
        java.lang.String elements = "";// $NON-NLS-1$

        int i = 0;
        for (java.lang.Object object : toDelete) {
            if (object instanceof org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer) {
                continue;
            }
            i++;
            if (i < 20) {
                // TODO this action should be based on the action enablement and check if the container is user managed or not
                if (object instanceof org.eclipse.mylyn.tasks.core.IRepositoryElement) {
                    elements += ("    " + ((org.eclipse.mylyn.tasks.core.IRepositoryElement) (object)).getSummary()) + "\n";// $NON-NLS-1$ //$NON-NLS-2$

                }
            } else {
                elements += "...";// $NON-NLS-1$

                break;
            }
        }
        return elements;
    }

    public static void prepareDeletion(java.util.Collection<?> toDelete) {
        for (java.lang.Object selectedObject : toDelete) {
            if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject));
                org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(task);
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.closeTaskEditorInAllPages(task, false);
            } else if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer) {
                // support both the unmatched and the unsubmitted
                if (toDelete.size() == 1) {
                    org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(((org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer) (selectedObject)).getChildren());
                }
            }
        }
    }

    public static void performDeletion(java.util.Collection<?> toDelete) {
        for (java.lang.Object selectedObject : toDelete) {
            if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask task = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (selectedObject));
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().deleteTask(task);
                try {
                    org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().deleteTaskData(task);
                } catch (org.eclipse.core.runtime.CoreException e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(// $NON-NLS-1$
                    new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to delete task data", e));
                }
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().deleteContext(task);
            } else if (selectedObject instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().deleteQuery(((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (selectedObject)));
            } else if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().deleteCategory(((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (selectedObject)));
            } else if (selectedObject instanceof org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer) {
                // support both the unmatched and the unsubmitted
                if (toDelete.size() == 1) {
                    // loop to ensure that all subtasks are deleted as well
                    for (int i = 0; i < 5; i++) {
                        java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children = ((org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer) (selectedObject)).getChildren();
                        if (children.isEmpty()) {
                            break;
                        }
                        org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.performDeletion(children);
                    }
                }
            }
        }
    }

    private void performDeletionFromServer(java.util.List<?> toDelete) {
        java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToDelete = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITask>();
        for (java.lang.Object element : toDelete) {
            if (element instanceof org.eclipse.mylyn.tasks.core.ITask) {
                tasksToDelete.add(((org.eclipse.mylyn.tasks.core.ITask) (element)));
            }
        }
        final org.eclipse.mylyn.internal.tasks.core.sync.DeleteTasksJob deleteTaskJob = new org.eclipse.mylyn.internal.tasks.core.sync.DeleteTasksJob(Messages.DeleteAction_Deleting_tasks_from_repositories, tasksToDelete, org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager());
        deleteTaskJob.setPriority(org.eclipse.core.runtime.jobs.Job.INTERACTIVE);
        deleteTaskJob.addJobChangeListener(new org.eclipse.core.runtime.jobs.JobChangeAdapter() {
            @java.lang.Override
            public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
                if ((deleteTaskJob.getStatus() != null) && (!deleteTaskJob.getStatus().isOK())) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(deleteTaskJob.getStatus());
                }
            }
        });
        deleteTaskJob.setUser(true);
        deleteTaskJob.schedule();
    }

    @java.lang.Override
    protected boolean updateSelection(org.eclipse.jface.viewers.IStructuredSelection selection) {
        java.util.List<?> elements = selection.toList();
        for (java.lang.Object object : elements) {
            if (object instanceof org.eclipse.mylyn.internal.tasks.core.UncategorizedTaskContainer) {
                return false;
            }
        }
        return true;
    }
}