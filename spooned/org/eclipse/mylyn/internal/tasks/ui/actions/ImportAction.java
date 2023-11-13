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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TransferList;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
/**
 *
 * @author Steffen Pingel
 */
public class ImportAction implements org.eclipse.ui.IViewActionDelegate {
    public static class ImportStatus extends org.eclipse.core.runtime.MultiStatus {
        private org.eclipse.mylyn.internal.tasks.core.TransferList taskList;

        public ImportStatus(java.lang.String pluginId, int code, java.lang.String message, java.lang.Throwable exception) {
            super(pluginId, code, message, exception);
        }

        public org.eclipse.mylyn.internal.tasks.core.TransferList getTaskList() {
            return taskList;
        }
    }

    public void init(org.eclipse.ui.IViewPart view) {
        // ignore
    }

    public void selectionChanged(org.eclipse.jface.action.IAction action, org.eclipse.jface.viewers.ISelection selection) {
        // ignore
    }

    public void run(org.eclipse.jface.action.IAction action) {
        org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell());
        dialog.setText(Messages.ImportAction_Dialog_Title);
        org.eclipse.mylyn.internal.tasks.ui.util.ImportExportUtil.configureFilter(dialog);
        java.lang.String path = dialog.open();
        if (path != null) {
            java.io.File file = new java.io.File(path);
            if (file.isFile()) {
                org.eclipse.core.runtime.IStatus result = org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.importElements(file);
                if (!result.isOK()) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(result);
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.displayStatus(Messages.ImportAction_Dialog_Title, new org.eclipse.core.runtime.MultiStatus(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, 0, new org.eclipse.core.runtime.IStatus[]{ result }, Messages.ImportAction_Problems_encountered, null));
                }
            }
        }
    }

    public static org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.ImportStatus importElements(java.io.File file) {
        org.eclipse.mylyn.internal.tasks.core.TransferList list = new org.eclipse.mylyn.internal.tasks.core.TransferList();
        org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.ImportStatus result = // $NON-NLS-1$
        new org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.ImportStatus(org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants.ID_PLUGIN, 0, "Problems encounted during importing", null);
        result.taskList = list;
        org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer externalizer = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getDefault().createTaskListExternalizer();
        try {
            externalizer.readTaskList(list, file);
        } catch (org.eclipse.core.runtime.CoreException e) {
            result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Problems encountered reading import file", e));// $NON-NLS-1$

        }
        org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
        for (org.eclipse.mylyn.internal.tasks.core.AbstractTask task : list.getAllTasks()) {
            if (!org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.validateRepository(task.getConnectorKind(), task.getRepositoryUrl())) {
                result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Task {0} ignored, unknown connector", task.getSummary())));// $NON-NLS-1$

                continue;
            }
            if (taskList.getTask(task.getHandleIdentifier()) != null) {
                result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Task {0} ignored, already exists in Task List", task.getSummary())));// $NON-NLS-1$

            } else {
                // need to deactivate since activation is managed centrally
                task.setActive(false);
                taskList.addTask(task);
                // TODO support importing of contexts
                // ContextCore.getContextStore().importContext(task.getHandleIdentifier(), zipFile);
            }
        }
        for (org.eclipse.mylyn.internal.tasks.core.RepositoryQuery query : list.getQueries()) {
            if (!org.eclipse.mylyn.internal.tasks.ui.actions.ImportAction.validateRepository(query.getConnectorKind(), query.getRepositoryUrl())) {
                result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Query {0} ignored, unknown connector", query.getSummary())));// $NON-NLS-1$

                continue;
            }
            if (taskList.getQueries().contains(query)) {
                query.setHandleIdentifier(taskList.getUniqueHandleIdentifier());
            }
            taskList.addQuery(query);
            org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepositoryConnector(query.getConnectorKind());
            if (connector != null) {
                org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.synchronizeQuery(connector, query, null, true);
            }
        }
        for (org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory toImport : list.getCategories()) {
            if (toImport instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) {
                java.util.Optional<org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory> match = taskList.getCategories().stream().filter(c -> (c instanceof org.eclipse.mylyn.internal.tasks.core.TaskCategory) && c.equals(toImport)).findFirst();
                org.eclipse.mylyn.internal.tasks.core.TaskCategory category;
                if (match.isPresent()) {
                    category = ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (match.get()));
                    result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.INFO, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, // $NON-NLS-1$
                    org.eclipse.osgi.util.NLS.bind("Category {0} already exists in the Task List, the existing category will be updated instead.", category.getSummary())));
                } else {
                    category = ((org.eclipse.mylyn.internal.tasks.core.TaskCategory) (toImport));
                    taskList.addCategory(category);
                }
                for (org.eclipse.mylyn.tasks.core.ITask task : toImport.getChildren()) {
                    toImport.internalRemoveChild(task);
                    org.eclipse.mylyn.tasks.core.ITask localVersion = taskList.getTask(task.getHandleIdentifier());
                    if (localVersion != null) {
                        taskList.addTask(localVersion, category);
                    }
                }
            } else {
                result.add(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.WARNING, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, org.eclipse.osgi.util.NLS.bind("Category {0} ignored, only Task Categories may be imported.", toImport.getSummary())));// $NON-NLS-1$

            }
        }
        return result;
    }

    private static boolean validateRepository(java.lang.String connectorKind, java.lang.String repositoryUrl) {
        org.eclipse.mylyn.tasks.core.TaskRepository repository = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().getRepository(connectorKind, repositoryUrl);
        if (repository == null) {
            if (org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(connectorKind) == null) {
                return false;
            }
            repository = new org.eclipse.mylyn.tasks.core.TaskRepository(connectorKind, repositoryUrl);
            org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryManager().addRepository(repository);
        }
        return true;
    }
}