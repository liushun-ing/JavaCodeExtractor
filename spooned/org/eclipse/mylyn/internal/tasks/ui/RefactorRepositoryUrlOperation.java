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
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
/**
 *
 * @author Rob Elves
 */
public class RefactorRepositoryUrlOperation extends org.eclipse.mylyn.internal.tasks.ui.TaskListModifyOperation {
    private final java.lang.String oldUrl;

    private final java.lang.String newUrl;

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    public RefactorRepositoryUrlOperation(java.lang.String oldUrl, java.lang.String newUrl) {
        this(null, oldUrl, newUrl);
    }

    public RefactorRepositoryUrlOperation(org.eclipse.mylyn.tasks.core.TaskRepository repository, java.lang.String oldUrl, java.lang.String newUrl) {
        super(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ROOT_SCHEDULING_RULE);
        org.eclipse.core.runtime.Assert.isNotNull(oldUrl);
        org.eclipse.core.runtime.Assert.isNotNull(newUrl);
        org.eclipse.core.runtime.Assert.isTrue(!oldUrl.equals(newUrl));
        this.repository = repository;
        this.oldUrl = oldUrl;
        this.newUrl = newUrl;
    }

    @java.lang.Override
    protected void operations(org.eclipse.core.runtime.IProgressMonitor monitor) throws org.eclipse.core.runtime.CoreException, java.lang.reflect.InvocationTargetException, java.lang.InterruptedException {
        try {
            // TasksUiPlugin.getTaskListManager().deactivateAllTasks();
            monitor.beginTask(Messages.RefactorRepositoryUrlOperation_Repository_URL_update, org.eclipse.core.runtime.IProgressMonitor.UNKNOWN);
            refactorOfflineHandles(oldUrl, newUrl);
            getTaskList().refactorRepositoryUrl(oldUrl, newUrl);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().refactorRepositoryUrl(repository, oldUrl, newUrl);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityMonitor().reloadActivityTime();
        } finally {
            monitor.done();
        }
    }

    private void refactorOfflineHandles(java.lang.String oldRepositoryUrl, java.lang.String newRepositoryUrl) throws org.eclipse.core.runtime.CoreException {
        org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager();
        for (org.eclipse.mylyn.tasks.core.ITask task : getTaskList().getAllTasks()) {
            if (oldRepositoryUrl.equals(task.getAttribute(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL))) {
                taskDataManager.refactorRepositoryUrl(task, task.getRepositoryUrl(), newRepositoryUrl);
            }
            if (task.getRepositoryUrl().equals(oldRepositoryUrl)) {
                taskDataManager.refactorRepositoryUrl(task, newRepositoryUrl, newRepositoryUrl);
                // RepositoryTaskData newTaskData = taskDataManager.getNewTaskData(repositoryTask.getRepositoryUrl(),
                // repositoryTask.getTaskId());
                // RepositoryTaskData oldTaskData = taskDataManager.getOldTaskData(repositoryTask.getRepositoryUrl(),
                // repositoryTask.getTaskId());
                // Set<RepositoryTaskAttribute> edits = taskDataManager.getEdits(repositoryTask.getRepositoryUrl(),
                // repositoryTask.getTaskId());
                // taskDataManager.remove(repositoryTask.getRepositoryUrl(), repositoryTask.getTaskId());
                // 
                // if (newTaskData != null) {
                // newTaskData.setRepositoryURL(newRepositoryUrl);
                // taskDataManager.setNewTaskData(newTaskData);
                // }
                // if (oldTaskData != null) {
                // oldTaskData.setRepositoryURL(newRepositoryUrl);
                // taskDataManager.setOldTaskData(oldTaskData);
                // }
                // if (!edits.isEmpty()) {
                // taskDataManager.saveEdits(newRepositoryUrl, repositoryTask.getTaskId(), edits);
                // }
            }
        }
        // TasksUiPlugin.getTaskDataStorageManager().saveNow();
    }
}