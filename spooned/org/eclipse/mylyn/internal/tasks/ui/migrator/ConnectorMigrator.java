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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryManager;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.sync.SynchronizationJob;
import org.eclipse.osgi.util.NLS;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.any;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForConnector;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskForConnector;
import static org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskSynchronizing;
/**
 * Allows users to migrate their data from an old connector to a new one for the same repository. Performs the following
 * steps:
 *
 * <pre>
 * * uses task list message service to prompt users to migrate
 * * backs up the task list
 * * automatically migrates repositories
 * * directs user to manually migrate queries and click a button in the task list message to complete migration
 * * once new queries have finished syncing, any tasks that are missing from the new queries are fetched by searching by task key
 * * private data (context, notes, categories, scheduled dates, and private due dates) is automatically migrated for all tasks
 * * all tasks are marked read except those which were incoming before migration
 * * old repositories are deleted
 * </pre>
 */
public class ConnectorMigrator {
    private static final com.google.common.collect.ImmutableSet<java.lang.String> EXCLUDED_REPOSITORY_PROPERTIES = com.google.common.collect.ImmutableSet.of(org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_CONNECTOR_KIND, org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_SYNCTIMESTAMP, org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants.PROPERTY_URL);

    protected static class OldTaskState {
        private final org.eclipse.mylyn.tasks.core.ITask.SynchronizationState syncState;

        private final org.eclipse.mylyn.tasks.core.ITask oldTask;

        public OldTaskState(org.eclipse.mylyn.tasks.core.ITask oldTask) {
            this.oldTask = oldTask;
            this.syncState = oldTask.getSynchronizationState();
        }

        public org.eclipse.mylyn.tasks.core.ITask getOldTask() {
            return oldTask;
        }

        public org.eclipse.mylyn.tasks.core.ITask.SynchronizationState getSyncState() {
            return syncState;
        }
    }

    private final java.util.Map<java.lang.String, java.lang.String> connectorKinds;

    private final java.lang.String explanatoryText;

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.TasksState tasksState;

    private java.util.List<java.lang.String> connectorsToMigrate = com.google.common.collect.ImmutableList.of();

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi migrationUi;

    private final java.util.Map<org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.TaskRepository> repositories = new java.util.HashMap<org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.TaskRepository>();

    private final com.google.common.collect.Table<org.eclipse.mylyn.tasks.core.TaskRepository, java.lang.String, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState> oldTasksStates = com.google.common.collect.HashBasedTable.create();

    private java.util.Map<org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> categories;

    private final org.eclipse.mylyn.internal.tasks.ui.migrator.JobListener syncTaskJobListener = new org.eclipse.mylyn.internal.tasks.ui.migrator.JobListener(new java.lang.Runnable() {
        @java.lang.Override
        public void run() {
            completeMigration();
        }
    });

    private boolean anyQueriesMigrated;

    private boolean allQueriesMigrated = true;

    public ConnectorMigrator(java.util.Map<java.lang.String, java.lang.String> connectorKinds, java.lang.String explanatoryText, org.eclipse.mylyn.internal.tasks.ui.migrator.TasksState tasksState, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi migrationUi) {
        checkArgument(!connectorKinds.isEmpty());
        this.connectorKinds = connectorKinds;
        this.explanatoryText = explanatoryText;
        this.migrationUi = migrationUi;
        this.tasksState = tasksState;
    }

    public java.util.Map<java.lang.String, java.lang.String> getConnectorKinds() {
        return com.google.common.collect.ImmutableMap.copyOf(connectorKinds);
    }

    public java.lang.String getExplanatoryText() {
        return explanatoryText;
    }

    public boolean needsMigration() {
        for (java.util.Map.Entry<java.lang.String, java.lang.String> entry : connectorKinds.entrySet()) {
            java.lang.String oldKind = entry.getKey();
            java.lang.String newKind = entry.getValue();
            if (((getRepositoryManager().getRepositoryConnector(oldKind) != null) && (getRepositoryManager().getRepositoryConnector(newKind) != null)) && (!getRepositoryManager().getRepositories(oldKind).isEmpty())) {
                return true;
            }
        }
        return false;
    }

    public void setConnectorsToMigrate(java.util.List<java.lang.String> connectors) {
        checkArgument(connectorKinds.keySet().containsAll(connectors));
        this.connectorsToMigrate = com.google.common.collect.ImmutableList.copyOf(connectors);
    }

    protected void migrateConnectors(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.io.IOException {
        final java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> failedValidation = new java.util.ArrayList<>();
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> oldRepositories = gatherRepositoriesToMigrate(connectorsToMigrate);
        monitor.beginTask(Messages.ConnectorMigrator_Migrating_repositories, oldRepositories.size() + 1);
        getMigrationUi().backupTaskList(monitor);
        for (org.eclipse.mylyn.tasks.core.TaskRepository repository : oldRepositories) {
            if (monitor.isCanceled()) {
                throw new org.eclipse.core.runtime.OperationCanceledException();
            }
            monitor.subTask(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_Migrating_X, repository.getRepositoryLabel()));
            java.lang.String kind = repository.getConnectorKind();
            java.lang.String newKind = getConnectorKinds().get(kind);
            org.eclipse.mylyn.tasks.core.TaskRepository newRepository = getMigratedRepository(newKind, repository);
            getRepositoryManager().addRepository(newRepository);
            repositories.put(repository, newRepository);
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasksToMigrate = com.google.common.collect.Sets.filter(getTaskList().getTasks(repository.getRepositoryUrl()), org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskForConnector(repository.getConnectorKind()));
            for (org.eclipse.mylyn.tasks.core.ITask task : tasksToMigrate) {
                oldTasksStates.put(newRepository, task.getTaskKey(), new org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState(task));
            }
            migrateQueries(repository, newRepository, monitor);
            disconnect(repository);
            monitor.worked(1);
        }
        java.util.Set<org.eclipse.mylyn.tasks.core.TaskRepository> newRepositories = com.google.common.collect.ImmutableSet.copyOf(repositories.values());
        monitor.beginTask(Messages.ConnectorMigrator_Validating_repository_connections, newRepositories.size());
        for (org.eclipse.mylyn.tasks.core.TaskRepository newRepository : newRepositories) {
            if (monitor.isCanceled()) {
                throw new org.eclipse.core.runtime.OperationCanceledException();
            }
            monitor.subTask(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_Validating_connection_to_X, newRepository.getRepositoryLabel()));
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector newConnector = getRepositoryManager().getRepositoryConnector(newRepository.getConnectorKind());
            try {
                newConnector.validateRepository(newRepository, monitor);
            } catch (java.lang.UnsupportedOperationException | org.eclipse.core.runtime.CoreException e) {
                failedValidation.add(newRepository);
            }
            monitor.worked(1);
        }
        monitor.done();
        if (!failedValidation.isEmpty()) {
            getMigrationUi().warnOfValidationFailure(failedValidation);
        }
    }

    protected void migrateQueries(org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.TaskRepository newRepository, org.eclipse.core.runtime.IProgressMonitor monitor) {
        java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queriesForUrl = getTaskList().getRepositoryQueries(repository.getRepositoryUrl());
        java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = com.google.common.collect.Sets.filter(queriesForUrl, org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForConnector(repository.getConnectorKind()));
        org.eclipse.core.runtime.SubMonitor subMonitor = org.eclipse.core.runtime.SubMonitor.convert(monitor, Messages.ConnectorMigrator_Migrating_Queries, queries.size());
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : queries) {
            org.eclipse.mylyn.internal.tasks.core.RepositoryQuery migratedQuery = migrateQuery(query, repository, newRepository, subMonitor);
            if (migratedQuery != null) {
                getTaskList().addQuery(migratedQuery);
                anyQueriesMigrated = true;
            } else {
                allQueriesMigrated = false;
            }
            subMonitor.worked(1);
        }
    }

    /**
     * Connectors can override to attempt to automatically migrate queries if possible.
     */
    protected org.eclipse.mylyn.internal.tasks.core.RepositoryQuery migrateQuery(org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.TaskRepository newRepository, org.eclipse.core.runtime.IProgressMonitor monitor) {
        return null;
    }

    /**
     *
     * @return whether any queries have been migrated for any repository
     */
    protected boolean anyQueriesMigrated() {
        return anyQueriesMigrated;
    }

    /**
     *
     * @return whether all queries have been migrated for all migrated repositories; returns <code>true</code> if no
    repositories have yet been migrated
     */
    protected boolean allQueriesMigrated() {
        return allQueriesMigrated;
    }

    protected void disconnect(org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        repository.setOffline(true);
        // we need to change the label so that the new repo doesn't have the same label, so that it can be edited
        repository.setRepositoryLabel(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_X_Unsupported_do_not_delete, repository.getRepositoryLabel()));
        java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queriesForUrl = getTaskList().getRepositoryQueries(repository.getRepositoryUrl());
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : com.google.common.collect.Sets.filter(queriesForUrl, org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isQueryForConnector(repository.getConnectorKind()))) {
            query.setAutoUpdate(false);// prevent error logged when Mylyn asks new connector to sync query for old connector

        }
    }

    protected java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> gatherRepositoriesToMigrate(java.util.List<java.lang.String> connectors) {
        java.util.List<org.eclipse.mylyn.tasks.core.TaskRepository> oldRepositories = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.TaskRepository>();
        for (java.lang.String kind : connectors) {
            oldRepositories.addAll(getRepositoryManager().getRepositories(kind));
        }
        return oldRepositories;
    }

    protected org.eclipse.mylyn.tasks.core.TaskRepository getMigratedRepository(java.lang.String newKind, org.eclipse.mylyn.tasks.core.TaskRepository oldRepository) {
        java.lang.String migratedRepositoryUrl = getMigratedRepositoryUrl(oldRepository);
        org.eclipse.mylyn.tasks.core.TaskRepository newRepository = getRepositoryManager().getRepository(newKind, migratedRepositoryUrl);
        if (newRepository == null) {
            newRepository = migrateRepository(newKind, migratedRepositoryUrl, oldRepository);
        }
        return newRepository;
    }

    protected java.lang.String getMigratedRepositoryUrl(org.eclipse.mylyn.tasks.core.TaskRepository oldRepository) {
        return oldRepository.getRepositoryUrl();
    }

    protected org.eclipse.mylyn.tasks.core.TaskRepository migrateRepository(java.lang.String newKind, java.lang.String migratedRepositoryUrl, org.eclipse.mylyn.tasks.core.TaskRepository oldRepository) {
        org.eclipse.mylyn.tasks.core.TaskRepository newRepository = new org.eclipse.mylyn.tasks.core.TaskRepository(newKind, migratedRepositoryUrl);
        for (java.util.Map.Entry<java.lang.String, java.lang.String> entry : oldRepository.getProperties().entrySet()) {
            if (!org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.EXCLUDED_REPOSITORY_PROPERTIES.contains(entry.getKey())) {
                newRepository.setProperty(entry.getKey(), entry.getValue());
            }
        }
        for (org.eclipse.mylyn.commons.net.AuthenticationType type : org.eclipse.mylyn.commons.net.AuthenticationType.values()) {
            org.eclipse.mylyn.commons.net.AuthenticationCredentials credentials = oldRepository.getCredentials(type);
            newRepository.setCredentials(type, credentials, oldRepository.getSavePassword(type));
        }
        return newRepository;
    }

    protected void migrateTasks(org.eclipse.core.runtime.IProgressMonitor monitor) {
        tasksState.getTaskActivityManager().deactivateActiveTask();
        // Note: we're assuming the new connector uses different task IDs (and therefore different handle identifiers)
        // from the old one. This may not be the case for Bugzilla.
        for (java.util.Map.Entry<org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.TaskRepository> entry : repositories.entrySet()) {
            org.eclipse.mylyn.tasks.core.TaskRepository oldRepository = entry.getKey();
            org.eclipse.mylyn.tasks.core.TaskRepository newRepository = entry.getValue();
            monitor.subTask(org.eclipse.osgi.util.NLS.bind(Messages.ConnectorMigrator_Migrating_tasks_for_X, newRepository));
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector newConnector = getRepositoryManager().getRepositoryConnector(newRepository.getConnectorKind());
            java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasksToMigrate = com.google.common.collect.Sets.filter(getTaskList().getTasks(oldRepository.getRepositoryUrl()), org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskForConnector(oldRepository.getConnectorKind()));
            migrateTasks(tasksToMigrate, oldRepository, newRepository, newConnector, monitor);
        }
        monitor.subTask(Messages.ConnectorMigrator_Waiting_for_tasks_to_synchronize);
        getSyncTaskJobListener().start();
        while (!getSyncTaskJobListener().isComplete()) {
            try {
                java.lang.Thread.sleep(100);
            } catch (java.lang.InterruptedException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, e.getMessage(), e));
            }
        } 
    }

    protected void migrateTasks(final java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasksToMigrate, final org.eclipse.mylyn.tasks.core.TaskRepository oldRepository, final org.eclipse.mylyn.tasks.core.TaskRepository newRepository, final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector newConnector, final org.eclipse.core.runtime.IProgressMonitor monitor) {
        com.google.common.collect.ImmutableMap<java.lang.String, org.eclipse.mylyn.tasks.core.ITask> tasksByKey = com.google.common.collect.FluentIterable.from(getTaskList().getTasks(newRepository.getRepositoryUrl())).filter(org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskForConnector(newConnector.getConnectorKind())).uniqueIndex(new com.google.common.base.Function<org.eclipse.mylyn.tasks.core.ITask, java.lang.String>() {
            @java.lang.Override
            public java.lang.String apply(org.eclipse.mylyn.tasks.core.ITask task) {
                return task.getTaskKey();
            }
        });
        final java.util.Map<org.eclipse.mylyn.internal.tasks.core.AbstractTask, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState> migratedTasks = new java.util.HashMap<>();
        java.util.Set<org.eclipse.mylyn.tasks.core.ITask> tasksToSynchronize = new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>();
        for (org.eclipse.mylyn.tasks.core.ITask oldTask : tasksToMigrate) {
            java.lang.String taskKey = oldTask.getTaskKey();
            org.eclipse.mylyn.tasks.core.ITask newTask = tasksByKey.get(taskKey);
            if (newTask == null) {
                org.eclipse.mylyn.tasks.core.data.TaskData taskData = getTaskData(taskKey, newConnector, newRepository, monitor);
                if (taskData != null) {
                    newTask = createTask(taskData, newRepository);
                    tasksToSynchronize.add(newTask);
                }
            }
            if (newTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) {
                org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState oldTaskState = oldTasksStates.get(newRepository, oldTask.getTaskKey());
                if (oldTaskState == null) {
                    oldTaskState = new org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState(oldTask);
                }
                migratedTasks.put(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (newTask)), oldTaskState);
            }
            if ((newTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (oldTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) {
                migratePrivateData(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (oldTask)), ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (newTask)), monitor);
            }
        }
        oldTasksStates.row(newRepository).clear();
        migrateTaskContext(migratedTasks);
        getMigrationUi().delete(tasksToMigrate, oldRepository, newRepository, monitor);
        for (org.eclipse.mylyn.tasks.core.ITask task : tasksToSynchronize) {
            getTaskList().addTask(task);
        }
        org.eclipse.mylyn.tasks.core.sync.SynchronizationJob job = tasksState.getTaskJobFactory().createSynchronizeTasksJob(newConnector, newRepository, tasksToSynchronize);
        getSyncTaskJobListener().add(job, new java.lang.Runnable() {
            @java.lang.Override
            public void run() {
                long start = java.lang.System.currentTimeMillis();
                while (any(migratedTasks.keySet(), org.eclipse.mylyn.internal.tasks.ui.migrator.TaskPredicates.isTaskSynchronizing()) && ((java.lang.System.currentTimeMillis() - start) < java.util.concurrent.TimeUnit.MILLISECONDS.convert(4, java.util.concurrent.TimeUnit.HOURS))) {
                    try {
                        java.lang.Thread.sleep(java.util.concurrent.TimeUnit.MILLISECONDS.convert(3, java.util.concurrent.TimeUnit.SECONDS));
                    } catch (java.lang.InterruptedException e) {
                        // NOSONAR
                    }
                } 
                for (java.util.Map.Entry<org.eclipse.mylyn.internal.tasks.core.AbstractTask, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState> entry : migratedTasks.entrySet()) {
                    org.eclipse.mylyn.internal.tasks.core.AbstractTask newTask = entry.getKey();
                    org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState oldTask = entry.getValue();
                    newTask.setSynchronizationState(oldTask.getSyncState());
                }
                java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = getTaskList().getRepositoryQueries(newRepository.getRepositoryUrl());
                if (!queries.isEmpty()) {
                    org.eclipse.mylyn.tasks.core.sync.SynchronizationJob synchronizeQueriesJob = tasksState.getTaskJobFactory().createSynchronizeQueriesJob(newConnector, newRepository, queries);
                    synchronizeQueriesJob.schedule();
                }
            }
        });
        job.schedule();
    }

    private void migrateTaskContext(java.util.Map<org.eclipse.mylyn.internal.tasks.core.AbstractTask, org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrator.OldTaskState> taskStates) {
        java.util.Map<org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.tasks.core.ITask> tasks = taskStates.entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getValue().getOldTask(), e -> e.getKey()));
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().moveContext(tasks);
    }

    protected void completeMigration() {
        categories = null;
        getMigrationUi().notifyMigrationComplete();
    }

    protected void migratePrivateData(org.eclipse.mylyn.internal.tasks.core.AbstractTask oldTask, org.eclipse.mylyn.internal.tasks.core.AbstractTask newTask, org.eclipse.core.runtime.IProgressMonitor monitor) {
        org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category = getCategories().get(oldTask);
        if (category != null) {
            getTaskList().addTask(newTask, category);
        }
        newTask.setNotes(oldTask.getNotes());
        tasksState.getTaskActivityManager().setScheduledFor(newTask, oldTask.getScheduledForDate());
        tasksState.getTaskActivityManager().setDueDate(newTask, oldTask.getDueDate());
        newTask.setEstimatedTimeHours(oldTask.getEstimatedTimeHours());
    }

    protected org.eclipse.mylyn.tasks.core.ITask createTask(org.eclipse.mylyn.tasks.core.data.TaskData taskData, org.eclipse.mylyn.tasks.core.TaskRepository repository) {
        return new org.eclipse.mylyn.internal.tasks.core.TaskTask(repository.getConnectorKind(), repository.getRepositoryUrl(), taskData.getTaskId());
    }

    /**
     * This method is used to support migrating tasks that are not contained in any migrated query.
     */
    protected org.eclipse.mylyn.tasks.core.data.TaskData getTaskData(java.lang.String taskKey, org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector newConnector, org.eclipse.mylyn.tasks.core.TaskRepository newRepository, org.eclipse.core.runtime.IProgressMonitor monitor) {
        try {
            if (newConnector.supportsSearchByTaskKey(newRepository)) {
                return newConnector.searchByTaskKey(newRepository, taskKey, monitor);
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, (("Failed to migrate task "// $NON-NLS-1$
             + taskKey) + " for repository ") + newRepository.getRepositoryLabel(), e));// $NON-NLS-1$

        }
        return null;
    }

    public java.util.Map<java.lang.String, java.lang.String> getSelectedConnectors() {
        return com.google.common.collect.Maps.filterKeys(getConnectorKinds(), in(connectorsToMigrate));
    }

    protected org.eclipse.mylyn.internal.tasks.core.TaskList getTaskList() {
        return tasksState.getTaskList();
    }

    protected org.eclipse.mylyn.tasks.core.IRepositoryManager getRepositoryManager() {
        return tasksState.getRepositoryManager();
    }

    /**
     *
     * @return The task categorization that existed the first time this method was called
     */
    protected java.util.Map<org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> getCategories() {
        if (categories == null) {
            categories = new java.util.HashMap<org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory>();
            for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory category : getTaskList().getCategories()) {
                for (org.eclipse.mylyn.tasks.core.ITask task : category.getChildren()) {
                    categories.put(task, category);
                }
            }
        }
        return categories;
    }

    public org.eclipse.mylyn.internal.tasks.ui.migrator.ConnectorMigrationUi getMigrationUi() {
        return migrationUi;
    }

    protected org.eclipse.mylyn.internal.tasks.ui.migrator.JobListener getSyncTaskJobListener() {
        return syncTaskJobListener;
    }
}