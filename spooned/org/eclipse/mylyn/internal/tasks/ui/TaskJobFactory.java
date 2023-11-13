/**
 * *****************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.IRepositoryModel;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.sync.SubmitJob;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationJob;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.ui.progress.IProgressConstants;
/**
 *
 * @author David Green
 */
public class TaskJobFactory extends org.eclipse.mylyn.internal.tasks.core.TaskJobFactory {
    public TaskJobFactory(org.eclipse.mylyn.internal.tasks.core.TaskList taskList, org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager, org.eclipse.mylyn.tasks.core.IRepositoryManager repositoryManager, org.eclipse.mylyn.tasks.core.IRepositoryModel tasksModel) {
        super(taskList, taskDataManager, repositoryManager, tasksModel);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SynchronizationJob createSynchronizeTasksJob(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasks) {
        return decorateJob(super.createSynchronizeTasksJob(connector, tasks), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SynchronizationJob createSynchronizeTasksJob(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasks) {
        return decorateJob(super.createSynchronizeTasksJob(connector, taskRepository, tasks), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SynchronizationJob createSynchronizeQueriesJob(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository repository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries) {
        return decorateJob(super.createSynchronizeQueriesJob(connector, repository, queries), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SynchronizationJob createSynchronizeRepositoriesJob(java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> repositories) {
        return decorateJob(super.createSynchronizeRepositoriesJob(repositories), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SYNCHRONIZE);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SubmitJob createSubmitTaskJob(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.data.TaskData taskData, java.util.Set<org.eclipse.mylyn.tasks.core.data.TaskAttribute> oldAttributes) {
        return decorateJob(super.createSubmitTaskJob(connector, taskRepository, task, taskData, oldAttributes), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SUBMIT);
    }

    @java.lang.Override
    public org.eclipse.mylyn.tasks.core.sync.SubmitJob createSubmitTaskAttachmentJob(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.TaskRepository taskRepository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource source, java.lang.String comment, org.eclipse.mylyn.tasks.core.data.TaskAttribute attachmentAttribute) {
        return decorateJob(super.createSubmitTaskAttachmentJob(connector, taskRepository, task, source, comment, attachmentAttribute), org.eclipse.mylyn.tasks.ui.TasksUiImages.REPOSITORY_SUBMIT);
    }

    private <T extends org.eclipse.core.runtime.jobs.Job> T decorateJob(T job, org.eclipse.jface.resource.ImageDescriptor iconImageDescriptor) {
        if (iconImageDescriptor != null) {
            job.setProperty(org.eclipse.ui.progress.IProgressConstants.ICON_PROPERTY, iconImageDescriptor);
        }
        return job;
    }
}