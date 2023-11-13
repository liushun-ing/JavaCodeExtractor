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
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskJobFactory;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore;
public class TasksState {
    private final org.eclipse.mylyn.internal.tasks.core.TaskActivityManager taskActivityManager;

    private final org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager;

    private final org.eclipse.mylyn.internal.tasks.core.RepositoryModel repositoryModel;

    private final org.eclipse.mylyn.internal.tasks.core.TaskList taskList;

    private final org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore contextStore;

    private final org.eclipse.mylyn.internal.tasks.core.TaskJobFactory taskJobFactory;

    private final org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager repositoryManager;

    public TasksState(org.eclipse.mylyn.internal.tasks.core.TaskList taskList, org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager taskDataManager, org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager repositoryManager, org.eclipse.mylyn.internal.tasks.core.RepositoryModel repositoryModel, org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore contextStore, org.eclipse.mylyn.internal.tasks.core.TaskActivityManager taskActivityManager, org.eclipse.mylyn.internal.tasks.core.TaskJobFactory taskJobFactory) {
        this.taskList = taskList;
        this.taskDataManager = taskDataManager;
        this.repositoryManager = repositoryManager;
        this.repositoryModel = repositoryModel;
        this.contextStore = contextStore;
        this.taskActivityManager = taskActivityManager;
        this.taskJobFactory = taskJobFactory;
    }

    public org.eclipse.mylyn.internal.tasks.core.TaskActivityManager getTaskActivityManager() {
        return taskActivityManager;
    }

    public org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager getTaskDataManager() {
        return taskDataManager;
    }

    public org.eclipse.mylyn.internal.tasks.core.RepositoryModel getRepositoryModel() {
        return repositoryModel;
    }

    public org.eclipse.mylyn.internal.tasks.core.TaskList getTaskList() {
        return taskList;
    }

    public org.eclipse.mylyn.tasks.core.context.AbstractTaskContextStore getContextStore() {
        return contextStore;
    }

    public org.eclipse.mylyn.internal.tasks.core.TaskJobFactory getTaskJobFactory() {
        return taskJobFactory;
    }

    public org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }
}