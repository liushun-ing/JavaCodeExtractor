/**
 * *****************************************************************************
 * Copyright (c) 2004, 2016 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public class OpenRepositoryTaskJob extends org.eclipse.core.runtime.jobs.Job {
    private final java.lang.String repositoryUrl;

    private final java.lang.String repositoryKind;

    private final java.lang.String taskId;

    private final java.lang.String taskUrl;

    private org.eclipse.mylyn.tasks.core.ITask task;

    private org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener;

    private final long timestamp;

    private java.lang.String taskKey;

    private org.eclipse.mylyn.tasks.core.TaskRepository repository;

    /**
     * Creates a job that searches for a task with the given task <i>key</i> and opens it if found.
     */
    public OpenRepositoryTaskJob(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String taskKey, java.lang.String taskUrl, org.eclipse.ui.IWorkbenchPage page) {
        super(java.text.MessageFormat.format(Messages.OpenRepositoryTaskJob_Opening_repository_task_X, taskKey));
        this.repositoryKind = repository.getConnectorKind();
        this.taskId = null;
        this.repositoryUrl = repository.getRepositoryUrl();
        this.taskUrl = taskUrl;
        this.timestamp = 0;
        this.repository = repository;
        this.taskKey = taskKey;
    }

    /**
     * Creates a job that fetches a task with the given task id and opens it.
     */
    public OpenRepositoryTaskJob(java.lang.String repositoryKind, java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String taskUrl, org.eclipse.ui.IWorkbenchPage page) {
        this(repositoryKind, repositoryUrl, taskId, taskUrl, 0, page);
    }

    /**
     * Creates a job that fetches a task with the given task id and opens it, expanding all comments made after the
     * given timestamp.
     */
    public OpenRepositoryTaskJob(java.lang.String repositoryKind, java.lang.String repositoryUrl, java.lang.String taskId, java.lang.String taskUrl, long timestamp, org.eclipse.ui.IWorkbenchPage page) {
        super(java.text.MessageFormat.format(Messages.OpenRepositoryTaskJob_Opening_repository_task_X, taskId));
        this.repositoryKind = repositoryKind;
        this.taskId = taskId;
        this.repositoryUrl = repositoryUrl;
        this.taskUrl = taskUrl;
        this.timestamp = timestamp;
    }

    /**
     * Creates a job that fetches a task with the given task id or key and opens it.
     */
    public OpenRepositoryTaskJob(org.eclipse.ui.IWorkbenchPage page, java.lang.String repositoryKind, java.lang.String repositoryUrl, java.lang.String taskIdOrKey, java.lang.String taskUrl) {
        this(repositoryKind, repositoryUrl, taskIdOrKey, taskUrl, page);
        taskKey = taskIdOrKey;
    }

    /**
     * Returns the task if it was created when opening
     *
     * @return  */
    public org.eclipse.mylyn.tasks.core.ITask getTask() {
        return task;
    }

    public void setListener(org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener listener) {
        this.listener = listener;
    }

    @java.lang.Override
    public org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
        monitor.beginTask(Messages.OpenRepositoryTaskJob_Opening_Remote_Task, 10);
        if (repository == null) {
            repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(repositoryKind, repositoryUrl);
        }
        if (repository == null) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    org.eclipse.jface.dialogs.MessageDialog.openError(null, Messages.OpenRepositoryTaskJob_Repository_Not_Found, (java.text.MessageFormat.format(Messages.OpenRepositoryTaskJob_Could_not_find_repository_configuration_for_X, repositoryUrl) + "\n")// $NON-NLS-1$
                     + java.text.MessageFormat.format(Messages.OpenRepositoryTaskJob_Please_set_up_repository_via_X, Messages.TasksUiPlugin_Task_Repositories));
                    org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(taskUrl);
                }
            });
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }
        org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repositoryKind);
        try {
            final org.eclipse.mylyn.tasks.core.data.TaskData taskData = getTaskData(connector, monitor);
            if (taskData != null) {
                task = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryModel().createTask(repository, taskData.getTaskId());
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().putUpdatedTaskData(task, taskData, true);
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent event = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTask(task, taskId);
                        if ((listener != null) && (event != null)) {
                            listener.taskOpened(event);
                        }
                        if ((timestamp != 0) && (event != null)) {
                            java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
                            if (commentAttributes.size() > 0) {
                                for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : commentAttributes) {
                                    org.eclipse.mylyn.tasks.core.data.TaskAttribute commentCreateDate = commentAttribute.getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_DATE);
                                    if (commentCreateDate != null) {
                                        java.util.Date dateValue = taskData.getAttributeMapper().getDateValue(commentCreateDate);
                                        if (dateValue.getTime() < timestamp) {
                                            continue;
                                        }
                                        org.eclipse.mylyn.tasks.core.data.TaskAttribute dn = commentAttribute.getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_NUMBER);
                                        org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (event.getEditor()));
                                        if (dn != null) {
                                            editor.selectReveal(org.eclipse.mylyn.tasks.core.data.TaskAttribute.PREFIX_COMMENT + dn.getValue());
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            } else {
                org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                    public void run() {
                        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openUrl(taskUrl);
                    }
                });
            }
        } catch (final org.eclipse.core.runtime.CoreException e) {
            org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(new java.lang.Runnable() {
                public void run() {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.OpenRepositoryTaskJob_Unable_to_open_task, e.getStatus());
                }
            });
        } finally {
            monitor.done();
        }
        return org.eclipse.core.runtime.Status.OK_STATUS;
    }

    org.eclipse.mylyn.tasks.core.data.TaskData getTaskData(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        if (((taskId != null) && (taskKey != null)) && connector.supportsSearchByTaskKey(repository)) {
            try {
                org.eclipse.mylyn.tasks.core.data.TaskData data = getTaskDataByKey(connector, monitor);
                if (data != null) {
                    return data;
                }
            } catch (org.eclipse.core.runtime.CoreException | java.lang.RuntimeException e) {
            }
            try {
                return connector.getTaskData(repository, taskId, monitor);
            } catch (org.eclipse.core.runtime.CoreException | java.lang.RuntimeException e) {
                // do not display connector's message because it may be about using a task key as an ID which is not the real error
                throw new org.eclipse.core.runtime.CoreException(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Could not find task with ID \"{0}\" on repository {1}.", taskId, repository), e));// $NON-NLS-1$

            }
        } else if (taskId != null) {
            return connector.getTaskData(repository, taskId, monitor);
        } else if ((taskKey != null) && connector.supportsSearchByTaskKey(repository)) {
            return getTaskDataByKey(connector, monitor);
        }
        return null;
    }

    private org.eclipse.mylyn.tasks.core.data.TaskData getTaskDataByKey(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.tasks.core.data.TaskData searchTaskData = connector.searchByTaskKey(repository, taskKey, monitor);
        if ((searchTaskData != null) && searchTaskData.isPartial()) {
            return connector.getTaskData(repository, searchTaskData.getTaskId(), monitor);
        }
        return searchTaskData;
    }
}