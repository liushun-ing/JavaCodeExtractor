/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.commands;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.monitor.ui.MonitorUi;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 *
 * @author Steffen Pingel
 */
public abstract class MarkTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
    public static class ClearOutgoingHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public ClearOutgoingHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
            org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction action = new org.eclipse.mylyn.internal.tasks.ui.actions.ClearOutgoingAction(java.util.Collections.singletonList(((org.eclipse.mylyn.tasks.core.IRepositoryElement) (task))));
            if (action.isEnabled()) {
                action.run();
            }
        }
    }

    public static class ClearActiveTimeHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public ClearActiveTimeHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
            if (org.eclipse.jface.dialogs.MessageDialog.openConfirm(org.eclipse.mylyn.commons.workbench.WorkbenchUtil.getShell(), org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorPlanningPart_Confirm_Activity_Time_Deletion, org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorPlanningPart_Do_you_wish_to_reset_your_activity_time_on_this_task_)) {
                org.eclipse.mylyn.monitor.ui.MonitorUi.getActivityContextManager().removeActivityTime(task.getHandleIdentifier(), 0L, java.lang.System.currentTimeMillis());
            }
        }
    }

    public static class MarkTaskCompleteHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public static final java.lang.String ID_COMMAND = "org.eclipse.mylyn.tasks.ui.command.markTaskComplete";// $NON-NLS-1$


        public MarkTaskCompleteHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
            if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.hasLocalCompletionState(task)) {
                task.setCompletionDate(new java.util.Date());
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementChanged(task);
            }
        }
    }

    public static class MarkTaskIncompleteHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public MarkTaskIncompleteHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.tasks.core.ITask task) throws org.eclipse.core.commands.ExecutionException {
            if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.hasLocalCompletionState(task)) {
                task.setCompletionDate(null);
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList().notifyElementChanged(task);
            }
        }
    }

    public static class MarkTaskReadHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public static final java.lang.String ID_COMMAND = "org.eclipse.mylyn.tasks.ui.command.markTaskRead";// $NON-NLS-1$


        public MarkTaskReadHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(final org.eclipse.core.commands.ExecutionEvent event, final org.eclipse.mylyn.tasks.core.ITask[] tasks) throws org.eclipse.core.commands.ExecutionException {
            org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.markTasksRead(event, tasks, true);
        }
    }

    public static class MarkTaskUnreadHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskHandler {
        public MarkTaskUnreadHandler() {
            setFilterBasedOnActiveTaskList(true);
        }

        @java.lang.Override
        protected void execute(final org.eclipse.core.commands.ExecutionEvent event, final org.eclipse.mylyn.tasks.core.ITask[] tasks) throws org.eclipse.core.commands.ExecutionException {
            org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.markTasksRead(event, tasks, false);
        }
    }

    private static class MarkTaskReadOperation extends org.eclipse.core.commands.operations.AbstractOperation {
        private final org.eclipse.core.runtime.IAdaptable info = new org.eclipse.core.runtime.IAdaptable() {
            @java.lang.SuppressWarnings("rawtypes")
            @java.lang.Override
            public java.lang.Object getAdapter(java.lang.Class adapter) {
                if (adapter == org.eclipse.swt.widgets.Shell.class) {
                    return shell;
                }
                return null;
            }
        };

        private final boolean markRead;

        private java.util.List<org.eclipse.mylyn.tasks.core.ITask> tasks;

        private final org.eclipse.swt.widgets.Shell shell;

        public MarkTaskReadOperation(org.eclipse.swt.widgets.Shell shell, java.lang.String label, boolean markRead, org.eclipse.mylyn.tasks.core.ITask[] tasks) {
            super(label);
            this.shell = shell;
            this.markRead = markRead;
            this.tasks = java.util.Arrays.asList(tasks);
            addContext(org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getUndoContext());
        }

        private void execute() throws org.eclipse.core.commands.ExecutionException {
            org.eclipse.core.commands.operations.IOperationHistory operationHistory = org.eclipse.ui.PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
            operationHistory.execute(this, new org.eclipse.core.runtime.NullProgressMonitor(), info);
        }

        @java.lang.Override
        public org.eclipse.core.runtime.IStatus execute(org.eclipse.core.runtime.IProgressMonitor monitor, org.eclipse.core.runtime.IAdaptable info) throws org.eclipse.core.commands.ExecutionException {
            java.util.List<org.eclipse.mylyn.tasks.core.ITask> affectedTasks = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITask>(tasks.size());
            for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                if (org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().setTaskRead(task, markRead)) {
                    affectedTasks.add(task);
                }
            }
            if (!affectedTasks.containsAll(tasks)) {
                tasks = affectedTasks;
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }

        @java.lang.Override
        public org.eclipse.core.runtime.IStatus undo(org.eclipse.core.runtime.IProgressMonitor monitor, org.eclipse.core.runtime.IAdaptable info) throws org.eclipse.core.commands.ExecutionException {
            for (org.eclipse.mylyn.tasks.core.ITask task : tasks) {
                org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskDataManager().setTaskRead(task, !markRead);
            }
            return org.eclipse.core.runtime.Status.OK_STATUS;
        }

        @java.lang.Override
        public org.eclipse.core.runtime.IStatus redo(org.eclipse.core.runtime.IProgressMonitor monitor, org.eclipse.core.runtime.IAdaptable info) throws org.eclipse.core.commands.ExecutionException {
            return execute(monitor, info);
        }
    }

    public static class MarkTaskReadGoToNextUnreadTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskListViewHandler {
        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.tasks.core.IRepositoryElement item) throws org.eclipse.core.commands.ExecutionException {
            if (item instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (item));
                org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.markTasksRead(event, new org.eclipse.mylyn.tasks.core.ITask[]{ task }, true);
                org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler.execute(event, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.DOWN);
            }
        }
    }

    public static class MarkTaskReadGoToPreviousUnreadTaskHandler extends org.eclipse.mylyn.internal.tasks.ui.commands.AbstractTaskListViewHandler {
        @java.lang.Override
        protected void execute(org.eclipse.core.commands.ExecutionEvent event, org.eclipse.mylyn.internal.tasks.ui.views.TaskListView taskListView, org.eclipse.mylyn.tasks.core.IRepositoryElement item) throws org.eclipse.core.commands.ExecutionException {
            if (item instanceof org.eclipse.mylyn.tasks.core.ITask) {
                org.eclipse.mylyn.tasks.core.ITask task = ((org.eclipse.mylyn.tasks.core.ITask) (item));
                org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.markTasksRead(event, new org.eclipse.mylyn.tasks.core.ITask[]{ task }, true);
                org.eclipse.mylyn.internal.tasks.ui.commands.GoToUnreadTaskHandler.execute(event, org.eclipse.mylyn.internal.tasks.ui.util.TreeWalker.Direction.UP);
            }
        }
    }

    private static void markTasksRead(final org.eclipse.core.commands.ExecutionEvent event, final org.eclipse.mylyn.tasks.core.ITask[] tasks, boolean markRead) throws org.eclipse.core.commands.ExecutionException {
        org.eclipse.swt.widgets.Shell shell = org.eclipse.ui.handlers.HandlerUtil.getActiveShell(event);
        java.lang.String label = (markRead) ? Messages.MarkTaskHandler_MarkTasksReadOperation : Messages.MarkTaskHandler_MarkTasksUnreadOperation;
        org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.MarkTaskReadOperation operation = new org.eclipse.mylyn.internal.tasks.ui.commands.MarkTaskHandler.MarkTaskReadOperation(shell, label, markRead, tasks);
        operation.execute();
    }
}