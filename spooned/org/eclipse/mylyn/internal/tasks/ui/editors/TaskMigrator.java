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
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskMigrationEvent;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
/**
 *
 * @author Steffen Pingel
 */
public class TaskMigrator {
    private static boolean active;

    private final org.eclipse.mylyn.tasks.core.ITask oldTask;

    private boolean delete;

    private boolean openEditors;

    private org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor;

    private boolean migrateDueDate = true;

    public TaskMigrator(org.eclipse.mylyn.tasks.core.ITask oldTask) {
        this.oldTask = oldTask;
        this.openEditors = true;
    }

    public void setMigrateDueDate(boolean migrateDueDate) {
        this.migrateDueDate = migrateDueDate;
    }

    public boolean openEditors() {
        return openEditors;
    }

    public void setOpenEditors(boolean openEditors) {
        this.openEditors = openEditors;
    }

    public boolean delete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    /**
     * Migrates local properties of <code>oldTask</code> to <code>newTask</code>:
     * <ul>
     * <li>Copy properties
     * <li>Delete old task
     * <li>Reactivate new task
     * <li>Open new task
     * </ul>
     *
     * @param newTask
     * 		the task to migrate properties to
     */
    public void execute(org.eclipse.mylyn.tasks.core.ITask newTask) {
        copyProperties(newTask);
        // invoke participants
        final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector = org.eclipse.mylyn.tasks.ui.TasksUi.getRepositoryConnector(newTask.getConnectorKind());
        if (connector != null) {
            final org.eclipse.mylyn.tasks.core.TaskMigrationEvent event = new org.eclipse.mylyn.tasks.core.TaskMigrationEvent(oldTask, newTask);
            org.eclipse.core.runtime.SafeRunner.run(new org.eclipse.core.runtime.ISafeRunnable() {
                public void handleException(java.lang.Throwable e) {
                    org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error in task migrator: "// $NON-NLS-1$
                     + connector.getClass(), e));
                }

                public void run() throws java.lang.Exception {
                    connector.migrateTask(event);
                }
            });
        }
        try {
            // temporarily disable auto editor management
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator.active = true;
            boolean reactivate = oldTask.isActive();
            if (reactivate) {
                org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().deactivateTask(oldTask);
            }
            boolean editorIsActive = closeEditor();
            deleteOldTask();
            if (reactivate) {
                org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().activateTask(newTask);
            }
            if (openEditors()) {
                if (editorIsActive) {
                    org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(newTask);
                } else {
                    org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openTaskInBackground(newTask, false);
                }
            }
        } finally {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator.active = false;
        }
    }

    public void setEditor(org.eclipse.mylyn.tasks.ui.editors.TaskEditor editor) {
        this.editor = editor;
    }

    public org.eclipse.mylyn.tasks.ui.editors.TaskEditor getEditor() {
        return editor;
    }

    private boolean closeEditor() {
        boolean editorIsActive = false;
        if (editor != null) {
            org.eclipse.ui.IWorkbenchWindow window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                org.eclipse.ui.IWorkbenchPage activePage = window.getActivePage();
                if (activePage != null) {
                    if (activePage.getActiveEditor() == editor) {
                        editorIsActive = true;
                    }
                }
            }
            editor.close(false);
        }
        return editorIsActive;
    }

    private void deleteOldTask() {
        // delete old task details
        if (delete()) {
            org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskList().deleteTask(oldTask);
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().deleteContext(oldTask);
            try {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().deleteTaskData(oldTask);
            } catch (org.eclipse.core.runtime.CoreException e) {
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Failed to delete task data", e));// $NON-NLS-1$

            }
        }
    }

    private void copyProperties(org.eclipse.mylyn.tasks.core.ITask newTask) {
        // migrate task details
        if ((oldTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask) && (newTask instanceof org.eclipse.mylyn.internal.tasks.core.AbstractTask)) {
            ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (newTask)).setNotes(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (oldTask)).getNotes());
            org.eclipse.mylyn.internal.tasks.core.DateRange scheduledDate = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (oldTask)).getScheduledForDate();
            org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setScheduledFor(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (newTask)), scheduledDate);
            java.util.Date dueDate = ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (oldTask)).getDueDate();
            if (migrateDueDate) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskActivityManager().setDueDate(newTask, dueDate);
            }
            ((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (newTask)).setEstimatedTimeHours(((org.eclipse.mylyn.internal.tasks.core.AbstractTask) (oldTask)).getEstimatedTimeHours());
        }
        // migrate context
        org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getContextStore().moveContext(oldTask, newTask);
    }

    public static boolean isActive() {
        return org.eclipse.mylyn.internal.tasks.ui.editors.TaskMigrator.active;
    }
}