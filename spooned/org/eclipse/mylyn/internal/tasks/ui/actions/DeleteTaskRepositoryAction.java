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
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
/**
 *
 * @author Mik Kersten
 * @author David Shepherd
 * @author Steffen Pingel
 */
public class DeleteTaskRepositoryAction extends org.eclipse.mylyn.internal.tasks.ui.actions.AbstractTaskRepositoryAction {
    private static final java.lang.String ID = "org.eclipse.mylyn.tasklist.repositories.delete";// $NON-NLS-1$


    public DeleteTaskRepositoryAction() {
        super(Messages.DeleteTaskRepositoryAction_Delete_Repository);
        setImageDescriptor(org.eclipse.ui.internal.WorkbenchImages.getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_TOOL_DELETE));
        setId(org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction.ID);
        setEnabled(false);
        setActionDefinitionId(org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds.DELETE);
        setSingleSelect(true);
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    @java.lang.Override
    public void run() {
        final org.eclipse.mylyn.tasks.core.TaskRepository repositoryToDelete = getTaskRepository(getStructuredSelection());
        if (repositoryToDelete != null) {
            org.eclipse.mylyn.internal.tasks.ui.actions.DeleteTaskRepositoryAction.run(repositoryToDelete);
        }
    }

    public static void run(final org.eclipse.mylyn.tasks.core.TaskRepository repositoryToDelete) {
        org.eclipse.core.runtime.Assert.isNotNull(repositoryToDelete);
        final java.util.List<org.eclipse.mylyn.tasks.core.IRepositoryQuery> queriesToDelete = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.IRepositoryQuery>();
        final java.util.List<org.eclipse.mylyn.internal.tasks.core.AbstractTask> tasksToDelete = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.AbstractTask>();
        // check for queries over this repository
        java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().getQueries();
        for (org.eclipse.mylyn.tasks.core.IRepositoryQuery query : queries) {
            if (repositoryToDelete.getRepositoryUrl().equals(query.getRepositoryUrl()) && repositoryToDelete.getConnectorKind().equals(query.getConnectorKind())) {
                queriesToDelete.add(query);
            }
        }
        // check for tasks from this repository
        final java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasks = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getTasks(repositoryToDelete.getRepositoryUrl());
        for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
            if (repositoryToDelete.getRepositoryUrl().equals(task.getRepositoryUrl()) && repositoryToDelete.getConnectorKind().equals(task.getConnectorKind())) {
                tasksToDelete.add(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)));
            }
        }
        // add unsubmitted tasks
        org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer unsubmitted = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().getUnsubmittedContainer(repositoryToDelete.getRepositoryUrl());
        if (unsubmitted != null) {
            java.util.Collection<org.eclipse.mylyn.tasks.core.ITask> children = unsubmitted.getChildren();
            if (children != null) {
                for (org.eclipse.mylyn.tasks.core.ITask task : children) {
                    tasksToDelete.add(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (task)));
                }
            }
        }
        // confirm that the user wants to delete all tasks and queries that are associated
        boolean deleteConfirmed;
        if ((queriesToDelete.size() > 0) || (tasksToDelete.size() > 0)) {
            deleteConfirmed = org.eclipse.jface.dialogs.MessageDialog.openQuestion(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.DeleteTaskRepositoryAction_Confirm_Delete, org.eclipse.osgi.util.NLS.bind(Messages.DeleteTaskRepositoryAction_Delete_the_selected_task_repositories, new java.lang.Integer[]{ tasksToDelete.size(), queriesToDelete.size() }));
        } else {
            deleteConfirmed = org.eclipse.jface.dialogs.MessageDialog.openQuestion(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), Messages.DeleteTaskRepositoryAction_Confirm_Delete, org.eclipse.osgi.util.NLS.bind(Messages.DeleteTaskRepositoryAction_Delete_Specific_Task_Repository, new java.lang.String[]{ repositoryToDelete.getRepositoryLabel() }));
        }
        if (deleteConfirmed) {
            org.eclipse.mylyn.commons.core.ICoreRunnable op = new org.eclipse.mylyn.commons.core.ICoreRunnable() {
                public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                    try {
                        monitor.beginTask(Messages.DeleteTaskRepositoryAction_Delete_Repository_In_Progress, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
                        org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(tasksToDelete);
                        org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.prepareDeletion(queriesToDelete);
                        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().run(new org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable() {
                            public void execute(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
                                // delete tasks
                                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.performDeletion(tasksToDelete);
                                // delete queries
                                org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction.performDeletion(queriesToDelete);
                                // delete repository
                                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getRepositoryManager().removeRepository(repositoryToDelete);
                                // if repository is contributed via template, ensure it isn't added again
                                org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryUtil.disableAddAutomatically(repositoryToDelete.getRepositoryUrl());
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
                org.eclipse.core.runtime.Status status = new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Problems encountered deleting task repository: {0}", e.getMessage()), e);// $NON-NLS-1$

                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.logAndDisplayStatus(Messages.DeleteTaskRepositoryAction_Delete_Task_Repository_Failed, status);
            } catch (org.eclipse.core.runtime.OperationCanceledException e) {
                // canceled
            }
        }
    }
}