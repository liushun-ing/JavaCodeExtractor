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
package org.eclipse.mylyn.internal.tasks.ui.actions;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.Person;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskGroup;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.actions.ActionFactory;
/**
 *
 * @author Mik Kersten
 * @author Rob Elves
 */
public class SynchronizeSelectedAction extends org.eclipse.ui.actions.ActionDelegate implements org.eclipse.ui.IViewActionDelegate {
    @java.lang.Override
    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView = org.eclipse.mylyn.internal.tasks.ui.views.TaskListView.getFromActivePerspective();
        if (taskListView != null) {
            org.eclipse.jface.viewers.ISelection selection = taskListView.getViewer().getSelection();
            if (selection.isEmpty()) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeAllRepositories(true);
            } else if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
                synchronizeSelected(((org.eclipse.jface.viewers.IStructuredSelection) (selection)));
            }
        }
    }

    private void synchronizeSelected(org.eclipse.jface.viewers.IStructuredSelection selection) {
        java.util.Map<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector, java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> queriesToSyncMap = new java.util.LinkedHashMap<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector, java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>>();
        java.util.Map<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector, java.util.List<org.eclipse.mylyn.tasks.core.ITask>> tasksToSyncMap = new java.util.LinkedHashMap<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector, java.util.List<org.eclipse.mylyn.tasks.core.ITask>>();
        // collect queries and tasks
        for (java.lang.Object obj : selection.toList()) {
            if (obj instanceof org.eclipse.mylyn.tasks.core.IRepositoryQuery) {
                final org.eclipse.mylyn.internal.tasks.core.RepositoryQuery repositoryQuery = ((org.eclipse.mylyn.internal.tasks.core.RepositoryQuery) (obj));
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repositoryQuery.getConnectorKind());
                if (client != null) {
                    java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queriesToSync = queriesToSyncMap.get(client);
                    if (queriesToSync == null) {
                        queriesToSync = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>();
                        queriesToSyncMap.put(client, queriesToSync);
                    }
                    queriesToSync.add(repositoryQuery);
                }
            } else if (obj instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                org.eclipse.mylyn.internal.tasks.core.TaskCategory cat = ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (obj));
                for (org.eclipse.mylyn.tasks.core.ITask task : cat.getChildren()) {
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
                    addTaskToSync(client, task, tasksToSyncMap);
                }
            } else if (obj instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.internal.tasks.core.AbstractTask repositoryTask = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (obj));
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repositoryTask.getConnectorKind());
                addTaskToSync(client, repositoryTask, tasksToSyncMap);
            } else if (obj instanceof org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) {
                org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer scheduledContainer = ((org.eclipse.mylyn.internal.tasks.core.ScheduledTaskContainer) (obj));
                for (org.eclipse.mylyn.tasks.core.ITask task : scheduledContainer.getChildren()) {
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
                    addTaskToSync(client, task, tasksToSyncMap);
                }
            } else if (obj instanceof org.eclipse.mylyn.internal.tasks.core.Person) {
                org.eclipse.mylyn.internal.tasks.core.Person person = ((org.eclipse.mylyn.internal.tasks.core.Person) (obj));
                for (org.eclipse.mylyn.tasks.core.ITask task : person.getChildren()) {
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
                    addTaskToSync(client, task, tasksToSyncMap);
                }
            } else if (obj instanceof org.eclipse.mylyn.internal.tasks.core.TaskGroup) {
                org.eclipse.mylyn.internal.tasks.core.TaskGroup group = ((org.eclipse.mylyn.internal.tasks.core.TaskGroup) (obj));
                for (org.eclipse.mylyn.tasks.core.ITask task : group.getChildren()) {
                    org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector client = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(task.getConnectorKind());
                    addTaskToSync(client, task, tasksToSyncMap);
                }
            }
        }
        // update queries
        if (!queriesToSyncMap.isEmpty()) {
            // determine which repositories to synch changed tasks for
            java.util.HashMap<org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> repositoriesToSync = new java.util.HashMap<org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>>();
            for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : queriesToSyncMap.keySet()) {
                java.util.List<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queriesToSync = queriesToSyncMap.get(connector);
                if ((queriesToSync == null) || queriesToSync.isEmpty()) {
                    continue;
                }
                for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : queriesToSync) {
                    org.eclipse.mylyn.tasks.core.TaskRepository repos = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(query.getConnectorKind(), query.getRepositoryUrl());
                    if (repos == null) {
                        org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, ("Failed to synchronize query \"" + query.getUrl())// $NON-NLS-1$
                         + "\" because Repository is null"));// $NON-NLS-1$

                        continue;
                    }
                    java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = repositoriesToSync.get(repos);
                    if (queries == null) {
                        queries = new java.util.HashSet<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>();
                        repositoriesToSync.put(repos, queries);
                    }
                    queries.add(query);
                }
            }
            for (java.util.Map.Entry<org.eclipse.mylyn.tasks.core.TaskRepository, java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery>> entry : repositoriesToSync.entrySet()) {
                org.eclipse.mylyn.tasks.core.TaskRepository repository = entry.getKey();
                org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(repository.getConnectorKind());
                java.util.Set<org.eclipse.mylyn.internal.tasks.core.RepositoryQuery> queries = entry.getValue();
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeQueries(connector, repository, queries, null, true);
            }
        }
        // update tasks
        if (!tasksToSyncMap.isEmpty()) {
            for (org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector : tasksToSyncMap.keySet()) {
                java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToSync = tasksToSyncMap.get(connector);
                if ((tasksToSync != null) && (tasksToSync.size() > 0)) {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeTasks(connector, new java.util.HashSet<org.eclipse.mylyn.tasks.core.ITask>(tasksToSync), true, null);
                }
            }
        }
    }

    private void addTaskToSync(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, org.eclipse.mylyn.tasks.core.ITask task, java.util.Map<org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector, java.util.List<org.eclipse.mylyn.tasks.core.ITask>> tasksToSyncMap) {
        if (((connector == null)// 
         || (task instanceof org.eclipse.mylyn.internal.tasks.core.LocalTask))// 
         || (task.getSynchronizationState() == org.eclipse.mylyn.tasks.core.ITask.SynchronizationState.OUTGOING_NEW)) {
            return;
        }
        java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasksToSync = tasksToSyncMap.get(connector);
        if (tasksToSync == null) {
            tasksToSync = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITask>();
            tasksToSyncMap.put(connector, tasksToSync);
        }
        tasksToSync.add(task);
    }

    private org.eclipse.jface.action.IAction action;

    @java.lang.Override
    public void init(org.eclipse.jface.action.IAction action) {
        this.action = action;
    }

    public void init(org.eclipse.ui.IViewPart view) {
        org.eclipse.ui.IActionBars actionBars = view.getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(org.eclipse.ui.actions.ActionFactory.REFRESH.getId(), action);
        actionBars.updateActionBars();
    }
}